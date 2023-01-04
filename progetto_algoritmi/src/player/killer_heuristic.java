package player;


import mnkgame.MNKCell;






public class killer_heuristic {
	
	public class killer_cell {
		public int weight;
		public MNKCell killer_move;
		public killer_cell(){	
		}
		public void insert(MNKCell move, int weight) {
			this.weight = weight;
			killer_move = move;			
		}
	}
	protected killer_cell[][] killerMoves;
	protected final int slot = 3;
	protected final int max_distance_from_root;
	protected final int decrement_distance_from_root; //sotto una certa soglia di distanza dalla radice non ci sono cut off 
	protected final int weight_bound = 4;                 //da testare, se la mossa ha un bound <3 significa che � scarsa perch� la mossa killer non � servita per cut off
	protected int[] size;

	public killer_heuristic(int M, int N, int K) {
		decrement_distance_from_root = K - 1;
		max_distance_from_root = M*N - decrement_distance_from_root;
		size = new int [max_distance_from_root];
		killerMoves = new killer_cell[max_distance_from_root][slot];
		for(int i=0;i<max_distance_from_root;i++){
			size[i]=0;
			for(int j=0; j<slot; j++) {
				killerMoves[i][j] =  new killer_cell();
			}
		}
	}
	public void insert_KM(MNKCell move, int weight, int distance_from_root) {
		distance_from_root = distance_from_root - decrement_distance_from_root;
		if(distance_from_root >=max_distance_from_root)              //non si sa mai, lo facciamo per non avere un OutOfBound
			distance_from_root = max_distance_from_root - 1;
		if(size[distance_from_root]>=slot && killerMoves[distance_from_root][slot-1].weight<=weight_bound) {
			return;
		}
		killer_cell k_move = new killer_cell();
		k_move.insert(move, weight); 
		
		size[distance_from_root]++;
		killerMoves[distance_from_root][size[distance_from_root]-1]=k_move;
		adjust_weight(distance_from_root);
	}

	public boolean is_a_KM(MNKCell move, int distance_from_root) {
		if(distance_from_root >=max_distance_from_root)              //non si sa mai, lo facciamo per non avere un OutOfBound
			distance_from_root = max_distance_from_root - 1;
		distance_from_root = distance_from_root - decrement_distance_from_root;
		if(distance_from_root >=max_distance_from_root)              //non si sa mai, lo facciamo per non avere un OutOfBound
			distance_from_root = max_distance_from_root - 1;
		for(int i=0; i<size[distance_from_root]; i++) {
			if(myEqual(move,killerMoves[distance_from_root][i].killer_move)) {
				return true;
			}
		}
		return false;
	}
	public void change_weight(MNKCell move, int increment, int distance_from_root) { //l'incremento pu� essere pure negativo basta mettere il meno, pi� � basso il peso e pi� la killer move � forte
		distance_from_root = distance_from_root - decrement_distance_from_root;
		if(distance_from_root >=max_distance_from_root)              //non si sa mai, lo facciamo per non avere un OutOfBound
			distance_from_root = max_distance_from_root - 1;
		for(int i=0; i<size[distance_from_root]; i++) {
			if(myEqual(move,killerMoves[distance_from_root][i].killer_move)) {
				killerMoves[distance_from_root][i].weight = killerMoves[distance_from_root][i].weight + increment; 
			}
		}
		adjust_weight(distance_from_root);
	}
	public void adjust_weight(int distance_from_root) {
		
		for(int i=0; i<size[distance_from_root]; i++) {
			for(int j=0; j<size[distance_from_root]; j++) {
				
				if(killerMoves[distance_from_root][i].weight>killerMoves[distance_from_root][j].weight && i<j) {
					swapKillerCell(killerMoves[distance_from_root], i, j);
				}
			}
		}
		
		for(int i = size[distance_from_root] - 1; i>=0; i--) {
			if(killerMoves[distance_from_root][i].weight>weight_bound) {     //se la killer moves � scarsa la elimino
				size[distance_from_root]--;
			}
		}
		
	}
	
	public void move_ordering(MNKCell[] FC, int lenght, int distance_from_root) {		
		if(distance_from_root<decrement_distance_from_root)
			return ;
		distance_from_root = distance_from_root - decrement_distance_from_root;
		if(distance_from_root >=max_distance_from_root)              //non si sa mai, lo facciamo per non avere un OutOfBound
			distance_from_root = max_distance_from_root - 1;
		if(lenght<slot)
			return;

		for(int j=0; j<size[distance_from_root]; j++) {
			for(int i=j; i<lenght; i++) {
					if(myEqual(FC[i],killerMoves[distance_from_root][j].killer_move)) {
						swapFC(FC, i, j);
					}
				
			}
		}

	}
	
	public void swapFC(MNKCell FC[], int index_a, int index_b) {
		if(index_a == index_b)
			return;
		MNKCell tmp = FC[index_a];
		FC[index_a] = FC[index_b];
		FC[index_b] = tmp;
	}
	public void swapKillerCell(killer_cell killerMoves[], int index_a, int index_b) {
		killer_cell tmp = killerMoves[index_a];
		killerMoves[index_a] = killerMoves[index_b];
		killerMoves[index_b] = tmp;
	}
	
	public boolean deep_enough(int distance_from_root) {
		if(distance_from_root>=decrement_distance_from_root) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean myEqual(MNKCell a, MNKCell b) {
		if(a.i==b.i && a.j==b.j)
			return true;
		else return false;
	}
	
	

}







/*
	protected PriorityQueue<killer_cell>[] killerMoves;
	protected int distance_from_root;
	protected int slot;

	public killer_heuristic() {
		
		distance_from_root = 20;
		slot=3;
        Comparator<killer_cell> comparator = (s1, s2) -> {
            return s1.weight - s2.weight;
        };
        
		PriorityQueue<killer_cell> killerMoves[] = new PriorityQueue[distance_from_root];
		for(int i=0;i<distance_from_root;i++){
			//PriorityQueue<killer_cell> pq = new PriorityQueue<killer_cell>(3,comparator);
			//killer_cell c = new killer_cell();
			//pq.add(c);
			//killerMoves[i] = pq;
			killerMoves[i] =  new PriorityQueue<killer_cell>(3,comparator);
		}
	}
	public void insert_KM(MNKCell move, int weight, int distance_from_root) {
		killer_cell k_move = new killer_cell();
		k_move.insert(move, weight);
		distance_from_root = distance_from_root - 2; //a distanza 0, 1, 2 mi sa che non ci sono cut off
		if(killerMoves[distance_from_root].size()<slot)
			killerMoves[distance_from_root].add(k_move);
		else
	}

	public boolean is_a_KM(MNKCell move, int distance_from_root) {
		killer_cell[] tmp_1 = new killer_cell [slot];
		killer_cell[] tmp_2 = killerMoves[distance_from_root].toArray(tmp_1); 
		
		for(int i=0; i<slot; i++) {
			if(tmp_2[i].killer_move==move) {
				return true;
			}
		}
		return false;
	}
	public void change_weight(MNKCell move,int increment, int distance_from_root) { //l'incremento pu� essere pure negativo basta mettere il meno
		killer_cell[] tmp = new killer_cell[slot];
		for(int i=0; i<slot; i++) {
			tmp[i] = killerMoves[distance_from_root].poll();
			if(tmp[i].killer_move==move) {
				tmp[i].weight=tmp[i].weight + increment;
			}
			killerMoves[distance_from_root].add(tmp[i]);
		}
	}
*/