package player;
import java.util.HashSet;
import java.util.Iterator;

import mnkgame.MNKBoard;
import mnkgame.MNKCellState;

public class EvaluationTool {
	//variables for threat evaluation
	int openSeq[][];
	int sopenSeq[][];
	int minSeq;
	int openThreatEval[][];
	int sopenThreatEval[][];
	int MAX_THREATS;
	
	public int MAX_EVALUATION = 32767;				//if we win
	public int MIN_EVALUATION = -32768;				//if enemy player win
	
	//maps that store how many symbols there are in each row/column to evaluate a board
	//saves time if n symbols < k-2
	public int rowSymbols[][];
	public int colSymbols[][];
	public int diagRowSymb[][];
	public int antiDiagRowSymb[][];
	int diagColSymb[][];
	int antiDiagColSymb[][];

	public static int diagBoard[][];

	HashSet<Integer> rowEval = new HashSet<Integer>();
	HashSet<Integer> colEval = new HashSet<Integer>();
	
	//memorizes if i'm P1 or P2
	MNKCellState mySymb;
	MNKCellState enemySymb;
	
	//array that memorizes which cells give origin to interesting diagonals for evaluation
	int diagRow[];
	int diagCol[];
	int k; 				//number of aligned symbols required to win; 
	int m;
	int n;
	
	public EvaluationTool(int m, int n, int k, boolean first) {
		this.k = k;
		this.m = m;
		this.n = n;

		//threats structure initialization
		createThreatStructure();

		if (first) {
			mySymb = MNKCellState.P1;
			enemySymb = MNKCellState.P2;
		}
		else {
			mySymb = MNKCellState.P2;
			enemySymb = MNKCellState.P1;
		}
		

		//Symbols memorization initialization
		diagBoard = new int[m][n];
		diagRow = new int[n];
		diagRowSymb = new int[n][2];
		antiDiagRowSymb = new int[n][2];
		/*
		for (int i = 0; i < n; i++){
			diagRow[i] = 0;
		}
		*/

		diagCol = new int[m];
		diagColSymb = new int[m][2];
		antiDiagColSymb = new int[m][2];
		/*
		for (int i = 0; i < m; i++)
			diagCol[i] = 0;
		*/
		rowSymbols = new int[m][2];
		colSymbols = new int[n][2];

		
		//calculates which diagonals are interesting for us
		diagonalCalculations(m, n, k);
	}
	
	
	
	
	//provide board
	//true if it is our turn, false if enemy's
	
