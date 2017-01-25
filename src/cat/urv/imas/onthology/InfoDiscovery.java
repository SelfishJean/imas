/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import java.util.Map;

/**
 *
 * @author albertOlivares
 */
public class InfoDiscovery implements java.io.Serializable {
    /**
     * Column in the map for the related discovery.
     */
    private int column;
    /**
     * Row in the map for the related discovery.
     */
    private int row; 
    /**
     * Garbage of the building: it can only be of one type at a time.
     * But, once generated, it can be of any type and amount.
     */
    protected Map<GarbageType, Integer> garbage;
    
    /**
     * Gets new discovery column.
     *
     * @return agent current column.
     */
    public int getColumn() {
        return this.column;
    }

    /**
     * Sets the new discovery column.
     *
     * @param int new discovery column.
     */
    public void setColumn(int column) {
        this.column = column;
    }
    
    /**
     * Gets discovery row.
     *
     * @return new discovery column.
     */
    public int getRow() {
        return this.row;
    }

    /**
     * Sets the new discovery row.
     *
     * @param int new discovery row.
     */
    public void setRow(int row) {
        this.row = row;
    }
    
    /**
     * Gets discovery garbage.
     *
     * @return new discovery column.
     */
    public Map<GarbageType, Integer> getGarbage() {
        return this.garbage;
    }

    /**
     * Sets the new discovery garbage.
     *
     * @param Map new discovery.
     */
    public void setGarbage(Map<GarbageType, Integer> temp) {
        this.garbage = temp;
    }
}
