package org.anyline.entity.generator.init;

import org.anyline.entity.data.DatabaseType;
import org.anyline.entity.generator.PrimaryGenerator;

import java.util.List;

/**
 * 不生成主键,可以针对某个表覆盖全局配置
 */
public class DisableGenerator implements PrimaryGenerator {
    @Override
    public boolean create(Object entity, DatabaseType type, String table, List<String> columns, List<String> inserts, String other) {
       return false;
    }
}
