import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/*
 * The UCT algorithm from http://mcts.ai/pubs/mcts-survey-master.pdf
 */
		
public class MonteCarlo {

	private int timeLimit;
	private double cp;
	private boolean symmetry;

	public MonteCarlo(int timeLimit, double cp, boolean symmetry) {
		this.timeLimit = timeLimit;
		this.cp = cp;
		this.symmetry = symmetry; // to test with and without the symmetry implementation
	}

	/*
	 * function UctSearch(s0)
	 * 		create root node v0 with state s0
	 * 		while within computational budget do
	 * 			v1 <= TreePolicy(v0)
	 * 			delta <= DefaultPolicy(s(v1))
	 * 			Backup(v1, delta)
	 * 		return action(BestChild(v0,0))
	 */
	public String UCTSearch(QuartoBoard board, Integer piece) {
		Node root = createRoot(board, piece);

		long startTime = System.currentTimeMillis();
		long endTime = startTime + (this.timeLimit);
		while (System.currentTimeMillis() < endTime) {
			Node child = treePolicy(root);	
			int score = defaultPolicy(child, board, child.player);
			backup(child, score);
		}

		// print the statistics of the actions at the top level
		printTree("Root", root);
		return bestChild(root, 0).getAction();
	}
	
	/*
	 * Create a root node based on the the board state and current piece
	 */
	public Node createRoot(QuartoBoard board, Integer piece) {
		Node root;
		if (piece == null) {
			root = new SelectPieceNode(board);
		} else {
			if (symmetry) {
				// uses symmetry to set the remaining moves (hopefully reduces the tree size)
				root = new SelectMoveNode(board, piece);
			} else {
				root = new SelectMoveNode(board);
			}
			((SelectMoveNode) root).setAction(piece);
		}
		return root;
	}
	
	/*
	 *  function TreePolicy(v)
	 *  	while v is nonterminal do
	 *  		if v not fully expanded then
	 *  			return Expand(v)
	 *  		else
	 *  			v <= BestChild(v, Cp)
	 *  	return v
	 *  
	 *  Cp is the exploration term and the constant can be
	 *  	adjusted to lower or increase the amount of 
	 *  	exploration performed
	 *  
	 *  Cp =  1 / Math.sqrt(2) is mentioned in the paper 
	 *  
	 *  If we had more time it would be interesting to have 
	 *  tried to tune this parameter.
	 *  
	 */
	private Node treePolicy(Node node) {
		while (!(node instanceof TerminatingNode)) {
			if (node.getRemainingMoves().size() != 0){
				return expand(node);
			}
			else {
				node = bestChild(node, this.cp);
			}
		}
		return node;
		
//		if (node instanceof TerminatingNode)
//			return node;
//		if (node.getRemainingMoves().size() != 0)
//			return expand(node);
//		return bestChild(node, this.cp);
		
	}

	/*
	 *  function Expand(v)
	 *  	choose action from untried actions for the node v
	 *  	add a new child v' to v
	 *  		with s(v') = Result(s(v),action)
	 *  		and action(v') = action 
	 *  	return v'
	 */
	private Node expand(Node node) {
		Node child;
		
		String action = node.getRemainingMoves().get(0);
		if (node instanceof SelectPieceNode) {
			if (symmetry) {
				int pieceId = parsePiece(action);
				child = new SelectMoveNode(node.getBoard(), pieceId);				
			} else {
				child = new SelectMoveNode(node.getBoard());
			}
		} else {
			QuartoBoard copyBoard = new QuartoBoard(node.getBoard());
			int piece = parsePiece(node.getAction());
			int[] move = parseMove(action);
			copyBoard.insertPieceOnBoard(move[0], move[1], piece);
			
			if (isWin(copyBoard, move[0], move[1])) {
				if (node.player)
					child = new TerminatingNode(copyBoard, -1);
				child = new TerminatingNode(copyBoard, 1);
			} else if (copyBoard.checkIfBoardIsFull()) {
				child = new TerminatingNode(copyBoard, 0);
			} else {
				child = new SelectPieceNode(copyBoard);
			}
		}
		node.addChild(child, action);
		return child;
	}
	
	/*
	 *  function BestChild(v, delta)
	 *  	return argmax of c in children of v: evaluate(c, delta)
	 */
	private Node bestChild(Node node, double delta) {
		return argmax(node, delta);
	}
		
