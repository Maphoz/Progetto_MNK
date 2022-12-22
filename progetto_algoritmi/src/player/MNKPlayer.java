package player;

import java.util.*;

import mnkgame.MNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKGameState;

public class MNKPlayer implements mnkgame.MNKPlayer {
	MNKBoard myBoard;
	MNKGameState winningCondition;
	alphabeta solver;
	int timeout;
	boolean first;
	Calendar cal;

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		myBoard = new MNKBoard (M, N, K);
		
		this.first = first;
		this.timeout = timeout_in_secs*1000;
		
		if (first)
			winningCondition = MNKGameState.WINP1;
		else
			winningCondition = MNKGameState.WINP2;
		cal = Calendar.getInstance();
		solver = new alphabeta(first);
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		long startTime = cal.getTimeInMillis();
		long currentTime = startTime;
		//adding to my board representation the last move played by the adversary
		
		if (MC.length != 0) {
			myBoard.markCell(MC[MC.length - 1].i, MC[MC.length - 1].j);
		}
		
		//selecting my move
		MNKCell selected_move = FC[0];
		int k = 0;
		double best_value = Double.NEGATIVE_INFINITY;
		int value;
		while (k < FC.length && currentTime < startTime + timeout - 200) {						//add time control
			if (myBoard.markCell(FC[k].i, FC[k].j) == winningCondition)
				return FC[k];
			value = solver.alphaBeta(myBoard, true);
			if (value > best_value) {
				best_value = value;
				selected_move = FC[k];
			}
			myBoard.unmarkCell();
		    k++;
		    //update time
		}
		
		return selected_move;
	}

	@Override
	public String playerName() {
		return "Giusama";
	}

}
