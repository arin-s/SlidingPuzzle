import java.util.*;

public class Main {
	public static void main(String[] args) {
		//Create the initial node
		Node testNode = new Node(initialState);
		//Run the a-star algorithm and store the final node
		Node solved = solve(testNode);
		//Retrieve the optimal path and store it in a list
		LinkedList<Node> path = solved.completePath();
		//Print the optimal path
		int i = -1;
		for (Node n : path) {
			i++;
			n.print();
		}
		System.out.println("Total turns: " + i);
	}

	//The initial state of the puzzle
	static String[] initialState = {"B", "B", "B", " ", "W", "W", "W"};

	static Node solve(Node state) {
		//Create our frontier priority queue which sorts based on each Node's fCost
		PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(n -> n.fCost));
		//Add the initial node to the priority queue
		frontier.add(state);
		//Create the explored set
		Set<Node> explored = new HashSet<>();
		Node currentNode;
		while (true) {
			//The frontier should never be empty, throw an error if it is
			if (frontier.isEmpty())
				throw new RuntimeException("Empty frontier");
			//Pop the node with the lowest fCost from the priority queue
			currentNode = frontier.remove();
			//If the heuristic function returns 0, then we have achieved our goal
			if (currentNode.calcH() == 0)
				return currentNode;
			//As we have just visited this node, add it to our explored set
			explored.add(currentNode);
			//Loop through every possible move we can make from here
			for (Node child : currentNode.getPossibleStates()) {
				//If a node with the same state isn't in the explored set then that means this is a completely new state
				if (getDuplicateNode(child, explored) == null) {
					frontier.add(child);
					continue;
				}
				//Check if a node with the same state is already in the frontier
				Node frontierDuplicate = getDuplicateNode(child, frontier);
				//If it isn't, go to the next loop iteration
				if (frontierDuplicate == null)
					continue;
				//If the current node costs less than the duplicate node, replace it
				if (currentNode.pathCost < frontierDuplicate.pathCost) {
					frontier.remove(getDuplicateNode(child, frontier));
					frontier.add(child);
				}
			}
		}
	}

	//The output of this function is a Node from the frontier set which has the same state as the "child" node;
	//The inputs are the node which needs to be checked against and the collection that needs to be searched
	//Utility function which checks to see if a node with the same state already exists in a given collection
	static Node getDuplicateNode(Node child, Collection<Node> collection) {
		//Loop through each node in the given collection
		for (Node collectionNode : collection) {
			//Check if the child node's state is the same as the collection node's state
			if (Arrays.deepEquals(collectionNode.state, child.state))
				return collectionNode;
		}
		//Return null if no duplicate is found
		return null;
	}
}

//Represents each node on the graph
//Contains state, parent, f-cost and various utility functions
class Node {
	//Enum of each action, names should be self-explanatory
	enum Action {
		HOP2LEFT, HOPLEFT, LEFT, RIGHT, HOPRIGHT, HOP2RIGHT, START
	}
	//The path cost (the distance from the initial state)
	public int pathCost;
	//The f-cost (f = g() + h()
	public int fCost;
	//A 2d array of integers representing the puzzle state
	String[] state;
	//This node's parent
	Node parent;
	Action action;


	//Returns a clone of the current Node object
	Node copy() {
		Node copy = new Node();
		copy.pathCost = pathCost;
		copy.fCost = fCost;
		//Deep copy state
		copy.state = Arrays.copyOf(state, state.length);
		copy.parent = this;
		return copy;
	}

	public Node() {

	}

	public Node(String[] state) {
		this.state = state;
		this.parent = null;
		this.pathCost = 0;
		this.fCost = 0;
		this.action = Action.START;
	}

	//The heuristic function
	//Calculates number of white tiles to the right of each black tile
	int calcH() {
		int h = 0;
		//Loop through each black tile in state
		for (int i = 0; i < state.length; i++)
			//If on a black tile, loop all proceeding tiles
			if (state[i].equals("B"))
				//Looping all proceeding tiles, checking for white
				for (int j = i; j < state.length; j++)
					if (state[j].equals("W"))
						h += 1;
		return h;
	}

	//Print the node to console
	void print() {
		System.out.println("-------------");
		for (String s : state) {
			System.out.print("| " + s + " ");
		}
		System.out.println("|");
		System.out.print("Distance from s0: " + pathCost);
		System.out.println("  Action: " + action);
	}

	//The output of this function is a path of nodes starting from the initial state to the current state
	LinkedList<Node> completePath() {
		//List to store the nodes
		LinkedList<Node> states = new LinkedList<>();
		//Set the iterator to the current Node, then successively loop through each node's parent until the initial node is reached
		for (Node iterator = this; iterator != null; iterator = iterator.parent)
			//Add this node to the start of the list
			states.addFirst(iterator);
		return states;
	}

	//The output of this function is the result of every action possible from this node
	public LinkedList<Node> getPossibleStates() {
		//List to store the possible states
		LinkedList<Node> states = new LinkedList<>();
		//A hashmap which pairs each action enum with a pair of integer offsets used to calculate the action result
		HashMap<Integer, Action> actionMap = new HashMap<>();
		//Add actions to the hashmap
		actionMap.put(-3, Action.HOP2LEFT);
		actionMap.put(-2, Action.HOPLEFT);
		actionMap.put(-1, Action.LEFT);
		actionMap.put(3, Action.HOP2RIGHT);
		actionMap.put(2, Action.HOPRIGHT);
		actionMap.put(1, Action.RIGHT);

		//Try every move on every tile
		for (int move : actionMap.keySet()) {
			for (int i = 0; i < state.length; i++)
				//If on a black tile, loop all proceeding tiles
				if (state[i].equals("B") || state[i].equals("W")) {
					//out of bounds check
					if (i + move < 0 || i + move >= state.length)
						continue;
					//Cannot move into non-empty space
					if (!state[i + move].equals(" "))
						continue;
					//Clone the current node
					Node node = copy();
					//Swap the blank with the number above it
					node.state[move+i] = node.state[i];
					node.state[i] = " ";
					//Set the path cost to the absolute distance moved
					node.pathCost += Math.abs(move);
					//Calculate and set the node's fCost
					node.fCost = node.calcH() + node.pathCost;
					//Set the node's action to the action we just performed
					node.action = actionMap.get(move);
					//Add the node to the result list
					states.add(node);
				}
		}
		return states;
	}
}