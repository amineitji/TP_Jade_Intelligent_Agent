package launcher;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import java.util.ArrayList;
import java.util.List;

/**
 * Lanceur simple pour le système multi-agents du musée
 * Démarre JADE et lance l'interface JavaFX
 */
public class SimpleLauncher {
    
    private static AgentContainer mainContainer;
    private static List<AgentController> agentControllers = new ArrayList<>();
    private static boolean systemRunning = false;
    
    /**
     * Démarre le système JADE
     */
    public static boolean startJadeSystem() {
        try {
            System.setProperty("java.net.preferIPv4Stack", "true");
            jade.core.Runtime rt = jade.core.Runtime.instance();
            
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.MAIN, "true");
            profile.setParameter(Profile.GUI, "false");
            
            mainContainer = rt.createMainContainer(profile);
            systemRunning = true;
            
            // Créer les agents initiaux
            createInitialAgents();
            
            return true;
        } catch (Exception e) {
            System.err.println("Erreur démarrage JADE: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Crée les agents de base
     */
    private static void createInitialAgents() {
        try {
            // Créer les guides
            String[] guideNames = {"GuideRenaissance", "GuideModerne", "GuideImpressionniste"};
            for (String guideName : guideNames) {
                AgentController guide = mainContainer.createNewAgent(
                    guideName, "agents.guide.GuideAgent", null);
                guide.start();
                agentControllers.add(guide);
                Thread.sleep(500);
            }
            
            // Créer quelques touristes initiaux
            String[] touristNames = {"Alice_FR", "Bob_IT", "Charlie_EN", "Diana_DE"};
            for (String touristName : touristNames) {
                AgentController tourist = mainContainer.createNewAgent(
                    touristName, "agents.tourist.TouristAgent", null);
                tourist.start();
                agentControllers.add(tourist);
                Thread.sleep(500);
            }
            
        } catch (Exception e) {
            System.err.println("Erreur création agents: " + e.getMessage());
        }
    }
    
    /**
     * Ajoute un touriste dynamiquement
     */
    public static boolean addTourist(String name) {
        if (!systemRunning || mainContainer == null) return false;
        
        try {
            AgentController tourist = mainContainer.createNewAgent(
                name, "agents.tourist.TouristAgent", null);
            tourist.start();
            agentControllers.add(tourist);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur ajout touriste: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Arrête le système JADE
     */
    public static void shutdownSystem() {
        if (!systemRunning) return;
        
        try {
            for (AgentController agent : agentControllers) {
                try {
                    agent.kill();
                } catch (Exception e) {
                    // Ignorer les erreurs d'arrêt
                }
            }
            
            if (mainContainer != null) {
                mainContainer.kill();
            }
            
            systemRunning = false;
            agentControllers.clear();
            
        } catch (Exception e) {
            System.err.println("Erreur arrêt système: " + e.getMessage());
        }
    }
    
    // Getters
    public static boolean isSystemRunning() { return systemRunning; }
    public static AgentContainer getMainContainer() { return mainContainer; }
    public static List<AgentController> getAgentControllers() { return agentControllers; }
    
    /**
     * Point d'entrée principal
     */
    public static void main(String[] args) {
        // Lancer l'interface JavaFX
        MuseumVisualizationApp.main(args);
    }
}