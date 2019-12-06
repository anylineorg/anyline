package org.anyline.weixin.wap.entity; 
 
import org.anyline.weixin.entity.PerPayResult; 
 
public class WXWapPrePayResult extends PerPayResult{ 
	private String mweb_url = null;	//mweb_url为拉起微信支付收银台的中间页面，可通过访问该url来拉起微信客户端，完成支付,mweb_url的有效期为5分钟。 
 
	public String getMweb_url() { 
		return mweb_url; 
	} 
 
	public void setMweb_url(String mweb_url) { 
		this.mweb_url = mweb_url; 
	} 
	 
} 
