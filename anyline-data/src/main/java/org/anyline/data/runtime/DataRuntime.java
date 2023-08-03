package org.anyline.data.runtime;

import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.DriverAdapter;

public interface DataRuntime {
    AnylineDao getDao() ;

     void setDao(AnylineDao dao) ;
    String getFeature() ;

    void setFeature(String feature) ;

    String getVersion();

    void setVersion(String version) ;
    String getKey() ;
    void setKey(String key) ;
    Object getClient() ;

    void setClient(Object client);

    DriverAdapter getAdapter() ;
    String datasource();
    void setAdapter(DriverAdapter adapter);
}
