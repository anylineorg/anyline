package org.anyline.data.prepare;
 
import org.anyline.data.prepare.init.DefaultVariable;
import org.anyline.entity.Compare;
import org.anyline.util.BasicUtil;
 
public class SyntaxHelper {
 
	/** 
0.[ID=:ID ]				1.[ID=]				2.[:ID]			3.[ ]		 <br/>
0.[IN(:TYPE)]			1.[IN(]				2.[:TYPE]		3.[)]		 <br/>
0.[= ::SORT ]			1.[=]				2.[::SORT]		3.[ ]		 <br/>
0.[':NM%]				1.[']				2.[:NM]			3.[%]		 <br/>
0.[+ :CODE ]			1.[+]				2.[:CODE]		3.[ ]		 <br/>
0.[+ ::CODE ]			1.[+]				2.[::CODE]		3.[ ]		 <br/>
====================== <br/>
0.[ID=${ID} ]			1.[ID=]				2.[${ID}]		3.[ ]		 <br/>
0.[IN(${TYPE})]			1.[IN(]				2.[${TYPE}]		3.[)]		 <br/>
0.['${SORT}']			1.[']				2.[${SORT}]		3.[']		 <br/>
0.['%${NM}%]			1.['%]				2.[${NM}]		3.[%]		 <br/>
0.[CONTAT('%',{CODE}]	1.[CONTAT('%',]		2.[{CODE}]		3.[null]	 <br/>
 
	   @param signType 1:以:区分 2:以{}区分
	 * @param all  all
	 * @param prefix  prefix CODE = 'A:1' prefix = "A"(因为这个规则，所以吸用来解析SQL体不要用来解析查询条件)
	 * @param fullKey  fullKey
	 * @param afterChar  afterChar
	 * @return Variable
	 */ 
	public static Variable buildVariable(int signType, String all, String prefix, String fullKey, String afterChar){
		int varType = -1;
		if(BasicUtil.isNotEmpty(prefix)){
			//CODE = 'A:1' prefix = "A"
			if(prefix.matches(".*[a-zA-Z0-9]$")){
				return null;
			}
		}
		Compare compare = Compare.EQUAL;
		if(null == afterChar){ 
			afterChar = ""; 
		} 
		Variable var = new DefaultVariable();
		String key = null; 
		if(signType ==1){ 
			key = fullKey.replace(":", ""); 
		}else if(signType ==2){ 
			key = fullKey.replace("${", "").replace("#{", "").replace("{", "").replace("}", "");
		} 
		 
		if(fullKey.startsWith("$") || fullKey.startsWith("::")){ 
			// AND CD = ${CD}  
			// AND CD = ::CD 
			varType = Variable.VAR_TYPE_REPLACE;
		}else if("'".equals(afterChar)){ 
			// AND CD = '{CD}' 
			// AND CD = ':CD' 
			varType = Variable.VAR_TYPE_KEY_REPLACE;
		}else if(prefix.endsWith("%") || afterChar.startsWith("%")){ 
			// AND CD LIKE '%{CD}%' 
			// AND CD LIKE '%:CD%' 
			varType = Variable.VAR_TYPE_KEY;
			if(prefix.endsWith("%") && afterChar.startsWith("%")){ 
				compare = Compare.LIKE;
			}else if(prefix.endsWith("%")){ 
				compare = Compare.LIKE_PREFIX;
			}else if(afterChar.startsWith("%")){ 
				compare = Compare.LIKE_SUFFIX;
			} 
		}else{ 
			varType = Variable.VAR_TYPE_KEY;
			if(prefix.equalsIgnoreCase("IN") || prefix.equalsIgnoreCase("IN(")){ 
				// AND CD IN({CD}) 
				compare = Compare.IN;
			} 
		} 
		var.setSignType(signType); 
		var.setKey(key); 
		var.setType(varType); 
		var.setCompare(compare); 
		return var; 
	} 
	public static Compare parseCompare(int code){
		for (Compare type : Compare.values()) { 
			if(type.getCode() == code){ 
				return type; 
			} 
        } 
		return null; 
	} 
	public static Compare parseCompare(String code){
		if(BasicUtil.isEmpty(code)){ 
			return null; 
		} 
		for (Compare type : Compare.values()) { 
			if(code.equals(type.getCode()+"")){ 
				return type; 
			} 
        } 
		return null; 
	} 
} 
