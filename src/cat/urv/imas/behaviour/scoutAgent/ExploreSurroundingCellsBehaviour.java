/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.scoutAgent;
import cat.urv.imas.behaviour.scoutCoordinator.*;
import cat.urv.imas.behaviour.coordinator.*;
import jade.core.behaviours.*;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.onthology.MessageContent;
import jade.lang.acl.*;
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
/**
 * 
 * @author albertOlivares
 */
public class ExploreSurroundingCellsBehaviour extends SimpleBehaviour
{
    private ACLMessage msg;
    boolean hasReply;
    
    public ExploreSurroundingCellsBehaviour(Agent a) 
    {
        super(a);
    }
    
    @Override
    public void action() 
    { 
        System.out.println("(Scout) Starting ExploreSurroundingCellsBehaviour");
        exploration();
        
    }
    
    public void exploration() {
        Scout agent = (Scout) this.getAgent();
        
        InfoAgent information = agent.getInfoAgent();
        Cell [][] mapa;
        GameSettings game;
        game = agent.getGame();
        mapa = game.getMap();
        ArrayList<InfoDiscovery> newDiscoveries;
        newDiscoveries = new ArrayList<InfoDiscovery>(); // Initialization
        
        int row = information.getRow();
        int column = information.getColumn();
        
        // We explore eight cells which surround the agent
        for (int i = row-1; i <= row+1; i++)
        {
            for (int j = column-1; j <= column+1; j++)
            {
                
                if (mapa[i][j] instanceof BuildingCell) 
                {
                    //System.out.println("*_*_*_*_*_*_*_*_*_*_*_*_*_*"+mapa[i][j].getCellType());
                    InfoDiscovery oneDiscovery;
                    oneDiscovery = new InfoDiscovery(); // Initialization
                    
                    BuildingCell cell = (BuildingCell) mapa[i][j]; 
                    Map<GarbageType,Integer> garbage;
                    
                    if (cell.getFound() == false) // Only if the garbage has not been discovered before.
                    {
                        garbage = cell.detectGarbage(); // If there is garbage, this method sets found=true.
                        //cell.setFound(true);
                        
                        if (!garbage.isEmpty())
                        {
                            oneDiscovery.setRow(i);
                            oneDiscovery.setColumn(j);
                            oneDiscovery.setGarbage(garbage);
                            newDiscoveries.add(oneDiscovery);
                            //System.out.println("*_*_*_*_*_*_*_*_*_*_*_*_*_*"+garbage+" "+i+" "+j);
                        }   
                    }
                    //else
                        //System.out.println("-..-..-.-.-..-...-.--..-.-ThisBuildingHasAlreadyBeenFound.....");
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