/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterCoordinator;

import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.agent.*;
import jade.core.Agent;

public class SendingNewCollectedGarbageBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply;

    public SendingNewCollectedGarbageBehaviour(Agent a) {
        super(a);
        hasReply = false;

    }

    @Override
    public void action() {
        HarvestCoordinator agent = (HarvestCoordinator) this.getAgent();

        ACLMessage NewStepRequest = new ACLMessage(ACLMessage.REQUEST);
        NewStepRequest.clearAllReceiver();
        NewStepRequest.addReceiver(agent.coordinatorAgent);
        NewStepRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        agent.log("Request message to agent");

        try {
            NewStepRequest.setContentObject(agent.getCellsCollectedGarbage());
            agent.log("Request message to send new changes on the map (CollectedGarbage)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.msg = NewStepRequest;
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
                        agent.log("INFORM (new CollectedGarbage confirm) received from " + ((AID) response.getSender()).getLocalName());
                        hasReply = true;
                        try {
                            Object content = (Object) response.getContent();
                            if (content.equals(MessageContent.NEXT_STEP)) {
                                agent.log("Coordinator Agent has received changes.");
                            } else {
                                agent.log("Coordinator Agent has NOT received changes. WHY?");
                            }
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

    @Override
    public int onEnd() {
        hasReply = false;
        return 0;
    }
}
