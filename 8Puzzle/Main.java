import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
	//The program's entry point
	public static void main(String[] args) {
		int[][] initialState = new int[3][];
		/*Load state from file state.txt
		Expected format:
			439
			812
			607
		Three rows of three numbers, no other characters
		 */
		try {
			//Loads the state.txt file
			File file = new File("state.txt");
			//Scanner for reading it line by line
			Scanner lineScanner = new Scanner(file);
			//Loop three times
			for (int row = 0; row < 3; row++) {
				//If we another line isn't detected, then this file doesn't have at least three lines, throw an error
				if(!lineScanner.hasNextLine())
					throw new RuntimeException("Text file must only contain a 3x3 grid of numbers");
				//Load the next line
				char[] lineChar = lineScanner.nextLine().toCharArray();
				//Create an int array of size 3
				int[] lineInt = new int[3];
				//Loop three times
				for (int col = 0; col < 3; col++)
					//Convert each char to an int and copy it to the int array
					lineInt[col] = Character.getNumericValue(lineChar[col]);
				//Store the int array in the initialState variable
				initialState[row] = lineInt;
			}
		//Throw error if no file is found
		} catch (FileNotFoundException e) {
			System.out.println("File exception occurred");
			e.printStackTrace();
			return;
		}
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

	//Goal state
	public static int[][] goalState = {
			{1, 2, 3},
			{8, 0, 4},
			{7, 6, 5}
	};

	/*This is the a-star algorithm which I implemented from the prescribed text
    Artificial Intelligence: A Modern Approach (3rd ed.)
	I started by implementing the Uniform-Cost Algorithm (page 84) then added the a-star heuristic after

	The input is a Node which represents the initial state
	The output is a Node which represents the goal state, related through the use of a parent variable
	*/
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
			//If the current node state matches the goal state, we have achieved our goal, return
			if (Arrays.deepEquals(currentNode.state, goalState))
				return currentNode;
			//As we have just visited this node, add it to our explored set
			explored.add(currentNode);
			//Loop through every possible move we can make from here
			for (Node child : currentNode.getPossibleStates()) {
				//If a node with the same state isn't in the explored set then that means this is a completely new state
				if (getDuplicateNode(child, explored) == null) {
					//Add it to the frontier and then go to the next loop iteration
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
	enum Action {
		LEFT, RIGHT, UP, DOWN, START
	}
	//The path cost (the distance from the initial state)
	public int pathCost;
	//The f-cost (f = g() + h()
	public int fCost;
	//A 2d array of integers representing the puzzle state
	int[][] state;
	//This node's parent
	Node parent;
	Action action;


	//Returns a clone of the current Node object
	Node copy() {
		Node copy = new Node();
		copy.pathCost = pathCost;
		copy.fCost = fCost;
		//Deep copy state
		copy.state = new int[state.length][];
		for (int i = 0; i < state.length; i++)
			copy.state[i] = Arrays.copyOf(state[i], state[i].length);
		copy.parent = this;
		return copy;
	}

	public Node() {

	}

	//Constructor
	public Node(int[][] state) {
		this.state = state;
		this.parent = null;
		this.pathCost = 0;
		this.fCost = 0;
		this.action = Action.START;
	}

	//The output of this function is a Point containing the number's position in the grid
	//A Point contains an X and a Y variable
	//col is the x-axis of the blank, row is y-axis.
	Point findNumber(int i, int[][] state) {
		int size = state.length;
		//Two for loops iterating through state to find the number
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++)
				if (state[y][x] == i)
					return new Point(x, y);
		//Throw an error if the number isn't detected
		throw new RuntimeException("No n in state");
	}

	//The output of this function is the manhattan distance between the current state and the goal state for each number 1-8
	int calcH() {
		//Set the initial value of h to 0
		int h = 0;
		//Loop through each number 1-8 in state
		for (int i = 1; i <= 8; i++) {
			//Retrieve coordinates for the current number in the current state
			Point p1 = findNumber(i, state);
			//Retrieve coordinates for the current number in the goal state
			Point p2 = findNumber(i, Main.goalState);
			//Increment h by the difference between the x coordinates of points 1 and 2 and the y coordinates of points 1 and 2
			h += Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
		}
		return h;
	}

	//Print the node to console
	void print() {
		int size = state.length;
		System.out.println("-------------");
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				System.out.print("| " + state[y][x] + " ");
			}
			System.out.println("|");
			System.out.println("-------------");
		}
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
		//Get the position of the blank (zero)
		Point blankPos = findNumber(0, state);
		//A hashmap which pairs each action enum with a pair of integer offsets used to calculate the action result
 		HashMap<int[], Action> actionMap = new HashMap<>();
		//Add actions to the hashmap
		actionMap.put(new int[]{0, -1}, Action.LEFT);
		actionMap.put(new int[]{0, 1}, Action.RIGHT);
		actionMap.put(new int[]{-1, 0}, Action.UP);
		actionMap.put(new int[]{1, 0}, Action.DOWN);
		//Set constants used to make array access more readable
		final int X = 1, Y = 0;
		//Try every move available in the hashmap
		for (int[] move : actionMap.keySet()) {
			//Clone the current node
			Node node = copy();
			//Skip if the move is out of bounds
			if (blankPos.x + move[X] > 2 || blankPos.x + move[X] < 0 || blankPos.y + move[Y] > 2 || blankPos.y + move[Y] < 0)
				continue;
			//Depending on the current action selected, use the corresponding offsets to get the position that the blank will move in.
			//Get the number in that position and replace the blank with it.
			node.state[blankPos.y][blankPos.x] = node.state[blankPos.y + move[Y]][blankPos.x + move[X]];
			//Replace the number we just retrieved with a zero (blank) so that they would have swapped
			node.state[blankPos.y + move[Y]][blankPos.x + move[X]] = 0;
			//Increment the path cost by one
			node.pathCost += 1;
			//Calculate and set the node's fCost
			node.fCost = node.calcH() + node.pathCost;
			//Set the node's action to the action we just performed
			node.action = actionMap.get(move);
			//Add the node to the result list
			states.add(node);
		}
		return states;
	}
}