//给你一个 32 位的有符号整数 x ，返回将 x 中的数字部分反转后的结果。
//
// 如果反转后整数超过 32 位的有符号整数的范围 [−2³¹, 231 − 1] ，就返回 0。
//假设环境不允许存储 64 位整数（有符号或无符号）。
//
//
//
// 示例 1：
//
//
//输入：x = 123
//输出：321
//
//
// 示例 2：
//
//
//输入：x = -123
//输出：-321
//
//
// 示例 3：
//
//
//输入：x = 120
//输出：21
//
//
// 示例 4：
//
//
//输入：x = 0
//输出：0
//
//
//
//
// 提示：
//
//
// -2³¹ <= x <= 2³¹ - 1
//
// Related Topics 数学 👍 3583 👎 0


package editor.cn;

/**
 * 整数反转
 * @author DY
 * @date 2022-08-09 16:41:51
 */
public class P7_ReverseInteger{
	private Solution solution=new P7_ReverseInteger.Solution();

	public static void main(String[] args) {
	 	 //测试代码
	 	 Solution solution = new P7_ReverseInteger().new Solution();
		//solution.reverse(-123);
	 }

//力扣代码
//leetcode submit region begin(Prohibit modification and deletion)
class Solution {
    public int reverse(int x) {
		try {
		if(x==Integer.MIN_VALUE){
			return 0;
		}
		String begin=String.valueOf(x);
		if(x<0){
			begin=begin.substring(1);
		}
		String result="";
		for(int i=0;i<begin.length();i++){
			result=result+begin.charAt(begin.length()-i-1);
		}
		if(x<0){
			result='-'+result;
		}
		return Integer.parseInt(result);
		}
		catch (NumberFormatException e){
			return 0;
		}
    }
}
//leetcode submit region end(Prohibit modification and deletion)

}
