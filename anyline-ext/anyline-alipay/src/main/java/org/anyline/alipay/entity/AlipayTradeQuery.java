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


package org.anyline.alipay.entity;
/** 
 * 单笔转账到支付宝账户 
 * 
 */ 
public class AlipayTradeQuery {
	private String out_biz_no	; // 商户转账唯一订单号:发起转账来源方定义的转账单据ID. 和支付宝转账单据号不能同时为空.当和支付宝转账单据号同时提供时,将用支付宝转账单据号进行查询,忽略本参数.	 
	private String order_id		; // 支付宝转账单据号:和商户转账唯一订单号不能同时为空.当和商户转账唯一订单号同时提供时,将用本参数进行查询,忽略商户转账唯一订单号. 
	public String getOut_biz_no() {
		return out_biz_no; 
	} 
	public void setOut_biz_no(String out_biz_no) {
		this.out_biz_no = out_biz_no; 
	} 
	public String getOrder_id() {
		return order_id; 
	} 
	public void setOrder_id(String order_id) {
		this.order_id = order_id; 
	} 
	 
} 
