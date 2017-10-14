public class QuartoCarloAgent extends QuartoAgent {

    //Example AI
    public QuartoCarloAgent(GameClient gameClient, String stateFileName) {
        // because super calls one of the super class constructors(you can overload constructors), you need to pass the parameters required.
        super(gameClient, stateFileName);
    }

    //MAIN METHOD
    public static void main(String[] args) {
        //start the server
        GameClient gameClient = new GameClient();

        String ip = null;
        String stateFileName = null;
        //IP must be specified
        if(args.length > 0) {
            ip = args[0];
        } else {
            System.out.println("No IP Specified");
            System.exit(0);
        }
        if (args.length > 1) {
            stateFileName = args[1];
        }

        gameClient.connectToServer(ip, 4321);
        QuartoSemiRandomAgent quartoAgent = new QuartoSemiRandomAgent(gameClient, stateFileName);
        quartoAgent.play();

        gameClient.closeConnection();

    }

    /*
	 * This code will try to find a piece that the other player can't use to win immediately
	 */
    @Override
    protected String pieceSelectionAlgorithm() {		
		MonteCarlo mc = new MonteCarlo(new QuartoBoard(this.quartoBoard));
		
	    int[] pieces = mc.getPossiblePieces();
	    
		mc.runSimulation(800, pieces);	
		
    	int max = 0;
    	for(int i = 1; i < mc.simulations.size(); i++) {
    		double maxProb = mc.wins.get(""+max) / mc.simulations.get(""+max);
    		double prob = mc.wins.get(""+i) / mc.simulations.get(""+i);
    		if (maxProb < prob) max = i;
    	}
    	
    	String BinaryString = String.format("%5s", Integer.toBinaryString(max)).replace(' ', '0');
        return BinaryString;
    }

    /*
     * Do Your work here
     * The server expects a move in the form of:   row,column
     */
    @Override
    protected String moveSelectionAlgorithm(int pieceID) {
    	MonteCarlo mc = new MonteCarlo(new QuartoBoard(this.quartoBoard));
    	
    	int[][] moves = mc.getPossibleMoves();
    	
    	mc.runSimulation(800, moves, 0);	
		
    	int max = 0;
    	for (int i = 1; i < moves.length; i++) {
    		String maxMove = moves[max][0] + ":" + moves[max][1];
    		String move = moves[i][0] + ":" + moves[i][1];
    		double maxProb = mc.wins.get(maxMove) / mc.simulations.get(maxMove);
    		double prob = mc.wins.get(move) / mc.simulations.get(move);
    		if (maxProb < prob) max = i;
    	}

    	return moves[max][0] + "," + moves[max][1];
    }



    //loop through board and see if the game is in a won state
    private boolean checkIfGameIsWon() {

        //loop through rows
        for(int i = 0; i < NUMBER_OF_ROWS; i++) {
            //gameIsWon = this.quartoBoard.checkRow(i);
            if (this.quartoBoard.checkRow(i)) {
                System.out.println("Win via row: " + (i) + " (zero-indexed)");
                return true;
            }

        }
        //loop through columns
        for(int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            //gameIsWon = this.quartoBoard.checkColumn(i);
            if (this.quartoBoard.checkColumn(i)) {
                System.out.println("Win via column: " + (i) + " (zero-indexed)");
                return true;
            }

        }

        //check Diagonals
        if (this.quartoBoard.checkDiagonals()) {
            System.out.println("Win via diagonal");
            return true;
        }

        return false;
    }
}

