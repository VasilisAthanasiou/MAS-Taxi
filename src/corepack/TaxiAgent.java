package corepack;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;




public class TaxiAgent extends Agent {

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




}
