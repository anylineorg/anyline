package org.anyline.data.listener;

import org.springframework.context.ApplicationContext;

import java.util.List;

public interface DatasourceLoader {
    List<String> load(ApplicationContext context);
}
