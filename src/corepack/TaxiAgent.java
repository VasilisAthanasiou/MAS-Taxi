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

import mastutils.Node;



public class TaxiAgent extends Agent {

    Stack<String> actionStack = new Stack<String>();
    String []locations = new String[2]; // 0 : Agent, 1 : Goal

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
                ACLMessage msg = null;
                // Waiting to receive message
                msg = blockingReceive();

                switch (msg.getContent()){
                    case "PLAN":
                        System.out.println(getLocalName() + " will start planning\n");
                        break;
                    case "EXECUTE":
                        System.out.println(getLocalName() + " will execute plan\n");
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

    private void setActionStack(){
        actionStack.push("UP");
        actionStack.push("DOWN");
        actionStack.push("LEFT");
        actionStack.push("RIGHT");
    }

    private void act(String action){
        String msg = "ACTION:" + action;
        sendMessage(msg);
    }




}
