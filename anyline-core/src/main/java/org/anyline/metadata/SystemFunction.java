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


package org.anyline.metadata;

import org.anyline.metadata.type.DatabaseType;

import java.util.List;

/**
 * 系统函数定义接口
 * <p>
 * 每个函数通过 META 枚举定义，包含：
 * <ul>
 *   <li>title - 中文说明</li>
 *   <li>category - 功能类别（见 {@link Category}）</li>
 *   <li>database - 数据库独有标记，null 表示需跨数据库兼容，非 null 表示仅该数据库独有无需兼容</li>
 * </ul>
 * <p>
 * dbExclusive 判断标准：
 * <ol>
 *   <li>null → 该函数在多个数据库中存在类似功能但命名/语法不同，AnyLine 需要统一抽象</li>
 *   <li>DatabaseType.PostgreSQL → PostgreSQL 独有函数，无跨DB对应</li>
 *   <li>DatabaseType.ORACLE → Oracle 独有函数，无跨DB对应</li>
 *   <li>DatabaseType.MySQL → MySQL 独有函数，无跨DB对应</li>
 * </ol>
 */
public interface SystemFunction {

        enum Category {
                /** NULL值判断、替换、合并等跨数据库差异最大的痛点 */
                NULL_HANDLE("NULL处理"),

                /** TO_CHAR/TO_NUMBER/CAST/CONVERT等各DB命名和语法不同 */
                TYPE_CAST("类型转换"),

                /** 截取、查找、连接、填充、比较等日常最高频操作 */
                STRING("字符串处理"),

                /** 日期加减、格式化、提取、时区等跨DB差异最大的领域 */
                DATE_TIME("日期时间"),

                /** IF/CASE/NVL/GREATEST/LEAST等条件与选择逻辑 */
                CONDITIONAL("条件逻辑"),

                /** 序列值获取、自增ID等各DB实现完全不同 */
                SEQUENCE("序列自增"),

                /** UUID生成与转换，各DB函数名完全不同 */
                UUID("唯一标识"),

                /** AES/SHA/MD5等加密与哈希，各DB实现库不同 */
                CRYPTO("加密哈希"),

                /** 正则匹配/替换/提取，语法差异大 */
                REGEX("正则表达式"),

                /** 统计聚合与窗口函数，核心分析能力 */
                AGGREGATE("聚合与窗口"),

                /** ST_*空间函数，各DB扩展命名差异极大 */
                SPATIAL("空间几何"),

                /** JSON解析/提取/构造，近年新增差异最大 */
                JSON("JSON处理"),

                /** XML解析/查询/构造，各DB实现不同 */
                XML("XML处理"),

                /** 基础/高级数学函数，度数三角和双曲函数跨DB差异大 */
                MATH("数学运算"),

                /** 命名锁/咨询锁，各DB锁机制完全不同 */
                LOCK("锁与并发"),

                /** IS_TRUE/IS_NULL等布尔判断 */
                BOOLEAN("布尔判断"),

                /** 按位与或异或，聚合与标量语义不同 */
                BIT_OP("位运算"),

                /** 数组构造/查询/变换，PG独有类型 */
                ARRAY("数组操作"),

                /** IP地址解析/计算/格式化 */
                NETWORK("网络IP"),

                /** 国产数据库标签安全策略，达梦/金仓/神通/瀚高间需兼容 */
                LABEL_SECURITY("标签安全"),

                /** 版本/连接/配置/存储等系统管理函数 */
                SYSTEM("系统管理"),

                /** 流复制/组复制/复制槽等，PG和MySQL各有独有实现 */
                REPLICATION("复制管理"),

                /** 对象权限检查，PG信息模式独有 */
                PRIVILEGE("权限检查"),

                /** 全文检索引擎，PG独有tsvector/tsquery */
                FULLTEXT("全文搜索"),

                /** 预测/聚类/特征/统计检验，Oracle独有 */
                DATA_MINING("数据挖掘"),

                /** 数据压缩与解压 */
                COMPRESS("压缩处理"),

                /** 排序规则/字符编码/Unicode处理 */
                ENCODING("编码处理"),

                /** 范围类型操作，PG独有 */
                RANGE("范围类型"),

                /** 系统目录查询/DDL重建/元数据解析 */
                CATALOG("系统目录"),

                /** 未归类的杂项函数 */
                OTHER("其他");

                private final String title;

                Category(String title) {
                        this.title = title;
                }

                public String title() {
                        return title;
                }
        }

