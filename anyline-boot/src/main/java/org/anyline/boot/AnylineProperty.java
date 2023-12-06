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


package org.anyline.boot;

import org.anyline.boot.datasource.DataSourceProperty;
import org.anyline.entity.Compare;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.util.ConfigTable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "anyline")
public class AnylineProperty {
    /**
     * 数据源列表，包括JDBC及非JDBC，逗号分隔<br/>
     * anyline.datasource-list=crm,erp<br/>
     * 设置好列表后，为每个数据源设置连接参数<br/>
     * anyline.datasource.crm.url=...<br/>
     * anyline.datasource.erp.url=...<br/>
     */
    protected String datasourceList;
    /**
     * 多数据源配置
     *  anyline.datasource.{数据源key}.url=...
     */
    protected Map<String, DataSourceProperty> datasource;
    /**
     * 用来配置默认的 mongodb 数据源，如果还有其他数据源(包括JDBC)可以合并到datasource
     */
    protected DataSourceProperty mongodb;
    /**
     * 用来配置默认的 elasticsearch 数据源，如果还有其他数据源(包括JDBC)可以合并到datasource
     */
    protected DataSourceProperty elasticsearch;
    //以下属性与ConfigTable一一对应
    /**
     * debug状态会输出更多日志
     */
    protected boolean debug 									= true			;	// debug状态会输出更多日志
    /**
     * 执行sql时是否输出日志
     */
    protected boolean logSql									= true			;	// 执行sql时是否输出日志
    protected boolean logSlowSql                               = true          ;   // 执行慢sql时是否输出日志
    /**
     * 是否抛出convert异常提示
     */
    protected boolean throwConvertException					    = false			;   // 是否抛出convert异常提示()
    /**
     * 捕捉但未抛出的异常是否显示详细信息
     */
    protected boolean printExceptionStackTrace					= false			;   // 捕捉但未抛出的异常是否显示详细信息

    /**
     * 慢sql,如果配置了>0的毫秒数,在sql执行超出时限后会输出日志,并调用DMListener.slow
     */
    protected long slowSqlMillis								= 0			    ; 	// 慢sql,如果配置了>0的毫秒数,在sql执行超出时限后会输出日志,并调用dmlistener.slow
    /**
     * 执行sql时是否输出参数日志
     */
    protected boolean logSqlParam								= true			;	// 执行sql时是否输出参数日志
    /**
     * 执行sql异常时是否输出日志
     */
    protected boolean logSqlWhenError						    = true			;	// 执行sql异常时是否输出日志
    /**
     * 执行sql异常时是否输出参数日志
     */
    protected boolean logSqlParamWhenError					    = true			;	// 执行sql异常时是否输出参数日志
    /**
     * 加载自定义sql时是否输出日志
     */
    protected boolean sqlDebug	 								= false			;	// 加载自定义sql时是否输出日志

    protected boolean sqlLogPlaceholder                         = true          ;   // SQL日志是否显示占位符
    /**
     * 调用http接口时是否出输出日志
     */
    protected boolean httpLog 									= true			;	// 调用http接口时是否出输出日志
    /**
     * http参数是否需要解码 0:自动识别 1:确认编码 -1:确认未编码
     */
    protected int httpParamEncode								= 0			    ;   // http参数是否需要解码 0:自动识别 1:确认编码 -1:确认未编码
    /**
     * 如果有多数据源为每个数据源生成独立的service
     */
    protected boolean multipleService                            = true         ;   // 如果有多数据源为每个数据源生成独立的service
    /**
     * 否将数据库中与Java bytes[]对应的类型自动转换如Point > double[](返回DataRow时受此开关景程)
     */
    protected boolean autoConvertBytes 							 = true		    ;   // 否将数据库中与Java bytes[]对应的类型自动转换如Point > double[](返回DataRow时受此开关景程)

