package LRUDiskBackedCache;

class DoublyLinkedList<K, V> {

	private DoublyLinkedListNode<K, V> head;
	private DoublyLinkedListNode<K, V> tail;
	private int capacity;
	private int count;

	private DoublyLinkedListNode<K, V> ensureCapacity() {
		if (count == capacity) {
			if (tail != null) {
				DoublyLinkedListNode<K, V> evictedNode = tail;
				DoublyLinkedListNode<K, V> previousNode = tail.getPrevious();
				previousNode.setNext(null);
				tail = previousNode;
				return evictedNode;
			}
		}
		return null;
	}

	public DoublyLinkedList(int capacity) {
		head = tail = null;
		this.capacity = capacity;
		count = 0;
	}

	public AddedEvictedNodePair<K, V> add(K key, V data) {
		
		LoggingFunctions.DebugLog("Adding key " + key.toString() + " with value: " + data.toString() + " to Doubly Linked List");
		
		DoublyLinkedListNode<K, V> evictedNode = null;
		DoublyLinkedListNode<K, V> newNode = new DoublyLinkedListNode<>(key,
				data);
		if (head == null) {
			head = newNode;
			tail = head;
		} else {
			evictedNode = ensureCapacity();
			newNode.setNext(head);
			head.setPrevious(newNode);
			head = newNode;
		}
		if (evictedNode == null) {
			count++;
		}
		return new AddedEvictedNodePair<>(newNode, evictedNode);
	}

	public void touch(DoublyLinkedListNode<K, V> node) {
		if (node == null) {
			return;
		}
		if (node == head) {
			return;
		}
		DoublyLinkedListNode<K, V> previousNode = node.getPrevious();
		DoublyLinkedListNode<K, V> nextNode = node.getNext();
		if (previousNode != null) {
			previousNode.setNext(nextNode);
		}
		if (nextNode != null) {
			nextNode.setPrevious(previousNode);
		}
		node.setNext(head);
		head.setPrevious(node);
	}

	/*
	 * NOTE !! For debugging purposes only, call has to ensure calling in a
	 * thread safe way
	 */
	public void printDoublyLinkedList() {
		if (head == null) {
			System.out.println("Doubly Linked List is null !");
		} else {
			System.out.println("Printing Doubly Linked List Contents:-");
			DoublyLinkedListNode<K, V> dllIterator = head;
			while(dllIterator != null) {
				System.out.println("Key: " + dllIterator.getKey() + " | Value: " + dllIterator.getData());
				dllIterator = dllIterator.getNext();
			}
		}
	}

}
