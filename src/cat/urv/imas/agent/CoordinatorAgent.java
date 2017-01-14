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
package cat.urv.imas.agent;

import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import java.util.List;
import java.util.ArrayList;

/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgent {

    /**
     * Game settings in use.
     */
    public GameSettings game;
    /**
     * System agent id.
     */
    public AID systemAgent;
    /**
     * InfoAgent. It contains the following information of all agents.
     */
    public ArrayList<InfoAgent> newInfoAgent;
    /**
     * isMapUpdated. If the map has been already updated, then we can send the new positions
     * (we need it because we need to know the new map to modify the previous position
     * of our agents in the newInfoAgent variable in order to delete the agents correctly).
     */
    public boolean updatedMap = false;
    /**
     * updatingMapBehaviour. It is going to be the behaviour that sends a request to 
     * update the map.
     */
    public Behaviour updatingMapBehaviour;
    
    public void setUpdatedMap(boolean temp) {
        this.updatedMap = temp;
    }
    
    public boolean getUpdatedMap() {
        return this.updatedMap;
    }
    
    /**
     * isMapUpdated. If the map has been already updated, then we can send the new positions
     * (we need it because we need to know the new map to modify the previous position
     * of our agents in the newInfoAgent variable in order to delete the agents correctly).
     */
    public boolean sentNewPositions = false;
    
    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.COORDINATOR.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }

        // search SystemAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        this.systemAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID

        /* ********************************************************************/
        

        //we add a behaviour that sends the message and waits for an answer
        
        
        ACLMessage NewStepRequest = new ACLMessage(ACLMessage.REQUEST);
        NewStepRequest.clearAllReceiver();
        NewStepRequest.addReceiver(this.systemAgent);
        NewStepRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        //log("Request message to agent");
        try {
            NewStepRequest.setContentObject(this.newInfoAgent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        //mapaBehaviour.restart();
        int i = 0;
        while (i<3) {
            if (i==1)
                this.addBehaviour(new UpdateMapBehaviour(this, NewStepRequest));
            else
                this.addBehaviour(new RequestMapBehaviour(this, initialRequest));
            i++;
        }
*/
        //we add a behaviour that sends the message and waits for an answer
        //this.addBehaviour(new UpdateMapBehaviour(this, NewStepRequest));

        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
        
        // Finite State Machine
        
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                            System.out.println("(CoordinatorAgent) FSM behaviour completed.");
                            myAgent.doDelete();
                            return super.onEnd();
                    }
        };
        
        fsm.registerFirstState(new WaitForMapBehaviour(this), "STATE_1");
        fsm.registerState(new GenerateNewPositionsBehaviour(this), "STATE_2");
        fsm.registerState(new WaitForNewSimulationStepBehaviour(this), "STATE_3");
        fsm.registerState(new WaitForMapBehaviour(this), "STATE_4");
        
        //fsm.registerState(new AuxiliarSimpleBehaviour(this), "STATE_3");
        
        //fsm.registerState(new NewPositions(this), "STATE_2");
        //fsm.registerState(new UpdateMapBehaviour(this), "STATE_3");
        
        fsm.registerDefaultTransition("STATE_1", "STATE_2");
        //fsm.registerDefaultTransition("STATE_2", "STATE_1", new String[] {"STATE_1"});
        fsm.registerDefaultTransition("STATE_2", "STATE_3");
        fsm.registerDefaultTransition("STATE_3", "STATE_4", new String[] {"STATE_4"});
        fsm.registerDefaultTransition("STATE_4", "STATE_2");
        //fsm.registerDefaultTransition("STATE_3", "STATE_1");
        //fsm.registerDefaultTransition("STATE_3", "STATE_2");
        //fsm.registerDefaultTransition("STATE_4", "STATE_2");

        this.addBehaviour(fsm);

    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }
    
    /**
     * Gets the next information for all agents.
     *
     * @return info ALL agentS (is a list). It has to be an ArrayList because List is not serializable.
     */
    public ArrayList<InfoAgent> getNewInfoAgent() {
        return this.newInfoAgent;
    }
    
    /**
     * Update agent information.
     *
     * @param newInfo information of all agents for next simulation step.
     */
    public void setNewInfoAgent(ArrayList<InfoAgent> newInfo) {
        try {
            this.newInfoAgent.clear();
        } catch (Exception e) {
            
        }
        this.newInfoAgent = newInfo;
    }

}
