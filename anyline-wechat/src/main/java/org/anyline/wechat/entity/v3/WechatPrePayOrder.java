package org.anyline.wechat.entity.v3;
 
import org.anyline.wechat.util.WechatConfig.TRADE_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WechatPrePayOrder {
	private String sp_appid						;//服务商公众号ID		string（32）	是	 服务商申请的公众号或移动应用appid。示例值：wx8888888888888888
	private String sp_mchid						;//服务商户号			string（32）	是	 服务商户号，由微信支付生成并下发示例值：1230000109
	private String sub_appid					;//子商户公众号ID		string（32）	否	 子商户申请的公众号或移动应用appid。示例值：wxd678efh567hg6999
	private String sub_mchid					;//子商户号				string（32）	是	 子商户的商户号，有微信支付生成并下发。示例值：1900000109
	private String description					;//商品描述				string（127）	是	 商品描述示例值：Image形象店-深圳腾大-QQ公仔
	private String out_trade_no					;//商户订单号			string（32）	是	 商户系统内部订单号，只能是数字、大小写字母_-*且在同一个商户号下唯一，详见【商户订单号】。特殊规则：最小字符长度为6示例值：1217752501201407033233368018
	private String time_expire					;//交易结束时间			string（64）	否	 订单失效时间，遵循rfc3339标准格式，格式为YYYY-MM-DDTHH:mm:ss+TIMEZONE，YYYY-MM-DD表示年月日，T出现在字符串中，表示time元素的开头，HH:mm:ss表示时分秒，TIMEZONE表示时区（+08:00表示东八区时间，领先UTC 8小时，即北京时间）。例如：2015-05-20T13:29:35+08:00表示，北京时间2015年5月20日 13点29分35秒。示例值：2018-06-08T10:34:56+08:00
	private String attach						;//附加数据				string（128）	否	 附加数据，在查询API和支付通知中原样返回，可作为自定义参数使用示例值：自定义数据
	private String notify_url					;//通知地址				string（256）	是	 通知URL必须为直接可访问的URL，不允许携带查询串。格式：URL示例值：https://www.weixin.qq.com/wxpay/pay.php
	private String goods_tag					;//订单优惠标记			string（32）	否	 订单优惠标记示例值：WXG
	private Map<String,Object> settle_info		;//结算信息profit_sharing,subsidy_amount
	private Map<String,Object> amount			;//订单金额 total currency
	private Map<String,Object> payer			;//支付者 sp_openid sub_openid
	private Map<String,Object> detail			;
	private Map<String,Object> scene_info		;//支付场景

	public String getSp_appid() {
		return sp_appid;
	}

	public void setSp_appid(String sp_appid) {
		this.sp_appid = sp_appid;
	}

	public String getSp_mchid() {
		return sp_mchid;
	}

	public void setSp_mchid(String sp_mchid) {
		this.sp_mchid = sp_mchid;
	}

	public String getSub_appid() {
		return sub_appid;
	}

	public void setSub_appid(String sub_appid) {
		this.sub_appid = sub_appid;
	}

	public String getSub_mchid() {
		return sub_mchid;
	}

	public void setSub_mchid(String sub_mchid) {
		this.sub_mchid = sub_mchid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOut_trade_no() {
		return out_trade_no;
	}

	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}

	public String getTime_expire() {
		return time_expire;
	}

	public void setTime_expire(String time_expire) {
		this.time_expire = time_expire;
	}

	public String getAttach() {
		return attach;
	}

	public void setAttach(String attach) {
		this.attach = attach;
	}

	public String getNotify_url() {
		return notify_url;
	}

	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}

	public String getGoods_tag() {
		return goods_tag;
	}

	public void setGoods_tag(String goods_tag) {
		this.goods_tag = goods_tag;
	}

	public Map<String, Object> getSettle_info() {
		return settle_info;
	}

	public void setSettle_info(Map<String, Object> settle_info) {
		this.settle_info = settle_info;
	}
	public void setSettle_info(boolean profit_sharing, int subsidy_amount) {
		this.settle_info = new HashMap<String,Object>();
		settle_info.put("profit_sharing", profit_sharing);
		settle_info.put("subsidy_amount", subsidy_amount);
	}

	public Map<String, Object> getAmount() {
		return amount;
	}

	public void setAmount(Map<String, Object> amount) {
		this.amount = amount;
	}
	public void setAmount(int total, String currency) {
		this.amount = new HashMap<String,Object>();
		amount.put("total",total);
		amount.put("currency",currency);
	}
	public void setAmount(int total) {
		setAmount(total,"CNY");
	}

	public Map<String, Object> getPayer() {
		return payer;
	}

	public void setPayer(Map<String, Object> payer) {
		this.payer = payer;
	}
	public void setPayer(String sp_openid, String sub_openid) {
		this.payer = new HashMap<String,Object>();
		payer.put("sp_openid",sp_openid);
		payer.put("sub_openid",sub_openid);
	}

	public Map<String, Object> getDetail() {
		return detail;
	}

	public void setDetail(Map<String, Object> detail) {
		this.detail = detail;
	}
	public void setCost_price(int cost_price){
		if(detail == null){
			detail = new HashMap<String, Object>();
		}
		detail.put("cost_price",cost_price);
	}
	public void setInvoice_id(String invoice_id){
		if(detail == null){
			detail = new HashMap<String, Object>();
		}
		detail.put("invoice_id",invoice_id);
	}
	public void setGoods_detail(List<Map<String,Object>> goods_detail){
		if(detail == null){
			detail = new HashMap<String, Object>();
		}
		detail.put("goods_detail",goods_detail);
	}
	public void addGoods_detail(Map<String,Object> goods){
		if(detail == null){
			detail = new HashMap<String, Object>();
		}
		List<Map<String,Object>> goods_detail = (List<Map<String,Object>>)detail.get("goods_detail");
		if(null == goods_detail){
			goods_detail = new ArrayList<Map<String,Object>>();
		}
		goods_detail.add(goods);
	}

	/**
	 *
	 * @param merchant_goods_id 商户侧商品编码
	 * @param wechatpay_goods_id 微信侧商品编码
	 * @param goods_name 商品名称
	 * @param quantity 商品数量
	 * @param unit_price 商品单价
	 */
	public void addGoods_detail(String merchant_goods_id, String wechatpay_goods_id, String goods_name, int quantity, int unit_price){
		Map<String,Object> goods = new HashMap<String, Object>();
		goods.put("merchant_goods_id",merchant_goods_id);
		goods.put("wechatpay_goods_id",wechatpay_goods_id);
		goods.put("goods_name",goods_name);
		goods.put("quantity",quantity);
		goods.put("unit_price",unit_price);
		addGoods_detail(goods);
	}
	public Map<String, Object> getScene_info() {
		return scene_info;
	}

	public void setScene_info(Map<String, Object> scene_info) {
		this.scene_info = scene_info;
	}

	/**
	 * 用户终端IP
	 * @param payer_client_ip 用户终端IP
	 */
	public void setPayer_client_ip(String payer_client_ip){
		if(null == scene_info){
			scene_info = new HashMap<String,Object>();
		}
		scene_info.put("payer_client_ip",payer_client_ip);
	}

	/**
	 * 商户端设备号
	 * @param device_id
	 */
	public void setDevice_id(String device_id){
		if(null == scene_info){
			scene_info = new HashMap<String,Object>();
		}
		scene_info.put("device_id",device_id);
	}

	/**
	 * 商户门店信息
	 * @param store_info 商户门店信息
	 */
	public void setStore_info(Map<String,Object> store_info){
		if(null == scene_info){
			scene_info = new HashMap<String,Object>();
		}
		scene_info.put("store_info",store_info);
	}

	/**
	 * 商户门店信息
	 * @param id 门店编号
	 * @param name 门店名称
	 * @param area_code 地区编码
	 * @param address 详细地址
	 */
	public void setStore_info(String id, String name, String area_code, String address){
		Map<String,Object> store_info = new HashMap<String,Object>();
		store_info.put("id",id);
		store_info.put("name",name);
		store_info.put("area_code",area_code);
		store_info.put("address",address);
		setStore_info(store_info);
	}
}
