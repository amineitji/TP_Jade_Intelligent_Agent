package main;

import gui.ExhibitionGUI;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import agents.*;

import javax.swing.*;

public class ExpositionSystemGUI {
    private AgentContainer mainContainer;
    private Runtime rt;
    private ExhibitionGUI gui;
    private boolean systemRunning = false;
    
    public static void main(String[] args) {
        // Forcer l'utilisation de Swing sur l'EDT
        SwingUtilities.invokeLater(() -> {
            ExpositionSystemGUI system = new ExpositionSystemGUI();
            system.initializeGUI();
            system.startJADESystem();
        });
    }
    
    private void initializeGUI() {
        System.out.println("🖥️  INITIALISATION DE L'INTERFACE GRAPHIQUE");
        
        // Créer et afficher l'interface GUI
        gui = ExhibitionGUI.getInstance();
        gui.setVisible(true);
        
        // Ajouter un shutdown hook pour nettoyer proprement
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (systemRunning) {
                stopSystem();
            }
        }));
    }
    
    private void startJADESystem() {
        System.out.println("🏛️  DÉMARRAGE DU SYSTÈME D'EXPOSITION AVEC GUI");
        System.out.println("====================================================");
        
        try {
            // Configuration JADE
            rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true"); // Interface graphique JADE
            
            // Création du conteneur principal
            mainContainer = rt.createMainContainer(profile);
            systemRunning = true;
            
            // Démarrage du coordinateur
            startCoordinator();
            
            // Attente pour que le coordinateur s'initialise
            Thread.sleep(2000);
            
            // Démarrage des guides avec GUI
            startGuidesGUI();
            
            // Attente pour que les guides s'initialisent
            Thread.sleep(3000);
            
            // Démarrage des groupes de touristes avec GUI
            startTouristGroupsGUI();
            
            System.out.println("✅ Système d'exposition avec GUI complètement initialisé");
            System.out.println("📊 Interface graphique active - Surveillance en cours...\n");
            
            // Simulation continue
            simulateContinuousOperation();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(gui, 
                "Erreur lors du démarrage du système JADE:\n" + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
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
    
    private void startGuidesGUI() throws StaleProxyException {
        String[] guideNames = {
            "GuideMartin", "GuideSophie", "GuideJean", 
            "GuideMarie", "GuideAlex"
        };
        
        for (String guideName : guideNames) {
            AgentController guide = mainContainer.createNewAgent(
                guideName, 
                "agents.GuideAgentGUI", 
                new Object[]{}
            );
            guide.start();
            System.out.println("👨‍🏫 Guide " + guideName + " démarré avec GUI");
            
            // Petit délai pour éviter la surcharge
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void startTouristGroupsGUI() throws StaleProxyException, InterruptedException {
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
                "agents.TouristGroupAgentGUI", 
                new Object[]{groupName, size, nationality}
            );
            group.start();
            
            System.out.println("👥 " + groupName + " démarré avec GUI (" + 
                size + " personnes, " + nationality + ")");
            
            // Délai entre les arrivées de groupes
            Thread.sleep(3000);
        }
    }
    
    private void simulateContinuousOperation() throws InterruptedException, StaleProxyException {
        System.out.println("\n🔄 SIMULATION AVEC GUI EN COURS...");
        
        // Simulation pendant 5 minutes pour permettre l'observation
        for (int i = 0; i < 30; i++) {
            Thread.sleep(10000); // 10 secondes
            
            // Occasionnellement, ajouter de nouveaux groupes
            if (Math.random() < 0.2 && systemRunning) {
                addRandomTouristGroup();
            }
            
            // Occasionnellement, simuler des événements
            if (Math.random() < 0.1 && systemRunning) {
                simulateRandomEvent();
            }
            
            // Vérifier si le système doit continuer
            if (!systemRunning) {
                break;
            }
        }
        
        System.out.println("\n🔚 ARRÊT DE LA SIMULATION");
        System.out.println("⏰ Temps d'attente pour terminer les visites en cours...");
        
        // Attendre que toutes les visites se terminent
        Thread.sleep(15000);
        
        System.out.println("🏁 FIN DU SYSTÈME D'EXPOSITION AVEC GUI");
    }
    
    private void addRandomTouristGroup() throws StaleProxyException {
        String[] nationalities = {
            "Canadienne", "Australienne", "Brésilienne", 
            "Chinoise", "Coréenne", "Hollandaise", "Suédoise",
            "Norvégienne", "Danoise", "Finlandaise"
        };
        
        String nationality = nationalities[(int) (Math.random() * nationalities.length)];
        String groupName = "Groupe_" + nationality + "_" + System.currentTimeMillis() % 1000;
        int size = (int) (Math.random() * 10) + 5; // 5-14 personnes
        
        try {
            AgentController group = mainContainer.createNewAgent(
                groupName, 
                "agents.TouristGroupAgentGUI", 
                new Object[]{groupName, size, nationality}
            );
            group.start();
            
            System.out.println("🆕 Nouveau groupe arrivé: " + groupName + 
                " (" + size + " personnes, " + nationality + ")");
                
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du groupe: " + e.getMessage());
        }
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
        
        // Affichage dans l'interface si possible
        SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                // Cette méthode pourrait être ajoutée à ExhibitionGUI pour afficher les événements
                // gui.addSystemEvent(event);
            }
        });
    }
    
    public void stopSystem() {
        systemRunning = false;
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
    
    // Méthode pour redémarrer le système (peut être appelée depuis l'interface)
    public void restartSystem() {
        System.out.println("🔄 Redémarrage du système...");
        stopSystem();
        
        // Attendre un peu avant de redémarrer
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        startJADESystem();
    }
    
    // Getters pour l'interface
    public boolean isSystemRunning() {
        return systemRunning;
    }
    
    public ExhibitionGUI getGui() {
        return gui;
    }
}