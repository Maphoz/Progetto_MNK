package player;

import java.util.*;


import mnkgame.MNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKGameState;
import mnkgame.MNKCellState;


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
	public static int threatBoard[][];
	public static MNKCellState ourState;
	public static MNKCellState enemyState;
	

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		this.M = M;
		this.N = N;
		this.key = (long)0;
		distance_from_root = 0;
		
		this.killer = new killer_heuristic(M,N, K);
		
		rand = new Random(System.currentTimeMillis()); 
		FirstTurn=true;
		myBoard = new MNKBoard (M, N, K);
		
		//saving the timeout in milliseconds
		this.timeout = timeout_in_secs*1000;
		
		//identifying the win conditions for me and my opponent
		if (first) {
			winCondition = MNKGameState.WINP1;
			losCondition = MNKGameState.WINP2;
			ourState = MNKCellState.P1;
			enemyState = MNKCellState.P2;
		}
		else {
			winCondition = MNKGameState.WINP2;
			losCondition = MNKGameState.WINP1;
			ourState = MNKCellState.P2;
			enemyState = MNKCellState.P1;
		}
		this.TT = new Transposition_table(M,N);
		TT.initTableRandom();
		//instance of the alphabeta class to solve the problem
		solver = new alphabeta(winCondition, losCondition, first);
		eval = new EvaluationTool(M, N, K, first);
		
		threatBoard = new int[M][N];
		calculateCellThreats(K);
		
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
			key = TT.generate_key(key, MC[MC.length - 1].i, MC[MC.length - 1].j, enemyState);
			//System.out.println("chiave con mossa fatta dall'avversario " + key + " con numero random " + TT.getStorage(MC[MC.length - 1].i, MC[MC.length - 1].j, enemyState));
		}
		
		
		if(FirstTurn) {
			MNKCell selected_move = center(FC, FC.length, M, N);
			myBoard.markCell(selected_move.i,selected_move.j);
			eval.addSymbol(selected_move.i,selected_move.j, true);
			key = TT.generate_key(key, selected_move.i, selected_move.j, ourState);
			solver.firstIterative(myBoard, FC, myBoard.M * myBoard.N - MC.length, TT, killer, distance_from_root, eval, startTime, key);
			FirstTurn = false;
			return selected_move;
		}
		
		//checking if there are any winning or losing moves
		MNKCell enemy_winning = FC[0];
		boolean enemyWin = false;
		if (FC.length == 1)
			return FC[0];
		else {
			for (int z = 0; z < FC.length; z++) {
				if (myBoard.markCell(FC[z].i, FC[z].j) == winCondition)
					return FC[z];
				else{
					if (myBoard.markCell(FC[(z+1)%FC.length].i, FC[(z+1)%FC.length].j) == losCondition) {
						enemyWin = true;
						enemy_winning = FC[(z+1)%FC.length];
						myBoard.unmarkCell();
						myBoard.unmarkCell();
					}
					else {
						myBoard.unmarkCell();
						myBoard.unmarkCell();
					}
				}
			}
		}
		if (enemyWin) {
			myBoard.markCell(enemy_winning.i, enemy_winning.j);
			eval.addSymbol(enemy_winning.i, enemy_winning.j, true);
			key = TT.generate_key(key, enemy_winning.i, enemy_winning.j, ourState);
			return enemy_winning;
		}

		MNKCell bestCell = solver.iterativeDeepening(myBoard, FC, myBoard.M * myBoard.N - MC.length, TT, killer, distance_from_root, eval, startTime, key);

		//System.out.println("chiave prima di aver giocato la mossa: " + key);
		myBoard.markCell(bestCell.i, bestCell.j);
		eval.addSymbol(bestCell.i, bestCell.j, true);
		key = TT.generate_key(key, bestCell.i, bestCell.j, ourState);
		//System.out.println("chiave dopo aver giocato la mossa: " + key +" con numero random " + TT.getStorage(bestCell.i, bestCell.j, ourState));
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
	
	protected void calculateCellThreats(int k){
		for (int i = 0; i < M; i++){
			for (int j = 0; j < N; j++){
				threatBoard[i][j] = 2;
				if (canBelongDiagonal(i, j, k))
					threatBoard[i][j]++;
				if (canBelongDiagonal(i, N - j - 1, k))			//calling this function with the opposite column returns wheter a cell can belong to an antiagonal (math)
					threatBoard[i][j]++;
			}
		}
	}

	protected boolean canBelongDiagonal(int row, int col, int k){
		int x, y;
		if (row > col){
			x = row - col;
			y = 0;
			return(Math.min(M - x, N) >= k);
		}
		else if (col > row){
			x = 0;
			y = col - row;
			return(Math.min(M, N - y) >= k);
		}
		else{
			return(Math.min(M, N) >= k);
		}
	}

}