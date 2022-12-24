package player;

import mnkgame.MNKBoard;

import mnkgame.MNKCell;
import mnkgame.MNKGameState;	

public class alphabeta{
	MNKGameState wCond;
	MNKGameState lCond;
	
	public alphabeta(MNKGameState wc, MNKGameState lc) {
		//saving the win conditions
		wCond = wc;
		lCond = lc;
	}
	
	public int alphaBeta(MNKBoard board, boolean maximizer) {
		//the move is being tried by the player class, so we look for opponent best response
		return min(board, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	protected int max(MNKBoard board, int alpha, int beta) {
		
		MNKCell[] FC = board.getFreeCells();
		MNKGameState state;
		int maxValue = Integer.MIN_VALUE;
		for (MNKCell d: FC) {
			state = board.markCell(d.i, d.j);				
			if (state == wCond) {							//if it is a winning cell, return the best evaluation
				board.unmarkCell();
				return 1;
			}
			if (state == MNKGameState.DRAW) {				//if it is a drawing cell, return the null evaluation
				board.unmarkCell();
				return 0;
			}
			int value = min(board, alpha, beta);			//else recursive call and compare the evaluations
			maxValue = Math.max(value, maxValue);
			alpha = Math.max(alpha, maxValue);
			board.unmarkCell();
			if (alpha >= beta) break;
		}
		return maxValue;
	}
	
	protected int min(MNKBoard board, int alpha, int beta) {
		MNKCell[] FC = board.getFreeCells();
		MNKGameState state;
		int minValue = Integer.MAX_VALUE;
		for (MNKCell d: FC) {
			state = board.markCell(d.i, d.j);
			if (state == lCond) {
				board.unmarkCell();
				return -1;
			}
			if (state == MNKGameState.DRAW) {
				board.unmarkCell();
				return 0;
			}
			int value = max(board, alpha, beta);
			minValue = Math.min(value, minValue);
			beta = Math.min(beta, minValue);
			board.unmarkCell();
			if (alpha >= beta) break;
		}
		return minValue;
	}

}