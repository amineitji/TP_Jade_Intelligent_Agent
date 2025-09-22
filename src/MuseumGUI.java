import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import jade.core.AID;

public class MuseumGUI extends JFrame {
    private static final int WINDOW_WIDTH = 1600;
    private static final int WINDOW_HEIGHT = 1000;
    private static final int PANEL_WIDTH = 1000;
    private static final int PANEL_HEIGHT = 700;
    
    // Couleurs du thème moderne
    private static final Color DARK_BG = new Color(32, 32, 32);
    private static final Color LIGHT_BG = new Color(248, 248, 248);
    private static final Color ACCENT_BLUE = new Color(64, 158, 255);
    private static final Color ACCENT_GREEN = new Color(76, 217, 100);
    private static final Color ACCENT_RED = new Color(255, 69, 58);
    private static final Color ACCENT_ORANGE = new Color(255, 149, 0);
    private static final Color ACCENT_PURPLE = new Color(175, 82, 222);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 20);
    
    // État de l'interface
    private MuseumPanel museumPanel;
    private JPanel rightPanel;
    private JScrollPane logScrollPane;
    private JTextArea logArea;
    private JLabel statsLabel;
    private JPanel statsPanel;
    private JPanel controlPanel;
    private Timer animationTimer;
    private boolean isDarkMode = false;
    
    // Données de l'interface
    private Map<String, Point2D.Double> locationPositions = new HashMap<>();
    private Map<AID, AgentSprite> agentSprites = new ConcurrentHashMap<>();
    private java.util.List<String> logMessages = new ArrayList<>();
    private java.util.List<ParticleEffect> particles = new ArrayList<>();
    
    // Animation et effets
    private double animationTime = 0.0;
    private Map<String, Double> heatMap = new HashMap<>();
    private java.util.List<PathTrail> pathTrails = new ArrayList<>();
    
    public MuseumGUI() {
        initializeLocationPositions();
        setupUI();
        startAnimation();
        createInitialEffects();
        
        System.out.println("Interface graphique avancée du musée initialisée");
    }
    
    private void initializeLocationPositions() {
        // Positions optimisées pour une meilleure circulation
        locationPositions.put("PointA", new Point2D.Double(120, 120));
        locationPositions.put("Tableau1", new Point2D.Double(250, 150));
        locationPositions.put("Tableau2", new Point2D.Double(450, 130));
        locationPositions.put("Tableau3", new Point2D.Double(650, 180));
        locationPositions.put("Tableau4", new Point2D.Double(580, 350));
        locationPositions.put("Tableau5", new Point2D.Double(350, 420));
        locationPositions.put("SalleRepos", new Point2D.Double(180, 480));
        locationPositions.put("Sortie", new Point2D.Double(750, 550));
        locationPositions.put("Accueil", new Point2D.Double(50, 300));
        locationPositions.put("Boutique", new Point2D.Double(850, 200));
    }
    
    private void setupUI() {
        setTitle("🎨 Système Multi-Agents - Guide Touristique Intelligent v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Configuration de la fenêtre moderne
        setUndecorated(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(LIGHT_BG);
        
        // Panel principal avec la vue 2D avancée
        museumPanel = new MuseumPanel();
        museumPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        museumPanel.setBackground(LIGHT_BG);
        
        // Panel de droite avec design moderne
        rightPanel = createModernRightPanel();
        
        // Panel de contrôles stylisé
        controlPanel = createStylizedControlPanel();
        
        // Assembly avec layout moderne
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(LIGHT_BG);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 0));
        centerWrapper.add(museumPanel, BorderLayout.CENTER);
        
        add(centerWrapper, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);
        
        // Effets de fenêtre
        setIconImage(createAppIcon());
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        addLog("🚀 Interface graphique avancée initialisée");
    }
    
    private JPanel createModernRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(400, PANEL_HEIGHT));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header moderne
        JPanel headerPanel = createModernHeader();
        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(20));
        
        // Panneau de statistiques avec cartes
        statsPanel = createModernStatsPanel();
        panel.add(statsPanel);
        panel.add(Box.createVerticalStrut(20));
        
        // Panneau de statut des tableaux
        JPanel tableauStatusPanel = createModernTableauPanel();
        panel.add(tableauStatusPanel);
        panel.add(Box.createVerticalStrut(20));
        
        // Zone de logs stylisée
        JPanel logsPanel = createModernLogsPanel();
        panel.add(logsPanel);
        
        return panel;
    }
    
    private JPanel createModernHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(createModernBorder());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JLabel titleLabel = new JLabel("🏛️ Musée Intelligent", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(DARK_BG);
        
        JLabel subtitleLabel = new JLabel("Système Multi-Agents JADE", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        
        JButton themeToggle = createModernButton("🌙", "Basculer mode sombre");
        themeToggle.addActionListener(e -> toggleDarkMode());
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setBackground(CARD_BG);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        panel.add(titlePanel, BorderLayout.CENTER);
        panel.add(themeToggle, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createModernStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LIGHT_BG);
        
        // Titre de section
        JLabel sectionTitle = new JLabel("📊 Statistiques Temps Réel");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sectionTitle);
        panel.add(Box.createVerticalStrut(10));
        
        // Cartes de statistiques
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        cardsPanel.setBackground(LIGHT_BG);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        
        cardsPanel.add(createStatCard("👥", "Guides", "0", ACCENT_BLUE));
        cardsPanel.add(createStatCard("🧳", "Touristes", "0", ACCENT_GREEN));
        cardsPanel.add(createStatCard("😊", "Satisfaction", "0.00", ACCENT_ORANGE));
        cardsPanel.add(createStatCard("⚡", "Activité", "Faible", ACCENT_PURPLE));
        
        panel.add(cardsPanel);
        
        return panel;
    }
    
    private JPanel createStatCard(String icon, String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(CARD_BG);
        card.setBorder(createModernBorder());
        
        JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setPreferredSize(new Dimension(40, 40));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(Color.GRAY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(accentColor);
        
        textPanel.add(titleLabel);
        textPanel.add(valueLabel);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createModernTableauPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LIGHT_BG);
        
        JLabel sectionTitle = new JLabel("🖼️ État des Œuvres");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sectionTitle);
        panel.add(Box.createVerticalStrut(10));
        
        JPanel tableauGrid = new JPanel(new GridLayout(5, 1, 0, 5));
        tableauGrid.setBackground(LIGHT_BG);
        tableauGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        for (int i = 1; i <= 5; i++) {
            JPanel tableauItem = createTableauStatusItem("Tableau " + i, true, i * 12 + "%");
            tableauGrid.add(tableauItem);
        }
        
        panel.add(tableauGrid);
        return panel;
    }
    
    private JPanel createTableauStatusItem(String name, boolean available, String popularity) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(createThinBorder());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel statusLabel = new JLabel(available ? "✅" : "❌");
        JLabel popularityLabel = new JLabel(popularity);
        popularityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        popularityLabel.setForeground(Color.GRAY);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.setBackground(CARD_BG);
        rightPanel.add(popularityLabel);
        rightPanel.add(statusLabel);
        
        panel.add(nameLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createModernLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_BG);
        
        JLabel sectionTitle = new JLabel("📝 Journal Système");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(sectionTitle, BorderLayout.NORTH);
        
        logArea = new JTextArea(12, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(0, 255, 0));
        logArea.setCaretColor(Color.GREEN);
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScrollPane.setBorder(createModernBorder());
        
        panel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        panel.add(logScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStylizedControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        // Boutons de contrôle stylisés
        JButton startBtn = createModernActionButton("▶️ Démarrer", ACCENT_GREEN);
        JButton pauseBtn = createModernActionButton("⏸️ Pause", ACCENT_ORANGE);
        JButton resetBtn = createModernActionButton("🔄 Reset", ACCENT_RED);
        JButton addTouristBtn = createModernActionButton("➕ Touriste", ACCENT_BLUE);
        
        startBtn.addActionListener(e -> startSimulation());
        pauseBtn.addActionListener(e -> pauseSimulation());
        resetBtn.addActionListener(e -> resetSimulation());
        addTouristBtn.addActionListener(e -> addRandomTourist());
        
        // Slider de vitesse moderne
        JPanel speedPanel = createModernSpeedControl();
        
        // Sélecteur de vue
        JPanel viewPanel = createViewSelector();
        
        panel.add(startBtn);
        panel.add(pauseBtn);
        panel.add(resetBtn);
        panel.add(addTouristBtn);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(speedPanel);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(viewPanel);
        
        return panel;
    }
    
    private JButton createModernActionButton(String text, Color accentColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(accentColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Effet hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(accentColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(accentColor);
            }
        });
        
        return button;
    }
    
    private JButton createModernButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(40, 40));
        button.setBackground(LIGHT_BG);
        button.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JPanel createModernSpeedControl() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(CARD_BG);
        
        JLabel label = new JLabel("🚀 Vitesse:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JSlider slider = new JSlider(1, 10, 5);
        slider.setPreferredSize(new Dimension(100, 30));
        slider.setBackground(CARD_BG);
        slider.addChangeListener(e -> updateAnimationSpeed(slider.getValue()));
        
        panel.add(label, BorderLayout.WEST);
        panel.add(slider, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createViewSelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(CARD_BG);
        
        JLabel label = new JLabel("👁️ Vue:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JComboBox<String> viewCombo = new JComboBox<>(new String[]{
            "Vue Générale", "Vue Détaillée", "Carte Thermique", "Flux de Circulation"
        });
        viewCombo.setPreferredSize(new Dimension(130, 30));
        viewCombo.addActionListener(e -> changeViewMode(viewCombo.getSelectedIndex()));
        
        panel.add(label);
        panel.add(viewCombo);
        
        return panel;
    }
    
    private Border createModernBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );
    }
    
    private Border createThinBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(240, 240, 240), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
    }
    
    private BufferedImage createAppIcon() {
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(ACCENT_BLUE);
        g2d.fillRoundRect(4, 4, 24, 24, 8, 8);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        g2d.drawString("🏛️", 8, 22);
        
        g2d.dispose();
        return icon;
    }
    
    private void startAnimation() {
        animationTimer = new Timer(16, e -> { // 60 FPS
            animationTime += 0.016;
            updateDisplay();
            updateParticles();
            updatePathTrails();
            museumPanel.repaint();
        });
        animationTimer.start();
    }
    
    private void createInitialEffects() {
        // Créer des particules d'ambiance
        for (int i = 0; i < 20; i++) {
            particles.add(new ParticleEffect(
                Math.random() * PANEL_WIDTH,
                Math.random() * PANEL_HEIGHT,
                ParticleEffect.Type.AMBIENT
            ));
        }
    }
    
    // Méthodes d'action
    private void startSimulation() {
        addLog("🚀 Démarrage de la simulation avancée...");
        createDemoAgents();
        createWelcomeEffect();
    }
    
    private void pauseSimulation() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
            addLog("⏸️ Simulation en pause");
        } else {
            animationTimer.start();
            addLog("▶️ Simulation reprise");
        }
    }
    
    private void resetSimulation() {
        agentSprites.clear();
        particles.clear();
        pathTrails.clear();
        logMessages.clear();
        logArea.setText("");
        createInitialEffects();
        addLog("🔄 Simulation réinitialisée");
        museumPanel.repaint();
    }
    
    private void addRandomTourist() {
        String touristName = "Visiteur" + (agentSprites.size() + 1);
        Point2D.Double startPos = locationPositions.get("PointA");
        
        AgentSprite sprite = new AgentSprite(
            touristName, AgentType.TOURIST, startPos.x, startPos.y
        );
        agentSprites.put(new AID(touristName, AID.ISLOCALNAME), sprite);
        
        // Effet d'apparition
        createSpawnEffect(startPos.x, startPos.y);
        addLog("➕ Nouveau visiteur: " + touristName);
    }
    
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        applyTheme();
        addLog(isDarkMode ? "🌙 Mode sombre activé" : "☀️ Mode clair activé");
    }
    
    private void updateAnimationSpeed(int speed) {
        if (animationTimer != null) {
            int delay = Math.max(8, 80 - (speed * 8)); // 8ms à 72ms (12.5 à 125 FPS)
            animationTimer.setDelay(delay);
        }
    }
    
    private void changeViewMode(int mode) {
        museumPanel.setViewMode(mode);
        String[] modes = {"Générale", "Détaillée", "Thermique", "Flux"};
        addLog("👁️ Vue changée: " + modes[mode]);
    }
    
    private void applyTheme() {
        // Application du thème sombre/clair
        Color bgColor = isDarkMode ? DARK_BG : LIGHT_BG;
        Color cardColor = isDarkMode ? new Color(48, 48, 48) : CARD_BG;
        
        getContentPane().setBackground(bgColor);
        rightPanel.setBackground(bgColor);
        museumPanel.setBackground(bgColor);
        // ... autres composants
        
        repaint();
    }
    
    private void createDemoAgents() {
        // Créer des agents de démonstration avec plus de variété
        String[] guideNames = {"Sophie_Renaissance", "Marco_Moderne", "Yuki_Impressionniste"};
        String[] specializations = {"Renaissance", "Art Moderne", "Impressionnisme"};
        
        for (int i = 0; i < guideNames.length; i++) {
            Point2D.Double pos = new Point2D.Double(120 + i * 40, 120 + i * 20);
            AgentSprite sprite = new AgentSprite(
                guideNames[i], AgentType.GUIDE, pos.x, pos.y
            );
            sprite.specialization = specializations[i];
            agentSprites.put(new AID(guideNames[i], AID.ISLOCALNAME), sprite);
        }
        
        // Créer des touristes avec plus de diversité
        String[] touristNames = {
            "Alice_🇫🇷", "Bob_🇮🇹", "Chen_🇨🇳", "Diana_🇩🇪", "Elena_🇪🇸",
            "François_🇫🇷", "Giovanni_🇮🇹", "Hans_🇩🇪", "Isabella_🇪🇸", "James_🇬🇧"
        };
        
        for (int i = 0; i < touristNames.length; i++) {
            Point2D.Double pos = new Point2D.Double(
                100 + (i % 5) * 30,
                160 + (i / 5) * 25
            );
            AgentSprite sprite = new AgentSprite(
                touristNames[i], AgentType.TOURIST, pos.x, pos.y
            );
            sprite.satisfaction = 0.3 + Math.random() * 0.4;
            sprite.fatigue = Math.random() * 0.3;
            agentSprites.put(new AID(touristNames[i], AID.ISLOCALNAME), sprite);
        }
        
        startDemoTour();
    }
    
    private void startDemoTour() {
        Timer demoTimer = new Timer(4000, new ActionListener() {
            private int step = 0;
            private String[] locations = {"Tableau1", "Tableau2", "Tableau3", "Tableau4", "Tableau5", "SalleRepos", "Sortie"};
            
            public void actionPerformed(ActionEvent e) {
                if (step < locations.length) {
                    moveGroupTo(locations[step]);
                    addLog("🚶 Groupe se dirige vers " + locations[step]);
                    
                    // Créer des effets visuels
                    Point2D.Double targetPos = locationPositions.get(locations[step]);
                    if (targetPos != null) {
                        createLocationHighlight(targetPos.x, targetPos.y);
                    }
                    
                    step++;
                } else {
                    ((Timer)e.getSource()).stop();
                    addLog("✅ Visite de démonstration terminée");
                    createCompletionEffect();
                }
            }
        });
        demoTimer.start();
    }
    
    private void moveGroupTo(String location) {
        Point2D.Double targetPos = locationPositions.get(location);
        if (targetPos == null) return;
        
        // Mettre à jour la heatmap
        heatMap.put(location, heatMap.getOrDefault(location, 0.0) + 1.0);
        
        // Déplacer les agents avec formation dynamique
        int guideIndex = 0;
        for (AgentSprite sprite : agentSprites.values()) {
            if (sprite.type == AgentType.GUIDE) {
                sprite.setTarget(targetPos.x, targetPos.y);
                sprite.currentLocation = location;
                
                // Créer un trail de déplacement
                pathTrails.add(new PathTrail(sprite.x, sprite.y, targetPos.x, targetPos.y));
                
                // Déplacer les touristes en formation plus naturelle
                int touristIndex = 0;
                for (AgentSprite touristSprite : agentSprites.values()) {
                    if (touristSprite.type == AgentType.TOURIST) {
                        // Formation en demi-cercle avec variation naturelle
                        double angle = (touristIndex * Math.PI) / 6 - Math.PI/4;
                        double radius = 50 + Math.random() * 30;
                        double offsetX = Math.cos(angle) * radius;
                        double offsetY = Math.sin(angle) * radius;
                        
                        touristSprite.setTarget(
                            targetPos.x + offsetX, 
                            targetPos.y + offsetY
                        );
                        touristSprite.currentLocation = location;
                        
                        // Simulation des états
                        touristSprite.satisfaction += (Math.random() - 0.5) * 0.15;
                        touristSprite.fatigue += 0.03 + Math.random() * 0.05;
                        touristSprite.satisfaction = Math.max(0.0, Math.min(1.0, touristSprite.satisfaction));
                        touristSprite.fatigue = Math.max(0.0, Math.min(1.0, touristSprite.fatigue));
                        
                        touristIndex++;
                    }
                }
                guideIndex++;
                break;
            }
        }
    }
    
    private void updateDisplay() {
        // Mettre à jour les sprites
        for (AgentSprite sprite : agentSprites.values()) {
            sprite.update();
        }
        
        // Mettre à jour les statistiques avec animation
        updateStatistics();
        
        // Faire décroître la heatmap
        heatMap.replaceAll((k, v) -> Math.max(0.0, v - 0.01));
}
    
    private void updateParticles() {
        particles.removeIf(p -> !p.update());
        
        // Ajouter de nouvelles particules occasionnellement
        if (Math.random() < 0.1) {
            particles.add(new ParticleEffect(
                Math.random() * PANEL_WIDTH,
                Math.random() * PANEL_HEIGHT,
                ParticleEffect.Type.AMBIENT
            ));
        }
    }
    
    private void updatePathTrails() {
        pathTrails.removeIf(trail -> !trail.update());
    }
    
    private void updateStatistics() {
        int guideCount = 0;
        int touristCount = 0;
        double totalSatisfaction = 0.0;
        String activity = "Faible";
        
        for (AgentSprite sprite : agentSprites.values()) {
            if (sprite.type == AgentType.GUIDE) {
                guideCount++;
            } else if (sprite.type == AgentType.TOURIST) {
                touristCount++;
                totalSatisfaction += sprite.satisfaction;
            }
        }
        
        double avgSatisfaction = touristCount > 0 ? totalSatisfaction / touristCount : 0.0;
        
        if (avgSatisfaction > 0.7) activity = "Élevée";
        else if (avgSatisfaction > 0.4) activity = "Modérée";
        
        // Mise à jour des cartes de stats (simulation)
        updateStatCard(0, String.valueOf(guideCount));
        updateStatCard(1, String.valueOf(touristCount));
        updateStatCard(2, String.format("%.2f", avgSatisfaction));
        updateStatCard(3, activity);
    }
    
    private void updateStatCard(int index, String value) {
        // Cette méthode serait implémentée pour mettre à jour les cartes individuelles
        // Pour la démo, on se contente de la logique de base
    }
    
    private void createSpawnEffect(double x, double y) {
        for (int i = 0; i < 10; i++) {
            particles.add(new ParticleEffect(x, y, ParticleEffect.Type.SPAWN));
        }
    }
    
    private void createLocationHighlight(double x, double y) {
        particles.add(new ParticleEffect(x, y, ParticleEffect.Type.HIGHLIGHT));
    }
    
    private void createWelcomeEffect() {
        for (int i = 0; i < 20; i++) {
            particles.add(new ParticleEffect(
                Math.random() * PANEL_WIDTH,
                Math.random() * PANEL_HEIGHT,
                ParticleEffect.Type.CELEBRATION
            ));
        }
    }
    
    private void createCompletionEffect() {
        Point2D.Double sortie = locationPositions.get("Sortie");
        for (int i = 0; i < 15; i++) {
            particles.add(new ParticleEffect(
                sortie.x + (Math.random() - 0.5) * 100,
                sortie.y + (Math.random() - 0.5) * 100,
                ParticleEffect.Type.COMPLETION
            ));
        }
    }
    
    private void addLog(String message) {
        String timestamp = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logMessages.add("[" + timestamp + "] " + message);
        
        if (logMessages.size() > 100) {
            logMessages.remove(0);
        }
        
        SwingUtilities.invokeLater(() -> {
            logArea.setText(String.join("\n", logMessages));
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    // Panel de dessin avancé
    private class MuseumPanel extends JPanel {
        private int viewMode = 0; // 0: General, 1: Detailed, 2: Heatmap, 3: Flow
        private BufferedImage backgroundBuffer;
        private boolean needsBackgroundUpdate = true;
        
        public MuseumPanel() {
            setDoubleBuffered(true);
        }
        
        public void setViewMode(int mode) {
            this.viewMode = mode;
            needsBackgroundUpdate = true;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            setupRenderingHints(g2d);
            
            // Dessiner le fond en cache si nécessaire
            if (needsBackgroundUpdate || backgroundBuffer == null) {
                updateBackgroundBuffer();
                needsBackgroundUpdate = false;
            }
            
            g2d.drawImage(backgroundBuffer, 0, 0, null);
            
            // Dessiner les éléments dynamiques
            drawDynamicElements(g2d);
            
            g2d.dispose();
        }
        
        private void setupRenderingHints(Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        
        private void updateBackgroundBuffer() {
            backgroundBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = backgroundBuffer.createGraphics();
            setupRenderingHints(g2d);
            
            drawMuseumLayout(g2d);
            
            g2d.dispose();
        }
        
        private void drawMuseumLayout(Graphics2D g2d) {
            // Fond avec gradient moderne
            GradientPaint bgGradient = new GradientPaint(
                0, 0, isDarkMode ? new Color(45, 45, 45) : new Color(250, 250, 255),
                getWidth(), getHeight(), isDarkMode ? new Color(35, 35, 35) : new Color(240, 240, 250)
            );
            g2d.setPaint(bgGradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Murs du musée avec style moderne
            drawModernWalls(g2d);
            
            // Zones et tableaux selon le mode de vue
            switch (viewMode) {
                case 0: drawGeneralView(g2d); break;
                case 1: drawDetailedView(g2d); break;
                case 2: drawHeatmapView(g2d); break;
                case 3: drawFlowView(g2d); break;
            }
        }
        
        private void drawModernWalls(Graphics2D g2d) {
            // Ombres des murs
            g2d.setColor(SHADOW_COLOR);
            g2d.fillRoundRect(22, 22, getWidth() - 44, getHeight() - 44, 20, 20);
            
            // Murs principaux
            g2d.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(isDarkMode ? new Color(80, 80, 80) : new Color(150, 150, 150));
            g2d.drawRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 20, 20);
            
            // Colonnes décoratives
            drawColumns(g2d);
        }
        
        private void drawColumns(Graphics2D g2d) {
            g2d.setColor(isDarkMode ? new Color(60, 60, 60) : new Color(200, 200, 200));
            int[] columnX = {150, 400, 650, 300, 550};
            int[] columnY = {100, 80, 120, 400, 380};
            
            for (int i = 0; i < columnX.length; i++) {
                // Ombre de colonne
                g2d.setColor(SHADOW_COLOR);
                g2d.fillOval(columnX[i] - 12, columnY[i] - 12, 26, 26);
                
                // Colonne
                g2d.setColor(isDarkMode ? new Color(70, 70, 70) : new Color(220, 220, 220));
                g2d.fillOval(columnX[i] - 10, columnY[i] - 10, 20, 20);
                
                // Highlight
                g2d.setColor(isDarkMode ? new Color(90, 90, 90) : Color.WHITE);
                g2d.fillOval(columnX[i] - 7, columnY[i] - 7, 6, 6);
            }
        }
        
        private void drawGeneralView(Graphics2D g2d) {
            // Dessiner les zones et tableaux normalement
            for (Map.Entry<String, Point2D.Double> entry : locationPositions.entrySet()) {
                String location = entry.getKey();
                Point2D.Double pos = entry.getValue();
                
                if (location.startsWith("Tableau")) {
                    drawTableau(g2d, location, pos.x, pos.y);
                } else {
                    drawZone(g2d, location, pos.x, pos.y);
                }
            }
            
            // Chemins entre zones
            drawConnections(g2d);
        }
        
        private void drawDetailedView(Graphics2D g2d) {
            drawGeneralView(g2d);
            
            // Ajouter des détails supplémentaires
            drawRoomLabels(g2d);
            drawCapacityIndicators(g2d);
        }
        
        private void drawHeatmapView(Graphics2D g2d) {
            // Fond de heatmap
            for (Map.Entry<String, Double> entry : heatMap.entrySet()) {
                Point2D.Double pos = locationPositions.get(entry.getKey());
                if (pos != null && entry.getValue() > 0) {
                    float intensity = (float)Math.min(1.0, entry.getValue() / 5.0);
                    Color heatColor = new Color(1.0f, 1.0f - intensity, 1.0f - intensity, 0.3f);
                    
                    g2d.setColor(heatColor);
                    g2d.fillOval((int)(pos.x - 60), (int)(pos.y - 60), 120, 120);
                }
            }
            
            drawGeneralView(g2d);
        }
        
        private void drawFlowView(Graphics2D g2d) {
            // Dessiner les flux de circulation
            drawFlowArrows(g2d);
            drawGeneralView(g2d);
        }
        
        private void drawTableau(Graphics2D g2d, String name, double x, double y) {
            // Ombre du tableau
            g2d.setColor(SHADOW_COLOR);
            g2d.fillRoundRect((int)x - 27, (int)y - 22, 54, 44, 8, 8);
            
            // Cadre du tableau avec gradient
            GradientPaint frameGradient = new GradientPaint(
                (int)x - 25, (int)y - 20, new Color(139, 69, 19),
                (int)x + 25, (int)y + 20, new Color(160, 82, 45)
            );
            g2d.setPaint(frameGradient);
            g2d.fillRoundRect((int)x - 25, (int)y - 20, 50, 40, 6, 6);
            
            // Œuvre d'art avec style selon le tableau
            Color artColor = getArtworkColor(name);
            GradientPaint artGradient = new GradientPaint(
                (int)x - 20, (int)y - 15, artColor,
                (int)x + 20, (int)y + 15, artColor.darker()
            );
            g2d.setPaint(artGradient);
            g2d.fillRoundRect((int)x - 20, (int)y - 15, 40, 30, 4, 4);
            
            // Highlight sur le verre
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillRoundRect((int)x - 18, (int)y - 13, 15, 8, 2, 2);
            
            // Numéro du tableau moderne
            g2d.setColor(isDarkMode ? Color.WHITE : Color.BLACK);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String number = name.substring(name.length() - 1);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(number);
            g2d.drawString(number, (int)x - textWidth/2, (int)y + 5);
            
            // Titre élégant
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            textWidth = g2d.getFontMetrics().stringWidth(name);
            g2d.drawString(name, (int)x - textWidth/2, (int)y + 40);
            
            // Indicateur de popularité
            drawPopularityIndicator(g2d, x, y, name);
        }
        
        private Color getArtworkColor(String tableauName) {
            switch (tableauName) {
                case "Tableau1": return new Color(255, 215, 0); // Or - Renaissance
                case "Tableau2": return new Color(135, 206, 235); // Bleu ciel - Van Gogh
                case "Tableau3": return new Color(105, 105, 105); // Gris - Guernica
                case "Tableau4": return new Color(220, 20, 60); // Rouge - Cubisme
                case "Tableau5": return new Color(144, 238, 144); // Vert clair - École d'Athènes
                default: return ACCENT_ORANGE;
            }
        }
        
        private void drawPopularityIndicator(Graphics2D g2d, double x, double y, String name) {
            double popularity = heatMap.getOrDefault(name, 0.0);
            if (popularity > 0) {
                int stars = Math.min(5, (int)(popularity / 2) + 1);
                g2d.setColor(new Color(255, 215, 0));
                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 8));
                String starString = "★".repeat(stars);
                int textWidth = g2d.getFontMetrics().stringWidth(starString);
                g2d.drawString(starString, (int)x - textWidth/2, (int)y - 25);
            }
        }
        
        private void drawZone(Graphics2D g2d, String name, double x, double y) {
            Color zoneColor = getZoneColor(name);
            
            // Ombre de la zone
            g2d.setColor(SHADOW_COLOR);
            g2d.fillOval((int)x - 32, (int)y - 32, 64, 64);
            
            // Zone avec gradient
            GradientPaint zoneGradient = new GradientPaint(
                (int)x - 30, (int)y - 30, zoneColor,
                (int)x + 30, (int)y + 30, zoneColor.darker()
            );
            g2d.setPaint(zoneGradient);
            g2d.fillOval((int)x - 30, (int)y - 30, 60, 60);
            
            // Bordure élégante
            g2d.setColor(zoneColor.darker().darker());
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval((int)x - 30, (int)y - 30, 60, 60);
            
            // Icône de zone
            g2d.setColor(isDarkMode ? Color.WHITE : Color.BLACK);
            g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            String icon = getZoneIcon(name);
            FontMetrics fm = g2d.getFontMetrics();
            int iconWidth = fm.stringWidth(icon);
            g2d.drawString(icon, (int)x - iconWidth/2, (int)y + 7);
            
            // Nom de la zone stylisé
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2d.setColor(isDarkMode ? Color.WHITE : Color.BLACK);
            int nameWidth = g2d.getFontMetrics().stringWidth(name);
            
            // Fond du texte
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.fillRoundRect((int)x - nameWidth/2 - 5, (int)y + 42, nameWidth + 10, 16, 8, 8);
            
            g2d.setColor(Color.BLACK);
            g2d.drawString(name, (int)x - nameWidth/2, (int)y + 52);
        }
        
        private Color getZoneColor(String name) {
            switch (name) {
                case "PointA": return ACCENT_GREEN.brighter();
                case "SalleRepos": return ACCENT_BLUE.brighter();
                case "Sortie": return ACCENT_RED.brighter();
                case "Accueil": return ACCENT_PURPLE.brighter();
                case "Boutique": return ACCENT_ORANGE.brighter();
                default: return new Color(180, 180, 180);
            }
        }
        
        private String getZoneIcon(String name) {
            switch (name) {
                case "PointA": return "🏁";
                case "SalleRepos": return "🛋️";
                case "Sortie": return "🚪";
                case "Accueil": return "🎫";
                case "Boutique": return "🛍️";
                default: return "📍";
            }
        }
        
        private void drawConnections(Graphics2D g2d) {
            g2d.setColor(new Color(200, 200, 200, 100));
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
                0, new float[]{10, 5}, (float)animationTime * 10));
            
            // Chemins suggérés avec animation
            String[][] connections = {
                {"PointA", "Tableau1"}, {"Tableau1", "Tableau2"}, {"Tableau2", "Tableau3"},
                {"Tableau3", "Tableau4"}, {"Tableau4", "Tableau5"}, {"Tableau5", "SalleRepos"},
                {"SalleRepos", "Sortie"}, {"PointA", "Accueil"}, {"Accueil", "Boutique"}
            };
            
            for (String[] connection : connections) {
                Point2D.Double from = locationPositions.get(connection[0]);
                Point2D.Double to = locationPositions.get(connection[1]);
                if (from != null && to != null) {
                    drawAnimatedPath(g2d, from, to);
                }
            }
        }
        
        private void drawAnimatedPath(Graphics2D g2d, Point2D.Double from, Point2D.Double to) {
            // Courbe de Bézier pour un chemin plus naturel
            double ctrlX = (from.x + to.x) / 2 + Math.sin(animationTime) * 20;
            double ctrlY = (from.y + to.y) / 2 + Math.cos(animationTime) * 10;
            
            QuadCurve2D.Double curve = new QuadCurve2D.Double(
                from.x, from.y, ctrlX, ctrlY, to.x, to.y
            );
            
            g2d.draw(curve);
        }
        
        private void drawRoomLabels(Graphics2D g2d) {
            g2d.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            g2d.setColor(new Color(100, 100, 100));
            
            // Labels des salles
            g2d.drawString("Salle Renaissance", 180, 100);
            g2d.drawString("Galerie Moderne", 420, 80);
            g2d.drawString("Espace Repos", 120, 520);
        }
        
        private void drawCapacityIndicators(Graphics2D g2d) {
            // Indicateurs de capacité pour chaque zone
            for (Map.Entry<String, Point2D.Double> entry : locationPositions.entrySet()) {
                Point2D.Double pos = entry.getValue();
                drawCapacityBar(g2d, pos.x + 35, pos.y - 10, Math.random());
            }
        }
        
        private void drawCapacityBar(Graphics2D g2d, double x, double y, double capacity) {
            // Barre de capacité moderne
            g2d.setColor(new Color(220, 220, 220));
            g2d.fillRoundRect((int)x, (int)y, 30, 4, 2, 2);
            
            Color capColor = capacity > 0.8 ? ACCENT_RED : 
                           capacity > 0.5 ? ACCENT_ORANGE : ACCENT_GREEN;
            g2d.setColor(capColor);
            g2d.fillRoundRect((int)x, (int)y, (int)(30 * capacity), 4, 2, 2);
        }
        
        private void drawFlowArrows(Graphics2D g2d) {
            g2d.setColor(new Color(ACCENT_BLUE.getRed(), ACCENT_BLUE.getGreen(), ACCENT_BLUE.getBlue(), 100));
            g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            // Flèches de flux animées
            double offset = Math.sin(animationTime * 2) * 10;
            for (PathTrail trail : pathTrails) {
                drawFlowArrow(g2d, trail.startX + offset, trail.startY, trail.endX + offset, trail.endY);
            }
        }
        
        private void drawFlowArrow(Graphics2D g2d, double fromX, double fromY, double toX, double toY) {
            g2d.drawLine((int)fromX, (int)fromY, (int)toX, (int)toY);
            
            // Pointe de flèche
            double angle = Math.atan2(toY - fromY, toX - fromX);
            int arrowLength = 8;
            double arrowAngle = Math.PI / 6;
            
            int x1 = (int)(toX - arrowLength * Math.cos(angle - arrowAngle));
            int y1 = (int)(toY - arrowLength * Math.sin(angle - arrowAngle));
            int x2 = (int)(toX - arrowLength * Math.cos(angle + arrowAngle));
            int y2 = (int)(toY - arrowLength * Math.sin(angle + arrowAngle));
            
            g2d.drawLine((int)toX, (int)toY, x1, y1);
            g2d.drawLine((int)toX, (int)toY, x2, y2);
        }
        
        private void drawDynamicElements(Graphics2D g2d) {
            // Particules
            for (ParticleEffect particle : particles) {
                particle.draw(g2d);
            }
            
            // Trails de chemins
            for (PathTrail trail : pathTrails) {
                trail.draw(g2d);
            }
            
            // Agents
            for (AgentSprite sprite : agentSprites.values()) {
                sprite.draw(g2d);
            }
            
            // Informations contextuelles
            drawContextualInfo(g2d);
        }
        
        private void drawContextualInfo(Graphics2D g2d) {
            // Horloge moderne
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2d.setColor(isDarkMode ? Color.WHITE : Color.BLACK);
            String time = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            g2d.drawString("🕐 " + time, getWidth() - 120, 30);
            
            // Compteur d'agents
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2d.drawString("Agents actifs: " + agentSprites.size(), getWidth() - 150, 50);
            
            // Mode de vue actuel
            String[] modeNames = {"Vue Générale", "Vue Détaillée", "Carte Thermique", "Flux de Circulation"};
            g2d.drawString("Mode: " + modeNames[viewMode], 20, getHeight() - 20);
        }
    }
    
    // Classes pour les effets visuels avancés
    private static class ParticleEffect {
        enum Type { AMBIENT, SPAWN, HIGHLIGHT, CELEBRATION, COMPLETION }
        
        double x, y, vx, vy;
        Color color;
        float alpha;
        float size;
        int life, maxLife;
        Type type;
        
        public ParticleEffect(double x, double y, Type type) {
            this.x = x;
            this.y = y;
            this.type = type;
            
            switch (type) {
                case AMBIENT:
                    this.vx = (Math.random() - 0.5) * 0.5;
                    this.vy = (Math.random() - 0.5) * 0.5;
                    this.color = new Color(200, 200, 255);
                    this.alpha = 0.3f;
                    this.size = 2.0f;
                    this.maxLife = life = 300 + (int)(Math.random() * 200);
                    break;
                    
                case SPAWN:
                    this.vx = (Math.random() - 0.5) * 4;
                    this.vy = (Math.random() - 0.5) * 4;
                    this.color = ACCENT_GREEN;
                    this.alpha = 1.0f;
                    this.size = 4.0f;
                    this.maxLife = life = 60;
                    break;
                    
                case HIGHLIGHT:
                    this.vx = 0;
                    this.vy = 0;
                    this.color = ACCENT_ORANGE;
                    this.alpha = 0.8f;
                    this.size = 20.0f;
                    this.maxLife = life = 120;
                    break;
                    
                case CELEBRATION:
                    this.vx = (Math.random() - 0.5) * 3;
                    this.vy = -Math.random() * 2 - 1;
                    this.color = new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
                    this.alpha = 1.0f;
                    this.size = 3.0f;
                    this.maxLife = life = 180;
                    break;
                    
                case COMPLETION:
                    this.vx = (Math.random() - 0.5) * 2;
                    this.vy = (Math.random() - 0.5) * 2;
                    this.color = ACCENT_BLUE;
                    this.alpha = 0.9f;
                    this.size = 6.0f;
                    this.maxLife = life = 150;
                    break;
            }
        }
        
        public boolean update() {
            x += vx;
            y += vy;
            life--;
            
            // Mise à jour selon le type
            if (type == Type.AMBIENT) {
                vy -= 0.01; // Légère gravité inverse
            } else if (type == Type.CELEBRATION) {
                vy += 0.05; // Gravité
            }
            
            // Fade out progressif
            alpha = Math.max(0, (float)life / maxLife);
            
            return life > 0;
        }
        
        public void draw(Graphics2D g2d) {
            Color drawColor = new Color(
                color.getRed(), color.getGreen(), color.getBlue(), 
                (int)(alpha * 255)
            );
            g2d.setColor(drawColor);
            
            if (type == Type.HIGHLIGHT) {
                // Effet de pulse pour highlight
                float pulseSize = size + (float)Math.sin(System.currentTimeMillis() * 0.01) * 5;
                g2d.fillOval((int)(x - pulseSize/2), (int)(y - pulseSize/2), (int)pulseSize, (int)pulseSize);
            } else {
                g2d.fillOval((int)(x - size/2), (int)(y - size/2), (int)size, (int)size);
            }
        }
    }
    
    private static class PathTrail {
        double startX, startY, endX, endY;
        int life, maxLife;
        
        public PathTrail(double startX, double startY, double endX, double endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.maxLife = life = 200;
        }
        
        public boolean update() {
            life--;
            return life > 0;
        }
        
public void draw(Graphics2D g2d) {
            float alpha = (float)life / maxLife;
            Color trailColor = new Color(ACCENT_BLUE.getRed(), ACCENT_BLUE.getGreen(), ACCENT_BLUE.getBlue(), (int)(alpha * 100));
            
            g2d.setColor(trailColor);
            g2d.setStroke(new BasicStroke(3.0f * alpha, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine((int)startX, (int)startY, (int)endX, (int)endY);
        }
    }
    
    // Énumération des types d'agents
    enum AgentType {
        GUIDE, TOURIST, COORDINATOR
    }
    
    // Classe pour représenter un agent à l'écran
    class AgentSprite {
        String name;
        AgentType type;
        double x, y;
        double targetX, targetY;
        String currentLocation = "PointA";
        
        // Propriétés spécifiques
        String specialization = "";
        String nationality = "";
        double satisfaction = 0.5;
        double fatigue = 0.0;
        double speed = 2.0;
        
        // Animation
        boolean isMoving = false;
        double bobOffset = 0;
        Color personalColor;
        
        public AgentSprite(String name, AgentType type, double x, double y) {
            this.name = name;
            this.type = type;
            this.x = x;
            this.y = y;
            this.targetX = x;
            this.targetY = y;
            this.personalColor = generatePersonalColor();
            this.bobOffset = Math.random() * Math.PI * 2;
        }
        
        private Color generatePersonalColor() {
            Random rand = new Random(name.hashCode());
            return new Color(
                100 + rand.nextInt(155),
                100 + rand.nextInt(155),
                100 + rand.nextInt(155)
            );
        }
        
        public void setTarget(double targetX, double targetY) {
            this.targetX = targetX;
            this.targetY = targetY;
            this.isMoving = true;
        }
        
        public void update() {
            if (isMoving) {
                double dx = targetX - x;
                double dy = targetY - y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance < speed) {
                    x = targetX;
                    y = targetY;
                    isMoving = false;
                } else {
                    x += (dx / distance) * speed;
                    y += (dy / distance) * speed;
                }
            }
            
            // Animation de "marche"
            bobOffset += isMoving ? 0.3 : 0.05;
        }
        
        public void draw(Graphics2D g2d) {
            // Configuration de base
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x, y);
            
            // Couleur et taille selon le type
            Color agentColor = getAgentColor();
            int baseSize = getAgentSize();
            
            // Effet de marche (bobbing)
            double walkBob = Math.sin(bobOffset) * (isMoving ? 2 : 0.5);
            g2dCopy.translate(0, walkBob);
            
            // Ombre de l'agent
            g2dCopy.setColor(new Color(0, 0, 0, 50));
            g2dCopy.fillOval(-baseSize/2, -baseSize/2 + 2, baseSize, baseSize);
            
            // Corps principal de l'agent
            GradientPaint bodyGradient = new GradientPaint(
                -baseSize/2, -baseSize/2, agentColor.brighter(),
                baseSize/2, baseSize/2, agentColor.darker()
            );
            g2dCopy.setPaint(bodyGradient);
            g2dCopy.fillOval(-baseSize/2, -baseSize/2, baseSize, baseSize);
            
            // Contour
            g2dCopy.setColor(agentColor.darker().darker());
            g2dCopy.setStroke(new BasicStroke(2.0f));
            g2dCopy.drawOval(-baseSize/2, -baseSize/2, baseSize, baseSize);
            
            // Highlight
            g2dCopy.setColor(new Color(255, 255, 255, 100));
            g2dCopy.fillOval(-baseSize/2 + 3, -baseSize/2 + 3, baseSize/3, baseSize/3);
            
            // Détails selon le type
            if (type == AgentType.GUIDE) {
                drawGuideDetails(g2dCopy, baseSize);
            } else if (type == AgentType.TOURIST) {
                drawTouristDetails(g2dCopy, baseSize);
            }
            
            // Indicateur de mouvement
            if (isMoving) {
                g2dCopy.setColor(new Color(255, 255, 0, 150));
                g2dCopy.fillOval(-3, -baseSize/2 - 12, 6, 6);
            }
            
            // Nom de l'agent
            drawLabel(g2dCopy, baseSize);
            
            g2dCopy.dispose();
        }
        
        private Color getAgentColor() {
            switch (type) {
                case GUIDE:
                    return ACCENT_BLUE;
                case TOURIST:
                    if (satisfaction > 0.7) return ACCENT_GREEN;
                    else if (satisfaction > 0.4) return ACCENT_ORANGE;
                    else return ACCENT_RED;
                default:
                    return Color.GRAY;
            }
        }
        
        private int getAgentSize() {
            return type == AgentType.GUIDE ? 20 : 16;
        }
        
        private void drawGuideDetails(Graphics2D g2d, int size) {
            // Chapeau ou insigne de guide
            g2d.setColor(new Color(139, 69, 19)); // Marron
            g2d.fillRect(-size/3, -size/2 - 4, size*2/3, 4);
            
            // Insigne
            g2d.setColor(new Color(255, 215, 0)); // Or
            g2d.fillOval(-3, -size/2 + 3, 6, 6);
            
            // Badge avec spécialisation
            if (!specialization.isEmpty()) {
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 6));
                g2d.setColor(Color.BLACK);
                String abbrev = specialization.substring(0, 1);
                FontMetrics fm = g2d.getFontMetrics();
                int w = fm.stringWidth(abbrev);
                g2d.drawString(abbrev, -w/2, -size/2 + 7);
            }
        }
        
        private void drawTouristDetails(Graphics2D g2d, int size) {
            // Sac à dos
            g2d.setColor(personalColor.darker());
            g2d.fillRect(-size/4, size/4, size/2, size/3);
            
            // Barres d'état
            if (type == AgentType.TOURIST) {
                drawStatusBars(g2d, size);
            }
        }
        
        private void drawStatusBars(Graphics2D g2d, int size) {
            int barWidth = 24;
            int barHeight = 3;
            int barY = size/2 + 8;
            
            // Fond des barres
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRoundRect(-barWidth/2, barY, barWidth, barHeight, 2, 2);
            g2d.fillRoundRect(-barWidth/2, barY + 6, barWidth, barHeight, 2, 2);
            
            // Barre de satisfaction
            Color satisfactionColor = satisfaction > 0.7 ? ACCENT_GREEN : 
                                    satisfaction > 0.4 ? ACCENT_ORANGE : ACCENT_RED;
            g2d.setColor(satisfactionColor);
            g2d.fillRoundRect(-barWidth/2, barY, (int)(barWidth * satisfaction), barHeight, 2, 2);
            
            // Barre de fatigue
            g2d.setColor(ACCENT_RED);
            g2d.fillRoundRect(-barWidth/2, barY + 6, (int)(barWidth * fatigue), barHeight, 2, 2);
            
            // Labels des barres
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 6));
            g2d.setColor(Color.BLACK);
            g2d.drawString("S", -barWidth/2 - 8, barY + 2);
            g2d.drawString("F", -barWidth/2 - 8, barY + 8);
        }
        
        private void drawLabel(Graphics2D g2d, int size) {
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(name);
            int textHeight = fm.getHeight();
            
            // Fond du label
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillRoundRect(-textWidth/2 - 4, size/2 + 20, textWidth + 8, textHeight, 6, 6);
            
            // Bordure du label
            g2d.setColor(new Color(200, 200, 200));
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawRoundRect(-textWidth/2 - 4, size/2 + 20, textWidth + 8, textHeight, 6, 6);
            
            // Texte du nom
            g2d.setColor(Color.BLACK);
            g2d.drawString(name, -textWidth/2, size/2 + 20 + fm.getAscent());
            
            // Informations supplémentaires pour les guides
            if (type == AgentType.GUIDE && !specialization.isEmpty()) {
                g2d.setFont(new Font("Segoe UI", Font.ITALIC, 7));
                g2d.setColor(Color.GRAY);
                int specWidth = g2d.getFontMetrics().stringWidth(specialization);
                g2d.drawString(specialization, -specWidth/2, size/2 + 35);
            }
        }
    }
    
    // Méthodes utilitaires publiques
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MuseumGUI();
        });
    }
    
    // Méthodes d'intégration avec JADE
    public void updateFromCoordinator(CoordinatorAgent coordinator) {
        // Synchronisation avec les données du coordinateur
        if (coordinator != null) {
            // Mise à jour en temps réel
            SwingUtilities.invokeLater(() -> {
                museumPanel.needsBackgroundUpdate = true;
            });
        }
    }
    
    public void addGuideAgent(GuideAgent guide) {
        Point2D.Double pos = locationPositions.get(guide.getCurrentLocation());
        if (pos != null) {
            AgentSprite sprite = new AgentSprite(
                guide.getLocalName(), AgentType.GUIDE, pos.x, pos.y
            );
            sprite.specialization = guide.getSpecialization();
            agentSprites.put(guide.getAID(), sprite);
            
            addLog("Guide " + guide.getLocalName() + " ajouté (Spé: " + guide.getSpecialization() + ")");
        }
    }
    
    public void addTouristAgent(TouristAgent tourist) {
        Point2D.Double pos = locationPositions.get(tourist.getCurrentLocation());
        if (pos != null) {
            AgentSprite sprite = new AgentSprite(
                tourist.getLocalName(), AgentType.TOURIST, pos.x, pos.y
            );
            sprite.nationality = tourist.getNationality();
            sprite.satisfaction = tourist.getSatisfaction();
            sprite.fatigue = tourist.getFatigue();
            agentSprites.put(tourist.getAID(), sprite);
            
            addLog("Touriste " + tourist.getLocalName() + " ajouté (" + tourist.getNationality() + ")");
        }
    }
}