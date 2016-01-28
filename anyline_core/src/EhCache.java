import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;


public class EhCache {
	public static void main(String args[]) throws Exception {
		CacheManager manager = CacheManager.create();//.newInstance("E:\\develop\\workspace\\myeclipse2014\\anyline_core\\config\\ehcache.xml");
		manager.addCache("home");  
		Cache test = manager.getCache("home");  
		test.put(new Element("key1", "value1"));
		System.out.println(test.get("key1").getObjectValue());
		manager.shutdown();
	}
}
