import java.util.*;
import java.lang.Math;

/*
 * The Minimax class contains:
	- The main() method
	- alphaBetaSearch
	- maxValue
	- minValue
	- code to build example game trees
**/

public class Minimax {

	/*
	 * The main method()
	 *
	 * If a parameter is provided (e.g., java Minimax 1234), then a
	 * minimax tree with depth 3 and branching factor 3 is built,
	 * using randomly-generated integers, with the parameter used
	 * as the seed for the random-number generator.
	 *
	 * If no parameter is provided, then the example minimax tree
	 * in the buildAlphaBetaTreeExample method is used.
	 */
	public static void main(String[] args) {

		
	}


	/*
	 * The alphaBetaSearch algorithm is used for computing the value for the alpha-beta tree example
	 * @param node  a node from a minimax game tree
	 * @return the value of the tree.
	 */
	public static int alphaBetaSearch(Node node) {

                // call maxValue on the root of the tree, with
                // alpha and beta set to very small and very large initial values
		int value = maxValue(node, Integer.MIN_VALUE, Integer.MAX_VALUE);

		return value;
	}


	/*
	 * The maxValue algorithm is used for computing the value of a MAX node,
	 * using alpha-beta pruning where appropriate.
	 * @param node  a node from a minimax game tree
	 * @param alpha  the alpha value passed from the node's parent
	 * @param beta   the beta value passed from the node's parent
	 * @return the value of the node.
	 */
	public static int maxValue(Node node, int alpha, int beta) {

		// if the current node is a TerminatingNode
		if (node instanceof TerminatingNode) {
			//return the value assigned to the Terminating node
			return ((TerminatingNode)node).getValue();
		}

		else {
			int value = Integer.MIN_VALUE;

			List<Node> children = node.getChildren();
			for(Iterator<Node> i = children.iterator(); i.hasNext(); ) {
				Node child = i.next();

				// Insert necessary code below, to update the node's value,
				// as well as alpha and beta.
				// Use alpha-beta pruning where appropriate.
				//
				// When a node's remaining children are to be pruned, use the
				// following two lines to print information before returning
				// the value:
				//
				//	System.out.println("** All children of " + node.getName() + " after " + child.getName() + " are pruned.");
				//	System.out.println("Value returned for node " + node.getName() + " is " + value);

				// System.out.println("Value of child node " + child.getName() + " = " + value);
				// System.out.println("Value of beta at child node : " + child.getName() + " = " + beta);
				// System.out.println("Value of alpha at child node : " + child.getName() + " = " + alpha);
				value = Math.max(value, minValue(child, alpha, beta));
				if (value >= beta) {
					System.out.println("** All children of " + node.getAction() + " after " + child.getAction() + " are pruned.");
					System.out.println("Value returned for node " + node.getAction() + " is " + value);
					return value;
				}
				alpha = Math.max(alpha, value);
			}

			System.out.println("Value returned for node " + node.getAction() + " is " + value);
			return value;
		}

	}


	/*
	 * The minValue algorithm is used for computing the value of a MIN node,
	 * using alpha-beta pruning where appropriate.
	 * @param node  a node from a minimax game tree
	 * @param alpha  the alpha value passed from the node's parent
	 * @param beta   the beta value passed from the node's parent
	 * @return the value of the node.
	 */
	public static int minValue(Node node, int alpha, int beta) {

		// if the current node is a TerminatingNode
		if (node instanceof TerminatingNode) {
			//return the value assigned to the Terminating node
			return ((TerminatingNode)node).getValue();
		}

		else {
			int value = Integer.MAX_VALUE;

			List<Node> children = node.getChildren();
			for(Iterator<Node> i = children.iterator(); i.hasNext(); ) {
				Node child = i.next();

				// Insert necessary code below, to update the node's value,
				// as well as alpha and beta.
				// Use alpha-beta pruning where appropriate.
				//
				// When a node's remaining children are to be pruned, use the
				// following two lines to print information before returning
				// the value:
				//
				//	System.out.println("** All children of " + node.getName() + " after " + child.getName() + " are pruned.");
				//	System.out.println("Value returned for node " + node.getName() + " is " + value);

				value = Math.min(value, maxValue(child, alpha, beta));
				if (value <= alpha) {
					System.out.println("** All children of " + node.getAction() + " after " + child.getAction() + " are pruned.");
					System.out.println("Value returned for node " + node.getAction() + " is " + value);
					return value;
				}
				beta = Math.min(beta, value);
			}

			System.out.println("Value returned for node " + node.getAction() + " is " + value);
			return value;
		}

	}

	/*
	 * This function is used for building a game tree
	 * @return root node
	 */
	public static Node buildAlphaBetaTreeExample(QuartoBoard quartoBoard, int piece) {

		//start with root node (max)
		Node root = (new MaxNode(quartoBoard, piece));
		
		if (!quartoBoard.isPieceOnBoard(piece)) {
            for (int row = 0; row < quartoBoard.getNumberOfRows(); row++) {
                for (int col = 0; col < quartoBoard.getNumberOfColumns(); col++) {
                    if (!quartoBoard.isSpaceTaken(row, col)) {
                    	MinNode min = new MinNode(row + " " + col);
                    	root.addChild(min);
                    }
                }
            }
		}

		return root;
	}
}
