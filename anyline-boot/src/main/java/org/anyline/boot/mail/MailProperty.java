package org.anyline.boot.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("anyline.boot.mail")
@ConfigurationProperties(prefix = "anyline.mail")
public class MailProperty {

    public String account					 	;
    public String password						;
    public String username						;
    public String protocol 						;
    public String host							;
    public String port						    ;
    public String attachmentDir 				;	// 附件下载地址
    public boolean ssl 					        ;  // 是否需要ssl验证  具体看服务商情况  smtp  25不需要  465需要
    public boolean autoDownloadAttachment   	;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAttachmentDir() {
        return attachmentDir;
    }

    public void setAttachmentDir(String attachmentDir) {
        this.attachmentDir = attachmentDir;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isAutoDownloadAttachment() {
        return autoDownloadAttachment;
    }

    public void setAutoDownloadAttachment(boolean autoDownloadAttachment) {
        this.autoDownloadAttachment = autoDownloadAttachment;
    }
}
