package org.anyline.aliyun.oss.util; 
 
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Hashtable;

@Component
public class OSSBean extends AnylineConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();

	@Value("${anyline.aliyun.oss.id:}")
	public String ACCESS_ID		;
	@Value("${anyline.aliyun.oss.secret:}")
	public String ACCESS_SECRET ;
	@Value("${anyline.aliyun.oss.endpoint:}")
	public String ENDPOINT		;
	@Value("${anyline.aliyun.oss.bucket:}")
	public String BUCKET		;
	@Value("${anyline.aliyun.oss.dir:}")
	public String DIR			;
	@Value("${anyline.aliyun.oss.expire:}")
	public int EXPIRE_SECOND 	;

	@PostConstruct
	private void init(){
		OSSConfig.register(
				BasicUtil.evl(ACCESS_ID, OSSConfig.DEFAULT_ACCESS_ID)
				, BasicUtil.evl(ACCESS_SECRET, OSSConfig.DEFAULT_ACCESS_SECRET)
				, BasicUtil.evl(ENDPOINT, OSSConfig.DEFAULT_ENDPOINT)
				, BasicUtil.evl(BUCKET, OSSConfig.DEFAULT_BUCKET)
				, BasicUtil.evl(DIR, OSSConfig.DEFAULT_DIR)
				, BasicUtil.evl(EXPIRE_SECOND, OSSConfig.DEFAULT_EXPIRE_SECOND)
		);
	}
	@Bean
	public OSSUtil instance(){
		return OSSUtil.getInstance();
	}
} 
