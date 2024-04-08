package org.anyline.proxy;

import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.transaction.TransactionDefine;
import org.anyline.data.transaction.TransactionManage;
import org.anyline.data.transaction.TransactionState;

public class TransactionProxy {
    /**
     * 启动事务
     * @param datasource 数据源
     * @param define 事务定义相关参数
     * @return status 回溯或提交时需要
     */
    public static TransactionState start(String datasource, TransactionDefine define) throws Exception {
        TransactionManage manage = TransactionManage.instance(datasource);
        if(null == manage){
            throw new Exception("未创建相关数据源("+datasource+")事务管理器");
        }
        return manage.start(define);
    }

    /**
     * 启动事务
     * @param datasource 数据源
     * @return behavior 事务传播方式<br/>
     * 更多参数调用start(String datasource, TransactionDefine define)
     */
    public static TransactionState start(String datasource, int behavior) throws Exception {
        TransactionManage manage = TransactionManage.instance(datasource);
        if(null == manage){
            throw new Exception("未创建相关数据源("+datasource+")事务管理器");
        }
        return manage.start(behavior);
    }

    /**
     * 启动事务
     * 	 * @param datasource 数据源
     * 更多参数调用start(String datasource, TransactionDefine define)
     */
    public static TransactionState start(String datasource)  throws Exception {
        return start(datasource, TransactionDefine.PROPAGATION_REQUIRED);
    }

    /**
     * 启动事务(默认数据源)
     * @param define 事务定义相关参数
     * @return status 回溯或提交时需要
     */
    public static TransactionState start(TransactionDefine define) throws Exception {
        return start(RuntimeHolder.runtime().datasource(), define);
    }

    /**
     * 启动事务(默认数据源)
     * @param behavior 事务传播方式<br/>
     * 更多参数调用start(String datasource, TransactionDefine define)
     * @return status 回溯或提交时需要
     */
    public static TransactionState start(int behavior) throws Exception {
        return start(RuntimeHolder.runtime().datasource(), behavior);
    }

    /**
     * 启动事务(默认数据源)
     * @return status 回溯或提交时需要
     */
    public static TransactionState start() throws Exception {
        return start(RuntimeHolder.runtime().datasource());
    }
    /**
     * 提交事务
     * @param state 启动事务时返回status
     */
    public static void commit(TransactionState state) throws Exception {
        TransactionManage manage = TransactionManage.instance(state);
        if(null == manage){
            throw new Exception("事务管理器异常");
        }
        manage.commit(state);
    }
    /**
     * 回滚事务
     * @param state 启动事务时返回status
     */
    public static void rollback(TransactionState state) throws Exception {
        TransactionManage manage = TransactionManage.instance(state);
        if(null == manage){
            throw new Exception("事务管理器异常");
        }
        manage.rollback(state);
    }
}
