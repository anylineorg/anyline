/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.anyline.data.transaction;


import org.anyline.data.transaction.init.DefaultTransactionDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

public interface TransactionManage {
    Logger log = LoggerFactory.getLogger(TransactionManage.class);
    Map<TransactionState, TransactionManage> records = new Hashtable<>();
    Map<String, TransactionManage> instances = new Hashtable<>();
    static void reg(String datasource, TransactionManage instance) {
        instances.put(datasource, instance);
    }
    static TransactionManage instance(TransactionState state) {
        return records.get(state);
    }
    static TransactionManage instance(String datasource) {
        return instances.get(datasource);
    }


    /**
     * 启动事务
     * @param define 事务定义相关参数
     * @return status 回溯或提交时需要
     */
    TransactionState start(TransactionDefine define) throws SQLException;

    /**
     * 启动事务
     * @return behavior 事务传播方式<br/>
     * 更多参数调用start(String datasource, TransactionDefine define)
     */
    default TransactionState start(int behavior) throws SQLException {
        TransactionDefine define = new DefaultTransactionDefine(behavior);
        return start(define);
    }

    /**
     * 启动事务
     * 更多参数调用start(TransactionDefine define)
     */
    default TransactionState start() throws SQLException {
        return start(TransactionDefine.PROPAGATION_REQUIRED);
    }

    /**
     * 提交事务
     * @param state 启动事务时返回state
     */
    void commit(TransactionState state) throws SQLException;
    /**
     * 回滚事务
     * @param state 启动事务时返回state
     */
    void rollback(TransactionState state) throws SQLException;
}
