/*  
 * Copyright 2006-2015 www.anyline.org 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 *           
 */ 
 
 
package org.anyline.util; 
 
import java.util.EnumMap; 
import java.util.HashMap; 
import java.util.Map; 
 
public abstract class NumberTextUtil { 
	protected NumberTextUtil() {} 
 
	public abstract String getText(long number); 
	public abstract String getOrdinalText(long number); 
	public static enum Lang { 
		English, EnglishWithCleanSpaceOnly, ChineseSimplified, ChineseTraditional, ; 
	} 
	public static NumberTextUtil getInstance(Lang lang) { 
 
		if (lang == null) 
			throw new NullPointerException(); 
 
		NumberTextUtil result = null; 
		synchronized (_InstancePool) { 
			result = _InstancePool.get(lang); 
			if (result == null) { 
				switch (lang) { 
				case English: 
					result = new NumberTextEnglish(); 
					break; 
				case EnglishWithCleanSpaceOnly: 
					result = new NumberTextEnglishCleanSpaceOnly(); 
					break; 
				case ChineseSimplified: 
					result = new NumberTextChinese( 
							NumberTextChinese.Type.Simplified); 
					break; 
				case ChineseTraditional: 
					result = new NumberTextChinese( 
							NumberTextChinese.Type.Traditional); 
					break; 
				default: 
					throw new UnsupportedOperationException( 
							"Language not supported yet : " + lang); 
				} 
				_InstancePool.put(lang, result); 
			} 
		} 
		return result; 
	} 
 
	private static final Map<Lang, NumberTextUtil> _InstancePool = new EnumMap<Lang, NumberTextUtil>( 
			Lang.class); 
 
	private static boolean checkNumber(String number) { 
 
		return number != null && number.matches("[0-9]+"); 
	} 
 
	/*---------------------------------------------------------------------------- 
	 * English Implementation 
	 ---------------------------------------------------------------------------*/ 
	private static class NumberTextEnglish extends NumberTextUtil { 
 
		static enum Connect { 
 
			Minus("minus"), Hundred("hundred"), And("and"), AfterMinus(" "), AfterNumber( 
					" "), AfterPower(" "), AfterHundred(" "), AfterAnd(" "), AfterTen( 
					"-"), ; 
 
			final String display; 
 
			Connect(String display) { 
				this.display = display; 
			} 
 
			private static boolean isConnect(char c) { 
				return c == ' ' || c == '-'; 
			} 
		} 
 
		static enum Power { 
 
			Thousand("thousand"), // 10 ^ 3 
			Million("million"), // 10 ^ 6 
			Billion("billion"), // 10 ^ 9 
			Trillion("trillion"), // 10 ^ 12 
			Quadrillion("quadrillion"), // 10 ^ 15 
			Quintillion("quintillion"), // 10 ^ 18 (enough for Long.MAX_VALUE) 
			Sextillion("sextillion"), // 10 ^ 21 
			Septillion("septillion"), // 10 ^ 24 
			Octillion("octillion"), // 10 ^ 27 
			Nonillion("nonillion"), // 10 ^ 30 
			Decillion("decillion"), // 10 ^ 33 
			Undecillion("undecillion"), // 10 ^ 36 
			Duodecillion("duodecillion"), // 10 ^ 39 
			Tredecillion("tredecillion"), // 10 ^ 42 
			Quattuordecillion("quattuordecillion"), // 10 ^ 45 
			Quindecillion("quindecillion"), // 10 ^ 48 
			Sexdecillion("sexdecillion"), // 10 ^ 51 
			Septendecillion("septendecillion"), // 10 ^ 54 
			Octodecillion("octodecillion"), // 10 ^ 57 
			Novemdecillion("novemdecillion"), // 10 ^ 60 
			Vigintillion("vigintillion"), // 10 ^ 63 
			; 
 
			final String display; 
 
			Power(String display) { 
				this.display = display; 
			} 
		} 
 
		static enum Digit { 
 
