package org.anyline.entity;

import java.util.ArrayList;
import java.util.List;

public class DefaultHavingStore implements HavingStore{

    private List<Having> list = new ArrayList<>();
    @Override
    public List<Having> gets() {
        return list;
    }

    @Override
    public void add(Having having) {
        list.add(having);
    }

    @Override
    public void add(String having) {
        list.add(new Having(having));
    }

    @Override
    public String getRunText() {
        StringBuilder builder = new StringBuilder();
        if(!isEmpty()){
            builder.append("HAVING ");
        }
        boolean first = true;
        for(Having having:list){
            if(!first){
                builder.append(" AND ");
            }
            first = false;
            builder.append(having.text());
        }
        return builder.toString();
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public HavingStore clone() {
        HavingStore store = new DefaultHavingStore();
        for(Having having:list){
            store.add(having);
        }
        return store;
    }
}
