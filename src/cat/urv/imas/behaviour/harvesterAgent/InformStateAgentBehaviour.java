/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterAgent;

import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.agent.*;
import cat.urv.imas.map.Cell;
import jade.core.Agent;

public class InformStateAgentBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply;
    int nextBehaviour;

    public InformStateAgentBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        Harvester agent = (Harvester) this.getAgent();

        // We set the value of the first message.
        ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();

        initialRequest.addReceiver(agent.harvesterCoordinator);
        initialRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        agent.log("Request message");
        try {
            initialRequest.setContent(agent.getCurrentAgentState());
            agent.log("Request message content:" + initialRequest.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.msg = initialRequest;

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
                        agent.log("INFORM (agents can work) received from " + ((AID) response.getSender()).getLocalName());

                        try {
                            Cell[] c = (Cell[]) response.getContentObject();
                            agent.setNewGoalCell(c[0]);
                            agent.goalBuilding = c[1];
                            agent.log("New Goal received correctly");
                            nextBehaviour = 1; // StartingGoalBehaviour
                        } catch (Exception e) {
                            agent.log("Message with the inform: " + response.getContent());
                            if (response.getContent().equals(MessageContent.NO_GOAL)) {
                                nextBehaviour = 5; // ChillingBehaviour
                            }
                        }

                        hasReply = true;
                        switch (agent.state) {
                            case MessageContent.MOVING:
                                nextBehaviour = 2; // MovingBehaviour
                                break;
                            case MessageContent.COLLECTING:
                                nextBehaviour = 3; // CollectingBehaviour
                                break;
                            case MessageContent.DUMPING:
                                nextBehaviour = 4; // DumpingBehaviour
                                break;
                        }
                        break;
                    case ACLMessage.FAILURE:
                        agent.log("The action has failed.");
                        nextBehaviour = 0; // We repeat this behaviour.
                        return;
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
        return nextBehaviour;
    }

}
