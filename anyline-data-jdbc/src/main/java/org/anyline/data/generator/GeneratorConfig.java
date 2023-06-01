package org.anyline.data.generator;

import java.util.HashMap;
import java.util.Map;

public class GeneratorConfig {
    private static Map<String, PrimaryGenerator> generators = new HashMap<>();
    public static void put(String table, PrimaryGenerator generator){
        generators.put(table.toUpperCase(), generator);
    }
    public static PrimaryGenerator get(String table){
        return generators.get(table.toUpperCase());
    }
}
