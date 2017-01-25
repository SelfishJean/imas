/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.scoutCoordinator;

import cat.urv.imas.behaviour.coordinator.*;
import jade.core.behaviours.*;
import cat.urv.imas.agent.*;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.map.StreetCell;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import cat.urv.imas.onthology.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;




/**
 *
 * @author albertOlivares
 */
public class GenerateNewSimulatedDiscoveriesBehaviour extends SimpleBehaviour {
    
    public GenerateNewSimulatedDiscoveriesBehaviour(Agent a) {
        super(a);
    }
    @Override
    public void action() { 
        System.out.println("(ScoutCoordinator) GenerateNewSimulatedDiscoveriesBehaviour starting...");
        foundGarbage();

    }
    
    public void foundGarbage()
    {
        ScoutCoordinator agent = (ScoutCoordinator) this.getAgent();
        Cell [][] mapa;
        GameSettings game;
        game = agent.getGame();
        mapa = game.getMap();
        ArrayList<InfoDiscovery> newDiscoveries;
        newDiscoveries = new ArrayList<InfoDiscovery>(); // Initialization
        
        
        for(int i = 0; i < mapa.length; ++i)
        {
            for(int j = 0; j < mapa[i].length; ++j)
            {
                if (mapa[i][j] instanceof BuildingCell) 
                {
                    InfoDiscovery oneDiscovery;
                    oneDiscovery = new InfoDiscovery(); // Initialization
                    
                    BuildingCell cell = (BuildingCell) mapa[i][j]; 
                    Map<GarbageType,Integer> garbage;
                    garbage = cell.detectGarbage();
                    
                    if (!garbage.isEmpty())
                    {
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
            //System.out.println("End of"+getBehaviourName());
            return 0;
    }
}
