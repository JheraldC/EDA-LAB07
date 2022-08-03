package clase8;

public class BTreeGeneric<E extends Comparable<? super E>> {

	BTree tree;
	
    public boolean add(E value) {
    	if(tree.search(value) != value) {
    		tree.insert(value);
    		return true;
    	} else {
    		return false;
    	}
    }

    public E remove(E value) {
       if(tree.search(value) == value) {
    	   tree.remove(value);
    	   return value;
       } else {
    	   return null;
       }
    }

    public void clear() {
    	for (int i = 0; i < size(); i++) {
			tree.root.remove(i);
		}
    }

    public boolean search(E value) {
        if(tree.search(value) == value) {
        	return true;
        } else {
        	return false;
        }
    }

    public int size() {
        return tree.root.num;
    }
}
