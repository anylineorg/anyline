package org.anyline.qq.map.util;

import org.anyline.util.AnylineConfig;

import java.util.Hashtable;

public class QQMapConfig extends AnylineConfig {
    public static String CONFIG_NAME = "anyline-qq-map.xml";
    private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();
}
