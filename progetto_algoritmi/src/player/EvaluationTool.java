package player;
import java.util.HashMap;

import mnkgame.MNKBoard;
import mnkgame.MNKCellState;

public class EvaluationTool {
	//variables for threat evaluation
	int k1OpenIndex = 0;
	int k2OpenIndex = 1;
	int k1SopenIndex = 2;
	int threatsEval[];
	int MAX_THREATS = 3;
	int myThreats[];
	int enemyThreats[];
	
	public int MAX_EVALUATION = 200;				//if we win
	public int MIN_EVALUATION = -200;				//if enemy player win
	
	//maps that store how many symbols there are in each row/column to evaluate a board
	//saves time if n symbols < k-2
	HashMap<Integer, int[]> row_symbols = new HashMap<>();
	HashMap<Integer, int[]> col_symbols = new HashMap<>();
	
	//memorizes if i'm P1 or P2
	MNKCellState mySymb;
	MNKCellState enemySymb;
	
	//array that memorizes which cells give origin to interesting diagonals for evaluation
	int diagRow[];
	int diagCol[];
	int k; 				//number of aligned symbols required to win; 
	
	public EvaluationTool(int m, int n, int k, boolean first) {
		this.k = k;
		
		//memorize if I'm p1 or p2
		if (first) {
			mySymb = MNKCellState.P1;
			enemySymb = MNKCellState.P2;
		}
		else {
			mySymb = MNKCellState.P2;
			enemySymb = MNKCellState.P1;
		}
		
		diagRow = new int[n];
		for (int i = 0; i < n; i++)
			diagRow[i] = 0;
		diagCol = new int[m];
		for (int i = 0; i < m; i++)
			diagCol[i] = 0;
		
		//calculates which diagonals are interesting for us
		diagonalCalculations(m, n, k);
		
		threatsEval = new int[MAX_THREATS];
		threatsEval[k1OpenIndex] = 100;
		threatsEval[k2OpenIndex] = 10;
		threatsEval[k1SopenIndex] = 1;
		
		myThreats = new int[MAX_THREATS];
		enemyThreats = new int[MAX_THREATS];
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
		for (int i = 0; i < board.M; i++) {
			//if they have sufficient amount of symbols in them, for either player
			//try to find sequences
			if (row_symbols.containsKey(i) && row_symbols.get(i)[0] + row_symbols.get(i)[1] < board.N) {
				if (row_symbols.get(i)[0] >= k-1) {
					countRowSequence(board, mySymb, true, i, myThreats);
					if (myTurn && checkWin(myTurn))
						return MAX_EVALUATION;
				}
				else if (row_symbols.get(i)[0] >= k-2) {
					countRowSequence(board, mySymb, false, i, myThreats);
					if (myTurn && checkWin(myTurn))
						return MAX_EVALUATION;
				}
				if (row_symbols.get(i)[1] >= k-1) {
					countRowSequence(board, enemySymb, true, i, enemyThreats);
					if (!myTurn && checkWin(myTurn))
						return MIN_EVALUATION;
				}
				else if (row_symbols.get(i)[1] >= k-2) {
					countRowSequence(board, enemySymb, false, i, enemyThreats);
					if (!myTurn && checkWin(myTurn))
						return MIN_EVALUATION;
				}
			}
		}
		for (int i = 0; i < board.N; i++) {
			//if they have sufficient amount of symbols in them, for either player
			//try to find sequences
			if (col_symbols.containsKey(i) && col_symbols.get(i)[0] + col_symbols.get(i)[1] < board.M) {
				if (col_symbols.get(i)[0] >= k-1) {
					countColSequence(board, mySymb, true, i, myThreats);
					if (myTurn && checkWin(myTurn))
						return MAX_EVALUATION;
				}
				else if (col_symbols.get(i)[0] >= k-2) {
					countColSequence(board, mySymb, false, i, myThreats);
					if (myTurn && checkWin(myTurn))
						return MAX_EVALUATION;
				}
				if (col_symbols.get(i)[1] >= k-1) {
					countColSequence(board, enemySymb, true, i, enemyThreats);
					if (!myTurn && checkWin(myTurn))
						return MIN_EVALUATION;
				}
				else if (col_symbols.get(i)[1] >= k-2) {
					countColSequence(board, enemySymb, false, i, enemyThreats);
					if (!myTurn && checkWin(myTurn))
						return MIN_EVALUATION;
				}
			}
		}
		
		//count diagonal and anti-diagonal sequences
		
		for (int i = 0; i < board.N; i++) {
			switch (diagRow[i]) {
				case 1: {
					countDiagSequence(0, i, board);
					break;
				}
				case 2: {
					countAntidiagSequence(0, i, board);
					break;
				}
				case 3:{
					countDiagSequence(0, i, board);
					countAntidiagSequence(0, i, board);
					break;
				}
				default:
					break;
			}
		}
		
		int i = 0;
		while (diagCol[i] > 0) {
			countDiagSequence(i, 0, board);
			countAntidiagSequence(i, board.N - 1, board);
			i++;
		}
		
		return threatCalculation();
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
					while (board.cellState(row, j) == symb && symCount < k -1 && j < board.N) {
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
						else if (board.cellState(row, j) == MNKCellState.FREE) {
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
					if (!diff_symb && symCount == k-2 && board.cellState(row, z + symCount + 1) == MNKCellState.FREE)
						threats[k2OpenIndex]++;
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
					while (board.cellState(j, col) == symb && symCount < k -1 && j < board.M) {
						symCount++;	
						j++;
					}
					if (j == board.M) {
						if (symCount == k-1)
							threats[k1SopenIndex]++;
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
						else if (board.cellState(j, col) == MNKCellState.FREE) {
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
			for (int z = 0; z < board.M - k; z++) {
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
		int i = 0;
		while (row + i < board.M && col + i < board.N) {
			if (board.cellState(row + i, col + i) == MNKCellState.FREE) {
				if (row + i + 1 >= board.M || col + i + 1 >= board.N || board.cellState(row + i + 1, col + i + 1) == MNKCellState.FREE) {
					i++;
				}
				else {
					int j = i + 1;
					MNKCellState symb = board.cellState(row + j, col + j);
					int symCount = 0;
					while (board.cellState(row + j, col + j) == symb && symCount < k-1 && row + j < board.M && col + j < board.N) {
						symCount++;
						j++;
					}
					if (row + j == board.M || col + j == board.N) {
						if (symCount == k - 1)
							increaseThreat(k1SopenIndex, symb);
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
				while (counter < k) {
					if (board.cellState(row + i + counter, col + i + counter) == MNKCellState.FREE) { 
						zeros_count++;
						future_index = i + counter;
					}
					else if (board.cellState(row + i + counter, col + i + counter) != symb) {
						future_index = i + counter + 1;
						diff_symb = true;
						break;
					}
					counter++;
				}
				if (zeros_count <= 1 && !diff_symb) {
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
		int i = 0;
		while (row + i < board.M && col - i >= 0) {
			if (board.cellState(row + i, col - i) == MNKCellState.FREE) {
				if (row + i + 1 >= board.M || col - i - 1 < 0 || board.cellState(row + i + 1, col - i - 1) == MNKCellState.FREE) {
					i++;
				}
				else {
					int j = i + 1;
					MNKCellState symb = board.cellState(row + j, col - j);
					int symCount = 0;
					while (board.cellState(row + j, col - j) == symb && symCount < k-1 && row + j < board.M && col - j >= 0) {
						symCount++;
						j++;
					}
					if (row + j == board.M || col - j == - 1) {
						if (symCount == k - 1)
							increaseThreat(k1SopenIndex, symb);
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
				while (counter < k) {
					if (board.cellState(row + i + counter, col - i - counter) == MNKCellState.FREE) { 
						zeros_count++;
						future_index = i + counter;
					}
					else if (board.cellState(row + i + counter, col - i - counter) != symb) {
						future_index = i + counter + 1;
						diff_symb = true;
						break;
					}
				}
				if (zeros_count <= 1 && !diff_symb) {
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
		for (int i = 0; i < m; i++) {
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
			if (!row_symbols.containsKey(row)) {
				row_symbols.put(row, new int[] {0, 0});
			}
			row_symbols.get(row)[0]++;
			
			if (!col_symbols.containsKey(col)) {
				col_symbols.put(col, new int[] {0, 0});
			}
			col_symbols.get(col)[0]++;
		}
		
		else {
			if (!row_symbols.containsKey(row)) {
				row_symbols.put(row, new int[] {0, 0});
			}
			row_symbols.get(row)[1]++;
			
			if (!col_symbols.containsKey(col)) {
				col_symbols.put(col, new int[] {0, 0});
			}
			col_symbols.get(col)[1]++;
		}
	}
	
	
	public void removeSymbol(int row, int col, boolean my_move) {
		if (my_move) {
			row_symbols.get(row)[0]--;
			col_symbols.get(col)[0]--;
		}
		else {
			row_symbols.get(row)[1]--;
			col_symbols.get(col)[1]--;
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
	}
	
	
	
	//computes the number of threats * evaluation
	protected int threatCalculation() {
		int eval = 0;
		for (int i = 0; i < MAX_THREATS; i++) {
			eval = eval + (myThreats[i] - enemyThreats[i]) * threatsEval[i];
		}
		return eval;
	}
	
}
