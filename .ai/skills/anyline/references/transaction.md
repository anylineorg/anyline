# 事务管理

## 概述
Anyline提供完整的事务管理能力，支持编程式事务，包含事务定义、事务状态和事务管理器三个核心组件。

## 1 核心接口

### 1.1 TransactionDefine（事务定义）

事务定义接口，用于配置事务的传播级别、隔离级别、超时时间等参数。

#### 1.1.1 事务模式

| 模式 | 说明 |
|------|------|
| THREAD | 线程内有效（默认） |
| APPLICATION | 应用内有效（跨线程） |
| DISTRIBUTED | 分布式事务 |

#### 1.1.2 事务传播级别

| 常量 | 值 | 说明 |
|------|---|------|
| PROPAGATION_REQUIRED | 0 | 如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务 |
| PROPAGATION_SUPPORTS | 1 | 如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行 |
| PROPAGATION_MANDATORY | 2 | 如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常 |
| PROPAGATION_REQUIRES_NEW | 3 | 创建一个新的事务，如果当前存在事务，则把当前事务挂起 |
| PROPAGATION_NOT_SUPPORTED | 4 | 以非事务方式运行，如果当前存在事务，则把当前事务挂起 |
| PROPAGATION_NEVER | 5 | 以非事务方式运行，如果当前存在事务，则抛出异常 |
| PROPAGATION_NESTED | 6 | 如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则等价于REQUIRED |

#### 1.1.3 事务隔离级别

| 常量 | 值 | 说明 |
|------|---|------|
| ISOLATION_DEFAULT | -1 | 使用底层数据库的默认隔离级别 |
| ISOLATION_READ_UNCOMMITTED | 1 | 允许脏读、不可重复读和幻读 |
| ISOLATION_READ_COMMITTED | 2 | 禁止脏读，允许不可重复读和幻读 |
| ISOLATION_REPEATABLE_READ | 4 | 禁止脏读和不可重复读，允许幻读 |
| ISOLATION_SERIALIZABLE | 8 | 禁止脏读、不可重复读和幻读 |

#### 1.1.4 方法列表

```java
// 获取传播级别
int getPropagationBehavior();

// 获取传播级别名称
String getPropagationBehaviorName();

// 获取隔离级别
int getIsolationLevel();

// 设置超时时间（秒）
void setTimeout(int timeout);

// 获取超时时间
int getTimeout();

// 设置只读模式
void setReadOnly(boolean readOnly);

// 是否只读
boolean isReadOnly();

// 获取事务名称
String getName();

// 设置事务名称
void setName(String name);

// 获取事务模式
TransactionDefine.MODE getMode();

// 设置事务模式
void setMode(TransactionDefine.MODE mode);
```

### 1.2 TransactionManage（事务管理器）

事务管理器接口，提供事务的启动、提交、回滚等操作。

#### 1.2.1 静态方法

```java
// 注册事务管理器
TransactionManage.reg("datasource", manager);

// 根据状态获取事务管理器
TransactionManage manager = TransactionManage.instance(state);

// 根据数据源获取事务管理器
TransactionManage manager = TransactionManage.instance("datasource");
```

#### 1.2.2 实例方法

```java
// 启动事务（完整配置）
TransactionState state = manager.start(define);

// 启动事务（指定传播级别）
TransactionState state = manager.start(TransactionDefine.PROPAGATION_REQUIRED);

// 启动事务（默认传播级别 REQUIRED）
TransactionState state = manager.start();

// 提交事务
manager.commit(state);

// 回滚事务
manager.rollback(state);
```

### 1.3 TransactionState（事务状态）

事务状态接口，用于跟踪事务的执行状态。

#### 1.3.1 方法列表

