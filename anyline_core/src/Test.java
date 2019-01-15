import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.AESUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.CodeUtil;
import org.anyline.util.DateUtil;
import org.anyline.util.FTPUtil;
import org.anyline.util.FileUtil;
import org.anyline.util.HttpUtil;
import org.anyline.util.ImgUtil;
import org.anyline.util.RSAUtil;
import org.anyline.util.VerifyCodeUtil;
import org.anyline.util.ZipUtil;

public class Test {

	public static void main(String args[]) {
		// select(); //DataSet 模拟sql
		// fileMD5(); //文件md5验证
		// aes(); //AES加密解密
		// rsa(); //RSA加密解密 签名验签

		// createGif(); //合成gif文件
		// code(); //编码转换
		// amap(); //高德
		// ftp();
		// verify(); //验证码
		// zip(); //压缩
		// dispatchItems();
		// upload();
		
	}
	public static void upload(){
		Map<String,File> files = new HashMap<String,File>();
		files.put("file", new File("D:\\admin.css"));
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("token", "s");
		String txt = HttpUtil.upload("http://www.xxx.com", files, params);
		System.out.println(txt);
	}
	public static void dispatchItems() {
		DataSet set = new DataSet();
		for (int i = 0; i < 10; i++) {
			DataRow row = new DataRow();
			row.put("ID", i);
			if (i > 0) {
				int base = BasicUtil.getRandomNumber(0, i);
				if (base != i) {
					row.put("BASE_ID", base);
				}
			}
			set.add(row);
		}
		set.dispatchItems(true, "ID:BASE_ID");
		System.out.println(set.toJSON());
	}

	public static void sql() {
		// try {
		// String reg = SQL.SQL_PARAM_VAIRABLE_REGEX;
		// String text = "SELECT * FROM TAB WHERE ID=:ID "
		// + "\nAND TYPE IN(:TYPE) "
		// + "\nAND SORT = ::SORT "
		// + "\nAND NM LIKE ':NM%' "
		// + "\nAND CODE LIEK '%' + :CODE + '%' "
		// + "\nAND CODE LIEK '%' + ::CODE + '%'";
		// List<List<String>> keys = RegularUtil.fetch(text, reg,
		// Regular.MATCH_MODE.CONTAIN);
		// for(List<String> ks:keys){
		// int i = 0;
		// for(String k:ks){
		// System.out.print(i+++".["+k+"]\t\t");
		// }
		// System.out.println("");
		// }
		// System.out.println("======================");
		// reg = SQL.SQL_PARAM_VAIRABLE_REGEX_EL;
		// text = "SELECT * FROM TAB WHERE ID=${ID} "
		// + "\nAND TYPE IN(${TYPE}) "
		// + "\nAND SORT = '${SORT}' "
		// + "\nAND NM LIKE '%${NM}%' "
		// + "\nAND CODE LIKE CONTAT('%', ${CODE},'%')";
		// keys = RegularUtil.fetch(text, reg, Regular.MATCH_MODE.CONTAIN);
		// for(List<String> ks:keys){
		// int i = 0;
		// for(String k:ks){
		// System.out.print(i+++".["+k+"]\t\t");
		// }
		// System.out.println("");
		// }
		// System.out.println("----------------------");
		// reg = SQL.SQL_PARAM_VAIRABLE_REGEX_EL;
		// text = "SELECT * FROM TAB WHERE ID={ID} "
		// + "\nAND TYPE IN ({TYPE}) "
		// + "\nAND SORT = '{SORT}' "
		// + "\nAND NM LIKE '%{NM}%' "
		// + "\nAND NM LIKE '%{NM}' "
		// + "\nAND CODE LIKE CONTAT('%', {CODE},'%')";
		// keys = RegularUtil.fetch(text, reg, Regular.MATCH_MODE.CONTAIN);
		// for(List<String> ks:keys){
		// int i = 0;
		// for(String k:ks){
		// System.out.print(i+++".["+k+"]\t\t");
		// }
		// System.out.println("");
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

	}

