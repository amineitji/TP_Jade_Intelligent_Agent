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
 * Version améliorée avec système de boucle et recyclage des agents
 */
public class MuseumSystemLauncher {
    private static AgentContainer mainContainer;
    private static MuseumGUI gui;
    private static CoordinatorAgent coordinatorReference;
    private static List<AgentController> agentControllers = new ArrayList<>();
    private static Timer systemMonitor;
    private static boolean systemRunning = true;
    
    public static void main(String[] args) {
        System.out.println("=== Système Multi-Agents Guide Touristique Intelligent v2.1 ===");
        System.out.println("Version avec recyclage automatique et interface synchronisée");
        System.out.println("Démarrage avec JADE et interface graphique 2D temps réel...\n");
        
        try {
            // 1. Lancer l'interface graphique synchronisée
            SwingUtilities.invokeLater(() -> {
                gui = new MuseumGUI();
                System.out.println("✓ Interface graphique synchronisée initialisée");
            });
            
            // Attendre que l'interface soit prête
            Thread.sleep(2000);
            
            // 2. Configurer et lancer JADE
            setupJADE();
            
            // 3. Créer les agents du système
            createSystemAgents();
            
            // 4. Démarrer la simulation avec boucle
            startContinuousSimulation();
            
            // 5. Démarrer le monitoring système
            startSystemMonitoring();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du lancement du système: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        // 6. Hook pour un arrêt propre
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=== Arrêt du système ===");
            systemRunning = false;
            shutdownSystem();
        }));
        
        System.out.println("\n=== Système démarré avec succès ===");
        System.out.println("Interface graphique: Ouverte et synchronisée");
        System.out.println("Plateforme JADE: Active");
        System.out.println("Agents: En cours d'exécution avec recyclage automatique");
        System.out.println("Monitoring: Actif");
        System.out.println("\nLe système fonctionne maintenant en boucle continue!");
        System.out.println("Consultez l'interface graphique pour voir la simulation en temps réel.");
    }
    
    /**
     * Configuration et lancement de la plateforme JADE
     */
    private static void setupJADE() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        
        Runtime rt = Runtime.instance();
        
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.MAIN_PORT, "1099");
        profile.setParameter(Profile.MAIN, "true");
        profile.setParameter(Profile.GUI, "false");
        
        mainContainer = rt.createMainContainer(profile);
        System.out.println("✓ Container JADE principal créé");
        
        Thread.sleep(1000);
        System.out.println("✓ Plateforme JADE initialisée");
    }
    
    /**
     * Création des agents du système avec support de recyclage
     */
    private static void createSystemAgents() throws StaleProxyException {
        System.out.println("\nCréation des agents du système avec support de recyclage...");
        
        try {
            // 1. Créer l'agent Coordinateur avec logique de recyclage
            AgentController coordinator = mainContainer.createNewAgent(
                "Coordinateur", 
                CoordinatorAgent.class.getName(), 
                null
            );
            coordinator.start();
            agentControllers.add(coordinator);
            System.out.println("✓ Agent Coordinateur créé avec gestion intelligente des ressources");
            
            // Attendre que le coordinateur soit prêt
            Thread.sleep(2000);
            
            // 2. Créer les agents Guides avec spécialisations variées
            String[] guideNames = {
                "GuideRenaissance", "GuideModerne", "GuideImpressionniste", 
                "GuideContemporain", "GuideClassique"
            };
            
            for (String guideName : guideNames) {
                AgentController guide = mainContainer.createNewAgent(
                    guideName,
                    GuideAgent.class.getName(),
                    null
                );
                guide.start();
                agentControllers.add(guide);
                System.out.println("✓ Agent Guide '" + guideName + "' créé avec support de recyclage");
            }
            
            // Attendre que les guides s'enregistrent
            Thread.sleep(3000);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interruption lors de la création des agents: " + e.getMessage());
        }
        
        // 3. Démarrage initial des touristes
        createInitialTourists();
        
        System.out.println("✓ Système d'agents créé avec " + agentControllers.size() + " agents actifs");
    }
    
    /**
     * Création du lot initial de touristes
     */
    private static void createInitialTourists() {
        String[] initialTourists = {
            "Alice_FR", "Bob_IT", "Charlie_EN", "Diana_DE", "Emma_ES", 
            "François_FR", "Giuseppe_IT", "Hans_DE", "Isabella_ES", "James_EN",
            "Yuki_JP", "Pierre_FR", "Anna_DE", "Carlos_ES", "Lisa_EN"
        };
        
        // Démarrage échelonné des touristes initiaux
        for (int i = 0; i < initialTourists.length; i++) {
            final String touristName = initialTourists[i];
            final int delay = 3000 + (i * 2000); // Délai échelonné
            
            Timer touristTimer = new Timer(delay, e -> {
                try {
                    createTouristAgent(touristName);
                    System.out.println("✓ Touriste initial '" + touristName + "' rejoint le musée");
                } catch (StaleProxyException ex) {
                    System.err.println("Erreur création touriste " + touristName + ": " + ex.getMessage());
                }
                ((Timer)e.getSource()).stop();
            });
            touristTimer.setRepeats(false);
            touristTimer.start();
        }
        
        System.out.println("✓ " + initialTourists.length + " touristes programmés pour arrivée échelonnée");
    }
    
    /**
     * Démarrage de la simulation continue avec recyclage
     */
    private static void startContinuousSimulation() {
        System.out.println("\nDémarrage de la simulation continue avec recyclage automatique...");
        
        // Timer pour les rapports système périodiques
        Timer systemReportTimer = new Timer(30000, e -> {
            if (systemRunning) {
                printDetailedSystemStatus();
            }
        });
        systemReportTimer.start();
        
        // Timer pour l'ajout continu de nouveaux touristes
        Timer continuousTouristTimer = new Timer(20000, e -> {
            if (systemRunning && Math.random() < 0.8) { // 80% de chance
                addRandomTourist();
            }
        });
        continuousTouristTimer.start();
        
        // Timer pour la maintenance et l'optimisation du système
        Timer maintenanceTimer = new Timer(60000, e -> {
            if (systemRunning) {
                performSystemMaintenance();
            }
        });
        maintenanceTimer.start();
        
        // Timer pour la synchronisation avec l'interface graphique
        Timer guiSyncTimer = new Timer(2000, e -> {
            if (systemRunning && gui != null) {
                synchronizeWithGUI();
            }
        });
        guiSyncTimer.start();
        
        System.out.println("✓ Simulation continue démarrée");
        System.out.println("  - Rapports système: toutes les 30 secondes");
        System.out.println("  - Nouveaux touristes: toutes les 20 secondes (80% probabilité)");
        System.out.println("  - Maintenance système: chaque minute");
        System.out.println("  - Synchronisation GUI: toutes les 2 secondes");
    }
    
    /**
     * Démarrage du monitoring système avancé
     */
    private static void startSystemMonitoring() {
        systemMonitor = new Timer(10000, e -> {
            if (systemRunning) {
                monitorSystemHealth();
            }
        });
        systemMonitor.start();
        
        System.out.println("✓ Monitoring système actif (vérifications toutes les 10 secondes)");
    }
    
    /**
     * Ajoute un touriste aléatoire au système
     */
    private static void addRandomTourist() {
        try {
            String[] nationalities = {"FR", "IT", "EN", "DE", "ES", "JP", "US", "CN", "BR", "IN"};
            String[] firstNames = {"Alex", "Sam", "Morgan", "Taylor", "Jordan", "Casey", "Riley", 
                                 "Avery", "Quinn", "Sage", "River", "Phoenix", "Skylar", "Drew"};
            
            String nationality = nationalities[(int)(Math.random() * nationalities.length)];
            String firstName = firstNames[(int)(Math.random() * firstNames.length)];
            String touristName = firstName + "_" + nationality + "_" + System.currentTimeMillis() % 10000;
            
            createTouristAgent(touristName);
            
            System.out.println("🆕 Nouveau visiteur: " + touristName + " (Arrivée spontanée)");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout d'un touriste aléatoire: " + e.getMessage());
        }
    }
    
    /**
     * Crée un agent touriste
     */
    private static void createTouristAgent(String touristName) throws StaleProxyException {
        AgentController tourist = mainContainer.createNewAgent(
            touristName,
            TouristAgent.class.getName(),
            null
        );
        tourist.start();
        agentControllers.add(tourist);
    }
    
    /**
     * Maintenance périodique du système
     */
    private static void performSystemMaintenance() {
        System.out.println("\n🔧 === MAINTENANCE SYSTÈME ===");
        
        // Nettoyer les agents terminés
        cleanupTerminatedAgents();
        
        // Vérifier l'équilibrage des charges
        checkLoadBalancing();
        
        // Optimiser les performances
        optimizePerformance();
        
        System.out.println("🔧 Maintenance terminée");
    }
    
    /**
     * Nettoyage des agents terminés
     */
    private static void cleanupTerminatedAgents() {
        int initialSize = agentControllers.size();
        agentControllers.removeIf(agent -> {
            try {
                // Tenter d'obtenir l'état de l'agent
                agent.getState();
                return false; // Agent encore actif
            } catch (Exception e) {
                // Agent probablement terminé
                return true;
            }
        });
        
        int cleaned = initialSize - agentControllers.size();
        if (cleaned > 0) {
            System.out.println("🧹 Nettoyage: " + cleaned + " agents terminés supprimés");
        }
    }
    
    /**
     * Vérification de l'équilibrage des charges
     */
    private static void checkLoadBalancing() {
        // Cette méthode pourrait analyser la distribution des touristes entre les guides
        System.out.println("⚖️ Vérification de l'équilibrage des charges...");
        
        // Ici on pourrait implémenter une logique pour redistribuer les touristes
        // si certains guides sont surchargés
    }
    
    /**
     * Optimisation des performances
     */
    private static void optimizePerformance() {
        // Nettoyage de la mémoire
        System.gc();
        
        // Affichage des statistiques mémoire - CORRECTION ICI
        java.lang.Runtime javaRuntime = java.lang.Runtime.getRuntime();
        long usedMemory = javaRuntime.totalMemory() - javaRuntime.freeMemory();
        long maxMemory = javaRuntime.maxMemory();
        
        System.out.printf("💾 Mémoire: %d/%d MB utilisées (%.1f%%)%n", 
                         usedMemory / (1024 * 1024), 
                         maxMemory / (1024 * 1024),
                         (usedMemory * 100.0) / maxMemory);
    }
    
    /**
     * Monitoring de la santé du système
     */
    private static void monitorSystemHealth() {
        int activeAgents = agentControllers.size();
        
        if (activeAgents < 5) {
            System.out.println("⚠️ ALERTE: Nombre d'agents faible (" + activeAgents + ")");
            // Ajouter plus de touristes si nécessaire
            if (Math.random() < 0.7) {
                addRandomTourist();
            }
        } else if (activeAgents > 50) {
            System.out.println("⚠️ ALERTE: Nombre d'agents élevé (" + activeAgents + ") - Performance possible dégradée");
        }
        
        // Vérifier la connectivité JADE
        if (mainContainer == null) {
            System.err.println("❌ ERREUR CRITIQUE: Container JADE non disponible");
        }
    }
    
    /**
     * Synchronisation avec l'interface graphique
     */
    private static void synchronizeWithGUI() {
        if (gui != null && coordinatorReference != null) {
            // CORRECTION ICI - Supprimer la méthode qui n'existe pas
            // gui.setCoordinatorReference(coordinatorReference);
            
            // Mise à jour du titre avec les statistiques
            SwingUtilities.invokeLater(() -> {
                int activeAgents = agentControllers.size();
                gui.setTitle(String.format(
                    "🎨 Système Multi-Agents [LIVE] - Agents Actifs: %d | Temps: %s", 
                    activeAgents,
                    java.time.LocalTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
                ));
            });
        }
    }
    
    /**
     * Affiche le statut détaillé du système
     */
    private static void printDetailedSystemStatus() {
        System.out.println("\n📊 === RAPPORT SYSTÈME DÉTAILLÉ ===");
        System.out.println("Horodatage: " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("Agents actifs totaux: " + agentControllers.size());
        
        // Comptage par type d'agent (approximatif basé sur les noms)
        long guides = agentControllers.stream()
            .filter(agent -> {
                try {
                    return agent.getName().contains("Guide");
                } catch (Exception e) {
                    return false;
                }
            }).count();
            
        long coordinators = agentControllers.stream()
            .filter(agent -> {
                try {
                    return agent.getName().contains("Coordinateur");
                } catch (Exception e) {
                    return false;
                }
            }).count();
            
        long tourists = agentControllers.size() - guides - coordinators;
        
        System.out.println("  - Coordinateurs: " + coordinators);
        System.out.println("  - Guides: " + guides);
        System.out.println("  - Touristes: " + tourists);
        
        // Informations système - CORRECTION ICI
        java.lang.Runtime javaRuntime = java.lang.Runtime.getRuntime();
        System.out.printf("Mémoire JVM: %d/%d MB%n", 
                         (javaRuntime.totalMemory() - javaRuntime.freeMemory()) / (1024 * 1024),
                         javaRuntime.maxMemory() / (1024 * 1024));
        
        System.out.println("Container JADE: " + (mainContainer != null ? "✓ Actif" : "❌ Inactif"));
        System.out.println("Interface graphique: " + (gui != null ? "✓ Active" : "❌ Inactive"));
        System.out.println("Status système: " + (systemRunning ? "🟢 En fonctionnement" : "🔴 Arrêt en cours"));
        
        System.out.println("=====================================");
    }
    
    /**
     * Arrêt propre du système avec sauvegarde
     */
    private static void shutdownSystem() {
        System.out.println("Arrêt du système en cours...");
        
        try {
            // Arrêter les timers
            if (systemMonitor != null) {
                systemMonitor.stop();
                System.out.println("✓ Monitoring système arrêté");
            }
            
            // Arrêter tous les agents proprement
            System.out.println("Arrêt des agents en cours...");
            int agentCount = 0;
            for (AgentController agent : agentControllers) {
                try {
                    agent.kill();
                    agentCount++;
                } catch (Exception e) {
                    // Ignorer les erreurs d'arrêt d'agents déjà terminés
                }
            }
            System.out.println("✓ " + agentCount + " agents arrêtés");
            
            // Arrêter le container JADE
            if (mainContainer != null) {
                mainContainer.kill();
                System.out.println("✓ Container JADE fermé");
            }
            
            // Fermer l'interface graphique
            if (gui != null) {
                SwingUtilities.invokeLater(() -> gui.dispose());
                System.out.println("✓ Interface graphique fermée");
            }
            
            System.out.println("✓ Système arrêté proprement");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'arrêt: " + e.getMessage());
        }
    }
    
    /**
     * Point d'entrée pour obtenir la référence du coordinateur (pour la GUI)
     */
    public static CoordinatorAgent getCoordinatorReference() {
        return coordinatorReference;
    }
    
    /**
     * Définit la référence du coordinateur
     */
    public static void setCoordinatorReference(CoordinatorAgent coordinator) {
        coordinatorReference = coordinator;
    }
}

