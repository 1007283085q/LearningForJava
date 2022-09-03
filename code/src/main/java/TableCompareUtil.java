
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 * 类说明：数据库库表比对工具
 * 如果发现GROUP_CONCAT函数执行出来的长度不一致, 请在数据库执行SET GLOBAL group_concat_max_len = 102400
 * <p>
 * 类名称: TableCompareUtil.java
 *
 * @version v1.0.0
 * @ur: TODO
 * @date 2022/5/13 14:38
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 */
@Slf4j
public class TableCompareUtil {

    private static final String TMP_SQL = "SELECT table_name AS tableName, GROUP_CONCAT(column_name) AS columnName, GROUP_CONCAT(column_type separator '#') AS columnType , GROUP_CONCAT(IF(column_comment = '', '无', column_comment) separator '#') AS columnComment FROM information_schema.`COLUMNS` WHERE table_schema = '#schemeName#' GROUP BY tableName ORDER BY tableName;";

    public static StringBuffer[] message = {
            new StringBuffer().append("\r\n"),
            new StringBuffer().append("\r\n"),
            new StringBuffer().append("\r\n"),
            new StringBuffer().append("\r\n"),
            new StringBuffer().append("\r\n"),
            new StringBuffer().append("\r\n")
    };

    public static void main(String[] args) throws Exception {
        DataBaseInfo prodDataBaseInfo = new DataBaseInfo("com.mysql.jdbc.Driver", "jdbc:mysql://172.21.96.51:3306/pms_hql", "root", "root@abc123", "脚本库", "pms_core_dev");
        DataBaseInfo devDataBaseInfo = new DataBaseInfo("com.mysql.jdbc.Driver", "jdbc:mysql://172.21.96.51:3306/pms_core_dev", "root", "root@abc123", "测试库", "pms_hql");
        compareTables(prodDataBaseInfo, devDataBaseInfo);
        writeFile("C:\\My");
    }

    /**
     * 比较统一版本库和开发库的数据表，包括表名、字段名、字段类型、字段注解
     *
     * @param prodDataBaseInfo 统一版本库信息
     * @param devDataBaseInfo  开发库信息
     * @throws Exception
     */
    public static void compareTables(DataBaseInfo prodDataBaseInfo, DataBaseInfo devDataBaseInfo) throws Exception {
        /**
         * 1. 初始化提示信息
         */
        initMessage();

        /**
         * 2. 加载数据库表
         */
        Map<String, TableInfo> prodTableInfo = getTableInfo(prodDataBaseInfo);

        Map<String, TableInfo> devTableInfo = getTableInfo(devDataBaseInfo);


        log.info("START----开始比对开发库");
        // 遍历开发库Map
        for (Iterator<String> iter_table = devTableInfo.keySet().iterator(); iter_table
                .hasNext(); ) {
            String tableName = iter_table.next();
            TableInfo devTable = devTableInfo.get(tableName);// 获得开发库中的表
            TableInfo prodTable = prodTableInfo.get(tableName);// 尝试从统一版本库中获得同名表
            if (prodTable == null) { // 如果获得表为空，说明开发存在，统一版本不存在
                append(devTable, null, 2);
            }
            else { // 表相同，判断字段、字段类型、字段注解
                for (Iterator<String> column_develop = devTable.column.keySet().iterator(); column_develop
                        .hasNext(); ) {
                    String key_column = column_develop.next();
                    String value_develop = devTable.column.get(key_column);// 获得开发库中的列
                    String value_column = prodTable.column.get(key_column);// 尝试从统一版本库中获得同名列
                    if (value_column == null) {// 如果列名为空，说明开发存在，统一版本不存在
                        append(devTable, key_column, 4);
                    }
                    else {// 说明两者都存在
                        // 字段类型不一致
                        if (!value_column.equals(value_develop)) {
                            append(devTable, key_column, 5);
                        }

                        String comment_product = prodTable.columnComment.get(key_column);// 获得统一版本库中的字段注解
                        String develop_product = devTable.columnComment.get(key_column);// 尝试从开发库中获得同名字段注解
                        if (!comment_product.equals(develop_product))// 字段注解不一致
                        {
                            append(devTable, key_column, 6);
                        }
                    }
                }
            }
        }
        log.info("END----结束比对开发库");

        log.info("START----开始比对统一版本库");
        // 遍历统一版本库Map
        for (Iterator<String> iter_table = prodTableInfo.keySet().iterator(); iter_table
                .hasNext(); ) {
            String tableName = iter_table.next();
            TableInfo prodTable = prodTableInfo.get(tableName);// 从统一版本库中获得同名表
            TableInfo devTable = devTableInfo.get(tableName);// 尝试获得开发库中的表
            if (devTable == null) { // 如果获得表为空，说明统一版本存在，开发不存在
                append(prodTable, null, 1);
            }
            else { // 表相同，判断字段、字段类型、字段注解
                for (Iterator<String> column_product = prodTable.getColumn().keySet().iterator(); column_product
                        .hasNext(); ) {
                    String key_column = column_product.next();
                    String value_develop = devTable.column.get(key_column);// 尝试从开发库中获得同名列
                    if (value_develop == null) {// 如果列名为空，说明统一版本存在，开发不存在
                        append(devTable, key_column, 3);
                    }
                    // 字段相等 类型或者注解不相等上一步已经比对过，这里无需重复比对。
                }
            }
        }
        log.info("END----结束比对统一版本库");

    }

