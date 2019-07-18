package org.anyline.net;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
	
	public DownloadTask(){
		
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
	}
	public void error(int code, String message){
		log.error("[文件下载][下载异常]][code:"+code+"][message:"+message+"]");
	}
	public void finish(){
		this.rate = 100.00;
		this.end = System.currentTimeMillis();
		log();
		if(ConfigTable.isDebug()){
			log.info("[文件下载][下载完成][耗时:"+getExpendFormat()+"][url:"+url+"][local:"+getLocal().getAbsolutePath()+"]");
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

	public void start(DownloadProgress progress){
		if(!isRunning()){
			if(start ==0){
				start = System.currentTimeMillis();
			}
			HttpUtil.download(progress, url, local, false);
		}
	}
	/**
	 * 停止下载任务
	 */
	public void stop(){
		
	}
	public boolean isRunning(){
		if(end !=0 && getExpend()>0){
			return true;
		}
		return false;
	}
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
	/**
	 * 每秒下载byte
	 * @return
	 */
	public long getSpeed(){
		long expend = getExpend();
		if(expend ==0){
			return 0;
		}
		return finish*1000/expend;
	}
	/**
	 * 下载速度/s
	 * @return
	 */
	public String getSpeedFormat(){
		long speed = getSpeed();
		return FileUtil.conversion(speed)+"/s";
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
		return FileUtil.conversion(getLength());
	}
	public void setLength(long length) {
		this.length = length;
	}
	public long getTotal(){
		return length + past;
	}
	public String getTotalFormat(){
		return FileUtil.conversion(getTotal());
	}

	public long getFinish() {
		return finish;
	}
	public String getFinishFormat(){
		return FileUtil.conversion(getFinish());
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
	public boolean isFinish(){
		return rate == 100;
	}
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
		return FileUtil.conversion(getPast());
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
}
