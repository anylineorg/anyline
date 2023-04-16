package org.anyline.data.listener;

import org.anyline.data.run.Run;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.listener.EntityListener;
import org.anyline.util.ConfigTable;

import java.util.List;

public class ControlHandle {
    /**
     * 执行后返回结果
     */
    private DataSet set;
    private DataRow row;
    private int count;
    private boolean exists;
    private List<Run> runs;

    /**
     * 执行配置
     */
    public DMListener dmListener;
    public DDListener ddListener;
    public EntityListener entityListener;


    public  boolean IS_SHOW_SQL									= ConfigTable.IS_SHOW_SQL								;		// true		;	// 执行SQL时是否输出日志
    public  long SLOW_SQL_MILLIS								= ConfigTable.SLOW_SQL_MILLIS							;		// 0		; 	// 慢SQL,如果配置了>0的毫秒数,在SQL执行超出时限后会输出日志,并调用DMListener.slow
    public  boolean IS_SHOW_SQL_PARAM							= ConfigTable.IS_SHOW_SQL_PARAM							;		// true		;	// 执行SQL时是否输出日志
    public  boolean IS_SHOW_SQL_WHEN_ERROR						= ConfigTable.IS_SHOW_SQL_WHEN_ERROR					;		// true		;	// 执行SQL异常时是否输出日志
    public  boolean IS_SHOW_SQL_PARAM_WHEN_ERROR				= ConfigTable.IS_SHOW_SQL_PARAM_WHEN_ERROR				;		// true		;	// 执行SQL异常时是否输出日志
    public  boolean IS_SQL_DEBUG								= ConfigTable.IS_SQL_DEBUG								;		// false	;	// 加载自定义SQL时是否输出日志
    public  boolean IS_HTTP_LOG									= ConfigTable.IS_HTTP_LOG								;		// true		;	// 调用HTTP接口时是否出输出日志
    public  boolean IS_HTTP_PARAM_AUTO_TRIM						= ConfigTable.IS_HTTP_PARAM_AUTO_TRIM					;		// true		;   // http参数值是否自动trim
    public  boolean IS_IGNORE_EMPTY_HTTP_KEY					= ConfigTable.IS_IGNORE_EMPTY_HTTP_KEY					;		// true		;	// AnylineController.entity(String ck)是否忽略http未提交的key
    public  int HTTP_PARAM_ENCODE								= ConfigTable.HTTP_PARAM_ENCODE							;		// 0		;   // http参数是否解码0:自动识别 1:确认编码 -1:确认未编码
    public  boolean IS_MULTIPLE_SERVICE							= ConfigTable.IS_MULTIPLE_SERVICE						;		// true		;	// 如果有多数据源为每个数据源生成独立的service
    public  boolean IS_AUTO_CONVERT_BYTES						= ConfigTable.IS_AUTO_CONVERT_BYTES						;		// true		;   // 否将数据库中与Java bytes[]对应的类型自动转换如Point > double[](返回DataRow时受此开关景程)
    public  boolean IS_AUTO_SPLIT_ARRAY							= ConfigTable.IS_AUTO_SPLIT_ARRAY						;		// true		;	// 更新数据库时，是把自动把数组/集合类型拆分
    public  boolean IS_UPPER_KEY								= ConfigTable.IS_UPPER_KEY								;		// true		;	// DataRow是否自动转换成大写
    public  boolean IS_LOWER_KEY								= ConfigTable.IS_LOWER_KEY								;		// false	;	// DataRow是否自动转换成小写
    public  boolean IS_KEY_IGNORE_CASE							= ConfigTable.IS_KEY_IGNORE_CASE						;		// true		;	// DataRow是否忽略大小写
    public  boolean IS_THROW_SQL_QUERY_EXCEPTION				= ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION				;		// true		;	// SQL查询异常时是否抛出
    public  boolean IS_THROW_SQL_UPDATE_EXCEPTION				= ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION				;		// true		;	// SQL执行异常时是否抛出
    public  boolean IS_UPDATE_NULL_COLUMN						= ConfigTable.IS_UPDATE_NULL_COLUMN						;		// false	;	// DataRow是否更新nul值的列(针对DataRow)
    public  boolean IS_UPDATE_EMPTY_COLUMN						= ConfigTable.IS_UPDATE_EMPTY_COLUMN					;		// false	;	// DataRow是否更新空值的列
    public  boolean IS_INSERT_NULL_COLUMN						= ConfigTable.IS_INSERT_NULL_COLUMN						;		// false	;	// DataRow是否更新nul值的列
    public  boolean IS_INSERT_EMPTY_COLUMN						= ConfigTable.IS_INSERT_EMPTY_COLUMN					;		// false	;	// DataRow是否更新空值的列
    public  boolean IS_UPDATE_NULL_FIELD						= ConfigTable.IS_UPDATE_NULL_FIELD						;		// false	;	// Entity是否更新nul值的属性(针对Entity)
    public  boolean IS_UPDATE_EMPTY_FIELD						= ConfigTable.IS_UPDATE_EMPTY_FIELD						;		// false	;	// Entity是否更新空值的属性
    public  boolean IS_INSERT_NULL_FIELD						= ConfigTable.IS_INSERT_NULL_FIELD						;		// false	;	// Entity是否更新nul值的属性
    public  boolean IS_INSERT_EMPTY_FIELD						= ConfigTable.IS_INSERT_EMPTY_FIELD						;		// false	;	// Entity是否更新空值的属性
    public  boolean IS_SQL_DELIMITER_OPEN						= ConfigTable.IS_SQL_DELIMITER_OPEN						;		// false	;	// 是否开启 界定符
    public  boolean IS_SQL_DELIMITER_PLACEHOLDER_OPEN			= ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN			;		// false	;	// 是否开启 界定符的占位符
    public  String SQL_DELIMITER_PLACEHOLDER					= ConfigTable.SQL_DELIMITER_PLACEHOLDER					;		// "`"		;	// 界定符的占位符
    public  boolean IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL		= ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL		;		// false	;	// service.query() DataSet.getRow()返回null时,是否替换成new DataRow(), new Entity()
    public  boolean IS_AUTO_CHECK_METADATA						= ConfigTable.IS_AUTO_CHECK_METADATA					;		// false	; 	// insert update 时是否自动检测表结构(删除表中不存在的属性)
    public  boolean IS_DISABLED_DEFAULT_ENTITY_ADAPTER			= ConfigTable.IS_DISABLED_DEFAULT_ENTITY_ADAPTER		;		// false	; 	// 禁用默认的entity adapter
    public  boolean IS_REMOVE_EMPTY_HTTP_KEY					= ConfigTable.IS_REMOVE_EMPTY_HTTP_KEY					;		// true		;   // DataRow row = entity("ID:id") 如果参数(如request)中未提供id参数时,row中是否清空ID属性
    public  boolean IS_CACHE_DISABLED							= ConfigTable.IS_CACHE_DISABLED							;		// false	; 	// 是否禁用查询缓存
    public  String DEFAULT_PRIMARY_KEY							= ConfigTable.DEFAULT_PRIMARY_KEY						;		// "ID"		;	// 默认主键
    public  int PRIMARY_GENERATOR_WORKER_ID						= ConfigTable.PRIMARY_GENERATOR_WORKER_ID				;		// 1		;	// 主键生成器机器ID
    public  String PRIMARY_GENERATOR_PREFIX						= ConfigTable.PRIMARY_GENERATOR_PREFIX					;		// ""		;	// 主键前缀(随机主键)
    public  int PRIMARY_GENERATOR_RANDOM_LENGTH					= ConfigTable.PRIMARY_GENERATOR_RANDOM_LENGTH			;		// 32		;	// 主随机主键总长度
    public  boolean PRIMARY_GENERATOR_UPPER						= ConfigTable.PRIMARY_GENERATOR_UPPER					;		// true		;	// 生成主键大写
    public  boolean PRIMARY_GENERATOR_LOWER						= ConfigTable.PRIMARY_GENERATOR_LOWER					;		// false	;	// 生成主键小写
    public  String PRIMARY_GENERATOR_TIME_FORMAT				= ConfigTable.PRIMARY_GENERATOR_TIME_FORMAT				;		// null		;	// 生成主键日期格式(默认yyyyMMddHHmmssSSS)
    public  int PRIMARY_GENERATOR_TIME_SUFFIX_LENGTH			= ConfigTable.PRIMARY_GENERATOR_TIME_SUFFIX_LENGTH		;		// 3		;   // 生成主键TIME/TIMESTAMP后缀随机数长度
    public  boolean PRIMARY_GENERATOR_UUID_ACTIVE				= ConfigTable.PRIMARY_GENERATOR_UUID_ACTIVE				;		// false	;	// 是否开启默认的主键生成器(UUID)
    public  boolean PRIMARY_GENERATOR_SNOWFLAKE_ACTIVE			= ConfigTable.PRIMARY_GENERATOR_SNOWFLAKE_ACTIVE		;		// false	;	// 是否开启默认的主键生成器(雪花)
    public  boolean PRIMARY_GENERATOR_RANDOM_ACTIVE				= ConfigTable.PRIMARY_GENERATOR_RANDOM_ACTIVE			;		// false	;	// 是否开启默认的主键生成器(随机)
    public  boolean PRIMARY_GENERATOR_TIMESTAMP_ACTIVE			= ConfigTable.PRIMARY_GENERATOR_TIMESTAMP_ACTIVE		;		// false	;	// 是否开启默认的主键生成器(时间戳)
    public  boolean PRIMARY_GENERATOR_TIME_ACTIVE				= ConfigTable.PRIMARY_GENERATOR_TIME_ACTIVE				;		// false	;	// 是否开启默认的主键生成器(年月日时分秒毫秒)

