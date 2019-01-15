import java.io.File;

import org.anyline.aliyun.oss.util.OSSUtil;


public class Test {

	public static void main(String args[]) {
		oss();
		
	}
	public static void oss(){
		OSSUtil util = OSSUtil.getInstance();
		util.upload(new File("E:\\img\\ship"), "/pro/mirror/img/ship");
	}
}
