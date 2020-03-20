package org.anyline.video; 
 
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import org.anyline.util.DateUtil;
import org.bytedeco.javacv.FFmpegFrameGrabber; 
import org.bytedeco.javacv.Frame; 
import org.bytedeco.javacv.Java2DFrameConverter; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
public class VideoUtil { 
	private static Logger log = LoggerFactory.getLogger(VideoUtil.class); 
	/** 
	 * 截取视频获得指定帧的图片并写入输出流
	 * @param in 视频流(执行结束后将关闭input)
	 * @param out 输出流(一般提供一个文件流)
	 * @param index 截取第几帧
	 * @return return
	 */ 
	public static boolean frame(InputStream in, OutputStream out, int index) {

		long fr = System.currentTimeMillis(); 
		FFmpegFrameGrabber ff = new FFmpegFrameGrabber(in);
		try { 
			ff.start(); 
			 
			// 截取中间帧图片 
			int i = 0; 
			int length = ff.getLengthInFrames(); 
			if(index <=0){ 
				index= length / 2; 
			} 
			Frame frame = null; 
			while (i < length) { 
				frame = ff.grabFrame(); 
				if (i > index && null != frame.image) { 
					break; 
				} 
				i++; 
			 } 
 
			Java2DFrameConverter converter = new Java2DFrameConverter(); 
			BufferedImage srcImage = converter.getBufferedImage(frame); 
			int srcImageWidth = srcImage.getWidth(); 
			int srcImageHeight = srcImage.getHeight(); 
			BufferedImage thumbnailImage = new BufferedImage(srcImageWidth, srcImageHeight, BufferedImage.TYPE_3BYTE_BGR); 
			thumbnailImage.getGraphics().drawImage(srcImage.getScaledInstance(srcImageWidth, srcImageHeight, 1), 0, 0, null); 
			ImageIO.write(thumbnailImage, "jpg", out); 
			ff.stop(); 
			log.warn("[视频截图][耗时:{}]",DateUtil.conversion(System.currentTimeMillis()-fr));
			return true; 
		} catch (Exception e) { 
			e.printStackTrace(); 
			return false; 
		}finally{ 
			try { 
				ff.close(); 
			} catch (Exception e) {} 
			try{ 
				out.flush(); 
			}catch(Exception e){}
			try{
				out.close();
			}catch(Exception e){}
			try{
				in.close();
			}catch(Exception e){}
		} 
		 
	}
	public static boolean frame(File video, OutputStream out, int index){
		if(null == video || null == out || !video.exists()){
			log.warn("[视频截图][文件异常]");
			return false;
		}
		try {
			return frame(new FileInputStream(video), out, index);
		}catch (Exception e){
			return false;
		}
	}
	/*** 
	 * 截取视频中间帧图片并写入输出流
	 * @param video  视频
	 * @param out  输出流(一般提供一个文件流)
	 * @return return
	 */ 
	public static boolean frame(File video, OutputStream out) { 
		return frame(video, out, -1); 
	}
	/***
	 * 截取视频中间帧图片并写入输出文件
	 * @param video  视频
	 * @param img  输出文件
	 * @return return
	 */
	public static boolean frame(File video, File img) { 
		boolean result = false; 
		try { 
			File dir = img.getParentFile(); 
			if(!dir.exists()){ 
				dir.mkdirs(); 
			} 
			result = frame(video, new FileOutputStream(img)); 
			if(null != img){ 
				log.warn("[视频截图][截图文件:{}]", img.getAbsolutePath()); 
			} 
			return result; 
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
			return false; 
		} 
	} 
	public static boolean frame(String video, String img) { 
		return frame(new File(video), new File(img)); 
	} 
	public static boolean frame(String video, OutputStream out) { 
		return frame(new File(video), out); 
	} 
}
