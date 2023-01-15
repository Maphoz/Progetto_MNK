package player;

public class memory {
	int i;
	int j;
	int score;
	int depth;
	boolean incompleteLevel;
	
	public memory(int score, int i, int j, int depth, boolean flag) {
		this.i = i;
		this.j = j;
		this.score = score;
		this.depth = depth;
		incompleteLevel = flag;
	}
}
