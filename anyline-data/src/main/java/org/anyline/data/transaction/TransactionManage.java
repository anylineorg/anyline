package org.anyline.data.transaction;


import org.anyline.data.transaction.init.DefaultTransactionDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Map;

public interface TransactionManage {
    Logger log = LoggerFactory.getLogger(TransactionManage.class);
    Map<TransactionState, TransactionManage> records = new Hashtable<>();
    Map<String, TransactionManage> instances = new Hashtable<>();
    static void reg(String datasource, TransactionManage instance){
        instances.put(datasource, instance);
    }
    static TransactionManage instance(TransactionState state){
        return records.get(state);
    }
    static TransactionManage instance(String datasource){
        return instances.get(datasource);
    }


    /**
     * 启动事务
     * @param define 事务定义相关参数
     * @return status 回溯或提交时需要
     */
    TransactionState start(TransactionDefine define) throws Exception;

    /**
     * 启动事务
     * @return behavior 事务传播方式<br/>
     * 更多参数调用start(String datasource, TransactionDefine define)
     */
    default TransactionState start(int behavior) throws Exception{
        TransactionDefine define = new DefaultTransactionDefine(behavior);
        return start(define);
    }

    /**
     * 启动事务
     * 更多参数调用start(TransactionDefine define)
     */
    default TransactionState start() throws Exception{
        return start(TransactionDefine.PROPAGATION_REQUIRED);
    }


    /**
     * 提交事务
     * @param state 启动事务时返回state
     */
    void commit(TransactionState state) throws Exception;
    /**
     * 回滚事务
     * @param state 启动事务时返回state
     */
    void rollback(TransactionState state) throws Exception;
}
