/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.map.Cell;
import java.util.ArrayList;

/**
 * This class contains all changes Agents are producing in the map (e.g finding or 
 * collecting garbage). 
 * @author albertOlivares
 */
public class InfoMapChanges implements java.io.Serializable {
    /**
     * Garbage we have found. List of Cells of all buildings in which our scouts
     * agents have discover garbage in the last turn.
     */
    private ArrayList<Cell> foundGarbage;
    
    /**
     * Garbage we have collected. List of Cells of all buildings in which our harvesters
     * agents have collected garbage in the last turn.
     */
    private ArrayList<Cell> collectedGarbage;
    
    
    /**
     * Update the NewChangesOnMap.
     *
     * @param temp current new changes we did this turn.
     */
    public void setFoundGarbage(ArrayList<Cell> temp) {
        this.foundGarbage = temp;
    }

    /**
     * Gets the current NewChangesOnMap.
     *
     * @return .
     */
    public ArrayList<Cell> getFoundGarbage() {
        return this.foundGarbage;
    }
    
    /**
     * Update the NewChangesOnMap.
     *
     * @param temp current new changes we did this turn.
     */
    public void setCollectedGarbage(ArrayList<Cell> temp) {
        this.collectedGarbage = temp;
    }

    /**
     * Gets the current NewChangesOnMap.
     *
     * @return .
     */
    public ArrayList<Cell> getCollectedGarbage() {
        return this.collectedGarbage;
    }
    
    
}
