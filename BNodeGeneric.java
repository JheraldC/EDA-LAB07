package clase8;
import java.util.Vector;

public class BNodeGeneric<T extends Comparable<T>> {

	Vector<T> keys;// claves de nodo
	int MinDeg;// El grado mínimo del nodo B-tree
	Vector<BNodeGeneric<T>> children;// nodos secundarios
	int num;// El número de claves de nodos
	boolean isLeaf;// Verdadero cuando es un nodo hoja

	public BNodeGeneric(int deg, boolean isLeaf) {
		this.MinDeg = deg;
        this.isLeaf = isLeaf;
        
        //this.keys = new int[2*this.MinDeg-1]; El nodo tiene como máximo 2 * teclas MinDeg-1
        for (int i = 0; i < 2*MinDeg-1; i++) {
			keys.add(null);
		}
        
        //this.children = new BTreeNode[2*this.MinDeg];

        for (int i = 0; i < 2*this.MinDeg; i++) {
        	children.add(null);
		}
        this.num = 0;
	}
	
    // Encuentra el primer índice de posición igual o mayor que la clave
    public int findKey(T key){
        int idx = 0;
        
        // Las condiciones para salir del ciclo son: 
        // 1. idx == num, es decir, escanear todo
        // 2. idx < num, es decir, encontrar clave o mayor que clave        
        while(idx < num && keys.get(idx).compareTo(key) < 0) {
            ++idx;
        }
        return idx;
    }
    
    public void remove(T key){

        int idx = findKey(key);
        if (idx < num && keys.get(idx).compareTo(key) == 0){ // Encontrar la llave
            if (isLeaf) // la clave está en el nodo hoja
                removeFromLeaf(idx);
            else // la clave no está en el nodo hoja
                removeFromNonLeaf(idx);
        }
        else{
            if (isLeaf){ // Si el nodo es un nodo hoja, entonces el nodo no está en el árbol B
                System.out.printf("The key %d is does not exist in the tree\n",key);
                return;
            }

            // De lo contrario, la clave que se eliminará existe en el subárbol enraizado en el nodo

            // Este indicador indica si la clave existe en el subárbol enraizado en el último nodo secundario del nodo
            // Cuando idx es igual a num, se compara todo el nodo, el indicador es verdadero
            boolean flag = idx == num; 
            
            if (children.get(idx).num < MinDeg) { // Cuando el nodo hijo de este nodo no está lleno, llénalo primero
                fill(idx);
            }

            // Si el último nodo secundario se ha fusionado, entonces debe haberse fusionado con el nodo secundario anterior, por lo que recurrimos recursivamente en el nodo secundario (idx-1).
            // De lo contrario, recurrimos al nodo secundario (idx) th, que ahora tiene al menos las claves de grado mínimo
            if (flag && idx > num)
            	children.get(idx-1).remove(key);
            else
            	children.get(idx).remove(key);
        }
    }
    
    public void removeFromLeaf(int idx){

        // Retroceder de idx
        for (int i = idx +1;i < num;++i)
        	keys.set(i-1, keys.get(i));
        num --;
    }

    public void removeFromNonLeaf(int idx){

        T key = keys.get(idx);

        // Si el subárbol antes de la clave (hijos [idx]) tiene al menos t claves
        // Luego, busque el predecesor 'pre' de la clave en el subárbol enraizado en hijos [idx]
        // Reemplazar clave con'pred ', eliminar recursivamente pred en niños [idx]        	
        if(children.get(idx).num >= MinDeg) {
        	
            T pred = getPred(idx);
            keys.set(idx, pred);
            children.get(idx).remove(pred);
        }
        // Si children [idx] tiene menos claves que MinDeg, verifique children [idx + 1]
        // Si children [idx + 1] tiene al menos claves MinDeg, en el subárbol rooteado en children [idx + 1]
        // Encuentra el sucesor 'suc' de la clave y borra recursivamente succ en hijos [idx + 1]
        else if (children.get(idx+1).num >= MinDeg){
            T succ = getSucc(idx);
            keys.set(idx, succ);
            children.get(idx+1).remove(succ);
        }
        else{
            // Si los hijos [idx] y los hijos [idx + 1] tienen menos de MinDeg
            // Luego, combina la clave y los elementos secundarios [idx + 1] en elementos secundarios [idx]
            // Ahora hijos [idx] contiene clave 2t-1
            // Liberar hijos [idx + 1], eliminar recursivamente la clave en hijos [idx]
            merge(idx);
            children.get(idx).remove(key);
        }
    }
    
