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
import cat.urv.imas.behaviour.harvesterCoordinator.*;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.InfoDiscovery;
import jade.core.*;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HarvestCoordinator extends ImasAgent {

    /**
     * Game settings in use.
     */
    public GameSettings game;
    /**
     * Coordinator agent id.
     */
    public AID coordinatorAgent;
    /**
     * NewInfoAgent. It contains the following information of all agents.
     */
    public ArrayList<InfoAgent> newInfoAgent;
    /**
     * ListOfHarvesters. It contains all information related to Harvester
     * Agents.
     */
    public ArrayList<InfoAgent> harvesters;
    /**
     * InfoDiscovery. It contains all new discoveries of a turn.
     */
    public ArrayList<InfoDiscovery> newInfoDiscoveriesList;

    /**
     * OngoingGoals. All goals our harvester are doing. Once we have assigned a
     * goal we save it in this variable (we consider the trip until the building
     * as part of the goal).
     */
    public Map<AID, Cell> ongoingGoals;
    /**
     * CellsCollectedGarbage. All cells in which our harvesters have collected
     * garbage in this turn.
     */
    public ArrayList<Cell> cellsCollectedGarbage;

    public void setGame(GameSettings game) {
        this.game = game;
    }

    public GameSettings getGame() {
        return this.game;
    }

    /**
     * Gets the next information for all agents.
     *
     * @return info ALL agentS (is a list). It has to be an ArrayList because
     * List is not serializable.
     */
    public ArrayList<InfoAgent> getNewInfoAgent() {
        return this.newInfoAgent;
    }

    public void setNewInfoAgent(InfoAgent newInfo) {
        try {
            this.newInfoAgent.clear();
        } catch (Exception e) {

        }
        // We initialize the variable
        this.newInfoAgent = new ArrayList<InfoAgent>();
        this.newInfoAgent.add(newInfo);
    }

    /**
     * Update value of the positions of this turn.
     *
     * @param newInfo information about new discoveries discovered in this turn.
     */
    public void addNewInfoAgent(InfoAgent newInfo) {
        try {
            boolean condition;
            condition = this.newInfoAgent.isEmpty();
            if (!condition) {
                this.newInfoAgent.add(newInfo);
            } else {
                this.newInfoAgent.add(newInfo);
            }
        } catch (Exception e) {

        }
    }

    /**
     * Gets the info for new discoveries in this turn.
     *
     * @return info ALL discoveries (is a list). It has to be an ArrayList
     * because List is not serializable.
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
        this.newInfoDiscoveriesList = newInfo;
    }

    /**
     * Update value of the discoveries of this turn.
     *
     * @param newInfo information about new discoveries discovered in this turn.
     */
    public void addNewInfoDiscoveriesList(ArrayList<InfoDiscovery> newInfo) {
        try {
            boolean condition;
            condition = this.newInfoDiscoveriesList.isEmpty();

            if (!condition) {
                for (int g = 0; g < newInfo.size(); g++) {
                    ArrayList<InfoDiscovery> tempList;
                    tempList = (ArrayList<InfoDiscovery>) this.newInfoDiscoveriesList.clone();
                    boolean add = true;
                    for (int y = 0; y < tempList.size(); y++) {
                        InfoDiscovery temp;
                        temp = tempList.get(y);
                        if (temp.getColumn() == newInfo.get(g).getColumn() && temp.getRow() == newInfo.get(g).getRow()) {
                            add = false;
                            break;
                        }
                    }

                    if (add == true) {
                        this.log("Heap size:" + this.newInfoDiscoveriesList.size()
                                + "; garbage:" + newInfo.get(g).getGarbage().toString()
                                + newInfo.get(g).getRow() + " " + newInfo.get(g).getColumn());
                        this.newInfoDiscoveriesList.add(newInfo.get(g));
                    }
                }
            } else {
                this.newInfoDiscoveriesList = newInfo;
            }
        } catch (Exception e) {

        }
    }

    /**
     * Initialization of ongoingGoals.
     *
     * @param temp new assigned goal.
     */
    public void initializeOngoingGoals() {
        this.ongoingGoals = new HashMap<AID, Cell>() {
        };
    }

    /**
     * Adding new ongoingGoals.
     *
     * @param temp new assigned goal.
     */
    public void addOngoingGoals(AID agent, Cell temp) {
        this.ongoingGoals.put(agent, temp);
    }

    /**
     * Gets the current ongoingGoals.
     *
     * @return the current ongoingGoals.
     */
    public Map<AID, Cell> getOngoingGoals() {
        return this.ongoingGoals;
    }

    /**
     * Initialization of cellsCollectedGarbage.
     *
     * @param temp new assigned goal.
     */
    public void initializeCellsCollectedGarbage() {
        this.cellsCollectedGarbage = new ArrayList<Cell>();
    }

    /**
     * Adding new ongoingGoals.
     *
     * @param temp new assigned goal.
     */
    public void addCellsCollectedGarbage(Cell temp) {
        this.cellsCollectedGarbage.add(temp);
    }

    /**
     * Gets the current ongoingGoals.
     *
     * @return the current ongoingGoals.
     */
    public ArrayList<Cell> getCellsCollectedGarbage() {
        return this.cellsCollectedGarbage;
    }

    /**
     * Builds the coordinator agent.
     */
    public HarvestCoordinator() {
        super(AgentType.HARVESTER_COORDINATOR);
    }

    /**
     * Gets information for all harvesters.
     *
     * @return info ALL agentS (is a list). It has to be an ArrayList because
     * List is not serializable.
     */
    public ArrayList<InfoAgent> getListHarvesters() {
        return this.harvesters;
    }

    /**
     * Set the list of scouts.
     *
     * @param .
     */
    public void setListHarvesters(ArrayList<InfoAgent> hv) {
        try {
            this.harvesters.clear();
        } catch (Exception e) {

        }
        this.harvesters = hv;
    }

    /**
     * Builds a list of all HarvesterAgents.
     */
    public void initHarvesters() {
        Map listOfAgents = this.getGame().getAgentList();
        List<Cell> positions = (List<Cell>) listOfAgents.get(AgentType.HARVESTER);
        ArrayList<InfoAgent> allHarvesters;
        allHarvesters = new ArrayList<InfoAgent>();

        for (Cell pos : positions) {
            StreetCell temp = (StreetCell) pos;

            if (temp.isThereAnAgent() == true) {
                allHarvesters.add(temp.getAgent());
            }
        }
        this.setListHarvesters(allHarvesters);
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
        sd1.setType(AgentType.HARVESTER_COORDINATOR.toString());
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

        // search CoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);

        // Finite State Machine
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                System.out.println("(ScoutCoordinator) FSM behaviour completed.");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        fsm.registerFirstState(new WaitingForMapBehaviour(this), "STATE_1");
        fsm.registerState(new SendingMapBehaviour(this), "STATE_2");
        fsm.registerState(new WaitingForNewDiscoveriesBehaviour(this), "STATE_3");
        fsm.registerState(new WaitingAgentsStateBehaviour(this), "STATE_4");
        fsm.registerState(new WaitingForNewPositionsBehaviour(this), "STATE_5");
        fsm.registerState(new SendingNewPositionsBehaviour(this), "STATE_6");
        fsm.registerState(new SendingNewCollectedGarbageBehaviour(this), "STATE_7");
        fsm.registerFirstState(new WaitingForMapBehaviour(this), "STATE_8");

        fsm.registerDefaultTransition("STATE_1", "STATE_2");
        fsm.registerDefaultTransition("STATE_2", "STATE_3");
        fsm.registerDefaultTransition("STATE_3", "STATE_4");
        fsm.registerDefaultTransition("STATE_4", "STATE_5");
        fsm.registerDefaultTransition("STATE_5", "STATE_6");
        fsm.registerDefaultTransition("STATE_6", "STATE_7");
        fsm.registerDefaultTransition("STATE_7", "STATE_8", new String[]{"STATE_8"});
        fsm.registerDefaultTransition("STATE_8", "STATE_2");

        this.addBehaviour(fsm);
    }

    public Cell getCollectingCell(InfoAgent harvester, InfoDiscovery building) {
        Cell[][] map = game.getMap();
        if (harvester.getRow() > building.getRow()
                && map[building.getRow() + 1][building.getColumn()].getCellType() == CellType.STREET) {
            return map[building.getRow() + 1][building.getColumn()];
        } else if ((harvester.getRow() <= building.getRow()
                && map[building.getRow() - 1][building.getColumn()].getCellType() == CellType.STREET)) {
            return map[building.getRow() - 1][building.getColumn()];
        } else if ((harvester.getColumn() <= building.getColumn()
                && map[building.getRow()][building.getColumn() - 1].getCellType() == CellType.STREET)) {
            return map[building.getRow()][building.getColumn() - 1];
        } else {
            return map[building.getRow()][building.getColumn() + 1];
        }
    }

    public Cell[] assignGoal(AID harvesterAID) {
        InfoAgent harvester = null;
        for (int i = 0; i < harvesters.size(); ++i) {
            if (harvesters.get(i).getAID().equals(harvesterAID)) {
                harvester = harvesters.get(i);
            }
        }

        // get nearest garbage
        ArrayList<InfoDiscovery> discoveries = this.getNewInfoDiscoveriesList();
        double minDist = 100000000;
        int buildingIndex = 0;
        for (int i = 0; i < discoveries.size(); ++i) {
            InfoDiscovery temp = discoveries.get(i);
            double tempDist = (harvester.getRow() - temp.getRow())^2 + (harvester.getColumn() - temp.getColumn())^2;

            if (tempDist < minDist) {
                minDist = tempDist;
                buildingIndex = i;
            }
        }

        InfoDiscovery building = this.newInfoDiscoveriesList.remove(buildingIndex);
        Cell goal = this.getCollectingCell(harvester, building);
        Cell buildingCell = this.game.getMap()[building.getRow()][building.getColumn()];
        Cell[] result = {goal, buildingCell};
        return result;
    }
}
