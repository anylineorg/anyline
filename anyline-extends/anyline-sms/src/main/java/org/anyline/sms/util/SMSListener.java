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


package org.anyline.sms.util;

import org.anyline.sms.entity.SMSResult;

import java.util.Map;

public interface SMSListener {

    /**
     * 发送短信
     * @param instance 实例KEY
     * @param sign 签名(如果不指定则使用配置文件中默认签名)
     * @param template 模板code(SMS_88550009,注意不要写成工单号)
     * @param mobile 手机号,多个以逗号分隔
     * @param params 参数
     * @param extend 上行短信扩展码。上行短信指发送给通信服务提供商的短信,用于定制某种服务、完成查询,或是办理某种业务等,需要收费,按运营商普通短信资费进行扣费。
     * @param out 外部流水扩展字段。
     * @return boolean 是否继续发送
     */
    public boolean before(String instance, String sign, String template, String extend, String out, String mobile, Map<String, String> params);
    public boolean after(SMSResult result, String instance, String sign, String template, String extend, String out, String mobile, Map<String, String> params);
}
