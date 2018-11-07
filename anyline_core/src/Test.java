import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.AESUtil;
import org.anyline.util.DateUtil;
import org.anyline.util.FileUtil;
import org.anyline.util.ImgUtil;
import org.anyline.util.RSAUtil;

public class Test {

	public static void main(String args[]) {
		// select();	//DataSet 模拟sql
		// fileMD5();	//文件md5验证
		// aes();		//AES加密解密
		 rsa();		//RSA加密解密  签名验签 
		
		//createGif();	//合成gif文件
	}
	/**
	 * aes加密
	 */
	public static void aes() {
		String s = "hello,您好";
		System.out.println("原文:" + s);
		String s1 = AESUtil.encrypt(s, "1234");
		System.out.println("密文:" + s1);
		System.out.println("解密:" + AESUtil.decrypt(s1, "1234"));
	}
	/**
	 * rsa加密
	 */
	public static void rsa() {
		Map<String, String> keyMap = RSAUtil.createKeys(2048);
		String publicKey = keyMap.get("public");
		String privateKey = keyMap.get("private");
		System.out.println("公钥:\n" + publicKey);
		System.out.println("私钥:\n" + privateKey);
		System.out.println("公钥加密:私钥解密");
		try {
			String str = "www.anyline.org+中文&%$";
			System.out.println("明文:" + str);
			System.out.println("明文大小:" + str.getBytes().length);
			String encodedData = RSAUtil.publicEncrypt(str, RSAUtil.getPublicKey(publicKey));
			System.out.println("密文:" + encodedData);
			String decodedData = RSAUtil.privateDecrypt(encodedData, RSAUtil.getPrivateKey(privateKey));
			System.out.println("解密:" + decodedData);

			String sign = RSAUtil.sign(str, privateKey);
			System.out.println("私钥签名:" + sign);
	        boolean status = RSAUtil.verify(str, publicKey, sign);
	        System.err.println("公钥验签:" + status);
		        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * DataSet 模拟sql
	 */
	public static void select() {
		DataSet set = new DataSet();
		for (int i = 8; i <= 12; i++) {
			DataRow row = new DataRow();
			row.put("age", i);
			row.put("date", DateUtil.parse("2018-10-" + i));
			System.out.println(i + ":2018-10-" + i);
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
		System.out.println(set.select.between("age", 9, 11).size());
		System.out.println(set.select.between("date", "2018-10-09", "2018-10-11").size());

		List list = new ArrayList();
		Map<String, String> map = new HashMap<String, String>();
		map.put("age", "123");
		list.add(map);
		System.out.println(DataSet.parse(list));
	}
	/**
	 * 文件md5验证
	 */
	public static void fileMD5() {
		try {
			String base = "";
			base = FileUtil.md5(new File("D:\\a.jpg"));
			// base = ImgUtil.base64(new
			// URL("http://10.16.242.62:11100/briefing/common/upload/20180709/202309017634.png"));
			// System.out.println(base);
			// base = ImgUtil.base64(new
			// URL("http://10.16.242.62:11100/briefing/common/upload/20180709/201843040059.png"));
			System.out.println(base);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void createGif() {
		List<File> files = FileUtil.getAllChildrenFile(new File("D:\\20181103"));
		File localGifPath= new File("D:\\a.gif");
		ImgUtil.createGif(250, localGifPath, files);
	}
}
