package player;

import mnkgame.MNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;

import java.util.HashSet;

public class GameBoard extends MNKBoard{

    public int proximityMatrix[][];     //keeps memorized how many marked cells there are adjacent to each cell
    HashSet<MNKCell> IC;                //Interesting cells


    public GameBoard(int M, int N, int K)  throws IllegalArgumentException {
        super(M, N, K);
        proximityMatrix = new int[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                proximityMatrix[i][j] = 0;

        IC = new HashSet<MNKCell>((int) Math.ceil((M*N) / 0.75)); 
    }

    public MNKCell[] getInterestingCells() {
		return IC.toArray(new MNKCell[IC.size()]);
	}

    public boolean isFree(int i, int j){
        return B[i][j] == MNKCellState.FREE;
    }
    
    @Override
    public MNKGameState markCell(int i, int j) throws IndexOutOfBoundsException, IllegalStateException {
        //chiamamo il metodo regolare markCell
        MNKGameState state = super.markCell(i, j);

        MNKCell mCell = new MNKCell(i, j);
        IC.remove(mCell);
        //aggiorniamo la matrice e l'hashset
        for (int z = i - 1; z < i + 2; z++){
            for (int k = j - 1; k < j + 2; k++){
                if ((z != i || k != j) && inBounds(z, k)){
                    proximityMatrix[z][k]++;
                    if (isFree(z,k) && proximityMatrix[z][k] == 1)
                        IC.add(new MNKCell(z, k));
                }
            }
        }
        return state;
    }

    @Override
    public void unmarkCell() throws IllegalStateException {
        MNKCell tmp = MC.getLast();
        MNKCell last = new MNKCell(tmp.i, tmp.j);

        super.unmarkCell();
        if (proximityMatrix[last.i][last.j] > 0)
            IC.add(last);
        for (int z = last.i - 1; z < last.i + 2; z++)
            for (int k = last.j - 1; k < last.j + 2; k++){
                if ((z != last.i || k != last.j) && inBounds(z, k)){
                    proximityMatrix[z][k]--;
                    if (isFree(z,k) && proximityMatrix[z][k] == 0)
                        IC.remove(new MNKCell(z, k));
                }
            }
    }

    public boolean isEqual(int i, int j, MNKCellState P){
        return  (B[i][j] == P);
    }
    
    private boolean inBounds(int i, int j){
        return (i >= 0 && i < M && j >= 0 && j < N);
    }
}
