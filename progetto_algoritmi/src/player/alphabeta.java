package player;

import mnkgame.MNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKGameState;	

public class alphabeta{
	MNKGameState wCond;
	MNKGameState lCond;
	
	public alphabeta(boolean first) {
		if (first) {
			wCond = MNKGameState.WINP1;
			lCond = MNKGameState.WINP2;
		}
		else {
			wCond = MNKGameState.WINP2;
			lCond = MNKGameState.WINP1;
		}
	}
	
	public int alphaBeta(MNKBoard board, boolean maximizer) {
		return max(board, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	protected int max(MNKBoard board, int alpha, int beta) {
		if  (board.gameState() == MNKGameState.DRAW)
				return 0;
		MNKCell[] FC = board.getFreeCells();
		int k = 0;
		
		int maxValue = Integer.MIN_VALUE;
		while (k < FC.length) {
			if (board.markCell(FC[k].i, FC[k].j) == wCond) {
				board.unmarkCell();
				return 1;
			}
			int value = min(board, alpha, beta);
			maxValue = Math.max(value, maxValue);
			alpha = Math.max(alpha, maxValue);
			board.unmarkCell();
			if (alpha >= beta) break;
		}
		return maxValue;
	}
	
	protected int min(MNKBoard board, int alpha, int beta) {
		if  (board.gameState() == MNKGameState.DRAW)
			return 0;
		MNKCell[] FC = board.getFreeCells();
		int k = 0;
				
		int minValue = Integer.MAX_VALUE;
		while (k < FC.length) {
			if (board.markCell(FC[k].i, FC[k].j) == lCond) {
				board.unmarkCell();
				return 1;
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