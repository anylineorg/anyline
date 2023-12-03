/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.mail.entity;
 
import java.io.File; 
import java.util.ArrayList; 
import java.util.Date; 
import java.util.List; 
 
public class Mail {
	private String subject; 
	private String content; 
	private Date sendTime; 
	private String sender; 
	private Date receiveTime; 
	private String receiver; 
	private List<File> attachments = new ArrayList<>();
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
	public Date getSendTime() {
		return sendTime; 
	} 
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime; 
	} 
	public String getSender() {
		return sender; 
	} 
	public void setSender(String sender) {
		this.sender = sender; 
	} 
	public Date getReceiveTime() {
		return receiveTime; 
	} 
	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime; 
	} 
	public String getReceiver() {
		return receiver; 
	} 
	public void setReceiver(String receiver) {
		this.receiver = receiver; 
	} 
	public List<File> getAttachments() {
		return attachments; 
	} 
	public void setAttachments(List<File> attachments) {
		this.attachments = attachments; 
	} 
 
	 
} 
