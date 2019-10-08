package org.anyline.net;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyline.util.DateUtil;
import org.anyline.util.FileUtil;
import org.anyline.util.regular.RegularUtil;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
  
public class FTPUtil {  
      
    private static Logger log = Logger.getLogger(FTPUtil.class);
    private static Map<String,FTPUtil> instances = new HashMap<String,FTPUtil>();
    private FTPClient client;  
	private String host;
	private int port=21;
	private String account;
	private String password;
	private String dir;
	
  
    public FTPUtil() {  
        client = new FTPClient();  
        client.setControlEncoding("UTF-8"); 
    }  
    public FTPUtil(String host, String account, String password, int port){
        client = new FTPClient();  
    	this.host = host;
    	this.account = account;
    	this.password = password;
    	this.port = port;
        client.setControlEncoding("UTF-8"); 
		connect();
    }  
    public FTPUtil(String host, String account, String password){
        client = new FTPClient();  
    	this.host = host;
    	this.account = account;
    	this.password = password;
        client.setControlEncoding("UTF-8");  
		connect();
    }

    public static FTPUtil getInstance (String host, String account, String password, int port){
    	String key = "host:"+host+",account:"+account+",password:"+password+",port:"+port;
    	FTPUtil util = instances.get(key);
    	if(null == util){
    		util = new FTPUtil(host, account, password, port);
    	}
    	return util;
    }  
    public static FTPUtil getInstance(String host, String account, String password){
    	return getInstance(host, account, password, 21);
    }
      
    public void setTimeOut(int defaultTimeoutSecond, int connectTimeoutSecond, int dataTimeoutSecond){  
        try {  
            client.setDefaultTimeout(defaultTimeoutSecond * 1000);  
            client.setConnectTimeout(connectTimeoutSecond * 1000); //commons-net-3.5.jar  
            client.setSoTimeout(connectTimeoutSecond * 1000); //commons-net-1.4.1.jar 连接后才能设置  
            client.setDataTimeout(dataTimeoutSecond * 1000);  
        } catch (SocketException e) {
        	e.printStackTrace();
        }  
    }  

