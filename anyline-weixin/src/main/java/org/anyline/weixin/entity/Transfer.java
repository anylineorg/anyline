package org.anyline.weixin.entity; 
 
public class Transfer { 
	protected String mch_appid				;// 商户appid(conf)		是	wx8888888888888888	String(128)	申请商户号的appid或商户号绑定的appid 
	protected String mchid					;// 商户号(conf)			是	1900000109	String(32)	微信支付分配的商户号 
	protected String nonce_str				;// 随机字符串(auto)		是	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	String(32)	随机字符串，不长于32位 
	protected String sign					;// 签名(auto)			是	C380BEC2BFD727A4B6845133519F3AD6	String(32)	签名，详见签名算法 
	protected String partner_trade_no		;// 商户订单号(evl)		是	如果不指定则随机	String(32)	商户订单号，需保持唯一性(只能是字母或者数字，不能包含有其他字符) 
	protected String openid					;// 用户openid			是	oxTWIuGaIt6gTKsQRLau2M0yL16E	String(64)	商户appid下，某用户的openid 
	protected String check_name="NO_CHECK"	;// 校验用户姓名选项(def)	是	FORCE_CHECK	String(16)	NO_CHECK：不校验真实姓名  FORCE_CHECK：强校验真实姓名 
	protected String re_user_name			;// 收款用户姓名			可选	王小王	String(64)	收款用户真实姓名。 如果check_name设置为FORCE_CHECK，则必填用户真实姓名 
	protected int amount					;// 金额					是	10099	int	企业付款金额，单位为分 
	protected String desc					;// 企业付款备注			是	理赔	String(100)	企业付款备注，必填。 
	protected String spbill_create_ip		;// Ip地址				是	192.168.0.1	String(32)	该IP同在商户平台设置的IP白名单中的IP没有关联，该IP可传用户端或者服务端的IP。 
	protected String device_info			;// 设备号				否	013467007045764	String(32)	微信支付分配的终端设备号 
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
	public String getPartner_trade_no() { 
		return partner_trade_no; 
	} 
	public void setPartner_trade_no(String partner_trade_no) { 
		this.partner_trade_no = partner_trade_no; 
	} 
	public String getOpenid() { 
		return openid; 
	} 
	public void setOpenid(String openid) { 
		this.openid = openid; 
	} 
	public String getCheck_name() { 
		return check_name; 
	} 
	public void setCheck_name(String check_name) { 
		this.check_name = check_name; 
	} 
	public String getRe_user_name() { 
		return re_user_name; 
	} 
	public void setRe_user_name(String re_user_name) { 
		this.re_user_name = re_user_name; 
	} 
	 
	public int getAmount() { 
		return amount; 
	} 
	public void setAmount(int amount) { 
		this.amount = amount; 
	} 
	public String getDesc() { 
		return desc; 
	} 
	public void setDesc(String desc) { 
		this.desc = desc; 
	} 
	public String getSpbill_create_ip() { 
		return spbill_create_ip; 
	} 
	public void setSpbill_create_ip(String spbill_create_ip) { 
		this.spbill_create_ip = spbill_create_ip; 
	} 
	public String getDevice_info() { 
		return device_info; 
	} 
	public void setDevice_info(String device_info) { 
		this.device_info = device_info; 
	} 
	 
} 
