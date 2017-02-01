/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.scoutAgent;

import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import cat.urv.imas.agent.*;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.InfoDiscovery;
import jade.core.Agent;
import java.util.Map;

public class ExploreSurroundingCellsBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply;

    public ExploreSurroundingCellsBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        exploration();
    }

    public void exploration() {
        Scout agent = (Scout) this.getAgent();

        InfoAgent information = agent.getInfoAgent();
        Cell[][] mapa;
        GameSettings game;
        game = agent.getGame();
        mapa = game.getMap();
        ArrayList<InfoDiscovery> newDiscoveries;
        newDiscoveries = new ArrayList<InfoDiscovery>();

        int row = information.getRow();
        int column = information.getColumn();

        // We explore eight cells which surround the agent
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = column - 1; j <= column + 1; j++) {

                if (mapa[i][j] instanceof BuildingCell) {
                    InfoDiscovery oneDiscovery;
                    oneDiscovery = new InfoDiscovery();

                    BuildingCell cell = (BuildingCell) mapa[i][j];
                    Map<GarbageType, Integer> garbage;

                    // Only if the garbage has not been discovered before.
                    if (cell.getFound() == false) {
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
