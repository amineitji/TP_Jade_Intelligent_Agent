import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Lanceur principal pour le système multi-agents de guide touristique intelligent
 * Intègre JADE avec une interface graphique 2D style jeu vidéo
 */
public class MuseumSystemLauncher {
    private static AgentContainer mainContainer;
    private static MuseumGUI gui;
    private static List<AgentController> agentControllers = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("=== Système Multi-Agents Guide Touristique Intelligent ===");
        System.out.println("Démarrage avec JADE et interface graphique 2D...\n");
        
        try {
            // 1. Lancer l'interface graphique
            SwingUtilities.invokeLater(() -> {
                gui = new MuseumGUI();
                System.out.println("✓ Interface graphique initialisée");
            });
            
            // Attendre que l'interface soit prête
            Thread.sleep(1000);
            
            // 2. Configurer et lancer JADE
            setupJADE();
            
            // 3. Créer les agents du système
            createSystemAgents();
            
            // 4. Démarrer la simulation
            startSimulation();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du lancement du système: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        // 5. Hook pour un arrêt propre
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nArrêt du système...");
            shutdownSystem();
        }));
        
        System.out.println("\n=== Système démarré avec succès ===");
        System.out.println("Interface graphique: Ouverte");
        System.out.println("Plateforme JADE: Active");
        System.out.println("Agents: En cours d'exécution");
        System.out.println("\nConsultez l'interface graphique pour voir la simulation en action!");
    }
    
    /**
     * Configuration et lancement de la plateforme JADE
     */
    private static void setupJADE() throws Exception {
        // Configuration de JADE
        System.setProperty("java.net.preferIPv4Stack", "true");
        
        // Récupération du runtime JADE
        Runtime rt = Runtime.instance();
        
        // Création du profil principal
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.MAIN_PORT, "1099");
        profile.setParameter(Profile.MAIN, "true");
        profile.setParameter(Profile.GUI, "false"); // Pas d'interface JADE, on utilise la nôtre
        
        // Création du container principal
        mainContainer = rt.createMainContainer(profile);
        System.out.println("✓ Container JADE principal créé");
        
        // Démarrage des services JADE
        Thread.sleep(1000); // Laisser le temps au container de s'initialiser
        System.out.println("✓ Plateforme JADE initialisée");
    }
    
    /**
     * Création des agents du système
     */
    private static void createSystemAgents() throws StaleProxyException {
        System.out.println("\nCréation des agents du système...");
        
        // 1. Créer l'agent Coordinateur (unique)
        AgentController coordinator = mainContainer.createNewAgent(
            "Coordinateur", 
            CoordinatorAgent.class.getName(), 
            null
        );
        coordinator.start();
        agentControllers.add(coordinator);
        System.out.println("✓ Agent Coordinateur créé et démarré");
        
        // 2. Créer les agents Guides
        String[] guideNames = {"GuideRenaissance", "GuideModerne", "GuideImpressionniste"};
        for (String guideName : guideNames) {
            AgentController guide = mainContainer.createNewAgent(
                guideName,
                GuideAgent.class.getName(),
                null
            );
            guide.start();
            agentControllers.add(guide);
            System.out.println("✓ Agent Guide '" + guideName + "' créé et démarré");
        }
        
        // Attendre un peu pour que les guides s'enregistrent
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 3. Créer les agents Touristes
        String[] touristNames = {
            "Alice_FR", "Bob_IT", "Charlie_EN", "Diana_DE", "Emma_ES", 
            "François_FR", "Giuseppe_IT", "Hans_DE", "Isabella_ES", "James_EN"
        };
        
        for (String touristName : touristNames) {
            // Démarrage échelonné des touristes (simulation d'arrivée naturelle)
            final String name = touristName;
            Timer touristTimer = new Timer(2000 + (int)(Math.random() * 8000), e -> {
                try {
                    AgentController tourist = mainContainer.createNewAgent(
                        name,
                        TouristAgent.class.getName(),
                        null
                    );
                    tourist.start();
                    agentControllers.add(tourist);
                    System.out.println("✓ Agent Touriste '" + name + "' créé et rejoint le musée");
                } catch (StaleProxyException ex) {
                    System.err.println("Erreur création touriste " + name + ": " + ex.getMessage());
                }
                ((Timer)e.getSource()).stop();
            });
            touristTimer.setRepeats(false);
            touristTimer.start();
        }
        
        System.out.println("✓ Agents programmés pour démarrage échelonné");
    }
    
    /**
     * Démarrage de la simulation
     */
    private static void startSimulation() {
        System.out.println("\nDémarrage de la simulation du système multi-agents...");
        
        // Timer pour les rapports système périodiques
        Timer systemReportTimer = new Timer(30000, e -> {
            printSystemStatus();
        });
        systemReportTimer.start();
        
        // Timer pour l'ajout périodique de nouveaux touristes
        Timer newTouristTimer = new Timer(45000, e -> {
            if (Math.random() < 0.7) { // 70% de chance d'ajouter un nouveau touriste
                addRandomTourist();
            }
        });
        newTouristTimer.start();
        
        System.out.println("✓ Simulation démarrée");
        System.out.println("  - Rapports système: toutes les 30 secondes");
        System.out.println("  - Nouveaux touristes: possibles toutes les 45 secondes");
    }
    
    /**
     * Ajoute un touriste aléatoire au système
     */
    private static void addRandomTourist() {
        try {
            String[] nationalities = {"FR", "IT", "EN", "DE", "ES", "JP", "US"};
            String[] firstNames = {"Alex", "Sam", "Morgan", "Taylor", "Jordan", "Casey", "Riley"};
            
            String nationality = nationalities[(int)(Math.random() * nationalities.length)];
            String firstName = firstNames[(int)(Math.random() * firstNames.length)];
            String touristName = firstName + "_" + nationality + "_" + System.currentTimeMillis() % 1000;
            
            AgentController tourist = mainContainer.createNewAgent(
                touristName,
                TouristAgent.class.getName(),
                null
            );
            tourist.start();
            agentControllers.add(tourist);
            
            System.out.println("🆕 Nouveau touriste spontané: " + touristName);
            
        } catch (StaleProxyException e) {
            System.err.println("Erreur lors de l'ajout d'un touriste aléatoire: " + e.getMessage());
        }
    }
    
    /**
     * Affiche le statut du système
     */
    private static void printSystemStatus() {
        System.out.println("\n--- RAPPORT SYSTÈME ---");
        System.out.println("Temps: " + java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        System.out.println("Agents actifs: " + agentControllers.size());
        System.out.println("Container JADE: " + (mainContainer != null ? "Actif" : "Inactif"));
        System.out.println("Interface graphique: " + (gui != null ? "Active" : "Inactive"));
        
        // Statistiques détaillées si disponibles
        if (gui != null) {
            System.out.println("Simulation en cours dans l'interface graphique 2D");
        }
        System.out.println("----------------------");
    }
    
    /**
     * Arrêt propre du système
     */
    private static void shutdownSystem() {
        try {
            // Arrêter tous les agents
            for (AgentController agent : agentControllers) {
                try {
                    agent.kill();
                } catch (Exception e) {
                    // Ignorer les erreurs d'arrêt d'agents déjà terminés
                }
            }
            
            // Arrêter le container JADE
            if (mainContainer != null) {
                mainContainer.kill();
            }
            
            System.out.println("✓ Tous les agents arrêtés");
            System.out.println("✓ Container JADE fermé");
            System.out.println("✓ Système arrêté proprement");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'arrêt: " + e.getMessage());
        }
    }
    
    /**
     * Classe Timer simple pour les tâches périodiques
     */
    private static class Timer {
        private final int delay;
        private final ActionListener listener;
        private boolean repeats = true;
        private java.util.Timer timer;
        private java.util.TimerTask task;
        
        public Timer(int delay, ActionListener listener) {
            this.delay = delay;
            this.listener = listener;
        }
        
        public void start() {
            if (timer != null) {
                timer.cancel();
            }
            
            timer = new java.util.Timer();
            task = new java.util.TimerTask() {
                @Override
                public void run() {
                    listener.actionPerformed(new ActionEvent(Timer.this, 0, "timer"));
                    if (!repeats) {
                        timer.cancel();
                    }
                }
            };
            
            if (repeats) {
                timer.scheduleAtFixedRate(task, delay, delay);
            } else {
                timer.schedule(task, delay);
            }
        }
        
        public void stop() {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
        
        public void setRepeats(boolean repeats) {
            this.repeats = repeats;
        }
    }
    
    /**
     * Interface pour les listeners d'événements timer
     */
    private interface ActionListener {
        void actionPerformed(ActionEvent e);
    }
    
    /**
     * Classe simple pour représenter un événement d'action
     */
    private static class ActionEvent {
        private final Object source;
        private final int id;
        private final String command;
        
        public ActionEvent(Object source, int id, String command) {
            this.source = source;
            this.id = id;
            this.command = command;
        }
        
        public Object getSource() {
            return source;
        }
        
        public int getID() {
            return id;
        }
        
        public String getActionCommand() {
            return command;
        }
    }
}

/**
 * Classe de configuration pour paramétrer le système
 */
class MuseumConfig {
    // Paramètres du musée
    public static final int MAX_TOURISTS_PER_GUIDE = 8;
    public static final int MIN_TOURISTS_FOR_TOUR = 3;
    public static final int MAX_CONCURRENT_GROUPS = 5;
    
    // Paramètres de simulation
    public static final int SIMULATION_SPEED_MS = 1000;
    public static final boolean ENABLE_RANDOM_EVENTS = true;
    public static final boolean ENABLE_GUI_UPDATES = true;
    
    // Paramètres des agents
    public static final double BASE_SATISFACTION = 0.5;
    public static final double MAX_FATIGUE_THRESHOLD = 0.8;
    public static final double MIN_SATISFACTION_THRESHOLD = 0.2;
    
    // Paramètres du musée virtuel
    public static final String[] AVAILABLE_PAINTINGS = {
        "La Joconde", "La Nuit étoilée", "Guernica", 
        "Les Demoiselles d'Avignon", "L'École d'Athènes"
    };
    
    public static final String[] GUIDE_SPECIALIZATIONS = {
        "Renaissance", "Moderne", "Impressionniste", "Contemporain"
    };
    
    public static final String[] TOURIST_NATIONALITIES = {
        "Français", "Italien", "Anglais", "Allemand", "Espagnol", "Japonais"
    };
}

/**
 * Classe utilitaire pour les logs et debugging
 */
class MuseumLogger {
    private static boolean DEBUG_MODE = true;
    
    public static void info(String message) {
        System.out.println("[INFO] " + timestamp() + " " + message);
    }
    
    public static void debug(String message) {
        if (DEBUG_MODE) {
            System.out.println("[DEBUG] " + timestamp() + " " + message);
        }
    }
    
    public static void error(String message) {
        System.err.println("[ERROR] " + timestamp() + " " + message);
    }
    
    public static void agent(String agentName, String message) {
        System.out.println("[" + agentName + "] " + timestamp() + " " + message);
    }
    
    private static String timestamp() {
        return java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }
    
    public static void setDebugMode(boolean enabled) {
        DEBUG_MODE = enabled;
    }
}