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
    final private String[] discreteLoc = {"00", "40", "34", "04"};
    Hashtable<String, String> agentLocations = new Hashtable<>(); // 0 : Client Location | 1,2,..,n : Agents locations | n - 1 : Client Destination Location TODO: CHANGE THIS IMPLEMENTATION TO WORK FOR MULTIPLE AGENTS AND CLIENTS
    ArrayList<Itinerary> itineraries = new ArrayList<>();
    int iteration = 0;
    Node [][] worldGraph;
    Stack<String> messageStack = new Stack<>();


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
                }
                // Make sure there are messages to be sent
                else if(!messageStack.isEmpty()){
                    switch (messageStack.pop()){
                        case "SEND_GRAPH":
                            System.out.println("World will send graph to Taxi Agent");
                            sendObject(worldGraph);
                            break;
                        case "SEND_LOCATIONS":
                            System.out.println("World will send locations to Taxi Agent");
                            for (String agent: agentArray){ sendLocation(agentLocations.get(agent), agent); } // Send every agent its location
                            sendObject(itineraries); // Send the itineraries to every agent
                            break;
//                        case "PLAN":
//                            System.out.println("World asks Taxi Agent to start planning");
//                            sendMessage("PLAN");
//                            break;
//                        case "EXECUTE":
//                            System.out.println("World asks Taxi Agent to execute plan");
//                            sendMessage("EXECUTE");
//                            break;
                        case "SET_STACK":
                            System.out.println("World will set its stack");
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

    private void resolveUpdatedLocation(String move, String agentName){ // TODO: CHANGE THIS IMPLEMENTATION TO WORK FOR MULTIPLE AGENTS AND CLIENTS

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

    /* -------------------------------------------------------------------------------------------------------------------- */

    /* --------------------------------------- World Functions ------------------------------------------------------------ */

    // This will be a hardcoded set of messages the World agent will have to communicate to a TaxiAgent
    private void setMessageStack(){
        messageStack.push("SET_STACK");
        messageStack.push("EXECUTE");
        messageStack.push("PLAN");
        messageStack.push("SEND_LOCATIONS");
        messageStack.push("SEND_GRAPH");

        return;
    }

    private void setLocations(String agentName){ // TODO: CHANGE THIS IMPLEMENTATION TO WORK FOR MULTIPLE AGENTS AND CLIENTS
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

//        int xClient = locations.get(0).charAt(0) - '0';
//        int yClient = locations.get(0).charAt(1) - '0';
//
//        // TODO: Bad code. Will refactor for next assignment to accommodate more agents
//        int xAgent = locations.get(1).charAt(0) - '0';
//        int yAgent = locations.get(1).charAt(1) - '0';
//
//
//        System.out.println(locations.get(0) + " " + locations.get(1));
//        for (int j = 0; j < y; j++) {
//            String line = "";
//            for (int i = 0; i < x; i++) {
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
        System.out.println("DRAW FUNCTION WAS CALLED");
        return;
    }
}
