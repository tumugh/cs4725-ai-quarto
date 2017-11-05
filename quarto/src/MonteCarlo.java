import java.util.ArrayList;
import java.util.HashMap;

public class MonteCarlo {

	private QuartoBoard board;
	public HashMap<String, Double> wins;
	public HashMap<String, Integer> simulations;
	
	public MonteCarlo(QuartoBoard board) {
		this.board = board;
		this.wins = new HashMap<String, Double>();
		this.simulations = new HashMap<String, Integer>();
		//chooseRandomPieceNotPlayed
	}
	
	private int playGame(int startingPiece) {
		
		QuartoBoard copyBoard = new QuartoBoard(this.board);
		
		int piece = startingPiece;
		
		Boolean player1 = true;
		
		while (true) {
		
			//int[] move = randomMove(piece, copyBoard);
			int[] move = semiRandomMove(piece, copyBoard);
			
			copyBoard.insertPieceOnBoard(move[0], move[1], piece);
			
			if (isWin(copyBoard, move[0], move[1])) {
				if (player1) return 1;
				return 0;
			}
			
			if (copyBoard.checkIfBoardIsFull()) return 0;
			
			
			//piece = randomPieceSelection(copyBoard);
			piece = semiRandomPieceSelection(copyBoard);
			
			// Switch payers
			player1 = !player1;
		}
	}
	
	private int playGame(int[] startingMove, int startingPiece) {
		
		QuartoBoard copyBoard = new QuartoBoard(board);
		
		int piece = startingPiece;
		int[] move = startingMove;
		
		Boolean player1 = false;
		
		while (true) {
		
			//int[] move = randomMove(piece, copyBoard);
			if (move == null) move = semiRandomMove(piece, copyBoard);
			
			copyBoard.insertPieceOnBoard(move[0], move[1], piece);
			
			if (isWin(copyBoard, move[0], move[1])) {
				if (player1) return 1;
				return 0;
			}
			
			if (copyBoard.checkIfBoardIsFull()) return 0;

			//piece = randomPieceSelection(copyBoard);
			piece = semiRandomPieceSelection(copyBoard);
			
			// Switch payers
			player1 = !player1;
			move = null;
		}
	}
	
	public void runSimulation(int timeLimit, int[] pieces) {
		long startTime = System.currentTimeMillis();
		long endTime = startTime+(timeLimit);
		while(System.currentTimeMillis()<endTime){
			for (int idx = 0; idx < pieces.length ; idx++) {	
				this.wins.put(""+pieces[idx], this.wins.getOrDefault(""+pieces[idx], 0.0) + playGame(pieces[idx]));
				this.simulations.put(""+pieces[idx], this.simulations.getOrDefault(""+pieces[idx], 0) + 1);
	    	}
		}  
		
//    	for (int idx = 0; idx < pieces.length ; idx++) {
//    		double totalWins = 0.0;	
//    		
//			for (int i = 0; i < numOfSimulations; i++) {
//				totalWins += playGame(pieces[idx]);
//			}
//			double prob = totalWins / numOfSimulations;
//			System.out.println(pieces[idx] + " : " + prob);
//			this.wins.put(""+pieces[idx], totalWins);
//			this.simulations.put(""+pieces[idx], numOfSimulations);
//    	}
    }
	