			Zero("zero", "zeroth", "ten", ""), One("one", "first", "eleven", 
					"ten"), Two("two", "second", "twelve", "twenty"), Three( 
					"three", "third", "thirteen", "thirty"), Four("four", 
					"fourth", "fourteen", "fourty"), Five("five", "fifth", 
					"fifteen", "fifty"), Six("six", "sixth", "sixteen", "sixty"), Seven( 
					"seven", "seventh", "seventeen", "seventy"), Eight("eight", 
					"eighth", "eighteen", "eighty"), Nine("nine", "nineth", 
					"nineteen", "ninety"), ; 
 
			final String display, displayOrdinal, plusTen, multiTen; 
 
			Digit(String display, String displayOrdinal, String plusTen, 
					String multiTen) { 
				this.display = display; 
				this.displayOrdinal = displayOrdinal; 
				this.plusTen = plusTen; 
				this.multiTen = multiTen; 
			} 
		} 
 
		private static final Map<String, String> _Ordinals; 
		static { 
			_Ordinals = new HashMap<String, String>(); 
			for (Digit d : Digit.values()) 
				_Ordinals.put(d.display, d.displayOrdinal); 
		} 
 
		@Override 
		public String getText(long number) { 
			StringBuilder builder = new StringBuilder(); 
			buildText(builder, number); 
			return builder.toString(); 
		} 
 
		@Override 
		public String getOrdinalText(long number) { 
			StringBuilder builder = new StringBuilder(); 
			buildText(builder, number); 
			replaceLastTokenWithOrdinal(builder); 
			return builder.toString(); 
		} 
 
		private void buildText(StringBuilder builder, long number) { 
 
			assert builder != null; 
 
			if (number < 0) { 
				builder.append(getConnectDisplay(Connect.Minus)).append( 
						getConnectDisplay(Connect.AfterMinus)); 
				number = -number; 
			} 
 
			String numString = Long.toString(number); 
 
			int power = 0; 
			while (numString.length() > (power + 1) * 3) 
				power++; 
 
			while (power > 0) { 
				boolean modified = extendToken(builder, numString, power * 3); 
				if (modified) 
					builder.append(getConnectDisplay(Connect.AfterNumber)) 
							.append(getPowerDisplay(Power.values()[power - 1])); 
				power--; 
			} 
			extendToken(builder, Long.toString(number), 0); 
		} 
 
		private boolean extendToken(StringBuilder builder, String number, 
				int suffix) { 
 
			assert builder != null && checkNumber(number) 
					&& suffix < number.length(); 
 
			int len = number.length() - suffix; 
			int hundreds = len > 2 ? (int) (number.charAt(len - 3) - '0') : -1; 
			int tens = len > 1 ? (int) (number.charAt(len - 2) - '0') : -1; 
			int inds = (int) (number.charAt(len - 1) - '0'); 
 
			if (hundreds <= 0 && tens <= 0 && inds <= 0 && suffix > 0) 
				return false; 
			else if (len > 3) 
				builder.append(getConnectDisplay(Connect.AfterPower)); 
 
			if (hundreds == 0) { 
				if (len > 3 && (tens > 0 || inds > 0)) 
					builder.append(getConnectDisplay(Connect.And)).append( 
							getConnectDisplay(Connect.AfterAnd)); 
			} else if (hundreds > 0) { 
				builder.append(getDigitName(Digit.values()[hundreds])) 
						.append(getConnectDisplay(Connect.AfterNumber)) 
						.append(getConnectDisplay(Connect.Hundred)); 
				if (tens > 0 || inds > 0) 
					builder.append(getConnectDisplay(Connect.AfterHundred)) 
							.append(getConnectDisplay(Connect.And)) 
							.append(getConnectDisplay(Connect.AfterAnd)); 
			} 
 
			if (tens > 1) { 
				builder.append(getDigitMultiTen(Digit.values()[tens])); 
				if (inds > 0) 
					builder.append(getConnectDisplay(Connect.AfterTen)); 
			} 
 
			if (tens == 1) 
				builder.append(getDigitPlusTen(Digit.values()[inds])); 
			else if (inds > 0 || number.length() == 1) 
				builder.append(getDigitName(Digit.values()[inds])); 
 
			return true; 
		} 
 
