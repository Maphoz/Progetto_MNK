package player;

import mnkgame.MNKBoard;

import mnkgame.MNKCell;
import mnkgame.MNKGameState;
import player.killer_heuristic.killer_cell;

import java.util.*;



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
	protected final int slot = 2;
	protected final int max_distance_from_root = 100;
	protected final int decrement_distance_from_root = 100; //a distanza 0, 1, 2 mi sa che non ci sono cut off
	protected final int weight_bound = 20;                 //da testare, se la mossa ha un bound <3 significa che è scarsa perchè la mossa killer non è servita per cut off
	protected int[] size;

	public killer_heuristic() {
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
		killer_cell k_move = new killer_cell();
		k_move.insert(move, weight);
		distance_from_root = distance_from_root - decrement_distance_from_root; 
		if(size[distance_from_root]==0) {
			size[distance_from_root]++;
			killerMoves[distance_from_root][size[distance_from_root]-1]=k_move;
			adjust_weight(distance_from_root);/*
			System.out.print("inserita mossa ");
			System.out.print(move);
			System.out.print(" con profondità ");
			System.out.println(distance_from_root);*/
		}
		else if(size[distance_from_root]==1) {
			if(move!=killerMoves[distance_from_root][0].killer_move) {
				size[distance_from_root]++;
				killerMoves[distance_from_root][size[distance_from_root]-1]=k_move;
				adjust_weight(distance_from_root);/*
				System.out.print("inserita mossa ");
				System.out.print(move);
				System.out.print(" con profondità ");
				System.out.println(distance_from_root);*/
				
			}
		}
		else if(killerMoves[distance_from_root][slot-1].weight>weight_bound){
			killerMoves[distance_from_root][slot-1]=k_move;
			adjust_weight(distance_from_root);/*
			System.out.print("inserita mossa ");
			System.out.print(move);
			System.out.print(" con profondità ");
			System.out.println(distance_from_root);*/
		}
	}

	public boolean is_a_KM(MNKCell move, int distance_from_root) {
		if(distance_from_root<decrement_distance_from_root) {
			//System.out.println("distanza insufficiente");
			return false;
		}
		distance_from_root = distance_from_root - decrement_distance_from_root;
		for(int i=0; i<size[distance_from_root]; i++) {
			if(killerMoves[distance_from_root][i].killer_move==move) {
				/*System.out.print(move);
				System.out.println("  è una KM");
				System.out.print(" profondità ");
				System.out.print(distance_from_root);*/
				return true;
			}
		}
		return false;
	}
	public void change_weight(MNKCell move, int increment, int distance_from_root) { //l'incremento può essere pure negativo basta mettere il meno, più è basso il peso e più la killer move è forte
		if(distance_from_root<decrement_distance_from_root)
			return ;
		distance_from_root = distance_from_root - decrement_distance_from_root;
		for(int i=0; i<size[distance_from_root]; i++) {
			if(killerMoves[distance_from_root][i].killer_move==move) {
				killerMoves[distance_from_root][i].weight = killerMoves[distance_from_root][i].weight + increment; 
				/*System.out.print(move);
				System.out.println("  cambiata weight");
				System.out.print(" profondità ");
				System.out.print(distance_from_root);*/
			}
		}
		adjust_weight(distance_from_root);
	}
	public void adjust_weight(int distance_from_root) {
				if(size[distance_from_root]==slot && killerMoves[distance_from_root][0].weight>killerMoves[distance_from_root][1].weight) {
					killer_cell tmp = new killer_cell();
					tmp=killerMoves[distance_from_root][0];
					killerMoves[distance_from_root][0]=killerMoves[distance_from_root][1];
					killerMoves[distance_from_root][1] = tmp;
					if(killerMoves[distance_from_root][1].weight>weight_bound) {     //se la killer moves è scarsa la elimino
						size[distance_from_root]--;
						/*System.out.print(killerMoves[distance_from_root][1].killer_move);
						System.out.println("  KM eliminata");
						System.out.print(" profondità ");
						System.out.print(distance_from_root);*/
					}
				}
				

	}
	public boolean isEmpty(int distance_from_root) {
		distance_from_root = distance_from_root - decrement_distance_from_root;
		if(size[distance_from_root]>0) {
			return false;
		}
		return true;
	}/*
	public MNKCell getKM(int distance_from_root) {  //si presume che non è vuoto l'array corrispondente
		distance_from_root = distance_from_root - decrement_distance_from_root;
		return killerMoves[distance_from_root][0].killer_move;
	}
	public boolean KMisFree(MNKCell[] FC, int lenght, int distance_from_root) {  //si presume che non è vuoto l'array corrispondente
		distance_from_root = distance_from_root - decrement_distance_from_root;
		for(int i=0; i<lenght; i++) {
			if(FC[i]==killerMoves[distance_from_root][0].killer_move)
				return true;
		}
		return false;
	}*/
	public void move_ordering(MNKCell[] FC, int lenght, int distance_from_root) {
		if(distance_from_root<decrement_distance_from_root)
			return ;
		distance_from_root = distance_from_root - decrement_distance_from_root;
		if(size[distance_from_root]<=0) {
			return;
		}
		else if(lenght>=size[distance_from_root]) {
			
			if(size[distance_from_root]==1) {
				
				for(int i=0; i<lenght; i++) {
					if(FC[i]==killerMoves[distance_from_root][0].killer_move) {
						MNKCell tmp = FC[0];
						FC[0]=FC[i];
						FC[i]=tmp;
					}
				}
			}
			else if(size[distance_from_root]==2) {
				for(int i=0; i<lenght; i++) {
					if(FC[i]==killerMoves[distance_from_root][0].killer_move) {
						MNKCell tmp = FC[0];
						FC[0]=FC[i];
						FC[i]=tmp;
					}
					if(FC[i]==killerMoves[distance_from_root][1].killer_move) {
						MNKCell tmp = FC[1];
						FC[1]=FC[i];
						FC[i]=tmp;
					}
				}
			}
		}
		
	}
	public boolean deep_enough(int distance_from_root) {
		if(distance_from_root>=decrement_distance_from_root)
			return true;
		else {
			return false;
		}
	}/*
	public void debug_print() {
		System.out.println(" ");
		for(int i=0; i<max_distance_from_root; i++) {
			for(int j=0; j<size[i]; j++) {
				System.out.print(killerMoves[i][j].killer_move);
				System.out.print(" peso= ");
				System.out.print(killerMoves[i][j].weight);
				System.out.print(" ");
			}
			System.out.println(" ");
		}
	}*/

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