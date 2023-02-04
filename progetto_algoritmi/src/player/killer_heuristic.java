package player;


import mnkgame.MNKCell;
import player.ScoreMove;





public class killer_heuristic {
	
	 public static class QuickSort {
		 		public QuickSort() {
		 			
		 		}
			    static int partition(ScoreMove arr[], int low, int high)
			    {
			        int pivot = arr[high].score;
			        int i = (low-1); // index of smaller element
			        for (int j=low; j<high; j++)
			        {
			            // If current element is smaller than or
			            // equal to pivot
			            if (arr[j].score <= pivot)
			            {
			                i++;
			 
			                // swap arr[i] and arr[j]
			                ScoreMove temp = arr[i];
			                arr[i] = arr[j];
			                arr[j] = temp;
			            }
			        }
			 
			        // swap arr[i+1] and arr[high] (or pivot)
			        ScoreMove temp = arr[i+1];
			        arr[i+1] = arr[high];
			        arr[high] = temp;
			 
			        return i+1;
			    }
			    static void sort(ScoreMove arr[], int low, int high)
			    {
			        if (low < high)
			        {
			            /* pi is partitioning index, arr[pi] is
			              now at right place */
			            int pi = partition(arr, low, high);
			 
			            // Recursively sort elements before
			            // partition and after partition
			            sort(arr, low, pi-1);
			            sort(arr, pi+1, high);
			        }
			    }
			}	
	
	
	protected class killer_cell {
		protected int weight;
		protected MNKCell killer_move;
		protected killer_cell(){
		}
		protected void insert(MNKCell move, int weight) {
			this.weight = weight;
			killer_move = move;
		}
	}
	protected killer_cell[][] killerMoves;
	protected final int slot;
	public final MNKCell KM_default;
	protected final int max_distance_from_root;
	protected final int weight_bound = 4;                 //da testare, se la mossa ha un bound <3 significa che è scarsa perchè la mossa killer non è servita per cut off
	protected final int max_negative_weight = -50;   //le mosse killer non possono scendere più in basso di questo valore, serve a risolvere il bug che c'erano mosse killer che avevano pesi tipo -1000
	protected int[] size;

	public killer_heuristic(int M, int N) {
		slot = fix_slot_number(M,N);
		KM_default = new MNKCell (-5,5);
		max_distance_from_root = M*N;
		size = new int [max_distance_from_root];
		killerMoves = new killer_cell[max_distance_from_root][slot];
		for(int i=0;i<max_distance_from_root;i++){
			size[i]=0;
			for(int j=0; j<slot; j++) {
				killerMoves[i][j] =  new killer_cell();
			}
		}
	}
	protected int fix_slot_number(int M, int N) {
		if(M*N<=16)        //5 5
			return 2;
		else if(M*N<=40)     //9 9
			return 3;
		else if (M*N<=65)   //20 20
			return 4;
		else 
			return 5;
	}

	public void insert_KM(MNKCell move, int weight, int distance_from_root, boolean ispreviousBestMove) {
		if(size[distance_from_root]>=slot && killerMoves[distance_from_root][slot-1].weight<=weight_bound && !ispreviousBestMove) {
			return;
		}
		killer_cell k_move = new killer_cell();
		
		k_move.insert(move, weight); 
		
		if(ispreviousBestMove && is_a_KM(move, distance_from_root)) {		
			
			for(int i=0; i<size[distance_from_root]; i++) {
				if(myEqual(move,killerMoves[distance_from_root][i].killer_move)) {
					swapKillerCell(killerMoves[distance_from_root], i, 0, true);
				}
					
			}
		}
		else  { 
               if(size[distance_from_root]<slot)
            	   size[distance_from_root]++;
			killerMoves[distance_from_root][size[distance_from_root]-1]=k_move;
		}
		adjust_weight(distance_from_root);
	}

	
	public int get_first_KM_weight(int distance_from_root) {
		if(size[distance_from_root]>0) {
			return killerMoves[distance_from_root][0].weight - 1;
		}
		else {
			return 1;
		}
		
	}

