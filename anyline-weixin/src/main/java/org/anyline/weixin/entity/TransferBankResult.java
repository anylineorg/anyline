package org.anyline.weixin.entity; 
 
public class TransferBankResult { 
	protected String return_code		;//返回状态码		是	String(16)	SUCCESS/FAIL此字段是通信标识，非付款标识，付款是否成功需要查看result_code来判断 
	protected String return_msg			;//返回信息		否	String(128)	返回信息，如非空，为错误原因 签名失败 参数格式校验错误 
	//以下字段在return_code为SUCCESS的时候有返回 
	protected String result_code		;//业务结果		是	string(32)	SUCCESS/FAIL，注意：当状态为FAIL时，存在业务结果未明确的情况，所以如果状态为FAIL，请务必通过查询接口确认此次付款的结果（关注错误码err_code字段）。如果要继续进行这笔付款，请务必用原商户订单号和原参数来重入此接口。 
	protected String err_code			;//错误代码		否 string(32)错误码信息，注意：出现未明确的错误码时，如（SYSTEMERROR）等，请务必用原商户订单号重试，或通过查询接口确认此次付款的结果 
	protected String err_code_des		;//错误代码描述	否	string(128)	错误信息描述 
	protected String mch_id				;//商户号			是	string(32)	微信支付分配的商户号 
	protected String partner_trade_no	;//商户企业付款单号	是	string(32)	商户订单号，需要保持唯一 
	protected String amount				;//代付金额		是	int	代付金额RMB:分 
	protected String nonce_str			;//随机字符串		是	string(32)	随机字符串，长度小于32位 
	protected String sign				;//签名			是	string(32)	返回包携带签名给商户 
	//以下字段在return_code 和result_code都为SUCCESS的时候有返回 
	protected String payment_no			;//微信企业付款单号	是	string(64)	代付成功后，返回的内部业务单号 
	protected String cmms_amt			;//手续费金额		是	int	手续费金额 RMB：分 
	public TransferBankResult(){ 
		 
	} 
	public TransferBankResult(boolean result, String msg){ 
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
	public String getAmount() { 
		return amount; 
	} 
	public void setAmount(String amount) { 
		this.amount = amount; 
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
	public String getPayment_no() { 
		return payment_no; 
	} 
	public void setPayment_no(String payment_no) { 
		this.payment_no = payment_no; 
	} 
	public String getCmms_amt() { 
		return cmms_amt; 
	} 
	public void setCmms_amt(String cmms_amt) { 
		this.cmms_amt = cmms_amt; 
	} 
	 
} 
