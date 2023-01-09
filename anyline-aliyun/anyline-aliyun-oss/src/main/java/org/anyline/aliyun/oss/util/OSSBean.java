package org.anyline.aliyun.oss.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.oss.load.bean")
public class OSSBean implements InitializingBean {

	@Value("${anyline.aliyun.oss.accessId:}")
	public String ACCESS_ID		;
	@Value("${anyline.aliyun.oss.accessSecret:}")
	public String ACCESS_SECRET ;
	@Value("${anyline.aliyun.oss.endpoint:}")
	public String ENDPOINT		;
	@Value("${anyline.aliyun.oss.bucket:}")
	public String BUCKET		;
	@Value("${anyline.aliyun.oss.dir:}")
	public String DIR			;
	@Value("${anyline.aliyun.oss.expire:1800}")
	public int EXPIRE_SECOND 	;

	@Override
	public void afterPropertiesSet() throws Exception {
		ACCESS_SECRET = BasicUtil.evl(ACCESS_SECRET, OSSConfig.DEFAULT_ACCESS_SECRET);
 		if(BasicUtil.isEmpty(ACCESS_SECRET)){
			return;
		}
		OSSConfig.register(
				BasicUtil.evl(ACCESS_ID, OSSConfig.DEFAULT_ACCESS_ID)
				, BasicUtil.evl(ACCESS_SECRET, OSSConfig.DEFAULT_ACCESS_SECRET)
				, BasicUtil.evl(ENDPOINT, OSSConfig.DEFAULT_ENDPOINT)
				, BasicUtil.evl(BUCKET, OSSConfig.DEFAULT_BUCKET)
				, BasicUtil.evl(DIR, OSSConfig.DEFAULT_DIR)
				, BasicUtil.evl(EXPIRE_SECOND, OSSConfig.DEFAULT_EXPIRE_SECOND)
		);
	}
	@Bean("anyline.aliyun.oss.init.util")
	public OSSUtil instance(){
		return OSSUtil.getInstance();
	}


}
