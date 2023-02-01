package player;

import mnkgame.MNKBoard;
import java.util.*;


import mnkgame.MNKCell;
import mnkgame.MNKGameState;

public class alphabeta{
	MNKGameState wCond;
	MNKGameState lCond;
	killer_heuristic killer;
	Transposition_table TT;
	long key;
	long startingTime;
	int time_span = 300;
	int depth_span = 1;
	int starting_depth = 1;
	
	public alphabeta(MNKGameState wc, MNKGameState lc, boolean first) {
		
		//saving the win conditions
		wCond = wc;
		lCond = lc;
	}
	
	public void firstIterative(GameBoard board, int maxDepth, Transposition_table TT, killer_heuristic killer,  int distance_from_root, EvaluationTool eval, long startTime, long key) {
		this.TT = TT;
		this.killer = killer;
		this.key = key;
		
		startingTime = startTime;
		int depth = starting_depth;
	
		while (!outOfTime() && depth < maxDepth + 1) {
			//System.out.println("sto facendo ID e sono a depth " + depth);
			int value = min(board, eval.MIN_EVALUATION, eval.MAX_EVALUATION, depth, distance_from_root, eval, true, key);
			depth += depth_span;
		}
		//System.out.println("Sono arrivato fino a depth: " + depth);
	}
	
