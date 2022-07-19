package org.anyline.aliyun.sms.util;

public class SMSTemplate {

    public static enum STATUS{
        ERROR           			{public String getCode(){return "ERROR";} public String getName(){return "异常状态";}},
        AUDIT_STATE_INIT			{public String getCode(){return "AUDIT_STATE_INIT";} public String getName(){return "待审核";}},
        AUDIT_STATE_PASS			{public String getCode(){return "AUDIT_STATE_PASS";} public String getName(){return "已审核通过";}},
        AUDIT_STATE_NOT_PASS		{public String getCode(){return "AUDIT_STATE_NOT_PASS";} public String getName(){return "审核未通过";}},
        AUDIT_SATE_CANCEL		    {public String getCode(){return "AUDIT_SATE_CANCEL";} public String getName(){return "撤销申请";}};
        public abstract String getCode();
        public abstract String getName();
    }
    public static enum TYPE{
        VERIFY_CODE     			{public int getCode(){return 0;} public String getName(){return "验证码";}},
        NOTICE			            {public int getCode(){return 1;} public String getName(){return "通知短信";}},
        POPULARIZE		            {public int getCode(){return 2;} public String getName(){return "推广短信";}};
        public abstract int getCode();
        public abstract String getName();
    }
    private STATUS status;
    private String code;
    private String name;
    private TYPE type; //0:验证码 1:通知短信 2:推广短信
    private String content;
    private String createTime;
    private String rejectTime;
    private String rejectInfo;
    private String rejectSubInfo;

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setStatus(String status) {
        if(STATUS.AUDIT_STATE_INIT.getCode().equals(status)){
            this.status = STATUS.AUDIT_STATE_INIT;
        }else if(STATUS.AUDIT_STATE_PASS.getCode().equals(status)){
            this.status = STATUS.AUDIT_STATE_PASS;
        }else if(STATUS.AUDIT_STATE_NOT_PASS.getCode().equals(status)){
            this.status = STATUS.AUDIT_STATE_NOT_PASS;
        }else if(STATUS.AUDIT_SATE_CANCEL.getCode().equals(status)){
            this.status = STATUS.AUDIT_SATE_CANCEL;
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type(type);
    }
    public static TYPE type(int type){
        if(type == 0){
            return TYPE.VERIFY_CODE;
        }
        if(type == 1){
            return TYPE.NOTICE;
        }
        if(type == 2){
            return TYPE.POPULARIZE;
        }
        return null;
    }
    public void setType(TYPE type) {
       this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getRejectTime() {
        return rejectTime;
    }

    public void setRejectTime(String rejectTime) {
        this.rejectTime = rejectTime;
    }

    public String getRejectInfo() {
        return rejectInfo;
    }

    public void setRejectInfo(String rejectInfo) {
        this.rejectInfo = rejectInfo;
    }

    public String getRejectSubInfo() {
        return rejectSubInfo;
    }

    public void setRejectSubInfo(String rejectSubInfo) {
        this.rejectSubInfo = rejectSubInfo;
    }
}
