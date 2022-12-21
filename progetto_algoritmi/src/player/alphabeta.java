package player;

import mnkgame.MNKBoard;
import mnkgame.MNKCell;

public class alphabeta{
	
	public int alphaBeta(MNKBoard board, boolean maximizer) {
		
		if (maximizer) {
			return max(board, Integer.MIN_VALUE, Integer.MAX_VALUE, 2);
		}
		else
			return min(board, Integer.MIN_VALUE, Integer.MAX_VALUE, 2);
	}
	
	protected int max(MNKBoard board, int alpha, int beta, int depth) {
		//if (depth == 0)
		//		return evaluation(board);
		MNKCell[] FC = board.getFreeCells();
		int k = 0;
		
		int maxValue = Integer.MIN_VALUE;
		while (k < FC.length) {
			board.markCell(FC[k].i, FC[k].j);
			int value = min(board, alpha, beta, depth - 1);
			maxValue = Math.max(value, maxValue);
			alpha = Math.max(alpha, maxValue);
			board.unmarkCell();
			if (alpha >= beta) break;
		}
		return maxValue;
	}
	
	protected int min(MNKBoard board, int alpha, int beta, int depth) {
		//if (depth == 0)
		//		return evaluation(board);
		MNKCell[] FC = board.getFreeCells();
		int k = 0;
				
		int minValue = Integer.MAX_VALUE;
		while (k < FC.length) {
			board.markCell(FC[k].i, FC[k].j);
			int value = max(board, alpha, beta, depth - 1);
			minValue = Math.min(value, minValue);
			beta = Math.min(beta, minValue);
			board.unmarkCell();
			if (alpha >= beta) break;
				}
		return minValue;
	}

}
