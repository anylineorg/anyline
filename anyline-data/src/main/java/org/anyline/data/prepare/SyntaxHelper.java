/*
 * Copyright 2006-2023 www.anyline.org
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
 */



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
0.[CONTAT('%', {CODE}]	1.[CONTAT('%', ]		2.[{CODE}]		3.[null]	 <br/>
 
	   @param signType 1:以:区分 2:以{}区分
	 * @param all  all
	 * @param prefix  prefix CODE = 'A:1' prefix = "A"(因为这个规则，所以只用来解析SQL体不要用来解析查询条件)
	 * @param fullKey  fullKey
	 * @param afterChar  afterChar
	 * @return Variable
	 */ 
	public static Variable buildVariable(int signType, String all, String prefix, String fullKey, String afterChar) {
		int varType = -1;
		if(null != prefix && null != fullKey) {
			if(prefix.endsWith(":") && fullKey.startsWith(":")) {
				//IN(::IDS) > IN(:, :IDS
				prefix = prefix.substring(0, prefix.length()-1);
				fullKey = ":"+fullKey;
			}
		}
		if(BasicUtil.isNotEmpty(prefix)) {
			//CODE = 'A:1' prefix = "A"
			//CODE = 'IN:1' prefix = "IN"
			//如果遇到以上情况 应该用#{}${}占位
			//CODE IN(:CODES)  prefix = "IN("
			//CODE IN (:CODES)  prefix = "IN" afterChar =")"
			//LIMIT :LIMIT OFFSET :OFFSET
			/*if(fullKey.startsWith(":")
					&& !fullKey.startsWith("::")
					&& prefix.matches(".*[a-zA-Z0-9]$")
					&& !")".equals(afterChar)
			) {
				return null;
			}*/
		}
		Compare compare = Compare.EQUAL;
		if(null == afterChar) {
			afterChar = ""; 
		} 
		Variable var = new DefaultVariable();
		 
		if(fullKey.startsWith("$") || fullKey.startsWith("::")) {
			// AND CD = ${CD}  
			// AND CD = ::CD 
			varType = Variable.VAR_TYPE_REPLACE;
		}else if(fullKey.startsWith("#")) {
			// AND CD = #{CD}
			if("'".equals(afterChar)) {
				// AND CD = '#{CD}'
				varType = Variable.VAR_TYPE_KEY_REPLACE;
			}else {
				varType = Variable.VAR_TYPE_KEY;
			}
		}else if("'".equals(afterChar)) {
			// AND CD = '#{CD}'
			// AND CD = '${CD}'
			// AND CD = ':CD' 
			varType = Variable.VAR_TYPE_KEY_REPLACE;
		}else if(prefix.endsWith("%") || afterChar.startsWith("%")) {
			// AND CD LIKE '%{CD}%' 
			// AND CD LIKE '%:CD%' 
			varType = Variable.VAR_TYPE_KEY;
			if(prefix.endsWith("%") && afterChar.startsWith("%")) {
				compare = Compare.LIKE;
			}else if(prefix.endsWith("%")) {
				compare = Compare.LIKE_PREFIX;
			}else if(afterChar.startsWith("%")) {
				compare = Compare.LIKE_SUFFIX;
			} 
		}else{
			varType = Variable.VAR_TYPE_KEY;
		}
		if(prefix.equalsIgnoreCase("IN") || prefix.equalsIgnoreCase("IN(")) {
			//  AND CD IN(#{CD})  AND CD IN(${CD}) AND CD IN(:CD)
			compare = Compare.IN;
		}
		var.setFullKey(fullKey);
		var.setSignType(signType);
		var.setType(varType); 
		var.setCompare(compare); 
		return var; 
	} 
	public static Compare parseCompare(int code) {
		for (Compare type : Compare.values()) {
			if(type.getCode() == code) {
				return type; 
			} 
        } 
		return null; 
	} 
	public static Compare parseCompare(String code) {
		if(BasicUtil.isEmpty(code)) {
			return null; 
		} 
		for (Compare type : Compare.values()) {
			if(code.equals(type.getCode()+"")) {
				return type; 
			} 
        } 
		return null; 
	} 
} 
