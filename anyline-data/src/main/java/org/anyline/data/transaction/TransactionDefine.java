package org.anyline.data.transaction;


public interface TransactionDefine {
    enum MODE{
        THREAD,         //线程内有效
        APPLICATION,    //应用内有效
        DISTRIBUTED     //分布式
    }
    /**
     * 如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务
     */
    int PROPAGATION_REQUIRED = 0;

    /**
     * 如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。
     */
    int PROPAGATION_SUPPORTS = 1;

    /**
     * 如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。
     */
    int PROPAGATION_MANDATORY = 2;

    /**
     * 创建一个新的事务，如果当前存在事务，则把当前事务挂起。
     */
    int PROPAGATION_REQUIRES_NEW = 3;

    /**
     * 以非事务方式运行，如果当前存在事务，则把当前事务挂起。
     */
    int PROPAGATION_NOT_SUPPORTED = 4;

    /**
     * 以非事务方式运行，如果当前存在事务，则抛出异常。
     */
    int PROPAGATION_NEVER = 5;

    /**
     * 如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于TransactionDefinition.PROPAGATION_REQUIRED。
     */
    int PROPAGATION_NESTED = 6;


    /**
     * Use the default isolation level of the underlying datastore.
     * All other levels correspond to the JDBC isolation levels.
     * @see java.sql.Connection
     */
    int ISOLATION_DEFAULT = -1;

    /**
     * Indicates that dirty reads, non-repeatable reads and phantom reads
     * can occur.
     * <p>This level allows a row changed by one transaction to be read by another
     * transaction before any changes in that row have been committed (a "dirty read").
     * If any of the changes are rolled back, the second transaction will have
     * retrieved an invalid row.
     * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
     */
    int ISOLATION_READ_UNCOMMITTED = 1;  // same as java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;

    /**
     * Indicates that dirty reads are prevented; non-repeatable reads and
     * phantom reads can occur.
     * <p>This level only prohibits a transaction from reading a row
     * with uncommitted changes in it.
     * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
     */
    int ISOLATION_READ_COMMITTED = 2;  // same as java.sql.Connection.TRANSACTION_READ_COMMITTED;

    /**
     * Indicates that dirty reads and non-repeatable reads are prevented;
     * phantom reads can occur.
     * <p>This level prohibits a transaction from reading a row with uncommitted changes
     * in it, and it also prohibits the situation where one transaction reads a row,
     * a second transaction alters the row, and the first transaction re-reads the row,
     * getting different values the second time (a "non-repeatable read").
     * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
     */
    int ISOLATION_REPEATABLE_READ = 4;  // same as java.sql.Connection.TRANSACTION_REPEATABLE_READ;

    /**
     * Indicates that dirty reads, non-repeatable reads and phantom reads
     * are prevented.
     * <p>This level includes the prohibitions in {@link #ISOLATION_REPEATABLE_READ}
     * and further prohibits the situation where one transaction reads all rows that
     * satisfy a {@code WHERE} condition, a second transaction inserts a row
     * that satisfies that {@code WHERE} condition, and the first transaction
     * re-reads for the same condition, retrieving the additional "phantom" row
     * in the second read.
     * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
     */
    int ISOLATION_SERIALIZABLE = 8;  // same as java.sql.Connection.TRANSACTION_SERIALIZABLE;


    /**
     * Use the default timeout of the underlying transaction system,
     * or none if timeouts are not supported.
     */
    int TIMEOUT_DEFAULT = -1;


    /**
     * Return the propagation behavior.
     * <p>Must return one of the {@code PROPAGATION_XXX} constants
     * <p>The default is {@link #PROPAGATION_REQUIRED}.
     * @return the propagation behavior
     * @see #PROPAGATION_REQUIRED
     */
    default int getPropagationBehavior() {
        return PROPAGATION_REQUIRED;
    }
    String getPropagationBehaviorName();

    /**
     * Return the isolation level.
     * <p>Must return one of the {@code ISOLATION_XXX} constants defined on
     * <p>Exclusively designed for use with {@link #PROPAGATION_REQUIRED} or
     * transactions. Consider switching the "validateExistingTransactions" flag to
     * "true" on your transaction manager if you'd like isolation level declarations
     * to get rejected when participating in an existing transaction with a different
     * isolation level.
     * <p>The default is {@link #ISOLATION_DEFAULT}. Note that a transaction manager
     * that does not support custom isolation levels will throw an exception when
     * given any other level than {@link #ISOLATION_DEFAULT}.
     * @return the isolation level
     */
    default int getIsolationLevel() {
        return ISOLATION_DEFAULT;
    }

    /**
     * Return the transaction timeout.
     * <p>Must return a number of seconds, or {@link #TIMEOUT_DEFAULT}.
     * <p>Exclusively designed for use with {@link #PROPAGATION_REQUIRED} or
     * {@link #PROPAGATION_REQUIRES_NEW} since it only applies to newly started
     * transactions.
     * <p>Note that a transaction manager that does not support timeouts will throw
     * an exception when given any other timeout than {@link #TIMEOUT_DEFAULT}.
     * <p>The default is {@link #TIMEOUT_DEFAULT}.
     * @return the transaction timeout
     */
    default int getTimeout() {
        return TIMEOUT_DEFAULT;
    }

    /**
     * Return whether to optimize as a read-only transaction.
     * <p>The read-only flag applies to any transaction context, whether backed
     * by an actual resource transaction ({@link #PROPAGATION_REQUIRED}/
     * {@link #PROPAGATION_REQUIRES_NEW}) or operating non-transactionally at
     * the resource level ({@link #PROPAGATION_SUPPORTS}). In the latter case,
     * the flag will only apply to managed resources within the application,
     * such as a Hibernate {@code Session}.
     * <p>This just serves as a hint for the actual transaction subsystem;
     * it will <i>not necessarily</i> cause failure of write access attempts.
     * A transaction manager which cannot interpret the read-only hint will
     * <i>not</i> throw an exception when asked for a read-only transaction.
     * @return {@code true} if the transaction is to be optimized as read-only
     * ({@code false} by default)
     */
    default boolean isReadOnly() {
        return false;
    }

    /**
     * Return the name of this transaction. Can be {@code null}.
     * <p>This will be used as the transaction name to be shown in a
     * transaction monitor, if applicable (for example, WebLogic's).
     * <p>In case of Spring's declarative transactions, the exposed name will be
     * the {@code fully-qualified class name + "." + method name} (by default).
     * @return the name of this transaction ({@code null} by default}
     */
    default String getName() {
        return null;
    }
    default MODE getMode(){
        return MODE.THREAD;
    }


}
