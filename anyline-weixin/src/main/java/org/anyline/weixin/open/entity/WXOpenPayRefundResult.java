package org.anyline.weixin.open.entity;

import org.anyline.weixin.entity.RefundResult;



public class WXOpenPayRefundResult  extends RefundResult{
	public WXOpenPayRefundResult(){
		super();
	}
	public WXOpenPayRefundResult(boolean result){
		super(result, null);
	}
	public WXOpenPayRefundResult(boolean result, String msg){
		super(result, msg);
	}
}