import LRUDiskBackedCache.LRUDiskBackedCache;

public class Test {
	public static void main(String[] args) throws Exception {
		LRUDiskBackedCache<String, String> lruDiskBackedCache = LRUDiskBackedCache
				.getLRUDiskBackedCache(10, "/work/cache");

		int testingTime = 5 * 60 * 1000; // 5 minutes
		int timeElapsed = 0;
		int timeStep = 1000; // Seconds

		System.out.println("Put: " + lruDiskBackedCache.put("00001", "Hello World One"));
		System.out.println("Put: " + lruDiskBackedCache.put("00002", "Hello World Two"));
		System.out.println("Put: " + lruDiskBackedCache.put("00003", "Hello World Three"));
		System.out.println("Put: " + lruDiskBackedCache.put("00004", "Hello World Four"));
		System.out.println("Put: " + lruDiskBackedCache.put("00005", "Hello World Five"));

		Thread.sleep(timeStep);
		lruDiskBackedCache.printCache();
		
		System.out.println("Put: " + lruDiskBackedCache.put("00006", "Hello World Six"));
		System.out.println("Put: " + lruDiskBackedCache.put("00007", "Hello World Seven"));
		System.out.println("Put: " + lruDiskBackedCache.put("00008", "Hello World Eight"));
		System.out.println("Put: " + lruDiskBackedCache.put("00009", "Hello World Nine"));
		System.out.println("Put: " + lruDiskBackedCache.put("000010", "Hello World Ten"));
		
		Thread.sleep(timeStep);
		lruDiskBackedCache.printCache();
		
		System.out.println("Put: " + lruDiskBackedCache.put("000011", "Hello World Eleven"));
		System.out.println("Put: " + lruDiskBackedCache.put("000012", "Hello World Twelve"));
		
		System.out.println("Puts done");
		
		Thread.sleep(timeStep);
		lruDiskBackedCache.printCache();
		
		System.out.println("Get: " + lruDiskBackedCache.get("00001").getIOStatus());
		System.out.println("Get: " + lruDiskBackedCache.get("00002").getIOStatus());
		System.out.println("Get: " + lruDiskBackedCache.get("00003").getIOStatus());
		
		Thread.sleep(timeStep);
		lruDiskBackedCache.printCache();
		
		System.out.println("Get: " + lruDiskBackedCache.get("0000333").getIOStatus());
		System.out.println("Put: " + lruDiskBackedCache.put(null, "Test"));
		System.out.println("Put: " + lruDiskBackedCache.put("Test", null));

		lruDiskBackedCache.dispose();
	}
}