	private Node argmax(Node node, double delta) {
		ArrayList<Node> children = node.getChildren();
		
		double maxValue = evaluate(children.get(0), node.getN(), delta);
		Node maxNode = children.get(0);
		
		for (int i = 1; i < children.size() ; i++) {
			Node child = children.get(i);
			
			double value = evaluate(child, node.getN(), delta);
			
			if (value > maxValue) {
				maxValue = value;
				maxNode = child;
			}
		}
		return maxNode;
	}
	
	private void printTree(String name, Node node) {
		System.out.println(name+ ": Q=>"+node.getQ()+", N=>"+node.getN());
		ArrayList<Node> children = node.getChildren();

		for (Node child : children) {
			System.out.println("\t"+child.getAction() + ": Q=>"+child.getQ()+", N=>"+child.getN());
		}
		if ("Root" == name) {
			System.out.println("Best Action: " + bestChild(node, 0).getAction());
		}
	}
	
	/*
	 * BestChild equation from paper
	 */
	private double evaluate(Node node, int simulations, double delta) {
		return (double)node.getQ() / node.getN() + delta*Math.sqrt(2*Math.log(simulations) / node.getN());
	}

	/*
	 *  return score from randomly simulated game
	 */
	private int defaultPolicy(Node child, QuartoBoard board, Boolean player1) {
		QuartoBoard copyBoard = new QuartoBoard(board);
		int score;
		int piece;
		
		if (child instanceof SelectMoveNode) {
			piece = parsePiece(child.getAction());
			score = playGame(copyBoard, piece, player1);
		} else if (child instanceof SelectPieceNode){
			int[] move = parseMove(child.getAction());
			piece = parsePiece(child.getParentNode().getAction());
			copyBoard.insertPieceOnBoard(move[0], move[1], piece);
			piece = randomPieceSelection(copyBoard);
			score = playGame(copyBoard, piece, !player1); 
		} else {
			score = ((TerminatingNode) child).getValue();
		}
		return score;
	}

	/*
	 *  function Backup(v, delta)
	 *  	while v is not null do
	 *  		N(v) <= N(v) + 1
	 *  		Q(v) <= Q(v) + delta(v,p)
	 *  	v <= parent of v
	 *  	
	 */
	private void backup(Node node, int score) {
		while (node != null) {
			node.setN(node.getN()+1);
			if (node.player){
				node.setQ(node.getQ()+score);
			} else {
				node.setQ(node.getQ()-score);
			}
			node = node.getParentNode();
		}
	}
	
	private int parsePiece(String piece) {
		return Integer.parseInt(piece, 2);
	}
	
	private int[] parseMove(String moveStr) {
		String[] moveString = moveStr.split(",");
		int[] move = new int[2];
		move[0] = Integer.parseInt(moveString[0]);
		move[1] = Integer.parseInt(moveString[1]);
		return move;
	}

	public int playGame(QuartoBoard board, int startingPiece, Boolean player1) {

		int piece = startingPiece;

		while (true) {

			int[] move = randomMove(piece, board);

			board.insertPieceOnBoard(move[0], move[1], piece);

			if (isWin(board, move[0], move[1])) {
				if (player1)
					return 1;
				return -1;
			}

			if (board.checkIfBoardIsFull())
				return 0;

			piece = randomPieceSelection(board);

			// Switch payers
			player1 = !player1;
		}
	}

	protected Boolean isWin(QuartoBoard board, int row, int col) {
		if (board.checkRow(row) || board.checkColumn(col)
				|| board.checkDiagonals()) {
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
							if (copyBoard.checkRow(row)
									|| copyBoard.checkColumn(col)
									|| copyBoard.checkDiagonals()) {
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
					if (copyBoard.checkRow(row) || copyBoard.checkColumn(col)
							|| copyBoard.checkDiagonals()) {
						move[0] = row;
						move[1] = col;
						return move;
					}
				}
			}
		}

