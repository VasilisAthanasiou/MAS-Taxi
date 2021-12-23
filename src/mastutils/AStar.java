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
            for(int i = 0; i < currentNode.neighbours.size(); i++){
                // Add a neighbour to the list
                if(!currentNode.neighbours.get(i).isVisited)
                    notTestedNodes.add(currentNode.neighbours.get(i));

                // Calculate neighbours potential lowest parent distance
                double localGoal = currentNode.sLocal + euclideanDist(currentNode, currentNode.neighbours.get(i)) + currentNode.edgeCost.get(i);

                if(localGoal < currentNode.neighbours.get(i).sLocal){
                    currentNode.neighbours.get(i).parent = currentNode;
                    currentNode.neighbours.get(i).sLocal = localGoal;

                    // Change neighbours global score
                    currentNode.neighbours.get(i).setsGlobal(currentNode.neighbours.get(i).sLocal + euclideanDist(currentNode.neighbours.get(i), goalNode ));

                }
            }
        }
        // Follow path from goal to start, by iterating through parents
        Stack<String> path = new Stack<>();
        // Push the goal node
        path.push(goalNode.location);

        currentNode = goalNode;
        while(!currentNode.location.equals(startNode.location) && currentNode.isVisited){
            path.push(currentNode.parent.getLocation());
            System.out.println("Parent of " + currentNode.getLocation() + " = " + currentNode.parent.getLocation());
            currentNode = currentNode.parent;
        }
        for (String loc : path) {
            System.out.println(loc);
        }

        return path;
    }

    private double euclideanDist(Node a, Node b){
        double x1 = (double)a.location.charAt(0) - '0';
        double y1 = (double)a.location.charAt(1) - '0';
        double x2 = (double)b.location.charAt(0) - '0';
        double y2 = (double)b.location.charAt(1) - '0';

        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private ArrayList<Node> sort(ArrayList<Node> list){
        list.sort((n1, n2) ->Double.compare(n1.sGlobal, n2.sGlobal));
        return list;
    }
}
