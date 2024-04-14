package org.anyline.data.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface DataSourceLoader {
    Logger log = LoggerFactory.getLogger(DataSourceLoader.class);
    List<String> load();
    DataSourceHolder holder();
}
