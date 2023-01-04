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
	public static int timeout;
	boolean FirstTurn;
	Random rand;
	EvaluationTool eval;
	Transposition_table TT;
	killer_heuristic killer;
	int distance_from_root;
	long key;
	int M;  //righe
	int N;  //colonne

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		this.M = M;
		this.N = N;
		this.key = (long)0;
		distance_from_root = 0;
		this.TT = new Transposition_table(M,N);
		this.killer = new killer_heuristic(M,N, K);
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
		eval = new EvaluationTool(M, N, K, first);
		
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		
		distance_from_root = MC.length + 1;
		
		//starting the time count
		long startTime = System.currentTimeMillis();
		//System.out.println("tempo di inizio: " + startTime);
		
		//adding to my board representation the last move played by the opponent
		if (MC.length != 0) {
			myBoard.markCell(MC[MC.length - 1].i, MC[MC.length - 1].j);
			eval.addSymbol(MC[MC.length - 1].i, MC[MC.length - 1].j, false);
			key = TT.generate_key(key, MC[MC.length - 1].i, MC[MC.length - 1].j, MC[MC.length - 1].state);
		}
		
		
		if(FirstTurn) {
			MNKCell calcCell = solver.iterativeDeepening(myBoard, FC, myBoard.M * myBoard.N - MC.length, TT, killer, distance_from_root, eval, startTime);
			//MNKCell selected_move = FC[rand.nextInt(FC.length)];
			MNKCell selected_move = center(FC, FC.length, M, N);
			myBoard.markCell(selected_move.i,selected_move.j);
			eval.addSymbol(selected_move.i,selected_move.j, true);
			key = TT.generate_key(key, selected_move.i, selected_move.j, myBoard.cellState(selected_move.i, selected_move.j));
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
		
		
		MNKCell bestCell = solver.iterativeDeepening(myBoard, FC, myBoard.M * myBoard.N - MC.length, TT, killer, distance_from_root, eval, startTime);
		
		myBoard.markCell(bestCell.i, bestCell.j);
		eval.addSymbol(bestCell.i, bestCell.j, true);
		key = TT.generate_key(key, bestCell.i, bestCell.j, myBoard.cellState(bestCell.i, bestCell.j));
		return bestCell;
	}

	@Override
	public String playerName() {
		return "Giusama";
	}
	
	protected MNKCell center(MNKCell[] FC, int lenght, int M, int N) { //la prima mossa la metti sempre al centro
		MNKCell center = new MNKCell(M/2, N/2);
		MNKCell up_left_corner = new MNKCell(M/2-1, N/2-1);
		for(int i = 0; i<lenght; i++) {
			if(FC[i].i == center.i && FC[i].j == center.j) 
				return center;			
			}
		return up_left_corner;
	}

}
