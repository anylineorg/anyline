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


package org.anyline.sms.entity;
 
public class SMSResult {
	private boolean result;
	// 1 等待回执
	// 2 发送失败
	// 3 发送成功
	private int status;
	private String code;
	private String msg;
	private String biz;	// 发送回执ID,即发送流水号,查询送达状态时用到
	private String requestId;
	// 发送后查询状态
	private String content;
	private String out;
	private String mobile;
	private String receiveTime;
	private String sendTime;
	private String template;


	public String getRequestId() {
		return this.requestId;
	}

	public void setRequestId(final String requestId) {
		this.requestId = requestId;
	}

	public boolean isResult() {
		return result; 
	} 
	public void setResult(boolean result) {
		this.result = result; 
	} 
	public String getMsg() {
		return msg; 
	} 
	public void setMsg(String msg) {
		this.msg = msg; 
	} 
	public String getCode() {
		return code; 
	} 
	public void setCode(String code) {
		this.code = code; 
	}

	public String getBiz() {
		return biz;
	}

	public void setBiz(String biz) {
		this.biz = biz;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(String receiveTime) {
		this.receiveTime = receiveTime;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}
}
