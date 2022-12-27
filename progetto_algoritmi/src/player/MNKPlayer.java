package player;

import java.util.*;

import mnkgame.MNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKGameState;


public class MNKPlayer implements mnkgame.MNKPlayer {
	MNKBoard myBoard;
	MNKGameState winCondition;
	MNKGameState losCondition;
	alphabeta solver;
	int timeout;
	boolean FirstTurn;
	Random rand;
	Transposition_table TT;
	int distance_from_root;
	long key;

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		this.key = (long)0;
		distance_from_root = 0;
		this.TT = new Transposition_table(M,N);
		TT.initTableRandom();
		rand = new Random(System.currentTimeMillis()); 
		FirstTurn=true;
		myBoard = new MNKBoard (M, N, K);
		
		//saving the timeout in milliseconds
		this.timeout = timeout_in_secs*1000;
		
		//identifying the win conditions for me and my opponent
		if (first) {
			winCondition = MNKGameState.WINP1;
			losCondition = MNKGameState.WINP2;
		}
		else {
			winCondition = MNKGameState.WINP2;
			losCondition = MNKGameState.WINP1;
		}
		//instance of the alphabeta class to solve the problem
		solver = new alphabeta(winCondition, losCondition);
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		distance_from_root++;
		//starting the time count
		long startTime = System.currentTimeMillis();
		long currentTime = startTime;
		
		//adding to my board representation the last move played by the opponent
		if (MC.length != 0) {
			myBoard.markCell(MC[MC.length - 1].i, MC[MC.length - 1].j);
			key = TT.generate_key(key, MC[MC.length - 1].i, MC[MC.length - 1].j, MC[MC.length - 1].state);
		}
		
		if(FirstTurn) {
			MNKCell selected_move = FC[rand.nextInt(FC.length)];
			myBoard.markCell(selected_move.i,selected_move.j);
			key = TT.generate_key(key, selected_move.i, selected_move.j, myBoard.cellState(selected_move.i, selected_move.j));
			int value = solver.alphaBeta(myBoard, true, 10, TT, distance_from_root, key);			//fai un alpha beta con una depth pi� grande perch� hai pi� tempo
			FirstTurn = false;
			return selected_move;
		}
		//checking if there are any winning moves
		for (int k = 0; k < FC.length; k++) {
			if (myBoard.markCell(FC[k].i, FC[k].j) == winCondition)
				return FC[k];
			else
				myBoard.unmarkCell();
		}
		
		//selecting my move
		MNKCell selected_move = FC[0];
		int k = 0;
		double best_value = Double.NEGATIVE_INFINITY;			// we are always the maximizing player in this implementation
		int value;
		while (k < FC.length && currentTime < startTime + timeout - 200) {	
			myBoard.markCell(FC[k].i, FC[k].j);					//mark the cell we want to test
			key = TT.generate_key(key, FC[k].i, FC[k].j, myBoard.cellState(FC[k].i, FC[k].j));
			value = solver.alphaBeta(myBoard, true, 3, TT, distance_from_root, key);			//launch the alphabeta tree
			if (value > best_value) {							//if the move tried is better than the previous best one, swap
				best_value = value;
				selected_move = FC[k];
			}
			key = TT.undo_key(key, FC[k].i, FC[k].j, myBoard.cellState(FC[k].i, FC[k].j));
			myBoard.unmarkCell();								//remove the cell and iterate again
		    k++;
		    currentTime = System.currentTimeMillis();
		}
		myBoard.markCell(selected_move.i, selected_move.j);		//mark and return the best cell found
		key = TT.generate_key(key, selected_move.i, selected_move.j, myBoard.cellState(selected_move.i, selected_move.j));
		return selected_move;
	}

	@Override
	public String playerName() {
		return "Giusama";
	}

}
