import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.anyline.util.FileUtil;


public class Test {
	public static int idx=0;
	public static void main(String args[]) {
		File dir = new File("D:\\imgs");
		File dirs[] = dir.listFiles();
		for(File sub:dirs){
			List<File> files = FileUtil.getAllChildrenFile(sub);
			for(File file:files){
				cut(file);
			}
		}
	}

	public static void cut(File src){
		try {
			System.out.println("cut file "+ idx++ +" " +System.currentTimeMillis() + " "+src);
			File dst = new File(src.getAbsolutePath().replace("imgs","imgs_bak"));
			
		//	dst = new File("D:\\imgs_bak\\cosplay\\"+src.gets);
			if(dst.exists()){
				return;
			}
			File dir = dst.getParentFile();
			if(!dir.exists()){
				dir.mkdirs();
			}
			BufferedImage img = ImageIO.read(src); // 读入文件
			int w = img.getWidth();
			int h = img.getHeight() - 50;
			Iterator iterator = ImageIO.getImageReadersByFormatName("JPEG");/*PNG,BMP*/     
	        ImageReader reader = (ImageReader)iterator.next();/*获取图片尺寸*/  
	        InputStream inputStream = new FileInputStream(src);    
	        ImageInputStream iis = ImageIO.createImageInputStream(inputStream);     
	        reader.setInput(iis, true);     
	        ImageReadParam param = reader.getDefaultReadParam();     
	        Rectangle rectangle = new Rectangle(0,0, w, h);/*指定截取范围*/      
	        param.setSourceRegion(rectangle);     
	        BufferedImage bi = reader.read(0,param);   
	        ImageIO.write(bi, "JPEG", dst);  
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
