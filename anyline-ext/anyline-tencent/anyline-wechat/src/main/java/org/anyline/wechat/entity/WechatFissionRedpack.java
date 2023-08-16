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


package org.anyline.wechat.entity;
 
public class WechatFissionRedpack extends WechatRedpack {
	protected String amt_type = "ALL_RAND"	;//红包金额设置方式	是	ALL_RAND	String(32)	红包金额设置方式 ALL_RAND—全部随机,商户指定总金额和红包发放总人数,由微信支付随机计算出各红包金额 
 
	public String getAmt_type() {
		return amt_type; 
	} 
 
	public void setAmt_type(String amt_type) {
		this.amt_type = amt_type; 
	} 
	 
} 
