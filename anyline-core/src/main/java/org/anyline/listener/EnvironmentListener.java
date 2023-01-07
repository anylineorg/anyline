package org.anyline.listener;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class EnvironmentListener implements EnvironmentAware {
    private Logger log = LoggerFactory.getLogger(EnvironmentListener.class);

    @Override
    public void setEnvironment(Environment environment) {
        Field[] fields = ConfigTable.class.getDeclaredFields();
        for(Field field:fields){
            String name = field.getName();
            String value = BeanUtil.value("anyline", environment, "." + name);
            if(BasicUtil.isNotEmpty(value)) {
                BeanUtil.setFieldValue(null, field, value);
            }
        }
    }
}
