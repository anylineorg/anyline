package org.anyline.alipay.entity;

import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;

public class TransferResult extends BasicResult{
	private String out_biz_no	; //商户转账唯一订单号：发起转账来源方定义的转账单据号。请求时对应的参数，原样返回。	3142321423432
	private String order_id		; //支付宝转账单据号，成功一定返回，失败可能不返回也可能返回。	20160627110070001502260006780837
	private String pay_date		; //支付时间：格式为yyyy-MM-dd HH:mm:ss，仅转账成功返回。	2013-01-01 08:08:08
	
	public TransferResult(){
		
	}
	public TransferResult(AlipayFundTransToaccountTransferResponse res){
		setSuccess(res.isSuccess());
		setCode(res.getCode());
		setSub_code(res.getSubCode());
		setMsg(res.getMsg());
		setSub_msg(res.getSubMsg());
		
		setOut_biz_no(res.getOutBizNo());
		setOrder_id(res.getOrderId());
		setPay_date(res.getPayDate());
	}
	public String getOut_biz_no() {
		return out_biz_no;
	}
	public void setOut_biz_no(String out_biz_no) {
		this.out_biz_no = out_biz_no;
	}
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public String getPay_date() {
		return pay_date;
	}
	public void setPay_date(String pay_date) {
		this.pay_date = pay_date;
	}
	
	
}