    public  int AFTER_ALTER_COLUMN_EXCEPTION_ACTION				= ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION		;		// 1000		;   // DDL修改列异常后 0:中断修改 1:删除列 n:总行数小于多少时更新值否则触发另一个监听
    public  boolean IS_DDL_AUTO_DROP_COLUMN						= ConfigTable.IS_DDL_AUTO_DROP_COLUMN					;		// false	;   // DDL执行时是否自动删除定义中不存在的列
    public  String SQL_STORE_DIR								= ConfigTable.SQL_STORE_DIR								;		// null		;	// 自定义SQL目录(包括MyBatis) 默认${classpath}/sql .表示项目根目录 ${classpath}表示classes目录
    public  boolean IS_OPEN_PARSE_MYBATIS						= ConfigTable.IS_OPEN_PARSE_MYBATIS						;		// true		; 	// 是否开始解析mybatis定义的SQL
    public  String ENTITY_FIELD_COLUMN_MAP						= ConfigTable.ENTITY_FIELD_COLUMN_MAP					;		// "camel_" ;	// 实体属性 与数据库表列名对照时 默认属性小驼峰转下划线 userName > USER_NAME
    public  String ENTITY_CLASS_TABLE_MAP						= ConfigTable.ENTITY_CLASS_TABLE_MAP                    ;	    // 实体类名 与数据库表名对照时 默认属性大驼峰转下划线 CrmUser > CRM_USER
    public  String ENTITY_TABLE_ANNOTATION						= ConfigTable.ENTITY_TABLE_ANNOTATION					;		// null		;   // 表名注解
    public  String ENTITY_COLUMN_ANNOTATION						= ConfigTable.ENTITY_COLUMN_ANNOTATION					;		// null		;	// 列名注解
    public  String ENTITY_PRIMARY_KEY_ANNOTATION				= ConfigTable.ENTITY_PRIMARY_KEY_ANNOTATION				;		// null		;   // 主键注解(逗号分隔,不区分大小写,支持正则匹配) TableId.value,Id.name,Id(如果不指定注解属性名则依次按name,value解析)
    public  String HTTP_PARAM_KEY_CASE							= ConfigTable.HTTP_PARAM_KEY_CASE						;		// "camel"	;	// http参数格式 camel:小驼峰 Camel:大驼峰 lower:小写 upper:大写  service.column2param会把 USER_NAME 转成userName
    public  String TABLE_METADATA_CACHE_KEY						= ConfigTable.TABLE_METADATA_CACHE_KEY					;		// ""		;	// 表结构缓存key
    public  int TABLE_METADATA_CACHE_SECOND						= ConfigTable.TABLE_METADATA_CACHE_SECOND				;		// 3600*24	;	// 表结构缓存时间(没有设置缓存key的情况下生效)(-1:表示永不失效)
    public  String MIX_DEFAULT_SEED								= ConfigTable.MIX_DEFAULT_SEED							;		// "al"		;   // MixUti.mix默认seed
    public  String EL_ATTRIBUTE_PREFIX							= ConfigTable.EL_ATTRIBUTE_PREFIX						;		// "al"		;

    public DataSet getSet() {
        return set;
    }

    public void setSet(DataSet set) {
        this.set = set;
    }

    public DataRow getRow() {
        return row;
    }

    public void setRow(DataRow row) {
        this.row = row;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public List<Run> getRuns() {
        return runs;
    }

    public void setRuns(List<Run> runs) {
        this.runs = runs;
    }

    public DMListener getDmListener() {
        return dmListener;
    }

    public void setDmListener(DMListener dmListener) {
        this.dmListener = dmListener;
    }

    public DDListener getDdListener() {
        return ddListener;
    }

    public void setDdListener(DDListener ddListener) {
        this.ddListener = ddListener;
    }

    public EntityListener getEntityListener() {
        return entityListener;
    }

    public void setEntityListener(EntityListener entityListener) {
        this.entityListener = entityListener;
    }
}
