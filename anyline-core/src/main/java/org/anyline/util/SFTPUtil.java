package org.anyline.util;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

public class SFTPUtil {
	private final Logger log = Logger.getLogger(SFTPUtil.class);
    /** 
     * sftp连接池. 
     */  
    private Map<String, Channel> SFTP_CHANNEL_POOL = new HashMap<String, Channel>();  
	private String host;
	private int port=22;
	private String user;
	private String password;
	private ChannelSftp client;
	public SFTPUtil(String host, int port, String user, String password) throws Exception{
		this(host, user, password, 22);
	}
	public SFTPUtil(String host, String user, String password) throws Exception{
		this(host, user, password, 22);
	}
	public SFTPUtil(String host, String user, String password, int port) throws Exception{
		this.host = host;
		this.user = user;
		this.password = password;
		Session sshSession = null;  
        Channel channel = null;  
        String key = host + "," + port + "," + user + "," + password;  
        if (null == SFTP_CHANNEL_POOL.get(key)) {  
            JSch jsch = new JSch();  
            jsch.getSession(user, host, port);  
            sshSession = jsch.getSession(user, host, port);  
            sshSession.setPassword(password);  
            Properties sshConfig = new Properties();  
            sshConfig.put("StrictHostKeyChecking", "no");  
            sshSession.setConfig(sshConfig);
            sshSession.connect();  
            channel = sshSession.openChannel("sftp");  
            channel.connect();  
            SFTP_CHANNEL_POOL.put(key, channel);  
        } else {  
            channel = SFTP_CHANNEL_POOL.get(key);  
            sshSession = channel.getSession();  
            if (!sshSession.isConnected())  
                sshSession.connect();  
            if (!channel.isConnected())  
                channel.connect();  
        }  
        client = (ChannelSftp) channel;
	}
	 /** 
     * 下载文件-sftp协议. 
     * @param remoteFile 下载的文件 
     * @param localFile 存在本地的路径 
     * @return 文件 
     * @throws Exception 异常 
     */  
    public void download(String remote, String local)  throws Exception {  
        FileOutputStream os = null;  
        File localFile = new File(local);  
        try {  
            if (!localFile.exists()) {  
                File parentFile = localFile.getParentFile();  
                if (!parentFile.exists()) {  
                    parentFile.mkdirs();  
                }  
                localFile.createNewFile();  
            }  
            os = new FileOutputStream(localFile);  
            List<String> list = formatPath(remote);
            long fr = System.currentTimeMillis();
            if(ConfigTable.isDebug()){
            	log.warn("[文件下载][file:"+list.get(0) + list.get(1)+"]");
            }
            String remotePath = list.get(0) + list.get(1);
            SftpATTRS attr = client.stat(remotePath);
            long length = attr.getSize();
            client.get(remotePath, os, new SFTPProgressMonitor(remotePath,local, length));  
            if(ConfigTable.isDebug()){
            	log.warn("[文件下载完成][time:"+(System.currentTimeMillis()-fr)+"][file:"+list.get(0) + list.get(1)+"]");
            }
        } catch (Exception e) {  
            throw e;  
        } finally {  
            os.close();  
        }  
    }  
	public boolean disconnect(){
		try {
			client.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
    public int fileSize(String remoteDir){
    	int size = 0;
    	try {
			Vector<?> files = client.ls(remoteDir);
			size = files.size();
		} catch (SftpException e) {
		}
    	return size;
    }
    /** 
     * 删除文件-sftp协议. 
     * @param path 要删除的文件 
     * @throws SftpException 异常 
     */  
    public void deleteFile(String path) throws SftpException {  
        List<String> list = formatPath(path);  
        String dir = list.get(0);  
        String file = list.get(1);  
        if (dirExist(dir + file)) {  
            client.rm(list.get(0) + list.get(1));  
        }  
    }  
  
    /** 
     * 删除文件夹-sftp协议.如果文件夹有内容，则会抛出异常. 
     * @param path 文件夹路径 
     * @throws SftpException 
     */  
    public void deleteDir(String path) throws SftpException {  
        @SuppressWarnings("unchecked")  
        Vector<LsEntry> vector = client.ls(path);  
        if (vector.size() == 1) { // 文件，直接删除  
            client.rm(path);  
        } else if (vector.size() == 2) { // 空文件夹，直接删除  
            client.rmdir(path);  
        } else {  
            String fileName = "";  
            // 删除文件夹下所有文件  
            for (LsEntry en : vector) {  
                fileName = en.getFilename();  
                if (".".equals(fileName) || "..".equals(fileName)) {  
                    continue;  
                } else {  
                	deleteDir(path + "/" + fileName);  
                }  
            }  
            // 删除文件夹  
            client.rmdir(path);  
        }  
    }  
  
    /** 
     * 上传文件-sftp协议. 
     * @param localFile 源文件 
     * @param dir 保存路径 
     * @param fileName 保存文件名 
     * @throws Exception 异常 
     */  
    public void uploadFile(String localFile, String dir, String remoteFile) throws SftpException {  
        mkdir(dir);  
        client.cd(dir);  
        client.put(localFile, remoteFile);  
    }  
  
    /** 
     * 上传文件-sftp协议. 
     * @param srcFile 源文件路径，/xxx/xx.yy 或 x:/xxx/xxx.yy 
     * @return 上传成功与否 
     * @throws SftpException 异常 
     */  
    public boolean uploadFile(String localFile) throws SftpException {  
        File file = new File(localFile);  
        if (file.exists()) {  
            List<String> list = formatPath(localFile);  
            uploadFile(localFile, list.get(0), list.get(1));  
            return true;  
        }  
        return false;  
    }  
  
    /** 
     * 根据路径创建文件夹. 
     * @param dir 路径 必须是 /xxx/xxx/ 不能就单独一个/ 
     * @throws SftpException 异常 
     */  
    public boolean mkdir(String dir) throws SftpException {  
        if (StringUtils.isBlank(dir))  
            return false;  
        String md = dir.replaceAll("\\\\", "/");  
        if (md.indexOf("/") != 0 || md.length() == 1)  
            return false;  
        return mkdirs(md);  
    }  
  
    /** 
     * 递归创建文件夹. 
     * @param dir 路径 
     * @return 是否创建成功 
     * @throws SftpException 异常 
     */  
    public boolean mkdirs(String dir) throws SftpException { 
        String[] dirArr = dir.split("/");  
        String base = "";  
        for (String d : dirArr) {  
        	if(BasicUtil.isEmpty(d)){
        		continue;
        	}
            base += "/" + d;  
            if (dirExist(base + "/")) {  
                continue;  
            } else {  
                client.mkdir(base + "/");  
            }  
        }  
        return true;  
    }  
  
    /** 
     * 判断文件夹是否存在. 
     * @param dir 文件夹路径， /xxx/xxx/ 
     * @param sftp sftp协议 
     * @return 是否存在 
     */  
    public boolean dirExist(String dir) {  
        try {  
            Vector<?> vector = client.ls(dir);  
            if (null == vector)  
                return false;  
            else  
                return true;  
        } catch (SftpException e) {  
            return false;  
        }  
    }  
  
    /** 
     * 格式化路径. 
     * @param srcPath 原路径. /xxx/xxx/xxx.yyy 或 X:/xxx/xxx/xxx.yy 
     * @return list, 第一个是路径（/xxx/xxx/）,第二个是文件名（xxx.yy） 
     */  
    public List<String> formatPath(String srcPath) {  
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
    public List<String> files(String dir){
    	List<String> list = new ArrayList<String>();
    	try {
			Vector<LsEntry> files = client.ls(dir);
			for(LsEntry file:files){
				int t = file.getAttrs().getATime();
				String s= file.getAttrs().getAtimeString();
				int t1 = file.getAttrs().getMTime();
				String s1= file.getAttrs().getMtimeString();
				String nm = file.getFilename();
				if(".".equals(nm) || "..".equals(nm)){
					continue;
				}
				list.add(nm);
			}
		} catch (SftpException e) {
			log.warn("[scan dir error][dir:"+dir+"][msg:"+e.getMessage()+"]");
		}
    	if(ConfigTable.isDebug()){
    		log.warn("[scan dir][dir:"+dir+"][file size:"+list.size()+"]");
    	}
    	return list;
    }
    public boolean fileExists(String dir, String file){
    	List<String> files = files(dir);
    	if(ConfigTable.isDebug()){
    		log.warn("[check file exists][dir:"+dir+"][file:"+file+"]");
    	}
    	for(String item:files){
    		if(item.equals(file)){
    			return true;
    		}
    	}
    	return false;
    }
    public boolean fileExists(String path){
    	List<String> list = formatPath(path);
    	String dir = list.get(0);
    	String file = list.get(1);
    	if(ConfigTable.isDebug()){
    		log.warn("[check file exists][path:"+path+"]");
    	}
    	return fileExists(dir, file);
    }
    /** 
     * 关闭协议-sftp协议.(关闭会导致连接池异常，因此不建议用户自定义关闭) 
     */  
    private void exit() {  
        client.exit();  
    }
    
}

class SFTPProgressMonitor implements SftpProgressMonitor {
	private final Logger log = Logger.getLogger(SFTPProgressMonitor.class);
	private String remote = "";
	private String local = "";
	private double length;		//总长度
	private double transfered;	//已下载长度
	private double displayRate;		//最后显示下载比例
	private long displayTime;		//最后显示下载时间

	public SFTPProgressMonitor(String remote, long length){
		this.remote = remote;
		this.length = length;
	}
	public SFTPProgressMonitor(String remote, String local, long length){
		this.remote = remote;
		this.local = local;
		this.length = length;
	}
	public SFTPProgressMonitor(long length){
		this.length = length;
	}
	@Override
	public boolean count(long count) {
		double curRate = (transfered+count)/length * 100;
		if(curRate - displayRate  >= 0.5 || System.currentTimeMillis() - displayTime > 1000 * 5 || curRate == 100){
			displayRate = curRate; 
			displayTime = System.currentTimeMillis();
			String total_title = "[已下载:" + FileUtil.process(length, transfered) +"]";
			if(null != local){
				total_title = "[local:"+local+"]" + total_title;
			}
			if(null != remote){
				total_title = "[remote:"+remote+"]"  + total_title;
			}
			log.warn(total_title);
		}
		transfered = transfered + count;
		return true;

	}

	@Override
	public void end() {
		log.warn("下载完成.");
	}

	@Override
	public void init(int op, String src, String dest, long max) {
		log.warn("开始下载.");
	}
	
	
}
