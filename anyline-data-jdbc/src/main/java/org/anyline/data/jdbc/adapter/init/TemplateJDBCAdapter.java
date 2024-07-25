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



package org.anyline.data.jdbc.adapter.init;

import org.anyline.data.param.ConfigStore;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Column;
import org.anyline.metadata.Metadata;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;

//
public abstract class TemplateJDBCAdapter extends AbstractJDBCAdapter {

/*    public DatabaseType type() {
        return DatabaseType.NONE;
    }

    public TemplateJDBCAdapter() {
        super();
        delimiterFr = "`";
        delimiterTo = "`";
        for(MySQLTypeMetadataAlias alias:MySQLTypeMetadataAlias.values()) {
            reg(alias);
			alias(alias.name(), alias.standard());
        }
        for (MySQLTypeMetadataAlias alias: MySQLTypeMetadataAlias.values()) {
			reg(alias);
			alias(alias.name(), alias.standard());
        }
        for(MySQLWriter writer: MySQLWriter.values()) {
            reg(writer.supports(), writer.writer());
        }
        for(MySQLReader reader: MySQLReader.values()) {
            reg(reader.supports(), reader.reader());
        }
    }
    
    private String delimiter;
*/

    /* *****************************************************************************************************************
     *
     * 													复制过程
     * 1.添加TypeMetadataAlias
     * 2.如果有类型转换需要添加writer reader
     * 3.放开以上注释
     * 4.复制TemplateAdapter到这里
     *
     *  ***************************************************************************************************************/

    /* *****************************************************************************************************************
     *
     * 														JDBC
     *
     *  ***************************************************************************************************************/

    @Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, DataSource datasource, T meta) {
        super.checkSchema(runtime, datasource, meta);
    }

    @Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, Connection con, T meta) {
        super.checkSchema(runtime, con, meta);
    }
    /**
     * 根据运行环境识别 catalog与schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Metadata
     * @param <T> Metadata
     */
	@Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, T meta) {
        super.checkSchema(runtime, meta);
    }

	/**
	 * 识别根据jdbc返回的catalog与schema, 部分数据库(如mysql)系统表与jdbc标准可能不一致根据实际情况处理<br/>
	 * 注意一定不要处理从SQL中返回的，应该在SQL中处理好
	 * @param meta Metadata
	 * @param catalog catalog
	 * @param schema schema
     * @param overrideMeta 如果meta中有值，是否覆盖
     * @param overrideRuntime 如果runtime中有值，是否覆盖，注意结果集中可能跨多个schema，所以一般不要覆盖runtime,从con获取的可以覆盖ResultSet中获取的不要覆盖
	 * @param <T> Metadata
	 */
	@Override
    public <T extends Metadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema, boolean overrideRuntime, boolean overrideMeta) {
        super.correctSchemaFromJDBC(runtime, meta, catalog, schema, overrideRuntime, overrideMeta);
    }

	/**
	 * 在调用jdbc接口前处理业务中的catalog, schema, 部分数据库(如mysql)业务系统与dbc标准可能不一致根据实际情况处理<br/>
	 * @param catalog catalog
	 * @param schema schema
	 * @return String[]
	 */
	@Override
	public String[] correctSchemaFromJDBC(String catalog, String schema) {
		return super.correctSchemaFromJDBC(catalog, schema);
	}

    public String insertFoot(ConfigStore configs, LinkedHashMap<String, Column> columns) {
        return super.insertFoot(configs, columns);
    }

    /**
     * 内置函数 多种数据库兼容时需要
     * @param value SQL_BUILD_IN_VALUE
     * @return String
     */
    @Override
    public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value) {
        return super.value(runtime, column, value);
    }

    /**
     * 拼接字符串
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param args args
     * @return String
     */
    @Override
    public String concat(DataRuntime runtime, String... args) {
        return super.concat(runtime, args);
    }

    /**
     * 伪表
     * @return String
     */
    protected String dummy() {
        return super.dummy();
    }
    /* *****************************************************************************************************************
     *
     * 														具体数据库
     *
     *  ***************************************************************************************************************/
}
