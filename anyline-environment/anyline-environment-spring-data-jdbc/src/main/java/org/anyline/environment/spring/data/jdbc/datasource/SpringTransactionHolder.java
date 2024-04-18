package org.anyline.environment.spring.data.jdbc.datasource;

public class SpringTransactionHolder {/*
    private static Logger log = LoggerFactory.getLogger(SpringTransactionHolder.class);
    private static Map<TransactionStatus, String> records = new Hashtable<>();

    *//**
     * 启动事务
     * @param datasource 数据源
     * @param definition 事务定义相关参数
     * @return TransactionStatus 回溯或提交时需要
     *//*
    public static TransactionStatus start(String datasource, TransactionDefinition definition){
        DataSourceTransactionManager dtm = null;
        if(BasicUtil.isEmpty(datasource) || "default".equals(datasource) ||!ConfigTable.IS_OPEN_TRANSACTION_MANAGER){
            dtm = (DataSourceTransactionManager) ConfigTable.environment().getBean("transactionManager");
            datasource = "default";
        }else {
            dtm = (DataSourceTransactionManager) ConfigTable.environment().getBean(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  datasource);
        }
        // 获取事务
        TransactionStatus status = dtm.getTransaction(definition);
        records.put(status, datasource);
        return status;
    }

    *//**
     * 启动事务
     * @param datasource 数据源
     * @return behavior 事务传播方式<br/>
     * 更多参数调用start(String datasource, TransactionDefine define)
     *//*
    public static TransactionStatus start(String datasource, int behavior){
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        // 定义事务传播方式
        definition.setPropagationBehavior(behavior);
        return start(datasource, definition);
    }

    *//**
     * 启动事务
     * 	 * @param datasource 数据源
     * 更多参数调用start(String datasource, TransactionDefine define)
     *//*
    public static TransactionStatus start(String datasource){
        return start(datasource, TransactionDefinition.PROPAGATION_REQUIRED);
    }

    *//**
     * 启动事务(默认数据源)
     * @param definition 事务定义相关参数
     * @return TransactionStatus 回溯或提交时需要
     *//*
    public static TransactionStatus start(TransactionDefinition definition){
        return start(RuntimeHolder.runtime().getKey(), definition);
    }

    *//**
     * 启动事务
     * @param behavior 事务传播方式<br/>
     * 更多参数调用start(String datasource, TransactionDefine define)
     * @return TransactionStatus 回溯或提交时需要
     *//*
    public static TransactionStatus start(int behavior){
        return start(RuntimeHolder.runtime().getKey(), behavior);
    }

    *//**
     * 启动事务
     * @return TransactionStatus 回溯或提交时需要
     *//*
    public static TransactionStatus start(){
        return start(RuntimeHolder.runtime().getKey());
    }

    *//**
     * 提交事务
     * @param status 启动事务时返回TransactionStatus
     *//*
    public static void commit(TransactionStatus status){
        String datasource = records.get(status);
        DataSourceTransactionManager dtm = null;
        if(BasicUtil.isEmpty(datasource) || !ConfigTable.IS_OPEN_TRANSACTION_MANAGER){
            dtm = (DataSourceTransactionManager) ConfigTable.environment().getBean("transactionManager");
        }else {
            dtm = (DataSourceTransactionManager) ConfigTable.environment().getBean(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  datasource);
        }
        if(null != dtm) {
            dtm.commit(status);
        }else{
            log.error("[提交事务][datasource:{}][result:false][message:管理管理器不存在]", datasource);
        }
        records.remove(status);
    }

    *//**
     * 回滚事务
     * @param status 启动事务时返回TransactionStatus
     *//*
    public static void rollback(TransactionStatus status){
        String datasource = records.get(status);
        DataSourceTransactionManager dtm = null;
        if(BasicUtil.isEmpty(datasource) || !ConfigTable.IS_OPEN_TRANSACTION_MANAGER){
            dtm = (DataSourceTransactionManager) ConfigTable.environment().getBean("transactionManager");
        }else {
            dtm = (DataSourceTransactionManager) ConfigTable.environment().getBean(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  datasource);
        }
        if(null != dtm) {
            dtm.rollback(status);
        }else{
            log.error("[回滚事务][datasource:{}][result:false][message:管理管理器不存在]", datasource);
        }
        records.remove(status);
    }*/
}
