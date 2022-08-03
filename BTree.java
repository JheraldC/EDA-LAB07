package clase8;

public class BTree<T extends Comparable<T>> {
	BNodeGeneric<T> root;
	int MinDeg;

	// constructor
	public BTree(int deg) {
		this.root = null;
		this.MinDeg = deg;
	}

	public void traverse() {
		if (root != null) {
			root.traverse();
		}
	}

	// Encuentra la función clave
	public BNodeGeneric<T> search(T key) {
		return root == null ? null : root.search(key);
	}

	public void insert(T key) {

		if (root == null) {

			root = new BNodeGeneric<T>(MinDeg, true);
			root.keys.set(0, key);
			root.num = 1;
		} else {
			// Cuando el nodo raíz está lleno, el árbol crecerá más alto
			if (root.num == 2 * MinDeg - 1) {
				BNodeGeneric<T> s = new BNodeGeneric<T>(MinDeg, false);
				// El antiguo nodo raíz se convierte en hijo del nuevo nodo raíz
				s.children.set(0, root);
				// Separa el antiguo nodo raíz y dale una clave al nuevo nodo
				s.splitChild(0, root);
				// El nuevo nodo raíz tiene 2 nodos secundarios, debe mover el antiguo nodo raíz
				// allí
				int i = 0;
				if (s.keys.get(0).compareTo(key) < 0)
					i++;
				s.children.get(i).insertNotFull(key);

				root = s;
			} else
				root.insertNotFull(key);
		}
	}

	public void remove(T key) {
		if (root == null) {
			System.out.println("The tree is empty");
			return;
		}

		root.remove(key);

		if (root.num == 0) { // Si el nodo raíz tiene 0 claves
			// Si tiene un nodo hijo, use su primer nodo hijo como el nuevo nodo raíz,
			// de lo contrario, establezca el nodo raíz en nulo
			if (root.isLeaf)
				root = null;
			else
				root = root.children.get(0);
		}
	}
}
