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

import org.anyline.adapter.init.JavaTypeAdapter;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.geometry.Point;
import org.anyline.metadata.type.Convert;
import org.anyline.metadata.type.ConvertException;
import org.anyline.metadata.type.DataType;
import org.anyline.metadata.type.init.AbstractConvert;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.*;

public class ConvertProxy {

    private static final Logger log = LoggerFactory.getLogger(ConvertProxy.class);
    public static Map<Class, Map<Class, Convert>> converts = new Hashtable();    //DATE > {STRING>SrintCovert, TIME>timeConvert}
    public ConvertProxy() {}

    public static void reg(Convert convert) {
        Class origin = convert.getOrigin(); //源类型
        Class target = convert.getTarget(); //目标类型
        Map<Class, Convert> map = converts.computeIfAbsent(origin, k -> new Hashtable<>());
        map.put(target, convert);

        DataType type = JavaTypeAdapter.types.get(origin);
        if(null != type) {
            type.convert(convert);
        }
    }

    /**
     *
     * @param origin 原类
     * @param target 目标类
     * @return Convert
     */
    public static Convert getConvert(Class origin, Class target) {
        Map<Class, Convert> map = converts.get(origin);
        if(null != map) {
            return map.get(target);
        }
        return null;
    }

    public static Object convert(Object value, Class target, boolean array) {
        return convert(value, target, array, null);
    }

    public static Object convert(Object value, Class target, boolean array, Object def) {
        return convert(value, target, array, def, true);
    }

