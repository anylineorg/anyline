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

    Having get(String having);
    String getRunText(String delimiter);
    void clear();
    boolean isEmpty();
    HavingStore clone();
}
