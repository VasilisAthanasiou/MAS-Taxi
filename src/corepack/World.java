package corepack;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;

import mastutils.Node;



public class World extends Agent {

    // Taxi agent fields
    private String[] agentArray;
    private int numberOfAgents;

    // World fields
    private int x = 5;
    private int y = 5;
    private String[][] grid;
    private String[] discreteLoc = {"00", "40", "34", "04"};
    ArrayList<String> locations = new ArrayList<String>();
    int iteration = 0;
    Node [][] worldGraph;


    public void setup(){
        try {Thread.sleep(50);} catch (InterruptedException ie) {} // Make sure all agents are initialized
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
        setLocations();

        // Create World graph
        worldGraph = new Node[x][y];
        createGraph();

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                try {Thread.sleep(50);} catch (InterruptedException ie) {}
                // Send client location to any agent
                if(iteration < 5){
                    for (String agentName: agentArray) {
                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                        // Refer to receiver by local name
                        message.addReceiver(new AID(agentName, AID.ISLOCALNAME));
                        message.setContent(locations.get(0));
                        send(message);
                    }
                }
                else{
                    for (String agentName: agentArray) {
                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                        // Refer to receiver by local name
                        message.addReceiver(new AID(agentName, AID.ISLOCALNAME));
                        message.setContent("Terminate");
                        send(message);
                    }
                    doDelete();
                }
                draw();
                iteration++;
            }
        });

    }

    private void setLocations(){
        Random rand = new Random();
        String location;
        // Set random client location
        locations.add(discreteLoc[rand.nextInt(4)]);
        // Set random agent locations
        for(int i = 0; i < numberOfAgents; i++) {
            location = String.valueOf(rand.nextInt(4)); // Beware of reassigned variable
            location += String.valueOf(rand.nextInt(4));
            if(location.length() == 1)
                location = "0" + location;
            locations.add(location);
        }
        return;
    }

    /* ------------------------------------------- Create a graph representation of the World --------------------------------------------- */
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
                    worldGraph[i][j].edgeCost.add(1);
                }
                // If node is not on right edge. Set right neighbour
                if(i < x - 1){
                    worldGraph[i][j].neighbours.add(worldGraph[i + 1][j]);
                    worldGraph[i][j].edgeCost.add(1);
                }
                // If node is not on top edge. Set top neighbour
                if(j > 0){
                    worldGraph[i][j].neighbours.add(worldGraph[i][j - 1]);
                    worldGraph[i][j].edgeCost.add(1);
                }
                // If node is not on bottom edge. Set bottom neighbour
                if(j < y - 1){
                    worldGraph[i][j].neighbours.add(worldGraph[i][j + 1]);
                    worldGraph[i][j].edgeCost.add(1);
                }
            }
        }
        // Hard code walls
        worldGraph[0][3].edgeCost.set(0, 100);
        worldGraph[1][3].edgeCost.set(0, 100);

        worldGraph[0][4].edgeCost.set(0, 100);
        worldGraph[1][4].edgeCost.set(0, 100);

        worldGraph[1][0].edgeCost.set(1, 100);
        worldGraph[2][0].edgeCost.set(0, 100);

        worldGraph[1][1].edgeCost.set(1 , 100);
        worldGraph[2][1].edgeCost.set(0 , 100);

        worldGraph[2][3].edgeCost.set(1, 100);
        worldGraph[3][3].edgeCost.set(0, 100);

        worldGraph[2][4].edgeCost.set(1, 100);
        worldGraph[3][4].edgeCost.set(0, 100);

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

                // Print Nodes and neighbours
                System.out.printf("Node " + worldGraph[i][j].getLocation() + " has these neighbours and the respective costs to them: ");
                for(Node neighbour : worldGraph[i][j].neighbours){
                    System.out.printf(neighbour.getLocation() + " ");
                }
                System.out.printf(" | ");
                for(Integer cost : worldGraph[i][j].edgeCost){
                    System.out.printf(cost + " ");
                }
                System.out.println();
            }
            //System.out.println(line);
            System.out.println();
        }
        System.out.println();
    }
}
