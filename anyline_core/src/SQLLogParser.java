import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.anyline.util.FileUtil;
import org.anyline.util.regular.RegularUtil;


public class SQLLogParser {
	public static void main(String args[]) throws Exception{
		//修改文件格式:UTF-8
		List<SQLLog> logs = parse(new File("E:\\a.txt"));
		Collections.sort(logs, new Comparator<SQLLog>() {  
            public int compare(SQLLog o1, SQLLog o2) {  
                int result = o1.getExeTime() - o2.getExeTime();  
                return result;  
            }  
        });  
		for(SQLLog log:logs){
			System.out.println("SQL:"+log.getKey() + " 耗时:"+log.getExeTime());
		}
	}
	public static List<SQLLog> parse(File file) throws Exception{
		List<SQLLog> logs = new ArrayList<SQLLog>();
		if(null == file || !file.exists()){
			System.out.println("文件不存在 ");
			return logs;
		}
		String content = FileUtil.readFile(file).toString();
		String regex = "\\[SQL:(\\d+?-\\d+?)\\]\\[TXT";
		List<String> heads = RegularUtil.fetch(content, regex,2,1);
		for(String head:heads){
			String tag = "[SQL:"+head+"][TXT:";
			String txt = RegularUtil.cut(content,tag,"]");
			tag = "[SQL:"+head+"][参数:[";
			String param = RegularUtil.cut(content,tag,"]");
			tag = "[SQL:"+head+"][执行耗时:";
			String exeTime = RegularUtil.cut(content,tag,"ms");
			tag = "[SQL:"+head+"][封装耗时:";
			String packTime = RegularUtil.cut(content,tag,"ms");
			String rows = RegularUtil.cut(content,tag, "行数:","]");
			
			SQLLog sql = new SQLLog();
			sql.setKey(head);
			sql.setExeTime(exeTime);
			sql.setPackTime(packTime);
			sql.setParams(param);
			sql.setRows(rows);
			logs.add(sql);
			if(sql.getExeTime() > 100){
				System.out.println("SQL:"+sql.getKey() + " 耗时:"+sql.getExeTime());
			}
		}
		return logs;
	}
	
}
