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

package org.anyline.log.init;

import org.anyline.log.Log;
import org.slf4j.Logger;

public class DefaultLog implements Log {
    private Logger log = null;
    public DefaultLog(Logger log) {
        this.log = log;
    }

    @Override
    public String getName() {
        return log.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        log.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        log.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        log.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        log.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        log.trace(msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        log.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        log.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        log.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        log.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        log.debug(msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        log.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        log.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        log.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        log.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        log.info(msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        log.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        log.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        log.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        log.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        log.warn(msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        log.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        log.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        log.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        log.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        log.error(msg, t);
    }
}
