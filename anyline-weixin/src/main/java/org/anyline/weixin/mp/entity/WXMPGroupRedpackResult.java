package org.anyline.weixin.mp.entity;



public class WXMPGroupRedpackResult extends WXMPRedpackResult{
	public WXMPGroupRedpackResult(){
		super();
	}
	public WXMPGroupRedpackResult(boolean result){
		super(result, null);
	}
	public WXMPGroupRedpackResult(boolean result, String msg){
		super(result, msg);
	}
}
