package org.anyline.qq.open.entity; 
/** 
 * notify 结果 
 * 
 */ 
public class QQPayTradeNotify { 
	private String appid			; //应用ID	String(32)	否	腾讯开放平台审核通过的应用APPID或腾讯公众平台审核通过的公众号APPID	1007033799 
	private String mch_id			; //商户号	String(32)	是	QQ钱包分配的商户号	1900000109 
	private String nonce_str		; //随机字符串	String(32)	是	随机字符串，不长于32位	3e5a036cb4bc3a677a38ad9d69eb3feb 
	private String sign				; //签名	String(128)	是	商户签名，详见商户签名算法规则C380BEC2BFD727A4B6845133519F3AD6 
	private String device_info		; //设备号	String(32)	否	调用接口提交的终端设备号	013467007045764 
	private String trade_type		; //支付场景	String(16)	是	MICROPAY、APP、JSAPI、NATIVE	MICROPAY 
	private String trade_state		; //支付状态	String(32)	是	固定值Success	Success 
	private String bank_type		; //付款银行	String(16)	是	银行类型，采用字符串类型的银行卡标识	CCB_DEBIT 
	private String fee_type			; //货币类型	String(16)	是	默认为人民币：CNY	CNY 
	private String total_fee		; //订单金额	Int	是	商户订单总金额，单位为分，只能为整数，详见交易金额 
	private String cash_fee			; //用户支付金额	Int	是	用户本次交易中，实际支付的金额	666 
	private String coupon_fee		; //QQ钱包优惠金额	Int	否	本次交易中，QQ钱包提供的优惠金额	222 
	private String transaction_id	; //QQ钱包订单号	String(32)	是	QQ钱包订单号	1353933301461607211903715555 
	private String out_trade_no		; //商户订单号	String(32)	是	商户系统内部的订单号	20150806125346 
	private String attach			; //附加数据	String(128)	否	附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据	说明 
	private String time_end			; //支付完成时间	String(14)	是	订单支付时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010	20141030133525 
	private String openid			; //用户标识	String(128)	否	用户在商户appid下的唯一标识	oUpF8uMuAJO_M2pxb1Q9zNjWeS6o 
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
	public String getDevice_info() { 
		return device_info; 
	} 
	public void setDevice_info(String device_info) { 
		this.device_info = device_info; 
	} 
	public String getTrade_type() { 
		return trade_type; 
	} 
	public void setTrade_type(String trade_type) { 
		this.trade_type = trade_type; 
	} 
	public String getTrade_state() { 
		return trade_state; 
	} 
	public void setTrade_state(String trade_state) { 
		this.trade_state = trade_state; 
	} 
	public String getBank_type() { 
		return bank_type; 
	} 
	public void setBank_type(String bank_type) { 
		this.bank_type = bank_type; 
	} 
	public String getFee_type() { 
		return fee_type; 
	} 
	public void setFee_type(String fee_type) { 
		this.fee_type = fee_type; 
	} 
	public String getTotal_fee() { 
		return total_fee; 
	} 
	public void setTotal_fee(String total_fee) { 
		this.total_fee = total_fee; 
	} 
	public String getCash_fee() { 
		return cash_fee; 
	} 
	public void setCash_fee(String cash_fee) { 
		this.cash_fee = cash_fee; 
	} 
	public String getCoupon_fee() { 
		return coupon_fee; 
	} 
	public void setCoupon_fee(String coupon_fee) { 
		this.coupon_fee = coupon_fee; 
	} 
	public String getTransaction_id() { 
		return transaction_id; 
	} 
	public void setTransaction_id(String transaction_id) { 
		this.transaction_id = transaction_id; 
	} 
	public String getOut_trade_no() { 
		return out_trade_no; 
	} 
	public void setOut_trade_no(String out_trade_no) { 
		this.out_trade_no = out_trade_no; 
	} 
	public String getAttach() { 
		return attach; 
	} 
	public void setAttach(String attach) { 
		this.attach = attach; 
	} 
	public String getTime_end() { 
		return time_end; 
	} 
	public void setTime_end(String time_end) { 
		this.time_end = time_end; 
	} 
	public String getOpenid() { 
		return openid; 
	} 
	public void setOpenid(String openid) { 
		this.openid = openid; 
	} 
	 
	 
	 
} 
