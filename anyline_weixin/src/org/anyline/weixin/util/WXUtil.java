package org.anyline.weixin.util;

import java.util.Map;

import org.anyline.util.BasicUtil;
import org.anyline.util.MD5Util;

public class WXUtil {

	/**
	 * 支付参数签名
	 * @param apisecrect 
	 * @param params
	 * @return
	 */
	public static String paySign(String apisecrect, Map<String, Object> params) {
		String sign = "";
		sign = BasicUtil.joinBySort(params);
		sign += "&key=" + apisecrect;
		sign = MD5Util.crypto(sign).toUpperCase();
		return sign;
	}
}
