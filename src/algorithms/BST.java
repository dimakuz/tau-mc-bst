package algorithms;

import main.BSTInterface;

public class BST implements BSTInterface {
	static enum Direction {
		LEFT, RIGHT;
		
		public static Direction next(int current, int target) {
			if (current < target) {
				return Direction.RIGHT;
			} else if (current > target){
				return Direction.LEFT;
			} else {
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
				left = node;
				break;
			case RIGHT:
				right = node;
				break;
			default:
				throw new RuntimeException(dir.toString());
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
    				if (curr.get(Direction.next(curr.key, key)) == null) {
    					curr.set(Direction.next(curr.key, key), new Node(key));
    					return true;
    				}
    			}
			}
    	}
    }
    
    private final boolean validate(final Node parent, final Node child, final Direction dir) {
    	return !parent.marked &&
    			!child.marked &&
    			parent.get(dir) == child;
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
    				if (validate(pred, curr, curr_dir)) {
    					final Node left = curr.left;
    					final Node right = curr.right;
    					
    					if (left != null && right != null) {
    						Node succ_parent = curr;
    						Node succ = right;
    						Direction succ_dir = Direction.RIGHT;
    						Node next = succ.left;
    						
    						while (next != null) {
    							succ_parent = succ;
    							succ = next;
    							succ_dir = Direction.LEFT;
    							next = next.left;
    						}
    						
    						synchronized (succ_parent) {
    							synchronized (succ) {
									if (validate(succ_parent, succ, succ_dir) && succ.left == null) {
										Node replacement = new Node(succ.key);
										replacement.left = curr.left;
										
										if (succ_parent != curr)
											replacement.right = curr.right;
										else
											replacement.right = succ.right;

										curr.marked = true;
										pred.set(curr_dir, replacement);

										succ.marked = true;
										if (succ_parent != curr) {
											succ_parent.set(succ_dir, succ.right);
										}
										
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
    								if (validate(curr, next, Direction.next(curr.key, next.key))) {
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
