package org.anyline.data.transaction;

public interface TransactionState {
    Object getOrigin();
    void setOrigin(Object origin);
    boolean hasSavepoint();
    boolean isNewTransaction();
    void setRollbackOnly();
    boolean isRollbackOnly();
    boolean isCompleted();
    Object createSavepoint() throws Exception;
    void rollbackToSavepoint(Object savepoint) throws Exception;
    void releaseSavepoint(Object savepoint) throws Exception;
}
