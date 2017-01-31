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
package cat.urv.imas.onthology;

/**
 * Content messages for inter-agent communication.
 */
public class MessageContent {
    
    /**
     * Message sent from Coordinator agent to System agent to get the whole
     * city information.
     */
    public static final String GET_MAP = "Get map";
    
    /**
     * Message sent from System Agent to Coordinator Agent to confirm that next 
     * step has been initiated.
     */
    public static final String NEXT_STEP = "Next step";
    
    /**
     * Message sent from Harvester agent to Harvester Coordinator to inform about its
     * state.
     */
    public static final String FREE = "Agent is free";
    
    /**
     * Message sent from Harvester agent to Harvester Coordinator to inform about its
     * state.
     */
    public static final String MOVING = "Agent is moving";
    
    /**
     * Message sent from Harvester agent to Harvester Coordinator to inform about its
     * state.
     */
    public static final String COLLECTING = "Agent is collecting";
    
    /**
     * Message sent from Harvester agent to Harvester Coordinator to inform about its
     * state.
     */
    public static final String DUMPING = "Agent is dumping";
    
    /**
     * Message sent from Harvester Coordinator to Harvester agent as an Inform.
     */
    public static final String OK = "OK";
    
    /**
     * Message sent from Harvester Coordinator to Harvester agent as an Inform.
     */
    public static final String NO_GOAL = "No goal";
    
}