```java
// 获取原始连接
Object getOrigin();
void setOrigin(Object origin);

// 保存点操作
boolean hasSavepoint();
Savepoint getPoint();
void setPoint(Savepoint point);
Object createSavepoint() throws Exception;
void rollbackToSavepoint(Object savepoint) throws Exception;
void releaseSavepoint(Object savepoint) throws Exception;

// 事务状态
boolean isNewTransaction();
void setRollbackOnly();
boolean isRollbackOnly();
boolean isCompleted();

// 数据源和连接
void setDataSource(DataSource datasource);
DataSource getDataSource();
void setConnection(Connection connection);
Connection getConnection();

// 事务信息
String getName();
void setName(String name);
TransactionDefine.MODE getMode();
void setMode(TransactionDefine.MODE mode);
```

## 2 编程式事务

### 2.1 基本使用

```java
// 获取事务管理器
TransactionManage manager = TransactionManage.instance("datasource");

// 方式一：使用默认配置启动事务
TransactionState state = manager.start();
try {
    // 执行数据库操作
    service.insert("USER", data);
    service.update("ORDER", updateData);
    
    // 提交事务
    manager.commit(state);
} catch (Exception e) {
    // 回滚事务
    manager.rollback(state);
    throw e;
}
```

### 2.2 指定传播级别

```java
// 指定事务传播级别
TransactionState state = manager.start(TransactionDefine.PROPAGATION_REQUIRES_NEW);
try {
    // 事务操作
    manager.commit(state);
} catch (Exception e) {
    manager.rollback(state);
    throw e;
}
```

### 2.3 完整配置事务

```java
// 创建完整的事务定义
TransactionDefine define = new DefaultTransactionDefine();
define.setPropagationBehavior(TransactionDefine.PROPAGATION_REQUIRED);
define.setIsolationLevel(TransactionDefine.ISOLATION_READ_COMMITTED);
define.setTimeout(30); // 30秒超时
define.setReadOnly(false);
define.setName("user_transaction");
define.setMode(TransactionDefine.MODE.THREAD);

// 启动事务
TransactionState state = manager.start(define);
try {
    // 事务操作
    manager.commit(state);
} catch (Exception e) {
    manager.rollback(state);
    throw e;
}
```

## 3 跨线程事务

### 3.1 应用级事务

```java
// 创建应用级事务（跨线程有效）
TransactionDefine define = new DefaultTransactionDefine();
define.setMode(TransactionDefine.MODE.APPLICATION);
define.setName("app_transaction");

TransactionState state = manager.start(define);
try {
    // 在不同线程中执行操作
    executorService.submit(() -> {
        // 该操作会加入同一个事务
        service.insert("LOG", logData);
    });
    
    manager.commit(state);
} catch (Exception e) {
    manager.rollback(state);
    throw e;
}
```

## 4 保存点操作

```java
TransactionState state = manager.start();
try {
    // 执行操作1
    service.insert("TABLE_A", dataA);
    
    // 创建保存点
    Object savepoint = state.createSavepoint();
    
    // 执行操作2
    service.insert("TABLE_B", dataB);
    
    // 如果操作2失败，可以回滚到保存点
    manager.commit(state);
} catch (Exception e) {
    // 回滚到保存点（如果有）或完全回滚
    if (state.hasSavepoint()) {
        state.rollbackToSavepoint(state.getPoint());
        // 继续处理或提交部分操作
        manager.commit(state);
    } else {
        manager.rollback(state);
    }
    throw e;
}
```

## 5 事务管理器注册

```java
// 创建事务管理器
TransactionManage customManager = new DefaultTransactionManage(dataSource);

// 注册到全局管理器
TransactionManage.reg("custom_ds", customManager);

// 获取已注册的事务管理器
TransactionManage manager = TransactionManage.instance("custom_ds");
```

## 6 Spring 事务配合

AnyLine 事务可以与 Spring 的声明式事务配合使用，根据场景选择合适的事务管理方式。

### 6.1 Spring 声明式事务（推荐）

在 Spring 环境中，推荐使用 `@Transactional` 注解管理事务：

```java
@Service
public class UserService {
    
    @Autowired
    private AnylineService service;
    
    // Spring 声明式事务
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void saveUser(DataRow user) {
        service.insert("USER", user);
        service.insert("USER_LOG", createLog(user));
    }
    
    // 嵌套事务
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(DataRow log) {
        service.insert("USER_LOG", log);
    }
}
```

