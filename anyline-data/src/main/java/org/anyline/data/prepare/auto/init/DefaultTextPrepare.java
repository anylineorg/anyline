/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.prepare.auto.init;

import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.SyntaxHelper;
import org.anyline.data.prepare.auto.TextPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.TextRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;

import java.util.List;

public class DefaultTextPrepare extends DefaultAutoPrepare implements TextPrepare {
	private String up;
	private String order;
	private String having;
	private String group;
	private String where;
	private Integer limit;
	private Integer offset;

	public DefaultTextPrepare(String text) {
		super(); 
		this.text = text;
		chain = new DefaultAutoConditionChain();
		split();
	}
	public String getText() {
		return this.text; 
	}

	@Override
	public Run build(DataRuntime runtime) {
		TextRun run = new TextRun();
		run.setPrepare(this);
		run.setRuntime(runtime);
		if(null != orders){
			run.setOrders(orders);
		}
		if(null != group){
			GroupStore groups = run.getGroups();
			groups.add(group);
		}
		if(null != having){
			run.having(having);
		}
		if(null != where){
			run.addCondition(where);
		}
		if(null != limit || null != offset){
			PageNavi navi = run.getPageNavi();
			if(null == navi){
				navi = new DefaultPageNavi();
				run.setPageNavi(navi);
			}
			if(null != offset){
				navi.limit(offset, limit);
			}else{
				navi.limit(0, limit);
			}
		}

		return run;
	}

	@Override
	public DataRow map(boolean empty, boolean join) {
		DataRow row = new OriginRow();
		row.put("text", text);
		return row;
	}

	@Override
	public RunPrepare setContent(String content) {
		this.text = content;
		return this;
	}

	private void split(){
		if(text.contains(";")){
			//可能是多个SQL 不解析
			return;
		}
		text = text.replaceAll("\\s{2,}", " ");
		up = text.toUpperCase();
		String page = split("LIMIT");
		if(BasicUtil.isNotEmpty(page)){
			page = page.toUpperCase();
			if(page.contains("OFFSET")){
				//limit 10 offset 0
				String[] tmps = page.split("OFFSET");
				if(tmps.length == 2){
					limit = BasicUtil.parseInt(tmps[0].trim(), null);
					offset = BasicUtil.parseInt(tmps[1].trim(), null);
				}
			}else{
				if(page.contains(",")){
					//limit 0,10
					String[] tmps = page.split(",");
					if(tmps.length == 2){
						offset = BasicUtil.parseInt(tmps[0], null);
						limit = BasicUtil.parseInt(tmps[1], null);
					}
				}else{
					//limit 10
					limit = BasicUtil.parseInt(page, null);
				}
			}

		}
		order = split("ORDER BY");
		if(BasicUtil.isNotEmpty(order)) {
			orders.add(order);
		}
		having = split("HAVING");
		group = split("GROUP BY");
		where = split("WHERE");
		if(null != where && isPlaceholder(where)){
			// ${KEY} ::KEY是占位符,需要整体替换成参数值,不能拆分成查询条件(拆分后会与${原生SQL}混淆,被当成原生SQL原样拼接或k:v查询条件解析)
			text = text + " WHERE " + where;
			up = up + " WHERE " + where.toUpperCase();
			where = null;
		}
	}
	/**
	 * 是否是 ${KEY} ::KEY 形式的占位符(而不是${原生SQL})
	 * ${KEY} ::KEY:整体替换成参数值   ${ID > :ID}:把内容原样拼接到SQL
	 * @param txt txt
	 * @return boolean
	 */
	private boolean isPlaceholder(String txt){
		if(null == txt){
			return false;
		}
		String body = null;
		if(BasicUtil.checkEl(txt)){
			// ${KEY}
			body = txt.substring(2, txt.length()-1).trim();
		}else if(txt.startsWith("::")){
			// ::KEY 与 ${KEY} 一样是整体替换
			body = txt.substring(2).trim();
		}
		if(null != body && body.matches("\\w+")){
			//排除 '2023-01-01'::date 这种类型转换
			return !SyntaxHelper.checkType(body);
		}
		return false;
	}
	private String split(String type){
		String key = null;
		try {
			List<String> keys = RegularUtil.fetch(up, "\\s" + type + "\\s");
			if(!keys.isEmpty()){
				key = keys.get(0);
			}
		}catch (Exception e){

		}
		if(null == key){
			return null;
		}
		int idx = up.lastIndexOf(key);
		if(idx != -1) {
			String chk = up.substring(idx);
			if (BasicUtil.charCount(chk, "(") == BasicUtil.charCount(chk, ")")) {
				if (BasicUtil.charCount(chk, "'")%2 == 0) {
					up = up.substring(0, idx);
					String result = text.substring(idx + key.length());
					text = text.substring(0, idx);
					return result;
				}
			}
		}
		return null;
	}
}