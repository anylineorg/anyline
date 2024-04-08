package org.anyline.environment.spring.data.transaction;

import org.anyline.data.transaction.TransactionDefine;
import org.anyline.data.transaction.TransactionManage;
import org.anyline.data.transaction.TransactionState;
import org.anyline.data.transaction.init.DefaultTransactionManage;
import org.anyline.data.transaction.init.DefaultTransactionState;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

public class SpringTransactionManage extends DefaultTransactionManage implements TransactionManage {
    private final DataSourceTransactionManager manager;
    public SpringTransactionManage(DataSource datasource){
        manager = new DataSourceTransactionManager();
        manager.setDataSource(datasource);
    }
    public SpringTransactionManage(DataSourceTransactionManager manager){
        this.manager = manager;
    }
    @Override
    public TransactionState start(TransactionDefine define) {
        TransactionStatus status = manager.getTransaction(definition(define));
        TransactionState state = state(status);
        TransactionManage.records.put(state, this);
        return state;
    }

    /**
     * 提交事务
     * @param state 启动事务时返回status
     */
    @Override
    public void commit(TransactionState state) {
        Object origin = state.getOrigin();
        manager.commit((TransactionStatus)origin);
    }

    /**
     * 回滚事务
     * @param state 启动事务时返回status
     */
    @Override
    public void rollback(TransactionState state) {
        Object origin = state.getOrigin();
        manager.rollback((TransactionStatus)origin);
    }

    /**
     * anyline2spring
     * @param define TransactionDefine
     * @return TransactionDefinition
     */
    private TransactionDefinition definition(TransactionDefine define){
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(define.getPropagationBehavior());
        String behaviorName = define.getPropagationBehaviorName();
        if(null != behaviorName) {
            definition.setPropagationBehaviorName(behaviorName);
        }
        definition.setIsolationLevel(define.getIsolationLevel());
        definition.setName(define.getName());
        definition.setTimeout(define.getTimeout());
        definition.setReadOnly(define.isReadOnly());
        return definition;
    }

    /**
     * spring2anyline
     * @param status TransactionStatus
     * @return TransactionState
     */
    private TransactionState state(TransactionStatus status){
        TransactionState state = new DefaultTransactionState();
        state.setOrigin(status);
        return state;
    }
}
