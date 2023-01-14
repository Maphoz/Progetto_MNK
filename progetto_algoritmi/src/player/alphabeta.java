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
	int time_span = 500;
	int depth_span = 1;
	int starting_depth = 1;
	
	public alphabeta(MNKGameState wc, MNKGameState lc, boolean first) {
		
		//saving the win conditions
		wCond = wc;
		lCond = lc;
	}
	
	public void firstIterative(MNKBoard board, MNKCell[] FC, int maxDepth, Transposition_table TT, killer_heuristic killer,  int distance_from_root, EvaluationTool eval, long startTime, long key) {
		this.TT = TT;
		this.killer = killer;
		this.key = key;
		
		startingTime = startTime;
		int depth = starting_depth;
	
		while (!outOfTime() && depth < maxDepth + 1) {
				System.out.println("sto facendo ID e sono a depth " + depth);
			int value = min(board, Integer.MIN_VALUE, Integer.MAX_VALUE, depth, distance_from_root + 1, eval);
			depth += depth_span;
		}
		System.out.println("Sono arrivato fino a depth: " + depth);
	}
	
	public MNKCell iterativeDeepening(MNKBoard board, MNKCell[] FC, int maxDepth, Transposition_table TT, killer_heuristic killer,  int distance_from_root, EvaluationTool eval, long startTime, long key){
		this.key = key;
		
		int depth = starting_depth;
		startingTime = startTime;
		//pre-ordering moves through killer heuristic
		int size = FC.length;
		if(killer.deep_enough(distance_from_root)) {
			killer.move_ordering(FC, size, distance_from_root);
		}
		
		
		MNKCell previousBestCell = FC[0];
		int previousBestValue;
		memory history;
		history = TT.gain_score(key, depth);
		if (history.score != TT.ScoreNotFound) {
			System.out.println("ho salvato qualcosa!");
			previousBestValue = history.score;
			MNKCell tempCell = new MNKCell (history.i, history.j);
			previousBestCell = tempCell;
			System.out.println("Best cell: " + previousBestCell.i + " " + previousBestCell.j + " tempCell: " + tempCell.i + " " + tempCell.j);
			depth = history.depth;
		}
		
		System.out.println("parto a fare l'iterative deepening a partire dalla depth: " + depth);

		MNKCell selected_cell = FC[0];
		boolean previousEvaluated = false;
		while (!outOfTime() && depth < maxDepth + 1) {
			System.out.println("Sto facendo iterative a depth: " + depth);
			int best_value = Integer.MIN_VALUE;
			previousEvaluated = false;
			selected_cell = FC[0];
			for (int i = 0; i< size; i++) {
				MNKCell d = FC[i];
				board.markCell(d.i, d.j);					
				eval.addSymbol(d.i, d.j, true);
				key = TT.generate_key(key, d.i, d.j, board.cellState(d.i, d.j));
				if (outOfTime()) {
					key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
					board.unmarkCell();								//remove the cell and iterate again
			    	eval.removeSymbol(d.i, d.j, true);
					break;
				}
				if (d.i == previousBestCell.i && d.j == previousBestCell.j) {
					previousEvaluated = true;
				}
				int value = min(board, Integer.MIN_VALUE, Integer.MAX_VALUE, depth, distance_from_root + 1, eval);
				//System.out.println("La mossa: " + d.i + " " + d.j + " ha dato valutazione: " + value);
				if (value == best_value){
					selected_cell = smartestCell(selected_cell, d);
				}
				else if (value > best_value){
					selected_cell = d;
					best_value = value;
				}
				key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
				board.unmarkCell();								//remove the cell and iterate again
		    	eval.removeSymbol(d.i, d.j, true);
			}
			if (outOfTime()) {
				if (!previousEvaluated) {
					if (best_value < eval.MAX_EVALUATION) {
						selected_cell = previousBestCell;
					}
				}
				else {
					if (best_value == eval.MIN_EVALUATION && best_value == eval.MAX_EVALUATION) {
						selected_cell = previousBestCell;
					}
				}
				break;
			}
			if (best_value != eval.MIN_EVALUATION && best_value != eval.MAX_EVALUATION) {
				previousBestCell = selected_cell;
				previousBestValue = best_value;
			}
			depth += depth_span;
		}
		System.out.println("Sono arrivato fino a depth: " + depth);
		return selected_cell;
	}
	
	
	/*public int alphaBeta(MNKBoard board, int depth, Transposition_table TT, killer_heuristic killer,  int distance_from_root, long key, EvaluationTool eval) {
		this.key = key;
		this.TT = TT;
		this.killer = killer;

		//the move is being tried by the player class, so we look for opponent best response
		return min(board, Integer.MIN_VALUE, Integer.MAX_VALUE, depth, distance_from_root, eval);
		//return NegaScoutmax(board, Integer.MAX_VALUE, Integer.MIN_VALUE, depth, distance_from_root, false);

	}
	*/
	//---------
	//ALPHABETA CON TT E KILLER
	protected int max(MNKBoard board, int alpha, int beta, int depth, int distance_from_root, EvaluationTool eval) {
		if(depth==0) {
			int evaluation = eval.evaluation(board, true);
			//TT.save_data(evaluation, key, depth);
			return evaluation;
		}
		
		MNKCell[] FC = board.getFreeCells();
		int lenght = FC.length;
		if(killer.deep_enough(distance_from_root)) {
			killer.move_ordering(FC, lenght, distance_from_root);
		}
		MNKGameState state;
		MNKCell bestCell = FC[0];
		long bestKey = 0;
		int maxValue = Integer.MIN_VALUE;
		for (int i = 0; i< lenght; i++) {
			MNKCell d = FC[i];
			//System.out.println("Sto testando la cella nella chiamata ricorsiva di max: " + d.i + " " + d.j);
			state = board.markCell(d.i, d.j);	
			eval.addSymbol(d.i, d.j, true);
			key = TT.generate_key(key, d.i, d.j, board.cellState(d.i, d.j));
			if (state == wCond) {							//if it is a winning cell, return the best evaluation
				//TT.save_data(eval.MAX_EVALUATION, key, depth);
				key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, true);
				return eval.MAX_EVALUATION;
			}
			if (state == MNKGameState.DRAW) {				//if it is a drawing cell, return the null evaluation
				//TT.save_data(0, key, depth);
				key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, true);
				return 0;
			}
			if (outOfTime()) {
				key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
				TT.save_data(maxValue, bestKey, depth, bestCell.i, bestCell.j);
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, true);
				break;
			}
			int value = min(board, alpha, beta, depth - 1, distance_from_root + 1, eval);			//else recursive call and compare the evaluations
			key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
			if (value > maxValue) {
				bestCell = d;
				maxValue = value;
				bestKey = key;
			}
			alpha = Math.max(alpha, maxValue);
			
			if (i == lenght-1){
				//System.out.println("Sono nella chiamata max e sto salvando la miglior cella per me: " + bestCell.i + " " + bestCell.j);
				TT.save_data(maxValue, bestKey, depth, bestCell.i, bestCell.j);
			}
			board.unmarkCell();
			eval.removeSymbol(d.i, d.j, true);
			if (alpha >= beta) { 
				if(killer.deep_enough(distance_from_root) && !killer.is_a_KM(d, distance_from_root) ) {
					killer.insert_KM(d, 1, distance_from_root);          //inserisco la killer move
				}
				else if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root) ) {
					killer.change_weight(d, - 1, distance_from_root);  //mpssa buona 
				}
				//System.out.println("Sono nella chiamata max e sto prunando la miglior cella per me: " + bestCell.i + " " + bestCell.j);
				TT.save_data(maxValue, bestKey, depth, bestCell.i, bestCell.j);
				break;
			}
			else {
				if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root)) {
					killer.change_weight(d, + 1, distance_from_root);       //la mossa era scarsotta perchè non ha fatto cut off quindi abbassiamo la priorità
				}
				
			}
		}
		
		return maxValue;
	}
	
	
	protected int min(MNKBoard board, int alpha, int beta, int depth, int distance_from_root, EvaluationTool eval) {
		if(depth==0) {
			int evaluation = eval.evaluation(board, false);
			//TT.save_data(evaluation, key, depth);
			return evaluation;
		}
		
		MNKCell[] FC = board.getFreeCells();
		int lenght = FC.length;
		if(killer.deep_enough(distance_from_root))
			killer.move_ordering(FC, lenght, distance_from_root);
		MNKGameState state;
		MNKCell bestCell = FC[0];
		int minValue = Integer.MAX_VALUE;
		long bestKey = 0;
		for (int i = 0; i< lenght; i++) {
			MNKCell d = FC[i];
			//System.out.println("Sto testando la cella: " + d.i + " " + d.j);
			state = board.markCell(d.i, d.j);
			eval.addSymbol(d.i, d.j, false);
			key = TT.generate_key(key, d.i, d.j, board.cellState(d.i, d.j));
			if (state == lCond) {
				//TT.save_data(eval.MIN_EVALUATION, key, depth);
				key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, false);
				return eval.MIN_EVALUATION;
			}
			if (state == MNKGameState.DRAW) {
				//TT.save_data(0, key, depth);
				key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, false);
				return 0;
			}
			if (outOfTime()) {
				key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
				TT.save_data(minValue, bestKey, depth, bestCell.i, bestCell.j);	
				board.unmarkCell();
				eval.removeSymbol(d.i, d.j, false);
				break;
			}
			int value = max(board, alpha, beta, depth - 1, distance_from_root + 1, eval);
			key = TT.undo_key(key, d.i, d.j, board.cellState(d.i, d.j));
			if (value < minValue) {
				bestCell = d;
				minValue = value;
				bestKey = key;
			}
			beta = Math.min(beta, minValue);
			
			if (i == lenght - 1)
				TT.save_data(minValue, bestKey, depth, bestCell.i, bestCell.j);
			board.unmarkCell();
			eval.removeSymbol(d.i, d.j, false);
			if (alpha >= beta) { 
				if(killer.deep_enough(distance_from_root) && !killer.is_a_KM(d, distance_from_root) ) {
					killer.insert_KM(d, 1, distance_from_root);          //inserisco la killer move
				}
				else if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root) ) {
					killer.change_weight(d, - 1, distance_from_root);  //mpssa buona 
				}
				//System.out.println("Sono nella chiamata max e sto prunando la miglior cella per me: " + bestCell.i + " " + bestCell.j);
				TT.save_data(minValue, bestKey, depth, bestCell.i, bestCell.j);
				break;
			}
			else {
				if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root)) {
					killer.change_weight(d, + 1, distance_from_root);       //la mossa era scarsotta perchè non ha fatto cut off quindi abbassiamo la priorità
				}
				
			}
		}
		return minValue;
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
<<<<<<< HEAD
			state = board.markCell(d.i, d.j);
			eval.addSymbol(d.i, d.j, true);
