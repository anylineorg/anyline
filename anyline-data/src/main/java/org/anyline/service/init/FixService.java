package org.anyline.service.init;

import org.anyline.service.AnylineService;

public class FixService extends DefaultService{
    private String datasource;
    public AnylineService datasource(String datasource){
        //不可切换
        //ClientHolder.setDataSource(datasource);
        return this;
    }
    public AnylineService datasource(){
        //不可切换
        //ClientHolder.setDefaultDataSource();
        return this;
    }
    public AnylineService setDataSource(String datasource){
        //不可切换
        //ClientHolder.setDataSource(datasource);
        return this;
    }
    public AnylineService setDataSource(String datasource, boolean auto){
        //不可切换
        //ClientHolder.setDataSource(datasource, auto);
        return this;
    }
    public AnylineService setDefaultDataSource(){
        //不可切换
        //ClientHolder.setDefaultDataSource();
        return this;
    }
    // 恢复切换前数据源
    public AnylineService recoverDataSource(){
        //不可切换
        //ClientHolder.recoverDataSource();
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