    /**
     * 查询元数据时忽略大小写
     */
    protected boolean metadataIgnoreCase                        = true          ;
    /**
     * DataRow是否自动转换成大写
     */
    protected boolean upperKey 								    = true			;	// DataRow是否自动转换成大写
    /**
     * DataRow是否自动转换成小写
     */
    protected boolean lowerKey 								    = false			;	// DataRow是否自动转换成小写
    /**
     * DataRow是否忽略大小写
     */
    protected boolean keyIgnoreCase 							= true			;	// DataRow是否忽略大小写
    /**
     * sql查询异常时是否抛出
     */
    protected boolean throwSqlQueryException 					= true			;	// sql查询异常时是否抛出
    /**
     * sql执行异常时是否抛出
     */
    protected boolean throwSqlUpdateException 				    = true			;	// sql执行异常时是否抛出
    /**
     * http参数值是否自动trim
     */
    protected boolean httpParamAutoTrim						    = true		    ;   // http参数值是否自动trim
    /**
     * AnylineController.entity(String ck)是否忽略http未提交的key
     */
    protected boolean ignoreEmptyHttpKey						= false			;	// AnylineController.entity(String ck)是否忽略http未提交的key
    /**
     * DataRow是否更新nul值的列
     */
    protected boolean updateNullColumn							= false			;	// DataRow是否更新nul值的列
    /**
     * DataRow是否更新空值的列
     */
    protected boolean updateEmptyColumn						    = false			;	// DataRow是否更新空值的列
    /**
     * DataRow是否更新nul值的列
     */
    protected boolean insertNullColumn							= false			;	// DataRow是否更新nul值的列
    /**
     * DataRow是否更新空值的列
     */
    protected boolean insertEmptyColumn						    = false			;	// DataRow是否更新空值的列
    /**
     * Entity是否更新nul值的属性
     */
    protected boolean updateNullField							= false			;	// Entity是否更新nul值的属性
    /**
     * Entity是否更新空值的属性
     */
    protected boolean updateEmptyField						    = false			;	// Entity是否更新空值的属性
    /**
     * 是否把""替换成null
     */
    protected boolean replaceEmptyNull                          = true          ;   // 是否把""替换成null
    /**
     * Entity是否更新nul值的属性
     */
    protected boolean insertNullField							= false			;	// Entity是否更新nul值的属性
    /**
     * Entity是否更新空值的属性
     */
    protected boolean insertEmptyField						    = false			;	// Entity是否更新空值的属性
    /**
     * List/Array转换成String后的格式 concat:A,B,C json:["A","B","C"]
     */
    protected String list2stringFormat                          = "concat"		;	// List/Array转换成String后的格式 concat:A,B,C json:["A","B","C"]
    /**
     * 是否禁用查询缓存
     */
    protected boolean cacheDisabled                             = false         ;   // 是否禁用查询缓存
    /**
     * 禁用默认的entity adapter
     */
    protected boolean disabledDefaultEntityAdapter              = false         ;   // 禁用默认的entity adapter
    /**
     * 是否开启 界定符
     */
    protected boolean sqlDelimiterOpen 						    = false			;	// 是否开启 界定符
    /**
     * 是否自动检测关键字
     */
    protected boolean autoCheckKeyword                          = true;         ;   // 是否自动检测关键字
    /**
     * 是否开启 界定符的占位符
     */
    protected boolean sqlDelimiterPlaceholderOpen 			    = false			;	// 是否开启 界定符的占位符
    /**
     * 界定符的点位符
     */
    protected String sqlDelimiterPlaceholder					= "`"		    ;   // 界定符的点位符
    protected boolean returnEmptyStringReplaceNull               = false         ;
    /**
     * service.query() dataset.getRow()返回null时,是否替换成new DataRow(), new entity()
     */
    protected boolean returnEmptyInstanceReplaceNull			= false			;	// service.query() dataset.getRow()返回null时,是否替换成new DataRow(), new entity()
    /**
     * 更新数据库时，是把自动把数组/集合类型拆分
     */
    protected boolean autoSplitArray						    = true			;	// 更新数据库时，是把自动把数组/集合类型拆分

    /**
     * insert update 时是否自动检测表结构(删除表中不存在的属性)
     */
    protected boolean autoCheckMetadata						    = false		    ; 	// insert update 时是否自动检测表结构(删除表中不存在的属性)
    /**
     * 查询返回空DataSet时，是否检测元数据信息
     */
    protected boolean checkEmptySetMetadata                     = false         ;   // 查询返回空DataSet时，是否检测元数据信息
    /**
     * DataRow row = entity("ID:id") 如果参数(如request)中未提供id参数时,row中是否清空ID属性
     */
    protected boolean removeEmptyHttpKey                        = false         ;   // DataRow row = entity("ID:id") 如果参数(如request)中未提供id参数时,row中是否清空ID属性


    /**
     * ddl修改列异常后 0:中断修改 1:删除列 n:总行数小于多少时更新值否则触发另一个监听
     */
    protected int afterAlterColumnExceptionAction				= 1000		    ;   // ddl修改列异常后 0:中断修改 1:删除列 n:总行数小于多少时更新值否则触发另一个监听
    /**
     * ddl执行时是否自动删除定义中不存在的列
     */
    protected boolean ddlAutoDropColumn						    = false		    ;   // ddl执行时是否自动删除定义中不存在的列
    /**
     * 查询列时，是否自动检测主键标识
     */
    protected boolean metadataAutoCheckColumnPrimary			= false			;   // 查询列时，是否自动检测主键标识
    /**
     * 自定义SQL目录(包括MyBatis) 默认${classpath}/sql .表示项目根目录 ${classpath}表示classes目录
     */
    protected String sqlStoreDir								= null			;	// 自定义SQL目录(包括MyBatis) 默认${classpath}/sql .表示项目根目录 ${classpath}表示classes目录
    /**
     * 是否开始解析mybatis定义的SQL
     */
    protected boolean openParseMybatis							= true		    ; 	// 是否开始解析mybatis定义的SQL
    /**
     * 实体属性 与数据库表列名对照时 默认属性小驼峰转下划线 userName > USER_NAME
     */
    protected String entityFieldColumnMap                       = "camel_"      ;   // 实体属性 与数据库表列名对照时 默认属性小驼峰转下划线 userName > USER_NAME
    /**
     * 实体类名 与数据库表名对照时 默认属性大驼峰转下划线 CrmUser > CRM_USER
     */
    protected String entityClassTableMap						= "Camel_"  	;	// 实体类名 与数据库表名对照时 默认属性大驼峰转下划线 CrmUser > CRM_USER
    /**
     * 表名注解
     */
    protected String entityTableAnnotation						= null		    ;   // 表名注解
    /**
     * 列名注解
     */
    protected String entityColumnAnnotation					    = null			;	// 列名注解
    /**
     * 主键注解(逗号分隔,不区分大小写,支持正则匹配) tableId.value,id.name,id(如果不指定注解属性名则依次按name,value解析)
     */
    protected String entityPrimaryKeyAnnotation				    = null		    ;   // 主键注解(逗号分隔,不区分大小写,支持正则匹配) tableId.value,id.name,id(如果不指定注解属性名则依次按name,value解析)

