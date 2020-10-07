package org.anyline.p10ss.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.net.HttpUtil;
import org.anyline.p10ss.util.P10ssConfig.URL;
import org.anyline.util.BasicUtil;
import org.anyline.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

public class P10ssUtil {
    private static final Logger log = LoggerFactory.getLogger(P10ssUtil.class);

    private static DataSet accessTokens = new DataSet();
    private P10ssConfig config = null;

    private static Hashtable<String, P10ssUtil> instances = new Hashtable<String, P10ssUtil>();
    public static P10ssUtil getInstance(){
        return getInstance("default");
    }
    public P10ssUtil(P10ssConfig config){
        this.config = config;
    }
    public P10ssUtil(String key, DataRow config){
        P10ssConfig conf = P10ssConfig.parse(key, config);
        this.config = conf;
        instances.put(key, this);
    }
    public static P10ssUtil reg(String key, DataRow config){
        P10ssConfig conf = P10ssConfig.reg(key, config);
        P10ssUtil util = new P10ssUtil(conf);
        instances.put(key, util);
        return util;
    }
    public static P10ssUtil getInstance(String key){
        if(BasicUtil.isEmpty(key)){
            key = "default";
        }
        P10ssUtil util = instances.get(key);
        if(null == util){
            P10ssConfig config = P10ssConfig.getInstance(key);
            if(null != config) {
                util = new P10ssUtil(config);
                instances.put(key, util);
            }
        }
        return util;
    }

    public P10ssConfig getConfig() {
        return config;
    }
    private DataRow api(P10ssConfig.URL url, Map<String,Object> params){
        DataRow result = null;
        long time = System.currentTimeMillis()/1000;
        params.put("client_id", config.APP_ID);
        params.put("timestamp",time);
        params.put("sign", sign(time));
        params.put("id",UUID.randomUUID().toString());
        Map<String,String> header = new HashMap<String,String>();
        header.put("Content-Type","application/x-www-form-urlencoded");
        String txt = HttpUtil.post(header,url.getCode(), "UTF-8",params).getText();
        log.warn("[invoike api][result:{}]", txt);
        DataRow row = DataRow.parseJson(txt);
        if(row.getInt("error",-1) ==0){
            result = row.getRow("body");
            if(null == result){
                result = new DataRow();
            }
            result.put("success", true);
        }else{
            result = new DataRow();
            result.put("success", false);
            result.put("error",row.getString("error_description"));
        }
        return result;
    }
    //{'error':'0','error_description':'success','body':{'access_token':'xxxx','refresh_token':'xxxx','expires_in':2592000,'scope':'all'}}
    //自用
    public DataRow getAccessToken(){
        DataRow row = null;
        row = accessTokens.getRow("APP_ID", config.APP_ID);
        if(null == row){
            row = newAccessToken();
        }else if(row.isExpire()){//过期刷新
            row = refreshAccessToken(row.getString("refresh_token"));
        }
        return row;

    }
    //开放平台
    public DataRow getAccessToken(String code){
        DataRow row = null;
        row = newAccessToken(code);
        return row;
    }
    //自用
    public DataRow newAccessToken(){
        DataRow row = null;
        if(BasicUtil.isEmpty(config.ACCESS_TOKEN_SERVER)){
            Map<String,Object> params = new HashMap<String,Object>();
            params.put("grant_type", "client_credentials");
            params.put("scope","all");
            row = api(P10ssConfig.URL.ACCESS_TOKEN, params);
            log.warn("[new access token][token:{}]", row);
        }else{
            String url = config.ACCESS_TOKEN_SERVER+ "?appid="+config.APP_ID+"&secret="+config.APP_SECRET;

            String text = HttpUtil.post(url,"UTF-8").getText();
            row = DataRow.parseJson(text);
            log.warn("[new access token][server:{}][token:{}]", config.ACCESS_TOKEN_SERVER, row);
        }
        row.put("APP_ID", config.APP_ID);
        accessTokens.addRow(row);
        return row;
    }
    //开放平台
    public DataRow newAccessToken(String code){
        DataRow row = null;
//        if(BasicUtil.isEmpty(config.ACCESS_TOKEN_SERVER)){
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("grant_type", "authorization_code");			//开放平台
        params.put("code",code);
        params.put("scope","all");
        row = api(P10ssConfig.URL.ACCESS_TOKEN, params);
        log.warn("[new access token][code:{}][token:{}]", code, row);
/*        }else{
            String url = config.ACCESS_TOKEN_SERVER+ "?appid="+config.APP_ID+"&secret="+config.APP_SECRET;
            if(BasicUtil.isNotEmpty(code)){
                url += "&code="+code;
            }
            String text = HttpUtil.post(url,"UTF-8").getText();
            row = DataRow.parseJson(text);
            log.warn("[new access token][server:{}][code:{}][token:{}]", config.ACCESS_TOKEN_SERVER, code, row);
        }*/
        row.put("APP_ID", config.APP_ID);
        return row;
    }
    public DataRow refreshAccessToken(String refresh){
        DataRow row = null;
        // if(BasicUtil.isEmpty(config.ACCESS_TOKEN_SERVER)){
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("grant_type", "refresh_token");
        params.put("scope", "all");
        params.put("refresh_token", refresh);
        row = api(URL.ACCESS_TOKEN, params);
/*        }else{
        	String url = config.ACCESS_TOKEN_SERVER+ "?appid="+config.APP_ID+"&secret="+config.APP_SECRET;
            url += "&refresh="+refresh;
            String text = HttpUtil.post(url,"UTF-8").getText();
            row = DataRow.parseJson(text);
            log.warn("[refresh access token][server:{}][refresh:{}][token:{}]", config.ACCESS_TOKEN_SERVER, refresh, row);
        }*/
        return row;
    }

