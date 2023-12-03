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


package org.anyline.wechat.pay.util;

import org.anyline.entity.DataRow;
import org.anyline.net.HttpBuilder;
import org.anyline.net.HttpUtil;
import org.anyline.net.SimpleHttpUtil;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.encrypt.RSAUtil;
import org.anyline.wechat.entity.*;
import org.anyline.wechat.util.WechatUtil;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class WechatPayUtil {
    protected static final Logger log = LoggerFactory.getLogger(WechatPayUtil.class);

    private WechatPayConfig config = null;

    private static Hashtable<String, WechatPayUtil> instances = new Hashtable<String,WechatPayUtil>();

    static {
        Hashtable<String, AnylineConfig> configs = WechatPayConfig.getInstances();
        for(String key:configs.keySet()){
            instances.put(key, getInstance(key));
        }
    }

    public static Hashtable<String, WechatPayUtil> getInstances(){
        return instances;
    }
    public static WechatPayUtil getInstance(){
        return getInstance(WechatPayConfig.DEFAULT_INSTANCE_KEY);
    }
    public WechatPayUtil(WechatPayConfig config){
        this.config = config;
    }
    public WechatPayUtil(String key, DataRow config){
        WechatPayConfig conf = WechatPayConfig.parse(key, config);
        this.config = conf;
        instances.put(key, this);
    }
    public static WechatPayUtil register(String key, DataRow config){
        WechatPayConfig conf = WechatPayConfig.register(key, config);
        WechatPayUtil util = new WechatPayUtil(conf);
        instances.put(key, util);
        return util;
    }
    public static WechatPayUtil getInstance(String key){
        if(BasicUtil.isEmpty(key)){
            key = WechatPayConfig.DEFAULT_INSTANCE_KEY;
        }
        WechatPayUtil util = instances.get(key);
        if(null == util){
            WechatPayConfig config = WechatPayConfig.getInstance(key);
            if(null != config) {
                util = new WechatPayUtil(config);
                instances.put(key, util);
            }
        }
        return util;
    }

    public WechatPayConfig getConfig() {
        return config;
    }

    /**
     * 统一下单
     * @param order  order
     * @return WechatPrePayResult WechatPrePayResult
     * @throws Exception 异常 Exception
     */
    public WechatPrePayResult unifiedorder(WechatPrePayOrder order) throws Exception{
        WechatPrePayResult result = null;
        order.setNonce_str(BasicUtil.getRandomLowerString(20));
        if(null == order.getAppid()){
            throw new Exception("未设置appid");
        }
        if("JSAPI".equals(order.getTrade_type()) && null == order.getOpenid()){
            throw new Exception("未设置openid");
        }
        if(BasicUtil.isEmpty(order.getMch_id())){
            order.setMch_id(config.MCH_ID);
        }
        if(BasicUtil.isEmpty(order.getNotify_url())){
            order.setNotify_url(config.NOTIFY_URL);
        }
        if(BasicUtil.isEmpty(order.getNotify_url())){
            // 	order.setNotify_url(WechatProgramConfig.getInstance().NOTIFY_URL);
        }
        if(BasicUtil.isEmpty(order.getOut_trade_no())){
            throw new Exception("未设置交易单号");
        }
        Map<String, Object> map = BeanUtil.toMap(order);
        String sign = WechatUtil.sign(config.API_SECRET,map);
        map.put("sign", sign);
        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[统一下单][sign:{}}", sign);
        }
        String xml = BeanUtil.map2xml(map);

        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[统一下单][xml:{}]", xml);
        }
        String rtn = SimpleHttpUtil.post(WechatPayConfig.API_URL_UNIFIED_ORDER, xml);

        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[统一下单][return:{}]", rtn);
        }
        result = BeanUtil.xml2object(rtn, WechatPrePayResult.class);
        if(BasicUtil.isNotEmpty(result.getPrepay_id())){
            result.setResult(true);
        }

        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[统一下单][prepay id:{}]", result.getPrepay_id());
        }
        return result;
    }


    /**
     * JSAPI调起支付所需参数
     * @param appid appid
     * @param prepayid 预支付id(由统一下单接口返回)
     * @return DataRow
     */
    public DataRow callUpParam(String appid, String prepayid){
        String timestamp = System.currentTimeMillis()/1000+"";
        String random = BasicUtil.getRandomLowerString(20);
        String pkg = "prepay_id="+prepayid;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("package", pkg);
        params.put("timeStamp", timestamp);
        params.put("appId", appid);
        params.put("nonceStr", random);
        params.put("signType", "MD5");
        String sign = WechatUtil.sign(config.API_SECRET, params);
        params.put("paySign", sign);

        DataRow row = new DataRow(params);
        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[jsapi调起微信支付][参数:{}]", row.toJSON());
        }
        return row;
    }

    /**
     * 退款申请
     * @param refund  refund
     * @return WechatRefundResult
     * @throws Exception 异常  Exception
     */
    public WechatRefundResult refund(WechatRefund refund) throws Exception{
        WechatRefundResult result = null;
        if(null == refund.getAppid()){
            throw new Exception("未设置appid");
        }
        refund.setNonce_str(BasicUtil.getRandomLowerString(20));
        if(BasicUtil.isEmpty(refund.getMch_id())){
            refund.setMch_id(config.MCH_ID);
        }
        Map<String, Object> map = BeanUtil.toMap(refund);
        String sign = WechatUtil.sign(config.API_SECRET,map);

        map.put("sign", sign);

        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[退款申请][sign:{}]", sign);
        }
        String xml = BeanUtil.map2xml(map);

        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[退款申请][xml:{}]", xml);
            log.warn("[退款申请][证书:{}]", config.KEY_STORE_FILE);
        }
        File keyStoreFile = new File(config.KEY_STORE_FILE);
        if(!keyStoreFile.exists()){
            log.warn("[密钥文件不存在][file:{}]",config.KEY_STORE_FILE);
            return new WechatRefundResult(false,"密钥文件不存在");
        }
        String keyStorePassword = config.KEY_STORE_PASSWORD;
        if(BasicUtil.isEmpty(keyStorePassword)){
            log.warn("未设置密钥文件密码");
            return new WechatRefundResult(false,"未设置密钥文件密码");
        }
        try{
            CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity reqEntity  = new StringEntity(xml);
            reqEntity.setContentType("application/x-www-form-urlencoded");
            String txt = HttpBuilder.init()
                    .setClient(httpclient)
                    .setUrl(WechatPayConfig.API_URL_REFUND)
                    .setCharset("UTF-8")
                    .setEntity(reqEntity)
                    .build().get().getText();
            // String txt = HttpUtil.post(httpclient, WechatPayConfig.API_URL_REFUND, "UTF-8", reqEntity).getText();
            if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
                log.warn("[退款申请调用][result:{}", txt);
            }
            result = BeanUtil.xml2object(txt, WechatRefundResult.class);
        }catch(Exception e){
            e.printStackTrace();
            return new WechatRefundResult(false,e.toString());
        }
        return result;
    }
    /**
     * 发送红包
     * @param pack  pack
     * @return WechatRedpackResult
     * @throws Exception 异常  Exception
     */
    public WechatRedpackResult sendRedpack(WechatRedpack pack) throws Exception{
        WechatRedpackResult result = new WechatRedpackResult();
        if(null == pack.getWxappid()){
            throw new Exception("未设置wxappid");
        }
        if(null == pack.getRe_openid()){
            throw new Exception("未设置reopenid");
        }
        pack.setNonce_str(BasicUtil.getRandomLowerString(20));
        if(BasicUtil.isEmpty(pack.getMch_id())){
            pack.setMch_id(config.MCH_ID);
        }

        if(BasicUtil.isEmpty(pack.getMch_billno())){
            pack.setMch_billno(BasicUtil.getRandomLowerString(20));
        }
        Map<String, Object> map = BeanUtil.toMap(pack);
        String sign = WechatUtil.sign(config.API_SECRET,map);

        map.put("sign", sign);

        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[发送红包[sign:{}]", sign);
        }
        String xml = BeanUtil.map2xml(map);
        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[发送红包][xml:{}]", xml);
            log.warn("[发送红包][证书:{}]", config.KEY_STORE_FILE);
        }

        File keyStoreFile = new File(config.KEY_STORE_FILE);
        if(!keyStoreFile.exists()){
            log.warn("[密钥文件不存在][file:{}]",config.KEY_STORE_FILE);
            return new WechatRedpackResult(false,"密钥文件不存在");
        }
        String keyStorePassword = config.KEY_STORE_PASSWORD;
        if(BasicUtil.isEmpty(keyStorePassword)){
            log.warn("未设置密钥文件密码");
            return new WechatRedpackResult(false,"未设置密钥文件密码");
        }
        try{
            CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
            reqEntity.setContentType("application/x-www-form-urlencoded");
            String txt = HttpBuilder.init()
                    .setClient(httpclient)
                    .setUrl(WechatPayConfig.API_URL_SEND_REDPACK)
                    .setCharset("UTF-8")
                    .setEntity(reqEntity)
                    .build().get().getText();
            // String txt = HttpUtil.post(httpclient, WechatPayConfig.API_URL_SEND_REDPACK, "UTF-8", reqEntity).getText();
            if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
                log.warn("[发送红包调用][result:{}]", txt);
            }
            result = BeanUtil.xml2object(txt, WechatRedpackResult.class);
        }catch(Exception e){
            e.printStackTrace();
            return new WechatRedpackResult(false,e.toString());
        }
        return result;
    }
    /**
     * 发送裂变红包
     * @param pack  pack
     * @return WechatFissionRedpackResult
     * @throws Exception 异常  Exception
     */
    public WechatFissionRedpackResult sendRedpack(WechatFissionRedpack pack) throws Exception{
        if(null == pack.getWxappid()){
            throw new Exception("未设置wxappid");
        }
        if(null == pack.getRe_openid()){
            throw new Exception("未设置reopenid");
        }
        WechatFissionRedpackResult result = new WechatFissionRedpackResult();
        pack.setNonce_str(BasicUtil.getRandomLowerString(20));
        if(BasicUtil.isEmpty(pack.getMch_id())){
            pack.setMch_id(config.MCH_ID);
        }
        if(BasicUtil.isEmpty(pack.getMch_billno())){
            pack.setMch_billno(BasicUtil.getRandomLowerString(20));
        }
        Map<String, Object> map = BeanUtil.toMap(pack);
        String sign = WechatUtil.sign(config.API_SECRET,map);

        map.put("sign", sign);

        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[发送裂变红包][sign:{}]", sign);
        }
        String xml = BeanUtil.map2xml(map);
        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[发送裂变红包][xml:{}]", xml);
            log.warn("[发送裂变红包][证书:{}]", config.KEY_STORE_FILE);
        }

        File keyStoreFile = new File(config.KEY_STORE_FILE);
        if(!keyStoreFile.exists()){
            log.warn("[密钥文件不存在][file:{}]", config.KEY_STORE_FILE);
            return new WechatFissionRedpackResult(false,"密钥文件不存在");
        }
        String keyStorePassword = config.KEY_STORE_PASSWORD;
        if(BasicUtil.isEmpty(keyStorePassword)){
            log.warn("未设置密钥文件密码");
            return new WechatFissionRedpackResult(false,"未设置密钥文件密码");
        }
        try{
            CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
            reqEntity.setContentType("application/x-www-form-urlencoded");
            String txt = HttpBuilder.init()
                    .setClient(httpclient)
                    .setUrl(WechatPayConfig.API_URL_SEND_GROUP_REDPACK)
                    .setCharset("UTF-8")
                    .setEntity(reqEntity)
                    .build().get().getText();
            // String txt = HttpUtil.post(httpclient, WechatPayConfig.API_URL_SEND_GROUP_REDPACK, "UTF-8", reqEntity).getText();
            if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
                log.warn("[发送裂变红包调用][result:{}]", txt);
            }
            result = BeanUtil.xml2object(txt, WechatFissionRedpackResult.class);
        }catch(Exception e){
            e.printStackTrace();
            return new WechatFissionRedpackResult(false,e.toString());
        }
        return result;
    }

    /**
     * 企业付款
     * @param transfer  transfer
     * @return WechatEnterpriseTransferResult
     */
    public WechatEnterpriseTransferResult transfer(WechatEnterpriseTransfer transfer){
        WechatEnterpriseTransferResult result = new WechatEnterpriseTransferResult();
        transfer.setNonce_str(BasicUtil.getRandomLowerString(20));
        if(BasicUtil.isEmpty(transfer.getMchid())){
            transfer.setMchid(config.MCH_ID);
        }
        if(BasicUtil.isEmpty(transfer.getPartner_trade_no())){
            transfer.setPartner_trade_no(BasicUtil.getRandomLowerString(20));
        }
        Map<String, Object> map = BeanUtil.toMap(transfer);
        String sign = WechatUtil.sign(config.API_SECRET,map);

        map.put("sign", sign);

        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[付款][sign:{}]", sign);
        }
        String xml = BeanUtil.map2xml(map);
        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[付款][xml:{}]", xml);
            log.warn("[付款][证书:{}]", config.KEY_STORE_FILE);
        }

        File keyStoreFile = new File(config.KEY_STORE_FILE);
        if(!keyStoreFile.exists()){
            log.warn("[密钥文件不存在][file:{}]",config.KEY_STORE_FILE);
            return new WechatEnterpriseTransferResult(false,"密钥文件不存在");
        }
        String keyStorePassword = config.KEY_STORE_PASSWORD;
        if(BasicUtil.isEmpty(keyStorePassword)){
            log.warn("未设置密钥文件密码");
            return new WechatEnterpriseTransferResult(false,"未设置密钥文件密码");
        }
        try{
            CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
            reqEntity.setContentType("application/x-www-form-urlencoded");
            String txt = HttpBuilder.init()
                    .setClient(httpclient)
                    .setUrl(WechatPayConfig.API_URL_COMPANY_TRANSFER)
                    .setCharset("UTF-8")
                    .setEntity(reqEntity)
                    .build().get().getText();
            // String txt = HttpUtil.post(httpclient, WechatPayConfig.API_URL_COMPANY_TRANSFER, "UTF-8", reqEntity).getText();
            if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
                log.warn("[付款调用][result:{}]", txt);
            }
            result = BeanUtil.xml2object(txt, WechatEnterpriseTransferResult.class);
        }catch(Exception e){
            e.printStackTrace();
            return new WechatEnterpriseTransferResult(false,e.toString());
        }
        return result;
    }
    /**
     * 企业付款到银行卡
     * @param transfer  transfer
     * @return WechatEnterpriseTransferBankResult
     */
    public WechatEnterpriseTransferBankResult transfer(WechatEnterpriseTransferBank transfer){
        WechatEnterpriseTransferBankResult result = new WechatEnterpriseTransferBankResult();
        transfer.setNonce_str(BasicUtil.getRandomLowerString(20));
        String enc_bank_no = transfer.getEnc_bank_no();
        String enc_true_name = transfer.getEnc_true_name();
        if(BasicUtil.isEmpty(enc_bank_no)){
            log.warn("未提供收款卡号");
            return new WechatEnterpriseTransferBankResult(false,"未提供收款卡号");
        }
        if(BasicUtil.isEmpty(enc_true_name)){
            log.warn("未提供收款人姓名");
            return new WechatEnterpriseTransferBankResult(false,"未提供收款人姓名");
        }
        try {
            enc_bank_no = RSAUtil.encrypt(enc_bank_no, RSAUtil.createPublicKey(new File(config.BANK_RSA_PUBLIC_KEY_FILE)));
        }catch(Exception e){
            e.printStackTrace();
        }
        if(BasicUtil.isEmpty(transfer.getMch_id())){
            transfer.setMch_id(config.MCH_ID);
        }
        if(BasicUtil.isEmpty(transfer.getPartner_trade_no())){
            transfer.setPartner_trade_no(BasicUtil.getRandomLowerString(20));
        }
        Map<String, Object> map = BeanUtil.toMap(transfer);
        String sign = WechatUtil.sign(config.API_SECRET,map);

        map.put("sign", sign);

        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[付款][sign:{}]", sign);
        }
        String xml = BeanUtil.map2xml(map);
        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[付款][xml:{}]", xml);
            log.warn("[付款][证书:{}]", config.KEY_STORE_FILE);
        }

        File keyStoreFile = new File(config.KEY_STORE_FILE);
        if(!keyStoreFile.exists()){
            log.warn("[密钥文件不存在][file:{}]",config.KEY_STORE_FILE);
            return new WechatEnterpriseTransferBankResult(false,"密钥文件不存在");
        }
        String keyStorePassword = config.KEY_STORE_PASSWORD;
        if(BasicUtil.isEmpty(keyStorePassword)){
            log.warn("未设置密钥文件密码");
            return new WechatEnterpriseTransferBankResult(false,"未设置密钥文件密码");
        }
        try{
            CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
            reqEntity.setContentType("application/x-www-form-urlencoded");
            String txt = HttpBuilder.init()
                    .setClient(httpclient)
                    .setUrl(WechatPayConfig.API_URL_COMPANY_TRANSFER_BANK)
                    .setCharset("UTF-8")
                    .setEntity(reqEntity)
                    .build().get().getText();
            // String txt = HttpUtil.post(httpclient, WechatPayConfig.API_URL_COMPANY_TRANSFER_BANK, "UTF-8", reqEntity).getText();
            if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
                log.warn("[付款调用][result:{}]", txt);
            }
            result = BeanUtil.xml2object(txt, WechatEnterpriseTransferBankResult.class);
        }catch(Exception e){
            e.printStackTrace();
            return new WechatEnterpriseTransferBankResult(false,e.toString());
        }
        return result;
    }

    /**
     * 获取RSA公钥
     * @return String
     */
    public String getPublicKey() {
        String txt = WechatUtil.getPublicKey(config.MCH_ID, config.API_SECRET, new File(config.KEY_STORE_FILE), config.KEY_STORE_PASSWORD);
        Map<String,?> map = BeanUtil.xml2map(txt);
        return (String)map.get("pub_key");
    }
}
