package corepack;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;



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
        setLocations();
//        System.exit(2);

        /*
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // Do stuff
            }
        });

         */
    }

    private void setLocations(){
        Random rand = new Random();
        String location;
        // Set random client location
        locations.add(discreteLoc[rand.nextInt(4)]);
        // Set random agent locations
        for(int i = 0; i < numberOfAgents; i++) {
            location = String.valueOf(rand.nextInt(45)); // Beware of reassigned variable
            if(location.length() == 1)
                location = "0" + location;
            locations.add(location);
        }
        return;
    }
}
