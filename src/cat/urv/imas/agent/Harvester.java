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
import cat.urv.imas.behaviour.harvesterAgent.*;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.core.*;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;

import java.util.List;
import cat.urv.imas.map.AStar;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.map.RecyclingCenterCell;
import java.util.Map;

public class Harvester extends ImasAgent {

    /**
     * Game settings in use.
     */
    public GameSettings game;
    /**
     * Harvester Coordinator id.
     */
    public AID harvesterCoordinator;
    /**
     * Agent state {FREE || MOVING || COLLECTING || DUMPING}.
     */
    public String state;
    /**
     * New goal Cell (with garbage to collect or a recycling center to dump).
     */
    public Cell newGoalCell;
    public Cell goalBuilding;
    /**
     * NewInfoAgent. It contains the following information of the agent.
     */
    public InfoAgent newInfoAgent;

    public boolean dumping = false;

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
     * Update the state.
     *
     * @param state current state of the agent.
     */
    public void setCurrentAgentState(String temp) {
        this.state = temp;
    }

    /**
     * Gets the current state of the agent.
     *
     * @return the current state of the agent.
     */
    public String getCurrentAgentState() {
        return this.state;
    }

    /**
     * Update the value of the Cell we want to go (to collect or to dump
     * garbage).
     *
     * @param newGoalCell
     */
    public void setNewGoalCell(Cell temp) {
        this.newGoalCell = temp;
    }

    /**
     * Gets the value of the Cell we want to go (to collect or to dump garbage).
     *
     * @return the value of the Cell we want to go (to collect or to dump
     * garbage).
     */
    public Cell getNewGoalCell() {
        return this.newGoalCell;
    }

    /**
     * Gets the next information for the agent.
     *
     * @return info agent.
     */
    public InfoAgent getNewInfoAgent() {
        return this.newInfoAgent;
    }

    /**
     * Update agent information.
     *
     * @param newInfo information of the agent for next simulation step.
     */
    public void setNewInfoAgent(InfoAgent newInfo) {
        this.newInfoAgent = newInfo;
    }

    private List<Cell> path;
    Map<GarbageType, Integer> curGarbage;
    int capacity;

    /**
     * Builds the coordinator agent.
     */
    public Harvester() {
        super(AgentType.HARVESTER);
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
        sd1.setType(AgentType.HARVESTER.toString());
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

        // search HarvesterCoordinator
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.HARVESTER_COORDINATOR.toString());
        this.harvesterCoordinator = UtilsAgents.searchAgent(this, searchCriterion);

        // We set the current state of the Agent as FREE.
        this.setCurrentAgentState(MessageContent.FREE);

        // Finite State Machine
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                System.out.println("(HarvesterAgent) FSM behaviour completed.");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        fsm.registerFirstState(new WaitingForMapBehaviour(this), "STATE_1");
        fsm.registerState(new InformStateAgentBehaviour(this), "STATE_2");
        fsm.registerState(new StartingNewGoalBehaviour(this), "STATE_3");
        fsm.registerState(new MovingBehaviour(this), "STATE_4");
        fsm.registerState(new CollectingBehaviour(this), "STATE_5");
        fsm.registerState(new DumpingBehaviour(this), "STATE_6");
        fsm.registerState(new ChillingBehaviour(this), "STATE_7");
        fsm.registerState(new SendingNewPositionBehaviour(this), "STATE_8");
        fsm.registerState(new WaitingForMapBehaviour(this), "STATE_9");

        fsm.registerDefaultTransition("STATE_1", "STATE_2");
        fsm.registerTransition("STATE_2", "STATE_2", 0);
        fsm.registerTransition("STATE_2", "STATE_3", 1);
        fsm.registerDefaultTransition("STATE_3", "STATE_4");
        fsm.registerTransition("STATE_2", "STATE_4", 2);
        fsm.registerDefaultTransition("STATE_4", "STATE_8");
        fsm.registerTransition("STATE_2", "STATE_5", 3);
        fsm.registerDefaultTransition("STATE_5", "STATE_8");
        fsm.registerTransition("STATE_2", "STATE_6", 4);
        fsm.registerDefaultTransition("STATE_6", "STATE_8");
        fsm.registerTransition("STATE_2", "STATE_7", 5);
        fsm.registerDefaultTransition("STATE_7", "STATE_8");
        fsm.registerTransition("STATE_8", "STATE_8", 0);
        fsm.registerTransition("STATE_8", "STATE_9", 1, new String[]{"STATE_9"});
        fsm.registerDefaultTransition("STATE_9", "STATE_2");

