package LRUDiskBackedCache;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class LRUDiskBackedCache<K, V extends Serializable> implements
		ILRUCacheable<K, V> {

	public static final short ERROR = 0;
	public static final short KEY_FOUND = 1;
	public static final short KEY_NOT_FOUND = 2;
	public static final short LAZY_LOAD_KEY = 3;
	private static final String journalFileName = "lrudiskcache.journal";
	private static final short JOURNAL_ADD = 0;
	private static final short JOURNAL_ACCESS = 1;
	private static final short JOURNAL_EVICT = 2;

	private final ConcurrentHashMap<K, DoublyLinkedListNode<K, V>> keyValueHashMap;
	private final DoublyLinkedList<K, V> valueLinkedList;
	private final String diskCacheDirectoryPath;
	private final LRUEditor<K, V> lruEditor;
	private Thread lruEditorThread;
	private static FileWriter journalWriter;

	@SuppressWarnings("rawtypes")
	private static LRUDiskBackedCache lruDiskBackedCache = null;

	private LRUDiskBackedCache(int capacity, String diskCacheDirectoryPath) {
		keyValueHashMap = new ConcurrentHashMap<>(capacity);
		valueLinkedList = new DoublyLinkedList<>(capacity);
		this.diskCacheDirectoryPath = diskCacheDirectoryPath;
		lruEditor = LRUEditor.getEditor(this);
		lruEditorThread = new Thread(lruEditor);
	}

	private synchronized boolean RequestEdit(EditRequestClass<K, V> editRequest) {
		boolean rValue = false;
		try {
			lruEditor.add(editRequest);
			rValue = true;
		} catch (Exception ex) {
			LoggingFunctions.ErrorLog(ex.getMessage());
		}
		return rValue;
	}

	private void journal(short operation, K key) {
		try {
			if (journalWriter == null) {
				String journalAbsolutePath = HelperFunctions
						.constructAbsolutePath(diskCacheDirectoryPath,
								journalFileName);
				journalWriter = new FileWriter(journalAbsolutePath, true);
			}
			StringBuilder line = new StringBuilder();
			line.append(operation).append(",").append(key.toString())
					.append("\n");
			journalWriter.write(line.toString());
			journalWriter.flush();
		} catch (Exception ex) {
			LoggingFunctions.ErrorLog(ex.getMessage());
			if (journalWriter != null) {
				try {
					journalWriter.close();
				} catch (Exception e) {
					LoggingFunctions.ErrorLog(e.getMessage());
				}
			}
			journalWriter = null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <K, V extends Serializable> LRUDiskBackedCache<K, V> getLRUDiskBackedCache(
			int capacity, String diskCacheDirectoryPath) {
		if (lruDiskBackedCache == null) {
			File file = new File(diskCacheDirectoryPath);
			if (file.isDirectory() && capacity > 0) {
				lruDiskBackedCache = new LRUDiskBackedCache<>(capacity,
						diskCacheDirectoryPath);
				lruDiskBackedCache.lruEditorThread.start();
			}
		}
		return lruDiskBackedCache;
	}

	public LRURetrivedDataClass<V> get(K key) {
		LoggingFunctions.DebugLog("get() called");
		DoublyLinkedListNode<K, V> dllNode = keyValueHashMap.get(key);

		short status = ERROR;

		if (dllNode == null) {
			String keyAbsolutePath = HelperFunctions.constructAbsolutePath(
					diskCacheDirectoryPath, key.toString());
			try {
				if (HelperFunctions.existsOnDisk(keyAbsolutePath)) {
					EditRequestClass<K, V> editRequest = new EditRequestClass<>(
							EditRequestClass.LOAD_DATA_FROM_DISK, key, null);
					status = RequestEdit(editRequest) ? LAZY_LOAD_KEY : ERROR;
				}
			} catch (Exception ex) {
				LoggingFunctions.ErrorLog(ex.getMessage());
			}
		} else {
			EditRequestClass<K, V> editRequest = new EditRequestClass<>(
					EditRequestClass.UPDATE_LRU_CACHE, key, dllNode.getData());
			status = RequestEdit(editRequest) ? KEY_FOUND : status;
		}

		return new LRURetrivedDataClass<>(dllNode != null ? dllNode.getData()
				: null, status);
	}

	public short put(K key, V value) {
		LoggingFunctions.DebugLog("put() called");

		short status = ERROR;

		if (key != null && value != null) {
			String keyAbsolutePath = HelperFunctions.constructAbsolutePath(
					diskCacheDirectoryPath, key.toString());
			try {
				status = HelperFunctions.existsOnDisk(keyAbsolutePath) ? LAZY_LOAD_KEY
						: KEY_NOT_FOUND;

			} catch (Exception ex) {
				LoggingFunctions.ErrorLog(ex.getMessage());
			}
			if (status != ERROR) {
				EditRequestClass<K, V> editRequest = new EditRequestClass<>(
						EditRequestClass.SYNC, key, value);
				status = RequestEdit(editRequest) ? status : ERROR;
			}
		}
		return status;
	}

	@Override
	public void updateLRUCache(EditRequestClass<K, V> editRequest) {
		short operation1 = -1;
		short operation2 = -1;
		K evictedKey = null;
		DoublyLinkedListNode<K, V> dllNode = keyValueHashMap.get(editRequest
				.getKey());
		if (dllNode == null) {
			operation1 = JOURNAL_ADD;
			AddedEvictedNodePair<K, V> addedEvictedNodePair = valueLinkedList
					.add(editRequest.getKey(), editRequest.getValue());
			if (addedEvictedNodePair.getEvictedNode() != null) {
				evictedKey = addedEvictedNodePair.getEvictedNode().getKey();
				keyValueHashMap.remove(evictedKey);
				operation2 = JOURNAL_EVICT;
			}
			dllNode = addedEvictedNodePair.getAddedNode();
		} else {
			operation1 = JOURNAL_ACCESS;
			dllNode.setData(editRequest.getValue());
			valueLinkedList.touch(dllNode);
		}
		keyValueHashMap.put(editRequest.getKey(), dllNode);
		journal(operation1, editRequest.getKey());
		if (operation2 != -1) {
			journal(operation2, evictedKey);
		}
	}

	@Override
	public String getDiskCacheAbsolutePath() {
		return diskCacheDirectoryPath;
	}

	public void dispose() {
		try {
			if (journalWriter != null) {
				journalWriter.close();
			}
			lruEditor.requestJoin();
			lruEditorThread.join();
		} catch (Exception ex) {
			LoggingFunctions.ErrorLog(ex.getMessage());
		}
	}

	/*
	 * NOTE !! For debugging purposes only, call has to ensure calling in a
	 * thread safe way
	 */
	public void printCache() {
		if (keyValueHashMap == null) {
			System.out.println("Concurrent HashMap is null !");
		} else {
			System.out.println("Printing Concurrent HashMap Contents:-");
			for (Map.Entry<K, DoublyLinkedListNode<K, V>> entry : keyValueHashMap
					.entrySet()) {
				String key = entry.getKey().toString();
				String value = entry.getValue().getData().toString();
				System.out.println("Key: " + key + " | Value: " + value);
			}
		}
		if (valueLinkedList == null) {
			System.out.println("Doubly Linked List is null !");
		} else {
			valueLinkedList.printDoublyLinkedList();
		}
	}

}