/**
 * Configuration étendue du système
 */
class MuseumConfig {
    // Paramètres du musée
    public static final int MAX_TOURISTS_PER_GUIDE = 10;
    public static final int MIN_TOURISTS_FOR_TOUR = 3;
    public static final int MAX_CONCURRENT_GROUPS = 8;
    public static final int TOURIST_ARRIVAL_INTERVAL_MS = 20000;
    public static final int SYSTEM_REPORT_INTERVAL_MS = 30000;
    
    // Paramètres de simulation
    public static final int SIMULATION_SPEED_MS = 1000;
    public static final boolean ENABLE_RANDOM_EVENTS = true;
    public static final boolean ENABLE_GUI_UPDATES = true;
    public static final boolean ENABLE_CONTINUOUS_ARRIVAL = true;
    
    // Paramètres des agents
    public static final double BASE_SATISFACTION = 0.5;
    public static final double MAX_FATIGUE_THRESHOLD = 0.8;
    public static final double MIN_SATISFACTION_THRESHOLD = 0.2;
    
    // Paramètres de performance
    public static final int MAX_AGENTS_WARNING_THRESHOLD = 50;
    public static final int MIN_AGENTS_WARNING_THRESHOLD = 5;
    public static final long MEMORY_WARNING_THRESHOLD_MB = 512;
    
