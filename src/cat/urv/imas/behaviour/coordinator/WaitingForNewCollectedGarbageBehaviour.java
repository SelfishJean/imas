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

import cat.urv.imas.agent.CoordinatorAgent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.MessageContent;
import jade.core.Agent;
import cat.urv.imas.onthology.InfoDiscovery;
import cat.urv.imas.onthology.InfoMapChanges;
import java.util.ArrayList;

/**
 * A request-responder behaviour for System agent, answering to queries from the
 * Coordinator agent. The Coordinator Agent sends a REQUEST of the whole game
 * information and the System Agent sends an AGREE and then an INFORM with the
 * city information.
 */
public class WaitingForNewCollectedGarbageBehaviour extends SimpleBehaviour {

    boolean hasReply;

    public WaitingForNewCollectedGarbageBehaviour(Agent agent) //It cannot be SystemAgent type
    {
        super(agent);
        hasReply = false;
    }

    @Override
    public void action() {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();

        while (done() == false) {
            ACLMessage response = myAgent.receive();

            if (response != null) {
                ACLMessage reply = response.createReply();
                try {
                    agent.log("Request received");
                    reply.setPerformative(ACLMessage.AGREE);
                    agent.send(reply);

                    // Sending an Inform..
                    ACLMessage reply2 = response.createReply();
                    reply2.setPerformative(ACLMessage.INFORM);

                    try {
                        NewChangesOnMap((ArrayList<Cell>) response.getContentObject());
                        agent.log("New collected garbage has been saved");
                        reply2.setContent(MessageContent.NEXT_STEP);
                        hasReply = true;
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

    public void NewChangesOnMap(ArrayList<Cell> newCollections) {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        ArrayList<InfoDiscovery> newDiscoveries = agent.getNewInfoDiscoveriesList();
        ArrayList<Cell> cellsNewDiscoveries;
        cellsNewDiscoveries = new ArrayList<Cell>();
        InfoMapChanges newChanges;
        newChanges = new InfoMapChanges();

        for (int i = 0; i < newDiscoveries.size(); i++) {
            InfoDiscovery temp = newDiscoveries.get(i);
            cellsNewDiscoveries.add(agent.game.getMap()[temp.getRow()][temp.getColumn()]);
        }
        newChanges.setFoundGarbage(cellsNewDiscoveries);
        newChanges.setCollectedGarbage(newCollections);
        agent.setNewChangesOnMap(newChanges);
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