	public int evaluation(GameBoard board, boolean myTurn) {
		//reset the threats array
		for (int i = 0; i < MAX_THREATS; i++) {
			openSeq[i][0] = 0;
			openSeq[i][1] = 0;
		}

		sopenSeq[0][0] = 0;
		sopenSeq[0][1] = 0;
		sopenSeq[1][0] = 0;
		sopenSeq[1][1] = 0;

		//iterate through all the rows
		Iterator<Integer> iteratorRow = rowEval.iterator();
		while (iteratorRow.hasNext()) {
			int row = iteratorRow.next();
			//System.out.println("La row: " + row + " e nella mappa");
			if ((rowSymbols[row][0] + rowSymbols[row][1]) < n){
				countRowSequence(board, row);
				if (myTurn && checkWin(myTurn))
					return MAX_EVALUATION;
				if (!myTurn && checkWin(myTurn))
					return MIN_EVALUATION;
			}
		}

		//iterate through all the columns
		Iterator<Integer> iteratorCol = colEval.iterator();
		while (iteratorCol.hasNext()) {
   			int col = iteratorCol.next();
			//System.out.println("La col: " + col + " e nella mappa");
			if ((colSymbols[col][0] + colSymbols[col][1]) < m){
				countColSequence(board, col);
				if (myTurn && checkWin(myTurn))
					return MAX_EVALUATION;
				if (!myTurn && checkWin(myTurn))
					return MIN_EVALUATION;
			}
		}
		
		//count diagonal and anti-diagonal sequences
		for (int i = 0; i < board.N; i++) {
			switch (diagRow[i]) {
				case 1: {
					if (diagRowSymb[i][0] + diagRowSymb[i][1] < Math.min(m, n - i) && ( diagRowSymb[i][0] >= minSeq || diagRowSymb[i][1] >= minSeq)){
						countDiagSequence(board, 0, i);
						if (myTurn && checkWin(myTurn)) {
							return MAX_EVALUATION;
						}
						if (!myTurn && checkWin(myTurn)) {
							return MIN_EVALUATION;
						}
					}
					break;
				}
				case 2: {
					if (antiDiagRowSymb[i][0] + antiDiagRowSymb[i][1] < Math.min(m, i + 1) && ( antiDiagRowSymb[i][0] >= minSeq || antiDiagRowSymb[i][1] >= minSeq)){
						countAntiDiagSequence(board, 0, i);
						if (myTurn && checkWin(myTurn)) {
							return MAX_EVALUATION;
						}
						if (!myTurn && checkWin(myTurn)) {
							return MIN_EVALUATION;
						}
					}
					break;
				}
				case 3:{
					if (diagRowSymb[i][0] + diagRowSymb[i][1] < Math.min(m, n - i) && ( diagRowSymb[i][0] >= minSeq || diagRowSymb[i][1] >= minSeq))
						countDiagSequence(board, 0, i);
					if (antiDiagRowSymb[i][0] + antiDiagRowSymb[i][1] < Math.min(m, i + 1) && ( antiDiagRowSymb[i][0] >= minSeq || antiDiagRowSymb[i][1] >= minSeq))
						countAntiDiagSequence(board, 0, i);
					if (myTurn && checkWin(myTurn)) {
						return MAX_EVALUATION;
					}
					if (!myTurn && checkWin(myTurn)) {
						return MIN_EVALUATION;
					}
					break;
				}
				default:
					break;
			}
		}
		
		int i = 1;
		while (diagCol[i] > 0) {
			if (diagColSymb[i][0] + diagColSymb[i][1] < m - i && (diagColSymb[i][0] >= minSeq || diagColSymb[i][1] >= minSeq)){
				countDiagSequence(board, i, 0);
				if (myTurn && checkWin(myTurn)) {
					return MAX_EVALUATION;
				}
				if (!myTurn && checkWin(myTurn)) {
					return MIN_EVALUATION;
				}
			}
			if (antiDiagColSymb[i][0] + antiDiagColSymb[i][1] < m - i && (antiDiagColSymb[i][0] >= minSeq || antiDiagColSymb[i][1] >= minSeq)){
				countAntiDiagSequence(board, i, n - 1);
				if (myTurn && checkWin(myTurn)) {
					return MAX_EVALUATION;
				}
				if (!myTurn && checkWin(myTurn)) {
					return MIN_EVALUATION;
				}
			}
			i++;
		}
		
		return threatCalculation(myTurn);
	}
	
	
	
	
	
		/*
		 * 		CALCULATING SEQUENCE REGION
		 */
	
