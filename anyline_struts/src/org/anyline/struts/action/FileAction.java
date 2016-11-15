/* 
 * Copyright 2006-2015 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */
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
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
public class FileAction extends AnylineAction {
	public String upload() {
		try {
			response.setCharacterEncoding("UTF-8");
			// 获得输出流
			PrintWriter writer = response.getWriter();
			// 文件保存目录路径
			File root = new File(request.getSession().getServletContext().getRealPath("/"));
			String dirRel = "upload/" + getUploadDir(null) + "/editor/" + getParam("dir");// 相对目录
			File dstDir = new File(root.getParentFile(), dirRel);
			// 定义文件上传到扩展名
			String[] fileTypes = new String[] { "gif", "jpg", "jpeg", "png", "bmp" };
			// 定义文件上传大小
			long maxSize = 2048000;
			// 获得struts2上传文件过滤器
			MultiPartRequestWrapper wrapper = (MultiPartRequestWrapper) request;
			String fileName = wrapper.getFileNames("imgFile")[0] + "";
			File file = wrapper.getFiles("imgFile")[0];
			// 扩展名
			String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

			// 判断文件类型
			if (!Arrays.asList(fileTypes).contains(fileExt)) {
				writer.print(getError("上传文件扩展名是不允许的扩展名。"));
				return "none";
			}
			// 判断文件大小
			if (file.length() > maxSize) {
				writer.print(getError("上传文件太大。"));
				return "none";
			}
			// 根据日期构造文件夹-每天创建一个新的文件夹
			// 检查目录
			if (!dstDir.exists()) {
				dstDir.mkdirs();
			}
			// 检查目录权限
			if (!dstDir.canWrite()) {
				writer.print(getError("目录不可写。"));
				return "none";
			}
			// 构造新文件名
			String newFileName = BasicUtil.getRandomLowerString(20) + "." + fileExt;
			String dstPath = dstDir.getAbsolutePath() + "/" + newFileName;
			// 设置 KE 中的图片文件地址
			// String newFileUrl = saveUrl+newFileName;
			DataRow fileRow = new DataRow();
			fileRow.put("TITLE", fileName);
			fileRow.put("SRC_NAME", fileName);
			fileRow.put("ROOT", root.getAbsolutePath());
			fileRow.put("PATH_ABS", dstPath);
			fileRow.put("PATH_REL", dirRel + "/" + newFileName);

			service.save(getUploadTable(null), fileRow);

			byte[] buffer = new byte[1024];
			FileOutputStream fos = null;
			InputStream is = null;
			try {
				fos = new FileOutputStream(dstPath);
				is = new FileInputStream(file);
				int num = 0;
				while ((num = is.read(buffer)) > 0) {
					fos.write(buffer, 0, num);
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if(null != is){
						is.close();
					}
					if(null != fos){
						fos.close();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			JSONObject jo = new JSONObject();
			jo.put("error", 0);// 0返回成功
			String url = "/ig?cd=" + fileRow.getCd();
			String cf = getParam("cf");
			if (null != cf) {
				url += "&cf=" + cf;
			}
			jo.put("url", url);
			writer.write(jo.toString());

		} catch (Exception e) {
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

	public String img() {
		String table = getUploadTable(null);
		DataRow row = service.cacheRow("static_1800",table, parseConfig("+CD:cd"));
		File file = null;
		if (row != null) {
			File root = new File(this.request.getSession().getServletContext().getRealPath("/"));
			String dirRel = row.getString("PATH_REL");
			file = new File(root.getParentFile(), dirRel);
		}
		return success(file);
	}
}