	public static void zip() {
		File zip = new File("D:\\aaa.zip");
		List<File> files = new ArrayList<File>();
		files.add(new File("D:\\spark-md5.js"));
		ZipUtil.zip(files, zip, "cc");
	}

	public static void verify() {
		try {
			File dir = new File("F:/verify");
			FileUtil.deleteDir(dir);
			dir.mkdirs();
			int w = 200, h = 80;
			for (int i = 0; i < 50; i++) {
				String verifyCode = VerifyCodeUtil.getRandomCode(4);
				File file = new File(dir, verifyCode + ".jpg");
				VerifyCodeUtil.outputImage(w, h, file, verifyCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ftp() {
		String host = "XX.XX.XX.XX";
		String user = "XX@XX.com";
		String password = "XX";
		FTPUtil util = new FTPUtil(host, user, password);
		// util.download("img111", "img2.gif", "D:\\zzz",null);
		// util.download("img", "D:\\img");
		System.out.println(util.fileSize("/js"));
	}

	public static void amap() {
		// System.out.println(BasicUtil.cut("370212",0,4));
		// ConfigTable.setDebug(true);
		// AmapConfig.setConfigDir(new
		// File("D:\\develop\\git\\anyline\\anyline_amap\\config\\anyline-amap.xml"));
		// System.out.println(AmapUtil.getInstance().regeo("111.86413224156792,10.243679193560395"));
		// AmapUtil util = AmapUtil.getInstance();
		// MapLocation fr = util.geo("山东省青岛市香港中路11号");

		// MapLocation to = util.geo("山东省青岛市流亭国际机场");
		// MapLocation mid1 = util.geo("山东省青岛市市南区延安路1号");
		// MapLocation mid2 = util.geo("山东省青岛市市南区动漫产业园");
		// double distance0 =0;
		// double distance1 =0;
		// double distance2 =0;
		// DataRow row0 =
		// util.directionDrive(fr.getLocation(),to.getLocation());
		// if(null != row0){
		// distance0 = row0.getDouble("distance");
		// }
		// DataRow row1 = util.directionDrive(fr.getLocation(),to.getLocation(),
		// mid1.getLocation());
		// if(null != row1){
		// distance1 = row1.getDouble("distance");
		// }
		// DataRow row2 =
		// util.directionDrive(fr.getLocation(),to.getLocation(),mid2.getLocation());
		// if(null != row2){
		// distance2 = row2.getDouble("distance");
		// }
		// System.out.println("原路线:"+distance0+"米");
		// System.out.println("路线1:"+distance1+"米");
		// System.out.println("路线2:"+distance2+"米");
		// System.out.println("路线1相差:"+Math.abs(distance1 - distance0)+"米");
		// System.out.println("路线2相差:"+Math.abs(distance2 - distance0)+"米");
		//
	}

	public static void code() {
		String str = "中s12_*";
		System.out.println("src\t\t:" + str);
		str = CodeUtil.string2unicode(str);
		System.out.println("string2unicode\t:" + str);
		str = CodeUtil.unicode2string(str);
		System.out.println("unicode2string\t:" + str);
		str = CodeUtil.string2ascii(str);
		System.out.println("string2ascii\t:" + str);
		str = CodeUtil.ascii2string(str);
		System.out.println("ascii2string\t:" + str);
		str = CodeUtil.escape(str);
		System.out.println("escape\t\t:" + str);
		str = CodeUtil.unescape(str);
		System.out.println("unescape\t:" + str);
		String url = "http://www.anyline.org?a=1&b=2&name=张";
		url = CodeUtil.urlEncode(url);
		System.out.println("url encoder\t:" + url);
		url = CodeUtil.urlDecode(url);
		System.out.println("url decoder\t:" + url);
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
		File localGifPath = new File("D:\\a.gif");
		ImgUtil.createGif(250, localGifPath, files);
	}
}
