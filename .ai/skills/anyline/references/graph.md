# 图数据库操作

AnyLine 提供对图数据库的统一 API 支持，包括 Neo4j、Nebula Graph、TuGraph 等。通过统一的接口操作图数据库，框架自动生成对应数据库的查询语言（如 Cypher、nGQL）。

## 支持的图数据库

| 数据库 | 模块 | 查询语言 |
|--------|------|----------|
| Neo4j | `anyline-data-neo4j` | Cypher |
| Nebula Graph | `anyline-data-nebula` | nGQL |
| TuGraph | `anyline-data-tugraph` | Cypher |

## 图数据库元数据对象

AnyLine 定义了图数据库特有的元数据对象：

| 类 | 说明 | 对应概念 |
|----|------|----------|
| `VertexTable` | 点表 | Neo4j: Label, Nebula: Tag |
| `EdgeTable` | 边表 | Neo4j: Relationship Type, Nebula: EdgeType |
| `VertexIndex` | 点索引 | 点属性索引 |
| `EdgeIndex` | 边索引 | 边属性索引 |

## 数据源配置

### Neo4j 配置

```yaml
# application.yml
anyline:
  datasource:
    neo4j:
      url: bolt://localhost:7687
      username: neo4j
      password: password
```

### Nebula Graph 配置

```yaml
# application.yml
anyline:
  datasource:
    nebula:
      hosts: 127.0.0.1:9669
      username: root
      password: nebula
      space: test_space
```

## 基本操作

### 1. 创建点（Vertex）

```java
// 创建点表（Label/Tag）
VertexTable vertexTable = new VertexTable("Person");
vertexTable.addColumn("name", ColumnType.STRING);
vertexTable.addColumn("age", ColumnType.INT);
service.ddl().create(vertexTable);

// 插入点数据
DataRow person = new DataRow();
person.put("name", "张三");
person.put("age", 30);
service.insert(new VertexTable("Person"), person);
```

### 2. 创建边（Edge）

```java
// 创建边表（Relationship Type/EdgeType）
EdgeTable edgeTable = new EdgeTable("FRIEND");
edgeTable.addColumn("since", ColumnType.DATE);
service.ddl().create(edgeTable);

// 插入边数据（需要起点和终点ID）
DataRow edge = new DataRow();
edge.put("from", "person1_id");  // 起点
edge.put("to", "person2_id");    // 终点
edge.put("since", "2020-01-01");
service.insert(new EdgeTable("FRIEND"), edge);
```

### 3. 查询点

```java
// 查询所有 Person 点
DataSet persons = service.selects(new VertexTable("Person"));

// 条件查询
ConfigStore configs = new DefaultConfigStore();
configs.and("name", "张三");
DataSet result = service.selects(new VertexTable("Person"), configs);
```

### 4. 查询边

```java
// 查询所有 FRIEND 边
DataSet edges = service.selects(new EdgeTable("FRIEND"));

// 查询特定起点/终点的边
ConfigStore configs = new DefaultConfigStore();
configs.and("from", "person1_id");
DataSet edges = service.selects(new EdgeTable("FRIEND"), configs);
```

## Neo4j 特定操作

### Cypher 查询

```java
// 执行原生 Cypher 查询
String cypher = "MATCH (p:Person) WHERE p.age > 25 RETURN p.name, p.age";
DataSet result = service.querys(cypher);

// 带参数的 Cypher 查询
String cypher = "MATCH (p:Person) WHERE p.name = $name RETURN p";
ConfigStore configs = new DefaultConfigStore();
configs.set("name", "张三");
DataSet result = service.querys(cypher, configs);
```

### 路径查询

```java
// 查询两点之间的路径
String cypher = """
    MATCH path = (a:Person {name: '张三'})-[:FRIEND*]-(b:Person {name: '李四'})
    RETURN path
    """;
DataSet paths = service.querys(cypher);

// 查询深度为 1-3 的朋友关系
String cypher = """
    MATCH (p:Person {name: '张三'})-[:FRIEND*1..3]-(friend)
    RETURN friend.name
    """;
DataSet friends = service.querys(cypher);
```

### 聚合查询

```java
// 统计每个年龄的人数
String cypher = """
    MATCH (p:Person)
    RETURN p.age, count(p) as count
    ORDER BY count DESC
    """;
DataSet stats = service.querys(cypher);
```

## Nebula Graph 特定操作

### nGQL 查询

```java
// 执行原生 nGQL 查询
String nGql = "MATCH (p:Person) WHERE p.age > 25 RETURN p.name, p.age";
DataSet result = service.querys(nGql);

// GO 查询（Nebula 特有）
String nGql = "GO FROM 'person1_id' OVER FRIEND YIELD $$.Person.name";
DataSet result = service.querys(nGql);
```

### Space 操作

```java
// 创建 Space（类似数据库）
Space space = new Space("my_space");
service.ddl().create(space);

// 切换 Space
service.metadata().switchSpace("my_space");
```

### Tag 和 EdgeType 操作

