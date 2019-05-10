package org.anyline.weixin.entity;

public class GroupRedpack extends Redpack{
	protected String amt_type = "ALL_RAND"	;//红包金额设置方式	是	ALL_RAND	String(32)	红包金额设置方式 ALL_RAND—全部随机,商户指定总金额和红包发放总人数，由微信支付随机计算出各红包金额

	public String getAmt_type() {
		return amt_type;
	}

	public void setAmt_type(String amt_type) {
		this.amt_type = amt_type;
	}
	
}
