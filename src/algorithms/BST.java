package algorithms;

import main.BSTInterface;

public class BST implements BSTInterface {
	static enum DIR {
		LEFT, RIGHT;
		
		public static DIR next(int current, int target) {
			if (current < target) {
				return DIR.LEFT;
			} else if (current > target){
				return DIR.RIGHT;
			} else {
				throw new RuntimeException();
			}
		}
		
		public DIR other() {
			switch (this) {
			case LEFT:
				return DIR.RIGHT;
			case RIGHT:
				return DIR.LEFT;
			default:
				throw new RuntimeException();
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
		
		public final Node get(final DIR dir) {
			switch (dir) {
			case LEFT:
				return left;
			case RIGHT:
				return right;
			default:
				throw new RuntimeException();
			}
		}	
		
		public final Node set(final DIR dir, final Node node) {
			switch (dir) {
			case LEFT:
				left = node;
			case RIGHT:
				right = node;
			default:
				throw new RuntimeException();
			}
		}
	}
	
	private final Node root;

    public BST() {
    	root = new Node(Integer.MIN_VALUE);
    }

    public final boolean contains(final int key) {
    	Node curr = root;
    	
    	while (curr != null && curr.key != key) {
    		curr = curr.get(DIR.next(curr.key, key));
    	}
    	
    	return curr != null && !curr.marked && curr.key == key;
    }

    public final boolean insert(final int key) {
    	while (true) {
    		Node curr = root;
    		
    		while (curr.key != key) {
    			Node next = curr.get(DIR.next(curr.key, key));
    			if (next == null)
    				break;
    		}
    		
    		synchronized (curr) {
    			if (!curr.marked) {
    				if (curr.key == key)
    					return false;
    				curr.set(DIR.next(curr.key, key), new Node(key));
    				return true;
    			}
			}
    	}
    }
    
    private final boolean validate(final Node parent, final Node child, final DIR dir) {
    	return !parent.marked &&
    			!child.marked &&
    			parent.get(dir) == child;
    }

    public final boolean remove(final int key) {
    	while (true) {
    		Node pred = null;
    		Node curr = root;
    		
    		while (curr.key != key) {
    			Node next = curr.get(DIR.next(curr.key, key));
    			if (next == null)
    				return false;
    			pred = curr;
    			curr = next;
    		}
    		
    		synchronized (pred) {
    			synchronized (curr) {
    				DIR curr_dir = DIR.next(pred.key, curr.key);
    				if (validate(pred, curr, curr_dir)) {
    					final Node left = curr.left;
    					final Node right = curr.right;
    					
    					if (left != null && right != null) {
    						Node succ_parent = curr;
    						Node succ = curr.right;
    						DIR succ_dir = DIR.RIGHT;
    						
    						while (succ.left != null) {
    							succ_parent = succ;
    							succ = succ.left;
    							succ_dir = DIR.LEFT;
    						}
    						
    						synchronized (succ_parent) {
    							synchronized (succ) {
									if (validate(succ_parent, succ, succ_dir)) {
										Node replacement = new Node(succ.key);
										replacement.left = curr.left;
										
										if (succ_parent != curr)
											replacement.right = curr.right;
										else
											replacement.right = succ.right;

										pred.set(curr_dir, replacement);
										curr.marked = true;
										
										if (succ_parent != curr) {
											succ_parent.set(succ_dir, succ.right);
										}
										succ.marked = true;
										
										return true;
									}
								}
							}
    					} else {
    						if (left == null && right == null) {
    							curr.marked = true;
    							pred.set(curr_dir, null);
    							return true;
    						} else  {
    							Node next = (left != null) ? left : right;

    							synchronized (next) {
    								if (validate(curr, next, DIR.next(curr.key, next.key))) {
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
