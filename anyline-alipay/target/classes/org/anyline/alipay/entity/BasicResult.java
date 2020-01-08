package org.anyline.alipay.entity; 
 
public class BasicResult { 
	protected boolean success	; 
	protected String code		; //网关返回码,详见文档	40004 
	protected String msg		; //网关返回码描述,详见文档	Business Failed 
	protected String sub_code	; //业务返回码,详见文档	ACQ.TRADE_HAS_SUCCESS 
	protected String sub_msg	; //业务返回码描述,详见文档	交易已被支付 
	protected String sign		; // 
	public String getCode() { 
		return code; 
	} 
	public void setCode(String code) { 
		this.code = code; 
	} 
	public String getMsg() { 
		return msg; 
	} 
	public void setMsg(String msg) { 
		this.msg = msg; 
	} 
	public String getSub_code() { 
		return sub_code; 
	} 
	public void setSub_code(String sub_code) { 
		this.sub_code = sub_code; 
	} 
	public String getSub_msg() { 
		return sub_msg; 
	} 
	public void setSub_msg(String sub_msg) { 
		this.sub_msg = sub_msg; 
	} 
	public String getSign() { 
		return sign; 
	} 
	public void setSign(String sign) { 
		this.sign = sign; 
	} 
	public boolean isSuccess() { 
		return success; 
	} 
	public void setSuccess(boolean success) { 
		this.success = success; 
	} 
	 
	 
} 
