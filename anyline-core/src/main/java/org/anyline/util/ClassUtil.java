/* 
 * Copyright 2006-2022 www.anyline.org
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
 *
 *          
 */
package org.anyline.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile; 
 
public class ClassUtil {

	/**
	 * 是否是基础类型(不包含String类型)
	 * @param obj obj
	 * @return boolean
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isPrimitiveClass(Object obj) {
		try {
			Class clazz = null;
			if(obj instanceof Class){
				clazz = (Class) obj;
			}else{
				clazz = obj.getClass();
			}
			if(clazz.isPrimitive() || ((Class<?>)clazz.getField("TYPE").get(null)).isPrimitive()){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			//e.printStackTrace();
			return false;
		}
	}

	/**
	 * 是否是封装类(基础类型之外的类) String类返回true
	 * @param obj obj
	 * @return boolean
	 */
	public static boolean isWrapClass(Object obj){
		return !isPrimitiveClass(obj);
	}
	public static List<Class<?>> list(String packageName, boolean recursion, Class<?> ... bases){
		List<Class<?>> list = new ArrayList<Class<?>>();
		try {
			List<String> names = getClassNameList(packageName, recursion);
			for (String name : names) {
				try {
					Class<?> c = Class.forName(name);
					if (isInSub(c, bases)) {
						list.add(c);
					}
				} catch (Exception e) {

				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return list;
	}

	public static List<String> nameList(String packageName, boolean recursion, Class<?> ... bases){
		List<String> list = new ArrayList<>();
		try {
			List<String> names = getClassNameList(packageName, recursion);
			for (String name : names) {
				try {
					Class<?> c = Class.forName(name);
					if (isInSub(c, bases)) {
						list.add(name);
					}
				} catch (Exception e) {

				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return list;
	}
	/**
	 * 是否是bases子类或实现了basees接口(满足其中一个)
	 * @param c  c
	 * @param bases  bases
	 * @return boolean
	 */
	public static boolean isInSub(Class<?> c, Class<?> ... bases){
		if(null == bases || bases.length == 0){
			return true;
		}
		for(Class<?> base : bases){
			if(!base.isAssignableFrom(c)){
				return false;
			}
		}
		return true;
	}
	/**
	 * 是否是bases子类或实现了basees接口(满足全部)
	 * @param c  c
	 * @param bases  bases
	 * @return boolean
	 */
	public static boolean isAllSub(Class<?> c, Class<?> ... bases){
		if(null == bases || bases.length == 0){
			return true;
		}
		for(Class<?> base : bases){
			if(base.isAssignableFrom(c)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取某包下所有类
	 *
	 * @param packageName  包名
	 * @param childPackage 是否遍历子包
	 * @return 类的完整名称
	 * @throws UnsupportedEncodingException
	 */
	private static List<String> getClassNameList(String packageName, boolean childPackage) throws IOException {
		List<String> fileNames = new ArrayList<>();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String packagePath = packageName.replace(".", "/");
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
	 * @throws UnsupportedEncodingException  UnsupportedEncodingException
	 */
	private static List<String> getClassNameListFromFile(String filePath, boolean childPackage) throws UnsupportedEncodingException {
		List<String> myClassName = new ArrayList<>();
		//filePath = UrlDecode.getURLDecode(filePath);
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
					childFilePath = childFilePath.substring(childFilePath.indexOf("/classes/") + 9, childFilePath.lastIndexOf("."));
					childFilePath = childFilePath.replace("/", ".");
					myClassName.add(childFilePath);
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
	 * @throws UnsupportedEncodingException UnsupportedEncodingException
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
							entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
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
							entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
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
	 * 从所有jar中搜索该包,并获取该包下所有类
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

	/**
	 * 反射注解的属性值 在不确定具体注解与属性的情况下使用
	 * 注解名与属性名不区分大小写
	 * *表示任意字符
	 * @param clazz 类
	 * @param annotation 注解类名 如: *, Table*
	 * @param field 属性名 如: *, value, name, *package*
	 * @param qty 最多取几个值 -1:不限制
	 * @return List
	 */
	public static List<Object> parseAnnotationFieldValues(Class clazz, String annotation, String field, int qty){
		List<Object> list = new ArrayList<>();
		Annotation[] annotations = clazz.getAnnotations();
		for(Annotation an : annotations){
			String name = an.annotationType().getSimpleName();
			if(!match(name, annotation)){
				continue;//注解名不匹配
			}
			Method methods[] = an.annotationType().getMethods();
			for(Method method:methods){
				name = method.getName();
				if(!match(name, field)){
					continue;//属性名不匹配
				}
				try {
					Object value = method.invoke(an);
					if (value instanceof Object[]) {
						Object values[] = (Object[]) value;
						for (Object v : values) {
							list.add(v);
							if(qty >0 && list.size()>=qty){
								return list;
							}
						}
					} else {
						list.add(value);
					}
				}catch (Exception e){
				}
			}

		}
		return list;
	}
	public static List<Object> parseAnnotationFieldValues(Class clazz, String annotation, String field){
		return parseAnnotationFieldValues(clazz, annotation, field, -1);
	}
	public static Object parseAnnotationFieldValue(Class clazz, String annotation, String field){
		List<Object> values = parseAnnotationFieldValues(clazz, annotation, field, 1);
		if(values.size() > 0){
			return values.get(0);
		}
		return null;
	}
	private static boolean match(String value, String regex){
		regex = regex.replace("*",".*").toUpperCase();
		return value.toUpperCase().matches(regex);
	}

} 
