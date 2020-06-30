package org.anyline.mail.util; 
 
import org.anyline.util.BasicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Hashtable;
import java.util.Properties;
 
public class MailUtil { 
	private static final Logger log = LoggerFactory.getLogger(MailUtil.class); 
	private MailConfig config = null; 
	private Properties props = new Properties(); 
	private static Hashtable<String, MailUtil> instances = new Hashtable<String, MailUtil>();


	public MailConfig getConfig(){ 
		return config; 
	} 
	public static MailUtil getInstance() { 
		return getInstance("default"); 
	} 

	public static MailUtil getInstance(String key) { 
		if (BasicUtil.isEmpty(key)) { 
			key = "default"; 
		} 
		MailUtil util = instances.get(key); 
		if (null == util) { 
			util = new MailUtil(); 
			MailConfig config = MailConfig.getInstance(key); 
			util.config = config; 
			util.props.put("username", config.ACCOUNT); 
			util.props.put("password", config.PASSWORD); 
			util.props.put("mail.transport.protocol", config.PROTOCOL); 
			util.props.put("mail.smtp.host", config.HOST); 
			util.props.put("mail.smtp.port", config.PORT); 
 
			instances.put(key, util); 
		} 
		return util; 
	} 
 
	/** 
	 *  
	 * @param fr		发送人姓名  fr		发送人姓名
	 * @param to		收件人地址  to		收件人地址
	 * @param title		邮件主题  title		邮件主题
	 * @param content	邮件内容  content	邮件内容
	 * @return return
	 */ 
	public boolean send(String fr, String to, String title, String content) { 
		log.warn("[send email][fr:{}][to:{}][title:{}][content:{}]", fr,to,title,content);
		try { 
			Session mailSession = Session.getDefaultInstance(props); 
			Message msg = new MimeMessage(mailSession); 
			msg.setFrom(new InternetAddress(config.ACCOUNT,fr)); 
			msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); 
			msg.setSubject(title + ""); 
			msg.setContent(content + "", "text/html;charset=UTF-8"); 
			msg.saveChanges(); 
			Transport transport = mailSession.getTransport("smtp"); 
			transport.connect(config.HOST,
					config.ACCOUNT, 
					config.PASSWORD);
			transport.sendMessage(msg, msg.getAllRecipients()); 
			transport.close(); 
		} catch (Exception e) { 
			e.printStackTrace(); 
			return false; 
		} 
 
		return true; 
	} 
	public boolean send(String to, String title, String content) { 
		return send(config.USERNAME, to, title, content);
	} 
}
