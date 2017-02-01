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
package cat.urv.imas.behaviour.harvesterCoordinator;

import cat.urv.imas.agent.HarvestCoordinator;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.core.Agent;
import cat.urv.imas.onthology.InfoAgent;
import java.util.ArrayList;

public class WaitingForNewPositionsBehaviour extends SimpleBehaviour {

    int count;
    ArrayList<InfoAgent> allHarvesters;

    public WaitingForNewPositionsBehaviour(Agent agent) //It cannot be SystemAgent type
    {
        super(agent);
        count = 0;
        allHarvesters = new ArrayList<InfoAgent>();
    }

    @Override
    public void action() {
        HarvestCoordinator agent = (HarvestCoordinator) this.getAgent();
        allHarvesters = agent.getListHarvesters();
        try {
            agent.newInfoAgent.clear();
        } catch (Exception e) {

        }

        while (done() == false) {
            ACLMessage response = myAgent.receive();

            if (response != null) {
                switch (response.getPerformative()) {
                    case ACLMessage.INFORM:
                        agent.log("INFORM (new position) received from " + ((AID) response.getSender()).getLocalName());

                        try {
                            InfoAgent newPosition = (InfoAgent) response.getContentObject();

                            if (count < 1) {
                                agent.setNewInfoAgent(newPosition);
                            } else {
                                agent.addNewInfoAgent(newPosition);
                            }

                            agent.log("New positions saved");

                            // Sending an Agree..
                            ACLMessage reply = response.createReply();
                            reply.setPerformative(ACLMessage.AGREE);
                            agent.log("Sending Agreement");
                            agent.send(reply);
                            count++;
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

    public boolean done() {
        if (count < allHarvesters.size()) {
            return false;
        } else {
            return true;
        }
    }

    public int onEnd() {
        count = 0;
        return 0;
    }

}