	public MNKCell iterativeDeepening(GameBoard board, MNKCell[] FC, int maxDepth, Transposition_table TT, killer_heuristic killer,  int distance_from_root, EvaluationTool eval, long startTime, long key){
		//this.TT = TT;
		
		this.key = key;
		int depth = starting_depth;
		startingTime = startTime;
		
		MNKCell previousBestCell = FC[0];
		memory history;
		
		history = TT.gain_score(key, depth);
		if (history.score != TT.ScoreNotFound) {
			System.out.println("ho preso lo score!" + history.i + " " + history.j);
			MNKCell tempCell = new MNKCell (history.i, history.j);
			previousBestCell = tempCell;
			killer.insert_KM(previousBestCell, killer.get_first_KM_weight(distance_from_root), history.distance_from_root, true);          //inserisco la previousBestCell nelle mosse killer, metto -4 perch� � molto forte rispetto a una semplice mossa che fa pruning
			if (history.incompleteLevel)
				depth = history.depth - 1;
			else
				depth = history.depth;
		}
		
		//pre-ordering moves through killer heuristic
		int size = FC.length;
		killer.move_ordering(FC, size, distance_from_root);
		//variables for behaving as the max node
		MNKCell selected_cell = FC[0];
		boolean previousEvaluated = false;
		boolean allEvalEqual = true;
		int calculatedMoves = 0;
		
		while (!outOfTime() && depth < maxDepth) {
			//System.out.println("Faccio iterative a depth: " + depth);
			previousEvaluated = false;
			allEvalEqual = true;
			selected_cell = FC[0];
			int alpha = eval.MIN_EVALUATION;
			int beta = eval.MAX_EVALUATION;
			for (int i = 0; i< size; i++) {
				MNKCell d = FC[i];
				board.markCell(d.i, d.j);					
				eval.addSymbol(d.i, d.j, true);
				key = TT.generate_key(key, d.i, d.j, MNKPlayer.ourState);
				int value = min(board, alpha, beta, depth, distance_from_root + 1, eval, true, key);
				//System.out.println("La mossa: " + d.i + " " + d.j + " ha dato valutazione: " + value);
				key = TT.undo_key(key, d.i, d.j, MNKPlayer.ourState);
				if (outOfTime()) {
					board.unmarkCell();								//remove the cell and iterate again
			    	eval.removeSymbol(d.i, d.j, true);
			    	calculatedMoves = i;
					break;
				}
				if (d.i == previousBestCell.i && d.j == previousBestCell.j) {
					previousEvaluated = true;
				}
				if (value > alpha){
					selected_cell = d;
					alpha = value;
				}
				board.unmarkCell();								//remove the cell and iterate again
		    	eval.removeSymbol(d.i, d.j, true);
		    	if (alpha >= beta)
		    		break;
			}
			if (outOfTime()){
				if (!previousEvaluated)
					selected_cell = previousBestCell;
			}
			else	
				previousBestCell = selected_cell;
			depth += depth_span;
		}

	
		return selected_cell;
	}
	

	
	//ALPHABETA CON TT E KILLER
	protected int max(GameBoard board, int alpha, int beta, int depth, int distance_from_root, EvaluationTool eval, boolean saveNode, long key) {
		if(depth==0) {
			int evaluation = eval.evaluation(board, true);
			return evaluation;
		}
		
		MNKCell[] FC = board.getInterestingCells();
		int lenght = FC.length;
		killer.move_ordering(FC, lenght, distance_from_root);
		
		MNKGameState state;
		MNKCell bestCell = FC[0];
		for (int i = 0; i< lenght; i++) {
			MNKCell d = FC[i];
			state = board.markCell(d.i, d.j);	
			eval.addSymbol(d.i, d.j, true);
			key = TT.generate_key(key, d.i, d.j, MNKPlayer.ourState);
			if (state == wCond) {							//if it is a winning cell, return the best evaluation
				key = TT.undo_key(key, d.i, d.j, MNKPlayer.ourState);
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, true);
				return eval.MAX_EVALUATION;
			}
			if (state == MNKGameState.DRAW) {				//if it is a drawing cell, return the null evaluation
				key = TT.undo_key(key, d.i, d.j, MNKPlayer.ourState);
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, true);
				return 0;
			}
			int value = min(board, alpha, beta, depth - 1, distance_from_root + 1, eval, false, key);			//else recursive call and compare the evaluations
			key = TT.undo_key(key, d.i, d.j, MNKPlayer.ourState);
			if (outOfTime()) {
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, true);
				break;
			}
			if (value > alpha) {
				bestCell = d;
				alpha = value;
			}
			if (saveNode && i == lenght-1){
				if (depth >= 3) {
					TT.save_data(alpha, key, depth, bestCell.i, bestCell.j, false, distance_from_root);
				}
			}
			board.unmarkCell();
			eval.removeSymbol(d.i, d.j, true);
			if (alpha >= beta) { 
				if(!killer.is_a_KM(d, distance_from_root) ) {
					killer.insert_KM(d, 1, distance_from_root, false);          //inserisco la killer move
				}
				else if(killer.is_a_KM(d, distance_from_root) ) {
					killer.change_weight(d, - 1, distance_from_root);  //mossa buona 
				}
				if (saveNode && depth >= 3) {
	                TT.save_data(alpha, key, depth, bestCell.i, bestCell.j, false, distance_from_root);
				}
				break;
			}
			else {
				if(killer.is_a_KM(d, distance_from_root)) {
					killer.change_weight(d, + 1, distance_from_root);       //la mossa era scarsotta perchè non ha fatto cut off quindi abbassiamo la priorità
				}
				
			}
		}
		return alpha;
	}
	
	
	protected int min(GameBoard board, int alpha, int beta, int depth, int distance_from_root, EvaluationTool eval, boolean saveNode, long key) {
		if(depth==0) {
			int evaluation = eval.evaluation(board, false);
			return evaluation;
		}
		
		MNKCell[] FC = board.getInterestingCells();
		int lenght = FC.length;
		killer.move_ordering(FC, lenght, distance_from_root);
		MNKGameState state;
		//int minValue = Integer.MAX_VALUE;
		
		for (int i = 0; i< lenght; i++) {
			MNKCell d = FC[i];
			state = board.markCell(d.i, d.j);
			eval.addSymbol(d.i, d.j, false);
			key = TT.generate_key(key, d.i, d.j, MNKPlayer.enemyState);
			if (state == lCond) {
				key = TT.undo_key(key, d.i, d.j, MNKPlayer.enemyState);
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, false);
				return eval.MIN_EVALUATION;
			}
			if (state == MNKGameState.DRAW) {
				key = TT.undo_key(key, d.i, d.j, MNKPlayer.enemyState);
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, false);
				return 0;
			}
			int value = max(board, alpha, beta, depth - 1, distance_from_root + 1, eval, saveNode, key);
			key = TT.undo_key(key, d.i, d.j, MNKPlayer.enemyState);
			if (outOfTime()) {
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, false);
				break;
			}
			if (value < beta) {
				beta = value;
			}
			board.unmarkCell();
			eval.removeSymbol(d.i, d.j, false);
			if (alpha >= beta) { 
				if(!killer.is_a_KM(d, distance_from_root) ) {
					killer.insert_KM(d, 1, distance_from_root, false);          //inserisco la killer move
				}
				else if(killer.is_a_KM(d, distance_from_root) ) {
					killer.change_weight(d, - 1, distance_from_root);  //mpssa buona 
				}
				break;
			}
			else {
				if(killer.is_a_KM(d, distance_from_root)) {
					killer.change_weight(d, + 1, distance_from_root);       //la mossa era scarsotta perchè non ha fatto cut off quindi abbassiamo la priorità
				}
				
			}
		}
		return beta;
	}
	
	protected boolean outOfTime() {
		return ((startingTime + MNKPlayer.timeout - time_span) < System.currentTimeMillis());
	}
	
	protected MNKCell smartestCell(MNKCell a, MNKCell b){
		int dangerA = MNKPlayer.threatBoard[a.i][a.j];
		int dangerB = MNKPlayer.threatBoard[b.i][b.j];

		if (dangerA < dangerB)
			return b;
		else
			return a;
	}
	//-----------
	//ALPHABETA CON KILLER
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
			eval.addSymbol(d.i, d.j, true);
			if (state == wCond) {							//if it is a winning cell, return the best evaluation
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, true);
				return 1;
			}
			if (state == MNKGameState.DRAW) {				//if it is a drawing cell, return the null evaluation
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, true);
				return 0;
			}
			int value = min(board, alpha, beta, eval);			//else recursive call and compare the evaluations
			maxValue = Math.max(value, maxValue);
			alpha = Math.max(alpha, maxValue);
			board.unmarkCell();
			eval.removeSymbol(d.i, d.j, true);
			if (alpha >= beta) break;
		}
		return maxValue;
	}
	
	protected int min(MNKBoard board, int alpha, int beta, EvaluationTool eval) {
		MNKCell[] FC = board.getFreeCells();
		int size = FC.length;
		if(killer.deep_enough(distance_from_root))
			killer.move_ordering(FC, size, distance_from_root);
		MNKGameState state;
		int minValue = Integer.MAX_VALUE;
		for (MNKCell d: FC) {
			state = board.markCell(d.i, d.j);
			eval.addSymbol(d.i, d.j, false);
			if (state == lCond) {
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, false);
				return -1;
			}
			if (state == MNKGameState.DRAW) {
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, false);
				return 0;
			}
			int value = max(board, alpha, beta, eval);
			minValue = Math.min(value, minValue);
			beta = Math.min(beta, minValue);
			board.unmarkCell();
			eval.removeSymbol(d.i, d.j, false);
			if (alpha >= beta) break;
		}
		return minValue;
	}
	*/
	
	
	
	
	
	//------------------------
	//NEGASCOUT CON TT E KILLER
	/*
	protected int NegaScoutmax(MNKBoard board, int alpha, int beta, int depth, int distance_from_root, boolean isMax) {
		MNKGameState Cond;
		if(isMax) {
			Cond = wCond;
		}
		else {
			Cond = lCond;
		}
		
		MNKCell[] FC = board.getFreeCells();
		int lenght = FC.length;
		if(killer.deep_enough(distance_from_root))
			killer.move_ordering(FC, lenght, distance_from_root);
		MNKGameState state;
		int maxValue = Integer.MIN_VALUE;
		int b = beta;
		for (int i = 0; i< lenght; i++) {
			MNKCell d = FC[i];
			state = board.markCell(d.i, d.j);	
			key = TT.generate_key(key, d.i, d.j, board.cellState(d.i, d.j));
			if(depth==0) {
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
			if (state == Cond) {							//if it is a winning cell, return the best evaluation
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
			int value = -NegaScoutmax(board, -alpha, -b, depth - 1, distance_from_root + 1, !isMax);			//else recursive call and compare the evaluations
			if(value > alpha && value < beta && i>0 && depth>1) {  //research e non sei nel primo figlio
				System.out.println("research");
				value = -NegaScoutmax(board, -value, -beta, depth - 1, distance_from_root + 1, !isMax);
			}
			key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
			maxValue = Math.max(value, maxValue);
			alpha = Math.max(alpha, value);
			board.unmarkCell();
			
			if (alpha >= beta) { 
				if(killer.deep_enough(distance_from_root) && !killer.is_a_KM(d, distance_from_root) ) {
					killer.insert_KM(d, 1, distance_from_root);          //inserisco la killer move
				}
				else if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root) ) {
					killer.change_weight(d, - 1, distance_from_root);  //mpssa buona 
				}
				return alpha;
			}
			else {
				if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root)) {
					killer.change_weight(d, + 1, distance_from_root);       //la mossa era scarsotta perchè non ha fatto cut off quindi abbassiamo la priorità
				}
			}
			b = alpha + 1;       //metti una nuova finestra null
		}
		return maxValue;
	}
	*/
	//---------------------
	

  	//------------------------
	protected int NegaScoutmax(MNKBoard board, int alpha, int beta, int depth, int distance_from_root, boolean isMax) {
		MNKGameState Cond;
		if(isMax) {
			Cond = wCond;
		}
		else {
			Cond = lCond;
		}
		if(depth==0) {
			return 0;   //da mettere qua l'evaluation
		}
		MNKCell[] FC = board.getFreeCells();
		int lenght = FC.length;
		MNKGameState state;
		int maxValue = Integer.MIN_VALUE;
		int b = beta;
		for (int i = 0; i< lenght; i++) {
			MNKCell d = FC[i];
			state = board.markCell(d.i, d.j);	
			if (state == Cond) {							//if it is a winning cell, return the best evaluation
				board.unmarkCell();
				return 1;
			}
			if (state == MNKGameState.DRAW) {				//if it is a drawing cell, return the null evaluation
				board.unmarkCell();
				return 0;
			}
			int value = -NegaScoutmax(board, -alpha, -b, depth - 1, distance_from_root + 1, !isMax);			//else recursive call and compare the evaluations
			if(value > alpha && value < beta && i>0 && depth>1) {  //research e non sei nel primo figlio
				System.out.println("research");
				value = -NegaScoutmax(board, -value, -beta, depth - 1, distance_from_root + 1, !isMax);
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
	
	//---------------------
	
	
	
	//-------------------
	//Alphabeta con TT
	/*
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
	*/
	//---------------
	
	
	

}
