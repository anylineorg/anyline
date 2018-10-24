import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.DateUtil;


public class DemoTest {

	public static void main(String args[]){
		select();
	}
	public static void select(){

		DataSet set = new DataSet();
		for(int i=8; i<=12; i++){
			DataRow row = new DataRow();
			row.put("age", i);
			row.put("date", DateUtil.parse("2018-10-"+i));
			System.out.println(i+":2018-10-"+i);
			set.addRow(row);
		}
		System.out.println("<10");
		System.out.println(set.select.less("age", 10).size());
		System.out.println(set.select.less("date", "2018-10-10").size());
		System.out.println("<=10");
		System.out.println(set.select.lessEqual("age", 10).size());
		System.out.println(set.select.lessEqual("date", "2018-10-10").size());
		System.out.println(">10");
		System.out.println(set.select.greater("age", 10).size());
		System.out.println(set.select.greater("date", "2018-10-10").size());
		System.out.println(">=10");
		System.out.println(set.select.greaterEqual("age", 10).size());
		System.out.println(set.select.greaterEqual("date", "2018-10-10").size());
		System.out.println("9-11");
		System.out.println(set.select.between("age", 9,11).size());
		System.out.println(set.select.between("date", "2018-10-09", "2018-10-11").size());
	}
}
