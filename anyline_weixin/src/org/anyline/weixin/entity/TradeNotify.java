package org.anyline.weixin.entity;

public class TradeNotify {
	protected String return_code			;
	protected String return_msg				;
	
	protected String appid					; //公众账号ID			是			String(32)			wx8888888888888888			微信分配的公众账号ID（企业号corpid即为此appId）
	protected String mch_id					; //商家号				是			String(32)			1900000109			微信支付分配的商家号
	protected String device_info			; //设备号				否			String(32)			013467007045764			微信支付分配的终端设备号，
	protected String nonce_str				; //随机字符串				是			String(32)			5K8264ILTKCH16CQ2502SI8ZNMTM67VS			随机字符串，不长于32位
	protected String sign					; //签名					是			String(32)			C380BEC2BFD727A4B6845133519F3AD6			签名，详见签名算法
	protected String sign_type				; //签名类型				否			String(32)			HMAC-SHA256			签名类型，目前支持HMAC-SHA256和MD5，默认为MD5
	protected String result_code			; //业务结果				是			String(16)			SUCCESS			SUCCESS/FAIL
	protected String err_code				; //错误代码				否			String(32)			SYSTEMERROR			错误返回的信息描述
	protected String err_code_des			; //错误代码描述			否			String(128)			系统错误			错误返回的信息描述
	protected String openid					; //用户标识				是			String(128)			wxd930ea5d5a258f4f			用户在商家appid下的唯一标识
	protected String is_subscribe			; //是否关注公众账号		否			String(1)			Y			用户是否关注公众账号，Y-关注，N-未关注，仅在公众账号类型支付有效
	protected String trade_type				; //交易类型				是			String(16)			JSAPI			JSAPI、NATIVE、APP
	protected String bank_type				; //付款银行				是			String(16)			CMC			银行类型，采用字符串类型的银行标识，银行类型见银行列表
	protected String total_fee				; //订单金额				是			Int					100			订单总金额，单位为分
	protected String settlement_total_fee	; //应结订单金额			否			Int					100			应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额。
	protected String fee_type				; //货币种类				否			String(8)			CNY			货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
	
	protected String cash_fee				; //现金支付金额			是			Int					100			现金支付金额订单现金支付金额，详见支付金额
	protected String cash_fee_type			; //现金支付货币类型		否			String(16)			CNY			货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
	protected String coupon_fee				; //总代金券金额			否			Int					10			代金券金额<=订单金额，订单金额-代金券金额=现金支付金额，详见支付金额
	protected String coupon_count			; //代金券使用数量			否			Int					1			代金券使用数量
	protected String coupon_type_$n			; //代金券类型				否			Int					CASH			CASH--充值代金券 NO_CASH---非充值代金券订单使用代金券时有返回（取值：CASH、NO_CASH）。$n为下标,从0开始编号，举例：coupon_type_0
	protected String coupon_id_$n			; //代金券ID				否			String(20)			10000			代金券ID,$n为下标，从0开始编号
	protected String coupon_fee_$n			; //单个代金券支付金额		否			Int					100			单个代金券支付金额,$n为下标，从0开始编号
	protected String transaction_id			; //微信支付订单号			是			String(32)			1217752501201407033233368018			微信支付订单号
	protected String out_trade_no			; //商家订单号				是			String(32)			1212321211201407033568112322			商家系统的订单号，与请求一致。
	protected String attach					; //商家数据包				否			String(128)			123456			商家数据包，原样返回
	protected String time_end				; //支付完成时间			是			String(14)			20141030133525			支付完成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则

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
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getIs_subscribe() {
		return is_subscribe;
	}
	public void setIs_subscribe(String is_subscribe) {
		this.is_subscribe = is_subscribe;
	}
	public String getTrade_type() {
		return trade_type;
	}
	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}
	public String getBank_type() {
		return bank_type;
	}
	public void setBank_type(String bank_type) {
		this.bank_type = bank_type;
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
	public String getCoupon_fee() {
		return coupon_fee;
	}
	public void setCoupon_fee(String coupon_fee) {
		this.coupon_fee = coupon_fee;
	}
	public String getCoupon_count() {
		return coupon_count;
	}
	public void setCoupon_count(String coupon_count) {
		this.coupon_count = coupon_count;
	}
	public String getCoupon_type_$n() {
		return coupon_type_$n;
	}
	public void setCoupon_type_$n(String coupon_type_$n) {
		this.coupon_type_$n = coupon_type_$n;
	}
	public String getCoupon_id_$n() {
		return coupon_id_$n;
	}
	public void setCoupon_id_$n(String coupon_id_$n) {
		this.coupon_id_$n = coupon_id_$n;
	}
	public String getCoupon_fee_$n() {
		return coupon_fee_$n;
	}
	public void setCoupon_fee_$n(String coupon_fee_$n) {
		this.coupon_fee_$n = coupon_fee_$n;
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
	
}