	//given board, symb: P1 and P2 based on which sequences are we looking for
	//allSeq: true if symbols > k-1, false otherwise
	//row: number of the row we are inspecting
	//threats: myThreats or enemyThreats based on whose threats we are counting
	protected void countRowSequence(GameBoard board, int row) {
		//System.out.println("Errore avviene in row");
		int prevFree = 0;
		int z = 0;
		while(z < board.N - minSeq){
			if (board.isFree(row, z)){
				prevFree++;
				if (z + 1 < board.N && board.isFree(row, z + 1)){
					z++;
				}
				else{
					int newInd = z + 1;
					int symCount = 0;
					MNKCellState currP = board.cellState(row, newInd);
					while(newInd < board.N && board.isEqual(row, newInd, currP) && symCount < k - 1){
						newInd++;
						symCount++;
					}
					if (newInd == board.N){
						if (symCount == k - 1){
							updateSopen(currP, symCount);
						}
						z = board.N;
					}
					else{
						if (board.isFree(row, newInd)){
							if (symCount >= minSeq){
								int freeInd = newInd;
								int sucFree = 0;
								//System.out.println((freeInd < board.N) + " " + board.isFree(row, freeInd) + (prevFree + symCount + sucFree < K));
								//System.out.println(prevFree + " " + symCount + " " + sucFree + " " + K);
								while(freeInd < board.N && board.isFree(row, freeInd) && (prevFree + symCount + sucFree < k)){
									freeInd++;
									sucFree++;
								}

								if (prevFree + symCount + sucFree == k){
									updateOpen(symCount, currP);
								}
								if (symCount == k - 1){	
									z = newInd;
									//remove k - 1 symb
									prevFree = 0;
								}
								else if (sucFree > 1){ 
									z = freeInd - 1;
									//remove countSymb
									prevFree = sucFree - 1;
								}
								else z++;
							}
							else{
								z++;
							}
						}
						else{
							z = newInd;
							if (symCount == k - 1){
								updateSopen(currP, symCount);
							}
							if (symCount == k - 2 && prevFree >= 2){
								updateSopen(currP, symCount);
							}
							//reduce the symbols by symCount
							prevFree = 0;
						}
					}
				}
			}
			else{
				MNKCellState symb = board.cellState(row,z);
				int future_index = z + 1;
				int j = future_index;
				int zeros_count = 0;
				boolean diff_symb = false;
				int symbCount = 1;
				while (zeros_count < 2 && j < board.N && symbCount < k - 1){
					if (board.isFree(row, j)){
						zeros_count++;
					}
					else if (!board.isEqual(row, j, symb)){
						diff_symb = true;
						break;
					}
					else
						symbCount++;
					j++;
				}
				if (!diff_symb) {
					if (symbCount == k - 1 && zeros_count <= 1){
						updateSopen(symb, symbCount);
						z++;
						//rimuovi un simbolo
						prevFree = 0;
					}
					else if (symbCount == k - 2 && zeros_count == 2){
						updateSopen(symb, symbCount);
						z++;
						//rimuovi un simbolo
						prevFree = 0;
					}
					else{
						if (zeros_count == 2){
							z = j - 1;
							//rimuovi symCount - 1
							if (j - 2 >= 0 && board.isFree(row, j - 2)){
								prevFree = 1;
							}
							else
								prevFree = 0;
						}
						else
							z = j;
					}
				}
				else {
					if (symbCount == k - 2 && zeros_count == 1 && z - 1 >= 0 && board.isEqual(row, z-1, symb))
						updateSopen(symb, symbCount);
					z = j;
					if (j - 2 >= 0 && board.isFree(row, j - 2)){
						prevFree = 1;
					}
					else
						prevFree = 0;
					//rimuovi symbCount dalle celle rimanenti
				}
			}
		}			
	}

