package org.anyline.weixin.mp.entity; 
 
import org.anyline.weixin.entity.RedpackResult; 
 
public class WXMPRedpackResult extends RedpackResult{ 
	public WXMPRedpackResult(){ 
		super(); 
	} 
	public WXMPRedpackResult(boolean result){ 
		super(result, null); 
	} 
	public WXMPRedpackResult(boolean result, String msg){ 
		super(result, msg); 
	} 
} 
