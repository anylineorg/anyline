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

import org.anyline.metadata.type.DatabaseType;

import java.util.List;

public class CommandException extends RuntimeException {
	protected DatabaseType database;
	protected String datasource;
	protected Exception src;
	protected String cmd;
	protected List<Object> values;
	public CommandException() {
		super(); 
	}
	public CommandException(String title) {
		super(title);
	}
	public CommandException(String title, Exception src) {
		super(title, src);
		if(null != src) {
			super.setStackTrace(src.getStackTrace());
		}
	}

	public DatabaseType getDatabase() {
		return database;
	}

	public void setDatabase(DatabaseType database) {
		this.database = database;
	}

	public String getDatasource() {
		return datasource;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
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

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}
}