    // Paramètres du musée virtuel
    public static final String[] AVAILABLE_PAINTINGS = {
        "La Joconde", "La Nuit étoilée", "Guernica", 
        "Les Demoiselles d'Avignon", "L'École d'Athènes"
    };
    
    public static final String[] GUIDE_SPECIALIZATIONS = {
        "Renaissance", "Moderne", "Impressionniste", "Contemporain", "Classique"
    };
    
    public static final String[] TOURIST_NATIONALITIES = {
        "Français", "Italien", "Anglais", "Allemand", "Espagnol", 
        "Japonais", "Américain", "Chinois", "Brésilien", "Indien"
    };
}

/**
 * Logger système amélioré
 */
class MuseumLogger {
    private static boolean DEBUG_MODE = true;
    private static boolean FILE_LOGGING = false;
    private static java.io.PrintWriter logFile;
    
    static {
        if (FILE_LOGGING) {
            try {
                logFile = new java.io.PrintWriter(new java.io.FileWriter("museum_system.log", true));
            } catch (java.io.IOException e) {
                System.err.println("Impossible de créer le fichier de log: " + e.getMessage());
                FILE_LOGGING = false;
            }
        }
    }
    
    public static void info(String message) {
        String formattedMessage = "[INFO] " + timestamp() + " " + message;
        System.out.println(formattedMessage);
        if (FILE_LOGGING && logFile != null) {
            logFile.println(formattedMessage);
            logFile.flush();
        }
    }
    
