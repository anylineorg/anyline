/* 
 * Copyright 2006-2015 www.anyline.org
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
 *
 *          
 */


package org.anyline.web.util; 
 
import java.util.Set; 
import java.util.TreeSet; 

import javax.servlet.http.HttpServletRequest; 

import org.anyline.util.BasicUtil;
import org.anyline.util.Constant;
import org.anyline.util.MD5Util;
 
public class TokenUtil { 
	/** 
	 * 创建token并放入session 
	 * @param request  request
	 * @return return
	 */ 
	public static String createToken(HttpServletRequest request){ 
		String token = null; 
		String key = null; 
		String value = null; 
		String action = (String)request.getAttribute("struts.request_uri"); 
		if(null != action){ 
			key = createTokenKey(action); 
		}else{ 
			String uri = request.getRequestURI(); 
			if(null != uri){ 
				key = createTokenKey(uri); 
			} 
		} 
		if(null != key){ 
			value = BasicUtil.getRandomNumberString(20); 
			token = "<input type=\"hidden\" name=\"" + key + "\" value=\"" + value + "\"></input>"; 
			request.getSession().setAttribute(key, value); 
			//setTokenRequiredRefer(request,key); 
		} 
		return token; 
	} 
 
	/** 
	 * 验证token 
	 * @param request request
	 * @return return
	 */ 
	public static boolean checkToken(HttpServletRequest request){ 
		boolean result = false; 
		String tokenKey = null;				//tokey key 
		String requestValue = null;			//提交的token值 
		String sessionValue = null;			//session中的token值 
		boolean isRequired = false; 
		if(null != request.getAttribute("IS_CHECK_TOKEN")){ 
			return true; 
		} 
		if(WebUtil.isAjaxRequest(request)){ 
			return true; 
		} 
		String refer = WebUtil.fetchReferUri(request); 
		tokenKey = createTokenKey(refer); 
		if(null != tokenKey){ 
			requestValue = request.getParameter(tokenKey); 
			sessionValue = (String)request.getSession().getAttribute(tokenKey); 
			request.getSession().removeAttribute(tokenKey); 
		} 
		if(null != sessionValue && null != requestValue){ 
			isRequired = true; 
			setTokenRequiredUri(request, createTokenKey(request.getRequestURI())); 
		} 
		if(!isRequired){ 
			isRequired = checkRequired(request); 
		} 
		if(isRequired){ 
			//必须 
			if(null != sessionValue && sessionValue.equals(requestValue)){ 
				result = true; 
			} 
		}else{ 
			//非必须 
			if(null == sessionValue || null == requestValue || sessionValue.equals(requestValue)){ 
				result = true; 
			} 
		} 
		request.setAttribute("IS_CHECK_TOKEN", true); 
		return result; 
	} 
 
	public static String createTokenKey(String key){ 
		String result = Constant.HTML_NAME_TOKEN_KEY_PREFIX + MD5Util.crypto2(key); 
		return result; 
	} 
	/** 
	 * token是否必须 
	 * @param request  request
	 * @return return
	 */ 
	public static boolean checkRequired(HttpServletRequest request){ 
		boolean result = false; 
		String tokenUri = createTokenKey(request.getRequestURI()); 
		Set setUri = (TreeSet)request.getSession().getServletContext().getAttribute(Constant.SERVLET_ATTR_REQUIRED_TOKEN_URI); 
		if(null != setUri && null != tokenUri){ 
			result = setUri.contains(tokenUri); 
		} 
		if(result){ 
			return result; 
		} 
 
		String tokenRefer = createTokenKey(WebUtil.fetchReferUri(request)); 
		Set setRefer = (TreeSet)request.getSession().getServletContext().getAttribute(Constant.SERVLET_ATTR_REQUIRED_TOKEN_REFER); 
		if(null != setRefer && null != tokenRefer){ 
			result = setRefer.contains(tokenRefer); 
		} 
		if(result){ 
			return result; 
		} 
		return result; 
	} 
	/** 
	 * 设置token必须状态 
	 * @param request  request
	 * @param tokenKey  tokenKey
	 */ 
	public static void setTokenRequiredRefer(HttpServletRequest request, String tokenKey){ 
		//添加到servlet防止页面伪造 
		Set<String> requiredTokenReferList = (Set<String>)request.getSession().getServletContext().getAttribute(Constant.SERVLET_ATTR_REQUIRED_TOKEN_REFER); 
		if(null == requiredTokenReferList){ 
			requiredTokenReferList = new TreeSet<String>(); 
		} 
		requiredTokenReferList.add(tokenKey); 
	} 
	public static void setTokenRequiredUri(HttpServletRequest request, String tokenKey){ 
		//添加到servlet防止页面伪造 
		Set<String> requiredTokenReferList = (Set<String>)request.getSession().getServletContext().getAttribute(Constant.SERVLET_ATTR_REQUIRED_TOKEN_URI); 
		if(null == requiredTokenReferList){ 
			requiredTokenReferList = new TreeSet<String>(); 
		} 
		requiredTokenReferList.add(tokenKey); 
	} 
} 
 
