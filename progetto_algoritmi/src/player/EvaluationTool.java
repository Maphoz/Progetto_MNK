package player;
import java.util.HashSet;
import java.util.Iterator;

import mnkgame.MNKBoard;
import mnkgame.MNKCellState;

public class EvaluationTool {
	//variables for threat evaluation
	int k1OpenIndex = 0;
	int k2OpenIndex = 1;
	int k1SopenIndex = 2;
	int enemyThreatsEval[];
	int myThreatsEval[];
	int MAX_THREATS = 3;
	public static int myThreats[];
	public static int enemyThreats[];
	
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
		myThreats = new int[MAX_THREATS];
		enemyThreats = new int[MAX_THREATS];
		enemyThreatsEval = new int[MAX_THREATS];
		myThreatsEval = new int[MAX_THREATS];

		enemyThreatsEval[k1OpenIndex] = 5020;
		enemyThreatsEval[k2OpenIndex] = 1300;
		enemyThreatsEval[k1SopenIndex] = 2000;
		if (first) {
			mySymb = MNKCellState.P1;
			enemySymb = MNKCellState.P2;
			for (int i = 0; i < MAX_THREATS; i++){
				myThreatsEval[i] = enemyThreatsEval[i];
			}
		}
		else {
			mySymb = MNKCellState.P2;
			enemySymb = MNKCellState.P1;
			myThreatsEval[k1OpenIndex] = 250;
			myThreatsEval[k2OpenIndex] = 100;
			myThreatsEval[k1SopenIndex] = 80;
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
	
	public int evaluation(MNKBoard board, boolean myTurn) {
		//reset the threats array
		for (int i = 0; i < MAX_THREATS; i++) {
			myThreats[i] = 0;
			enemyThreats[i] = 0;
		}

		//iterate through all the rows
		Iterator<Integer> iteratorRow = rowEval.iterator();
		while (iteratorRow.hasNext()) {
   			int row = iteratorRow.next();
			if ((rowSymbols[row][0] + rowSymbols[row][1]) < n){
				//checking for my threats
				if (rowSymbols[row][0] >= k - 1){
					//System.out.println("Controllo la riga " + row + "per me (k - 1)");
					countRowSequence(board, mySymb, true, row, myThreats);
					if (myTurn && checkWin(myTurn)) {
						return MAX_EVALUATION;
					}
				}
				else if (rowSymbols[row][0] == k - 2){
					//System.out.println("Controllo la riga " + row + "per me (k - 2)");
					countRowSequence(board, mySymb, false, row, myThreats);
				}
				//checking for enemy threats
				if (rowSymbols[row][1] >= k - 1){
					//System.out.println("Controllo la riga " + row + "per lui (k - 1)");
					countRowSequence(board, enemySymb, true, row, enemyThreats);
					if (!myTurn && checkWin(myTurn)) {
						return MIN_EVALUATION;
					}
				}
				else if (rowSymbols[row][1] == k - 2){
					//System.out.println("Controllo la riga " + row + "per lui (k - 2)");
					countRowSequence(board, enemySymb, false, row, enemyThreats);
				}
			}
		}

		//iterate through all the columns
		Iterator<Integer> iteratorCol = colEval.iterator();
		while (iteratorCol.hasNext()) {
   			int col = iteratorCol.next();
			if ((colSymbols[col][0] + colSymbols[col][1]) < m){

				//checking for my threats
				if (colSymbols[col][0] >= k - 1){
					//System.out.println("Controllo la colonna " + col + "per me (k - 1)");
					countColSequence(board, mySymb, true, col, myThreats);
					if (myTurn && checkWin(myTurn)) {
						return MAX_EVALUATION;
					}
				}
				else if (colSymbols[col][0] == k - 2){
					//System.out.println("Controllo la colonna " + col + "per me (k - 2)");
					countColSequence(board, mySymb, false, col, myThreats);
				}

				//checking for enemy threats
				if (colSymbols[col][1] >= k - 1){
					//System.out.println("Controllo la colonna " + col + "per lui (k - 1)");
					countColSequence(board, enemySymb, true, col, enemyThreats);
					if (!myTurn && checkWin(myTurn)) {
						return MIN_EVALUATION;
					}
				}
				else if (colSymbols[col][1] == k - 2){
					//System.out.println("Controllo la colonna " + col + "per lui (k - 2)");
					countColSequence(board, enemySymb, false, col, enemyThreats);
				}
			}
		}
		
		//count diagonal and anti-diagonal sequences
		for (int i = 0; i < board.N; i++) {
			switch (diagRow[i]) {
				case 1: {
					if (diagRowSymb[i][0] + diagRowSymb[i][1] < Math.min(m, n - i) && ( diagRowSymb[i][0] >= k - 2 || diagRowSymb[i][1] >= k - 2)){
						countDiagSequence(0, i, board);
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
					if (antiDiagRowSymb[i][0] + antiDiagRowSymb[i][1] < Math.min(m, i + 1) && ( antiDiagRowSymb[i][0] >= k - 2 || antiDiagRowSymb[i][1] >= k - 2)){
						countAntidiagSequence(0, i, board);
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
					if (diagRowSymb[i][0] + diagRowSymb[i][1] < Math.min(m, n - i) && ( diagRowSymb[i][0] >= k - 2 || diagRowSymb[i][1] >= k - 2))
						countDiagSequence(0, i, board);
					if (antiDiagRowSymb[i][0] + antiDiagRowSymb[i][1] < Math.min(m, i + 1) && ( antiDiagRowSymb[i][0] >= k - 2 || antiDiagRowSymb[i][1] >= k - 2))
						countAntidiagSequence(0, i, board);
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
			if (diagColSymb[i][0] + diagColSymb[i][1] < m - i && (diagColSymb[i][0] >= k - 2 || diagColSymb[i][1] >= k - 2)){
				countDiagSequence(i, 0, board);
				if (myTurn && checkWin(myTurn)) {
					return MAX_EVALUATION;
				}
				if (!myTurn && checkWin(myTurn)) {
					return MIN_EVALUATION;
				}
			}
			if (diagColSymb[i][0] + diagColSymb[i][1] < m - i && (diagColSymb[i][0] >= k - 2 || diagColSymb[i][1] >= k - 2)){
				countAntidiagSequence(i, n - 1, board);
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
	protected void countRowSequence(MNKBoard board, MNKCellState symb, boolean allSeq, int row, int[] threats) {
		if (allSeq) {
			int z = 0;
			while (z < board.N - k + 1) {
				if (board.cellState(row, z) == symb) {
					int future_index = z + 1;
					int zeros_count = 0;
					boolean diff_symb = false;
					for (int j = z + 1; j < z + k; j++) {
						if (board.cellState(row, j) == MNKCellState.FREE) { 
							zeros_count++;
							future_index = j;
						}
						else if (board.cellState(row, j) != symb) {
							future_index = j+1;
							diff_symb = true;
							break;
						}
					}
					if (zeros_count <= 1 && !diff_symb) {
						threats[k1SopenIndex]++;
						z++;
					}
					else {
						z = future_index;
					}
				}
				else if (board.cellState(row, z) == MNKCellState.FREE) {
					int j = z+1;
					int symCount = 0;
					while (j < board.N && board.cellState(row, j) == symb && symCount < k -1) {
						symCount++;	
						j++;
					}
					if (j == board.N) {
						if (symCount == k-1) {
							threats[k1SopenIndex]++;
							z = board.N;
						}
					}
					else {
						if (symCount == k-1 && board.cellState(row, j) == MNKCellState.FREE) {
							threats[k1OpenIndex]++;
							z = j;
						}
						else if (symCount == k-2 && board.cellState(row, j) == MNKCellState.FREE){
							threats[k2OpenIndex]++;
							z++;
						}
						else if (symCount == k-1) {
							threats[k1SopenIndex]++;
							z = j + 1;
						}
						else if (board.cellState(row, j) == MNKCellState.FREE && symCount > 0) {
							z = j - 1;
						}
						else if (board.cellState(row, j) == MNKCellState.FREE && symCount == 0){
							z = j;
						}
						else {
							z = j + 1;
						}
					}
				}
				else
					z++;
			}
		}
		else {
			for (int z = 0; z < board.N - k + 1; z++) {
				if (board.cellState(row, z) == MNKCellState.FREE && board.cellState(row, z+1) == symb) {
					boolean diff_symb = false;
					int symCount = 1;
					while (!diff_symb && symCount < k-2) {
						if (board.cellState(row, z + symCount + 1) != symb) {
							diff_symb = true;
							break;
						}
						symCount++;
					}
					if (!diff_symb && symCount == k-2 && board.cellState(row, z + symCount + 1) == MNKCellState.FREE) {
						threats[k2OpenIndex]++;					
					}
				}
			}
		}
	}
	
	
	//given board, symb: P1 and P2 based on which sequences are we looking for
	//allSeq: true if symbols > k-1, false otherwise
	//col: number of the column we are inspecting
	//threats: myThreats or enemyThreats based on whose threats we are counting
	protected void countColSequence(MNKBoard board, MNKCellState symb, boolean allSeq, int col, int[] threats) {
		if (allSeq) {
			int z = 0;
			while (z < board.M - k + 1) {
				if (board.cellState(z, col) == symb) {
					int future_index = z + 1;
					int zeros_count = 0;
					boolean diff_symb = false;
					for (int j = z + 1; j < z + k; j++) {
						if (board.cellState(j, col) == MNKCellState.FREE) { 
							zeros_count++;
							future_index = j;
						}
						else if (board.cellState(j, col) != symb) {
							future_index = j+1;
							diff_symb = true;
							break;
						}
					}
					if (zeros_count <= 1 && !diff_symb) {
						threats[k1SopenIndex]++;
						z++;
					}
					else {
						z = future_index;
					}
				}
				else if (board.cellState(z, col) == MNKCellState.FREE) {
					int j = z+1;
					int symCount = 0;
					while (j < board.M && board.cellState(j, col) == symb && symCount < k -1) {
						symCount++;	
						j++;
					}
					if (j == board.M) {
						if (symCount == k-1) {
							threats[k1SopenIndex]++;
						}
						return;
					}
					else {
						if (symCount == k-1 && board.cellState(j, col) == MNKCellState.FREE) {
							threats[k1OpenIndex]++;
							z = j;
						}
						else if (symCount == k-2 && board.cellState(j, col) == MNKCellState.FREE){
							threats[k2OpenIndex]++;
							z++;
						}
						else if (symCount == k-1) {
							threats[k1SopenIndex]++;
							z = j + 1;
						}
						else if (board.cellState(j, col) == MNKCellState.FREE && symCount > 0) {
							z = j - 1;
						}
						else if (board.cellState(j, col) == MNKCellState.FREE && symCount == 0){
							z = j;
						}
						else {
							z = j + 1;
						}
					}
				}
				else
					z++;
			}
		}
		else {
			for (int z = 0; z < board.M - k + 1; z++) {
				if (board.cellState(z, col) == MNKCellState.FREE && board.cellState(z+1, col) == symb) {
					boolean diff_symb = false;
					int symCount = 1;
					while (!diff_symb && symCount < k-2) {
						if (board.cellState(z + symCount + 1, col) != symb) {
							diff_symb = true;
							break;
						}
						symCount++;
					}
					if (!diff_symb && symCount == k-2 && board.cellState(z + symCount + 1, col) == MNKCellState.FREE) {
						threats[k2OpenIndex]++;
					}
					return;
				}
			}
		}
	}
	
	protected void countDiagSequence (int row, int col, MNKBoard board) {
		//System.out.println("Sto controllando la diagonale che parte da " + row + " " + col);
		int i = 0;
		while (row + i < board.M && col + i < board.N) {
			if (board.cellState(row + i, col + i) == MNKCellState.FREE) {
				if ((row + i + 1 >= board.M || col + i + 1 >= board.N) || board.cellState(row + i + 1, col + i + 1) == MNKCellState.FREE) {
					i++;
				}
				else {
					int j = i + 1;
					MNKCellState symb = board.cellState(row + j, col + j);
					int symCount = 0;
					while (row + j < board.M && col + j < board.N && board.cellState(row + j, col + j) == symb && symCount < k-1) {
						symCount++;
						j++;
					}
					if (row + j == board.M || col + j == board.N) {
						if (symCount == k - 1) {
							increaseThreat(k1SopenIndex, symb);
						}
						return;
					}
					else {
						if (symCount == k - 1 && board.cellState(row + j, col + j) == MNKCellState.FREE) {
							increaseThreat(k1OpenIndex, symb);
							i = j;
						}
						else if (symCount == k - 2 && board.cellState(row + j, col + j) == MNKCellState.FREE) {
							increaseThreat(k2OpenIndex, symb);
							i++;
						}
						else if (symCount == k - 1) {
							increaseThreat(k1SopenIndex, symb);
							i = j + 1;
						}
						else if (board.cellState(row + j, col + j) == MNKCellState.FREE)
							i = j;
						else
							i = j + 1;
					}
				}
			}
			else {
				MNKCellState symb = board.cellState(row + i, col + i);
				int future_index = i + 1;
				int zeros_count = 0;
				boolean diff_symb = false;
				int counter = 1;
				int symb_count = 1;
				while ((row + i + counter) < board.M && (col + i + counter) < board.N && counter < k) {
					if (board.cellState(row + i + counter, col + i + counter) == MNKCellState.FREE) { 
						if (zeros_count == 0)
							future_index = i + counter;
						else
							future_index = i + counter - 1;
						zeros_count++;
					}
					else if (board.cellState(row + i + counter, col + i + counter) != symb) {
						future_index = i + counter;
						diff_symb = true;
						break;
					}
					else
						symb_count++;
					counter++;
				}
				if (zeros_count <= 1 && !diff_symb && symb_count == k-1) {
					increaseThreat(k1SopenIndex, symb);
					i++;
				}
				else {
					i = future_index;
				}
			}
		}
	}
	
	protected void countAntidiagSequence (int row, int col, MNKBoard board) {
		//System.out.println("Sto controllando l' antidiagonale che parte da " + row + " " + col);
		int i = 0;
		while (row + i < board.M && col - i >= 0) {
			if (board.cellState(row + i, col - i) == MNKCellState.FREE) {
				if ((row + i + 1 >= board.M || col - i - 1 < 0) || board.cellState(row + i + 1, col - i - 1) == MNKCellState.FREE) {
					i++;
				}
				else {
					int j = i + 1;
					MNKCellState symb = board.cellState(row + j, col - j);
					int symCount = 0;
					while (row + j < board.M && col - j >= 0 && board.cellState(row + j, col - j) == symb && symCount < k-1) {
						symCount++;
						j++;
					}
					if (row + j == board.M || col - j == - 1) {
						if (symCount == k - 1) {
							increaseThreat(k1SopenIndex, symb);
						}
						return;
					}
					else {
						if (symCount == k - 1 && board.cellState(row + j, col - j) == MNKCellState.FREE) {
							increaseThreat(k1OpenIndex, symb);
							i = j;
						}
						else if (symCount == k - 2 && board.cellState(row + j, col - j) == MNKCellState.FREE) {
							increaseThreat(k2OpenIndex, symb);
							i++;
						}
						else if (symCount == k - 1) {
							increaseThreat(k1SopenIndex, symb);
							i = j + 1;
						}
						else if (board.cellState(row + j, col - j) == MNKCellState.FREE)
							i = j;
						else
							i = j + 1;
					}
				}
			}
			else {
				MNKCellState symb = board.cellState(row + i, col - i);
				int future_index = i + 1;
				int zeros_count = 0;
				boolean diff_symb = false;
				int counter = 1;
				int symbCount = 1;
				while (counter < k && (row + i + counter) < board.M && (col - i - counter) >= 0) {
					if (board.cellState(row + i + counter, col - i - counter) == MNKCellState.FREE) { 
						if (zeros_count == 0)
							future_index = i + counter;
						else
							future_index = i + counter - 1;
						zeros_count++;
					}
					else if (board.cellState(row + i + counter, col - i - counter) != symb) {
						future_index = i + counter;
						diff_symb = true;
						break;
					}
					else
						symbCount++;
					counter++;
				}
				if (zeros_count <= 1 && !diff_symb && symbCount == k-1) {
					increaseThreat(k1SopenIndex, symb);
					i++;
				}
				else {
					i = future_index;
				}
			}
		}		
	}
	
	
	

		/*	
		 * 		AUXILLARY FUNCTIONS REGION
		 */
	
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
	}
	
	/*	These two functions update the maps that keep track of the current symbols in each row/column
	 * 	given a row and col, they add/remove one symbol from the indicated maps
	 *  boolean my_move is true if it is our move, false otherwise
	 */
	
	public void addSymbol(int row, int col, boolean my_move) {
		if (my_move) {
			rowSymbols[row][0]++;
			if (rowSymbols[row][0] == k-2 && rowSymbols[row][1] < k-2)
				rowEval.add(row);
			colSymbols[col][0]++;
			if (colSymbols[col][0] == k-2 && colSymbols[col][1] < k-2)
				colEval.add(col);

			if (diagBoard[row][col] == 1 || diagBoard[row][col] == 3)
				symbDiag(row, col, 0, 1);

			if (diagBoard[row][col] >= 2)
				symbAntiDiag(row, col, 0, 1);
		}
		
		else {
			rowSymbols[row][1]++;
			if (rowSymbols[row][1] == k-2 && rowSymbols[row][0] < k-2)
				rowEval.add(row);
			colSymbols[col][1]++;
			if (colSymbols[col][1] == k-2 && colSymbols[col][0] < k-2)
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
			if (rowSymbols[row][0] == k-3 && rowSymbols[row][1] < k-2)
				rowEval.remove(row);
			colSymbols[col][0]--;
			if (colSymbols[col][0] == k-3 && colSymbols[col][1] < k-2)
				colEval.remove(col);

			if (diagBoard[row][col] == 1 || diagBoard[row][col] == 3)
				symbDiag(row, col, 0, -1);

			if (diagBoard[row][col] >= 2)
				symbAntiDiag(row, col, 0, -1);

		}
		else {
			rowSymbols[row][1]--;
			if (rowSymbols[row][1] == k-3 && rowSymbols[row][0] < k-2)
				rowEval.remove(row);
			colSymbols[col][1]--;
			if (colSymbols[col][1] == k-3 && colSymbols[col][0] < k-2)
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
			return (myThreats[k1OpenIndex] + myThreats[k1SopenIndex] > 0);
		else
			return (enemyThreats[k1OpenIndex] + enemyThreats[k1SopenIndex] > 0);
	}
	

	protected void increaseThreat (int index, MNKCellState symb) {
		if (symb == mySymb)
			myThreats[index]++;
		else
			enemyThreats[index]++;
		//commento a caso per far eun push
	}
	
	public void symbDiag(int row, int col, int player, int delta){
		if (row > col)
			diagColSymb[row - col][player] += delta;
		else
			diagRowSymb[col - row][player] += delta;
	}

	public void symbAntiDiag(int row, int col, int player, int delta){
		int deltaCol = diagRow.length - col - 1;
		if (row <= deltaCol)
			antiDiagRowSymb[row + col][player] += delta;
		else
			antiDiagColSymb[row - deltaCol][player] += delta;
	}

	//computes the number of threats * evaluation
	protected int threatCalculation(boolean myTurn) {
		if (myTurn && (enemyThreats[k1OpenIndex] > 0 || enemyThreats[k1SopenIndex] > 1) && (myThreats[k1OpenIndex] + myThreats[k1SopenIndex] == 0))
			return MIN_EVALUATION;
		
		if (!myTurn && (myThreats[k1OpenIndex] > 0 || myThreats[k1SopenIndex] > 1) && (enemyThreats[k1OpenIndex] + enemyThreats[k1SopenIndex] == 0))
			return MAX_EVALUATION;

		int eval = 0;
		for (int i = 0; i < MAX_THREATS; i++) {
			eval = eval + (myThreats[i] * myThreatsEval[i]) - (enemyThreats[i] * enemyThreatsEval[i]);
		}
		return eval;
	}
	
}