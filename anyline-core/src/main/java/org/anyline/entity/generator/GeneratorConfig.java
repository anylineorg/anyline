package org.anyline.entity.generator;

import java.util.HashMap;
import java.util.Map;

public class GeneratorConfig {
    public static PrimaryGenerator generator = null;
    private static Map<String, PrimaryGenerator> generators = new HashMap<>();



    /**
     * 设置全部表的主键生成器
     * @param generator 生成器
     */
    public static void put(PrimaryGenerator generator){
        GeneratorConfig.generator = generator;
    }
    public static void put(Map<String, PrimaryGenerator> generators){
        GeneratorConfig.generators = generators;
    }
    public static PrimaryGenerator get(){
        return generator;
    }
    public void set(PrimaryGenerator generator){
        GeneratorConfig.generator = generator;
    }
    public void set(Map<String, PrimaryGenerator> generators){
        GeneratorConfig.generators = generators;
    }
    /**
     * 设置单个表的主键生成器
     * @param table 表
     * @param generator 生成器
     */
    public static void put(String table, PrimaryGenerator generator){
        generators.put(table.toUpperCase(), generator);
    }
    public void set(String table, PrimaryGenerator generator){
        generators.put(table.toUpperCase(), generator);
    }
    public static PrimaryGenerator get(String table){
        return generators.get(table.toUpperCase());
    }
}
