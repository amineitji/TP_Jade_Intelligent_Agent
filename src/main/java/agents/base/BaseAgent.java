package agents.base;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Classe de base abstraite pour tous les agents du système musée
 * Fournit les fonctionnalités communes et l'interface de base
 */
public abstract class BaseAgent extends Agent {
    protected Logger logger;
    protected AgentStatus status;
    protected String agentType;
    
    @Override
    protected void setup() {
        // Initialisation du logger
        logger = Logger.getLogger(getClass().getName());
        logger.info("Agent " + getLocalName() + " démarré");
        
        // Initialisation du statut
        status = createInitialStatus();
        
        // Configuration spécifique de l'agent
        configureAgent();
        
        // Enregistrement du service
        registerService();
        
        // Ajout des comportements de base
        addBaseBehaviours();
        
        // Configuration spécifique
        setupSpecificBehaviours();
    }
    
    /**
     * Crée le statut initial de l'agent
     */
    protected abstract AgentStatus createInitialStatus();
    
    /**
     * Configuration spécifique de l'agent
     */
    protected abstract void configureAgent();
    
    /**
     * Enregistrement du service dans les Pages Jaunes
     */
    protected void registerService() {
        try {
            DFAgentDescription dfd = createServiceDescription();
            DFService.register(this, dfd);
            logger.info("Service " + agentType + " enregistré pour " + getLocalName());
        } catch (FIPAException fe) {
            logger.severe("Erreur enregistrement service: " + fe.getMessage());
        }
    }
    
    /**
     * Crée la description du service pour les Pages Jaunes
     */
    protected abstract DFAgentDescription createServiceDescription();
    
    /**
     * Ajoute les comportements de base communs
     */
    protected void addBaseBehaviours() {
        addBehaviour(new MessageHandler(this));
        addBehaviour(new StatusReporter(this));
    }
    
    /**
     * Configuration des comportements spécifiques à l'agent
     */
    protected abstract void setupSpecificBehaviours();
    
    /**
     * Gestion générique des messages
     */
    protected abstract void handleMessage(jade.lang.acl.ACLMessage message);
    
    /**
     * Mise à jour du statut de l'agent
     */
    public void updateStatus(AgentStatus newStatus) {
        this.status = newStatus;
        logger.fine("Statut mis à jour pour " + getLocalName());
    }
    
    /**
     * Récupération du statut actuel
     */
    public AgentStatus getStatus() {
        return status;
    }
    
    /**
     * Récupération du type d'agent
     */
    public String getAgentType() {
        return agentType;
    }
    
    /**
     * Arrêt propre de l'agent
     */
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            logger.info("Agent " + getLocalName() + " désenregistré et terminé");
        } catch (FIPAException fe) {
            logger.warning("Erreur lors du désenregistrement: " + fe.getMessage());
        }
        
        performCleanup();
    }
    
    /**
     * Nettoyage spécifique à l'agent
     */
    protected abstract void performCleanup();
    
    /**
     * Utilitaire pour envoyer un message
     */
    protected void sendMessage(AID receiver, int performative, String content) {
        jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(performative);
        message.addReceiver(receiver);
        message.setContent(content);
        send(message);
        logger.fine("Message envoyé à " + receiver.getLocalName() + ": " + content);
    }
    
    /**
     * Utilitaire pour créer un AID à partir d'un nom local
     */
    protected AID createAID(String localName) {
        return new AID(localName, AID.ISLOCALNAME);
    }
}