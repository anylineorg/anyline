/*
 * Copyright 2006-2023 www.anyline.org
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


package org.anyline.seo.util;

import org.anyline.net.HttpResponse;
import org.anyline.net.HttpUtil;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BingSeoClient {
    private static Logger log = LoggerFactory.getLogger(BingSeoClient.class);

    public BingSeoConfig config = null;
    private static Hashtable<String, BingSeoClient> instances = new Hashtable<>();

    static {
        Hashtable<String, AnylineConfig> configs = BingSeoConfig.getInstances();
        for(String key:configs.keySet()){
            instances.put(key, getInstance(key));
        }
    }
    public static Hashtable<String, BingSeoClient> getInstances(){
        return instances;
    }

    public BingSeoConfig getConfig(){
        return config;
    }
    public static BingSeoClient getInstance() {
        return getInstance("default");
    }

    public static BingSeoClient getInstance(String key) {
        if (BasicUtil.isEmpty(key)) {
            key = "default";
        }
        BingSeoClient client = instances.get(key);
        if (null == client) {
            BingSeoConfig config = BingSeoConfig.getInstance(key);
            if(null != config) {
                client = new BingSeoClient();
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
    public PushResponse push(List<String> urls) {
        String api = "https://www.bing.com/webmaster/api.svc/json/SubmitUrlbatch?apikey="+config.KEY;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Host","ssl.bing.com");
        StringBuilder builder = new StringBuilder();
        builder.append("{\"siteUrl\":\"").append(config.SITE).append("\"");
        builder.append(",\"urlList\":[");
        boolean first = true;
        for(String url:urls){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append("\"").append(url).append("\"");
        }
        builder.append("]}");
        HttpResponse response = HttpUtil.post(headers, api, "UTF-8" ,new StringEntity(builder.toString(),"utf-8"));
        return response(response);
    }
    public PushResponse push(String url){
        List<String> urls = new ArrayList<>();
        urls.add(url);
        return push(urls);
    }

    /*
    {
    "ErrorCode": 2,
    "Message": "ERROR!!! You have exceeded your daily url submission quota : 100"
     }
    * */
    private PushResponse response(HttpResponse response){
        PushResponse result = new PushResponse();
        result.setResult(true);
        if(response.getStatus() == 200){
            result.setResult(true);
            String txt = response.getText();
            result.setMessage(txt);
            if(txt.contains("ErrorCode")){
                result.setResult(false);
            }else{
                result.setResult(true);
            }
        }
        return result;
    }

}
