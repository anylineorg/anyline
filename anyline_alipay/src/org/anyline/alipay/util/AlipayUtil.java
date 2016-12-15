package org.anyline.alipay.util;


	import java.io.UnsupportedEncodingException;
	import java.net.URLEncoder;
	import java.util.ArrayList;
	import java.util.Collections;
	import java.util.Date;
	import java.util.List;
	import java.util.Map;

	import javax.servlet.http.HttpServletRequest;

	import com.alipay.api.AlipayConstants;

	/**
	 * 创建时间：2016年11月2日 下午7:12:44
	 * 
	 * @author andy
	 * @version 2.2
	 */

	public class AlipayUtil {

	    /**
	     * 生成订单号
	     * 
	     * @return
	     */
	    public static String getTradeNo() {
	        // 自增8位数 00000001
	        return "TNO" + DatetimeUtil.formatDate(new Date(), DatetimeUtil.TIME_STAMP_PATTERN) + "00000001";
	    }

	    /**
	     * 退款单号
	     * 
	     * @return
	     */
	    public static String getRefundNo() {
	        // 自增8位数 00000001
	        return "RNO" + DatetimeUtil.formatDate(new Date(), DatetimeUtil.TIME_STAMP_PATTERN) + "00000001";
	    }

	    /**
	     * 退款单号
	     * 
	     * @return
	     */
	    public static String getTransferNo() {
	        // 自增8位数 00000001
	        return "TNO" + DatetimeUtil.formatDate(new Date(), DatetimeUtil.TIME_STAMP_PATTERN) + "00000001";
	    }

	    /**
	     * 返回客户端ip
	     * 
	     * @param request
	     * @return
	     */
	    public static String getRemoteAddrIp(HttpServletRequest request) {
	        String ip = request.getHeader("X-Forwarded-For");
	        if (StringUtil.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
	            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
	            int index = ip.indexOf(",");
	            if (index != -1) {
	                return ip.substring(0, index);
	            } else {
	                return ip;
	            }
	        }
	        ip = request.getHeader("X-Real-IP");
	        if (StringUtil.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
	            return ip;
	        }
	        return request.getRemoteAddr();
	    }

	    /**
	     * 获取服务器的ip地址
	     * 
	     * @param request
	     * @return
	     */
	    public static String getLocalIp(HttpServletRequest request) {
	        return request.getLocalAddr();
	    }

	    /**
	     * 创建支付随机字符串
	     * 
	     * @return
	     */
	    public static String getNonceStr() {
	        return RandomUtil.randomString(RandomUtil.LETTER_NUMBER_CHAR, 32);
	    }

	    /**
	     * 支付时间戳
	     * 
	     * @return
	     */
	    public static String payTimestamp() {
	        return Long.toString(System.currentTimeMillis() / 1000);
	    }

	    /**
	     * 返回签名编码拼接url
	     * 
	     * @param params
	     * @param isEncode
	     * @return
	     */
	    public static String getSignEncodeUrl(Map<String, String> map, boolean isEncode) {
	        String sign = map.get("sign");
	        String encodedSign = "";
	        if (CollectionUtil.isNotEmpty(map)) {
	            map.remove("sign");
	            List<String> keys = new ArrayList<String>(map.keySet());
	            // key排序
	            Collections.sort(keys);

	            StringBuilder authInfo = new StringBuilder();

	            boolean first = true;// 是否第一个
	            for (String key: keys) {
	                if (first) {
	                    first = false;
	                } else {
	                    authInfo.append("&");
	                }
	                authInfo.append(key).append("=");
	                if (isEncode) {
	                    try {
	                        authInfo.append(URLEncoder.encode(map.get(key), AlipayConstants.CHARSET_UTF8));
	                    } catch (UnsupportedEncodingException e) {
	                        e.printStackTrace();
	                    }
	                } else {
	                    authInfo.append(map.get(key));
	                }
	            }

	            try {
	                encodedSign = authInfo.toString() + "&sign=" + URLEncoder.encode(sign, AlipayConstants.CHARSET_UTF8);
	            } catch (UnsupportedEncodingException e) {
	                e.printStackTrace();
	            }
	        }

	        return encodedSign.replaceAll("\\+", "%20");
	    }

	    /**
	     * 对支付参数信息进行签名
	     * 
	     * @param map
	     *            待签名授权信息
	     * 
	     * @return
	     */
	    public static String getSign(Map<String, String> map, String rsaKey) {
	        List<String> keys = new ArrayList<String>(map.keySet());
	        // key排序
	        Collections.sort(keys);

	        StringBuilder authInfo = new StringBuilder();
	        boolean first = true;
	        for (String key : keys) {
	            if (first) {
	                first = false;
	            } else {
	                authInfo.append("&");
	            }
	            authInfo.append(key).append("=").append(map.get(key)); 
	        }

	        return SignUtils.sign(authInfo.toString(), rsaKey);
	    }

	}