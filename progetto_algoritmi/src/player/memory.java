package player;

public class memory {
	int i;
	int j;
	int score;
	int depth;
	int distance_from_root;
	boolean incompleteLevel;
	
	public memory(int score, int i, int j, int depth, boolean flag, int distance_from_root) {
		this.i = i;
		this.j = j;
		this.score = score;
		this.depth = depth;
		this.distance_from_root = distance_from_root;
		incompleteLevel = flag;
	}
}
