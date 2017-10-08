package LRUDiskBackedCache;

interface ILRUCacheable<K, V> {
	public void updateLRUCache(EditRequestClass<K, V> editRequest);
	public String getDiskCacheAbsolutePath();

}
