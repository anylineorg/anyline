package org.anyline.qq.util;

import org.anyline.util.BasicConfig;

public class QQConfig extends BasicConfig{
	public static enum TRADE_TYPE{
		JSAPI			{public String getCode(){return "JSAPI";} 		public String getName(){return "公从号";}},
		APP				{public String getCode(){return "APP";} 		public String getName(){return "APP";}},
		NATIVE			{public String getCode(){return "NATIVE";} 		public String getName(){return "原生扫码";}},
		MICROPAY		{public String getCode(){return "MICROPAY";} 	public String getName(){return "刷卡";}};
		public abstract String getName();
		public abstract String getCode();
	};
	/**
	 * 支付接口地址
	 */
	//支付统一接口(POST)
	public final static String UNIFIED_ORDER_URL 	= "https://qpay.qq.com/cgi-bin/pay/qpay_unified_order.cgi";
	//订单查询接口(POST)
	public final static String QUERY_ORDER_URL 		= "https://qpay.qq.com/cgi-bin/pay/qpay_order_query.cgi";
	//关闭订单接口(POST)
	public final static String CLOSE_ORDER_URL 		= "https://qpay.qq.com/cgi-bin/pay/qpay_close_order.cgi";
	//退款接口(POST)
	public final static String REFUND_URL 			= "https://api.qpay.qq.com/cgi-bin/pay/qpay_refund.cgi";
	//退款查询接口(POST)
	public final static String QUERY_REFUND_URL 	= "https://qpay.qq.com/cgi-bin/pay/qpay_refund_query.cgi";
	//对账单接口(POST)
	public final static String DOWNLOAD_BILL_URL 	= "https://qpay.qq.com/cgi-bin/sp_download/qpay_mch_statement_down.cgi";
	public final static String URL_OAUTH 			= "https://graph.qq.com/oauth2.0/authorize";

}
