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

package org.anyline.proxy;

import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.transaction.TransactionDefine;
import org.anyline.data.transaction.TransactionManage;
import org.anyline.data.transaction.TransactionState;

import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.sql.SQLTransientException;

public class TransactionProxy {
    /**
     * 启动事务
     * @param datasource 数据源
     * @param define 事务定义相关参数
     * @return status 回溯或提交时需要
     */
    public static TransactionState start(String datasource, TransactionDefine define) throws SQLException {
        TransactionManage manage = TransactionManage.instance(datasource);
        if(null == manage) {
            throw new NullPointerException("未创建相关数据源("+datasource+")事务管理器");
        }
        return manage.start(define);
    }

    /**
     * 启动事务
     * @param datasource 数据源
     * @return behavior 事务传播方式<br/>
     * 更多参数调用start(String datasource, TransactionDefine define)
     */
    public static TransactionState start(String datasource, int behavior) throws SQLException {
        TransactionManage manage = TransactionManage.instance(datasource);
        if(null == manage) {
            // 每个数据源都会注册默认的事务管理器，应该不会到这里
            throw new NullPointerException("未创建相关数据源("+datasource+")事务管理器");
        }
        return manage.start(behavior);
    }

    /**
     * 启动事务
     * 	 * @param datasource 数据源
     * 更多参数调用start(String datasource, TransactionDefine define)
     */
    public static TransactionState start(String datasource)  throws SQLException {
        return start(datasource, TransactionDefine.PROPAGATION_REQUIRED);
    }

    /**
     * 启动事务(默认数据源)
     * @param define 事务定义相关参数
     * @return status 回溯或提交时需要
     */
    public static TransactionState start(TransactionDefine define) throws SQLException {
        return start(RuntimeHolder.runtime().datasource(), define);
    }

    /**
     * 启动事务(默认数据源)
     * @param behavior 事务传播方式<br/>
     * 更多参数调用start(String datasource, TransactionDefine define)
     * @return status 回溯或提交时需要
     */
    public static TransactionState start(int behavior) throws SQLException {
        return start(RuntimeHolder.runtime().datasource(), behavior);
    }

    /**
     * 启动事务(默认数据源)
     * @return status 回溯或提交时需要
     */
    public static TransactionState start() throws SQLException {
        return start(RuntimeHolder.runtime().datasource());
    }

    /**
     * 提交事务 同时释放连接(发生异常时,不会释放连接)
     * @param state 启动事务时返回status
     * @throws Exception 发生异常时,不会释放连接
     */
    public static void commit(TransactionState state) throws SQLException {
        TransactionManage manage = TransactionManage.instance(state);
        if(null == manage) {
            throw new SQLTransientException("事务提交失败，不存在事务信息");
        }
        manage.commit(state);
    }

    /**
     * 回滚事务 同时释放连接(发生异常时,不会释放连接)
     * @param state 启动事务时返回status
     * @throws Exception 发生异常时,不会释放连接
     */
    public static void rollback(TransactionState state) throws SQLException {
        TransactionManage manage = TransactionManage.instance(state);
        if(null == manage) {
            throw new SQLTransactionRollbackException("事务回滚失败，不存在事务信息");
        }
        manage.rollback(state);
    }
}
