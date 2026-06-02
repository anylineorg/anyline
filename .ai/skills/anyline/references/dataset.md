# DataSet 操作

## 概述
DataSet是Anyline的核心数据结果集对象，继承自`ArrayList<E>`（E通常为DataRow），提供丰富的数据处理、聚合、分组等能力。

## 1 基本操作

### 1.1 创建与初始化

```java
// 空创建
DataSet<DataRow> set = new DataSet<>();

// 从List<Map>创建
List<Map<String, Object>> list = new ArrayList<>();
DataSet<DataRow> set = new DataSet<>(list);

// 从JSON创建
String json = "[{\"ID\":1,\"NAME\":\"张三\"},{\"ID\":2,\"NAME\":\"李四\"}]";
DataSet<DataRow> set = DataSet.parseJson(json);

// 从Collection创建
List<DataRow> rows = new ArrayList<>();
DataSet<DataRow> set = DataSet.parse(rows);
```

### 1.2 添加数据

```java
// 添加单条
set.add(row);

// 添加多条
set.addAll(rows);

// 批量添加
set.add(list);
```

### 1.3 遍历与处理

```java
// 遍历处理
for (DataRow row : set) {
    String name = row.getString("NAME");
    // 处理逻辑
}

// 流式处理
set.stream()
    .filter(row -> row.getInt("AGE") > 18)
    .forEach(row -> System.out.println(row.getString("NAME")));

// 获取指定行
DataRow first = set.getRow(0);
DataRow last = set.getRow(set.size() - 1);
```

## 2 数据转换

### 2.1 Key大小写转换

```java
// 下划线转大驼峰 USER_NAME -> UserName
set.Camel();

// 驼峰转下划线 UserName -> user_name
set.camel_();

// 下划线转小驼峰 USER_NAME -> userName
set.camel();
set.camel(false); // false:不处理全大写无下划线的情况
```

### 2.2 字符串处理

```java
// 去除首尾空格（所有String类型）
set.trim();
set.trim("NAME", "DESCRIPTION"); // 指定字段

// 多个空白压缩成一个空格
set.compress();
set.compress("CONTENT"); // 指定字段

// 全角转半角
set.sbc2dbc();
set.sbc2dbc("REMARK"); // 指定字段
```

## 3 数据过滤与筛选

### 3.1 移除字段

```java
// 移除指定字段
set.remove("PASSWORD", "REMARK");
```

### 3.2 设置主键

```java
// 设置主键（应用到所有DataRow）
set.addPrimaryKey("ID");
set.addPrimaryKey("ID", "CODE");

// 设置过滤键
set.addFilterKey("PASSWORD");
```

## 4 元数据管理

```java
// 设置元数据
set.setMetadata("ID", column);
set.setMetadata(columns);

// 获取元数据
Column col = set.getMetadata("ID");
LinkedHashMap<String, Column> metadatas = set.getMetadatas();

// 获取类型信息
String typeName = set.getMetadataTypeName("ID");
String className = set.getMetadataClassName("ID");
String fullType = set.getMetadataFullType("ID");
```

## 5 属性与配置

```java
// 设置属性
set.setAttribute("key", value);

// 获取属性
Object value = set.getAttribute("key");
DataRow attrs = set.getAttributes();

// 设置覆盖模式（用于更新操作）
set.setOverride(true);
set.setOverride(true, true); // override, sync

// 设置Unicode支持
set.setUnicode(true);

// 设置是否为新数据（用于新增操作）
set.setIsNew(true);

// 设置原始数据
set.putOrigin("key", value);
Object origin = set.getOrigin("key");
```

## 6 索引管理

```java
// 创建索引（便于快速查找）
set.createIndex("ID");
set.createIndex("CODE");
```

## 7 分页信息

```java
// 设置分页信息
set.setPageSize(10);
set.setPageIndex(1);
set.setTotal(100);
set.setTotalPage(10);

// 获取分页信息
int pageSize = set.getPageSize();
int pageIndex = set.getPageIndex();
long total = set.getTotal();
int totalPage = set.getTotalPage();
```

## 8 数据导出

```java
// 导出为JSON字符串
String json = set.toJSON();

// 导出为List<Map>
List<Map<String, Object>> list = set.toList();

// 导出为XML
String xml = set.toXML();
```

## 9 数据聚合

### 9.1 基础聚合方法

DataSet内置了丰富的聚合方法：