	public void runSimulation(int timeLimit, int[][] moves, int piece) {
		long startTime = System.currentTimeMillis();
		long endTime = startTime+(timeLimit);
		while(System.currentTimeMillis()<endTime){
			for (int idx = 0; idx < moves.length ; idx++) {	
				String move = moves[idx][0] + ":" + moves[idx][1];
				this.wins.put(move, this.wins.getOrDefault(move, 0.0) + playGame(moves[idx], piece));
				this.simulations.put(move, this.simulations.getOrDefault(move, 0) + 1);
	    	}
		}
		
//    	for (int idx = 0; idx < moves.length ; idx++) {
//    		double totalWins = 0.0;	
//    		
//			for (int i = 0; i < numOfSimulations; i++) {
//				totalWins += playGame(moves[idx], piece);
//			}
//			double prob = totalWins / numOfSimulations;
//			System.out.println(moves[idx][0] + ":" + moves[idx][1] + " - " + prob);
//			this.stats.put(moves[idx][0] + ":" + moves[idx][1], prob);	
//    	}
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
	
    protected int semiRandomPieceSelection(QuartoBoard board) {
        boolean skip = false;
        for (int i = 0; i < board.getNumberOfPieces(); i++) {
            skip = false;
            if (!board.isPieceOnBoard(i)) {
                for (int row = 0; row < board.getNumberOfRows(); row++) {
                    for (int col = 0; col < board.getNumberOfColumns(); col++) {
                        if (!board.isSpaceTaken(row, col)) {
                            QuartoBoard copyBoard = new QuartoBoard(board);
                            copyBoard.insertPieceOnBoard(row, col, i);
                            if (copyBoard.checkRow(row) || copyBoard.checkColumn(col) || copyBoard.checkDiagonals()) {
                                skip = true;
                                break;
                            }
                        }
                    }
                    if (skip) {
                        break;
                    }

                }
                if (!skip) {
                    return i;
                }
            }
        }
        QuartoBoard copyBoard = new QuartoBoard(board);
        return copyBoard.chooseRandomPieceNotPlayed(100);
    }
    
    protected int[] randomMove(int pieceID, QuartoBoard board) {
        int[] move = new int[2];
        QuartoBoard copyBoard = new QuartoBoard(board);
        move = copyBoard.chooseRandomPositionNotPlayed(100);

        return move;
    }
	
    protected int[] semiRandomMove(int pieceID, QuartoBoard board) {
        int[] move = new int[2];
        for (int row = 0; row < board.getNumberOfRows(); row++) {
            for (int col = 0; col < board.getNumberOfColumns(); col++) {
                if (!board.isSpaceTaken(row, col)) {
                    QuartoBoard copyBoard = new QuartoBoard(board);
                    copyBoard.insertPieceOnBoard(row, col, pieceID);
                    if (copyBoard.checkRow(row) || copyBoard.checkColumn(col) || copyBoard.checkDiagonals()) {
                    	move[0] = row;
                    	move[1] = col;
                    	return move;
                    }
                }
            }
        }

        // If no winning move is found in the above code, then return a random (unoccupied) square
        QuartoBoard copyBoard = new QuartoBoard(board);
        return copyBoard.chooseRandomPositionNotPlayed(100);
    }
    
    public int[][] getPossibleMoves() {
    	ArrayList<int[]> movesList = new ArrayList<int[]>();
    	
    	for (int row = 0; row < this.board.getNumberOfRows(); row++) {
             for (int col = 0; col < this.board.getNumberOfColumns(); col++) {
            	 if(!this.board.isSpaceTaken(row, col)) {
            		 int[] moves = {row, col};
            		 movesList.add(moves);
            	 }
             }
    	}
    	Object[] movesObjects = movesList.toArray();
    	
    	int[][] moves = new int[movesObjects.length][2];
    	
    	for  (int i = 1; i < movesObjects.length; i++) {
    		moves[i][0] = ((int[])movesObjects[i])[0];
    		moves[i][1] = ((int[])movesObjects[i])[1];
    	}
    	
    	return moves;
    }

    public int[] getPossiblePieces() {
    	ArrayList<Integer> pieceList = new ArrayList<Integer>();
    	for (int i = 0; i < this.board.getNumberOfPieces(); i++) {
    		 if (!this.board.isPieceOnBoard(i)) {
    			 pieceList.add(i);
    		 }
    	}
    	Object[] piecesObjects = pieceList.toArray();
    	
    	int[] pieces = new int[piecesObjects.length];
    	
    	for  (int i = 1; i < piecesObjects.length; i++) {
    		pieces[i] = (int)piecesObjects[i];
    	}
    	
    	return pieces;
    }
    
	public static void main(String[] args) {
		
		QuartoBoard board = new QuartoBoard(5,5,32, null);
		
		MonteCarlo mc = new MonteCarlo(board);
		
	    int[] pieces = mc.getPossiblePieces();
	    
		mc.runSimulation(800, pieces);	
		
    	int max = 0;
    	for(int i = 1; i < mc.simulations.size(); i++) {
    		double maxProb = mc.wins.get(""+max) / mc.simulations.get(""+max);
    		double prob = mc.wins.get(""+i) / mc.simulations.get(""+i);
    		if (maxProb < prob) max = i;
    	}
    	double maxProbability = mc.wins.get(""+max) / mc.simulations.get(""+max);
    	System.out.println(max + " with prob " + maxProbability + " with # of simulations: " + mc.simulations.get(""+max));
    	
    	int[][] moves = mc.getPossibleMoves();
    	
    	mc.runSimulation(800, moves, 0);	
		
    	max = 0;
    	for (int i = 1; i < moves.length; i++) {
    		String maxMove = moves[max][0] + ":" + moves[max][1];
    		String move = moves[i][0] + ":" + moves[i][1];
    		double maxProb = mc.wins.get(maxMove) / mc.simulations.get(maxMove);
    		double prob = mc.wins.get(move) / mc.simulations.get(move);
    		if (maxProb < prob) max = i;
    	}

    	String maxMove = moves[max][0] + ":" + moves[max][1];
    	maxProbability = mc.wins.get(maxMove) / mc.simulations.get(maxMove);
    	System.out.println(maxMove + " with prob " + maxProbability + " with # of simulations: " + mc.simulations.get(maxMove));
    	
	}
}
