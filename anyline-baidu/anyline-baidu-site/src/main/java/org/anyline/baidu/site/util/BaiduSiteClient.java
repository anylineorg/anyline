package org.anyline.baidu.site.util;

import org.anyline.entity.DataRow;
import org.anyline.net.HttpResponse;
import org.anyline.net.HttpUtil;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.FileUtil;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class BaiduSiteClient {
    private static Logger log = LoggerFactory.getLogger(BaiduSiteClient.class);

    public BaiduSiteConfig config = null;
    private static Hashtable<String, BaiduSiteClient> instances = new Hashtable<>();

    static {
        Hashtable<String, AnylineConfig> configs = BaiduSiteConfig.getInstances();
        for(String key:configs.keySet()){
            instances.put(key, getInstance(key));
        }
    }
    public static Hashtable<String, BaiduSiteClient> getInstances(){
        return instances;
    }

    public BaiduSiteConfig getConfig(){
        return config;
    }
    public static BaiduSiteClient getInstance() {
        return getInstance("default");
    }

    public static BaiduSiteClient getInstance(String key) {
        if (BasicUtil.isEmpty(key)) {
            key = "default";
        }
        BaiduSiteClient client = instances.get(key);
        if (null == client) {
            BaiduSiteConfig config = BaiduSiteConfig.getInstance(key);
            if(null != config) {
                client = new BaiduSiteClient();
                client.config = config;
                instances.put(key, client);
            }
        }
        return client;
    }

    /**
     * 提交url
     * @param urls urls
     * @return SubmitResponse
     */
    public SubmitResponse submit(List<String> urls) {
        String api = "http://data.zz.baidu.com/urls?site="+config.SITE+"&token="+config.TOKEN;
        StringBuilder builder = new StringBuilder();
        for(String url:urls){
            builder.append(url).append("\n");
        }
        HttpResponse response = HttpUtil.post(api, "UTF-8" ,new StringEntity(builder.toString(),"utf-8"));
        return response(response);
    }
    public SubmitResponse submit(String url){
        List<String> urls = new ArrayList<>();
        urls.add(url);
        return submit(urls);
    }

    /**
     *
     * 提交url
     * @param file url文件 每行一个url
     * @return SubmitResponse
     */
    public SubmitResponse submit(File file) {
        SubmitResponse result = new SubmitResponse();
        String api = "http://data.zz.baidu.com/urls?site="+config.SITE+"&token="+config.TOKEN;
        HttpResponse response = HttpUtil.post(api, "UTF-8" ,new StringEntity(FileUtil.read(file).toString(),"utf-8"));
        return response(response);
    }
    private SubmitResponse response(HttpResponse response){
        SubmitResponse result = new SubmitResponse();
        result.setStatus(response.getStatus());
        if(response.getStatus() == 200){
            result.setResult(true);
            String txt = response.getText();
            DataRow row = DataRow.parseJson(txt);
            result.setSuccess(row.getInt("success",0));
            result.setRemain(row.getInt("remain",0));
            result.setMessage(txt);
        }
        return result;
    }

}
