package corepack;
import jade.core.AID;
import jade.core.Agent;

public class TaxiAgent extends Agent {
    protected void setup(){
        System.out.println("Taxi agent "+ this.getLocalName() +" is ready.");
    }
}
