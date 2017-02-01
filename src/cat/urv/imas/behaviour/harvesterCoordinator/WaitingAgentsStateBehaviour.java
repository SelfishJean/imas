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
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import java.util.Map;
import jade.core.Agent;
import java.util.ArrayList;

/**
 * A request-responder behaviour for System agent, answering to queries from the
 * Coordinator agent. The Coordinator Agent sends a REQUEST of the whole game
 * information and the System Agent sends an AGREE and then an INFORM with the
 * city information.
 */
public class WaitingAgentsStateBehaviour extends SimpleBehaviour {

    boolean hasReply, first;
    ArrayList<InfoAgent> allHarvesters;
    int count;

    public WaitingAgentsStateBehaviour(Agent agent) {
        super(agent);
        hasReply = false;
        first = true;
        allHarvesters = new ArrayList<InfoAgent>();
        count = 0;
        HarvestCoordinator a = (HarvestCoordinator) this.getAgent();
        a.initializeOngoingGoals();
    }

    @Override
    public void action() {
        HarvestCoordinator agent = (HarvestCoordinator) this.getAgent();
        agent.log("Starting WaitingAgentsStateBehaviour");
        agent.initializeCellsCollectedGarbage();

        Map<AID, Cell> onGoingGoals = agent.getOngoingGoals();
        for (Map.Entry<AID, Cell> entry : onGoingGoals.entrySet()) {
            if (entry.getValue().getCellType() == CellType.BUILDING) {
                BuildingCell temp = (BuildingCell) entry.getValue();
                if (temp.detectGarbage().isEmpty()) {
                    onGoingGoals.remove(entry);
                }
            }
        }

        if (first) {
            first = false;
            allHarvesters = agent.getListHarvesters();
        }

        while (done() == false) {
            ACLMessage response = myAgent.receive();
            if (response != null) {
                switch (response.getPerformative()) {
                    case ACLMessage.REQUEST:
                        hasReply = true;
                        ACLMessage reply = response.createReply();
                        try {
                            Object content = (Object) response.getContent();
                            agent.log("Request received from " + ((AID) response.getSender()).getLocalName());

                            // Sending an Agree..
                            reply.setPerformative(ACLMessage.AGREE);
                            agent.log("Sending Agreement");
                            agent.send(reply);

                            // Sending an Inform..
                            ACLMessage reply2 = response.createReply();
                            reply2.setPerformative(ACLMessage.INFORM);
                            try {
                                switch (content.toString()) {
                                    case MessageContent.FREE:
                                        agent.log(content.toString());

                                        try {
                                            boolean condition;
                                            condition = agent.newInfoDiscoveriesList.isEmpty();
                                            AID sender = (AID) response.getSender();
                                            if (!condition) {
                                                Cell c;
                                                AID harvester = response.getSender();

                                                Cell[] result = agent.assignGoal(harvester);
                                                c = result[0];
                                                Cell c2 = result[1];

                                                Map<AID, Cell> currentGoals = agent.getOngoingGoals();
                                                if (!currentGoals.containsKey(sender)) {
                                                    agent.addOngoingGoals(sender, c2);
                                                }

                                                reply2.setContentObject(result);
                                            } else {
                                                reply2.setContent(MessageContent.NO_GOAL);
                                            }
                                        } catch (Exception e) {

                                        }
                                        break;
                                    case MessageContent.MOVING:
                                        agent.log(content.toString());
                                        reply2.setContent(MessageContent.OK);
                                        break;
                                    case MessageContent.COLLECTING:
                                        AID sender = (AID) response.getSender();
                                        Map<AID, Cell> currentGoals = agent.getOngoingGoals();
                                        if (currentGoals.containsKey(sender)) {
                                            agent.addCellsCollectedGarbage(currentGoals.get(sender));
                                        }
                                        agent.log(content.toString());
                                        reply2.setContent(MessageContent.OK);
                                        break;
                                    case MessageContent.DUMPING:
                                        agent.log(content.toString());
                                        reply2.setContent(MessageContent.OK);
                                        break;
                                }
                                count++;
                            } catch (Exception e) {
                                reply2.setPerformative(ACLMessage.FAILURE);
                                System.err.println(e.toString());
                                e.printStackTrace();
                            }
                            agent.log("Sending Inform");
                            agent.send(reply2);
                        } catch (Exception e) {
                            reply.setPerformative(ACLMessage.FAILURE);
                            agent.errorLog(e.getMessage());
                            e.printStackTrace();
                        }
                        break;

                    default:
                        agent.log("Failed to process the message");
                        ACLMessage reply2 = response.createReply();

                        // Sending a Failure
                        reply2.setPerformative(ACLMessage.FAILURE);
                        agent.send(reply2);
                        break;
                }
            }
        }
    }

    @Override
    public boolean done() {
        if (count < allHarvesters.size()) {
            return false;
        } else {
            return true;
        }
    }

    public int onEnd() {
        hasReply = false;
        count = 0;
        return 0;
    }

}
