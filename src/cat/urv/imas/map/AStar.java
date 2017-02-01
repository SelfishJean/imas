/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.map;
/**
 *
 * @author Alex
 */
import java.util.*;

public class AStar 
{
    public static final int V_H_COST = 1;
    static ACell[][] grid;
    static boolean closed[][]; // is the cell of the grid free to move
    
    static PriorityQueue<ACell> open;
    
    static int startRow, startCol;
    static int endRow, endCol;
    
    public AStar(Cell start, Cell end, Cell[][] map)
    {
        setStartCell(start.getRow(), start.getCol()); // init start Cell 
        setEndCell(end.getRow(), end.getCol()); // init end Cell
        initGrid(map); // init map
        
        open = new PriorityQueue(16, new Comparator()
        {
            @Override
            public int compare(Object o1, Object o2) 
            {
                ACell c1 = (ACell) o1;
                ACell c2 = (ACell) o2;
                return c1.finalCost < c2.finalCost ? -1
                        : c1.finalCost > c2.finalCost ? 1 : 0;
            }
        });
    }
    
    public static void initGrid(Cell[][] map)
    {
        int mapRows = map.length;
        int mapCols = 0;
        
        if(mapRows > 0)
        {
            mapCols = map[0].length;
        }
        else
        {
            System.out.print("Astar Error(initGrid method): invalid Map Size");
            return;
        }
        
        grid = new ACell[mapRows][mapCols];
        closed = new boolean[mapRows][mapCols];
        for(int i = 0; i < mapRows; ++i)
        {
            for(int j = 0; j < mapCols; ++j)
            {
                grid[i][j] = new ACell(i, j);
                map[i][j].getRow();
                map[i][j].getCol();
                grid[i][j].toString();
                grid[i][j].heuristicCost = Math.abs(i - endRow) + Math.abs(j - endCol);
                
                if(map[i][j].getCellType().equals(CellType.STREET))
                {
                    closed[i][j] = false;
                }
                else
                {
                    closed[i][j] = true;
                }
            }
        }
    }
    
    static class ACell
    {  
        int heuristicCost = 0;
        int finalCost = 0;
        int row, col;
        ACell parent; 
        
        ACell(int row, int col)
        {
            this.row = row;
            this.col = col; 
        }
        
        @Override
        public String toString()
        {
            return "[" + this.row + ", " + this.col + "]";
        }
    }
    
    // unreachable cells have a null ACell value in the grid
    public static void setBlocked(int row, int col)
    {
        grid[row][col] = null;
    }
    
    public static void setStartCell(int row, int col)
    {
        startRow = row;
        startCol = col;
    }
    
    public static void setEndCell(int row, int col)
    {
        endRow = row;
        endCol = col; 
    }
    
    static void checkAndUpdateCost(ACell current, ACell next, int cost)
    {
        if(next == null || closed[next.row][next.col])
            return;
        
        int next_final_cost = next.heuristicCost + cost;
        
        boolean inOpen = open.contains(next);
        if(inOpen == false || next_final_cost < next.finalCost)
        {
            next.finalCost = next_final_cost;
            next.parent = current;
            if(inOpen == false)
            {
                open.add(next);
            }
        }
    }
    
    public static void AStar()
    { 
        //add the start location to open list.
        if(closed[startRow][startCol] == true)
        {
            System.out.printf("(AStar) Start Cell: %d %d", startRow, startCol);
            System.out.printf("(AStart) End Cell: %d %d", endRow, endCol);
            System.out.print("AStarError(AStar method): the start cell is blocked");
            return;
        }
        open.add(grid[startRow][startCol]);
        
        ACell current;
        while(true)
        { 
            current = open.poll();
            
            if (current == null)
                break;
            
            closed[current.row][current.col] = true;
            if(current.equals(grid[endRow][endCol]))
            {
                return; 
            } 

            ACell next;  
            if(current.row - 1 >= 0)
            {
                next = grid[current.row - 1][current.col];
                checkAndUpdateCost(current, next, current.finalCost + V_H_COST); 
            } 

            if(current.col - 1 >= 0)
            {
                next = grid[current.row][current.col - 1];
                checkAndUpdateCost(current, next, current.finalCost + V_H_COST); 
            }

            if(current.col + 1 < grid[0].length){
                next = grid[current.row][current.col + 1];
                checkAndUpdateCost(current, next, current.finalCost + V_H_COST); 
            }

            if(current.row + 1 < grid.length)
            {
                next = grid[current.row + 1][current.col];
                checkAndUpdateCost(current, next, current.finalCost + V_H_COST); 
            }
        } 
    }
    
    public List<Cell> getPath()
    {
        grid[startRow][startCol].finalCost = 0;
   
        for(int i = 0; i < closed.length; ++i) 
        {
            for(int j = 0; j < closed[i].length; ++j)
            {
                if(closed[i][j] == true)
                {
                    setBlocked(i, j);
                }
            }   
        }
        
        //Display initial map
        /*
        System.out.println("Grid: ");
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                if (i == startRow && j == startCol) {
                    System.out.print("SO  "); //Source
                } else if (i == endRow && j == endCol) {
                    System.out.print("DE  ");  //Destination
                } else if (grid[i][j] != null) {
                    System.out.printf("%-3d ", 0);
                } else {
                    System.out.print("BL  ");
                }
            }
            System.out.println();
        }
        System.out.println();*/

        AStar();
        
        // Show scores for the cells
        /*
        System.out.println("\nScores for cells: ");
        for (int i = 0; i < rows; ++i) 
        {
            for (int j = 0; j < cols; ++j) 
            {
                if (grid[i][j] != null) 
                {
                    System.out.printf("%-3d ", grid[i][j].finalCost);
                } else {
                    System.out.print("BL ");
                }
            }
            System.out.println();
        }
        System.out.println(); */

        List<Cell> path = new ArrayList<Cell>();;
        
        if (closed[endRow][endCol]) 
        {
            //Trace back the path 
            //System.out.println("Path: ");
            ACell current = grid[endRow][endCol];
            //System.out.print(current);
            while (current != null) 
            {
                //System.out.print(" -> " + current.parent);
                
                path.add(new StreetCell(current.row, current.col));
                current = current.parent;
            }
            //System.out.println();
        } 
        else 
        {
            System.out.println("AStar Error(solve method): No possible path");
        }
        
        Collections.reverse(path);
        
        /*
        for(int k = 0; k < path.size(); ++k)
        {
            System.out.println(Integer.toString(path.get(k).getRow()) + " " 
                    + Integer.toString(path.get(k).getCol()));
        }*/
        
        return path;
    }

}