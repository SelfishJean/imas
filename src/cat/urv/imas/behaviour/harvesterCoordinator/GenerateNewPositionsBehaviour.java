/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterCoordinator;

import jade.core.behaviours.*;
import cat.urv.imas.agent.*;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import jade.core.Agent;
import cat.urv.imas.onthology.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GenerateNewPositionsBehaviour extends SimpleBehaviour {

    public GenerateNewPositionsBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        System.out.println("(HarvesterCoordinator) GenerationNewPositionsBehaviour starting...");
        changeAgentsPositions();

    }

    public void changeAgentsPositions() {
        InfoAgent info, infoCopy;
        boolean found;
        int i, j, ri, rj, imax, jmax;
        Random rand = new Random();

        Cell[][] mapa;
        HarvestCoordinator agent = (HarvestCoordinator) this.getAgent();

        mapa = agent.getGame().getMap();
        imax = mapa.length;
        jmax = mapa[0].length;
        Map listOfAgents = agent.getGame().getAgentList();
        Set<AgentType> setOfAgents = listOfAgents.keySet();
        HashMap newListOfAgents; // We need to create another list of agents and update it.
        newListOfAgents = new HashMap<AgentType, List<Cell>>();
        List<Cell> newCells; // The list of agents is a map of AgentType and list of cells.
        StreetCell ce;
        ArrayList<InfoAgent> newPositions;
        newPositions = new ArrayList<InfoAgent>();
        found = false;

        for (AgentType at : setOfAgents) {
            List<Cell> cells = (List<Cell>) listOfAgents.get(at);
            newCells = new ArrayList<Cell>(); // For every agentType we change the list of cells.
            if (at.toString() == "HARVESTER") {
                for (Cell c : cells) {
                    i = c.getRow();
                    j = c.getCol();

                    if (mapa[i][j] instanceof StreetCell) {
                        info = ((StreetCell) mapa[i][j]).getAgent();
                        infoCopy = ((StreetCell) mapa[i][j]).getAgent();

                        while (!found) {
                            ri = Math.abs(rand.nextInt(imax)) + 0;
                            rj = Math.abs(rand.nextInt(jmax)) + 0;

                            if (mapa[ri][rj].getCellType().toString() == "STREET"
                                    && !((StreetCell) mapa[ri][rj]).isThereAnAgent()
                                    && ri != i && rj != j) {
                                found = true;

                                infoCopy.setRow(ri);
                                infoCopy.setColumn(rj);
                                infoCopy.setPreRow(i);
                                infoCopy.setPreColumn(j);
                                newPositions.add(infoCopy);
                            }
                        }
                        found = false;
                    }
                }
            }
        }
    }

    @Override
    public boolean done() {
        return true;
    }

    @Override
    public int onEnd() {
        return 0;
    }
}
