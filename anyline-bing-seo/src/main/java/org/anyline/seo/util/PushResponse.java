package org.anyline.seo.util;

public class PushResponse {
    private boolean result  ; // 是否成功
    private int status      ; // http状态
    private String message  ; // 返回消息体
    private int remain      ; // 当天剩余额度
    private int success     ; // 提成成功数量

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRemain() {
        return remain;
    }

    public void setRemain(int remain) {
        this.remain = remain;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }
}
