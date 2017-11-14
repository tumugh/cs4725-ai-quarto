import java.util.*;

class Node {

	protected ArrayList<Node> children;

	private QuartoBoard board;
	private String action;
	protected ArrayList<String> remainingMoves;
	
	private int n;
	private int q;

	protected Node parent;
	
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
		return n;
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
	public SelectPieceNode(QuartoBoard board) {
		super(board);
		int[] moves = MonteCarlo.getPossiblePieces(board);
		for (int i = 0 ; i < moves.length ; i++) {
			String move = String.format("%5s", Integer.toBinaryString(moves[i])).replace(' ', '0');
			this.remainingMoves.add(move);
		}
	}	
	
	public void addChild(Node child, String action) {
		super.addChild(child, action);
		child.player = !child.parent.player;
	}
}

class SelectMoveNode extends Node {
	
	public SelectMoveNode(QuartoBoard board) {
		super(board);
		int[][] moves = MonteCarlo.getPossibleMoves(board); //new int[][]{{0,0}, {0,2}, {0,1}, {0,3}, {1,1}, {1,2}, {2,2}};
		for (int i = 0 ; i < moves.length ; i++) {
			String move = moves[i][0] + "," + moves[i][1];
			this.remainingMoves.add(move);
		}
	}
	
	public SelectMoveNode(QuartoBoard board, Integer piece) {
		super(board);
		int[][] moves = MonteCarlo.getPossibleMoves(board, piece); //new int[][]{{0,0}, {0,2}, {0,1}, {0,3}, {1,1}, {1,2}, {2,2}};
		for (int i = 0 ; i < moves.length ; i++) {
			String move = moves[i][0] + "," + moves[i][1];
			this.remainingMoves.add(move);
		}
	}
	
	public void setAction(int piece) {
		String action = String.format("%5s", Integer.toBinaryString(piece)).replace(' ', '0');
		super.setAction(action);
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
	
	//return the value
	public int getValue() {
		return this.value;
	}
}