		private void replaceLastTokenWithOrdinal(StringBuilder builder) { 
 
			assert builder != null && builder.length() > 0; 
 
			int suffix = builder.length() - 1; 
			while (suffix >= 0 && !isConnect(builder.charAt(suffix))) 
				suffix--; 
			String lastToken = builder.substring(suffix + 1); 
			builder.delete(suffix + 1, builder.length()).append( 
					toOrdinal(lastToken)); 
		} 
 
		String getPowerDisplay(Power power) { 
 
			assert power != null; 
 
			return power.display; 
		} 
 
		String getConnectDisplay(Connect connect) { 
 
			assert connect != null; 
 
			return connect.display; 
		} 
 
		String getDigitName(Digit digit) { 
 
			assert digit != null; 
 
			return digit.display; 
		} 
 
		String toOrdinal(String name) { 
 
			assert name != null && !name.isEmpty(); 
 
			String result = _Ordinals.get(name); 
			if (result == null) { 
				if (name.charAt(name.length() - 1) == 'y') 
					result = name.substring(0, name.length() - 1) + "ieth"; 
				else 
					result = name + "th"; 
			} 
			return result; 
		} 
 
		String getDigitPlusTen(Digit digit) { 
 
			assert digit != null; 
 
			return digit.plusTen; 
		} 
 
		String getDigitMultiTen(Digit digit) { 
 
			assert digit != null; 
 
			return digit.multiTen; 
		} 
 
		boolean isConnect(char c) { 
			return Connect.isConnect(c); 
		} 
	} 
 
	/*---------------------------------------------------------------------------- 
	 * English with only Clean Space Connectors 
	 ---------------------------------------------------------------------------*/ 
	private static class NumberTextEnglishCleanSpaceOnly extends 
			NumberTextEnglish { 
 
		@Override 
		String getConnectDisplay(Connect connect) { 
 
			return connect == Connect.AfterTen ? " " : super 
					.getConnectDisplay(connect); 
		} 
	} 
 
	/*---------------------------------------------------------------------------- 
	 * Chinese Implementation 
	 ---------------------------------------------------------------------------*/ 
	private static class NumberTextChinese extends NumberTextUtil { 
 
		static enum Type { 
			Simplified, Traditional; 
		} 
 
		static enum Connect { 
			Di("第", "第"), Fu("负", "負"), Ling("零", "零"), Shi("十", "拾"), Bai("百", 
					"佰"), Qian("千", "仟"), ; 
 
			final String display, displayTraditional; 
 
			Connect(String display, String displayTraditional) { 
				this.display = display; 
				this.displayTraditional = displayTraditional; 
			} 
		} 
 
		static enum Power { 
 
			Wan("万", "萬"), // 10^4 
			Yi("亿", "億"), // 10^8 
			Zhao("兆", "兆"), // 10^12 
			Jing("京", "京"), // 10^16 (enough for Long.MAX_VALUE) 
			Gai("垓", "垓"), // 10^20 
			Zi("秭", "秭"), // 10^24 
			Rang("穰", "穰"), // 10^28 
			Gou("沟", "溝"), // 10^32 
			Jian("涧", "澗"), // 10^36 
			Zheng("正", "正"), // 10^40 
			Zai("载", "載"), // 10^44 
			; 
 
			final String display, displayTraditional; 
 
			Power(String display, String displayTraditional) { 
				this.display = display; 
				this.displayTraditional = displayTraditional; 
			} 
		} 
 
		static enum Digit { 
 
			Ling("零", "零"), // just to occupy this position 
			Yi("一", "壹"), Er("二", "贰"), San("三", "叁"), Si("四", "肆"), Wu("五", 
					"伍"), Liu("六", "陆"), Qi("七", "柒"), Ba("八", "捌"), Jiu("九", 
					"玖"), ; 
 
			final String display, displayTraditional; 
 
