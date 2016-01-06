/* 
 * Copyright 2006-2015 the original author or authors.
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
 */

package org.anyline.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ImgUtil {
	static Logger log = Logger.getLogger(ImgUtil.class);
	/**
	 * 创建验证码图片
	 * @return
	 */
	public static BufferedImage createValidateImg(String code){
		int width = ConfigTable.getInt("VALIDATE_CODE_IMG_WIDTH");				//图片宽度
		int height = ConfigTable.getInt("VALIDATE_CODE_IMG_HEIGHT");			//图片高度
		int charSize = ConfigTable.getInt("VALIDATE_CODE_IMG_CHAR_SIZE");		//字符数量
		int lineSize = ConfigTable.getInt("VALIDATE_CODE_IMG_COMPLEXITY");		//干扰线数量
		int maxXOffset = 10;													//x轴最大偏移
		int maxYOffset = 5;														//y轴最大偏移
		int charHeight = height - 2;
		int xx = width / (charSize + 2); 
		int yy = height - 4;
		
		BufferedImage img = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
		Graphics2D gd = img.createGraphics();
		// 将图像填充为白色
		gd.setColor(Color.WHITE);
		gd.fillRect(0, 0, width, height);
		//设置字体
		gd.setFont(new Font("Fixedsys", Font.PLAIN, charHeight));
		//边框。
		gd.setColor(Color.BLACK);
		gd.drawRect(0, 0, width - 1, height - 1);
		//干扰线
		Random random = new Random();
		int xFr = random.nextInt(width/2);
		int yFr = random.nextInt(height/2);
		for (int i = 0; i < lineSize; i++){
			int xOffset = random.nextInt(maxXOffset);
			int yOffset = random.nextInt(maxYOffset);
			int xEnd = xFr + xOffset;
			int yEnd = yFr + yOffset;
			if(xEnd > width-1) xEnd = xFr - xOffset;
			if(yEnd > height-1) yEnd = yFr - yOffset;
			gd.drawLine(xFr, yFr, xEnd, yEnd);
			//头尾相连的线
			xFr = xEnd;
			yFr = yEnd;
		}
		// randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
		int red = 0;
		int green = 0;
		int blue = 0;
		// 随机产生codeCount数字的验证码。
		for (int i = 0; i < charSize; i++) {
			// 得到随机产生的验证码数字。
			String chr = code.substring(i,i+1);
			// 产生随机的颜色分量来构造颜色值
			red = random.nextInt(200);
			green = random.nextInt(200);
			blue = random.nextInt(200);
			// 用随机产生的颜色将验证码绘制到图像中
			gd.setColor(new Color(red, green, blue));
			gd.drawString(chr, (i + 1) * xx, yy);
		}
		return img;
	}
	/**
	 * 按比例缩放图片
	 * @param src	源文件
	 * @param dist	目标文件
	 * @param width	最大宽度
	 * @param height最大高度
	 */
	public static void reduce(File src, File dist, int width, int height){
		if(null == src || !src.exists()){
			return;
		}
		try{
			Image srcImg = ImageIO.read(src);  
			double w = srcImg.getWidth(null);   
			double h = srcImg.getHeight(null);
			double xw = width/w;
			double xh = height/h;
			if(xw > xh){
				xw = xh;
				width = (int)(w * xw);
			}else{
				xh = xw;
				height = (int)(h * xh);
			}
			BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);  
			/* 
			* Image.SCALE_SMOOTH 的缩略算法   生成缩略图片的平滑度的优先级比速度高 生成的图片质量比较好 但速度慢 
			*/   
			tag.getGraphics().drawImage(    
			srcImg.getScaledInstance(width, height,    
			Image.SCALE_SMOOTH), 0, 0, null);    
	
			FileOutputStream out = new FileOutputStream(dist);    
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);    
			encoder.encode(tag);    
			out.close();
		}catch(Exception e){
			log.error(e);
		}
	}
}
