package org;

import java.io.File;
import java.util.List;

import org.anyline.util.FileUtil;

public class Test {
	public static void main(String args[]){
		List<File> files = FileUtil.getAllChildrenFile(new File("D:\\develop\\git\\anyboot"), "java");
		for(File file:files){
			String nm = file.getName().replace(".java", "");
			String path = file.getAbsolutePath();
			String pk = path.substring(path.indexOf("org\\"), path.lastIndexOf("\\")).replace("\\", ".");
			String txt = "package "+pk+";\n\n"
					+"public class "+nm+" extends "+pk.replace("anyboot", "anyline")+"."+nm.replace("Anyboot", "Anyline")+"{\n}";
			System.out.println(txt);
			FileUtil.write(txt, file);
		}
	}
}
/*package org.anyboot.easemob.util;
public class EasemobConfig extends BasicConfig{*/