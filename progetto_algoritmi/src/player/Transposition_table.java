package player;
import java.util.Random;
import mnkgame.MNKCellState;


public class Transposition_table {
	private class transposition_hash_cell {
		public int score;
		public long key;
		public transposition_hash_cell(int score, long key){	
			this.score=score;
			this.key = key;
			
		}
	}
	
	protected final int hash_size;
	protected final int ScoreNotFound;
	protected final int max_ite;
	protected final int max_ispezione;
	protected int M;
	protected int N;
	protected long[][][] storage;//deve essere una matrice tridimensionale
	protected transposition_hash_cell[] transposition_hash;    //l'hash table � 2^16, da inizializzare con tutti i campi val a -2 o comunque un valore per far capire che quella cella � vuota

	public Transposition_table(int M, int N){
		hash_size = (int)Math.pow(2,9);  //dimensione della tabella hash 
		max_ite = 50;  //n_max_iterazioni prima di ritornare ScoreNotFound nella ricerca della transposition_hash per trovare un Game_State uguale 
		max_ispezione = 60;
		ScoreNotFound = -10; //indica se quando Osama controlla se � presente nella transposition_hash lo stesso Game_state, non lo trova
		transposition_hash = new transposition_hash_cell[hash_size];
		for(int i=0; i<hash_size; i++){
			transposition_hash_cell t = new transposition_hash_cell(-2, 0);
			transposition_hash[i] = t;
		}
		this.M=M;
		this.N=N;
	}
	public void initTableRandom()
	{
		this.storage = new long[2][M][N];
		for(int i=0; i<2; i++){
			for(int j=0; j<M; j++){
				for(int k=0; k<N; k++){
						storage[i][j][k]= new Random().nextLong();//il numero random in questo caso pu� essere pure negativo
				}
			}
		}
    }
	public long generate_key(long father_key_hash, int x, int y, MNKCellState p){ //y colonne e x le righe, genera la chiave relativa a una cella, la radice ha father_key_hash=(long)0
		if(p == MNKCellState.P1){
			father_key_hash ^= storage[0][x][y];
			}
		if(p == MNKCellState.P2){
			father_key_hash ^= storage[1][x][y];
			}	
		return 	father_key_hash; //con un hash a 64 bit, le collisioni possono avvenire 1 ogni sqrt(2^64) cio� dopo circa 2^32 o 4 miliardi di posizioni calcolate
    }
	public long undo_key(long node_key, int x, int y, MNKCellState p){ //y colonne e x le righe, genera la chiave relativa a una cella, 
		if(p == MNKCellState.P1){
			node_key ^= storage[0][x][y];
			}
		if(p == MNKCellState.P2){
			node_key ^= storage[1][x][y];
			}	
		return 	node_key; //ritorna la chiave padre
    }
	
	//LOWBIAS
	int lowbias32(int x)
	{
	    x ^= x >>> 16;
	    x *= 0x7feb352d;
	    x ^= x >>> 15;
	    x *= 0x846ca68b;
	    x ^= x >>> 16;
	    return x;
	}

	private int ispezione (long key){ //trova la prima cella libera 
		int transposition_table_index = Math.abs((int) (lowbias32((int)key) % (hash_size - 1)));
		int i = 0;
		while(true){	
			if(transposition_hash[transposition_table_index].score==-2){ //hai trovato una cella vuota
				return transposition_table_index;
			}
			else if(i>=max_ispezione){
				return ScoreNotFound;
			}
			transposition_table_index = Math.abs((int) (lowbias32((int)key) % (hash_size - 1)));
			i++;      
		}
	}

	public int gain_score (long key){   //funzione che deve fare osama per prendere lo score, ritorna la costante ScoreNotFound se non � stato trovato
		int transposition_table_index = Math.abs((int) (lowbias32((int)key) % (hash_size - 1)));
		int i=0;
		while(true){ 
			if(transposition_hash[transposition_table_index].key==key) {
				return transposition_hash[transposition_table_index].score;
			}
			transposition_table_index = Math.abs((int) (lowbias32((int)key) % (hash_size - 1))); //ispezione 
			i++;
			if(i>=max_ite){  //si cerca nella transposition_table fino a max_it 
				return ScoreNotFound;
			}
		}
}

	//Osama genera la chiave, controlla se � presente nella tabella tramite gain_score, se non c'� fa una evaluation e poi salva lo score con save_data
	public void save_data(int score, long key){
		int transposition_table_index = ispezione(key);
		if(transposition_table_index==ScoreNotFound) {
			return;
		}
		transposition_hash[transposition_table_index].score=score;
		transposition_hash[transposition_table_index].key=key;
	}
	
	public boolean are_transpositions(MNKCellState[][] A, MNKCellState[][] B, int M, int N){
		if(N==M){
			boolean sim0rot90=true;
			boolean sim0rot180=true;
			boolean sim0rot270=true;
			boolean sim1rot0=true;
			boolean sim1rot90=true;
			boolean sim1rot180=true;
			boolean sim1rot270=true;
			for(int i=0; i<N;i++){
				for(int j=0; j<N; j++){
					if(sim0rot90){
						if(A[i][j]!=B[N-1-j][i])
							sim0rot90=false;
					}
					if(sim0rot180){
						if(A[i][j]!=B[N-1-i][N-1-j])
							sim0rot180=false;
					}
					if(sim0rot270){
						if(A[i][j]!=B[j][N-1-i])
							sim0rot270=false;
					}
					if(sim1rot0){
						int tmp = N-j-1;
						if(i>tmp)
							break;
						else if(A[i][j]!=B[i][tmp])  
							sim1rot0=false;
					}
					if(sim1rot90){
						if(A[i][j]!=B[j][i])
							sim1rot90=false;
					}
					if(sim1rot180){
						if(A[i][j]!=B[N-1-i][j])
							sim1rot180=false;
					}
					if(sim1rot270){
						if(A[i][j]!=B[j][N-1-i])
							sim1rot270=false;
					}

				}
			}
			return(sim0rot90 || sim0rot180 || sim0rot270 || sim1rot0 || sim1rot90 || sim1rot180 || sim1rot270);
		}
		else{
			boolean sim0rot180=true;
			boolean sim1rot0=true;
			boolean sim1rot180=true;
			for(int i =0; i <M; i++){
				for(int j=0; j<N; j++){
					if(sim0rot180){
						if(A[i][j]!=B[M-1-i][N-1-j])
							sim0rot180=false;
					}
					if(sim1rot0){
						int tmp = N-j-1;
						if(i>tmp)
							break;
						else if(A[i][j]!=B[i][tmp])  
							sim1rot0=false;
					}
					if(sim1rot180){
						if(A[i][j]!=B[M-1-i][j])
							sim0rot180=false;
					}
				}
			}
			return(sim0rot180 || sim1rot0 || sim1rot180);
		}
	}
	
}

