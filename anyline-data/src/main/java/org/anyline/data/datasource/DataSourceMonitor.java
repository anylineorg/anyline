package org.anyline.data.datasource;

public interface DataSourceMonitor {
    /**
     * 数据源是否正被使用<br/>
     * 注意 有些动态数源中可能连接多个数据源 需要遍历检测<br/>
     * 需要在项目中实现,在注销和覆盖数据源时会调用当前方法，如果数据源正在使用会根据情况抛出异常或忽略注销<br/>
     * @param datasource 数据源 一般会是一个连接池实例
     *                   如 HikariDataSource DruidDataSource
     *                   其他类型参考相应DataRuntime.getProcessor()的返回值 如ElasticSearchRuntime.client(org.elasticsearch.client.RestClient)
     * @return true:使用中
     */
    default boolean using(Object datasource, String current) {
        return false;
    }

    /**
     * 释放占用资源
     * @param datasource 数据源
     * @return 1:当前方法内已释放 不需要上层继续操作 0:当前方法内未释放 需要上层继续操作
     */
    default int release(Object datasource) {
        return 0;
    }

    /**
     * 数据源特征 默认不需要实现  由上层方法自动提取一般会通过 driver_产品名_url 合成
     * @param datasource 数据源
     * @return String 返回null由上层自动提取
     */
    default String feature(Object datasource) {
        return null;
    }

    /**
     * 同一个数据源是否保持同一个adapter<br/>
     * 针对同一个数据源对应多个不同类型数据库时才需要返回false
     * @param datasource 数据源
     * @return boolean false:每次操作都会检测一次adapter true:同一数据源使用同一个adapter
     */
    default boolean keepAdapter(Object datasource) {
        return true;
    }
}