        this.addBehaviour(fsm);
    }

    public Cell getCurrentCell() {
        Cell[][] map = this.game.getMap();
        int rows = map.length;
        int cols = map[0].length;
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                if (map[i][j].getCellType() == CellType.STREET) {
                    StreetCell temp = (StreetCell) map[i][j];
                    if (temp.isThereAnAgent() && temp.getAgent().getAID().equals(this.getAID())) {
                        return map[i][j];
                    }
                }
            }
        }

        this.log("(getCurrentCell): null cell");
        return null;
    }

    public void calculatePath(Cell cur, Cell goal) {
        AStar l = new AStar(cur, goal, game.getMap());
        path = l.getPath();

        if (path != null && path.size() > 0) {
            this.log("The path is created");

            for (int i = 0; i < path.size(); ++i) {
                System.out.printf("(%d,%d)", path.get(i).getRow(), path.get(i).getCol());
            }
        } else {
            this.log("Error: the path isn't created");
            System.out.printf("CurrentCell: %d %d; CurrentGoal: %d %d \n", cur.getRow(),
                    cur.getCol(), goal.getRow(), goal.getCol());
        }
    }

    public List<Cell> getPath() {
        return path;
    }

    public Cell getNextMove() {
        Cell res = null;
        if (path.size() > 0) {
            res = path.get(0);
        }

        return res;
    }

    public void removeNextMove() {
        if (path.size() > 0) {
            path.remove(0);
        }
    }

    public int getCurrentCapacity() {
        capacity = this.game.getHarvestersCapacity();
        int curCapacity = 0;
        for (Map.Entry<GarbageType, Integer> entry : curGarbage.entrySet()) {
            curCapacity = entry.getValue();
        }
        return curCapacity;
    }

    public boolean collectGarbage(Cell curCell) {
        BuildingCell building = (BuildingCell) this.goalBuilding;
        this.log("Collecting Garbage: " + curCell.getRow() + " " + curCell.getCol());

        if (building == null) {
            this.log("(collectGarbage): Collecting garbage failed");
        }

        if (this.hasGarbage() == false) {
            curGarbage = building.detectGarbage();
            if (curGarbage.isEmpty()) {
            }

            for (Map.Entry<GarbageType, Integer> entry : curGarbage.entrySet()) {
                curGarbage.replace(entry.getKey(), 0);
            }
        }

        int curCapacity = getCurrentCapacity();

        if (curCapacity < capacity) {
            if (building.detectGarbage().isEmpty() == false) {
                for (Map.Entry<GarbageType, Integer> entry : curGarbage.entrySet()) {
                    curGarbage.replace(entry.getKey(), entry.getValue() + 1);
                }
            }
        }

        for (Map.Entry<GarbageType, Integer> entry : curGarbage.entrySet()) {
            this.log("Collecting garbage: success " + entry.getKey()
                    + " " + entry.getValue() + " " + capacity);
        }

        if (curCapacity == capacity || building.detectGarbage().isEmpty()) {
            this.log("Harvester finished collecting " + curCapacity
                    + "/" + capacity);
            return true;
        } else {
            return false;
        }
    }

    public Cell getRecyclingCell(Cell harvester, Cell building) {
        Cell[][] map = game.getMap();
        if (harvester.getRow() > building.getRow()
                && map[building.getRow() + 1][building.getCol()].getCellType() == CellType.STREET) {
            return map[building.getRow() + 1][building.getCol()];
        } else if ((harvester.getRow() <= building.getRow()
                && map[building.getRow() - 1][building.getCol()].getCellType() == CellType.STREET)) {
            return map[building.getRow() - 1][building.getCol()];
        } else if ((harvester.getCol() <= building.getCol()
                && map[building.getRow()][building.getCol() - 1].getCellType() == CellType.STREET)) {
            return map[building.getRow()][building.getCol() - 1];
        } else {
            return map[building.getRow()][building.getCol() + 1];
        }
    }

    // this method assigns new goal for recycling center
    public void findRCenter() {
        Cell curPos = getCurrentCell();
        Cell[][] map = game.getMap();
        int rows = map.length;
        int cols = map[0].length;

        double minDist = 1000000000;
        Cell building = null;
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                if (map[i][j].getCellType() == CellType.RECYCLING_CENTER) {
                    Cell temp = map[i][j];
                    double tempDist = (curPos.getRow() - temp.getRow())^2 + (curPos.getCol() - temp.getCol())^2;

                    if (tempDist < minDist) {
                        minDist = tempDist;
                        building = temp;
                    }
                }
            }
        }

        this.setNewGoalCell(this.getRecyclingCell(curPos, building));
        this.goalBuilding = building;
    }

    public boolean dumpGarbage(Cell curCell) {
        this.log("Dumping Garbage: " + curCell.getRow() + " " + curCell.getCol());
        RecyclingCenterCell building = (RecyclingCenterCell) this.goalBuilding;

        if (building == null) {
            this.log("(dumpGarbage): Dumping garbage failed");
        }

        int[] prices = building.getPrices();
        double score = 0;
        int currentCapacity = -1;
        for (Map.Entry<GarbageType, Integer> entry : curGarbage.entrySet()) {
            GarbageType type = entry.getKey();
            if (type == GarbageType.PLASTIC) {
                score += prices[0];
            } else if (type == GarbageType.GLASS) {
                score += prices[1];
            } else {
                score += prices[2];
            }

            currentCapacity = entry.getValue() - 1;
            curGarbage.replace(entry.getKey(), entry.getValue() - 1);
        }

        if (currentCapacity == 0 || currentCapacity == -1) {
            this.log("Dumping garbage finished");
            curGarbage.clear();
            return true;
        } else {
            this.log("Dumping garbage failed: capacity " + currentCapacity);
            return false;
        }
    }

    public boolean hasGarbage() {
        if (curGarbage == null) {
            return false;
        }

        return !curGarbage.isEmpty();
    }
}