```java
// 计数
int rowCount = set.size();
int total = set.count(false, "ID");

// 求和
BigDecimal totalAmount = set.sum("AMOUNT");
BigDecimal sumRange = set.sum(0, 10, "AMOUNT"); // 前10行求和

// 多列求和
DataRow sums = set.sums("AMOUNT", "QUANTITY", "PRICE");
DataRow sums = set.sums(resultRow, "AMOUNT", "QUANTITY");

// 平均值
BigDecimal avg = set.avg("SCORE");
DataRow avgs = set.avgs("SCORE", "AGE");

// 带精度和舍入模式的平均值
DataRow avgs = set.avgs(false, 2, BigDecimal.ROUND_HALF_UP, "SCORE");

// 最大值
BigDecimal max = set.maxDecimal("AMOUNT");
int maxInt = set.maxInt("AGE");
double maxDouble = set.maxDouble("PRICE");
long maxLong = set.maxLong("COUNT");
double maxFloat = set.maxFloat("RATE");

// 最小值
BigDecimal min = set.minDecimal("AMOUNT");
int minInt = set.minInt("AGE");
double minDouble = set.minDouble("PRICE");
double minFloat = set.minFloat("RATE");

// 中位数
BigDecimal median = set.median("SCORE");

// 标准差
BigDecimal stdev = set.stdev(2, BigDecimal.ROUND_HALF_UP, "SCORE");
BigDecimal stdevp = set.stdevp(2, BigDecimal.ROUND_HALF_UP, "SCORE");
BigDecimal stdeva = set.stdeva(2, BigDecimal.ROUND_HALF_UP, "SCORE");
BigDecimal stdevpa = set.stdevpa(2, BigDecimal.ROUND_HALF_UP, "SCORE");

// 方差
BigDecimal var = set.var(2, BigDecimal.ROUND_HALF_UP, "SCORE");
BigDecimal vara = set.vara(2, BigDecimal.ROUND_HALF_UP, "SCORE");
BigDecimal varp = set.varp(2, BigDecimal.ROUND_HALF_UP, "SCORE");
BigDecimal varpa = set.varpa(2, BigDecimal.ROUND_HALF_UP, "SCORE");
```

### 9.2 通用聚合方法 agg()

通过`Aggregation`枚举实现统一的聚合调用：

```java
import org.anyline.entity.Aggregation;

// 求和
Object sum = set.agg(Aggregation.SUM, "AMOUNT");

// 带精度的聚合
Object avg = set.agg(Aggregation.AVG, 2, BigDecimal.ROUND_HALF_UP, "SCORE");

// 支持的聚合类型
Aggregation.COUNT    // 计数
Aggregation.SUM      // 求和
Aggregation.AVG      // 平均值
Aggregation.AVGA     // 平均值(含空值)
Aggregation.MEDIAN   // 中位数
Aggregation.MAX      // 最大值
Aggregation.MAX_DECIMAL
Aggregation.MAX_DOUBLE
Aggregation.MAX_FLOAT
Aggregation.MAX_INT
Aggregation.MIN      // 最小值
Aggregation.MIN_DECIMAL
Aggregation.MIN_DOUBLE
Aggregation.MIN_FLOAT
Aggregation.MIN_INT
Aggregation.STDEV    // 标准差
Aggregation.STDEVP   // 总体标准差
Aggregation.STDEVA   // 标准差(含逻辑值)
Aggregation.STDEVPA  // 总体标准差(含逻辑值)
Aggregation.VAR      // 方差
Aggregation.VARA     // 方差(含逻辑值)
Aggregation.VARP     // 总体方差
Aggregation.VARPA    // 总体方差(含逻辑值)
```

## 10 数据分组

### 10.1 基础分组

```java
// 按字段分组，保留条目数据（默认）
DataSet<DataRow> groups = set.group("DEPT");
DataSet<DataRow> groups = set.group("DEPT", "SEX"); // 多字段分组

// 分组但不保留条目数据
DataSet<DataRow> groups = set.group(false, "DEPT");

// 指定比较方式的分组
DataSet<DataRow> groups = set.group("items_field", Compare.EQUAL, "DEPT");
```

### 10.2 分组聚合