			Digit(String display, String displayTraditional) { 
				this.display = display; 
				this.displayTraditional = displayTraditional; 
			} 
		} 
 
		private final Type type; 
 
		private NumberTextChinese(Type type) { 
			assert type != null; 
 
			this.type = type; 
		} 
 
		@Override 
		public String getText(long number) { 
 
			StringBuilder builder = new StringBuilder(); 
			buildText(builder, number); 
			return builder.toString(); 
		} 
 
		@Override 
		public String getOrdinalText(long number) { 
 
			StringBuilder builder = new StringBuilder().append("Di"); 
			buildText(builder, number); 
			return builder.toString(); 
		} 
 
		private void buildText(StringBuilder builder, long number) { 
 
			assert builder != null; 
 
			if (number < 0) { 
				builder.append(getConnectDisplay(Connect.Fu)); 
				number = -number; 
			} 
			String numString = Long.toString(number); 
 
			int power = 0; 
			while (numString.length() > (power + 1) * 4) 
				power++; 
 
			while (power > 0) { 
				if (extendToken(builder, numString, power * 4)) 
					builder.append(getPowerDisplay(Power.values()[power - 1])); 
				power--; 
			} 
			extendToken(builder, numString, 0); 
		} 
 
		private boolean extendToken(StringBuilder builder, String number, 
				int suffix) { 
 
			assert builder != null && checkNumber(number) 
					&& number.length() > suffix; 
 
			int len = number.length() - suffix; 
			int qian = len > 3 ? (int) (number.charAt(len - 4) - '0') : -1; 
			int bai = len > 2 ? (int) (number.charAt(len - 3) - '0') : -1; 
			int shi = len > 1 ? (int) (number.charAt(len - 2) - '0') : -1; 
			int ind = (int) (number.charAt(len - 1) - '0'); 
 
			boolean nonZero = false; // true if any of the digits is not zero 
			if (qian == 0) { 
				if (bai > 0 || shi > 0 || ind > 0) 
					builder.append(getConnectDisplay(Connect.Ling)); 
			} else if (qian > 0) { 
				builder.append(getDigitDisplay(Digit.values()[qian])).append( 
						getConnectDisplay(Connect.Qian)); 
				nonZero = true; 
			} 
 
			if (bai == 0) { 
				if (qian > 0 && (shi > 0 || ind > 0)) 
					builder.append(getConnectDisplay(Connect.Ling)); 
			} else if (bai > 0) { 
				builder.append(getDigitDisplay(Digit.values()[bai])).append( 
						getConnectDisplay(Connect.Bai)); 
				nonZero = true; 
			} 
 
			if (shi == 0) { 
				if (bai > 0 && ind > 0) 
					builder.append(getConnectDisplay(Connect.Ling)); 
			} else if (shi > 0) { 
				if (number.length() > 2 || shi != 1) 
					builder.append(getDigitDisplay(Digit.values()[shi])); 
				builder.append(getConnectDisplay(Connect.Shi)); 
				nonZero = true; 
			} 
 
			if (ind == 0) { 
				boolean addZero = len == 1; 
				for (int i = 1; addZero && i <= suffix; i++) { 
					if (number.charAt(i) != '0') 
						addZero = false; 
				} 
				if (addZero) 
					builder.append(getConnectDisplay(Connect.Ling)); 
			} else { 
				builder.append(getDigitDisplay(Digit.values()[ind])); 
				nonZero = true; 
			} 
			return nonZero; 
		} 
 
		String getConnectDisplay(Connect connect) { 
 
			assert connect != null; 
 
			return type == Type.Simplified ? connect.display 
					: connect.displayTraditional; 
		} 
 
		String getPowerDisplay(Power power) { 
 
			assert power != null; 
 
			return type == Type.Simplified ? power.display 
					: power.displayTraditional; 
		} 
 
		String getDigitDisplay(Digit digit) { 
 
			assert digit != null; 
 
			return type == Type.Simplified ? digit.display 
					: digit.displayTraditional; 
		} 
	} 
} 