=======
			state = board.markCell(d.i, d.j);	
			if(depth==0) {
				board.unmarkCell();
				return 0;   //da mettere qua l'evaluation
			}
>>>>>>> branch 'master' of https://github.com/Maphoz/Progetto_MNK.git
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
<<<<<<< HEAD
			int value = min(board, alpha, beta, eval);			//else recursive call and compare the evaluations
=======
			int value = min(board, alpha, beta, depth - 1, distance_from_root + 1);			//else recursive call and compare the evaluations
>>>>>>> branch 'master' of https://github.com/Maphoz/Progetto_MNK.git
			maxValue = Math.max(value, maxValue);
			alpha = Math.max(alpha, maxValue);
			board.unmarkCell();
<<<<<<< HEAD
			eval.removeSymbol(d.i, d.j, true);
			if (alpha >= beta) break;
=======
			if (alpha >= beta) { 
				if(killer.deep_enough(distance_from_root) && !killer.is_a_KM(d, distance_from_root) ) {
					killer.insert_KM(d, 1, distance_from_root);          //inserisco la killer move
				}
				else if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root) ) {
					killer.change_weight(d, - 1, distance_from_root);  //mpssa buona 
				}
				break;
			}
			else {
				if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root)) {
					killer.change_weight(d, + 1, distance_from_root);       //la mossa era scarsotta perchè non ha fatto cut off quindi abbassiamo la priorità
				}
				
			}
