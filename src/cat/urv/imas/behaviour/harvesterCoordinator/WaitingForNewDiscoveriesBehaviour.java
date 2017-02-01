/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterCoordinator;

import jade.core.behaviours.*;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import cat.urv.imas.agent.*;
import cat.urv.imas.onthology.InfoDiscovery;
import jade.core.Agent;

public class WaitingForNewDiscoveriesBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply, first;

    public WaitingForNewDiscoveriesBehaviour(Agent a) {
        super(a);
        first = true;
    }

    @Override
    public void action() {
        System.out.println("(HarvesterCoordinator) Starting WaitingForNewDiscoveriesBehaviour");
        HarvestCoordinator agent = (HarvestCoordinator) this.getAgent();

        hasReply = false;

        while (done() == false) {
            ACLMessage response = myAgent.receive();

            if (response != null) {
                switch (response.getPerformative()) {
                    case ACLMessage.INFORM:
                        agent.log("INFORM (new discoveries) received from " + ((AID) response.getSender()).getLocalName());

                        try {
                            ArrayList<InfoDiscovery> newDiscoveries = (ArrayList<InfoDiscovery>) response.getContentObject();
                            // We first initialize the list of newDiscoveries using set method
                            if (first) {
                                first = false;
                                agent.setNewInfoDiscoveriesList(newDiscoveries);
                            } else // Once it is initialized we can just add new discoveries
                            {
                                agent.addNewInfoDiscoveriesList(newDiscoveries);
                            }

                            hasReply = true;
                        } catch (Exception e) {
                            agent.errorLog("Incorrect content: " + e.toString());
                        }
                        break;
                    case ACLMessage.FAILURE:
                        break;
                    default:
                        ACLMessage reply = response.createReply();
                        // Sending a Failure
                        reply.setPerformative(ACLMessage.FAILURE);
                        agent.send(reply);
                        break;
                }
            }
        }
    }

    @Override
    public boolean done() {
        return hasReply;
    }

    public int onEnd() {
        hasReply = false;
        return 0;
    }
}
