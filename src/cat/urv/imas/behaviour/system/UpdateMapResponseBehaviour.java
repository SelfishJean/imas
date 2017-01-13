/**
 *  IMAS implemented (Group 8) code for the practical work.
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
package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.AgentType;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.MessageContent;
import java.util.List;
import java.util.Map;
import jade.core.*;
import cat.urv.imas.onthology.InfoAgent;
import jade.domain.FIPANames;
import java.util.ArrayList;
import jade.core.behaviours.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author albertOlivares
 */
public class UpdateMapResponseBehaviour extends SimpleBehaviour
{
    boolean hasReply;
    
    public UpdateMapResponseBehaviour(SystemAgent agent) 
    {
        super(agent);
        hasReply = false;
        agent.log("Waiting REQUESTs for new step from authorized agents");
    }

    /**
     * When System Agent receives a REQUEST message, it agrees. Only if
     * message type is AGREE, method prepareResultNotification() will be invoked.
     * 
     * @param msg message received.
     * @return AGREE message when all was ok, or FAILURE otherwise.
     */
    
    @Override
    public void action() 
    { 
        while(done() == false) 
        {
            ACLMessage response = myAgent.receive();
            if (response != null && response.getPerformative() == ACLMessage.REQUEST) 
            {
                SystemAgent agent = (SystemAgent) this.getAgent();
                ACLMessage reply = response.createReply();
                try 
                {
                    agent.setNewInfoAgent((ArrayList<InfoAgent>) response.getContentObject());
                    agent.log("Request received");
                    reply.setPerformative(ACLMessage.AGREE);
                } 
                catch (Exception e) 
                {
                    reply.setPerformative(ACLMessage.FAILURE);
                    agent.errorLog(e.getMessage());
                    e.printStackTrace();
                }
                agent.log("Updating map...");
                agent.send(reply);

                hasReply = true;
            }
        }
    }
    
    
    /*
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) {

        // it is important to make the createReply in order to keep the same context of
        // the conversation

        SystemAgent agent = (SystemAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        
        try {
            reply.setContent(MessageContent.NEXT_STEP);
            //agent.setMapAlreadySent(false);
            //agent.setThereIsNewTurn(true);
        } catch (Exception e) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.errorLog(e.toString());
            e.printStackTrace();
        }
        agent.log("Simulation has been updated");
        return reply;
    }*/
    
    public boolean done() 
    {
        return hasReply;
    }
    
    public int onEnd() 
    {
        hasReply = false;
        return 0;
    }
}
