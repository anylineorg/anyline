package org.anyline.weixin.mp.entity;

import org.anyline.weixin.entity.TradeResult;

public class WXMPPayTradeResult extends TradeResult{
	private String code_url;

	public String getCode_url() {
		return code_url;
	}

	public void setCode_url(String code_url) {
		this.code_url = code_url;
	}
	
}
