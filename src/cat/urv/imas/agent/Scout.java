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

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.behaviour.scoutAgent.*;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.InfoDiscovery;
import jade.core.*;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Scout extends ImasAgent {

    /**
     * Game settings in use.
     */
    public GameSettings game;
    /**
     * System agent id.
     */
    public AID scoutCoordinator;
    /**
     * Own InfoAgent.
     */
    public InfoAgent info;
    /**
     * InfoDiscovery. It contains all new discoveries of a turn.
     */
    public ArrayList<InfoDiscovery> newInfoDiscoveriesList;
    
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
     * Gets the info for new discoveries in this turn.
     *
     * @return info ALL discoveries (is a list). It has to be an ArrayList because List is not serializable.
     */
    public ArrayList<InfoDiscovery> getNewInfoDiscoveriesList() {
        return this.newInfoDiscoveriesList;
    }
    
    /**
     * Update value of the discoveries of this turn.
     *
     * @param newInfo information about new discoveries discovered in this turn.
     */
    public void setNewInfoDiscoveriesList(ArrayList<InfoDiscovery> newInfo) {
        try {
            this.newInfoDiscoveriesList.clear();
        } catch (Exception e) {
            
        }
        this.newInfoDiscoveriesList = newInfo;
    }

    /**
     * Builds the coordinator agent.
     */
    public Scout() {
        super(AgentType.SCOUT);
    }
    
    /**
     * Updates the InfoAgent of this scout.
     */
    public void updateInfoAgent() {
        Map listOfAgents = this.getGame().getAgentList();
        List<Cell> positions = (List<Cell>) listOfAgents.get(AgentType.SCOUT);
        for (Cell c : positions)
        {
            StreetCell temp = (StreetCell) c;
            if (temp.getAgent().getAID().equals(this.getAID())) 
                this.info = temp.getAgent();
        }
    }
    
    /**
     * Gets the current info of the agent.
     *
     * @return the last updated info of the agent in the current game.
     */
    public InfoAgent getInfoAgent() {
        updateInfoAgent();
        return this.info;
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
        sd1.setType(AgentType.SCOUT.toString());
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

        // search ScoutCoordinator
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SCOUT_COORDINATOR.toString());
        this.scoutCoordinator = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID

        // Finite State Machine
        
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                            System.out.println("(ScoutAgent) FSM behaviour completed.");
                            myAgent.doDelete();
                            return super.onEnd();
                    }
        };
        
        fsm.registerFirstState(new WaitingForMapBehaviour(this), "STATE_1");
        fsm.registerState(new ExploreSurroundingCellsBehaviour(this), "STATE_2");
        fsm.registerState(new SendingNewDiscoveriesBehaviour(this), "STATE_3");
        fsm.registerState(new WaitingForMapBehaviour(this), "STATE_4");
        
        fsm.registerDefaultTransition("STATE_1", "STATE_2");
        fsm.registerDefaultTransition("STATE_2", "STATE_3");
        fsm.registerDefaultTransition("STATE_3", "STATE_4", new String[] {"STATE_4"});
        fsm.registerDefaultTransition("STATE_4", "STATE_2");
        
        this.addBehaviour(fsm);
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
    }
}