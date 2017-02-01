/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import jade.core.behaviours.*;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import cat.urv.imas.agent.*;
import cat.urv.imas.onthology.InfoDiscovery;
import jade.core.Agent;

public class WaitingForNewDiscoveriesBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply;

    public WaitingForNewDiscoveriesBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        hasReply = false;

        while (done() == false) {
            ACLMessage response = myAgent.receive();

            if (response != null) {
                switch (response.getPerformative()) {
                    case ACLMessage.INFORM:
                        agent.log("INFORM (new discoveries) received from " + ((AID) response.getSender()).getLocalName());

                        try {
                            ArrayList<InfoDiscovery> newDiscoveries = (ArrayList<InfoDiscovery>) response.getContentObject();
                            agent.setNewInfoDiscoveriesList(newDiscoveries);
                            agent.log("New discoveries saved");
                            hasReply = true;

                        } catch (Exception e) {
                            agent.errorLog("Incorrect content: " + e.toString());
                        }
                        break;
                    case ACLMessage.FAILURE:
                        agent.log("The action has failed.");
                        break;
                    default:
                        agent.log("Failed to process the message");
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
