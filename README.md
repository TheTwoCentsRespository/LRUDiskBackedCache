# LRUDiskBackedCache
Java implementation of a Disk Backed LRU Cache

A disk backed LRU Cache, implemented using Java. As of now, it can be used as a library which will be tightly coupled with the underlying code. In order to make this library scalable as a service, a wrapper can be made using the library which exposes the features as a service over TCP / IP. The Cache can be used in a multi-threaded environment and is heavily read-optimized assuming majority of the payload / queries will be read-intensive.

# Overall notes about the library & initial assumptions

1. O(1) time bound operations. With a bit of lazy loading :)

2. Use of generics for key (K) & value (V extends Serializable). Serializable for committing the values to disk

3. Internally uses a ConcurrentHashMap and custom implementation of DoublyLinkedList. The hashmap stores the nodes of the linked list hence a custom implementation had to be written.

4. All writes to the data structures are performed asynchronously using a dedicated work queue and a separate editor thread. This also excludes the possibilities of race conditions arising out of manipulating 2 data structures (a hash map and a doubly linked list) at the same time.

5. When there is a cache miss, item is lazily loaded from the disk and corresponding status is returned to the caller, so that the caller can distinguish between the fact that the key-value pair is absent or will be loaded in the future. The caller can decide to try again in a while, in case the item exists in disk (lazy load). This has been done so as to reduce the time taken by a thread to perform disk I/O in a get operation. This can be modified to return the value read directly from the disk - which can be attuned as per specific requirements from the cache and the load handling capabilities required.

6. ConcurrentHashMap has been used for minimizing dirty reads when concurrent updates & reads to the map takes place

7. A journal is maintained where each keys are logged based on keys added, accessed or evicted (represented by a number and the key itself)

8. This is originally an Eclipse Project

9. Use TestClass.java to test the library. Some boilerplate code has been provided.
  
# API Usage

public static <K, V extends Serializable> LRUDiskBackedCache<K, V> getLRUDiskBackedCache(int capacity, String diskCacheDirectoryPath);

Method returns a singleton LRUDiskBackedCache instance. Need to provide capacity of the cache, and the directory where the files would be created.

O(1) time complexity and O(j+k) space complexity. Where j is the number of keys. Not exactly j+k, since memory is allocated up-front to reserve space for references and not the actual data themselves. Also k <= n, where n is a much larger key-value store that is saved on the disk and only a subset resides in the cache. Also  When the cache is full, it will reserve Theta(2k) space but only accounting for references since both Linked List and Hash Map will have a copy of the data.

public LRURetrivedDataClass<V> get(K key);
  
Returns instance of LRURetrivedDataClass which contains the retrieved object and it's status (ERROR, KEY_FOUND, KEY_NOT_FOUND, LAZY_LOAD_KEY). O(1) space and time. In reality we need to account for the lazy load as well, which individually is O(1) time.

public short put(K key, V value);

Retuns a status indicating ERROR, LAZY_LOAD_KEY, KEY_NOT_FOUND. It updates the value if key was already present. O(1) space and time.

ERROR = > There was an internal error processing the request
KEY_FOUND => The key exists in cache
KEY_NOT_FOUND => The key does not exist in cache / disk (based on method call get or put respectively)
LAZY_LOAD_KEY => The key is a valid key but is absent in the cache and will be lazily loaded

Neither null keys nor null values are supported.

# Known improvement areas

1. More granular, faster method (maybe some C / C++ & JNI - OS specific implementation) to check if a file exists on disk. Empirical measurement of performance might yield a better method to use. 

2. For a large number of files, management of the files may become difficult if stored in one directory, hence a logic can be implemented which can distribute files across directories.

3. The disk backed journal can be used to replay the logs for the cache to warm-up to its backed-up state on startup. 

4. Journal can be periodically compressed.

5. Exception / Error handling can be improved.

# Initial System Considerations for load testing to plan sizing
1. A quad core CPU to start with, with 2 threads per core so a total of 8 threads. Can be increased based on payload / observations.
2. RAM / Memory requirements can be computed as per the size of one key value pair and amount to be cached at a given time.
3. Use disks in RAID 1 - gives mirroring capabilities which is robust & provides capabilities to handle twice the amount reads.
