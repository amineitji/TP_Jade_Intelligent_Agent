package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import agents.*;

public class ExpositionSystem {
    private AgentContainer mainContainer;
    private Runtime rt;
    
    public static void main(String[] args) {
        ExpositionSystem system = new ExpositionSystem();
        system.startSystem();
        
        // Garder le système en vie
        Runtime.instance().shutDown();
    }
    
    public void startSystem() {
        System.out.println("🏛️  DÉMARRAGE DU SYSTÈME D'EXPOSITION");
        System.out.println("=====================================");
        
        try {
            // Configuration JADE
            rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true"); // Interface graphique JADE
            
            // Création du conteneur principal
            mainContainer = rt.createMainContainer(profile);
            
            // Démarrage du coordinateur
            startCoordinator();
            
            // Attente pour que le coordinateur s'initialise
            Thread.sleep(2000);
            
            // Démarrage des guides
            startGuides();
            
            // Attente pour que les guides s'initialisent
            Thread.sleep(3000);
            
            // Démarrage des groupes de touristes
            startTouristGroups();
            
            System.out.println("✅ Système d'exposition complètement initialisé");
            System.out.println("📊 Surveillance en cours...\n");
            
            // Simulation continue
            simulateContinuousOperation();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startCoordinator() throws StaleProxyException {
        AgentController coordinator = mainContainer.createNewAgent(
            "Coordinateur", 
            "agents.CoordinatorAgent", 
            new Object[]{}
        );
        coordinator.start();
        System.out.println("👔 Coordinateur démarré");
    }
    
    private void startGuides() throws StaleProxyException {
        String[] guideNames = {
            "GuideMartin", "GuideSophie", "GuideJean", 
            "GuideMarie", "GuideAlex"
        };
        
        for (String guideName : guideNames) {
            AgentController guide = mainContainer.createNewAgent(
                guideName, 
                "agents.GuideAgent", 
                new Object[]{}
            );
            guide.start();
            System.out.println("👨‍🏫 Guide " + guideName + " démarré");
        }
    }
    
    private void startTouristGroups() throws StaleProxyException, InterruptedException {
        // Configuration des groupes de touristes
        Object[][] groupConfigs = {
            {"Groupe_Français", 8, "Française"},
            {"Groupe_Allemands", 12, "Allemande"},
            {"Groupe_Japonais", 6, "Japonaise"},
            {"Groupe_Américains", 10, "Américaine"},
            {"Groupe_Italiens", 14, "Italienne"},
            {"Groupe_Espagnols", 9, "Espagnole"},
            {"Groupe_Britanniques", 7, "Britannique"}
        };
        
        for (Object[] config : groupConfigs) {
            String groupName = (String) config[0];
            Integer size = (Integer) config[1];
            String nationality = (String) config[2];
            
            AgentController group = mainContainer.createNewAgent(
                groupName, 
                "agents.TouristGroupAgent", 
                new Object[]{groupName, size, nationality}
            );
            group.start();
            
            System.out.println("👥 " + groupName + " démarré (" + 
                size + " personnes, " + nationality + ")");
            
            // Délai entre les arrivées de groupes
            Thread.sleep(2000);
        }
    }
    
    private void simulateContinuousOperation() throws InterruptedException, StaleProxyException {
        System.out.println("\n🔄 SIMULATION EN COURS...");
        
        // Simulation pendant 3 minutes
        for (int i = 0; i < 18; i++) {
            Thread.sleep(10000); // 10 secondes
            
            // Occasionnellement, ajouter de nouveaux groupes
            if (Math.random() < 0.3) {
                addRandomTouristGroup();
            }
            
            // Occasionnellement, simuler des événements
            if (Math.random() < 0.1) {
                simulateRandomEvent();
            }
        }
        
        System.out.println("\n🔚 ARRÊT DE LA SIMULATION");
        System.out.println("⏰ Temps d'attente pour terminer les visites en cours...");
        
        // Attendre que toutes les visites se terminent
        Thread.sleep(30000);
        
        System.out.println("🏁 FIN DU SYSTÈME D'EXPOSITION");
    }
    
    private void addRandomTouristGroup() throws StaleProxyException {
        String[] nationalities = {
            "Canadienne", "Australienne", "Brésilienne", 
            "Chinoise", "Coréenne", "Hollandaise", "Suédoise"
        };
        
        String nationality = nationalities[(int) (Math.random() * nationalities.length)];
        String groupName = "Groupe_" + nationality + "_" + System.currentTimeMillis() % 1000;
        int size = (int) (Math.random() * 10) + 5; // 5-14 personnes
        
        AgentController group = mainContainer.createNewAgent(
            groupName, 
            "agents.TouristGroupAgent", 
            new Object[]{groupName, size, nationality}
        );
        group.start();
        
        System.out.println("🆕 Nouveau groupe arrivé: " + groupName + 
            " (" + size + " personnes, " + nationality + ")");
    }
    
    private void simulateRandomEvent() {
        String[] events = {
            "🔧 Maintenance préventive sur un tableau",
            "📢 Annonce spéciale pour les visiteurs",
            "🚶‍♂️ Flux important de visiteurs individuels",
            "🎨 Arrivée d'une œuvre temporaire",
            "📱 Mise à jour du système audio-guide",
            "🌟 Visite VIP programmée",
            "🔄 Réorganisation temporaire d'une section"
        };
        
        String event = events[(int) (Math.random() * events.length)];
        System.out.println("🎭 Événement: " + event);
    }
    
    public void stopSystem() {
        try {
            if (mainContainer != null) {
                mainContainer.kill();
            }
            if (rt != null) {
                rt.shutDown();
            }
            System.out.println("🛑 Système arrêté proprement");
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}