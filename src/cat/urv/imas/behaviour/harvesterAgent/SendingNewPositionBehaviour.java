/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterAgent;

import jade.core.behaviours.*;
import cat.urv.imas.agent.*;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class SendingNewPositionBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply;
    int nextBehaviour;

    public SendingNewPositionBehaviour(Agent a) {
        super(a);
        hasReply = false;
    }

    @Override
    public void action() {
        Harvester agent = (Harvester) this.getAgent();
        agent.log("SendingNewPositionBehaviour...");

        // We set the value of the message. 
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.clearAllReceiver();
        message.addReceiver(agent.harvesterCoordinator);

        try {
            message.setContentObject(agent.getNewInfoAgent());
            agent.log("Sending new position");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.msg = message;
        myAgent.send(msg);
        while (done() == false) {
            ACLMessage response = myAgent.receive();

            if (response != null) {
                switch (response.getPerformative()) {
                    case ACLMessage.AGREE:
                        hasReply = true;
                        nextBehaviour = 1; // Waiting map behaviour
                        return;
                    case ACLMessage.FAILURE:
                        nextBehaviour = 0; // We repeat this behaviour.
                        return;
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
