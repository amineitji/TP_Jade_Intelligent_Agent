package utils;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Utilitaire pour rechercher des services dans les Pages Jaunes JADE
 */
public class ServiceFinder {
    private static final Logger logger = Logger.getLogger(ServiceFinder.class.getName());
    
    /**
     * Trouve un agent par type de service
     */
    public static AID findAgent(Agent requester, String serviceType) {
        List<AID> agents = findAgents(requester, serviceType);
        return agents.isEmpty() ? null : agents.get(0);
    }
    
    /**
     * Trouve tous les agents d'un type de service donné
     */
    public static List<AID> findAgents(Agent requester, String serviceType) {
        return findAgents(requester, serviceType, null);
    }
    
    /**
     * Trouve les agents par type de service et nom de service
     */
    public static List<AID> findAgents(Agent requester, String serviceType, String serviceName) {
        List<AID> result = new ArrayList<>();
        
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(serviceType);
            if (serviceName != null) {
                sd.setName(serviceName);
            }
            template.addServices(sd);
            
            DFAgentDescription[] agents = DFService.search(requester, template);
            
            for (DFAgentDescription agent : agents) {
                result.add(agent.getName());
            }
            
            logger.info("Trouvé " + result.size() + " agents pour le service " + serviceType);
            
        } catch (FIPAException fe) {
            logger.severe("Erreur lors de la recherche de service: " + fe.getMessage());
        }
        
        return result;
    }
    
    /**
     * Trouve un guide disponible avec une spécialisation donnée
     */
    public static AID findAvailableGuide(Agent requester, String specialization) {
        List<AID> guides = findAgents(requester, "guide-service");
        return guides.isEmpty() ? null : guides.get(0);
    }
    
    /**
     * Trouve le coordinateur du système
     */
    public static AID findCoordinator(Agent requester) {
        return findAgent(requester, "coordinator-service");
    }
    
    /**
     * Trouve tous les touristes actifs
     */
    public static List<AID> findAllTourists(Agent requester) {
        return findAgents(requester, "tourist-service");
    }
    
    /**
     * Trouve tous les guides actifs
     */
    public static List<AID> findAllGuides(Agent requester) {
        return findAgents(requester, "guide-service");
    }
    
    /**
     * Vérifie si un service existe
     */
    public static boolean serviceExists(Agent requester, String serviceType) {
        return !findAgents(requester, serviceType).isEmpty();
    }
    
    /**
     * Compte le nombre d'agents d'un type donné
     */
    public static int countAgents(Agent requester, String serviceType) {
        return findAgents(requester, serviceType).size();
    }
    
    /**
     * Affiche tous les services disponibles (pour debug)
     */
    public static void listAllServices(Agent requester) {
        try {
            DFAgentDescription template = new DFAgentDescription();
            DFAgentDescription[] agents = DFService.search(requester, template);
            
            logger.info("=== SERVICES DISPONIBLES ===");
            for (DFAgentDescription agent : agents) {
                logger.info("Agent: " + agent.getName().getLocalName());
                // Correction: utiliser Iterator au lieu de for-each
                Iterator<?> services = agent.getAllServices();
                while (services.hasNext()) {
                    Object service = services.next();
                    if (service instanceof ServiceDescription) {
                        ServiceDescription sd = (ServiceDescription) service;
                        logger.info("  - Service: " + sd.getType() + " (" + sd.getName() + ")");
                    }
                }
            }
            logger.info("=============================");
            
        } catch (FIPAException fe) {
            logger.severe("Erreur lors du listage des services: " + fe.getMessage());
        }
    }
}