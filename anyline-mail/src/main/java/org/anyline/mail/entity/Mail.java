package org.anyline.mail.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Mail {
	private String subject;
	private String content;
	private String sendTime;
	private String sender;
	private String receiveTime;
	private String receiver;
	private List<File> attachments = new ArrayList<File>();
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List<File> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<File> attachments) {
		this.attachments = attachments;
	}
	public String getSendTime() {
		return sendTime;
	}
	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getReceiveTime() {
		return receiveTime;
	}
	public void setReceiveTime(String receiveTime) {
		this.receiveTime = receiveTime;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	
}
