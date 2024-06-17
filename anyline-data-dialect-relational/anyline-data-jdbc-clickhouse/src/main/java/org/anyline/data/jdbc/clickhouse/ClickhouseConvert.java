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



package org.anyline.data.jdbc.clickhouse;

import com.clickhouse.data.value.UnsignedByte;
import com.clickhouse.data.value.UnsignedLong;
import org.anyline.metadata.type.ConvertException;
import org.anyline.metadata.type.init.AbstractConvert;
import org.anyline.proxy.ConvertProxy;
import org.anyline.util.DateUtil;

import java.util.Date;

public class ClickhouseConvert {
    public static void reg() {
        ConvertProxy.reg(new AbstractConvert(UnsignedByte.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return ((UnsignedByte)value).doubleValue();
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertProxy.reg(new AbstractConvert(UnsignedByte.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return ((UnsignedByte)value).longValue();
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertProxy.reg(new AbstractConvert(UnsignedByte.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return ((UnsignedByte)value).intValue();
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertProxy.reg(new AbstractConvert(UnsignedByte.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return ((UnsignedByte)value).shortValue();
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertProxy.reg(new AbstractConvert(UnsignedByte.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return ((UnsignedByte)value).floatValue();
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertProxy.reg(new AbstractConvert(UnsignedLong.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return ((UnsignedLong)value).doubleValue();
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertProxy.reg(new AbstractConvert(UnsignedLong.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return ((UnsignedLong)value).longValue();
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertProxy.reg(new AbstractConvert(UnsignedLong.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return ((UnsignedLong)value).intValue();
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertProxy.reg(new AbstractConvert(UnsignedLong.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return ((UnsignedLong)value).shortValue();
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertProxy.reg(new AbstractConvert(UnsignedLong.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return ((UnsignedLong)value).floatValue();
                } catch (Exception e) {
                    return value;
                }
            }
        });
    }
}
