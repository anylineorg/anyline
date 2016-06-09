
import java.io.File;
import java.util.List;

import org.anyline.util.FileUtil;


public class LicenseUtil {
	private static String license = 
"/* \n"+
" * Copyright 2006-2015 www.anyline.org\n"+
" *\n"+
" * Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
" * you may not use this file except in compliance with the License.\n"+
" * You may obtain a copy of the License at\n"+
" *\n"+
" *      http://www.apache.org/licenses/LICENSE-2.0\n"+
" *\n"+
" * Unless required by applicable law or agreed to in writing, software\n"+
" * distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
" * See the License for the specific language governing permissions and\n"+
" * limitations under the License.\n"+
" *\n"+
" *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统\n"+
" *\n"+
" */\npa";
	public static void main(String args[]){
		addLicense();
		//removeLicense();
	}
	public static void removeLicense(){
		File dir = new File(LicenseUtil.class.getResource("").getPath().replace("WebRoot/WEB-INF/classes", "src/org"));
		List<File> files = FileUtil.getAllChildrenFile(dir, "java");
		for(File file:files){
			String content = FileUtil.readFile(file).toString();
			if(content.contains("Apache License")){
				int fr = content.lastIndexOf("limitations under the License");
				fr = content.indexOf("*/",fr);
				content = content.substring(fr+2);
				FileUtil.writeFile(content, file, false);
			}
		}
	}
	public static void addLicense(){
		File dir = new File(LicenseUtil.class.getResource("").getPath().replace("WebRoot/WEB-INF/classes", "src/org"));
		List<File> files = FileUtil.getAllChildrenFile(dir, "java");
		for(File file:files){
			String content = FileUtil.readFile(file).toString();
			if(!content.contains("Apache License")){
				content = license + content;
			}else{
				int fr = content.lastIndexOf("limitations under the License");
				fr = content.indexOf("*/",fr);
				content = content.substring(fr+4);
				content = license + content;
			}
			FileUtil.writeFile(content, file, false);
		}
	}
}
