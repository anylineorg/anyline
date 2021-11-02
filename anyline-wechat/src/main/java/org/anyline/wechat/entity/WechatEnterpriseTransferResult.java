package org.anyline.wechat.entity;
 
public class WechatEnterpriseTransferResult {
	private String return_code		;// 返回状态码		是	SUCCESS	String(16)	SUCCESS/FAIL此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断 
	private String return_msg		;// 返回信息		否	签名失败	String(128)	返回信息，如非空，为错误原因 签名失败 参数格式校验错误 
	//以下字段在return_code为SUCCESS的时候有返回 
	private String mch_appid		;// 商户appid	是	wx8888888888888888	String(128)	申请商户号的appid或商户号绑定的appid（企业号corpid即为此appId） 
	private String mchid			;// 商户号		是	1900000109	String(32)	微信支付分配的商户号 
	private String device_info		;// 设备号		否	013467007045764	String(32)	微信支付分配的终端设备号， 
	private String nonce_str		;// 随机字符串		是	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	String(32)	随机字符串，不长于32位 
	private String result_code		;// 业务结果		是	SUCCESS	String(16)	SUCCESS/FAIL，注意：当状态为FAIL时，存在业务结果未明确的情况，所以如果状态FAIL，请务必再请求一次查询接口[请务必关注错误代码（err_code字段），通过查询查询接口确认此次付款的结果。]，以确认此次付款的结果。 
	private String err_code			;// 错误代码		否	SYSTEMERROR	String(32)	错误码信息，注意：出现未明确的错误码时（SYSTEMERROR等）[出现系统错误的错误码时（SYSTEMERROR），请务必用原商户订单号重试，或通过查询接口确认此次付款的结果。]，请请务必再请求一次查询接口，以确认此次付款的结果。 
	private String err_code_des		;// 错误代码描述	否	系统错误	String(128)	结果信息描述 
	//以下字段在return_code 和result_code都为SUCCESS的时候有返回 
	private String partner_trade_no	;// 商户订单号		是	1217752501201407033233368018	String(32)	商户订单号，需保持历史全局唯一性(只能是字母或者数字，不能包含有其他字符) 
	private String payment_no		;// 微信付款单号	是	1007752501201407033233368018	String(64)	企业付款成功，返回的微信付款单号 
	private String payment_time		;// 付款成功时间	是	2015-05-19 15：26：59	String(32)	企业付款成功时间 
	 
	public WechatEnterpriseTransferResult(){
		 
	} 
	public WechatEnterpriseTransferResult(boolean result, String msg){
		if(result){ 
			this.return_code = "SUCCESS"; 
		}else{ 
			this.return_code = "FAIL"; 
		} 
		this.return_msg = msg; 
	} 
	 
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
	public String getMch_appid() { 
		return mch_appid; 
	} 
	public void setMch_appid(String mch_appid) { 
		this.mch_appid = mch_appid; 
	} 
	public String getMchid() { 
		return mchid; 
	} 
	public void setMchid(String mchid) { 
		this.mchid = mchid; 
	} 
	public String getDevice_info() { 
		return device_info; 
	} 
	public void setDevice_info(String device_info) { 
		this.device_info = device_info; 
	} 
	public String getNonce_str() { 
		return nonce_str; 
	} 
	public void setNonce_str(String nonce_str) { 
		this.nonce_str = nonce_str; 
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
	public String getErr_code_des() { 
		return err_code_des; 
	} 
	public void setErr_code_des(String err_code_des) { 
		this.err_code_des = err_code_des; 
	} 
	public String getPartner_trade_no() { 
		return partner_trade_no; 
	} 
	public void setPartner_trade_no(String partner_trade_no) { 
		this.partner_trade_no = partner_trade_no; 
	} 
	public String getPayment_no() { 
		return payment_no; 
	} 
	public void setPayment_no(String payment_no) { 
		this.payment_no = payment_no; 
	} 
	public String getPayment_time() { 
		return payment_time; 
	} 
	public void setPayment_time(String payment_time) { 
		this.payment_time = payment_time; 
	} 
	 
} 