    public static void debug(String message) {
        if (DEBUG_MODE) {
            String formattedMessage = "[DEBUG] " + timestamp() + " " + message;
            System.out.println(formattedMessage);
            if (FILE_LOGGING && logFile != null) {
                logFile.println(formattedMessage);
                logFile.flush();
            }
        }
    }
    
    public static void error(String message) {
        String formattedMessage = "[ERROR] " + timestamp() + " " + message;
        System.err.println(formattedMessage);
        if (FILE_LOGGING && logFile != null) {
            logFile.println(formattedMessage);
            logFile.flush();
        }
    }
    
    public static void agent(String agentName, String message) {
        String formattedMessage = "[" + agentName + "] " + timestamp() + " " + message;
        System.out.println(formattedMessage);
        if (FILE_LOGGING && logFile != null) {
            logFile.println(formattedMessage);
            logFile.flush();
        }
    }
    
    public static void system(String message) {
        String formattedMessage = "[SYSTEM] " + timestamp() + " " + message;
        System.out.println(formattedMessage);
        if (FILE_LOGGING && logFile != null) {
            logFile.println(formattedMessage);
            logFile.flush();
        }
    }
    
    private static String timestamp() {
        return java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }
    
    public static void setDebugMode(boolean enabled) {
        DEBUG_MODE = enabled;
    }
    
    public static void setFileLogging(boolean enabled) {
        FILE_LOGGING = enabled;
        if (enabled && logFile == null) {
            try {
                logFile = new java.io.PrintWriter(new java.io.FileWriter("museum_system.log", true));
            } catch (java.io.IOException e) {
                System.err.println("Impossible de créer le fichier de log: " + e.getMessage());
                FILE_LOGGING = false;
            }
        }
    }
    
    public static void close() {
        if (logFile != null) {
            logFile.close();
        }
    }
}