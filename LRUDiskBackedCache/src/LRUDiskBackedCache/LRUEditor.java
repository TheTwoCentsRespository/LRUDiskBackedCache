package LRUDiskBackedCache;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Queue;

class LRUEditor<K, V> implements Runnable {
	private final Queue<EditRequestClass<K, V>> workQueue;
	@SuppressWarnings("rawtypes")
	private static LRUEditor lruEditor = null;
	private boolean canJoin = false;
	private final ILRUCacheable<K, V> lruCache;

	private LRUEditor(ILRUCacheable<K, V> lruCache) {
		workQueue = new LinkedList<>();
		this.lruCache = lruCache;
	}

	private EditRequestClass<K, V> getWork() {
		synchronized (workQueue) {
			EditRequestClass<K, V> editRequest = null;
			editRequest = workQueue.poll();
			return editRequest;
		}
	}

	@SuppressWarnings("unchecked")
	public static <K, V> LRUEditor<K, V> getEditor(ILRUCacheable<K, V> lruCache) {
		if (lruEditor == null) {
			if (lruCache != null) {
				lruEditor = new LRUEditor<>(lruCache);
			}
		}
		return lruEditor;
	}

	public void add(EditRequestClass<K, V> editRequest) {
		synchronized (workQueue) {
			workQueue.add(editRequest);
			workQueue.notify();
		}
	}

	public void requestJoin() {
		canJoin = true;
	}

	public void run() {
		boolean canRun = !canJoin || workQueue.size() > 0;
		do {
			try {
				EditRequestClass<K, V> editRequest = getWork();
				if (editRequest == null) {
					synchronized (workQueue) {
						workQueue.wait();
						LoggingFunctions.DebugLog("LRUEditorThread: I am alive, canRun: "
								+ canRun);
					}
				} else {
					boolean synced = false;
					switch (editRequest.getRequestCode()) {
					case EditRequestClass.SYNC:
						FileOutputStream fos = new FileOutputStream(
								HelperFunctions.constructAbsolutePath(
										lruCache.getDiskCacheAbsolutePath(),
										editRequest.getKey().toString()));
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						oos.writeObject(editRequest.getValue());
						oos.close();
						fos.close();
						synced = true;
					case EditRequestClass.LOAD_DATA_FROM_DISK:
						if (!synced) {
							FileInputStream fis = new FileInputStream(
									HelperFunctions.constructAbsolutePath(
											lruCache.getDiskCacheAbsolutePath(),
											editRequest.getKey().toString()));
							ObjectInputStream ois = new ObjectInputStream(fis);
							@SuppressWarnings("unchecked")
							V value = (V) ois.readObject();
							editRequest.setValue(value);
							ois.close();
							fis.close();
						}
					case EditRequestClass.UPDATE_LRU_CACHE:
						lruCache.updateLRUCache(editRequest);
						break;

					}
				}
			} catch (Exception ex) {
				LoggingFunctions.ErrorLog(ex.getMessage());
			}
			canRun = !canJoin || workQueue.size() > 0;
		} while (canRun);
	}
}