        /**
         * 函数元数据枚举
         * <p>
         * 每个枚举项包含：
         * <ul>
         *   <li>title - 中文说明</li>
         *   <li>category - 功能类别</li>
         *   <li>dbExclusive - 数据库独有标记（null=需跨DB兼容, DatabaseType.PostgreSQL/DatabaseType.ORACLE/DatabaseType.MySQL=数据库独有）</li>
         * </ul>
         */
        enum META {
                ILLEGAL("不支持", Category.OTHER, null),
                ABS("绝对值", Category.MATH, null, "value"),
                ACL_DEFAULT("构造一个数组，该数组为属于具有 OID ownerId 的角色的类型类型对象的默认访问权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                ACL_EXPLODE("将数组作为一组行返回。如果被授权者是伪角色 PUBLIC，则在被授权者列中用零表示", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                ACOS("弧余弦", Category.MATH, null, "value"),
                ACOSD("反余弦,结果为度数", Category.MATH, null, "value"),
                ACOSH("反双曲余弦", Category.MATH, null),
                ADD_DATE("添加日期", Category.DATE_TIME, null),
                ADD_MONTHS("添加月份", Category.DATE_TIME, null),
                ADD_TIME("添加时间", Category.DATE_TIME, null),
                ADVISORY_LOCK("获取独占会话级咨询锁", Category.LOCK, DatabaseType.PostgreSQL),
                ADVISORY_LOCK_SHARED("获取共享会话级咨询锁", Category.LOCK, DatabaseType.PostgreSQL),
                ADVISORY_UNLOCK("释放以前获取的独占会话级咨询锁", Category.LOCK, DatabaseType.PostgreSQL),
                ADVISORY_UNLOCK_ALL("释放当前会话持有的所有会话级公告锁", Category.LOCK, DatabaseType.PostgreSQL),
                ADVISORY_UNLOCK_SHARED("释放先前获得的共享会话级咨询锁", Category.LOCK, DatabaseType.PostgreSQL),
                ADVISORY_XACT_LOCK("获得专属的交易级咨询锁", Category.LOCK, DatabaseType.PostgreSQL),
                ADVISORY_XACT_LOCK_SHARED("获得共享的交易级咨询锁", Category.LOCK, DatabaseType.PostgreSQL),
                AES_DECRYPT("AES 解密", Category.CRYPTO, null),
                AES_ENCRYPT("AES 加密", Category.CRYPTO, null),
                ALTER_COLUMN_CONTROL("修改列级控制", Category.LABEL_SECURITY, null),
                ALTER_COMPARTMENT("修改安全隔间全称名", Category.LABEL_SECURITY, null),
                ALTER_LEVEL("修改级别全称名", Category.LABEL_SECURITY, null),
                ALTER_POLICY("修改一个安全策略的策略选项", Category.LABEL_SECURITY, null),
                ALTER_ROW_CONTROL("修改行级控制选项", Category.LABEL_SECURITY, null),
                ALTER_TAB_CONTROL_BASE_TIME("修改基于时间段的列集合访问控制时间间隔", Category.LABEL_SECURITY, null),
                ALTER_TABLE_CONTROL("修改表级控制属性", Category.LABEL_SECURITY, null),
                ANY_VALUE("", Category.CONDITIONAL, null),
                APPLY_COLUMN_CONTROL("应用列级控制", Category.LABEL_SECURITY, null),
                APPLY_COLUMN_CONTROL_CONSTRAINT("应用列级控制推理约束", Category.LABEL_SECURITY, null),
                APPLY_ROW_CONTROL("应用行级控制", Category.LABEL_SECURITY, null),
                APPLY_TABLE_CONTROL("应用表级控制", Category.LABEL_SECURITY, null),
                APPLY_TABLE_POLICY("应用安全策略到表", Category.LABEL_SECURITY, null),
                APPROX_COUNT("表达式的近似计数", Category.AGGREGATE, DatabaseType.ORACLE),
                APPROX_COUNT_DISTINCT("包含不同 expr 值的行数", Category.AGGREGATE, DatabaseType.ORACLE),
                APPROX_COUNT_DISTINCT_AGG("将包含有关近似非重复值计数的信息的详细信息列作为其输入，并能够执行这些计数的聚合", Category.AGGREGATE, DatabaseType.ORACLE),
                APPROX_COUNT_DISTINCT_DETAIL("计算包含不同expr值的行数的近似信息，并返回一个称为detail的BLOB值，该值以特殊格式包含该信息", Category.AGGREGATE, DatabaseType.ORACLE),
                APPROX_MEDIAN("一个近似的中间值或一个近似插值", Category.AGGREGATE, DatabaseType.ORACLE),
                APPROX_PERCENTILE("百分位数值会落入该百分比值", Category.AGGREGATE, DatabaseType.ORACLE),
                APPROX_PERCENTILE_AGG("将包含近似百分位信息的详细信息列作为其输入，并能够执行该信息的聚合", Category.AGGREGATE, DatabaseType.ORACLE),
                APPROX_PERCENTILE_DETAIL("计算 expr 值的近似百分位数信息", Category.AGGREGATE, DatabaseType.ORACLE),
                APPROX_RANK("一组值中的近似值", Category.AGGREGATE, DatabaseType.ORACLE),
                APPROX_SUM("表达式的近似和", Category.AGGREGATE, DatabaseType.ORACLE),
                AREA("计算面积", Category.SPATIAL, null),
                ARRAY_AGG("从非空输入值返回任意值", Category.ARRAY, null),
                ARRAY_APPEND("将元素附加到数组的末尾", Category.ARRAY, DatabaseType.PostgreSQL, "array,value"),
                ARRAY_CAT("连接两个数组", Category.ARRAY, DatabaseType.PostgreSQL, "array1,array2"),
                ARRAY_DIMS("数组维度的文本表示形式", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_FILL("一个数组，其中填充了给定值的副本", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_LENGTH("请求的数组维度的长度", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_LOWER("请求的数组维度的下限", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_NDIMS("数组的维度数", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_POSITION("数组中第二个参数首次出现的下标", Category.ARRAY, DatabaseType.PostgreSQL, "array,value"),
                ARRAY_POSITIONS("第一个参数中第二个参数所有出现的下标数组", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_PREPEND("在数组开头前加一个元素", Category.ARRAY, DatabaseType.PostgreSQL, "value,array"),
                ARRAY_REMOVE("从数组中移除所有等于给定值的元素", Category.ARRAY, DatabaseType.PostgreSQL, "array,value"),
                ARRAY_REPLACE("将等于第二个参数的每个数组元素替换为第三个参数", Category.ARRAY, DatabaseType.PostgreSQL, "array,old,new"),
                ARRAY_REVERSE("将数组的第一维度反转", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_SAMPLE("一个由 N 个元素组成的数组，从数组中随机选择", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_SHUFFLE("随机打乱数组的第一个维度", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_SORT("对数组的第一个维度进行排序", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_TO_JSON("ARRAY_TO_JSON", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_TO_STRING("将每个数组元素转换为其文本表示形式", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_TO_TSVECTOR("将文本字符串数组转换为 .给定的字符串按原样用作词位", Category.ARRAY, DatabaseType.PostgreSQL),
                ARRAY_UPPER("请求的数组维度的上限", Category.ARRAY, DatabaseType.PostgreSQL),
                AS_TABLESPACE_PRIVILEGE("用户是否具有表空间权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                ASCII("第一个字符的ASCII值", Category.STRING, null),
                ASIN("弧正弦", Category.MATH, null, "value"),
                ASIND("逆正弦，结果为度数", Category.MATH, null),
                ASINH("逆双曲正弦", Category.MATH, null),
                ASYNCHRONOUS_CONNECTION_FAILOVER_ADD_MANAGED("将组成员源服务器配置信息添加到 复制通道源列表", Category.REPLICATION, DatabaseType.MySQL),
                ASYNCHRONOUS_CONNECTION_FAILOVER_ADD_SOURCE("将源服务器配置信息服务器添加到 复制通道源列表", Category.REPLICATION, DatabaseType.MySQL),
                ASYNCHRONOUS_CONNECTION_FAILOVER_DELETE_MANAGED("从复制通道源列表中删除托管组", Category.REPLICATION, DatabaseType.MySQL),
                ASYNCHRONOUS_CONNECTION_FAILOVER_DELETE_SOURCE("从复制通道源列表中删除源服务器", Category.REPLICATION, DatabaseType.MySQL),
                ASYNCHRONOUS_CONNECTION_FAILOVER_RESET("删除与异步组复制相关的所有设置 故障转移", Category.REPLICATION, DatabaseType.MySQL),
                ATAN("圆弧切线", Category.MATH, null, "value"),
                ATAN2("两个参数的圆弧切线", Category.MATH, null, "y,x"),
                ATAN2D("反正切，结果为度数", Category.MATH, null),
                ATAND("反正切，结果以度数为单位", Category.MATH, null),
                ATANH("反双曲正切", Category.MATH, null),
                AVAILABLE_WAL_SUMMARIES("关数据目录中存在的 WAL 摘要文件的信息", Category.REPLICATION, DatabaseType.PostgreSQL),
                AVG("平均值", Category.AGGREGATE, null),
                BACKEND_PID("附加到当前会话的服务器进程的进程 ID", Category.SYSTEM, DatabaseType.PostgreSQL),
                BACKUP_START("准备服务器开始联机备份", Category.SYSTEM, DatabaseType.PostgreSQL),
                BACKUP_STOP("完成在线备份的执行", Category.SYSTEM, DatabaseType.PostgreSQL),
                BASE_TYPE("PG_BASETYPE", Category.CATALOG, DatabaseType.PostgreSQL),
                BENCHMARK("重复执行表达式", Category.SYSTEM, DatabaseType.MySQL),
                BETWEEN_AND("否在值范围内", Category.CONDITIONAL, null),
                BFILE_NAME("获取BFILE类型文件路径", Category.SYSTEM, DatabaseType.ORACLE),
                BIN_TO_NUM("二进制位转换为十进制数", Category.TYPE_CAST, DatabaseType.ORACLE),
                BINARY("包含数字二进制表示的字符串", Category.TYPE_CAST, null),
                BINARY_TO_UUID("二进制 UUID 转换为字符串", Category.UUID, null),
                BINCHAR("", Category.TYPE_CAST, null),
                BIT_AND("按位 AND", Category.BIT_OP, null, "value1,value2"),
                BIT_COUNT("位数", Category.BIT_OP, null),
                BIT_LENGTH("长度(以BIT为单位)", Category.BIT_OP, null),
                BIT_OR("按位或", Category.BIT_OP, null, "value1,value2"),
                BIT_XOR("按位异或", Category.BIT_OP, null, "value1,value2"),
                BLOCKING_PIDS("阻止具有指定进程 ID 的服务器进程获取锁的会话的进程 ID 数组", Category.LOCK, DatabaseType.PostgreSQL),
                BOOL_AND("", Category.AGGREGATE, null),
                BOOL_OR("", Category.AGGREGATE, null),
                BOX_BOUND("计算两个盒子的边界盒", Category.SPATIAL, null),
                BOX_CIRCLE("计算最小的圆围盒", Category.SPATIAL, null),
                BOX_HEIGHT("计算框的垂直尺寸", Category.SPATIAL, null),
                BOX_WIDTH("计算框的水平大小", Category.SPATIAL, null),
                BRIN_DE_SUMMARIZE_RANGE("删除汇总覆盖给定表块的页面范围的 BRIN 索引元组。", Category.SYSTEM, DatabaseType.PostgreSQL),
                BRIN_SUMMARIZE_NEW_VALUES("查找基表中索引当前未汇总的页面范围", Category.SYSTEM, DatabaseType.PostgreSQL),
                BRIN_SUMMARIZE_RANGE("汇总涵盖给定块的页面范围", Category.SYSTEM, DatabaseType.PostgreSQL),
                CAN_ACCESS_COLUMN("", Category.LABEL_SECURITY, null),
                CAN_ACCESS_DATABASE("", Category.LABEL_SECURITY, null),
                CAN_ACCESS_TABLE("", Category.LABEL_SECURITY, null),
                CAN_ACCESS_USER("", Category.LABEL_SECURITY, null),
                CAN_ACCESS_VIEW("", Category.LABEL_SECURITY, null),
                CANCEL_BACKEND("取消其后端进程具有指定进程 ID 的会话的当前查询", Category.SYSTEM, DatabaseType.PostgreSQL),
                CARDINALITY("数组中的元素总数", Category.ARRAY, null),
                CASE("CASE WHEN", Category.CONDITIONAL, null, "condition,true_value,false_value"),
                CASE_FOLD("C根据排序规则执行输入字符串的大小写折叠", Category.STRING, DatabaseType.PostgreSQL),
                CAST("值强制转换为特定类型", Category.TYPE_CAST, null),
                CBRT("立方根", Category.MATH, null),
                CEIL("不小于参数的最小整数值", Category.MATH, null, "value"),
                CEILING("不小于参数的最小整数值", Category.MATH, null, "value"),
                CENTER("中心点", Category.SPATIAL, null),
                CENTER_POINT("中心点", Category.SPATIAL, null),
                CHAR("int转字符", Category.STRING, null),
                CHAR_INDEX("字符或者字符串在另一个字符串中的起始位置", Category.STRING, null),
                CHAR_LENGTH("字符数", Category.STRING, null),
                CHAR_TO_ENCODING("将提供的编码名称转换为表示某些系统目录表中使用的内部标识符的整数", Category.SYSTEM, DatabaseType.PostgreSQL),
                CHAR_TO_ROWID("字符串值转换为 ROWID 数据类型", Category.TYPE_CAST, DatabaseType.ORACLE),
                CHARACTER_LENGTH("字符数", Category.STRING, null),
                CHARSET("字符集", Category.STRING, DatabaseType.MySQL),
                CHR("int转字符", Category.STRING, null),
                CLEAR_ATTRIBUTE_STATS("清除给定关系和属性的列级统计信息", Category.SYSTEM, DatabaseType.PostgreSQL),
                CLEAR_RELATION_STATS("清除给定关系的表级统计信息", Category.SYSTEM, DatabaseType.PostgreSQL),
                CLIENT_ENCODING("当前客户端编码名称", Category.SYSTEM, DatabaseType.PostgreSQL),
                CLUSTER_DETAILS("所选内容中每一行的集群详细信息", Category.DATA_MINING, DatabaseType.ORACLE),
                CLUSTER_DISTANCE("所选内容中每一行的簇距离", Category.DATA_MINING, DatabaseType.ORACLE),
                CLUSTER_ID("选择中每一行的最高概率簇的标识符", Category.DATA_MINING, DatabaseType.ORACLE),
                CLUSTER_PROBABILITY("选择中每一行的概率", Category.DATA_MINING, DatabaseType.ORACLE),
                CLUSTER_SET("返回每行的簇ID和概率对", Category.DATA_MINING, DatabaseType.ORACLE),
                COALESCE("第一个非空值,支持多个参数", Category.NULL_HANDLE, null, "value1,value2"),
                COERCIBILITY("字符串参数的排序规则强制性值", Category.STRING, null),
                COL_DESCRIPTION("列注释", Category.CATALOG, DatabaseType.PostgreSQL),
                COLLATION("字符串参数的排序规则", Category.STRING, null),
                COLLATION_ACTUAL_VERSION("排序规则对象当前安装在作系统中的实际版本", Category.ENCODING, DatabaseType.PostgreSQL),
                COLLATION_IS_VISIBLE("排序规则在搜索路径中是否可见", Category.ENCODING, DatabaseType.PostgreSQL),
                COLLECT("置于 CAST 函数内部使用", Category.CATALOG, DatabaseType.ORACLE),
                COLUMN_COMPRESSION("显示用于压缩单个可变长度值的压缩算法", Category.SYSTEM, DatabaseType.PostgreSQL),
                COLUMN_SIZE("显示用于存储任何单个数据值的字节数", Category.SYSTEM, DatabaseType.PostgreSQL),
                COLUMN_TOAST_CHUNK_ID("显示磁盘上的 TOASTED 值", Category.SYSTEM, DatabaseType.PostgreSQL),
                COMPOSE("将 Unicode 规范组合应用于给定的字符串参数的结果", Category.STRING, null),
                COMPRESS("以二进制字符串的形式", Category.COMPRESS, null),
                CONCAT("串联字符串", Category.STRING, null, "string1,string2"),
                CONCAT_WS("用分隔符连接字符串", Category.STRING, null, "separator,string1,string2"),
                CONF_LOAD_TIME("服务器配置文件最后一次加载的时间", Category.SYSTEM, DatabaseType.PostgreSQL),
                CONNECTION_ID("当前连接ID", Category.SYSTEM, DatabaseType.MySQL),
                CONTROL_CHECKPOINT("有关当前检查点状态的信息", Category.SYSTEM, DatabaseType.PostgreSQL),
                CONTROL_INIT("关于簇初始化状态的信息", Category.SYSTEM, DatabaseType.PostgreSQL),
                CONTROL_RECOVERY("恢复状态的信息", Category.SYSTEM, DatabaseType.PostgreSQL),
                CONTROL_SYSTEM("有关当前控制文件状态的信息", Category.SYSTEM, DatabaseType.PostgreSQL),
                CONVERSION_IS_VISIBLE("转化在搜索路径中可见", Category.ENCODING, DatabaseType.PostgreSQL),
                CONVERT("将值强制转换为特定类型", Category.TYPE_CAST, null),
                CONVERT_DBID_TO_ID("", Category.TYPE_CAST, DatabaseType.ORACLE),
                CONVERT_FROM("将表示编码src_encoding文本的二进制字符串转换为数据库编码中的文本", Category.TYPE_CAST, DatabaseType.PostgreSQL),
                CONVERT_GUID_TO_ID("", Category.TYPE_CAST, DatabaseType.ORACLE),
                CONVERT_NAME_TO_ID("", Category.TYPE_CAST, DatabaseType.ORACLE),
                CONVERT_NUMBER("不同进制(2-36)之间转换数字", Category.TYPE_CAST, DatabaseType.ORACLE),
                CONVERT_TO("将字符串（在数据库编码中）转换为以编码dest_encoding编码的二进制字符串", Category.TYPE_CAST, DatabaseType.PostgreSQL),
                CONVERT_TZ("从一个时区转换为另一个时区", Category.DATE_TIME, null),
                CONVERT_UID_TO_ID("", Category.TYPE_CAST, DatabaseType.ORACLE),
                COPY_LOGICAL_REPLICATION_SLOT("复制逻辑槽", Category.REPLICATION, DatabaseType.PostgreSQL),
                COPY_PHYSICAL_REPLICATION_SLOT("复制物理槽", Category.REPLICATION, DatabaseType.PostgreSQL),
                CORR("计算相关系数", Category.AGGREGATE, null),
                CORR_K("计算肯德尔的 tau-b 相关系数", Category.AGGREGATE, null),
                CORR_S("计算斯皮尔曼的ρ相关系数", Category.AGGREGATE, null),
                COS("余弦", Category.MATH, null, "value"),
                COSD("参数的元素的余弦,以度为单位", Category.MATH, null),
                COSH("双曲余弦", Category.MATH, null),
                COT("余切", Category.MATH, null, "value"),
                COTD("余切函数，以度为单位的参数", Category.MATH, null),
                COUNT("行数计数", Category.AGGREGATE, null),
                COUNT_NOT_NULL("非空参数的数量", Category.NULL_HANDLE, null),
                COUNT_NULL("空参数的数量", Category.NULL_HANDLE, null),
                COVAR_POP("计算总体协方差", Category.AGGREGATE, null),
                COVAR_SAMP("计算样本协方差", Category.AGGREGATE, null),
                CRC32("循环冗余校验值", Category.CRYPTO, DatabaseType.MySQL),
                CRC32C("计算二进制字符串的 CRC-32C 值", Category.CRYPTO, DatabaseType.PostgreSQL),
                CREATE_COMPARTMENT("创建一个安全隔间", Category.LABEL_SECURITY, null),
                CREATE_LEVEL("创建一个安全级别", Category.LABEL_SECURITY, null),
                CREATE_LOGICAL_REPLICATION_SLOT("使用输出插件插件创建新逻辑（解码）复制槽", Category.REPLICATION, DatabaseType.PostgreSQL),
                CREATE_PHYSICAL_REPLICATION_SLOT("创建新物理复制槽", Category.REPLICATION, DatabaseType.PostgreSQL),
                CREATE_POLICY("创建一个安全策略", Category.LABEL_SECURITY, null),
                CREATE_RESTORE_POINT("在预写日志中创建一个命名标记记录", Category.SYSTEM, DatabaseType.PostgreSQL),
                CUBE_TABLE("从立方体或维度中提取数据，并以二维关系表格式返回", Category.DATA_MINING, DatabaseType.ORACLE),
                CUME_DIST("累积分布概率", Category.AGGREGATE, null),
                CURR_VAL("序列值", Category.SEQUENCE, null),
                CURRENT_CATALOG("", Category.SYSTEM, null),
                CURRENT_DATABASE("数据库名称", Category.SYSTEM, null),
                CURRENT_DATABASE_ID("当前连接的数据库 id", Category.SYSTEM, null),
                CURRENT_DATE("当前日期", Category.DATE_TIME, null),
                CURRENT_DATETIME("当前时间", Category.DATE_TIME, null),
                CURRENT_LOGFILE("日志记录收集器当前使用的日志文件的路径名", Category.OTHER, DatabaseType.PostgreSQL),
                CURRENT_QUERY("客户端提交的当前正在执行的查询的文本", Category.SYSTEM, DatabaseType.PostgreSQL),
                CURRENT_ROLE("当前活动角色", Category.SYSTEM, null),
                CURRENT_SCHEMA("当前 schema", Category.SYSTEM, null),
                CURRENT_SCHEMAS("schema 搜索路径", Category.SYSTEM, DatabaseType.PostgreSQL),
                CURRENT_SESSION_ID("显示当前用户的连接号", Category.SYSTEM, null),
                CURRENT_SETTING("查看某个参数值", Category.SYSTEM, DatabaseType.PostgreSQL),
                CURRENT_SNAPSHOT("当前快照", Category.REPLICATION, DatabaseType.PostgreSQL),
                CURRENT_TIME("当前时间", Category.DATE_TIME, null),
                CURRENT_TIMESTAMP("当前时间戳", Category.DATE_TIME, null),
                CURRENT_USER("当前用户", Category.SYSTEM, null),
                CURRENT_USER_ID("当前用户ID", Category.SYSTEM, null),
                CURRENT_WAL_FLUSH_LSN("当前预写日志刷新位置", Category.REPLICATION, DatabaseType.PostgreSQL),
                CURRENT_WAL_INSERT_LSN("当前预写日志插入位置", Category.REPLICATION, DatabaseType.PostgreSQL),
                CURRENT_WAL_LSN("当前预写日志写入位置", Category.REPLICATION, DatabaseType.PostgreSQL),
                CURRENT_XACT_ID("当前事务的 ID", Category.REPLICATION, DatabaseType.PostgreSQL),
                CURRENT_XACT_ID_IF_ASSIGNED("当前事务的 ID", Category.REPLICATION, DatabaseType.PostgreSQL),
                CURSOR_TO_XML("CURSOR_TO_XML", Category.XML, DatabaseType.PostgreSQL),
                CURSOR_TO_XML_SCHEMA("CURSOR_TO_XMLSCHEMA", Category.XML, DatabaseType.PostgreSQL),
                CV("从规则的左侧到右侧的维度列或分区列的当前值", Category.DATA_MINING, DatabaseType.ORACLE),
                DATA_OBJECT_TO_MAT_PARTITION("数据对象映射到物化视图分区", Category.CATALOG, DatabaseType.ORACLE),
                DATA_OBJECT_TO_PARTITION("域索引数据的存储位置", Category.CATALOG, DatabaseType.ORACLE),
                DATABASE_COLLATION_ACTUAL_VERSION("当前安装在作系统中的数据库排序规则的实际版本", Category.ENCODING, DatabaseType.PostgreSQL),
                DATABASE_SIZE("计算具有指定名称或 OID 的数据库使用的总磁盘空间", Category.SYSTEM, null),
                DATABASE_TO_XML("DATABASE_TO_XML", Category.XML, DatabaseType.PostgreSQL),
                DATABASE_TO_XML_AND_XML_SCHEMA("DATABASE_TO_XML_AND_XMLSCHEMA", Category.XML, DatabaseType.PostgreSQL),
                DATABASE_TO_XML_SCHEMA("DATABASE_TO_XMLSCHEMA", Category.XML, DatabaseType.PostgreSQL),
                DATE("提取日期或日期时间表达式的日期部分", Category.DATE_TIME, null),
                DATE_ADD("将时间值（间隔）添加到日期值", Category.DATE_TIME, null, "date,interval,unit"),
                DATE_BIN("输入到与指定原点对齐的指定区间", Category.DATE_TIME, null),
                DATE_DIFF("时间差", Category.DATE_TIME, null, "unit,start,end"),
                DATE_FORMAT("按指定格式设置日期", Category.DATE_TIME, null, "value,format"),
                DATE_LARGER("最大日期", Category.DATE_TIME, null),
                DATE_MI("计算两个 DATE 值相差的天数", Category.DATE_TIME, DatabaseType.ORACLE),
                DATE_MII("将指定的日期减去指定的天数", Category.DATE_TIME, DatabaseType.ORACLE),
                DATE_PART("获取给定日期的指定子域值", Category.DATE_TIME, null),
                DATE_PLI("将指定的日期增加指定的天数", Category.DATE_TIME, DatabaseType.ORACLE),
                DATE_SMALLER("最小日期", Category.DATE_TIME, null),
                DATE_SUB("从日期中减去时间值", Category.DATE_TIME, null, "date,interval,unit"),
                DATE_TRUNCATE("给定一个日期，截断成指定的精度", Category.DATE_TIME, null),
                DAY_NAME("工作日的名称", Category.DATE_TIME, null),
                DAY_OF_MONTH("月份中的某一天", Category.DATE_TIME, null),
                DAY_OF_WEEK("工作日索引", Category.DATE_TIME, null),
                DAY_OF_WEEK_ISO("工作日索引", Category.DATE_TIME, null),
                DAY_OF_YEAR("一年中的某一天", Category.DATE_TIME, null),
                DAYS("得到指定日期与 0001 年 1 月 1 日的天数差值", Category.DATE_TIME, null),
                DECODE("将指定的字符串以特定的解码规则解码", Category.CONDITIONAL, DatabaseType.ORACLE, "value,search,result"),
                DECOMPOSE("将一个 Unicode 分解应用于给定的参数的结果", Category.STRING, null),
                DEFAULT("表列的默认值", Category.SYSTEM, null),
                DEGREES("弧度转换为度数", Category.MATH, null, "value"),
                DENSE_RANK("分区内当前行的排名", Category.AGGREGATE, null),
                DEPTH("条件指定的路径中具有相同相关变量的级别数", Category.DATA_MINING, DatabaseType.ORACLE),
                DEREF("参数expr的对象引用", Category.CATALOG, DatabaseType.ORACLE),
                DESCRIBE_OBJECT("由目录 OID、对象 OID 和子对象 ID 标识的数据库对象的文本描述", Category.CATALOG, DatabaseType.PostgreSQL),
                DIAGONAL("将框的对角线提取为线段", Category.SPATIAL, null),
                DIAMETER("计算圆的直径", Category.SPATIAL, null),
                DIFFERENCE("求两个字符串的读音相似性", Category.STRING, null),
                DIV("整数除法", Category.MATH, null),
                DM_PARTITION_NAME("与输入行关联的分区名称", Category.SYSTEM, DatabaseType.ORACLE),
                DROP_COMPARTMENT_BY_NAME("删除一个安全隔间", Category.LABEL_SECURITY, null),
                DROP_LEVEL_BY_NAME("删除一个级别", Category.LABEL_SECURITY, null),
                DROP_POLICY("删除一个安全策略", Category.LABEL_SECURITY, null),
                DROP_REPLICATION_SLOT("删除物理或逻辑复制槽", Category.REPLICATION, DatabaseType.PostgreSQL),
                DROP_TAB_CONTROL_BASE_TIME("删除基于时间段的列集合访问控制", Category.LABEL_SECURITY, null),
                DST_AFFECTED("解析为一个带时区的TIMESTAMP值", Category.DATE_TIME, DatabaseType.ORACLE),
                DST_CONVERT("指定的日期时间表达式指定错误处理方式", Category.DATE_TIME, DatabaseType.ORACLE),
                DST_ERROR("解析为一个 TIMESTAMP WITH TIME ZONE 值 并指示新的日期时间值是否会导致时区数据出错", Category.DATE_TIME, DatabaseType.ORACLE),
                DUMP("包含数据类型代码、字节长度和expr内部表示的值", Category.SYSTEM, DatabaseType.ORACLE),
                ELT("参数列表中指定位置的字符串", Category.STRING, DatabaseType.MySQL, "index,string"),
                EMPTY_BLOB("对 BLOB 定位符进行初始化", Category.SYSTEM, DatabaseType.ORACLE),
                EMPTY_CLOB("对 CLOB 定位符进行初始化", Category.SYSTEM, DatabaseType.ORACLE),
                ENABLE_POLICY("修改一个安全策略的可用状态", Category.LABEL_SECURITY, null),
                ENABLE_TABLE_POLICY("修改策略应用状态", Category.LABEL_SECURITY, null),
                ENCODE("将二进制串进行编码", Category.TYPE_CAST, null),
                ENCODING_TO_CHAR("将某些系统目录表中用作编码内部标识符的整数转换为可读的字符串", Category.SYSTEM, DatabaseType.PostgreSQL),
                ENUM_FIRST("枚举类型的第一个值", Category.CATALOG, DatabaseType.PostgreSQL),
                ENUM_LAST("枚举类型的最后一个值", Category.CATALOG, DatabaseType.PostgreSQL),
                ENUM_RANGE("两个给定枚举值之间的范围", Category.CATALOG, DatabaseType.PostgreSQL),
                ERF("误差", Category.MATH, null),
                ERFC("互补误差", Category.MATH, null),
                EVENT_TRIGGER_DDL_COMMANDS("每个用户作执行的 DDL 命令列表", Category.CATALOG, DatabaseType.PostgreSQL),
                EVENT_TRIGGER_TABLE_REWRITE_OID("即将重写的表的 OID", Category.CATALOG, DatabaseType.PostgreSQL),
                EVENT_TRIGGER_TABLE_REWRITE_REASON("解释重写原因的代码", Category.CATALOG, DatabaseType.PostgreSQL),
                EVERY("", Category.AGGREGATE, null),
                EXISTS("查询结果是否包含任何行", Category.CONDITIONAL, null),
                EXISTS_NODE("通过指定路径遍历XML文档是否会得到任何节点", Category.XML, DatabaseType.ORACLE),
                EXP("自然对数的底数e的指定次幂", Category.MATH, null, "value"),
                EXPORT_SET("将整数转换为由指定字符串分隔的二进制位表示", Category.STRING, DatabaseType.MySQL),
                EXPORT_SNAPSHOT("保存事务的当前快照并返回标识快照的字符串", Category.REPLICATION, DatabaseType.PostgreSQL),
                EXTRACT("获取子域", Category.DATE_TIME, null, "field,source"),
                EXTRACT_DATE_PART("提取日期的一部分", Category.DATE_TIME, null),
                EXTRACT_VALUE("使用 XPath 表示法从 XML 字符串中提取值", Category.XML, null),
                FACTORIAL("阶乘", Category.MATH, null),
                FEATURE_COMPARE("使用特征提取模型来比较两个不同的文档", Category.DATA_MINING, DatabaseType.ORACLE),
                FEATURE_DETAILS("选择中每一行的返回功能细节", Category.DATA_MINING, DatabaseType.ORACLE),
                FEATURE_ID("选择中每一行最高值特征的标识符", Category.DATA_MINING, DatabaseType.ORACLE),
                FEATURE_SET("所选中每一行的一组要素 ID 和要素值对", Category.DATA_MINING, DatabaseType.ORACLE),
                FEATURE_VALUE("所选中每一行的要素值", Category.DATA_MINING, DatabaseType.ORACLE),
                FIELD("一个值(value)在给定列表(val1, val2, ...)中第一次出现的索引", Category.STRING, DatabaseType.MySQL, "string,value"),
                FILE_NODE_RELATION("给定表空间 OID 和存储它的文件节点", Category.CATALOG, DatabaseType.PostgreSQL),
                FIND_IN_SET("第二个参数中第一个参数的索引", Category.STRING, DatabaseType.MySQL),
                FIRST_ROW("第一行", Category.AGGREGATE, null),
                FIRST_VALUE("第一行的参数值", Category.AGGREGATE, null),
                FLOOR("不大于参数的最大整数值", Category.MATH, null, "value"),
                FORMAT("格式化为指定小数位数的数字", Category.STRING, null),
                FORMAT_BYTES("将字节计数转换为带单位的值", Category.SYSTEM, DatabaseType.PostgreSQL),
                FORMAT_PICO_TIME("将以皮秒为单位的时间转换为带单位的值", Category.SYSTEM, DatabaseType.PostgreSQL),
                FORMAT_TYPE("类型 OID 和可能的类型修饰符标识的数据类型的 SQL 名称", Category.CATALOG, DatabaseType.PostgreSQL),
                FOUND_ROWS("最近一次SELECT查询返回的行数", Category.SYSTEM, DatabaseType.MySQL),
                FROM_BASE64("解码base64", Category.TYPE_CAST, null),
                FROM_DAYS("将日数转换为日期", Category.TYPE_CAST, DatabaseType.MySQL),
                FROM_TZ("将一个 TIMESTAMP 类型的值与时区信息组合", Category.DATE_TIME, DatabaseType.ORACLE),
                FROM_UNIX_TIME("将 Unix 时间戳格式化为日期", Category.DATE_TIME, null),
                FUNCTION_IS_VISIBLE("搜索路径中功能可见", Category.CATALOG, DatabaseType.PostgreSQL),
                GAMMA("伽马", Category.MATH, null),
                GCD("最大公约数", Category.MATH, null),
                GENERATE_SERIES("生成从开始到停止的一系列值", Category.OTHER, DatabaseType.PostgreSQL),
                GENERATE_SUBSCRIPTS("生成一个序列，其中包含给定数组的暗维的有效下标", Category.ARRAY, DatabaseType.PostgreSQL),
                GEOMETRY_COLLECTION("从几何构造几何集合", Category.SPATIAL, null),
                GET_ACL("数据库对象的 ACL", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                GET_BIT("从二进制字符串中提取第 n 位", Category.BIT_OP, null),
                GET_BYTE("从二进制字符串中提取第 n 个字节", Category.BIT_OP, null),
                GET_CATALOG_FOREIGN_KEYS("描述POSTGRESQL系统目录中存在的外键关系", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_CONSTRAINT_DEF("重建约束的创建命令", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_CURRENT_TS_CONFIG("当前默认文本搜索配置的 OID", Category.FULLTEXT, DatabaseType.PostgreSQL),
                GET_DATE_TIME_FORMAT("日期或时间格式字符串", Category.DATE_TIME, null),
                GET_DD_COLUMN_PRIVILEGES("", Category.SYSTEM, DatabaseType.MySQL),
                GET_DD_CREATE_OPTIONS("", Category.SYSTEM, DatabaseType.MySQL),
                GET_DD_INDEX_SUB_PART_LENGTH("", Category.SYSTEM, DatabaseType.MySQL),
                GET_EXPR("反编译存储在系统目录中的表达式的内部形式", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_FUNCTION_ARGUMENTS("以函数或过程需要出现的形式（包括默认值）重建函数或过程的参数列表", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_FUNCTION_DEF("重建函数或过程的创建命令", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_FUNCTION_IDENTITY_ARGUMENTS("重建识别函数或过程所需的参数列表", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_FUNCTION_RESULT("重建函数的子句", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_INDEX_DEF("重建索引的创建命令", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_KEYWORDS("描述服务器识别的关键字", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_LOADED_MODULES("加载到当前服务器会话中的可加载模块的列表", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_LOCK("获取命名锁", Category.LOCK, null),
                GET_OBJECT_ADDRESS("以唯一标识类型代码以及对象名称和参数数组指定的数据库对象", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_PART_KEY_DEF("重建分区表的分区键的定义", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_RULE_DEF("重建规则的创建命令", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_SERIAL_SEQUENCE("与列关联的序列的名称", Category.SEQUENCE, DatabaseType.PostgreSQL),
                GET_STATISTICS_OBJECT_DEF("重建扩展统计信息对象的创建命令", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_TRIGGER_DEF("重建触发器的创建命令", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_TZ("获取当前数据库的时区", Category.DATE_TIME, null),
                GET_USER_BY_ID("OID 的角色名称", Category.SYSTEM, DatabaseType.PostgreSQL),
                GET_UTC_DATE("获取当前格林尼治标准时间", Category.DATE_TIME, null),
                GET_VIEW_DEF("重建视图或具体化视图的基础命令", Category.CATALOG, DatabaseType.PostgreSQL),
                GET_WAL_REPLAY_PAUSE_STATE("恢复暂停状态", Category.REPLICATION, DatabaseType.PostgreSQL),
                GET_WAL_RESOURCE_MANAGERS("系统中当前加载的 WAL 资源管理器", Category.REPLICATION, DatabaseType.PostgreSQL),
                GET_WAL_SUMMARIZER_STATE("有关WAL摘要器进度的信息", Category.REPLICATION, DatabaseType.PostgreSQL),
                GIN_CLEAN_PENDING_LIST("通过将指定 GIN 索引中的条目批量移动到主 GIN 数据结构来清理指定 GIN 索引的“待处理”列表", Category.SYSTEM, DatabaseType.PostgreSQL),
                GREATEST("最大参数", Category.CONDITIONAL, null, "value1,value2"),
                GROUP_CONCAT("串联分组字符串", Category.STRING, null),
                GROUP_ID("", Category.AGGREGATE, DatabaseType.ORACLE),
                GROUP_REPLICATION_DISABLE_MEMBER_ACTION("禁用指定事件的成员作", Category.REPLICATION, DatabaseType.MySQL),
                GROUP_REPLICATION_ENABLE_MEMBER_ACTION("为指定的事件启用成员作", Category.REPLICATION, DatabaseType.MySQL),
                GROUP_REPLICATION_GET_COMMUNICATION_PROTOCOL("获取当前组复制通信协议的版本", Category.REPLICATION, DatabaseType.MySQL),
                GROUP_REPLICATION_GET_WRITE_CONCURRENCY("获取当前为组设置的最大共识实例数", Category.REPLICATION, DatabaseType.MySQL),
                GROUP_REPLICATION_RESET_MEMBER_ACTIONS("将所有成员作重置为默认值和配置版本 数字变为 1", Category.REPLICATION, DatabaseType.MySQL),
                GROUP_REPLICATION_SET_AS_PRIMARY("将特定组成员设为主要组成员", Category.REPLICATION, DatabaseType.MySQL),
                GROUP_REPLICATION_SET_COMMUNICATION_PROTOCOL("设置要使用的组复制通信协议的版本", Category.REPLICATION, DatabaseType.MySQL),
                GROUP_REPLICATION_SET_WRITE_CONCURRENCY("设置可在 平行", Category.REPLICATION, DatabaseType.MySQL),
                GROUP_REPLICATION_SWITCH_TO_MULTI_PRIMARY_MODE("将以单主模式运行的组的模式更改为 多主模式", Category.REPLICATION, DatabaseType.MySQL),
                GROUP_REPLICATION_SWITCH_TO_SINGLE_PRIMARY_MODE("将以多主模式运行的组的模式更改为 单主模式", Category.REPLICATION, DatabaseType.MySQL),
                GROUPING("区分超聚合 ROLLUP 行和常规行", Category.AGGREGATE, null),
                GROUPING_ID("", Category.AGGREGATE, DatabaseType.ORACLE),
                HAS_ANY_COLUMN_PRIVILEGE("用户是否对表的任何列具有权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_COLUMN_PRIVILEGE("用户是否对指定的表列具有权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_DATABASE_PRIVILEGE("用户是否具有数据库权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_FOREIGN_DATA_WRAPPER_PRIVILEGE("用户是否具有外部数据包装器的权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_FUNCTION_PRIVILEGE("用户是否具有功能权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_LANGUAGE_PRIVILEGE("用户是否具有语言权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_LARGE_OBJECT_PRIVILEGE("用户是否具有大型对象的权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_PARAMETER_PRIVILEGE("用户是否具有配置参数的权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_ROLE("用户是否具有角色权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_SCHEMA_PRIVILEGE("用户是否具有架构权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_SEQUENCE_PRIVILEGE("用户是否具有序列权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_SERVER_PRIVILEGE("用户是否具有外部服务器的权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_TABLE_PRIVILEGE("用户是否具有表的权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HAS_TYPE_PRIVILEGE("用户是否具有数据类型的权限", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                HASH("计算给定表达式哈希值", Category.CRYPTO, null),
                HEAD_ASCII("第一个字符的ASCII值", Category.STRING, null),
                HEX("十进制或字符串值的十六进制表示", Category.TYPE_CAST, null),
                HEX_TO_RAW("将由参数给定的十六进制值转为原始值", Category.TYPE_CAST, DatabaseType.ORACLE),
                HOUR("提取小时", Category.DATE_TIME, null),
                ICU_UNICODE_VERSION("ICU 使用的 UNICODE 版本", Category.ENCODING, DatabaseType.PostgreSQL),
                ICU_VERSION("ICU库版", Category.ENCODING, DatabaseType.PostgreSQL),
                IDENTIFY_OBJECT("包含足够信息的行，以唯一标识由目录 OID、对象 OID 和子对象 ID 指定的数据库对象", Category.CATALOG, DatabaseType.PostgreSQL),
                IDENTIFY_OBJECT_AS_ADDRESS("以唯一标识由目录 OID、对象 OID 和子对象 ID 指定的数据库对象", Category.CATALOG, DatabaseType.PostgreSQL),
                IF("IF", Category.CONDITIONAL, null, "condition,true_value,false_value"),
                IF_ELSE("", Category.CONDITIONAL, null, "condition,true_value,false_value"),
                IF_EQUAL_NULL_ELSE_FIRST("如果相等返回NULL否则返回第一个", Category.NULL_HANDLE, null),
                IF_NULL_C("是否NULL", Category.NULL_HANDLE, null),
                IF_NULL_TERNARY("参数1为空时返回参数2否则返回参数3", Category.NULL_HANDLE, null, "value,not_null,null"),
                IMPORT_SYSTEM_COLLATIONS("根据它在作系统中找到的所有区域设置将排序规则添加到系统目录中", Category.ENCODING, DatabaseType.PostgreSQL),
                IN("值是否在一组值中", Category.CONDITIONAL, null),
                INDEX_AM_HAS_PROPERTY("测试索引访问方法是否具有命名属性", Category.CATALOG, DatabaseType.PostgreSQL),
                INDEX_COLUMN_HAS_PROPERTY("测试索引列是否具有命名属性", Category.CATALOG, DatabaseType.PostgreSQL),
                INDEX_HAS_PROPERTY("测试索引是否具有命名属性", Category.CATALOG, DatabaseType.PostgreSQL),
                INDEXES_SIZE("计算附加到指定表的索引使用的总磁盘空间", Category.SYSTEM, DatabaseType.PostgreSQL),
                INET_ATON("将标准的点分十进制IP地址转换为无符号整数", Category.NETWORK, null),
                INET_CLIENT_ADDR("当前客户端的 IP 地址", Category.NETWORK, DatabaseType.PostgreSQL),
                INET_CLIENT_PORT("当前客户端的 IP 端口号", Category.NETWORK, DatabaseType.PostgreSQL),
                INET_NTOA("将无符号整数转换为IPv4地址字符串", Category.NETWORK, null),
                INET_SERVER_ADDR("服务器接受当前连接的 IP 地址", Category.NETWORK, DatabaseType.PostgreSQL),
                INET_SERVER_PORT("服务器接受当前连接的 IP 端口号", Category.NETWORK, DatabaseType.PostgreSQL),
                INITCAP("将每个单词的第一个字母转换为大写，将其余字母转换为小写", Category.STRING, null),
                INNER_BOX("内接框", Category.SPATIAL, null),
                INPUT_ERROR_INFO("测试给定字符串是否是指定数据类型的有效输入", Category.STRING, null),
                INPUT_IS_VALID("测试给定字符串是否是指定数据类型的有效输入", Category.STRING, null),
                INSERT("在指定位置插入子字符串", Category.STRING, DatabaseType.MySQL, "string,position,length,newstring"),
                INSERT_TEXT("用一个字符串替换另一个字符串中指定位置指定长度的子串", Category.STRING, DatabaseType.MySQL),
                INSERT_TEXT_C("从指定的位置，删除指定的长度，再加入指定的字符串", Category.STRING, null),
                INSTR("第一次出现的子字符串的索引", Category.STRING, null, "string,substring"),
                INTERNAL_AUTO_INCREMENT("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_AVG_ROW_LENGTH("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_CHECK_TIME("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_CHECKSUM("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_DATA_FREE("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_DATA_LENGTH("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_DD_CHAR_LENGTH("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_GET_COMMENT_OR_ERROR("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_GET_ENABLED_ROLE_JSON("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_GET_HOSTNAME("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_GET_USERNAME("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_GET_VIEW_WARNING_OR_ERROR("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_INDEX_COLUMN_CARDINALITY("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_INDEX_LENGTH("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_IS_ENABLED_ROLE("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_IS_MANDATORY_ROLE("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_KEYS_DISABLED("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_MAX_DATA_LENGTH("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_TABLE_ROWS("", Category.SYSTEM, DatabaseType.MySQL),
                INTERNAL_UPDATE_TIME("", Category.SYSTEM, DatabaseType.MySQL),
                INTERVAL("小于第一个参数的索引", Category.CONDITIONAL, null),
                INTERVAL_DTS_COMPARE("比较两个 INTERVALDTS 值", Category.DATE_TIME, DatabaseType.ORACLE),
                INTERVAL_DTS_LARGER("比较两个 INTERVALDTS 值, 返回较大的值", Category.DATE_TIME, DatabaseType.ORACLE),
                INTERVAL_DTS_SMALLER("比较两个 INTERVALDTS 值, 返回较小的值", Category.DATE_TIME, DatabaseType.ORACLE),
                INTERVAL_YTM_COMPARE("比较两个 intervalytm 值", Category.DATE_TIME, DatabaseType.ORACLE),
                INTERVAL_YTM_LARGER("比较两个 INTERVALYTM 值, 返回较大的值", Category.DATE_TIME, DatabaseType.ORACLE),
                INTERVAL_YTM_SMALLER("比较两个 INTERVALYTM 值, 返回较小的值", Category.DATE_TIME, DatabaseType.ORACLE),
                INVOKING_USER("", Category.SYSTEM, DatabaseType.PostgreSQL),
                INVOKING_USER_ID("", Category.SYSTEM, DatabaseType.PostgreSQL),
                IP_ABBREV("将缩写的显示格式创建为文", Category.NETWORK, DatabaseType.PostgreSQL),
                IP_BROADCAST("计算地址网络的广播地址", Category.NETWORK, DatabaseType.PostgreSQL),
                IP_HOST("以文本形式返回 IP 地址，忽略网络掩码", Category.NETWORK, DatabaseType.PostgreSQL),
                IP_HOST_MASK("计算地址网络的主机掩码", Category.NETWORK, DatabaseType.PostgreSQL),
                IP_INET_MERGE("计算包含两个给定网络的最小网络", Category.NETWORK, DatabaseType.PostgreSQL),
                IP_INET_SAME_FAMILY("测试地址是否属于同一IP系列", Category.NETWORK, DatabaseType.PostgreSQL),
                IP_MASK_LENGTH("网络掩码长度（以位为单位）", Category.NETWORK, DatabaseType.PostgreSQL),
                IP_NETMASK("计算地址网络的网络掩码", Category.NETWORK, DatabaseType.PostgreSQL),
                IP_NETWORK("地址的网络部分，将网络掩码右侧的任何内容归零", Category.NETWORK, DatabaseType.PostgreSQL),
                IP_SET_MASK_LENGTH("设置值的网络掩码长度。地址部分不变", Category.NETWORK, DatabaseType.PostgreSQL),
                IP_TEXT("以文本形式返回未缩写的 IP 地址和网络掩码长度", Category.NETWORK, DatabaseType.PostgreSQL),
                IP_VERSION("IP版本", Category.NETWORK, DatabaseType.PostgreSQL),
                IS("针对布尔值测试值", Category.CONDITIONAL, null),
                IS_EMPTY("范围是否空", Category.BOOLEAN, null),
                IS_FALSE("判断一个 bool 值是否为 FALSE", Category.BOOLEAN, null),
                IS_FINITE("否是有穷的日期时间", Category.MATH, null),
                IS_FREE_LOCK("命名锁是否空闲", Category.LOCK, DatabaseType.MySQL),
                IS_IN_RECOVERY("恢复是否在进行中", Category.SYSTEM, DatabaseType.PostgreSQL),
                IS_NOT("", Category.BOOLEAN, null),
                IS_NOT_FALSE("判断一个 bool 值是否不为 FALSE", Category.BOOLEAN, null),
                IS_NOT_NULL("", Category.BOOLEAN, null),
                IS_NOT_TRUE("判断一个 bool 值是否不为 TRUE", Category.BOOLEAN, null),
                IS_NULL("", Category.BOOLEAN, null),
                IS_OTHER_TEMP_SCHEMA("如果给定的 OID 是另一个会话的临时架构的 OID", Category.SYSTEM, DatabaseType.PostgreSQL),
                IS_TRUE("判断一个 bool 值是否为 TRUE", Category.BOOLEAN, null),
                IS_USED_LOCK("命名锁是否正在使用中", Category.LOCK, DatabaseType.MySQL),
                IS_UUID("否为有效 UUID", Category.BOOLEAN, null),
                IS_WAL_REPLAY_PAUSED("请求恢复是否暂停", Category.REPLICATION, DatabaseType.PostgreSQL),
                ITERATION_NUMBER("表示通过模型规则完成的迭代", Category.DATA_MINING, DatabaseType.ORACLE),
                JIT_AVAILABLE("JIT 编译器扩展可用", Category.SYSTEM, DatabaseType.PostgreSQL),
                JSON_AGG("将所有输入值（包括空值）收集到 JSON 数组中", Category.JSON, null),
                JSON_AGG_STRICT("将所有输入值（跳过 null）收集到 JSON 数组中", Category.JSON, DatabaseType.PostgreSQL),
                JSON_ARRAY("创建 JSON 数组", Category.JSON, null),
                JSON_ARRAY_AGG("将结果集作为单个 JSON 数组", Category.JSON, DatabaseType.PostgreSQL),
                JSON_ARRAY_APPEND("将数据附加到 JSON", Category.JSON, null),
                JSON_ARRAY_ELEMENTS("JSON_ARRAY_ELEMENTS", Category.JSON, DatabaseType.PostgreSQL),
                JSON_ARRAY_ELEMENTS_TEXT("JSON_ARRAY_ELEMENTS_TEXT", Category.JSON, DatabaseType.PostgreSQL),
                JSON_ARRAY_INSERT("插入 JSON 数组", Category.JSON, null),
                JSON_ARRAY_LENGTH("JSON_ARRAY_LENGTH", Category.JSON, DatabaseType.PostgreSQL),
                JSON_BUILD_ARRAY("JSON_BUILD_ARRAY", Category.JSON, DatabaseType.PostgreSQL),
                JSON_BUILD_OBJECT("JSON_BUILD_OBJECT", Category.JSON, DatabaseType.PostgreSQL),
                JSON_CONTAINS("JSON在PATH上是否包含特定对象", Category.JSON, null),
                JSON_CONTAINS_PATH("JSON是否在PATH上包含任何数据", Category.JSON, DatabaseType.PostgreSQL),
                JSON_DATA_GUIDE("", Category.JSON, DatabaseType.PostgreSQL),
                JSON_DEPTH("JSON的最大深度", Category.JSON, null),
                JSON_EACH("JSON_EACH", Category.JSON, DatabaseType.PostgreSQL),
                JSON_EACH_TEXT("JSON_EACH_TEXT", Category.JSON, DatabaseType.PostgreSQL),
                JSON_EXISTS("JSON_EXISTS", Category.JSON, null),
                JSON_EXTRACT("从JSON提取数据", Category.JSON, null),
                JSON_EXTRACT_PATH("JSON_EXTRACT_PATH", Category.JSON, DatabaseType.PostgreSQL),
                JSON_EXTRACT_PATH_TEXT("JSON_EXTRACT_PATH_TEXT", Category.JSON, DatabaseType.PostgreSQL),
                JSON_INSERT("将数据插入JSON", Category.JSON, null),
                JSON_KEYS("JSON中的键数组", Category.JSON, null),
                JSON_LENGTH("JSON中的元素数", Category.JSON, null),
                JSON_MERGE("合并 JSON，保留重复的键", Category.JSON, null),
                JSON_MERGE_PATCH("合并 JSON，替换重复键的值", Category.JSON, null),
                JSON_MERGE_PRESERVE("合并 JSON，保留重复的键", Category.JSON, null),
                JSON_OBJECT("创建 JSON 对象", Category.JSON, null),
                JSON_OBJECT_AGG("将结果集作为单个JSON对象", Category.JSON, null),
                JSON_OBJECT_AGG_STRICT("将所有键/值对收集到 JSON 对象中", Category.JSON, DatabaseType.PostgreSQL),
                JSON_OBJECT_AGG_UNIQUE("将所有键/值对收集到 JSON 对象中", Category.JSON, DatabaseType.PostgreSQL),
                JSON_OBJECT_AGG_UNIQUE_STRICT("将所有键/值对收集到 JSON 对象中", Category.JSON, DatabaseType.PostgreSQL),
                JSON_OBJECT_KEYS("JSON_OBJECT_KEYS", Category.JSON, DatabaseType.PostgreSQL),
                JSON_OVERLAPS("比较两个 JSON，如果它们有任何 键值对或数组元素", Category.JSON, null),
                JSON_POPULATE_RECORD("JSON_POPULATE_RECORD", Category.JSON, DatabaseType.PostgreSQL),
                JSON_POPULATE_RECORDSET("JSON_POPULATE_RECORDSET", Category.JSON, DatabaseType.PostgreSQL),
                JSON_PRETTY("以可读的格式打印JSON", Category.JSON, null),
                JSON_QUERY("JSON_QUERY", Category.JSON, null),
                JSON_QUOTE("将字符串转换为JSON格式", Category.JSON, null),
                JSON_REMOVE("从JSON中删除数据", Category.JSON, null),
                JSON_REPLACE("替换JSON中的值", Category.JSON, null),
                JSON_SCALAR("JSON_SCALAR", Category.JSON, null),
                JSON_SCHEMA_VALID("根据JSON模式验证JSON", Category.JSON, null),
                JSON_SCHEMA_VALIDATION_REPORT("根据JSON模式验证JSON", Category.JSON, DatabaseType.PostgreSQL),
                JSON_SEARCH("在JSON文档中搜索指定字符串", Category.JSON, null),
                JSON_SERIALIZE("JSON_SERIALIZE", Category.JSON, null),
                JSON_SET("将数据插入JSON", Category.JSON, null),
                JSON_STORAGE_FREE("JSON 列值的二进制表示形式内释放的空间", Category.JSON, null),
                JSON_STORAGE_SIZE("用于存储JSON的二进制表示的空间", Category.JSON, null),
                JSON_STRIP_NULLS("JSON_STRIP_NULLS", Category.JSON, DatabaseType.PostgreSQL),
                JSON_TABLE("将JSON表达式中的数据作为关系表", Category.JSON, null),
                JSON_TO_RECORD("JSON_TO_RECORD", Category.JSON, DatabaseType.PostgreSQL),
                JSON_TO_RECORDSET("JSON_TO_RECORDSET", Category.JSON, DatabaseType.PostgreSQL),
                JSON_TO_TS_VECTOR("根据指定或默认配置对单词进行规范化。", Category.JSON, DatabaseType.PostgreSQL),
                JSON_TYPE("JSON值的类型", Category.JSON, null),
                JSON_TYPEOF("JSON_TYPEOF", Category.JSON, DatabaseType.PostgreSQL),
                JSON_UNQUOTE("取消引用JSON值", Category.JSON, null),
                JSON_VALID("JSON值是否有效", Category.JSON, null),
                JSON_VALUE("从路径指向的位置的JSON中提取值", Category.JSON, null),
                JSONB_AGG("将所有输入值（包括空值）收集到 JSON 数组中", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_AGG_STRICT("将所有输入值（跳过 null）收集到 JSON 数组中", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_ARRAY_ELEMENTS("JSONB_ARRAY_ELEMENTS", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_ARRAY_ELEMENTS_TEXT("JSONB_ARRAY_ELEMENTS_TEXT", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_ARRAY_LENGTH("JSONB_ARRAY_LENGTH", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_EACH("JSONB_EACH", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_EACH_TEXT("JSONB_EACH_TEXT", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_EXTRACT_PATH("JSONB_EXTRACT_PATH", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_EXTRACT_PATH_TEXT("JSONB_EXTRACT_PATH_TEXT", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_INSERT("JSONB_INSERT", Category.JSON, DatabaseType.PostgreSQL, "json,path,value"),
                JSONB_OBJECT("JSONB_OBJECT", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_OBJECT_AGG("将所有键/值对收集到 JSON 对象中", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_OBJECT_AGG_STRICT("将所有键/值对收集到 JSON 对象中", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_OBJECT_AGG_UNIQUE("将所有键/值对收集到 JSON 对象中", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_OBJECT_AGG_UNIQUE_STRICT("将所有键/值对收集到 JSON 对象中", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_OBJECT_KEYS("JSONB_OBJECT_KEYS", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_PATH_EXISTS("JSONB_PATH_EXISTS", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_PATH_EXISTS_TZ("JSONB_PATH_EXISTS_TZ", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_PATH_MATCH("JSONB_PATH_MATCH", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_PATH_MATCH_TZ("JSONB_PATH_MATCH_TZ", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_PATH_QUERY("JSONB_PATH_QUERY", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_PATH_QUERY_ARRAY("JSONB_PATH_QUERY_ARRAY", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_PATH_QUERY_ARRAY_TZ("JSONB_PATH_QUERY_ARRAY_TZ", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_PATH_QUERY_FIRST("JSONB_PATH_QUERY_FIRST", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_PATH_QUERY_FIRST_TZ("JSONB_PATH_QUERY_FIRST_TZ", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_PATH_QUERY_TZ("JSONB_PATH_QUERY_TZ", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_POPULATE_RECORD("JSONB_POPULATE_RECORD", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_POPULATE_RECORD_VALID("JSONB_POPULATE_RECORD_VALID ", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_POPULATE_RECORDSET("JSONB_POPULATE_RECORDSET", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_PRETTY("JSONB_PRETTY", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_SET("JSONB_SET", Category.JSON, DatabaseType.PostgreSQL, "json,path,value"),
                JSONB_SET_LAX("JSONB_SET_LAX", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_STRIP_NULLS("JSONB_STRIP_NULLS", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_TO_RECORD("JSONB_TO_RECORD", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_TO_RECORDSET("JSONB_TO_RECORDSET", Category.JSON, DatabaseType.PostgreSQL),
                JSONB_TYPEOF("JSONB_TYPEOF", Category.JSON, DatabaseType.PostgreSQL),
                JULIAN("计算给定日期的儒略日", Category.DATE_TIME, null),
                JUSTIFY_DAYS("调整间隔，将 30 天的时间段转换为月份", Category.DATE_TIME, null),
                JUSTIFY_HOURS("调整间隔，将 24 小时时间段转换为天", Category.DATE_TIME, null),
                JUSTIFY_INTERVAL("使用 和 调整间隔，并进行额外的符号调整", Category.DATE_TIME, null),
                LAG("获取当前行前N行的值", Category.AGGREGATE, null),
                LAST_COMMITTED_XACT("最新提交事务的事务 ID、提交时间戳和复制源", Category.SYSTEM, DatabaseType.PostgreSQL),
                LAST_DAY("参数的当月最后一天", Category.DATE_TIME, null),
                LAST_INSERT_ID("最后一个 INSERT 的 AUTOINCREMENT 列的值", Category.SEQUENCE, null),
                LAST_ROW("最后行", Category.OTHER, null),
                LAST_VAL("序列最后值", Category.SEQUENCE, null),
                LAST_VALUE("最后一行的参数值", Category.AGGREGATE, null),
                LAST_WAL_RECEIVE_LSN("通过流复制接收并同步到磁盘的最后一个预写日志位置", Category.REPLICATION, DatabaseType.PostgreSQL),
                LAST_WAL_REPLAY_LSN("在恢复期间重放的最后一个预写日志位置", Category.REPLICATION, DatabaseType.PostgreSQL),
                LAST_XACT_REPLAY_TIMESTAMP("恢复期间重放的最后一个事务的时间戳", Category.SYSTEM, DatabaseType.PostgreSQL),
                LCM("最小公倍数", Category.MATH, null),
                LEAD("分区中当前行前导行的参数值", Category.AGGREGATE, null),
                LEAST("最小参数", Category.CONDITIONAL, null, "value1,value2"),
                LEFT("指定的最左边的字符数", Category.STRING, null, "string,length"),
                LENGTH("字符串以字节为单位的长度", Category.STRING, null, "string"),
                LGAMMA("伽马函数绝对值的自然对数", Category.MATH, null),
                LIKE("LIKE匹配", Category.STRING, null),
                LINE_STRING_TO_MULTI_LINE_STRING("根据 LineString 值构建 MultiLineString", Category.SPATIAL, null),
                LINE_STRING_TO_POLYGON("从 LineString 参数构造多边形", Category.SPATIAL, null),
                LIST_AGG("", Category.STRING, null, "string,delimiter"),
                LISTENING_CHANNELS("当前会话正在侦听的异步通知通道的一组名", Category.SYSTEM, DatabaseType.PostgreSQL),
                LN("参数的自然对数", Category.MATH, null, "value"),
                LNNVL("条件取反", Category.CONDITIONAL, DatabaseType.ORACLE),
                LOAD_FILE("加载命名文件", Category.SYSTEM, DatabaseType.PostgreSQL),
                LOCAL_TIME("", Category.DATE_TIME, null),
                LOCAL_TIMESTAMP("", Category.DATE_TIME, null),
                LOCATE("第一次出现的子字符串的位置", Category.STRING, DatabaseType.MySQL, "substring,string"),
                LOG("自然对数", Category.MATH, null, "value"),
                LOG_BACKEND_MEMORY_CONTEXTS("请求日志后端的内存上下文，并指定进程ID", Category.SYSTEM, DatabaseType.PostgreSQL),
                LOG_STANDBY_SNAPSHOT("拍摄正在运行的事务的快照并将其写入 WAL", Category.SYSTEM, DatabaseType.PostgreSQL),
                LOG10("以 10 为底的对数", Category.MATH, null, "value"),
                LOG2("以 2 为底的对数", Category.MATH, null, "value"),
                LOGICAL_SLOT_GET_BINARY_CHANGES("", Category.REPLICATION, DatabaseType.PostgreSQL),
                LOGICAL_SLOT_GET_CHANGES("槽中的更改，从最后使用更改的点开始", Category.REPLICATION, DatabaseType.PostgreSQL),
                LOGICAL_SLOT_PEEK_BINARY_CHANGES("", Category.REPLICATION, DatabaseType.PostgreSQL),
                LOGICAL_SLOT_PEEK_CHANGES("", Category.REPLICATION, DatabaseType.PostgreSQL),
                LOWER("小写", Category.STRING, null, "string"),
                LOWER_INC("范围的下限是否包含", Category.RANGE, DatabaseType.PostgreSQL),
                LOWER_INF("范围是否有下限", Category.RANGE, DatabaseType.PostgreSQL),
                LOWER_LIMIT("取字符串与其最近的下限", Category.RANGE, null),
                LPAD("字符串参数，左填充指定字符串", Category.STRING, null),
                LS_ARCHIVE_STATUS_DIR("服务器的WAL存档状态目录中每个普通文件的名称，大小和上次修改时间", Category.SYSTEM, DatabaseType.PostgreSQL),
                LS_DIR("指定目录中所有文件", Category.SYSTEM, DatabaseType.PostgreSQL),
                LS_LOG_DIR("服务器日志目录中每个普通文件的名称、大小和上次修改时间", Category.SYSTEM, DatabaseType.PostgreSQL),
                LS_LOGICAL_MAP_DIR("服务器目录中每个普通文件的名称、大小和上次修改时间", Category.SYSTEM, DatabaseType.PostgreSQL),
                LS_LOGICALS_NAP_DIR("服务器目录中每个普通文件的名称、大小和上次修改时间", Category.SYSTEM, DatabaseType.PostgreSQL),
                LS_REPLSLOT_DIR("服务器目录中每个普通文件的名称、大小和上次修改时间", Category.SYSTEM, DatabaseType.PostgreSQL),
                LS_SUMMARIES_DIR("服务器的WAL摘要目录中每个普通文件的名称，大小和上次修改时间", Category.SYSTEM, DatabaseType.PostgreSQL),
                LS_TMPDIR("指定表空间的临时文件目录中每个普通文件的名称、大小和上次修改时间", Category.SYSTEM, DatabaseType.PostgreSQL),
                LS_WAL_DIR("服务器预写日志 （WAL） 目录中每个普通文件的名称、大小和上次修改时间", Category.REPLICATION, DatabaseType.PostgreSQL),
                LSEG("对角线", Category.SPATIAL, null),
                LTRIM("删除前导空格", Category.STRING, null),
                MAKE_ACL_ITEM("构造具有给定属性的属性", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                MAKE_DATE("从年份和年份中的日期创建日期", Category.DATE_TIME, null),
                MAKE_INTERVAL("从年、月、周、日、小时、分钟和秒字段创建间隔", Category.DATE_TIME, null),
                MAKE_REF("创建对象视图的行或对象表中的行，其对象标识符基于主键", Category.CATALOG, DatabaseType.ORACLE),
                MAKE_SET("根据二进制位选择对应的字符串参数，返回由指定位的字符串组成的集合", Category.STRING, DatabaseType.MySQL),
                MAKE_TIME("从小时、分钟、秒创建时间", Category.DATE_TIME, null),
                MAKE_TIMESTAMP("从年、月、日、小时、分钟和秒字段创建时间戳", Category.DATE_TIME, null),
                MASTER_POS_WAIT("阻粘直到副本读取并应用了所有更新", Category.REPLICATION, DatabaseType.MySQL),
                MATCH("全文搜索", Category.FULLTEXT, DatabaseType.PostgreSQL),
                MAX("最大值", Category.AGGREGATE, null),
                MBR_CONTAINS("判断一个几何对象的最小包围矩形（MBR）是否完全包含另一个几何对象的MBR", Category.SPATIAL, null),
                MBR_COVERED_BY("一个 MBR 是否被另一个 MBR 覆盖", Category.SPATIAL, null),
                MBR_COVERS("一个 MBR 是否涵盖另一个", Category.SPATIAL, null),
                MBR_DISJOINT("两个几何形状的MBR是否不相交", Category.SPATIAL, null),
                MBR_EQUALS("两个几何形状的MBR是否相等", Category.SPATIAL, null),
                MBR_INTERSECTS("两个几何形状的 MBR 是否相交", Category.SPATIAL, null),
                MBR_OVERLAPS("两个几何形状的 MBR 是否重叠", Category.SPATIAL, null),
                MBR_TOUCHES("两个几何形状的 MBR 是否接触", Category.SPATIAL, null),
                MBR_WITHIN("一个几何的 MBR 是否在另一个几何的 MBR 内", Category.SPATIAL, null),
                MCV_LIST_ITEMS("存储在多列 MCV 列表中的所有项目", Category.CATALOG, DatabaseType.PostgreSQL),
                MD5("MD5", Category.CRYPTO, null),
                MEDIAN("中位数", Category.AGGREGATE, null),
                MEMBER_OF("检查一个值是否存在于数组中", Category.CONDITIONAL, null),
                MERGE_ACTION("为当前行执行的合并作命令", Category.CONDITIONAL, null),
                MICROSECOND("微秒", Category.DATE_TIME, null),
                MID("从指定位置开始的子字符串", Category.STRING, DatabaseType.MySQL),
                MIDNIGHT_SECONDS("计算指定的时间与午夜之间的秒数", Category.DATE_TIME, null),
                MIN("最小值", Category.AGGREGATE, null),
                MIN_SCALE("精确表示所提供值所需的最小刻度", Category.MATH, null),
                MINUTE("分钟", Category.DATE_TIME, null),
                MOD("余数", Category.MATH, null, "dividend,divisor"),
                MODE("计算一个值与一组值之和的比值", Category.OTHER, null),
                MONTH("月份", Category.DATE_TIME, null),
                MONTH_NAME("月份名称", Category.DATE_TIME, null),
                MONTHS_BETWEEN("月份差", Category.DATE_TIME, DatabaseType.ORACLE),
                MULTI_RANGE("仅包含给定范围的多范围", Category.RANGE, DatabaseType.PostgreSQL),
                MXID_AGE("当前 MULTIXACTS 计数器之间的 MULTIXACTS ID 数", Category.SYSTEM, DatabaseType.PostgreSQL),
                MY_TEMP_SCHEMA("当前会话的临时模式的 OID", Category.SYSTEM, DatabaseType.PostgreSQL),
                NAME_CONST("指定列名", Category.STRING, DatabaseType.MySQL),
                NAN_NVL("当第一个参数为NaN非数字时返回第二个参数", Category.NULL_HANDLE, null),
                NAN_VALUE("是否NAN", Category.NULL_HANDLE, null),
                NCHR("与数字二进制等价的字符", Category.TYPE_CAST, DatabaseType.ORACLE),
                NEW_ID("全球唯一标识符", Category.UUID, null),
                NEW_TIME("将一个日期和时间值从一个指定的时区转换到另一个指定的时区", Category.DATE_TIME, null),
                NEXT_DAY("指定日期 date 和星期 X, 求出星期 X 的日期", Category.DATE_TIME, DatabaseType.ORACLE),
                NEXT_VAL("序列值", Category.SEQUENCE, null),
                NLS_CHARSET_DECL_LEN("特定字符集下的声明长度", Category.STRING, DatabaseType.ORACLE),
                NLS_CHARSET_ID("字符集名称对应的字符集 ID", Category.STRING, DatabaseType.ORACLE),
                NLS_CHARSET_NAME("字符集ID对应的字符集名称", Category.STRING, DatabaseType.ORACLE),
                NLS_COLLATION_ID("根据字符排序名称返回对应的排序 ID 编号", Category.STRING, DatabaseType.ORACLE),
                NLS_COLLATION_NAME("根据字符排序名称返回对应的排序 ID 编号", Category.STRING, DatabaseType.ORACLE),
                NLS_INITCAP("根据语言环境第一个字母转换为大写", Category.STRING, DatabaseType.ORACLE),
                NLS_LOWER("小写", Category.STRING, DatabaseType.ORACLE),
                NLS_SORT("将字符串转换为指定排序方式对应的二进制编码排序", Category.STRING, DatabaseType.ORACLE),
                NLS_UPPER("大写", Category.STRING, DatabaseType.ORACLE),
                NORMALIZE("将字符串转换为指定的 Unicode 规范化形式", Category.ENCODING, null),
                NOTIFICATION_QUEUE_USAGE("异步通知队列的最大大小的分数", Category.SYSTEM, DatabaseType.PostgreSQL),
                NOW("当前日期和时间", Category.DATE_TIME, null),
                NTH_VALUE("第 N 行的参数值", Category.AGGREGATE, null),
                NTILE("其分区中当前行的存储桶号", Category.AGGREGATE, null),
                NULLIF("如果expr1 = expr2 成立，那么返回值为NULL", Category.NULL_HANDLE, null, "expr1,expr2"),
                NUM_NODE("词位加上运算符的数量", Category.FULLTEXT, DatabaseType.PostgreSQL),
                NUMA_AVAILABLE("服务器已使用 NUMA 支持进行编译", Category.SYSTEM, DatabaseType.PostgreSQL),
                NUMBER_TO_DS_INTERVAL("将数值转换成 INTERVAL DAY TO SECOND 类型", Category.TYPE_CAST, DatabaseType.ORACLE),
                NUMBER_TO_YM_INTERVAL("将数值转换成 INTERVAL YEAR TO MONTH 类型", Category.TYPE_CAST, DatabaseType.ORACLE),
                NVL("第一个非空值,支持两个参数", Category.CONDITIONAL, null, "value,replacement"),
                OBJ_DESCRIPTION("获得对象的描述信息", Category.CATALOG, DatabaseType.PostgreSQL),
                OCT("包含数字的八进制表示的字符串", Category.TYPE_CAST, DatabaseType.MySQL),
                OCTET_LENGTH("字节数", Category.STRING, null),
                OPERATOR_CLASS_IS_VISIBLE("运算符类在搜索路径中可见", Category.CATALOG, DatabaseType.PostgreSQL),
                OPERATOR_FAMILY_IS_VISIBLE("运算符族在搜索路径中是否可见", Category.CATALOG, DatabaseType.PostgreSQL),
                OPERATOR_IS_VISIBLE("运算符在搜索路径中可见", Category.CATALOG, DatabaseType.PostgreSQL),
                OPTIONS_TO_TABLE("存储选项集", Category.CATALOG, DatabaseType.PostgreSQL),
                ORD("参数最左侧字符的字符代码", Category.TYPE_CAST, DatabaseType.ORACLE),
                OVERLAY("从指定的位置，取指定的长度，用另一个字符中来代替", Category.STRING, null),
                PARSE_IDENT("将qualified_identifier拆分为标识符数组，删除单个标识符的任何引号", Category.STRING, DatabaseType.PostgreSQL),
                PARTITION_ANCESTORS("列出给定分区的祖先关系", Category.CATALOG, DatabaseType.PostgreSQL),
                PARTITION_ROOT("给定关系所属的分区树的最顶层父级", Category.CATALOG, DatabaseType.PostgreSQL),
                PARTITION_TREE("列出给定分区表或分区索引的分区树中的表或索引", Category.CATALOG, DatabaseType.PostgreSQL),
                PAT_INDEX("一个字串在指定表达式中的起始位置，", Category.STRING, null),
                PATH("指向父条件中指定的资源的相对路径", Category.OTHER, DatabaseType.ORACLE),
                PERCENT_RANK("排名值百分比", Category.AGGREGATE, null),
                PERCENTILE_CONT("计算连续百分位数", Category.AGGREGATE, null),
                PERCENTILE_DISC("计算离散百分位数", Category.AGGREGATE, null),
                PERIOD_ADD("向年月添加期间", Category.DATE_TIME, null),
                PERIOD_DIFF("期间之间的月数", Category.DATE_TIME, null),
                PHRASE_TO_TSQUERY("将文本转换为 ，根据指定或默认配置规范化单词", Category.FULLTEXT, DatabaseType.PostgreSQL),
                PI("PI", Category.MATH, null),
                PLAIN_TO_TSQUERY("将文本转换为 ，根据指定或默认配置规范化单词", Category.FULLTEXT, DatabaseType.PostgreSQL),
                POINT_TO_LINE_STRING("从点值构造 LineString", Category.SPATIAL, null),
                POINT_TO_MULTI_POINT("根据 Point 值构造 MultiPoint", Category.SPATIAL, null),
                POLYGON_TO_MULTI_POLYGON("从多边形值构造多边形集合", Category.SPATIAL, null),
                POSITION("LOCATE", Category.STRING, null, "substring,string"),
                POSTMASTER_START_TIME("服务器启动的时间", Category.SYSTEM, DatabaseType.PostgreSQL),
                POW("幂", Category.MATH, null, "base,exponent"),
                POWER("幂", Category.MATH, null, "base,exponent"),
                POWER_MULTIS_ET("一个包含输入嵌套表所有非空子集的嵌套表", Category.CATALOG, DatabaseType.ORACLE),
                POWER_MULTIS_ET_BY_CARDINALITY("接收一个嵌套表和一个基数作为输入，返回一个包含所有非空子集的嵌套表嵌套表", Category.CATALOG, DatabaseType.ORACLE),
                PREDICTION("所选内容中每一行的预测", Category.DATA_MINING, DatabaseType.ORACLE),
                PREDICTION_BOUNDS("预测所选内容中每一行的类或值", Category.DATA_MINING, DatabaseType.ORACLE),
                PREDICTION_COST("选择中每一行的成本", Category.DATA_MINING, DatabaseType.ORACLE),
                PREDICTION_DETAILS("选择中每一行的预测细节", Category.DATA_MINING, DatabaseType.ORACLE),
                PREDICTION_PROBABILITY("选择中每一行的概率", Category.DATA_MINING, DatabaseType.ORACLE),
                PREDICTION_SET("所选内容中每一行的概率或成本", Category.DATA_MINING, DatabaseType.ORACLE),
                PRESENTNNV("", Category.CONDITIONAL, DatabaseType.ORACLE),
                PRESENTV("", Category.CONDITIONAL, DatabaseType.ORACLE),
                PREVIOUS("", Category.CONDITIONAL, DatabaseType.ORACLE),
                PROMOTE("将备用服务器提升为主状态", Category.SYSTEM, DatabaseType.PostgreSQL),
                PS_CURRENT_THREAD_ID("当前线程的性能架构线程 ID", Category.SYSTEM, DatabaseType.MySQL),
                PS_THREAD_ID("给定线程的性能架构线程 ID", Category.SYSTEM, DatabaseType.MySQL),
                PWD_COMPARE("比较数据库用户显示密码和加密密码", Category.SYSTEM, DatabaseType.ORACLE),
                QUARTER("季度", Category.DATE_TIME, null),
                QUERY_TO_XML("QUERY_TO_XML", Category.XML, DatabaseType.PostgreSQL),
                QUERY_TO_XML_AND_XML_SCHEMA("QUERY_TO_XML_AND_XMLSCHEMA", Category.XML, DatabaseType.PostgreSQL),
                QUERY_TO_XML_SCHEMA("QUERY_TO_XMLSCHEMA", Category.XML, DatabaseType.PostgreSQL),
                QUERY_TREE("将文本转换为 ，根据指定或默认配置规范化单词", Category.FULLTEXT, DatabaseType.PostgreSQL),
                QUOTE("转义", Category.STRING, DatabaseType.PostgreSQL),
                QUOTE_IDENT("输出字符串表达式", Category.STRING, DatabaseType.PostgreSQL),
                QUOTE_LITERAL("输出字符串表达式", Category.STRING, DatabaseType.PostgreSQL),
                QUOTE_NULLABLE("输出字符串表达式", Category.STRING, DatabaseType.PostgreSQL),
                RADIANS("转换为弧度", Category.MATH, null),
                RADIUS("计算圆的半径", Category.SPATIAL, null),
                RAND("随机浮点值", Category.MATH, null),
                RANDOM("随机浮点值", Category.MATH, null),
                RANDOM_BYTES("一个随机字节向量", Category.MATH, null),
                RANDOM_INT("随机整数值", Category.MATH, null),
                RANDOM_NORMAL("从给定参数的正态分布中返回一个随机值", Category.MATH, null),
                RANGE_AGG("计算非空输入值的并集", Category.RANGE, DatabaseType.PostgreSQL),
                RANGE_INTERSECT_AGG("计算非空输入值的交集", Category.RANGE, DatabaseType.PostgreSQL),
                RANGE_MERGE("计算包括整个多范围的最小范围", Category.RANGE, DatabaseType.PostgreSQL),
                RANK("其分区中当前行的排名", Category.AGGREGATE, null),
                RATIO_TO_REPORT("", Category.CATALOG, DatabaseType.ORACLE),
                RAW_TO_HEX("将原始值转换为一个包含其十六进制表示的字符值", Category.TYPE_CAST, DatabaseType.ORACLE),
                RAW_TO_NHEX("将原始值转换为一个包含其十六进制表示的字符值", Category.TYPE_CAST, DatabaseType.ORACLE),
                READ_BINARY_FILE("全部或部分文件", Category.SYSTEM, DatabaseType.PostgreSQL),
                READ_FILE("文本文件的全部或部分，从给定的字节偏移量开始，返回最长的字节", Category.SYSTEM, DatabaseType.PostgreSQL),
                REBUILD_TABLE("重建所有表", Category.SYSTEM, DatabaseType.ORACLE),
                REF("为绑定到变量或行的对象实例返回一个值", Category.CATALOG, DatabaseType.ORACLE),
                REF_TO_HEX("将参数expr转换为包含其十六进制等价项的字符值", Category.TYPE_CAST, DatabaseType.ORACLE),
                REGEXP("字符串是否与正则表达式匹配", Category.REGEX, null),
                REGEXP_COUNT("正则表达式模式匹配的次数", Category.REGEX, null),
                REGEXP_INSTR("正则表达式模式在字符串中的位置", Category.REGEX, null, "string,pattern,position,occurrence,return_option,match_parameter,subexpr"),
                REGEXP_LIKE("字符串是否与正则表达式匹配", Category.REGEX, null),
                REGEXP_MATCH("正则表达式模式与字符串的第一个匹配项内的子字符串", Category.REGEX, DatabaseType.PostgreSQL),
                REGEXP_MATCHES("正则表达式模式与字符串的所有匹配项内的子字符串", Category.REGEX, DatabaseType.PostgreSQL),
                REGEXP_POSITION("子字符串匹配正则表达式的起始索引", Category.REGEX, null),
                REGEXP_REPLACE("替换与正则表达式匹配的子字符串", Category.REGEX, null, "string,pattern,replacement,position,occurrence,match_parameter"),
                REGEXP_SPLIT_TO_ARRAY("正则表达式作为分隔符拆分字符串，生成结果数组", Category.REGEX, DatabaseType.PostgreSQL),
                REGEXP_SPLIT_TO_TABLE("正则表达式作为分隔符拆分字符串，生成一组结果集", Category.REGEX, DatabaseType.PostgreSQL),
                REGEXP_SUBSTR("子字符串匹配正则表达式", Category.REGEX, null, "string,pattern,position,occurrence,match_parameter,subexpr"),
                REGR_AVGX("计算自变量 的平均值", Category.AGGREGATE, null),
                REGR_AVGY("计算因变量 的平均值", Category.AGGREGATE, null),
                REGR_COUNT("计算两个输入都为非空的行数", Category.AGGREGATE, null),
                REGR_INTERCEPT("计算由 （X， Y） 对确定的最小二乘拟合线性方程的 y 截距", Category.AGGREGATE, null),
                REGR_R2("计算相关系数的平方", Category.AGGREGATE, null),
                REGR_SLOPE("计算由 （X， Y） 对确定的最小二乘拟合线性方程的斜率", Category.AGGREGATE, null),
                REGR_SXX("计算自变量的“平方和”", Category.AGGREGATE, null),
                REGR_SXY("计算独立乘时因变量的“乘积之和”", Category.AGGREGATE, null),
                REGR_SYY("计算因变量的“平方和”", Category.AGGREGATE, null),
                RELATION_FILE_NODE("当前分配给指定关系的“FILENODE”编号", Category.SYSTEM, DatabaseType.PostgreSQL),
                RELATION_FILEPATH("关系的整个文件路径名", Category.SYSTEM, DatabaseType.PostgreSQL),
                RELATION_SIZE("计算指定关系的一个“分支”使用的磁盘空间", Category.SYSTEM, DatabaseType.PostgreSQL),
                RELEASE_ALL_LOCKS("释放所有当前命名锁", Category.LOCK, DatabaseType.MySQL),
                RELEASE_LOCK("释放命名锁", Category.LOCK, null),
                RELOAD_CONF("使 POSTGRESQL 服务器的所有进程重新加载其配置文件", Category.SYSTEM, DatabaseType.PostgreSQL),
                REMAINDER("余数", Category.MATH, null),
                REMOVE_COLUMN_CONTROL("移除列级控制", Category.LABEL_SECURITY, null),
                REMOVE_COLUMN_CONTROL_CONSTRAINT("移除列级控制推理约束", Category.LABEL_SECURITY, null),
                REMOVE_ROW_CONTROL("移除行级控制", Category.LABEL_SECURITY, null),
                REMOVE_TABLE_CONTROL("移除表级控制", Category.LABEL_SECURITY, null),
                REMOVE_TABLE_POLICY("取消应用策略到表", Category.LABEL_SECURITY, null),
                REPEAT("重复字符串指定次数", Category.STRING, null),
                REPLACE("替换指定字符串的出现次数", Category.STRING, null, "string,search,replace"),
                REPLICATION_ORIGIN_ADVANCE("将给定节点的复制进度设置为给定位置", Category.REPLICATION, DatabaseType.PostgreSQL),
                REPLICATION_ORIGIN_CREATE("使用给定的外部名称创建复制源，并返回分配给它的内部 ID", Category.REPLICATION, DatabaseType.PostgreSQL),
                REPLICATION_ORIGIN_DROP("删除以前创建的复制源", Category.REPLICATION, DatabaseType.PostgreSQL),
                REPLICATION_ORIGIN_OID("按名称查找复制源并返回内部 ID", Category.REPLICATION, DatabaseType.PostgreSQL),
                REPLICATION_ORIGIN_PROGRESS("给定复制源的重放位置", Category.REPLICATION, DatabaseType.PostgreSQL),
                REPLICATION_ORIGIN_SESSION_IS_SETUP("当前会话中选择了复制源", Category.REPLICATION, DatabaseType.PostgreSQL),
                REPLICATION_ORIGIN_SESSION_PROGRESS("当前会话中选择的复制源的重放位置", Category.REPLICATION, DatabaseType.PostgreSQL),
                REPLICATION_ORIGIN_SESSION_RESET("取消效果", Category.REPLICATION, DatabaseType.PostgreSQL),
                REPLICATION_ORIGIN_SESSION_SETUP("将当前会话标记为从给定源的重播", Category.REPLICATION, DatabaseType.PostgreSQL),
                REPLICATION_ORIGIN_XACT_RESET("取消效果", Category.REPLICATION, DatabaseType.PostgreSQL),
                REPLICATION_ORIGIN_XACT_SETUP("将当前事务标记为重播在给定 LSN 和时间戳处提交的事务", Category.REPLICATION, DatabaseType.PostgreSQL),
                REPLICATION_SLOT_ADVANCE("前进复制槽的当前确认位置", Category.REPLICATION, DatabaseType.PostgreSQL),
                RESTATISTICS("重建所有统计信息", Category.SYSTEM, DatabaseType.PostgreSQL),
                RESTORE_ATTRIBUTE_STATS("创建或更新列级统计信息", Category.SYSTEM, DatabaseType.PostgreSQL),
                RESTORE_RELATION_STATS("更新表级统计信息", Category.SYSTEM, DatabaseType.PostgreSQL),
                REVERSE("反转字符串中的字符", Category.STRING, null),
                RIGHT("指定的最右边的字符数", Category.STRING, null, "string,length"),
                RLIKE("字符串是否与正则表达式匹配", Category.REGEX, null),
                ROLES_GRAPHML("表示内存角色子图的 GraphML", Category.OTHER, DatabaseType.PostgreSQL),
                ROTATE_LOGFILE("提示日志文件管理器立即切换到新的输出文件", Category.SYSTEM, DatabaseType.PostgreSQL),
                ROUND("四舍五入", Category.MATH, null, "value,precision"),
                ROW_COUNT("更新的行数", Category.SYSTEM, null),
                ROW_ID_TO_CHAR("rowid 值转为 VARCHAR2 类型", Category.TYPE_CAST, DatabaseType.ORACLE),
                ROW_ID_TO_NCHAR("rowid 值转为 VARCHAR2 类型", Category.TYPE_CAST, DatabaseType.ORACLE),
                ROW_NUMBER("其分区中的当前行数", Category.AGGREGATE, null),
                ROW_SECURITY_ACTIVE("在当前用户和当前环境的上下文中，指定表的行级安全性是否处于活动状态", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                ROW_TO_JSON("ROW_TO_JSON", Category.JSON, DatabaseType.PostgreSQL),
                RPAD("附加字符串指定次数", Category.STRING, null),
                RTRIM("删除尾随空格", Category.STRING, null),
                SAFE_SNAPSHOT_BLOCKING_PIDS("阻止具有指定进程 ID 的服务器进程获取安全快照的会话的进程 ID 数组", Category.LOCK, DatabaseType.PostgreSQL),
                SCALE("小数的比例(小数部分中的小数位数)", Category.MATH, null),
                SCHEMA("SCHEMA", Category.SYSTEM, null),
                SCHEMA_TO_XML("SCHEMA_TO_XML", Category.XML, DatabaseType.PostgreSQL),
                SCHEMA_TO_XML_AND_XML_SCHEMA("SCHEMA_TO_XML_AND_XMLSCHEMA", Category.XML, DatabaseType.PostgreSQL),
                SCHEMA_TO_XML_SCHEMA("SCHEMA_TO_XMLSCHEMA", Category.XML, DatabaseType.PostgreSQL),
                SCN_TO_TIMESTAMP("给定的系统更改号相关联的近似时间戳", Category.DATE_TIME, DatabaseType.ORACLE),
                SEC_TO_TIME("将秒转换为时间", Category.DATE_TIME, null),
                SECOND("秒", Category.DATE_TIME, null),
                SESSION_TIMEZONE("时区", Category.DATE_TIME, DatabaseType.ORACLE),
                SESSION_USER("当前用户", Category.SYSTEM, null),
                SET("通过消除重复项将嵌套表转换为集合", Category.CATALOG, DatabaseType.ORACLE),
                SET_BIT("设置二进制字符串中的第 n 位", Category.BIT_OP, null),
                SET_BYTE("设置二进制字符串中的第 n 字节", Category.BIT_OP, null),
                SET_CONFIG("设置某个配置参数的值", Category.SYSTEM, DatabaseType.PostgreSQL),
                SET_SEED("设置后续和调用的种子", Category.MATH, DatabaseType.PostgreSQL),
                SET_TAB_CONTROL_BASE_TIME("设置基于时间段的列集合访问控制", Category.LABEL_SECURITY, null),
                SET_USER_PRIVILEGE("为用户授权特权", Category.PRIVILEGE, DatabaseType.PostgreSQL),
                SET_VAL("设置一个序列的当前值", Category.SEQUENCE, null),
                SET_WEIGHT("为向量的每个元素分配指定的权重", Category.FULLTEXT, DatabaseType.PostgreSQL),
                SETTINGS_GET_FLAGS("给定 GUC 关联的标志数组", Category.SYSTEM, DatabaseType.PostgreSQL),
                SH_OBJECT_DESCRIPTION("OID 指定的共享数据库对象的注释和包含系统目录的名称", Category.CATALOG, DatabaseType.PostgreSQL),
                SHA1("计算 SHA-1 160 位校验和", Category.CRYPTO, null),
                SHA2("计算 SHA-2 校验和", Category.CRYPTO, null),
                SHA224("计算二进制字符串的 SHA-224 哈希值", Category.CRYPTO, null),
                SHA256("计算二进制字符串的 SHA-256 哈希值", Category.CRYPTO, null),
                SHA384("计算二进制字符串的 SHA-384 哈希值", Category.CRYPTO, null),
                SHA512("计算二进制字符串的 SHA-512 哈希值", Category.CRYPTO, null),
                SHOW_BINLOG_EVENTS("获得指定 Binlog 文件的详细内容", Category.REPLICATION, DatabaseType.MySQL),
                SIGN("符号", Category.MATH, null),
                SIN("正弦", Category.MATH, null, "value"),
                SIND("正弦,以度为单位的参数", Category.MATH, null),
                SINH("双曲正弦", Category.MATH, null),
                SIZE_BYTES("将可读格式的大小转换为字节", Category.SYSTEM, null),
                SIZE_PRETTY("将以字节为单位的大小转换为更易于阅读的格式", Category.SYSTEM, DatabaseType.PostgreSQL),
                SLEEP("睡眠几秒钟", Category.SYSTEM, null),
                SLEEP_INTERVAL("睡眠间隔", Category.SYSTEM, null),
                SLEEP_UNTIL("睡眠到", Category.SYSTEM, null),
                SLOPE("斜率", Category.AGGREGATE, null),
                SNAPSHOT_XIP("快照中包含的一组正在进行的事务 ID", Category.REPLICATION, DatabaseType.PostgreSQL),
                SNAPSHOT_XMAX("快照的XMAX", Category.REPLICATION, DatabaseType.PostgreSQL),
                SNAPSHOT_XMIN("快照的XMIN", Category.REPLICATION, DatabaseType.PostgreSQL),
                SOUNDEX("SOUNDEX 值", Category.STRING, null),
                SOUNDS_LIKE("比较声音", Category.STRING, DatabaseType.MySQL),
                SOURCE_POS_WAIT("阻止直到副本读取并应用了所有更新", Category.REPLICATION, DatabaseType.MySQL),
                SPACE("指定空格数的字符串", Category.STRING, null),
                SPLIT_PART("以给定的字符串为分隔符，来分隔另一个字符串，并取出指定的分隔部分", Category.STRING, DatabaseType.PostgreSQL),
                SPLIT_WAL_FILE_NAME("从 WAL 文件名中提取序列号和时间线 ID", Category.REPLICATION, DatabaseType.PostgreSQL),
                SQRT("平方根", Category.MATH, null, "value"),
                ST_AREA("多边形或多多边形区域", Category.SPATIAL, null),
                ST_BUFFER("与几何给定距离内的点的几何", Category.SPATIAL, null),
                ST_BUFFER_STRATEGY("生成ST_BUFFER的策略选项", Category.SPATIAL, null),
                ST_CENTROID("质心作为点", Category.SPATIAL, null),
                ST_COLLECT("将空间值聚合到集合中", Category.SPATIAL, null),
                ST_CONTAINS("一个几何是否包含另一个几何", Category.SPATIAL, null),
                ST_CONVEX_HULL("几何凸包", Category.SPATIAL, null),
                ST_CROSSES("一个几何体是否与另一个几何体交叉", Category.SPATIAL, null),
                ST_DIFFERENCE("两个几何形状的点设置差异", Category.SPATIAL, null),
                ST_DIMENSION("几何尺寸", Category.SPATIAL, null),
                ST_DISJOINT("一个几何是否与另一个几何不相交", Category.SPATIAL, null),
                ST_DISTANCE("一个几何体与另一个几何体的距离", Category.SPATIAL, null),
                ST_DISTANCE_SPHERE("两个几何形状之间的地球上最小距离", Category.SPATIAL, null),
                ST_END_POINT("LINESTRING的终点", Category.SPATIAL, null),
                ST_ENVELOPE("几何的MBR", Category.SPATIAL, null),
                ST_EQUALS("一个几何是否等于另一个几何", Category.SPATIAL, null),
                ST_EXTERIOR_RING("多边形的外环", Category.SPATIAL, null),
                ST_FRECHET_DISTANCE("一个几何体与另一个几何体的离散FRÉCHET距离", Category.SPATIAL, null),
                ST_GEOMETRY_COLLECTION_FROM_TEXT("从WKT几何集合", Category.SPATIAL, null),
                ST_GEOMETRY_COLLECTION_FROM_WKB("从WKB几何集合", Category.SPATIAL, null),
                ST_GEOMETRY_FROM_GEO_JSON("从GEOJSON对象生成几何图形", Category.SPATIAL, null),
                ST_GEOMETRY_FROM_TEXT("从WKT几何", Category.SPATIAL, null),
                ST_GEOMETRY_FROM_WKB("从WKB几何", Category.SPATIAL, null),
                ST_GEOMETRY_HASH("生成地理哈希值", Category.SPATIAL, null),
                ST_GEOMETRY_N("从几何集合中第N个几何", Category.SPATIAL, null),
                ST_GEOMETRY_TYPE("几何类型的名称", Category.SPATIAL, null),
                ST_HAUSDORFF_DISTANCE("一个几何与另一个几何的离散豪斯多夫距离", Category.SPATIAL, null),
                ST_INTERIOR_RING_N("多边形的第N个内环", Category.SPATIAL, null),
                ST_INTERSECTION("点集两个几何的交点", Category.SPATIAL, null),
                ST_INTERSECTS("一个几何体是否与另一个几何体相交", Category.SPATIAL, null),
                ST_IS_CLOSED("几何体是否封闭且简单", Category.SPATIAL, null),
                ST_IS_EMPTY("几何是否为空", Category.SPATIAL, null),
                ST_IS_OPEN("几何体是否开放", Category.SPATIAL, null),
                ST_IS_SIMPLE("几何形状是否简单", Category.SPATIAL, null),
                ST_IS_VALID("几何是否有效", Category.SPATIAL, null),
                ST_LAT_FROM_GEO_HASH("从GEOHASH值纬度", Category.SPATIAL, null),
                ST_LATITUDE("点的纬度", Category.SPATIAL, null),
                ST_LENGTH("LINESTRING的长度", Category.SPATIAL, null),
                ST_LINE_INTERPOLATE_POINT("沿LINESTRING指向给定百分比", Category.SPATIAL, null),
                ST_LINE_INTERPOLATE_POINTS("沿LINESTRING指向给定百分比的", Category.SPATIAL, null),
                ST_LINE_STRING_FROM_TEXT("从WKT构造LINESTRING", Category.SPATIAL, null),
                ST_LINE_STRING_FROM_WKB("从WKB构造LINESTRING", Category.SPATIAL, null),
                ST_LONG_FROM_GEO_HASH("从GEOHASH值经度", Category.SPATIAL, null),
                ST_LONGITUDE("点的经度", Category.SPATIAL, null),
                ST_MAKE_ENVELOPE("围绕两点的矩形", Category.SPATIAL, null),
                ST_MULTI_LINE_STRING_FROM_TEXT("从WKT构造MULTILINESTRING", Category.SPATIAL, null),
                ST_MULTI_LINE_STRING_FROM_WKB("从WKB构造MULTILINESTRING", Category.SPATIAL, null),
                ST_MULTI_POINT_FROM_TEXT("从WKT构造MULTIPOINT", Category.SPATIAL, null),
                ST_MULTI_POINT_FROM_WKB("从WKB构造MULTIPOINT", Category.SPATIAL, null),
                ST_MULTI_POLYGON_FROM_TEXT("从WKT构造多多边形", Category.SPATIAL, null),
                ST_MULTI_POLYGON_FROM_WKB("从WKB构造多多边形", Category.SPATIAL, null),
                ST_NUM_GEOMETRIES("几何集合中的几何数", Category.SPATIAL, null),
                ST_NUM_INTERIOR_RINGS("多边形中的内环数", Category.SPATIAL, null),
                ST_NUM_POINTS("LINESTRING中的点数", Category.SPATIAL, null),
                ST_OVERLAPS("一个几何体是否与另一个几何体重叠", Category.SPATIAL, null),
                ST_PATH_CLOSE("将路径转换为封闭形式", Category.SPATIAL, null),
                ST_PATH_OPEN("将路径转换为开放形式", Category.SPATIAL, null),
                ST_POINT_AT_DISTANCE("沿LINESTRING给定距离的点", Category.SPATIAL, null),
                ST_POINT_COUNT("点数量", Category.SPATIAL, null),
                ST_POINT_FROM_GEO_HASH("将地理哈希值转换为POINT值", Category.SPATIAL, null),
                ST_POINT_FROM_TEXT("从WKT构造点", Category.SPATIAL, null),
                ST_POINT_FROM_WKB("从WKB构造点", Category.SPATIAL, null),
                ST_POINT_N("从LINESTRING第N个点", Category.SPATIAL, null),
                ST_POLYGON_FROM_TEXT("从WKT构造多边形", Category.SPATIAL, null),
                ST_POLYGON_FROM_WKB("从WKB构造多边形", Category.SPATIAL, null),
                ST_SIMPLIFY("简化的几何", Category.SPATIAL, null),
                ST_SRID("几何的空间参考系统ID", Category.SPATIAL, null),
                ST_START_POINT("LINESTRING的起点", Category.SPATIAL, null),
                ST_SWAP_XY("交换X/Y坐标的参数", Category.SPATIAL, null),
                ST_SYM_DIFFERENCE("点集两个几何的对称差值", Category.SPATIAL, null),
                ST_TO_GEO_JSON("从几何图形生成GEO JSON对象", Category.SPATIAL, null),
                ST_TO_WKB("从内部几何格式转换为WKB", Category.SPATIAL, null),
                ST_TO_WKT("从内部几何格式转换为WKT", Category.SPATIAL, null),
                ST_TOUCHES("一个几何体是否接触另一个几何体", Category.SPATIAL, null),
                ST_TRANSFORM("变换几何坐标", Category.SPATIAL, null),
                ST_UNION("点集两个几何的并集", Category.SPATIAL, null),
                ST_VALIDATE("已验证的几何图形", Category.SPATIAL, null),
                ST_WITHIN("一个几何是否在另一个几何中", Category.SPATIAL, null),
                ST_X("点的X坐标", Category.SPATIAL, null),
                ST_Y("点的Y坐标", Category.SPATIAL, null),
                STANDARD_HASH("计算给定表达式哈希值", Category.CRYPTO, DatabaseType.ORACLE),
                STARTS_WITH("是否字符串以前缀开头", Category.STRING, null),
                STATEMENT_DIGEST("计算语句摘要哈希值", Category.SYSTEM, DatabaseType.MySQL),
                STATEMENT_DIGEST_TEXT("计算规范化语句摘要", Category.SYSTEM, DatabaseType.MySQL),
                STATISTICS_OBJ_IS_VISIBLE("统计对象在搜索路径中是否可见", Category.CATALOG, DatabaseType.PostgreSQL),
                STATS_BINOMIAL_TEST("用于二分变量的精确概率检验", Category.DATA_MINING, DatabaseType.ORACLE),
                STATS_CROSS_TAB("交叉表分析", Category.DATA_MINING, DatabaseType.ORACLE),
                STATS_F_TEST("测试两个方差是否显著不同", Category.DATA_MINING, DatabaseType.ORACLE),
                STATS_KS_TEST("比较两个样本来检验它们是否来自同一总体", Category.DATA_MINING, DatabaseType.ORACLE),
                STATS_MODE("出现频率最高的值", Category.DATA_MINING, DatabaseType.ORACLE),
                STATS_MW_TEST("检验两个总体分布函数相同的原假设与两个分布函数不同的替代假设", Category.DATA_MINING, DatabaseType.ORACLE),
                STATS_ONE_WAY_ANOVA("比较两个不同的方差估计值来检验均值", Category.DATA_MINING, DatabaseType.ORACLE),
                STATS_T_TEST_INDEP("两个独立组具有相同方差的t检验", Category.DATA_MINING, DatabaseType.ORACLE),
                STATS_T_TEST_INDEPU("两个方差不等的独立组的t检验", Category.DATA_MINING, DatabaseType.ORACLE),
                STATS_T_TEST_ONE("一个单样本t检验", Category.DATA_MINING, DatabaseType.ORACLE),
                STATS_T_TEST_PAIRED("一种两样本配对t检验", Category.DATA_MINING, DatabaseType.ORACLE),
                STATS_WSR_TEST("", Category.DATA_MINING, DatabaseType.ORACLE),
                STD("总体标准差", Category.AGGREGATE, null),
                STDDEV("总体标准差", Category.AGGREGATE, null),
                STDDEV_POP("总体标准差", Category.AGGREGATE, null),
                STDDEV_SAMP("样本标准差", Category.AGGREGATE, null),
                STR_TO_DATE("将字符串转换为日期", Category.DATE_TIME, null),
                STRCMP("比较两个字符串", Category.STRING, null),
                STRING_AGG("将非空输入值连接成字符串", Category.STRING, null),
                STRING_FORMAT("根据格式字符串格式化的输出", Category.STRING, null),
                STRING_POSITION("确定二个 TEXT 串在第一个 TEXT 串中第一次出现的位置", Category.STRING, null, "string,substring"),
                STRING_TO_BINARY("字符串转换为二进制字符串", Category.TYPE_CAST, DatabaseType.MySQL),
                STRING_TO_TABLE("在出现分隔符时拆分字符串，并将结果字段作为一组行返回", Category.STRING, DatabaseType.PostgreSQL),
                SUB_TIME("TIME1减去TIME2后的时间", Category.DATE_TIME, null),
                SUBSTR("从指定的位置开始，截取定长子串", Category.STRING, null, "string,start,length"),
                SUBSTR_BIT("从指定的位置开始，按字节截取子串", Category.STRING, null),
                SUBSTRING("SUBSTRING", Category.STRING, null),
                SUBSTRING_INDEX("从指定数量之前的字符串  子字符串分隔符的出现次数", Category.STRING, DatabaseType.MySQL, "string,delimiter,count"),
                SUM("合计", Category.AGGREGATE, null),
                SUPPRESS_REDUNDANT_UPDATES_TRIGGER("禁止执行不执行任何作的更新作", Category.SYSTEM, DatabaseType.PostgreSQL),
                SWITCH_WAL("强制服务器切换到新的预写日志文件", Category.REPLICATION, DatabaseType.PostgreSQL),
                SYNC_REPLICATION_SLOTS("将逻辑故障转移复制槽从主服务器同步到备用服务器", Category.REPLICATION, DatabaseType.PostgreSQL),
                SYS_CONNECT_BY_PATH("列值从根到节点的路径", Category.CATALOG, DatabaseType.ORACLE),
                SYS_CONTEXT("该函数用于获取上下文里属性的值", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_DATE("函数执行的时间", Category.DATE_TIME, null),
                SYS_DB_URI_GEN("生成特定列或行对象的数据类型URL", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_EXTRACT_UTC("从带有时区偏移或时区区域名称的日期时间值中提取UTC", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_GET_BLOCK_SIZE("存储块大小", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_GET_EXTRA_NAME_CHARACTERS("得到所有用于 unquoted SQL 标识符的特别字符，除了 a-z,A-Z,0-9,_", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_GET_HOME_DIR("获取数据库路径", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_GET_PAGE_SIZE("存储页大小", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_GET_PHY_DBNAME("获取数据库名", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_GET_REL_ID("从模式名和表名得到表的 oid", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_GET_SPC_HIT_RATE("计算 SPC 的命中率", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_GET_SQL_KEYWORDS("取得所有关键字，用逗号隔开", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_GET_USER_LOGIN_FAIL_COUNT("检查用户登路失败次数", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_GUID("全球唯一标识符", Category.UUID, DatabaseType.ORACLE),
                SYS_IS_CURRENT_USER_DBA("检查用户是不是 DBA", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_KILL_SESSION("kill一个会话", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_OP_ZONE_ID("将 rowid 作为其参数并返回区域 ID", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_RESET_ALL_SESSION_STAT("重置所有 session 统计信息", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_RESET_SESSION_STAT("重置指定 session 统计信息", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_RESET_SPC_STAT("重置计算 SPC 的命中率", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_TYPE_ID("最具体类型的类型", Category.SYSTEM, DatabaseType.ORACLE),
                SYS_XML_AGG("聚合 expr 表示的所有 XML 文档或片段，并生成单个 XML 文档", Category.XML, DatabaseType.ORACLE),
                SYS_XML_GEN("包含 XML 文档的类型的实例", Category.XML, DatabaseType.ORACLE),
                SYSTEM_USER("", Category.SYSTEM, null),
                TABLE_IS_VISIBLE("表格在搜索路径中可见", Category.CATALOG, DatabaseType.PostgreSQL),
                TABLE_SIZE("计算指定表使用的磁盘空间", Category.SYSTEM, null),
                TABLE_TO_XML("TABLE_TO_XML", Category.XML, DatabaseType.PostgreSQL),
                TABLE_TO_XML_AND_XML_SCHEMA("TABLE_TO_XML_AND_XMLSCHEMA", Category.XML, DatabaseType.PostgreSQL),
                TABLE_TO_XML_SCHEMA("TABLE_TO_XMLSCHEMA", Category.XML, DatabaseType.PostgreSQL),
                TABLESPACE_DATABASES("在指定表空间中存储对象的数据库的 OID 集", Category.SYSTEM, DatabaseType.PostgreSQL),
                TABLESPACE_LOCATION("此表空间所在的文件系统路径", Category.SYSTEM, DatabaseType.PostgreSQL),
                TABLESPACE_SIZE("计算具有指定名称或 OID 的表空间中使用的总磁盘空间", Category.SYSTEM, DatabaseType.PostgreSQL),
                TAN("切线", Category.MATH, null, "value"),
                TAND("切线 以度为单位的参数", Category.MATH, null),
                TANH("双曲正切", Category.MATH, null),
                TERMINATE_BACKEND("终止其后端进程具有指定进程 ID 的会话", Category.SYSTEM, DatabaseType.PostgreSQL),
                TEXT_LARGER("比较字符串，取相应位在字符表中靠后的", Category.STRING, null),
                TEXT_SMALLER("比较字符串，取相应位在字符表中靠前的", Category.STRING, null),
                TIME("时间部分", Category.DATE_TIME, null),
                TIME_DIFF("时间差", Category.DATE_TIME, null),
                TIME_FORMAT("时间格式化", Category.DATE_TIME, null),
                TIME_LARGER("最大时间", Category.DATE_TIME, null),
                TIME_OF_DAY("", Category.DATE_TIME, DatabaseType.PostgreSQL),
                TIME_OVERLAPS("前后两个时间区域是否有重叠", Category.DATE_TIME, null),
                TIME_SMALLER("最小时间", Category.DATE_TIME, null),
                TIME_TO_SEC("转换为秒的参数", Category.DATE_TIME, null),
                TIME_ZONE("数据库时区", Category.DATE_TIME, DatabaseType.ORACLE),
                TIME_ZONE_OFFSET("与参数对应的时区偏移量", Category.DATE_TIME, DatabaseType.ORACLE),
                TIMESTAMP("时间戳", Category.DATE_TIME, null),
                TIMESTAMP_ADD("添加时间", Category.DATE_TIME, null),
                TIMESTAMP_COMPARE("比较两个 TIMESTAMP 值", Category.DATE_TIME, null),
                TIMESTAMP_DIFF("时间差", Category.DATE_TIME, null),
                TIMESTAMP_LARGER("最大时间", Category.DATE_TIME, null),
                TIMESTAMP_SMALLER("最小时间", Category.DATE_TIME, null),
                TIMESTAMP_TO_SCN("与给定的时间戳相关联的近似系统更改号", Category.DATE_TIME, DatabaseType.ORACLE),
                TO_APPROX_COUNT_DISTINCT("输入包含近似不同值计数信息的详细信息，并将其转换为一个值", Category.TYPE_CAST, DatabaseType.ORACLE),
                TO_APPROX_PERCENTILE("将expr转换为双精度浮点数", Category.TYPE_CAST, DatabaseType.ORACLE),
                TO_BASE64("BASE64", Category.TYPE_CAST, null),
                TO_BINARY_DOUBLE("将给定的表达式转换为双精度浮点数", Category.TYPE_CAST, DatabaseType.ORACLE),
                TO_BINARY_FLOAT("将给定的表达式转换为浮点数", Category.TYPE_CAST, DatabaseType.ORACLE),
                TO_BLOB("值转换为 BLOB 值", Category.TYPE_CAST, DatabaseType.ORACLE),
                TO_CHAR("CHAR", Category.TYPE_CAST, null, "value,format"),
                TO_DATE("将一个文本类型的 TIMESTAMP 类型数据，转换成 DATE 类型", Category.TYPE_CAST, null, "string,format"),
                TO_DAY_SECOND_INTERVAL("换为 INTERVAL DAY TO SECOND 数据类型的值", Category.TYPE_CAST, DatabaseType.ORACLE),
                TO_DAYS("转换为天数", Category.TYPE_CAST, DatabaseType.MySQL),
                TO_DECIMAL("转换成 NUMERIC 类型", Category.TYPE_CAST, null),
                TO_HEX("16进制", Category.TYPE_CAST, null),
                TO_JSON("TO_JSON", Category.JSON, null),
                TO_JSONB("TO_JSONB", Category.JSON, DatabaseType.PostgreSQL),
                TO_LOB("将列给定的列中的 LONG 或 LONG RAW 值转换为 LOB 值", Category.TYPE_CAST, DatabaseType.ORACLE),
                TO_MULTI_BYTE("单字节字符转换为相应的多字节字符", Category.TYPE_CAST, DatabaseType.ORACLE),
                TO_NCHAR("转换为国家字符集", Category.TYPE_CAST, DatabaseType.ORACLE),
                TO_NCLOB("值转换为 NCLOB 值", Category.TYPE_CAST, DatabaseType.ORACLE),
                TO_NUMBER("转换成 NUMERIC 类型", Category.TYPE_CAST, null, "string,format"),
                TO_PATH("", Category.TYPE_CAST, null),
                TO_POLYGON("转换成多边形", Category.TYPE_CAST, null),
                TO_REG_CLASS("将文本关系名称转换为其 OID", Category.CATALOG, DatabaseType.PostgreSQL),
                TO_REG_COLLATION("将文本排序规则名称转换为其 OID", Category.CATALOG, DatabaseType.PostgreSQL),
                TO_REG_NAMESPACE("将文本架构名称转换为其 OID", Category.CATALOG, DatabaseType.PostgreSQL),
                TO_REG_OPER("将文本运算符名称转换为其 OID", Category.CATALOG, DatabaseType.PostgreSQL),
                TO_REG_OPERATOR("将文本运算符名称（带有参数类型）转换为其 OID", Category.CATALOG, DatabaseType.PostgreSQL),
                TO_REG_PROC("将文本函数或过程名称转换为其 OID", Category.CATALOG, DatabaseType.PostgreSQL),
                TO_REG_PROCEDURE("将文本函数或过程名称（带有参数类型）转换为其 OID", Category.CATALOG, DatabaseType.PostgreSQL),
                TO_REG_ROLE("将文本角色名称转换为其 OID", Category.CATALOG, DatabaseType.PostgreSQL),
                TO_REG_TYPE("解析文本字符串，从中提取潜在的类型名称，并将该名称转换为类型 OID", Category.CATALOG, DatabaseType.PostgreSQL),
                TO_REG_TYPE_MOD("解析文本字符串，从中提取潜在的类型名称，并翻译其类型修饰符", Category.CATALOG, DatabaseType.PostgreSQL),
                TO_SECONDS("转换为秒", Category.TYPE_CAST, DatabaseType.MySQL),
                TO_SINGLE_BYTE("多字节字符转换为相应的单字节字符", Category.TYPE_CAST, DatabaseType.ORACLE),
                TO_TIMESTAMP("", Category.TYPE_CAST, null, "string,format"),
                TO_TIMESTAMP_TZ("", Category.TYPE_CAST, null),
                TO_TS_QUERY("根据指定或默认配置规范化单词", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TO_TS_VECTOR("将 JSON 文档中的每个字符串值转换为 ，根据指定或默认配置对单词进行规范化", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TO_YEAR_MONTH_INTERVAL("转换为 INTERVAL MONTH TO YEAR 数据类型的值", Category.TYPE_CAST, DatabaseType.ORACLE),
                TOTAL_RELATION_SIZE("计算指定表使用的总磁盘空间", Category.SYSTEM, DatabaseType.PostgreSQL),
                TRANSACTION_TIMESTAMP("当前事务开始时间", Category.DATE_TIME, null),
                TRANSLATE("将字符串中的指定子串转换成新的子串并返回", Category.STRING, null),
                TREAT("更改表达式的声明类型", Category.CATALOG, DatabaseType.ORACLE),
                TRIGGER_DEPTH("触发器的当前嵌套级别", Category.CATALOG, DatabaseType.ORACLE),
                TRIM("删除前导和尾随空格", Category.STRING, null),
                TRIM_ARRAY("通过删除最后 N 个元素来修剪数组", Category.ARRAY, DatabaseType.PostgreSQL),
                TRIM_SCALE("删除尾随零", Category.MATH, null),
                TRUNC("截断到指定的小数位数", Category.MATH, null, "value,precision"),
                TRUNCATE("截断到指定的小数位数", Category.MATH, null),
                TRY_ADVISORY_LOCK("获得独占的会话级咨询锁", Category.LOCK, DatabaseType.PostgreSQL),
                TRY_ADVISORY_LOCK_SHARED("共享会话级的咨询锁", Category.LOCK, DatabaseType.PostgreSQL),
                TRY_ADVISORY_XACT_LOCK("获得专属的交易级咨询锁", Category.LOCK, DatabaseType.PostgreSQL),
                TRY_ADVISORY_XACT_LOCK_SHARED("获取共享事务级咨询锁", Category.LOCK, DatabaseType.PostgreSQL),
                TS_CONFIG_IS_VISIBLE("文本搜索配置在搜索路径中是否可见", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_DEBUG("根据指定或默认的文本搜索配置从文档中提取和规范化标记，并返回每个标记的处理方式", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_DELETE("从向量中删除给定词位的任何出现", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_DICT_IS_VISIBLE("文本搜索词典在搜索路径中是否可见", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_FILTER("仅从向量中选择具有给定权重的元素", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_HEADLINE("以缩写形式显示文档中查询的匹配项", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_LEXIZE("如果输入词典已知，返回替换词汇数组;若词典已知但为停止字，则返回空数组;若不是已知词", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_PARSE("使用命名解析器从文档中提取词元", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_PARSER_IS_VISIBLE("文本搜索解析器在搜索路径中是否可见", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_QUERY_PHRASE("构造一个短语查询，用于在连续的词位上搜索", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_RANK("计算一个分数，显示向量与查询的匹配程度", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_RANK_CD("使用覆盖密度算法计算一个分数，显示向量与查询的匹配程度", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_REWRITE("根据执行命令获得的目标替换部分查询和替代", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_STAT("数据中每个不同词汇的统计数据", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_TEMPLATE_IS_VISIBLE("文本搜索模板在搜索路径中是否可见", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_TOKEN_TYPE("描述命名解析器能识别的每种类型的令牌", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_VECTOR_TO_ARRAY("转换为词位数组", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_VECTOR_UPDATE_TRIGGER("自动更新关联的纯文本文档列中的列", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TS_VECTOR_UPDATE_TRIGGER_COLUMN("自动更新关联的纯文本文档列中的列", Category.FULLTEXT, DatabaseType.PostgreSQL),
                TYPE_IS_VISIBLE("类型（或域）在搜索路径中是否可见", Category.CATALOG, DatabaseType.PostgreSQL),
                TYPEOF("传递给它的值的数据类型的 OID", Category.CATALOG, DatabaseType.PostgreSQL),
                UMINUS("取反", Category.MATH, null),
                UN_COMPRESS("解压缩压缩的字符串", Category.COMPRESS, DatabaseType.MySQL),
                UN_HEX("包含数字十六进制表示的字符串", Category.TYPE_CAST, DatabaseType.MySQL),
                UNCOMPRESSED_LENGTH("压缩前字符串的长度", Category.COMPRESS, DatabaseType.MySQL),
                UNI_STR("以国家字符集返回给定的字符数据", Category.STRING, DatabaseType.ORACLE),
                UNICODE_ASSIGNED("是否为字符串中的所有字符分配了 Unicode 代码点", Category.ENCODING, null),
                UNICODE_STRING("计算参数中转义的 Unicode 字符", Category.STRING, null),
                UNICODE_VERSION("使用的 UNICODE 版本", Category.ENCODING, DatabaseType.PostgreSQL),
                UNIX_TIMESTAMP("UNIX 时间戳", Category.DATE_TIME, null),
                UNNEST("将 a 扩展为一组行，每个词位一个", Category.ARRAY, null),
                UPDATE_XML("替换XML片段", Category.XML, null),
                UPPER("大写", Category.STRING, null, "string"),
                UPPER_INC("该范围的上限是否包含", Category.RANGE, DatabaseType.PostgreSQL),
                UPPER_INF("范围是否有上限", Category.RANGE, DatabaseType.PostgreSQL),
                UPPER_LIMIT("取字符串与其最近的上限", Category.STRING, null),
                USER("客户端提供的用户名和主机名", Category.SYSTEM, null),
                USER_ENV("当前会话的信息", Category.SYSTEM, DatabaseType.ORACLE),
                UTC_DATE("当前 UTC 日期", Category.DATE_TIME, null),
                UTC_TIME("当前 UTC 时间", Category.DATE_TIME, null),
                UTC_TIMESTAMP("当前 UTC 时间戳", Category.DATE_TIME, null),
                UUID("UUID", Category.UUID, null),
                UUID_EXTRACT_TIMESTAMP("提取时间戳", Category.UUID, DatabaseType.PostgreSQL),
                UUID_EXTRACT_VERSION("UUID 中提取版本", Category.UUID, DatabaseType.PostgreSQL),
                UUID_SHORT("整数值通用标识符", Category.UUID, DatabaseType.MySQL),
                UUID_TO_BIN("将字符串 UUID 转换为二进制", Category.UUID, DatabaseType.MySQL),
                VALIDATE_CONVERSION("是否可以将给定的表达式转换为给定的数据类型", Category.TYPE_CAST, null),
                VALIDATE_PASSWORD_STRENGTH("确定密码的强度", Category.SYSTEM, DatabaseType.MySQL),
                VAR_POP("总体标准方差", Category.AGGREGATE, null),
                VAR_SAMP("样本方差", Category.AGGREGATE, null),
                VARIANCE("总体标准方差", Category.AGGREGATE, null),
                VERSION("服务器版本", Category.SYSTEM, null),
                VISIBLE_IN_SNAPSHOT("事务 ID 是否可见", Category.REPLICATION, DatabaseType.PostgreSQL),
                VSIZE("内部表示中的字节数", Category.SYSTEM, DatabaseType.ORACLE),
                WAIT_FOR_EXECUTED_GTID_SET("等待给定的 GTID 在副本上执行", Category.REPLICATION, DatabaseType.MySQL),
                WAL_FILE_NAME("将预写日志位置转换为保存该位置的WAL文件的名", Category.REPLICATION, DatabaseType.PostgreSQL),
                WAL_FILE_NAME_OFFSET("将预写日志位置转换为WAL文件名和该文件中的字节偏移量", Category.REPLICATION, DatabaseType.PostgreSQL),
                WAL_LSN_DIFF("计算两个预写日志位置之间的字节差", Category.REPLICATION, DatabaseType.PostgreSQL),
                WAL_REPLAY_PAUSE("请求暂停恢复", Category.REPLICATION, DatabaseType.PostgreSQL),
                WAL_REPLAY_RESUME("重新启动恢复", Category.REPLICATION, DatabaseType.PostgreSQL),
                WAL_SUMMARY_CONTENTS("有关由TLI标识的单个WAL摘要文件的内容以及开始和结束LSN的信息", Category.REPLICATION, DatabaseType.PostgreSQL),
                WEBSEARCH_TO_TSQUERY("将文本转换为 ，根据指定或默认配置规范化单词", Category.FULLTEXT, DatabaseType.PostgreSQL),
                WEEK("周数", Category.DATE_TIME, null),
                WEEK_OF_YEAR("日期的日历周", Category.DATE_TIME, null),
                WEEKDAY("工作日索引", Category.DATE_TIME, null),
                WEIGHT_STRING("字符串的权重字符串", Category.STRING, DatabaseType.MySQL),
                WIDTH_BUCKET("直方图中的桶数或编号", Category.MATH, null),
                XACT_COMMIT_TIMESTAMP("事务的提交时间戳", Category.REPLICATION, DatabaseType.PostgreSQL),
                XACT_COMMIT_TIMESTAMP_ORIGIN("事务的提交时间戳和复制源", Category.REPLICATION, DatabaseType.PostgreSQL),
                XACT_STATUS("最近事务的提交状态", Category.REPLICATION, DatabaseType.PostgreSQL),
                XML_AGG("XML聚合", Category.XML, null),
                XML_CAST("转换为 datatype 指定的标量 SQL 数据类型", Category.XML, null),
                XML_CDATA("生成 CDATA", Category.XML, null),
                XML_COL_ATT_VAL("创建一个XML片段", Category.XML, null),
                XML_COMMENT("XML注释", Category.XML, null),
                XML_CONCAT("XML值串接", Category.XML, null),
                XML_DIFF("符合 Xdiff 模式的 XML 中的差异", Category.XML, null),
                XML_ELEMENT("XML元素", Category.XML, null),
                XML_EXISTS("", Category.XML, null),
                XML_FOREST("XML林", Category.XML, null),
                XML_IS_VALID("是否符合相关的XML模式", Category.XML, null),
                XML_IS_WELL_FORMED("", Category.XML, null),
                XML_IS_WELL_FORMED_CONTENT("", Category.XML, null),
                XML_IS_WELL_FORMED_DOCUMENT("", Category.XML, null),
                XML_PARSE("解析并从评估结果生成 XML 实例", Category.XML, null),
                XML_PATCH("更改修补 XML 文档", Category.XML, null),
                XML_PI("创建 XML 处理指令", Category.XML, null),
                XML_QUERY("计算 XQuery 表达式的结果", Category.XML, null),
                XML_ROOT("XML 值的根节点", Category.XML, null),
                XML_SEQUENCE("最高层节点的 varray", Category.XML, DatabaseType.ORACLE),
                XML_SERIALIZE("生成包含value_expr内容的字符串或LOB", Category.XML, null),
                XML_TABLE("基于XML值生成一个表", Category.XML, null),
                XML_TO_TEXT("XML文本", Category.XML, null),
                XML_TRANSFORM("", Category.XML, null),
                XOR("逻辑异或", Category.BIT_OP, null),
                XPATH("根据 XML 值 xml 计算 XPath", Category.XML, null),
                XPATH_EXISTS("", Category.XML, null),
                XY_TO_POINT("从坐标构造点", Category.SPATIAL, null),
                YEAR("年份", Category.DATE_TIME, null),
                YEAR_WEEK("年份和周", Category.DATE_TIME, null),
                ;

                META(String title, Category category, DatabaseType database) {
                        this.title = title;
                        this.category = category;
                        this.database = database;
                        this.synonym = null;
                        this.params = null;
                }

                META(String title, Category category, DatabaseType database, META synonym) {
                        this.title = title;
                        this.category = category;
                        this.database = database;
                        this.synonym = synonym;
                        this.params = null;
                }

                META(String title, Category category, DatabaseType database, String params) {
                        this.title = title;
                        this.category = category;
                        this.database = database;
                        this.synonym = null;
                        this.params = params;
                }

                private final String title;
                private final Category category;
                private final DatabaseType database;
                private final META synonym;
                private final String params;

                public String title() {
                        return title;
                }

                /**
                 * 函数功能类别
                 * @return FunctionCategory
                 */
                public Category category() {
                        return category;
                }

                /**
                 * 数据库独有标记
                 * @return null=需跨数据库兼容映射; DatabaseType.PostgreSQL/DatabaseType.ORACLE/DatabaseType.MySQL=该数据库独有无需兼容
                 */
                public DatabaseType database() {
                        return database;
                }

                /**
                 * 获取该函数可用的参数名称列表（逗号分隔）
                 * @return 参数名称列表字符串
                 */
                public String params() {
                        return params;
                }

                /**
                 * 检查参数名称是否在该函数允许的参数列表中
                 * @param paramName 参数名称
                 * @return 是否允许使用该参数
                 */
                public boolean isValidParam(String paramName) {
                        if (params == null || paramName == null) {
                                return false;
                        }
                        String[] allowedParams = params.split(",");
                        for (String param : allowedParams) {
                                if (param.trim().equals(paramName)) {
                                        return true;
                                }
                        }
                        return false;
                }

        }

        /**
         * 数据库类型
         * @return DatabaseType
         */
        DatabaseType database();

        /**
         * 参数,注意参数有可能是个函数
         * @return list
         */
        List<String> params();
        void params(List<String> params);

        /**
         * 获取本数据库中函数的参数顺序（语义名称列表）
         * <p>
         * 例如：SUBSTRING(str, pos, len) 返回 ["string", "position", "length"]
         * <p>
         * 各数据库实现可覆盖此方法以指定本数据库的参数顺序
         * <p>
         * 默认实现会从 formulaDefine() 中自动提取参数顺序
         * @return 参数名称列表，如果返回null则表示参数顺序与标准顺序一致无需重排
         */
        default List<String> paramOrder(){
                // 从 formulaDefine() 中自动提取参数顺序
                String formula = formulaDefine();
                if (formula != null && formula.contains("${")) {
                        return extractPlaceholders(formula);
                }
                return null;
        };

        /**
         * 根据目标数据库的参数顺序重新排序参数
         * <p>
         * 将源顺序的参数列表转换为目标数据库需要的顺序
         * @param sourceOrder 源数据库的参数顺序定义（语义名称列表）
         * @param params 要重排的参数列表（按源数据库顺序排列）
         * @return 重排后的参数列表（按本数据库顺序排列）
         */
        default List<String> reorderParams(List<String> sourceOrder, List<String> params){
                if (null == sourceOrder || null == params || params.isEmpty()) {
                        return params;
                }
                List<String> targetOrder = this.paramOrder();
                if (null == targetOrder || targetOrder.isEmpty()) {
                        return params;
                }
                // 如果顺序相同，直接返回
                if (sourceOrder.equals(targetOrder)) {
                        return params;
                }
                // 构建参数索引映射
                java.util.Map<String, String> paramMap = new java.util.LinkedHashMap<>();
                for (int i = 0; i < sourceOrder.size() && i < params.size(); i++) {
                        paramMap.put(sourceOrder.get(i), params.get(i));
                }
                // 按目标顺序重新排列参数
                List<String> result = new java.util.ArrayList<>();
                for (String key : targetOrder) {
                        String param = paramMap.get(key);
                        if (param != null) {
                                result.add(param);
                        }
                }
                // 添加未映射的额外参数
                for (int i = sourceOrder.size(); i < params.size(); i++) {
                        result.add(params.get(i));
                }
                return result;
        };

        /**
         * 转换后格式 包含参数
         * <p>
         * 如果 formulaDefine 包含 ${paramName} 占位符，则替换为实际参数值
         * 例如：POSITION(${substring} IN ${string}) 替换为 POSITION('B' IN 'ABC')
         * 占位符名称按出现顺序自动提取，无需单独定义 paramOrder
         * <p>
         * 只有在 META.params() 中定义的参数名称才能使用
         * <p>
         * 如果 formulaDefine 返回 null，则使用默认格式 define(param1, param2, ...)
         * @return 转换后格式
         */
        default String formula() {
                List<String> params = params();
                
                // 如果 formulaDefine 包含 ${} 占位符，进行参数替换
                String formulaDef = formulaDefine();
                if (formulaDef != null && formulaDef.contains("${")) {
                        String result = formulaDef;
                        if (null != params) {
                                // 从 formulaDefine 中提取所有占位符名称（按出现顺序）
                                java.util.List<String> placeholders = extractPlaceholders(formulaDef);
                                // 验证参数名称是否在 META.params() 中定义
                                META meta = meta();
                                for (String placeholder : placeholders) {
                                        if (!meta.isValidParam(placeholder)) {
                                                // 参数名称未在 META 中定义，使用默认格式
                                                return defaultFormula(params);
                                        }
                                }
                                for (int i = 0; i < placeholders.size() && i < params.size(); i++) {
                                        String placeholder = "${" + placeholders.get(i) + "}";
                                        result = result.replace(placeholder, params.get(i));
                                }
                        }
                        return result;
                }
                
                // 默认格式：define(param1, param2, ...)
                return defaultFormula(params);
        }
        
        /**
         * 默认格式：define(param1, param2, ...)
         * @param params 参数列表
         * @return 格式化字符串
         */
        default String defaultFormula(List<String> params) {
                StringBuilder formula = new StringBuilder();
                formula.append(define()).append("(");
                if(null != params) {
                        boolean first = true;
                        for(String param : params) {
                                if(!first) {
                                        formula.append(",");
                                }
                                first = false;
                                formula.append(param);
                        }
                }
                formula.append(")");
                return formula.toString();
        }
        
        /**
         * 从 define 中提取所有 ${paramName} 占位符名称（按出现顺序）
         * @param define 函数定义字符串
         * @return 占位符名称列表
         */
        static java.util.List<String> extractPlaceholders(String define) {
                java.util.List<String> placeholders = new java.util.ArrayList<>();
                if (define == null) {
                        return placeholders;
                }
                // 匹配 ${paramName} 格式的占位符
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{(\\w+)\\}");
                java.util.regex.Matcher matcher = pattern.matcher(define);
                while (matcher.find()) {
                        placeholders.add(matcher.group(1));
                }
                return placeholders;
        }

        META meta();

        /**
         * 函数名 不包含参数
         * @return String
         */
        String define();
        
        /**
         * 带参数占位符的完整格式
         * <p>
         * 例如：POSITION(${substring} IN ${string})
         * 如果返回 null，则使用默认格式 define(param1, param2, ...)
         * @return 带占位符的格式字符串
         */
        default String formulaDefine() {
                return null;
        }

}