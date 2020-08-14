package org.anyline.wechat.entity;
 
public class WechatPrePayResult {
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
	protected String request_id;
	protected String request_status;
	private String code_url = null;//trade_type为NATIVE时有返回，用于生成二维码，展示给用户进行扫码支付
	private String mweb_url = null;	//mweb_url为拉起微信支付收银台的中间页面，可通过访问该url来拉起微信客户端，完成支付,mweb_url的有效期为5分钟。
	private boolean result;

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getMweb_url() {
		return mweb_url;
	}

	public void setMweb_url(String mweb_url) {
		this.mweb_url = mweb_url;
	}

	public String getCode_url() {
		return code_url;
	}

	public void setCode_url(String code_url) {
		this.code_url = code_url;
	}
	public WechatPrePayResult(){
		 
	} 
	public WechatPrePayResult(boolean result, String msg){
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

	public String getRequest_status() {
		return request_status;
	}

	public void setRequest_status(String request_status) {
		this.request_status = request_status;
	}

	public String getRequest_id() {
		return request_id;
	}

	public void setRequest_id(String request_id) {
		this.request_id = request_id;
	}
}
