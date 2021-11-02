package org.anyline.wechat.entity;

/**
 * 分帐
 */
public class WechatProfitReceiver {

    public static enum TYPE{
        MERCHANT_ID			{public String getCode(){return "MERCHANT_ID";} 		public String getName(){return "商户ID";}},
        PERSONAL_WECHATID	{public String getCode(){return "PERSONAL_WECHATID";} 	public String getName(){return "个人微信号";}},
        PERSONAL_OPENID		{public String getCode(){return "PERSONAL_OPENID";} 	public String getName(){return "个人openid";}},
        PERSONAL_SUB_OPENID	{public String getCode(){return "MICROPAY";} 	        public String getName(){return "个人sub_openid";}};
        public abstract String getName();
        public abstract String getCode();
    };
    private TYPE type			; //分账接收方类型		是	string(32)	MERCHANT_ID	MERCHANT_ID：商户ID  PERSONAL_WECHATID：个人微信号PERSONAL_OPENID：个人openid（由父商户APPID转换得到）PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）
    private String account		; //分账接收方帐号		是	string(64)	86693852	类型是MERCHANT_ID时，是商户ID类型是PERSONAL_WECHATID时，是个人微信号类型是PERSONAL_OPENID时，是个人openid类型是PERSONAL_SUB_OPENID时，是个人sub_openid
    private int amount		    ; //分账金额			是	int	888	分账金额，单位为分，只能为整数，不能超过原订单支付金额及最大分账比例金额
    private String description	; //分账描述			是	string(80)	分给商户A	分账的原因描述，分账账单中需要体现
    public WechatProfitReceiver(TYPE type, String account, int amount, String description){
        this.type = type;
        this.account = account;
        this.amount = amount;
        this.description = description;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
