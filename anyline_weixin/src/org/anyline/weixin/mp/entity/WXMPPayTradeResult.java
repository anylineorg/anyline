package org.anyline.weixin.mp.entity;

import org.anyline.weixin.entity.TradeResult;

public class WXMPPayTradeResult extends TradeResult{
	private String code_url = null;//trade_type为NATIVE时有返回，用于生成二维码，展示给用户进行扫码支付

	public String getCode_url() {
		return code_url;
	}

	public void setCode_url(String code_url) {
		this.code_url = code_url;
	}
	
}
