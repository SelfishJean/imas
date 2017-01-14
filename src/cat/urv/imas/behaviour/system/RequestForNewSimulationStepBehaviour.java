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
import java.util.HashMap;
import jade.core.Agent;
import cat.urv.imas.onthology.InfoAgent;
import java.util.ArrayList;

/**
 * A request-responder behaviour for System agent, answering to queries
 * from the Coordinator agent. The Coordinator Agent sends a REQUEST of the whole
 * game information and the System Agent sends an AGREE and then an INFORM
 * with the city information.
 */
public class RequestForNewSimulationStepBehaviour extends SimpleBehaviour 
{
    boolean hasReply;
    
    public RequestForNewSimulationStepBehaviour(Agent agent) //It cannot be SystemAgent type
    {
        super(agent);
        hasReply = false;
        //agent.log("Waiting REQUESTs of the map from authorized agents");
        System.out.println("(SystemAgent) Waiting REQUESTs of the map from authorized agents");
    }

    @Override
    public void action() 
    { 
        //hasReply = false;
        //boolean communicationOK = false;
        while(done() == false) 
        {
            ACLMessage response = myAgent.receive();
            //System.out.println(response.getPerformative());

            if(response != null) 
            {
                hasReply = true;
                //SystemAgent agent = (SystemAgent)myAgent;
                SystemAgent agent = (SystemAgent)this.getAgent();
                ACLMessage reply = response.createReply();
                try 
                {
                    // Sending an Agree..
                    agent.setNewInfoAgent((ArrayList<InfoAgent>)response.getContentObject());
                    agent.log("Request received");
                    reply.setPerformative(ACLMessage.AGREE);
                    agent.send(reply);

                    // Sending an Inform..
                    ACLMessage reply2 = response.createReply();
                    reply2.setPerformative(ACLMessage.INFORM);

                    try {
                        moveAgents();
                        agent.log("Map updated");
                        reply2.setContent(MessageContent.NEXT_STEP);
                    } catch (Exception e) {
                        reply2.setPerformative(ACLMessage.FAILURE);
                        agent.errorLog(e.toString());
                        e.printStackTrace();
                    }
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

     public void moveAgents() {
        InfoAgent info;
        int i,j, ri, rj, imax, jmax;

        Cell [][] mapa;
        SystemAgent agent = (SystemAgent)this.getAgent();
        
        mapa = agent.getGame().getMap();
        imax = mapa.length;
        jmax = mapa[0].length;
        Map listOfAgents = agent.getGame().getAgentList();
        HashMap newListOfAgents; // We need to create another list of agents and update it.
        newListOfAgents = new HashMap<AgentType, List<Cell>>();
        List<Cell> newCellsSC; // The list of agents is a map of AgentType and list of cells.
        List<Cell> newCellsH; // The list of agents is a map of AgentType and list of cells.
 
        newCellsSC = new ArrayList<Cell>(); // For every agentType we change the list of cells.
        newCellsH = new ArrayList<Cell>(); // For every agentType we change the list of cells.
        
        for (InfoAgent ag : agent.newInfoAgent) {
            i = ag.getPreRow();
            j = ag.getPreColumn();
            if (mapa[i][j] instanceof StreetCell) {
                info = ((StreetCell)mapa[i][j]).getAgent();
                ((StreetCell)mapa[i][j]).removeAgentWithAID(ag.getAID());
                ri = ag.getRow();
                rj = ag.getColumn();
                try {
                    ((StreetCell)mapa[ri][rj]).addAgent(ag);
                }catch(Exception e){
                //System.err.println(e);
                }
                
                if (ag.getType().equals(AgentType.SCOUT)) {
                    newCellsSC.add(mapa[ri][rj]);
                }else {
                    newCellsH.add(mapa[ri][rj]);
                }  
            }
        }
        newListOfAgents.put(AgentType.SCOUT, newCellsSC);
        newListOfAgents.put(AgentType.HARVESTER, newCellsH);

        agent.getGame().setAgentList(newListOfAgents);
    }
    
    
    @Override
    public boolean done() 
    {
        return hasReply;
    }
    
    
    public int onEnd() 
    {
        //System.out.println("End of RequestForNewSimulationStep");
        hasReply = false;
        return 0;
    }
    
}
