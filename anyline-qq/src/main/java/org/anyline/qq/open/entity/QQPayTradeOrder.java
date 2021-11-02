package org.anyline.qq.open.entity; 
 
public class QQPayTradeOrder { 
 
	private String mch_id			; //商户号		 
	private String nonce_str		; //随机字符串 
	private String sign				; //签名 
	private String out_trade_no		; //商户订单号 
	private String fee_type = "CNY"	; //货币类型定义 
	private String total_fee		; //订单金额 
	private String spbill_create_ip	; //用户终端IP 
	private String limit_pay		; //支付方式限制 
	private String trade_type		; //支付场景 
	private String notify_url		; //回调 
	private String device_info		; //调用接口提交的终端设备号 
	 
	private String appid			; //应用ID 
	private String body				; //商品描述 
	private String attach			; //附加数据 
	private String time_start		; //订单生成时间yyyyMMddHHmmss 
	private String time_expire		; //订单超时时间yyyyMMddHHmmss 
	private String contract_code	; // 代扣签约序列号 商户侧记录的用户代扣协议序列号，支付中开通代扣必传 
	private String promotion_tag	; //QQ钱包活动标识 
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
	public String getOut_trade_no() { 
		return out_trade_no; 
	} 
	public void setOut_trade_no(String out_trade_no) { 
		this.out_trade_no = out_trade_no; 
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
	public String getSpbill_create_ip() { 
		return spbill_create_ip; 
	} 
	public void setSpbill_create_ip(String spbill_create_ip) { 
		this.spbill_create_ip = spbill_create_ip; 
	} 
	public String getLimit_pay() { 
		return limit_pay; 
	} 
	public void setLimit_pay(String limit_pay) { 
		this.limit_pay = limit_pay; 
	} 
	public String getTrade_type() { 
		return trade_type; 
	} 
	public void setTrade_type(String trade_type) { 
		this.trade_type = trade_type; 
	} 
	public String getNotify_url() { 
		return notify_url; 
	} 
	public void setNotify_url(String notify_url) { 
		this.notify_url = notify_url; 
	} 
	public String getDevice_info() { 
		return device_info; 
	} 
	public void setDevice_info(String device_info) { 
		this.device_info = device_info; 
	} 
	public String getAppid() { 
		return appid; 
	} 
	public void setAppid(String appid) { 
		this.appid = appid; 
	} 
	public String getBody() { 
		return body; 
	} 
	public void setBody(String body) { 
		this.body = body; 
	} 
	public String getAttach() { 
		return attach; 
	} 
	public void setAttach(String attach) { 
		this.attach = attach; 
	} 
	public String getTime_start() { 
		return time_start; 
	} 
	public void setTime_start(String time_start) { 
		this.time_start = time_start; 
	} 
	public String getTime_expire() { 
		return time_expire; 
	} 
	public void setTime_expire(String time_expire) { 
		this.time_expire = time_expire; 
	} 
	public String getContract_code() { 
		return contract_code; 
	} 
	public void setContract_code(String contract_code) { 
		this.contract_code = contract_code; 
	} 
	public String getPromotion_tag() { 
		return promotion_tag; 
	} 
	public void setPromotion_tag(String promotion_tag) { 
		this.promotion_tag = promotion_tag; 
	} 
	 
	 
} 
