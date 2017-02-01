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
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.CoordinatorAgent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import jade.core.Agent;
import cat.urv.imas.onthology.InfoAgent;
import java.util.ArrayList;

/**
 * A request-responder behaviour for System agent, answering to queries from the
 * Coordinator agent. The Coordinator Agent sends a REQUEST of the whole game
 * information and the System Agent sends an AGREE and then an INFORM with the
 * city information.
 */
public class WaitingForNewPositionsBehaviour extends SimpleBehaviour {

    boolean hasReplyFromHC, hasReplyFromSC;

    public WaitingForNewPositionsBehaviour(Agent agent) {
        super(agent);
        hasReplyFromHC = false;
        hasReplyFromSC = false;
    }

    @Override
    public void action() {
        System.out.println("(CoordinatorAgent) Action method of WaitingForNewPositionsBehaviour");
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();

        try {
            agent.newInfoAgent.clear();
        } catch (Exception e) {

        }

        int count = 0;
        while (done() == false) {
            ACLMessage response = myAgent.receive();
            if (response != null) {
                if ("ScoutCoordinator".equals(((AID) response.getSender()).getLocalName())) {
                    hasReplyFromSC = true;
                    count++;
                } else if ("HarvestCoordinator".equals(((AID) response.getSender()).getLocalName())) {
                    hasReplyFromHC = true;
                    count++;
                }

                ACLMessage reply = response.createReply();
                try {
                    // Sending an Agree..
                    if (count < 2) {
                        agent.setNewInfoAgent((ArrayList<InfoAgent>) response.getContentObject());
                    } else {
                        agent.addNewInfoAgent((ArrayList<InfoAgent>) response.getContentObject());
                    }

                    agent.log("Request received");
                    reply.setPerformative(ACLMessage.AGREE);
                    agent.send(reply);

                    // Sending an Inform..
                    ACLMessage reply2 = response.createReply();
                    reply2.setPerformative(ACLMessage.INFORM);

                    try {
                        agent.log("New positions have been saved");
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

    public void moveAgents() {
        InfoAgent info;
        int i, j, g, ri, rj, imax, jmax;

        Cell[][] mapa;
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();

        mapa = agent.getGame().getMap();
        imax = mapa.length;
        jmax = mapa[0].length;
        Map listOfAgents = agent.getGame().getAgentList();
        HashMap newListOfAgents; // We need to create another list of agents and update it.
        newListOfAgents = new HashMap<AgentType, List<Cell>>();
        List<Cell> newCellsSC; // The list of agents is a map of AgentType and list of cells.
        List<Cell> newCellsH; // The list of agents is a map of AgentType and list of cells.

        newCellsSC = new ArrayList<Cell>(); // For every agentType we change the list of cells.
        newCellsH = new ArrayList<Cell>(); // For every agentType we change the list of cells.
        int cont = 1;
        for (InfoAgent ag : agent.newInfoAgent) {
            i = ag.getPreRow();
            j = ag.getPreColumn();
            if (mapa[i][j] instanceof StreetCell) {
                ri = ag.getRow();
                rj = ag.getColumn();
                info = ((StreetCell) mapa[i][j]).getAgent();

                // If there is an Agent in the future cell we do not move the agent.
                if (((StreetCell) mapa[ri][rj]).isThereAnAgent()) {
                    // Modifying the new list of agents but with the same position.
                    if (ag.getType().equals(AgentType.SCOUT)) {
                        newCellsSC.add(mapa[i][j]);
                    } else if (ag.getType().equals(AgentType.HARVESTER)) {
                        newCellsH.add(mapa[i][j]);
                    }
                } else {
                    ((StreetCell) mapa[i][j]).removeAgentWithAID(ag.getAID());

                    try {
                        ((StreetCell) mapa[ri][rj]).addAgent(ag);
                    } catch (Exception e) {

                    }

                    if (ag.getType().equals(AgentType.SCOUT)) {
                        newCellsSC.add(mapa[ri][rj]);
                    } else if (ag.getType().equals(AgentType.HARVESTER)) {
                        newCellsH.add(mapa[ri][rj]);
                    }
                }
            }
        }

        newListOfAgents.put(AgentType.SCOUT, newCellsSC);
        newListOfAgents.put(AgentType.HARVESTER, newCellsH);
        agent.getGame().setAgentList(newListOfAgents);
    }

    public boolean done() {
        if (hasReplyFromHC == true && hasReplyFromSC == true) {
            return true;
        } else {
            return false;
        }

    }

    public int onEnd() {
        hasReplyFromHC = false;
        hasReplyFromSC = false;
        return 0;
    }
}