    /**
     * 实体类属性依赖层级 > 0:查询属性关联表
     */
    protected int entityFieldSelectDependency = 0             ;   // 实体类属性依赖层级 > 0:查询属性关联表

    protected String entityFieldSelectDependencyCompare         = "IN"         ;   //实体类属性依赖查询方式 EQUAL:逐行查询 IN:一次查询
    /**
     * 实体类属性依赖层级 > 0:插入属性关联表
     */
    protected int entityFieldInsertDependency = 0             ;   // 实体类属性依赖层级 > 0:插入属性关联表

    /**
     * 实体类属性依赖层级 > 0:更新属性关联表
     */
    protected int entityFieldUpdateDependency = 0             ;   // 实体类属性依赖层级 > 0:更新属性关联表

    /**
     * 实体类属性依赖层级 > 0:删除属性关联表
     */
    protected int entityFieldDeleteDependency = 0             ;   // 实体类属性依赖层级 > 0:删除属性关联表
    /**
     * http参数格式 camel:小驼峰 Camel:大驼峰 lower:小写 upper:大写  service.column2param会把 USER_NAME 转成userName
     */
    protected String httpParamKeyCase							= "camel"		;	// http参数格式 camel:小驼峰 Camel:大驼峰 lower:小写 upper:大写  service.column2param会把 USER_NAME 转成userName
    /**
     * 表结构缓存key
     */
    protected String tableMetadataCacheKey					    = ""			;	// 表结构缓存key
    /**
     * 表结构缓存时间(没有设置缓存key的情况下生效)(-1:表示永不失效)
     */
    protected int tableMetadataCacheSecond						= 3600*24		;	// 表结构缓存时间(没有设置缓存key的情况下生效)(-1:表示永不失效)
    /**
     * MixUtil.mix默认seed
     */
    protected String mixDefaultSeed                             = "al"          ;   // MixUtil.mix默认seed
    protected String elAttributePrefix      					= "al"		    ;

    /**
     * 默认主键
     */
    protected String defaultPrimaryKey							= "id"			;	// 默认主键
    /**
     * 是否需要提供主事务管理器,多数据源时需要
     */
    protected boolean openPrimaryTransactionManager             = false         ;   // 是否需要提供主事务管理器,多数据源时需要
    /**
     * 是否需要提供主管理器,会根据数据源生成相应的事务管理器
     */
    protected boolean openTransactionManager                    = true         ;   // 是否需要提供主管理器,会根据数据源生成相应的事务管理器
    /**
     * 主键生成器机器id
     */
    public int primaryGeneratorWorkerId					        = 1				;	// 主键生成器机器id
    /**
     * 主键前缀(随机主键)
     */
    public String primaryGeneratorPrefix					    = ""			;	// 主键前缀(随机主键)
    /**
     * 主随机主键总长度
     */
    public int primaryGeneratorRandomLength				        = 32			;	// 主随机主键总长度
    /**
     * 生成主键大写
     */
    public boolean primaryGeneratorUpper					    = true			;	// 生成主键大写
    /**
     * 生成主键小写
     */
    public boolean primaryGeneratorLower					    = false			;	// 生成主键小写
    /**
     * 生成主键日期格式(默认yyyyMMddhhmmssSSS)
     */
    public String primaryGeneratorTimeFormat					= null		    ;	// 生成主键日期格式(默认yyyyMMddhhmmssSSS)
    /**
     * 生成主键time/timestamp后缀随机数长度
     */
    public int primaryGeneratorTimeSuffixLength				    = 3			    ;   // 生成主键time/timestamp后缀随机数长度
    /**
     * 雪花算法开始日期
     */
    public String snowflakeTwepoch                              = "2000-01-01"  ;   //雪花算法开始日期
    /**
     * 是否开启默认的主键生成器(UUID)
     */
    public boolean primaryGeneratorUuidActive			        = false			;	// 是否开启默认的主键生成器(UUID)
    /**
     * 是否开启默认的主键生成器(雪花)
     */
    public boolean primaryGeneratorSnowflakeActive		        = false			;	// 是否开启默认的主键生成器(雪花)
    /**
     * 是否开启默认的主键生成器(随机)
     */
    public boolean primaryGeneratorRandomActive			        = false			;	// 是否开启默认的主键生成器(随机)
    /**
     * 是否开启默认的主键生成器(时间戳)
     */
    public boolean primaryGeneratorTimestampActive			    = false			;	// 是否开启默认的主键生成器(时间戳)
    /**
     * 是否开启默认的主键生成器(年月日时分秒毫秒)
     */
    public boolean primaryGeneratorTimeActive					= false			;	// 是否开启默认的主键生成器(年月日时分秒毫秒)

    public PrimaryGenerator.GENERATOR generator                = null          ;   // 全局默认主键生成器

    public String generatorTables                               = "*"           ;   // 主键生成器适用的表

    public Map<String, PrimaryGenerator.GENERATOR> generators          = new HashMap();



    public boolean isDebug() {
        return debug;
    }

    public String getEntityFieldColumnMap() {
        return entityFieldColumnMap;
    }

    public void setEntityFieldColumnMap(String entityFieldColumnMap) {
        this.entityFieldColumnMap = entityFieldColumnMap;
        ConfigTable.ENTITY_FIELD_COLUMN_MAP = entityFieldColumnMap;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        ConfigTable.IS_DEBUG = debug;
    }

    public boolean isLogSql() {
        return logSql;
    }

    public void setLogSql(boolean logSql) {
        this.logSql = logSql;
        ConfigTable.IS_LOG_SQL = logSql;
    }

    public boolean isLogSqlSlowSql() {
        return logSlowSql;
    }

