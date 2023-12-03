
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


package org.anyline.exception;

import java.util.List;

public class SQLException extends RuntimeException{
	protected Exception src;
	protected String sql;
	protected List<Object> values;
	public SQLException(){
		super(); 
	}
	public SQLException(String title){
		super(title);
	}
	public SQLException(String title, Exception src){
		super(title, src);
		if(null != src) {
			super.setStackTrace(src.getStackTrace());
		}
	}

	public Exception getSrc() {
		return src;
	}

	public void setSrc(Exception src) {
		if(null != src) {
			super.setStackTrace(src.getStackTrace());
		}
		this.src = src;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}
}
