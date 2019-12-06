package org.anyline.config.db; 
 
import org.anyline.config.db.SQL.COMPARE_TYPE; 
import org.anyline.config.db.impl.SQLVariableImpl; 
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
	 * @return return
	 */ 
	public static SQLVariable buildVariable(int signType, String all, String prefix, String fullKey, String afterChar){ 
		int varType = -1; 
		COMPARE_TYPE compare = SQL.COMPARE_TYPE.EQUAL; 
		if(null == afterChar){ 
			afterChar = ""; 
		} 
		SQLVariable var = new SQLVariableImpl(); 
		String key = null; 
		if(signType ==1){ 
			key = fullKey.replace(":", ""); 
		}else if(signType ==2){ 
			key = fullKey.replace("$", "").replace("{", "").replace("}", ""); 
		} 
		 
		if(fullKey.startsWith("$") || fullKey.startsWith("::")){ 
			// AND CD = ${CD}  
			// AND CD = ::CD 
			varType = SQLVariable.VAR_TYPE_REPLACE; 
		}else if("'".equals(afterChar)){ 
			// AND CD = '{CD}' 
			// AND CD = ':CD' 
			varType = SQLVariable.VAR_TYPE_KEY_REPLACE; 
		}else if(prefix.endsWith("%") || afterChar.startsWith("%")){ 
			//AND CD LIKE '%{CD}%' 
			//AND CD LIKE '%:CD%' 
			varType = SQLVariable.VAR_TYPE_KEY; 
			if(prefix.endsWith("%") && afterChar.startsWith("%")){ 
				compare = SQL.COMPARE_TYPE.LIKE; 
			}else if(prefix.endsWith("%")){ 
				compare = SQL.COMPARE_TYPE.LIKE_PREFIX; 
			}else if(afterChar.startsWith("%")){ 
				compare = SQL.COMPARE_TYPE.LIKE_SUBFIX; 
			} 
		}else{ 
			varType = SQLVariable.VAR_TYPE_KEY; 
			if(prefix.equalsIgnoreCase("IN") || prefix.equalsIgnoreCase("IN(")){ 
				//AND CD IN({CD}) 
				compare = SQL.COMPARE_TYPE.IN; 
			} 
		} 
		var.setSignType(signType); 
		var.setKey(key); 
		var.setType(varType); 
		var.setCompare(compare); 
		return var; 
	} 
	public static SQL.COMPARE_TYPE parseCompare(int code){ 
		for (COMPARE_TYPE type : COMPARE_TYPE.values()) { 
			if(type.getCode() == code){ 
				return type; 
			} 
        } 
		return null; 
	} 
	public static SQL.COMPARE_TYPE parseCompare(String code){ 
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
