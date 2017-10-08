package LRUDiskBackedCache;

public class LRURetrivedDataClass<V> {
    private final V value;
    private final short ioStatus;
    
    public LRURetrivedDataClass(V value, short ioStatus) {
		this.value = value;
		this.ioStatus = ioStatus;
	}
    
    public V getValue() {
    	return value;
    }
    
    public short getIOStatus() {
    	return ioStatus;
    }
}
