package org.anyline.jdbc.prepare.sql;
 
import org.anyline.jdbc.prepare.Variable;
import org.anyline.jdbc.prepare.RunPrepare.COMPARE_TYPE;
import org.anyline.jdbc.prepare.simple.VariableImpl;
import org.anyline.util.BasicUtil; 
 
public class SQLHelper { 
 
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
		COMPARE_TYPE compare = COMPARE_TYPE.EQUAL;
		if(null == afterChar){ 
			afterChar = ""; 
		} 
		Variable var = new VariableImpl();
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
			//AND CD LIKE '%{CD}%' 
			//AND CD LIKE '%:CD%' 
			varType = Variable.VAR_TYPE_KEY;
			if(prefix.endsWith("%") && afterChar.startsWith("%")){ 
				compare = COMPARE_TYPE.LIKE;
			}else if(prefix.endsWith("%")){ 
				compare = COMPARE_TYPE.LIKE_PREFIX;
			}else if(afterChar.startsWith("%")){ 
				compare = COMPARE_TYPE.LIKE_SUBFIX;
			} 
		}else{ 
			varType = Variable.VAR_TYPE_KEY;
			if(prefix.equalsIgnoreCase("IN") || prefix.equalsIgnoreCase("IN(")){ 
				//AND CD IN({CD}) 
				compare = COMPARE_TYPE.IN;
			} 
		} 
		var.setSignType(signType); 
		var.setKey(key); 
		var.setType(varType); 
		var.setCompare(compare); 
		return var; 
	} 
	public static COMPARE_TYPE parseCompare(int code){
		for (COMPARE_TYPE type : COMPARE_TYPE.values()) { 
			if(type.getCode() == code){ 
				return type; 
			} 
        } 
		return null; 
	} 
	public static COMPARE_TYPE parseCompare(String code){
		if(BasicUtil.isEmpty(code)){ 
			return null; 
		} 
		for (COMPARE_TYPE type : COMPARE_TYPE.values()) { 
			if(code.equals(type.getCode()+"")){ 
				return type; 
			} 
        } 
		return null; 
	} 
} 