    public T getPred(int idx){ // El nodo predecesor siempre está buscando el nodo más a la derecha del subárbol izquierdo

        // Moverse al nodo más a la derecha hasta que llegue al nodo hoja
    	BNodeGeneric<T> cur = children.get(idx);
        while (!cur.isLeaf)
        	cur = cur.children.get(cur.num);
        return cur.keys.get(cur.num-1);
    }

    public T getSucc(int idx){ // El sucesor siempre está mirando desde el subárbol derecho hacia la izquierda

        // Continúa moviendo el nodo más a la izquierda de los hijos [idx + 1] hasta que llegue al nodo hoja
    	BNodeGeneric<T> cur = children.get(idx+1);
        while (!cur.isLeaf)
            cur = cur.children.get(0);
        return cur.keys.get(0);
    }
    
    // Rellene hijos [idx] que tiene menos de MinDeg claves
    public void fill(int idx){

        // Si el nodo secundario anterior tiene varias claves MinDeg-1, pídalo prestado
        if (idx != 0 && children.get(idx-1).num >= MinDeg)
            borrowFromPrev(idx);
        // El último nodo hijo tiene varias claves MinDeg-1, pídalo prestado
        else if (idx != num && children.get(idx+1).num >= MinDeg)
            borrowFromNext(idx);
        else{
            // Fusionar hijos [idx] y su hermano
            // Si children [idx] es el último nodo hijo
            // Luego, combínalo con el nodo secundario anterior o, de lo contrario, combínalo con su próximo hermano
            if (idx != num)
                merge(idx);
            else
                merge(idx-1);
        }
    }
    
    // Pedir prestada una clave de hijos [idx-1] e insertarla en hijos [idx]
    public void borrowFromPrev(int idx){

    	BNodeGeneric<T> child = children.get(idx);
    	BNodeGeneric<T> sibling = children.get(idx-1);

        // La última clave de los hijos [idx-1] se desbordó al nodo padre
        // El subflujo de la clave [idx-1] desde el nodo primario se inserta como la primera clave en los elementos secundarios [idx]
        // Por lo tanto, el hermano disminuye en uno y los niños aumentan en uno
        for (int i = child.num-1; i >= 0; --i) // hijos [idx] avanzar
        	child.keys.set(i+1, child.keys.get(i));

        if (!child.isLeaf){ // Cuando child [idx] no es un nodo hoja, mueve su nodo hijo hacia atrás
            for (int i = child.num; i >= 0; --i)
            	child.children.set(i+1, child.children.get(i));
        }

        // Establecer la primera clave del nodo secundario a las claves del nodo actual [idx-1]
        child.keys.set(0, keys.get(idx-1));
        if (!child.isLeaf) // Usa el último nodo hijo de hermano como el primer nodo hijo de hijos [idx]
        	child.children.set(0, sibling.children.get(sibling.num));

        // Mueve la última clave del hermano hasta la última del nodo actual
        keys.set(idx-1, sibling.keys.get(sibling.num-1));
        child.num += 1;
        sibling.num -= 1;
    }
    
    // Simétrico con loanFromPrev
    public void borrowFromNext(int idx){

    	BNodeGeneric<T> child = children.get(idx);
    	BNodeGeneric<T> sibling = children.get(idx+1);

    	child.keys.set(child.num, keys.get(idx));

        if (!child.isLeaf)
        	child.children.set(child.num+1, sibling.children.get(0));

        keys.set(idx, sibling.keys.get(0));

        for (int i = 1; i < sibling.num; ++i)
        	sibling.keys.set(i-1, sibling.keys.get(i));

        if (!sibling.isLeaf){
            for (int i= 1; i <= sibling.num;++i)
            	sibling.children.set(i-1, sibling.children.get(i));
        }
        child.num += 1;
        sibling.num -= 1;
    }
    
