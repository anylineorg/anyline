package org.anyline.tencent.cos;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("anyline.tencent.cos.load.bean")
public class COSBean implements InitializingBean {

    @Value("${anyline.tencent.cos.id:}")
    public String ACCESS_ID					; //
    @Value("${anyline.tencent.cos.secret :}")
    public String ACCESS_SECRET 			; //
    @Value("${anyline.tencent.cos.endpoint:}")
    public String ENDPOINT					; //
    @Value("${anyline.tencent.cos.bucket:}")
    public String BUCKET					; //
    @Value("${anyline.tencent.cos.dir:}")
    public String DIR						; //
    @Value("${anyline.tencent.cos.expire:3600}")
    public int EXPIRE_SECOND 				; //

    @Override
    public void afterPropertiesSet() throws Exception {
        ACCESS_ID = BasicUtil.evl(ACCESS_ID, COSConfig.DEFAULT_ACCESS_ID);
        if(BasicUtil.isEmpty(ACCESS_ID)){
            return;
        }
        DataRow row = new DataRow();
        row.put("ACCESS_ID", BasicUtil.evl(ACCESS_ID, COSConfig.DEFAULT_ACCESS_ID));
        row.put("ACCESS_SECRET", BasicUtil.evl(ACCESS_SECRET, COSConfig.DEFAULT_ACCESS_SECRET));
        row.put("ENDPOINT", BasicUtil.evl(ENDPOINT, COSConfig.DEFAULT_ENDPOINT));
        row.put("BUCKET", BasicUtil.evl(BUCKET, COSConfig.DEFAULT_BUCKET));
        row.put("DIR", BasicUtil.evl(DIR, COSConfig.DEFAULT_DIR));
        row.put("EXPIRE_SECOND", BasicUtil.evl(EXPIRE_SECOND, COSConfig.DEFAULT_EXPIRE_SECOND));
        COSConfig.register(row);
    }

    /*@Bean("anyline.tencent.cos.init.bean")
    public COSUtil instance(){
        return COSUtil.getInstance();
    }*/
}
