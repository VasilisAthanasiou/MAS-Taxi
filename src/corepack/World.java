package corepack;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import mastutils.Itinerary;
import mastutils.Node;


public class World extends Agent {

    // Taxi agent fields
    private String[] agentArray;
    private int numberOfAgents;

    // World fields
    final private int x = 5;
    final private int y = 5;
    final private String[] discreteLoc = {"00", "40", "34", "04"}; // RGBY Locations described in assignment
    Hashtable<String, String> agentLocations = new Hashtable<>(); // Contains pairs of <agentName, agentLocation>
    ArrayList<Itinerary> itineraries = new ArrayList<>(); // List of all itineraries. Itineraries are objects that contain the clients current location and their desired destination
    ArrayList<String[]> itineraryRequests = new ArrayList<>(); // Hashtable of itinerary request messages from agents
    int iteration = 0;
    Node [][] worldGraph; // Node representation of the world. Used for computing shortest path
    Stack<String> messageStack = new Stack<>(); // Messages the world will send to agents
    int numberOfConflictingAgents;
    ArrayList<String[]> bids = new ArrayList<>();


    public void setup(){
        try {Thread.sleep(50);} catch (InterruptedException ie) { // Make sure all agents are initialized
            System.out.println(ie);
        }

        // Search the registry for agents
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agent");
        dfd.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, dfd);
            numberOfAgents = result.length;
            agentArray = new String[numberOfAgents];
            for (int i = 0; i < numberOfAgents; ++i) {
                agentArray[i] = result[i].getName().getLocalName();
            }
        }
        catch (FIPAException fe) {fe.printStackTrace();}
        Arrays.sort(agentArray);

        // Display agent names and set their location on the world
        System.out.println("Found:");
        for (int i = 0; i < numberOfAgents; ++i){
            System.out.println(agentArray[i]);
            // Initialize the locations of the agents
            setLocations(agentArray[i]);
        }

        // Sets client itineraries (5 clients at this moment)
        for(int i = 0; i < 5; i++) {
            setLocations("");
        }

        // Create World graph
        worldGraph = new Node[x][y];
        createGraph();

        // Draw the World for the first time
        draw();

        // Initialize the message stack
        setMessageStack();

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                try {Thread.sleep(50);} catch (InterruptedException ie) {System.out.println(ie);}

                ACLMessage msg;
                msg = receive();
                // Make sure if there are any messages to be read
                if (!(msg == null)){
                    // If the message is an action message, update agents position in World
                    while(msg.getContent().contains("ACTION")){  // TODO : CHANGE THIS TO WORK FOR MULTIPLE AGENTS
                        String []action = msg.getContent().split(":", 2);
                        System.out.println(msg.getContent());
                        resolveUpdatedLocation(action[1], msg.getSender().getLocalName());
                        draw();
                        try {Thread.sleep(50);} catch (InterruptedException ie) {System.out.println(ie);}
                        msg = receive();
                    }
                    if(msg.getContent().equals("GRAPH")){
                        //System.out.println("World will send graph to Taxi Agent per agents request\n");
                        sendObject(worldGraph);
                    }
                    else if(msg.getContent().contains("ITINERARY")){
                        String [] request = new String[2];
                        request[0] = msg.getSender().getLocalName();
                        request[1] = msg.getContent().split(",", 2)[1];

                        itineraryRequests.add(request);
                        ArrayList<String> conflictingAgents = new ArrayList<>();
                        if(itineraryRequests.size() == agentArray.length){ // If the world has received requests from all agents
                            conflictingAgents = identifyConflicts();
                        }
                        if(!conflictingAgents.isEmpty()){
                            for (String conflict : conflictingAgents) {
                                String [] agents = conflict.split(":", 3);
                                if(agents.length > 1){
                                    System.out.println("Conflicting agents : " + conflict);
                                    for (String agent : agents) {
                                        numberOfConflictingAgents = agents.length;
                                        sendMessage("BID", agent);
                                    }
                                }
                            }
                        }
                    }
                    else if(msg.getContent().contains("BID")){
                        String [] bid = new String[2];
                        bid[0] = msg.getSender().getLocalName();
                        bid[1] = msg.getContent().split(":",2)[1];

                        bids.add(bid);
                        if(bids.size() == numberOfConflictingAgents){
                            int winningIndex = determineAuctionWinner();
                            System.out.println("Agent : " + bids.get(winningIndex)[0] + " won the auction");
                            for (int i = 0; i < numberOfConflictingAgents; i++) {
                                if(i == winningIndex){
                                    sendMessage("WON", bids.get(winningIndex)[0]);
                                }
                                else {
                                    sendMessage("LOST", bids.get(i)[0]);
                                }
                            }
                        }

                    }
                }
                // Make sure there are messages to be sent
                else if(!messageStack.isEmpty()){
                    switch (messageStack.pop()){
                        case "SEND_GRAPH":
                            System.out.println("World will send graph to Taxi Agent\n");
                            sendObject(worldGraph);
                            break;
                        case "SEND_LOCATIONS":
                            System.out.println("World will send locations to Taxi Agent\n");
                            for (String agent: agentArray){ sendLocation(agentLocations.get(agent), agent); } // Send every agent its location
                            sendObject(itineraries); // Send the itineraries to every agent
                            break;
                        case "SET_STACK":
                            System.out.println("World will set its stack\n");

                            break;
                    }
                }
                else{
                    sendMessage("TERMINATE", "EVERYONE");
                    doDelete();

                }
            }
        });
    }

    /* --------------------------------------- JADE Functions --------------------------------------------------------------*/

    // Sends a message of type String
    private void sendMessage(String content, String recipient){
        if(recipient == "EVERYONE"){
            for (String agentName: agentArray) {
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                // Refer to receiver by local name
                message.addReceiver(new AID(agentName, AID.ISLOCALNAME));
                message.setContent(content);
                send(message);
            }
        }
        else{
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            // Refer to receiver by local name
            message.addReceiver(new AID(recipient, AID.ISLOCALNAME));
            message.setContent(content);
            send(message);
        }

    }

    private void sendObject(Serializable obj){
        for (String agentName: agentArray) {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            // Refer to receiver by local name
            message.addReceiver(new AID(agentName, AID.ISLOCALNAME));
            try {
                message.setContentObject(obj);
            }catch (IOException io){};
            send(message);
        }
    }

    private void sendLocation(String agentLoc, String agentName){
        String msg = "LOCATION," +agentLoc;
        sendMessage(msg, agentName);
    }


    /* -------------------------------------------------------------------------------------------------------------------- */

    /* --------------------------------------- World Functions ------------------------------------------------------------ */

    // Run auction
    // This will be a hardcoded set of messages the World agent will have to communicate to a TaxiAgent
    private void setMessageStack(){
        messageStack.push("SET_STACK");
        messageStack.push("EXECUTE");
        messageStack.push("PLAN");
        messageStack.push("SEND_LOCATIONS");
        messageStack.push("SEND_GRAPH");

        return;
    }

    private void setLocations(String agentName){
        Random rand = new Random();
        String clientLocation;
        String clientDestination;
        String agentLocation;

        if (agentName != "") {
            // Set random agent location
            while(true){
                agentLocation = String.valueOf(rand.nextInt(4)); // Beware of reassigned variable
                agentLocation += String.valueOf(rand.nextInt(4));

                if(!agentLocations.containsValue(agentLocation)){
                    agentLocations.put(agentName, agentLocation);
                    break;
                }
            }
        }
        else{
            // Set random client location
            clientLocation = discreteLoc[rand.nextInt(4)];

            // Set client destination randomly
            while(true){
                clientDestination = discreteLoc[rand.nextInt(4)];
                if(!clientDestination.equals(clientLocation)){break;}
            }

            Itinerary itin = new Itinerary(clientLocation, clientDestination);
            System.out.println("Itinerary : Customer location : " + clientLocation + " | Customer destination : " + clientDestination);
            itineraries.add(itin);
        }
        return;
    }

    private void resolveUpdatedLocation(String move, String agentName){

        int xAgent = agentLocations.get(agentName).charAt(0) - '0';
        int yAgent = agentLocations.get(agentName).charAt(1) - '0';

        switch (move){
            case "UP":
                yAgent--;
                agentLocations.put(agentName, agentLocations.get(agentName).charAt(0) + String.valueOf(yAgent));
                return;
            case "DOWN":
                yAgent++;
                agentLocations.put(agentName, agentLocations.get(agentName).charAt(0) + String.valueOf(yAgent));
                return;
            case "LEFT":
                xAgent--;
                agentLocations.put(agentName, String.valueOf(xAgent) + agentLocations.get(agentName).charAt(1));
                return;
            case "RIGHT":
                xAgent++;
                agentLocations.put(agentName, String.valueOf(xAgent) + agentLocations.get(agentName).charAt(1));
                return;
        }
    }

    // Compares all itinerary requests and returns pairs of conflicting agents
    private ArrayList<String> identifyConflicts(){
        ArrayList<String> conflictingAgents = new ArrayList<>();
        String potentialConflict = "";

        for (int i = 0; i < itineraries.size(); i++) {
            potentialConflict = "";
            for (int j = 0; j < agentArray.length; j++) {
                if(itineraryRequests.get(j)[1].equals(String.valueOf(i))){
                    potentialConflict += itineraryRequests.get(j)[0] + ":";
                }
            }
            conflictingAgents.add(potentialConflict);
        }
        return conflictingAgents;
    }

    private int determineAuctionWinner(){
        int bestBid = 100;
        int bestBidIndex = -1;

        for (int i = 0; i < bids.size(); i++) {
            int currentBid = Integer.valueOf(bids.get(i)[1]);
            if(currentBid < bestBid){
                bestBid = currentBid;
                bestBidIndex = i;
            }
        }
        return bestBidIndex;
    }

    /* ----------------------------------------- Create a graph representation of the World --------------------------------------------- */
    public void createGraph(){

        // First initialize all nodes and add them to the world graph
        for (int j = 0; j < y; j++) {
            for (int i = 0; i < x; i++) {
                Node worldNode = new Node(String.valueOf(i) + String.valueOf(j));
                worldGraph[i][j] = worldNode;
            }
        }

        // Connect all nodes add assign a cost of 1 on every edge
        for (int j = 0; j < y; j++) {
            for (int i = 0; i < x; i++) {
                // If node is not on left edge. Set left neighbour
                if(i > 0){
                    worldGraph[i][j].neighbours.add(worldGraph[i - 1][j]);
                    worldGraph[i][j].edgeCost.add(1.);
                }
                // If node is not on right edge. Set right neighbour
                if(i < x - 1){
                    worldGraph[i][j].neighbours.add(worldGraph[i + 1][j]);
                    worldGraph[i][j].edgeCost.add(1.);
                }
                // If node is not on top edge. Set top neighbour
                if(j > 0){
                    worldGraph[i][j].neighbours.add(worldGraph[i][j - 1]);
                    worldGraph[i][j].edgeCost.add(1.);
                }
                // If node is not on bottom edge. Set bottom neighbour
                if(j < y - 1){
                    worldGraph[i][j].neighbours.add(worldGraph[i][j + 1]);
                    worldGraph[i][j].edgeCost.add(1.);
                }
            }
        }
        // Hard code walls.
        worldGraph[0][3].edgeCost.set(0, 100.);
        worldGraph[1][3].edgeCost.set(0, 100.);

        worldGraph[0][4].edgeCost.set(0, 100.);
        worldGraph[1][4].edgeCost.set(0, 100.);

        worldGraph[1][0].edgeCost.set(1, 100.);
        worldGraph[2][0].edgeCost.set(0, 100.);

        worldGraph[1][1].edgeCost.set(1 , 100.);
        worldGraph[2][1].edgeCost.set(0 , 100.);

        worldGraph[2][3].edgeCost.set(1, 100.);
        worldGraph[3][3].edgeCost.set(0, 100.);

        worldGraph[2][4].edgeCost.set(1, 100.);
        worldGraph[3][4].edgeCost.set(0, 100.);

        return;
    }
    /* ---------------------------------------------------------------------------------------------------------------------------------------------------- */

    /* ------- Function meant to draw the world and the agents on it. Agents are represented by their numbers and customers by an asterisk (*) ------------ */
    private void draw(){

//        ArrayList<String> clientLocations = new ArrayList<>();
//        int xClient = locations.get(0).charAt(0) - '0';
//        int yClient = locations.get(0).charAt(1) - '0';
//
//        // TODO: Bad code. Will refactor for next assignment to accommodate more agents
//        int xAgent = locations.get(1).charAt(0) - '0';
//        int yAgent = locations.get(1).charAt(1) - '0';
//
//        for (Itinerary it : itineraries) {
//            clientLocations.add(it.getClientLocation());
//        }
//
//        for (int j = 0; j < y; j++) {
//            String line = "";
//            for (int i = 0; i < x; i++) {
//                if(){
//
//                }
//                if(xAgent == xClient && yAgent == yClient && xClient == i && yClient == j){
//                    line += "[1* ]";
//                }
//                else if(i == xAgent && j == yAgent){
//                    line += "[ 1 ]";
//                }
//                else if(i == xClient && j == yClient){
//                    line += "[ * ]";
//                }
//                else{
//                    line += "[   ]";
//                }
//
////                // Print Nodes and neighbours
////                System.out.printf("Node " + worldGraph[i][j].getLocation() + " has these neighbours and the respective costs to them: ");
////                for(Node neighbour : worldGraph[i][j].neighbours){
////                    System.out.printf(neighbour.getLocation() + " ");
////                }
////                System.out.printf(" | ");
////                for(Integer cost : worldGraph[i][j].edgeCost){
////                    System.out.printf(cost + " ");
////                }
////                System.out.println();
//            }
//            System.out.println(line);
//            // System.out.println();
//        }
//        System.out.println();
        return;
    }
}
