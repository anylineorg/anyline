# 监听器 (Listener)

监听器用于监听数据库操作事件，包括 DDL 操作、DML 操作和数据源加载事件。与拦截器不同，监听器主要用于事件通知，不直接干预 SQL 执行流程。

## 监听器类型

AnyLine 提供以下监听器接口：

| 监听器 | 说明 |
|--------|------|
| `DDListener` | DDL 操作监听器（表、列、索引、约束等） |
| `DMListener` | DML 操作监听器（查询、插入、更新、删除） |
| `DataSourceListener` | 数据源加载监听器 |

## DDListener - DDL 操作监听

DDL 监听器监听数据库结构变更操作，包括表、列、视图、索引、约束等对象的创建、修改、删除。

### 支持的元数据对象

- `Table` - 表
- `Column` - 列
- `View` - 视图
- `Index` - 索引
- `PrimaryKey` - 主键
- `ForeignKey` - 外键
- `Constraint` - 约束
- `Procedure` - 存储过程
- `MasterTable` - 主表（分区表）
- `PartitionTable` - 分区表

### 监听时机

每个 DDL 操作提供三个监听时机：

| 方法前缀 | 说明 |
|----------|------|
| `prepareXxx` | DDL SQL 构建前，可修改元数据对象 |
| `beforeXxx` | DDL SQL 执行前，SQL 已生成 |
| `afterXxx` | DDL SQL 执行后 |

### 示例：表创建监听

```java
@Component
public class TableCreateListener implements DDListener {
    
    @Override
    public SWITCH prepareCreate(DataRuntime runtime, String random, Table table) {
        // 表创建前，可以修改表结构
        log.info("准备创建表: {}", table.getName());
        return SWITCH.CONTINUE;
    }
    
    @Override
    public SWITCH beforeCreate(DataRuntime runtime, String random, Table table, List<Run> runs) {
        // SQL 已生成，即将执行
        for (Run run : runs) {
            log.info("执行DDL: {}", run.getFinalUpdate());
        }
        return SWITCH.CONTINUE;
    }
    
    @Override
    public SWITCH afterCreate(DataRuntime runtime, String random, Table table, 
                               List<Run> runs, boolean result, long millis) {
        // 表创建完成
        if (result) {
            log.info("表 {} 创建成功, 耗时: {}ms", table.getName(), millis);
        } else {
            log.error("表 {} 创建失败", table.getName());
        }
        return SWITCH.CONTINUE;
    }
}
```

### 示例：列修改异常处理

```java
@Component
public class ColumnAlterListener implements DDListener {
    
    @Override
    public SWITCH afterAlterColumnException(DataRuntime runtime, String random, 
                                             Table table, Column column, Exception exception) {
        // 列类型转换异常时的处理
        log.warn("列 {} 类型转换异常: {}", column.getName(), exception.getMessage());
        
        // 可以在这里执行数据转换逻辑
        // 返回 SWITCH.CONTINUE 让框架继续尝试
        
        return SWITCH.CONTINUE;
    }
}
```

## DMListener - DML 操作监听

DML 监听器监听数据操作，包括查询、插入、更新、删除。

### 监听时机

| 方法 | 说明 |
|------|------|
| `prepareSelect` | 查询 SQL 构建前，可修改查询条件 |
| `beforeTotal` | 统计总数前 |
| `afterTotal` | 统计总数后 |
| `beforeSelect` | 查询执行前 |
| `afterSelect` | 查询执行后 |
| `beforeCount` | count 执行前 |
| `afterCount` | count 执行后 |
| `beforeExists` | exists 执行前 |
| `afterExists` | exists 执行后 |
| `prepareInsert` | 插入 SQL 构建前 |
| `beforeInsert` | 插入执行前 |
| `afterInsert` | 插入执行后 |
| `prepareUpdate` | 更新 SQL 构建前 |
| `beforeUpdate` | 更新执行前 |
| `afterUpdate` | 更新执行后 |
| `prepareDelete` | 删除 SQL 构建前 |
| `beforeDelete` | 删除执行前 |
| `afterDelete` | 删除执行后 |

### 示例：查询监听

```java
@Component
public class QueryListener implements DMListener {
    
    @Override
    public SWITCH prepareSelect(DataRuntime runtime, String random, 
                                RunPrepare prepare, ConfigStore configs, String... conditions) {
        // 可以在这里添加默认查询条件
        log.info("准备查询表: {}", prepare.getTable());
        return SWITCH.CONTINUE;
    }
    
    @Override
    public SWITCH afterSelect(DataRuntime runtime, String random, Run run, 
                               boolean success, DataSet<DataRow> set, long millis) {
        // 查询完成，记录日志
        log.info("查询完成, 结果数: {}, 耗时: {}ms", set.size(), millis);
        return SWITCH.CONTINUE;
    }
}
```

### 示例：插入监听 - 自动填充字段

