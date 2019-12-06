package org.anyline.config; 
 
import java.io.Serializable; 
import java.util.ArrayList; 
import java.util.List; 
 
import org.anyline.config.db.Condition; 
import org.anyline.config.db.SQL; 
import org.anyline.config.db.SQL.COMPARE_TYPE; 
 
public class ParseResult implements Serializable{ 
	private static final long serialVersionUID = 1L; 
	public static int FETCH_REQUEST_VALUE_TYPE_SINGLE = 1;	//单值 
	public static int FETCH_REQUEST_VALUE_TYPE_MULIT  = 2;	//数组 
	 
	private boolean required				; //是否必须(空值拼接IS NULL) 
	private boolean strictRequired			; //是否严格必须(空值不查询) 
	private String id						; //xml定义中的id 
	private String field					; //实体属性或表列名 
	private String clazz					; //取值后处理类 
	private String method					; //处理方法 
	 
	private String key						; //http key 
	private boolean isKeyEncrypt			; //key是否加密 
	private boolean isValueEncrypt			; //value是否加密 
	 
//	private String def						; //默认值 
//	private boolean isDefKeyEncrypt			; //key是否加密 
//	private boolean isDefValueEncrypt		; //value是否加密 
//	 
	private boolean setEncrypt = false		; //是否已指定加密方式 
 
	private List<ParseResult> defs = new ArrayList<ParseResult>();	//默认值 
	private COMPARE_TYPE compare = SQL.COMPARE_TYPE.EQUAL				; //比较方式			 
	private String join = Condition.CONDITION_JOIN_TYPE_AND		; //连接方式 
	private int paramFetchType = FETCH_REQUEST_VALUE_TYPE_SINGLE; //request取值方式 
	 
	public List<ParseResult> getDefs(){ 
		return defs; 
	} 
	public void addDef(ParseResult def){ 
		defs.add(def); 
	} 
	 
	public boolean isRequired() { 
		return required; 
	} 
	public void setRequired(boolean required) { 
		this.required = required; 
	} 
	public COMPARE_TYPE getCompare() { 
		return compare; 
	} 
	public void setCompare(COMPARE_TYPE compare) { 
		this.compare = compare; 
	} 
	public String getKey() { 
		return key; 
	} 
	public void setKey(String key) { 
		this.key = key; 
	} 
	public String getField() { 
		return field; 
	} 
	public void setField(String field) { 
		this.field = field; 
	} 
//	public String getDef() { 
//		return def; 
//	} 
//	public void setDef(String def) { 
//		this.def = def; 
//	} 
	public String getClazz() { 
		return clazz; 
	} 
	public void setClazz(String clazz) { 
		this.clazz = clazz; 
	} 
	public String getMethod() { 
		return method; 
	} 
	public void setMethod(String method) { 
		this.method = method; 
	} 
	public int getParamFetchType() { 
		return paramFetchType; 
	} 
	public void setParamFetchType(int paramFetchType) { 
		this.paramFetchType = paramFetchType; 
	} 
	public boolean isKeyEncrypt() { 
		return isKeyEncrypt; 
	} 
	public void setKeyEncrypt(boolean isKeyEncrypt) { 
		this.isKeyEncrypt = isKeyEncrypt; 
	} 
	public boolean isValueEncrypt() { 
		return isValueEncrypt; 
	} 
	public void setValueEncrypt(boolean isValueEncrypt) { 
		this.isValueEncrypt = isValueEncrypt; 
	} 
	public boolean isSetEncrypt() { 
		return setEncrypt; 
	} 
	public void setSetEncrypt(boolean setEncrypt) { 
		this.setEncrypt = setEncrypt; 
	} 
	public String getId() { 
		return id; 
	} 
	public void setId(String id) { 
		this.id = id; 
	} 
	public String getJoin() { 
		return join; 
	} 
	public void setJoin(String join) { 
		this.join = join; 
	} 
//	public boolean isDefKeyEncrypt() { 
//		return isDefKeyEncrypt; 
//	} 
//	public void setDefKeyEncrypt(boolean isDefKeyEncrypt) { 
//		this.isDefKeyEncrypt = isDefKeyEncrypt; 
//	} 
//	public boolean isDefValueEncrypt() { 
//		return isDefValueEncrypt; 
//	} 
//	public void setDefValueEncrypt(boolean isDefValueEncrypt) { 
//		this.isDefValueEncrypt = isDefValueEncrypt; 
//	} 
	public boolean isStrictRequired() { 
		return strictRequired; 
	} 
	public void setStrictRequired(boolean strictRequired) { 
		this.strictRequired = strictRequired; 
	} 
 
} 