	protected void countColSequence(GameBoard board, int col) {
		//System.out.println("Errore avviene in col");

		int prevFree = 0;
		int z = 0;
		while(z < board.M - minSeq){
			if (board.isFree(z, col)){
				prevFree++;
				if (z + 1 < board.M && board.isFree(z + 1, col)){
					z++;
				}
				else{
					int newInd = z + 1;
					int symCount = 0;
					MNKCellState currP = board.cellState(newInd, col);
					while(newInd < board.M && board.isEqual(newInd, col, currP) && symCount < k - 1){
						newInd++;
						symCount++;
					}
					if (newInd == board.M){
						if (symCount == k - 1){
							updateSopen(currP, symCount);
						}
						z = board.M;
					}
					else{
						if (board.isFree(newInd, col)){
							if (symCount >= minSeq){
								int freeInd = newInd;
								int sucFree = 0;
								//System.out.println((freeInd < board.N) + " " + board.isFree(row, freeInd) + (prevFree + symCount + sucFree < K));
								//System.out.println(prevFree + " " + symCount + " " + sucFree + " " + K);
								while(freeInd < board.M && board.isFree(freeInd, col) && (prevFree + symCount + sucFree < k)){
									freeInd++;
									sucFree++;
								}

								if (prevFree + symCount + sucFree == k){
									updateOpen(symCount, currP);
								}
								if (symCount == k - 1){	
									z = newInd;
									prevFree = 0;
								}
								else if (sucFree > 1){ 
									z = freeInd - 1;
									prevFree = 0;
								}
								else z++;
							}
							else{
								z++;
							}
						}
						else{
							z = newInd;
							if (symCount == k - 1){
								updateSopen(currP, symCount);
							}
							if (symCount == k - 2 && prevFree >= 2){
								updateSopen(currP, symCount);
							}
							prevFree = 0;
						}
					}
				}
			}
			else{
				MNKCellState symb = board.cellState(z,col);
				int j = z + 1;
				int zeros_count = 0;
				boolean diff_symb = false;
				int symbCount = 1;
				while (zeros_count < 2 && j < board.M && symbCount < k - 1){
					if (board.isFree(j, col)){
						zeros_count++;
					}
					else if (!board.isEqual(j, col, symb)){
						diff_symb = true;
						break;
					}
					else
						symbCount++;
					j++;
				}
				if (!diff_symb) {
					if (symbCount == k - 1 && zeros_count <= 1){
						updateSopen(symb, symbCount);
						z++;
						prevFree = 0;
					}
					else if (symbCount == k - 2 && zeros_count == 2){
						updateSopen(symb, symbCount);
						z++;
						prevFree = 0;
					}
					else{
						if (zeros_count == 2){
							z = j - 1;
							//rimuovi symCount - 1
							if (j - 2 >= 0 && board.isFree(j - 2, col)){
								prevFree = 1;
							}
							else
								prevFree = 0;
						}
						else
							z = j;
					}
				}
				else {
					if (symbCount == k - 2 && zeros_count == 1 && z - 1 >= 0 && board.isEqual(z - 1, col, symb))
						updateSopen(symb, symbCount);
					z = j;
					if (j - 2 >= 0 && board.isFree(j - 2, col)){
						prevFree = 1;
					}
					else
						prevFree = 0;
				}
			}
		}			
	}