    public void setLogSlowSql(boolean logSlowSql) {
        this.logSlowSql = logSlowSql;
        ConfigTable.IS_LOG_SLOW_SQL = logSlowSql;
    }

    public long getSlowSqlMillis() {
        return slowSqlMillis;
    }

    public void setSlowSqlMillis(long slowSqlMillis) {
        this.slowSqlMillis = slowSqlMillis;
        ConfigTable.SLOW_SQL_MILLIS = slowSqlMillis;
    }

    public boolean isLogSqlParam() {
        return logSqlParam;
    }

    public void setLogSqlParam(boolean logSqlParam) {
        this.logSqlParam = logSqlParam;
        ConfigTable.IS_LOG_SQL_PARAM = logSqlParam;
    }

    public boolean isLogSqlWhenError() {
        return logSqlWhenError;
    }

    public void setLogSqlWhenError(boolean logSqlWhenError) {
        this.logSqlWhenError = logSqlWhenError;
        ConfigTable.IS_LOG_SQL_WHEN_ERROR = logSqlWhenError;
    }

    public boolean isLogSqlParamWhenError() {
        return logSqlParamWhenError;
    }

    public void setLogSqlParamWhenError(boolean logSqlParamWhenError) {
        this.logSqlParamWhenError = logSqlParamWhenError;
        ConfigTable.IS_LOG_SQL_PARAM_WHEN_ERROR = logSqlParamWhenError;
    }

    public boolean isSqlDebug() {
        return sqlDebug;
    }

    public void setSqlDebug(boolean sqlDebug) {
        this.sqlDebug = sqlDebug;
        ConfigTable.IS_SQL_DEBUG = sqlDebug;
    }

    public boolean isSqlLogPlaceholder() {
        return sqlLogPlaceholder;
    }

    public void setSqlLogPlaceholder(boolean sqlLogPlaceholder) {
        this.sqlLogPlaceholder = sqlLogPlaceholder;
        ConfigTable.IS_SQL_LOG_PLACEHOLDER = sqlLogPlaceholder;
    }

    public boolean isHttpLog() {
        return httpLog;
    }

    public void setHttpLog(boolean httpLog) {
        this.httpLog = httpLog;
        ConfigTable.IS_HTTP_LOG = httpLog;
    }

    public int getHttpParamEncode() {
        return httpParamEncode;
    }

    public void setHttpParamEncode(int httpParamEncode) {
        this.httpParamEncode = httpParamEncode;
        ConfigTable.HTTP_PARAM_ENCODE = httpParamEncode;
    }

    public boolean isMultipleService() {
        return multipleService;
    }

    public void setMultipleService(boolean multipleService) {
        this.multipleService = multipleService;
        ConfigTable.IS_MULTIPLE_SERVICE = multipleService;
    }

    public boolean isMetadataIgnoreCase() {
        return metadataIgnoreCase;
    }

    public void setMetadataIgnoreCase(boolean metadataIgnoreCase) {
        this.metadataIgnoreCase = metadataIgnoreCase;
        ConfigTable.IS_METADATA_IGNORE_CASE = metadataIgnoreCase;
    }

    public boolean isUpperKey() {
        return upperKey;
    }

    public void setUpperKey(boolean upperKey) {
        this.upperKey = upperKey;
        ConfigTable.IS_UPPER_KEY = upperKey;
    }

    public boolean isLowerKey() {
        return lowerKey;
    }

    public void setLowerKey(boolean lowerKey) {
        this.lowerKey = lowerKey;
        ConfigTable.IS_LOWER_KEY = lowerKey;
    }

    public boolean isKeyIgnoreCase() {
        return keyIgnoreCase;
    }

    public void setKeyIgnoreCase(boolean keyIgnoreCase) {
        this.keyIgnoreCase = keyIgnoreCase;
        ConfigTable.IS_KEY_IGNORE_CASE = keyIgnoreCase;
    }

    public boolean isThrowSqlQueryException() {
        return throwSqlQueryException;
    }

    public void setThrowSqlQueryException(boolean throwSqlQueryException) {
        this.throwSqlQueryException = throwSqlQueryException;
        ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION = throwSqlQueryException;
    }

    public int getEntityFieldSelectDependency() {
        return entityFieldSelectDependency;
    }

    public void setEntityFieldSelectDependency(int entityFieldSelectDependency) {
        this.entityFieldSelectDependency = entityFieldSelectDependency;
        ConfigTable.ENTITY_FIELD_SELECT_DEPENDENCY = entityFieldSelectDependency;
    }

    public int getEntityFieldInsertDependency() {
        return entityFieldInsertDependency;
    }

    public void setEntityFieldInsertDependency(int entityFieldInsertDependency) {
        this.entityFieldInsertDependency = entityFieldInsertDependency;
        ConfigTable.ENTITY_FIELD_INSERT_DEPENDENCY = entityFieldInsertDependency;
    }

    public int getEntityFieldUpdateDependency() {
        return entityFieldUpdateDependency;
    }

    public void setEntityFieldUpdateDependency(int entityFieldUpdateDependency) {
        this.entityFieldUpdateDependency = entityFieldUpdateDependency;
        ConfigTable.ENTITY_FIELD_UPDATE_DEPENDENCY = entityFieldUpdateDependency;
    }

    public int getEntityFieldDeleteDependency() {
        return entityFieldDeleteDependency;
    }

    public void setEntityFieldDeleteDependency(int entityFieldDeleteDependency) {
        this.entityFieldDeleteDependency = entityFieldDeleteDependency;
        ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY = entityFieldDeleteDependency;
    }

