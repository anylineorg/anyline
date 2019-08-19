package org.anyline.net;


public interface DownloadListener {
	public void finish(DownloadTask task);
	public void error(DownloadTask task);
	public void start(DownloadTask task);
}