	protected void countDiagSequence(GameBoard board, int row, int col) {
		//System.out.println("Errore avviene in diag");
		int prevFree = 0;
		int z = 0;
		while(row + z < board.M - minSeq && col + z < board.N - minSeq){
			//System.out.println("row " + row + " z " + z);
			if (board.isFree(row + z, col + z)){
				prevFree++;
				if (row + z + 1 < board.M && col + z + 1 < board.N && board.isFree(row + z + 1, col + z + 1)){
					z++;
				}
				else{
					int newInd = z + 1;
					int symCount = 0;
					MNKCellState currP = board.cellState(row + newInd, col + newInd);
					while(row + newInd < board.M && col + newInd < board.N && board.isEqual(row + newInd, col + newInd, currP) && symCount < k - 1){
						newInd++;
						symCount++;
					}
					if (row + newInd == board.M || col + newInd == board.N){
						if (symCount == k - 1){
							updateSopen(currP, symCount);
						}
						z = board.M;
					}
					else{
						if (board.isFree(row + newInd, col + newInd)){
							if (symCount >= minSeq){
								int freeInd = newInd;
								int sucFree = 0;
								//System.out.println((freeInd < board.N) + " " + board.isFree(row, freeInd) + (prevFree + symCount + sucFree < K));
								//System.out.println(prevFree + " " + symCount + " " + sucFree + " " + K);
								while(row + freeInd < board.M && col + freeInd < board.N && board.isFree(row + freeInd, col + freeInd) && (prevFree + symCount + sucFree < k)){
									freeInd++;
									sucFree++;
								}

								if (prevFree + symCount + sucFree == k){
									updateOpen(symCount, currP);
								}
								if (symCount == k - 1){	
									z = newInd;
									prevFree = 0;
								}
								else if (sucFree > 1){ 
									z = freeInd - 1;
									prevFree = 0;
								}
								else z++;
							}
							else{
								z = z + 1;
							}
						}
						else{
							z = newInd;
							if (symCount == k - 1){
								updateSopen(currP, symCount);
							}
							if (symCount == k - 2 && prevFree >= 2){
								updateSopen(currP, symCount);
							}
							prevFree = 0;
						}
					}
				}
			}
			else{
				MNKCellState symb = board.cellState(row + z, col + z);
				int j = z + 1;
				int zeros_count = 0;
				boolean diff_symb = false;
				int symbCount = 1;
				while (zeros_count < 2 && row + j < board.M && col + j < board.N && symbCount < k - 1){
					if (board.isFree(row + j, col + j)){
						zeros_count++;
					}
					else if (!board.isEqual(row + j, col + j, symb)){
						diff_symb = true;
						break;
					}
					else
						symbCount++;
					j++;
				}
				if (!diff_symb) {
					if (symbCount == k - 1 && zeros_count <= 1){
						updateSopen(symb, symbCount);
						z++;
						prevFree = 0;
					}
					else if (symbCount == k - 2 && zeros_count <= 2){
						updateSopen(symb, symbCount);
						z++;
						prevFree = 0;
					}
					else{
						if (zeros_count == 2){
							z = j - 1;
							//rimuovi symCount - 1
							if (row + j - 2 >= 0 && col + j - 2 >= 0 && board.isFree(row + j - 2, col + j - 2)){
								prevFree = 1;
							}
							else
								prevFree = 0;
						}
						else
							z = j;
					}
				}
				else {
					if (symbCount == k - 2 && zeros_count == 1 && row + z - 1 >= 0 && col + z - 1 >= 0 && board.isEqual(row + z - 1, col + z - 1, symb))
						updateSopen(symb, symbCount);
					z = j;
					if (row + j - 2 >= 0 && col + j - 2 >= 0 && board.isFree(row + j - 2, col + j - 2)){
						prevFree = 1;
					}
					else
						prevFree = 0;
				}
			}
		}			
	}

