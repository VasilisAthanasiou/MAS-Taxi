package corepack;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Stack;
import jade.lang.acl.UnreadableException;

import mastutils.Itinerary;
import mastutils.Node;
import mastutils.AStar;

public class TaxiAgent extends Agent {

    // AGENTS BELIEF SYSTEM OR KNOWLEDGE BASE
    ArrayList<String> actionList = new ArrayList<String>();
    String location; // Agent location
    ArrayList<Itinerary> itineraries = new ArrayList<>(); // List of all itineraries [0] = customer location, [1] = customer destination
    ArrayList<Stack<String>> paths = new ArrayList<>();
    int itineraryIndex = -1;
    Node [][] worldGraph;
    int credits;
    // ----------------------------------------------------

    public void setup(){

        System.out.println("Agent " + getLocalName() + " is online");
        credits = 0;

        // Register agent to directory facilitator
        DFAgentDescription dfd = new DFAgentDescription();
        // Agent id
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agent");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {DFService.register(this, dfd);}
        catch (FIPAException fe) {fe.printStackTrace();}

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg;
                // Waiting to receive message
                msg = blockingReceive();

                if(!(msg.hasByteSequenceContent())){ // If the message is of type String
                    switch (msg.getContent()){
//                        case "PLAN":
//                            System.out.println(getLocalName() + " will start planning\n");
//                            setActionStack(executeAStar());
//                            break;
                        case "EXECUTE":
                            System.out.println(getLocalName() + " will execute plan\n");
                            executeActions();
                            break;
                        case "TERMINATE":
                            System.out.println(getLocalName() + " is terminating");
                            // Terminate agent
                            takeDown();
                            doDelete();
                            break;
                    }
                    if (msg.getContent().contains("LOCATION")){

                        // Split message which is in form: (LOCATIONS,agentLocation,goalLocation) and update agent knowledge of locations
                        String []splitMsg = msg.getContent().split(",", 2);
                        setLocation(splitMsg[1]);
                        System.out.println(getLocalName() + " location is : " + location);
                    }
                }
                else { // If the message is of type Serialized
                    // Receive the graph representation of the World
                    try {
                        if(msg.getContentObject() instanceof Node[][]){ // Make sure the received object is a 2D array of Nodes
                            worldGraph = (Node[][])msg.getContentObject();
                        }
                        else if(msg.getContentObject() instanceof ArrayList<?>){
                            itineraries = (ArrayList<Itinerary>)msg.getContentObject();
                            System.out.println("Computing the best itinerary");
                            itineraryIndex = computeBestItinerary();
                            System.out.println("Best itinerary has index : " + itineraryIndex);
                            setActionStack(paths.get(itineraryIndex));
                            executeActions();
                        }
                    } catch (UnreadableException e){e.printStackTrace();}
                }
            }
        });
    }


    // Sends a message of type String
    private void sendMessage(String content){

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        // Refer to receiver by local name
        message.addReceiver(new AID("World", AID.ISLOCALNAME));
        message.setContent(content);
        send(message);
    }


    private void setLocation(String agentLocation){
        location = agentLocation;
    }

    // The action stack represents the agents Intents
    private void setActionStack(Stack<String> path){

        String initial = path.pop();
        int x = initial.charAt(0) - '0';
        int y = initial.charAt(1) - '0';

        System.out.println(x + " " + y);

        while(!path.isEmpty()){
            String next = path.pop();

            if(x < next.charAt(0) - '0'){
                actionList.add("RIGHT");
            }
            if(x > next.charAt(0) - '0'){
                actionList.add("LEFT");
            }
            if(y > next.charAt(1) - '0'){
                actionList.add("UP");
            }
            if(y < next.charAt(1) - '0'){
                actionList.add("DOWN");
            }
            x = next.charAt(0) - '0';
            y = next.charAt(1) - '0';
        }
    }

    private void act(String action){
        String msg = "ACTION:" + action;
        sendMessage(msg);
    }

    private void executeActions(){
        for (String action : actionList) {
            act(action);
        }
        sendMessage("DONE");
    }
    // -------------------------------------- Figure out which itinerary is best for agent ---------------------------------------------- */

    // Calculate all paths from agents location to clients location and pick the smallest one
    private int computeBestItinerary(){
        Stack<String> shortestPath = new Stack<>();
        Stack<String> placeholderPath = new Stack<>();
        int shortestPathIndex = -1;

        // Compute and compare all paths from agent to clients and store the index of the shortest one
        for (int i = 0; i < itineraries.size(); i++) {
            placeholderPath = executeAStar(location, itineraries.get(i).getClientLocation());
            if(shortestPath.size() == 0 || shortestPath.size() > placeholderPath.size()){
                shortestPath = placeholderPath;
                shortestPathIndex = i;
            }
            paths.add(placeholderPath);
        }
        return shortestPathIndex;
    }

    // ---------------------------------------------- Run A* and translate path into moves ---------------------------------------------- */

    private Stack<String> executeAStar(String start, String finish){
        AStar astar = new AStar(locationToNode(start), locationToNode(finish), worldGraph);

        return astar.compute();
    }

    private Node locationToNode(String location){
        int x = location.charAt(0) - '0';
        int y = location.charAt(1) - '0';
        //System.out.println(x + " " + y);
        return worldGraph[x][y];
    }

}
