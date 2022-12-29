package player;
import mnkgame.MNKBoard;

import mnkgame.MNKCell;
import mnkgame.MNKGameState;
import player.killer_heuristic;
import player.killer_heuristic.killer_cell;

public class main {

	public static void main(String[] args) {
		killer_heuristic killer = new killer_heuristic();
		/*//System.out.println(k.killerMoves[3].size());
		MNKCell c = new MNKCell(1, 1);
		MNKCell d = new MNKCell(0, 0);
		MNKCell h = new MNKCell(2, 2);
		k.insert_KM(c, 2, 3);
		k.insert_KM(d, 2, 3);
		k.insert_KM(h, -1, 3);
		
		for(int i=0; i<3;i++) {
			System.out.println(k.killerMoves[1][i].killer_move);
		}
		k.change_weight(c, -5, 3);
		for(int i=0; i<3;i++) {
			System.out.println(k.killerMoves[1][i].killer_move);
		}
	}
		int distance_from_root = 3;
		MNKCell FC[] = new MNKCell[5];
		FC[0] = new MNKCell (1,0);
		FC[1] = new MNKCell (0,0);
		FC[2] = new MNKCell (2,2);
		FC[3] = new MNKCell (3,3);
		FC[4] = new MNKCell (4,4);
		MNKCell c = new MNKCell(5,5);
		int size = FC.length;
		/*System.out.println(size);
		for(int i=0; i<size; i++) {
			System.out.print(FC[i]);
			System.out.print("  ");
		}
		System.out.println(" ");
		killer.move_ordering(FC, size, distance_from_root);
		for(int i=0; i<size; i++) {
			System.out.print(FC[i]);
			System.out.print("  ");
		}
		if(!killer.is_a_KM(FC[3], distance_from_root) && killer.deep_enough(distance_from_root)) {
			killer.insert_KM(FC[3], 1, distance_from_root);          //inserisco la killer move
		}
		killer.debug_print();
		killer.change_weight(FC[3], + 1, distance_from_root);
		killer.debug_print();
		if(!killer.is_a_KM(FC[1], distance_from_root) && killer.deep_enough(distance_from_root)) {
			killer.insert_KM(FC[1], 1, distance_from_root);          //inserisco la killer move
		}
		killer.debug_print();
		killer.move_ordering(FC, size, distance_from_root);
		for(int i=0; i<size; i++) {
			System.out.print(FC[i]);
			System.out.print("  ");
			
		}
		System.out.println(" ");
		killer.change_weight(FC[1], + 3, distance_from_root);
		killer.debug_print();
		killer.move_ordering(FC, size, distance_from_root);
		for(int i=0; i<size; i++) {
			System.out.print(FC[i]);
			System.out.print("  ");
			
		}
		System.out.println(" ");
		if(!killer.is_a_KM(FC[4], distance_from_root) && killer.deep_enough(distance_from_root)) {
			killer.insert_KM(FC[4], 1, distance_from_root);          //inserisco la killer move
		}
		killer.debug_print();
		killer.move_ordering(FC, size, distance_from_root);
		for(int i=0; i<size; i++) {
			System.out.print(FC[i]);
			System.out.print("  ");
			
		}
		for(int i=0; i<size; i++) {
			System.out.print(FC[i]);
			System.out.print("  ");
			
		}
		killer.debug_print();
		killer.move_ordering(FC, size, distance_from_root);
		for(int i=0; i<size; i++) {
			System.out.print(FC[i]);
			System.out.print("  ");
			
		}
		if(!killer.is_a_KM(FC[4], distance_from_root) && killer.deep_enough(distance_from_root)) {
			killer.insert_KM(FC[4], 1, distance_from_root);          //inserisco la killer move
		
		}
		killer.debug_print();
		killer.move_ordering(FC, size, distance_from_root);
		for(int i=0; i<size; i++) {
			System.out.print(FC[i]);
			System.out.print("  ");
			
		}*/

	}
	
}


