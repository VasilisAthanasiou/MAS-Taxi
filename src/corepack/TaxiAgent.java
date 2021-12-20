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

                // Receive client location and plan route
                if(!msg.getContent().isEmpty() && !msg.getContent().equals("Terminate")){
                    //System.out.println("Agent must go to location : " + msg.getContent());
                }
                else if(msg.getContent().equals("Terminate")){
                    //System.out.println("Agent : " + getLocalName() +" has terminated");
                    // Take down from registry
                    takeDown();
                    // Terminate
                    doDelete();
                }
            }
        });


    }
}
