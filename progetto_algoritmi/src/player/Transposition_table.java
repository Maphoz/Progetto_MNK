package player;
import java.util.Random;

import mnkgame.MNKCellState;
import player.memory;


public class Transposition_table {
	protected class transposition_hash_cell {
		protected short score;
		protected short depth;     //sarebbe la distanza dalle foglie e serve a capire se dobbiamo prendere il valore nella cella della TT o se � scarso, impreciso e lo dobbiamo ricalcolare
		protected int mask_key;  // questa funge da maschera per la chiave long, le collisioni sono bassissime, tipo 3% in configurazione 10 10 7 o anche meno di 3%, anche 0.5%

		protected byte i;
		protected byte j;
		protected boolean flag;
		protected short distance_from_root;
		protected transposition_hash_cell(int score){	
			this.score=(short)score;
			this.depth = 0;
			this.mask_key = 0;
			this.i = -1;
			this.j = -1;
			this.distance_from_root = -1;
		}
	}
	
	protected final int hash_size;
	protected final int ScoreNotFound;
	
	protected int M;
	protected int N;

	protected long[][][] storage;//deve essere una matrice tridimensionale
	protected transposition_hash_cell[] transposition_hash;    //l'hash table � 2^16, da inizializzare con tutti i campi val a -2 o comunque un valore per far capire che quella cella � vuota

	public Transposition_table(int M, int N){
		
		hash_size = (int)Math.pow(2,22);  //dimensione della tabella hash 
	
		ScoreNotFound = 50000; //indica se quando Osama controlla se e' presente nella transposition_hash lo score , non lo trova
		transposition_hash = new transposition_hash_cell[hash_size];
		for(int i=0; i<hash_size; i++){
			transposition_hash_cell t = new transposition_hash_cell(-2);
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
	protected int lowbias32(int x)
	{
	    x ^= x >> 17;
	    x *= 0xed5ad4bb;
	    x ^= x >> 11;
	    x *= 0xac4c1b51;
	    x ^= x >> 15;
	    x *= 0x31848bab;
	    x ^= x >> 14;
	    return x;
	}

	public memory gain_score (long key, int depth){   //funzione che deve fare osama per prendere lo score, ritorna la costante ScoreNotFound se non � stato trovato
		int transposition_table_index = Math.abs((int) (lowbias32((int)key) % (hash_size - 1)));
		memory mem;
		if(transposition_hash[transposition_table_index].depth >= depth) {
			
			if(transposition_hash[transposition_table_index].mask_key == (int)key) { //le collisioni dovute alla maschera sono estremamente basse
				mem = new memory((int)transposition_hash[transposition_table_index].score, (int)transposition_hash[transposition_table_index].i, (int)transposition_hash[transposition_table_index].j, (int)transposition_hash[transposition_table_index].depth, transposition_hash[transposition_table_index].flag, (int)transposition_hash[transposition_table_index].distance_from_root);
				return mem;
			}
			else {
				mem = new memory(ScoreNotFound, -1, -1, -1, false, -1);
				return mem;
			}
		}
		else {
			mem = new memory(ScoreNotFound, -1, -1, -1, false, -1);
			return mem;
		}
	}

	public int gain_score (long key){
		int transposition_table_index = Math.abs((int) (lowbias32((int)key) % (hash_size - 1)));

		if(transposition_hash[transposition_table_index].score != -2 && transposition_hash[transposition_table_index].mask_key == (int)key && !transposition_hash[transposition_table_index].flag){
			return transposition_hash[transposition_table_index].score;
		}
		else
			return ScoreNotFound;
	}

	//Osama genera la chiave, controlla se e' presente nella tabella tramite gain_score, se non c'e' fa una evaluation e poi salva lo score con save_data
	public void save_data(int score, long key, int depth, int i, int j, boolean saveMove, int distance_from_root){
		int transposition_table_index =  Math.abs((int) (lowbias32((int)key) % (hash_size - 1)));

		/* 
		if(transposition_hash[transposition_table_index].score == -2 || transposition_hash[transposition_table_index].depth<(short)depth) {  //replace in base a cella vuota o score scarso gi� presente nella TT
			transposition_hash[transposition_table_index].score=(short)score;
			transposition_hash[transposition_table_index].depth=(short)depth;
			transposition_hash[transposition_table_index].mask_key= (int)key; 
			transposition_hash[transposition_table_index].i = (byte)i;
			transposition_hash[transposition_table_index].j = (byte)j;
			transposition_hash[transposition_table_index].flag = saveMove;
			transposition_hash[transposition_table_index].distance_from_root = (short)distance_from_root;
		}
		*/

		if (saveMove && transposition_hash[transposition_table_index].depth<(short)depth){
			transposition_hash[transposition_table_index].score=(short)score;
			transposition_hash[transposition_table_index].depth=(short)depth;
			transposition_hash[transposition_table_index].mask_key= (int)key; 
			transposition_hash[transposition_table_index].i = (byte)i;
			transposition_hash[transposition_table_index].j = (byte)j;
			transposition_hash[transposition_table_index].flag = saveMove;
			transposition_hash[transposition_table_index].distance_from_root = (short)distance_from_root;
		}
		else if (!saveMove && transposition_hash[transposition_table_index].score == -2){
			transposition_hash[transposition_table_index].score=(short)score;
			transposition_hash[transposition_table_index].depth=(short)0;
			transposition_hash[transposition_table_index].mask_key= (int)key; 
			transposition_hash[transposition_table_index].i = -1;
			transposition_hash[transposition_table_index].j = -1;
			transposition_hash[transposition_table_index].flag = false;
			transposition_hash[transposition_table_index].distance_from_root = (short)-1;
		}
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