    /**
     *
     * @param value 值
     * @param target 目标类型
     * @param array 目标类型是否是数组
     * @param def 默认值
     * @param warn 是否异常提示
     * @return Object
     */
    public static <T> Object convert(Object value, Class<T> target, boolean array, Object def, boolean warn) {
        if(null == value) {
            value = def;
        }
        Object result = value;
        if(null != result && null != target) {
            Class<? extends Object> clazz = result.getClass();
            if(ClassUtil.isInSub(clazz, target)) {
                return result;
            }
            //Map转换成Entity
            if(result instanceof Map && ClassUtil.isWrapClass(target) && target != String.class) {
                if(result instanceof DataRow) {
                    result = ((DataRow)result).entity(target);
                }else {
                    result = BeanUtil.map2object((Map) result, target);
                }
                return result;
            }
            //数组
            if(array) {
                if(value instanceof Collection || value.getClass().isArray()) {
                    List<Object> list = BeanUtil.list(value);
                    int size = list.size();
                    T[] arrays = (T[])Array.newInstance(target, size);
                    int idx = 0;
                    for(Object item:list) {
                        arrays[idx++] = (T)convert(item, target, false, def, warn);
                    }
                    return arrays;
                }
            }
            //转换成String类型
            if(target == String.class) {
                if(value instanceof Collection) {
                    //集合<基础类型> > String
                    Collection col = (Collection) value;
                    Class component = ClassUtil.getComponentClass(value);
                    if(ClassUtil.isPrimitiveClass(component) || component == String.class) {
                        if("concat".equalsIgnoreCase(ConfigTable.LIST2STRING_FORMAT)) {
                            return BeanUtil.concat(col);
                        }else if("json".equalsIgnoreCase(ConfigTable.LIST2STRING_FORMAT)) {
                            return BeanUtil.object2json(col);
                        }
                    }
                }else if(value.getClass().isArray()) {
                    //数组[基础类型] > String
                    Class component = ClassUtil.getComponentClass(value);
                    Object[] list = null;
                    if(component == String.class) {
                       list = (String[]) value;
                    }else if(component == int.class) {
                        list = BeanUtil.int2Integer((int[])value);
                    }else if(component == double.class) {
                        list = BeanUtil.double2Double((double[])value);
                    }else if(component == long.class) {
                        list = BeanUtil.long2Long((long[])value);
                    }else if(component == float.class) {
                        list = BeanUtil.float2Float((float[])value);
                    }
                    if(null != list) {
                        if ("concat".equalsIgnoreCase(ConfigTable.LIST2STRING_FORMAT)) {
                            return BeanUtil.concat(list);
                        } else if ("json".equalsIgnoreCase(ConfigTable.LIST2STRING_FORMAT)) {
                            return BeanUtil.object2json(list);
                        }
                    }
                }else if(value instanceof Map) {
                    Map map = (Map)value;
                    //return BeanUtil.map2json(map);
                    return BeanUtil.map2object(map,  target);
                }else if(ClassUtil.isPrimitiveClass(value.getClass()) || value instanceof String) {
                    return value.toString();
                }
            }
            boolean success = false;

            //根据注册的Convert类型转换
            Map<Class, Convert> map = converts.get(clazz);
            Convert convert = null;
            if(null != map) {
                convert = map.get(target);
                if(null != convert) {
                    try {
                        result = convert.exe(value, def);
                        success = true;
                    }catch (ConvertException e) {
                        //TODO 根据异常信息 决定下一行
                        if(ConfigTable.IS_LOG_CONVERT_EXCEPTION) {
                            e.printStackTrace();
                        }
                    }
                }else if(target == String.class) {
                    result = value.toString();
                    success = true;
                }
            }
            if(!success) {
                try{
                    result = target.cast(value);
                    success = true;
                }catch (Exception e) {
                    if(ConfigTable.IS_LOG_CONVERT_EXCEPTION) {
                        e.printStackTrace();
                    }
                }
            }
            if(!success && warn) {
                log.warn("[{}][origin:{}][target:{}][value:{}]", LogUtil.format("convert定位失败", 31), ClassUtil.type(clazz), ClassUtil.type(target), value);
                if(ConfigTable.IS_THROW_CONVERT_EXCEPTION) {
                    throw new RuntimeException("类型转换异常");
                }
            }
        }
        return result;
    }
    static {
        //内置转换器
        //枚举太慢
        /*Convert[] array = DefaultConvert.values();
        for (Convert convert : array) {
            reg(convert);
        }*/
        reg(new AbstractConvert(String.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return BasicUtil.parseBoolean(value);
                }catch (Exception e) {
                    return value;
                }
            }
        });
        reg(new AbstractConvert(String.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return BasicUtil.parseBoolean(value).booleanValue();
                }catch (Exception e) {
                    return value;
                }
            }
        });
        reg(new AbstractConvert(String.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    return BasicUtil.parseDecimal(value, null);
                }catch (Exception e) {
                    return value;
                }
            }
        });
        reg(new AbstractConvert(String.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf(BasicUtil.parseLong(value, null));
            }
        });
        reg(new AbstractConvert(String.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseInt(value, null);
            }
        });
        reg(new AbstractConvert(String.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseInt(value, null).intValue();
            }
        });
        reg(new AbstractConvert(String.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseLong(value, null);
            }
        });
        reg(new AbstractConvert(String.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseLong(value, null).longValue();
            }
        });
        reg(new AbstractConvert(String.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseDouble(value, null);
            }
        });
        reg(new AbstractConvert(String.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseDouble(value, null).doubleValue();
            }
        });
        reg(new AbstractConvert(String.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseFloat(value, null);
            }
        });
        reg(new AbstractConvert(String.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseFloat(value, null).floatValue();
            }
        });
        reg(new AbstractConvert(String.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseShort(value, null);
            }
        });
        reg(new AbstractConvert(String.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseShort(value, null).shortValue();
            }
        });
        reg(new AbstractConvert(String.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseByte(value, null);
            }
        });
        reg(new AbstractConvert(String.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BasicUtil.parseByte(value, null).byteValue();
            }
        });
        reg(new AbstractConvert(String.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(value.toString().toCharArray()[0]);
            }
        });
        reg(new AbstractConvert(String.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString().toCharArray()[0];
            }
        });
        reg(new AbstractConvert(Boolean.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(Boolean.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Boolean)value).booleanValue();
            }
        });
        reg(new AbstractConvert(Boolean.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return BigDecimal.ONE;
                }else{
                    return BigDecimal.ZERO;
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return BigInteger.ONE;
                }else{
                    return BigInteger.ZERO;
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return Integer.valueOf(1);
                }else{
                    return Integer.valueOf(0);
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return 1;
                }else{
                    return 0;
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return Long.valueOf(1);
                }else{
                    return Long.valueOf(0);
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return 1L;
                }else{
                    return 0L;
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return Double.valueOf(1);
                }else{
                    return Double.valueOf(0);
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return 1D;
                }else{
                    return 0D;
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return Float.valueOf(1);
                }else{
                    return Float.valueOf(0);
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return 1F;
                }else{
                    return 0F;
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return Short.valueOf("1");
                }else{
                    return Short.valueOf("0");
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return Short.valueOf("1").shortValue();
                }else{
                    return Short.valueOf("0").shortValue();
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return Byte.valueOf("1");
                }else{
                    return Byte.valueOf("0");
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return (byte)1;
                }else{
                    return (byte)0;
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return Character.valueOf('1');
                }else{
                    return Character.valueOf('0');
                }
            }
        });
        reg(new AbstractConvert(Boolean.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Boolean)value) {
                    return '1';
                }else{
                    return '0';
                }
            }
        });
        reg(new AbstractConvert(boolean.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(boolean.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Boolean.valueOf((boolean)value);
            }
        });
        reg(new AbstractConvert(boolean.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return BigDecimal.ONE;
                }else{
                    return BigDecimal.ZERO;
                }
            }
        });
        reg(new AbstractConvert(boolean.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return BigInteger.ONE;
                }else{
                    return BigInteger.ZERO;
                }
            }
        });
        reg(new AbstractConvert(boolean.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return Integer.valueOf(1);
                }else{
                    return Integer.valueOf(0);
                }
            }
        });
        reg(new AbstractConvert(boolean.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return 1;
                }else{
                    return 0;
                }
            }
        });
        reg(new AbstractConvert(boolean.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return Long.valueOf(1);
                }else{
                    return Long.valueOf(0);
                }
            }
        });
        reg(new AbstractConvert(boolean.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return 1L;
                }else{
                    return 0L;
                }
            }
        });
        reg(new AbstractConvert(boolean.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return Double.valueOf(1);
                }else{
                    return Double.valueOf(0);
                }
            }
        });
        reg(new AbstractConvert(boolean.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return 1D;
                }else{
                    return 0D;
                }
            }
        });
        reg(new AbstractConvert(boolean.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return Float.valueOf(1);
                }else{
                    return Float.valueOf(0);
                }
            }
        });
        reg(new AbstractConvert(boolean.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return 1F;
                }else{
                    return 0F;
                }
            }
        });
        reg(new AbstractConvert(boolean.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return Short.valueOf("1");
                }else{
                    return Short.valueOf("0");
                }
            }
        });
        reg(new AbstractConvert(boolean.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return (short)1;
                }else{
                    return (short)0;
                }
            }
        });
        reg(new AbstractConvert(boolean.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return Byte.valueOf("1");
                }else{
                    return Byte.valueOf("0");
                }
            }
        });
        reg(new AbstractConvert(boolean.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return (byte)1;
                }else{
                    return (byte)0;
                }
            }
        });
        reg(new AbstractConvert(boolean.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return Character.valueOf('1');
                }else{
                    return Character.valueOf('0');
                }
            }
        });
        reg(new AbstractConvert(boolean.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((boolean)value) {
                    return '1';
                }else{
                    return '0';
                }
            }
        });
        reg(new AbstractConvert(BigDecimal.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(BigDecimal.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(value.equals("0")) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(BigDecimal.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(value.equals("0")) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(BigDecimal.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigDecimal)value).toBigInteger();
            }
        });
        reg(new AbstractConvert(BigDecimal.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf(((BigDecimal)value).intValue());
            }
        });
        reg(new AbstractConvert(BigDecimal.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigDecimal)value).intValue();
            }
        });
        reg(new AbstractConvert(BigDecimal.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(((BigDecimal)value).longValue());
            }
        });
        reg(new AbstractConvert(BigDecimal.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigDecimal)value).longValue();
            }
        });
        reg(new AbstractConvert(BigDecimal.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(((BigDecimal)value).doubleValue());
            }
        });
        reg(new AbstractConvert(BigDecimal.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigDecimal)value).doubleValue();
            }
        });
        reg(new AbstractConvert(BigDecimal.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf(((BigDecimal)value).floatValue());
            }
        });
        reg(new AbstractConvert(BigDecimal.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigDecimal)value).floatValue();
            }
        });
        reg(new AbstractConvert(BigDecimal.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(((BigDecimal)value).shortValue());
            }
        });
        reg(new AbstractConvert(BigDecimal.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigDecimal)value).shortValue();
            }
        });
        reg(new AbstractConvert(BigDecimal.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(((BigDecimal)value).byteValue());
            }
        });
        reg(new AbstractConvert(BigDecimal.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigDecimal)value).byteValue();
            }
        });
        reg(new AbstractConvert(BigDecimal.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(((BigDecimal)value).intValue(), 10));
            }
        });
        reg(new AbstractConvert(BigDecimal.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(((BigDecimal)value).intValue(), 10);
            }
        });
        reg(new AbstractConvert(BigInteger.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(BigInteger.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(value.equals("0")) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(BigInteger.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(value.equals("0")) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(BigInteger.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return new BigDecimal((BigInteger)value, 0);
            }
        });
        reg(new AbstractConvert(BigInteger.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf(((BigInteger)value).intValue());
            }
        });
        reg(new AbstractConvert(BigInteger.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigInteger)value).intValue();
            }
        });
        reg(new AbstractConvert(BigInteger.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(((BigInteger)value).longValue());
            }
        });
        reg(new AbstractConvert(BigInteger.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigInteger)value).longValue();
            }
        });
        reg(new AbstractConvert(BigInteger.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(((BigInteger)value).doubleValue());
            }
        });
        reg(new AbstractConvert(BigInteger.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigInteger)value).doubleValue();
            }
        });
        reg(new AbstractConvert(BigInteger.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf(((BigInteger)value).floatValue());
            }
        });
        reg(new AbstractConvert(BigInteger.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigInteger)value).floatValue();
            }
        });
        reg(new AbstractConvert(BigInteger.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(((BigInteger)value).shortValue());
            }
        });
        reg(new AbstractConvert(BigInteger.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigInteger)value).shortValue();
            }
        });
        reg(new AbstractConvert(BigInteger.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(((BigInteger)value).byteValue());
            }
        });
        reg(new AbstractConvert(BigInteger.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((BigInteger)value).byteValue();
            }
        });
        reg(new AbstractConvert(BigInteger.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(((BigInteger)value).intValue(), 10));
            }
        });
        reg(new AbstractConvert(BigInteger.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(((BigInteger)value).intValue(), 10);
            }
        });
        reg(new AbstractConvert(Integer.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(Integer.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if("0".equals(value)) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(Integer.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if("0".equals(value)) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(Integer.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return new BigDecimal((Integer)value);
            }
        });
        reg(new AbstractConvert(Integer.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf((Integer)value);
            }
        });
        reg(new AbstractConvert(Integer.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Integer)value).intValue();
            }
        });
        reg(new AbstractConvert(Integer.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(((Integer)value).longValue());
            }
        });
        reg(new AbstractConvert(Integer.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Integer)value).longValue();
            }
        });
        reg(new AbstractConvert(Integer.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(((Integer)value).doubleValue());
            }
        });
        reg(new AbstractConvert(Integer.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Integer)value).doubleValue();
            }
        });
        reg(new AbstractConvert(Integer.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf(((Integer)value).floatValue());
            }
        });
        reg(new AbstractConvert(Integer.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Integer)value).floatValue();
            }
        });
        reg(new AbstractConvert(Integer.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(((Integer)value).shortValue());
            }
        });
        reg(new AbstractConvert(Integer.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Integer)value).shortValue();
            }
        });
        reg(new AbstractConvert(Integer.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(((Integer)value).byteValue());
            }
        });
        reg(new AbstractConvert(Integer.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Integer)value).byteValue();
            }
        });
        reg(new AbstractConvert(Integer.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(((Integer)value).intValue(), 10));
            }
        });
        reg(new AbstractConvert(Integer.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(((Integer)value).intValue(), 10);
            }
        });
        reg(new AbstractConvert(int.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(int.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if("0".equals(value)) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(int.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if("0".equals(value)) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(int.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((int)value);
            }
        });
        reg(new AbstractConvert(int.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf((int)value);
            }
        });
        reg(new AbstractConvert(int.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((int)value);
            }
        });
        reg(new AbstractConvert(int.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(((int)value));
            }
        });
        reg(new AbstractConvert(int.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(((int)value)).longValue();
            }
        });
        reg(new AbstractConvert(int.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(((int)value));
            }
        });
        reg(new AbstractConvert(int.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(((int)value)).doubleValue();
            }
        });
        reg(new AbstractConvert(int.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((int)value);
            }
        });
        reg(new AbstractConvert(int.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((int)value).floatValue();
            }
        });
        reg(new AbstractConvert(int.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(Integer.valueOf((int)value).shortValue());
            }
        });
        reg(new AbstractConvert(int.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((int)value).shortValue();
            }
        });
        reg(new AbstractConvert(int.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(Integer.valueOf((int)value).byteValue());
            }
        });
        reg(new AbstractConvert(int.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((int)value).byteValue();
            }
        });
        reg(new AbstractConvert(int.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(((int)value), 10));
            }
        });
        reg(new AbstractConvert(int.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(((int)value), 10);
            }
        });
        reg(new AbstractConvert(Long.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(Long.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if("0".equals(value)) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(Long.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if("0".equals(value)) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(Long.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((Long)value);
            }
        });
        reg(new AbstractConvert(Long.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf((Long)value);
            }
        });
        reg(new AbstractConvert(Long.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf(((Long)value).intValue());
            }
        });
        reg(new AbstractConvert(Long.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Long)value).intValue();
            }
        });
        reg(new AbstractConvert(Long.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Long)value).longValue();
            }
        });
        reg(new AbstractConvert(Long.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(((Long)value).doubleValue());
            }
        });
        reg(new AbstractConvert(Long.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Long)value).doubleValue();
            }
        });
        reg(new AbstractConvert(Long.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf(((Long)value).floatValue());
            }
        });
        reg(new AbstractConvert(Long.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Long)value).floatValue();
            }
        });
        reg(new AbstractConvert(Long.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(((Long)value).shortValue());
            }
        });
        reg(new AbstractConvert(Long.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Long)value).shortValue();
            }
        });
        reg(new AbstractConvert(Long.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(((Long)value).byteValue());
            }
        });
        reg(new AbstractConvert(Long.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Long)value).byteValue();
            }
        });
        reg(new AbstractConvert(Long.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(((Long)value).intValue(), 10));
            }
        });
        reg(new AbstractConvert(Long.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(((Long)value).intValue(), 10);
            }
        });
        reg(new AbstractConvert(long.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(long.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if("0".equals(value)) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(long.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if("0".equals(value)) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(long.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((long)value);
            }
        });
        reg(new AbstractConvert(long.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf((long)value);
            }
        });
        reg(new AbstractConvert(long.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((Long.valueOf((long)value).intValue()));
            }
        });
        reg(new AbstractConvert(long.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return (Long.valueOf((long)value)).intValue();
            }
        });
        reg(new AbstractConvert(long.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((long)value);
            }
        });
        reg(new AbstractConvert(long.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((long)value);
            }
        });
        reg(new AbstractConvert(long.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((long)value).doubleValue();
            }
        });
        reg(new AbstractConvert(long.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((long)value);
            }
        });
        reg(new AbstractConvert(long.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((long)value).floatValue();
            }
        });
        reg(new AbstractConvert(long.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(Long.valueOf((long)value).shortValue());
            }
        });
        reg(new AbstractConvert(long.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((long)value).shortValue();
            }
        });
        reg(new AbstractConvert(long.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(Long.valueOf((long)value).byteValue());
            }
        });
        reg(new AbstractConvert(long.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((long)value).byteValue();
            }
        });
        reg(new AbstractConvert(long.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(Long.valueOf((long)value).intValue(), 10));
            }
        });
        reg(new AbstractConvert(long.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(Long.valueOf((long)value).intValue(), 10);
            }
        });
        reg(new AbstractConvert(Double.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(Double.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Double)value == 0) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(Double.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Double)value == 0) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(Double.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((Double)value);
            }
        });
        reg(new AbstractConvert(Double.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf(((Double)value).longValue());
            }
        });
        reg(new AbstractConvert(Double.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf(((Double)value).intValue());
            }
        });
        reg(new AbstractConvert(Double.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Double)value).intValue();
            }
        });
        reg(new AbstractConvert(Double.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(((Double)value).longValue());
            }
        });
        reg(new AbstractConvert(Double.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Double)value).longValue();
            }
        });
        reg(new AbstractConvert(Double.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Double)value).doubleValue();
            }
        });
        reg(new AbstractConvert(Double.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf(value.toString());
            }
        });
        reg(new AbstractConvert(Double.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf(value.toString()).floatValue();
            }
        });
        reg(new AbstractConvert(Double.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(((Double)value).shortValue());
            }
        });
        reg(new AbstractConvert(Double.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Double)value).shortValue();
            }
        });
        reg(new AbstractConvert(Double.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(((Double)value).byteValue());
            }
        });
        reg(new AbstractConvert(Double.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Double)value).byteValue();
            }
        });
        reg(new AbstractConvert(Double.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(((Double)value).intValue(), 10));
            }
        });
        reg(new AbstractConvert(Double.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(((Double)value).intValue(), 10);
            }
        });
        reg(new AbstractConvert(double.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(double.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((double)value == 0) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(double.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((double)value == 0) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(double.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((double)value);
            }
        });
        reg(new AbstractConvert(double.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf(Double.valueOf((double)value).longValue());
            }
        });
        reg(new AbstractConvert(double.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf(Double.valueOf((double)value).intValue());
            }
        });
        reg(new AbstractConvert(double.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((double)value).intValue();
            }
        });
        reg(new AbstractConvert(double.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(Double.valueOf((double)value).longValue());
            }
        });
        reg(new AbstractConvert(double.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((double)value).longValue();
            }
        });
        reg(new AbstractConvert(double.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((double)value);
            }
        });
        reg(new AbstractConvert(double.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf(value.toString()).floatValue();
            }
        });
        reg(new AbstractConvert(double.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(value.toString()).floatValue();
            }
        });
        reg(new AbstractConvert(double.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(Double.valueOf(value.toString()).shortValue());
            }
        });
        reg(new AbstractConvert(double.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((double)value).shortValue();
            }
        });
        reg(new AbstractConvert(double.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(Double.valueOf(value.toString()).byteValue());
            }
        });
        reg(new AbstractConvert(double.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(value.toString()).byteValue();
            }
        });
        reg(new AbstractConvert(double.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(((Double)value).intValue(), 10));
            }
        });
        reg(new AbstractConvert(double.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(((Double)value).intValue(), 10);
            }
        });
        reg(new AbstractConvert(Float.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(Float.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Float)value == 0) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(Float.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((Float)value == 0) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(Float.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((Float)value);
            }
        });
        reg(new AbstractConvert(Float.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf(((Float)value).longValue());
            }
        });
        reg(new AbstractConvert(Float.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf(((Float)value).intValue());
            }
        });
        reg(new AbstractConvert(Float.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Float)value).intValue();
            }
        });
        reg(new AbstractConvert(Float.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(((Float)value).longValue());
            }
        });
        reg(new AbstractConvert(Float.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Float)value).longValue();
            }
        });
        reg(new AbstractConvert(Float.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(value.toString()).doubleValue();
            }
        });
        reg(new AbstractConvert(Float.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(value.toString()).doubleValue();
            }
        });
        reg(new AbstractConvert(Float.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Float)value).floatValue();
            }
        });
        reg(new AbstractConvert(Float.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(((Float)value).shortValue());
            }
        });
        reg(new AbstractConvert(Float.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Float)value).shortValue();
            }
        });
        reg(new AbstractConvert(Float.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(((Float)value).byteValue());
            }
        });
        reg(new AbstractConvert(Float.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Float)value).byteValue();
            }
        });
        reg(new AbstractConvert(Float.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(((Float)value).intValue(), 10));
            }
        });
        reg(new AbstractConvert(Float.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(((Float)value).intValue(), 10);
            }
        });
        reg(new AbstractConvert(float.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(float.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((float)value == 0) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(float.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if((float)value == 0) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(float.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((float)value);
            }
        });
        reg(new AbstractConvert(float.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf(Float.valueOf((float)value).longValue());
            }
        });
        reg(new AbstractConvert(float.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf(Float.valueOf((float)value).intValue());
            }
        });
        reg(new AbstractConvert(float.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((float)value).intValue();
            }
        });
        reg(new AbstractConvert(float.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(Float.valueOf((float)value).longValue());
            }
        });
        reg(new AbstractConvert(float.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((float)value).longValue();
            }
        });
        reg(new AbstractConvert(float.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(Float.valueOf((float)value).doubleValue());
            }
        });
        reg(new AbstractConvert(float.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf(value.toString()).doubleValue();
            }
        });
        reg(new AbstractConvert(float.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((float)value);
            }
        });
        reg(new AbstractConvert(float.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(Float.valueOf((float)value).shortValue());
            }
        });
        reg(new AbstractConvert(float.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((float)value).shortValue();
            }
        });
        reg(new AbstractConvert(float.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(Float.valueOf((float)value).byteValue());
            }
        });
        reg(new AbstractConvert(float.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((float)value).byteValue();
            }
        });
        reg(new AbstractConvert(float.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(Float.valueOf((float)value).intValue(), 10));
            }
        });
        reg(new AbstractConvert(float.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(Float.valueOf((float)value).intValue(), 10);
            }
        });
        reg(new AbstractConvert(Short.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
            reg(new AbstractConvert(UUID.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(Short.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(0 == (Short)value) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(Short.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(0 == (Short)value) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(Short.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((Short)value);
            }
        });
        reg(new AbstractConvert(Short.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf((Short)value);
            }
        });
        reg(new AbstractConvert(Short.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((Short)value);
            }
        });
        reg(new AbstractConvert(Short.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Short)value).intValue();
            }
        });
        reg(new AbstractConvert(Short.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(((Short)value).longValue());
            }
        });
        reg(new AbstractConvert(Short.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Short)value).longValue();
            }
        });
        reg(new AbstractConvert(Short.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf(((Short)value).doubleValue());
            }
        });
        reg(new AbstractConvert(Short.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Short)value).doubleValue();
            }
        });
        reg(new AbstractConvert(Short.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf(((Short)value).floatValue());
            }
        });
        reg(new AbstractConvert(Short.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Short)value).floatValue();
            }
        });
        reg(new AbstractConvert(Short.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(((Short)value).shortValue());
            }
        });
        reg(new AbstractConvert(Short.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(((Short)value).byteValue());
            }
        });
        reg(new AbstractConvert(Short.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Short)value).byteValue();
            }
        });
        reg(new AbstractConvert(Short.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(((Short)value).intValue(), 10));
            }
        });
        reg(new AbstractConvert(Short.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(((Short)value).intValue(), 10);
            }
        });
        reg(new AbstractConvert(short.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(short.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if (0 == (short)value) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(short.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if (0 == (short)value) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(short.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((short)value);
            }
        });
        reg(new AbstractConvert(short.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf((short)value);
            }
        });
        reg(new AbstractConvert(short.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((short)value);
            }
        });
        reg(new AbstractConvert(short.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((short)value).intValue();
            }
        });
        reg(new AbstractConvert(short.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((short)value);
            }
        });
        reg(new AbstractConvert(short.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((short)value).longValue();
            }
        });
        reg(new AbstractConvert(short.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((short)value);
            }
        });
        reg(new AbstractConvert(short.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((short)value).doubleValue();
            }
        });
        reg(new AbstractConvert(short.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((short)value);
            }
        });
        reg(new AbstractConvert(short.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((short)value).floatValue();
            }
        });
        reg(new AbstractConvert(short.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf((short)value);
            }
        });
        reg(new AbstractConvert(short.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(((Short)value).byteValue());
            }
        });
        reg(new AbstractConvert(short.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Short)value).byteValue();
            }
        });
        reg(new AbstractConvert(short.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit(((short)value), 10));
            }
        });
        reg(new AbstractConvert(short.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit(((short)value), 10);
            }
        });
        reg(new AbstractConvert(Byte.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(Byte.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(0 == (Byte)value) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(Byte.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(0 == (Byte)value) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(Byte.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((Byte)value);
            }
        });
        reg(new AbstractConvert(Byte.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf((Byte)value);
            }
        });
        reg(new AbstractConvert(Byte.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((Byte)value);
            }
        });
        reg(new AbstractConvert(Byte.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Byte)value).intValue();
            }
        });
        reg(new AbstractConvert(Byte.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((Byte)value);
            }
        });
        reg(new AbstractConvert(Byte.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Byte)value).longValue();
            }
        });
        reg(new AbstractConvert(Byte.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((Byte)value);
            }
        });
        reg(new AbstractConvert(Byte.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Byte)value).doubleValue();
            }
        });
        reg(new AbstractConvert(Byte.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((Byte)value);
            }
        });
        reg(new AbstractConvert(Byte.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Byte)value).floatValue();
            }
        });
        reg(new AbstractConvert(Byte.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf((Byte)value);
            }
        });
        reg(new AbstractConvert(Byte.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Byte)value).shortValue();
            }
        });
        reg(new AbstractConvert(Byte.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Byte)value).byteValue();
            }
        });
        reg(new AbstractConvert(Byte.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit((Byte)value, 10));
            }
        });
        reg(new AbstractConvert(Byte.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit((Byte)value, 10);
            }
        });
        reg(new AbstractConvert(byte.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(byte.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(0 == (byte)value) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(byte.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(0 == (byte)value) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(byte.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((byte)value);
            }
        });
        reg(new AbstractConvert(byte.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf((byte)value);
            }
        });
        reg(new AbstractConvert(byte.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((byte)value);
            }
        });
        reg(new AbstractConvert(byte.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((byte)value).intValue();
            }
        });
        reg(new AbstractConvert(byte.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((byte)value);
            }
        });
        reg(new AbstractConvert(byte.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((byte)value).longValue();
            }
        });
        reg(new AbstractConvert(byte.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((byte)value);
            }
        });
        reg(new AbstractConvert(byte.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((byte)value).doubleValue();
            }
        });
        reg(new AbstractConvert(byte.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((byte)value);
            }
        });
        reg(new AbstractConvert(byte.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((byte)value).floatValue();
            }
        });
        reg(new AbstractConvert(byte.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf((byte)value);
            }
        });
        reg(new AbstractConvert(byte.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf((byte)value).shortValue();
            }
        });
        reg(new AbstractConvert(byte.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf((byte)value);
            }
        });
        reg(new AbstractConvert(byte.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf(Character.forDigit((byte)value, 10));
            }
        });
        reg(new AbstractConvert(byte.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.forDigit((byte)value, 10);
            }
        });
        reg(new AbstractConvert(Character.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(Character.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(0 == (Character)value) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(Character.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(0 == (Character)value) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(Character.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((Character)value);
            }
        });
        reg(new AbstractConvert(Character.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigInteger.valueOf((Character)value);
            }
        });
        reg(new AbstractConvert(Character.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((Character)value);
            }
        });
        reg(new AbstractConvert(Character.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((Character)value).intValue();
            }
        });
        reg(new AbstractConvert(Character.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((Character)value);
            }
        });
        reg(new AbstractConvert(Character.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((Character)value).longValue();
            }
        });
        reg(new AbstractConvert(Character.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((Character)value);
            }
        });
        reg(new AbstractConvert(Character.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((Character)value).doubleValue();
            }
        });
        reg(new AbstractConvert(Character.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((Character)value);
            }
        });
        reg(new AbstractConvert(Character.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((Character)value).floatValue();
            }
        });
        reg(new AbstractConvert(Character.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(Integer.valueOf((Character)value).shortValue());
            }
        });
        reg(new AbstractConvert(Character.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((Character)value).shortValue();
            }
        });
        reg(new AbstractConvert(Character.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(Integer.valueOf((Character)value).byteValue());
            }
        });
        reg(new AbstractConvert(Character.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((Character)value).byteValue();
            }
        });
        reg(new AbstractConvert(Character.class, char.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((Character)value).charValue();
            }
        });
        reg(new AbstractConvert(char.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(char.class, Boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(0 == (char)value) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        reg(new AbstractConvert(char.class, boolean.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(0 == (char)value) {
                    return false;
                }
                return true;
            }
        });
        reg(new AbstractConvert(char.class, BigDecimal.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((char)value);
            }
        });
        reg(new AbstractConvert(char.class, BigInteger.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return BigDecimal.valueOf((char)value);
            }
        });
        reg(new AbstractConvert(char.class, Integer.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((char)value);
            }
        });
        reg(new AbstractConvert(char.class, int.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((char)value).intValue();
            }
        });
        reg(new AbstractConvert(char.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((char)value);
            }
        });
        reg(new AbstractConvert(char.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf((char)value).longValue();
            }
        });
        reg(new AbstractConvert(char.class, Double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((char)value);
            }
        });
        reg(new AbstractConvert(char.class, double.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Double.valueOf((char)value).doubleValue();
            }
        });
        reg(new AbstractConvert(char.class, Float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((char)value);
            }
        });
        reg(new AbstractConvert(char.class, float.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Float.valueOf((char)value).floatValue();
            }
        });
        reg(new AbstractConvert(char.class, Short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Short.valueOf(Integer.valueOf((char)value).shortValue());
            }
        });
        reg(new AbstractConvert(char.class, short.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((char)value).shortValue();
            }
        });
        reg(new AbstractConvert(char.class, Byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Byte.valueOf(Integer.valueOf((char)value).byteValue());
            }
        });
        reg(new AbstractConvert(char.class, byte.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Integer.valueOf((char)value).byteValue();
            }
        });
        reg(new AbstractConvert(char.class, Character.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Character.valueOf((char)value);
            }
        });

        /* *****************************************************************************************************************
         *                                                  date
         * *****************************************************************************************************************/
        reg(new AbstractConvert(String.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value);
            }
        });
        reg(new AbstractConvert(String.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(String.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(String.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTimestamp(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(String.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                Year year = Year.of(DateUtil.year(date));
                return year;
            }
        });
        reg(new AbstractConvert(String.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
                return yearMonth;
            }
        });
        reg(new AbstractConvert(String.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                Month month = Month.of(DateUtil.month(date));
                return month;
            }
        });
        reg(new AbstractConvert(String.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(String.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(String.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(String.class, ZonedDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.zonedDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(Long.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value);
            }
        });
        reg(new AbstractConvert(Long.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(Long.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(Long.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTimestamp(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(Long.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Year.of(DateUtil.year(DateUtil.parse(value)));
            }
        });
        reg(new AbstractConvert(Long.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
                return yearMonth;
            }
        });
        reg(new AbstractConvert(Long.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                Month month = Month.of(DateUtil.month(date));
                return month;
            }
        });
        reg(new AbstractConvert(Long.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(Long.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(Long.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(Long.class, ZonedDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.zonedDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(long.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value);
            }
        });
        reg(new AbstractConvert(long.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(long.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(long.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTimestamp(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(long.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                Year year = Year.of(DateUtil.year(date));
                return year;
            }
        });
        reg(new AbstractConvert(long.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
                return yearMonth;
            }
        });
        reg(new AbstractConvert(long.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                Month month = Month.of(DateUtil.month(date));
                return month;
            }
        });
        reg(new AbstractConvert(long.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(long.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(long.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(long.class, ZonedDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.zonedDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.util.Date.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.format((java.util.Date)value);
            }
        });
        reg(new AbstractConvert(java.util.Date.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(((java.util.Date)value).getTime());
            }
        });
        reg(new AbstractConvert(java.util.Date.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((java.util.Date)value).getTime();
            }
        });
        reg(new AbstractConvert(java.util.Date.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlDate((java.util.Date)value);
            }
        });
        reg(new AbstractConvert(java.util.Date.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTime((java.util.Date)value);
            }
        });
        reg(new AbstractConvert(java.util.Date.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTimestamp((java.util.Date)value);
            }
        });
        reg(new AbstractConvert(java.util.Date.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Year year = Year.of(DateUtil.year((java.util.Date)value));
                return year;
            }
        });
        reg(new AbstractConvert(java.util.Date.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = (java.util.Date)value;
                YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
                return yearMonth;
            }
        });
        reg(new AbstractConvert(java.util.Date.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = (java.util.Date)value;
                Month month = Month.of(DateUtil.month(date));
                return month;
            }
        });
        reg(new AbstractConvert(java.util.Date.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDate((java.util.Date)value);
            }
        });
        reg(new AbstractConvert(java.util.Date.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localTime((java.util.Date)value);
            }
        });
        reg(new AbstractConvert(java.util.Date.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDateTime((java.util.Date)value);
            }
        });
        reg(new AbstractConvert(java.util.Date.class, ZonedDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.zonedDateTime((java.util.Date)value);
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.format(DateUtil.parse(value), "yyyy-MM-dd");
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(DateUtil.parse(value).getTime());
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value).getTime();
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value);
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTimestamp(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                Year year = Year.of(DateUtil.year(date));
                return year;
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
                return yearMonth;
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                Month month = Month.of(DateUtil.month(date));
                return month;
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Date.class, ZonedDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.zonedDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.format(DateUtil.parse(value), "HH:mm:ss");
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(DateUtil.parse(value).getTime());
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value).getTime();
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value);
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTimestamp(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                Year year = Year.of(DateUtil.year(date));
                return year;
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
                return yearMonth;
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                Month month = Month.of(DateUtil.month(date));
                return month;
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Time.class, ZonedDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.zonedDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.format(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(DateUtil.parse(value).getTime());
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value).getTime();
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value);
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                Year year = Year.of(DateUtil.year(date));
                return year;
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
                return yearMonth;
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                Month month = Month.of(DateUtil.month(date));
                return month;
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.sql.Timestamp.class, ZonedDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.zonedDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.Year.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString();
            }
        });
        reg(new AbstractConvert(java.time.Year.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(((Year)value).getValue());
            }
        });
        reg(new AbstractConvert(java.time.Year.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(((Year)value).getValue()).longValue();
            }
        });
        reg(new AbstractConvert(java.time.Year.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(((Year)value).getValue()+"-01-01");
                return date;
            }
        });
        reg(new AbstractConvert(java.time.Year.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(((Year)value).getValue()+"-01-01");
                return DateUtil.sqlDate(date);
            }
        });
        reg(new AbstractConvert(java.time.Year.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(((Year)value).getValue()+"-01-01");
                return DateUtil.sqlTime(date);
            }
        });
        reg(new AbstractConvert(java.time.Year.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(((Year)value).getValue()+"-01-01");
                return DateUtil.sqlTimestamp(date);
            }
        });
        reg(new AbstractConvert(java.time.Year.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(((Year)value).getValue()+"-01-01");
                YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
                return yearMonth;
            }
        });
        reg(new AbstractConvert(java.time.Year.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Month month = Month.of(1);
                return month;
            }
        });
        reg(new AbstractConvert(java.time.Year.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(((Year)value).getValue()+"-01-01");
                return DateUtil.localDate(date);
            }
        });
        reg(new AbstractConvert(java.time.Year.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(((Year)value).getValue()+"-01-01");
                return DateUtil.localTime(date);
            }
        });
        reg(new AbstractConvert(java.time.Year.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(((Year)value).getValue()+"-01-01");
                return DateUtil.localDateTime(date);
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                int m = month.getMonthValue();
                if(m <10) {
                    return month.getYear()+"-0"+m;
                }
                return month.getYear()+"-"+m;
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                Date date = DateUtil.parse(month.getYear()+"-"+month.getMonthValue()+"-01");
                return Long.valueOf(date.getTime());
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                Date date = DateUtil.parse(month.getYear()+"-"+month.getMonthValue()+"-01");
                return date.getTime();
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                Date date = DateUtil.parse(month.getYear()+"-"+month.getMonthValue()+"-01");
                return date;
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                Date date = DateUtil.parse(month.getYear()+"-"+month.getMonthValue()+"-01");
                return DateUtil.sqlDate(date);
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                Date date = DateUtil.parse(month.getYear()+"-"+month.getMonthValue()+"-01");
                return DateUtil.sqlTime(date);
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                Date date = DateUtil.parse(month.getYear()+"-"+month.getMonthValue()+"-01");
                return DateUtil.sqlTimestamp(date);
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                return Year.of(month.getYear());
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                return Month.of(month.getMonthValue());
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                Date date = DateUtil.parse(month.getYear()+"-"+month.getMonthValue()+"-01");
                return DateUtil.localDate(date);
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                Date date = DateUtil.parse(month.getYear()+"-"+month.getMonthValue()+"-01");
                return DateUtil.localTime(date);
            }
        });
        reg(new AbstractConvert(java.time.YearMonth.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                YearMonth month =  ((YearMonth)value);
                Date date = DateUtil.parse(month.getYear()+"-"+month.getMonthValue()+"-01");
                return DateUtil.localDateTime(date);
            }
        });
        reg(new AbstractConvert(java.time.Month.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Month month =  (Month)value;
                if(month.getValue()<10) {
                    return "0"+month.getValue();
                }
                return ""+month.getValue();
            }
        });
        reg(new AbstractConvert(java.time.Month.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return YearMonth.of(DateUtil.year(), (Month)value);
            }
        });

        reg(new AbstractConvert(java.time.LocalDate.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.format(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(DateUtil.parse(value).getTime());
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value).getTime();
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value);
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTimestamp(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Year.of(DateUtil.year(DateUtil.parse(value)));
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                return YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                return Month.of(DateUtil.month(date));
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDate.class, ZonedDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.zonedDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.format(DateUtil.parse(value), "HH:mm:ss");
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(DateUtil.parse(value).getTime());
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value).getTime();
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value);
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTimestamp(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Year.of(DateUtil.year(DateUtil.parse(value)));
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                return DateUtil.yearMonth(date);
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Date date = DateUtil.parse(value);
                return Month.of(DateUtil.month(date));
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalTime.class, ZonedDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.zonedDateTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.format(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(DateUtil.parse(value).getTime());
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value).getTime();
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value);
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTimestamp(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Year.of(DateUtil.year(DateUtil.parse(value)));
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.yearMonth(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Month.of(DateUtil.month(DateUtil.parse(value)));
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(java.time.LocalDateTime.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localTime(DateUtil.parse(value));
            }
        });

        reg(new AbstractConvert(java.time.LocalDateTime.class, ZonedDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.zonedDateTime(DateUtil.parse(value));
            }
        });

        reg(new AbstractConvert(ZonedDateTime.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.format(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, Long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Long.valueOf(DateUtil.parse(value).getTime());
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, long.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value).getTime();
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.parse(value);
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.sqlTimestamp(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, java.time.Year.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Year.of(DateUtil.year(DateUtil.parse(value)));
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, java.time.YearMonth.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.yearMonth(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, java.time.Month.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return Month.of(DateUtil.month(DateUtil.parse(value)));
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDate(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localTime(DateUtil.parse(value));
            }
        });
        reg(new AbstractConvert(ZonedDateTime.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DateUtil.localDateTime(DateUtil.parse(value));
            }
        });
        /* *****************************************************************************************************************
         *                                               array
         * =================================================================================================================
         * java.entity.Double
         *
         * ****************************************************************************************************************/
        reg(new AbstractConvert(Double[].class, Point.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Point point = new Point((Double[])value);
                return point;
            }
        });
        reg(new AbstractConvert(double[].class, Point.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Point point = new Point((double[])value);
                return point;
            }
        });

        reg(new AbstractConvert(Point.class, int[].class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Point point = (Point) value;
                try{
                    int[] ints = new int[2];
                    ints[0] = point.x().intValue();
                    ints[1] = point.y().intValue();
                    value = ints;
                }catch (Exception e) {
                    e.printStackTrace();
                }
                return value;
            }
        });

        reg(new AbstractConvert(Point.class, Double[].class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Point point = (Point) value;
                Double[] xy = new Double[2];
                xy[0] = point.x();
                xy[1] = point.y();
                value = xy;
                return value;
            }
        });
        reg(new AbstractConvert(Point.class, double[].class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                Point point = (Point) value;
                try{
                    value = point.doubles();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                return value;
            }
        });
        reg(new AbstractConvert(byte[].class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return new String((byte[]) value);
            }
        });

        /* *****************************************************************************************************************
         *                                               Point
         * ==================================================================================================================
         * java.entity.Point
         *
         * *****************************************************************************************************************/

        reg(new AbstractConvert(DataRow.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((DataRow)value).toJSON();
            }
        });
        reg(new AbstractConvert(DataSet.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return ((DataSet)value).toJSON();
            }
        });
        reg(new AbstractConvert(Map.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
               return BeanUtil.map2json((Map)value);
            }
        });
        reg(new AbstractConvert(Map.class, DataRow.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return DataRow.parse((Map)value);
            }
        });
        reg(new AbstractConvert(String.class, byte[].class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                return value.toString().getBytes();
            }
        });
    }
}
