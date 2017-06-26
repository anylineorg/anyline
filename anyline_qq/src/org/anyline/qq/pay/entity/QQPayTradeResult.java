package org.anyline.qq.pay.entity;
/**
 * 统一下单返回结果
 */
public class QQPayTradeResult {
	private String 	return_code		; //返回状态码
	private String 	return_msg		; //返回信息
	private String 	retcode			; //手Q CGI原始错误码
	private String 	retmsg			; //手Q CGI原始错误信息
	private String 	appid			; //应用ID
	private String 	mch_id			; //商户号
	private String 	sign			; //商户签名算法规则
	private String 	result_code		; //业务结果SUCCESS/FAIL
	private String 	err_code		; //错误代码
	private String 	err_code_desc	; //错误代码描述
	private String 	nonce_str		; //随机字符串
	private String 	trade_type		; //支付场景
	private String 	prepay_id		; //QQ钱包的预支付会话标识
	private String 	code_url		; //二维码链接
	
	public String getReturn_code() {
		return return_code;
	}
	public void setReturn_code(String return_code) {
		this.return_code = return_code;
	}
	public String getReturn_msg() {
		return return_msg;
	}
	public void setReturn_msg(String return_msg) {
		this.return_msg = return_msg;
	}
	public String getRetcode() {
		return retcode;
	}
	public void setRetcode(String retcode) {
		this.retcode = retcode;
	}
	public String getRetmsg() {
		return retmsg;
	}
	public void setRetmsg(String retmsg) {
		this.retmsg = retmsg;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getMch_id() {
		return mch_id;
	}
	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getResult_code() {
		return result_code;
	}
	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}
	public String getErr_code() {
		return err_code;
	}
	public void setErr_code(String err_code) {
		this.err_code = err_code;
	}
	public String getErr_code_desc() {
		return err_code_desc;
	}
	public void setErr_code_desc(String err_code_desc) {
		this.err_code_desc = err_code_desc;
	}
	public String getNonce_str() {
		return nonce_str;
	}
	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}
	public String getTrade_type() {
		return trade_type;
	}
	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}
	public String getPrepay_id() {
		return prepay_id;
	}
	public void setPrepay_id(String prepay_id) {
		this.prepay_id = prepay_id;
	}
	public String getCode_url() {
		return code_url;
	}
	public void setCode_url(String code_url) {
		this.code_url = code_url;
	}
}
