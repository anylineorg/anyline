package org.anyline.video;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.anyline.util.DateUtil;
import org.apache.log4j.Logger;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class VideoUtil {
	private static Logger log = Logger.getLogger(VideoUtil.class);
	/**
	 * 截取视频获得指定帧的图片
	 * @param video 视频
	 * @param out 输入流
	 */
	public static boolean frame(File video, OutputStream out, int index) {
		if(null == video || null == out || !video.exists()){
			log.warn("[视频截图][文件异常]");
			return false;
		}
		long fr = System.currentTimeMillis();
		FFmpegFrameGrabber ff = new FFmpegFrameGrabber(video);
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
			log.warn("[视频截图][耗时:"+DateUtil.conversion(System.currentTimeMillis()-fr)+"][video:"+video.getAbsolutePath()+"]");
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
		}
		
	}
	/***
	 * 截取视频中间帧图片
	 * @param video
	 * @param out
	 * @return
	 */
	public static boolean frame(File video, OutputStream out) {
		return frame(video, out, -1);
	}
	public static boolean frame(File video, File img) {
		boolean result = false;
		try {
			File dir = img.getParentFile();
			if(!dir.exists()){
				dir.mkdirs();
			}
			result = frame(video, new FileOutputStream(img));
			if(null != img){
				log.warn("[视频截图][截图文件:"+img.getAbsolutePath()+"]");
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