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
package cat.urv.imas.behaviour.scoutAgent;

import cat.urv.imas.behaviour.scoutCoordinator.*;
import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.behaviour.system.*;
import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.agent.Scout;
import cat.urv.imas.agent.ScoutCoordinator;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import java.util.List;
import java.util.Map;
import jade.core.Agent;
import jade.domain.FIPANames;

/**
 * A request-responder behaviour for System agent, answering to queries
 * from the Coordinator agent. The Coordinator Agent sends a REQUEST of the whole
 * game information and the System Agent sends an AGREE and then an INFORM
 * with the city information.
 */
public class SendingNewDiscoveriesBehaviour extends SimpleBehaviour 
{
    private ACLMessage msg;
    boolean hasReply;
    
    public SendingNewDiscoveriesBehaviour(Agent agent) 
    {
        super(agent);
        hasReply = false;
        //agent.log("Waiting REQUESTs of the map from authorized agents");
        System.out.println("(ScoutAgent) Behaviour to send discoveries to Coordinator");
    }

    @Override
    public void action() 
    { 
        System.out.println("(ScoutAgent) Starting SendingNewDiscoveriesBehaviour");
        Scout agent = (Scout) this.getAgent();
        
        // We set the value of the message. IT IS NECESSARY TO BE HERE BECAUSE THE INFO WE SEND CHANGES EVERY TURN
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.clearAllReceiver();
        message.addReceiver(agent.scoutCoordinator);
        
        try {
            
            message.setContentObject(agent.getNewInfoDiscoveriesList());
            agent.log("Sending new discoveries");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.msg = message;

        hasReply = false;
        myAgent.send(msg);
        hasReply = true;

    }
    
    @Override
    public boolean done() 
    {
        return hasReply;
    }
    
    
    @Override
    public int onEnd() 
    {
        hasReply = false;
        return 0;
    }
    
}
