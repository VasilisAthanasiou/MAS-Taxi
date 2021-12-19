package corepack;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.Arrays;


public class World extends Agent {

    private String[] agentArray;
    private int numberOfAgents;
    private int x = 5;
    private int y = 5;
    private int[][] locations;
    private int visibility = 3;

    protected void setup(){
        try {Thread.sleep(50);} catch (InterruptedException ie) {} // Make sure all agents are initialized
        // Search the registry for agents
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agent");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
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
        //findStartingLocations();
//        System.exit(2);

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // Do stuff
            }
        });
    }
}
