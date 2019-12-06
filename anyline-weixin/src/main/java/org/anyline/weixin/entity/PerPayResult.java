package org.anyline.weixin.entity; 
 
public class PerPayResult { 
	protected String appid; 
	protected String mch_id; 
	protected String return_code; 
	protected String return_msg; 
	protected String device_info; 
	protected String nonce_str; 
	protected String sign; 
	protected String result_code; 
	protected String err_code; 
	protected String err_code_des; 
	protected String trade_type; 
	protected String prepay_id; 
 
	public PerPayResult(){ 
		 
	} 
	public PerPayResult(boolean result, String msg){ 
		if(result){ 
			this.return_code = "SUCCESS"; 
		}else{ 
			this.return_code = "FAIL"; 
		} 
		this.return_msg = msg; 
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
	 
} 
