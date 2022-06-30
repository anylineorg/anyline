package org.anyline.aliyun.sms.util;

public class SMSTemplate {

    public static enum STATUS{
        AUDIT_STATE_INIT			{public String getCode(){return "AUDIT_STATE_INIT";} public String getName(){return "待审核";}},
        AUDIT_STATE_PASS			{public String getCode(){return "AUDIT_STATE_PASS";} public String getName(){return "已审核通过";}},
        AUDIT_STATE_NOT_PASS		{public String getCode(){return "AUDIT_STATE_NOT_PASS";} public String getName(){return "审核未通过";}},
        AUDIT_SATE_CANCEL		    {public String getCode(){return "AUDIT_SATE_CANCEL";} public String getName(){return "撤销申请";}};
        public abstract String getCode();
        public abstract String getName();
    }
    private String status;
    private String code;
    private String name;
    private int type;
    private String content;
    private String createTime;
    private String rejectTime;
    private String rejectInfo;
    private String rejectSubInfo;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
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
