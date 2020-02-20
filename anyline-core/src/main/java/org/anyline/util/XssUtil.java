package org.anyline.util; 
 
import java.util.ArrayList; 
import java.util.List; 
import java.util.regex.Matcher; 
import java.util.regex.Pattern; 
 
public class XssUtil { 
	private static List<Pattern> patterns = new ArrayList<Pattern>(); 
	static { 
		List<Object[]> regexps = new ArrayList<Object[]>(); 
		regexps.add(new Object[] { "<(no)?script[^>]*>.*?</(no)?script>", Pattern.CASE_INSENSITIVE }); 
		regexps.add(new Object[] { "eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL });  
		regexps.add(new Object[] { "expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL }); 
		regexps.add(new Object[] { "(javascript:|vbscript:|view-source:)*", Pattern.CASE_INSENSITIVE }); 
		regexps.add(new Object[] { "<(no)?iframe[^>]*>.*?</(no)?iframe>", Pattern.CASE_INSENSITIVE }); 
		regexps.add(new Object[] { "<(no)?iframe[^>]*>", Pattern.CASE_INSENSITIVE }); 
		//regexps.add(new Object[] { "<(\"[^\"]*\"|\'[^\']*\'|[^\'\">])*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL }); 
		regexps.add(new Object[] {"(window\\.location|window\\.|\\.location|document\\.cookie|document\\.|alert\\(.*?\\)|window\\.open\\()*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL }); 
		regexps.add(new Object[] {"<+\\s*\\w*\\s*(oncontrolselect|oncopy|oncut|ondataavailable|ondatasetchanged|ondatasetcomplete|ondblclick|ondeactivate|ondrag|ondragend|ondragenter|ondragleave|ondragover|ondragstart|ondrop|onerror=|onerroupdate|onfilterchange|onfinish|onfocus|onfocusin|onfocusout|onhelp|onkeydown|onkeypress|onkeyup|onlayoutcomplete|onload|onlosecapture|onmousedown|onmouseenter|onmouseleave|onmousemove|onmousout|onmouseover|onmouseup|onmousewheel|onmove|onmoveend|onmovestart|onabort|onactivate|onafterprint|onafterupdate|onbefore|onbeforeactivate|onbeforecopy|onbeforecut|onbeforedeactivate|onbeforeeditocus|onbeforepaste|onbeforeprint|onbeforeunload|onbeforeupdate|onblur|onbounce|oncellchange|onchange|onclick|oncontextmenu|onpaste|onpropertychange|onreadystatechange|onreset|onresize|onresizend|onresizestart|onrowenter|onrowexit|onrowsdelete|onrowsinserted|onscroll|onselect|onselectionchange|onselectstart|onstart|onstop|onsubmit|onunload)+\\s*=+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL }); 
 
		String regex = null; 
		Integer flag = null; 
		int arrLength = 0; 
		for (Object[] arr : regexps) { 
			arrLength = arr.length; 
			for (int i = 0; i < arrLength; i++) { 
				regex = (String) arr[0]; 
				flag = (Integer) arr[1]; 
				patterns.add(Pattern.compile(regex, flag)); 
			} 
		} 
	} 
	/** 
	 * 过滤字符 
	 * @param value  value
	 * @return return
	 */ 
	public static String strip(String value) { 
		if (BasicUtil.isNotEmpty(value)) { 
			Matcher matcher = null; 
			for (Pattern pattern : patterns) { 
				matcher = pattern.matcher(value); 
				if (matcher.find()) { 
					value = matcher.replaceAll(""); 
				} 
			} 
			//value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;"); 
		} 
		return value; 
	} 
 
	/** 
	 * 检测是否存在非法字符 通过(没有非法字条)返回true 
	 * @param value  value
	 * @return return
	 */ 
	public static boolean check(String value) { 
		if (BasicUtil.isNotEmpty(value)) { 
			Matcher matcher = null; 
			for (Pattern pattern : patterns) { 
				matcher = pattern.matcher(value); 
				if (matcher.find()) { 
					return false; 
				} 
			} 
		} 
		return true; 
	} 
	 
} 
