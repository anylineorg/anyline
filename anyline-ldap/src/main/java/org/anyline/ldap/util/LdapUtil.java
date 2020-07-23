package org.anyline.ldap.util;


import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.*;

public class LdapUtil {
 	private static Logger log = LoggerFactory.getLogger(LdapUtil.class);

	private LdapConfig config = null;
	private DirContext dc = null;
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
	/**
	 * 登录
	 * @param account 帐号
	 * @param password 密码
	 * @return 是否登录成功
	 */
	public boolean login(String account, String password) throws Exception{
		try{
			connect(account, password).close();
		}catch(Exception e){
			log.warn("[ldap login][result:false][msg:{}]", e.getMessage());
			throw e;
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
	public DirContext connect(String account, String password) throws NamingException {
		if(null != account && !account.endsWith(config.DOMAIN)){
			account = account + "@" + config.DOMAIN;
		}
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.SECURITY_PRINCIPAL, account);		//用户名
		env.put(Context.SECURITY_CREDENTIALS, password);	//密码
		env.put(Context.PROVIDER_URL, config.URL);			//LDAP的地址：端口
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");//LDAP工厂类
		env.put(Context.SECURITY_AUTHENTICATION, config.SECURITY_AUTHENTICATION);//认证类型
		try{
			dc = new InitialLdapContext(env, null);//连接
		}catch(NamingException e){
			log.warn("[ldap connect][result:false][msg:{}]", e.getMessage());
			throw e;
		}
		return dc;
	}


	/**
	 * 注册用户
	 * @param name 用户名
	 * @param password 密码
	 * @param attributes 其他属性
	 * @return boolean
	 */
	public boolean addUser(String name, String password, Map<String,String> attributes) {
		try {
			BasicAttributes attrs = new BasicAttributes();
			BasicAttribute objclassSet = new BasicAttribute("objectClass");
			objclassSet.add("UserPrincipalName");
			objclassSet.add("employeeID");
			attrs.put(objclassSet);
			attrs.put("ou", name);
			List<String> keys = BeanUtil.getMapKeys(attributes);
			for(String key:keys){
				attrs.put(key, attributes.get(key));
			}
			byte[] unicodePassword = null;
			try {
				unicodePassword = password.getBytes("UTF-16LE");
			} catch (Exception e) {
				e.printStackTrace();
			}
			attrs.put("unicodePwd", unicodePassword);
			dc.createSubcontext("ou=" + name + "," + config.ROOT, attrs);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 移动到新ou
	 * @param dn dn
	 * @param ou ou
	 * @return boolean
	 */
	public boolean changeOU(String dn, String ou) {
		String newDN = dn.split(",")[0] + "," + ou;
		try {
			dc.rename(dn, newDN);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 添加部门
	 * @param name 名称
	 * @return boolean
	 */
	public boolean addOrganizationalUnit(String name) {
		try {
			BasicAttributes attrs = new BasicAttributes();
			BasicAttribute objclassSet = new BasicAttribute("objectClass");
			objclassSet.add("top");
			objclassSet.add("organizationalUnit");
			attrs.put(objclassSet);
			attrs.put("ou", name);
			attrs.put("description", name);
			dc.createSubcontext("ou=" + name + "," + config.ROOT, attrs);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 重命名
	 * @param fr 原名
	 * @param to 新名
	 * @return boolean
	 */
	public boolean rename(String fr, String to) {
		try {
			dc.rename(fr, to);
			return true;
		} catch (NamingException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除用户
	 * @param dn dn
	 * @return boolean
	 */
	public boolean delete(String dn) {
		try {
			dc.destroySubcontext(dn);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	/**
	 * 修改属性
	 * @param dn dn
	 * @param params 属性
	 * @return boolean
	 */
	public boolean update(String dn, Map<String,String> params) {
		try {
			ModificationItem[] mods = new ModificationItem[1];
			List<String> keys = BeanUtil.getMapKeys(params);
			int index = 0;
			for(String key:keys){
				String value = params.get(key);
				Attribute attr = new BasicAttribute(key, value);
				ModificationItem mod = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
				mods[index++] = mod;
			}
			dc.modifyAttributes(dn + ","+config.ROOT, mods);
			return true;
		} catch (NamingException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @param base  ：根节点(在这里是"dc=example,dc=com")
	 * @param scope ：搜索范围,本节点(SearchControls.OBJECT_SCOPE),单层(SearchControls.ONELEVEL_SCOPE),遍历(SearchControls.SUBTREE_SCOPE)
	 * @param filter  ：指定子节点(格式为"(objectclass=*)",*是指全部，可以指定某一特定类型的树节点)
	 */
	public DataSet search(String base, int scope, String filter) {
		DataSet set = new DataSet();
		SearchControls sc = new SearchControls();
		sc.setSearchScope(scope);
		NamingEnumeration ne = null;
		try {
			ne = dc.search(base, filter, sc);
			while (ne.hasMore()) {
				DataRow row = new DataRow();
				SearchResult sr = (SearchResult) ne.next();
				String name = sr.getName();

				if (base != null && !base.equals("")) {
					row.put("name", name + "," + base);
				} else {
					row.put("name", name);
				}

				Attributes at = sr.getAttributes();
				NamingEnumeration attrs = at.getAll();
				while (attrs.hasMore()) {
					Attribute attr = (Attribute) attrs.next();
					String attrId = attr.getID();
					NamingEnumeration values = attr.getAll();
					List<String> vals = new ArrayList<String>();
					while (values.hasMore()) {
						Object oneVal = values.nextElement();
						if (oneVal instanceof String) {
							vals.add((String) oneVal);
						} else {
							vals.add(new String((byte[]) oneVal));
						}
					}
					row.put(attrId,  vals);
				}
				set.add(row);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return set;
	}

} 
