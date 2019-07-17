package org.anyline.util;

import java.io.File;

public interface DownloadProcess {
	/**
	 * 设置进度
	 * @param finish
	 */
	public void process(long finish);
	public void setTotal(long total);
	public long getTotal();
	public long getFinish();
	public long getStart();//开始时间
	public long getExpend();//已耗时ms
	public long getExpect();//预计剩余ms
	public void start(); //开始
	public void setProcess(double process);
	public double getProcess();//进度(89.12)%
	public String getMessage();//提示信息[进度:319.54kb/8.83mb(3.54%)][耗时:1.265秒/34.514秒(252.60kb/s)]
	public void setUrl(String url);
	public void setLocal(File local);
}
