package org.anyline.ldap.util;


import org.anyline.util.BasicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

public class LdapUtil {
 	private static Logger log = LoggerFactory.getLogger(LdapUtil.class);

	private LdapConfig config = null;
	private static Hashtable<String, LdapUtil> instances = new Hashtable<String, LdapUtil>();


	public static LdapUtil getInstance(){
		return getInstance("default"); 
	} 
	public static LdapUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		LdapUtil util = instances.get(key);
		if(null == util){ 
			util = new LdapUtil();
			LdapConfig config = LdapConfig.getInstance(key);
			util.config = config;
			instances.put(key, util);
		} 
		return util; 
	}
	public boolean login(String account, String password) {
		try{
			connect(account, password).close();
		}catch(Exception e){
			log.warn("[ldap login][result:false][msg:{}]", e.getMessage());
			return false;
		}
		return true;
	}
	/**
	 * 连接
	 * @param account 登录帐号 如 admin@anyline.org
	 * @param password 登录密码
	 * @return LdapContext
	 * @throws NamingException 如果抛出异常表示登录失败
	 */
	public LdapContext connect(String account, String password) throws NamingException {
		if(null != account && account.endsWith(config.DOMAIN)){
			account = account + "@" + config.DOMAIN;
		}
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.SECURITY_PRINCIPAL, account);		//用户名
		env.put(Context.SECURITY_CREDENTIALS, password);	//密码
		env.put(Context.PROVIDER_URL, config.URL);			//LDAP的地址：端口
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");//LDAP工厂类
		env.put(Context.SECURITY_AUTHENTICATION, config.SECURITY_AUTHENTICATION);//认证类型
		LdapContext ctx = null;
		try{
			ctx = new InitialLdapContext(env, null);//连接
		}catch(NamingException e){
			log.warn("[ldap connect][result:false][msg:{}]", e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return ctx;
	}
} 
