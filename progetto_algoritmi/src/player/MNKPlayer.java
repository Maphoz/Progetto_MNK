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

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		
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
		//starting the time count
		long startTime = System.currentTimeMillis();
		long currentTime = startTime;
		
		//adding to my board representation the last move played by the opponent
		if (MC.length != 0) {
			myBoard.markCell(MC[MC.length - 1].i, MC[MC.length - 1].j);
		}
		
		if(FirstTurn) {
			MNKCell selected_move = FC[rand.nextInt(FC.length)];
			myBoard.markCell(selected_move.i,selected_move.j);
			int value;
			value = solver.alphaBeta(myBoard, true, 200);			//fai un alpha beta con una depth più grande perchè hai più tempo
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
			value = solver.alphaBeta(myBoard, true, 200);			//launch the alphabeta tree
			if (value > best_value) {							//if the move tried is better than the previous best one, swap
				best_value = value;
				selected_move = FC[k];
			}
			myBoard.unmarkCell();								//remove the cell and iterate again
		    k++;
		    currentTime = System.currentTimeMillis();
		}
		myBoard.markCell(selected_move.i, selected_move.j);		//mark and return the best cell found
		return selected_move;
	}

	@Override
	public String playerName() {
		return "Giusama";
	}

}
