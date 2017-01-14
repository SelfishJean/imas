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

/**
 * A request-responder behaviour for System agent, answering to queries
 * from the Coordinator agent. The Coordinator Agent sends a REQUEST of the whole
 * game information and the System Agent sends an AGREE and then an INFORM
 * with the city information.
 */
public class RequestResponseBehaviour extends SimpleBehaviour 
{
    boolean hasReply;
    
    public RequestResponseBehaviour(Agent agent) 
    {
        super(agent);
        hasReply = false;
        //agent.log("Waiting REQUESTs of the map from authorized agents");
        System.out.println("(SystemAgent) Waiting REQUESTs of the map from authorized agents");
    }

    @Override
    public void action() 
    { 
        System.out.println("Starting RequestResponseBehaviour");
        //boolean communicationOK = false;
        while(done() == false) 
        {
            ACLMessage response = myAgent.receive();
            //System.out.println(response.getPerformative());
            if(response != null && response.getPerformative() == ACLMessage.REQUEST) 
            {
                hasReply = true;
                //SystemAgent agent = (SystemAgent)myAgent;
                SystemAgent agent = (SystemAgent)this.getAgent();
                ACLMessage reply = response.createReply();
                try 
                {
                    Object content = (Object) response.getContent();
                    if (content.equals(MessageContent.GET_MAP)) 
                    {
                        agent.log("Request received from " + ((AID) response.getSender()).getLocalName());
                        // Sending an Agree..
                        reply.setPerformative(ACLMessage.AGREE);
                        agent.log("Sending Agreement");
                        agent.send(reply);
                        
                        // Sending the Map..
                        ACLMessage reply2 = response.createReply();
                        reply2.setPerformative(ACLMessage.INFORM);

                        try {
                                reply2.setContentObject(agent.getGame());
                        } catch (Exception e) {
                                reply2.setPerformative(ACLMessage.FAILURE);
                                System.err.println(e.toString());
                                e.printStackTrace();
                        }
                        agent.log("Sending Map Setings");
                        agent.send(reply2);
                    }
                } 
                catch (Exception e) 
                {
                    reply.setPerformative(ACLMessage.FAILURE);
                    agent.errorLog(e.getMessage());
                    e.printStackTrace();
                }
                //agent.log("Response being prepared");
                //agent.send(reply);
                
                //hasReply = true;
            }
        }
    }
    
    @Override
    public boolean done() 
    {
        return hasReply;
    }
    
    
    public int onEnd() 
    {
        //System.out.println("End of RequestResponseBehaviour");
        hasReply = false;
        return 0;
    }
    
}
