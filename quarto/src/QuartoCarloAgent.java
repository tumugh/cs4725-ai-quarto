// Julie Anne Moore
// Colin Barber

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
        QuartoCarloAgent quartoAgent = new QuartoCarloAgent(gameClient, stateFileName);
        quartoAgent.play();

        gameClient.closeConnection();
    }

    /*
	 * This code will try to find a piece that the other player can't use to win immediately
	 */
    @Override
    protected String pieceSelectionAlgorithm() {	
		 MonteCarlo mc = new MonteCarlo(this.timeLimitForResponse-1000, 1 / Math.sqrt(2), false);
		 String bestAction = mc.UCTSearch(this.quartoBoard, null);
		 return bestAction; 
    }

    /*
     * Do Your work here
     * The server expects a move in the form of:   row,column
     */
    @Override
    protected String moveSelectionAlgorithm(int pieceID) { 
		 MonteCarlo mc = new MonteCarlo(this.timeLimitForResponse-1000, 1 / Math.sqrt(2), false);
		 String bestAction = mc.UCTSearch(this.quartoBoard, pieceID);
		 return bestAction; 
    }

    //loop through board and see if the game is in a won state
    private boolean checkIfGameIsWon() {

    	System.out.println(this.timeLimitForResponse+"");
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

