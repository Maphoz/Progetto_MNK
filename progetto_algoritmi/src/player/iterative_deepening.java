/*package player;

public class iterative_deepening extends alphabeta {
// ITERATIVE DEEPING con tecnica Aspiration Windows
	public iterative_deepening() {
		isFirstTurn = True;
		for(int distance = 1; distance < MAX_DISTANCE && !outOfTime(); distance++) {
			  if(eval <= alpha || eval >= beta || isFirstTurn) // Eval outside window, alla prima iterazione lo facciamo avvenire sempre così facciamo un normale alphabeta
				{
				   // We need to do a full search
				   isFirstTurn = false;
				   eval=alphabeta(position, -inf, + inf, distance); //rifai l'alphabeta alla stessa distanza
				}
			    alpha = eval -10;            //window_size 10, da modificare e fare test
				beta = eval +10;
				eval = alphaBeta(position, alpha, beta, distance); //alphabeta modificato più veloce
				current_depth++;
			}
			bestmove=eval;
			play(bestmove);			
	}

}


/*

But since we now have a previous iteration's result we could estimate what the alpha and beta should be. Instead of using -infinity and +infinity we can use the evaluation 
returned from the last iteration and put a 'window' around it. So we call alphaBeta() with:

alpha = last_iteration_eval + window_size
beta = last_iteration_eval - window_size





valWINDOW_alpha = 50
valWINDOW_beta = 50
alpha = -INFINITY;
beta = INFINITY;

for (depth = 1;;) {
    val = AlphaBeta(depth, alpha, beta);
    if (TimedOut())
        break;
    if ((val <= alpha) || (val >= beta)) {
    	if(val <= alpha)                                                     WINDOW SIZE INCREMENTALE un' idea importante e di aumentare solo la window size di alpha o di beta se fallisce
    		valWINDOW_alpha = valWINDOW_alpha + incremento           
    	else if (val >= beta)
    		valWINDOW_beta = valWINDOW_beta + incremento
    		
        alpha = -INFINITY;    // We fell outside the window, so try again with a
        beta = INFINITY;      //  full-width window (and the same depth).
        val = AlphaBeta(depth, alpha, beta);
    }

    alpha = val - valWINDOW_alpha;  // Set up the window for the next iteration.
    beta = val + valWINDOW_beta;
    depth++;
}

*/