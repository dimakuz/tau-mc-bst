package algorithms;

import main.BSTInterface;

public class BST implements BSTInterface {
	static enum Direction {
		LEFT, RIGHT;
		
		public static Direction next(final int current, final int target) {
			if (current < target) {
				return Direction.RIGHT;
			} else if (current > target){
				return Direction.LEFT;
			} else {
				throw new RuntimeException(String.format("%d, %d", current, target));
			}
		}
	}
	
	static class Node {
		public final int key;
		public volatile Node left;
		public volatile Node right;
		public volatile boolean marked;
		
		Node(int key) {
			this.key = key;
		}
		
		public final Node get(final Direction dir) {
			switch (dir) {
			case LEFT:
				return left;
			case RIGHT:
				return right;
			default:
				throw new RuntimeException(dir.toString());
			}
		}	
		
		public final void set(final Direction dir, final Node node) {
			switch (dir) {
			case LEFT:
				if (node != null && node.key > key)
					throw new RuntimeException();
				left = node;
				break;
			case RIGHT:
				if (node != null && node.key < key)
					throw new RuntimeException();
				right = node;
				break;
			default:
				throw new RuntimeException(dir.toString());
			}
		}
		
		public String toString() {
			return String.format("Node(key=%d)", key);
		}
	}
	
	private final Node root;

    public BST() {
    	root = new Node(Integer.MIN_VALUE);
    }

    public final boolean contains(final int key) {
    	Node curr = root;
    	
    	while (curr != null && curr.key != key) {
    		curr = curr.get(Direction.next(curr.key, key));
    	}
    	
    	return curr != null && !curr.marked && curr.key == key;
    }

    public final boolean insert(final int key) {
    	while (true) {
    		Node curr = root;
    		
    		while (curr.key != key) {
    			Node next = curr.get(Direction.next(curr.key, key));
    			if (next == null)
    				break;
    			curr = next;
    		}
    		
    		synchronized (curr) {
    			if (!curr.marked) {
    				if (curr.key == key)
    					return false;
    				Direction dir = Direction.next(curr.key, key);
    				if (curr.get(dir) == null) {
    					// Perform extra traversal, in case we're inserting value to a recent successor node.
    					Node sanity = root;    		
    					while (sanity.key != key) {
    		    			Node next = sanity.get(Direction.next(sanity.key, key));
    		    			if (next == null)
    		    				break;
    		    			sanity = next;
    		    		}
    					if (sanity == curr) {
    						Node node = new Node(key);
        					curr.set(dir, node);
        					return true;    						
    					}
    				}
    			}
			}
    	}
    }
    
    private final boolean validate(final Node parent, final Node child) {
    	return !parent.marked &&
    			!child.marked &&
    			((parent.key < child.key && parent.right == child) ||
    			 (parent.key > child.key && parent.left == child));
    }

    public final boolean remove(final int key) {
    	while (true) {
    		Node pred = null;
    		Node curr = root;
    		
    		while (curr.key != key) {
    			Node next = curr.get(Direction.next(curr.key, key));
    			if (next == null)
    				return false;
    			pred = curr;
    			curr = next;
    		}
    		
    		synchronized (pred) {
    			synchronized (curr) {
    				Direction curr_dir = Direction.next(pred.key, curr.key);
    				if (validate(pred, curr)) {
    					final Node left = curr.left;
    					final Node right = curr.right;
    					
    					if (left != null && right != null) {
    						synchronized (left) {
    							synchronized (right) {
    	    						Node succ_pred = curr;
    	    						Node succ = right;
    	    						Node next = succ.left;
    	    						
    	    						while (next != null) {
    	    							succ_pred = succ;
    	    							succ = next;
    	    							next = next.left;
    	    						}
    	    						
    	    						synchronized (succ_pred) {
    	    							synchronized (succ) {
    										if (validate(succ_pred, succ) && succ.left == null) {
    											if (succ == right) {
    												right.left = left;
    												curr.marked = true;
    												pred.set(curr_dir, right);
    												return true;
    											} else {
    												final Node replacement = new Node(succ.key);    												
    												replacement.left = left;
    												replacement.right = right;
    												curr.marked = true;
    												pred.set(curr_dir, replacement);
    												succ.marked = true;
    												succ_pred.left = succ.right;
    												return true;
    											}
    										}
    									}
    								}
								}
							}
    					} else {
    						if (left == null && right == null) {
    							curr.marked = true;
    							pred.set(curr_dir, null);
    							return true;
    						} else  {
    							final Node next = (left != null) ? left : right;

    							synchronized (next) {
    								if (validate(curr, next)) {
    									curr.marked = true;
    									pred.set(curr_dir, next);
    									return true;
    								}
								}
    						}
    					}
    				}
				}
			}
    	}
    }

    // Return your ID #
    public String getName() {
        return "322081183";
    }

    // Returns size of the tree.
    // NOTE: Guaranteed to be called without concurrent operations.
    public final int size() {
    	return size(root.right);
    }
    
    private final int size(final Node node) {
    	if (node == null)
    		return 0;
    	return 1 + size(node.left) + size(node.right);
    }

    // Returns the sum of keys in the tree
    // NOTE: Guaranteed to be called without concurrent operations.
    public final long getKeysum() {
    	return getKeysum(root.right);
    }
    
    private final long getKeysum(final Node node) {
    	if (node == null) 
    		return 0;
    	
    	return node.key + getKeysum(node.left) + getKeysum(node.right);
    }
}
