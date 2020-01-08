package org.anyline.weixin.entity; 
 
public class RedpackResult { 
	protected String return_code		;//返回状态码 		是	SUCCESS	String(16)	SUCCESS/FAIL此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断 
	protected String return_msg			;//返回信息		否	签名失败	String(128)	返回信息，如非空，为错误原因签名失败参数格式校验错误 
	 
	//以下字段在return_code为SUCCESS的时候有返回 
	protected String 	result_code		;//业务结果		是	SUCCESS	String(16)	SUCCESS/FAIL 
	protected String 	err_code		;//错误代码		否	SYSTEMERROR	String(32)	错误码信息 
	protected String 	err_code_des	;//错误代码描述	否	系统错误	String(128)	结果信息描述 
	 
	//以下字段在return_code和result_code都为SUCCESS的时候有返回 
	protected String 	mch_billno		;//商户订单号		是	10000098201411111234567890	String(28)	商户订单号（每个订单号必须唯一）组成：mch_id+yyyymmdd+10位一天内不能重复的数字 
	protected String 	mch_id			;//商户号			是	10000098	String(32)	微信支付分配的商户号 
	protected String 	wxappid			;//公众账号appid	是	wx8888888888888888	String(32)	商户appid，接口传入的所有appid应该为公众号的appid（在mp.weixin.qq.com申请的），不能为APP的appid（在open.weixin.qq.com申请的）。 
	protected String 	re_openid		;//用户openid	是	oxTWIuGaIt6gTKsQRLau2M0yL16E	String(32)	接受收红包的用户用户在wxappid下的openid 
	protected String 	total_amount	;//付款金额		是	1000	int	付款金额，单位分 
	protected String 	send_listid		;//微信单号		是	100000000020150520314766074200	String(32)	红包订单的微信单号 
	public RedpackResult(){ 
		 
	} 
	public RedpackResult(boolean result, String msg){ 
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
	public String getMch_billno() { 
		return mch_billno; 
	} 
	public void setMch_billno(String mch_billno) { 
		this.mch_billno = mch_billno; 
	} 
	public String getMch_id() { 
		return mch_id; 
	} 
	public void setMch_id(String mch_id) { 
		this.mch_id = mch_id; 
	} 
	public String getWxappid() { 
		return wxappid; 
	} 
	public void setWxappid(String wxappid) { 
		this.wxappid = wxappid; 
	} 
	public String getRe_openid() { 
		return re_openid; 
	} 
	public void setRe_openid(String re_openid) { 
		this.re_openid = re_openid; 
	} 
	public String getTotal_amount() { 
		return total_amount; 
	} 
	public void setTotal_amount(String total_amount) { 
		this.total_amount = total_amount; 
	} 
	public String getSend_listid() { 
		return send_listid; 
	} 
	public void setSend_listid(String send_listid) { 
		this.send_listid = send_listid; 
	} 
	 
} 
