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

import cat.urv.imas.behaviour.system.*;
import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.agent.HarvestCoordinator;
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
import java.util.ArrayList;

/**
 * A request-responder behaviour for System agent, answering to queries
 * from the Coordinator agent. The Coordinator Agent sends a REQUEST of the whole
 * game information and the System Agent sends an AGREE and then an INFORM
 * with the city information.
 */
public class WaitingAgentsStateBehaviour extends SimpleBehaviour 
{
    boolean hasReply,first;
    ArrayList<InfoAgent> allHarvesters;
    int count;
    
    public WaitingAgentsStateBehaviour(Agent agent) 
    {
        super(agent);
        hasReply = false;
        first = true;
        allHarvesters = new ArrayList<InfoAgent>();
        count = 0;
        //agent.log("Waiting REQUESTs of the map from authorized agents");
        System.out.println("(HarvesterCoordinator) Waiting REQUESTs sending states from authorized agents");
    }

    @Override
    public void action() 
    { 
        HarvestCoordinator agent = (HarvestCoordinator) this.getAgent();
        agent.log("Starting WaitingAgentsStateBehaviour");
        //boolean communicationOK = false;
        
        if (first)
        {
            // Scouts list initialization
            //agent.initHarvesters(); 
            first = false;
            allHarvesters = agent.getListHarvesters();
        }
        
        while(done() == false) 
        {
            ACLMessage response = myAgent.receive();
            //System.out.println(response.getPerformative());
            if(response != null && response.getPerformative() == ACLMessage.REQUEST) 
            {
                hasReply = true;
                ACLMessage reply = response.createReply();
                try 
                {
                    Object content = (Object) response.getContent();
                    
                    agent.log("Request received from " + ((AID) response.getSender()).getLocalName());
                    // Sending an Agree..
                    reply.setPerformative(ACLMessage.AGREE);
                    agent.log("Sending Agreement");
                    agent.send(reply);

                    // Sending an Inform..
                    ACLMessage reply2 = response.createReply();
                    reply2.setPerformative(ACLMessage.INFORM);
                    try 
                    {
                        switch (content.toString())
                        {
                            case MessageContent.FREE:
                                agent.log(content.toString());
                                /* HERE WE HAVE TWO OPTIONS: THERE ARE GOALS OR NOT.

                                IF THERE ARE GOALS...RUN A FUNCTION TO CHOOSE ONE AND SEND A CELL AS AN OBJECT
                                    reply2.setContentObject(CELL WHIT GARBAGE);
                                IF THERE ARE NOT GOALS...SEND A MESSAGE INFORMING ABOUT IT
                                    reply2.setContent(MessageContent.NO_GOAL);
                                
                                For the time being, in order to keep it as simple as possible, we 
                                are going to use just NO_GOAL and we will choose randomly in the other side 
                                to run StartingGoalBehaviour or ChillingBehaviour.
                                */
                                reply2.setContent(MessageContent.NO_GOAL);
                                break;
                            case MessageContent.MOVING:
                                agent.log(content.toString());
                                reply2.setContent(MessageContent.OK);
                                break;
                            case MessageContent.COLLECTING:
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
        if (count < allHarvesters.size())
            return false;
        else
            return true;
    }
    
    
    public int onEnd() 
    {
        //System.out.println("End of RequestResponseBehaviour");
        hasReply = false;
        count = 0;
        return 0;
    }
    
}