    /**
     * 封装输出数据
     *
     * @param tableInfo
     * @param column
     * @param flag
     */
    public static void append(TableInfo tableInfo, String column, int flag) {
        switch (flag) {
            case 1:
                message[0].append(tableInfo.getTableName() + "\r\n");
                break;
            case 2:
                message[1].append(tableInfo.getTableName() + "\r\n");
                break;
            case 3:
                message[2].append(tableInfo.getTableName() + "[" + column + "]\r\n");
                break;
            case 4:
                message[3].append(tableInfo.getTableName() + "[" + column + "]\r\n");
                break;
            case 5:
                message[4].append(tableInfo.getTableName() + "[" + column + "][" + tableInfo.column.get(column) + "]\r\n");
                break;
            case 6:
                message[5].append(tableInfo.getTableName() + "[" + column + "][" + tableInfo.columnComment.get(column) + "]\r\n");
                break;
        }
    }

    /**
     * 输出结果到文件
     *
     * @param path
     * @throws Exception
     */
    public static void writeFile(String path) throws Exception {
        /**
         * 合并输出到文件
         */
        StringBuffer rs = new StringBuffer();
        Arrays.stream(message).forEach(s -> rs.append(s).append("\r\n\r\n"));
        StringBuffer fileName = new StringBuffer().append(path);
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        fileName.append("TABLE_COMPARE_RESULT.txt");
        File file = new File(fileName.toString());
        OutputStream os = new FileOutputStream(file);
        log.info("...比对文件生成成功");
        os.write(rs.toString().getBytes());
        os.flush();
        os.close();
    }


    /**
     * 加载表结构
     *
     * @param dataBaseInfo
     * @return
     * @throws Exception
     */
    public static Map<String, TableInfo> getTableInfo(DataBaseInfo dataBaseInfo) throws Exception {
        /**
         * 1. 创建数据库链接
         */
        Connection conn = createConnection(dataBaseInfo);
        Statement statement = conn.createStatement();
        String sql = TMP_SQL.replace("#schemeName#", dataBaseInfo.getSchemeName());
        ResultSet rs = statement.executeQuery(sql);
        Map<String, TableInfo> map = new HashMap<>(16);
        while (rs.next()) {
            TableInfo tableInfo = new TableInfo();
            Map<String, String> columnMap = new HashMap<>(16);
            Map<String, String> columnCommentMap = new HashMap<>(16);
            // 设置属性
            tableInfo.setTableName(rs.getString("tableName"));
            String columnName = rs.getString("columnName");
            String columnType = rs.getString("columnType");
            String columnComment = rs.getString("columnComment");
            String[] columnNameArr = columnName.split(",");
            String[] columnTypeArr = columnType.split("#");
            String[] columnCommentArr = columnComment.split("#", -1);
            for (int i = 0; i < columnNameArr.length; i++) {
                columnMap.put(columnNameArr[i], columnTypeArr[i]);
                columnCommentMap.put(columnNameArr[i], columnCommentArr[i]);
            }
            tableInfo.setColumn(columnMap);
            tableInfo.setColumnComment(columnCommentMap);
            map.put(tableInfo.getTableName(), tableInfo);
        }
        if (rs != null) {
            rs.close();
        }
        if (conn != null) {
            conn.close();
        }
        return map;
    }

    /**
     * 初始化 比对文件提示信息
     */
    public static void initMessage() {
        message[0].append("1、测试库存在，脚本库不存在的表：\r\n");
        message[1].append("2、测试库不存在，脚本库存在的表：\r\n");
        message[2].append("3、测试库存在，脚本库不存在的字段：\r\n");
        message[3].append("4、测试库不存在，脚本库存在的字段：\r\n");
        message[4].append("5、表和字段都相同，但字段类型不同的内容：\r\n");
        message[5].append("6、表和字段、字段类型都相同，但字段注解不同的内容：\r\n");
    }

    /**
     * 创建数据库链接
     *
     * @param dataBaseInfo
     * @return
     */
    public static Connection createConnection(DataBaseInfo dataBaseInfo) throws Exception {
        //       Assert.isNull(dataBaseInfo, "数据库信息不能为空");
//        Class.forName(dataBaseInfo.getDriverClassName());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataBaseInfo.getDataSource());
        Connection conn = jdbcTemplate.getDataSource().getConnection();
//        Connection conn = DriverManager.getConnection(dataBaseInfo.getUrl(), dataBaseInfo.getUserName(), dataBaseInfo.getPassWord());
        if (!ObjectUtils.isEmpty(conn)) {
            log.info("{}加载成功...", dataBaseInfo.getDesc());
        }
        return conn;
    }

    /**
     * 表属性
     */
    @Data
    static class TableInfo {
        /**
         * 表名
         */
        private String tableName;

        /**
         * 字段名称+字段类型
         */
        private Map<String, String> column;

        /**
         * 字段名称+字段注解
         */
        private Map<String, String> columnComment;
    }

    /**
     * 数据库属性
     */
    @Data
    static class DataBaseInfo {
        /**
         * 驱动类名称
         */
        private String driverClassName;

        /**
         * 数据库连接URL
         */
        private String url;

        /**
         * 数据库连接用户名
         */
        private String userName;

        /**
         * 数据库连接密码
         */
        private String passWord;

        /**
         * 数据库描述
         */
        private String desc;

        /**
         * 数据库名称
         */
        private String schemeName;

        public DataBaseInfo(String driverClassName, String url, String userName, String passWord, String desc, String schemeName) {
            this.driverClassName = driverClassName;
            this.url = url;
            this.userName = userName;
            this.passWord = passWord;
            this.desc = desc;
            this.schemeName = schemeName;
        }

        public DataSource getDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(this.driverClassName);
            dataSource.setUrl(this.url);
            dataSource.setPassword(this.passWord);
            dataSource.setUsername(this.userName);
            return dataSource;
        }

        public Map<Object, Object> getInfo() {
            Map<Object, Object> map = new HashMap<>(3);
            map.put("url", this.url);
            map.put("username", this.userName);
            map.put("password", this.passWord);
            return map;
        }

    }

}
