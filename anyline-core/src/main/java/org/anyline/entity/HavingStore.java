package org.anyline.entity;

import java.util.List;

public interface HavingStore {
    List<Having> gets();
    void add(Having having) ;
    /**
     * COUNT(*) > 10
     * having("CD, NM");
     * @param having having
     */
    void add(String having) ;
    String getRunText();
    void clear();
    boolean isEmpty();
    HavingStore clone();
}
