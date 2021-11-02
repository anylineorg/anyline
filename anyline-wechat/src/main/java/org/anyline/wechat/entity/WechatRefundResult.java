package org.anyline.wechat.entity; //
 
public class WechatRefundResult {
	private String return_code				; // 返回状态码			//是	String(16)	SUCCESS	SUCCESS/FAIL 
	private String return_msg				; // 返回信息				//否	String(128)	签名失败	  
	private String result_code				; // 业务结果				//是	String(16)	SUCCESS	 SUCCESS/FAIL SUCCESS退款申请接收成功，结果通过退款查询接口查询FAIL 提交业务失败 
	private String err_code					; // 错误代码				//否	String(32)	SYSTEMERROR	列表详见错误码列表 
	private String err_code_des				; // 错误代码描述			//否	String(128)	系统超时	结果信息描述 
	private String appid					; // 公众账号ID			//是	String(32)	wx8888888888888888	微信分配的公众账号ID 
	private String mch_id					; // 商户号				//是	String(32)	1900000109	微信支付分配的商户号 
	private String nonce_str				; // 随机字符串			//是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，不长于32位 
	private String sign						; // 签名				//是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	签名，详见签名算法 
	private String transaction_id			; // 微信订单号			//是	String(32)	4007752501201407033233368018	微信订单号 
	private String out_trade_no				; // 商户订单号			//是	String(32)	33368018	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 
	private String out_refund_no			; // 商户退款单号			//是	String(64)	121775250	商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔。 
	private String refund_id				; // 微信退款单号			//是	String(32)	2007752501201407033233368018	微信退款单号 
	private String refund_fee				; // 退款金额				//是	Int	100	退款总金额,单位为分,可以做部分退款 
	private String settlement_refund_fee	; // 应结退款金额			//否	Int	100	去掉非充值代金券退款金额后的退款金额，退款金额=申请退款金额-非充值代金券退款金额，退款金额<=申请退款金额 
	private String total_fee				; // 标价金额				//是	Int	100	订单总金额，单位为分，只能为整数，详见支付金额 
	private String settlement_total_fee		; // 应结订单金额			//否	Int	100	去掉非充值代金券金额后的订单总金额，应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额。 
	private String fee_type					; // 标价币种				//否	String(8)	CNY	订单金额货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型 
	private String cash_fee					; // 现金支付金额			//是	Int	100	现金支付金额，单位为分，只能为整数，详见支付金额 
	private String cash_fee_type			; // 现金支付币种			//否	String(16)	CNY	货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型 
	private String cash_refund_fee			; // 现金退款金额			//否	Int	100	现金退款金额，单位为分，只能为整数，详见支付金额 
	private String coupon_type_$n			; // 代金券类型			//否	String(8)	CASH	 CASH--充值代金券  NO_CASH---非充值代金券 订单使用代金券时有返回（取值：CASH、NO_CASH）。$n为下标,从0开始编号，举例：coupon_type_0 
	private String coupon_refund_fee		; // 代金券退款总金额		//否	Int	100	代金券退款金额<=退款金额，退款金额-代金券或立减优惠退款金额为现金，说明详见代金券或立减优惠 
	private String coupon_refund_fee_$n		; // 单个代金券退款金额		//否	Int	100	代金券退款金额<=退款金额，退款金额-代金券或立减优惠退款金额为现金，说明详见代金券或立减优惠 
	private String coupon_refund_count		; // 退款代金券使用数量		//否	Int	1	退款代金券使用数量 
	private String coupon_refund_id_$n		; // 退款代金券ID			//否	String(20)	10000 	退款代金券ID, $n为下标，从0开始编号 
	public WechatRefundResult(){
		 
	} 
	public WechatRefundResult(boolean result, String msg){
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
	public String getOut_refund_no() { 
		return out_refund_no; 
	} 
	public void setOut_refund_no(String out_refund_no) { 
		this.out_refund_no = out_refund_no; 
	} 
	public String getRefund_id() { 
		return refund_id; 
	} 
	public void setRefund_id(String refund_id) { 
		this.refund_id = refund_id; 
	} 
	public String getRefund_fee() { 
		return refund_fee; 
	} 
	public void setRefund_fee(String refund_fee) { 
		this.refund_fee = refund_fee; 
	} 
	public String getSettlement_refund_fee() { 
		return settlement_refund_fee; 
	} 
	public void setSettlement_refund_fee(String settlement_refund_fee) { 
		this.settlement_refund_fee = settlement_refund_fee; 
	} 
	public String getTotal_fee() { 
		return total_fee; 
	} 
	public void setTotal_fee(String total_fee) { 
		this.total_fee = total_fee; 
	} 
	public String getSettlement_total_fee() { 
		return settlement_total_fee; 
	} 
	public void setSettlement_total_fee(String settlement_total_fee) { 
		this.settlement_total_fee = settlement_total_fee; 
	} 
	public String getFee_type() { 
		return fee_type; 
	} 
	public void setFee_type(String fee_type) { 
		this.fee_type = fee_type; 
	} 
	public String getCash_fee() { 
		return cash_fee; 
	} 
	public void setCash_fee(String cash_fee) { 
		this.cash_fee = cash_fee; 
	} 
	public String getCash_fee_type() { 
		return cash_fee_type; 
	} 
	public void setCash_fee_type(String cash_fee_type) { 
		this.cash_fee_type = cash_fee_type; 
	} 
	public String getCash_refund_fee() { 
		return cash_refund_fee; 
	} 
	public void setCash_refund_fee(String cash_refund_fee) { 
		this.cash_refund_fee = cash_refund_fee; 
	} 
	public String getCoupon_type_$n() { 
		return coupon_type_$n; 
	} 
	public void setCoupon_type_$n(String coupon_type_$n) { 
		this.coupon_type_$n = coupon_type_$n; 
	} 
	public String getCoupon_refund_fee() { 
		return coupon_refund_fee; 
	} 
	public void setCoupon_refund_fee(String coupon_refund_fee) { 
		this.coupon_refund_fee = coupon_refund_fee; 
	} 
	public String getCoupon_refund_fee_$n() { 
		return coupon_refund_fee_$n; 
	} 
	public void setCoupon_refund_fee_$n(String coupon_refund_fee_$n) { 
		this.coupon_refund_fee_$n = coupon_refund_fee_$n; 
	} 
	public String getCoupon_refund_count() { 
		return coupon_refund_count; 
	} 
	public void setCoupon_refund_count(String coupon_refund_count) { 
		this.coupon_refund_count = coupon_refund_count; 
	} 
	public String getCoupon_refund_id_$n() { 
		return coupon_refund_id_$n; 
	} 
	public void setCoupon_refund_id_$n(String coupon_refund_id_$n) { 
		this.coupon_refund_id_$n = coupon_refund_id_$n; 
	} 
	 
} 
