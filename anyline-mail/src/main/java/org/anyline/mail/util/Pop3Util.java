package org.anyline.mail.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.anyline.mail.entity.Mail;
import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;
import org.anyline.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pop3Util {
	private static final Logger log = LoggerFactory.getLogger(Pop3Util.class);
	private MailConfig config = null;
	private Properties props = new Properties();
	private static Hashtable<String, Pop3Util> instances = new Hashtable<String, Pop3Util>();

	public MailConfig getConfig() {
		return config;
	}

	public static Pop3Util getInstance() {
		return getInstance("default");
	}

	public static Pop3Util getInstance(String key) {
		if (BasicUtil.isEmpty(key)) {
			key = "default";
		}
		Pop3Util util = instances.get(key);
		if (null == util) {
			util = new Pop3Util();
			MailConfig config = MailConfig.getInstance(key);
			util.config = config;
			util.props.put("username", config.ACCOUNT);
			util.props.put("password", config.PASSWORD);
			util.props.put("mail.store.protocol", config.PROTOCOL);
			util.props.put("mail.pop3.host", config.HOST);
			util.props.put("mail.pop3.port", config.PORT);
			instances.put(key, util);
		}
		return util;
	}

	/**
	 * 
	 * @param fr 发送人姓名
	 * @param to 收件人地址
	 * @param title 邮件主题
	 * @param content  邮件内容
	 * @return
	 */
	public boolean send(String fr, String to, String title, String content) {
		log.warn("[send email][fr:{}][to:{}][title:{}][centent:{}]", fr, to, title, content);
		try {
			Session mailSession = Session.getDefaultInstance(props);
			Message msg = new MimeMessage(mailSession);
			msg.setFrom(new InternetAddress(config.ACCOUNT, fr));
			msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			msg.setSubject(title);
			msg.setContent(content, "text/html;charset=UTF-8");
			msg.saveChanges();
			Transport transport = mailSession.getTransport("smtp");
			transport.connect(config.HOST, config.ACCOUNT, config.PASSWORD);
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	public int getMailQty(){
		int qty = -1;
		Session session = Session.getInstance(props);
		Store store = null;
		Folder folder = null;
		try{
			store = session.getStore("pop3");
			store.connect(config.ACCOUNT, config.PASSWORD);
			// 收件箱
			folder = store.getFolder("INBOX");
			qty = folder.getMessageCount();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			// 释放资源
			try {
				if(null != folder){
					folder.close(true);
				}
			} catch (MessagingException e) {}
			try {
				if(null != store){
					store.close();
				}
			} catch (MessagingException e) {}
		}
		return qty;
	}
	public boolean send(String to, String title, String content) {
		return send(config.USERNAME, to, title, content);
	}

	public List<Mail> receive(int qty){
		return receive(false, false, qty);
	}
	public List<Mail> receive(boolean read, boolean delete){
		return receive(read, delete,1,-1);
	}
	public List<Mail> receive(boolean read, boolean delete, int qty){
		return receive(read, delete, 1, qty);
	}
	/**
	 * 接收邮件
	 * @param read 是否设置已读
	 * @param delete 是否删除
	 * @param fr 下标从1开始
	 * @param to -1:全部
	 * @return
	 */
	public List<Mail> receive(boolean read, boolean delete, int fr, int to){
		List<Mail> mails = new ArrayList<Mail>();
		Session session = Session.getInstance(props);
		Store store = null;
		Folder folder = null;
		try{
			store = session.getStore("pop3");
			store.connect(config.ACCOUNT, config.PASSWORD);
			// 收件箱
			folder = store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
			// 得到收件箱中的所有邮件,并解析
			int cnt = folder.getMessageCount();
			if(to == -1 || to > cnt){
				to = cnt;
			}
			if(fr == 0){
				fr = 1;
				to = to +1;
			}
			if(to >cnt){
				to = cnt;
			}
			Message[] messages = folder.getMessages(fr, to);
			mails = parse(read, delete, messages);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			// 释放资源
			try {
				if(null != folder){
					folder.close(true);
				}
			} catch (MessagingException e) {}
			try {
				if(null != store){
					store.close();
				}
			} catch (MessagingException e) {}
		}
		return mails;
	}

	/**
	 * 解析邮件
	 * @param messages   要解析的邮件列表
	 */
	public List<Mail> parse( boolean setRead, boolean delete, Message... messages){
		List<Mail> mails = new ArrayList<Mail>();
		try{
		for (Message item:messages) {
			Mail mail = new Mail();
			MimeMessage msg = (MimeMessage) item;
			Date sendTime = msg.getSentDate();
			Date receiveTime = msg.getReceivedDate();
			String subject = msg.getSubject();
			boolean isSeen = isSeen(msg);
			boolean isContainerAttachment = isContainAttachment(msg);
			log.info("[解析邮件][subject:{}][发送时间:{}][是否已读:{}][是否包含附件:{}]",subject,DateUtil.format(sendTime),isSeen,isContainerAttachment);
			mail.setSubject(subject);
			mail.setSendTime(sendTime);
			mail.setReceiveTime(receiveTime);
			if(config.AUTO_DOWNLOAD_ATTACHMENT){
				if (isContainerAttachment) {
					String dir = config.ATTACHMENT_DIR.replace("{ymd}", DateUtil.format(sendTime,"yyyyMMdd"));
					List<File> attachments = downloadAttachment(msg, dir , sendTime, null);
					mail.setAttachments(attachments);
				}
			}
			if(!isSeen && setRead){
				seen(msg);
			}
			if(delete){
				delete(msg);
			}
			mails.add(mail);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return mails;
	}

    /**
     * 删除邮件
     * @param messages
     * @throws MessagingException
     * @throws IOException
     */
    public static void delete(Message ...messages){  
        for (int i = 0, count = messages.length; i < count; i++) {  
            Message message = messages[i];
            String subject;
			try {
				subject = message.getSubject();
	            message.setFlag(Flags.Flag.DELETED, true);
	            log.warn("[删除邮件][subject:{}]",subject);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
        }
    } 
    
    /**
     * 标记为已读
     * @param messages
     * @throws MessagingException
     * @throws IOException
     */
    public static void seen(Message ...messages) {  
        for (int i = 0, count = messages.length; i < count; i++) {  
            Message message = messages[i];
            String subject;
			try {
				subject = message.getSubject();
	            message.setFlag(Flags.Flag.SEEN, true);
	            log.warn("[标记为已读][subject:{}]",subject);   
			} catch (MessagingException e) {
				e.printStackTrace();
			} 
        }
    } 
    /** 
     * 获得邮件主题 
     * @param msg 邮件内容 
     * @return 解码后的邮件主题 
     */  
    public static String getSubject(MimeMessage msg) throws UnsupportedEncodingException, MessagingException {  
        return MimeUtility.decodeText(msg.getSubject());  
    }  
      
    /** 
     * 获得邮件发件人 
     * @param msg 邮件内容 
     * @return 姓名 <Email地址> 
     * @throws MessagingException 
     * @throws UnsupportedEncodingException  
     */  
    public static String getFrom(MimeMessage msg){  
        String from = "";  
        Address[] froms;
		try {
			froms = msg.getFrom();
	        InternetAddress address = (InternetAddress) froms[0];  
	        String person = address.getPersonal();  
	        if (person != null) {  
	            person = MimeUtility.decodeText(person) + " ";  
	        } else {  
	            person = "";  
	        }  
	        from = person + "<" + address.getAddress() + ">";  
		} catch (Exception e) {
			e.printStackTrace();
		}  
        return from;  
    }  
      
	/**
	 * 判断邮件是否已读
	 * 
	 * @param msg
	 *            邮件内容
	 * @return 如果邮件已读返回true,否则返回false
	 * @throws MessagingException
	 */
	public static boolean isSeen(MimeMessage msg){
		try {
			return msg.getFlags().contains(Flags.Flag.SEEN);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return false;
	}


	/**
	 * 判断邮件中是否包含附件
	 * 
	 * @param msg
	 *            邮件内容
	 * @return 邮件中存在附件返回true，不存在返回false
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static boolean isContainAttachment(Part part)
			throws MessagingException, IOException {
		boolean flag = false;
		if (part.isMimeType("multipart/*")) {
			MimeMultipart multipart = (MimeMultipart) part.getContent();
			int partCount = multipart.getCount();
			for (int i = 0; i < partCount; i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				String disp = bodyPart.getDisposition();
				if (disp != null
						&& (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp
								.equalsIgnoreCase(Part.INLINE))) {
					flag = true;
				} else if (bodyPart.isMimeType("multipart/*")) {
					flag = isContainAttachment(bodyPart);
				} else {
					String contentType = bodyPart.getContentType();
					if (contentType.indexOf("application") != -1) {
						flag = true;
					}

					if (contentType.indexOf("name") != -1) {
						flag = true;
					}
				}

				if (flag)
					break;
			}
		} else if (part.isMimeType("message/rfc822")) {
			flag = isContainAttachment((Part) part.getContent());
		}
		return flag;
	}

	/**
	 * 保存附件
	 * 
	 * @param part
	 *            邮件中多个组合体中的其中一个组合体
	 * @param dir
	 *            附件保存目录
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */

	public static List<File> downloadAttachment(Part part, String dir, Date sendTime, List<File> files) throws UnsupportedEncodingException, MessagingException, FileNotFoundException, IOException {
		if (BasicUtil.isEmpty(files)) {
			files = new ArrayList<File>();
		}
		dir = dir.replace("{send_date}", DateUtil.format(sendTime,"yyyyMMdd"));
		dir = dir.replace("{send_time}", DateUtil.format(sendTime,"yyyyMMddhhmmss"));
		if (part.isMimeType("multipart/*")) {
			Multipart multipart = (Multipart) part.getContent(); // 复杂体邮件
			int partCount = multipart.getCount();
			for (int i = 0; i < partCount; i++) {
				boolean result = false;
				BodyPart bodyPart = multipart.getBodyPart(i);
				String disp = bodyPart.getDisposition();
				String name = decode(bodyPart.getFileName());
				String path = null;
				if(dir.contains("{file}")){
					path = dir.replace("{file}", name);
				}else{
					path = FileUtil.mergePath(dir, name);
				}
				File file =  new File(path);
				if (BasicUtil.isNotEmpty(name) && disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
					result = FileUtil.save(bodyPart.getInputStream(), file);
				} else if (bodyPart.isMimeType("multipart/*")) {
					downloadAttachment(bodyPart, dir, sendTime, files);
				} else if(BasicUtil.isNotEmpty(name)){
					String contentType = bodyPart.getContentType();
					if (contentType.indexOf("name") != -1 || contentType.indexOf("application") != -1) {
						result = FileUtil.save(bodyPart.getInputStream(), file);
					}
				}
				if (result) {
					files.add(file);
				}
			}
		} else if (part.isMimeType("message/rfc822")) {
			downloadAttachment((Part) part.getContent(), dir, sendTime, files);
		}
		return files;
	}

  
	/** 
     * 文本解码 
     * @param encodeText 解码MimeUtility.encodeText(String text)方法编码后的文本 
     * @return 解码后的文本 
     * @throws UnsupportedEncodingException 
     */  
    public static String decode(String text) throws UnsupportedEncodingException {  
        if (text == null || "".equals(text)) {  
            return "";  
        } else {  
            return MimeUtility.decodeText(text);  
        }  
    }
}