package org.anyline.jdbc.prepare;
 
import org.anyline.entity.Compare;
import org.anyline.jdbc.prepare.init.SimpleVariable;
import org.anyline.util.BasicUtil; 
 
public class SyntaxHelper {
 
	/** 
0.[ID=:ID ]				1.[ID=]				2.[:ID]			3.[ ]		 
0.[IN(:TYPE)]			1.[IN(]				2.[:TYPE]		3.[)]		 
0.[= ::SORT ]			1.[=]				2.[::SORT]		3.[ ]		 
0.[':NM%]				1.[']				2.[:NM]			3.[%]		 
0.[+ :CODE ]			1.[+]				2.[:CODE]		3.[ ]		 
0.[+ ::CODE ]			1.[+]				2.[::CODE]		3.[ ]		 
====================== 
0.[ID=${ID} ]			1.[ID=]				2.[${ID}]		3.[ ]		 
0.[IN(${TYPE})]			1.[IN(]				2.[${TYPE}]		3.[)]		 
0.['${SORT}']			1.[']				2.[${SORT}]		3.[']		 
0.['%${NM}%]			1.['%]				2.[${NM}]		3.[%]		 
0.[CONTAT('%',{CODE}]	1.[CONTAT('%',]		2.[{CODE}]		3.[null]		 
 
	   @param signType 1:已:区分 2:已{}区分	 
	 * @param all  all
	 * @param prefix  prefix
	 * @param fullKey  fullKey
	 * @param afterChar  afterChar
	 * @return Variable
	 */ 
	public static Variable buildVariable(int signType, String all, String prefix, String fullKey, String afterChar){
		int varType = -1; 
		Compare compare = Compare.EQUAL;
		if(null == afterChar){ 
			afterChar = ""; 
		} 
		Variable var = new SimpleVariable();
		String key = null; 
		if(signType ==1){ 
			key = fullKey.replace(":", ""); 
		}else if(signType ==2){ 
			key = fullKey.replace("${", "").replace("}", "");
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
