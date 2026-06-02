# DataRow 操作

## 概述
DataRow是Anyline的核心数据行对象，继承自`LinkedHashMap<String, Object>`，提供丰富的数据操作能力和类型转换功能。

## 1 数据读取

### 1.1 基本类型读取

```java
// 字符串读取
String name = row.getString("NAME");

// 整数读取
int age = row.getInt("AGE");
long id = row.getLong("ID");

// 浮点数读取
double price = row.getDouble("PRICE");
float rate = row.getFloat("RATE");

// 日期读取
Date createTime = row.getDate("CREATE_TIME");
```

### 1.2 带默认值的读取

```java
// 带默认值，避免NullPointerException
String name = row.getString("NAME", "未知");
int age = row.getInt("AGE", 0);
long count = row.getLong("COUNT", 0L);
double price = row.getDouble("PRICE", 0.0);
```

## 2 数据写入

```java
// 基本写入
row.put("ID", 1);
row.put("NAME", "张三");
row.put("AGE", 25);
row.put("CREATE_TIME", new Date());

// 链式调用
row.put("ID", 1)
   .put("NAME", "张三")
   .put("AGE", 25);
```

## 3 数据类型转换

### 3.1 Key大小写转换

```java
// 下划线转大驼峰 USER_NAME -> UserName
row.Camel();

// 驼峰转下划线 UserName -> user_name
row.camel_();

// 下划线转小驼峰 USER_NAME -> userName
row.camel();
row.camel(false); // false:不处理全大写无下划线的情况
```

### 3.2 字符串处理

```java
// 去除首尾空格（所有String类型）
row.trim();
row.trim("NAME", "DESCRIPTION"); // 指定字段

// 多个空白压缩成一个空格
row.compress();
row.compress("CONTENT"); // 指定字段

// 全角转半角
row.sbc2dbc();
row.sbc2dbc("REMARK"); // 指定字段
```

## 4 JSON序列化/反序列化

### 4.1 解析JSON

```java
// 解析JSON字符串
String json = "{\"ID\":1,\"NAME\":\"张三\"}";
DataRow row = DataRow.parseJson(json);

// 解析JSON对象（Jackson JsonNode）
JsonNode node = BeanUtil.JSON_MAPPER.readTree(json);
DataRow row = DataRow.parseJson(node);
```

### 4.2 序列化为JSON

```java
// 序列化为JSON字符串
String json = row.toJSON();

// 带格式化的JSON
String prettyJson = row.toJSON(true);
```

## 5 对象解析

### 5.1 从Map解析

```java
Map<String, Object> map = new HashMap<>();
map.put("ID", 1);
map.put("NAME", "张三");
DataRow row = new DataRow(map);
```

### 5.2 从实体类解析

```java
User user = new User();
user.setId(1);
user.setName("张三");

// 直接解析
DataRow row = DataRow.parse(user);

// 指定字段映射（列名:属性名）
DataRow row = DataRow.parse(user, "ID:id", "NAME:userName");
```

### 5.3 从数组解析

```java
List<Object> list = Arrays.asList(1, "张三", 25);

// 以下标作为key
DataRow row = DataRow.parseList(list);  // {"0":1, "1":"张三", "2":25}

// 指定字段名
DataRow row = DataRow.parseList(list, "ID", "NAME", "AGE");  // {"ID":1, "NAME":"张三", "AGE":25}
```

## 6 元数据管理

```java
// 设置元数据
row.setMetadata(column);
row.setMetadata("ID", column);

// 获取元数据
Column col = row.getMetadata("ID");
String typeName = row.getMetadataTypeName("ID");
String className = row.getMetadataClassName("ID");
```

## 7 主键管理

```java
// 添加主键
row.addPrimaryKey("ID");
row.addPrimaryKey("ID", "CODE");

// 获取主键
List<String> pks = row.getPrimaryKeys();
```

## 8 配置属性

```java
// 设置是否覆盖空值
row.setOverride(true);

// 设置创建时间
row.serCreateTime(System.currentTimeMillis());
row.serCreateTime(new Date());
```
