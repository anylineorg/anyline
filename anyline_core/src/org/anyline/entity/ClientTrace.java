
package org.anyline.entity;

import java.util.StringTokenizer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;

import org.anyline.util.WebUtil;

@Entity
@Table(name="dbo.CLIENT_TRACE")
public class ClientTrace extends BasicEntity{
	private static final long serialVersionUID = 1L;
	@Column(name="ADMIN_CD")
	private String adminCd;					//操作管理员
	@Column(name="SERVER_NAME")
	private String serverName;				//服务器
	@Column(name="SERVER_IP")
	private String serverIP;				//服务器IP
	@Column(name="SERVER_PORT")
	private int serverPort;					//服务器端口
	@Column(name="TRANSFER_SCHEME")
	private String transferScheme;			//传输协议
	@Column(name="SCHEME_VERSION")
	private String schemeVersion;			//传输协议版本
	@Column(name="ENCODING")
	private String encoding;				//编码
	@Column(name="CONTENT_TYPE")
	private String contentType;				//MIME类型
	@Column(name="CONTENT_LENGTH")
	private long contentLength;				//正文长度
	@Column(name="CONTEXT_PATH")
	private String contextPath;				//根目录
	@Column(name="LOCAL_IP")
	private String localIP;					//本地IP
	@Column(name="LOCAL_NAME")
	private String localName;				//本地主机名
	@Column(name="LOCAL_PORT")
	private int	localPort;					//本地端口
	@Column(name="REMOTE_IP")
	private String remoteIP;				//远程IP
	@Column(name="REMOTE_IP_NUM")
	private long remoteIPNum;				//远程IP number
	@Column(name="REMOTE_HOST")
	private String remoteHost;				//远程主机名
	@Column(name="REMOTE_PORT")
	private int remotePort;					//远程端口
	@Column(name="REMOTE_USER")
	private String remoteUser;				//远程用户名
	@Column(name="METHOD")
	private String method;					//请求方法
	@Column(name="REFFER")
	private String reffer;					//上一步链接
	@Column(name="URL")
	private String url;						//URL
	@Column(name="URI")	
	private String uri;						//URI
	@Column(name="SESSION_ID")
	private String sessionId;				//session id
	@Column(name="REQUEST_PARAM")
	private String requestParam;			//请求参数

	@Column(name="ACCEPT_LANGUAGE")
	private String acceptLanguage;			//访问语言
	@Column(name="EXPLORER_TYPE")
	private String explorerType;			//浏览器类型
	@Column(name="EXPLORER_VERSION")
	private String explorerVersion;			//浏览器版本
	@Column(name="OS")
	private String os;						//操作系统
	@Column(name="AGENT")
	private String agent;
	@Column(name="REQUEST_HASHCODE")
	private String requestHashCode;
	@Column(name="CLIENT_REPLY_STATUS")
	private String clientReplyStatus = "0";	//是否客户端回复确认(JS形式)
	

	public ClientTrace(){}
	
	public ClientTrace(HttpServletRequest request){
		if(null == request) return;
		adminCd = (String)request.getSession().getAttribute("ADMIN_CD");
		serverName = request.getServerName();
		serverIP = request.getLocalAddr();
		serverPort = request.getServerPort();
		localIP = request.getLocalAddr();
		localName = request.getLocalName();
		localPort = request.getLocalPort();
		transferScheme = request.getScheme();
		schemeVersion = request.getProtocol();
		encoding = request.getCharacterEncoding();
		contentType = request.getContentType();
		contentLength = request.getContentLength();
		contextPath = request.getContextPath();
		method = request.getMethod();
		reffer = request.getHeader("Referer");
		url = request.getRequestURL().toString();
		uri = request.getRequestURI();
		remoteIP = WebUtil.getRemoteIp(request);
		remoteIPNum = WebUtil.parseIp(remoteIP);
		remoteHost = request.getRemoteHost();
		remotePort = request.getRemotePort();
		remoteUser = request.getRemoteUser();
		sessionId = request.getRequestedSessionId();
		requestParam = request.getQueryString();
		acceptLanguage = request.getHeader("accept-language");
		requestHashCode = request.hashCode()+"";
		agent = request.getHeader("user-agent");
		try{
			String explorer = agent.substring(0,agent.indexOf("(")).trim();
			explorerType = explorer.substring(0,explorer.indexOf("/"));
			explorerVersion = explorer.substring(explorer.indexOf("/")+1);
			
			StringTokenizer token = new StringTokenizer(agent.substring(agent.indexOf("(")+1,agent.indexOf(")")),";");
			token.nextToken();
			token.nextToken();
			os = token.nextToken();
		}catch(Exception e){}
		
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getTransferScheme() {
		return transferScheme;
	}

	public void setTransferScheme(String transferScheme) {
		this.transferScheme = transferScheme;
	}

	public String getSchemeVersion() {
		return schemeVersion;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public void setSchemeVersion(String schemeVersion) {
		this.schemeVersion = schemeVersion;
	}

	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public long getContentLength() {
		return contentLength;
	}
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}
	public String getContextPath() {
		return contextPath;
	}
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	
	public String getLocalName() {
		return localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	public int getLocalPort() {
		return localPort;
	}
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public String getRemoteHost() {
		return remoteHost;
	}
	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}
	public int getRemotePort() {
		return remotePort;
	}
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}
	public String getRemoteUser() {
		return remoteUser;
	}
	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getRequestParam() {
		return requestParam;
	}

	public void setRequestParam(String requestParam) {
		this.requestParam = requestParam;
	}

	public String getAcceptLanguage() {
		return acceptLanguage;
	}
	public void setAcceptLanguage(String acceptLanguage) {
		this.acceptLanguage = acceptLanguage;
	}
	public String getExplorerType() {
		return explorerType;
	}
	public void setExplorerType(String explorerType) {
		this.explorerType = explorerType;
	}
	public String getExplorerVersion() {
		return explorerVersion;
	}
	public void setExplorerVersion(String explorerVersion) {
		this.explorerVersion = explorerVersion;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public String getReffer() {
		return reffer;
	}

	public void setReffer(String reffer) {
		this.reffer = reffer;
	}

	public String getLocalIP() {
		return localIP;
	}

	public void setLocalIP(String localIP) {
		this.localIP = localIP;
	}

	public String getRemoteIP() {
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	public long getRemoteIPNum() {
		return remoteIPNum;
	}

	public void setRemoteIPNum(long remoteIPNum) {
		this.remoteIPNum = remoteIPNum;
	}

	public String getAdminCd() {
		return adminCd;
	}

	public void setAdminCd(String adminCd) {
		this.adminCd = adminCd;
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public String getClientReplyStatus() {
		return clientReplyStatus;
	}

	public void setClientReplyStatus(String clientReplyStatus) {
		this.clientReplyStatus = clientReplyStatus;
	}

	public String getRequestHashCode() {
		return requestHashCode;
	}

	public void setRequestHashCode(String requestHashCode) {
		this.requestHashCode = requestHashCode;
	}
}
