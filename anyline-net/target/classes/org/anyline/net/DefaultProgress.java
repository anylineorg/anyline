package org.anyline.net; 
 
import java.io.File; 
 
import org.anyline.util.DateUtil; 
import org.anyline.util.FileUtil; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
public class DefaultProgress implements DownloadProgress{ 
	private Logger log = LoggerFactory.getLogger(DefaultProgress.class); 
	private String url			; //URL 
	private File local			; //本地文件 
	private long past			; //上次已下载长度 
	private long length			; //本次需下载长度 
	private long finish 		; //本次已下载长度 
	private long start			; //开始时间 
	private long expend			; //已耗时 
	private long expect			; //预计剩余时间 
	 
	private double lastLogRate	; //最后一次日志进度 
	private long lastLogTime	; //量后一次日志时间 
	private String message		; //进度提示 
	private double rate			; //完成百分比rate% 
	private int action = 1		; //1开始 0中断 
	public DefaultProgress(){ 
		this.start = System.currentTimeMillis(); 
	} 
	public DefaultProgress(String url, File local){ 
		this.url = url; 
		this.local = local; 
		this.start = System.currentTimeMillis(); 
		this.start = System.currentTimeMillis(); 
	} 
	public void init(String url, String thread, long total, long past){ 
		this.length = total; 
		this.past = past; 
	} 
	@Override 
	public void step(String url, String thread, long finish) { 
		this.finish += finish; 
		rate = (this.finish+this.past)*100.00/(this.length+this.past); 
 
    	if(this.finish > this.length){ 
    		this.finish = this.length; 
    		rate = 100; 
    	} 
    	if(rate ==100 && finish < length){ 
    		rate = 99.99; 
    	} 
		expend = System.currentTimeMillis() - start;//已耗时 
		if(expend>0){ 
			expect = (long)(this.length / (this.finish*1.0/expend) - expend);	//剩余时间=预计总耗时-已耗时 
			if(rate == 100){ 
				expect = 0; 
			} 
			log(); 
		} 
	} 
	/** 
	 * 设置已完成 
	 */ 
	@Override 
	public void finish(String url, String thread) { 
		this.rate= 100.00; 
	} 
	 
	private void log(){ 
		if(rate - lastLogRate  >= 0.5 || System.currentTimeMillis() - lastLogTime > 1000 * 5 || rate==100){ 
			long time = System.currentTimeMillis() - start; 
			message = "[进度:"+FileUtil.progress(length, finish)+"]" 
    				+ "[耗时:"+DateUtil.conversion(time)+"/"+DateUtil.conversion(expect)+"("+FileUtil.length(finish*1000/time)+"/s)]"; 
    		log.warn("[文件下载]{}[url:{}][local:{}]",message, url, local.getAbsolutePath()); 
    		lastLogRate = rate; 
    		lastLogTime = System.currentTimeMillis(); 
		} 
	} 
	@Override 
	public void error(String url, String thread, int code, String message) { 
		 
	} 
	@Override 
	public void setErrorListener(DownloadListener listener) { 
		 
	} 
	@Override 
	public void setFinishListener(DownloadListener listener) { 
		 
	} 
	public int getAction() { 
		return action; 
	} 
	public void stop(){ 
		this.action = 0; 
	} 
	 
} 
