package corepack;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.Stack;
import jade.lang.acl.UnreadableException;

import mastutils.Node;
import mastutils.AStar;

public class TaxiAgent extends Agent {

    Stack<String> actionStack = new Stack<String>();
    String []locations = new String[2]; // 0 : Agent, 1 : Goal
    Node [][] worldGraph;

    public void setup(){

        System.out.println("Agent " + getLocalName() + " is online");

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
                        case "PLAN":
                            System.out.println(getLocalName() + " will start planning\n");
                            setActionStack(executeAStar());
                            break;
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
                    if (msg.getContent().contains("LOCATIONS")){

                        // Split message which is in form: (LOCATIONS,agentLocation,goalLocation) and update agent knowledge of locations
                        String []splitMsg = msg.getContent().split(",", 3);
                        updateLocations(splitMsg[1], splitMsg[2]);
                        System.out.println("Agent location is : " + locations[0] + "\nGoal location is : "+ locations[1]);
                    }
                }
                else { // If the message is of type Serialized
                    // Receive the graph representation of the World
                    try {
                        if(msg.getContentObject() instanceof Node[][]){ // Make sure the received object is a 2D array of Nodes
                            worldGraph = (Node[][])msg.getContentObject();
                        }
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
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



    private void updateLocations(String agent, String goal){
        locations[0] = agent;
        locations[1] = goal;
    }

    private void setActionStack(Stack<String> path){
        int x = Integer.valueOf(locations[0].charAt(0));
        int y = Integer.valueOf(locations[0].charAt(1));


        while(!path.isEmpty()){
            String next = path.pop();
            if(x < Integer.valueOf(next.charAt(0))){
                actionStack.push("RIGHT");
            }
            if(x > Integer.valueOf(next.charAt(0))){
                actionStack.push("LEFT");
            }
            if(y > Integer.valueOf(next.charAt(1))){
                actionStack.push("UP");
            }
            if(y < Integer.valueOf(next.charAt(1))){
                actionStack.push("DOWN");
            }
            x = Integer.valueOf(next.charAt(0));
            y = Integer.valueOf(next.charAt(1));
        }
    }

    private void act(String action){
        String msg = "ACTION:" + action;
        sendMessage(msg);
    }

    private void executeActions(){
        while(!actionStack.isEmpty())
            act(actionStack.pop());

        sendMessage("DONE");
    }

    // ---------------------------------------------- Run A* and translate path into moves ---------------------------------------------- */

    private Stack<String> executeAStar(){
        AStar astar = new AStar(locationToNode(locations[0]), locationToNode(locations[1]), worldGraph);

        return astar.compute();
    }

    private Node locationToNode(String location){
        int x = location.charAt(0) - '0';
        int y = location.charAt(1) - '0';
        //System.out.println(x + " " + y);
        return worldGraph[x][y];
    }

}
