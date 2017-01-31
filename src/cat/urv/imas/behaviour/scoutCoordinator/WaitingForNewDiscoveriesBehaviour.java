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
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import jade.core.Agent;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.InfoDiscovery;
import java.util.ArrayList;

/**
 * A request-responder behaviour for System agent, answering to queries
 * from the Coordinator agent. The Coordinator Agent sends a REQUEST of the whole
 * game information and the System Agent sends an AGREE and then an INFORM
 * with the city information.
 */
public class WaitingForNewDiscoveriesBehaviour extends SimpleBehaviour 
{
    int count;
    ArrayList<InfoAgent> allScouts;
    
    
    public WaitingForNewDiscoveriesBehaviour(Agent agent) //It cannot be SystemAgent type
    {
        super(agent);
        ScoutCoordinator a = (ScoutCoordinator)this.getAgent();
        count = 0;
        allScouts = new ArrayList<InfoAgent>();
        
        //agent.log("Waiting REQUESTs of the map from authorized agents");
        System.out.println("(ScoutCoordinator) Waiting REQUESTs of the map from authorized agents");
    }

    @Override
    public void action() 
    { 
        System.out.println("(ScoutCoordinator) Action method of WaitingForNewDiscoveriesBehaviour");
        ScoutCoordinator agent = (ScoutCoordinator)this.getAgent();
        allScouts = agent.getListScouts();
        //hasReply = false;
        //boolean communicationOK = false;
        try {
            agent.newInfoAgent.clear();
        } catch (Exception e) {
            
        }

        while(done() == false) 
        {
            ACLMessage response = myAgent.receive();
            //System.out.println(response.getPerformative());

            if(response != null) 
            {
                
                switch(response.getPerformative()) 
                {
                    case ACLMessage.INFORM:
                        agent.log("INFORM (new discoveries) received from " + ((AID) response.getSender()).getLocalName());
                        
                        try 
                        {
                            //System.out.println("-------------before");
                            ArrayList<InfoDiscovery> newDiscoveries = (ArrayList<InfoDiscovery>) response.getContentObject();
                            /*try {
                                System.out.println("\n\nSC*_*_*_*_*_*_*_*_*_*_*_*_*_*"+newDiscoveries.iterator().next().getGarbage());
                            } catch (Exception e){
                                
                            }*/
                            if (count < 1)
                                agent.setNewInfoDiscoveriesList(newDiscoveries);
                            else 
                                agent.addNewInfoDiscoveriesList(newDiscoveries);
                            //System.out.println("-------------after");
                            agent.log("New discoveries saved");
                            
                            count++;
                        } 
                        catch (Exception e) 
                        {
                            agent.errorLog("Incorrect content: " + e.toString());
                        }
                        break;
                    case ACLMessage.FAILURE:
                        agent.log("The action has failed.");
                        break;
                    default:
                        agent.log("Failed to process the message");
                        break;
                }
            }
            
        }
    }
    
    
    
    public boolean done() 
    {
        if (count < allScouts.size())
            return false;
        else
            return true;
        
    }
    
    
    public int onEnd() 
    {
        //System.out.println("End of RequestForNewSimulationStep");
        count = 0;
        return 0;
    }
    
}
