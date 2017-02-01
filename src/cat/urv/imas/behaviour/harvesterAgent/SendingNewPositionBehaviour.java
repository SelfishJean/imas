/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterAgent;

import cat.urv.imas.behaviour.harvesterAgent.*;
import jade.core.behaviours.*;
import cat.urv.imas.agent.*;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import cat.urv.imas.onthology.*;
import jade.core.AID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;




/**
 *
 * @author albertOlivares
 */
public class SendingNewPositionBehaviour extends SimpleBehaviour {
    
    private ACLMessage msg;
    boolean hasReply;
    int nextBehaviour;
    
    public SendingNewPositionBehaviour(Agent a) {
        super(a);
        hasReply = false;
    }
    @Override
    public void action() { 
        Harvester agent = (Harvester) this.getAgent();
        agent.log("SendingNewPositionBehaviour...");
        
        
        // We set the value of the message. 
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.clearAllReceiver();
        message.addReceiver(agent.harvesterCoordinator);
        
        try {
            
            message.setContentObject(agent.getNewInfoAgent());
            agent.log("Sending new position");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.msg = message;

        //hasReply = false;
        myAgent.send(msg);
        
        while(done() == false) 
        {
            ACLMessage response = myAgent.receive();

            if(response != null) 
            {
                switch(response.getPerformative()) 
                {
                    case ACLMessage.AGREE:
                        hasReply = true;
                        nextBehaviour = 1; // Waiting map behaviour
                        agent.log("AGREE received from (my position has been received)" + ((AID) response.getSender()).getLocalName());
                        return;
                    case ACLMessage.FAILURE:
                        agent.log("The action has failed.");
                        nextBehaviour = 0; // We repeat this behaviour.
                        return;
                    
                }
            }
        }
        
        
        
    }
    
    @Override
    public boolean done() {
            return hasReply;
    }

    @Override
    public int onEnd() {
        hasReply = false;
            //System.out.println("STATE_3 return OK");
            return nextBehaviour;
    }
}
