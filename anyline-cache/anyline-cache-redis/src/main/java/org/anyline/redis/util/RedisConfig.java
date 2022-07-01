package org.anyline.redis.util;

import org.anyline.util.AnylineConfig;

import java.io.File;
import java.util.Hashtable;

public class RedisConfig  extends AnylineConfig {
    private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();
    public static String CONFIG_NAME = "anyline-redis.xml";
}
