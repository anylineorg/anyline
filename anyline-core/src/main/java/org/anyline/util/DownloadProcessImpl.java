package org.anyline.util;

import java.io.File;

import org.apache.log4j.Logger;

public class DownloadProcessImpl implements DownloadProcess{
	private Logger log = Logger.getLogger(DownloadProcessImpl.class);
	private String url		; //URL
	private File local		; //本地文件
	private long total		; //总大小
	private long finish		; //已完成
	private long start		; //开始时间
	private long expend		; //已耗时
	private long expect		; //预计剩余时间
	
	private double lastLogRate	; //最后一次日志进度
	private long lastLogTime	; //量后一次日志时间
	private String message		; //进度提示
	private double process		; //完成百分比process%
	public DownloadProcessImpl(){
		this.start = System.currentTimeMillis();
	}
	public DownloadProcessImpl(String url, File local){
		this.url = url;
		this.local = local;
		this.start = System.currentTimeMillis();
		this.start = System.currentTimeMillis();
	}
	@Override
	public void process(long finish) {
		this.finish += finish;
		process = this.finish*1.0/total*100;

    	if(this.finish > total){
    		this.finish = total;
    		process = 100;
    	}
		expend = System.currentTimeMillis() - start;//已耗时
		if(expend>0){
			expect = (long)(total / (this.finish*1.0/expend) - expend);	//剩余时间=预计总耗时-已耗时
			if(process == 100){
				expect = 0;
			}
			log();
		}
	}
	public void setProcess(double process){
		this.process = process;
	}
	private void log(){
		if(process - lastLogRate  >= 0.5 || System.currentTimeMillis() - lastLogTime > 1000 * 5 || process==100){
			long time = System.currentTimeMillis() - start;
			message = "[进度:"+FileUtil.progress(total, finish)+"]"
    				+ "[耗时:"+DateUtil.conversion(time)+"/"+DateUtil.conversion(expect)+"("+FileUtil.conversion(finish*1000/time)+"/s)]";
    		String txt = "[文件下载]"+ message + "[url:"+url+"][local:"+local.getAbsolutePath()+"]";
    		log.warn(txt);
    		lastLogRate = process;
    		lastLogTime = System.currentTimeMillis();
		}
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
	public void start(){
		this.start = System.currentTimeMillis();
	}
	@Override
	public void setTotal(long total) {
		this.total = total;
	}
	@Override
	public long getTotal() {
		return this.total;
	}
	@Override
	public long getFinish() {
		return this.finish;
	}
	@Override
	public long getStart() {
		return this.start;
	}
	public long getExpect(){
		return this.expect;
	}
	public long getExpend() {
		return expend;
	}
	@Override
	public double getProcess() {
		return process;
	}
	@Override
	public String getMessage() {
		return message;
	}
}
