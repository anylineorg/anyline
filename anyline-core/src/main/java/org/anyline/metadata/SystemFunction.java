package org.anyline.metadata;

import org.anyline.metadata.type.DatabaseType;

public interface SystemFunction {
    enum META{
        CONCAT("拼接String", 9);
        META(String title, int cnt){
            this.title = title;
            this.valueCount = cnt;
        }
        private final String title;
        private final int valueCount;
        /**
         * 支持参数数量 0:没有参数 IS NULL/IS EMPTY 1:一个参数 2:两个参数 BETWEEN 9:多个参数  IN()
         * @return boolean
         */
        public int valueCount() {
            return valueCount;
        }
        public String title() {
            return title;
        }
    }
    DatabaseType database();
    META meta();
    String formula(boolean placeholder, boolean unicode, Object ... args);
    default String formula(Object ... args) {
        return formula(false, false, args);
    }
    default String formula(boolean placeholder, Object ... args) {
        return formula(placeholder, false, args);
    }
}