### 6.2 AnyLine 编程式事务与 Spring 事务配合

当需要在 Spring 事务内部使用 AnyLine 编程式事务时：

```java
@Service
public class OrderService {
    
    @Autowired
    private AnylineService service;
    
    // Spring 事务
    @Transactional
    public void processOrder(DataRow order) {
        // Spring 管理的事务中执行
        service.insert("ORDER", order);
        
        // 需要独立事务的操作（不受外部事务影响）
        TransactionManage manager = TransactionManage.instance("default");
        TransactionDefine define = new DefaultTransactionDefine();
        define.setPropagationBehavior(TransactionDefine.PROPAGATION_REQUIRES_NEW);
        
        TransactionState state = manager.start(define);
        try {
            // 独立事务中执行，外部事务回滚不影响此操作
            service.insert("ORDER_LOG", createLog(order));
            manager.commit(state);
        } catch (Exception e) {
            manager.rollback(state);
            // 日志记录失败不影响主流程
        }
        
        // 继续在 Spring 事务中执行
        service.update("INVENTORY", reduceStock(order));
    }
}
```

### 6.3 多数据源事务

对于多数据源场景，Spring 事务需要指定数据源：

```java
@Service
public class MultiDsService {
    
    @Autowired
    @Qualifier("crmService")
    private AnylineService crmService;
    
    @Autowired
    @Qualifier("hrService")
    private AnylineService hrService;
    
    // CRM 数据源事务
    @Transactional(value = "crmTransactionManager", propagation = Propagation.REQUIRED)
    public void saveCrmData(DataRow data) {
        crmService.insert("CRM_CUSTOMER", data);
    }
    
    // HR 数据源事务
    @Transactional(value = "hrTransactionManager", propagation = Propagation.REQUIRED)
    public void saveHrData(DataRow data) {
        hrService.insert("HR_EMPLOYEE", data);
    }
    
    // 跨数据源事务（需要分布式事务或手动管理）
    public void saveBoth(DataRow crmData, DataRow hrData) {
        // 使用 AnyLine 编程式事务分别管理
        TransactionManage crmManager = TransactionManage.instance("crm");
        TransactionManage hrManager = TransactionManage.instance("hr");
        
        TransactionState crmState = crmManager.start();
        TransactionState hrState = hrManager.start();
        
        try {
            crmService.insert("CRM_CUSTOMER", crmData);
            hrService.insert("HR_EMPLOYEE", hrData);
            
            crmManager.commit(crmState);
            hrManager.commit(hrState);
        } catch (Exception e) {
            crmManager.rollback(crmState);
            hrManager.rollback(hrState);
            throw e;
        }
    }
}
```

### 6.4 事务配置建议

| 场景 | 推荐方式 |
|------|----------|
| 单数据源简单事务 | Spring `@Transactional` |
| 单数据源复杂事务（需要保存点） | AnyLine 编程式事务 |
| 多数据源独立事务 | Spring 多事务管理器 |
| 多数据源关联事务 | AnyLine 编程式事务 + 手动管理 |
| 跨线程事务 | AnyLine APPLICATION 模式事务 |

### 6.5 注意事项

1. **临时数据源不支持 Spring 事务**：通过 `ServiceProxy.temporary()` 创建的临时数据源不支持 `@Transactional` 注解，需使用 AnyLine 编程式事务
2. **事务传播**：AnyLine 编程式事务与 Spring 事务是独立的，不会自动传播
3. **连接释放**：确保事务完成后正确提交或回滚，避免连接泄漏
4. **超时设置**：长时间事务应设置超时时间，避免占用连接过久

```java
// 临时数据源事务示例
DataSource tempDs = DataSourceUtil.build(url, username, password);
AnylineService tempService = ServiceProxy.temporary(tempDs);

// 必须使用 AnyLine 编程式事务
TransactionManage manager = TransactionManage.instance(tempService.runtime());
TransactionState state = manager.start();
try {
    tempService.insert("TEMP_TABLE", data);
    manager.commit(state);
} catch (Exception e) {
    manager.rollback(state);
}
```
