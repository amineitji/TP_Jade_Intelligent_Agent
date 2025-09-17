package gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ExhibitionGUI extends JFrame {
    private static ExhibitionGUI instance;
    
    // Composants principaux
    private JTextArea logArea;
    private JLabel statsLabel;
    private DefaultListModel<String> guidesListModel;
    private DefaultListModel<String> groupsListModel;
    private JList<String> guidesList;
    private JList<String> groupsList;
    
    // Données
    private Map<String, String> guides = new ConcurrentHashMap<>();
    private Map<String, String> groups = new ConcurrentHashMap<>();
    private List<String> logEntries = new ArrayList<>(); // AJOUT DE CETTE LIGNE
    private int totalVisits = 0;
    private int totalTourists = 0;
    
    private ExhibitionGUI() {
        System.out.println("🚀 [DEBUG] Constructeur ExhibitionGUI() appelé");
        
        System.out.println("🔧 [DEBUG] Initialisation des collections...");
        // Les collections sont déjà initialisées dans la déclaration
        System.out.println("✅ [DEBUG] Collections initialisées");
        
        System.out.println("🔧 [DEBUG] Appel initComponents()...");
        initComponents();
        
        System.out.println("🔧 [DEBUG] Appel setupLayout()...");
        setupLayout();
        
        System.out.println("🔧 [DEBUG] Appel startUpdateTimer()...");
        startUpdateTimer();
        
        System.out.println("✅ [DEBUG] Constructeur terminé avec succès");
    }
    
    public static ExhibitionGUI getInstance() {
        System.out.println("🔍 [DEBUG] getInstance() appelé");
        
        if (instance == null) {
            System.out.println("🔧 [DEBUG] Instance null, création nouvelle instance...");
            instance = new ExhibitionGUI();
            System.out.println("✅ [DEBUG] Nouvelle instance créée");
        } else {
            System.out.println("✅ [DEBUG] Instance existante retournée");
        }
        return instance;
    }
    
    private void initComponents() {
        System.out.println("🔧 [DEBUG] Début initComponents()");
        
        setTitle("🏛️ Système d'Exposition - Monitoring");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        System.out.println("✅ [DEBUG] Fenêtre configurée");
        
        // Zone de statistiques
        statsLabel = new JLabel("<html><h3>📊 Statistiques</h3>" +
            "Visites: 0 | Touristes: 0 | Guides actifs: 0</html>");
        System.out.println("✅ [DEBUG] statsLabel créé");
        
        // Listes
        guidesListModel = new DefaultListModel<>();
        guidesListModel.addElement("👨‍🏫 Aucun guide pour le moment");
        guidesList = new JList<>(guidesListModel);
        System.out.println("✅ [DEBUG] guidesList créé avec " + guidesListModel.size() + " éléments");
        
        groupsListModel = new DefaultListModel<>();
        groupsListModel.addElement("👥 Aucun groupe pour le moment");
        groupsList = new JList<>(groupsListModel);
        System.out.println("✅ [DEBUG] groupsList créé avec " + groupsListModel.size() + " éléments");
        
        // Zone de log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        logArea.setText("=== SYSTÈME D'EXPOSITION DÉMARRÉ ===\n");
        logArea.setText(logArea.getText() + getCurrentTime() + " Interface graphique initialisée\n");
        System.out.println("✅ [DEBUG] logArea créé et configuré");
        
        System.out.println("✅ Interface GUI initialisée avec succès");
    }
    
    private void setupLayout() {
        System.out.println("🔧 [DEBUG] Début setupLayout()");
        
        setLayout(new BorderLayout());
        System.out.println("✅ [DEBUG] BorderLayout défini");
        
        // Panneau du haut - Statistiques
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBorder(new TitledBorder("Statistiques"));
        topPanel.add(statsLabel);
        topPanel.setPreferredSize(new Dimension(0, 80));
        System.out.println("✅ [DEBUG] topPanel créé avec taille " + topPanel.getPreferredSize());
        
        // Panneau central - Listes
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        System.out.println("✅ [DEBUG] centerPanel créé");
        
        // Panneau guides
        JPanel guidesPanel = new JPanel(new BorderLayout());
        guidesPanel.setBorder(new TitledBorder("👨‍🏫 Guides"));
        JScrollPane guidesScrollPane = new JScrollPane(guidesList);
        guidesPanel.add(guidesScrollPane, BorderLayout.CENTER);
        System.out.println("✅ [DEBUG] guidesPanel créé et ajouté à centerPanel");
        
        // Panneau groupes
        JPanel groupsPanel = new JPanel(new BorderLayout());
        groupsPanel.setBorder(new TitledBorder("👥 Groupes"));
        JScrollPane groupsScrollPane = new JScrollPane(groupsList);
        groupsPanel.add(groupsScrollPane, BorderLayout.CENTER);
        System.out.println("✅ [DEBUG] groupsPanel créé");
        
        centerPanel.add(guidesPanel);
        centerPanel.add(groupsPanel);
        System.out.println("✅ [DEBUG] Panneaux ajoutés à centerPanel");
        
        // Panneau du bas - Logs
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new TitledBorder("📝 Journal des Événements"));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(0, 200));
        bottomPanel.add(logScrollPane, BorderLayout.CENTER);
        System.out.println("✅ [DEBUG] bottomPanel créé avec taille " + logScrollPane.getPreferredSize());
        
        // Ajout des panneaux à la fenêtre principale
        add(topPanel, BorderLayout.NORTH);
        System.out.println("✅ [DEBUG] topPanel ajouté à NORTH");
        
        add(centerPanel, BorderLayout.CENTER);
        System.out.println("✅ [DEBUG] centerPanel ajouté à CENTER");
        
        add(bottomPanel, BorderLayout.SOUTH);
        System.out.println("✅ [DEBUG] bottomPanel ajouté à SOUTH");
        
        // Forcer le rafraîchissement
        revalidate();
        repaint();
        System.out.println("✅ [DEBUG] revalidate() et repaint() appelés");
        
        System.out.println("✅ Layout configuré avec succès");
    }
    
    private void startUpdateTimer() {
        javax.swing.Timer timer = new javax.swing.Timer(2000, e -> updateDisplay());
        timer.start();
        System.out.println("✅ Timer de mise à jour démarré");
    }
    
    private void updateDisplay() {
        SwingUtilities.invokeLater(() -> {
            // Mise à jour des statistiques
            long activeGuides = guides.values().stream().filter(s -> !"Disponible".equals(s)).count();
            statsLabel.setText("<html><h3>📊 Statistiques</h3>" +
                "Visites: " + totalVisits + 
                " | Touristes: " + totalTourists + 
                " | Guides actifs: " + activeGuides + "/" + guides.size() + 
                " | Groupes: " + groups.size() + "</html>");
            
            // Mise à jour des listes
            updateGuidesDisplay();
            updateGroupsDisplay();
        });
    }
    
    private void updateGuidesDisplay() {
        guidesListModel.clear();
        if (guides.isEmpty()) {
            guidesListModel.addElement("👨‍🏫 Aucun guide pour le moment");
        } else {
            for (Map.Entry<String, String> entry : guides.entrySet()) {
                String icon = "Disponible".equals(entry.getValue()) ? "💤" : "🏃‍♂️";
                guidesListModel.addElement(icon + " " + entry.getKey() + " - " + entry.getValue());
            }
        }
    }
    
    private void updateGroupsDisplay() {
        groupsListModel.clear();
        if (groups.isEmpty()) {
            groupsListModel.addElement("👥 Aucun groupe pour le moment");
        } else {
            for (Map.Entry<String, String> entry : groups.entrySet()) {
                String icon = getStatusIcon(entry.getValue());
                groupsListModel.addElement(icon + " " + entry.getKey() + " - " + entry.getValue());
            }
        }
    }
    
    private String getStatusIcon(String status) {
        switch (status) {
            case "EN_ATTENTE": return "⏳";
            case "ASSIGNE_GUIDE": return "👨‍🏫";
            case "EN_VISITE": return "🎨";
            case "VISITE_TERMINEE": return "✅";
            default: return "❓";
        }
    }
    
    private void addLogEntry(String message) {
        System.out.println("📝 [DEBUG] addLogEntry() appelé: " + message);
        
        SwingUtilities.invokeLater(() -> {
            String timestamp = getCurrentTime();
            String logMessage = "[" + timestamp + "] " + message;
            
            logEntries.add(logMessage);
            logArea.append(logMessage + "\n");
            
            // Garder seulement les 100 dernières entrées
            if (logEntries.size() > 100) {
                logEntries.remove(0);
            }
            
            // Auto-scroll vers le bas
            logArea.setCaretPosition(logArea.getDocument().getLength());
            
            System.out.println("✅ [DEBUG] Log ajouté: " + logMessage);
        });
    }
    
    private String getCurrentTime() {
        return java.time.LocalTime.now().toString().substring(0, 8);
    }
    
    // Méthodes publiques pour les agents
    public void addGuide(String guideId, String status) {
        System.out.println("📝 [DEBUG] addGuide() appelé: " + guideId + " - " + status);
        
        guides.put(guideId, status);
        addLogEntry("👨‍🏫 Guide " + guideId + " ajouté (" + status + ")");
        updateDisplay();
        
        System.out.println("✅ [DEBUG] Guide ajouté, total guides: " + guides.size());
    }
    
    public void updateGuideStatus(String guideId, String status, String groupId) {
        System.out.println("📝 [DEBUG] updateGuideStatus() appelé: " + guideId + " - " + status + " - " + groupId);
        
        guides.put(guideId, status);
        String message = "👨‍🏫 " + guideId + " → " + status;
        if (groupId != null) {
            message += " (Groupe: " + groupId + ")";
        }
        addLogEntry(message);
        updateDisplay();
        
        System.out.println("✅ [DEBUG] Statut guide mis à jour");
    }
    
    public void addGroup(String groupId, String nationality, int size) {
        System.out.println("📝 [DEBUG] addGroup() appelé: " + groupId + " - " + nationality + " - " + size);
        
        groups.put(groupId, "EN_ATTENTE");
        addLogEntry("👥 " + groupId + " arrivé (" + size + " " + nationality + "s)");
        updateDisplay();
        
        System.out.println("✅ [DEBUG] Groupe ajouté, total groupes: " + groups.size());
    }
    
    public void updateGroupStatus(String groupId, String status) {
        groups.put(groupId, status);
        addLogEntry("👥 " + groupId + " → " + status);
        updateDisplay();
    }
    
    public void addTourEvent(String guideId, String groupId, String tableau) {
        addLogEntry("🎨 " + guideId + " présente '" + tableau + "' au " + groupId);
    }
    
    public void completeTour(String groupId, String guideId, int groupSize) {
        totalVisits++;
        totalTourists += groupSize;
        groups.remove(groupId);
        guides.put(guideId, "Disponible");
        addLogEntry("✅ Visite terminée: " + groupId + " avec " + guideId + 
            " (" + groupSize + " personnes)");
        updateDisplay();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExhibitionGUI gui = ExhibitionGUI.getInstance();
            gui.setVisible(true);
            
            // Test de l'interface
            gui.addLogEntry("🔧 Test de l'interface - tout fonctionne !");
            
            // Simulation de données de test
            gui.addGuide("GuideMartin", "Disponible");
            gui.addGuide("GuideSophie", "En service");
            gui.addGroup("Groupe_Français", "Française", 8);
            gui.updateGroupStatus("Groupe_Français", "EN_VISITE");
        });
    }
}