package org.anyline.alipay.entity;
/**
 * 单笔转账到支付宝账户
 *
 */
public class TransferQuery {
	private String out_biz_no	; //订单支付时传入的商户订单号,和支付宝交易号不能同时为空。 trade_no,out_trade_no如果同时存在优先取trade_no	
	private String order_id		; //支付宝交易号，和商户订单号不能同时为空
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
	
}