```java
// 创建 Tag（点类型）
Tag tag = new Tag("Person");
tag.addColumn("name", ColumnType.STRING);
tag.addColumn("age", ColumnType.INT);
service.ddl().create(tag);

// 创建 EdgeType（边类型）
EdgeType edgeType = new EdgeType("FRIEND");
edgeType.addColumn("since", ColumnType.DATE);
service.ddl().create(edgeType);
```

## 图遍历操作

### 1. 查询邻居节点

```java
// 查询某节点的直接邻居
String cypher = """
    MATCH (p:Person {id: 'person1'})-[]-(neighbor)
    RETURN neighbor
    """;
DataSet neighbors = service.selects(cypher);

// 查询出边邻居
String cypher = """
    MATCH (p:Person {id: 'person1'})-[:FRIEND]->(friend)
    RETURN friend.name
    """;
DataSet friends = service.selects(cypher);
```

### 2. 查询路径

```java
// 最短路径
String cypher = """
    MATCH path = shortestPath(
        (a:Person {name: '张三'})-[*]-(b:Person {name: '王五'})
    )
    RETURN path
    """;
DataSet shortest = service.selects(cypher);

// 所有路径（限制深度）
String cypher = """
    MATCH path = (a:Person {name: '张三'})-[:FRIEND*1..5]-(b:Person {name: '王五'})
    RETURN path
    """;
DataSet paths = service.selects(cypher);
```

### 3. 子图查询

```java
// 查询子图
String cypher = """
    MATCH (p:Person)-[r:FRIEND]-(f:Person)
    WHERE p.name = '张三'
    RETURN p, r, f
    """;
DataSet subgraph = service.selects(cypher);
```

## 批量操作

### 批量插入点

```java
DataSet persons = new DataSet();
for (int i = 0; i < 1000; i++) {
    DataRow person = new DataRow();
    person.put("name", "用户" + i);
    person.put("age", 20 + i % 50);
    persons.add(person);
}

// 批量插入，batch=100 表示每批 100 条
service.insert(100, new VertexTable("Person"), persons);
```

### 批量插入边

```java
DataSet edges = new DataSet();
for (int i = 0; i < 100; i++) {
    DataRow edge = new DataRow();
    edge.put("from", "person_" + i);
    edge.put("to", "person_" + (i + 1));
    edge.put("since", "2020-01-01");
    edges.add(edge);
}

service.insert(50, new EdgeTable("FRIEND"), edges);
```

## 元数据查询

### 查询点表结构

```java
// 查询所有点表（Labels/Tags）
List<VertexTable> vertices = service.metadata().vertexTables();

// 查询特定点表结构
VertexTable vertex = service.metadata().vertexTable("Person");
List<Column> columns = vertex.getColumns();
```

### 查询边表结构

```java
// 查询所有边表（Relationship Types/EdgeTypes）
List<EdgeTable> edges = service.metadata().edgeTables();

// 查询特定边表结构
EdgeTable edge = service.metadata().edgeTable("FRIEND");
List<Column> columns = edge.getColumns();
```

### 查询索引

```java
// 查询点索引
List<VertexIndex> vertexIndexes = service.metadata().vertexIndexes();

// 查询边索引
List<EdgeIndex> edgeIndexes = service.metadata().edgeIndexes();
```

## DDL 操作

### 创建索引

```java
// 创建点属性索引
VertexIndex index = new VertexIndex("person_name_idx");
index.setTable(new VertexTable("Person"));
index.addColumn("name");
service.ddl().create(index);

// 创建复合索引
VertexIndex compositeIndex = new VertexIndex("person_name_age_idx");
compositeIndex.setTable(new VertexTable("Person"));
compositeIndex.addColumn("name");
compositeIndex.addColumn("age");
service.ddl().create(compositeIndex);
```

### 删除点表/边表

```java
// 删除点表
service.ddl().drop(new VertexTable("Person"));

// 删除边表
service.ddl().drop(new EdgeTable("FRIEND"));
```

## 注意事项

1. **ID 处理**：图数据库中点的 ID 通常由数据库自动生成，插入后可通过返回结果获取
2. **边必须有起点和终点**：插入边数据时必须提供 `from` 和 `to` 字段
3. **查询语言差异**：虽然 AnyLine 提供统一 API，但复杂查询可能需要使用原生查询语言
4. **性能考虑**：图遍历查询可能涉及大量数据，注意限制深度和使用索引
5. **事务支持**：Neo4j 支持事务，Nebula Graph 事务支持有限

## 与关系型数据库的区别

| 特性 | 关系型数据库 | 图数据库 |
|------|-------------|----------|
| 数据结构 | 表、行、列 | 点、边、属性 |
| 关系表示 | 外键、JOIN | 边、直接连接 |
| 查询方式 | SQL | Cypher/nGQL |
| 适用场景 | 结构化数据、事务处理 | 关系密集型数据、图分析 |
| 遍历性能 | JOIN 性能随深度下降 | 直接遍历，性能稳定 |