package org.anyline.boot;

import org.anyline.util.ConfigTable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "anyline")
public class AnylineProperty {
    //以下属性与ConfigTable一一对应

    /**
     * debug状态会输出更多日志
     */
    protected boolean debug 									= true		;	// debug状态会输出更多日志
    /**
     * 执行sql时是否输出日志
     */
    protected boolean showSql									= true		;	// 执行sql时是否输出日志
    /**
     * 慢sql,如果配置了>0的毫秒数,在sql执行超出时限后会输出日志,并调用DMListener.slow
     */
    protected long slowSqlMillis								= 0			; 	// 慢sql,如果配置了>0的毫秒数,在sql执行超出时限后会输出日志,并调用dmlistener.slow
    /**
     * 执行sql时是否输出参数日志
     */
    protected boolean showSqlParam								= true		;	// 执行sql时是否输出参数日志
    /**
     * 执行sql异常时是否输出日志
     */
    protected boolean showSqlWhenError						    = true		;	// 执行sql异常时是否输出日志
    /**
     * 执行sql异常时是否输出参数日志
     */
    protected boolean showSqlParamWhenError					    = true		;	// 执行sql异常时是否输出参数日志
    /**
     * 加载自定义sql时是否输出日志
     */
    protected boolean sqlDebug	 								= false		;	// 加载自定义sql时是否输出日志
    /**
     * 调用http接口时是否出输出日志
     */
    protected boolean httpLog 									= true		;	// 调用http接口时是否出输出日志
    /**
     * http参数是否需要解码 0:自动识别 1:确认编码 -1:确认未编码
     */
    protected int httpParamEncode								= 0			;   // http参数是否需要解码 0:自动识别 1:确认编码 -1:确认未编码
    /**
     * 如果有多数据源为每个数据源生成独立的service
     */
    protected boolean multipleService                            = true     ;   // 如果有多数据源为每个数据源生成独立的service
    /**
     * DataRow是否自动转换成大写
     */
    protected boolean upperKey 								    = true		;	// DataRow是否自动转换成大写
    /**
     * DataRow是否自动转换成小写
     */
    protected boolean lowerKey 								    = false		;	// DataRow是否自动转换成小写
    /**
     * DataRow是否忽略大小写
     */
    protected boolean keyIgnoreCase 							= true		;	// DataRow是否忽略大小写
    /**
     * sql查询异常时是否抛出
     */
    protected boolean throwSqlQueryException 					= true		;	// sql查询异常时是否抛出
    /**
     * sql执行异常时是否抛出
     */
    protected boolean throwSqlUpdateException 				    = true		;	// sql执行异常时是否抛出
    /**
     * http参数值是否自动trim
     */
    protected boolean httpParamAutoTrim						    = true		;   // http参数值是否自动trim
    /**
     * AnylineController.entity(String ck)是否忽略http未提交的key
     */
    protected boolean ignoreEmptyHttpKey						= false		;	// AnylineController.entity(String ck)是否忽略http未提交的key
    /**
     * DataRow是否更新nul值的列
     */
    protected boolean updateNullColumn							= false		;	// DataRow是否更新nul值的列
    /**
     * DataRow是否更新空值的列
     */
    protected boolean updateEmptyColumn						    = false		;	// DataRow是否更新空值的列
    /**
     * DataRow是否更新nul值的列
     */
    protected boolean insertNullColumn							= false		;	// DataRow是否更新nul值的列
    /**
     * DataRow是否更新空值的列
     */
    protected boolean insertEmptyColumn						    = false		;	// DataRow是否更新空值的列
    /**
     * Entity是否更新nul值的属性
     */
    protected boolean updateNullField							= false		;	// Entity是否更新nul值的属性
    /**
     * Entity是否更新空值的属性
     */
    protected boolean updateEmptyField						    = false		;	// Entity是否更新空值的属性
    /**
     * Entity是否更新nul值的属性
     */
    protected boolean insertNullField							= false		;	// Entity是否更新nul值的属性
    /**
     * Entity是否更新空值的属性
     */
    protected boolean insertEmptyField						    = false		;	// Entity是否更新空值的属性
    /**
     * 是否禁用查询缓存
     */
    protected boolean cacheDisabled                             = false     ;   // 是否禁用查询缓存
    /**
     * 禁用默认的entity adapter
     */
    protected boolean disabledDefaultEntityAdapter              = false     ;   // 禁用默认的entity adapter
    /**
     * 是否开启 界定符
     */
    protected boolean sqlDelimiterOpen 						    = false		;	// 是否开启 界定符
    /**
     * 是否开启 界定符的占位符
     */
    protected boolean sqlDelimiterPlaceholderOpen 			    = false		;	// 是否开启 界定符的占位符
    /**
     * 界定符的点位符
     */
    protected String sqlDelimiterPlaceholder					= "`"		;   // 界定符的点位符
    /**
     * service.query() dataset.getRow()返回null时,是否替换成new DataRow(), new entity()
     */
    protected boolean returnEmptyInstanceReplaceNull			= false		;	// service.query() dataset.getRow()返回null时,是否替换成new DataRow(), new entity()
    /**
     * insert update 时是否自动检测表结构(删除表中不存在的属性)
     */
    protected boolean autoCheckMetadata						    = false		; 	// insert update 时是否自动检测表结构(删除表中不存在的属性)
    /**
     * DataRow row = entity("ID:id") 如果参数(如request)中未提供id参数时,row中是否清空ID属性
     */
    protected boolean removeEmptyHttpKey                        = false     ;   // DataRow row = entity("ID:id") 如果参数(如request)中未提供id参数时,row中是否清空ID属性
    /**
     * 默认主键
     */
    protected String defaultPrimaryKey							= "id"		;	// 默认主键
    /**
     * 主键生成器机器id
     */
    public int primaryGeneratorWorkerId					        = 1			;	// 主键生成器机器id
    /**
     * 主键前缀(随机主键)
     */
    public String primaryGeneratorPrefix					    = ""		;	// 主键前缀(随机主键)
    /**
     * 主随机主键总长度
     */
    public int primaryGeneratorRandomLength				        = 32		;	// 主随机主键总长度
    /**
     * 生成主键大写
     */
    public boolean primaryGeneratorUpper					    = true		;	// 生成主键大写
    /**
     * 生成主键小写
     */
    public boolean primaryGeneratorLower					    = false		;	// 生成主键小写
    /**
     * 生成主键日期格式(默认yyyyMMddhhmmssSSS)
     */
    public String primaryGeneratorTimeFormat					= null		;	// 生成主键日期格式(默认yyyyMMddhhmmssSSS)
    /**
     * 生成主键time/timestamp后缀随机数长度
     */
    public int primaryGeneratorTimeSuffixLength				    = 3			;   // 生成主键time/timestamp后缀随机数长度
    /**
     * 是否开启默认的主键生成器(UUID)
     */
    public boolean primaryGeneratorUuidActive			        = false		;	// 是否开启默认的主键生成器(UUID)
    /**
     * 是否开启默认的主键生成器(雪花)
     */
    public boolean primaryGeneratorSnowflakeActive		        = false		;	// 是否开启默认的主键生成器(雪花)
    /**
     * 是否开启默认的主键生成器(随机)
     */
    public boolean primaryGeneratorRandomActive			        = false		;	// 是否开启默认的主键生成器(随机)
    /**
     * 是否开启默认的主键生成器(时间戳)
     */
    public boolean primaryGeneratorTimestampActive			    = false		;	// 是否开启默认的主键生成器(时间戳)
    /**
     * 是否开启默认的主键生成器(年月日时分秒毫秒)
     */
    public boolean primaryGeneratorTimeActive					= false		;	// 是否开启默认的主键生成器(年月日时分秒毫秒)