    public boolean isThrowSqlUpdateException() {
        return throwSqlUpdateException;
    }

    public void setThrowSqlUpdateException(boolean throwSqlUpdateException) {
        this.throwSqlUpdateException = throwSqlUpdateException;
        ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION = throwSqlUpdateException;
    }

    public boolean isUpdateNullColumn() {
        return updateNullColumn;
    }

    public void setUpdateNullColumn(boolean updateNullColumn) {
        this.updateNullColumn = updateNullColumn;
        ConfigTable.IS_UPDATE_NULL_COLUMN = updateNullColumn;
    }

    public boolean isUpdateEmptyColumn() {
        return updateEmptyColumn;
    }

    public void setUpdateEmptyColumn(boolean updateEmptyColumn) {
        this.updateEmptyColumn = updateEmptyColumn;
        ConfigTable.IS_UPDATE_EMPTY_COLUMN = updateEmptyColumn;
    }

    public boolean isInsertNullColumn() {
        return insertNullColumn;
    }

    public void setInsertNullColumn(boolean insertNullColumn) {
        this.insertNullColumn = insertNullColumn;
        ConfigTable.IS_INSERT_NULL_COLUMN = insertNullColumn;
    }

    public boolean isInsertEmptyColumn() {
        return insertEmptyColumn;
    }

    public void setInsertEmptyColumn(boolean insertEmptyColumn) {
        this.insertEmptyColumn = insertEmptyColumn;
        ConfigTable.IS_INSERT_EMPTY_COLUMN = insertEmptyColumn;
    }

    public boolean isAutoCheckKeyword() {
        return autoCheckKeyword;
    }

    public void setAutoCheckKeyword(boolean autoCheckKeyword) {
        this.autoCheckKeyword = autoCheckKeyword;
        ConfigTable.IS_AUTO_CHECK_KEYWORD = autoCheckKeyword;
    }

    public boolean isSqlDelimiterOpen() {
        return sqlDelimiterOpen;
    }

    public void setSqlDelimiterOpen(boolean sqlDelimiterOpen) {
        this.sqlDelimiterOpen = sqlDelimiterOpen;
        ConfigTable.IS_SQL_DELIMITER_OPEN = sqlDelimiterOpen;
    }

    public boolean isSqlDelimiterPlaceholderOpen() {
        return sqlDelimiterPlaceholderOpen;
    }

    public void setSqlDelimiterPlaceholderOpen(boolean sqlDelimiterPlaceholderOpen) {
        this.sqlDelimiterPlaceholderOpen = sqlDelimiterPlaceholderOpen;
        ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN = sqlDelimiterPlaceholderOpen;
    }

    public boolean isReturnEmptyStringReplaceNull() {
        return returnEmptyStringReplaceNull;
    }

    public void setReturnEmptyStringReplaceNull(boolean returnEmptyStringReplaceNull) {
        this.returnEmptyStringReplaceNull = returnEmptyStringReplaceNull;
        ConfigTable.IS_RETURN_EMPTY_STRING_REPLACE_NULL = returnEmptyStringReplaceNull;
    }

    public boolean isReturnEmptyInstanceReplaceNull() {
        return returnEmptyInstanceReplaceNull;
    }

    public void setReturnEmptyInstanceReplaceNull(boolean returnEmptyInstanceReplaceNull) {
        this.returnEmptyInstanceReplaceNull = returnEmptyInstanceReplaceNull;
        ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL = returnEmptyInstanceReplaceNull;
    }

    public boolean isAutoCheckMetadata() {
        return autoCheckMetadata;
    }

    public void setAutoCheckMetadata(boolean autoCheckMetadata) {
        this.autoCheckMetadata = autoCheckMetadata;
        ConfigTable.IS_AUTO_CHECK_METADATA = autoCheckMetadata;
    }

    public boolean isCheckEmptySetMetadata() {
        return checkEmptySetMetadata;
    }

    public void setCheckEmptySetMetadata(boolean checkEmptySetMetadata) {
        this.checkEmptySetMetadata = checkEmptySetMetadata;
        ConfigTable.IS_CHECK_EMPTY_SET_METADATA = checkEmptySetMetadata;
    }

    public boolean isCacheDisabled() {
        return cacheDisabled;
    }

    public void setCacheDisabled(boolean cacheDisabled) {
        this.cacheDisabled = cacheDisabled;
        ConfigTable.IS_CACHE_DISABLED = cacheDisabled;
    }

    public String getDefaultPrimaryKey() {
        return defaultPrimaryKey;
    }

    public void setDefaultPrimaryKey(String defaultPrimaryKey) {
        this.defaultPrimaryKey = defaultPrimaryKey;
        ConfigTable.DEFAULT_PRIMARY_KEY = defaultPrimaryKey;
    }

    public int getAfterAlterColumnExceptionAction() {
        return afterAlterColumnExceptionAction;
    }

    public void setAfterAlterColumnExceptionAction(int afterAlterColumnExceptionAction) {
        this.afterAlterColumnExceptionAction = afterAlterColumnExceptionAction;
        ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION = afterAlterColumnExceptionAction;
    }

    public boolean isDdlAutoDropColumn() {
        return ddlAutoDropColumn;
    }

    public void setDdlAutoDropColumn(boolean ddlAutoDropColumn) {
        this.ddlAutoDropColumn = ddlAutoDropColumn;
        ConfigTable.IS_DDL_AUTO_DROP_COLUMN = ddlAutoDropColumn;
    }

    public String getSqlStoreDir() {
        return sqlStoreDir;
    }

