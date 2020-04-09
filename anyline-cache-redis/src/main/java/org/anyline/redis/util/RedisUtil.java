package org.anyline.redis.util; 
 
import java.util.Hashtable; 
 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
public class RedisUtil { 
	private static final Logger log = LoggerFactory.getLogger(RedisUtil.class); 
	private static Hashtable<String,RedisUtil> instances = new Hashtable<String,RedisUtil>(); 
	private RedisConfig config = null; 
 
} 
