import java.util.*;

//Julie Anne Moore
//Colin Barber

class Node {

	protected ArrayList<Node> children;

	private QuartoBoard board;
	private String action;
	protected ArrayList<String> remainingMoves;
	
	private int n;
	private int q;

	protected Node parent;
	
	// True if node belongs to our agent, false otherwise
	protected boolean player;
	
	public Node(QuartoBoard board) {
		this.children = new ArrayList<Node>();
		this.remainingMoves = new ArrayList<String>();
		this.board = board;
		this.player = true;
	}
	
	public QuartoBoard getBoard() {
		return this.board;
	}
	
	public int getN() {
		return this.n;
	}
	
	public void setN(int n) {
		this.n = n;
	}
	
	public int getQ() {
		return q;
	}
	
	public void setQ(int q) {
		this.q = q;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	public void addChild(Node child, String action) {
		children.add(child);
		child.action = action;
		child.setParentNode(this);
		child.player = child.parent.player;
		this.getRemainingMoves().remove(action);
	}
	
	public ArrayList<Node> getChildren() {
		return children;
	}
	
	public void setParentNode(Node newParentNode) {
		this.parent = newParentNode;
	}
	
	public Node getParentNode() {
		return this.parent;
	}
	
	public ArrayList<String> getRemainingMoves() {
		return this.remainingMoves;
	}
}

class SelectPieceNode extends Node {
	
	// SelectPieceNode for non-symmetry agent
	public SelectPieceNode(QuartoBoard board) {
		super(board);
		ArrayList<Integer> moves = MonteCarlo.getPossiblePieces(board);
		// From playing against profs hard agent we noticed it's first piece was always 00000
		// Ours seems to be random, so we hardcode this little "hack"
		if (moves.size() == 32) {
			this.remainingMoves.add("00000");
			return;
		}
		for (Integer move: moves) {
			String action = String.format("%5s", Integer.toBinaryString(move)).replace(' ', '0');
			this.remainingMoves.add(action);
		}
	}	
	
	// Flip playing because the child of a SelectPieceNode is next player's SelectMoveNode
	public void addChild(Node child, String action) {
		super.addChild(child, action);
		child.player = !child.parent.player;
	}
}

class SelectMoveNode extends Node {
	
	// SelectMoveNode for non-symmetry agent
	public SelectMoveNode(QuartoBoard board) {
		super(board);
		ArrayList<int[]> movesList = MonteCarlo.getPossibleMoves(board);
		// From playing against profs hard agent we noticed it's first move was always 2,2
		// Ours seems to be random, so we hardcode this little "hack"
		if (movesList.size() == 25) {
			this.remainingMoves.add("2,2");
			return;
		}
		for (int[] move : movesList) {
			String action = move[0] + "," + move[1];
			this.remainingMoves.add(action);
		}
	}
	
	// SelectMoveNode for symmetry agent
	public SelectMoveNode(QuartoBoard board, Integer piece) {
		super(board);
		// From playing against profs hard agent we noticed it's first move was always 2,2
		// Ours seems to be random, so we hardcode this little "hack"
		if (isEmptyBoard(this.getBoard())) {
			this.remainingMoves.add("2,2");
			return;
		} else {
			ArrayList<int[]> movesList = MonteCarlo.getPossibleMoves(board, piece);
			for (int[] move : movesList) {
				String action = move[0] + "," + move[1];
				this.remainingMoves.add(action);
			}
		}
	}
	
	public void setAction(int piece) {
		String action = String.format("%5s", Integer.toBinaryString(piece)).replace(' ', '0');
		super.setAction(action);
	}
	
	public static boolean isEmptyBoard(QuartoBoard board) {
    	for (int row = 0; row < board.getNumberOfRows(); row++) {
			for (int col = 0; col < board.getNumberOfColumns(); col++) {
				if (board.isSpaceTaken(row, col)) {
					return false;
				}
			}
		}
    	
    	return true;
    }
	
}

class TerminatingNode extends Node {
	private int value;
	
	public TerminatingNode(QuartoBoard board, int value) {
		super(board);
		this.value = value;
	}
	
	public void addChild(Node child) {
		System.out.println("\nTerminating Nodes cannot have children");
		System.exit(-1);
	}
	
	public int getValue() {
		return this.value;
	}
}
