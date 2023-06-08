package org.anyline.entity.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public interface Table  extends Serializable {
    String getName();


    static <T extends Table> List<String> names(LinkedHashMap<String, T> tables) {
        List<String> names = new ArrayList<>();
        for (T table : tables.values()) {
            names.add(table.getName());
        }
        return names;
    }
}