    public void setSqlStoreDir(String sqlStoreDir) {
        this.sqlStoreDir = sqlStoreDir;
        ConfigTable.SQL_STORE_DIR = sqlStoreDir;
    }

    public boolean isOpenParseMybatis() {
        return openParseMybatis;
    }

    public void setOpenParseMybatis(boolean openParseMybatis) {
        this.openParseMybatis = openParseMybatis;
        ConfigTable.IS_OPEN_PARSE_MYBATIS = openParseMybatis;
    }

    public String getEntityTableAnnotation() {
        return entityTableAnnotation;
    }

    public void setEntityTableAnnotation(String entityTableAnnotation) {
        this.entityTableAnnotation = entityTableAnnotation;
        ConfigTable.ENTITY_TABLE_ANNOTATION = entityTableAnnotation;
    }

    public String getEntityColumnAnnotation() {
        return entityColumnAnnotation;
    }

    public void setEntityColumnAnnotation(String entityColumnAnnotation) {
        this.entityColumnAnnotation = entityColumnAnnotation;
        ConfigTable.ENTITY_COLUMN_ANNOTATION = entityColumnAnnotation;
    }

    public String getEntityPrimaryKeyAnnotation() {
        return entityPrimaryKeyAnnotation;
    }

    public void setEntityPrimaryKeyAnnotation(String entityPrimaryKeyAnnotation) {
        this.entityPrimaryKeyAnnotation = entityPrimaryKeyAnnotation;
        ConfigTable.ENTITY_PRIMARY_KEY_ANNOTATION = entityPrimaryKeyAnnotation;
    }

    public String getHttpParamKeyCase() {
        return httpParamKeyCase;
    }

    public void setHttpParamKeyCase(String httpParamKeyCase) {
        this.httpParamKeyCase = httpParamKeyCase;
        ConfigTable.HTTP_PARAM_KEY_CASE = httpParamKeyCase;
    }

    public String getTableMetadataCacheKey() {
        return tableMetadataCacheKey;
    }

    public void setTableMetadataCacheKey(String tableMetadataCacheKey) {
        this.tableMetadataCacheKey = tableMetadataCacheKey;
        ConfigTable.TABLE_METADATA_CACHE_KEY = tableMetadataCacheKey;
    }

    public int getTableMetadataCacheSecond() {
        return tableMetadataCacheSecond;
    }

    public void setTableMetadataCacheSecond(int tableMetadataCacheSecond) {
        this.tableMetadataCacheSecond = tableMetadataCacheSecond;
        ConfigTable.TABLE_METADATA_CACHE_SECOND = tableMetadataCacheSecond;
    }

    public String getSqlDelimiterPlaceholder() {
        return sqlDelimiterPlaceholder;
    }

    public void setSqlDelimiterPlaceholder(String sqlDelimiterPlaceholder) {
        this.sqlDelimiterPlaceholder = sqlDelimiterPlaceholder;
        ConfigTable.SQL_DELIMITER_PLACEHOLDER = sqlDelimiterPlaceholder;
    }

    public boolean isDisabledDefaultEntityAdapter() {
        return disabledDefaultEntityAdapter;
    }

    public void setDisabledDefaultEntityAdapter(boolean disabledDefaultEntityAdapter) {
        this.disabledDefaultEntityAdapter = disabledDefaultEntityAdapter;
        ConfigTable.IS_DISABLED_DEFAULT_ENTITY_ADAPTER = disabledDefaultEntityAdapter;
    }

    public boolean isRemoveEmptyHttpKey() {
        return removeEmptyHttpKey;
    }

    public void setRemoveEmptyHttpKey(boolean removeEmptyHttpKey) {
        this.removeEmptyHttpKey = removeEmptyHttpKey;
        ConfigTable.IS_REMOVE_EMPTY_HTTP_KEY = removeEmptyHttpKey;
    }

    public boolean isHttpParamAutoTrim() {
        return httpParamAutoTrim;
    }

    public void setHttpParamAutoTrim(boolean httpParamAutoTrim) {
        this.httpParamAutoTrim = httpParamAutoTrim;
        ConfigTable.IS_HTTP_PARAM_AUTO_TRIM = httpParamAutoTrim;
    }

    public boolean isIgnoreEmptyHttpKey() {
        return ignoreEmptyHttpKey;
    }

    public void setIgnoreEmptyHttpKey(boolean ignoreEmptyHttpKey) {
        this.ignoreEmptyHttpKey = ignoreEmptyHttpKey;
        ConfigTable.IS_IGNORE_EMPTY_HTTP_KEY = ignoreEmptyHttpKey;
    }

    public boolean isUpdateNullField() {
        return updateNullField;
    }

    public void setUpdateNullField(boolean updateNullField) {
        this.updateNullField = updateNullField;
        ConfigTable.IS_UPDATE_NULL_FIELD = updateNullField;
    }

    public boolean isUpdateEmptyField() {
        return updateEmptyField;
    }

    public void setUpdateEmptyField(boolean updateEmptyField) {
        this.updateEmptyField = updateEmptyField;
        ConfigTable.IS_UPDATE_EMPTY_FIELD = updateEmptyField;
    }

    public boolean isInsertNullField() {
        return insertNullField;
    }

    public void setInsertNullField(boolean insertNullField) {
        this.insertNullField = insertNullField;
        ConfigTable.IS_INSERT_NULL_FIELD = insertNullField;
    }

    public boolean isInsertEmptyField() {
        return insertEmptyField;
    }

    public void setInsertEmptyField(boolean insertEmptyField) {
        this.insertEmptyField = insertEmptyField;
        ConfigTable.IS_INSERT_EMPTY_FIELD = insertEmptyField;
    }

