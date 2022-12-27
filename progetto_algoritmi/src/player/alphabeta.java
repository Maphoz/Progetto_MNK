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
	long key;
	Transposition_table TT;
	
	public alphabeta(MNKGameState wc, MNKGameState lc) {
		this.killer = new killer_heuristic();
		//saving the win conditions
		wCond = wc;
		lCond = lc;
	}
	
	public int alphaBeta(MNKBoard board, boolean maximizer, int depth, Transposition_table TT, int distance_from_root, long key) {
		this.key = key;
		this.TT = TT;
		//the move is being tried by the player class, so we look for opponent best response
		return min(board, Integer.MIN_VALUE, Integer.MAX_VALUE, depth, distance_from_root);
	}
	/*
	protected int max(MNKBoard board, int alpha, int beta, int depth, int distance_from_root) {
		
		MNKCell[] FC = board.getFreeCells();
		int size = FC.length;
		if(killer.deep_enough(distance_from_root))
			killer.move_ordering(FC, size, distance_from_root);
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
				if(killer.deep_enough(distance_from_root) && !killer.is_a_KM(d, distance_from_root) ) {
					//System.out.println("inserisco");
					killer.insert_KM(d, 1, distance_from_root);          //inserisco la killer move
				}
				else if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root) ) {
					//System.out.println("cambio weight in max");
					killer.change_weight(d, - 1, distance_from_root);  //mpssa buona 
				}
				break;
			}
			else {
				if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root)) {
					//System.out.println("diminuisco");
					killer.change_weight(d, + 1, distance_from_root);       //la mossa era scarsotta perchè non ha fatto cut off quindi abbassiamo la priorità
				}
				
			}
		}
		return maxValue;
	}
	
	protected int min(MNKBoard board, int alpha, int beta, int depth, int distance_from_root) {
		MNKCell[] FC = board.getFreeCells();
		int size = FC.length;
		if(killer.deep_enough(distance_from_root))
			killer.move_ordering(FC, size, distance_from_root);
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
				if(killer.deep_enough(distance_from_root) && !killer.is_a_KM(d, distance_from_root)) {
					//System.out.println("inserisco");
					killer.insert_KM(d, 1, distance_from_root);          //inserisco la killer move
				}
				else if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root)) {
					//System.out.println("cambio weight in max");
					killer.change_weight(d, - 1, distance_from_root);      //mpssa buona
				}
				break;
			}
			else {
				if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root)) {
					//System.out.println("diminuisco");
					killer.change_weight(d, + 1, distance_from_root);       //la mossa era scarsotta perchè non ha fatto cut off quindi abbassiamo la priorità
				}
			}
		}
		return minValue;
	}*/
	
	
	//------------------------
	protected int NegaScoutmax(MNKBoard board, int alpha, int beta, int depth, int distance_from_root) {
		System.out.println("bella");
		MNKCell[] FC = board.getFreeCells();
		int lenght = FC.length;
		MNKGameState state;
		int maxValue = Integer.MIN_VALUE;
		int b = beta;
		for (int i = 0; i< lenght; i++) {
			MNKCell d = FC[i];
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
			int value = -NegaScoutmax(board, -alpha, -b, depth - 1, distance_from_root + 1);			//else recursive call and compare the evaluations
			if(value > alpha && value < beta && i>0 && depth>1) {  //research e non sei nel primo figlio
				System.out.println("research");
				value = -NegaScoutmax(board, -value, -beta, depth - 1, distance_from_root + 1);
			}
			maxValue = Math.max(value, maxValue);
			alpha = Math.max(alpha, value);
			board.unmarkCell();
			if (alpha >= beta) { 
				return alpha;
				//break;
			}
			b = alpha + 1;       //metti una nuova finestra null
		}
		return maxValue;
	}
	
	protected int NegaScoutmin(MNKBoard board, int alpha, int beta, int depth, int distance_from_root) {
		System.out.println("sbaglio");
		MNKCell[] FC = board.getFreeCells();
		int lenght = FC.length;
		int b = beta;
		MNKGameState state;
		int minValue = Integer.MAX_VALUE;
		for (int i = 0; i< lenght; i++) {
			MNKCell d = FC[i];
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
			int value = NegaScoutmax(board, alpha, b, depth - 1, distance_from_root + 1);
			if(value > alpha && value < beta && i>0 && depth>1) {  //research e non sei nel primo figlio
				value = NegaScoutmin(board, value, beta, depth - 1, distance_from_root + 1);
			}
			minValue = Math.min(value, minValue);
			beta = Math.min(beta, value);
			board.unmarkCell();
			if (alpha >= beta) { 
				return beta;
				//break;
			}
			b = alpha + 1;
		}
		return minValue;
	}
	
	
	
	//---------------------
	
	
	
	//-------------------
	protected int max(MNKBoard board, int alpha, int beta, int depth, int distance_from_root) {
		
		MNKCell[] FC = board.getFreeCells();
		int lenght = FC.length;
		MNKGameState state;
		int maxValue = Integer.MIN_VALUE;
		for (int i = 0; i< lenght; i++) {
			MNKCell d = FC[i];
			state = board.markCell(d.i, d.j);	
			key = TT.generate_key(key, d.i, d.j, board.cellState(d.i, d.j));
			if(depth==0) {

				int score = TT.gain_score(key);
				
				if(score==TT.ScoreNotFound) {
					int evaluation = 0;			//da mettere qua l'evaluation
					TT.save_data(evaluation, key);
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return evaluation;
				}				
				else {
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return score;  
				}
			}
			if (state == wCond) {							//if it is a winning cell, return the best evaluation
				int score = TT.gain_score(key);
				
				if(score==TT.ScoreNotFound) {
					int evaluation = 1;
					TT.save_data(evaluation, key);
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return evaluation;
				}			
				else {
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return score;  
				}
			}
			if (state == MNKGameState.DRAW) {				//if it is a drawing cell, return the null evaluation
				int score = TT.gain_score(key);
				if(score==TT.ScoreNotFound) {
					int evaluation = 0;
					TT.save_data(evaluation, key);
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return evaluation;
				}				
				else {
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return score;  
				}
			}
			int value = min(board, alpha, beta, depth - 1, distance_from_root + 1);			//else recursive call and compare the evaluations
			maxValue = Math.max(value, maxValue);
			alpha = Math.max(alpha, maxValue);
			key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
			board.unmarkCell();
			if (alpha >= beta) { 
				break;
			}
		}
		return maxValue;
	}
	
	
	protected int min(MNKBoard board, int alpha, int beta, int depth, int distance_from_root) {
		
		MNKCell[] FC = board.getFreeCells();
		int lenght = FC.length;
		MNKGameState state;
		int minValue = Integer.MAX_VALUE;
		for (int i = 0; i< lenght; i++) {
			MNKCell d = FC[i];
			state = board.markCell(d.i, d.j);
			key = TT.generate_key(key, d.i, d.j, board.cellState(d.i, d.j));
			if(depth==0) {

				int score = TT.gain_score(key);
				if(score==TT.ScoreNotFound) {
					int evaluation = 0;			//da mettere qua l'evaluation
					TT.save_data(evaluation, key);
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return evaluation;
				}				
				else {
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return score;  
				}
			}
			if (state == lCond) {

				int score = TT.gain_score(key);
				if(score==TT.ScoreNotFound) {
					int evaluation = -1;
					TT.save_data(evaluation, key);
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return evaluation;
				}			
				else {
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return score;  
				}
			}
			if (state == MNKGameState.DRAW) {

				int score = TT.gain_score(key);
				if(score==TT.ScoreNotFound) {
					int evaluation = 0;
					TT.save_data(evaluation, key);
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return evaluation;
				}				
				else {
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();
					return score;  
				}
			}
			int value = max(board, alpha, beta, depth - 1, distance_from_root + 1);
			minValue = Math.min(value, minValue);
			beta = Math.min(beta, minValue);
			key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
			board.unmarkCell();
			if (alpha >= beta) { 
				break;
			}
		}
		return minValue;
	}
	
	//---------------
	
	
	

}