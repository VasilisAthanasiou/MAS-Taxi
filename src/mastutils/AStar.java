package mastutils;
import mastutils.Node;

import java.lang.Math.*;
import java.util.ArrayList;
import java.util.Stack;
import java.lang.Double;

public class AStar {

    Node startNode;
    Node goalNode;
    Node[][] graph;

    public AStar(Node start, Node goal, Node[][] graph){
        this.startNode = start;
        this.goalNode = goal;
        this.graph = graph;
    }

    public Stack<String> compute(){
        // Initialize start node
        Node currentNode = startNode;
        currentNode.sLocal = 0;
        currentNode.sGlobal = euclideanDist(startNode, goalNode);

        // Initialize test list and add the start node
        ArrayList<Node> notTestedNodes = new ArrayList<>();
        notTestedNodes.add(startNode);

        // Algorithm implementation
        while (!notTestedNodes.isEmpty()){
            // Sort the not tested nodes list by their goal score
            notTestedNodes = sort(notTestedNodes);

            // Make sure that the list is not empty and that the first node on the list is visited. If it is, remove it
            while(!notTestedNodes.isEmpty() && notTestedNodes.get(0).isVisited){
                notTestedNodes.remove(0);
            }

            // Make sure the list isn't empty
            if(notTestedNodes.isEmpty())
                break;

            // Update the current node
            currentNode = notTestedNodes.get(0);
            currentNode.isVisited = true;

            // Add current node's neighbours on the list
            for (Node nodeNeighbour : currentNode.neighbours) {
                // Add a neighbour to the list
                notTestedNodes.add(nodeNeighbour);

                // Calculate neighbours potential lowest parent distance
                double localGoal = currentNode.sLocal + euclideanDist(currentNode, nodeNeighbour);

                if(localGoal < nodeNeighbour.sLocal){
                    nodeNeighbour.parent = currentNode;
                    nodeNeighbour.sLocal = localGoal;

                    // Change neighbours global score
                    nodeNeighbour.sGlobal = nodeNeighbour.sLocal + euclideanDist(nodeNeighbour, goalNode);
                }
            }
        }
        // Follow path from goal to start, by iterating through parents
        Stack<String> path = new Stack<>();
        // Push the goal node
        path.push(goalNode.location);

        currentNode = goalNode;
        while(!currentNode.location.equals(startNode.location)){
            path.push(currentNode.parent.location);
            currentNode = currentNode.parent;
        }

        return path;
    }

    private double euclideanDist(Node a, Node b){
        double x1 = Double.valueOf(a.location.indexOf(0));
        double y1 = Double.valueOf(a.location.indexOf(1));
        double x2 = Double.valueOf(b.location.indexOf(0));
        double y2 = Double.valueOf(b.location.indexOf(1));


        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private ArrayList<Node> sort(ArrayList<Node> list){
        list.sort((n1, n2) ->Double.compare(n1.sGlobal, n2.sGlobal));
        return list;
    }
}
