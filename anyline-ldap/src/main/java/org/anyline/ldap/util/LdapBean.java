package org.anyline.ldap.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.ldap.load.bean")
public class LdapBean implements InitializingBean {

    @Value("${anyline.ldap.address:}")
    private String ADDRESS					 	;
    @Value("${anyline.ldap.port:389}")
    private int PORT 							;
    @Value("${anyline.ldap.domain:}")
    private String DOMAIN						;
    @Value("${anyline.ldap.root:}")
    private String ROOT							;
    @Value("${anyline.ldap.auth:}")
    private String SECURITY_AUTHENTICATION		;
    @Value("${anyline.ldap.url:}")
    private String URL							; // ldap:{ADDRESS}:{PORT}
    @Value("${anyline.ldap.connectTimeout:0}")
    private int CONNECT_TIMEOUT 				;
    @Value("${anyline.ldap.readTimeout:0}")
    private int READ_TIMEOUT 					;


    @Override
    public void afterPropertiesSet() throws Exception {
        ADDRESS = BasicUtil.evl(ADDRESS, LdapConfig.DEFAULT_ADDRESS);
        if(BasicUtil.isEmpty(ADDRESS)){
            return;
        }
        DataRow row = new DataRow();
        row.put("ADDRESS", BasicUtil.evl(ADDRESS,LdapConfig.DEFAULT_ADDRESS));
        row.put("PORT", BasicUtil.evl(PORT,LdapConfig.DEFAULT_PORT));
        row.put("DOMAIN", BasicUtil.evl(DOMAIN,LdapConfig.DEFAULT_DOMAIN));
        row.put("ROOT", BasicUtil.evl(ROOT,LdapConfig.DEFAULT_ROOT));
        row.put("SECURITY_AUTHENTICATION", BasicUtil.evl(SECURITY_AUTHENTICATION,LdapConfig.DEFAULT_SECURITY_AUTHENTICATION));
        row.put("URL", BasicUtil.evl(URL,LdapConfig.DEFAULT_URL));
        row.put("CONNECT_TIMEOUT", BasicUtil.evl(CONNECT_TIMEOUT,LdapConfig.DEFAULT_CONNECT_TIMEOUT));
        row.put("READ_TIMEOUT", BasicUtil.evl(READ_TIMEOUT,LdapConfig.DEFAULT_READ_TIMEOUT));
        LdapConfig.register(row);
    }
    @Bean("anyline.ldap.init.util")
    public LdapUtil instance(){
        return LdapUtil.getInstance();
    }

}
