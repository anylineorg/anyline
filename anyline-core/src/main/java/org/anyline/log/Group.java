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

package org.anyline.log;

import java.util.ArrayList;
import java.util.List;

public class Group implements Log {
    private List<Log> logs = new ArrayList<>();
    private String name;
    private Class<?> clazz;

    public Group() {
    }
    public Group(Log log) {
        logs.add(log);
    }
    public Group add(Log log) {
        this.logs.add(log);
        return this;
    }
    @Override
    public String getName() {
        return name;
    }
    public Class<?> getClazz() {
        return clazz;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean isTraceEnabled() {
        boolean enable = false;
        for(Log log:logs) {
            enable = enable || log.isTraceEnabled();
        }
        return enable;
    }

    @Override
    public void trace(String msg) {
        for(Log log:logs) {
            log.trace(msg);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        for(Log log:logs) {
            log.trace(format, arg);
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        for(Log log:logs) {
            log.trace(format, arg1, arg2);
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        for(Log log:logs) {
            log.trace(format, arguments);
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        for(Log log:logs) {
            log.trace(msg, t);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        boolean enable = false;
        for(Log log:logs) {
            enable = enable || log.isDebugEnabled();
        }
        return enable;
    }

    @Override
    public void debug(String msg) {
        for(Log log:logs) {
            log.debug(msg);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        for(Log log:logs) {
            log.debug(format, arg);
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        for(Log log:logs) {
            log.debug(format, arg1, arg2);
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        for(Log log:logs) {
            log.debug(format, arguments);
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        for(Log log:logs) {
            log.debug(msg, t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        boolean enable = false;
        for(Log log:logs) {
            enable = enable || log.isInfoEnabled();
        }
        return enable;
    }

    @Override
    public void info(String msg) {
        for(Log log:logs) {
            log.info(msg);
        }
    }

    @Override
    public void info(String format, Object arg) {
        for(Log log:logs) {
            log.info(format, arg);
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        for(Log log:logs) {
            log.info(format, arg1, arg2);
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        for(Log log:logs) {
            log.info(format, arguments);
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        for(Log log:logs) {
            log.info(msg, t);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        boolean enable = false;
        for(Log log:logs) {
            enable = enable || log.isWarnEnabled();
        }
        return enable;
    }

    @Override
    public void warn(String msg) {
        for(Log log:logs) {
            log.warn(msg);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        for(Log log:logs) {
            log.warn(format, arg);
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        for(Log log:logs) {
            log.warn(format, arg1, arg2);
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        for(Log log:logs) {
            log.warn(format, arguments);
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        for(Log log:logs) {
            log.warn(msg, t);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        boolean enable = false;
        for(Log log:logs) {
            enable = enable || log.isErrorEnabled();
        }
        return enable;
    }

    @Override
    public void error(String msg) {
        for(Log log:logs) {
            log.error(msg);
        }
    }

    @Override
    public void error(String format, Object arg) {
        for(Log log:logs) {
            log.error(format, arg);
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        for(Log log:logs) {
            log.error(format, arg1, arg2);
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        for(Log log:logs) {
            log.error(format, arguments);
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        for(Log log:logs) {
            log.error(msg, t);
        }
    }
}