    /**
     * ddl修改列异常后 0:中断修改 1:删除列 n:总行数小于多少时更新值否则触发另一个监听
     */
    protected int afterAlterColumnExceptionAction				= 1000		;   // ddl修改列异常后 0:中断修改 1:删除列 n:总行数小于多少时更新值否则触发另一个监听
    /**
     * ddl执行时是否自动删除定义中不存在的列
     */
    protected boolean ddlAutoDropColumn						    = false		;   // ddl执行时是否自动删除定义中不存在的列
    /**
     * 自定义SQL目录(包括MyBatis) 默认${classpath}/sql .表示项目根目录 ${classpath}表示classes目录
     */
    protected String sqlStoreDir								= null		;	// 自定义SQL目录(包括MyBatis) 默认${classpath}/sql .表示项目根目录 ${classpath}表示classes目录
    /**
     * 是否开始解析mybatis定义的SQL
     */
    protected boolean openParseMybatis							= true		; 	// 是否开始解析mybatis定义的SQL
    /**
     * 实体属性 与数据库表列名对照时 默认属性小驼峰转下划线 userName > USER_NAME
     */
    protected String entityFieldColumnMap                       = "camel_"  ;   // 实体属性 与数据库表列名对照时 默认属性小驼峰转下划线 userName > USER_NAME
    /**
     * 表名注解
     */
    protected String entityTableAnnotation						= null		;   // 表名注解
    /**
     * 列名注解
     */
    protected String entityColumnAnnotation					    = null		;	// 列名注解
    /**
     * 主键注解(逗号分隔,不区分大小写,支持正则匹配) TableId.value,id.name,id(如果不指定注解属性名则依次按name,value解析)
     */
    protected String entityPrimaryKeyAnnotation				    = null		;   // 主键注解(逗号分隔,不区分大小写,支持正则匹配) TableId.value,id.name,id(如果不指定注解属性名则依次按name,value解析)
    /**
     * http参数格式 camel:小驼峰 Camel:大驼峰 lower:小写 upper:大写  service.column2param会把 USER_NAME 转成userName
     */
    protected String httpParamKeyCase							= "camel"	;	// http参数格式 camel:小驼峰 Camel:大驼峰 lower:小写 upper:大写  service.column2param会把 USER_NAME 转成userName
    /**
     * 表结构缓存key
     */
    protected String tableMetadataCacheKey					    = ""		;	// 表结构缓存key
    /**
     * 表结构缓存时间(没有设置缓存key的情况下生效)(-1:表示永不失效)
     */
    protected int tableMetadataCacheSecond						= 3600*24	;	// 表结构缓存时间(没有设置缓存key的情况下生效)(-1:表示永不失效)
    /**
     * MixUtil.mix默认seed
     */
    protected String mixDefaultSeed                             = "al"      ;   // MixUtil.mix默认seed
    protected String elAttributePrefix      					= "al"		;
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

    public boolean isMultipleService() {
        return multipleService;
    }

    public void setMultipleService(boolean multipleService) {
        this.multipleService = multipleService;
        ConfigTable.IS_MULTIPLE_SERVICE = multipleService;
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
}
