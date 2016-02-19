

package org.anyline.struts.action;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.anyline.config.http.ConfigStore;
import org.anyline.controller.AbstractBasicController;
import org.anyline.entity.ClientTrace;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.Constant;
import org.anyline.util.WebUtil;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.TextProviderFactory;
import com.opensymphony.xwork2.Validateable;
import com.opensymphony.xwork2.ValidationAware;
import com.opensymphony.xwork2.ValidationAwareSupport;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ValueStack;

public class AnylineAction extends AbstractBasicController implements ServletRequestAware, ServletResponseAware,Action, Validateable, ValidationAware, TextProvider, LocaleProvider, Serializable {
	protected static Logger LOG = Logger.getLogger(AnylineAction.class);
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected HttpSession session;
	protected ServletContext servlet;
	@Resource
	protected AnylineService service;


	protected Object data; 				// 返回数据
	protected boolean result = true; 	// 执行结果
	protected String msg; 				// 返回信息
	protected String url; 				// 动态跳转
	
	
	public <T> T entity(Class<T> clazz, boolean keyEncrypt, boolean valueEncrypt, String... params){
		return entity(request, clazz, keyEncrypt, valueEncrypt, params);
	}
	
	
	public <T> T entity(Class<T> clazz, boolean keyEncrypt, String... params) {
		return entity(request,clazz, keyEncrypt, false, params);
	}

	public <T> T entity(Class<T> clazz, String... params) {
		return entity(request, clazz, false, false, params);
	}
	public DataRow entityRow(DataRow row, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entityRow(request, row, keyEncrypt, valueEncrypt, params);
	}

	public DataRow entityRow(DataRow row, boolean keyEncrypt, String... params) {
		return entityRow(request, row, keyEncrypt, false, params);
	}
	public DataRow entityRow(DataRow row, String... params) {
		return entityRow(request, row, false, false, params);
	}

	public DataRow entityRow(boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entityRow(request,null, keyEncrypt, valueEncrypt, params);
	}


	public DataRow entityRow(boolean keyEncrypt, String... params) {
		return entityRow(request,null, keyEncrypt, false, params);
	}
	

	public DataRow entityRow(String... params) {
		return entityRow(request, null, false, false, params);
	}
	
	public DataSet entitySet(boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entitySet(request, keyEncrypt, valueEncrypt, params);
	}

	public DataSet entitySet(boolean keyEncrypt, String... params) {
		return entitySet(request,keyEncrypt, false, params);
	}

	public DataSet entitySet(String... params) {
		return entitySet(request, false, false, params);
	}
	

	protected ConfigStore parseConfig(boolean navi, String... configs) {
		return parseConfig(request, navi, configs);
	}
	

	protected ConfigStore parseConfig(int vol, String... configs) {
		return parseConfig(request, vol, configs);
	}

	protected ConfigStore parseConfig(int fr, int to, String... configs) {
		return parseConfig(request, fr, to, configs);
	}
	protected ConfigStore parseConfig(String... conditions) {
		return parseConfig(request, false, conditions);
	}

	protected String getParam(String key, boolean keyEncrypt, boolean valueEncrypt) {
		return getParam(request, key, keyEncrypt, valueEncrypt);
	}

	protected String getParam(String key, boolean keyEncrypt) {
		return getParam(request,key, keyEncrypt, false);
	}
	

	protected String getParam(String key) {
		return getParam(request, key, false, false);
	}


	protected List<Object> getParams(String key, boolean keyEncrypt) {
		return getParams(request, key, keyEncrypt, false);
	}


	protected List<Object> getParams(String key) {
		return getParams(request,key, false, false);
	}
	protected boolean checkRequired(boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return checkRequired(request, keyEncrypt, valueEncrypt, params);
	}

	protected boolean checkRequired(String... params) {
		return checkRequired(request, false, false, params);
	}
	protected boolean isAjaxRequest() {
		return isAjaxRequest(request);
	}
	
	
	