	protected void countAntiDiagSequence(GameBoard board, int row, int col) {
		//System.out.println("Errore avviene in antidiag");
		int prevFree = 0;
		int z = 0;
		while(row + z < board.M - 1 && col - z > 0){
			if (board.isFree(row + z, col - z)){
				//System.out.println("Errore avviene in free");
				prevFree++;
				if (row + z + 1 < board.M && col - z - 1 >= 0 && board.isFree(row + z + 1, col - z - 1)){
					z++;
				}
				else{
					int newInd = z + 1;
					int symCount = 0;
					MNKCellState currP = board.cellState(row + newInd, col - newInd);
					while(row + newInd < board.M && col - newInd >= 0 && board.isEqual(row + newInd, col - newInd, currP) && symCount < k - 1){
						newInd++;
						symCount++;
					}
					if (row + newInd == board.M || col - newInd == -1){
						if (symCount == k - 1){
							updateSopen(currP, symCount);
						}
						z = board.M;
					}
					else{
						if (board.isFree(row + newInd, col - newInd)){
							if (symCount >= minSeq){
								int freeInd = newInd;
								int sucFree = 0;
								while(row + freeInd < board.M && col - freeInd >= 0 && board.isFree(row + freeInd, col - freeInd) && (prevFree + symCount + sucFree < k)){
									freeInd++;
									sucFree++;
								}

								if (prevFree + symCount + sucFree == k){
									updateOpen(symCount, currP);
								}
								if (symCount == k - 1){	
									z = newInd;
									prevFree = 0;
								}
								else if (sucFree > 1){ 
									z = freeInd - 1;
									prevFree = 0;
								}
								else z++;
							}
							else{
								z = z + 1;
							}
						}
						else{
							z = newInd;
							if (symCount == k - 1){
								updateSopen(currP, symCount);
							}
							if (symCount == k - 2 && prevFree >= 2){
								updateSopen(currP, symCount);
							}
							prevFree = 0;
						}
					}
				}
			}
			else{
				//System.out.println("Errore avviene in symb");
				MNKCellState symb = board.cellState(row + z, col - z);
				int j = z + 1;
				int zeros_count = 0;
				boolean diff_symb = false;
				int symbCount = 1;
				//System.out.println("Errore avviene nel primo while");
				while (zeros_count < 2 && row + j < board.M && col - j >= 0 && symbCount < k - 1){
					if (board.isFree(row + j, col - j)){
						zeros_count++;
					}
					else if (!board.isEqual(row + j, col - j, symb)){
						diff_symb = true;
						break;
					}
					else
						symbCount++;
					j++;
				}
				//System.out.println("Errore avviene dopo il primo while");
				if (!diff_symb) {
					if (symbCount == k - 1 && zeros_count <= 1){
						updateSopen(symb, symbCount);
						z++;
						prevFree = 0;
					}
					else if (symbCount == k - 2 && zeros_count <= 2){
						updateSopen(symb, symbCount);
						z++;
						prevFree = 0;
					}
					else{
						if (zeros_count == 2){
							z = j - 1;
							//rimuovi symCount - 1
							if (row + j - 2 >= 0 && col - j + 2 < board.N && board.isFree(row + j - 2, col - j + 2)){
								prevFree = 1;
							}
							else
								prevFree = 0;
						}
						else
							z = j;
					}
				}
				else {
					if (symbCount == k - 2 && zeros_count == 1 && row + z - 1 >= 0 && col - z + 1 < board.N && board.isEqual(row + z - 1, col - z + 1, symb))
						updateSopen(symb, symbCount);
					z = j;
					if (row + j - 2 >= 0 && col - j + 2 < board.N && board.isFree(row + j - 2, col - j + 2)){
						prevFree = 1;
					}
					else
						prevFree = 0;
				}
			}
		}			
	}
	
	
	

		/*	
		 * 		AUXILLARY FUNCTIONS REGION
		 */

		 public boolean canBelongDiagonal(int row, int col, int k){
			int x, y;
			if (row > col){
				x = row - col;
				y = 0;
				return(Math.min(m - x, n) >= k);
			}
			else if (col > row){
				x = 0;
				y = col - row;
				return(Math.min(m, n - y) >= k);
			}
			else{
				return(Math.min(m, n) >= k);
			}
		}
	
	protected void diagonalCalculations (int m, int n, int k) {
		//row check
		for (int i = 0; i < n; i++) {
			if (Math.min(m, n-i) >= k) {
				diagRow[i]++;
				diagRow[n-1-i] += 2;
			}
			else break;
		}
		//column check
		for (int i = 1; i < m; i++) {
			if (Math.min(m-i, n) >= k)
				diagCol[i]++;
			else break;
		}

		for(int i = 0; i < m; i++){
			for (int j = 0; j < n; j++){
				diagBoard[i][j] = 0;
				if (canBelongDiagonal(i, j, k))
					diagBoard[i][j]++;
				if (canBelongDiagonal(i, n - j - 1, k))			//calling this function with the opposite column returns wheter a cell can belong to an antiagonal (math)
					diagBoard[i][j] += 2;
			}
		}
	}
	
	/*	These two functions update the maps that keep track of the current symbols in each row/column
	 * 	given a row and col, they add/remove one symbol from the indicated maps
	 *  boolean my_move is true if it is our move, false otherwise
	 */
	