```java
@Component
public class InsertListener implements DMListener {
    
    @Override
    public SWITCH prepareInsert(DataRuntime runtime, String random, int batch, 
                                 Table dest, Object obj, ConfigStore configs, List<String> columns) {
        // 自动填充创建人和创建时间
        if (obj instanceof DataRow) {
            DataRow row = (DataRow) obj;
            String userId = getCurrentUserId();
            if (userId != null && !row.containsKey("CREATE_USER")) {
                row.put("CREATE_USER", userId);
            }
            if (!row.containsKey("CREATE_TIME")) {
                row.put("CREATE_TIME", new Date());
            }
        }
        return SWITCH.CONTINUE;
    }
}
```

### 示例：慢查询告警

```java
@Component
public class SlowQueryListener implements DMListener {
    
    private static final long SLOW_THRESHOLD = 5000; // 5秒
    
    @Override
    public SWITCH afterSelect(DataRuntime runtime, String random, Run run, 
                               boolean success, DataSet<DataRow> set, long millis) {
        if (millis > SLOW_THRESHOLD) {
            log.warn("[慢查询告警] 数据源: {}, SQL: {}, 耗时: {}ms", 
                     runtime.getKey(), run.getFinalQuery(), millis);
            // 可以发送告警通知
            sendAlert(runtime.getKey(), run.getFinalQuery(), millis);
        }
        return SWITCH.CONTINUE;
    }
}
```

## DataSourceListener - 数据源加载监听

数据源加载监听器在数据源初始化完成后触发。

### 示例

```java
@Component
public class MyDataSourceListener implements DataSourceListener {
    
    @Override
    public void after() {
        // 所有数据源加载完成后的初始化操作
        log.info("数据源初始化完成");
        
        // 可以在这里执行：
        // 1. 初始化缓存
        // 2. 验证数据源连接
        // 3. 初始化元数据
    }
}
```

## 注册监听器

监听器通过 Spring 自动注册：

```java
@Component
public class MyDDListener implements DDListener {
    // Spring 会自动扫描并注册
}
```

或手动注册到 Adapter：

```java
DriverAdapter adapter = runtime.getAdapter();
adapter.setListener(new MyDDListener());  // DDListener
adapter.setListener(new MyDMListener());  // DMListener
```

## 监听器与拦截器的区别

| 特性 | 监听器 (Listener) | 拦截器 (Interceptor) |
|------|-------------------|----------------------|
| 主要用途 | 事件通知、日志记录 | 干预执行流程、修改参数 |
| 返回值影响 | 不影响执行流程 | 可中断或跳过执行 |
| 适用场景 | 操作日志、审计、告警 | 权限校验、数据脱敏、参数修改 |
| 注册方式 | 注册到 Adapter | 注册到 InterceptorProxy |

## 常见应用场景

### 1. 操作审计日志

```java
@Component
public class AuditListener implements DMListener {
    
    @Override
    public SWITCH afterInsert(DataRuntime runtime, String random, Run run, long count, 
                               Table dest, Object obj, List<String> columns, 
                               boolean success, long qty, long millis) {
        if (success) {
            AuditLog log = new AuditLog();
            log.setAction("INSERT");
            log.setTable(dest.getName());
            log.setUser(getCurrentUser());
            log.setTime(new Date());
            auditService.save(log);
        }
        return SWITCH.CONTINUE;
    }
    
    @Override
    public SWITCH afterUpdate(DataRuntime runtime, String random, Run run, long count, 
                               Table dest, Object obj, List<String> columns, 
                               boolean success, long qty, long millis) {
        // 记录更新审计日志
        // ...
        return SWITCH.CONTINUE;
    }
    
    @Override
    public SWITCH afterDelete(DataRuntime runtime, String random, Run run, 
                               boolean success, long qty, long millis) {
        // 记录删除审计日志
        // ...
        return SWITCH.CONTINUE;
    }
}
```

### 2. DDL 变更通知

```java
@Component
public class DDLNotifyListener implements DDListener {
    
    @Override
    public SWITCH afterCreate(DataRuntime runtime, String random, Table table, 
                               List<Run> runs, boolean result, long millis) {
        if (result) {
            notifyService.send("DDL", "表 " + table.getName() + " 已创建");
        }
        return SWITCH.CONTINUE;
    }
    
    @Override
    public SWITCH afterDrop(DataRuntime runtime, String random, Table table, 
                             List<Run> runs, boolean result, long millis) {
        if (result) {
            notifyService.send("DDL", "表 " + table.getName() + " 已删除");
        }
        return SWITCH.CONTINUE;
    }
}
```

### 3. 数据同步触发

```java
@Component
public class SyncTriggerListener implements DMListener {
    
    @Override
    public SWITCH afterInsert(DataRuntime runtime, String random, Run run, long count, 
                               Table dest, Object obj, List<String> columns, 
                               boolean success, long qty, long millis) {
        // 插入完成后触发数据同步
        if (success && needSync(dest.getName())) {
            syncService.triggerInsert(dest.getName(), obj);
        }
        return SWITCH.CONTINUE;
    }
}
```

## 注意事项

1. **监听器是全局的**：注册后对所有数据源生效
2. **不要在监听器中执行耗时操作**：会影响数据库操作性能
3. **返回值不影响流程**：监听器返回值主要用于日志记录，不会中断执行
4. **线程安全**：监听器实例是单例的，确保实现是线程安全的
5. **异常处理**：监听器中的异常不会影响数据库操作，但会被记录