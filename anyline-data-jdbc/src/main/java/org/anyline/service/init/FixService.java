package org.anyline.service.init;

import org.anyline.service.AnylineService;

public class FixService extends DefaultService{
    private String datasource;
    public AnylineService datasource(String datasource){
        //DataSourceHolder.setDataSource(datasource);
        return this;
    }
    public AnylineService datasource(){
        //DataSourceHolder.setDefaultDataSource();
        return this;
    }
    public AnylineService setDataSource(String datasource){
        //DataSourceHolder.setDataSource(datasource);
        return this;
    }
    public AnylineService setDataSource(String datasource, boolean auto){
        //DataSourceHolder.setDataSource(datasource, auto);
        return this;
    }
    public AnylineService setDefaultDataSource(){
        //DataSourceHolder.setDefaultDataSource();
        return this;
    }
    // 恢复切换前数据源
    public AnylineService recoverDataSource(){
        //DataSourceHolder.recoverDataSource();
        return this;
    }
    public String getDataSource(){
        return datasource;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }
}
