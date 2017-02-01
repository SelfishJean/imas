/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.scoutCoordinator;

import jade.core.behaviours.*;
import cat.urv.imas.agent.*;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.Cell;
import jade.core.Agent;
import cat.urv.imas.onthology.*;
import java.util.ArrayList;
import java.util.Map;

public class GenerateNewSimulatedDiscoveriesBehaviour extends SimpleBehaviour {

    public GenerateNewSimulatedDiscoveriesBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        foundGarbage();

    }

    public void foundGarbage() {
        ScoutCoordinator agent = (ScoutCoordinator) this.getAgent();
        Cell[][] mapa;
        GameSettings game;
        game = agent.getGame();
        mapa = game.getMap();
        ArrayList<InfoDiscovery> newDiscoveries;
        newDiscoveries = new ArrayList<InfoDiscovery>();

        for (int i = 0; i < mapa.length; ++i) {
            for (int j = 0; j < mapa[i].length; ++j) {
                if (mapa[i][j] instanceof BuildingCell) {
                    InfoDiscovery oneDiscovery;
                    oneDiscovery = new InfoDiscovery();

                    BuildingCell cell = (BuildingCell) mapa[i][j];
                    Map<GarbageType, Integer> garbage;
                    garbage = cell.detectGarbage();

                    if (!garbage.isEmpty()) {
                        oneDiscovery.setRow(i);
                        oneDiscovery.setColumn(j);
                        oneDiscovery.setGarbage(garbage);
                        newDiscoveries.add(oneDiscovery);
                    }
                }
            }
        }
        agent.setNewInfoDiscoveriesList(newDiscoveries);
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
