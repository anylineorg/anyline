package org.anyline.weixin.mp.entity;

import org.anyline.weixin.entity.TradeOrder;


public class WXMPPayTradeOrder extends TradeOrder{
	private String product_id;
	private String openid;
	public String getProduct_id() {
		return product_id;
	}
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	
}
