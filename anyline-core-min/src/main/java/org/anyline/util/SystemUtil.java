package org.anyline.util; 
 
import java.io.File; 
import java.lang.management.ManagementFactory; 
 
import org.anyline.entity.DataRow; 
 
import com.sun.management.OperatingSystemMXBean; 
 
public class SystemUtil { 
	private DataRow info = null; 
	public SystemUtil(){ 
		info = new DataRow(); 
		int kb = 1024; 
		if(System.getProperty("os.name").toLowerCase().contains("win")){ 
			OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean(); 
			long physicalFree = osmxb.getFreePhysicalMemorySize() / kb; 
			long physicalTotal = osmxb.getTotalPhysicalMemorySize() / kb; 
			info.put("MemTotal", physicalTotal);	//系统总内存 
			info.put("MemFree", physicalFree);		//系统空闲内存 
		}else{ 
			//String txt = FileUtil.readFile(new File("/proc/meminfo")).toString(); 
			String txt = FileUtil.read(new File("D:\\info.txt")).toString(); 
			String[] items = txt.split("\n"); 
			for(String item:items){ 
				String[] kv = item.split(":"); 
				if(kv.length !=2){ 
					continue; 
				} 
				String k = kv[0].trim(); 
				String v = kv[1].toLowerCase().replace("kb", "").trim(); 
				info.put(k, v); 
			} 
		} 
		// 虚拟机级内存情况查询 
		long vmFree = 0; 
		long vmTotal = 0; 
		long vmMax = 0; 
		Runtime rt = Runtime.getRuntime(); 
		vmTotal = rt.totalMemory() / kb;	//JVM总内存(已分配给JVM内存) 
		vmFree = rt.freeMemory() / kb;		//JVM空闲内存(已分配给JVM但还未使用内存) 
		vmMax = rt.maxMemory() / kb;		//JVM最大可分配内存 
		info.put("vmTotal", vmTotal); 
		info.put("vmFree", vmFree); 
		info.put("vmMax", vmMax); 
	} 
	public DataRow memoryInfo(){ 
		DataRow row = new DataRow(); 
		String txt = FileUtil.read(new File("/proc/meminfo")).toString(); 
		String[] items = txt.split("\n"); 
		for(String item:items){ 
			String[] kv = item.split(":"); 
			if(kv.length !=2){ 
				continue; 
			} 
			String k = kv[0].trim(); 
			String v = kv[1].toLowerCase().replace("kb", "").trim(); 
			row.put(k, v); 
		} 
		return row; 
	} 
	/** 
	 * 物理内存合计 
	 * @return return
	 */ 
	public int getMemoryTotal(){ 
		return info.getInt("MemTotal"); 
	} 
	/** 
	 * 可用内存 
	 * @return return
	 */ 
	public int getMemoryFree(){ 
		return info.getInt("MemFree"); 
	} 
	/** 
	 * 已用内存 
	 * @return return
	 */ 
	public int getMemoryUse(){ 
		return getMemoryTotal() - getMemoryFree(); 
	} 
	/** 
	 * JVM总内存(操作系统已分配给JVM内存) 
	 * @return return
	 */ 
	public int getVMTotal(){ 
		return info.getInt("vmTotal"); 
	} 
	/** 
	 * JVM空闲内存(操作系统已分配给JVM但还未使用内存) 
	 * @return return
	 */ 
	public int getVMFree(){ 
		return info.getInt("vmFree"); 
	} 
	/** 
	 * JVM已用内存 
	 * @return return
	 */ 
	public int getVMUse(){ 
		return getVMTotal() - getVMFree(); 
	} 
	/** 
	 * JVM最大可用内存(操作系统最大可分配给JVM内存) 
	 * @return return
	 */ 
	public int getVMMax(){ 
		return info.getInt("vmMax"); 
	} 
	 
	public static void main(String[] args) { 
		while(true){ 
		 info(); 
		 try { 
			Thread.sleep(2000); 
		} catch (Exception e) { 
			// TODO Auto-generated catch block 
			e.printStackTrace(); 
		} 
		} 
	} 
	public static void info(){ 
 
		// 虚拟机级内存情况查询 
		long vmFree = 0; 
		long vmUse = 0; 
		long vmTotal = 0; 
		long vmMax = 0; 
		int byteToMb = 1024 * 1024; 
		Runtime rt = Runtime.getRuntime(); 
		vmTotal = rt.totalMemory() / byteToMb; 
		vmFree = rt.freeMemory() / byteToMb; 
		vmMax = rt.maxMemory() / byteToMb; 
		vmUse = vmTotal - vmFree; 
		System.out.println("JVM内存已用的空间为：" + vmUse + " MB"); 
		System.out.println("JVM内存的空闲空间为：" + vmFree + " MB"); 
		System.out.println("JVM总内存空间为：" + vmTotal + " MB"); 
		System.out.println("JVM总内存空间为：" + vmMax + " MB"); 
  
		System.out.println("-----------------------------------------------"); 
		// 操作系统级内存情况查询 
		OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean(); 
		String os = System.getProperty("os.name"); 
		long physicalFree = osmxb.getFreePhysicalMemorySize() / byteToMb; 
		long physicalTotal = osmxb.getTotalPhysicalMemorySize() / byteToMb; 
		long physicalUse = physicalTotal - physicalFree; 
		System.out.println("操作系统的版本：" + os); 
		System.out.println("操作系统物理内存已用的空间为：" + physicalFree + " MB"); 
		System.out.println("操作系统物理内存的空闲空间为：" + physicalUse + " MB"); 
		System.out.println("操作系统总物理内存：" + physicalTotal + " MB"); 
		 
		// 获得线程总数 
		ThreadGroup parentThread; 
		int totalThread = 0; 
		for (parentThread = Thread.currentThread().getThreadGroup(); parentThread 
				.getParent() != null; parentThread = parentThread.getParent()) { 
			totalThread = parentThread.activeCount(); 
		} 
		System.out.println("获得线程总数:" + totalThread); 
		System.out.println("======================================"); 
		} 
} 
