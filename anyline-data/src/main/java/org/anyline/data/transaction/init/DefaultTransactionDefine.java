/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.transaction.init;

import org.anyline.data.transaction.TransactionDefine;

public class DefaultTransactionDefine implements TransactionDefine {

    /** Prefix for the propagation constants defined in TransactionDefinition. */
    public static final String PREFIX_PROPAGATION = "PROPAGATION_";

    /** Prefix for the isolation constants defined in TransactionDefinition. */
    public static final String PREFIX_ISOLATION = "ISOLATION_";

    /** Prefix for transaction timeout values in description strings. */
    public static final String PREFIX_TIMEOUT = "timeout_";

    /** Marker for read-only transactions in description strings. */
    public static final String READ_ONLY_MARKER = "readOnly";

    private int propagationBehavior = PROPAGATION_REQUIRED;

    private int isolationLevel = ISOLATION_DEFAULT;

    private int timeout = TIMEOUT_DEFAULT;

    private boolean readOnly = false;

    private String name;
    private MODE mode = MODE.THREAD;

    /**
     * Create a new DefaultTransactionDefinition, with default settings.
     * Can be modified through bean property setters.
     * @see #setTimeout
     * @see #setReadOnly
     * @see #setName
     */
    public DefaultTransactionDefine() {
        this.name = System.currentTimeMillis()+"";
    }

    /**
     * Copy constructor. Definition can be modified through bean property setters.
     * @see #setTimeout
     * @see #setReadOnly
     * @see #setName
     */
    public DefaultTransactionDefine(TransactionDefine other) {
        this.propagationBehavior = other.getPropagationBehavior();
        this.isolationLevel = other.getIsolationLevel();
        this.timeout = other.getTimeout();
        this.readOnly = other.isReadOnly();
        this.name = other.getName();
    }

    /**
     * Create a new DefaultTransactionDefinition with the given
     * propagation behavior. Can be modified through bean property setters.
     * @param propagationBehavior one of the propagation constants in the
     * TransactionDefinition interface
     */
    public DefaultTransactionDefine(int propagationBehavior) {
        this.propagationBehavior = propagationBehavior;
    }

    @Override
    public final int getPropagationBehavior() {
        return this.propagationBehavior;
    }

    @Override
    public String getPropagationBehaviorName() {
        return null;
    }

    @Override
    public final int getIsolationLevel() {
        return this.isolationLevel;
    }

    /**
     * Set the timeout to apply, as number of seconds.
     * Default is TIMEOUT_DEFAULT (-1).
     * <p>Exclusively designed for use with {@link #PROPAGATION_REQUIRED} or
     * {@link #PROPAGATION_REQUIRES_NEW} since it only applies to newly started
     * transactions.
     * <p>Note that a transaction manager that does not support timeouts will throw
     * an exception when given any other timeout than {@link #TIMEOUT_DEFAULT}.
     * @see #TIMEOUT_DEFAULT
     */
    @Override
    public void setTimeout(int timeout) {
        if (timeout < TIMEOUT_DEFAULT) {
            throw new IllegalArgumentException("Timeout must be a positive integer or TIMEOUT_DEFAULT");
        }
        this.timeout = timeout;
    }

    @Override
    public final int getTimeout() {
        return this.timeout;
    }

    /**
     * Set whether to optimize as read-only transaction.
     * Default is "false".
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
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public final boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Set the name of this transaction. Default is none.
     * <p>This will be used as transaction name to be shown in a
     * transaction monitor, if applicable (for example, WebLogic's).
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public MODE getMode() {
        return mode;
    }

    @Override
    public void setMode(MODE mode) {
        this.mode = mode;
    }
}
