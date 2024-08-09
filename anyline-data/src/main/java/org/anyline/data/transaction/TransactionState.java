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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Savepoint;

public interface TransactionState {
    Object getOrigin();
    void setOrigin(Object origin);
    boolean hasSavepoint();
    Savepoint getPoint();
    void setPoint(Savepoint point);
    boolean isNewTransaction();
    void setRollbackOnly();
    boolean isRollbackOnly();
    boolean isCompleted();
    Object createSavepoint() throws Exception;
    void rollbackToSavepoint(Object savepoint) throws Exception;
    void releaseSavepoint(Object savepoint) throws Exception;
    void setDataSource(DataSource datasource);
    DataSource getDataSource();
    void setConnection(Connection connection);
    Connection getConnection();
    default String getName() {
        return null;
    }
    default void setName(String name) {}
    default TransactionDefine.MODE getMode() {
        return TransactionDefine.MODE.THREAD;
    }
    void setMode(TransactionDefine.MODE mode);
}
