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

import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.behaviour.system.*;
import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.agent.ScoutCoordinator;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import java.util.List;
import java.util.Map;
import jade.core.Agent;
import jade.domain.FIPANames;
import java.util.ArrayList;

/**
 * A request-responder behaviour for System agent, answering to queries
 * from the Coordinator agent. The Coordinator Agent sends a REQUEST of the whole
 * game information and the System Agent sends an AGREE and then an INFORM
 * with the city information.
 */
public class SendingMapBehaviour extends SimpleBehaviour 
{
    private ACLMessage msg;
    boolean hasReply, first;
    ArrayList<InfoAgent> allScouts;
    
    public SendingMapBehaviour(Agent agent) 
    {
        super(agent);
        hasReply = false;
        first = true;
        allScouts = new ArrayList<InfoAgent>();
        //agent.log("Waiting REQUESTs of the map from authorized agents");
        System.out.println("(ScoutCoordinator) Behaviour to send maps to Scouts");
    }

    @Override
    public void action() 
    { 
        System.out.println("(ScoutCoordinator) Starting SendingMapBehaviour");
        ScoutCoordinator agent = (ScoutCoordinator) this.getAgent();
        if (first)
        {
            // Scouts list initialization
            agent.initScouts(); 
            first = false;
            allScouts = agent.getListScouts();
        }
        
        // We set the value of the message. IT IS NECESSARY TO BE HERE BECAUSE THE INFO WE SEND CHANGES EVERY TURN
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.clearAllReceiver();
        
        for (int i = 0; i < allScouts.size(); i++)
        {
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
    public boolean done() 
    {
        return hasReply;
    }
    
    
    public int onEnd() 
    {
        //System.out.println("End of SendingMapBehaviour");
        hasReply = false;
        return 0;
    }
    
}