    // fusionar childre [idx + 1] en childre [idx]
    public void merge(int idx){

    	BNodeGeneric<T> child = children.get(idx);
        BNodeGeneric<T> sibling = children.get(idx+1);

        // Inserta la última clave del nodo actual en la posición MinDeg-1 del nodo secundario
        child.keys.set(MinDeg-1, keys.get(idx));

        // claves: niños [idx + 1] copiados a niños [idx]
        for (int i =0 ; i< sibling.num; ++i)
        	child.keys.set(i+MinDeg, sibling.keys.get(i));

        // children: children [idx + 1] copiado a children [idx]
        if (!child.isLeaf){
            for (int i = 0;i <= sibling.num; ++i)
            	child.children.set(i+MinDeg, sibling.children.get(i));
        }

        // Mueve las teclas hacia adelante, no el espacio causado por mover las teclas [idx] a los niños [idx]
        for (int i = idx+1; i<num; ++i)
        	keys.set(i-1, keys.get(i));
        // Mueve el nodo hijo correspondiente hacia adelante
        for (int i = idx+2;i<=num;++i)
        	children.set(i-1, children.get(i));

        child.num += sibling.num + 1;
        num--;
    }
    
    public void insertNotFull(T key){

        int i = num -1; // inicializa i al índice del valor más a la derecha

        if (isLeaf){ // Cuando es un nodo hoja
            // Encuentra dónde se debe insertar la nueva clave
            while (i >= 0 && keys.get(i).compareTo(key) > 0){
                keys.set(i+1, keys.get(i));// teclas de vuelta
                i--;
            }
            keys.set(i+1, key);
            num = num +1;
        }
        else{
            // Encuentra la posición del nodo hijo que debe insertarse
            while (i >= 0 && keys.get(i).compareTo(key) > 0)
                i--;
            if (children.get(i+1).num == 2*MinDeg - 1){ // Cuando el nodo hijo está lleno
                splitChild(i+1, children.get(i+1));
                // Después de dividir, la clave en el medio del nodo secundario se mueve hacia arriba, el nodo secundario se divide en dos
                if (keys.get(i+1).compareTo(key) < 0)
                    i++;
            }
            children.get(i+1).insertNotFull(key);
        }
    }
    
    public void splitChild(int i ,BNodeGeneric<T> y){

        // Primero cree un nodo que contenga las claves de MinDeg-1 de y
    	BNodeGeneric<T> z = new BNodeGeneric<T>(y.MinDeg,y.isLeaf);
        z.num = MinDeg - 1;

        // Pase todos los atributos y a z
        for (int j = 0; j < MinDeg-1; j++)
        	z.keys.set(j, y.keys.get(j+MinDeg));
        if (!y.isLeaf){
            for (int j = 0; j < MinDeg; j++)
            	z.children.set(j, y.children.get(j+MinDeg));
        }
        y.num = MinDeg-1;

        // Inserta el nuevo nodo hijo en el nodo hijo
        for (int j = num; j >= i+1; j--)
        	children.set(j+1, children.get(j));
        children.set(i+1, z);

        // Mueve una tecla en y a este nodo
        for (int j = num-1;j >= i;j--)
        	keys.set(j+1, keys.get(j));
        keys.set(i, y.keys.get(MinDeg-1));

        num = num + 1;
    }
    
    public void traverse(){
        int i;
        for (i = 0; i< num; i++){
            if (!isLeaf)
            	children.get(i).traverse();
            System.out.printf(" %d",keys.get(i));
        }

        if (!isLeaf){
        	children.get(i).traverse();
        }
    }


    public BNodeGeneric<T> search(T key){
        int i = 0;
        while (i < num && keys.get(i).compareTo(key) < 0)
            i++;

        if (keys.get(i).compareTo(key) == 0)
            return this;
        if (isLeaf)
            return null;
        return children.get(i).search(key);
    }
}