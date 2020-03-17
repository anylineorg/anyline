package org.anyline.wechat.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 分帐
 */
public class WechatProfit {
    private String mch_id				;  //商户号			    是	string(32)	1900000100	微信支付分配的商户号
    private String sub_mch_id			;  //子商户号			是	string(32)	1900000109	微信支付分配的子商户号
    private String appid				;  //公众账号ID			是	string(32)	wx8888888888888888	微信分配的公众账号ID
    private String sub_appid			;  //子商户公众账号ID	否	string(32)	wx8888888888888888	微信分配的子商户公众账号ID
    private String nonce_str			;  //随机字符串			是	string(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，不长于32位。推荐随机数生成算法
    private String sign					;  //签名				是	string(64)	C380BEC2BFD727A4B6845133519F3AD6C380BEC2BFD727A4B6845133519F3AD6	签名，详见签名生成算法
    private String sign_type			;  //签名类型			否	string(32)	HMAC-SHA256	签名类型，目前只支持HMAC-SHA256
    private String transaction_id		;  //微信订单号			是	string(32)	4.20845E+27	微信支付订单号
    private String out_order_no			;  //商户分账单号		是	string(64)	P20150806125346	服务商系统内部的分账单号，在服务商系统内部唯一（单次分账、多次分账、完结分账应使用不同的商户分账单号），同一分账单号多次请求等同一次。只能是数字、大小写字母_-|*@ 
    private List<WechatProfitReceiver> receivers = new ArrayList<WechatProfitReceiver>();//接收方列表

    public WechatProfit addReceiver(WechatProfitReceiver receiver){
        receivers.add(receiver);
        return this;
    }
    public String getMch_id() {
        return mch_id;
    }

    public void setMch_id(String mch_id) {
        this.mch_id = mch_id;
    }

    public String getSub_mch_id() {
        return sub_mch_id;
    }

    public void setSub_mch_id(String sub_mch_id) {
        this.sub_mch_id = sub_mch_id;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSub_appid() {
        return sub_appid;
    }

    public void setSub_appid(String sub_appid) {
        this.sub_appid = sub_appid;
    }

    public String getNonce_str() {
        return nonce_str;
    }

    public void setNonce_str(String nonce_str) {
        this.nonce_str = nonce_str;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSign_type() {
        return sign_type;
    }

    public void setSign_type(String sign_type) {
        this.sign_type = sign_type;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getOut_order_no() {
        return out_order_no;
    }

    public void setOut_order_no(String out_order_no) {
        this.out_order_no = out_order_no;
    }

    public List<WechatProfitReceiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<WechatProfitReceiver> receivers) {
        this.receivers = receivers;
    }
}
