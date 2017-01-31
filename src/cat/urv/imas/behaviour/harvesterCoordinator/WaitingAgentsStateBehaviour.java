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
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.InfoDiscovery;
import cat.urv.imas.onthology.InfoMapChanges;
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
        
        HarvestCoordinator a = (HarvestCoordinator) this.getAgent();
        hasReply = false;
        first = true;
        allHarvesters = new ArrayList<InfoAgent>();
        count = 0;
        a.initializeOngoingGoals();
        
        //agent.log("Waiting REQUESTs of the map from authorized agents");
        System.out.println("(HarvesterCoordinator) Waiting REQUESTs sending states from authorized agents");
    }

    @Override
    public void action() 
    { 
        HarvestCoordinator agent = (HarvestCoordinator) this.getAgent();
        agent.log("Starting WaitingAgentsStateBehaviour");
        agent.initializeCellsCollectedGarbage();
        //boolean communicationOK = false;
        
        if (first)
        {
            first = false;
            allHarvesters = agent.getListHarvesters();
        }
        
        while(done() == false) 
        {
            ACLMessage response = myAgent.receive();
            //System.out.println(response.getPerformative());
            if(response != null) 
            {
                switch(response.getPerformative())
                {
                    case ACLMessage.REQUEST:
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

                                        try { 
                                            boolean condition;
                                            condition = agent.newInfoDiscoveriesList.isEmpty();
                                            AID sender;
                                            sender = (AID) response.getSender();
                                            if (!condition) { 
                                                System.out.println("_________________________________WaitingAgentsState   DiscoveriesListSizeBeforeRemoving"+agent.newInfoDiscoveriesList.size());
                                                Cell c;
                                                
                                                InfoDiscovery info = agent.newInfoDiscoveriesList.get(0);
                                                agent.newInfoDiscoveriesList.remove(0); //This causes a problem, if we delete the goal if we find it again we will take it into account again.
                                                System.out.println("_________________________________WaitingAgentsState   DiscoveriesListSize"+agent.newInfoDiscoveriesList.size());
                                                
                                                c = agent.game.getMap()[info.getRow()][info.getColumn()];
                                                
                                                Map<AID, Cell> currentGoals = agent.getOngoingGoals();
                                                if (!currentGoals.containsKey(sender))
                                                {
                                                    agent.addOngoingGoals(sender, c); // it is already initialized in constructor.
                                                }
                                                else
                                                {
                                                    System.out.println("_________________________________WaitingAgentsState   Sender has already a goal"+sender.getLocalName());
                                                }
                                                
                                                
                                                reply2.setContentObject(c);
                                                //System.out.println("WaitingStates_______________________THERE ARE DISCOVERIES"+agent.newInfoDiscoveriesList.size());
                                            }
                                            else
                                            {
                                                reply2.setContent(MessageContent.NO_GOAL);
                                                System.out.println("WaitingStates_________________________EMPTY DISCOVERIES");
                                            }

                                            // The following part should be in the case MessageContent.COLLECTING:
                                            Map<AID, Cell> currentGoals = agent.getOngoingGoals();
                                            if (currentGoals.containsKey(sender))
                                            {
                                                System.out.println("_________________________________WaitingAgentsState   Cell"+currentGoals.get(sender));
                                                agent.addCellsCollectedGarbage(currentGoals.get(sender)); // already initialized (beginning action method)
                                            }
                                            
                                        }catch (Exception e) {

                                        }
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
                        break;
                        
                    default:
                        agent.log("Failed to process the message");
                        ACLMessage reply2 = response.createReply(); 
                        // Sending a Failure
                        reply2.setPerformative(ACLMessage.FAILURE);
                        agent.send(reply2);
                        break;
                    
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
