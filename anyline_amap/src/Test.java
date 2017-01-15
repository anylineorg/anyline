import java.util.HashMap;
import java.util.Map;

import org.anyline.amap.util.AmapUtil;
import org.anyline.util.ConfigTable;


public class Test {

	public static void main(String[] args) {
		ConfigTable.put("AMAP_CONFIG_FILE", "D:\\develop\\git\\anyline\\anyline_amap\\config\\anyline-amap.xml");
//		create(BasicUtil.getRandomCnString(3), BasicUtil.getRandomNumber(100, 120)+".00","35.00",BasicUtil.getRandomCnString(15));
//		create(BasicUtil.getRandomCnString(3), "110.00",BasicUtil.getRandomNumber(25, 40)+".00",BasicUtil.getRandomCnString(15));
//		//update("9","test","120.00","35.00","山东青岛");
//		//delete("18");
//		//createMap("aa");
//		DataSet set = local("地","","","",100, 1);
//		System.out.println(set.toJSON());
		Map<String,String> map = new HashMap<String,String>();
		map.put("DATA_SORT_ID", "");
		AmapUtil.defaultInstance().update("99", "新NAMsssssE", "112","22", "山青", map);
	}
}
