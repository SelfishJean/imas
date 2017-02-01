/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.agent.*;
import cat.urv.imas.onthology.GameSettings;
import jade.core.Agent;

public class AskingForMapBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply;

    public AskingForMapBehaviour(Agent a) {
        super(a);

        // We set the value of the first message.
        ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        initialRequest.addReceiver(agent.systemAgent);
        initialRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        agent.log("Request message to agent");
        try {
            initialRequest.setContent(MessageContent.GET_MAP);
            agent.log("Request message content:" + initialRequest.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.msg = initialRequest;
    }

    @Override
    public void action() {
        System.out.println("Starting AskingForMapBehaviour");
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();

        hasReply = false;
        myAgent.send(msg);

        while (done() == false) {
            ACLMessage response = myAgent.receive();

            if (response != null) {
                switch (response.getPerformative()) {
                    case ACLMessage.AGREE:
                        agent.log("AGREE received from " + ((AID) response.getSender()).getLocalName());
                        break;
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
