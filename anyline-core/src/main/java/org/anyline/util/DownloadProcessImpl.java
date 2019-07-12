package org.anyline.util;

import java.io.File;

import org.apache.log4j.Logger;

public class DownloadProcessImpl implements DownloadProcess{
	private Logger log = Logger.getLogger(DownloadProcessImpl.class);
	private String url		; //URL
	private File local		; //本地文件
	private long total		; //总大小
	private long finish		; //已完成
	private double rate		; //完成百分比rate%
	private long time		; //已耗时	
	private long expect		; //预计耗时
	
	private double lastLogRate	; //最后一次日志进度
	private long lastLogTime	; //量后一次日志时间
	
	
	public DownloadProcessImpl(){}
	public DownloadProcessImpl(String url, File local){
		this.url = url;
		this.local = local;
	}
	@Override
	public void process(long total, long finish, long time) {
    	if(finish > total){
    		finish = total;
    	}
		this.total = total;
		this.finish = finish;
		this.time = time;

		rate = finish*1.0/total*100;
		expect = total / (finish/time);
		log();
	}
	private void log(){
		if(rate - lastLogRate  >= 0.5 || System.currentTimeMillis() - lastLogTime > 1000 * 5 || rate==100){
    		String process = "[文件下载][进度:"+FileUtil.process(total, finish)+"][耗时:"+DateUtil.conversion(time)+"/"+DateUtil.conversion(expect)+"("+FileUtil.size(finish*1000/time)+"/s)][url:"+url+"][local:"+local.getAbsolutePath()+"]";
    		log.warn(process);
    		lastLogRate = rate;
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
	
}
