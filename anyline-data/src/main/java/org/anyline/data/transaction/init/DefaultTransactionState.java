package org.anyline.data.transaction.init;

import org.anyline.data.transaction.TransactionState;

public class DefaultTransactionState implements TransactionState {

    private Object origin;
    private boolean hasSavepoint = false;
    private boolean isNewTransaction = false;

    @Override
    public Object getOrigin() {
        return origin;
    }

    @Override
    public void setOrigin(Object origin) {
        this.origin = origin;
    }

    @Override
    public boolean hasSavepoint() {
        return hasSavepoint;
    }

    @Override
    public boolean isNewTransaction() {
        return isNewTransaction;
    }

    @Override
    public void setRollbackOnly() {

    }

    @Override
    public boolean isRollbackOnly() {
        return false;
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public Object createSavepoint() throws Exception {
        return null;
    }

    @Override
    public void rollbackToSavepoint(Object savepoint) throws Exception {

    }

    @Override
    public void releaseSavepoint(Object savepoint) throws Exception {

    }
}
