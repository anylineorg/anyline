package org.anyline.mimio.util;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.anyline.util.BasicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
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
     * @throws Exception Exception
     */
    public void createBucket(String bucket) throws Exception {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    /**
     * 获取全部bucket
     * @return List
     * https://docs.minio.io/cn/java-client-api-reference.html#listBuckets
     * @throws Exception Exception
     */
    public List<Bucket> getAllBuckets() throws Exception {
        return client.listBuckets();
    }

    /**
     * 根据bucket获取信息
     * @param bucket bucket名称
     * @return Optional
     * @throws Exception Exception
     */
    public Optional<Bucket> getBucket(String bucket) throws Exception {
        return client.listBuckets().stream().filter(b -> b.name().equals(bucket)).findFirst();
    }

    /**
     * 根据bucket删除信息
     * @param bucket bucket名称
     * @throws Exception Exception
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
     * @return String
     * @throws Exception Exception
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

    public String putObject(String obj, InputStream stream, Long size) throws Exception{
        return putObject(config.BUCKET, obj, stream, size);
    }

    /**
     * 根据文件前置查询文件
     *
     * @param bucket bucket名称
     * @param prefix     前缀
     * @param recursive  是否递归查询
     * @return MinioItem 列表
     * @throws Exception Exception
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

    public List<Item> getAllObjectsByPrefix(String prefix, boolean recursive) throws Exception {
        return getAllObjectsByPrefix(config.BUCKET, prefix, recursive);
    }
    /**
     * 获取文件外链
     * getObjectURL("alcdn","a/b.txt")
     * 返回一个url
     * @param bucket bucket名称
     * @param obj 文件名称
     * @return url
     * @throws Exception Exception
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

    public String getObjectURL(String obj) throws Exception {
        return getObjectURL(config.BUCKET, obj);
    }
    /**
     * 获取文件
     *
     * @param bucket bucket名称
     * @param obj 文件名称
     * @return InputStream
     * @throws Exception Exception
     */
    public InputStream getObject(String bucket, String obj) throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(obj)
                .build();
        return client.getObject(getObjectArgs);
    }

    public InputStream getObject(String obj) throws Exception {
        return getObject(config.BUCKET, obj);
    }


    /**
     * 上传文件 base64
     * @param bucket bucket名称
     * @param obj 文件名称
     * @param base64 文件base64
     * @return String
     * @throws Exception Exception
     */
    public String putObject(String bucket, String obj, String base64) throws Exception{
        InputStream inputStream = new ByteArrayInputStream(base64.getBytes());
        // 进行解码
        byte[] byt = new byte[0];
        try {
            byt = Base64.getDecoder().decode(base64);
        } catch (Exception e) {
            e.printStackTrace();
        }
        inputStream = new ByteArrayInputStream(byt);
        putObject(bucket, obj, inputStream, Long.valueOf(byt.length));
        return obj;
    }

    public String putObject(String obj, String base64) throws Exception{
        return putObject(config.BUCKET, obj, base64);
    }
    /**
     * 上传文件
     * utObject("alcdn","a/b.txt",new File("D:\\a.txt"));
     * @param bucket bucket名称
     * @param obj 文件名称
     * @param file 文件
     * @return String
     * @throws Exception Exception
     */
    public String putObject(String bucket,String obj, File file) throws Exception{
        this.putObject(bucket, obj, new FileInputStream(file), file.length());
        return obj;
    }

    public String putObject(String obj, File file) throws Exception{
        return putObject(config.BUCKET, obj, file);
    }

    /**
     * 获取文件信息
     *
     * @param bucket bucket名称
     * @param obj 文件名称
     * @return StatObjectResponse
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#statObject
     */
    public StatObjectResponse getObjectInfo(String bucket, String obj) throws Exception {
        StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                .bucket(bucket)
                .object(obj)
                .build();
        return client.statObject(statObjectArgs);
    }

    public StatObjectResponse getObjectInfo(String obj) throws Exception {
        return getObjectInfo(config.BUCKET, obj);
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

    public void removeObject(String obj) throws Exception {
        removeObject(config.BUCKET, obj);
    }

    /**
     * 获取直传链接
     * @param bucket bucket名称
     * @param obj 文件名称
     * @throws Exception Exception
     * @return String
     * @throws Exception Exception
     */
    public String presignedUrl(String bucket,String obj) throws Exception{
        GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucket)
                .object(obj)
                .expiry(7, TimeUnit.DAYS)
                .build();
        return client.getPresignedObjectUrl(getPresignedObjectUrlArgs);
    }

    public String presignedUrl(String obj) throws Exception{
        return presignedUrl(config.BUCKET, obj);
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

    public String composeObject(List<String> chunks, String target) throws Exception{
        return composeObject(config.BUCKET, chunks, target);
    }

}


