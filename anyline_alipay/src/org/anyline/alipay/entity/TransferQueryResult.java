package org.anyline.alipay.entity;

import com.alipay.api.response.AlipayFundTransOrderQueryResponse;

public class TransferQueryResult extends BasicResult{
	private String status			; //转账单据状态。 SUCCESS：成功（配合"单笔转账到银行账户接口"产品使用时, 同一笔单据多次查询有可能从成功变成退票状态）； FAIL：失败（具体失败原因请参见error_code以及fail_reason返回值）； INIT：等待处理； DEALING：处理中； REFUND：退票（仅配合"单笔转账到银行账户接口"产品使用时会涉及, 具体退票原因请参见fail_reason返回值）； UNKNOWN：状态未知。	SUCCESS
	private String order_id			; //支付宝转账单据号，查询失败不返回。	2912381923
	private String out_biz_no		; //发起转账来源方定义的转账单据号。 该参数的赋值均以查询结果中 的 out_biz_no 为准。 如果查询失败，不返回该参数。	3142321423432
	private String pay_date			; //支付时间，格式为yyyy-MM-dd HH:mm:ss，转账失败不返回。	2013-01-01 08:08:08
	private String arrival_time_end	; //预计到账时间，转账到银行卡专用，格式为yyyy-MM-dd HH:mm:ss，转账受理失败不返回。 注意： 此参数为预计时间，可能与实际到账时间有较大误差，不能作为实际到账时间使用，仅供参考用途。	2013-01-01 08:08:08
	private String order_fee		; //预计收费金额（元），转账到银行卡专用，数字格式，精确到小数点后2位，转账失败或转账受理失败不返回。	0.02
	private String fail_reason		; //查询到的订单状态为FAIL失败或REFUND退票时，返回具体的原因。	单笔额度超限
	private String error_code		; //查询失败时，本参数为错误代 码。 查询成功不返回。 对于退票订单，不返回该参数。	ORDER_NOT_EXIST
	
	public TransferQueryResult(){
		
	}
	public TransferQueryResult(AlipayFundTransOrderQueryResponse res){
		setSuccess(res.isSuccess());
		setCode(res.getCode());
		setSub_code(res.getSubCode());
		setMsg(res.getMsg());
		setSub_msg(res.getSubMsg());
		
		setStatus(res.getStatus());
		setOrder_id(res.getOrderId());
		setOut_biz_no(res.getOutBizNo());
		setPay_date(res.getPayDate());
		setArrival_time_end(res.getArrivalTimeEnd());
		setOrder_fee(res.getOrderFee());
		setFail_reason(res.getFailReason());
		setError_code(res.getErrorCode());
	}
	
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPay_date() {
		return pay_date;
	}
	public void setPay_date(String pay_date) {
		this.pay_date = pay_date;
	}
	public String getArrival_time_end() {
		return arrival_time_end;
	}
	public void setArrival_time_end(String arrival_time_end) {
		this.arrival_time_end = arrival_time_end;
	}
	public String getOrder_fee() {
		return order_fee;
	}
	public void setOrder_fee(String order_fee) {
		this.order_fee = order_fee;
	}
	public String getFail_reason() {
		return fail_reason;
	}
	public void setFail_reason(String fail_reason) {
		this.fail_reason = fail_reason;
	}
	public String getOut_biz_no() {
		return out_biz_no;
	}
	public void setOut_biz_no(String out_biz_no) {
		this.out_biz_no = out_biz_no;
	}
	public String getError_code() {
		return error_code;
	}
	public void setError_code(String error_code) {
		this.error_code = error_code;
	}
	
	
}
