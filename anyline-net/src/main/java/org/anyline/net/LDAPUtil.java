package org.anyline.net;


import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDAPUtil {
	private static Logger log = LoggerFactory.getLogger(LDAPUtil.class);
	/**
	 * 登录
	 * @param url 登录地址 如 http://ladp.anyline.org:389
	 * @param account 登录帐号 如 admin@anyline.org
	 * @param password 登录密码
	 * @return boolean
	 * @throws NamingException 如果抛出异常表示登录失败
	 */
    public boolean login(String url, String account, String password) {
        try{
        	connect(url, account, password);
        }catch(NamingException e){
        	log.warn("[ldap login][result:false][msg:{}]", e.getMessage());
        	return false;
        }
        return true;
    }
    public LdapContext connect(String url, String account, String password) throws NamingException {
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.SECURITY_PRINCIPAL, account);//用户名
        env.put(Context.SECURITY_CREDENTIALS, password);//密码
        env.put(Context.PROVIDER_URL, url);//LDAP的地址：端口
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");//LDAP工厂类
        env.put(Context.SECURITY_AUTHENTICATION, "simple");//认证类型
        LdapContext ctx = null;
        try{
        	 ctx = new InitialLdapContext(env, null);//连接
        }catch(NamingException e){
        	log.warn("[ldap login][result:false][msg:{}]", e.getMessage());
        	throw e;
        }
        return ctx;
    }
}