	protected ClientTrace currentClient() {
		return currentClient(request);
	}
	


	protected String currentClientCd() {
		return currentClientCd(request);
	}
	
	

	protected void setRequestMessage(String key, Object value, String type) {
		setRequestMessage(request, key, value, type);
	}
	
	protected void setRequestMessage(String key, Object value) {
		setRequestMessage(request,key, value, null);
	}

	protected void setRequestMessage(Object value) {
		setRequestMessage(request,BasicUtil.getRandomLowerString(10), value, null);
	}

	protected void setMessage(String key, Object value, String type) {
		setRequestMessage(request, key, value, type);
	}

	protected void setMessage(String key, Object value) {
		setMessage(request, key, value, null);
	}

	protected void setMessage(Object value) {
		setMessage(request, BasicUtil.getRandomLowerString(10), value);
	}

	protected void setSessionMessage(String key, Object value, String type) {
		setSessionMessage(request.getSession(), key, value, type);
	}

	protected void setSessionMessage(String key, Object value) {
		setSessionMessage(request.getSession(), key, value, null);
	}

	protected void setSessionMessage(Object value) {
		setSessionMessage(request.getSession(), BasicUtil.getRandomLowerString(10), value, null);
	}
	protected boolean hasReffer() {
		return hasReffer(request);
	}

	protected boolean isSpider() {
		return !hasReffer(request);
	}
	

	protected boolean isWap() {
		return WebUtil.isWap(request);
	}
	
	
		private final ValidationAwareSupport validationAware = new ValidationAwareSupport();

	    private transient TextProvider textProvider;
	    private Container container;

	    public void setActionErrors(Collection<String> errorMessages) {
	        validationAware.setActionErrors(errorMessages);
	    }

	    public Collection<String> getActionErrors() {
	        return validationAware.getActionErrors();
	    }

	    public void setActionMessages(Collection<String> messages) {
	        validationAware.setActionMessages(messages);
	    }

	    public Collection<String> getActionMessages() {
	        return validationAware.getActionMessages();
	    }

	    /**
	     * @deprecated Use {@link #getActionErrors()}.
	     */
	    @Deprecated
	    public Collection<String> getErrorMessages() {
	        return getActionErrors();
	    }

	    /**
	     * @deprecated Use {@link #getFieldErrors()}.
	     */
	    @Deprecated
	    public Map<String, List<String>> getErrors() {
	        return getFieldErrors();
	    }

	    public void setFieldErrors(Map<String, List<String>> errorMap) {
	        validationAware.setFieldErrors(errorMap);
	    }

	    public Map<String, List<String>> getFieldErrors() {
	        return validationAware.getFieldErrors();
	    }

	    public Locale getLocale() {
	        ActionContext ctx = ActionContext.getContext();
	        if (ctx != null) {
	            return ctx.getLocale();
	        } else {
	            if (LOG.isDebugEnabled()) {
	            	LOG.debug("Action context not initialized");
	            }
	            return null;
	        }
	    }

	    public boolean hasKey(String key) {
	        return getTextProvider().hasKey(key);
	    }

	    public String getText(String aTextName) {
	        return getTextProvider().getText(aTextName);
	    }

	    public String getText(String aTextName, String defaultValue) {
	        return getTextProvider().getText(aTextName, defaultValue);
	    }

	    public String getText(String aTextName, String defaultValue, String obj) {
	        return getTextProvider().getText(aTextName, defaultValue, obj);
	    }

	    public String getText(String aTextName, List<?> args) {
	        return getTextProvider().getText(aTextName, args);
	    }

	    public String getText(String key, String[] args) {
	        return getTextProvider().getText(key, args);
	    }

	    public String getText(String aTextName, String defaultValue, List<?> args) {
	        return getTextProvider().getText(aTextName, defaultValue, args);
	    }

