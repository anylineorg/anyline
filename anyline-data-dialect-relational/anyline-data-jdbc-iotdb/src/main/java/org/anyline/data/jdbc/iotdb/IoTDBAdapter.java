 
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


package org.anyline.data.jdbc.iotdb;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.MySQLGenusAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.iotdb")
public class IoTDBAdapter extends MySQLGenusAdapter implements JDBCAdapter, InitializingBean {

	public DatabaseType type(){
		return DatabaseType.IoTDB;
	}


	@Value("${anyline.data.jdbc.delimiter.iotdb:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}

	public IoTDBAdapter(){
		super();
		delimiterFr = "`";
		delimiterTo = "`";
		for (IoTDBTypeMetadataAlias alias: IoTDBTypeMetadataAlias.values()){
			reg(alias);
			alias(alias.name(), alias.standard());
		}
	}

	/* *****************************************************************************************************
	 *
	 * 											DML
	 *
	 * ****************************************************************************************************/
	@Override
	public String mergeFinalQuery(DataRuntime runtime, Run run){
		return super.mergeFinalQuery(runtime, run);
	}
	public String keyword(Table table){
		return "timeseries";
	}
}