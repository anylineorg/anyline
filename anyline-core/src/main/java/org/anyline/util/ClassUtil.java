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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {

	private static final Logger log = LoggerFactory.getLogger(ClassUtil.class);
	private static Map<Class, Class> INTERFACE_IMPLEMENT = new HashMap<>();
	public static void regImplement(Class interfaceClass, Class implementClass) {
		INTERFACE_IMPLEMENT.put(interfaceClass, implementClass);
	}

	/**
	 * 是否是基础类型(不包含String类型)
	 * @param obj 对象或类, 如果是对象先getClass()
	 * @return boolean
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isPrimitiveClass(Object obj) {
		try {
			Class clazz = null;
			if(obj instanceof Class) {
				clazz = (Class) obj;
			}else{
				clazz = obj.getClass();
			}
			if(clazz.isPrimitive() || ((Class<?>)clazz.getField("TYPE").get(null)).isPrimitive()) {
				return true;
			}else{
				return false;
			}
		} catch (Exception ignored) {
			return false;
		}
	}

	/**
	 * 是否java基础类型(以java.开头的类)
	 * @param check 类或对象
	 * @return null返回true
	 */
	public static boolean isJavaType(Object check){
		if(null == check){
			return true;
		}
		if(ClassUtil.isPrimitiveClass(check)){
			return true;
		}
		if(check instanceof Class) {
			if (((Class)check).getName().startsWith("java.")) {
				return true;
			}
		}else{
			if (check.getClass().getName().startsWith("java.")) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 是否是封装类(基础类型之外的类) String类返回true
	 * @param obj obj
	 * @return boolean
	 */
	public static boolean isWrapClass(Object obj) {
		return !isPrimitiveClass(obj);
	}
	public static List<Class<?>> list(String packageName, boolean recursion, Class<?> ... bases) {
		List<Class<?>> list = new ArrayList<Class<?>>();
		try {
			List<String> names = getClassNames(packageName, recursion);
			for (String name : names) {
				try {
					if(name.startsWith("java")) {
						continue;
					}
					Class<?> c = Class.forName(name);
					if (isInSub(c, bases)) {
						list.add(c);
					}
				}catch (NoClassDefFoundError e) {
				}catch (Exception e) {
				}
			}
		}catch (NoClassDefFoundError e) {
		}catch(Exception e) {
		}
		return list;
	}

	public static List<String> names(String packageName, boolean recursion, Class<?> ... bases) {
		List<String> list = new ArrayList<>();
		try {
			List<String> names = getClassNames(packageName, recursion);
			for (String name : names) {
				try {
					Class<?> c = Class.forName(name);
					if (isInSub(c, bases)) {
						list.add(name);
					}
				} catch (Exception ignored) {

				}
			}
		}catch(Exception ignored) {
		}
		return list;
	}

	/**
	 * clazz是否是bases子类或实现了bases接口(满足其中一个)
	 * @param clazz  类
	 * @param bases  父类或接口
	 * @return boolean
	 */
	public static boolean isInSub(Class<?> clazz, Class<?> ... bases) {
		List<Class> list = new ArrayList<>();//避免[null]
		if(null != bases) {
			for(Class<?> base : bases) {
				if(null != base) {
					list.add(base);
				}
			}
		}
		if(list.isEmpty()) {
			return true;
		}
		for(Class<?> base : bases) {
			if(base.isAssignableFrom(clazz)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否是bases子类或实现了 bases 接口(满足全部)
	 * @param clazz  clazz
	 * @param bases  bases
	 * @return boolean
	 */
	public static boolean isAllSub(Class<?> clazz, Class<?> ... bases) {
		List<Class> list = new ArrayList<>();//避免[null]
		if(null != bases) {
			for(Class<?> base : bases) {
				if(null != base) {
					list.add(base);
				}
			}
		}
		if(list.isEmpty()) {
			return true;
		}

		for(Class<?> base : bases) {
			if(!base.isAssignableFrom(clazz)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取某包下所有类
	 *
	 * @param packageName  包名
	 * @param childPackage 是否遍历子包
	 * @return 类的完整名称
	 * @throws UnsupportedEncodingException UnsupportedEncodingException
	 */
	private static List<String> getClassNames(String packageName, boolean childPackage) throws IOException {
		List<String> fileNames = new ArrayList<>();
		log.info("[正在加载本地类][package:{}]", packageName);
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String packagePath = packageName.replace(".","/");
		Enumeration<URL> urls = loader.getResources(packagePath);
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			if (url == null)
				continue;
			String type = url.getProtocol();
			if (type.equals("file")) {
				fileNames.addAll(getClassNameListFromFile(url.getPath(), childPackage));
			} else if (type.equals("jar")) {
				fileNames.addAll(getClassNameListFromJar(url.getPath(), childPackage));
			}
		}
		return fileNames;
	}

	/**
	 * 从项目文件获取某包下所有类
	 *
	 * @param filePath     文件路径
	 *                     类名集合
	 * @param childPackage 是否遍历子包
	 * @return 类的完整名称
	 * @throws UnsupportedEncodingException
	 */
	private static List<String> getClassNameListFromFile(String filePath, boolean childPackage) throws UnsupportedEncodingException {
		List<String> myClassName = new ArrayList<>();
		// filePath = UrlDecode.getURLDecode(filePath);
		File file = new File(filePath);
		File[] childFiles = file.listFiles();
		if (childFiles == null)
			return myClassName;
		for (File childFile : childFiles) {
			if (childFile.isDirectory()) {
				if (childPackage) {
					myClassName.addAll(getClassNameListFromFile(childFile.getPath(), childPackage));
				}
			} else {
				String childFilePath = childFile.getPath();
				if (childFilePath.endsWith(".class") && !childFilePath.contains("$")) {
					String c = childFilePath.split("classes")[1].replace(".class","");
					c = c.replace("\\",".").replace("//",".");
					if(c.startsWith(".")) {
						c = c.substring(1);
					}
					myClassName.add(c);
				}
			}
		}
		return myClassName;
	}

	/**
	 * 从jar获取某包下所有类
	 *
	 * @param jarPath      jar文件路径
	 * @param childPackage 是否遍历子包
	 * @return 类的完整名称
	 * @throws UnsupportedEncodingException
	 */
	private static List<String> getClassNameListFromJar(String jarPath, boolean childPackage) throws UnsupportedEncodingException {
		List<String> names = new ArrayList<>();
		String[] jarInfo = jarPath.split("!");
		String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
		String packagePath = jarInfo[1].substring(1);
		try {
			JarFile jarFile = new JarFile(jarFilePath);
			Enumeration<JarEntry> entrys = jarFile.entries();
			while (entrys.hasMoreElements()) {
				JarEntry jarEntry = entrys.nextElement();
				String entryName = jarEntry.getName();
				if (entryName.endsWith(".class") && !entryName.contains("$")) {
					if (childPackage) {
						if (entryName.startsWith(packagePath)) {
							entryName = entryName.replace("/",".").substring(0, entryName.lastIndexOf("."));
							names.add(entryName);
						}
					} else {
						int index = entryName.lastIndexOf("/");
						String myPackagePath;
						if (index != -1) {
							myPackagePath = entryName.substring(0, index);
						} else {
							myPackagePath = entryName;
						}
						if (myPackagePath.equals(packagePath)) {
							entryName = entryName.replace("/",".").substring(0, entryName.lastIndexOf("."));
							names.add(entryName);
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return names;
	}

	/**
	 * 从所有jar中搜索该包, 并获取该包下所有类
	 *
	 * @param urls         URL集合
	 * @param packagePath  包路径
	 * @param childPackage 是否遍历子包
	 * @return 类的完整名称
	 * @throws UnsupportedEncodingException
	 */
	private static List<String> getClassNameListFromJar(URL[] urls, String packagePath, boolean childPackage) throws UnsupportedEncodingException {
		List<String> names = new ArrayList<>();
		if (urls != null) {
			for (int i = 0; i < urls.length; i++) {
				URL url = urls[i];
				String urlPath = url.getPath();
				// 不必搜索classes文件夹
				if (urlPath.endsWith("classes/")) {
					continue;
				}
				String jarPath = urlPath + "!/" + packagePath;
				names.addAll(getClassNameListFromJar(jarPath, childPackage));
			}
		}
		return names;
	}
	public static boolean hasAnnotation(Class clazz, String annotation) {
		Annotation[] annotations = clazz.getAnnotations();
		for(Annotation item:annotations) {
			if(item.annotationType().getSimpleName().equalsIgnoreCase(annotation)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 反射类注解的属性值 在不确定具体注解与属性的情况下使用
	 * 注解名与属性名不区分大小写
	 * *表示任意字符
	 * @param target 类
	 * @param annotation 注解类名 如: *, Table*
	 * @param field 属性名 如: *, value, name, *package*
	 * @param qty 最多取几个值 -1:不限制
	 * @return List
	 */
	public static List<Object> getAnnotationFieldValues(Class target, String annotation, String field, int qty) {
		Annotation[] annotations = target.getAnnotations();
		return getAnnotationFieldValues(annotations, annotation, field, qty);
	}
	public static List<Object> getAnnotationFieldValues(Class target, String annotation, String field) {
		return getAnnotationFieldValues(target, annotation, field, -1);
	}
	public static Object getAnnotationFieldValue(Class target, String annotation, String field) {
		List<Object> values = getAnnotationFieldValues(target, annotation, field, 1);
		if(!values.isEmpty()) {
			return values.get(0);
		}
		return null;
	}

	/**
	 * 反射属性target上的注解, 获取注解上指定的属性值
	 * 注解名与属性名不区分大小写
	 * *表示任意字符
	 * @param target 类的属性
	 * @param annotation 注解类名 支持模糊匹配 如: *, Table*
	 * @param field 注解的属性名 如: *, value, name, *package*
	 * @param qty 最多取几个值 -1:不限制
	 * @return List
	 */
	public static List<Object> getAnnotationFieldValues(Field target, String annotation, String field, int qty) {
		Annotation[] annotations = target.getAnnotations();
		return getAnnotationFieldValues(annotations, annotation, field, qty);
	}
	public static List<Object> getAnnotationFieldValues(Field target, String annotation, String field) {
		return getAnnotationFieldValues(target, annotation, field, -1);
	}
	public static Object getAnnotationFieldValue(Field target, String annotation, String field) {
		List<Object> values = getAnnotationFieldValues(target, annotation, field, 1);
		if(!values.isEmpty()) {
			return values.get(0);
		}
		return null;
	}
	private static List<Object> getAnnotationFieldValues(Annotation[] annotations, String annotation, String field, int qty) {
		List<Object> list = new ArrayList<>();
		for(Annotation an : annotations) {
			String name = an.annotationType().getSimpleName();
			if(!match(name, annotation)) {
				continue;//注解名不匹配
			}
			Method methods[] = an.annotationType().getMethods();
			for(Method method:methods) {
				name = method.getName();
				if(!match(name, field)) {
					continue;//属性名不匹配
				}
				try {
					Object value = method.invoke(an);
					if (null != value && value.getClass().isArray()) {
						int len = Array.getLength(value);
						for (int i=0; i<len; i++) {
							Object v = Array.get(value, i);
							list.add(v);
							if(qty >0 && list.size()>=qty) {
								return list;
							}
						}
					} else {
						list.add(value);
					}
				}catch (Exception e) {
				}
			}

		}
		return list;
	}

	private static boolean match(String value, String regex) {
		regex = regex.replace("*",".*").toUpperCase();
		return value.toUpperCase().matches(regex);
	}

	/**
	 * 提取类及父类的所有属性
	 * @param clazz  clazz
	 * @param statics  是否返回静态属性
	 * @param finals  是否返回final属性
	 * @return List
	 */
	public static List<Field> getFields(Class<?> clazz, boolean statics, boolean finals) {
		List<Field> fields = new ArrayList<Field>();
		while(null != clazz) {
			Field[] tmp = clazz.getDeclaredFields();
			for(Field field:tmp) {
				if(!statics && Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				if(!finals && Modifier.isFinal(field.getModifiers())) {
					continue;
				}
				fields.add(field);
			}
			clazz = clazz.getSuperclass();
		}
		return fields;
	}
	public static List<Field> getFields(Class<?> clazz) {
		return getFields(clazz, true, true);
	}
	public static List<String> getFieldsName(Class<?> clazz) {
		List<Field> fields = getFields(clazz);
		List<String> keys = new ArrayList<>();
		for(Field field:fields) {
			keys.add(field.getName());
		}
		return keys;
	}
	public static List<Method> getMethods(Class<?> clazz, boolean recursion) {
		List<Method> list = new ArrayList<>();
		Method[] methods = clazz.getMethods();
		for(Method method:methods) {
			list.add(method);
		}
		// 递归父类
		if(null != clazz && recursion) {
			clazz = clazz.getSuperclass();
			if(null != clazz) {
				list.addAll(getMethods(clazz, recursion));
			}
		}
		return list;
	}
	public static Method getMethod(Class<?> clazz, String name, boolean recursion, Class<?>... parameterTypes) {
		Method method = null;
		try{
			method = clazz.getMethod(name, parameterTypes);
		}catch(Exception ignored) {}
		if(null == method) {
			try{
				method = clazz.getDeclaredMethod(name, parameterTypes);
			}catch(Exception e) {

			}
		}
		// 递归父类
		if(null == method && recursion) {
			clazz = clazz.getSuperclass();
			if(null != clazz) {
				method = getMethod(clazz, name, recursion, parameterTypes);
			}
		}
		return method;
	}

	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		return getMethod(clazz, name, false, parameterTypes);
	}
	public static Field getField(Class<?> clazz, String name, boolean recursion) {
		return getField(clazz, name, recursion, false);
	}

	/**
	 * 根据名称获取属性
	 * @param clazz 类
	 * @param name 属性名
	 * @param recursion 是否递归父类
	 * @param ignoreCase 是否忽略大小写及其他符号
	 * @return Field
	 */

	public static Field getField(Class<?> clazz, String name, boolean recursion, boolean ignoreCase) {
		Field field = null;
		try{
			field = clazz.getField(name);
		}catch(Exception ignored) {}
		if(null == field) {
			try{
				field = clazz.getDeclaredField(name);
			}catch(Exception e) {

			}
		}
		//忽略大小写及其他符号
		if(null == field && ignoreCase){
			name = name.replace("_", "");
			Field[] fields = clazz.getFields();
			for(Field item:fields) {
				if(name.equalsIgnoreCase(item.getName().replace("_",""))){
					field = item;
					break;
				}
			}
			if(null == field){
				fields = clazz.getDeclaredFields();
				for(Field item:fields) {
					if(name.equalsIgnoreCase(item.getName().replace("_",""))){
						field = item;
						break;
					}
				}
			}
		}

		// 递归父类
		if(null == field && recursion) {
			clazz = clazz.getSuperclass();
			if(null != clazz) {
				field = getField(clazz, name, recursion, ignoreCase);
			}
		}
		return field;
	}
	/**
	 * 根据名称过滤属性
	 * @param fields 属性s
	 * @param name 名称
	 * @param ignoreCase 是否忽略大小写
	 * @param ignoreSplit 是否忽略分隔符号
	 * @return Field
	 */
	public static Field getField(List<Field> fields, String name, boolean ignoreCase, boolean ignoreSplit) {
		if(null == name) {
			return null;
		}
		Field field = null;
		for(Field item:fields) {
			String itemName = item.getName();
			if(ignoreCase) {
				itemName = itemName.toUpperCase();
				name = name.toUpperCase();
			}
			if(ignoreSplit) {
				itemName = itemName.replace("-","").replace("_","");
				name = name.replace("-","").replace("_","");
			}
			if(name.equals(itemName)) {
				field = item;
			}
		}
		return field;
	}
	public static Field getField(Class<?> clazz, String name) {
		return getField(clazz, name, true);
	}

	/**
	 * 查询指定类的有annotation注解的属性
	 * @param clazz  clazz
	 * @param annotation  annotation
	 * @return List
	 */
	@SuppressWarnings({"rawtypes","unchecked" })
	public static List<Field> getFieldsByAnnotation(Class clazz, Class annotation) {
		List<Field> list = new ArrayList<Field>();
		try{
			List<Field> fields = getFields(clazz);
			for(Field field:fields) {
				Annotation at = field.getAnnotation(annotation);
				if(null != at) {
					list.add(field);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 根据注解名称 获取属性上的注解
	 * @param field 属性
	 * @param names 注解名称
	 * @return Annotation
	 */
	public static List<Annotation> getFieldAnnotations(Field field, String ... names) {
		List<Annotation> list = new ArrayList<>();
		Annotation[] annotations = field.getAnnotations();
		for(Annotation annotation:annotations) {
			for(String name:names) {
				if (match(annotation.annotationType().getSimpleName(), name)) {
					list.add(annotation);
					break;
				}
			}
		}
		return list;
	}

	/**
	 * 根据注解名称 获取属性上的注解
	 * @param field 属性
	 * @param names 注解名称
	 * @return Annotation
	 */
	public static Annotation getFieldAnnotation(Field field, String ... names) {
		List<Annotation> list = getFieldAnnotations(field, names);
		if(!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * 查询指定类的有annotation注解的属性
	 * @param clazz  clazz
	 * @param annotations  annotation 支持模糊匹配, 不区分大小写 如 Table*
	 * @return List
	 */
	@SuppressWarnings({"rawtypes","unchecked" })
	public static List<Field> getFieldsByAnnotation(Class clazz, String ... annotations) {
		List<Field> list = new ArrayList<Field>();
		try{
			List<Field> fields = getFields(clazz);
			for(Field field:fields) {
				Annotation[] ans = field.getAnnotations();
				for(Annotation an:ans) {
					for(String annotation:annotations) {
						if(match(an.annotationType().getSimpleName(), annotation)) {
							list.add(field);
							break;
						}
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * pack包下的所有类 不包括jar包中定义类
	 * @param pack  pack
	 * @param bases bases
	 * @return List
	 */

	@SuppressWarnings({"unchecked","rawtypes" })
	public static List<Class> getClasses(String pack, Class ... bases) {
		List<Class> list = new ArrayList<Class>();
		File dir = new File(ClassUtil.class.getResource("/").getFile(), pack.replace(".", File.separator));
		List<File> files = FileUtil.getAllChildrenFile(dir, ".class");
		for(File file:files) {
			try{
				String path = file.getAbsolutePath();
				if(ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
					log.debug("[检索类][file:{}]", path);
				}
				if(path.contains(File.separator+"classes"+File.separator)) {
					path = path.substring(path.indexOf(File.separator+"classes"+File.separator));
				}
				path = path.replace(File.separator, ".");
				path = path.replace(".classes.","").replace(".class","");
				if(ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
					log.debug("[检索类][class:{}]", path);
				}
				Class clazz = Class.forName(path);
				if(clazz.getName().contains("$")) {
					continue;
				}
				if(null != bases && bases.length>0) {
					for(Class base:bases) {
						if(clazz.equals(base)) {
							continue;
						}
						if(base.isAssignableFrom(clazz)) {
							list.add(clazz);
							continue;
						}
					}
				}else{
					list.add(clazz);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * 根据注解名与注解类属性 获取指定类上的注解值
	 * @param clazz clazz上的注解
	 * @param configs 注册名.注解属性名, 不区分大小写 支持模糊匹配 如 *Table.ID*
	 * @return String
	 */
	public static String parseAnnotationFieldValue(Class clazz, String ... configs) {
		for(String config:configs) {
			String[] tmps = config.split("\\.");
			if(tmps.length <2) {
				continue;
			}
			Object name = getAnnotationFieldValue(clazz, tmps[0], tmps[1]);
			if(BasicUtil.isNotEmpty(name)) {
				return name.toString();
			}
		}
		return null;
	}

	/**
	 * 根据注解名与注解类属性 获取指定属性上的注解值
	 * @param field field上的注解
	 * @param configs 注册名.注解属性名, 不区分大小写 支持模糊匹配 如 *Table.ID*
	 *                可以只提供注解名如Column则依次按Column.name, Column.value解析
	 * @return String
	 */
	public static String parseAnnotationFieldValue(Field field, String ... configs) {
		for(String config:configs) {
			String[] tmps = config.split("\\.");
			if(tmps.length >= 2) {
				Object value = getAnnotationFieldValue(field, tmps[0], tmps[1]);
				if(BasicUtil.isNotEmpty(value)) {
					return value.toString();
				}
			}else {
				Object value = getAnnotationFieldValue(field, config, "name");
				if(BasicUtil.isNotEmpty(value)) {
					return value.toString();
				}
				value = getAnnotationFieldValue(field, config, "value");
				if(BasicUtil.isNotEmpty(value)) {
					return value.toString();
				}
			}
		}
		return null;
	}

	/**
	 * 根据属性获取 集合或数组的泛型类
	 * @param field 属性
	 * @return Class
	 */
	public static Class getComponentClass(Field field) {
		//数组
		if(field.getType().isArray()) {
			return field.getType().getComponentType();
		}
		//集合
		Type gtype = field.getGenericType();
		if(gtype instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) gtype;
			Type[] args = pt.getActualTypeArguments();
			if (null != args && args.length > 0 && args[0] instanceof Class) {
				Class itemClass = (Class) args[0];
				return itemClass;
			}
		}else if(gtype instanceof Class) {
			return (Class) gtype;
		}
		return null;
	}


	/**
	 * 根据属性获取 集合或数组的泛型类,如果不同Map类型 只返回class[0]
	 * @param field 属性
	 * @return Class
	 */
	public static Class[] getComponentClasses(Field field) {
		Class[] array = new Class[2];
		//数组
		if(field.getType().isArray()) {
			array[0] = field.getType().getComponentType();
		}
		//集合
		Type gtype = field.getGenericType();
		if(gtype instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) gtype;
			Type[] args = pt.getActualTypeArguments();
			if (null != args) {
				if(args.length > 0 && args[0] instanceof Class) {
					array[0] = (Class) args[0];
				}
				if(args.length > 1 && args[1] instanceof Class) {
					array[1] = (Class) args[1];
				}
			}
		}else if(gtype instanceof Class) {
			array[0] = (Class) gtype;
		}
		return array;
	}

	/**
	 * 集合或数组的泛型类
	 * @param clazz 需要是实例化过的对象getClass()返回的结果
	 * @return Class
	 */
	public static Class getComponentClass(Class clazz) {
		//数组
		if(clazz.isArray()) {
			return clazz.getComponentType();
		}
		//集合
		Type genericSuperclass = clazz.getGenericSuperclass();
		ParameterizedType pty= (ParameterizedType) genericSuperclass;
		Type actualTypeArgument = pty.getActualTypeArguments()[0];
		return (Class)actualTypeArgument;
	}
	public static Class getComponentClass(Object obj) {
		if(null == obj) {
			return null;
		}
		if(obj instanceof Field) {
			return getComponentClass((Field) obj);
		}else if(obj instanceof Class) {
			return getComponentClass((Class)obj);
		}
		if(obj instanceof Collection) {
			Collection col = (Collection) obj;
			for(Object item:col) {
				return item.getClass();
			}
		}else if(obj.getClass().isArray()) {
			return obj.getClass().getComponentType();
		}
		return getComponentClass(obj.getClass());
	}

	/**
	 * 对象类型<br/>
	 * int[] > int[]<br/>
	 * Integer[] > java.long.Integer[]<br/>
	 * @param obj 对象
	 * @return 类型
	 */
	public static String type(Object obj) {
		String type = null;
		if(null != obj) {
			type = type(obj.getClass());
		}
		return type;
	}
	public static String type(Field field) {
		String type = null;
		if(null != field) {
			Class clazz = field.getType();
			type = type(clazz);
		}
		return type;
	}
	public static String type(Class clazz) {
		String type = null;
		if(null != clazz) {
			if (clazz.isArray()) {
				type = clazz.getComponentType().getName() + "[]";
			} else {
				type = clazz.getName();
			}
		}
		return type;
	}
	public static Object newInstance(Class clazz) throws Exception {
		if(!clazz.isInterface()
				&& !clazz.isAnnotation()
				&& !clazz.isEnum()
				&& !clazz.isArray()
				&& !Modifier.isAbstract(clazz.getModifiers())
		) {
			return clazz.newInstance();
		}
		Class implementClass = INTERFACE_IMPLEMENT.get(clazz);
		if(null != implementClass) {
			return implementClass.newInstance();
		}
		if(isInSub(clazz, List.class)) {
			return new ArrayList<>();
		}
		if(isInSub(clazz, Set.class)) {
			return new HashSet();
		}
		if(isInSub(clazz, Collection.class)) {
			return new ArrayList<>();
		}
		if(isInSub(clazz, Map.class)) {
			return new HashMap<>();
		}
		return null;
	}
}