	public boolean is_a_KM(MNKCell move, int distance_from_root) {
		for(int i=0; i<size[distance_from_root]; i++) {
			if(myEqual(move,killerMoves[distance_from_root][i].killer_move)) {
				return true;
			}
		}
		return false;
	}
	public void change_weight(MNKCell move, int increment, int distance_from_root) { //l'incremento può essere pure negativo basta mettere il meno, più è basso il peso e più la killer move è forte
		for(int i=0; i<size[distance_from_root]; i++) {
			if(myEqual(move,killerMoves[distance_from_root][i].killer_move)) {
				if(increment>0)
					killerMoves[distance_from_root][i].weight = killerMoves[distance_from_root][i].weight + increment; 
				else if(killerMoves[distance_from_root][i].weight>max_negative_weight)
					killerMoves[distance_from_root][i].weight = killerMoves[distance_from_root][i].weight + increment; 
			}
		}
		adjust_weight(distance_from_root);
	}
	private void adjust_weight(int distance_from_root) {
		
		for(int i=0; i<size[distance_from_root]; i++) {
			for(int j=0; j<size[distance_from_root]; j++) {
				
				if(killerMoves[distance_from_root][i].weight>killerMoves[distance_from_root][j].weight && i<j) {
					swapKillerCell(killerMoves[distance_from_root], i, j, false);
				}
			}
		}
		
		for(int i = size[distance_from_root] - 1; i>=0; i--) {
			if(killerMoves[distance_from_root][i].weight>weight_bound) {     //se la killer moves è scarsa la elimino
				size[distance_from_root]--;
			}
		}
		
	}
	/*
	//ci creiamo un array con un indice e uno score e riordiniamo in base allo score
	public void move_ordering(MNKCell[] FC, ScoreMove s[], int distance_from_root) {	
		
		
		int counter = 0;
		for(int j=0; j<size[distance_from_root]; j++) {
			for(int i=counter; i<lenght; i++) {
					if(myEqual(FC[i],killerMoves[distance_from_root][j].killer_move)) {
						swapFC(FC, i, counter);
						counter ++;
					}		
			}
		}
	}
	*/
public MNKCell[] move_ordering(MNKCell[] FC, ScoreMove s[], int distance_from_root) {
		
		int max_index = Math.min(10,FC.length);
		MNKCell [] newFC = new MNKCell[max_index];
		int counter = 0;
		for(int j=0; j<size[distance_from_root]; j++) {
			for(int i=0; i<FC.length; i++) {
					if(myEqual(FC[i],killerMoves[distance_from_root][j].killer_move)) {
						newFC[counter] = FC[i];
						counter ++;
						break;
					}		
			}
		}
		QuickSort.sort(s, 0, s.length);
		int it = -1;
		for(int i = counter; i<max_index; i++) {
			while(it<FC.length) {
				it ++;
				if(!is_a_KM(FC[s[it].index], distance_from_root)) {
					newFC[i] = FC[s[it].index];
					break;
				}
			}	
		}
		return newFC;
	}
	
	/*
	private void swapFC(MNKCell FC[], int index_a, int index_b) {
		if(index_a == index_b)
			return;
		MNKCell tmp = FC[index_a];
		FC[index_a] = FC[index_b];
		FC[index_b] = tmp;
	}
	*/
	private void swapKillerCell(killer_cell killerMoves[], int index_a, int index_b, boolean swapweight) {
		if(index_a == index_b)
			return;
		int index_a_weight = killerMoves[index_a].weight;
		int index_b_weight = killerMoves[index_b].weight;
		killer_cell tmp = killerMoves[index_a];
		killerMoves[index_a] = killerMoves[index_b];
		killerMoves[index_b] = tmp;
		if(swapweight) {
			killerMoves[index_a].weight=index_a_weight;
			killerMoves[index_b].weight=index_b_weight;
		}
		
	}
	
	public void removeKM(int distance, MNKCell enemy_move, MNKCell my_move, int M, int N) {
		for(int d=distance; d<M*N; d++) {
			for(int i = 0; i<size[d]; i++) {
				if(myEqual(killerMoves[d][i].killer_move,enemy_move) || myEqual(killerMoves[d][i].killer_move,my_move))
					killerMoves[d][i].weight = weight_bound + 100;
			}
			adjust_weight(d);		
		}
	}
	
	private boolean myEqual(MNKCell a, MNKCell b) {
		if(a.i==b.i && a.j==b.j)
			return true;
		else return false;
	}
	/*
	//-------
	public void printFC(MNKCell FC[], int lenght) {
		System.out.println("printo FC");
		if(lenght>5)
			lenght = 5;
		for(int i=0; i<lenght; i++) {
			System.out.print(" " + FC[i] + " ");
		}
		System.out.println("___________________________");
	}
	public void printKiller(int distance) {
		System.out.println("printo mosse killer");
		//for(int j = distance; j<M*N; j++) {
			for(int i=0; i<size[distance]; i++) {
				System.out.print(" " + killerMoves[distance][i].killer_move + " a distanza " + distance + " con peso " + killerMoves[distance][i].weight);
			}
			System.out.println(" ");
		//}
		System.out.println("__________________");
		
		
	}
	
	//------
	*/

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
	public void change_weight(MNKCell move,int increment, int distance_from_root) { //l'incremento può essere pure negativo basta mettere il meno
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
