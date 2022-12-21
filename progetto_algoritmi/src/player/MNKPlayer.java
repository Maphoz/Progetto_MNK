package player;

import mnkgame.MNKBoard;
import player.alphabeta;
import mnkgame.MNKCell;
import mnkgame.MNKGameState;

public class MNKPlayer implements mnkgame.MNKPlayer {
	MNKBoard myBoard;
	MNKGameState winningCondition;
	
	int timeout;
	boolean first;

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		myBoard = new MNKBoard (M, N, K);
		
		this.first = first;
		this.timeout = timeout_in_secs;
		
		if (first)
			winningCondition = MNKGameState.WINP1;
		else
			winningCondition = MNKGameState.WINP2;

	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		//adding to my board representation the last move played by the adversary
		
		if (MC.length != 0) {
			myBoard.markCell(MC[MC.length - 1].i, MC[MC.length - 1].j);
		}
		
		//selecting my move
		MNKCell selected_move = FC[0];
		int k = 0;
		double best_value = Double.NEGATIVE_INFINITY;
		int value;
		MNKGameState state;
		while (k < FC.length) {						//add time control
			state = myBoard.markCell(FC[k].i, FC[k].j);
			if (state == winningCondition)
				return FC[k];
			value = alphaBeta(myBoard, true);
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
		// TODO Auto-generated method stub
		return null;
	}

}