>>>>>>> branch 'master' of https://github.com/Maphoz/Progetto_MNK.git
		}
		return maxValue;
	}
	
<<<<<<< HEAD
	protected int min(MNKBoard board, int alpha, int beta, EvaluationTool eval) {
=======
	protected int min(MNKBoard board, int alpha, int beta, int depth, int distance_from_root) {

>>>>>>> branch 'master' of https://github.com/Maphoz/Progetto_MNK.git
		MNKCell[] FC = board.getFreeCells();
		int size = FC.length;
		if(killer.deep_enough(distance_from_root))
			killer.move_ordering(FC, size, distance_from_root);
		MNKGameState state;
		int minValue = Integer.MAX_VALUE;
		for (MNKCell d: FC) {
			state = board.markCell(d.i, d.j);
<<<<<<< HEAD
			eval.addSymbol(d.i, d.j, false);
=======
			if(depth==0) {
				board.unmarkCell();
				return 0;   //da mettere qua l'evaluation
			}
>>>>>>> branch 'master' of https://github.com/Maphoz/Progetto_MNK.git
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
<<<<<<< HEAD
			int value = max(board, alpha, beta, eval);
=======
			int value = max(board, alpha, beta, depth - 1, distance_from_root + 1);
>>>>>>> branch 'master' of https://github.com/Maphoz/Progetto_MNK.git
			minValue = Math.min(value, minValue);
			beta = Math.min(beta, minValue);
			board.unmarkCell();
<<<<<<< HEAD
			eval.removeSymbol(d.i, d.j, false);
			if (alpha >= beta) break;
=======
			if (alpha >= beta) { 
				if(killer.deep_enough(distance_from_root) && !killer.is_a_KM(d, distance_from_root)) {
					killer.insert_KM(d, 1, distance_from_root);          //inserisco la killer move
				}
				else if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root)) {
					killer.change_weight(d, - 1, distance_from_root);      //mossa buona
				}
				break;
			}
			else {
				if(killer.deep_enough(distance_from_root) && killer.is_a_KM(d, distance_from_root)) {
					killer.change_weight(d, + 1, distance_from_root);       //la mossa era scarsotta perchè non ha fatto cut off quindi abbassiamo la priorità
				}
			}
>>>>>>> branch 'master' of https://github.com/Maphoz/Progetto_MNK.git
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
