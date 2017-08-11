package org.anyline.qq.mp.util;

import java.net.URLEncoder;
import java.util.Hashtable;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.HttpUtil;
import org.anyline.util.regular.RegularUtil;
import org.apache.log4j.Logger;

public class QQMPUtil {
	private static Logger log = Logger.getLogger(QQMPUtil.class);
	private static Hashtable<String,QQMPUtil> instances = new Hashtable<String,QQMPUtil>();
	private QQMPConfig config = null;
	public static QQMPUtil getInstance(){
		return getInstance("default");
	}
	public static QQMPUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		QQMPUtil util = instances.get(key);
		if(null == util){
			util = new QQMPUtil();
			util.config = QQMPConfig.getInstance(key);
			instances.put(key, util);
		}
		return util;
	}
	public DataRow getOpenId(String code){
		DataRow row = new DataRow();
		String redirect = QQMPConfig.getInstance().OAUTH_REDIRECT_URL;
		try{
			redirect = URLEncoder.encode(redirect, "UTF-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		//1.获取accesstoken
		String url = "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=" + config.APP_ID+"&client_secret="+config.APP_SECRECT+"&code="+code+"&redirect_uri="+redirect;
		String txt = HttpUtil.get(url)+"";
		if(ConfigTable.isDebug()){
			log.warn("[QQ登录][get accesstoken][txt:"+txt+"]");
		}
		//access_token=3442B853808CA8754EE03979AE23E9BB&expires_in=7776000&refresh_token=609BA09BBC0533116694D5F32FC2F8D5
		String accessToken = RegularUtil.cut(txt, "access_token=","&");
		//2.获取openid unionid
		url = "https://graph.qq.com/oauth2.0/me?access_token="+accessToken+"&unionid=1";
		txt = HttpUtil.get(url)+"";
		if(ConfigTable.isDebug()){
			log.warn("[QQ登录][get openid][txt:"+txt+"]");
		}
		//callback( {"client_id":"101420322","openid":"F1B5285FF5FF77DB097474C25273C01F","unionid":"UID_95588F17205C4CFA583DCAF8F0FE89D9"} );
		String openid= RegularUtil.cut(txt, "openid",":","\"","\"");
		String unionid = RegularUtil.cut(txt, "unionid",":","\"","\"");
		row.put("OPENID", openid);
		row.put("UNIONID", unionid);
		return row;
	} 
	public DataRow getUnionId(String code){
		return getOpenId(code);
	}
}
