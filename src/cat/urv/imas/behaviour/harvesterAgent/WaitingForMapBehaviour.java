/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterAgent;

import jade.core.behaviours.*;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.agent.*;
import cat.urv.imas.onthology.GameSettings;
import jade.core.Agent;

public class WaitingForMapBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply;

    public WaitingForMapBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        System.out.println("(Harvester) Starting WaitingForMapBehaviour");
        Harvester agent = (Harvester) this.getAgent();

        hasReply = false;

        while (done() == false) {
            ACLMessage response = myAgent.receive();

            if (response != null) {
                switch (response.getPerformative()) {
                    case ACLMessage.INFORM:
                        agent.log("INFORM (new map) received from " + ((AID) response.getSender()).getLocalName());

                        try {
                            GameSettings game = (GameSettings) response.getContentObject();
                            agent.setGame(game);
                            agent.log(game.getShortString());
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
