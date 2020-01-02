package org.anyline.net; 
 
 
import java.io.File; 
import java.io.FileOutputStream; 
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Map; 
import java.util.Properties; 
import java.util.Vector; 

import org.anyline.util.BasicUtil; 
import org.anyline.util.ConfigTable; 
import org.anyline.util.DateUtil; 
import org.anyline.util.FileUtil; 
import org.apache.commons.lang.StringUtils; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 

import com.jcraft.jsch.Channel; 
import com.jcraft.jsch.ChannelSftp; 
import com.jcraft.jsch.ChannelSftp.LsEntry; 
import com.jcraft.jsch.JSch; 
import com.jcraft.jsch.Session; 
import com.jcraft.jsch.SftpATTRS; 
import com.jcraft.jsch.SftpException; 
import com.jcraft.jsch.SftpProgressMonitor; 
 
public class SFTPUtil { 
	private final Logger log = LoggerFactory.getLogger(SFTPUtil.class); 
    private static Map<String,SFTPUtil> instances = new HashMap<String,SFTPUtil>(); 
	private String host; 
	private int port=22; 
	private String user; 
	private String password; 
	private ChannelSftp client; 
	private Session session;  
	public SFTPUtil() throws Exception{ 
	} 
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
        Channel channel = null; 
        JSch jsch = new JSch(); 
        session = jsch.getSession(this.user, this.host, this.port);   
        if(BasicUtil.isNotEmpty(this.password)){ 
        	session.setPassword(this.password);   
        } 
        Properties sshConfig = new Properties();   
        sshConfig.put("StrictHostKeyChecking", "no");   
        session.setConfig(sshConfig); 
        session.connect();   
        channel = session.openChannel("sftp");   
        channel.connect();   
        client = (ChannelSftp) channel; 
	} 
 
    public static SFTPUtil getInstance (String host, String account, String password, int port){ 
    	String key = "host:"+host+",account:"+account+",password:"+password+",port:"+port; 
    	SFTPUtil util = instances.get(key); 
    	if(null == util){ 
    		try { 
				util = new SFTPUtil(host, account, password, port); 
			} catch (Exception e) { 
				e.printStackTrace(); 
			} 
    	} 
    	return util; 
    }   
    public static SFTPUtil getInstance(String host, String account, String password){ 
    	return getInstance(host, account, password, 22); 
    } 
       
	 /**  
     * 下载文件-sftp协议.  
     * @param remote 下载的文件  
     * @param local 存在本地的路径  
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
            List<String> list = FTPUtil.formatPath(remote); 
            long fr = System.currentTimeMillis(); 
            if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
            	log.warn("[文件下载][file:{}]",list.get(0) + list.get(1)); 
            } 
            String remotePath = list.get(0) + list.get(1); 
            SftpATTRS attr = client.stat(remotePath); 
            long length = attr.getSize(); 
            SFTPProgressMonitor process = new SFTPProgressMonitor(remotePath,local, length); 
            client.get(remotePath, os, process);   
            if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
            	log.warn("[文件下载完成][耗时:{}][file:{}]",System.currentTimeMillis()-fr,list.get(0) + list.get(1)); 
            } 
        } catch (Exception e) {   
            throw e;   
        } finally {   
            os.close();   
        }   
    }   
    /** 
     * 断开连接 
     * @return return
     */ 
	public boolean disconnect(){ 
        if (session != null) {   
            if (session.isConnected()) {   
                session.disconnect();   
            }   
        }  
		if (client != null) {   
            if (client.isConnected()) {   
            	client.disconnect();   
            } 
            client.exit(); 
        }   
		return true; 
	} 
    public int fileSize(String remoteDir){ 
    	int size = 0; 
    	try { 
			Vector<?> files = client.ls(remoteDir); 
			size = files.size(); 
		} catch (Exception e) {
			if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
            	log.warn("[检测文件数量][result:fail][msg:{}]", e.getMessage()); 
            } 
		} 
    	return size; 
    } 
    /**  
     * 删除文件-sftp协议.  
     * @param path 要删除的文件  
     * @throws SftpException 异常  
     */   
    public void deleteFile(String path) throws SftpException {   
        List<String> list = FTPUtil.formatPath(path);   
        String dir = list.get(0);   
        String file = list.get(1);   
        if (dirExist(dir + file)) {   
            client.rm(list.get(0) + list.get(1));   
        }   
    }   
   
    /**  
     * 删除文件夹-sftp协议.如果文件夹有内容，则会抛出异常.  
     * @param path 文件夹路径  
     * @throws SftpException   SftpException
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
     * @param remoteDir 远程保存路径  
     * @param remoteFile 远程保存文件名  
     * @throws SftpException 异常  
     */   
    public void uploadFile(String localFile, String remoteDir, String remoteFile) throws SftpException { 
    	long fr = System.currentTimeMillis(); 
        mkdir(remoteDir);   
        client.cd(remoteDir);   
        client.put(localFile, remoteFile);   
        if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
        	log.warn("[文件上传][耗时:{}][local:{}][remote:{}]",DateUtil.conversion(System.currentTimeMillis()-fr),localFile,remoteDir+"/"+remoteFile); 
        } 
    }   
    public void uploadFile(File localFile, String remoteDir, String remoteFile) throws SftpException { 
    	uploadFile(localFile.getAbsoluteFile(), remoteDir, remoteFile); 
    }   
   
    /**  
     * 上传文件-sftp协议.  
     * @param localFile 源文件路径，/xxx/xx.yy 或 x:/xxx/xxx.yy  
     * @return 上传成功与否  
     * @throws SftpException 异常  
     */   
    public boolean uploadFile(String localFile) throws SftpException {   
        File file = new File(localFile);   
        if (file.exists()) {   
            List<String> list = FTPUtil.formatPath(localFile);   
            uploadFile(localFile, list.get(0), list.get(1));   
            return true;   
        }   
        return false;   
    }   
   
    /**  
     * 根据路径创建文件夹.  
     * @param dir 路径 必须是 /xxx/xxx/ 不能就单独一个/  
     * @return return
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
   
    @SuppressWarnings("unchecked")
	public List<String> files(String dir){ 
    	List<String> list = new ArrayList<String>(); 
    	try { 
			Vector<LsEntry> files = client.ls(dir); 
			for(LsEntry file:files){ 
//				int t = file.getAttrs().getATime(); 
//				String s= file.getAttrs().getAtimeString(); 
//				int t1 = file.getAttrs().getMTime(); 
//				String s1= file.getAttrs().getMtimeString(); 
				String nm = file.getFilename(); 
				if(".".equals(nm) || "..".equals(nm)){ 
					continue; 
				} 
				list.add(nm); 
			} 
		} catch (Exception e) { 
			log.warn("[scan dir error][dir:{}][error:{}]",dir,e.getMessage()); 
		} 
    	if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
    		log.warn("[scan dir][dir:{}][file size:{}]",dir,list.size()); 
    	} 
    	return list; 
    } 
    public boolean fileExists(String dir, String file){ 
    	List<String> files = files(dir); 
    	if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
    		log.warn("[check file exists][dir:{}][file:{}]",dir,file); 
    	} 
    	for(String item:files){ 
    		if(item.equals(file)){ 
    			return true; 
    		} 
    	} 
    	return false; 
    } 
    public boolean fileExists(String path){ 
    	List<String> list = FTPUtil.formatPath(path); 
    	String dir = list.get(0); 
    	String file = list.get(1); 
    	if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
    		log.warn("[check file exists][path:"+path+"]"); 
    	} 
    	return fileExists(dir, file); 
    } 
 
} 
 
class SFTPProgressMonitor implements SftpProgressMonitor { 
	private final Logger log = LoggerFactory.getLogger(SFTPProgressMonitor.class); 
	private String remote = ""; 
	private String local = ""; 
	private long length;		//总长度 
	private long transfered;	//已下载长度 
	private double displayRate;		//最后显示下载比例 
	private long startTime = 0; 
	private long displayTime;		//最后显示下载时间 
 
	public SFTPProgressMonitor(String remote, long length){ 
		this.remote = remote; 
		this.length = length; 
		this.startTime = System.currentTimeMillis(); 
	} 
	public SFTPProgressMonitor(String remote, String local, long length){ 
		this.remote = remote; 
		this.local = local; 
		this.length = length; 
		this.startTime = System.currentTimeMillis(); 
	} 
	public SFTPProgressMonitor(long length){ 
		this.length = length; 
		this.startTime = System.currentTimeMillis(); 
	} 
	@Override 
	public boolean count(long count) { 
		double curRate = (transfered+count)/length * 100; 
		if(curRate - displayRate  >= 0.5 || System.currentTimeMillis() - displayTime > 1000 * 5 || curRate == 100){ 
			displayRate = curRate;  
			displayTime = System.currentTimeMillis(); 
			long delay = System.currentTimeMillis()-startTime; 
			double expect = 0; 
			if(delay>0 && transfered>0){ 
				expect = length / (transfered/delay); 
				String total_title = "[文件下载][进度:" + FileUtil.progress(length, transfered) +"][耗时:"+DateUtil.conversion(delay)+"/"+DateUtil.conversion(expect)+"("+FileUtil.length(transfered*1000/delay)+"/s)]"; 
				if(null != local){ 
					total_title = "[local:"+local+"]" + total_title; 
				} 
				if(null != remote){ 
					total_title = "[remote:"+remote+"]"  + total_title; 
				} 
				log.warn(total_title); 
			} 
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
	public long getStartTime() { 
		return startTime; 
	} 
	public void setStartTime(long startTime) { 
		this.startTime = startTime; 
	} 
	 
	 
} 