```java
import org.anyline.entity.Aggregation;

// 分组后聚合：按部门分组，计算每个部门的平均工资
DataSet<DataRow> result = set.group("SALARY", Aggregation.AVG, "DEPT");
// 结果格式：[{"DEPT":"研发部", "SALARY_AVG":15000}, ...]

// 自定义聚合结果字段名
DataSet<DataRow> result = set.group(
    "items",           // 条目字段名（为空则不保留）
    "avg_sal",         // 聚合结果别名
    "SALARY",          // 计算字段
    Aggregation.AVG,   // 聚合类型
    2,                 // 精度（小数位）
    BigDecimal.ROUND_HALF_UP, // 舍入模式
    "DEPT"             // 分组字段
);

// 指定提取模式（是否只保留分组键）
DataSet<DataRow> result = set.group(false, "SALARY", Aggregation.SUM, "DEPT");
```

### 10.3 多聚合规则

```java
import org.anyline.entity.AggregationConfig;

// 创建多个聚合配置
List<AggregationConfig> aggs = new ArrayList<>();
aggs.add(new AggregationConfig("SALARY", Aggregation.SUM, 2, BigDecimal.ROUND_HALF_UP));
aggs.add(new AggregationConfig("SALARY", Aggregation.AVG, 2, BigDecimal.ROUND_HALF_UP));
aggs.add(new AggregationConfig("BONUS", Aggregation.MAX, 0, BigDecimal.ROUND_HALF_UP));

// 按部门分组，执行多个聚合
DataSet<DataRow> result = set.group("items", aggs, "DEPT");
// 结果格式：[{"DEPT":"研发部", "SALARY_SUM":xxx, "SALARY_AVG":xxx, "BONUS_MAX":xxx}, ...]
```

### 10.4 嵌套聚合

对DataRow中的嵌套DataSet进行聚合：

```java
// 假设有订单数据，每个订单包含多个商品(items)
DataSet<DataRow> orders = service.selects("ORDERS");

// 计算每个订单的商品总金额
orders.agg(Aggregation.SUM, "TOTAL_AMOUNT", "items", 2, BigDecimal.ROUND_HALF_UP, "PRICE");

// 带条件的嵌套聚合（只计算状态为1的商品）
orders.agg(Aggregation.SUM, "TOTAL_AMOUNT", "items", 2, BigDecimal.ROUND_HALF_UP, 
           "PRICE", Compare.GREATER, "STATUS:1");
```

## 11 Select 内部类（数据筛选）

DataSet内置`Select`内部类，提供便捷的数据筛选功能，支持索引加速（大数据集自动建索引）。

### 11.1 配置选项

```java
// 获取Select实例
DataSet.Select select = set.select;

// 设置筛选选项
select.setIgnoreCase(true);  // 是否忽略大小写（默认true）
select.setIgnoreNull(true);   // 是否忽略NULL（默认true）
```

### 11.2 等值筛选

```java
// 筛选 key=value 的子集
DataSet<DataRow> filtered = select.equals("DEPT", "研发部");

// 筛选 key!=value 的子集
DataSet<DataRow> filtered = select.notEquals("STATUS", 0);
```

### 11.3 范围比较

```java
// 小于
DataSet<DataRow> filtered = select.less("AGE", 18);

// 小于等于
DataSet<DataRow> filtered = select.lessEqual("AGE", 30);

// 大于
DataSet<DataRow> filtered = select.greater("SALARY", 5000);

// 大于等于
DataSet<DataRow> filtered = select.greaterEqual("SALARY", 10000);

// 区间筛选（大于等于min且小于等于max）
DataSet<DataRow> filtered = select.between("AGE", 18, 35);
```

### 11.4 模糊匹配

```java
// SQL LIKE 匹配（%表示任意字符，_表示单个字符）
DataSet<DataRow> filtered = select.like("NAME", "%张%");

// NOT LIKE 匹配
DataSet<DataRow> filtered = select.notLike("NAME", "%测试%");

// 前缀匹配
DataSet<DataRow> filtered = select.startWith("CODE", "DEPT_");

// 后缀匹配
DataSet<DataRow> filtered = select.endWith("EMAIL", "@example.com");

// 包含匹配
DataSet<DataRow> filtered = select.contains("NAME", "张");
```

### 11.5 集合匹配

```java
// IN 筛选
DataSet<DataRow> filtered = select.in("STATUS", 1, 2, 3);
DataSet<DataRow> filtered = select.in("DEPT", Arrays.asList("研发部", "市场部"));

// NOT IN 筛选
DataSet<DataRow> filtered = select.notIn("STATUS", 0, -1);
```

### 11.6 NULL 判断

