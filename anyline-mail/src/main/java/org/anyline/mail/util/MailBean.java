package org.anyline.mail.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.mail.load.bean")
public class MailBean implements InitializingBean {
    @Value("${anyline.mail.account:}")
    public String ACCOUNT					 	;
    @Value("${anyline.mail.password:}")
    public String PASSWORD						;
    @Value("${anyline.mail.user:}")
    public String USERNAME						;
    @Value("${anyline.mail.protocol:}")
    public String PROTOCOL 						;
    @Value("${anyline.mail.host:}")
    public String HOST							;
    @Value("${anyline.mail.port:}")
    public String PORT						    ;
    @Value("${anyline.mail.attachment:}")
    public String ATTACHMENT_DIR 				;	//附件下载地址
    @Value("${anyline.mail.ssl:false}")
    public boolean SSL_FLAG 					;  //是否需要ssl验证  具体看服务商情况  smtp  25不需要  465需要
    @Value("${anyline.mail.download:true}")
    public boolean AUTO_DOWNLOAD_ATTACHMENT 	;



    @Override
    public void afterPropertiesSet() throws Exception {
        ACCOUNT = BasicUtil.evl(ACCOUNT, MailConfig.DEFAULT_ACCOUNT);
        if(BasicUtil.isEmpty(ACCOUNT)){
            return;
        }
        DataRow row = new DataRow();
        row.put("ACCOUNT",BasicUtil.evl(ACCOUNT,MailConfig.DEFAULT_ACCOUNT));
        row.put("PASSWORD",BasicUtil.evl(PASSWORD,MailConfig.DEFAULT_PASSWORD));
        row.put("USERNAME",BasicUtil.evl(USERNAME,MailConfig.DEFAULT_USERNAME));
        row.put("PROTOCOL",BasicUtil.evl(PROTOCOL,MailConfig.DEFAULT_PROTOCOL));
        row.put("HOST",BasicUtil.evl(HOST,MailConfig.DEFAULT_HOST));
        row.put("PORT",BasicUtil.evl(PORT,MailConfig.DEFAULT_PORT));
        row.put("ATTACHMENT_DIR",BasicUtil.evl(ATTACHMENT_DIR,MailConfig.DEFAULT_ATTACHMENT_DIR));
        row.put("SSL_FLAG",SSL_FLAG);
        row.put("AUTO_DOWNLOAD_ATTACHMENT",AUTO_DOWNLOAD_ATTACHMENT);
        MailConfig.register(row);
    }

    @Bean("anyline.mail.init.mail.util")
    public MailUtil mailInstance(){
        return MailUtil.getInstance();
    }
    @Bean("anyline.mail.init.pop3.util")
    public Pop3Util pop3Instance(){
        return Pop3Util.getInstance();
    }
}
