package org.anyline.alipay.entity;
/**
 * 单笔转账到支付宝账户
 *
 */
public class AlipayTransfer {

	 //收款方账户类型
	public static enum PAYEE_TYPE{
		ALIPAY_USERID	{public String getCode(){return "ALIPAY_USERID";} 	public String getName(){return "收款方账户类型  支付宝账号对应的支付宝唯一用户号";}},
		ALIPAY_LOGONID	{public String getCode(){return "ALIPAY_LOGONID";} 	public String getName(){return "支付宝登录号，支持邮箱和手机号格式";}};

		public abstract String getName();
		public abstract String getCode();
	}
	private String out_biz_no		; //商户转账唯一订单号。			发起转账来源方定义的转账单据ID，用于将转账回执通知给来源方。不同来源方给出的ID可以重复，同一个来源方必须保证其ID的唯一性。只支
	private String payee_type		; //收款方账户类型。				可取值： 1、ALIPAY_USERID：支付宝账号对应的支付宝唯一用户号。以2088开头的16位纯数字组成。  2、ALIPAY_LOGONID：支付宝登录号，支持邮箱和手机号格式。
	private String payee_account	; //收款方账户。				与payee_type配合使用。付款方和收款方不能是同一个账户。
	private double amount			; //转账金额，单位：元。 			只支持2位小数，小数点前最大支持13位，金额必须大于等于0.1元。
	private String payer_show_name	; //付款方显示姓名				如果不传，则默认显示该账户在支付宝登记的实名。收款方可见。
	private String payee_real_name	; //收款方真实姓名				 如果本参数不为空，则会校验该账户在支付宝登记的实名是否与收款方真实姓名一致。
	private String remark			; //转账备注					 当付款方为企业账户，且转账金额达到（大于等于）50000元，remark不能为空。收款方可见，会展示在收款用户的账单中。
	
	public String getOut_biz_no() {
		return out_biz_no;
	}
	public void setOut_biz_no(String out_biz_no) {
		this.out_biz_no = out_biz_no;
	}
	public String getPayee_type() {
		return payee_type;
	}
	public void setPayee_type(PAYEE_TYPE type) {
		this.payee_type = type.getCode();
	}
	public String getPayee_account() {
		return payee_account;
	}
	public void setPayee_account(String payee_account) {
		this.payee_account = payee_account;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getPayer_show_name() {
		return payer_show_name;
	}
	public void setPayer_show_name(String payer_show_name) {
		this.payer_show_name = payer_show_name;
	}
	public String getPayee_real_name() {
		return payee_real_name;
	}
	public void setPayee_real_name(String payee_real_name) {
		this.payee_real_name = payee_real_name;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	
	
}
