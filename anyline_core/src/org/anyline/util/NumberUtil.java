
package org.anyline.util;


public class NumberUtil {
	public static String toUpper(String num){
		String result = "";
		char[] array = new char[num.length()];
		num.getChars(0,num.length(), array, 0);
		int k = -1;
		/**
		 * 将array数组中遇到的第一个非零数字，赋值给k
		 */
		//ArrayList<String> list = new ArrayList<String>();
		for(int i = 0; i < array.length; i++)
		{
			
			if('0' == array[i])
			{
				continue;
			}
			k = i;	
			
			break;
		}
		/**
		 * 输入全是0时，直接输出零，结束程序
		 */
		
		if(k == -1)
		{
			return "零";
		}
		
		/**
		 * 将非零开始的数字专为中文大写
		 */
		int n = array.length - k;//数字所在的数位
		result += change(array[k],'0', n,true);
		for(; k < array.length - 1; k++)
		{
			 n = array.length - k - 1;
			result += change(array[k + 1],array[k], n,false);
		}
		
		return result;
	}

	/**
	 * 
	 * @param ch每一位的数字
	 * @param last，ch的前一位数字
	 * @param n数字所在的数位
	 * @param m是否是数字的非零第一位
	 */
	private static String change(char ch,char last, int n,boolean m)
	{
		
		if(true == m)
		{
			switch(n)
			{
			case 1:return finger(ch);
			case 2:return finger(ch) + "十";
			case 3:return finger(ch) + "百";
			case 4:return finger(ch) + "千";
			case 5:return finger(ch) + "万";
			case 6:return finger(ch) + "十";
			case 7:return finger(ch) + "百";
			case 8:return finger(ch) + "千";
			}
			return "";
		}
		if('0' == last)
		{
			if('0' == ch)
			{
				return "";
			}
			switch(n)
			{
			case 1:return "零" + finger(ch);
			case 2:return "零" + finger(ch) + "十";
			case 3:return "零" + finger(ch) + "百";
			case 4:return "零" + finger(ch) + "千";
			case 5:return "零" + finger(ch) + "万";
			case 6:return "零" + finger(ch) + "十";
			case 7:return "零" + finger(ch) + "百";
			case 8:return "零" + finger(ch) + "千";
			}
		}
		if('0' != last)
		{
			if('0' == ch)
			{
				return "";
			}
			switch(n)
			{
			case 1:return finger(ch);
			case 2:return finger(ch) + "十";
			case 3:return finger(ch) + "百";
			case 4:return finger(ch) + "千";
			case 5:return finger(ch) + "万";
			case 6:return finger(ch) + "十";
			case 7:return finger(ch) + "百";
			case 8:return finger(ch) + "千";
			}
		}
		return "";
	}
	private static String finger(char ch)
	{
		switch(ch)
		{
		case '0': return "零"; 
		case '1': return "一"; 
		case '2': return "二"; 
		case '3': return "三"; 
		case '4': return "四"; 
		case '5': return "五"; 
		case '6': return "六"; 
		case '7': return "七"; 
		case '8': return "八"; 
		case '9': return "九"; 
		}
		return null;
	}

}
