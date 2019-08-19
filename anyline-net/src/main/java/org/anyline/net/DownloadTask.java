package org.anyline.net;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.anyline.util.ConfigTable;
import org.anyline.util.DateUtil;
import org.anyline.util.FileUtil;
import org.apache.log4j.Logger;

public class DownloadTask {
	private Logger log = Logger.getLogger(DownloadTask.class);
	private boolean openLog = true;
	private String url	; //url
	private File local	; //本地文件
	private int threads	; //线程数量
	private long past	; //上次已下载长度
	private long length	; //本次需下载长度
	private long finish ; //本次已下载长度
	private long start	; //开始时间
	private long end	; //结束时间
	private Map<String,String> headers;
	private Map<String,Object> params;
	private Map<String,Object> extras = new HashMap<String,Object>();	//扩展属性(回调时原样返回)
	private int index		; //任务下标从0开始
	private long expend		; //本次已耗时
	private long expect		; //本次预计剩余时间
	private double rate = 0 ; //完成比例

	private double lastLogRate	; //最后一次日志进度
	private long lastLogTime	; //量后一次日志时间
	private String errorMsg = ""; //异常信息
	private int errorCode = 0	; //异常编号
	private int action =1		; //1正常执行 0中断
	private int status = 0		; //0初始 1执行中 2暂停 -1异常 9已完成
	
	private Map<Long,Long> records = new HashMap<Long,Long>(); //下载记录
	private DownloadProgress progress = new DefaultProgress();
	private DownloadListener listener;
	private boolean override = false;
	
	/**
	 * 是否覆盖已存在文件
	 * @return
	 */
	public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	public DownloadTask(){
	}

	/**
	 * 每秒下载byte
	 * @return
	 */
	public long getSpeed(){
		Long sum = 0L;
		Long fr = 0L;
		Iterator<Entry<Long, Long>> entries = records.entrySet().iterator();  
		while (entries.hasNext()) {  
			Map.Entry<Long,Long> entry =  entries.next();   
			Long key = entry.getKey(); //记录时间
			Long value = entry.getValue();//记录值
			if(System.currentTimeMillis() - key> 1000*10){//10秒内的值
		    	entries.remove();
		    }else{
		    	sum += value;
		    	if(key < fr || fr ==0){
		    		fr = key;
		    	}
		    }
		}
		return sum*1000/(System.currentTimeMillis()-fr);
	}
	/**
	 * 下载速度/s
	 * @return
	 */
	public String getSpeedFormat(){
		long speed = getSpeed();
		return FileUtil.length(speed)+"/s";
	}
	public DownloadTask(String url, File local){
		this.url = url;
		this.local = local;
	}
	public DownloadTask(String url, File local, Map<String,String> headers, Map<String,Object> params, Map<String,Object> extras){
		this.url = url;
		this.local = local;
		this.headers = headers;
		this.params = params;
		this.extras = extras;
	}
	public DownloadTask(String url, File local, Map<String,String> headers, Map<String,Object> params){
		this.url = url;
		this.local = local;
		this.headers = headers;
		this.params = params;
	}

	public void init(long length, long past){
		this.length = length;
		this.past = past;
		this.start = System.currentTimeMillis();
		this.status = 0;
	}
	public void step(long len){
		this.finish +=  len;
		rate = new BigDecimal((this.finish+this.past)*100.0/(this.length+this.past)).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue(); //完成比例

    	if(this.finish >= length){
    		this.finish = length;
    		rate = 100.00;
    		end = System.currentTimeMillis();
    	}
    	if(rate ==100 && finish < length){
    		rate = 99.99;
    	}
    	if(end ==0){
    		expend = System.currentTimeMillis() - start;//已耗时
    	}else{
    		expend = end - start;//已耗时
    	}
		if(expend>0){
			expect = (long)(length / (this.finish*1.0/expend) - expend);	//剩余时间=预计总耗时-已耗时
			if(rate == 100){
				expect = 0;
			}
			if(ConfigTable.isDebug() && openLog){
				log();
			}
		}
		records.put(System.currentTimeMillis(), len);
		if(rate >=100){
			finish();
		}
	}
	public void error(int code, String message){
		status = -1;
		log.error("[文件下载][下载异常]][url:"+url+"][code:"+code+"][message:"+message+"]");
		this.errorCode = code;
		this.errorMsg = message;
		if(null != listener){
			listener.error(this);
		}
	}
	public void finish(){
		status = 9;
		this.rate = 100.00;
		this.end = System.currentTimeMillis();
		log();
		if(ConfigTable.isDebug()){
			log.info("[文件下载]"+"[下载完成][耗时:"+getExpendFormat()+"][url:"+url+"][local:"+getLocal().getAbsolutePath()+"]");
		}
		if(null != listener){
			listener.finish(this);
		}
	}
	private void log(){
		if(!ConfigTable.isDebug() && !openLog){
			return;
		}
		if(getExpend() ==0){
			return;
		}
		double rate = getFinishRate();
		//进度>0.5%或时间超过5秒或全部完成
		if(lastLogTime==0 || rate - lastLogRate  >= 0.5 || System.currentTimeMillis() - lastLogTime > 1000 * 5 || rate==100){
			log.warn("[文件下载]"+getMessage());
    		lastLogRate = rate;
    		lastLogTime = System.currentTimeMillis();
		}
	}
	
