
public class MonteCarlo {

	private QuartoBoard board;
	private int[] stats;
	
	public MonteCarlo(QuartoBoard board) {
		this.board = board;
		this.stats = new int [32];
		//chooseRandomPieceNotPlayed
	}
	
	public static void main(String[] args) {
		
		QuartoBoard board = new QuartoBoard(5,5,32, null);
		
		MonteCarlo mc = new MonteCarlo(board);
				
		mc.runSimulation(1000);	
	}

	private int playGame(int startingPiece) {
		
		QuartoBoard copyBoard = new QuartoBoard(this.board);
		
		int piece = startingPiece;
		
		Boolean player1 = true;
		
		while (true) {
		
			int[] move = randomMove(piece, copyBoard);
			copyBoard.insertPieceOnBoard(move[0], move[1], piece);
			
			if (isWin(copyBoard, move[0], move[1])) {
				if (player1) return 1;
				return 0;
			}
			
			if (copyBoard.checkIfBoardIsFull()) return 0;
			
			
			piece = randomPieceSelection(copyBoard);
			
			// Switch payers
			player1 = !player1;
		}
		
		
	}
	
	protected Boolean isWin(QuartoBoard board, int row, int col) {
		if (board.checkRow(row) || board.checkColumn(col) || board.checkDiagonals()) {
        	return true;
        }
		return false;
	}
	
    protected int randomPieceSelection(QuartoBoard board) {
        QuartoBoard copyBoard = new QuartoBoard(board);
        return copyBoard.chooseRandomPieceNotPlayed(100);
    }
	
    protected int[] randomMove(int pieceID, QuartoBoard board) {

        int[] move = new int[2];
        QuartoBoard copyBoard = new QuartoBoard(board);
        move = copyBoard.chooseRandomPositionNotPlayed(100);

        return move;
    }

	public void runSimulation(int numOfSimulations) {
    	for (int piece = 0; piece < 32 ; piece++) {
    		double totalWins = 0.0;	
    		
			for (int i = 0; i < numOfSimulations; i++) {
				totalWins += playGame(0);
			}
			
			System.out.println(piece + ": " + totalWins / numOfSimulations);	
    	}
    }
}
