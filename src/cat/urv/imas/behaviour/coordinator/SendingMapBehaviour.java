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

import cat.urv.imas.behaviour.system.*;
import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.CoordinatorAgent;
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
public class SendingMapBehaviour extends SimpleBehaviour 
{
    private ACLMessage msg;
    boolean hasReply;
    
    public SendingMapBehaviour(Agent agent) 
    {
        super(agent);
        hasReply = false;
        //agent.log("Waiting REQUESTs of the map from authorized agents");
        System.out.println("(CoordinatorAgent) Behaviour to send maps to Coordinators");
    }

    @Override
    public void action() 
    { 
        System.out.println("(CoordinatorAgent) Starting SendingMapBehaviour");
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        int count = 0;
        
        // We set the value of the message. IT IS NECESSARY TO BE HERE BECAUSE THE INFO WE SEND CHANGES EVERY TURN
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.clearAllReceiver();
        message.addReceiver(agent.scoutCoordinator);
        message.addReceiver(agent.harvesterCoordinator);
        
        try {
            
            message.setContentObject(agent.getGame());
            agent.log("Sending new map to Coordinators");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.msg = message;

        hasReply = false;
        myAgent.send(msg);
        hasReply = true;
        /*
        while(done() == false) 
        {
            ACLMessage response = myAgent.receive();

            if(response != null) 
            {
                //System.out.println(response.getPerformative());
                //CoordinatorAgent agent = (CoordinatorAgent) myAgent;
                //CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
                switch(response.getPerformative()) 
                {
                    case ACLMessage.AGREE:
                        agent.log("AGREE received from " + ((AID) response.getSender()).getLocalName());
                        if (count == 0)
                            count++;
                        else
                            hasReply = true;
                        break;
                    case ACLMessage.FAILURE:
                        agent.log("The action has failed.");
                        break;
                    default:
                        agent.log("Failed to process the message");
                        break;
                }

                
            }
        }*/
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