    public boolean isReplaceEmptyNull() {
        return replaceEmptyNull;
    }

    public void setReplaceEmptyNull(boolean replaceEmptyNull) {
        this.replaceEmptyNull = replaceEmptyNull;
        ConfigTable.IS_REPLACE_EMPTY_NULL = replaceEmptyNull;
    }

    public String getMixDefaultSeed() {
        return mixDefaultSeed;
    }

    public void setMixDefaultSeed(String mixDefaultSeed) {
        this.mixDefaultSeed = mixDefaultSeed;
        ConfigTable.MIX_DEFAULT_SEED = mixDefaultSeed;
    }

    public String getElAttributePrefix() {
        return elAttributePrefix;
    }

    public void setElAttributePrefix(String elAttributePrefix) {
        this.elAttributePrefix = elAttributePrefix;
        ConfigTable.EL_ATTRIBUTE_PREFIX = elAttributePrefix;
    }

    public int getPrimaryGeneratorWorkerId() {
        return primaryGeneratorWorkerId;
    }

    public void setPrimaryGeneratorWorkerId(int primaryGeneratorWorkerId) {
        this.primaryGeneratorWorkerId = primaryGeneratorWorkerId;
        ConfigTable.PRIMARY_GENERATOR_WORKER_ID = primaryGeneratorWorkerId;
    }

    public String getPrimaryGeneratorPrefix() {
        return primaryGeneratorPrefix;
    }

    public void setPrimaryGeneratorPrefix(String primaryGeneratorPrefix) {
        this.primaryGeneratorPrefix = primaryGeneratorPrefix;
        ConfigTable.PRIMARY_GENERATOR_PREFIX = primaryGeneratorPrefix;
    }

    public int getPrimaryGeneratorRandomLength() {
        return primaryGeneratorRandomLength;
    }

    public void setPrimaryGeneratorRandomLength(int primaryGeneratorRandomLength) {
        this.primaryGeneratorRandomLength = primaryGeneratorRandomLength;
        ConfigTable.PRIMARY_GENERATOR_RANDOM_LENGTH = primaryGeneratorRandomLength;
    }

    public boolean isPrimaryGeneratorUpper() {
        return primaryGeneratorUpper;
    }

    public void setPrimaryGeneratorUpper(boolean primaryGeneratorUpper) {
        this.primaryGeneratorUpper = primaryGeneratorUpper;
        ConfigTable.PRIMARY_GENERATOR_UPPER = primaryGeneratorUpper;
    }

    public boolean isPrimaryGeneratorLower() {
        return primaryGeneratorLower;
    }

    public void setPrimaryGeneratorLower(boolean primaryGeneratorLower) {
        this.primaryGeneratorLower = primaryGeneratorLower;
        ConfigTable.PRIMARY_GENERATOR_LOWER = primaryGeneratorLower;
    }

    public boolean isPrimaryGeneratorUuidActive() {
        return primaryGeneratorUuidActive;
    }

    public void setPrimaryGeneratorUuidActive(boolean primaryGeneratorUuidActive) {
        this.primaryGeneratorUuidActive = primaryGeneratorUuidActive;
        ConfigTable.PRIMARY_GENERATOR_UUID_ACTIVE = primaryGeneratorUuidActive;
    }

    public boolean isPrimaryGeneratorSnowflakeActive() {
        return primaryGeneratorSnowflakeActive;
    }

    public String getSnowflakeTwepoch() {
        return snowflakeTwepoch;
    }

    public void setSnowflakeTwepoch(String snowflakeTwepoch) {
        this.snowflakeTwepoch = snowflakeTwepoch;
        ConfigTable.SNOWFLAKE_TWEPOCH = snowflakeTwepoch;
    }

    public void setPrimaryGeneratorSnowflakeActive(boolean primaryGeneratorSnowflakeActive) {
        this.primaryGeneratorSnowflakeActive = primaryGeneratorSnowflakeActive;
        ConfigTable.PRIMARY_GENERATOR_SNOWFLAKE_ACTIVE = primaryGeneratorSnowflakeActive;
    }

    public boolean isPrimaryGeneratorRandomActive() {
        return primaryGeneratorRandomActive;
    }

    public void setPrimaryGeneratorRandomActive(boolean primaryGeneratorRandomActive) {
        this.primaryGeneratorRandomActive = primaryGeneratorRandomActive;
        ConfigTable.PRIMARY_GENERATOR_RANDOM_ACTIVE = primaryGeneratorRandomActive;
    }

    public String getPrimaryGeneratorTimeFormat() {
        return primaryGeneratorTimeFormat;
    }

    public void setPrimaryGeneratorTimeFormat(String primaryGeneratorTimeFormat) {
        this.primaryGeneratorTimeFormat = primaryGeneratorTimeFormat;
        ConfigTable.PRIMARY_GENERATOR_TIME_FORMAT = primaryGeneratorTimeFormat;
    }

    public int getPrimaryGeneratorTimeSuffixLength() {
        return primaryGeneratorTimeSuffixLength;
    }

    public void setPrimaryGeneratorTimeSuffixLength(int primaryGeneratorTimeSuffixLength) {
        this.primaryGeneratorTimeSuffixLength = primaryGeneratorTimeSuffixLength;
        ConfigTable.PRIMARY_GENERATOR_TIME_SUFFIX_LENGTH = primaryGeneratorTimeSuffixLength;
    }

    public boolean isPrimaryGeneratorTimestampActive() {
        return primaryGeneratorTimestampActive;
    }