	public void addSymbol(int row, int col, boolean my_move) {
		if (my_move) {
			rowSymbols[row][0]++;
			if (rowSymbols[row][0] == minSeq && rowSymbols[row][1] < minSeq){
				rowEval.add(row);
			}
			colSymbols[col][0]++;
			if (colSymbols[col][0] == minSeq && colSymbols[col][1] < minSeq)
				colEval.add(col);

			if (diagBoard[row][col] == 1 || diagBoard[row][col] == 3)
				symbDiag(row, col, 0, 1);

			if (diagBoard[row][col] >= 2)
				symbAntiDiag(row, col, 0, 1);
		}
		
		else {
			rowSymbols[row][1]++;
			if (rowSymbols[row][1] == minSeq && rowSymbols[row][0] < minSeq)
				rowEval.add(row);
			colSymbols[col][1]++;
			if (colSymbols[col][1] == minSeq && colSymbols[col][0] < minSeq)
				colEval.add(col);

			if (diagBoard[row][col] == 1 || diagBoard[row][col] == 3)
				symbDiag(row, col, 1, 1);

			if (diagBoard[row][col] >= 2)
				symbAntiDiag(row, col, 1, 1);
		}
	}

	public void removeSymbol(int row, int col, boolean my_move) {
		if (my_move) {
			rowSymbols[row][0]--;
			if (rowSymbols[row][0] == minSeq - 1 && rowSymbols[row][1] < minSeq)
				rowEval.remove(row);
			colSymbols[col][0]--;
			if (colSymbols[col][0] == minSeq - 1 && colSymbols[col][1] < minSeq)
				colEval.remove(col);

			if (diagBoard[row][col] == 1 || diagBoard[row][col] == 3)
				symbDiag(row, col, 0, -1);

			if (diagBoard[row][col] >= 2)
				symbAntiDiag(row, col, 0, -1);

		}
		else {
			rowSymbols[row][1]--;
			if (rowSymbols[row][1] == minSeq - 1 && rowSymbols[row][0] < minSeq)
				rowEval.remove(row);
			colSymbols[col][1]--;
			if (colSymbols[col][1] == minSeq - 1 && colSymbols[col][0] < minSeq)
				colEval.remove(col);

			if (diagBoard[row][col] == 1 || diagBoard[row][col] == 3)
				symbDiag(row, col, 1, -1);

			if (diagBoard[row][col] >= 2)
				symbAntiDiag(row, col, 1, -1);
		}
	}
	
	
	
	//if a player has k-1 threats and it's his turn, he will convert to a win
	protected boolean checkWin(boolean myTurn) {
		if (myTurn)
			return (openSeq[MAX_THREATS - 1][0] + sopenSeq[0][0] > 0);
		else
			return (openSeq[MAX_THREATS - 1][1] + sopenSeq[0][1] > 0);
	}
	

	protected void updateOpen(int symCount, MNKCellState P){
		if (P == mySymb)
			openSeq[symCount - minSeq][0]++;
		else
			openSeq[symCount-minSeq][1]++;
		
	}
	
	protected void updateSopen(MNKCellState P, int symb){
		if (P == enemySymb)
			sopenSeq[k - symb - 1][0]++;
		else
			sopenSeq[k - symb - 1][1]++;
	}

