/*
 * Copyright 2006-2025 www.anyline.org
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
 
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
 
public class VerifyCodeUtil {
 
	//, 字体只显示大写, 去掉了1, 0, i, o, 2, Z, L, C, G几个容易混淆的字符
	public static final String VERIFY_CODES = "3456789ABDEFHJKMNPQRSTUVWXY"; 
	private static final Random random = new Random();
 
	/** 
	 * 使用系统默认字符源生成验证码 
	 *  
	 * @param len 验证码长度 
	 * @return String
	 */ 
	public static String getRandomCode(int len) {
		return getRandomCode(len, VERIFY_CODES);
	} 
 
	/** 
	 * 使用指定源生成验证码 
	 *  
	 * @param verifySize  验证码长度 
	 * @param sources  验证码字符源 
	 * @return String
	 */ 
	public static String getRandomCode(int verifySize, String sources) {
		if (sources == null || sources.isEmpty()) {
			sources = VERIFY_CODES; 
		} 
		int codesLen = sources.length(); 
		Random rand = new Random(System.currentTimeMillis()); 
		StringBuilder verifyCode = new StringBuilder(verifySize); 
		for (int i = 0; i < verifySize; i++) {
			verifyCode.append(sources.charAt(rand.nextInt(codesLen - 1))); 
		} 
		return verifyCode.toString(); 
	} 
 
	/** 
	 * 生成随机验证码文件, 并返回验证码值
	 *  
	 * @param w  w
	 * @param h  h
	 * @param outputFile  outputFile
	 * @param verifySize  verifySize
	 * @return String
	 * @throws IOException IOException
	 */ 
	public static String outputVerifyImage(int w, int h, File outputFile, int verifySize) throws IOException {
		String verifyCode = getRandomCode(verifySize); 
		outputImage(w, h, outputFile, verifyCode);
		return verifyCode; 
	}

	/**
	 * 输出随机验证码图片流, 并返回验证码值
	 * 
	 * @param w w 
	 * @param h h
	 * @param os os
	 * @param verifySize verifySize
	 * @param trouble trouble
	 * @return String
	 * @throws IOException IOException
	 */
	public static String outputVerifyImage(int w, int h, OutputStream os, int verifySize, boolean trouble) throws IOException {
		String verifyCode = getRandomCode(verifySize);
		outputImage(w, h, os, verifyCode, trouble);
		return verifyCode;
	}

	public static String outputVerifyImage(int w, int h, OutputStream os, int verifySize) throws IOException {
		return outputVerifyImage(w, h, os, verifySize, true);
	}

	/**
	 * 输出计算公式
	 * @param w w
	 * @param h h
	 * @param os os
	 * @return String
	 * @throws IOException IOException
	 */
	public static String outputVerifyImageFormula(int w, int h, OutputStream os) throws IOException {
		String[] codes = createFormula();
		outputImage(w, h, os, codes[0]);
		return codes[1];
	}
	private static String[] createFormula() {
		String[] types = new String[]{"加", "减", "乘", "除"};
		int d1 = BasicUtil.getRandomNumber(1, 9);
		int d2 = BasicUtil.getRandomNumber(1, 9);
		int type = d1%4;
		if(d1 < d2 && type==1) {
			type = 0;
		}
		if(d1%d2 !=0 && type==3) {
			type = 2;
		}
		String str = d1 + types[type] + d2;
		int cal = d1 + d2;
		if(type ==1) {
			cal = d1 - d2;
		}else if(type ==2) {
			cal = d1 * d2;
		}
        return new String[]{str, cal+""};
	} 
 
	/** 
	 * 生成指定验证码图像文件 
	 *  
	 * @param w  w
	 * @param h  h
	 * @param outputFile  outputFile
	 * @param code  code
	 * @throws IOException IOException
	 */ 
	public static void outputImage(int w, int h, File outputFile, String code) throws IOException {
		if (outputFile == null) {
			return; 
		} 
		File dir = outputFile.getParentFile(); 
		if (null != dir && !dir.exists()) {
			dir.mkdirs(); 
		} 
		try {
			outputFile.createNewFile(); 
			FileOutputStream fos = new FileOutputStream(outputFile); 
			outputImage(w, h, fos, code);
			fos.close(); 
		} catch (IOException e) {
			throw e; 
		}
	}
	public static BufferedImage createImage(String code, int w, int h) {
		return createImage(code, w, h, true);
	} 
	public static BufferedImage createImage(String code, int w, int h, boolean trouble) {
		int verifySize = code.length(); 
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Random rand = new Random(); 
		Graphics2D g2 = image.createGraphics();
		g2.setFont(new Font("宋体", Font.PLAIN, 12));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Color[] colors = new Color[5]; 
		Color[] colorSpaces = new Color[] {Color.WHITE, Color.CYAN, Color.GRAY, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.YELLOW };
		float[] fractions = new float[colors.length]; 
		for (int i = 0; i < colors.length; i++) {
			colors[i] = colorSpaces[rand.nextInt(colorSpaces.length)]; 
			fractions[i] = rand.nextFloat(); 
		} 
		Arrays.sort(fractions); 
 
		g2.setColor(Color.GRAY);// 设置边框色 
		g2.fillRect(0, 0, w, h);
 
		Color c = getRandColor(200, 250);
		g2.setColor(c);// 设置背景色 
		g2.fillRect(0, 2, w, h - 4);

		if(trouble) {//干扰线
			shear(g2, w, h, c);
		}
		g2.setColor(getRandColor(100, 160));
		int fontSize = h - 4;
		Font font = new Font("Algerian", Font.ITALIC, fontSize);
		g2.setFont(font);
		char[] chars = code.toCharArray();
		for (int i = 0; i < verifySize; i++) {
			g2.setColor(getRandColor(0, 160));
			if(trouble) {//倾斜
				AffineTransform affine = new AffineTransform();
				affine.setToRotation(Math.PI / 4 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1), (w / verifySize) * i + fontSize / 2, h / 2);
				g2.setTransform(affine);
			}
			g2.drawChars(chars, i, 1, ((w - 10) / verifySize) * i, h / 2 + fontSize / 2 - 5);
		}

		
		if(trouble) {
			// 绘制干扰线
			Random random = new Random(); 
			g2.setColor(getRandColor(160, 200));// 设置线条的颜色
			for (int i = 0; i < 20; i++) {
				// g2.setStroke(new BasicStroke(2));
				int x = random.nextInt(w - 1); 
				int y = random.nextInt(h - 1); 
				int xl = random.nextInt(6) + 1; 
				int yl = random.nextInt(12) + 1; 
				g2.drawLine(x, y, x + xl + 40, y + yl + 20);
			} 
	 
			// 添加噪点
			float yawpRate = 0.1f;// 噪声率 
			int area = (int) (yawpRate * w * h); 
			for (int i = 0; i < area; i++) {
				int x = random.nextInt(w); 
				int y = random.nextInt(h); 
				int rgb = getRandomIntColor(); 
				image.setRGB(x, y, rgb);
			}
		} 
		g2.dispose(); 
		return image; 
	} 
	public static void outputImage(int w, int h, OutputStream os, String code) throws IOException {
		ImageIO.write(createImage(code, w, h, true), "jpg", os);
	}
	public static void outputImage(int w, int h, OutputStream os, String code, boolean trouble) throws IOException {
		ImageIO.write(createImage(code, w, h, trouble), "jpg", os);
	}
	public static String createBase64(int w, int h, String code, boolean trouble) throws IOException {
		BufferedImage img = createImage(code, w, h, trouble);
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", bao);
		byte[] bytes = Base64.getEncoder().encode(bao.toByteArray());
		String base64 = new String(bytes);
		base64 = base64.replaceAll("\n", "").replaceAll("\r", "");//删除 \r\n
		return "data:image/png;base64," + base64;
	}
	private static Color getRandColor(int fc, int bc) {
		if (fc > 255) 
			fc = 255; 
		if (bc > 255) 
			bc = 255; 
		int r = fc + random.nextInt(bc - fc); 
		int g = fc + random.nextInt(bc - fc); 
		int b = fc + random.nextInt(bc - fc); 
		return new Color(r, g, b);
	} 
 
	private static int getRandomIntColor() {
		int[] rgb = getRandomRgb(); 
		int color = 0; 
		for (int c : rgb) {
			color = color << 8; 
			color = color | c; 
		} 
		return color; 
	} 
 
	private static int[] getRandomRgb() {
		int[] rgb = new int[3]; 
		for (int i = 0; i < 3; i++) {
			rgb[i] = random.nextInt(255); 
		} 
		return rgb; 
	} 
 
	private static void shear(Graphics g, int w1, int h1, Color color) {
		shearX(g, w1, h1, color);
		shearY(g, w1, h1, color);
	} 
 
	private static void shearX(Graphics g, int w1, int h1, Color color) {
 
		int period = random.nextInt(2); 
 
		boolean borderGap = true; 
		int frames = 1; 
		int phase = random.nextInt(2); 
 
		for (int i = 0; i < h1; i++) {
			double d = (double) (period >> 1) * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames); 
			g.copyArea(0, i, w1, 1, (int) d, 0);
			if (borderGap) {
				g.setColor(color); 
				g.drawLine((int) d, i, 0, i);
				g.drawLine((int) d + w1, i, w1, i);
			} 
		} 
 
	} 
 
	private static void shearY(Graphics g, int w1, int h1, Color color) {
 
		int period = random.nextInt(40) + 10; // 50;
 
		boolean borderGap = true; 
		int frames = 20; 
		int phase = 7; 
		for (int i = 0; i < w1; i++) {
			double d = (double) (period >> 1) * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames); 
			g.copyArea(i, 0, 1, h1, 0, (int) d);
			if (borderGap) {
				g.setColor(color); 
				g.drawLine(i, (int) d, i, 0);
				g.drawLine(i, (int) d + h1, i, h1);
			} 
 
		} 
 
	} 
 
} 
