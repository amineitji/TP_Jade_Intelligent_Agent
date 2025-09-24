package launcher;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Interface graphique principale pour le système multi-agents du musée
 * Affiche les agents, statistiques, scénarios et permet le contrôle interactif
 */
public class MuseumSystemLauncher extends JFrame {
    
    // Composants JADE
    private static AgentContainer mainContainer;
    private static List<AgentController> agentControllers = new ArrayList<>();
    private static boolean systemRunning = false;
    
    // Composants GUI
    private JPanel agentsPanel;
    private JPanel statisticsPanel;
    private JPanel controlPanel;
    private JTextArea logArea;
    private JLabel systemStatusLabel;
    private JProgressBar tourProgressBar;
    
    // Données du système
    private Map<String, AgentInfo> agentInfoMap = new HashMap<>();
    private SystemStats stats = new SystemStats();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public MuseumSystemLauncher() {
        initializeGUI();
        setupSystemMonitoring();
    }
    
    /**
     * Initialise l'interface graphique
     */
    private void initializeGUI() {
        setTitle("Système Multi-Agents - Musée Virtuel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel principal avec onglets
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Onglet 1: Vue d'ensemble
        tabbedPane.addTab("Vue d'ensemble", createOverviewPanel());
        
        // Onglet 2: Agents
        tabbedPane.addTab("Agents", createAgentsPanel());
        
        // Onglet 3: Statistiques
        tabbedPane.addTab("Statistiques", createStatisticsPanel());
        
        // Onglet 4: Scénarios
        tabbedPane.addTab("Scénarios", createScenariosPanel());
        
        // Onglet 5: Logs
        tabbedPane.addTab("Logs", createLogsPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Panel de contrôle en bas
        add(createControlPanel(), BorderLayout.SOUTH);
        
        // Configuration de la fenêtre
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Gestionnaire de fermeture
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownSystem();
                System.exit(0);
            }
        });
    }
    
    /**
     * Crée le panel de vue d'ensemble
     */
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Status du système
        JPanel statusPanel = new JPanel(new FlowLayout());
        statusPanel.setBorder(new TitledBorder("État du Système"));
        
        systemStatusLabel = new JLabel("Système arrêté");
        systemStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        statusPanel.add(systemStatusLabel);
        
        // Barre de progression des visites
        tourProgressBar = new JProgressBar(0, 100);
        tourProgressBar.setStringPainted(true);
        tourProgressBar.setString("Aucune visite en cours");
        statusPanel.add(new JLabel("Progression:"));
        statusPanel.add(tourProgressBar);
        
        panel.add(statusPanel, BorderLayout.NORTH);
        
        // Graphique de visualisation simple
        JPanel visualPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSystemVisualization(g);
            }
        };
        visualPanel.setBorder(new TitledBorder("Visualisation du Musée"));
        visualPanel.setBackground(Color.WHITE);
        panel.add(visualPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crée le panel des agents
     */
    private JPanel createAgentsPanel() {
        agentsPanel = new JPanel();
        agentsPanel.setLayout(new BoxLayout(agentsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(agentsPanel);
        scrollPane.setBorder(new TitledBorder("Agents Actifs"));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Crée le panel des statistiques
     */
    private JPanel createStatisticsPanel() {
        statisticsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        statisticsPanel.setBorder(new TitledBorder("Statistiques Temps Réel"));
        
        // Statistiques de base
        updateStatisticsDisplay();
        
        return statisticsPanel;
    }
    
    /**
     * Crée le panel des scénarios
     */
    private JPanel createScenariosPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Liste des scénarios prédéfinis
        JPanel scenariosListPanel = new JPanel();
        scenariosListPanel.setLayout(new BoxLayout(scenariosListPanel, BoxLayout.Y_AXIS));
        scenariosListPanel.setBorder(new TitledBorder("Scénarios Disponibles"));
        
        // Scénario 1: Visite normale
        JPanel scenario1 = createScenarioPanel(
            "Visite Standard", 
            "3 guides spécialisés + 6-8 touristes", 
            () -> launchStandardScenario()
        );
        
        // Scénario 2: Affluence élevée
        JPanel scenario2 = createScenarioPanel(
            "Forte Affluence", 
            "3 guides + 15-20 touristes arrivant rapidement", 
            () -> launchHighTrafficScenario()
        );
        
        // Scénario 3: Test de stress
        JPanel scenario3 = createScenarioPanel(
            "Test de Stress", 
            "Création continue de nouveaux touristes", 
            () -> launchStressTestScenario()
        );
        
        // Scénario 4: Guides spécialisés
        JPanel scenario4 = createScenarioPanel(
            "Spécialisation", 
            "Test des spécialisations des guides", 
            () -> launchSpecializationScenario()
        );
        
        scenariosListPanel.add(scenario1);
        scenariosListPanel.add(scenario2);
        scenariosListPanel.add(scenario3);
        scenariosListPanel.add(scenario4);
        
        panel.add(new JScrollPane(scenariosListPanel), BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crée un panel pour un scénario
     */
    private JPanel createScenarioPanel(String name, String description, Runnable action) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.add(new JLabel("<html><b>" + name + "</b></html>"));
        infoPanel.add(new JLabel("<html><i>" + description + "</i></html>"));
        
        JButton launchButton = new JButton("Lancer");
        launchButton.addActionListener(e -> {
            if (systemRunning) {
                action.run();
                logMessage("Scénario lancé: " + name);
            } else {
                JOptionPane.showMessageDialog(this, "Démarrez d'abord le système JADE!", 
                                            "Système non démarré", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(launchButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Crée le panel des logs
     */
    private JPanel createLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new TitledBorder("Journal Système"));
        
        // Bouton pour effacer les logs
        JButton clearButton = new JButton("Effacer");
        clearButton.addActionListener(e -> logArea.setText(""));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(clearButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crée le panel de contrôle
     */
    private JPanel createControlPanel() {
        controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBorder(new TitledBorder("Contrôles Système"));
        
        JButton startButton = new JButton("Démarrer JADE");
        startButton.addActionListener(e -> startJadeSystem());
        
        JButton stopButton = new JButton("Arrêter Système");
        stopButton.addActionListener(e -> shutdownSystem());
        
        JButton addTouristButton = new JButton("+ Touriste");
        addTouristButton.addActionListener(e -> addRandomTourist());
        
        JButton systemInfoButton = new JButton("Info Système");
        systemInfoButton.addActionListener(e -> showSystemInfo());
        
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(addTouristButton);
        controlPanel.add(systemInfoButton);
        
        return controlPanel;
    }
    
    /**
     * Dessine la visualisation du système
     */
    private void drawSystemVisualization(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth() - 50;
        int height = getHeight() - 100;
        
        // Dessiner le layout du musée
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(50, 50, width, height);
        
        // Point A (Entrée)
        g2d.setColor(Color.BLUE);
        g2d.fillRect(60, 60, 40, 30);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Entrée", 65, 80);
        
        // Tableaux
        String[] tableaux = {"T1", "T2", "T3", "T4", "T5"};
        for (int i = 0; i < tableaux.length; i++) {
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(150 + i * 100, 100, 60, 40);
            g2d.setColor(Color.BLACK);
            g2d.drawString(tableaux[i], 165 + i * 100, 125);
        }
        
        // Salle de repos
        g2d.setColor(Color.GREEN);
        g2d.fillRect(300, 200, 80, 40);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Repos", 320, 225);
        
        // Sortie
        g2d.setColor(Color.RED);
        g2d.fillRect(width - 50, height, 40, 30);
        g2d.drawString("Sortie", width - 45, height + 20);
        
        // Afficher les agents actifs
        drawAgentsOnMap(g2d);
    }
    
    /**
     * Dessine les agents sur la carte
     */
    private void drawAgentsOnMap(Graphics2D g2d) {
        int guideCount = 0;
        int touristCount = 0;
        
        for (AgentInfo info : agentInfoMap.values()) {
            if (info.type.equals("Guide")) {
                g2d.setColor(Color.BLUE);
                g2d.fillOval(200 + guideCount * 30, 300, 15, 15);
                g2d.setColor(Color.BLACK);
                g2d.drawString("G", 205 + guideCount * 30, 310);
                guideCount++;
            } else if (info.type.equals("Tourist")) {
                g2d.setColor(Color.ORANGE);
                g2d.fillOval(200 + touristCount * 20, 330, 12, 12);
                g2d.setColor(Color.BLACK);
                g2d.drawString("T", 203 + touristCount * 20, 340);
                touristCount++;
            }
        }
    }
    
    /**
     * Démarre le système JADE
     */
    private void startJadeSystem() {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Démarrage du système JADE...");
                
                // Configuration JADE
                System.setProperty("java.net.preferIPv4Stack", "true");
                jade.core.Runtime rt = jade.core.Runtime.instance();
                
                Profile profile = new ProfileImpl();
                profile.setParameter(Profile.MAIN_HOST, "localhost");
                profile.setParameter(Profile.MAIN_PORT, "1099");
                profile.setParameter(Profile.MAIN, "true");
                profile.setParameter(Profile.GUI, "false");
                
                mainContainer = rt.createMainContainer(profile);
                publish("Container JADE créé");
                
                Thread.sleep(1000);
                
                // Créer les agents de base
                createInitialAgents();
                
                systemRunning = true;
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    logMessage(message);
                }
            }
            
            @Override
            protected void done() {
                systemStatusLabel.setText("Système démarré");
                systemStatusLabel.setForeground(Color.GREEN);
                repaint();
                JOptionPane.showMessageDialog(MuseumSystemLauncher.this, 
                                            "Système JADE démarré avec succès!", 
                                            "Démarrage réussi", 
                                            JOptionPane.INFORMATION_MESSAGE);
            }
        };
        
        worker.execute();
    }
    
    /**
     * Crée les agents initiaux
     */
    private void createInitialAgents() {
        try {
            // Créer les guides
            String[] guideNames = {"GuideRenaissance", "GuideModerne", "GuideImpressionniste"};
            for (String guideName : guideNames) {
                AgentController guide = mainContainer.createNewAgent(
                    guideName, "agents.guide.GuideAgent", null);
                guide.start();
                agentControllers.add(guide);
                
                agentInfoMap.put(guideName, new AgentInfo(guideName, "Guide", "Disponible"));
                
                SwingUtilities.invokeLater(() -> updateAgentsDisplay());
                Thread.sleep(1000);
            }
            
            // Créer quelques touristes initiaux
            createInitialTourists();
            
        } catch (Exception e) {
            logMessage("Erreur création agents: " + e.getMessage());
        }
    }
    
    /**
     * Crée les touristes initiaux
     */
    private void createInitialTourists() throws StaleProxyException, InterruptedException {
        String[] touristNames = {"Alice_FR", "Bob_IT", "Charlie_EN", "Diana_DE"};
        
        for (String touristName : touristNames) {
            AgentController tourist = mainContainer.createNewAgent(
                touristName, "agents.tourist.TouristAgent", null);
            tourist.start();
            agentControllers.add(tourist);
            
            agentInfoMap.put(touristName, new AgentInfo(touristName, "Tourist", "Arrivé"));
            
            SwingUtilities.invokeLater(() -> updateAgentsDisplay());
            Thread.sleep(2000);
        }
    }
    
    /**
     * Met à jour l'affichage des agents
     */
    private void updateAgentsDisplay() {
        agentsPanel.removeAll();
        
        for (AgentInfo info : agentInfoMap.values()) {
            JPanel agentPanel = new JPanel(new BorderLayout());
            agentPanel.setBorder(BorderFactory.createEtchedBorder());
            agentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            
            // Informations de l'agent
            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            infoPanel.add(new JLabel("<html><b>" + info.name + "</b> (" + info.type + ")</html>"));
            infoPanel.add(new JLabel("État: " + info.status));
            
            // Indicateur visuel
            JPanel statusPanel = new JPanel();
            JLabel statusIndicator = new JLabel("●");
            statusIndicator.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            
            if (info.type.equals("Guide")) {
                statusIndicator.setForeground(Color.BLUE);
            } else {
                statusIndicator.setForeground(Color.ORANGE);
            }
            
            statusPanel.add(statusIndicator);
            
            agentPanel.add(infoPanel, BorderLayout.CENTER);
            agentPanel.add(statusPanel, BorderLayout.EAST);
            
            agentsPanel.add(agentPanel);
        }
        
        agentsPanel.revalidate();
        agentsPanel.repaint();
    }
    
    /**
     * Met à jour l'affichage des statistiques
     */
    private void updateStatisticsDisplay() {
        statisticsPanel.removeAll();
        
        statisticsPanel.add(new JLabel("Agents totaux: " + agentInfoMap.size()));
        statisticsPanel.add(new JLabel("Guides actifs: " + countAgentsByType("Guide")));
        statisticsPanel.add(new JLabel("Touristes: " + countAgentsByType("Tourist")));
        statisticsPanel.add(new JLabel("Visites en cours: " + stats.activeVisits));
        statisticsPanel.add(new JLabel("Satisfaction moyenne: " + String.format("%.2f", stats.averageSatisfaction)));
        statisticsPanel.add(new JLabel("Temps moyen de visite: " + stats.averageVisitTime + " min"));
        
        statisticsPanel.revalidate();
        statisticsPanel.repaint();
    }
    
    /**
     * Compte les agents par type
     */
    private int countAgentsByType(String type) {
        return (int) agentInfoMap.values().stream()
            .filter(info -> info.type.equals(type))
            .count();
    }
    
    /**
     * Configure la surveillance du système
     */
    private void setupSystemMonitoring() {
        // Mise à jour périodique de l'interface
        scheduler.scheduleAtFixedRate(() -> {
            if (systemRunning) {
                SwingUtilities.invokeLater(() -> {
                    updateStatisticsDisplay();
                    updateSystemStatus();
                    repaint();
                });
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    /**
     * Met à jour le statut du système
     */
    private void updateSystemStatus() {
        if (systemRunning) {
            // Simuler quelques statistiques
            stats.activeVisits = Math.max(0, countAgentsByType("Tourist") / 3);
            stats.averageSatisfaction = 0.7 + Math.random() * 0.3;
            stats.averageVisitTime = 15 + (int)(Math.random() * 10);
            
            // Mettre à jour la barre de progression
            if (stats.activeVisits > 0) {
                int progress = (int)(Math.random() * 100);
                tourProgressBar.setValue(progress);
                tourProgressBar.setString(stats.activeVisits + " visite(s) - " + progress + "%");
            }
        }
    }
    
    // Méthodes pour les scénarios
    
    private void launchStandardScenario() {
        logMessage("Lancement du scénario standard...");
        // Le système est déjà configuré pour ce scénario
    }
    
    private void launchHighTrafficScenario() {
        logMessage("Lancement du scénario haute affluence...");
        // Créer plusieurs touristes rapidement
        for (int i = 0; i < 10; i++) {
            final int index = i;
            scheduler.schedule(() -> addRandomTourist(), i * 2, TimeUnit.SECONDS);
        }
    }
    
    private void launchStressTestScenario() {
        logMessage("Lancement du test de stress...");
        // Créer des touristes en continu
        scheduler.scheduleAtFixedRate(() -> {
            if (agentInfoMap.size() < 50) { // Limite de sécurité
                addRandomTourist();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
    
    private void launchSpecializationScenario() {
        logMessage("Test des spécialisations...");
        // Créer des touristes avec des préférences spécifiques
        String[] specialTourists = {"Expert_Renaissance", "Amateur_Moderne", "Curieux_Impressionniste"};
        for (String name : specialTourists) {
            addSpecificTourist(name);
        }
    }
    
    /**
     * Ajoute un touriste aléatoire
     */
    private void addRandomTourist() {
        if (!systemRunning) return;
        
        try {
            String[] nationalities = {"FR", "IT", "EN", "DE", "ES", "JP", "US"};
            String[] firstNames = {"Alex", "Sam", "Morgan", "Taylor", "Jordan", "Casey", "Robin"};
            
            String nationality = nationalities[(int)(Math.random() * nationalities.length)];
            String firstName = firstNames[(int)(Math.random() * firstNames.length)];
            String touristName = firstName + "_" + nationality + "_" + System.currentTimeMillis() % 1000;
            
            AgentController tourist = mainContainer.createNewAgent(
                touristName, "agents.tourist.TouristAgent", null);
            tourist.start();
            agentControllers.add(tourist);
            
            agentInfoMap.put(touristName, new AgentInfo(touristName, "Tourist", "Nouveau visiteur"));
            
            SwingUtilities.invokeLater(() -> {
                updateAgentsDisplay();
                logMessage("Nouveau touriste: " + touristName);
            });
            
        } catch (Exception e) {
            logMessage("Erreur création touriste: " + e.getMessage());
        }
    }
    
    /**
     * Ajoute un touriste spécifique
     */
    private void addSpecificTourist(String name) {
        if (!systemRunning) return;
        
        try {
            AgentController tourist = mainContainer.createNewAgent(
                name, "agents.tourist.TouristAgent", null);
            tourist.start();
            agentControllers.add(tourist);
            
            agentInfoMap.put(name, new AgentInfo(name, "Tourist", "Visiteur spécialisé"));
            
            SwingUtilities.invokeLater(() -> {
                updateAgentsDisplay();
                logMessage("Touriste spécialisé: " + name);
            });
            
        } catch (Exception e) {
            logMessage("Erreur création touriste spécialisé: " + e.getMessage());
        }
    }
    
    /**
     * Affiche les informations système
     */
    private void showSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        
        String info = String.format(
            "=== INFORMATIONS SYSTÈME ===\n" +
            "Statut JADE: %s\n" +
            "Agents actifs: %d\n" +
            "Guides: %d\n" +
            "Touristes: %d\n" +
            "Mémoire utilisée: %d MB / %d MB\n" +
            "Visites actives: %d\n" +
            "Satisfaction moyenne: %.2f",
            systemRunning ? "Actif" : "Inactif",
            agentInfoMap.size(),
            countAgentsByType("Guide"),
            countAgentsByType("Tourist"),
            usedMemory, totalMemory,
            stats.activeVisits,
            stats.averageSatisfaction
        );
        
        JOptionPane.showMessageDialog(this, info, "Informations Système", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Ajoute un message au log
     */
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * Arrête le système
     */
    private void shutdownSystem() {
        if (!systemRunning) return;
        
        logMessage("Arrêt du système en cours...");
        systemRunning = false;
        
        try {
            // Arrêter tous les agents
            for (AgentController agent : agentControllers) {
                try {
                    agent.kill();
                } catch (Exception e) {
                    // Ignorer les erreurs d'arrêt
                }
            }
            
            // Arrêter le container JADE
            if (mainContainer != null) {
                mainContainer.kill();
            }
            
            // Arrêter le scheduler
            scheduler.shutdown();
            
            agentInfoMap.clear();
            agentControllers.clear();
            
            SwingUtilities.invokeLater(() -> {
                systemStatusLabel.setText("Système arrêté");
                systemStatusLabel.setForeground(Color.RED);
                updateAgentsDisplay();
                updateStatisticsDisplay();
            });
            
            logMessage("Système arrêté avec succès");
            
        } catch (Exception e) {
            logMessage("Erreur lors de l'arrêt: " + e.getMessage());
        }
    }
    
    /**
     * Classe pour stocker les informations d'agent
     */
    private static class AgentInfo {
        String name;
        String type;
        String status;
        
        AgentInfo(String name, String type, String status) {
            this.name = name;
            this.type = type;
            this.status = status;
        }
    }
    
    /**
     * Classe pour les statistiques système
     */
    private static class SystemStats {
        int activeVisits = 0;
        double averageSatisfaction = 0.75;
        int averageVisitTime = 18;
    }
    
    /**
     * Point d'entrée principal
     */
    public static void main(String[] args) {
        // Configuration du Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Utiliser le Look and Feel par défaut
        }
        
        SwingUtilities.invokeLater(() -> {
            new MuseumSystemLauncher().setVisible(true);
        });
    }
}