	    public String getText(String key, String defaultValue, String[] args) {
	        return getTextProvider().getText(key, defaultValue, args);
	    }

	    public String getText(String key, String defaultValue, List<?> args, ValueStack stack) {
	        return getTextProvider().getText(key, defaultValue, args, stack);
	    }

	    public String getText(String key, String defaultValue, String[] args, ValueStack stack) {
	        return getTextProvider().getText(key, defaultValue, args, stack);
	    }

	    /**
	     * Dedicated method to support I10N and conversion errors
	     *
	     * @param key message which contains formatting string
	     * @param expr that should be formatted
	     * @return formatted expr with format specified by key
	     */
	    public String getFormatted(String key, String expr) {
	        Map<String, Object> conversionErrors = ActionContext.getContext().getConversionErrors();
	        if (conversionErrors.containsKey(expr)) {
	            String[] vals = (String[]) conversionErrors.get(expr);
	            return vals[0];
	        } else {
	            final ValueStack valueStack = ActionContext.getContext().getValueStack();
	            final Object val = valueStack.findValue(expr);
	            return getText(key, Arrays.asList(val));
	        }
	    }

	    public ResourceBundle getTexts() {
	        return getTextProvider().getTexts();
	    }

	    public ResourceBundle getTexts(String aBundleName) {
	        return getTextProvider().getTexts(aBundleName);
	    }

	    public void addActionError(String anErrorMessage) {
	        validationAware.addActionError(anErrorMessage);
	    }

	    public void addActionMessage(String aMessage) {
	        validationAware.addActionMessage(aMessage);
	    }

	    public void addFieldError(String fieldName, String errorMessage) {
	        validationAware.addFieldError(fieldName, errorMessage);
	    }

	    public String input() throws Exception {
	        return INPUT;
	    }

	    public String doDefault() throws Exception {
	        return super.SUCCESS;
	    }

	    /**
	     * A default implementation that does nothing an returns "success".
	     * <p/>
	     * Subclasses should override this method to provide their business logic.
	     * <p/>
	     * See also {@link com.opensymphony.xwork2.Action#execute()}.
	     *
	     * @return returns {@link #SUCCESS}
	     * @throws Exception can be thrown by subclasses.
	     */
	    public String execute() throws Exception {
	        return super.SUCCESS;
	    }

	    public boolean hasActionErrors() {
	        return validationAware.hasActionErrors();
	    }

	    public boolean hasActionMessages() {
	        return validationAware.hasActionMessages();
	    }

	    public boolean hasErrors() {
	        return validationAware.hasErrors();
	    }

	    public boolean hasFieldErrors() {
	        return validationAware.hasFieldErrors();
	    }

	    /**
	     * Clears field errors. Useful for Continuations and other situations
	     * where you might want to clear parts of the state on the same action.
	     */
	    public void clearFieldErrors() {
	        validationAware.clearFieldErrors();
	    }

	    /**
	     * Clears action errors. Useful for Continuations and other situations
	     * where you might want to clear parts of the state on the same action.
	     */
	    public void clearActionErrors() {
	        validationAware.clearActionErrors();
	    }

	    /**
	     * Clears messages. Useful for Continuations and other situations
	     * where you might want to clear parts of the state on the same action.
	     */
	    public void clearMessages() {
	        validationAware.clearMessages();
	    }

	    /**
	     * Clears all errors. Useful for Continuations and other situations
	     * where you might want to clear parts of the state on the same action.
	     */
	    public void clearErrors() {
	        validationAware.clearErrors();
	    }

	    /**
	     * Clears all errors and messages. Useful for Continuations and other situations
	     * where you might want to clear parts of the state on the same action.
	     */
	    public void clearErrorsAndMessages() {
	        validationAware.clearErrorsAndMessages();
	    }

	    /**
	     * A default implementation that validates nothing.
	     * Subclasses should override this method to provide validations.
	     */
	    public void validate() {
	    }