	public int fileSize(String dir){
		int size = 0;
		try {  
	        cd(dir);
	        client.setFileType(FTPClient.BINARY_FILE_TYPE);
	        size = client.listFiles().length;
	    } catch (IOException e) {  
	        e.printStackTrace();  
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
	public boolean downloadFile(String remote, File local) {  
	    boolean success = false;
	    long fr = System.currentTimeMillis();
	    try {
	        client.setFileType(FTPClient.BINARY_FILE_TYPE);
	        File _localDir = local.getParentFile();
        	if(!_localDir.exists()){
        		_localDir.mkdirs();
        	}
            OutputStream is = new FileOutputStream(local);   
	        client.retrieveFile(remote, is);
	        success = true;  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }
	    log.warn("[ftp download file][耗时:"+DateUtil.conversion(System.currentTimeMillis()-fr)+"][length:"+FileUtil.length(local.length())+"][remote:"+remote+"][local:"+local.getAbsolutePath()+"]");
	    return success;  
	}
	/////////////////////////////
    public FTPClient getFTPClient(){  
        return client;  
    }  
      
    public void setControlEncoding(String charset){  
        client.setControlEncoding(charset);  
    }  
      
    public void setFileType(int fileType) throws IOException {  
        client.setFileType(fileType);  
    }  
  
    public void connect() {
    	try{
	        client.connect(host, port);  
	        int reply = client.getReplyCode();  
	        if (!FTPReply.isPositiveCompletion(reply)) {  
	            disconnect();  
	        }  
	        if ("".equals(account)) {  
	        	account = "anonymous";  
	        }  
	        if (!client.login(account, password)) {  
	            disconnect();  
	        }  
	        client.setFileType(FTP.BINARY_FILE_TYPE);  
	        //ftp.setFileType(FTP.ASCII_FILE_TYPE);  
	        client.enterLocalPassiveMode(); 
    	}catch(Exception e){
    		e.printStackTrace();
    	}
          
    }  
      
    /** 
     * Test connection to ftp server 
     *  
     * @return true, if connected 
     */  
    public boolean isConnected() {  
        return client.isConnected();  
    }  
      
    public void disconnect(){  
        if (client.isConnected()) {  
            try {  
                client.logout();  
                client.disconnect();  
            } catch (IOException ex) {  
            }  
        }  
    }  
      
    /** 
     * Get file from ftp server into given output stream 
     *  
     * @param ftpFileName 
     *            file name on ftp server 
     * @param out 
     *            OutputStream 
     * @throws IOException 
     */  
    public void retrieveFile(String ftpFileName, OutputStream out) throws IOException {  
        try {  
            // Get file info.  
            FTPFile[] fileInfoArray = client.listFiles(ftpFileName);  
            if (fileInfoArray == null || fileInfoArray.length == 0) {  
                throw new FileNotFoundException("File '" + ftpFileName + "' was not found on FTP server.");  
            }  
  
            // Check file size.  
            FTPFile fileInfo = fileInfoArray[0];  
            long size = fileInfo.getSize();  
            if (size > Integer.MAX_VALUE) {  
                throw new IOException("File '" + ftpFileName + "' is too large.");  
            }  
  
            // Download file.  
            if (!client.retrieveFile(ftpFileName, out)) {  
                throw new IOException("Error loading file '" + ftpFileName + "' from FTP server. Check FTP permissions and path.");  
            }  
  
            out.flush();  
  
        } finally {  
            if (out != null) {  
                try {  
                    out.close();  
                } catch (IOException ex) {  
                }  
            }  
        }  
    }  
  
    /** 
     * Put file on ftp server from given input stream 
     *  
     * @param ftpFileName 
     *            file name on ftp server 
     * @param in 
     *            InputStream 
     * @throws IOException 
     */  
    public void storeFile(String ftpFileName, InputStream in) throws IOException {  
        try {  
            if (!client.storeFile(ftpFileName, in)) {  
                throw new IOException("Can't upload file '" + ftpFileName + "' to FTP server. Check FTP permissions and path.");  
            }  
        } finally {  
            try {  
                in.close();  
            } catch (IOException ex) {  
            }  
        }  
    }  
      
    /** 
     * 修改名称 
     * @param from 
     * @param to 
     * @throws IOException 
     */  
    public boolean rename(String from, String to) throws IOException {  
        return client.rename(from, to);  
    }  
      
    /** 
     * Delete the file from the FTP server. 
     *  
     * @param ftpFileName 
     *            server file name (with absolute path) 
     * @throws IOException 
     *             on I/O errors 
     */  
    public void deleteFile(String ftpFileName) throws IOException {  
        if (!client.deleteFile(ftpFileName)) {  
            throw new IOException("Can't remove file '" + ftpFileName + "' from FTP server.");  
        }  
    }  

    public boolean  uploadFile(File local, String remote) {
    	boolean result = false;
    	long fr = System.currentTimeMillis();
    	 InputStream in = null;  
         try {
             in = new BufferedInputStream(new FileInputStream(local));  
             List<String> paths = formatPath(remote);
             makeDir(paths.get(0));
             client.storeFile(remote, in);
             result = true;
         }catch(Exception e){
         	e.printStackTrace();
         } finally {  
             try {  
                 in.close();  
             } catch (IOException ex) {  
             }  
         } 
        log.warn("[ftp upload file][耗时:"+DateUtil.conversion(System.currentTimeMillis()-fr)+"][length:"+FileUtil.length(local.length())+"][remote:"+remote+"][local:"+local.getAbsolutePath()+"]");
        return result;
    }
    public boolean  uploadFile(String remote, File local) {
    	return uploadFile(local, remote);
    }
    public boolean  upload(String remote, File local) {
    	boolean result = false;
        if (!local.exists()) {  
        	return result;
        }
        if(local.isDirectory()){
        	uploadDir(remote, local);
        }else{
        	uploadFile(remote, local);
        }
        return result;
    }  
      
    /** 
     * 上传目录（会覆盖) 
     * @param remotePath 远程目录 /home/test/a 
     * @param localPath 本地目录 D:/test/a 
     * @throws IOException 
     */  
    public void uploadDir(String remotePath, File local){  
    	log.warn("[ftp upload dir][remote:"+remotePath+"][local:"+local.getAbsolutePath()+"]");
        if (null != local &&local.exists()) {  
            if(!cd(remotePath)){  
                try {
					client.makeDirectory(remotePath);
				} catch (IOException e) {
					e.printStackTrace();
				} 
                cd(remotePath); //切换成返回true，失败（不存在）返回false  
            }  
            File[] files = local.listFiles();  
            for (File f : files) {  
                if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {  
                    uploadDir(remotePath + "/" + f.getName(), f);  
                } else if (f.isFile()) {  
                    uploadFile(remotePath + "/" + f.getName(), f);  
                }  
            }  
        }  
    }  
  

	/**
	 * 下载整个目录
	 * @param remoteDir
	 * @param localDir
	 * @return
	 */
	public boolean downloadDir(String remoteDir, File localDir) {
	    boolean success = false;  
	    log.warn("[ftp download dir][remote:"+remoteDir+"][local:"+localDir.getAbsolutePath()+"]");
	    try {  
	        cd(remoteDir);
	        client.setFileType(FTPClient.BINARY_FILE_TYPE);
	        downloadDir(localDir);
	        success = true;  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }
	    return success;  
	}
	public boolean cd(String dir){
		boolean result = false;
		try {
			result = client.changeWorkingDirectory(dir);
			String path = client.doCommandAsStrings("pwd","")[0];
			this.dir = RegularUtil.cut(path, "\"", "\"");
			log.warn("[ftp change directory][directory:"+this.dir+"]");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	private void downloadDir(File localDir){
		try{
			FTPFile[] files = client.listFiles();
	        for(FTPFile file:files){
	        	if(file.isDirectory()){
	        		cd(file.getName());
	        		downloadDir(new File(localDir+"/"+file.getName()));
	        	}else{
		        	File local = new File(localDir,file.getName());
	        		downloadFile(this.dir+"/"+file.getName(), local);
	        	}
	        }
        }catch(Exception e){
    		e.printStackTrace();
    	}
	}
    public List<String> files(String dir) throws IOException {  
        List<String> fileList = new ArrayList<String>();  
  
        FTPFile[] ftpFiles = client.listFiles(dir);  
        for (int i = 0; ftpFiles!=null && i<ftpFiles.length; i++) {  
            FTPFile ftpFile = ftpFiles[i];  
            if (ftpFile.isFile()) {  
                fileList.add(ftpFile.getName());  
            }  
        }  
          
        return fileList;  
    }  
      
  
  
    public void sendSiteCommand(String args) throws IOException {  
        if (client.isConnected()) {  
            try {  
                client.sendSiteCommand(args);  
            } catch (IOException ex) {  
            }  
        }  
    }  
  
    public String printWorkingDirectory() {  
        if (!client.isConnected()) {  
            return "";  
        }  
  
        try {  
            return client.printWorkingDirectory();  
        } catch (IOException e) {  
        }  
  
        return "";  
    }  
    public boolean changeToParentDirectory() {  
        if (!client.isConnected()) {  
            return false;  
        }  
  
        try {  
            return client.changeToParentDirectory();  
        } catch (IOException e) {  
        }  
  
        return false;  
    }  
  
    /** 
     * Get parent directory name on ftp server 
     *  
     * @return parent directory 
     */  
    public String getParentDirectory() {  
        if (!client.isConnected()) {  
            return "";  
        }  
  
        String w = printWorkingDirectory();  
        changeToParentDirectory();  
        String p = printWorkingDirectory();  
        cd(w);  
        return p;  
    }  
      
    /** 
     * 创建目录 
     * @param pathname 
     * @throws IOException 
     */  
    public boolean makeDir(String pathname) throws IOException {  
        return client.makeDirectory(pathname);  
    }  


    /** 
     * 格式化路径. 
     * @param srcPath 原路径. /xxx/xxx/xxx.yyy 或 X:/xxx/xxx/xxx.yy 
     * @return list, 第一个是路径（/xxx/xxx/）,第二个是文件名（xxx.yy） 
     */  
    public static List<String> formatPath(String srcPath) {  
        List<String> list = new ArrayList<String>(2);  
        String repSrc = srcPath.replaceAll("\\\\", "/");  
        int firstP = repSrc.indexOf("/");  
        int lastP = repSrc.lastIndexOf("/");  
        String fileName = lastP + 1 == repSrc.length() ? "" : repSrc.substring(lastP + 1);  
        String dir = firstP == -1 ? "" : repSrc.substring(firstP, lastP);  
        dir = (dir.length() == 1 ? dir : (dir + "/"));  
        list.add(dir);  
        list.add(fileName);  
        return list;  
    }  
    
    public String getDir() {
		return dir;
	} 
  
}  