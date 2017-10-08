package LRUDiskBackedCache;
class AddedEvictedNodePair<K, V> {
	private final DoublyLinkedListNode<K, V> addedNode;
	private final DoublyLinkedListNode<K, V> evictedNode;

	public AddedEvictedNodePair(DoublyLinkedListNode<K, V> addedNode,
			DoublyLinkedListNode<K, V> evictedNode) {
		this.addedNode = addedNode;
		this.evictedNode = evictedNode;
	}

	DoublyLinkedListNode<K, V> getAddedNode() {
		return addedNode;
	}

	DoublyLinkedListNode<K, V> getEvictedNode() {
    	return evictedNode;
    }
}
