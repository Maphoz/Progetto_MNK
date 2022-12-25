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
	protected final int slot = 3;
	protected final int max_distance_from_root = 30;
	protected final int decrement_distance_from_root = 2; //a distanza 0, 1, 2 mi sa che non ci sono cut off
	protected final int weight_bound = 3;                 //da testare, se la mossa ha un bound <3 significa che è scarsa perchè la mossa killer non è servita per cut off
	protected int size;

	public killer_heuristic() {
		size = 0;
		killerMoves = new killer_cell[max_distance_from_root][slot];
		
		for(int i=0;i<max_distance_from_root;i++){
			for(int j=0; j<slot; j++) {
				killerMoves[i][j] =  new killer_cell();
			}
		}
	}
	public void insert_KM(MNKCell move, int weight, int distance_from_root) {
		killer_cell k_move = new killer_cell();
		k_move.insert(move, weight);
		distance_from_root = distance_from_root - decrement_distance_from_root; 
		if(size<slot) {
			size++;
			killerMoves[distance_from_root][size-1]=k_move;
			adjust_weight(distance_from_root);
		}
		else if(killerMoves[distance_from_root][slot-1].weight>weight_bound) {
			killerMoves[distance_from_root][slot-1]=k_move;
			adjust_weight(distance_from_root);
		}

	}

	public boolean is_a_KM(MNKCell move, int distance_from_root) {
		distance_from_root = distance_from_root - decrement_distance_from_root;
		for(int i=0; i<size; i++) {
			if(killerMoves[distance_from_root][i].killer_move==move) {
				return true;
			}
		}
		return false;
	}
	public void change_weight(MNKCell move, int increment, int distance_from_root) { //l'incremento può essere pure negativo basta mettere il meno
		distance_from_root = distance_from_root - decrement_distance_from_root;
		for(int i=0; i<size; i++) {
			if(killerMoves[distance_from_root][i].killer_move==move) {
				killerMoves[distance_from_root][i].weight = killerMoves[distance_from_root][i].weight + increment; 
			}
		}
		adjust_weight(distance_from_root);
	}
	public void adjust_weight(int distance_from_root) {
		for(int i=0; i<size-1; i++) {
			for(int j=0; j<size-1; j++) {
				if(killerMoves[distance_from_root][j].weight>killerMoves[distance_from_root][j+1].weight) {
					killer_cell tmp = new killer_cell();
					tmp=killerMoves[distance_from_root][j];
					killerMoves[distance_from_root][j]=killerMoves[distance_from_root][j+1];
					killerMoves[distance_from_root][j+1] = tmp;
				}
			}
		}
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