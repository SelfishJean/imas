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
package cat.urv.imas.behaviour.scoutCoordinator;

import cat.urv.imas.agent.ScoutCoordinator;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import cat.urv.imas.onthology.InfoAgent;
import jade.core.Agent;
import java.util.ArrayList;

public class SendingMapBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply, first;
    ArrayList<InfoAgent> allScouts;

    public SendingMapBehaviour(Agent agent) {
        super(agent);
        hasReply = false;
        first = true;
        allScouts = new ArrayList<InfoAgent>();
    }

    @Override
    public void action() {
        ScoutCoordinator agent = (ScoutCoordinator) this.getAgent();
        if (first) {
            // Scouts list initialization
            agent.initScouts();
            first = false;
            allScouts = agent.getListScouts();
        }

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.clearAllReceiver();
        for (int i = 0; i < allScouts.size(); i++) {
            message.addReceiver(allScouts.get(i).getAID());
        }

        try {
            message.setContentObject(agent.getGame());
            agent.log("Sending new map to all Scouts");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.msg = message;
        hasReply = false;
        myAgent.send(msg);
        hasReply = true;
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
