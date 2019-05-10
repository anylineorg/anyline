/* 
 * Copyright 2006-2015 www.anyline.org
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
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {
	public static List<Class<?>> getClassList(String packageName, boolean recursion, Class<?> ... bases){
		List<Class<?>> list = new ArrayList<Class<?>>();
		List<String> names = getClassNameList(packageName, recursion);
		for(String name:names){
			try{
				Class<?> c = Class.forName(name);
				if(isInSub(c, bases)){
					list.add(c);
				}
			}catch(Exception e){
				
			}
		}
		return list;
	}
	/**
	 * 是否是bases子类或实现了basees接口(满足其中一个)
	 * @param c
	 * @param bases
	 * @return
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
	 * @param c
	 * @param bases
	 * @return
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
	 * @param packageName
	 *            包名
	 * @param recursion
	 *            是否遍历子包
	 * @return 类的完整名称
	 */
	public static List<String> getClassNameList(String packageName, boolean recursion) {
		List<String> classNames = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String packagePath = packageName.replace(".", "/");

		URL url = loader.getResource(packagePath);
		if (url != null) {
			String protocol = url.getProtocol();
			if (protocol.equals("file")) {
				classNames = getClassNameListFromDir(url.getPath(), packageName, recursion);
			} else if (protocol.equals("jar")) {
				JarFile jarFile = null;
				try {
					jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (jarFile != null) {
					getClassNameListFromJar(jarFile.entries(), packageName, recursion);
				}
			}
		} else {
			/* 从所有的jar包中查找包名 */
			classNames = getClassNameFromJars(((URLClassLoader) loader).getURLs(), packageName, recursion);
		}

		return classNames;
	}

	/**
	 * 从项目文件获取某包下所有类
	 * 
	 * @param filePath
	 *            文件路径
	 * @param className
	 *            类名集合
	 * @param isRecursion
	 *            是否遍历子包
	 * @return 类的完整名称
	 */
	private static List<String> getClassNameListFromDir(String filePath, String packageName, boolean isRecursion) {
		List<String> className = new ArrayList<String>();
		File file = new File(filePath);
		File[] files = file.listFiles();
		for (File childFile : files) {
			if (childFile.isDirectory()) {
				if (isRecursion) {
					className.addAll(getClassNameListFromDir(childFile.getPath(), packageName + "." + childFile.getName(), isRecursion));
				}
			} else {
				String fileName = childFile.getName();
				if (fileName.endsWith(".class") && !fileName.contains("$")) {
					className.add(packageName + "." + fileName.replace(".class", ""));
				}
			}
		}

		return className;
	}

	/**
	 * @param jarEntries
	 * @param packageName
	 * @param isRecursion
	 * @return
	 */
	private static Set<String> getClassNameListFromJar(Enumeration<JarEntry> jarEntries, String packageName, boolean isRecursion) {
		Set<String> classNames = new HashSet<String>();

		while (jarEntries.hasMoreElements()) {
			JarEntry jarEntry = jarEntries.nextElement();
			if (!jarEntry.isDirectory()) {
				/*
				 * 这里是为了方便，先把"/" 转成 "." 再判断 ".class" 的做法可能会有bug (FIXME: 先把"/" 转成
				 * "." 再判断 ".class" 的做法可能会有bug)
				 */
				String entryName = jarEntry.getName().replace("/", ".");
				if (entryName.endsWith(".class") && !entryName.contains("$") && entryName.startsWith(packageName)) {
					entryName = entryName.replace(".class", "");
					if (isRecursion) {
						classNames.add(entryName);
					} else if (!entryName.replace(packageName + ".", "").contains(".")) {
						classNames.add(entryName);
					}
				}
			}
		}

		return classNames;
	}

	/**
	 * 从所有jar中搜索该包，并获取该包下所有类
	 * 
	 * @param urls
	 *            URL集合
	 * @param packageName
	 *            包路径
	 * @param isRecursion
	 *            是否遍历子包
	 * @return 类的完整名称
	 */
	private static List<String> getClassNameFromJars(URL[] urls, String packageName, boolean isRecursion) {
		List<String> classNames = new ArrayList<String>();

		for (int i = 0; i < urls.length; i++) {
			String classPath = urls[i].getPath();

			// 不必搜索classes文件夹
			if (classPath.endsWith("classes/")) {
				continue;
			}

			JarFile jarFile = null;
			try {
				jarFile = new JarFile(classPath.substring(classPath.indexOf("/")));
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (jarFile != null) {
				classNames.addAll(getClassNameListFromJar(jarFile.entries(), packageName, isRecursion));
			}
		}

		return classNames;
	}
}
