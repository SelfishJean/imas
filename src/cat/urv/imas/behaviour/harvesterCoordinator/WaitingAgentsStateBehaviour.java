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
import cat.urv.imas.map.BuildingCell;
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
        HarvestCoordinator a = (HarvestCoordinator) this.getAgent();
        a.initializeOngoingGoals();
        System.out.println("(HarvesterCoordinator) Waiting REQUESTs sending states from authorized agents");
    }

    @Override
    public void action() 
    { 
        HarvestCoordinator agent = (HarvestCoordinator) this.getAgent();
        agent.log("Starting WaitingAgentsStateBehaviour");
        agent.initializeCellsCollectedGarbage();
        //boolean communicationOK = false;
        
        Map<AID, Cell> onGoingGoals = agent.getOngoingGoals();
        for (Map.Entry<AID, Cell> entry : onGoingGoals.entrySet()) 
        {
            if(entry.getValue().getCellType() == CellType.BUILDING)
            {
                BuildingCell temp = (BuildingCell) entry.getValue();
                if(temp.detectGarbage().isEmpty())
                {
                    onGoingGoals.remove(entry);
                }
            }
        }
        
        
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
                                        /* HERE WE HAVE TWO OPTIONS: THERE ARE GOALS OR NOT.

                                        IF THERE ARE GOALS...RUN A FUNCTION TO CHOOSE ONE AND SEND A CELL AS AN OBJECT
                                            reply2.setContentObject(CELL WHIT GARBAGE);
                                        IF THERE ARE NOT GOALS...SEND A MESSAGE INFORMING ABOUT IT
                                            reply2.setContent(MessageContent.NO_GOAL);

                                        For the time being, in order to keep it as simple as possible, we 
                                        are going to use just NO_GOAL and we will choose randomly in the other side 
                                        to run StartingGoalBehaviour or ChillingBehaviour.
                                        */

                                        /*try { 
                                            boolean condition;
                                            condition = agent.newInfoDiscoveriesList.isEmpty();
                                            System.out.println("WaitingStates_______________________BeforeAdding"+agent.newInfoDiscoveriesList.size());
                                            if (!condition) { // Even though we have initialized it in the previous step, it could be empty (No discoveries for previous scout)
                                                System.out.println("WaitingStates_______________________AfterAdding"+agent.newInfoDiscoveriesList.size());
                                            }
                                            else
                                            {
                                                System.out.println("WaitingStates_________________________EMPTY DISCOVERIES");
                                            }
                                        }catch (Exception e) {

                                        }*/

                                        try { 
                                            boolean condition;
                                            condition = agent.newInfoDiscoveriesList.isEmpty();
                                            AID sender = (AID) response.getSender();
                                            if (!condition) { 
                                                Cell c;
                                                // modified**
                                                AID harvester = response.getSender();
                                                
                                                Cell[] result = agent.assignGoal(harvester);
                                                c = result[0];
                                                Cell c2 = result[1];
                                                
                                                // carefull, Alex
                                                Map<AID, Cell> currentGoals = agent.getOngoingGoals();
                                                if (!currentGoals.containsKey(sender))
                                                {
                                                    agent.addOngoingGoals(sender, c2); // it is already initialized in constructor.
                                                }
                                                else
                                                {
                                                    System.out.println("_________________________________WaitingAgentsState   Sender has already a goal"+sender.getLocalName());
                                                }
                                                
                                                reply2.setContentObject(result);
                                                //System.out.println("WaitingStates_______________________THERE ARE DISCOVERIES"+agent.newInfoDiscoveriesList.size());
                                            }
                                            else
                                            {
                                                reply2.setContent(MessageContent.NO_GOAL);
                                                //System.out.println("WaitingStates_________________________EMPTY DISCOVERIES");
                                            }
                                        }catch (Exception e) {

                                        }
                                        break;
                                    case MessageContent.MOVING:
                                        agent.log(content.toString());
                                        reply2.setContent(MessageContent.OK);
                                        break;
                                    case MessageContent.COLLECTING:
                                        AID sender = (AID) response.getSender();
                                        Map<AID, Cell> currentGoals = agent.getOngoingGoals();
                                        if (currentGoals.containsKey(sender)) 
                                        {
                                            System.out.println("_________________________________WaitingAgentsState   Cell" + currentGoals.get(sender));
                                            agent.addCellsCollectedGarbage(currentGoals.get(sender)); // already initialized (beginning action method)
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
