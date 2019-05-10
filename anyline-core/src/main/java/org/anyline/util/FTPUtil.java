package org.anyline.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class FTPUtil {
	private final Logger log = Logger.getLogger(FTPUtil.class);
	private String host;
	private int port=21;
	private String user;
	private String password;
	private FTPClient client;
	


	public FTPUtil(String host, String user, String password) {
		this(host, user, password, 21);
	}
	public FTPUtil(String host, String user, String password, int port) {
		this.host = host;
		this.user = user;
		this.port = port;
		this.password = password;
		client = new FTPClient();
	}
	public boolean disconnect(){
		try {
			client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public int fileSize(String dir){
		int size = 0;
		try {  
	        int reply;  
	        client.connect(host, port);  
	        client.login(user, password); 
	        client.enterLocalPassiveMode();
	        reply = client.getReplyCode();  
	        if (!FTPReply.isPositiveCompletion(reply)) {  
	            client.disconnect();  
	            return 0;
	        }  
	        client.changeWorkingDirectory(dir);
	        client.setFileType(FTPClient.BINARY_FILE_TYPE);
	        size = client.listFiles().length;
	        client.logout();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } finally {  
	        if (client.isConnected()) {  
	            try {  
	                client.disconnect();  
	            } catch (IOException ioe) {  
	            }  
	        }  
	    }
		return size;
	}
	/**
	 * 下载单个文件
	 * @param remoteDir
	 * @param remoteFile
	 * @param localDir
	 * @param localFile
	 * @return
	 */
	public boolean download(String remoteDir,String remoteFile,String localDir, String localFile) {  
	    boolean success = false;  
	    try {  
	    	int reply;  
	        client.connect(host, port);  
	        client.login(user, password); 
	        client.enterLocalPassiveMode();
	        reply = client.getReplyCode();  
	        if (!FTPReply.isPositiveCompletion(reply)) {  
	            client.disconnect();  
	            return success;  
	        }  
	        client.changeWorkingDirectory(remoteDir);
	        client.setFileType(FTPClient.BINARY_FILE_TYPE);
        	if(BasicUtil.isEmpty(localFile)){
        		localFile = remoteFile;
        	}
	        File _localFile = new File(localDir, localFile);
	        File _localDir = _localFile.getParentFile();
        	if(!_localDir.exists()){
        		_localDir.mkdirs();
        	}
            OutputStream is = new FileOutputStream(_localFile);   
	        client.retrieveFile(remoteFile, is);
	        client.logout();  
	        success = true;  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } finally {  
	        if (client.isConnected()) {  
	            try {  
	                client.disconnect();  
	            } catch (IOException ioe) {  
	            }  
	        }  
	    }
	    return success;  
	}
	public boolean download(String remoteDir,String remoteFile,String localDir) {  
		return download(remoteDir, remoteFile, localDir, null);
	}
	/**
	 * 下载整个目录
	 * @param remoteDir
	 * @param localDir
	 * @return
	 */
	public boolean download(String remoteDir,String localDir) {
	    boolean success = false;  
	    try {  
	        int reply;  
	        client.connect(host, port);  
	        client.login(user, password); 
	        client.enterLocalPassiveMode();
	        reply = client.getReplyCode();  
	        if (!FTPReply.isPositiveCompletion(reply)) {  
	            client.disconnect();
	            return success;
	        }  
	        client.changeWorkingDirectory(remoteDir);
	        client.setFileType(FTPClient.BINARY_FILE_TYPE);
	        downloadDir(localDir);
	        client.logout();  
	        success = true;  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } finally {  
	        if (client.isConnected()) {  
	            try {  
	                client.disconnect();  
	            } catch (IOException ioe) {  
	            }  
	        }  
	    }
	    return success;  
	}
	private void downloadDir(String localDir){
		try{
			FTPFile[] files = client.listFiles();
	        for(FTPFile file:files){
	        	if(file.isDirectory()){
	        		client.changeWorkingDirectory(file.getName());
	        		downloadDir(localDir+"/"+file.getName());
	        	}else{
		    		File _localDir = new File(localDir);
		        	if(!_localDir.exists()){
		        		_localDir.mkdirs();
		        	}
	    	        OutputStream is = new FileOutputStream(new File(_localDir,file.getName()));   
	    	        client.retrieveFile(file.getName(), is);  
	    	        is.close();  
	    	        log.warn("[FTP文件下载][remote:"+file.getLink()+"][local:"+_localDir+"/"+file.getName()+"]");
	        	}
	        }
        }catch(Exception e){
    		e.printStackTrace();
    	}
	}
}
