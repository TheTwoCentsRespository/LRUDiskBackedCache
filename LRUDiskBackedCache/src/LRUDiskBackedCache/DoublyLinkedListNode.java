package LRUDiskBackedCache;
class DoublyLinkedListNode<K, V> {
	private final K key;
	private V data;
	private DoublyLinkedListNode<K, V> previousNode;
	private DoublyLinkedListNode<K, V> nextNode;

	public DoublyLinkedListNode(K key, V data) {
		this.key = key;
		this.data = data;
		previousNode = null;
		nextNode = null;
	}

	public void setNext(DoublyLinkedListNode<K, V> next) {
		nextNode = next;
	}

	public void setPrevious(DoublyLinkedListNode<K, V> previous) {
		previousNode = previous;
	}

	public DoublyLinkedListNode<K, V> getNext() {
		return nextNode;
	}

	public DoublyLinkedListNode<K, V> getPrevious() {
		return previousNode;
	}

	public K getKey() {
		return key;
	}

	public V getData() {
		return data;
	}

	public void setData(V data) {
		this.data = data;
	}
}