		// If no winning move is found in the above code, then return a random
		// (unoccupied) square
		QuartoBoard copyBoard = new QuartoBoard(board);
		return copyBoard.chooseRandomPositionNotPlayed(100);
	}

	
	/** Create a list of pieces not on the board, and therefore possible to play
	 * @param board the current turn's board
	 * @return the list of possible pieces that can be played
	 */
	public static ArrayList<Integer> getPossiblePieces(QuartoBoard board) {
		ArrayList<Integer> pieces = new ArrayList<Integer>();
		for (int i = 0; i < board.getNumberOfPieces(); i++) {
			if (!board.isPieceOnBoard(i)) {
				pieces.add(i);
			}
		}

		return pieces;
	}
	
	/** This version of getPossibleMoves simply determines where there are free spaces on the board
	 * @param board the current turn's board
	 * @return the list of possible moves
	 */
	public static ArrayList<int[]> getPossibleMoves(QuartoBoard board) {
		ArrayList<int[]> movesList = new ArrayList<int[]>();

		for (int row = 0; row < board.getNumberOfRows(); row++) {
			for (int col = 0; col < board.getNumberOfColumns(); col++) {
				if (!board.isSpaceTaken(row, col)) {
					int[] moves = { row, col };
					movesList.add(moves);
				}
			}
		}
		
		return movesList;
	}
	
	/** This version of getPossibleMoves takes into considering board symmetry upon playing a piece
	 * @param board the current turn's board
	 * @param piece the piece id provided to our agent
	 * @return the list of possible moves
	 */
	public static ArrayList<int[]> getPossibleMoves(QuartoBoard board, Integer piece) {
		// keep track of unique moves with movesList
		ArrayList<int[]> movesList = new ArrayList<int[]>();
		// boardSet is used to keep track of any symmetric boards generated by placing the piece of the board
		// in all available spots
		ArrayList<QuartoBoard> boardSet = new ArrayList<QuartoBoard>();
		
		for (int row = 0; row < board.getNumberOfRows(); row++) {
			for (int col = 0; col < board.getNumberOfColumns(); col++) {
				if (!board.isSpaceTaken(row, col)) {
					QuartoBoard copyBoard = new QuartoBoard(board);
					copyBoard.insertPieceOnBoard(row, col, piece);
					
					// If the board generated by making that move exists in the board set, then there exists
					// a previously made move that, with symmetry, results in that board state
					boolean contains = false;
					for (QuartoBoard boardSetBoard : boardSet) {
						if (areEqualBoards(boardSetBoard, copyBoard)) {
							contains = true;
							break;
						}
					}
					
					// If that move results in a board state that is unique, or not part of the board set,
					// then add that move to the list of possible moves, and add all the symmetric boards generated by making
					// that move into the board set
					if (!contains) {
						int[] moves = {row, col};
						movesList.add(moves);
						ArrayList<QuartoBoard> symmetricBoards = findSymmetricBoards(copyBoard);
						boardSet.addAll(symmetricBoards);
					}
				}
			}
		}

		return movesList;
	}
	
	// Compare quarto boards for equality
	// Used in getPossibleMoves for an agent that takes advantage of board symmetry
	public static boolean areEqualBoards(QuartoBoard b1, QuartoBoard b2) {
		for (int i = 0; i < b1.getNumberOfRows(); i++) {
			for (int j = 0; j < b1.getNumberOfColumns(); j++) {
				if (!areEqualPieces(b1.board[i][j], b2.board[i][j])) {
					return false;
				}
			}
		}
		
		return true;
	}
	

	// Compares quarto pieces for equality
	public static boolean areEqualPieces(QuartoPiece p1, QuartoPiece p2) {
		if (p1 == null && p2 == null) {
			return true;
		} else if (p1 == null || p2 == null) {
			return false;
		} else {
			return p1.getPieceID() == p2.getPieceID();
		}
	}
	
	/** Combines lists of symmetric boards generated by rotation and mirroring a board
	 * @param board the initial board
	 * @return the list of symmetric boards generated by rotation and mirroring
	 */
	public static ArrayList<QuartoBoard> findSymmetricBoards(QuartoBoard board) {
		ArrayList<QuartoBoard> symBoards = new ArrayList<QuartoBoard>();
		symBoards.add(board);
		symBoards.addAll(getRotatedBoards(board));
		symBoards.addAll(getMirroredBoards(board));
		
		return symBoards;
	}
	
	
	/** Rotates a board clockwise
	 * @param board the initial board
	 * @return a clockwise rotated copy of the param board
	 */
	public static QuartoBoard rotateBoard(QuartoBoard board) {
		QuartoBoard copyBoard = new QuartoBoard(board);
		
		for(int i = 0; i < board.getNumberOfRows(); i++) {
			for(int j = board.getNumberOfColumns() - 1; j >= 0; j--) {
				copyBoard.board[i][board.getNumberOfColumns() - j - 1] = board.board[j][i];
			}
		}
		
		return copyBoard;
	}
	
	
	/** Creates a list of symmetric boards generated by rotation
	 * @param board the initial board
	 * @return the list of symmetric boards created by rotating the param board
	 */
	public static ArrayList<QuartoBoard> getRotatedBoards(QuartoBoard board) {
		ArrayList<QuartoBoard> rotatedBoards = new ArrayList<QuartoBoard>();
		QuartoBoard b1 = rotateBoard(board);
		QuartoBoard b2 = rotateBoard(b1);
		QuartoBoard b3 = rotateBoard(b2);
		rotatedBoards.add(b1);
		rotatedBoards.add(b2);
		rotatedBoards.add(b3);
		return rotatedBoards;
	}
	
	/** Transposing a board generates a mirror of the board 
	 * @param board the initial board
	 * @return a transposed copy of the param board
	 */
	public static QuartoBoard transposeBoard(QuartoBoard board) {
		QuartoBoard copyBoard = new QuartoBoard(board);
		
		for(int i = 0; i < board.getNumberOfRows(); i++) {
			for(int j = 0; j < board.getNumberOfColumns(); j++) {
				copyBoard.board[i][j] = board.board[j][i];
			}
		}
		
		return copyBoard;
	}
	
	/** Creates a list of symmetric boards generated by mirroring
	 * @param board the initial board
	 * @return the list of symmetric boards created by mirroring the param board
	 */
	public static ArrayList<QuartoBoard> getMirroredBoards(QuartoBoard board) {
		ArrayList<QuartoBoard> mirroredBoards = new ArrayList<QuartoBoard>();
	
		// Generate mirror boards by mirroring across center "y axis" and "x axis"
		QuartoBoard yMirror = new QuartoBoard(board);
		QuartoBoard xMirror = new QuartoBoard(board);
		for (int i = 0; i < board.getNumberOfRows(); i++) {
		    for (int j = 0; j < board.getNumberOfColumns() / 2; j++) {
		    	yMirror.board[i][j] = yMirror.board[i][board.getNumberOfColumns() - 1 - j];
		    	yMirror.board[i][board.getNumberOfColumns() - 1 - j] = board.board[i][j];;
		        xMirror.board[j][i] = xMirror.board[board.getNumberOfRows() - 1 - j][i];
		        xMirror.board[board.getNumberOfRows() - 1 - j][i] = board.board[j][i];;
		    }
		}
		
		// Transposing the board generates a mirror of the board across the imaginary axis from the
		// top left element ([0][0]) to the bottom right element ([4][4])
		QuartoBoard transpose = transposeBoard(board);
		
		// Generating the mirror of the board across the top right element ([0][4]) to the bottom left element ([4][0])
		// is equivalent to rotating the board twice, then transposing that board
		QuartoBoard diag = rotateBoard(board);
		diag = rotateBoard(diag);
		diag = transposeBoard(diag);
			
		mirroredBoards.add(yMirror);
		mirroredBoards.add(xMirror);
		mirroredBoards.add(transpose);
		mirroredBoards.add(diag);
		
		return mirroredBoards;
	}
	
	public static void main(String[] args) {
//		 QuartoBoard board = new QuartoBoard(5,5,32, "state.quarto");
		 QuartoBoard board = new QuartoBoard(5,5,32, null);
		 board.board[3][4] = new QuartoPiece(0);
		 
		 board.printBoardState();
//		 ArrayList<QuartoBoard> set = getMirroredBoards(board);
//		 for (QuartoBoard symboard : set) {
//			 symboard.printBoardState();
//		 }
		 
//
		 ArrayList<int[]> moves = getPossibleMoves(board, 3);
		 
		 for (int[] move : moves) {
		 	String action = move[0] + "," + move[1];
	 		System.out.println(action);
		 }
		 
//		 MonteCarlo mc = new MonteCarlo(9000, 1 / Math.sqrt(2), false);
//		
//		 String bestAction = mc.UCTSearch(board, null);
//		
//		 System.out.println(bestAction); 
	}
}
