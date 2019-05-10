package org.anyline.weixin.mp.entity;

import org.anyline.weixin.entity.TransferBankResult;

public class WXMPTransferBankResult extends TransferBankResult{
	public WXMPTransferBankResult(){
		super();
	}
	public WXMPTransferBankResult(boolean result){
		super(result, null);
	}
	public WXMPTransferBankResult(boolean result, String msg){
		super(result, msg);
	}
}
