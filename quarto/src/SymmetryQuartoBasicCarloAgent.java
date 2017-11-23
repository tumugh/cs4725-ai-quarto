import java.util.ArrayList;
import java.util.HashMap;

public class SymmetryQuartoBasicCarloAgent extends QuartoAgent {

    //Example AI
    public SymmetryQuartoBasicCarloAgent(GameClient gameClient, String stateFileName) {
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
        SymmetryQuartoBasicCarloAgent quartoAgent = new SymmetryQuartoBasicCarloAgent(gameClient, stateFileName);
        quartoAgent.play();

        gameClient.closeConnection();
    }

    /*
	 * This code will try to find a piece that the other player can't use to win immediately
	 */
    @Override
    protected String pieceSelectionAlgorithm() {	
		MonteCarlo mc = new MonteCarlo(this.timeLimitForResponse-1000, 1 / Math.sqrt(2), true);
		ArrayList<Integer> moves = mc.getPossiblePieces(this.quartoBoard);
		
		HashMap<String, int[]> stats = new HashMap<String, int[]>();
		
		long startTime = System.currentTimeMillis();
		long endTime = startTime + (this.timeLimitForResponse-1000);
		while (System.currentTimeMillis() < endTime) {
			for(Integer move : moves) {
				QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);


				int result = mc.playGame(copyBoard, move, false);
				String action = String.format("%5s", Integer.toBinaryString(move)).replace(' ', '0');
				
				int [] updatedStatus = stats.getOrDefault(action, new int[]{0,0});
				updatedStatus[0] += result;
				updatedStatus[1] += 1;
						
				stats.put(action, updatedStatus);
			}
		}
		
		double max = 0;
		String bestAction = "";
		
		for (String action : stats.keySet()) {
			int[] status = stats.get(action);
			double value = (double) status[0] / status[1];
			if (value > max) {
				max = value;
				bestAction = action;
			}
		}
		return bestAction; 
    }

    /*
     * Do Your work here
     * The server expects a move in the form of:   row,column
     */
    @Override
    protected String moveSelectionAlgorithm(int pieceID) { 
		MonteCarlo mc = new MonteCarlo(this.timeLimitForResponse-1000, 1 / Math.sqrt(2), true);
		ArrayList<int[]> moves = mc.getPossibleMoves(this.quartoBoard, pieceID);
		
		HashMap<String, int[]> stats = new HashMap<String, int[]>();
		
		long startTime = System.currentTimeMillis();
		long endTime = startTime + (this.timeLimitForResponse-1000);
		while (System.currentTimeMillis() < endTime) {
			for(int[] move : moves) {
				QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
				copyBoard.insertPieceOnBoard(move[0], move[1], pieceID);
				int piece = mc.randomPieceSelection(copyBoard);
				int result = mc.playGame(copyBoard, piece, false);
				String action = move[0] + "," + move[1];
				
				int [] updatedStatus = stats.getOrDefault(action, new int[]{0,0});
				updatedStatus[0] += result;
				updatedStatus[1] += 1;
						
				stats.put(action, updatedStatus);
			}
		}
		
		double max = 0;
		String bestAction = "";
		
		for (String action : stats.keySet()) {
			int[] status = stats.get(action);
			double value = (double) status[0] / status[1];
			if (value > max) {
				max = value;
				bestAction = action;
			}
		}
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