    /**
     * 获取用户授权码
     * @param redirect 回调地址
     * @param state 状态保持
     * @return url url
     */
    public String createAuthorizeCodeUrl(String redirect, String state){
        try {
            redirect = URLEncoder.encode(redirect, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        String url = "https://open-api.10ss.net/oauth/authorize?response_type=code&client_id="+config.APP_ID+"&redirect_uri="+redirect+"&state="+state;
        return url;
    }
    /**
     * 自用模式 添加打印机
     * @param code 打印机编号
     * @param secret 打印机密钥
     * @param phone phone
     * @param name name
     * @throws Exception 添加失败时异常
     */
    public void addPrinter(String code, String secret, String phone, String name) throws Exception{
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("machine_code", code);
        params.put("msign", secret);
        if(BasicUtil.isNotEmpty(phone)) {
            params.put("phone", phone);
        }
        if(BasicUtil.isNotEmpty(name)) {
            params.put("print_name", name);
        }
        DataRow token = getAccessToken();
        params.put("access_token", token.getString("access_token"));
        DataRow row = api(P10ssConfig.URL.ADD_PRINTER, params);
        if(!row.getBoolean("success",false)){
            throw new Exception(row.getString("error"));
        }
    }
    public void addPrinter(String code, String secret) throws Exception{
        addPrinter(code, secret, null, null);
    }

    /**
     * 文本打印
     * @param machine machine
     * @param order order
     * @param text text
     * @throws Exception Exception
     * @return DataRow
     */
    public DataRow print(String machine, String order, String text) throws Exception{
        DataRow token = getAccessToken();
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("machine_code", machine);
        if(BasicUtil.isNotEmpty(order)) {
            params.put("origin_id", order);
        }
        params.put("content",text);
        params.put("access_token", token.getString("access_token"));
        return api(URL.PRINT_TEXT, params);
    }
    private String sign(long time){
        String result = MD5Util.crypto(config.APP_ID+time+config.APP_SECRET).toLowerCase();
        return result;
    }
}
