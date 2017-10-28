import java.util.*;
/*
 * The node class contains:
	- A Node's reference to its children (if any),
	- A string to refer to the name of the node
	- A reference to the parent node
*/
class Node {

	protected List<Node> children;

	// Move that got us to this node
	private String action;
	
	private QuartoBoard board;
	
	private int depth;
	
	private int piece;

	//references the parent node
	protected Node parent;
	
	//constructor method
	public Node(String action) {
		this.children = new ArrayList<Node>();
		this.action = action;
	}
	
	//constructor method
	public Node(QuartoBoard board, int piece) {
		this.children = new ArrayList<Node>();
		this.board = board;
		this.depth = 0;
//		this.piece = Integer.parseInt(piece, 2);
		this.piece = piece;
	}
	
	//add a new child to the children list.  This also sets the child node's parent node
	public void addChild(Node child) {
		children.add(child);
		child.setParentNode(this);
		child.depth = this.depth + 1;
		child.piece = this.piece;
		child.board = new QuartoBoard(this.board);
		
		String[] moveString = child.action.split(",");
		int row = Integer.parseInt(moveString[0]);
		int col = Integer.parseInt(moveString[1]);
		
		child.board.insertPieceOnBoard(row, col, child.piece);
		
	}
	
	//returns the list of children for this node
	public List<Node> getChildren() {
		return children;
	}
	
	//changes the node's parent node
	public void setParentNode(Node newParentNode) {
		this.parent = newParentNode;
	}
	
	//gets the node's parent node
	public Node getParentNode() {
		return this.parent;
	}
	
	public String getAction() {
		return this.action;
	}
	
}

/*
 * The MinNode class is a subclass of the node class 
 * that requires no modification of functionality
*/
class MinNode extends Node {
	//constructor method
	public MinNode(String nodeName) {
		super(nodeName);
	}

}

/*
 * The MaxNode class is a subclass of the node class 
 * that requires no modification of functionality.
*/
class MaxNode extends Node {
	//constructor method
	public MaxNode(String nodeName) {
		super(nodeName);
	}
	
	public MaxNode(QuartoBoard board, int piece) {
		super(board, piece);
	}
}

/*
 * The TerminatingNode class is a subclass of the node class.
 * A TerminatingNode has a value assigned to it.
*/
class TerminatingNode extends Node {
	private int value;
	
	//constructor method
	public TerminatingNode(String nodeName, int value) {
		super(nodeName);
		this.value = value;
	
	}
	
	//terminating nodes should not have children
	public void addChild(Node child) {
		System.out.println("\nTerminating Nodes cannot have children");
		System.exit(-1);
	}
	
	//return the value
	public int getValue() {
		return this.value;
	}
}
