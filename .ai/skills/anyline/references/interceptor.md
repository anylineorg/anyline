# 拦截器 (Interceptor)

拦截器用于在 SQL 执行前后进行拦截处理，可以实现日志记录、权限校验、SQL 修改、数据脱敏等功能。

## 拦截器类型

AnyLine 提供以下拦截器接口：

| 拦截器 | 说明 |
|--------|------|
| `SelectInterceptor` | 查询拦截器 |
| `InsertInterceptor` | 插入拦截器 |
| `UpdateInterceptor` | 更新拦截器 |
| `DeleteInterceptor` | 删除拦截器 |

## 拦截时机

每个拦截器提供三个拦截时机：

| 方法 | 说明 | 可执行操作 |
|------|------|------------|
| `prepare()` | SQL 构建前 | 修改查询条件、修改参数、添加默认条件 |
| `before()` | SQL 执行前 | 记录日志、校验权限、修改 SQL |
| `after()` | SQL 执行后 | 记录日志、处理结果、数据脱敏 |

## 返回值 SWITCH

拦截器方法返回 `SWITCH` 枚举值控制执行流程：

| 值 | 说明 |
|----|------|
| `CONTINUE` | 继续执行，不中断 |
| `SKIP` | 跳过当前拦截器，继续执行后续拦截器 |
| `BREAK` | 中断执行，不执行 SQL |
| `RETURN` | 直接返回，不执行后续拦截器和 SQL |

## 实现拦截器

### 1. 查询拦截器示例

```java
@Component
public class LogSelectInterceptor implements SelectInterceptor {
    
    @Override
    public SWITCH before(DataRuntime runtime, String random, Run run, ConfigStore configs) {
        // SQL 执行前记录日志
        String sql = run.getFinalQuery();
        log.info("执行查询SQL: {}", sql);
        return SWITCH.CONTINUE;
    }
    
    @Override
    public SWITCH after(DataRuntime runtime, String random, Run run, boolean success, 
                         Object result, ConfigStore configs, long millis) {
        // SQL 执行后记录耗时
        log.info("查询完成, 耗时: {}ms, 结果数: {}", millis, 
                 result instanceof DataSet ? ((DataSet)result).size() : 0);
        return SWITCH.CONTINUE;
    }
}
```

### 2. 插入拦截器示例 - 自动填充创建时间

```java
@Component
public class CreateTimeInterceptor implements InsertInterceptor {
    
    @Override
    public SWITCH prepare(DataRuntime runtime, String random, int batch, 
                          Table dest, Object data, ConfigStore configs, List<String> columns) {
        // 自动填充创建时间
        if (data instanceof DataRow) {
            DataRow row = (DataRow) data;
            if (!row.containsKey("CREATE_TIME")) {
                row.put("CREATE_TIME", new Date());
            }
        }
        return SWITCH.CONTINUE;
    }
}
```

### 3. 更新拦截器示例 - 数据权限校验

```java
@Component
public class DataPermissionInterceptor implements UpdateInterceptor {
    
    @Override
    public SWITCH prepare(DataRuntime runtime, String random, int batch, 
                          Table dest, Object data, ConfigStore configs, List<String> columns) {
        // 添加数据权限条件
        String userId = getCurrentUserId();
        if (userId != null) {
            configs.and("UPDATE_USER", userId);
        }
        return SWITCH.CONTINUE;
    }
}
```

### 4. 删除拦截器示例 - 禁止删除

```java
@Component
public class DeleteProtectInterceptor implements DeleteInterceptor {
    
    @Override
    public SWITCH prepare(DataRuntime runtime, String random, int batch, 
                          Table dest, Object data, ConfigStore configs, List<String> columns) {
        // 禁止删除核心表数据
        if ("SYS_CONFIG".equals(dest.getName())) {
            log.warn("禁止删除系统配置表数据");
            return SWITCH.BREAK;  // 中断执行
        }
        return SWITCH.CONTINUE;
    }
}
```

## 注册拦截器

拦截器可以通过以下方式注册：

### 1. Spring 自动注册（推荐）

实现拦截器接口并添加 `@Component` 注解，Spring 会自动扫描并注册：