	    @Override
	    public Object clone() throws CloneNotSupportedException {
	        return super.clone();
	    }

	    /**
	     * <!-- START SNIPPET: pause-method -->
	     * Stops the action invocation immediately (by throwing a PauseException) and causes the action invocation to return
	     * the specified result, such as {@link #SUCCESS}, {@link #INPUT}, etc.
	     * <p/>
	     * <p/>
	     * The next time this action is invoked (and using the same continuation ID), the method will resume immediately
	     * after where this method was called, with the entire call stack in the execute method restored.
	     * <p/>
	     * <p/>
	     * Note: this method can <b>only</b> be called within the {@link #execute()} method.
	     * <!-- END SNIPPET: pause-method -->
	     *
	     * @param result the result to return - the same type of return value in the {@link #execute()} method.
	     */
	    public void pause(String result) {
	    }

	    /**
	     * If called first time it will create {@link com.opensymphony.xwork2.TextProviderFactory},
	     * inject dependency (if {@link com.opensymphony.xwork2.inject.Container} is accesible) into in,
	     * then will create new {@link com.opensymphony.xwork2.TextProvider} and store it in a field
	     * for future references and at the returns reference to that field
	     *
	     * @return reference to field with TextProvider
	     */
	    private TextProvider getTextProvider() {
	        if (textProvider == null) {
	            TextProviderFactory tpf = new TextProviderFactory();
	            if (container != null) {
	                container.inject(tpf);
	            }
	            textProvider = tpf.createInstance(getClass(), this);
	        }
	        return textProvider;
	    }

	    @Inject
	    public void setContainer(Container container) {
	        this.container = container;
	    }
	
	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
		this.session = request.getSession();
		this.servlet = this.session.getServletContext();
	}
	/******************************************************************************************************************
	 * 
	 * 返回执行结果路径
	 * 
	 *******************************************************************************************************************/
	/**
	 * 返回执行路径
	 * 
	 * @param result
	 *            执行结果
	 * @param success
	 *            执行成功时返回数据
	 * @param fail
	 *            执行失败时返回数据
	 */
	protected String result(HttpServletRequest request, boolean result, Object success, Object fail) {
		if (result) {
			return success(request, success);
		} else {
			return fail(request, fail);
		}
	}

	protected String result(boolean result, Object success, Object fail) {
		return result(request, result, success, fail);
	}
	/**
	 * 执行成功
	 * 
	 * @param result
	 * @return
	 */
	protected String success(HttpServletRequest request, Object data) {
		this.data = data;
		if (isAjaxRequest(request)) {
			result = true;
			return AJAX;
		}
		return super.SUCCESS;
	}
	protected String success(Object data){
		return success(request, data);
	}
	protected String success(HttpServletRequest request) {
		return success(request,data);
	}

	protected String success() {
		return success(request,data);
	}

	/**
	 * 执行失败
	 * 
	 * @return
	 */
	protected String fail(Object... msgs) {
		result = false;
		if (null != msgs && msgs.length > 0) {
			for (Object msg : msgs) {
				setMessage(request,msg);
			}
		}
		String html = "";
		DataSet messages = (DataSet) request
				.getAttribute(Constant.REQUEST_ATTR_MESSAGE);
		if (null != messages) {
			for (int i = 0; i < messages.size(); i++) {
				DataRow msg = messages.getRow(i);
				html += "\n" + msg.getString(Constant.MESSAGE_VALUE);
				messages.remove(msg);
			}
		}
		msg = BasicUtil.nvl(msg, "").toString() + BasicUtil.nvl(html, "").toString();
		request.getSession().setAttribute(Constant.SESSION_ATTR_ERROR_MESSAGE, msg);
		if (isAjaxRequest(request)) {
			return AJAX;
		} else {
			return FAIL;
		}
	}
	
	
	
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String message) {
		this.msg = message;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}