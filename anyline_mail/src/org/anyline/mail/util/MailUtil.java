package org.anyline.mail.util;

import java.util.Hashtable;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.anyline.util.BasicUtil;
import org.apache.log4j.Logger;

public class MailUtil {
	private static Logger log = Logger.getLogger(MailUtil.class);
	private MailConfig config = null;
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
			instances.put(key, util);
		}
		return util;
	}

	/**
	 * 
	 * @param fr		发送人姓名
	 * @param to		收件人地址
	 * @param title		邮件主题
	 * @param content	邮件内容
	 * @return
	 */
	public boolean send(String fr, String to, String title, String content) {
		log.warn("[send email][fr:"+fr+"][to:"+to+"][title:"+title+"][centent:"+content+"]");
		try {
			Properties props = new Properties();
			props.put("username", config.USER_NAME);
			props.put("password", config.PASSWORD);
			props.put("mail.transport.protocol", config.PROTOCOL);
			props.put("mail.smtp.host", config.HOST);
			props.put("mail.smtp.port", config.PORT);

			Session mailSession = Session.getDefaultInstance(props);

			Message msg = new MimeMessage(mailSession);
			msg.setFrom(new InternetAddress(config.USER_NAME,fr));
			msg.addRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));

			msg.setSubject(title + "");
			msg.setContent(content + "", "text/html;charset=UTF-8");

			msg.saveChanges();

			Transport transport = mailSession.getTransport("smtp");
			transport.connect(config.HOST,
					config.USER_NAME,
					config.PASSWORD);
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}