```java
// 筛选指定列为NULL的行
DataSet<DataRow> filtered = select.isNull("EMAIL", "PHONE");

// 筛选指定列不为NULL的行
DataSet<DataRow> filtered = select.isNotNull("EMAIL", "PHONE");
DataSet<DataRow> filtered = select.notNull("EMAIL", "PHONE"); // 同上
```

### 11.7 空值判断

```java
// 筛选指定列为空的行（空字符串或NULL）
DataSet<DataRow> filtered = select.empty("REMARK");

// 筛选指定列不为空的行
DataSet<DataRow> filtered = select.notEmpty("NAME", "AGE");
```

### 11.8 Set包含判断

```java
// values中有一个存在于item中即可（OR逻辑）
DataSet<DataRow> filtered = select.findInSetOr("TAGS", "Java", "Python");
DataSet<DataRow> filtered = select.findInSet("TAGS", "Java", "Python"); // 等同于findInSetOr

// values中每一个都存在于item中才返回（AND逻辑）
DataSet<DataRow> filtered = select.findInSetAnd("TAGS", "Java", "Spring");
```

### 11.9 OGNL表达式筛选

```java
// 使用OGNL表达式进行复杂筛选
DataSet<DataRow> filtered = select.ognl("AGE > 18 && SALARY > 5000");
```

### 11.10 通用过滤器

```java
// 使用Compare枚举进行筛选
DataSet<DataRow> filtered = select.filter(Compare.GREATER, "ID", 100);
DataSet<DataRow> filtered = select.filter(Compare.EQUAL, "STATUS", 1);
```

## 12 Format 内部类（数据格式化）

DataSet内置`Format`内部类，提供日期和数字的格式化功能。

### 12.1 日期格式化

```java
// 获取Format实例
DataSet.Format format = set.format;

// 根据列名格式化
format.date("yyyy-MM-dd", "CREATE_TIME", "UPDATE_TIME");

// 根据数据类型格式化
format.date("yyyy-MM-dd HH:mm:ss", Date.class, Timestamp.class);

// 格式化所有日期类型列（列名或类型中包含date关键字）
format.date(true, "yyyy-MM-dd");
format.date(true, "yyyy-MM-dd", "1970-01-01"); // 带默认值

// 日期格式转换（源列->目标列，失败时使用默认值）
format.date("yyyy-MM-dd", "CREATE_TIME:CREATE_DATE:1970-01-01");
```

### 12.2 数字格式化

```java
// 根据列名格式化
format.number("##.00", "AMOUNT", "PRICE");

// 根据数据类型格式化
format.number("#,##0.00", Double.class, BigDecimal.class);

// 格式化所有数字类型列
format.number(true, "#,##0.00");
format.number(true, "#,##0.00", "0.00"); // 带默认值
```

## 13 Parse 内部类（数据解析）

DataSet内置`Parse`内部类，提供字符串到日期/数字类型的解析功能。

```java
// 获取Parse实例
DataSet.Parse parse = set.parse;

// 日期解析（字符串转日期类型）
parse.date("CREATE_TIME", "UPDATE_TIME");

// 数字解析（字符串转数字类型）
parse.number("AMOUNT", "PRICE");

// 链式调用
set.parse.date("CREATE_TIME").number("AMOUNT");
```

## 14 数据排序

```java
// 按字段排序（使用Comparator）
set.sort((r1, r2) -> r1.getInt("AGE") - r2.getInt("AGE"));

// 多字段排序
set.sort(Comparator
    .comparingInt(row -> row.getInt("DEPT_ID"))
    .thenComparing(row -> row.getString("NAME")));

// 降序排序
set.sort(Comparator.comparingInt(row -> row.getInt("SCORE")).reversed());
```

## 15 舍入模式说明

聚合方法中使用的舍入模式参考BigDecimal静态常量：

| 常量 | 值 | 说明 |
|------|---|------|
| ROUND_UP | 0 | 舍入远离零，始终进一位 |
| ROUND_DOWN | 1 | 接近零，截断不进位 |
| ROUND_CEILING | 2 | 接近正无穷大 |
| ROUND_FLOOR | 3 | 接近负无穷大 |
| ROUND_HALF_UP | 4 | 四舍五入 |
| ROUND_HALF_DOWN | 5 | 五舍六入 |
| ROUND_HALF_EVEN | 6 | 银行家舍入法（四舍六入五留双） |
| ROUND_UNNECESSARY | 7 | 断言结果精确，否则抛异常 |
