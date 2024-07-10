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




package org.anyline.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.metadata.Column;
import org.anyline.metadata.type.Convert;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.proxy.ConvertProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.encrypt.DESUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ProblematicWhitespace")
public class BeanUtil {
	public static ObjectMapper JSON_MAPPER = new ObjectMapper();

	private static final Logger log = LoggerFactory.getLogger(BeanUtil.class);
	static{
		DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

		JSON_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		JSON_MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS);

		// Include.Include.ALWAYS 默认
		// Include.NON_DEFAULT 属性为默认值不序列化
		// Include.NON_EMPTY 属性为 空（“”） 或者为 NULL 都不序列化
		// Include.NON_NULL 属性为NULL 不序列化

		JSON_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JSON_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		JSON_MAPPER.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);

		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATETIME_FORMATTER));
		javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
		javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER));
		javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATETIME_FORMATTER));
		javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
		javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FORMATTER));
		JSON_MAPPER.registerModule(javaTimeModule);
		JSON_MAPPER.setTimeZone(TimeZone.getDefault());

	}

	private static ObjectMapper newObjectMapper(JsonInclude.Include include) {
		ObjectMapper result = new ObjectMapper();
		result.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		result.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		result.setSerializationInclusion(include);
		return result;
	}
	public static Object puarseFieldValue(Object value) {
		Object v = value;
		return v;
	}

	public static boolean setFieldValue(Object obj, Field field, Object value) {
		return setFieldValue(obj, field, null, value);
	}
	public static boolean setFieldValue(Object obj, Field field, Object value, boolean alert) {
		return setFieldValue(obj, field, null, value, alert);
	}
	private static List<String> arr = new ArrayList<>();

	/**
	 * 根据field集合条目泛型类转换
	 * @param field field
	 * @param value Collection&lt;Map&gt;
	 * @return Collection&lt;Entity&gt;
	 * @throws Exception
	 */
	public static Collection maps2object(Field field, Collection value) throws Exception {
		Class clazz = field.getType();
		Collection list = null;
		Class itemClass = ClassUtil.getComponentClass(field);
		if(null == itemClass) {
			list = value;
		}else{
			list = (Collection)ClassUtil.newInstance(clazz);
			for (Object item : value) {
				if (item instanceof Map) {
					Object oitem = BeanUtil.map2object((Map) item, itemClass, null, true, true, true);
					list.add(oitem);
				}
			}
		}
		/*
		Collection list = value.getClass().newInstance();
		if(!clazz.isAssignableFrom(list.getClass())) {
			if(clazz == Collection.class || clazz == List.class) {
				//TODO 更多类型
				list = new ArrayList();
			}
		}

		Type gtype = field.getGenericType();
		if(gtype instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) gtype;
			Type[] args = pt.getActualTypeArguments();
			if (null != args && args.length > 0) {
				Class itemClass = (Class) args[0];
				for (Object item : value) {
					if (item instanceof Map) {
						Object oitem = BeanUtil.map2object((Map) item, itemClass, null, true, true, true);
						list.add(oitem);
					}
				}
			}
		}else{
			list = value;
		}*/
		return list;
	}

	/**
	 * 根据field集合条目泛型类转换
	 * @param value Map&lt;?, Map&gt;
	 * @return Map&lt;?, Entity&gt;
	 * @throws Exception
	 */
	public static Map maps2object(Field field, Map value) throws Exception {
		Map map = value.getClass().newInstance();
		Type type = field.getGenericType();
		if(type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Type[] args = pt.getActualTypeArguments();
			if (null != args && args.length > 1) {
				Class itemClass = (Class) args[1];
				for (Object key : value.keySet()) {
					Object item = value.get(key);
					if (item instanceof Map) {
						Object oitem = BeanUtil.map2object((Map) item, itemClass, null, true, true, true);
						map.put(key, oitem);
					}
				}
			}
		}else{
			map = value;
		}
		return map;
	}
	public static boolean setFieldValue(Object obj, Field field, Column metadata, Object value) {
		return setFieldValue(obj, field, metadata, value, true);
	}

	public static Collection convertList(Object v, Class component) {
		Collection result = new ArrayList();
		if(v instanceof String) {
			if("concat".equalsIgnoreCase(ConfigTable.LIST2STRING_FORMAT)) {
				String[] tmps = v.toString().split(",");
				for(String tmp:tmps) {
					result.add(tmp);
				}
 			}else if("json".equalsIgnoreCase(ConfigTable.LIST2STRING_FORMAT)) {
				try {
					JavaType type = JSON_MAPPER.getTypeFactory().constructParametricType(List.class, component);
					result = JSON_MAPPER.readValue(v.toString(), type);
				}catch (Exception e) {
					log.error("convert exception:", e);
				}
			}else{
			}
		}

		//TODO 注意int double float long等基础无包装类型
		if(v.getClass().isArray()) {
			Object[] list = (Object[])v;
			for(Object item:list) {
				result.add(ConvertProxy.convert(item, component, false));
			}
		}else if(v instanceof Collection) {
			Collection list = (Collection) v;
			for(Object item:list) {
				result.add(ConvertProxy.convert(item, component, false));
			}
		}
		return result;
	}

	/**
	 * 属性赋值
	 * @param obj 对象 如果给类静态属性赋值, 传null
	 * @param field 属性
	 * @param value 值
	 * @param alert 失败提醒
	 * @return boolean
	 */
	public static boolean setFieldValue(Object obj, Field field, Column metadata, Object value, boolean alert) {
		if(null == field) {
			return false;
		}
		if(null != obj &&Modifier.isStatic(field.getModifiers())) {
			//对象不处理静态属性
			return false;
		}
		Object result = value;

		boolean compatible = true;//是否兼容 int long等不能设置null值
		String fieldType = field.getType().getSimpleName();
		Class targetClass = field.getType();
		String type = fieldType.toLowerCase();		//属性类型
		TypeMetadata columnType = null;
		String columnTypeName = ""; //列类型
		if(null != metadata) {
			columnType = metadata.getTypeMetadata();
			columnTypeName = metadata.getTypeName();
			if(null != columnTypeName) {
				columnTypeName = columnTypeName.toUpperCase();
			}
		}
		if(null != value){
			if(ClassUtil.isInSub(value.getClass(), targetClass)){
				try {
					if (field.isAccessible()) {
						// 可访问属性
						field.set(obj, result);
					} else {
						// 不可访问属性
						field.setAccessible(true);
						field.set(obj, result);
						field.setAccessible(false);
					}
					return true;
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		String srcTypeKey = ClassUtil.type(value);
		String tarTypeKey = ClassUtil.type(field);

		Class componentClass = ClassUtil.getComponentClass(field);
		try{
			if(null != value) {
				if(targetClass == Object.class) {

				}else if(!srcTypeKey.equals(tarTypeKey)) {
					Convert convert =  ConvertProxy.getConvert(value.getClass(), targetClass);
					try {
						//数组 集合 Map
						//entity
						//基础类型
						if(null == convert) {
							if (targetClass.isArray()) {
								Collection converts = convertList(value, componentClass);
								result = BeanUtil.collection2array(converts, componentClass);
							} else if (ClassUtil.isInSub(targetClass, Collection.class)) {
								Collection list = (Collection) ClassUtil.newInstance(targetClass);
								Collection converts = convertList(value, componentClass);
								list.addAll(converts);
								result = list;
							} else if (ClassUtil.isInSub(targetClass, Map.class)) {
								Map map = (Map) ClassUtil.newInstance(targetClass);
								Class[] cls = ClassUtil.getComponentClasses(field);
								map = object2map(map, value, null, cls[1]);
								result = map;
							} else if (ClassUtil.isWrapClass(targetClass) && !targetClass.getName().startsWith("java")) {
								//entity
								List<Field> fields = ClassUtil.getFields(targetClass, false, false);
								Object entity = ClassUtil.newInstance(targetClass);
								if (null != entity) {
									for (Field f : fields) {
										Object fv = getFieldValue(result, f.getName());
										setFieldValue(entity, f, fv);
									}
									result = entity;
								}
							}
						}else{
							result = ConvertProxy.convert(value, targetClass, false);
						}

					}catch (Exception e) {
						log.error("set field value exception:", e);
					}
				}

			}else{//v == null
				if(type.equals("int")
						|| type.equals("double")
						|| type.equals("long")
						|| type.equals("float")
						|| type.equals("boolean")
						|| type.equals("short")
						|| type.equals("byte")
				) {
					compatible = false;
				}
			}
			if(compatible) {
				if (field.isAccessible()) {
					// 可访问属性
					field.set(obj, result);
				} else {
					// 不可访问属性
					field.setAccessible(true);
					field.set(obj, result);
					field.setAccessible(false);
				}
			}
		}catch(Exception e) {
			if(alert) {
				log.error("set field value exception:", e);
				log.error("[set field value][result:fail][field:{}({})] < [value:{}({})][column:{}][msg:{}]", field, tarTypeKey, result, srcTypeKey, columnType, e.toString());
			}
			return false;
		}
		return true;
	}
	public static Double[] double2Double(double[] array) {
		if(null == array) {
			return null;
		}
		Double[] result = new Double[array.length];
		int idx = 0;
		for(double item:array) {
			result[idx++] = item;
		}
		return result;
	}
	public static double[] Double2double(Double[] array, double def) {
		if(null == array) {
			return null;
		}
		double[] result = new double[array.length];
		int idx = 0;
		for(Double item:array) {
			if(null == item) {
				item = def;
			}
			result[idx++] = item;
		}
		return result;
	}

	public static Long[] long2Long(long[] array) {
		if(null == array) {
			return null;
		}
		Long[] result = new Long[array.length];
		int idx = 0;
		for(long item:array) {
			result[idx++] = item;
		}
		return result;
	}
	public static long[] Long2long(Long[] array, long def) {
		if(null == array) {
			return null;
		}
		long[] result = new long[array.length];
		int idx = 0;
		for(Long item:array) {
			if(null == item) {
				item = def;
			}
			result[idx++] = item;
		}
		return result;
	}

	public static Integer[] int2Integer(int[] array) {
		if(null == array) {
			return null;
		}
		Integer[] result = new Integer[array.length];
		int idx = 0;
		for(int item:array) {
			result[idx++] = item;
		}
		return result;
	}
	public static int[] Integer2int(Integer[] array, int def) {
		if(null == array) {
			return null;
		}
		int[] result = new int[array.length];
		int idx = 0;
		for(Integer item:array) {
			if(null == item) {
				item = def;
			}
			result[idx++] = item;
		}
		return result;
	}

	public static Float[] float2Float(float[] array) {
		if(null == array) {
			return null;
		}
		Float[] result = new Float[array.length];
		int idx = 0;
		for(float item:array) {
			result[idx++] = item;
		}
		return result;
	}
	public static float[] Float2float(Float[] array, float def) {
		if(null == array) {
			return null;
		}
		float[] result = new float[array.length];
		int idx = 0;
		for(Float item:array) {
			if(null == item) {
				item = def;
			}
			result[idx++] = item;
		}
		return result;
	}
	public static byte[] char2bytes(char[] chars) {
		Charset charset = StandardCharsets.ISO_8859_1;
		CharBuffer charBuffer = CharBuffer.allocate(chars.length);
		charBuffer.put(chars);
		charBuffer.flip();
		ByteBuffer byteBuffer = charset.encode(charBuffer);
		return byteBuffer.array();
	}
	public static char[] byte2char(byte[] bytes) {
		Charset charset = StandardCharsets.ISO_8859_1;
		ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
		byteBuffer.put(bytes);
		byteBuffer.flip();
		CharBuffer charBuffer = charset.decode(byteBuffer);
		return charBuffer.array();
	}

	/**
	 * 设置属性值
	 * @param obj 对象
	 * @param field 属性名
	 * @param value 值
	 * @param recursion 是递归查换父类属性
	 * @param alert 设备失败是否提示日期
	 * @return boolean
	 */
	public static boolean setFieldValue(Object obj, String field, Object value, boolean recursion, boolean alert) {
		if(null == obj || null == field) {
			return false;
		}
		if(obj instanceof Map) {
			Map tmp = (Map)obj;
			tmp.put(field, value);
		}else{
			Field f = ClassUtil.getField(obj.getClass(), field, recursion);
			if(null == f) {
				try{
					Method method = obj.getClass().getMethod("set" + field.substring(0,1).toUpperCase() + field.substring(1), value.getClass());
					if(null != method) {
						method.invoke(obj, value);
					}
				}catch (Exception e) {
				}
			}else {
				setFieldValue(obj, f, value, alert);
			}

		}
		return true;
	}

	public static boolean setFieldValue(Object obj, String field, Object value, boolean recursion) {
		return setFieldValue(obj, field, value, recursion, true);
	}
	public static boolean setFieldValue(Object obj, String field, Object value) {
		return setFieldValue(obj, field, value, true);
	}

	public static Object getFieldValue(Object obj, Field field) {
		Object value = null;
		if(null == obj || null == field) {
			return null;
		}
		try{
			if(field.isAccessible()) {
				// 可访问属性
				value = field.get(obj);
			}else{
				// 不可访问属性
				field.setAccessible(true);
				value = field.get(obj);
				field.setAccessible(false);
			}
		}catch(Exception e) {
			return null;
		}
		return value;
	}
	@SuppressWarnings("rawtypes")
	public static Object getFieldValue(Object obj, String field, boolean recursion) {
		if(null == obj) {
			return null;
		}
		Object value = null;
		if(obj instanceof DataRow) {
			DataRow row = (DataRow)obj;
			value = row.get(field);
		}else if(obj instanceof Map) {
			Map map = (Map)obj;
			value = map.get(field);
		}else if(obj instanceof Class) {
			Field f = ClassUtil.getField((Class)obj, field, recursion);
			value = getFieldValue(obj, f);
		}else{
			Field f = ClassUtil.getField(obj.getClass(), field, recursion);
			if(null == f) {
				f = ClassUtil.getField(obj.getClass(), camel(field), recursion);
			}
			value = getFieldValue(obj, f);
		}
		return value;

	}

	public static Object getFieldValue(Object obj, String field) {
		return getFieldValue(obj, field, false);
	}
	@SuppressWarnings("rawtypes")
	public static List<String> getMapKeys(Map map) {
		List<String> list = new ArrayList<>();
		for(Object key:map.keySet()) {
			list.add(key.toString());
		}
		return list;
	}

	/**
	 * 对象转换成Map
	 * @param obj  obj
	 * @param keys keys
	 * @return Map
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> object2map(Object obj, String ... keys) {
		if(null == obj) {
			return null;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		if(null == keys || keys.length ==0) {
			if(obj instanceof Map) {
				// map to map
				Map<String, Object> tmp = (Map<String, Object>)obj;
				for(String key:tmp.keySet()) {
					map.put(key, tmp.get(key));
				}
			}else{
				// object to map
				List<Field> fields = ClassUtil.getFields(obj.getClass());
				for(Field field:fields) {
					String key = field.getName();
					Object value = getFieldValue(obj, field);
					if(null == value) {
						value = "";
					}
					map.put(key, value);
				}
			}
		}else{
			for(String key:keys) {
				Object value = null;
				if(obj instanceof Map) {
					value = ((Map<String, Object>)obj).get(key);
				}else{
					value = getFieldValue(obj, key);
					if(null == value) {
						value = "";
					}
				}
				map.put(key, value);
			}
		}
		return map;
	}
	public static List<Map<String, Object>> list2maps(Collection<?> objs, String ... keys) {
		if(null == objs) {
			return null;
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(Object obj:objs) {
			list.add(object2map(obj, keys));
		}
		return list;
	}

	/**
	 * 过虑指定属性
	 * @param objs  objs
	 * @param keys  keys
	 */
	public static void removeProperty(Collection<Object> objs, String ... keys) {
		if(null == keys || null == objs) {
			return;
		}
		for(String key:keys) {
			removeProperty(objs, key);
		}
	}
	@SuppressWarnings("rawtypes")
	public static void removeProperty(Object obj, String key) {
		if(null == obj || null == key) {
			return;
		}
		if(obj instanceof Map) {
			((Map) obj).remove(key);
		}else{
			setFieldValue(obj, key, null);
		}
	}

	/**
	 * 提取指定属性值
	 * @param objs  objs
	 * @param keys  keys
	 * @return Collection
	 */
	public static Collection<Object> fetch(Collection<Object> objs, String ... keys) {
		if(null == objs) {
			return null;
		}
		Collection<Object> list = new ArrayList<Object>();
		for(Object obj: objs) {
			list.add(fetch(obj, keys));
		}
		return list;
	}
	@SuppressWarnings({"unchecked","rawtypes" })
	public static Object fetch(Object obj, String ... keys) {
		if(null == obj) {
			return null;
		}
		Object result = null;
		try{
			result = obj.getClass().newInstance();
			if(null != keys) {
				for(String key:keys) {
					if(obj instanceof Map) {
						Object value = ((Map)obj).get(key);
						((Map)obj).put(key, value);
					}else{
						Object value = getFieldValue(obj, key);
						setFieldValue(obj, key, value);
					}
				}
			}
		}catch(Exception e) {

		}
		return result;
	}

	/**
	 * 参考 DataSet.getRows
	 * @param list  list
	 * @param params  params
	 * @return Collection
	 */
	public static Collection<?> select(Collection<?> list, String ... params) {
		if(null == list || null == params || params.length==0) {
			return list;
		}
		if(list instanceof DataSet) {
			return ((DataSet)list).getRows(params);
		}
		Map<String, String> kvs = new HashMap<String, String>();
		int len = params.length;
		int i = 0;
		while(i<len) {
			String p1 = params[i];
			if(BasicUtil.isEmpty(p1)) {
				i++;
				continue;
			}else if(p1.contains(":")) {
				String ks[] = parseKeyValue(p1);
				kvs.put(ks[0], ks[1]);
				i++;
				continue;
			}else{
				if(i+1<len) {
					String p2 = params[i+1];
					if(BasicUtil.isEmpty(p2) || !p2.contains(":")) {
						kvs.put(p1, p2);
						i+=2;
						continue;
					}else{
						String ks[] = parseKeyValue(p2);
						kvs.put(ks[0], ks[1]);
						i+=2;
						continue;
					}
				}

			}
			i++;
		}

		Object[] items = list.toArray();
		int size = list.size();
		for(i=size-1; i>=0; i--) {
			Object obj = items[i];
			boolean chk = true;//对比结果
			for(String k : kvs.keySet()) {
				String v = kvs.get(k);
				Object value = getFieldValue(obj, k);

				if(null == v) {
					if(null != value) {
						chk = false;
						break;
					}
				}else{
					if(!v.equals(value+"")) {
						chk = false;
						break;
					}
				}
			}
			if(!chk) {
				list.remove(obj);
			}
		}
		return list;
	}

	/**
	 * @param params key1, value1, key2:value2, key3, value3
	 *               "NM:zh%","AGE:&gt;20","NM","%zh%"
	 * @return Map
	 */
	public static Map<String, String> array2map(String ... params) {
		Map<String, String> map = new HashMap<>();
		int len = params.length;
		int i = 0;
		while (i < len) {
			String p1 = params[i];
			if (BasicUtil.isEmpty(p1)) {
				i++;
				continue;
			} else if (p1.contains(":")) {
				String ks[] = parseKeyValue(p1);
				map.put(ks[0], ks[1]);
				i++;
				continue;
			} else {
				if (i + 1 < len) {
					String p2 = params[i + 1];
					if (BasicUtil.isEmpty(p2) || !p2.contains(":")) {
						map.put(p1, p2);
						i += 2;
						continue;
					} else {
						String ks[] = parseKeyValue(p2);
						map.put(ks[0], ks[1]);
						i += 2;
						continue;
					}
				}

			}
			i++;
		}
		return  map;
	}

	/**
	 * map转实现
	 * @param obj 在此基础上执行, 如果不提供则新创建
	 * @param map 源数据
	 * @param clazz Entity class
	 * @param metadatas metadatas
	 * @param recursion 是否递归
	 * @param ignoreCase 是否忽略大小写
	 * @param ignoreSplit 是否忽略分隔符号
	 * @param keys field:key对照关系
	 * @return T
	 * @param <T> T
	 */
	@SuppressWarnings("rawtypes")
	public static <T> T map2object(T obj, Map<String, ?> map, Class<T> clazz, Map metadatas, boolean recursion, boolean ignoreCase, boolean ignoreSplit, String ... keys) {
		try {
			if(null == obj) {
				if(Map.class.isAssignableFrom(clazz)) {
					return (T)map;
				}
				if(Object.class == clazz) {
					return (T)map;
				}
				if(clazz == Map.class) {
					obj = (T)new HashMap<>();
				}else {
					obj = (T) clazz.newInstance();
				}
			}
			Set es = map.entrySet();
			Iterator it = es.iterator();
			List<Field> fields = ClassUtil.getFields(clazz);
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String k = (String) entry.getKey();
				Object v = entry.getValue();
				Field field = ClassUtil.getField(fields, k, ignoreCase, ignoreSplit);
				Column metadata = null;
				if(map instanceof DataRow) {
					metadata = ((DataRow)map).getMetadata(k);
				}
				if(null == metadata && null != metadatas) {
					metadata = (Column) metadatas.get(k.toUpperCase());
				}
				setFieldValue(obj, field, metadata, v);
			}
			if(null != keys) {
				for(String key:keys) {
					String f = key;
					String k = key;
					String[] tmp = key.split(":");
					if(tmp.length > 1) {
						f = tmp[0];
						k = tmp[1];
					}
					Object v = map.get(k);
					setFieldValue(obj, f, v, true);
				}
			}
		}catch(Exception e) {
			log.error("map2object exception:", e);
		}
		return obj;
	}

	public static <T> T map2object(Map<String, ?> map, Class<T> clazz, Map metadatas, boolean recursion, boolean ignoreCase, boolean ignoreSplit, String ... keys) {
		return map2object(null, map, clazz, metadatas, recursion, ignoreCase, ignoreSplit, keys);
	}
	public static <T> T map2object(T obj, Map<String, ?> map, Class<T> clazz, Map metadatas, boolean recursion, boolean ignoreCase, boolean ignoreSplit, Map<Field, String> fields) {
		obj = map2object(obj, map, clazz, metadatas, recursion, ignoreCase, ignoreSplit);
		for(Map.Entry item:fields.entrySet()) {
			Field field = (Field)item.getKey();
			String column = (String)item.getValue();
			Object value = map.get(column);
			setFieldValue(obj, field, value);
		}
		return obj;
	}

	public static <T> T map2object(Map<String, ?> map, Class<T> clazz, Map metadatas, boolean recursion, boolean ignoreCase, boolean ignoreSplit, Map<Field, String> fields) {
		return map2object(null, map, clazz, metadatas, recursion, ignoreCase, ignoreSplit, fields);
	}
	public static <T> T map2object(Map<String, ?> map, Class<T> clazz, boolean recursion, boolean ignoreCase, boolean ignoreSplit, Map<Field, String> fields) {
		return map2object(null, map, clazz, null, recursion, ignoreCase, ignoreSplit, fields);
	}
	public static <T> T map2object(T obj, Map<String, ?> map, Class<T> clazz, Map metadatas, Map<Field, String> fields) {
		return map2object(obj, map, clazz, metadatas, false, false, false, fields);
	}
	public static <T> T map2object(T obj, Map<String, ?> map, Class<T> clazz, Map<Field, String> fields) {
		return map2object(obj, map, clazz, null, false, false, false, fields);
	}
	public static <T> T map2object(Map<String, ?> map, Class<T> clazz, Map metadatas, Map<Field, String> fields) {
		return map2object(null, map, clazz, metadatas, false, false, false, fields);
	}
	public static <T> T map2object(Map<String, ?> map, Class<T> clazz, Map<Field, String> fields) {
		return map2object(null, map, clazz, null, false, false, false, fields);
	}
	public static <T> T map2object(T obj, Map<String, ?> map, Class<T> clazz, Map metadatas, String ... keys) {
		return map2object(obj, map, clazz, metadatas, false, false, false);
	}
	public static <T> T map2object(T obj, Map<String, ?> map, Class<T> clazz, String ... keys) {
		return map2object(obj, map, clazz, null, false, false, false);
	}
	public static <T> T map2object(Map<String, ?> map, Class<T> clazz, Map metadatas, String ... keys) {
		return map2object(null, map, clazz, metadatas, false, false, false);
	}
	public static <T> T map2object(Map<String, ?> map, Class<T> clazz, String ... keys) {
		return map2object(null, map, clazz, null, false, false, false);
	}

	public static <T> T json2oject(String json, Class<T> clazz, JsonInclude.Include include) {
		try {
			if(null != include) {
				return newObjectMapper(include).readValue(json, clazz);
			}
			return  JSON_MAPPER.readValue(json, clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static <T> T json2oject(String json, Class<T> clazz) {
		return json2oject(json, clazz, null);
	}
	@SuppressWarnings("rawtypes")
	public static String map2xml(Map<String, ?> map, boolean border, boolean order) {
		StringBuffer builder = new StringBuffer();
		if(border) {
			builder.append("<xml>");
		}
		if(order) {
			SortedMap<String, Object> sort = new TreeMap<String, Object>(map);
			Set es = sort.entrySet();
			Iterator it = es.iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String k = (String) entry.getKey();
				String v = entry.getValue()+"";
				if("null".equals(v)) {
					v = "";
				}
				builder.append("<" + k + ">" + v + "</" + k + ">");
			}
		}else{
			Set es = map.entrySet();
			Iterator it = es.iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String key = (String) entry.getKey();
				String value = entry.getValue()+"";
				if("null".equals(value)) {
					value = "";
				}
				builder.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
			}
		}
		if(border) {
			builder.append("</xml>");
		}
		return builder.toString();
	}
	public static String map2xml(Map<String, ?> map) {
		return map2xml(map, true, false);
	}
	public static String map2json(Map<String, ?> map) {
		return object2json(map);
	}
	public static String map2json(Map<String, ?> map, JsonInclude.Include include) {
		return object2json(map, include);
	}
	public static Map<String, Object> xml2map(String xml) {
		Map<String, Object> map = new HashMap<String, Object>();
		Document document;
		try {
			document =  DocumentHelper.parseText(xml);
			Element root = document.getRootElement();
			for(Iterator<Element> itrProperty=root.elementIterator(); itrProperty.hasNext();) {
				Element element = itrProperty.next();
				String key = element.getName();
				String value = element.getTextTrim();
				map.put(key, value);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 按key升序拼接
	 * @param map  数据源
	 * @param join key, value之间的拼接符(默认=)
	 * @param separator  separator 多个kv的分隔符(默认&amp;)
	 * @param ignoreEmpty 是否忽略空值
	 * @param order 是否排序
	 * @return String(a=1&amp;b=2&amp;b=3)
	 */
	@SuppressWarnings({"rawtypes","unchecked" })
	public static String map2string(Map map, String join, String separator, boolean ignoreEmpty, boolean order) {
		StringBuilder result = new StringBuilder();
		Set es = null;
		if(order) {
			SortedMap<String, Object> wrap = new TreeMap<String, Object>(map);
			es = wrap.entrySet();
		}else{
			es = map.entrySet();
		}
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			Object v = entry.getValue();
			if(ignoreEmpty && BasicUtil.isEmpty(v)) {
				continue;
			}
			if(v instanceof Collection) {
				List list = new ArrayList();
				list.addAll((Collection)v);
				Collections.sort(list);
				for(Object item: list) {
					if(ignoreEmpty && BasicUtil.isEmpty(item)) {
						continue;
					}
					if (result.length() > 0) {
						result.append(separator);
					}
					result.append(k).append(join).append(item);
				}
			}else if(v instanceof String[]) {
				String vals[] = (String[])v;
				Arrays.sort(vals);
				for(String item:vals) {
					if(ignoreEmpty && BasicUtil.isEmpty(item)) {
						continue;
					}
					if (result.length() > 0) {
						result.append(separator);
					}
					result.append(k).append(join).append(item);
				}
			}else{
				if (result.length() > 0) {
					result.append(separator);
				}
				result.append(k).append(join).append(v);
			}
		}
		return result.toString();
	}

	public static String map2string(Map map, boolean ignoreEmpty, boolean order) {
		return map2string(map, "=","&", ignoreEmpty, order);
	}
	public static String map2string(Map map) {
		return map2string(map, "=","&", true, true);
	}
	public static <T> T xml2object(String xml, Class<T> clazz, boolean recursion, boolean ignoreCase, boolean ignoreSplit) {
		return xml2object(xml, clazz, null, recursion, ignoreCase, ignoreSplit);
	}
	public static <T> T xml2object(String xml, Class<T> clazz, Map columns, boolean recursion, boolean ignoreCase, boolean ignoreSplit) {
		T obj = null;
		try {
			Map<String, ?> map = xml2map(xml);
			obj = map2object(map, clazz, columns, recursion, ignoreCase, ignoreSplit);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	public static <T> T xml2object(String xml, Class<T> clazz, Map columns, boolean recursion) {
		return xml2object(xml, clazz, columns, recursion, false, false);
	}
	public static <T> T xml2object(String xml, Class<T> clazz, boolean recursion) {
		return xml2object(xml, clazz, null, recursion, false, false);
	}
	public static <T> T xml2object(String xml, Class<T> clazz, Map columns) {
		return xml2object(xml, clazz, columns, true);
	}
	public static <T> T xml2object(String xml, Class<T> clazz) {
		return xml2object(xml, clazz, null, true);
	}
	public static String object2xml(Object obj) {
		if(null == obj) {
			return null;
		}
		StringBuffer builder = new StringBuffer();
		builder.append("<xml>");
		List<Field> fields = ClassUtil.getFields(obj.getClass());
		for(Field field:fields) {
			Object value = getFieldValue(obj, field);
			if(null == value) {
				value = "";
			}
			builder.append("<").append(field.getName()).append(">")
					.append(value)
					.append("</").append(field.getName()).append(">");
		}
		builder.append("</xml>");
		return builder.toString();
	}

	/**
	 * 对象属性转map格式
	 * @param obj obj
	 * @return map
	 */
	public static Map<String, Object> object2map(Object obj) {
		if(null == obj) {
			return null;
		}
		if(obj instanceof Map){
			return (Map<String, Object>)obj;
		}
		Map<String, Object> map = new HashMap<>();
		List<Field> fields = ClassUtil.getFields(obj.getClass());
		for(Field field:fields) {
			Object value = getFieldValue(obj, field);
			if(!ClassUtil.isJavaType(value)){
				//如果不是java基础类型需要继续转map
				if(value instanceof Map){
					//map的value转map
					Map vmap = (Map) value;
					for(Object k:vmap.keySet()){
						Object v = vmap.get(k);
						if(!ClassUtil.isJavaType(v)) {
							vmap.put(k, object2map(v));
						}else{
							vmap.put(k, v);
						}
					}
				}else if(value instanceof Collection){
					//集合条件转map
					Collection arrays = (Collection) value;
					List list = new ArrayList<>();
					for(Object item:arrays){
						if(!ClassUtil.isJavaType(item)) {
							list.add(object2map(item));
						}else{
							list.add(item);
						}
					}
					value = list;
				}else if(value.getClass().isArray()){
					List list = new ArrayList<>();
					int len = Array.getLength(value);
					for (int i = 0; i < len; i++) {
						Object item = Array.get(value, i);
						if(!ClassUtil.isJavaType(item)){
							item = object2map(item);
						}
						list.add(item);
					}
					value = list;
				}else {
					value = object2map(value);
				}
			}
			/*if(null == value) {
				value = "";
			}*/
			map.put(field.getName(), value);
		}
		return map;
	}
	public static <K, V> Map<K, V> object2map(Object obj, List<K> keys) {
		return object2map(new HashMap(), obj, keys, null);
	}

	public static <K, V> Map<K, V> object2map(Map map, Object obj) {
		return object2map(map, obj, null, null);
	}
	public static <K, V> Map<K, V> object2map(Map map, Object obj, List<K> keys, Class<? extends V> valueClass) {
		if(null == obj) {
			return null;
		}

		if(null == keys || keys.isEmpty()) {
			if(obj instanceof Map) {
				try {
					if(null == map) {
						map = (Map) obj.getClass().newInstance();
					}
					Map objmap = (Map)obj;
					for (Object key:objmap.keySet()) {
						Object value = objmap.get(key);
						if(null != valueClass && null != value){
							value = ConvertProxy.convert(value, valueClass, false);
						}
						map.put(key, value);
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				if(null == map) {
					map = new HashMap();
				}
				List<Field> fields = ClassUtil.getFields(obj.getClass());
				for(Field field:fields) {
					Object value = getFieldValue(obj, field);
					if(null != valueClass && null != value){
						value = ConvertProxy.convert(value, valueClass, false);
					}
					/*if (null == value) {
						value = "";
					}*/
					map.put(field.getName(), value);
				}
			}
		}else {

			if(null == map) {
				map = new HashMap();
			}
			if(obj instanceof Map) {
				map=(Map)obj;
			}else {
				for (K key : keys) {
					Object value = getFieldValue(obj, key.toString());

					if(null != valueClass && null != value){
						value = ConvertProxy.convert(value, valueClass, false);
					}
					/*if (null == value) {
						value = "";
					}*/
					map.put(key, value);
				}
			}
		}
		return map;
	}
	public static <T> List<Map<String, Object>> objects2maps(Collection<T> objs, List<String> keys) {
		List<Map<String, Object>> maps = new ArrayList<>();
		if(null != objs)
			for(T obj:objs) {
				if(obj instanceof Map) {
					Map<String, Object> item = new HashMap<>();
					for(String key:keys) {
						item.put(key, ((Map)obj).get(key));
					}
					maps.add(item);
				}else {
					maps.add(object2map(obj, keys));
				}
			}
		return maps;
	}
	public static <T> List<Map<String, Object>> objects2maps(Collection<T> objs) {
		List<Map<String, Object>> maps = new ArrayList<>();
		if(null != objs)
			for(T obj:objs) {
				if(obj instanceof Map) {
					maps.add((Map)obj);
				}else {
					maps.add(object2map(obj));
				}
			}
		return maps;
	}
	public static String object2json(Object obj, JsonInclude.Include include) {
		if(null != obj) {
			//json类型直接返回
			if(obj.getClass().getName().toUpperCase().contains("JSON")) {
				return obj.toString();
			}
			try {
				if (null != include) {
					return newObjectMapper(include).writeValueAsString(obj);
				}
				return JSON_MAPPER.writeValueAsString(obj);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	public static String object2json(Object obj) {
		return object2json(obj, null);
	}

	/**
	 * 参数转map
	 * 参数格式a=1&amp;b=2&amp;b=3
	 * 如果是多个值, 以String的List形式保存
	 * 如果是url将根据问号分割
	 * @param url 参数或url
	 * @param empty 结果中是否包含空值, 所有空值以""形式保存
	 * @param decode 是否需要解码
	 * @param charset 解码编码
	 * @return Map
	 */
	public static Map<String, Object> param2map(String url, boolean empty, boolean decode, String charset) {
		Map<String, Object> params = new HashMap<String, Object>();
		if(null != url) {
			int index = url.indexOf("?");
			if(index != -1) {
				url = url.substring(index);
			}
			String[] kvs = url.split("&");
			for(String kv:kvs) {
				String k = null;
				String v = null;

				int idx = kv.indexOf("=");
				if(idx != -1) {
					k = kv.substring(0, idx);
					v = kv.substring(idx+1);
				}
				if("null".equals(v)) {
					v = "";
				}else if("NULL".equals(v)) {
					v = null;
				}
				if(BasicUtil.isEmpty(v) && !empty) {
					continue;
				}
				if(decode) {
					v = urlDecode(v, charset);
				}
				if(params.containsKey(k)) {
					Object olds = params.get(k);
					List<String> vs = new ArrayList<>();
					if(null == olds) {
						vs.add(null);
					}else if(olds instanceof String) {
						vs.add(olds.toString());
					}else if(olds instanceof ArrayList) {
						vs = (ArrayList)olds;
					}
					vs.add(v);
					params.put(k, vs);
				}else{
					params.put(k, v);
				}

			}
		}
		return params;
	}

	public static String urlDecode(String src, String charset) {
		String result = null;
		if(null != src) {
			try{
				if(null == charset) {
					result = URLDecoder.decode(src);
				}else {
					result = URLDecoder.decode(src, charset);
				}
			}catch (Exception e) {
				result = src;
			}
		}
		return result;
	}
	public static Map<String, Object> param2map(String url, boolean empty) {
		return param2map(url, empty, false, "UTF-8");
	}
	public static Map<String, Object> param2map(String url, boolean empty, boolean decode) {
		return param2map(url, empty, decode, "UTF-8");
	}

	/**
	 * 提取集合中每个条目的key属性的值
	 * 如提取用户列表中的所有用户ID
	 * @param list  list
	 * @param key  key
	 * @return List
	 */
	public static List<Object> extract(Collection<?> list, String key) {
		List<Object> values = new ArrayList<Object>();
		if(null != list) {
			for(Object obj:list) {
				Object value = getFieldValue(obj, key);
				values.add(value);
			}
		}
		return values;
	}

	/**
	 * 提取集合中每个条目的多个key属性的值
	 * 如提取用户列表中的所有用户ID, CODE
	 * @param list  list
	 * @param keys  keys
	 * @return List
	 */
	public static List<Map<String, Object>> extracts(Collection<?> list, String ... keys) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if(null != list) {
			for(Object obj:list) {
				Map<String, Object> map = new HashMap<String, Object>();
				if(null !=keys) {
					for(String key:keys) {
						Object value = getFieldValue(obj, key);
						map.put(key, value);
					}
					result.add(map);
				}
			}
		}
		return result;
	}

	/**
	 * 去重
	 * @param <T> T
	 * @param list   list
	 * @param keys 根据keys列或属性值比较
	 * @return   return
	 */
	public static <T> Collection<T> distinct(Collection<T> list, String ... keys) {
		List<T> result = new ArrayList<T>();
		if(null != list) {
			for(T obj:list) {
				if(null == keys || keys.length==0) {
					if(!result.contains(obj)) {
						result.add(obj);
					}
				}else{
					if(contain(result, obj, keys)) {
						result.add(obj);
					}
				}
			}
		}
		return result;
	}
	public static <T> Collection<T> distinct(Collection<T> list, List<String> keys) {
		List<T> result = new ArrayList<T>();
		if(null != list) {
			for(T obj:list) {
				if(null == keys || keys.size()==0) {
					if(!result.contains(obj)) {
						result.add(obj);
					}
				}else{
					if(!contain(result, obj, keys)) {
						result.add(obj);
					}
				}
			}
		}
		return result;
	}

	/**
	 * 是否包含
	 * @param <T> T
	 * @param list  list
	 * @param obj  obj
	 * @param keys 只比较keys列, 基础类型不需要指定列
	 * @return T
	 */
	public static <T> boolean contain(Collection<T> list, T obj, String ... keys) {
		for(T item:list) {
			if(equals(item, obj)) {
				return true;
			}
		}
		return false;
	}
	public static <T> boolean contain(Collection<T> list, T obj, List<String> keys) {
		for(T item:list) {
			if(equals(item, obj, keys)) {
				return true;
			}
		}
		return false;
	}

	public static <T> boolean equals(T obj1, T obj2, List<String> keys) {
		if(null == keys || keys.isEmpty()) {
			if(null == obj1) {
				if(null == obj2) {
					return true;
				}else{
					return false;
				}
			}else if(!ClassUtil.isPrimitiveClass(obj1)) {
				if(null == obj2) {
					return false;
				}else{
					if(obj1.toString().equals(obj2.toString())) {
						return true;
					}
				}
			}
			return false;
		}

		for(String key:keys) {
			Object v1 = getFieldValue(obj1, key);
			Object v2 = getFieldValue(obj2, key);
			if(!equals(v1, v2)) {
				return false;
			}

		}
		return true;
	}

	public static <T> boolean equals(T obj1, T obj2, String ... keys) {
		return equals(obj1, obj2, array2list(keys));
	}

	/*

	 */
/**
 * 数组拼接成字符串
 *
 * @param list   集合
 * @param split  分隔符
 * @param field 条目属性
 * @return String
 *//*

	public static String concat(List<?> list, String field, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(getFieldValue(item, field));
			}
		}
		return builder.toString();
	}
	public static String concat(List<?> list, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(List<?> list) {
		return concat(list, ",");
	}
*/

	/**
	 * 拼接属性值
	 * @param list 集合
	 * @param field 属性
	 * @param required 是否必须 false:不拼接空值
	 * @param split 分隔符号
	 * @param recursion 是否递归提交父类属性
	 * @return String
	 */
	public static String concat(Collection<?> list, String field, String split, boolean required, boolean recursion) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(getFieldValue(item, field, recursion));
			}
		}
		return builder.toString();
	}
	public static String concat(Collection<?> list, String field, String split, boolean required) {
		return concat(list, field, split, required, false);
	}
	public static String concat(Collection<?> list, String field, String split) {
		return concat(list, field, split, false);
	}

	/**
	 * 集合拼接
	 * @param list list
	 * @param split 分隔符
	 * @param required 是否必须(遇到宿舍是否忽略)
	 * @return String
	 */
	public static String concat(Collection<?> list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(Collection<?> list, String split) {
		return concat(list, split, false);
	}
	public static String concat(Collection<?> list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(Collection<?> list) {
		return concat(list, false);
	}

	public static <T> String concat(T[] list, String key, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static <T> String concat(T[] list, String key, String split) {
		return concat(list, key, split, false);
	}
	public static <T> String concat(T[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static <T> String concat(T[] list, String split) {
		return concat(list, split, false);
	}
	public static <T> String concat(T[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static <T> String concat(T[] list) {
		return concat(list, false);
	}

	public static String concat(Integer[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(Integer[] list, String split) {
		return concat(list, split, false);
	}
	public static String concat(Integer[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(Integer[] list) {
		return concat(list, false);
	}

	public static String concat(Long[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(Long[] list, String split) {
		return concat(list, split, false);
	}
	public static String concat(Long[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(Long[] list) {
		return concat(list, false);
	}

	public static String concat(Double[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required &&BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(Double[] list, String split) {
		return concat(list, split, false);
	}
	public static String concat(Double[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(Double[] list) {
		return concat(list, false);
	}

	public static String concat(Float[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(Float[] list, String split) {
		return concat(list, split, false);
	}
	public static String concat(Float[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(Float[] list) {
		return concat(list, false);
	}

	public static String concat(Short[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(Short[] list, String split) {
		return concat(list, split, false);
	}
	public static String concat(Short[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(Short[] list) {
		return concat(list, false);
	}

	public static String concat(Byte[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}

	public static String concat(Byte[] list, String split) {
		return concat(list, split, false);
	}
	public static String concat(Byte[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(Byte[] list) {
		return concat(list, false);
	}

	public static String concat(int[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(int[] list, String split) {
		return concat(list, split, false);
	}
	public static String concat(int[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(int[] list) {
		return concat(list, false);
	}

	public static String concat(long[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(long[] list, String split) {
		return concat(list, split);
	}
	public static String concat(long[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(long[] list) {
		return concat(list, false);
	}

	public static String concat(double[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required &&BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(double[] list, String split) {
		return concat(list, split, false);
	}
	public static String concat(double[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(double[] list) {
		return concat(list, false);
	}

	public static String concat(float[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(float[] list, String split) {
		return concat(list, split, false);
	}
	public static String concat(float[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(float[] list) {
		return concat(list, false);
	}

	public static String concat(short[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}
	public static String concat(short[] list, String split) {
		return concat(list, split, false);
	}
	public static String concat(short[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(short[] list) {
		return concat(list, false);
	}

	public static String concat(byte[] list, String split, boolean required) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			for(Object item:list) {
				if(!required && BasicUtil.isEmpty(item)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(item);
			}
		}
		return builder.toString();
	}

	public static String concat(byte[] list, String split) {
		return concat(list, split, false);
	}
	public static String concat(byte[] list, boolean required) {
		return concat(list, ",", required);
	}
	public static String concat(byte[] list) {
		return concat(list, false);
	}

	public static List wrap(Collection list, String wrap) {
		List result = new ArrayList<>();
		for(Object obj:list) {
			if(null == obj) {
				result.add(null);
			}else{
				if(BasicUtil.isNumber(obj)) {
					result.add(obj);
				}else{
					result.add(wrap+obj+wrap);
				}
			}
		}
		return result;
	}
	public static List<String> toUpperCase(List<String> list) {
		return toUpperCase(list, false);
	}

	/**
	 * 条目转换大写
	 * @param list list
	 * @param update 是否更新原集合 或创建新集合
	 * @return List
	 */
	public static List<String> toUpperCase(List<String> list, boolean update) {
		if(null == list) {
			return list;
		}
		List<String> result = null;
		if(!update) {
			result = new ArrayList<>();
			for(String value:list) {
				if(null != value) {
					 value = value.toUpperCase();
				}
				result.add(value);
			}
		}else{
			int size = list.size();
			for(int i=0; i<size; i++) {
				String value = list.get(i);
				if(null != value) {
					result.set(i, value.toUpperCase());
				}
			}
			result = list;
		}

		return result;
	}
	public static List<String> toLowerCase(List<String> list) {
		if(null != list) {
			int size = list.size();
			for(int i=0; i<size; i++) {
				String value = list.get(i);
				if(null != value) {
					list.set(i, value.toLowerCase());
				}
			}
		}
		return list;
	}

	@SuppressWarnings({"rawtypes","unchecked" })
	public static Object toUpperCaseKey(Object obj, String ... keys) {
		if(null == obj) {
			return null;
		}
		if (obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Date) {
			return obj;
		}
		if(obj instanceof Map) {
			obj = toUpperCaseKey((Map<String, Object>)obj, keys);
		}else if(obj instanceof Collection) {
			obj = toUpperCaseKey((Collection)obj, keys);
		}
		return obj;
	}
	@SuppressWarnings("rawtypes")
	public static Collection toUpperCaseKey(Collection con, String ... keys) {
		if(null == con) {
			return con;
		}
		for(Object obj :con) {
			obj = toUpperCaseKey(obj, keys);
		}
		return con;
	}
	public static Map<String, Object> toUpperCaseKey(Map<String, Object> map, String ... keys) {
		if(null == map) {
			return map;
		}
		List<String> ks = getMapKeys(map);
		for(String k:ks) {
			if(null == keys || keys.length == 0 || BasicUtil.containsString(keys, k)) {
				Object v = map.get(k);
				String key = k.toUpperCase();
				map.remove(k);
				map.put(key, v);
			}
		}
		return map;
	}

	/**
	 * 需要保证数据类型一致
	 * @param list list
	 * @return array
	 * @param <T> T
	 */
	public static <T> T[] list2array(List<T> list) {
		if(null == list || list.isEmpty()) {
			return null;
		}
		T[] result = (T[]) Array.newInstance(list.get(0).getClass(), list.size());
		int index = 0;
		for(T item:list) {
			result[index++] = item;
		}
		return result;
	}

	public static <T> T[] collection2array(Collection<T> list) {
		if(null == list || list.isEmpty()) {
			return null;
		}
		Class clazz = ClassUtil.getComponentClass(list);
		T[] result = (T[]) Array.newInstance(clazz, list.size());
		int index = 0;
		for(T item:list) {
			result[index++] = item;
		}
		return result;
	}

	public static <T> Object collection2array(Collection list, Class<T> clazz) {
		if(null == list || list.isEmpty()) {
			return null;
		}
		Object result = null;
		int size = list.size();
		if(clazz == int.class) {
			int[] ints = new int[size];
			int index = 0;
			for(Object item:list) {
				ints[index++] = BasicUtil.parseInt(item, 0).intValue();
			}
			result = ints;
		}else if(clazz == double.class) {
			double[] doubles = new double[size];
			int index = 0;
			for(Object item:list) {
				doubles[index++] = BasicUtil.parseDouble(item, 0d).doubleValue();
			}
			result = doubles;
		}else if(clazz == float.class) {
			float[] floats = new float[size];
			int index = 0;
			for(Object item:list) {
				floats[index++] = BasicUtil.parseFloat(item, 0f).floatValue();
			}
			result = floats;
		}else if(clazz == long.class) {
			long[] longs = new long[size];
			int index = 0;
			for(Object item:list) {
				longs[index++] = BasicUtil.parseLong(item, 0L).longValue();
			}
			result = longs;
		}else {
			T[] array = (T[]) Array.newInstance(clazz, list.size());
			int index = 0;
			for (Object item : list) {
				array[index++] = (T) ConvertProxy.convert(item, clazz, false);
			}
			result = array;
		}
		return result;
	}

	/**
	 * 与toString不同的是 中间没有空格与引号[1, 2, 3]而不是[1, 2, 3]
	 * @param list List
	 * @return String
	 */
	public static String list2string(List<?> list) {
		return "[" + concat(list) + "]";
	}
	public static <T> String array2string(T[] array) {
		return "[" + concat(array) + "]";
	}

	/**
	 * 截取数组
	 * @param array 原数组
	 * @param fr 开始位置
	 * @param to 结束位置
	 * @return 新数组
	 * @param <T> 数据类型
	 */
	public static <T> T[] cut(T[] array, int fr, int to) {
		if(null == array || array.length == 0) {
			return array;
		}
		T[] result = (T[]) Array.newInstance(array[0].getClass(), to - fr + 1);
		for(int i=fr; i<=to; i++) {
			result[i-fr] = array[i];
		}
		return result;
	}
	public static byte[] cut(byte[] array, int fr, int to) {
		if(null == array || array.length == 0) {
			return array;
		}
		byte[] result = new byte[to - fr + 1];
		for(int i=fr; i<=to; i++) {
			result[i-fr] = array[i];
		}
		return result;
	}
	public static short[] cut(short[] array, int fr, int to) {
		if(null == array || array.length == 0) {
			return array;
		}
		short[] result = new short[to - fr + 1];
		for(int i=fr; i<=to; i++) {
			result[i-fr] = array[i];
		}
		return result;
	}
	public static int[] cut(int[] array, int fr, int to) {
		if(null == array || array.length == 0) {
			return array;
		}
		int[] result = new int[to - fr + 1];
		for(int i=fr; i<=to; i++) {
			result[i-fr] = array[i];
		}
		return result;
	}

	/**
	 * 左补齐
	 * @param bytes bytes
	 * @param len len
	 * @return bytes
	 */
	public static byte[] fill(byte[] bytes, int len) {
		byte[] result = new byte[len];
		for(int i=0; i<bytes.length && i<len; i++) {
			result[len + i - bytes.length] = bytes[i];
		}
		return result;
	}

	/**
	 * String 转map
	 * @param str name:zhang, age:20
	 * @return Map
	 */
	public static Map<String, String> string2map(String str) {
		Map<String, String> map = new HashMap<String, String>();
		if(BasicUtil.isNotEmpty(str)) {
			//if(str.startsWith("${") && str.endsWith("}")) {
			if(BasicUtil.checkEl(str)) {
				str = str.substring(2, str.length()-1);
			}else if(str.startsWith("{") && str.endsWith("}")) {
				str = str.substring(1, str.length()-1);
			}
			String[] list = str.split(",");
			for(String item:list) {
				String[] kv = item.split(":");
				if(kv.length ==2) {
					String k = kv[0];
					if(k.startsWith("\"") && k.endsWith("\"")) {
						k = k.substring(1, k.length()-1);
					}
					String v = kv[1];
					if(v.startsWith("\"") && v.endsWith("\"")) {
						v = v.substring(1, v.length()-1);
					}
					map.put(k, v);
				}else{
					map.put(item.replace(":",""), null);
				}
			}
		}
		return map;
	}

	public static byte[] stream2bytes(InputStream is) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();//创建输出流对象
		byte[] b = new byte[1024];
		int len;
		while ((len = is.read(b)) != -1) {
			bos.write(b, 0, len);
		}
		byte[] array = bos.toByteArray();
		bos.close();
		return array;
	}

	public static Map<String, String> createMap(String... params) {
		Map<String, String> result = new HashMap<>();
		if (null != params) {
			int size = params.length;
			for (int i = 0; i < size - 1; i += 2) {
				String key = params[i];
				String value = params[i + 1];
				if (null == value) {
					value = "";
				}
				result.put(key.toString(), value);
			}
		}
		return result;
	}

	/**
	 * 删除空值
	 * @param map  map
	 * @param recursion  是否递归检测集合map类型值的长度
	 */
	public static void clearEmpty(Map<String, Object> map, boolean recursion ) {
		if(null == map) {
			return;
		}
		List<String> keys = BasicUtil.getMapKeys(map);
		for(String key:keys) {
			Object value = map.get(key);
			if(BasicUtil.isEmpty(recursion, value)) {
				map.remove(key);
			}
		}
	}
	public static void clearEmpty(Map<String, Object> map) {
		clearEmpty(map, true);
	}

	/**
	 * 删除空值
	 * @param list  list
	 * @param recursion  是否递归检测集合map类型值的长度
	 */
	public static void clearEmpty(List<Object> list, boolean recursion) {
		if(null == list) {
			return;
		}
		int size = list.size();
		for(int i=size-1;i>=0;i--) {
			if(BasicUtil.isEmpty(recursion, list.get(i))) {
				list.remove(i);
			}
		}
	}
	public static void clearEmpty(List<Object> list) {
		clearEmpty(list, true);
	}

	/**
	 * 多个数组合并成一个数组(二维数组合成一维数组)
	 * @param <T> T
	 * @param first  first
	 * @param rest  rest
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] union(T[] first, T[]... rest) {
		int len = first.length;
		for (T[] array : rest) {
			len += array.length;
		}
		T[] result = Arrays.copyOf(first, len);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	/**
	 * 集合中与value差值最小的成员的下标
	 * @param array  array
	 * @param value  value
	 * @return int
	 */
	public static int closest(short[] array, short value) {
		int index = 0;
		int dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++) {
			if(array[i]==value) {
				index = i;
				break;
			}
			int abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(Short[] array, short value) {
		int index = 0;
		int dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++) {
			if(array[i]==value) {
				index = i;
				break;
			}
			int abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(List<Short> array, short value) {
		int index = 0;
		int dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++) {
			if(array.get(i)==value) {
				index = i;
				break;
			}
			int abs = Math.abs(array.get(i)-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static int closest(int[] array, int value) {
		int index = 0;
		int dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++) {
			if(array[i]==value) {
				index = i;
				break;
			}
			int abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(Integer[] array, int value) {
		int index = 0;
		int dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++) {
			if(array[i]==value) {
				index = i;
				break;
			}
			int abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(List<Integer> array, int value) {
		int index = 0;
		int dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++) {
			if(array.get(i)==value) {
				index = i;
				break;
			}
			int abs = Math.abs(array.get(i)-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static int closest(long[] array, long value) {
		int index = 0;
		long dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++) {
			if(array[i]==value) {
				index = i;
				break;
			}
			long abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(Long[] array, long value) {
		int index = 0;
		long dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++) {
			if(array[i]==value) {
				index = i;
				break;
			}
			long abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(List<Long> array, long value) {
		int index = 0;
		long dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++) {
			if(array.get(i)==value) {
				index = i;
				break;
			}
			long abs = Math.abs(array.get(i)-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static int closest(float[] array, float value) {
		int index = 0;
		float dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++) {
			if(array[i]==value) {
				index = i;
				break;
			}
			float abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(Float[] array, float value) {
		int index = 0;
		float dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++) {
			if(array[i]==value) {
				index = i;
				break;
			}
			float abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}

		}
		return index;
	}
	public static int closest(List<Float> array, float value) {
		int index = 0;
		float dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++) {
			if(array.get(i)==value) {
				index = i;
				break;
			}
			float abs = Math.abs(array.get(i)-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static int closest(double[] array, double value) {
		int index = 0;
		double dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++) {
			if(array[i]==value) {
				index = i;
				break;
			}
			double abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(Double[] array, double value) {
		int index = 0;
		double dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++) {
			if(array[i]==value) {
				index = i;
				break;
			}
			double abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}

		}
		return index;
	}
	public static int closest(List<Double> array, double value) {
		int index = 0;
		double dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++) {
			if(array.get(i)==value) {
				index = i;
				break;
			}
			double abs = Math.abs(array.get(i)-value);
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static int closest(BigDecimal[] array, BigDecimal value) {
		int index = 0;
		double dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++) {
			double abs = Math.abs(array[i].subtract(value).doubleValue());
			if(abs==0) {
				index = i;
				break;
			}
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(List<BigDecimal> array, BigDecimal value) {
		int index = 0;
		double dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++) {
			double abs = Math.abs(array.get(i).subtract(value).doubleValue());
			if(abs==0) {
				index = i;
				break;
			}
			if(dif == -1 || dif > abs) {
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static String parseFinalValue(Object obj, String key) {
		return parseFinalValue(obj, key, "");
	}
	public static String parseFinalValue(Object obj, String key, String def) {
		if(null == obj) {
			return key;
		}
		String value = key;
		if(BasicUtil.isNotEmpty(key)) {
			if(key.contains("${")) {
				try{
					List<String> ks =RegularUtil.fetch(key, "\\${\\w+\\}", Regular.MATCH_MODE.CONTAIN, 0);
					for(String k:ks) {
						Object v = getFieldValue(obj, k.replace("${","").replace("}",""));
						if(null == v) {
							v = "";
						}
						value = value.replace(k, v.toString());
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			} else {
				Object val = getFieldValue(obj, key);
				if(null != val) {
					value = val.toString();
				}
			}
		}
		if(BasicUtil.isEmpty(value) || value.equals(key)) {
			value = def;
		}
		return value;
	}

	/**
	 * 集合截取
	 * @param <T>  t
	 * @param list  list
	 * @param begin  begin
	 * @param end  end
	 * @return List
	 */
	public static <T> List<T> cuts(Collection<T> list, int begin, int end) {
		List<T> result = new ArrayList<T>();
		if(null != list) {
			if(begin <=0) {
				begin = 0;
			}
			if(end < 0 || end >= list.size()) {
				end = list.size()-1;
			}
		}
		int idx = 0;
		for(T obj:list) {
			if(idx >= begin && idx <= end) {
				result.add(obj);
			}
			idx ++;
			if(idx > end) {
				break;
			}
		}
		return result;
	}

	/**
	 * 驼峰转下划线
	 * userName : user_name
	 * @param str src
	 * @return String
	 */
	public static String camel_(String str) {
		/*
		a1Bc > a1_bc
		user_GroupId > user_group_id
		userGroupId > user_group_id
		userID > user_id
		ID > id
		ABC > abc
		*/
		Pattern pattern = Pattern.compile("(?<=[a-z0-9])([A-Z])");
		Matcher matcher = pattern.matcher(str);
		return matcher.replaceAll("_$1").toLowerCase();
	}

	/**
	 * 转小驼峰
	 * @param key src
	 * @param hold 是否保留分隔符
	 * @return String
	 */
	public static String camel(String key, boolean hold) {
		if(!key.contains("-") && !key.contains("_")) {
			return key;
		}
		String[] ks = key.split("_|-");
		String sKey = null;
		for(String k:ks) {
			if(null == sKey) {
				sKey = k.toLowerCase();
			}else{
				if(hold) {
					sKey += "_";
				}
				sKey += CharUtil.toUpperCaseHeader(k.toLowerCase());
			}
		}
		return sKey;
	}

	public static String camel(String key) {
		return camel(key, false);
	}
	/**
	 * 转大驼峰
	 * @param key src
	 * @param hold 是否保留分隔符
	 * @return String
	 */
	public static String Camel(String key, boolean hold) {
		if(!key.contains("-") && !key.contains("_")) {
			return key;
		}
		String[] ks = key.split("_|-");
		String sKey = null;
		for(String k:ks) {
			if(null == sKey) {
				sKey = CharUtil.toUpperCaseHeader(k.toLowerCase());
			}else{
				if(hold) {
					sKey += "_";
				}
				sKey +=  CharUtil.toUpperCaseHeader(k.toLowerCase());
			}
		}
		return sKey;
	}

	public static String Camel(String key) {
		return Camel(key, false);
	}

	/**
	 * 解析 key:vlue形式参数age:20
	 * 返回数组["age","20"]
	 * 如果值为空返回["age",""]
	 * 如果没有分隔符返回["age","age"]
	 * @param src  src
	 * @return String
	 */
	public static String[] parseKeyValue(String src) {
		if(BasicUtil.isEmpty(src)) {
			return null;
		}
		int len = 2;
		String[] result = null;
		String key1 = src;
		String key2 = src;
		if(src.contains(":")) {
			String tmp[] = src.split(":");
			len = NumberUtil.max(len, tmp.length);
			result = new String[len];
			key1 = tmp[0];
			if(tmp.length>1) {
				key2 = tmp[1];
			}else{
				key2 = "";
			}
			for(int i=2; i<len; i++) {
				result[i] = tmp[i];
			}
		}else{
			result = new String[2];
		}
		result[0] = key1;
		result[1] = key2;
		return result;
	}
	public static boolean isJson(Object json) {
		if(null == json) {
			return false;
		}
		try{
			JSON_MAPPER.readTree(json.toString());
		}catch(Exception e) {
			return false;
		}
		return true;
	}

	public static Object value(JsonNode json) {
		if(null == json) {
			return null;
		}else{
			if(json.isNull()) {
				return null;
			}else if(json.isInt()) {
				return json.asInt();
			}else if(json.isBoolean()) {
				return json.asBoolean();
			}else if(json.isDouble()) {
				return json.asDouble();
			}else if(json.isLong()) {
				return json.asLong();
			}else{
				return json.asText();
			}
		}
	}

	/**
	 * 递归提取src中的value
	 * 如
	 * {
	 *     key1:{
	 *         key11:{
	 *             key111:111   // 提取111 recursion(map, key1, key11, key111)
	 *         },
	 *         key12:{
	 *             key121:{
	 *                 key1211:1211,
	 *                 key1212:1212 // 提取1212 recursion(map, key1, key12, key121, key1212)
	 *             }
	 *         }
	 *     }
	 * }
	 * @param src 数据源
	 * @param voluntary 遇到基础类型是否停止(不取下一级)
	 *                  voluntary=false时遇到提取基础类型属性值时返回null
	 *                  voluntary=true时遇到提取基础类型属性值时返回当前value并return value
	 * @param keys keys 一级key.二级key.三级key
	 * @return Object
	 */
	public static Object extract(Object src, boolean voluntary, String ... keys) {
		if(null == keys || keys.length ==0) {
			return null;
		}
		Object result  = src;
		for (String key : keys) {
			if(null != result) {
				if (ClassUtil.isWrapClass(result) && !(result instanceof String)) {
					result = getFieldValue(result, key);
				}else{
					if(voluntary) {
						return result;
					}else {
						result = null;
					}
				}
			}
		}
		return result;
	}
	public static Object extract(Object src, String ... keys) {
		return extract(src, false, keys);
	}

	/**
	 * 取第一个不为空的值
	 * @param src 数据源
	 * @param keys keys
	 * @return Object
	 */
	public static Object nvl(Object src, String ... keys) {
		Object value = null;
		if(null == src || null == keys) {
			return value;
		}
		for(String key:keys) {
			value = getFieldValue(src, key);
			if(null != value) {
				return value;
			}
		}

		return value;
	}

	public static Object evl(Object src, String ... keys) {
		Object value = null;
		if(null == src || null == keys) {
			return value;
		}
		for(String key:keys) {
			value = getFieldValue(src, key);
			if(BasicUtil.isNotEmpty(value)) {
				return value;
			}
		}
		return value;
	}

	/**
	 * 提取第一个不为空的value
	 * @param map map
	 * @param keys keys
	 * @return String
	 */
	public static Object propertyNvl(Map<String, ?> map, String ... keys) {
		Object value = null;
		if(null == map || null == keys) {
			return value;
		}
		for(String key:keys) {
			value = map.get(key);
			if(null != value) {
				return value;
			}
			// 以下划线分隔的key
			String[] ks = key.split("_");
			String sKey = null;
			for(String k:ks) {
				if(null == sKey) {
					sKey = k;
				}else{
					sKey = sKey + CharUtil.toUpperCaseHeader(k);
				}
			}
			value = map.get(sKey);
			if(null != value) {
				return value;
			}
			// 以中划线分隔的key
			ks = key.split("-");
			sKey = null;
			for(String k:ks) {
				if(null == sKey) {
					sKey = k;
				}else{
					sKey = sKey + CharUtil.toUpperCaseHeader(k);
				}
			}
			value = map.get(sKey);
			if(null != value) {
				return value;
			}
		}
		return value;
	}

	/**
	 * 设置所有属性值
	 * @param obj obj
	 * @param map map
	 * @param alert 赋值失败时是否提示异常信息
	 */
	public static void setFieldsValue(Object obj, Map<String, ?> map, boolean alert) {
		if(null != map && null != obj) {
			List<String> fields = ClassUtil.getFieldsName(obj.getClass());
			for (String field : fields) {
				Object value = propertyNvl(map, field);
				if (BasicUtil.isNotEmpty(value)) {
					setFieldValue(obj, field, value, true, alert);
				}
			}
		}
	}

	public static void setFieldsValue(Object obj, Map<String, ?> map) {
		setFieldsValue(obj, map, true);
	}
	public static byte[] serialize(Object value) {
		byte[] rv=new byte[0];
		if (value == null) {
			return rv;
		}
		ByteArrayOutputStream bos = null;
		ObjectOutputStream os = null;
		try {
			bos = new ByteArrayOutputStream();
			os = new ObjectOutputStream(bos);
			os.writeObject(value);
			os.close();
			bos.close();
			rv = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(os!=null)os.close();
				if(bos!=null)bos.close();
			}catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return rv;
	}

	public static Object deserialize(byte[] in) {
		Object rv=null;
		ByteArrayInputStream bis = null;
		ObjectInputStream is = null;
		try {
			if(in != null) {
				bis=new ByteArrayInputStream(in);
				is=new ObjectInputStream(bis);
				rv=is.readObject();
				is.close();
				bis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(is!=null)is.close();
				if(bis!=null)bis.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return rv;
	}

	public static List list(Object array) {
		List list = new ArrayList<>();
		if(null != array) {
			if(array.getClass().isArray()) {
				int len = Array.getLength(array);
				for (int i = 0; i < len; i++) {
					list.add(Array.get(array, i));
				}
			}else if(array instanceof Collection) {
				Collection items = (Collection) array;
				for (Object item:items) {
					list.add(item);
				}
			}else{
				list.add(array);
			}
		}else{
			list.add(array);
		}
		return list;
	}
	public static Object first(Object object) {
		Object value = null;
		if(null != object) {
			if (object instanceof Collection) {
				Collection list = (Collection) object;
				if (!list.isEmpty()) {
					value = list.iterator().next();
				}
			} else if (object.getClass().isArray()) {
				int len = Array.getLength(object);
				if(len > 0) {
					value = Array.get(object, 0);
				}
			}else{
				value = object;
			}
		}
		return value;
	}
	public static <T> List<T> array2list(T[] ... arrays) {
		List<T> list = new ArrayList<T>();
		if(null != arrays) {
			for (T[] array : arrays) {
				if(null != array) {
					//list.addAll(Arrays.asList(array));
					for (T item : array) {
						if(item instanceof Collection) {
							list.addAll((Collection)item);
						}else {
							list.add(item);
						}
					}
				}
			}
		}
		return list;
	}

	/**
	 * 合成笛卡尔组合
	 *
	 * @param lists  二维数组
	 * @param <T> t
	 * @return List
	 * 输入:
	 * [[A, B, C], [1, 2, 3]]
	 * 输出:
	 *  [[A, 1], [A, 2], [A, 3]
	 *, [B, 1], [B, 2], [B, 3]
	 *, [C, 1], [C, 2], [C, 3]]
	 *
	 * 输入:
	 * [[A, B, C], [1, 2, 3], [一, 二, 三]]
	 * 输出:
	 *  [[A, 1, 一], [A, 1, 二], [A, 1, 三], [A, 2, 一], [A, 2, 二], [A, 2, 三], [A, 3, 一], [A, 3, 二], [A, 3, 三]
	 *, [B, 1, 一], [B, 1, 二], [B, 1, 三], [B, 2, 一], [B, 2, 二], [B, 2, 三], [B, 3, 一], [B, 3, 二], [B, 3, 三]
	 *, [C, 1, 一], [C, 1, 二], [C, 1, 三], [C, 2, 一], [C, 2, 二], [C, 2, 三], [C, 3, 一], [C, 3, 二], [C, 3, 三]
	 * ]
	 */
	public static <T> List<List<T>> descartes(List<List<T>> lists) {
		List<List<T>> result = new ArrayList<List<T>>();
		if(null == lists || lists.size()==0) {
			return result;
		}
		List<T> st = lists.get(0);
		for (T t : st) {
			List<T> tmp = new ArrayList<T>();
			tmp.add(t);
			result.add(tmp);
		}
		List<List<T>> store = new ArrayList<List<T>>();
		for (int i = 1; i < lists.size(); i++) {
			List<T> r2 = lists.get(i);
			for (int j = 0; j < result.size(); j++) {
				List<T> rns = result.get(j);
				for (int k = 0; k < r2.size(); k++) {
					List<T> mid = new ArrayList<T>();
					mid.addAll(rns);
					mid.add(r2.get(k));
					store.add(mid);
				}
			}
			result = new ArrayList<List<T>>();
			result.addAll(store);
			store = new ArrayList<List<T>>();
		}
		return result;
	}

	public static <T> List<List<T>> descartes(List<T> ... lists) {
		return descartes(array2list(lists));
	}

	/**
	 * 合并成新数组
	 * @param array 第一个数组
	 * @param items 其他数组
	 * @param <T> 数据类型
	 * @return 合并后数组
	 */
	public static <T> T[] merge(T[] array, T[]... items) {
		T[] result = null;
		int len = array.length;
		Class clazz = null;
		if(null != items) {
			for (T[] item : items) {
				if (null != item) {
					len += array.length;
					if(null == array && null == clazz) {
						for (T obj : item) {
							if (null != obj) {
								clazz = obj.getClass();
								break;
							}
						}
					}
				}
			}
		}
		if(null != array) {
			result = Arrays.copyOf(array, len);
		}else{
			if(null != clazz) {
				result = (T[]) Array.newInstance(clazz, len);
			}else{
				return null;
			}
		}

		int offset = array.length;
		if(null != items) {
			for (T[] item : items) {
				if (null != item) {
					System.arraycopy(item, 0, result, offset, item.length);
					offset += item.length;
				}
			}
		}
		return result;
	}

	/**
	 * maps合并成新map
	 * @param maps map
	 * @return map
	 * @param <K> k
	 * @param <V> v
	 */
	public static <K, V> Map<K, V> merge(Map<K, V> ... maps) {
		Map<K, V> result = new HashMap<>();
		if(null != maps) {
			for(Map<K, V> map:maps) {
				join(result, map, true);
			}
		}
		return result;
	}

	/**
	 * copy合并成src中
	 * @param src src
	 * @param copy copy6
	 * @param over key相同时是否覆盖
	 * @return map
	 * @param <K> k
	 * @param <V> v
	 */
	public static <K, V> Map<K, V>  join(Map<K,V> src, Map<K,V> copy, boolean over) {
		if(null == src) {
			src = new HashMap<K,V>();
		}
		if(null != copy) {
			for(K key:copy.keySet()) {
				if(!over && src.containsKey(key)) {
					continue;
				}
				src.put(key, copy.get(key));
			}
		}
		return src;
	}

	/**
	 * list与items合并成新集合
	 * @param list list
	 * @param items items
	 * @return list
	 * @param <T> T
	 */
	public static <T> List<T> merge(Collection<T> list, T ... items) {
		List<T> result = new ArrayList<>();
		if(null != list) {
			result.addAll(list);
		}
		if(null != items) {
			for(T item:items) {
				result.add(item);
			}
		}
		return result;
	}
	public static <T> List<T> merge(List<T> list, T ... items) {
		List<T> result = new ArrayList<>();
		if(null != list) {
			result.addAll(list);
		}
		if(null != items) {
			for(T item:items) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * items拼接到list中
	 * @param list list
	 * @param items items
	 * @return list
	 * @param <T> T
	 */
	public static <T> List<T> join(List<T> list, T... items) {
		if(null == list) {
			list = new ArrayList<>();
		}
		if(null != items) {
			for(T item:items) {
				list.add(item);
			}
		}
		return list;
	}

	public static <T> Collection<T> join(Collection<T> list, T... items) {
		if(null == list) {
			list = new ArrayList<>();
		}
		if(null != items) {
			for(T item:items) {
				list.add(item);
			}
		}
		return list;
	}

	/**
	 * 添加集合,并去重(不区分大小写)
	 * @param list list
	 * @param appends appends
	 * @param distinct 去重
	 */
	public static <T> void join(boolean distinct, Collection<T> list, Collection<T> appends) {
		for(T append:appends) {
			if(!distinct || !contains(list, append)) {
				list.add(append);
			}
		}
	}
	public static <T> void join(boolean distinct, Collection<T> list, T ... appends) {
		for(T append:appends) {
			if(!distinct || !contains(list, append)) {
				list.add(append);
			}
		}
	}

	/**
	 * list中是否包含item 不区分大小写
	 * @param list list
	 * @param item item
	 * @return boolean
	 */
	public static <T> boolean contains(Collection<T> list, Object item) {
		for(T i:list) {
			if(null ==i && null == item) {
				return true;
			}
			if(null != i && i.toString().equalsIgnoreCase(item.toString())) {
				return true;
			}
		}
		return false;
	}

	public static String parseRuntimeValue(Object obj, String key) {
		return parseRuntimeValue(obj, key, false);
	}
	public static String parseRuntimeValue(Object obj, String key, boolean encrypt) {
		if(null == obj) {
			return key;
		}
		String value = key;
		if(BasicUtil.isNotEmpty(key)) {
			if(key.contains("${")) {
				value = parseFinalValue(obj, key);
			} else {
				Object val = getFieldValue(obj, key);
				if(null != val) {
					value = val.toString();
					if (encrypt) {
						value = DESUtil.encryptValue(value + "");
					}
				}else{
					value = null;
				}
			}
		}
		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()) {
			//log.info("[parse run time value][key:"+key+"][value:"+value+"]");
		}
		return value;
	}

	public static Map<String,Object> copy(Map<String,Object> into, Map<String,Object> copy, List<String> keys) {
		if(null == copy ) {
			return  into;
		}
		if(null != keys) {
			for (String key : keys) {
				String ks[] = parseKeyValue(key);
				into.put(ks[0], copy.get(ks[1]));
			}
		}
		return into;
	}
	public static Map<String,Object> copy(Map<String,Object> into, Map<String,Object> copy, String ... keys) {
		if(null == copy ) {
			return  into;
		}
		if(null != keys) {
			for (String key : keys) {
				String ks[] = parseKeyValue(key);
				into.put(ks[0], copy.get(ks[1]));
			}
		}
		return into;
	}
	public static Map<String,Object> copy(Map<String,Object> into, Map<String,Object> copy) {
		return copy(into, copy, getMapKeys(copy));
	}
	public static <T> T query(Collection<T> datas, Map<String,Object> kvs) {
		List<T> list = querys(datas,0,1, kvs);
		if(list.size()>0) {
			return list.get(0);
		}
		return null;
	}

	public static <T> List<T> querys(Collection<T> datas, int begin, String... params) {
		return querys(datas,begin, 0, params);
	}

	public static <T> List<T> querys(Collection<T> datas, String... params) {
		return querys(datas,0, params);
	}

	public static <T> List<T> querys(Collection<T> datas, int begin, int qty, String... params) {
		Map<String, Object> kvs = new HashMap<>();
		int len = params.length;
		int i = 0;
		String srcFlagTag = "srcFlag"; // 参数含有{}的 在kvs中根据key值+tag 放入一个新的键值对
		while (i < len) {
			String p1 = params[i];
			if (BasicUtil.isEmpty(p1)) {
				i++;
				continue;
			} else if (p1.contains(":")) {
				String ks[] = parseKeyValue(p1);
				kvs.put(ks[0], ks[1]);
				i++;
				continue;
			} else {
				if (i + 1 < len) {
					String p2 = params[i + 1];
					if (BasicUtil.isEmpty(p2) || !p2.contains(":")) {
						kvs.put(p1, p2);
						i += 2;
						continue;
					//} else if (p2.startsWith("${") && p2.endsWith("}")) {
					} else if (BasicUtil.checkEl(p2)) {
						p2 = p2.substring(2, p2.length() - 1);
						kvs.put(p1, p2);
						kvs.put(p1 + srcFlagTag, "true");
						i += 2;
						continue;
					} else {
						String ks[] = parseKeyValue(p2);
						kvs.put(ks[0], ks[1]);
						i += 2;
						continue;
					}
				}

			}
			i++;
		}
		return querys(datas, begin, qty, kvs);
	}

	public static <T> List<T> querys(Collection<T> datas, int begin, int qty, Map<String, Object> kvs) {
		List<T> set = new ArrayList<>();
		String srcFlagTag = "srcFlag"; // 参数含有{}的 在kvs中根据key值+tag 放入一个新的键值对
		BigDecimal d1;
		BigDecimal d2;
		for (T row:datas) {
			boolean chk = true;//对比结果
			for (String k : kvs.keySet()) {
				boolean srcFlag = false;
				if (k.endsWith(srcFlagTag)) {
					continue;
				} else {
					String srcFlagValue = kvs.get(k + srcFlagTag)+"";
					if (BasicUtil.isNotEmpty(srcFlagValue)) {
						srcFlag = true;
					}
				}
				String v = kvs.get(k)+"";
				Object value = getFieldValue(row,k);

				if (null == v) {
					if (null != value) {
						chk = false;
						break;
					}else{
						chk = true;
						break;
					}
				} else {
					if (null == value) {
						chk = false;
						break;
					}
					String str = value + "";
					str = str.toLowerCase();
					v = v.toLowerCase();
					if (srcFlag) {
						v = "${" + v + "}";
					}
					if (!v.equals(str)) {
						chk = false;
						break;
					}

				}
			}//end for kvs
			if (chk) {
				set.add(row);
				if (qty > 0 && set.size() >= qty) {
					break;
				}
			}
		}//end for rows
		return set;
	}
	private static String concatValue(Map<String,Object> row, String split) {
		StringBuilder builder = new StringBuilder();
		List<String> keys = getMapKeys(row);
		for(String key:keys) {
			if(builder.length() > 0) {
				builder.append(split);
			}
			builder.append(row.get(key));
		}
		return builder.toString();
	}

	/**
	 * 行转列
	 * 表结构(编号, 姓名, 年度, 科目, 分数, 等级)
	 * @param datas      数据集
	 * @param pks       唯一标识key(如编号,姓名)
	 * @param classKeys 分类key(如年度,科目)
	 * @param valueKeys 取值key(如分数,等级),如果不指定key则将整行作为value
	 * @param <T> 数据类型
	 * @return List
	 * 如果指定key
	 * 返回结构 [
	 *      {编号:01,姓名:张三,2010-数学-分数:100},
	 *      {编号:01,姓名:张三,2010-数学-等级:A},
	 *      {编号:01,姓名:张三,2010-物理-分数:100}
	 *  ]
	 *  如果只有一个valueKey则返回[
	 *      {编号:01,姓名:张三,2010-数学:100},
	 *      {编号:01,姓名:张三,2010-物理:90}
	 *  ]
	 * 不指定valuekey则返回 [
	 *      {编号:01,姓名:张三,2010-数学:{分数:100,等级:A}},
	 *      {编号:01,姓名:张三,2010-物理:{分数:100,等级:A}}
	 *  ]
	 */
	public static <T> List<Map<String,Object>> pivot(Collection<T> datas, List<String> pks, List<String> classKeys, List<String> valueKeys) {
		List<Map<String,Object>> result = objects2maps(distinct(datas,pks),pks);
		List<Map<String,Object>> classValues = objects2maps(distinct(datas,classKeys),classKeys);  // [{年度:2010,科目:数学},{年度:2010,科目:物理},{年度:2011,科目:数学}]
		for (Map<String,Object> row : result) {
			for (Map<String,Object> classValue : classValues) {
				Map<String,Object> params = new HashMap<>();
				copy(params, row, pks);
				copy(params, classValue);
				T valueRow = query(datas,params);
				String finalKey = concatValue(classValue,"-");//2010-数学
				if(null != valueKeys && !valueKeys.isEmpty()) {
					if(valueKeys.size() == 1) {
						if (null != valueRow) {
							row.put(finalKey, getFieldValue(valueRow, valueKeys.get(0)));
						} else {
							row.put(finalKey, null);
						}
					}else {
						for (String valueKey : valueKeys) {
							// {2010-数学-分数:100;2010-数学-等级:A}
							if (null != valueRow) {
								row.put(finalKey + "-" + valueKey, getFieldValue(valueRow, valueKey));
							} else {
								row.put(finalKey + "-" + valueKey, null);
							}
						}
					}
				}else{
					if (null != valueRow) {
						row.put(finalKey, valueRow);
					}else{
						row.put(finalKey, null);
					}
				}
			}
		}
		return result;
	}

	public static <T> List<Map<String,Object>> pivot(Collection<T> datas, String[] pks, String[] classKeys, String[] valueKeys) {
		return pivot(datas, array2list(pks),array2list(classKeys),array2list(valueKeys));
	}

	/**
	 * 行转列
	 * @param datas    数据
	 * @param pk       唯一标识key(如姓名)多个key以,分隔如(编号,姓名)
	 * @param classKey 分类key(如科目)多个key以,分隔如(科目,年度)
	 * @param valueKey 取值key(如分数)多个key以,分隔如(分数,等级)
	 * @param <T> 数据类型
	 * @return List
	 *  表结构(姓名,科目,分数)
	 *  返回结构 [{姓名:张三,数学:100,物理:90,英语:80},{姓名:李四,数学:100,物理:90,英语:80}]
	 */
	public static <T> List<Map<String,Object>> pivot(Collection<T> datas, String pk, String classKey, String valueKey) {
		List<String> pks = array2list(pk.trim().split(","));
		List<String> classKeys = array2list(classKey.trim().split(","));
		List<String> valueKeys = array2list(valueKey.trim().split(","));
		return pivot(datas, pks, classKeys, valueKeys);
	}
	public static <T> List<Map<String,Object>> pivot(Collection<T> datas, String pk, String classKey) {
		List<String> pks = array2list(pk.trim().split(","));
		List<String> classKeys = array2list(classKey.trim().split(","));
		List<String> valueKeys = new ArrayList<>();
		return pivot(datas, pks, classKeys, valueKeys);
	}

	public static <T> List<Map<String,Object>> pivot(Collection<T> datas, List<String> pks, List<String> classKeys, String ... valueKeys) {
		List<String> list = new ArrayList<>();
		if(null != valueKeys) {
			for(String item:valueKeys) {
				list.add(item);
			}
		}
		return pivot(datas, pks, classKeys, valueKeys);
	}
	private String concatValue(DataRow row, String split) {
		StringBuilder builder = new StringBuilder();
		List<String> keys = row.keys();
		for(String key:keys) {
			if(builder.length() > 0) {
				builder.append(split);
			}
			builder.append(row.getString(key));
		}
		return builder.toString();
	}
	private static String[] kvs(Map<String,Object> row) {
		List<String> keys = getMapKeys(row);
		int size = keys.size();
		String[] kvs = new String[size*2];
		for(int i=0; i<size; i++) {
			String k = keys.get(i);
			String value = null;
			Object v = row.get(k);
			if(null != v) {
				value = v.toString();
			}
			kvs[i*2] = k;
			kvs[i*2+1] = value;
		}
		return kvs;
	}

	/**
	 * distinct 不区分大小写
	 * @param list List
	 * @return List
	 */
	public static List<String> distinct(Collection<String> list) {
		List<String> result = new ArrayList<>();
		List<String> check = new ArrayList<>();
		if(null != list) {
			for(String item:list) {
				String upper = item.toUpperCase();
				if(!check.contains(upper)) {
					result.add(item);
					check.add(upper);
				}
			}
		}
		return result;
	}

	/**
	 * 分页
	 * @param vol 每页多少行
	 * @return List
	 */
	public static <T> List<List<T>> page(Collection<T> origin, int vol) {
		List<List<T>> list = new ArrayList<>();
		if(vol <= 0) {
			vol = 1;
		}
		int size = origin.size();
		int page = (size-1) / vol + 1;
		for(int i=0; i<page; i++) {
			int fr = i*vol;
			int to = (i+1)*vol-1;
			if(i == page-1) {
				to = size-1;
			}
			List<T> item = cuts(origin, fr, to);
			list.add(item);
		}
		return list;
	}

	/**
	 * 每页最少1行,最少分1页,最多分DataSet.size()页
	 * 多余的从第1页开始追加
	 * 5行分2页:共分成2页(3+2)
	 * 5行分3页:共分成3页(2+2+1)
	 * 10行分3页:共分成3页(4+3+3)
	 * 10行分6页:共分成6页(2+2+2+2+1+1)
	 * 5行分0页:共分成1页(5)
	 * 2行分3页:共分成2页(1+1)
	 *
	 * DataSet拆分成size部分
	 * @param page 拆成多少部分
	 * @return list
	 */
	public static <T> List<List<T>> split(Collection<T> origin, int page) {
		List<List<T>> list = new ArrayList<>();
		int size = origin.size();
		if(page <=0 ) {
			page = 1;
		}
		if(page > size) {
			page = size;
		}
		int vol = size / page;//每页多少行
		int dif = size - vol*page;
		int fr = 0;
		int to = 0;
		for(int i=0; i<page; i++) {
			to = fr + vol-1;
			if(dif > 0) {
				to ++;
				dif --;
			}
			if(to >= size) {
				to = size-1;
			}
			List<T> item = cuts(origin, fr, to);
			list.add(item);
			fr = to +1;
		}
		return list;
	}

	/**
	 * removeAll 不区分大小写
	 * @param src src
	 * @param remove remove
	 * @return List
	 */
	public static List<String> removeAll(List<String> src, List<String> remove) {
		List<String> check = new ArrayList<>();
		if(null != src) {
			remove = toUpperCase(remove);
			for(String item:src) {
				if(null != item && remove.contains(item.toUpperCase())) {
					check.add(item);
				}
			}
			src.removeAll(check);
		}
		return src;
	}

	public static List<String> removeAll(List<String> src, String ... remove) {
		return removeAll(src, array2list(remove));
	}
	public static <T> List<T> copy(Collection<T> list) {
		return merge(list);
	}
	public static <T> Collection<T> copy(Collection<T> tar, Collection<T> ... items) {
		if(null != tar && null != items) {
			for(Collection<T> item:items) {
				tar.addAll(item);
			}
		}
		return tar;
	}

	/**
	 * 复制copy的属性值到src(src属性值为null的情况下生效)
	 * @param src src
	 * @param copy copy
	 */
	public static void copyFieldValueNvl(Object src, Object copy) {
		if(null == src || null == copy) {
			return;
		}
		List<Field> fields = ClassUtil.getFields(src.getClass());
		for(Field field:fields) {
			Object value = getFieldValue(src, field);
			if(null == value) {
				value = getFieldValue(copy, field);
				setFieldValue(src, field, value);
			}
		}
	}

	/**
	 * 复制copy的属性值到to
	 * @param to 赋值给to
	 * @param copy copy
	 */
	public static void copyFieldValue(Object to, Object copy) {
		List<Field> fields = ClassUtil.getFields(to.getClass(), false, false);
		for(Field field:fields) {
			setFieldValue(to, field, getFieldValue(copy, field));
		}
	}

	/**
	 * 复制copy的属性值到to(copy属性值is not null的情况下生效)
	 * @param to 赋值给to
	 * @param copy copy
	 */
	public static void copyFieldValueWithoutNull(Object to, Object copy) {
		List<Field> fields = ClassUtil.getFields(to.getClass());
		for(Field field:fields) {
			Object value = getFieldValue(copy, field);
			if(null != value) {
				setFieldValue(to, field, value);
			}
		}
	}

	public static boolean checkIsNew(Object obj) {
		if(null == obj) {
			return false;
		}
		if(obj instanceof DataRow) {
			DataRow row = (DataRow)obj;
			return row.isNew();
		}else if(obj instanceof Map) {
			Map map = (Map)obj;
			if(BasicUtil.isNotEmpty(map.get(ConfigTable.DEFAULT_PRIMARY_KEY))
					|| BasicUtil.isNotEmpty(map.get(ConfigTable.DEFAULT_PRIMARY_KEY.toLowerCase()))
					|| BasicUtil.isNotEmpty(map.get(ConfigTable.DEFAULT_PRIMARY_KEY.toUpperCase()))
			) {
				return false;
			}
			return true;
		}else{
			Map<String,Object> values = EntityAdapterProxy.primaryValues(obj);
			for(Map.Entry entry:values.entrySet()) {
				if(BasicUtil.isNotEmpty(entry.getValue())) {
					return false;
				}
			}
		}
		return true;
	}


	public static Object value(Map map, String keys, Map<String, HashSet<String>> alias) {
		return value(map, keys, alias, Object.class, null);
	}

	/**
	 * 从map中取值
	 * @param map map
	 * @param keys 多个key以,分隔
	 * @param clazz 返回值类型
	 * @param def 默认值
	 * @return T
	 * @param <T> T
	 */
	public static <T> T value(Map map, String keys, Map<String, HashSet<String>> alias, Class<T> clazz, T def) {
		T result = null;
		String[] ks = keys.split(",");
		Object value = null;
		for(String key:ks) {
			value = map.get(key);
			if(null == value) {
				HashSet<String> aliasList = alias.get(key);
				if (null != aliasList) {
					for (String item : aliasList) {
						value = map.get(item);
						if (null != value) {
							break;
						}
					}
				}
			}
			if(null != value) {
				break;
			}
		}
		if(null != value) {
			result = (T) ConvertProxy.convert(value, clazz, false);
		}
		if(null == result) {
			result = def;
		}
		return result;
	}
}
