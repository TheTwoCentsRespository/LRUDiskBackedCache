package LRUDiskBackedCache;
class EditRequestClass<K, V> {
	public static final short LOAD_DATA_FROM_DISK = 1;
	public static final short UPDATE_LRU_CACHE = 2;
	public static final short SYNC = 3;
	
	private final short requestCode;
	private final K key;
	private V value;
	
	public EditRequestClass(short requestCode, K key) {
		this.requestCode = requestCode;
		this.key = key;
		this.value = null;
	}
	
	public EditRequestClass(short requestCode, K key, V value) {
		this.requestCode = requestCode;
		this.key = key;
		this.value = value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}
	
	public short getRequestCode() {
		return requestCode;
	}
	
	public K getKey() {
		return key;
	}
	
	public V getValue() {
		return value;
	}

}
