package org.anyline.wechat.entity;
 
public class WechatFissionRedpackResult extends WechatRedpackResult {
    public WechatFissionRedpackResult(){}
    public WechatFissionRedpackResult(boolean result, String msg){
        if(result){
            this.return_code = "SUCCESS";
        }else{
            this.return_code = "FAIL";
        }
        this.return_msg = msg;
    }
} 
