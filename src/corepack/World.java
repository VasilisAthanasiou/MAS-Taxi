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
import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;
import java.util.Stack;

import mastutils.Node;


public class World extends Agent {

    // Taxi agent fields
    private String[] agentArray;
    private int numberOfAgents;

    // World fields
    final private int x = 5;
    final private int y = 5;
    final private String[] discreteLoc = {"00", "40", "34", "04"};
    ArrayList<String> locations = new ArrayList<>(); // 0 : Client Location | 1,2,..,n : Agents locations | n - 1 : Client Destination Location TODO: CHANGE THIS IMPLEMENTATION TO WORK FOR MULTIPLE AGENTS AND CLIENTS
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
        System.out.println("Found:");
        for (int i = 0; i < numberOfAgents; ++i) System.out.println(agentArray[i]);

        // Initialize the locations of the agents and the client
        setLocations(true, ""); // TODO : CHANGE THESE
        // Set clients destination
        setLocations(false, locations.get(0));

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
                        resolveUpdatedLocation(action[1], 1);
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
                            sendLocations(locations.get(1), locations.get(0)); // TODO : Will need to refactor this to work with more agents
                            break;
                        case "PLAN":
                            System.out.println("World asks Taxi Agent to start planning");
                            sendMessage("PLAN");
                            break;
                        case "EXECUTE":
                            System.out.println("World asks Taxi Agent to execute plan");
                            sendMessage("EXECUTE");
                            break;
                        case "SET_STACK":
                            System.out.println("World will set its stack");
                            break;
                    }
                }
                else{
                    sendMessage("TERMINATE");
                    doDelete();
                }
            }
        });

    }

    /* --------------------------------------- JADE Functions --------------------------------------------------------------*/

    // Sends a message of type String
    private void sendMessage(String content){
        for (String agentName: agentArray) {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            // Refer to receiver by local name
            message.addReceiver(new AID(agentName, AID.ISLOCALNAME));
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

    private void sendLocations(String agentLoc, String goalLoc){ // TODO: REFACTOR THIS TO SEND AGENT SPECIFIC LOCATION
        String msg = "LOCATIONS," +agentLoc + "," + goalLoc;
        sendMessage(msg);
    }

    private void resolveUpdatedLocation(String move, int agentIndex){ // TODO: CHANGE THIS IMPLEMENTATION TO WORK FOR MULTIPLE AGENTS AND CLIENTS

        int xAgent = locations.get(agentIndex).charAt(0) - '0';
        int yAgent = locations.get(agentIndex).charAt(1) - '0';

        switch (move){
            case "UP":
                yAgent--;
                locations.set(agentIndex, locations.get(agentIndex).charAt(0) + String.valueOf(yAgent));
                return;
            case "DOWN":
                yAgent++;
                locations.set(agentIndex, locations.get(agentIndex).charAt(0) + String.valueOf(yAgent));
                return;
            case "LEFT":
                xAgent--;
                locations.set(agentIndex, String.valueOf(xAgent) + locations.get(agentIndex).charAt(1));
                return;
            case "RIGHT":
                xAgent++;
                locations.set(agentIndex, String.valueOf(xAgent) + locations.get(agentIndex).charAt(1));
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

    private void setLocations(boolean setAgents, String exclude){ // TODO: CHANGE THIS IMPLEMENTATION TO WORK FOR MULTIPLE AGENTS AND CLIENTS
        Random rand = new Random();
        String location;

        // Set random client location
        while(true){
            location = discreteLoc[rand.nextInt(4)];
            if(!location.equals(exclude)){break;}
        }
        locations.add(location);

        if (setAgents) {
            // Set random agent locations
            for (int i = 0; i < numberOfAgents; i++) {
                location = String.valueOf(rand.nextInt(4)); // Beware of reassigned variable
                location += String.valueOf(rand.nextInt(4));
                locations.add(location);
            }
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

        int xClient = locations.get(0).charAt(0) - '0';
        int yClient = locations.get(0).charAt(1) - '0';

        // TODO: Bad code. Will refactor for next assignment to accommodate more agents
        int xAgent = locations.get(1).charAt(0) - '0';
        int yAgent = locations.get(1).charAt(1) - '0';


        System.out.println(locations.get(0) + " " + locations.get(1));
        for (int j = 0; j < y; j++) {
            String line = "";
            for (int i = 0; i < x; i++) {
                if(xAgent == xClient && yAgent == yClient && xClient == i && yClient == j){
                    line += "[1* ]";
                }
                else if(i == xAgent && j == yAgent){
                    line += "[ 1 ]";
                }
                else if(i == xClient && j == yClient){
                    line += "[ * ]";
                }
                else{
                    line += "[   ]";
                }

//                // Print Nodes and neighbours
//                System.out.printf("Node " + worldGraph[i][j].getLocation() + " has these neighbours and the respective costs to them: ");
//                for(Node neighbour : worldGraph[i][j].neighbours){
//                    System.out.printf(neighbour.getLocation() + " ");
//                }
//                System.out.printf(" | ");
//                for(Integer cost : worldGraph[i][j].edgeCost){
//                    System.out.printf(cost + " ");
//                }
//                System.out.println();
            }
            System.out.println(line);
            // System.out.println();
        }
        System.out.println();

        return;
    }
}
