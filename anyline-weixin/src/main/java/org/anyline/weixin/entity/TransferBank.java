package org.anyline.weixin.entity;

public class TransferBank {
	protected String mch_id				;//商户号	(auto)		是	1900000109	string(32)	微信支付分配的商户号
	protected String partner_trade_no	;//商户企业付款单号(evl)是	1212121221227	string(32)	商户订单号，需保持唯一（只允许数字[0~9]或字母[A~Z]和[a~z]，最短8位，最长32位）
	protected String nonce_str			;//随机字符串(auto)	是	5K8264ILTKCH16CQ2502SI8ZNMTM67Vs	string(32)	随机字符串，不长于32位
	protected String sign				;//签名(auto)		是	C380BEC2BFD727A4B6845133519F3AD6	string(32)	通过MD5签名算法计算得出的签名值，详见MD5签名生成算法
	protected String enc_bank_no		;//收款方银行卡号		是	8609cb22e1774a50a930e414cc71eca06121bcd266335cda230d24a7886a8d9f	string(64)	收款方银行卡号（采用标准RSA算法，公钥由微信侧提供）,详见获取RSA加密公钥API
	protected String enc_true_name		;//收款方用户名		是	ca775af5f841bdf424b2e6eb86a6e21e	string(64)	收款方用户名（采用标准RSA算法，公钥由微信侧提供）详见获取RSA加密公钥API
	protected String bank_code			;//收款方开户行		是	1001	string(64)	银行卡所在开户行编号,详见银行编号列表
	protected int amount				;//付款金额			是	100000	int	付款金额：RMB分（支付总额，不含手续费） 注：大于0的整数
	protected String desc				;//付款说明			否	理财	string(512)	企业付款到银行卡付款说明,即订单备注（UTF8编码，允许100个字符以内）
	public String getMch_id() {
		return mch_id;
	}
	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}
	public String getPartner_trade_no() {
		return partner_trade_no;
	}
	public void setPartner_trade_no(String partner_trade_no) {
		this.partner_trade_no = partner_trade_no;
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
	public String getEnc_bank_no() {
		return enc_bank_no;
	}
	public void setEnc_bank_no(String enc_bank_no) {
		this.enc_bank_no = enc_bank_no;
	}
	public String getEnc_true_name() {
		return enc_true_name;
	}
	public void setEnc_true_name(String enc_true_name) {
		this.enc_true_name = enc_true_name;
	}
	public String getBank_code() {
		return bank_code;
	}
	public void setBank_code(String bank_code) {
		this.bank_code = bank_code;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
}
