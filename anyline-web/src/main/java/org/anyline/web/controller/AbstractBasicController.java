/* 
 * Copyright 2006-2020 www.anyline.org
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


package org.anyline.web.controller; 
 
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.entity.PageNaviConfig;
import org.anyline.jdbc.config.ConfigParser;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.ParseResult;
import org.anyline.jdbc.config.db.impl.PageNaviImpl;
import org.anyline.jdbc.config.impl.ConfigStoreImpl;
import org.anyline.util.*;
import org.anyline.web.util.EntityListener;
import org.anyline.web.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AbstractBasicController{ 
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	protected String dir;				// <result>文件默认目录
	protected final String FAIL = "fail";
	protected final String JSON = "json";
	protected final String SUCCESS = "success";

	@Autowired(required = false)
	@Qualifier("anyline.entity.listener")
	protected static EntityListener listener;
	protected static boolean is_listener_load = false;
	private EntityListener getListener(HttpServletRequest request){
		if(null != listener){
			return listener;
		}
		if(!is_listener_load) {
			BeanFactory beanFactory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
			listener = (EntityListener) beanFactory.getBean("anyline.entity.listener");
			is_listener_load = true;
		}
		return listener;
	}
	/****************************************************************************************************************** 
	 *  
	 * 封装参数 
	 *  
	 *******************************************************************************************************************/ 
 
	/** 
	 * 从封装request参数到实体类对象 
	 *  
	 * @param request  request
	 * @param <T>  T
	 * @param clazz  clazz
	 * @param keyEncrypt  keyEncrypt
	 * @param valueEncrypt  valueEncrypt
	 * @param params  params
	 * @return return
	 */ 
	public <T> T entity(HttpServletRequest request, Class<T> clazz, boolean keyEncrypt, boolean valueEncrypt, String... params) { 
		T entity = null; 
		if (null == clazz) { 
			return entity; 
		} 
		try { 
			entity = (T) clazz.newInstance(); 
			/* 属性赋值 */ 
			if (null != params && params.length > 0) { 
				/* 根据指定的属性与request参数对应关系 */ 
 
				for (String param : params) { 
					/* 解析属性与request参数对应关系 */

					ParseResult parser = ConfigParser.parse(param,true);
					
					Object value = ConfigParser.getValues(WebUtil.value(request), parser);//getParam(request,parser.getKey(), parser.isKeyEncrypt(), parser.isValueEncrypt());
					BeanUtil.setFieldValue(entity, parser.getVar(), value);
				}// end for 
			} else {// end指定属性与request参数对应关系 
				/* 未指定属性与request参数对应关系 */ 
				/* 读取类属性 */ 
				List<Field> fields = BeanUtil.getFields(clazz); 
				for (Field field : fields) { 
					/* 取request参数值 */ 
					String fieldName = field.getName(); 
					Object value = getParam(request,fieldName, keyEncrypt, valueEncrypt); 
					/* 属性赋值 */ 
					BeanUtil.setFieldValue(entity, field, value); 
				} 
			}// end 未指定属性与request参数对应关系
			listener = getListener(request);
			if(null != listener){
				listener.init(request, entity);
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
		return entity; 
	} 
	 
	public <T> T entity(HttpServletRequest request, Class<T> clazz, boolean keyEncrypt, String... params) { 
		return entity(request,clazz, keyEncrypt, false, params); 
	}

	public <T> T entity(HttpServletRequest request, Class<T> clazz, String... params) {
		return entity(request, clazz, false, false, params);
	} 

	public DataRow entity(HttpServletRequest request, DataRow.KEY_CASE keyCase, DataRow row, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		if (null == row) {
			row = new DataRow(keyCase);
		}
		if (null != params && params.length > 0) {
			for (String param : params) {
				ParseResult parser = ConfigParser.parse(param,true);
				Object value = ConfigParser.getValue(WebUtil.value(request), parser);
				row.put(parser.getVar(), value);
				if(parser.isRequired()){
					row.addUpdateColumns(parser.getVar());
				}
			}
		}else{
			Enumeration<String> names = request.getParameterNames();
			while(names.hasMoreElements()){
				String name = names.nextElement();
				String value = request.getParameter(name);
				row.put(name, value);
			}
		}
		listener = getListener(request);
		if(null != listener){
			listener.init(request, row);
		}
		return row;
	}


	public DataRow entity(HttpServletRequest request, DataRow.KEY_CASE keyCase, DataRow row, boolean keyEncrypt, String... params) {
		return entity(request, keyCase, row, keyEncrypt,  params);
	}

	public DataRow entity(HttpServletRequest request, DataRow row, boolean keyEncrypt, String... params) {
		return entity(request, DataRow.KEY_CASE.CONFIG, row, keyEncrypt,  params);
	}
	public DataRow entity(HttpServletRequest request, DataRow.KEY_CASE keyCase, DataRow row, String... params) {
		return entity(request,keyCase, row, params);
	}
	public DataRow entity(HttpServletRequest request, DataRow row, String... params) {
		return entity(request, DataRow.KEY_CASE.CONFIG, row, params);
	}
	public DataRow entity(HttpServletRequest request, DataRow.KEY_CASE keyCase, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entity(request, keyCase, keyEncrypt, valueEncrypt, params);
	}
	public DataRow entity(HttpServletRequest request,boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entity(request, DataRow.KEY_CASE.CONFIG, keyEncrypt, valueEncrypt, params);
	}
	public DataRow entity(HttpServletRequest request, DataRow.KEY_CASE keyCase, boolean keyEncrypt, String... params) {
		return entity(request,keyCase, keyEncrypt, params);
	}
	public DataRow entity(HttpServletRequest request, boolean keyEncrypt, String... params) {
		return entity(request, DataRow.KEY_CASE.CONFIG, keyEncrypt, params);
	}

	public DataRow entity(HttpServletRequest request, DataRow.KEY_CASE keyCase, String... params) {
		return entity(request,keyCase,params);
	}
	public DataRow entity(HttpServletRequest request, String... params) {
		return entity(request, DataRow.KEY_CASE.CONFIG,params);
	}


	public DataSet entitys(HttpServletRequest request, DataRow.KEY_CASE keyCase, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		DataSet set = new DataSet();
		if (null != params && params.length > 0) {
			//raw [json]格式
			DataSet list = WebUtil.values(request);
			if(list.size()>0){
				for(DataRow item:list) {
					DataRow row = new DataRow();
					for (String param : params) {
						Object value = item.get(param);
						row.put(keyCase, param, value);
					}
					set.add(row);
				}
				return set;
			}
			//k=v格式
			Map<String,List<Object>> map = new HashMap<String,List<Object>>();
			int size = 0;
			for (String param : params) {
				ParseResult parser = ConfigParser.parse(param,true);
				List<Object> values = ConfigParser.getValues(WebUtil.value(request), parser);
				map.put(parser.getVar(), values);
				if(size <= values.size()){
					size = values.size();
				}
			}

			for(int i=0; i<size; i++){
				DataRow row = new DataRow(keyCase);
				for (String param : params) {
					ParseResult parser = ConfigParser.parse(param,true);
					List<Object> values = map.get(parser.getVar());
					if(values.size() > i){
						row.put(parser.getVar(), values.get(i));
					}else{
						List<ParseResult> defs = parser.getDefs();
						if(null != defs && defs.size()>0){
							ParseResult def = defs.get(0);
							String key = def.getKey();
							if(null != key && key.startsWith("{") && key.endsWith("}")){
								// col:value
								key = key.substring(1, key.length()-1);
							}
							row.put(parser.getVar(), key);
						}
					}
				}

				set.addRow(row);
			}
		}
		listener = getListener(request);
		if(null != listener){
			listener.init(request, set);
		}
		return set;
	}
	public DataSet entitys(HttpServletRequest request, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entitys(request, DataRow.KEY_CASE.CONFIG, keyEncrypt, valueEncrypt, params);
	}

	public DataSet entitys(HttpServletRequest request, DataRow.KEY_CASE keyCase, boolean keyEncrypt, String... params) {
		return entitys(request,keyCase,keyEncrypt, false, params);
	}

	public DataSet entitys(HttpServletRequest request, boolean keyEncrypt, String... params) {
		return entitys(request, DataRow.KEY_CASE.CONFIG,keyEncrypt, false, params);
	}
	public DataSet entitys(HttpServletRequest request, DataRow.KEY_CASE keyCase, String... params) {
		return entitys(request, keyCase, false, false, params);
	}
	public DataSet entitys(HttpServletRequest request,String... params) {
		return entitys(request, DataRow.KEY_CASE.CONFIG, false, false, params);
	}



	/** 
	 * 解析参数 
	 *  
	 * @param request  request
	 * @param navi  是否分页 
	 * @param configs   参数 
	 * @return return
	 */ 
	protected ConfigStore parseConfig(HttpServletRequest request, boolean navi, String... configs) { 
		ConfigStore store = new ConfigStoreImpl(configs); 
		if (navi) { 
			PageNavi pageNavi = parsePageNavi(request); 
			store.setPageNavi(pageNavi); 
		} 
		store.setValue(WebUtil.value(request));
		return store; 
	} 
	/** 
	 * 解析参数 
	 *  
	 * @param request  request
	 * @param vol   每页多少条记录 vol不大于0时不分页 
	 * @param configs    参数 
	 * @return return
	 */ 
	protected ConfigStore parseConfig(HttpServletRequest request, int vol, String... configs) { 
		ConfigStore store = new ConfigStoreImpl(configs);
		if(vol >0){ 
			PageNavi pageNavi = parsePageNavi(request); 
			pageNavi.setPageRows(vol);
			store.setPageNavi(pageNavi);
		} 
		store.setValue(WebUtil.value(request));
		return store; 
	} 
 
	/** 
	 * 解析参数 
	 *  
	 * @param request  request
	 * @param fr  开始行(下标从1开始) 
	 * @param to   结束行 
	 * @param configs     参数 
	 * @return return
	 */ 
	protected ConfigStore parseConfig(HttpServletRequest request, int fr, int to, String... configs) { 
		ConfigStore store = new ConfigStoreImpl(configs); 
		PageNavi navi = new PageNaviImpl(); 
		navi.setCalType(1); 
		navi.setFirstRow(fr); 
		navi.setLastRow(to); 
		store.setPageNavi(navi); 
		store.setValue(WebUtil.value(request));
		return store; 
	}
	protected ConfigStore parseConfig(HttpServletRequest request, String... conditions) {
		return parseConfig(request, false, conditions);
	}
	
	protected ConfigStore config(HttpServletRequest request, boolean navi, String... configs) {
		return parseConfig(request, navi, configs);
	}
	protected ConfigStore config(HttpServletRequest request, int vol, String... configs) {
		return parseConfig(request, vol, configs);
	}
	protected ConfigStore config(HttpServletRequest request, int fr, int to, String... configs) {
		return parseConfig(request, fr, to, configs);
	}
	protected ConfigStore config(HttpServletRequest request, String... conditions) {
		return parseConfig(request, conditions);
	}

	protected ConfigStore condition(HttpServletRequest request, boolean navi, String... configs) {
		return parseConfig(request, navi, configs);
	}
	protected ConfigStore condition(HttpServletRequest request, int vol, String... configs) {
		return parseConfig(request, vol, configs);
	}
	protected ConfigStore condition(HttpServletRequest request, int fr, int to, String... configs) {
		return parseConfig(request, fr, to, configs);
	}
	protected ConfigStore condition(HttpServletRequest request, String... conditions) {
		return parseConfig(request, conditions);
	} 
	/** 
	 * rquest.getParameter 
	 *  
	 * @param request  request
	 * @param key  key
	 * @param keyEncrypt  keyEncrypt
	 * @param valueEncrypt  valueEncrypt
	 * @param defs  defs
	 * @return return
	 */ 
	protected String getParam(HttpServletRequest request, String key, boolean keyEncrypt, boolean valueEncrypt, String ... defs) {
		String result =  (String) WebUtil.getHttpRequestParam(request, key,keyEncrypt, valueEncrypt);
		if(BasicUtil.isEmpty(result) && null != defs && defs.length>0){
			return (String)BasicUtil.nvl(defs);
		}
		return result;
	} 
 
	protected String getParam(HttpServletRequest request, String key, boolean valueEncrypt, String ... defs) {
		return getParam(request,key, false,valueEncrypt);
	}

	protected String getParam(HttpServletRequest request, String key, String ... defs) {
		return getParam(request, key, false, false, defs);
	} 
	protected List<Object> getParams(HttpServletRequest request, String key, boolean keyEncrypt, boolean valueEncrypt) {
		return WebUtil.getHttpRequestParams(request, key, keyEncrypt, valueEncrypt);
	} 
	 
	protected List<Object> getParams(HttpServletRequest request, String key, boolean valueEncrypt) {
		return getParams(request, key, false, valueEncrypt);
	} 
	protected List<Object> getParams(HttpServletRequest request, String key) {
		return getParams(request,key, false, false);
	}


	protected int getInt(HttpServletRequest request, String key, boolean keyEncrypt, boolean valueEncrypt) throws Exception{
		String val = getParam(request,key, keyEncrypt, valueEncrypt);
		return BasicUtil.parseInt(val);
	}

	protected int getInt(HttpServletRequest request, String key, boolean valueEncrypt) throws Exception{
		String val = getParam(request,key, valueEncrypt);
		return BasicUtil.parseInt(val);
	}

	protected int getInt(HttpServletRequest request, String key) throws Exception{
		String val = getParam(request,key);
		return BasicUtil.parseInt(val);
	}

	protected int getInt(HttpServletRequest request, String key, boolean keyEncrypt, boolean valueEncrypt, int def){
		String val = getParam(request,key, keyEncrypt, valueEncrypt);
		try {
			return BasicUtil.parseInt(val);
		}catch (Exception e){
			return def;
		}
	}

	protected int getInt(HttpServletRequest request, String key, boolean valueEncrypt, int def) {
		String val = getParam(request,key, valueEncrypt);
		try {
			return BasicUtil.parseInt(val);
		}catch (Exception e){
			return def;
		}
	}

	protected int getInt(HttpServletRequest request, String key, int def) {
		String val = getParam(request,key);
		try {
			return BasicUtil.parseInt(val);
		}catch (Exception e){
			return def;
		}
	}




	protected double getDouble(HttpServletRequest request, String key, boolean keyEncrypt, boolean valueEncrypt) throws Exception{
		String val = getParam(request,key, keyEncrypt, valueEncrypt);
		return Double.parseDouble(val);
	}

	protected double getDouble(HttpServletRequest request, String key, boolean valueEncrypt) throws Exception{
		String val = getParam(request,key, valueEncrypt);
		return Double.parseDouble(val);
	}

	protected double getDouble(HttpServletRequest request, String key) throws Exception{
		String val = getParam(request,key);
		return Double.parseDouble(val);
	}

	protected double getDouble(HttpServletRequest request, String key, boolean keyEncrypt, boolean valueEncrypt, double def){
		String val = getParam(request,key, keyEncrypt, valueEncrypt);
		try {
			return Double.parseDouble(val);
		}catch (Exception e){
			return def;
		}
	}

	protected double getDouble(HttpServletRequest request, String key, boolean valueEncrypt, double def) {
		String val = getParam(request,key, valueEncrypt);
		try {
			return Double.parseDouble(val);
		}catch (Exception e){
			return def;
		}
	}

	protected double getDouble(HttpServletRequest request, String key, double def) {
		String val = getParam(request,key);
		try {
			return Double.parseDouble(val);
		}catch (Exception e){
			return def;
		}
	}
	/** 
	 * 按PageNavi预订格式<br> 
	 * 从HttpServletRequest中提取分布数据 
	 *  
	 * @param request  request
	 * @return return
	 */ 
	protected  PageNavi parsePageNavi(HttpServletRequest request) { 
		if (null == request){ 
			return null;
		}
		String style = getParam(request,"style");
		PageNaviConfig config = PageNaviConfig.getInstance(style); 
		int pageNo = 1; // 当前页数 默认1
		int pageVol = config.VAR_PAGE_DEFAULT_VOL; // 每页多少条 默认10 
		// 提取request中请求参数 
		pageVol = BasicUtil.parseInt(request.getAttribute(config.KEY_PAGE_ROWS),pageVol);
		//是否启用前端设置显示行数
		if(config.VAR_CLIENT_SET_VOL_ENABLE){
			int httpVol = BasicUtil.parseInt(getParam(request,config.KEY_PAGE_ROWS),0);
			if(httpVol > 0){
				if(httpVol > config.VAR_PAGE_MAX_VOL){
					log.warn("[每页条数超出限制][参考anyline-navi.xml:VAR_PAGE_MAX_VOL]");
				}
				pageVol = NumberUtil.min(config.VAR_PAGE_MAX_VOL, httpVol);
			}
		}

		pageNo = BasicUtil.parseInt(getParam(request,config.KEY_PAGE_NO),pageNo);
		String uri = null; 
		if (null == uri) { 
			uri = request.getRequestURI(); 
		} 
		PageNavi navi = new PageNaviImpl(pageNo, pageVol, uri); 
		String flag = (String)request.getAttribute(config.KEY_ID_FLAG);
		if(null == flag){
			flag = getParam(request,config.KEY_ID_FLAG);
		}
		if(null != flag){
			flag = flag.replace("'", "").replace("\"", "");
		}
		navi.setFlag(flag);
		boolean showStat = config.VAR_SHOW_STAT;
		showStat = BasicUtil.parseBoolean(getParam(request,config.KEY_SHOW_STAT), showStat);
		navi.setShowStat(showStat);
		boolean showJump = config.VAR_SHOW_JUMP;
		showJump = BasicUtil.parseBoolean(getParam(request,config.KEY_SHOW_JUMP), showJump);
		navi.setShowJump(showJump);

		boolean showVol = config.VAR_SHOW_VOL;
		showJump = BasicUtil.parseBoolean(getParam(request,config.KEY_SHOW_VOL), showVol);
		navi.setShowVol(showVol);
		
		navi.setStyle(style);
		String guide = BasicUtil.nvl(getParam(request,config.KEY_GUIDE), config.STYLE_LOAD_MORE_FORMAT,"").toString();
		navi.setGuide(guide);
		request.setAttribute("navi", navi);
		 
		return navi; 
	} 
 
	/* ***************************************************************************************************************** 
	 *  
	 * 数据验证 
	 *  
	 * ******************************************************************************************************************/ 
	/** 
	 * 验证必须参数 
	 *  
	 * @param request request
	 * @param keyEncrypt keyEncrypt
	 * @param valueEncrypt valueEncrypt
	 * @param params params
	 * @return return
	 */ 
	protected boolean checkRequired(HttpServletRequest request, boolean keyEncrypt, boolean valueEncrypt, String... params) { 
		params = BasicUtil.compressionSpace(params); 
		boolean result = true; 
		for (String param : params) { 
			param = param.trim();
			if(BasicUtil.isEmpty(param)){
				continue;
			}

			ParseResult parser = ConfigParser.parse(param, true);
			if (BasicUtil.isEmpty(getParam(request, parser.getKey(), parser.isKeyEncrypt(), parser.isValueEncrypt()))) { 
				setMessage(request,"请提供必须参数:" + parser.getVar());
				result = false; 
			} 
		} 
		return result; 
	}
	protected boolean checkRequired(HttpServletRequest request, String... params) {
		return checkRequired(request, false, false, params);
	} 
 
	/****************************************************************************************************************** 
	 *  
	 * 数据库操作 
	 *  
	 *******************************************************************************************************************/ 
 
	/** 
	 * 是否是ajax请求 
	 *  
	 * @param request request
	 * @return return
	 */ 
	protected boolean isAjaxRequest(HttpServletRequest request) { 
		String header = request.getHeader("x-requested-with"); 
		if (header != null && "XMLHttpRequest".equals(header)) 
			return true; 
		else 
			return false; 
	} 
	/** 
	 * request参数保存到分页中 
	 *  
	 * @param single  single
	 *            是否取单个值(否则取数组) 
	 * @param request  request
	 * @param keys  keys
	 * @return return
	 */ 
	@SuppressWarnings("unused")
	private PageNavi saveParamToNavi(HttpServletRequest request, boolean single, boolean keyEncrypt, boolean valueEncrypt, String... keys) { 
		PageNavi navi = (PageNavi) request.getAttribute("navi"); 
		if (null == navi) { 
			navi = new PageNaviImpl(); 
			request.setAttribute("navi", navi); 
		} 
		if (null != keys) { 
			for (String key : keys) { 
				if (single) { 
					navi.addParam(key, WebUtil.getHttpRequestParam(request, key, keyEncrypt, valueEncrypt)); 
				} else { 
					navi.addParam(key, WebUtil.getHttpRequestParams(request, key, keyEncrypt, valueEncrypt)); 
				} 
			} 
		} 
		return navi; 
	} 
//	/** 
//	 * 当前操作客户端 
//	 *  
//	 * @return return
//	 */ 
//	protected ClientTrace currentClient(HttpServletRequest request) { 
//		ClientTrace client = null; 
//		client = (ClientTrace) request.getAttribute(Constant.REQUEST_ATTR_HTTP_CLIENT); 
//		if (null == client) { 
//			client = new ClientTrace(request); 
//			//service.save(ConfigTable.getString("CLIENT_TRACE_TABLE"), client); 
//		} 
//		return client; 
//	} 
//	protected String currentClientCd(HttpServletRequest request) { 
//		String result = null; 
//		ClientTrace client = currentClient(request); 
//		if (null != client) { 
//			result = client.getCd(); 
//		} 
//		return result; 
//	} 
	/******************************************************************************************************************************** 
	 *  
	 * 提示信息 
	 *  
	 ********************************************************************************************************************************/ 
	/** 
	 * 设置提示信息 
	 *  
	 * @param request request
	 * @param key  key
	 * @param value value
	 * @param type   消息类别 
	 */ 
	protected void setRequestMessage(HttpServletRequest request, String key, Object value, String type) { 
		DataSet messages = null; 
		messages = (DataSet) request.getAttribute(Constant.REQUEST_ATTR_MESSAGE); 
		if (null == messages) { 
			messages = new DataSet(); 
			request.setAttribute(Constant.REQUEST_ATTR_MESSAGE, messages); 
		}
		DataRow row = new DataRow();
		row.put(Constant.MESSAGE_KEY, key);
		row.put(Constant.MESSAGE_VALUE, value);
		row.put(Constant.MESSAGE_TYPE, type); 
		messages.addRow(row); 
	}
	 
	protected void setRequestMessage(HttpServletRequest request, String key, Object value) {
		setRequestMessage(request,key, value, null);
	} 
 
	protected void setRequestMessage(HttpServletRequest request, Object value) {
		setRequestMessage(request,BasicUtil.getRandomLowerString(10), value, null);
	}
	protected void setMessage(HttpServletRequest request, String key, Object value, String type) {
		setRequestMessage(request, key, value, type);
	} 

	protected void setMessage(HttpServletRequest request, String key, Object value) {
		setMessage(request, key, value, null);
	}
	
	protected void setMessage(HttpServletRequest request, Object value) {
		setMessage(request, BasicUtil.getRandomLowerString(10), value);
	}


	protected void setSessionMessage(HttpSession session, String key, Object value, String type) {
		DataSet messages = null;
		messages = (DataSet) session.getAttribute(Constant.SESSION_ATTR_MESSAGE);
		if (null == messages) {
			messages = new DataSet();
			session.setAttribute(Constant.SESSION_ATTR_MESSAGE, messages);
		}
		DataRow row = new DataRow();
		row.put(Constant.MESSAGE_KEY, key);
		row.put(Constant.MESSAGE_VALUE, value);
		row.put(Constant.MESSAGE_TYPE, type);
		messages.addRow(row);
	}
 

	protected void setSessionMessage(HttpSession session, String key, Object value) {
		setSessionMessage(session, key, value, null);
	} 

	protected void setSessionMessage(HttpSession session, Object value) {
		setSessionMessage(session, BasicUtil.getRandomLowerString(10), value, null);
	} 
 
	/** 
	 * reffer 
	 * @param request request
	 * @return return
	 */
	protected boolean hasReffer(HttpServletRequest request) {
		String reffer = request.getHeader("Referer");
		if (null == reffer || "".equals(reffer.trim())) {
			return false;
		}
		return true;
	} 
 
	/** 
	 * 是否是蜘蛛 
	 * @param request request
	 * @return return
	 */
	protected boolean isSpider(HttpServletRequest request) {
		return !hasReffer(request);
	}

 
	/** 
	 * 是否是移动端 
	 * @param request request
	 * @return return
	 */
	protected boolean isWap(HttpServletRequest request) {
		return WebUtil.isWap(request);
	} 
 
	/* ***************************************************************************************************************** 
	 *  
	 * 实现接口 
	 *  
	 * ******************************************************************************************************************/ 
	 
 
	public String getDir() { 
		String dir = null; 
		try{
			Field[] fields = getClass().getFields();
			for(Field field:fields){
				if(field.getName().equals("dir")){
					dir = (String)field.get(this);
				} 
			} 
		}catch(Exception e){ 
		} 
		return dir; 
	}
	/**
	 * 
	 * @param request request
	 * @param response response
	 * @param data data
	 * @param navi	分页 navi	分页
	 * @param page	数据模板以WEB-INF为相对目录根目录 page	数据模板以WEB-INF为相对目录根目录
	 * @param ext	扩展数据 ext	扩展数据
	 * @return return
	 */
	public Map<String,Object> navi(HttpServletRequest request, HttpServletResponse response, Object data, PageNavi navi, String page, Object ext){
		if(null == data){
			data = (DataSet)request.getAttribute("_anyline_navi_data");
		}else{
			request.setAttribute("_anyline_navi_data", data);
		}
		String html = "";
		try{
			html = WebUtil.parseJsp(request, response, page);
		}catch(Exception e){
			e.printStackTrace();
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("BODY", BasicUtil.escape(html));
		if(null != navi){
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[load jsp navi][rows:{}][page:{}]",navi.getTotalRow(),navi.getTotalPage());
			}
			int type = 0;
			String _type = getParam(request,"_anyline_navi_type");
			if("1".equals(_type)){
				type = 1;
			}
			navi.setType(type);
			map.put("NAVI", BasicUtil.escape(navi.html("ajax")));
			map.put("TOTAL_ROW", navi.getTotalRow()+"");
			map.put("TOTAL_PAGE", navi.getTotalPage()+"");
			map.put("CUR_PAGE", navi.getCurPage()+"");
			map.put("FIRST_ROW", navi.getFirstRow()+"");
			map.put("LAST_ROW", navi.getLastRow()+"");
			map.put("PAGE_ROWS", navi.getPageRows()+"");
		}
		map.put("EXT", ext);
		return map;
	}

	public Map<String,Object> navi(HttpServletRequest request, HttpServletResponse response, Object data, PageNavi navi, String page){
		return navi(request, response, data, navi, page, null);
	}
}
