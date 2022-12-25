package player;

import mnkgame.MNKBoard;

import player.killer_heuristic;
import mnkgame.MNKCell;
import mnkgame.MNKGameState;
import player.killer_heuristic.killer_cell;

public class alphabeta{
	MNKGameState wCond;
	MNKGameState lCond;
	killer_heuristic killer;
	
	public alphabeta(MNKGameState wc, MNKGameState lc) {
		killer_heuristic killer = new killer_heuristic();
		//saving the win conditions
		wCond = wc;
		lCond = lc;
	}
	
	public int alphaBeta(MNKBoard board, boolean maximizer, int depth) {
		int distance_from_root = 0;
		//the move is being tried by the player class, so we look for opponent best response
		return min(board, Integer.MIN_VALUE, Integer.MAX_VALUE, depth, distance_from_root);
	}
	
	protected int max(MNKBoard board, int alpha, int beta, int depth, int distance_from_root) {
		
		MNKCell[] FC = board.getFreeCells();
		MNKGameState state;
		int maxValue = Integer.MIN_VALUE;
		for (MNKCell d: FC) {
			state = board.markCell(d.i, d.j);	
			if(depth==0) {
				return 0;   //da mettere qua l'evaluation
			}
			if (state == wCond) {							//if it is a winning cell, return the best evaluation
				board.unmarkCell();
				return 1;
			}
			if (state == MNKGameState.DRAW) {				//if it is a drawing cell, return the null evaluation
				board.unmarkCell();
				return 0;
			}
			int value = min(board, alpha, beta, depth - 1, distance_from_root + 1);			//else recursive call and compare the evaluations
			maxValue = Math.max(value, maxValue);
			alpha = Math.max(alpha, maxValue);
			board.unmarkCell();
			if (alpha >= beta) { 
				if(!killer.is_a_KM(d, distance_from_root)) {
					killer.insert_KM(d, 1, distance_from_root);          //inserisco la killer move
				}
				else killer.change_weight(d, - 1, distance_from_root);  //mpssa buona 
				break;
			}
			else {
				if(killer.is_a_KM(d, distance_from_root)) {
					killer.change_weight(d, + 1, distance_from_root);       //la mossa era scarsotta perchè non ha fatto cut off quindi abbassiamo la priorità
				}
				
			}
		}
		return maxValue;
	}
	
	protected int min(MNKBoard board, int alpha, int beta, int depth, int distance_from_root) {
		MNKCell[] FC = board.getFreeCells();
		MNKGameState state;
		int minValue = Integer.MAX_VALUE;
		for (MNKCell d: FC) {
			state = board.markCell(d.i, d.j);
			if(depth==0) {
				return 0;   //da mettere qua l'evaluation
			}
			if (state == lCond) {
				board.unmarkCell();
				return -1;
			}
			if (state == MNKGameState.DRAW) {
				board.unmarkCell();
				return 0;
			}
			int value = max(board, alpha, beta, depth - 1, distance_from_root + 1);
			minValue = Math.min(value, minValue);
			beta = Math.min(beta, minValue);
			board.unmarkCell();
			if (alpha >= beta) { 
				if(!killer.is_a_KM(d, distance_from_root)) {
					killer.insert_KM(d, 1, distance_from_root);          //inserisco la killer move
				}
				else killer.change_weight(d, - 1, distance_from_root);      //mpssa buona 
				break;
			}
			else {
				if(killer.is_a_KM(d, distance_from_root)) {
					killer.change_weight(d, + 1, distance_from_root);       //la mossa era scarsotta perchè non ha fatto cut off quindi abbassiamo la priorità
				}
			}
		}
		return minValue;
	}

}