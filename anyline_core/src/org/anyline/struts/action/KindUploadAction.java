package org.anyline.struts.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;

import net.sf.json.JSONObject;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
import org.springframework.context.annotation.Scope;

@ParentPackage("anyline-default")
@Namespace("/")
@Scope("prototype")
public class KindUploadAction extends AnylineAction{
	@Action(value = "kind_upload")
	public String upload(){
		try{
			response.setCharacterEncoding("UTF-8");
			//获得输出流
			PrintWriter writer = response.getWriter();
			//文件保存目录路径
			File root = new File(request.getSession().getServletContext().getRealPath("/"));
			String dirRel = "upload/"+getUploadDir(null)+"/editor/"+getParam("dir");//相对目录
			File dstDir = new File(root.getParentFile(),dirRel);
			//定义文件上传到扩展名
			String[] fileTypes = new String[] { "gif", "jpg", "jpeg", "png", "bmp" };
			//定义文件上传大小
			long maxSize = 2048000;
			//获得struts2上传文件过滤器
			MultiPartRequestWrapper wrapper  =(MultiPartRequestWrapper) request;
			String fileName = wrapper.getFileNames("imgFile")[0]+"";
			File file = wrapper.getFiles("imgFile")[0];
			//扩展名
			String fileExt= fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
			
			//判断文件类型
			if(!Arrays.asList(fileTypes).contains(fileExt)){
				writer.print(getError("上传文件扩展名是不允许的扩展名。"));
				return "none";
			}
			//判断文件大小
			if(file.length()>maxSize){
				writer.print(getError("上传文件太大。"));
				return "none";
			}
			//根据日期构造文件夹-每天创建一个新的文件夹
			//检查目录
			if(!dstDir.exists()){
				dstDir.mkdirs();
			}
			//检查目录权限
			if(!dstDir.canWrite()){
				writer.print(getError("目录不可写。"));
				return "none";
			}
			//构造新文件名
			String newFileName = BasicUtil.getRandomLowerString(20)+"."+fileExt;
			String dstPath = dstDir.getAbsolutePath()+ "/" + newFileName;
			// 设置 KE 中的图片文件地址
			//String newFileUrl = saveUrl+newFileName;
			DataRow fileRow = new DataRow();
			fileRow.put("TITLE", fileName);
			fileRow.put("SRC_NAME", fileName);
			fileRow.put("ROOT", root.getAbsolutePath());
			fileRow.put("PATH_ABS", dstPath);
			fileRow.put("PATH_REL", dirRel+"/"+newFileName);
			
			service.save(getUploadTable(null), fileRow);
			
			byte[] buffer = new byte[1024];
			FileOutputStream fos = null;
			InputStream is =null;
			try{
				fos = new FileOutputStream(dstPath);
				is = new FileInputStream(file);
				int num=0;
				while((num=is.read(buffer))>0){
					fos.write(buffer,0,num);
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try {
					is.close();
					fos.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			
			JSONObject jo = new JSONObject();
			jo.put("error", 0);// 0返回成功
			String url = "/ig?cd="+fileRow.getCd();
			String cf = getParam("cf");
			if(null != cf){
				url += "&cf="+cf;
			}
			jo.put("url", url);
			writer.write(jo.toString());
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return "none";
	}
	private String getError(String message) {
		JSONObject jo = new JSONObject();
		jo.put("error", 1);// 0返回成功
		jo.put("message", message);
		return jo.toString();
	}
}
