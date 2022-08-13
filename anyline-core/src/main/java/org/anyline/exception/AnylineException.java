package org.anyline.exception;

public class AnylineException extends RuntimeException{
    private Exception src;
    private String code;
    private String title;
    private String content;

    public AnylineException(){
        super();
    }
    public AnylineException(String code, String title, String content){
        super(title);
        this.code = code;
        this.title = title;
        this.content = content;
    }
    public AnylineException(String code, String title){
        super(title);
        this.code = code;
        this.title = title;
    }
    public AnylineException(String code){
        super(code);
        this.code = code;
    }
    public Exception getSrc() {
        return this.src;
    }

    public void setSrc(final Exception src) {
        this.src = src;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(final String content) {
        this.content = content;
    }
}
