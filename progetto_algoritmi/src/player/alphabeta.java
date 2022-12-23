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
		return min(board, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	protected int max(MNKBoard board, int alpha, int beta) {
		MNKCell[] FC = board.getFreeCells();
		int k = 0;
		MNKGameState state;
		int maxValue = Integer.MIN_VALUE;
		while (k < FC.length) {
			System.out.println("sono max e marco" + FC[k].i + FC[k].j);
			state = board.markCell(FC[k].i, FC[k].j);
			System.out.println("sono max e controllo se ho vinto" + (state == wCond));
			if (state == wCond) {
				System.out.println("sono max e smarco" + FC[k].i + FC[k].j);
				board.unmarkCell();
				return 1;
			}
			else if (state == MNKGameState.DRAW) {
				System.out.println("sono max e smarco" + FC[k].i + FC[k].j);
				board.unmarkCell();
				return 0;
			}
			System.out.println("sono sempre max e richiamo min");
			int value = min(board, alpha, beta);
			maxValue = Math.max(value, maxValue);
			alpha = Math.max(alpha, maxValue);
			System.out.println("sono max e smarco" + FC[k].i + FC[k].j);
			board.unmarkCell();
			if (alpha >= beta) break;
		}
		return maxValue;
	}
	
	protected int min(MNKBoard board, int alpha, int beta) {

		MNKCell[] FC = board.getFreeCells();
		int k = 0;
		MNKGameState state;
		int minValue = Integer.MAX_VALUE;
		while (k < FC.length) {
			System.out.println("sono min e marco" + FC[k].i + FC[k].j);
			state = board.markCell(FC[k].i, FC[k].j);
			System.out.println("sono min e controllo se ho vinto" + (state == lCond));
			if (state == lCond) {
				board.unmarkCell();
				System.out.println("sono min e smarco" + FC[k].i + FC[k].j);
				return -1;
			}
			else if (state == MNKGameState.DRAW) {
				System.out.println("sono min e smarco" + FC[k].i + FC[k].j);
				board.unmarkCell();
				return 0;
			}
			System.out.println("sono sempre min e richiamo min");
			int value = max(board, alpha, beta);
			minValue = Math.min(value, minValue);
			beta = Math.min(beta, minValue);
			System.out.println("sono min e smarco" + FC[k].i + FC[k].j);
			board.unmarkCell();
			if (alpha >= beta) break;
				}
		return minValue;
	}

}