    public void setPrimaryGeneratorTimestampActive(boolean primaryGeneratorTimestampActive) {
        this.primaryGeneratorTimestampActive = primaryGeneratorTimestampActive;
        ConfigTable.PRIMARY_GENERATOR_TIMESTAMP_ACTIVE = primaryGeneratorTimestampActive;
    }

    public boolean isPrimaryGeneratorTimeActive() {
        return primaryGeneratorTimeActive;
    }

    public void setPrimaryGeneratorTimeActive(boolean primaryGeneratorTimeActive) {
        this.primaryGeneratorTimeActive = primaryGeneratorTimeActive;
        ConfigTable.PRIMARY_GENERATOR_TIME_ACTIVE = primaryGeneratorTimeActive;
    }

    public boolean isAutoConvertBytes() {
        return autoConvertBytes;
    }

    public void setAutoConvertBytes(boolean autoConvertBytes) {
        this.autoConvertBytes = autoConvertBytes;
        ConfigTable.IS_AUTO_CONVERT_BYTES = autoConvertBytes;
    }

    public boolean isAutoSplitArray() {
        return autoSplitArray;
    }

    public void setAutoSplitArray(boolean autoSplitArray) {
        this.autoSplitArray = autoSplitArray;
        ConfigTable.IS_AUTO_SPLIT_ARRAY = autoSplitArray;
    }

    public String getEntityClassTableMap() {
        return entityClassTableMap;
    }

    public void setEntityClassTableMap(String entityClassTableMap) {
        this.entityClassTableMap = entityClassTableMap;
        ConfigTable.ENTITY_CLASS_TABLE_MAP = entityClassTableMap;
    }

    public String getEntityFieldSelectDependencyCompare() {
        return entityFieldSelectDependencyCompare;
    }

    public void setEntityFieldSelectDependencyCompare(String entityFieldSelectDependencyCompare) {
        this.entityFieldSelectDependencyCompare = entityFieldSelectDependencyCompare;
        ConfigTable.ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE = Compare.valueOf(entityFieldSelectDependencyCompare.toUpperCase());
    }

    public boolean isOpenPrimaryTransactionManager() {
        return openPrimaryTransactionManager;
    }

    public void setOpenPrimaryTransactionManager(boolean openPrimaryTransactionManager) {
        this.openPrimaryTransactionManager = openPrimaryTransactionManager;
        ConfigTable.IS_OPEN_PRIMARY_TRANSACTION_MANAGER = openPrimaryTransactionManager;
    }

    public String getList2stringFormat() {
        return list2stringFormat;
    }

    public void setList2stringFormat(String list2stringFormat) {
        this.list2stringFormat = list2stringFormat;
        ConfigTable.LIST2STRING_FORMAT =  list2stringFormat;
    }

    public boolean isThrowConvertException() {
        return throwConvertException;
    }

    public void setThrowConvertException(boolean throwConvertException) {
        this.throwConvertException = throwConvertException;
        ConfigTable.IS_THROW_CONVERT_EXCEPTION = throwConvertException;
    }

    public boolean isOpenTransactionManager() {
        return openTransactionManager;
    }

    public void setOpenTransactionManager(boolean openTransactionManager) {
        this.openTransactionManager = openTransactionManager;
        ConfigTable.IS_OPEN_TRANSACTION_MANAGER = openTransactionManager;
    }

    public boolean isPrintExceptionStackTrace() {
        return printExceptionStackTrace;
    }

    public void setPrintExceptionStackTrace(boolean printExceptionStackTrace) {
        this.printExceptionStackTrace = printExceptionStackTrace;
        ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE = printExceptionStackTrace;
    }

    public PrimaryGenerator.GENERATOR getGenerator() {
        return generator;
    }

    public void setGenerator(PrimaryGenerator.GENERATOR generator) {
        this.generator = generator;
        ConfigTable.GENERATOR.set(generator);
    }

    public boolean isMetadataAutoCheckColumnPrimary() {
        return metadataAutoCheckColumnPrimary;
    }

    public void setMetadataAutoCheckColumnPrimary(boolean metadataAutoCheckColumnPrimary) {
        this.metadataAutoCheckColumnPrimary = metadataAutoCheckColumnPrimary;
        ConfigTable.IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY = metadataAutoCheckColumnPrimary;
    }

    public Map<String, PrimaryGenerator.GENERATOR> getGenerators() {
        return generators;
    }

    public void setGenerators(Map<String, PrimaryGenerator.GENERATOR> generators) {
        this.generators = generators;
        if(null != generators){
            for(String key:generators.keySet()){
                ConfigTable.GENERATOR.put(key, generators.get(key));
            }
        }
    }

    public String getGeneratorTables() {
        return generatorTables;
    }

    public void setGeneratorTables(String generatorTables) {
        this.generatorTables = generatorTables;
        ConfigTable.GENERATOR_TABLES = generatorTables;
    }

    public DataSourceProperty getMongodb() {
        return mongodb;
    }

    public void setMongodb(DataSourceProperty mongodb) {
        this.mongodb = mongodb;
    }

    public DataSourceProperty getElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(DataSourceProperty elasticsearch) {
        this.elasticsearch = elasticsearch;
    }


    public Map<String, DataSourceProperty> getDatasource() {
        return datasource;
    }

    public void setDatasource(Map<String, DataSourceProperty> datasource) {
        this.datasource = datasource;
    }

    public String getDatasourceList() {
        return datasourceList;
    }

    public void setDatasourceList(String datasourceList) {
        this.datasourceList = datasourceList;
    }

}