	protected void createThreatStructure(){
		minSeq = Math.min(2, k-2);
		MAX_THREATS = k - minSeq;
		openSeq = new int[MAX_THREATS][2];
		sopenSeq = new int[2][2];
		openThreatEval = new int[MAX_THREATS][2];
		sopenThreatEval = new int[2][2];
	
		double reducingCoefficient = 2.6;
	
		if (k <= 5){
			openThreatEval[MAX_THREATS - 1][1] = 5020;
		}
		else if (k <= 10){
			openThreatEval[MAX_THREATS - 1][1] = 10040;
		}
		else{
			openThreatEval[MAX_THREATS - 1][1] = (k/5 + 1) * 5020;
		}
		
		openThreatEval[MAX_THREATS - 1][0] = (int)(openThreatEval[MAX_THREATS - 1][1] / 15);
	
		for (int i = (MAX_THREATS - 2); i >= 0; i--){
			openThreatEval[i][1] = (int)(openThreatEval[i + 1][1] / reducingCoefficient);
			openThreatEval[i][0] = openThreatEval[i][1] / 15;
		}
	
		sopenThreatEval[0][1] = (int)(openThreatEval[MAX_THREATS - 1][1] / 4.3);
		sopenThreatEval[0][0] = sopenThreatEval[0][1] / 15;
		sopenThreatEval[1][1] = (int)(openThreatEval[MAX_THREATS - 2][1] / 4.3);
		sopenThreatEval[1][0] = sopenThreatEval[1][1] / 15;
		
		/*
		System.out.println("K = " + k);
		System.out.println("OPEN THREATS EVAL");
		int c = 1;
		for (int i = k - minSeq - 1; i >= 0; i--){
			System.out.println("Sequenze k - " + c + " , mia eval " + openThreatEval[i][0] + " sua eval " + openThreatEval[i][1]);
			System.out.println(" ");
			c++;
		}
	
		System.out.println("k - 1 eval mia: " + sopenThreatEval[0][0] + " sue: " + sopenThreatEval[0][1]);
		System.out.println("k - 2 eval mia: " + sopenThreatEval[1][0] + " sue: " + sopenThreatEval[1][1]);
		*/
	}
	
	public void symbDiag(int row, int col, int player, int delta){
		//System.out.println("Sono qui per aggiungere simboli alle diagonali");
		if (row > col)
			diagColSymb[row - col][player] += delta;
		else
			diagRowSymb[col - row][player] += delta;
	}

	public void symbAntiDiag(int row, int col, int player, int delta){
		//System.out.println("Sono qui per aggiungere simboli alle antidiagonali");
		int deltaCol = diagRow.length - col - 1;
		if (row <= deltaCol)
			antiDiagRowSymb[row + col][player] += delta;
		else
			antiDiagColSymb[row - deltaCol][player] += delta;
	}

	//computes the number of threats * evaluation
	protected int threatCalculation(boolean myTurn) {
		if (myTurn && (openSeq[MAX_THREATS - 1][1] > 0 || sopenSeq[0][1] > 1) && (openSeq[MAX_THREATS - 1][0] + sopenSeq[0][0] == 0))
			return MIN_EVALUATION;
		
		if (!myTurn && (openSeq[MAX_THREATS - 1][0] > 0 || sopenSeq[0][0] > 1) && (openSeq[MAX_THREATS - 1][1] + sopenSeq[0][1] == 0))
			return MAX_EVALUATION;

		int eval = 0;
		//valuto i threat open
		int c = MAX_THREATS;
		for (int i = 0; i < MAX_THREATS; i++){
			eval = eval + openSeq[i][0]*openThreatEval[i][0] - openSeq[i][1]*openThreatEval[i][1];
			//System.out.println("Minacce k - " + c + " mie: " + openSeq[i][0] + " sue: " + openSeq[i][1]);
			c--;
		}
		/*
		System.out.println("K - 1 semiopen, mie: " + sopenSeq[0][0] + " sue: " + sopenSeq[0][1]);
		System.out.println("K - 2 semiopen, mie: " + sopenSeq[1][0] + " sue: " + sopenSeq[1][1]);
		*/
		eval = eval + sopenSeq[0][0]*sopenThreatEval[0][0] - sopenSeq[0][1]*sopenThreatEval[0][1];
		eval = eval + sopenSeq[1][0]*sopenThreatEval[1][0] - sopenSeq[1][1]*sopenThreatEval[1][1];
		return eval;
	}
}

