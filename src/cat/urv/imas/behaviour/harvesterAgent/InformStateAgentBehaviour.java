/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterAgent;
import cat.urv.imas.behaviour.harvesterAgent.*;
import jade.core.behaviours.*;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.onthology.MessageContent;
import jade.lang.acl.*;
import java.util.ArrayList;
import cat.urv.imas.agent.*;
import cat.urv.imas.onthology.GameSettings;
import jade.core.Agent;
/**
 *
 * @author albertOlivares
 */
public class InformStateAgentBehaviour extends SimpleBehaviour
{
    private ACLMessage msg;
    boolean hasReply;
    int nextBehaviour;
    
    public InformStateAgentBehaviour(Agent a) 
    {
        super(a);
    }
    
    @Override
    public void action() 
    { 
        Harvester agent = (Harvester) this.getAgent();
        
        /* HERE WE SHOULD CHECK, IF THE PREVIOUS STATE OF THE AGENT WAS MOVING, 
        IF THE PATH HAS FINISHED OR NOT..SYSTEM AGENT CAN CHOOSE NOT TO MOVE AN
        AGENT IF THERE IS A CONFLICT SO WE CANNOT ENSURE THAT OUR LAST MOVEMENT
        ACTUALLY HAPPENED UNTIL NOW, WHEN THE MAP IS UPDATED
        */
        
        // We set the value of the first message.
        ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();
        
        initialRequest.addReceiver(agent.harvesterCoordinator);
        initialRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        agent.log("Request message");
        try {
            initialRequest.setContent(agent.getCurrentAgentState());
            agent.log("Request message content:" + initialRequest.getContent());
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        this.msg = initialRequest;
        

        hasReply = false;
        myAgent.send(msg);
        
        while(done() == false) 
        {
            ACLMessage response = myAgent.receive();

            if(response != null) 
            {
                //System.out.println(response.getPerformative());
                //CoordinatorAgent agent = (CoordinatorAgent) myAgent;
                //CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
                switch(response.getPerformative()) 
                {
                    case ACLMessage.AGREE:
                        agent.log("AGREE received from " + ((AID) response.getSender()).getLocalName());
                        break;
                    case ACLMessage.INFORM:
                        agent.log("INFORM (agents can work) received from " + ((AID) response.getSender()).getLocalName());
                        
                        try 
                        {
                            /* HERE WE SHOULD TRY TO SET THE VALUE OF "newGoalCell"
                            Cell c = (Cell) response.getContentObject();
                            agent.setNewGoalCell(c);
                            agent.log("New Goal received correctly");
                            hasReply = true;
                            */
                            
                        } 
                        catch (Exception e) 
                        {
                            /* HERE, IF IT IS NOT POSSIBLE TO SET THE VALUE THAT IS BECAUSE
                            THE MESSAGE WE HAVE RECEIVED WAS NOT A NEW GOAL SO IT 
                            HAS TO BE AN STRING..WE CAN JUST PRINT IT..
                            */
                            agent.log("Message with the inform: "+response.getContent());
                            //agent.errorLog("Incorrect content: " + e.toString());
                        }
                        agent.log("Message with the inform: "+response.getContent());
                        hasReply = true;
                        nextBehaviour = 1;
                        break;
                    case ACLMessage.FAILURE:
                        agent.log("The action has failed.");
                        nextBehaviour = 0;
                        return;
                    default:
                        agent.log("Failed to process the message");
                        break;
                }

                
            }
        }
    }
    
    @Override
    public boolean done() 
    {
        return hasReply;
    }
    
    @Override
    public int onEnd() 
    {
        hasReply = false;
        //System.out.println("End of"+getBehaviourName());
        return nextBehaviour;
    }

}