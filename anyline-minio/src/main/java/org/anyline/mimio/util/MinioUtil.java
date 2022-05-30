package org.anyline.mimio.util;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.anyline.util.BasicUtil;
import org.apache.commons.compress.compressors.FileNameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MinioUtil {
    private static final Logger log = LoggerFactory.getLogger(MinioUtil.class);


    private MinioClient client;


    private MinioConfig config = null;
    private static Hashtable<String, MinioUtil> instances = new Hashtable<String, MinioUtil>();
    public MinioUtil(){}
    public MinioUtil(String endpoint, String bucket, String key, String secret){
        MinioConfig config = new MinioConfig();
        config.ENDPOINT = endpoint;
        config.ACCESS_KEY = key;
        config.ACCESS_SECRET = secret;
        config.BUCKET = bucket;
        this.config = config;
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(key, secret)
                .build();

    }

    public static MinioUtil getInstance() {
        return getInstance("default");
    }

    public MinioClient getClient() {
        return client;
    }
    public void setClient(MinioClient client) {
        this.client = client;
    }
    public MinioConfig getConfig(){
        return config;
    }
    public void setConfig(MinioConfig config){
        this.config = config;
    }
    @SuppressWarnings("deprecation")
    public static MinioUtil getInstance(String key) {
        if (BasicUtil.isEmpty(key)) {
            key = "default";
        }
        MinioUtil util = instances.get(key);
        if (null == util) {
            util = new MinioUtil();
            MinioConfig config = MinioConfig.getInstance(key);
            util.config = config;
            util.client = MinioClient.builder()
                    .endpoint(config.ENDPOINT)
                    .credentials(config.ACCESS_KEY, config.ACCESS_SECRET)
                    .build();
            instances.put(key, util);
        }
        return util;
    }




    /**
     * 创建bucket
     *
     * @param bucket bucket名称
     */
    public void createBucket(String bucket) throws Exception {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    /**
     * 获取全部bucket
     *
     * https://docs.minio.io/cn/java-client-api-reference.html#listBuckets
     */
    public List<Bucket> getAllBuckets() throws Exception {
        return client.listBuckets();
    }

    /**
     * 根据bucket获取信息
     * @param bucket bucket名称
     */
    public Optional<Bucket> getBucket(String bucket) throws Exception {
        return client.listBuckets().stream().filter(b -> b.name().equals(bucket)).findFirst();
    }

    /**
     * 根据bucket删除信息
     * @param bucket bucket名称
     */
    public void removeBucket(String bucket) throws Exception {
        client.removeBucket(RemoveBucketArgs.builder().bucket(bucket).build());
    }

    /**
     * 分区上传文件
     * @param bucket bucket名称
     * @param obj 文件名称
     * @param stream 文件流
     * @param size 文件大小
     */
    public String putObject(String bucket, String obj, InputStream stream, Long size) throws Exception{
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucket)
                .object(obj)
                .stream(stream, size, config.PART_SIZE)
                .build();
        ObjectWriteResponse objectWriteResponse = client.putObject(putObjectArgs);
        return objectWriteResponse.object();
    }

    /**
     * 根据文件前置查询文件
     *
     * @param bucket bucket名称
     * @param prefix     前缀
     * @param recursive  是否递归查询
     * @return MinioItem 列表
     */
    public List<Item> getAllObjectsByPrefix(String bucket, String prefix, boolean recursive) throws Exception {
        List<Item> objectList = new ArrayList<>();
        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(prefix)
                .recursive(recursive)
                .build();

        Iterable<Result<Item>> objectsIterator = client
                .listObjects(listObjectsArgs);

        while (objectsIterator.iterator().hasNext()) {
            objectList.add(objectsIterator.iterator().next().get());
        }
        return objectList;
    }

    /**
     * 获取文件外链
     * 这里的 method 方法决定最后链接是什么请求获得
     *  expiry 决定这个链接多久失效
     * @param bucket bucket名称
     * @param obj 文件名称
     * @return url
     */
    public String getObjectURL(String bucket, String obj) throws Exception {
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .method(Method.GET)
                .expiry(7, TimeUnit.DAYS)
                .object(obj)
                .build();

        return client.getPresignedObjectUrl(args);
    }

    /**
     * 获取文件
     *
     * @param bucket bucket名称
     * @param obj 文件名称
     * @return 二进制流
     */
    public InputStream getObject(String bucket, String obj) throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(obj)
                .build();
        return client.getObject(getObjectArgs);
    }


    /**
     * 上传文件 base64
     * @param bucket bucket名称
     * @param obj 文件名称
     * @param base64 文件base64
     */
    public String putObject(String bucket, String obj, String base64) throws Exception{
        InputStream inputStream = new ByteArrayInputStream(base64.getBytes());
        // 进行解码
        BASE64Decoder base64Decoder = new BASE64Decoder();
        byte[] byt = new byte[0];
        try {
            byt = base64Decoder.decodeBuffer(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputStream = new ByteArrayInputStream(byt);
        putObject(bucket, obj, inputStream, Long.valueOf(byt.length));
        return obj;
    }

    /**
     * 上传文件
     * @param bucket bucket名称
     * @param obj 文件名称
     * @param file 文件
     * @throws Exception Exception
     */
    public String putObject( String bucket,String obj, File file) throws Exception{
        this.putObject(bucket, obj, new FileInputStream(file), file.length());
        return obj;
    }

    /**
     * 获取文件信息
     *
     * @param bucket bucket名称
     * @param obj 文件名称
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#statObject
     */
    public StatObjectResponse getObjectInfo(String bucket, String obj) throws Exception {
        StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                .bucket(bucket)
                .object(obj)
                .build();
        return client.statObject(statObjectArgs);
    }

    /**
     * 删除文件
     *
     * @param bucket bucket名称
     * @param obj 文件名称
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#removeObject
     */
    public void removeObject(String bucket, String obj) throws Exception {
        client.removeObject(RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(obj)
                .build());
    }


    /**
     * 获取直传链接
     * @param bucket bucket名称
     * @param obj 文件名称
     * @throws Exception Exception
     */
    public String presignedUrl( String bucket,String obj) throws Exception{
        GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucket)
                .object(obj)
                .expiry(7, TimeUnit.DAYS)
                .build();
        return client.getPresignedObjectUrl(getPresignedObjectUrlArgs);
    }

    /**
     * 合并文件
     * @param bucket bucket
     * @param chunks chunks
     * @param target target
     * @return String
     * @throws Exception Exception
     */
    public String composeObject(String bucket, List<String> chunks, String target) throws Exception{

        List<ComposeSource> sources = new ArrayList<>(chunks.size());
        for (String chunkName : chunks) {
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket(bucket)
                    .object(chunkName)
                    .build();
            sources.add(composeSource);
        }

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket)
                .sources(sources)
                .object(target)
                .build();
        ObjectWriteResponse objectWriteResponse = client.composeObject(composeObjectArgs);
        return objectWriteResponse.object();
    }

}


