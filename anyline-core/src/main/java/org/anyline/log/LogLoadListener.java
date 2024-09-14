package org.anyline.log;

import org.anyline.annotation.Component;
import org.anyline.bean.LoadListener;
import org.anyline.util.ConfigTable;

import java.util.Map;

@Component("anyline.environment.listener.log")
public class LogLoadListener  implements LoadListener {
    @Override
    public void start() {
        Map<String, LogFactory> factors = ConfigTable.environment().getBeans(LogFactory.class);
        for(LogFactory item:factors.values()) {
            LogProxy.addFactory(item);
        }
    }

    @Override
    public void after() {
    }
}