```java
@Component
public class MySelectInterceptor implements SelectInterceptor {
    // ...
}
```

### 2. 手动注册到 Adapter

```java
// 获取数据库适配器
DriverAdapter adapter = runtime.getAdapter();

// 注册拦截器
adapter.setInterceptor(new MySelectInterceptor());
```

## 拦截器执行顺序

多个拦截器按以下顺序执行：
1. 按 `@Order` 注解或 `Ordered` 接口的值排序
2. 数值小的先执行
3. 同一类型的拦截器按注册顺序执行

```java
@Component
@Order(1)  // 优先执行
public class FirstInterceptor implements SelectInterceptor {
    // ...
}

@Component
@Order(2)  // 后执行
public class SecondInterceptor implements SelectInterceptor {
    // ...
}
```

## 常见应用场景

### 1. SQL 日志记录

```java
@Component
public class SqlLogInterceptor implements SelectInterceptor, InsertInterceptor, 
                                          UpdateInterceptor, DeleteInterceptor {
    
    @Override
    public SWITCH before(DataRuntime runtime, String random, Run run, ConfigStore configs) {
        log.info("[SQL] {}", run.getFinalQuery());
        log.info("[参数] {}", run.getValues());
        return SWITCH.CONTINUE;
    }
    
    @Override
    public SWITCH after(DataRuntime runtime, String random, Run run, boolean success, 
                         Object result, ConfigStore configs, long millis) {
        if (!success) {
            log.error("[SQL执行失败] {}", run.getFinalQuery());
        }
        return SWITCH.CONTINUE;
    }
}
```

### 2. 数据脱敏

```java
@Component
public class DataMaskInterceptor implements SelectInterceptor {
    
    private static final List<String> MASK_COLUMNS = Arrays.asList("PHONE", "EMAIL", "ID_CARD");
    
    @Override
    public SWITCH after(DataRuntime runtime, String random, Run run, boolean success, 
                         Object result, ConfigStore configs, long millis) {
        if (result instanceof DataSet) {
            DataSet set = (DataSet) result;
            for (DataRow row : set) {
                for (String column : MASK_COLUMNS) {
                    if (row.containsKey(column)) {
                        row.put(column, mask(row.getString(column)));
                    }
                }
            }
        }
        return SWITCH.CONTINUE;
    }
    
    private String mask(String value) {
        if (value == null) return null;
        // 手机号脱敏：138****1234
        if (value.length() == 11) {
            return value.substring(0, 3) + "****" + value.substring(7);
        }
        return "****";
    }
}
```

### 3. 慢查询监控

```java
@Component
public class SlowQueryInterceptor implements SelectInterceptor {
    
    private static final long SLOW_THRESHOLD = 3000; // 3秒
    
    @Override
    public SWITCH after(DataRuntime runtime, String random, Run run, boolean success, 
                         Object result, ConfigStore configs, long millis) {
        if (millis > SLOW_THRESHOLD) {
            log.warn("[慢查询] 耗时: {}ms, SQL: {}", millis, run.getFinalQuery());
            // 可以发送告警通知
        }
        return SWITCH.CONTINUE;
    }
}
```

### 4. 自动添加租户条件

```java
@Component
public class TenantInterceptor implements SelectInterceptor, DeleteInterceptor {
    
    @Override
    public SWITCH prepare(DataRuntime runtime, String random, RunPrepare prepare, 
                          ConfigStore configs, String... conditions) {
        // 自动添加租户隔离条件
        String tenantId = getCurrentTenantId();
        if (tenantId != null) {
            configs.and("TENANT_ID", tenantId);
        }
        return SWITCH.CONTINUE;
    }
}
```

## 注意事项

1. **拦截器是全局的**：注册后对所有数据源生效，如需针对特定数据源，需在拦截器内部判断
2. **性能影响**：拦截器会在每次 SQL 执行时调用，避免在拦截器中执行耗时操作
3. **返回值控制**：合理使用 `SWITCH.BREAK` 和 `SWITCH.RETURN` 避免意外中断正常流程
4. **线程安全**：拦截器实例是单例的，确保实现是线程安全的