package org.anyline.boot;

import org.anyline.util.ConfigTable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "anyline")
public class DefaultConfig {
    //以下属性与ConfigTable一一对应

    protected  boolean debug 									= true		;	// debug状态会输出更多日志
    protected  boolean showSql									= true		;	// 执行sql时是否输出日志
    protected  long slowSqlMillis								= 0			; 	// 慢sql,如果配置了>0的毫秒数,在sql执行超出时限后会输出日志,并调用dmlistener.slow
    protected  boolean showSqlParam								= true		;	// 执行sql时是否输出日志
    protected  boolean showSqlWhenError						    = true		;	// 执行sql异常时是否输出日志
    protected  boolean showSqlParamWhenError					= true		;	// 执行sql异常时是否输出日志
    protected  boolean sqlDebug	 								= false		;	// 加载自定义sql时是否输出日志
    protected  boolean httpLog 									= true		;	// 调用http接口时是否出输出日志
    protected  int httpParamEncode								= 0			;   // 0:自动识别 1:确认编码 -1:确认未编码
    protected  boolean upperKey 								= true		;	// 是否自动转换成大写
    protected  boolean lowerKey 								= false		;	// 是否自动转换成小写
    protected  boolean keyIgnoreCase 							= true		;	// 是否忽略大小写
    protected  boolean throwSqlQueryException 					= true		;	// sql执行异常时是否抛出
    protected  boolean throwSqlUpdateException 				    = true		;	// sql执行异常时是否抛出
    protected  boolean updateNullColumn							= false		;	// 是否更新nul值的列
    protected  boolean updateEmptyColumn						= false		;	// 是否更新空值的列
    protected  boolean insertNullColumn							= false		;	// 是否更新nul值的列
    protected  boolean insertEmptyColumn						= false		;	// 是否更新空值的列
    protected  boolean sqlDelimiterOpen 						= false		;	// 是否开启 界定符
    protected  boolean sqlDelimiterPlaceholderOpen 			    = false		;	// 是否开启 界定符的占位符
    protected  boolean returnEmptyInstanceReplaceNull			= false		;	// service.query() dataset.getrow()返回null时,是否替换成new datarow(), new entity()
    protected  boolean autoCheckMetadata						= false		; 	// insert update 时是否自动检测表结构(删除表中不存在的属性)
    protected  String defaultPrimaryKey							= "id"		;	// 默认主键
    protected  int afterAlterColumnExceptionAction				= 1000		;   // ddl修改列异常后 0:中断修改 1:删除列 n:总行数小于多少时更新值否则触发另一个监听
    protected  boolean ddlAutoDropColumn						= false		;   // ddl执行时是否自动删除定义中不存在的列
    protected  String sqlStoreDir								= null		;	//自定义sql目录 默认${classpath}/sql
    protected  String entityTableAnnotation						= null		;   // 表名注解
    protected  String entityColumnAnnotation					= null		;	// 列名注解
    protected  String entityPrimaryKeyAnnotation				= null		;   // 主键注解(逗号分隔，不区分大小写，支持正则匹配) tableid.value,id.name,id(如果不指定注解属性名则依次按name,value解析)
    protected  String httpParamKeyCase							= "camel"	;	// http参数格式 camel:小驼峰 camel:大驼峰 lower:小写 upper:大写  service.column2param会把 userName 转成username
    protected  String tableMetadataCacheKey					    = ""		;	// 表结构缓存key
    protected  int tableMetadataCacheSecond						= 3600*24	;	// 表结构缓存时间(没有设置缓存key的情况下生效)(-1:表示永不失效)
    protected  String sqlDelimiterPlaceholder					= "`"		;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        ConfigTable.IS_DEBUG = debug;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
        ConfigTable.IS_SHOW_SQL = showSql;
    }

    public long getSlowSqlMillis() {
        return slowSqlMillis;
    }

    public void setSlowSqlMillis(long slowSqlMillis) {
        this.slowSqlMillis = slowSqlMillis;
        ConfigTable.SLOW_SQL_MILLIS = slowSqlMillis;
    }

    public boolean isShowSqlParam() {
        return showSqlParam;
    }

    public void setShowSqlParam(boolean showSqlParam) {
        this.showSqlParam = showSqlParam;
        ConfigTable.IS_SHOW_SQL_PARAM = showSqlParam;
    }

    public boolean isShowSqlWhenError() {
        return showSqlWhenError;
    }

    public void setShowSqlWhenError(boolean showSqlWhenError) {
        this.showSqlWhenError = showSqlWhenError;
        ConfigTable.IS_SHOW_SQL_WHEN_ERROR = showSqlWhenError;
    }

    public boolean isShowSqlParamWhenError() {
        return showSqlParamWhenError;
    }

    public void setShowSqlParamWhenError(boolean showSqlParamWhenError) {
        this.showSqlParamWhenError = showSqlParamWhenError;
        ConfigTable.IS_SHOW_SQL_PARAM_WHEN_ERROR = showSqlParamWhenError;
    }

    public boolean isSqlDebug() {
        return sqlDebug;
    }

    public void setSqlDebug(boolean sqlDebug) {
        this.sqlDebug = sqlDebug;
        ConfigTable.IS_SQL_DEBUG = sqlDebug;
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
}
