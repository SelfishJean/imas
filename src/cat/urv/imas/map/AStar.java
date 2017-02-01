/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.map;

import java.util.*;

public class AStar {

    public static final int V_H_COST = 1;
    static ACell[][] grid;
    static boolean closed[][]; // is the cell of the grid free to move

    static PriorityQueue<ACell> open;

    static int startRow, startCol;
    static int endRow, endCol;

    public AStar(Cell start, Cell end, Cell[][] map) {
        setStartCell(start.getRow(), start.getCol()); // init start Cell 
        setEndCell(end.getRow(), end.getCol()); // init end Cell
        initGrid(map); // init map

        open = new PriorityQueue(16, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                ACell c1 = (ACell) o1;
                ACell c2 = (ACell) o2;
                return c1.finalCost < c2.finalCost ? -1
                        : c1.finalCost > c2.finalCost ? 1 : 0;
            }
        });
    }

    public static void initGrid(Cell[][] map) {
        int mapRows = map.length;
        int mapCols = 0;

        if (mapRows > 0) {
            mapCols = map[0].length;
        } else {
            return;
        }

        grid = new ACell[mapRows][mapCols];
        closed = new boolean[mapRows][mapCols];
        for (int i = 0; i < mapRows; ++i) {
            for (int j = 0; j < mapCols; ++j) {
                grid[i][j] = new ACell(i, j);
                grid[i][j].heuristicCost = Math.abs(i - endRow) + Math.abs(j - endCol);

                if (map[i][j].getCellType().equals(CellType.STREET)) {
                    closed[i][j] = false;
                } else {
                    closed[i][j] = true;
                }
            }
        }
    }

    static class ACell {

        int heuristicCost = 0;
        int finalCost = 0;
        int row, col;
        ACell parent;

        ACell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return "[" + this.row + ", " + this.col + "]";
        }
    }

    // unreachable cells have a null ACell value in the grid
    public static void setBlocked(int row, int col) {
        grid[row][col] = null;
    }

    public static void setStartCell(int row, int col) {
        startRow = row;
        startCol = col;
    }

    public static void setEndCell(int row, int col) {
        endRow = row;
        endCol = col;
    }

    static void checkAndUpdateCost(ACell current, ACell next, int cost) {
        if (next == null || closed[next.row][next.col]) {
            return;
        }

        int next_final_cost = next.heuristicCost + cost;

        boolean inOpen = open.contains(next);
        if (inOpen == false || next_final_cost < next.finalCost) {
            next.finalCost = next_final_cost;
            next.parent = current;
            if (inOpen == false) {
                open.add(next);
            }
        }
    }

    public static void AStar() {
        //add the start location to open list.
        if (closed[startRow][startCol] == true) {
            return;
        }
        open.add(grid[startRow][startCol]);

        ACell current;
        while (true) {
            current = open.poll();

            if (current == null) {
                break;
            }

            closed[current.row][current.col] = true;
            if (current.equals(grid[endRow][endCol])) {
                return;
            }

            ACell next;
            if (current.row - 1 >= 0) {
                next = grid[current.row - 1][current.col];
                checkAndUpdateCost(current, next, current.finalCost + V_H_COST);
            }

            if (current.col - 1 >= 0) {
                next = grid[current.row][current.col - 1];
                checkAndUpdateCost(current, next, current.finalCost + V_H_COST);
            }

            if (current.col + 1 < grid[0].length) {
                next = grid[current.row][current.col + 1];
                checkAndUpdateCost(current, next, current.finalCost + V_H_COST);
            }

            if (current.row + 1 < grid.length) {
                next = grid[current.row + 1][current.col];
                checkAndUpdateCost(current, next, current.finalCost + V_H_COST);
            }
        }
    }

    public List<Cell> getPath() {
        grid[startRow][startCol].finalCost = 0;

        for (int i = 0; i < closed.length; ++i) {
            for (int j = 0; j < closed[i].length; ++j) {
                if (closed[i][j] == true) {
                    setBlocked(i, j);
                }
            }
        }

        AStar();

        List<Cell> path = new ArrayList<Cell>();;

        if (closed[endRow][endCol]) {
            ACell current = grid[endRow][endCol];
            while (current != null) {
                path.add(new StreetCell(current.row, current.col));
                current = current.parent;
            }
        }

        Collections.reverse(path);
        return path;
    }

}
