import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.FileUtil;


public class Tmp {
	public static void main(String ars[]){
		List<File> files = FileUtil.getAllChildrenFile(new File("E:\\qq"), ".txt");
		String txt = "";
		for(File file:files){
			txt += "\n" + FileUtil.readFile(file);
		}
		String list[] = txt.split("\n");
		int idx = 0;
		DataSet set = new DataSet();
		List<String> result = new ArrayList<String>();
		for(String l:list){
			l = l.trim();
			if(BasicUtil.isNotEmpty(l)){
				DataRow row = new DataRow();
				String tmps[] = l.split(":");
				String qq = tmps[0];
				if(tmps.length>1){
					String pwd = tmps[1];
				}
				if(set.getRows("QQ",qq).isEmpty()){
					row.put("QQ", qq);
					set.addRow(row);
					System.out.println(idx+++"="+l);
					result.add(l);
				}
			}
		}
		Collections.sort(result);
		for(String s:result){
			System.out.println(s);
		}
	}
}
