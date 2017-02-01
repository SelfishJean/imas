/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.behaviour.system;

import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.Agent;
import cat.urv.imas.onthology.InfoMapChanges;

public class WaitingNewChangesOnMapBehaviour extends SimpleBehaviour {

    boolean hasReply;

    public WaitingNewChangesOnMapBehaviour(Agent agent) {
        super(agent);
        hasReply = false;
    }

    @Override
    public void action() {
        while (done() == false) {
            ACLMessage response = myAgent.receive();

            if (response != null) {
                hasReply = true;
                SystemAgent agent = (SystemAgent) this.getAgent();
                ACLMessage reply = response.createReply();
                try {
                    // Sending an Agree..
                    agent.setNewChangesOnMap((InfoMapChanges) response.getContentObject());
                    agent.log("Request received");
                    reply.setPerformative(ACLMessage.AGREE);
                    agent.send(reply);

                    // Sending an Inform..
                    ACLMessage reply2 = response.createReply();
                    reply2.setPerformative(ACLMessage.INFORM);

                    try {
                        reply2.setContent(MessageContent.NEXT_STEP);
                    } catch (Exception e) {
                        reply2.setPerformative(ACLMessage.FAILURE);
                        agent.errorLog(e.toString());
                        e.printStackTrace();
                    }
                    agent.send(reply2);
                } catch (Exception e) {
                    reply.setPerformative(ACLMessage.FAILURE);
                    agent.errorLog(e.getMessage());
                    e.printStackTrace();
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