	public void start(){
		if(!isRunning() &&  action != 1){
			action = 1;
			status = 1;
			if(start ==0){
				start = System.currentTimeMillis();
			}
			HttpUtil.download(this);
		}else{
			action = 1;
			status = 1;
		}
	}
	/**
	 * 停止下载任务
	 */
	public void stop(){
		action =0;
		status = 2;
	}
	public int getAction(){
		return action;
	}
//	public boolean isRunning(){
//		if(end !=0 && getExpend()>0){
//			return true;
//		}
//		return false;
//	}
	public double getFinishRate(){
		return this.rate;
	}
	public String getMessage(){
		//"[进度:10.12mb/200.11mb(20%)][数量:1/5][耗时:1分3秒/12分2秒][网速:100kb/s]"
		String msg = "[进度:";
		if(this.past>0){
			msg += getPastFormat();
			if(this.finish>0){
				 msg += "+";
			}
		}
		if(this.finish>0){
			msg += getFinishFormat();
		}
		msg += "/"+getTotalFormat()+"("+getFinishRate()+"%)]"
				+ "[耗时:"+getExpendFormat()+"/"+getExpectFormat()+"][网速:"+getSpeedFormat()+"][url:"+url+"][local:"+local.getAbsolutePath()+"]";
		return msg;
	}
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public File getLocal() {
		return local;
	}

	public void setLocal(File local) {
		this.local = local;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public long getLength() {
		return length;
	}
	public String getLengthFormat(){
		return FileUtil.length(getLength());
	}
	public void setLength(long length) {
		this.length = length;
	}
	public long getTotal(){
		return length + past;
	}
	public String getTotalFormat(){
		return FileUtil.length(getTotal());
	}

	public long getFinish() {
		return finish;
	}
	public String getFinishFormat(){
		return FileUtil.length(getFinish());
	}
	

	public void setFinish(long finish) {
		this.finish = finish;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public Map<String, Object> getExtras() {
		return extras;
	}
	public void setExtras(Map<String, Object> extras) {
		this.extras = extras;
	}
	public void addExtras(String key, Object value){
		extras.put(key, value);
	}
	/**
	 * 耗时
	 * @return
	 */
	public long getExpend() {
		if(end ==0){
			expend = System.currentTimeMillis() - start;
		}else{
			expend = end - start;
		}
		return expend;
	}
	public String getExpendFormat(){
		return DateUtil.conversion(getExpend());
	}
	public void setExpend(long expend) {
		this.expend = expend;
	}
	/**
	 * 预计剩余时间
	 * 没有实际速度时，使用预计速度
	 * @return
	 */
	public long getExpect(long speed) {
		if(expend>0){
			expect = (long)(length / (this.finish*1.0/expend) - expend);	//剩余时间=预计总耗时-已耗时
			if(rate == 100){
				expect = 0;
			}
		}else if(speed>0){
			expect = (long)(length*1.0 / speed - expend);	//剩余时间=预计总耗时-已耗时
		}
		return expect;
	}
	public long getExpect() {
		return expect;
	}
	public String getExpectFormat(long speed){
		return DateUtil.conversion(getExpect(speed));
	}
	public String getExpectFormat(){
		return DateUtil.conversion(getExpect());
	}
	public void setExpect(long expect) {
		this.expect = expect;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
//	public boolean isFinish(){
//		return rate == 100;
//	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public long getPast() {
		return past;
	}
	public String getPastFormat(){
		return FileUtil.length(getPast());
	}
	public void setPast(long past) {
		this.past = past;
	}
	public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
	public void openLog(){
		this.openLog = true;
	}
	public void closeLog(){
		this.openLog = false;
	}

	public DownloadProgress getProgress() {
		return progress;
	}

	public void setProgress(DownloadProgress progress) {
		this.progress = progress;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public void setListener(DownloadListener listener) {
		this.listener = listener;
	}

	public int getStatus() {
		return status;
	}
	public boolean isInit(){
		return status == 0;
	}
	public boolean isStop(){
		return status == 2;
	}
	public boolean isRunning(){
		return status == 1;
	}
	public boolean isError(){
		return status == -1;
	}
	public boolean isFinish(){
		return status == 9;
	}
}
