package org.anyline.wechat.pay.util.v3;

import org.anyline.entity.DataRow;
import org.anyline.net.HttpResult;
import org.anyline.net.HttpUtil;
import org.anyline.util.Base64Util;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.wechat.entity.*;
import org.anyline.wechat.entity.v3.WechatPrePayOrder;
import org.anyline.wechat.pay.util.WechatPayConfig;
import org.anyline.wechat.util.WechatUtil;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class WechatPayUtil {
    protected static final Logger log = LoggerFactory.getLogger(WechatPayUtil.class);

    private WechatPayConfig config = null;

    private static Hashtable<String, WechatPayUtil> instances = new Hashtable<String, WechatPayUtil>();
    public static WechatPayUtil getInstance(){
        return getInstance("default");
    }
    public WechatPayUtil(WechatPayConfig config){
        this.config = config;
    }
    public WechatPayUtil(String key, DataRow config){
        WechatPayConfig conf = WechatPayConfig.parse(key, config);
        this.config = conf;
        instances.put(key, this);
    }
    public static WechatPayUtil reg(String key, DataRow config){
        WechatPayConfig conf = WechatPayConfig.reg(key, config);
        WechatPayUtil util = new WechatPayUtil(conf);
        instances.put(key, util);
        return util;
    }
    public static WechatPayUtil getInstance(String key){
        if(BasicUtil.isEmpty(key)){
            key = "default";
        }
        WechatPayUtil util = instances.get(key);
        if(null == util){
            WechatPayConfig config = WechatPayConfig.getInstance(key);
            util = new WechatPayUtil(config);
            instances.put(key, util);
        }
        return util;
    }

    public WechatPayConfig getConfig() {
        return config;
    }
    public WechatPrePayResult unifiedorder(WechatPayConfig.TRADE_TYPE type, WechatPrePayOrder order){
        return transactions(type,order);
    }

    /**
     * 统一下单
     * @param type 支付方式
     * @param order 订单
     * @return 结果主要包含prepay_id
     */
    public WechatPrePayResult transactions(WechatPayConfig.TRADE_TYPE type, WechatPrePayOrder order){
        WechatPrePayResult result = new WechatPrePayResult();
        if(BasicUtil.isEmpty(order.getMchid())){
            order.setMchid(config.MCH_ID);
        }
        if(BasicUtil.isEmpty(order.getNotify_url())){
            order.setNotify_url(config.NOTIFY_URL);
        }
        String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/"+type.getApi();
        DataRow row = api(url, BeanUtil.object2json(order));
        result.setPrepay_id(row.getString("PREPAY_ID"));
        result.setAppid(order.getAppid());
        result.setRequest_id(row.getString("REQUEST_ID"));
        result.setRequest_status(row.getString("REQUEST_STATUS"));
        return result;
    }

    /**
     * JSAPI调起支付所需参数
     * @param appid appid
     * @param prepayid 预支付id(由统一下单接口返回)
     * @return return
     */
    public DataRow callUpParam(String appid, String prepayid){
        String timestamp = System.currentTimeMillis()/1000+"";
        String random = BasicUtil.getRandomUpperString(32);

        String pkg = "prepay_id="+prepayid;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("package", pkg);
        params.put("timeStamp", timestamp);
        params.put("appId", appid);
        params.put("nonceStr", random);
        params.put("signType", "RSA");

        try {
            StringBuilder builder = new StringBuilder();
            builder.append(appid).append("\n");
            builder.append(timestamp).append("\n");
            builder.append(random).append("\n");
            builder.append(pkg).append("\n");
            String sign = sign(builder.toString().getBytes("UTF-8"));
            params.put("paySign", sign);
        }catch (Exception e){
            e.printStackTrace();
        }
        DataRow row = new DataRow(params);
        if(ConfigTable.isDebug() && log.isWarnEnabled()){
            log.warn("[jsapi调起微信支付][参数:{}]", row.toJSON());
        }
        return row;
    }

    /**
     * 退款申请
     * @param refund  refund
     * @return WechatRefundResult
     * @throws Exception  Exception
     */
    public WechatRefundResult refund(WechatRefund refund) throws Exception{
        return null;
    }

    /**
     * 企业付款
     * @param transfer  transfer
     * @return return
     */
    public WechatEnterpriseTransferResult transfer(WechatEnterpriseTransfer transfer){
        return null;
    }
    private DataRow api(String url, String json){
       DataRow row = new DataRow();
        try {
            String authorization = "WECHATPAY2-SHA256-RSA2048 " + authorization("POST", url.replace("https://api.mch.weixin.qq.com",""), json);
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            headers.put("Authorization", authorization);
            HttpResult http = HttpUtil.post(headers, url, "UTF-8", new StringEntity(json, "UTF-8"));
            headers = http.getHeaders();
            String requestId = headers.get("Request-ID");
            String txt = http.getText();
            log.warn("[api request][url:{}][text:{}]", url, txt);
            row = DataRow.parseJson(txt);
            row.put("REQUEST_ID", requestId);
            row.put("REQUEST_STATUS", http.getStatus());
        }catch (Exception e){
            e.printStackTrace();
        }
       return row;
    }
    private String authorization(String method, String url, String body) throws Exception{
        long timestamp = System.currentTimeMillis() / 1000;
        String nonce = BasicUtil.getRandomUpperString(32);
        String message =method + "\n"
                + url + "\n"
                + timestamp + "\n"
                + nonce + "\n"
                + body + "\n";
        String signature = sign(message.getBytes("utf-8"));

        return "mchid=\"" + config.MCH_ID + "\","
                + "nonce_str=\"" + nonce + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + config.CERTIFICATE_SERIAL + "\","
                + "signature=\"" + signature + "\"";
    }

    /**
     * 签名
     * @param message message
     * @return String
     * @throws Exception Exception
     */
    public String sign(byte[] message) throws Exception {
        String result = null;
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(getPrivateKey(config.MCH_PRIVATE_SECRET_FILE));
            sign.update(message);
            result = Base64.getEncoder().encodeToString(sign.sign());
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public boolean verifySign(){
        boolean result = false;
        return result;
    }


    /**
     * 获取私钥
     * @param path 文件地址
     * @return PrivateKey
     * @throws IOException IOException
     */
    public  PrivateKey getPrivateKey(String path) throws IOException {
        log.warn("[get private key][file path:{}]",path);
        String content = new String(Files.readAllBytes(Paths.get(path)), "utf-8");
        try {
            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前Java环境不支持RSA", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("无效的密钥格式");
        }
    }

    static final int KEY_LENGTH_BYTE = 32;
    static final int TAG_LENGTH_BIT = 128;
    /**
     * 解密数据
     * @param associated 附加数据包
     * @param nonce 加密使用的随机串初始化向量
     * @param ciphertext Base64编码后的密文
     * @return 明文
     * @throws GeneralSecurityException GeneralSecurityException
     * @throws IOException IOException
     */
    public String decrypt(String associated, String nonce, String ciphertext)
            throws Exception {

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            SecretKeySpec key = new SecretKeySpec(config.API_SECRET_V3.getBytes("UTF-8"), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, nonce.getBytes("UTF-8"));

            cipher.init(Cipher.DECRYPT_MODE, (Key) key, spec);
            cipher.updateAAD(associated.getBytes("UTF-8"));

            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), "utf-8");

    }

}
