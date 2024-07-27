/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anyline.environment.boot;

import org.anyline.environment.boot.datasource.DataSourceProperty;
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
     * anyline.datasource-list=crm, erp<br/>
     * 设置好列表后，为每个数据源设置连接参数<br/>
     * anyline.datasource.crm.url=...<br/>
     * anyline.datasource.erp.url=...<br/>
     */
    protected String datasourceList;
    /**
     * 多数据源配置
     *  anyline.datasource.{数据源key}.url=...
     */
    protected Map<String, DataSourceProperty> datasources;
    /**
     * 用来配置默认的 mongodb 数据源，如果还有其他数据源(包括JDBC)可以合并到dataSource
     */
    protected DataSourceProperty mongodb;
    /**
     * 用来配置默认的 elasticsearch 数据源，如果还有其他数据源(包括JDBC)可以合并到dataSource
     */
    protected DataSourceProperty elasticsearch;
    
    /**
     * debug状态会输出更多日志
     */
    protected boolean debug 									= true			;	
    /**
     * 执行sql时是否输出日志
     */
    protected boolean logSql								    = ConfigTable.IS_LOG_SQL			;
    protected boolean logSlowSql                               = ConfigTable.IS_LOG_SLOW_SQL          ;   
    /**
     * 是否抛出convert异常提示
     */
    protected boolean throwConvertException					    = ConfigTable.IS_THROW_CONVERT_EXCEPTION			;   
    /**
     * 捕捉但未抛出的异常是否显示详细信息
     */
    protected boolean printExceptionStackTrace					= ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE			;   

    /**
     * 慢sql, 如果配置了>0的毫秒数, 在sql执行超出时限后会输出日志, 并调用DMListener.slow
     */
    protected long slowSqlMillis								= ConfigTable.SLOW_SQL_MILLIS		    ; 	
    /**
     * 执行sql时是否输出参数日志
     */
    protected boolean logSqlParam								= ConfigTable.IS_LOG_SQL_PARAM			;
    /**
     * 执行sql异常时是否输出日志
     */
    protected boolean logSqlWhenError						    = ConfigTable.IS_LOG_SQL_WHEN_ERROR			;
    /**
     * 执行sql异常时是否输出参数日志
     */
    protected boolean logSqlParamWhenError					    = ConfigTable.IS_LOG_SQL_PARAM_WHEN_ERROR			;
    /**
     * 加载自定义sql时是否输出日志
     */
    protected boolean sqlDebug	 								= ConfigTable.IS_SQL_DEBUG			;

    protected boolean sqlLogPlaceholder                         = ConfigTable.IS_SQL_LOG_PLACEHOLDER          ;
    /**
     * 调用http接口时是否出输出日志
     */
    protected boolean httpLog 									= ConfigTable.IS_HTTP_LOG			;
    /**
     * http参数是否需要解码 0:自动识别 1:确认编码 -1:确认未编码
     */
    protected int httpParamEncode								= ConfigTable.HTTP_PARAM_ENCODE			    ;
    /**
     * 如果有多数据源为每个数据源生成独立的service
     */
    protected boolean multipleService                            = ConfigTable.IS_MULTIPLE_SERVICE         ;
    /**
     * 是否开启默认的jdbc adapter(仅支持部分标准SQL)遇到没有实现adapter的数据库时可以开启
     */
    protected boolean enableCommonJdbcAdapter                   = ConfigTable.IS_ENABLE_COMMON_JDBC_ADAPTER        ;
    /**
     * 否将数据库中与Java bytes[]对应的类型自动转换如Point > double[](返回DataRow时受此开关景程)
     */
    protected boolean autoConvertBytes 							 = ConfigTable.IS_AUTO_CONVERT_BYTES		    ;

    /**
     * 查询元数据时忽略大小写
     */
    protected boolean metadataIgnoreCase                        = ConfigTable.IS_METADATA_IGNORE_CASE          ;
    /**
     * DataRow是否自动转换成大写
     */
    protected boolean upperKey 								    = ConfigTable.IS_UPPER_KEY			;
    /**
     * DataRow是否自动转换成小写
     */
    protected boolean lowerKey 								    = ConfigTable.IS_LOWER_KEY			;
    /**
     * DataRow是否忽略大小写
     */
    protected boolean keyIgnoreCase 							= ConfigTable.IS_KEY_IGNORE_CASE			;
    /**
     * sql查询异常时是否抛出
     */
    protected boolean throwSqlQueryException 					= ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION			;
    /**
     * 命令执行异常时是否抛出
     */
    protected boolean throwSqlUpdateException 				    = ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION			;
    /**
     * http参数值是否自动trim
     */
    protected boolean httpParamAutoTrim						    = ConfigTable.IS_HTTP_PARAM_AUTO_TRIM		    ;
    /**
     * AnylineController.entity(String ck)是否忽略http未提交的key
     */
    protected boolean ignoreEmptyHttpKey						= ConfigTable.IS_IGNORE_EMPTY_HTTP_KEY			;
    /**
     * DataRow是否更新nul值的列
     */
    protected boolean updateNullColumn							= ConfigTable.IS_UPDATE_NULL_COLUMN			;
    /**
     * DataRow是否更新空值的列
     */
    protected boolean updateEmptyColumn						    = ConfigTable.IS_UPDATE_EMPTY_COLUMN			;
    /**
     * DataRow是否更新nul值的列
     */
    protected boolean insertNullColumn							= ConfigTable.IS_INSERT_NULL_COLUMN			;
    /**
     * DataRow是否更新空值的列
     */
    protected boolean insertEmptyColumn						    = ConfigTable.IS_INSERT_EMPTY_COLUMN			;
    /**
     * Entity是否更新nul值的属性
     */
    protected boolean updateNullField							= ConfigTable.IS_UPDATE_NULL_FIELD			;
    /**
     * Entity是否更新空值的属性
     */
    protected boolean updateEmptyField						    = ConfigTable.IS_UPDATE_EMPTY_FIELD			;
    /**
     * 是否把""替换成null
     */
    protected boolean replaceEmptyNull                          = ConfigTable.IS_REPLACE_EMPTY_NULL          ;
    /**
     * Entity是否更新nul值的属性
     */
    protected boolean insertNullField							= ConfigTable.IS_INSERT_NULL_FIELD			;
    /**
     * Entity是否更新空值的属性
     */
    protected boolean insertEmptyField						    = ConfigTable.IS_INSERT_EMPTY_FIELD			;
    /**
     * List/Array转换成String后的格式 concat:A, B, C json:["A", "B", "C"]
     */
    protected String list2stringFormat                          = ConfigTable.LIST2STRING_FORMAT		;
    /**
     * 是否禁用查询缓存
     */
    protected boolean cacheDisabled                             = false         ;   
    /**
     * 禁用默认的entity adapter
     */
    protected boolean disabledDefaultEntityAdapter              = false         ;   
    /**
     * 是否开启 界定符
     */
    protected boolean sqlDelimiterOpen 						    = false			;	
    /**
     * 是否自动检测关键字
     */
    protected boolean autoCheckKeyword                          = true;         ;
    /**
     * 是否自动检测el值
     */
    protected boolean autoCheckElValue                          = true;         ;
    /**
     * 是否开启 界定符的占位符
     */
    protected boolean sqlDelimiterPlaceholderOpen 			    = false			;	
    /**
     * 界定符的点位符
     */
    protected String sqlDelimiterPlaceholder					= "`"		    ;   
    protected boolean returnEmptyStringReplaceNull               = false         ;
    /**
     * service.query() dataset.getRow()返回null时, 是否替换成new DataRow(), new entity()
     */
    protected boolean returnEmptyInstanceReplaceNull			= false			;	
    /**
     * 更新数据库时，是把自动把数组/集合类型拆分
     */
    protected boolean autoSplitArray						    = true			;	

    /**
     * insert update 时是否自动检测表结构(删除表中不存在的属性)
     */
    protected boolean autoCheckMetadata						    = false		    ; 	
    /**
     * 查询返回空DataSet时，是否检测元数据信息
     */
    protected boolean checkEmptySetMetadata                     = false         ;   
    /**
     * DataRow row = entity("ID:id") 如果参数(如request)中未提供id参数时, row中是否清空ID属性
     */
    protected boolean removeEmptyHttpKey                        = false         ;   

    /**
     * ddl修改列异常后 0:中断修改 1:删除列 n:总行数小于多少时更新值否则触发另一个监听
     */
    protected int afterAlterColumnExceptionAction				= 1000		    ;   

    /**
     * 等待 查询SQL 语句完成的最大时间(s), 超过则抛出异常
     */
    protected int sqlQueryTimeout                               = -1            ;   
    /**
     * 等待 更新SQL 语句完成的最大时间(s), 超过则抛出异常
     */
    protected int sqlUpdateTimeout                              = -1            ;   
    /**
     * ddl执行时是否自动删除定义中不存在的列
     */
    protected boolean ddlAutoDropColumn						    = false		    ;   
    /**
     * 查询列时，是否自动检测主键标识
     */
    protected boolean metadataAutoCheckColumnPrimary			= false			;   
    /**
     * 自定义SQL目录(包括MyBatis) 默认${classpath}/sql .表示项目根目录 ${classpath}表示classes目录
     */
    protected String sqlStoreDir								= null			;	
    /**
     * 是否开始解析mybatis定义的SQL
     */
    protected boolean openParseMybatis							= true		    ; 	
    /**
     * 实体属性 与数据库表列名对照时 默认属性小驼峰转下划线 userName > USER_NAME
     */
    protected String entityFieldColumnMap                       = "camel_"      ;   
    /**
     * 实体类名 与数据库表名对照时 默认属性大驼峰转下划线 CrmUser > CRM_USER
     */
    protected String entityClassTableMap						= "Camel_"  	;	
    /**
     * 表名注解
     */
    protected String entityTableAnnotation						= null		    ;   
    /**
     * 列名注解
     */
    protected String entityColumnAnnotation					    = null			;	
    /**
     * 主键注解(逗号分隔, 不区分大小写, 支持正则匹配) tableId.value, id.name, id(如果不指定注解属性名则依次按name, value解析)
     */
    protected String entityPrimaryKeyAnnotation				    = null		    ;   

    /**
     * 实体类属性依赖层级 > 0:查询属性关联表
     */
    protected int entityFieldSelectDependency = 0             ;   

    protected String entityFieldSelectDependencyCompare         = "IN"         ;   
    /**
     * 实体类属性依赖层级 > 0:插入属性关联表
     */
    protected int entityFieldInsertDependency = 0             ;   

    /**
     * 实体类属性依赖层级 > 0:更新属性关联表
     */
    protected int entityFieldUpdateDependency = 0             ;   

    /**
     * 实体类属性依赖层级 > 0:删除属性关联表
     */
    protected int entityFieldDeleteDependency = 0             ;   
    
    /**
     *是否忽略查询结果中顶层的key,可能返回多个结果集
     * 0-不忽略
     * 1-忽略
     * 2-如果1个结果集则忽略 多个则保留
     */
    protected int ignoreGraphQueryResultTopKey = ConfigTable.IGNORE_GRAPH_QUERY_RESULT_TOP_KEY;
    /**
     * 是否忽略查询结果中的表名,数据可能存在于多个表中
     * 0-不忽略 CRM_USER.id
     * 1-忽略 id
     * 2-如果1个表则忽略 多个表则保留
     */
    protected int ignoreGraphQueryResultTable = ConfigTable.IGNORE_GRAPH_QUERY_RESULT_TABLE;
    /**
     * 是否合并查询结果中的表,合并后会少一层表名被合并到key中(如果不忽略表名)
     * 0-不合并 {"HR_USER":{"name":"n22","id":22},"CRM_USER":{"name":"n22","id":22}}
     * 1-合并  {"HR_USER.name":"n22","HR_USER.id":22,"CRM_USER.name":"n22","CRM_USER.id":22}}
     * 2-如果1个表则合并 多个表则不合并
     */
    protected int mergeGraphQueryResultTable = ConfigTable.MERGE_GRAPH_QUERY_RESULT_TABLE;

    /**
     * http参数格式 camel:小驼峰 Camel:大驼峰 lower:小写 upper:大写  service.column2param会把 USER_NAME 转成userName
     */
    protected String httpParamKeyCase							= "camel"		;	
    /**
     * 表结构缓存key
     */
    protected String tableMetadataCacheKey					    = ""			;	
    /**
     * 表结构缓存时间(没有设置缓存key的情况下生效)(-1:表示永不失效)
     */
    protected int tableMetadataCacheSecond						= 3600*24		;	
    /**
     * MixUtil.mix默认seed
     */
    protected String mixDefaultSeed                             = "al"          ;   
    protected String elAttributePrefix      					= "al"		    ;

    /**
     * 默认主键
     */
    protected String defaultPrimaryKey							= "id"			;	
    /**
     * 是否需要提供主事务管理器, 多数据源时需要
     */
    protected boolean openPrimaryTransactionManager             = false         ;   
    /**
     * 是否需要提供主管理器, 会根据数据源生成相应的事务管理器
     */
    protected boolean openTransactionManager                    = true         ;   
    /**
     * 主键生成器机器id
     */
    public int primaryGeneratorWorkerId					        = 1				;	
    /**
     * 主键前缀(随机主键)
     */
    public String primaryGeneratorPrefix					    = ""			;	
    /**
     * 主随机主键总长度
     */
    public int primaryGeneratorRandomLength				        = 32			;	
    /**
     * 生成主键大写
     */
    public boolean primaryGeneratorUpper					    = true			;	
    /**
     * 生成主键小写
     */
    public boolean primaryGeneratorLower					    = false			;	
    /**
     * 生成主键日期格式(默认yyyyMMddhhmmssSSS)
     */
    public String primaryGeneratorTimeFormat					= null		    ;	
    /**
     * 生成主键time/timestamp后缀随机数长度
     */
    public int primaryGeneratorTimeSuffixLength				    = 3			    ;   
    /**
     * 雪花算法开始日期
     */
    public String snowflakeTwepoch                              = "2000-01-01"  ;   
    /**
     * 是否开启默认的主键生成器(UUID)
     */
    public boolean primaryGeneratorUuidActive			        = false			;	
    /**
     * 是否开启默认的主键生成器(雪花)
     */
    public boolean primaryGeneratorSnowflakeActive		        = false			;	
    /**
     * 是否开启默认的主键生成器(随机)
     */
    public boolean primaryGeneratorRandomActive			        = false			;	
    /**
     * 是否开启默认的主键生成器(时间戳)
     */
    public boolean primaryGeneratorTimestampActive			    = false			;	
    /**
     * 是否开启默认的主键生成器(年月日时分秒毫秒)
     */
    public boolean primaryGeneratorTimeActive					= false			;	

    public PrimaryGenerator.GENERATOR generator                = null          ;   

    public String generatorTables                               = "*"           ;   

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

    public boolean isAutoCheckElValue() {
        return autoCheckElValue;
    }

    public void setAutoCheckElValue(boolean autoCheckElValue) {
        this.autoCheckElValue = autoCheckElValue;
        ConfigTable.IS_AUTO_CHECK_EL_VALUE = autoCheckElValue;
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

    public int getSqlQueryTimeout() {
        return sqlQueryTimeout;
    }

    public void setSqlQueryTimeout(int sqlQueryTimeout) {
        this.sqlQueryTimeout = sqlQueryTimeout;
        ConfigTable.SQL_QUERY_TIMEOUT = sqlQueryTimeout;
    }

    public int getSqlUpdateTimeout() {
        return sqlUpdateTimeout;
    }

    public void setSqlUpdateTimeout(int sqlUpdateTimeout) {
        this.sqlUpdateTimeout = sqlUpdateTimeout;
        ConfigTable.SQL_UPDATE_TIMEOUT = sqlUpdateTimeout;
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
        ConfigTable.METADATA_CACHE_KEY = tableMetadataCacheKey;
    }

    public int getTableMetadataCacheSecond() {
        return tableMetadataCacheSecond;
    }

    public void setTableMetadataCacheSecond(int tableMetadataCacheSecond) {
        this.tableMetadataCacheSecond = tableMetadataCacheSecond;
        ConfigTable.METADATA_CACHE_SECOND = tableMetadataCacheSecond;
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

    public void setGeneratorWorkerId(int primaryGeneratorWorkerId) {
        this.primaryGeneratorWorkerId = primaryGeneratorWorkerId;
        ConfigTable.PRIMARY_GENERATOR_WORKER_ID = primaryGeneratorWorkerId;
    }

    public String getPrimaryGeneratorPrefix() {
        return primaryGeneratorPrefix;
    }

    public void setGeneratorPrefix(String primaryGeneratorPrefix) {
        this.primaryGeneratorPrefix = primaryGeneratorPrefix;
        ConfigTable.PRIMARY_GENERATOR_PREFIX = primaryGeneratorPrefix;
    }

    public int getPrimaryGeneratorRandomLength() {
        return primaryGeneratorRandomLength;
    }

    public void setGeneratorRandomLength(int primaryGeneratorRandomLength) {
        this.primaryGeneratorRandomLength = primaryGeneratorRandomLength;
        ConfigTable.PRIMARY_GENERATOR_RANDOM_LENGTH = primaryGeneratorRandomLength;
    }

    public boolean isPrimaryGeneratorUpper() {
        return primaryGeneratorUpper;
    }

    public void setGeneratorUpper(boolean primaryGeneratorUpper) {
        this.primaryGeneratorUpper = primaryGeneratorUpper;
        ConfigTable.PRIMARY_GENERATOR_UPPER = primaryGeneratorUpper;
    }

    public boolean isPrimaryGeneratorLower() {
        return primaryGeneratorLower;
    }

    public void setGeneratorLower(boolean primaryGeneratorLower) {
        this.primaryGeneratorLower = primaryGeneratorLower;
        ConfigTable.PRIMARY_GENERATOR_LOWER = primaryGeneratorLower;
    }

    public boolean isPrimaryGeneratorUuidActive() {
        return primaryGeneratorUuidActive;
    }

    public void setGeneratorUuidActive(boolean primaryGeneratorUuidActive) {
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

    public void setGeneratorSnowflakeActive(boolean primaryGeneratorSnowflakeActive) {
        this.primaryGeneratorSnowflakeActive = primaryGeneratorSnowflakeActive;
        ConfigTable.PRIMARY_GENERATOR_SNOWFLAKE_ACTIVE = primaryGeneratorSnowflakeActive;
    }

    public boolean isPrimaryGeneratorRandomActive() {
        return primaryGeneratorRandomActive;
    }

    public void setGeneratorRandomActive(boolean primaryGeneratorRandomActive) {
        this.primaryGeneratorRandomActive = primaryGeneratorRandomActive;
        ConfigTable.PRIMARY_GENERATOR_RANDOM_ACTIVE = primaryGeneratorRandomActive;
    }

    public String getPrimaryGeneratorTimeFormat() {
        return primaryGeneratorTimeFormat;
    }

    public void setGeneratorTimeFormat(String primaryGeneratorTimeFormat) {
        this.primaryGeneratorTimeFormat = primaryGeneratorTimeFormat;
        ConfigTable.PRIMARY_GENERATOR_TIME_FORMAT = primaryGeneratorTimeFormat;
    }

    public int getPrimaryGeneratorTimeSuffixLength() {
        return primaryGeneratorTimeSuffixLength;
    }

    public void setGeneratorTimeSuffixLength(int primaryGeneratorTimeSuffixLength) {
        this.primaryGeneratorTimeSuffixLength = primaryGeneratorTimeSuffixLength;
        ConfigTable.PRIMARY_GENERATOR_TIME_SUFFIX_LENGTH = primaryGeneratorTimeSuffixLength;
    }

    public boolean isPrimaryGeneratorTimestampActive() {
        return primaryGeneratorTimestampActive;
    }

    public void setGeneratorTimestampActive(boolean primaryGeneratorTimestampActive) {
        this.primaryGeneratorTimestampActive = primaryGeneratorTimestampActive;
        ConfigTable.PRIMARY_GENERATOR_TIMESTAMP_ACTIVE = primaryGeneratorTimestampActive;
    }

    public boolean isPrimaryGeneratorTimeActive() {
        return primaryGeneratorTimeActive;
    }

    public void setGeneratorTimeActive(boolean primaryGeneratorTimeActive) {
        this.primaryGeneratorTimeActive = primaryGeneratorTimeActive;
        ConfigTable.PRIMARY_GENERATOR_TIME_ACTIVE = primaryGeneratorTimeActive;
    }

    public boolean isAutoConvertBytes() {
        return autoConvertBytes;
    }

    public boolean isEnableCommonJdbcAdapter() {
        return enableCommonJdbcAdapter;
    }

    public void setEnableCommonJdbcAdapter(boolean enableCommonJdbcAdapter) {
        this.enableCommonJdbcAdapter = enableCommonJdbcAdapter;
        ConfigTable.IS_ENABLE_COMMON_JDBC_ADAPTER = enableCommonJdbcAdapter;
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
        if(null != generators) {
            for(String key:generators.keySet()) {
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

    public int getIgnoreGraphQueryResultTopKey() {
        return ignoreGraphQueryResultTopKey;
    }

    public void setIgnoreGraphQueryResultTopKey(int ignoreGraphQueryResultTopKey) {
        this.ignoreGraphQueryResultTopKey = ignoreGraphQueryResultTopKey;
        ConfigTable.IGNORE_GRAPH_QUERY_RESULT_TOP_KEY = ignoreGraphQueryResultTopKey;
    }

    public int getIgnoreGraphQueryResultTable() {
        return ignoreGraphQueryResultTable;
    }

    public void setIgnoreGraphQueryResultTable(int ignoreGraphQueryResultTable) {
        this.ignoreGraphQueryResultTable = ignoreGraphQueryResultTable;
        ConfigTable.IGNORE_GRAPH_QUERY_RESULT_TABLE = ignoreGraphQueryResultTable;
    }

    public int getMergeGraphQueryResultTable() {
        return mergeGraphQueryResultTable;
    }

    public void setMergeGraphQueryResultTable(int mergeGraphQueryResultTable) {
        this.mergeGraphQueryResultTable = mergeGraphQueryResultTable;
        ConfigTable.MERGE_GRAPH_QUERY_RESULT_TABLE = mergeGraphQueryResultTable;
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

    public Map<String, DataSourceProperty> getDatasources() {
        return datasources;
    }

    public void setDataSources(Map<String, DataSourceProperty> datasources) {
        this.datasources = datasources;
    }

    public String getDatasourceList() {
        return datasourceList;
    }

    public void setDatasourceList(String datasourceList) {
        this.datasourceList = datasourceList;
    }

}
