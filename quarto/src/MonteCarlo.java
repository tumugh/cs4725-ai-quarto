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
		this.symmetry = symmetry;
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
		// create root node v0 with state s0
		Node root;
		if (piece == null) {
			root = new SelectPieceNode(board);
		} else {
			if (symmetry) {
				root = new SelectMoveNode(board, piece);
			} else {
				root = new SelectMoveNode(board);
			}
			((SelectMoveNode) root).setAction(piece);
		}

		long startTime = System.currentTimeMillis();
		long endTime = startTime + (this.timeLimit);
		while (System.currentTimeMillis() < endTime) {
			// v1 <= TreePolicy(v0)
			Node child = treePolicy(root);
			
			// delta <= DefaultPolicy(s(v1))
			int score;
			if (child instanceof SelectMoveNode) {
				piece = parsePiece(child.getAction());
				score = defaultPolicy(board, piece, child.player);
			} else if (child instanceof SelectPieceNode){
				int[] move = parseMove(child.getAction());
				QuartoBoard copyBoard = new QuartoBoard(board);
				copyBoard.insertPieceOnBoard(move[0], move[1], piece);
				score = defaultPolicy(copyBoard, null, child.player);
			} else {
				score = ((TerminatingNode) child).getValue();
			}
			
			//Backup(v1, delta)
			backup(child, score);
		}

		// return action(BestChild(v0,0))
		 printTree("Root", root);
		
		return bestChild(root, 0).getAction();
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
	 */
	private Node treePolicy(Node node) {
		if (node instanceof TerminatingNode)
			return node;
		if (node.getRemainingMoves().size() != 0)
			return expand(node);
		return bestChild(node, this.cp);
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
		for (int i = 0; i < children.size() ; i++) {
			printTree("\t"+children.get(i).getAction(), children.get(i));
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
	private int defaultPolicy(QuartoBoard board, Integer piece, Boolean player1) {
		QuartoBoard copyBoard = new QuartoBoard(board);
		int score;
		if (piece == null) {
			piece = randomPieceSelection(copyBoard);
			score = playGame(copyBoard, piece, false);
		} else {
			score = playGame(copyBoard, piece, true);
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

	private int playGame(QuartoBoard board, int startingPiece, Boolean player1) {

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

	public static ArrayList<Integer> getPossiblePieces(QuartoBoard board) {
		ArrayList<Integer> pieces = new ArrayList<Integer>();
		for (int i = 0; i < board.getNumberOfPieces(); i++) {
			if (!board.isPieceOnBoard(i)) {
				pieces.add(i);
			}
		}

		return pieces;
	}
	
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
	
	public static ArrayList<int[]> getPossibleMoves(QuartoBoard board, Integer piece) {
		ArrayList<int[]> movesList = new ArrayList<int[]>();
		ArrayList<QuartoBoard> boardSet = new ArrayList<QuartoBoard>();
		
		for (int row = 0; row < board.getNumberOfRows(); row++) {
			for (int col = 0; col < board.getNumberOfColumns(); col++) {
				if (!board.isSpaceTaken(row, col)) {
					QuartoBoard copyBoard = new QuartoBoard(board);
					copyBoard.insertPieceOnBoard(row, col, piece);
					
					// If the board generated by making that move exists in the board set, then don't add the move to the move list
					boolean contains = false;
					for (QuartoBoard boardSetBoard : boardSet) {
						if (areEqualBoards(boardSetBoard, copyBoard)) {
							contains = true;
							break;
						}
					}
					
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
	
	public static boolean areEqualPieces(QuartoPiece p1, QuartoPiece p2) {
		if (p1 == null && p2 == null) {
			return true;
		} else if (p1 == null || p2 == null) {
			return false;
		} else {
			return p1.getPieceID() == p2.getPieceID();
		}
	}
	
	public static ArrayList<QuartoBoard> findSymmetricBoards(QuartoBoard board) {
		ArrayList<QuartoBoard> symBoards = new ArrayList<QuartoBoard>();
		symBoards.add(board);
		symBoards.addAll(getRotatedBoards(board));
		symBoards.addAll(getMirroredBoards(board));
		
		return symBoards;
	}
	
	public static QuartoBoard rotateBoard(QuartoBoard board) {
		QuartoBoard copyBoard = new QuartoBoard(board);
		
		for(int i = 0; i < board.getNumberOfRows(); i++) {
			for(int j = board.getNumberOfColumns() - 1; j >= 0; j--) {
				copyBoard.board[i][board.getNumberOfColumns() - j - 1] = board.board[j][i];
			}
		}
		
		return copyBoard;
	}
	
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
	
	public static QuartoBoard transposeBoard(QuartoBoard board) {
		QuartoBoard copyBoard = new QuartoBoard(board);
		
		for(int i = 0; i < board.getNumberOfRows(); i++) {
			for(int j = 0; j < board.getNumberOfColumns(); j++) {
				copyBoard.board[i][j] = board.board[j][i];
			}
		}
		
		return copyBoard;
	}
	
	public static ArrayList<QuartoBoard> getMirroredBoards(QuartoBoard board) {
		ArrayList<QuartoBoard> mirroredBoards = new ArrayList<QuartoBoard>();
	
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
		
		QuartoBoard transpose = transposeBoard(board);
		
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
		 QuartoBoard board = new QuartoBoard(5,5,32, null);
		 board.board[0][0] = new QuartoPiece(1);
		 board.board[0][1] = new QuartoPiece(2);
		 board.board[0][2] = new QuartoPiece(25);
		 board.board[0][3] = new QuartoPiece(4);
		 board.board[0][4] = new QuartoPiece(5);
		 board.board[1][0] = new QuartoPiece(6);
		 board.board[1][1] = new QuartoPiece(7);
		 board.board[1][2] = new QuartoPiece(8);
		 board.board[1][3] = new QuartoPiece(9);
		 board.board[1][4] = new QuartoPiece(10);
		 board.board[2][0] = new QuartoPiece(11);
		 board.board[2][1] = new QuartoPiece(12);
		 board.board[2][2] = new QuartoPiece(13);
		 board.board[2][3] = new QuartoPiece(14);
		 board.board[2][4] = new QuartoPiece(15);

//		 board.board[0][4] = new QuartoPiece(4);
//		 board.board[0][2] = new QuartoPiece(2);
//		 board.board[0][3] = new QuartoPiece(3);
//		 
//		 board.printBoardState();
//		 ArrayList<QuartoBoard> set = getMirroredBoards(board);
//		 for (QuartoBoard symboard : set) {
//			 symboard.printBoardState();
//		 }
		 
		 ArrayList<int[]> moves = getPossibleMoves(board, 3);
		 
		 for (int[] move : moves) {
		 	String action = move[0] + "," + move[1];
	 		System.out.println(action);
		 }
		 
//		 MonteCarlo mc = new MonteCarlo(9000, 1 / Math.sqrt(2));
//		
//		 String bestAction = mc.UCTSearch(board, null);
//		
//		 System.out.println(bestAction); 
	}
}
