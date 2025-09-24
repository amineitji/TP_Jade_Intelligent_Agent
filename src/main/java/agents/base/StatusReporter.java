package agents.base;
import jade.core.behaviours.TickerBehaviour;
import agents.base.BaseAgent;
import agents.base.AgentStatus;

/**
 * Comportement périodique pour le rapport de statut des agents
 */
public class StatusReporter extends TickerBehaviour {
    private final BaseAgent agent;
    
    public StatusReporter(BaseAgent agent) {
        super(agent, 30000); // Rapport toutes les 30 secondes
        this.agent = agent;
    }
    
    @Override
    protected void onTick() {
        if (agent.getStatus().isActive()) {
            generateStatusReport();
        }
    }
    
    /**
     * Génère et envoie un rapport de statut
     */
    private void generateStatusReport() {
        AgentStatus status = agent.getStatus();
        
        String report = String.format("AGENT_STATUS:%s:%s:%s:%s",
                agent.getLocalName(),
                agent.getAgentType(),
                status.getCurrentLocation(),
                status.isAvailable());
        
        agent.logger.info("Rapport de statut: " + report);
        
        // Envoyer le rapport au coordinateur si nécessaire
        broadcastStatus(report);
    }
    
    /**
     * Diffuse le statut aux agents intéressés
     */
    private void broadcastStatus(String report) {
        // À implémenter selon les besoins spécifiques
        // Peut inclure l'envoi au coordinateur, à l'interface graphique, etc.
    }
}