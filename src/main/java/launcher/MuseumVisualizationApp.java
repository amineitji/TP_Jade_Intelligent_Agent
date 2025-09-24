package launcher;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Interface moderne et scientifique pour le système multi-agents JADE
 */
public class MuseumVisualizationApp extends Application {

    // Configuration interface moderne
    private static final int CANVAS_WIDTH = 1000;
    private static final int CANVAS_HEIGHT = 700;
    private static final Color BG_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = Color.rgb(59, 130, 246); // Blue moderne
    private static final Color SUCCESS_COLOR = Color.rgb(16, 185, 129); // Green moderne
    private static final Color WARNING_COLOR = Color.rgb(245, 158, 11); // Orange moderne
    private static final Color ERROR_COLOR = Color.rgb(239, 68, 68); // Red moderne
    private static final Color SECONDARY_COLOR = Color.rgb(107, 114, 128); // Gray moderne
    private static final Color LIGHT_GRAY = Color.rgb(249, 250, 251);
    private static final Color BORDER_COLOR = Color.rgb(229, 231, 235);
    
    // Composants GUI modernes
    private Canvas simulationCanvas;
    private GraphicsContext gc;
    private BorderPane root;
    private VBox agentStatsPanel;
    private TextArea logTerminal;
    private Label systemStatusLabel;
    private ProgressIndicator systemProgress;
    
    // Animation fluide
    private AnimationTimer renderLoop;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    
    // Données système
    private Map<String, Agent> agents = new HashMap<>();
    private List<TourGroup> tourGroups = new ArrayList<>();
    private List<Room> rooms = new ArrayList<>();
    private Point2D entrancePoint = new Point2D(100, 350);
    private Point2D exitPoint = new Point2D(900, 350);
    private boolean jadeSystemRunning = false;
    private double time = 0;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Système Multi-Agents JADE - Interface Scientifique Moderne");
        
        initializeModernInterface();
        initializeRoomLayout();
        startFluidAnimation();
        startSystemMonitoring();
        
        Scene scene = new Scene(root, 1500, 900);
        scene.getStylesheets().add("data:text/css," + getModernCSS());
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(e -> cleanup());
    }
    
    /**
     * Interface moderne avec design system
     */
    private void initializeModernInterface() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: white;");
        
        // Header moderne avec glass effect
        HBox header = createModernHeader();
        root.setTop(header);
        
        // Zone de simulation principale
        VBox centerPanel = createSimulationPanel();
        root.setCenter(centerPanel);
        
        // Panel latéral avec cards modernes
        VBox rightPanel = createModernStatsPanel();
        root.setRight(rightPanel);
        
        // Footer avec logs
        VBox bottomPanel = createModernLogPanel();
        root.setBottom(bottomPanel);
    }
    
    /**
     * Header moderne avec glass morphism
     */
    private HBox createModernHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, rgba(59, 130, 246, 0.1), rgba(139, 69, 19, 0.05));" +
                       "-fx-border-color: " + toHexString(BORDER_COLOR) + ";" +
                       "-fx-border-width: 0 0 1 0;" +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        Text title = new Text("SYSTÈME MULTI-AGENTS JADE");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setFill(Color.rgb(31, 41, 55));
        
        systemProgress = new ProgressIndicator();
        systemProgress.setPrefSize(24, 24);
        systemProgress.setVisible(false);
        
        systemStatusLabel = new Label("Système inactif");
        systemStatusLabel.setTextFill(SECONDARY_COLOR);
        systemStatusLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        Button startBtn = createModernButton("Démarrer JADE", PRIMARY_COLOR, false, this::startJadeSystem);
        Button stopBtn = createModernButton("Arrêter", ERROR_COLOR, false, this::stopSystem);
        Button addGuideBtn = createModernButton("+ Guide", SUCCESS_COLOR, false, this::addGuide);
        Button addTouristBtn = createModernButton("+ Touriste", WARNING_COLOR, false, this::addTourist);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(title, spacer, systemProgress, systemStatusLabel, 
                                   startBtn, stopBtn, addGuideBtn, addTouristBtn);
        return header;
    }
    
    /**
     * Panel de simulation avec style moderne
     */
    private VBox createSimulationPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(30));
        
        Label title = new Label("Simulation Temps Réel");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.rgb(31, 41, 55));
        
        simulationCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        simulationCanvas.setStyle("-fx-background-color: white;" +
                                 "-fx-border-color: " + toHexString(BORDER_COLOR) + ";" +
                                 "-fx-border-width: 1;" +
                                 "-fx-border-radius: 12;" +
                                 "-fx-background-radius: 12;" +
                                 "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 4);");
        gc = simulationCanvas.getGraphicsContext2D();
        
        // Controls avec design moderne
        HBox controls = createModernControls();
        
        panel.getChildren().addAll(title, simulationCanvas, controls);
        return panel;
    }
    
    /**
     * Contrôles modernes avec boutons fluides
     */
    private HBox createModernControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(20, 0, 0, 0));
        
        Button scenarioFamille = createModernButton("Scénario Famille", PRIMARY_COLOR, true, () -> runScenario("famille"));
        Button scenarioEcole = createModernButton("Scénario École", SUCCESS_COLOR, true, () -> runScenario("ecole"));
        Button scenarioStress = createModernButton("Test de Charge", WARNING_COLOR, true, () -> runScenario("stress"));
        Button resetBtn = createModernButton("Reset", ERROR_COLOR, true, this::resetSimulation);
        
        controls.getChildren().addAll(scenarioFamille, scenarioEcole, scenarioStress, resetBtn);
        return controls;
    }
    
    /**
     * Panel de stats avec cards modernes
     */
    private VBox createModernStatsPanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(380);
        panel.setPadding(new Insets(30, 30, 30, 20));
        panel.setStyle("-fx-background-color: " + toHexString(LIGHT_GRAY) + ";" +
                      "-fx-border-color: " + toHexString(BORDER_COLOR) + ";" +
                      "-fx-border-width: 1 0 0 1;");
        
        Label title = new Label("Analyse en Temps Réel");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.rgb(31, 41, 55));
        
        // Card moderne pour les stats
        VBox statsCard = new VBox(15);
        statsCard.setStyle("-fx-background-color: white;" +
                          "-fx-background-radius: 12;" +
                          "-fx-padding: 20;" +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 2);");
        
        agentStatsPanel = new VBox(12);
        
        ScrollPane statsScroll = new ScrollPane(agentStatsPanel);
        statsScroll.setStyle("-fx-background-color: transparent;" +
                            "-fx-border-color: transparent;" +
                            "-fx-background: transparent;");
        statsScroll.setPrefHeight(400);
        statsScroll.setFitToWidth(true);
        
        statsCard.getChildren().addAll(createStatsHeader(), statsScroll);
        
        // Card pour les métriques système
        VBox metricsCard = createSystemMetricsCard();
        
        panel.getChildren().addAll(title, statsCard, metricsCard);
        return panel;
    }
    
    /**
     * Header des statistiques avec métriques clés
     */
    private HBox createStatsHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox agentsBox = createMetricBox("Agents", "0", PRIMARY_COLOR);
        VBox groupsBox = createMetricBox("Groupes", "0", SUCCESS_COLOR);
        VBox activeBox = createMetricBox("Actifs", "0", WARNING_COLOR);
        
        agentsBox.setId("agentsMetric");
        groupsBox.setId("groupsMetric");
        activeBox.setId("activeMetric");
        
        header.getChildren().addAll(agentsBox, groupsBox, activeBox);
        return header;
    }
    
    private VBox createMetricBox(String label, String value, Color color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        valueLabel.setTextFill(color);
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        labelText.setTextFill(SECONDARY_COLOR);
        
        box.getChildren().addAll(valueLabel, labelText);
        return box;
    }
    
    /**
     * Card des métriques système
     */
    private VBox createSystemMetricsCard() {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white;" +
                     "-fx-background-radius: 12;" +
                     "-fx-padding: 20;" +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 2);");
        
        Label title = new Label("Métriques Système");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.rgb(31, 41, 55));
        
        VBox metricsContent = new VBox(10);
        metricsContent.setId("systemMetrics");
        
        card.getChildren().addAll(title, metricsContent);
        return card;
    }
    
    /**
     * Panel de logs moderne
     */
    private VBox createModernLogPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(0, 30, 30, 30));
        panel.setPrefHeight(160);
        
        HBox logHeader = new HBox(15);
        logHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Journal Système");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(Color.rgb(31, 41, 55));
        
        Button clearBtn = createModernButton("Effacer", SECONDARY_COLOR, true, () -> logTerminal.clear());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        logHeader.getChildren().addAll(title, spacer, clearBtn);
        
        logTerminal = new TextArea();
        logTerminal.setEditable(false);
        logTerminal.setStyle("-fx-control-inner-background: white;" +
                            "-fx-text-fill: #374151;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-font-size: 11px;" +
                            "-fx-border-color: " + toHexString(BORDER_COLOR) + ";" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;");
        
        panel.getChildren().addAll(logHeader, logTerminal);
        return panel;
    }
    
    /**
     * Bouton moderne avec animations
     */
    private Button createModernButton(String text, Color color, boolean outlined, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        if (outlined) {
            btn.setStyle(String.format("-fx-background-color: white;" +
                                     "-fx-text-fill: %s;" +
                                     "-fx-border-color: %s;" +
                                     "-fx-border-width: 1;" +
                                     "-fx-border-radius: 8;" +
                                     "-fx-background-radius: 8;" +
                                     "-fx-padding: 10 16;",
                                     toHexString(color), toHexString(color)));
        } else {
            btn.setTextFill(Color.WHITE);
            btn.setStyle(String.format("-fx-background-color: %s;" +
                                     "-fx-background-radius: 8;" +
                                     "-fx-padding: 10 16;",
                                     toHexString(color)));
        }
        
        // Animations fluides
        btn.setOnMouseEntered(e -> {
            btn.setOpacity(0.9);
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
        });
        btn.setOnMouseExited(e -> {
            btn.setOpacity(1.0);
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });
        btn.setOnAction(e -> action.run());
        
        return btn;
    }
    
    /**
     * Layout des salles simplifié
     */
    private void initializeRoomLayout() {
        // Salles rectangulaires simples
        rooms.add(new Room("Accueil", 50, 250, 150, 200, LIGHT_GRAY));
        rooms.add(new Room("Renaissance", 250, 150, 120, 100, Color.rgb(254, 243, 199)));
        rooms.add(new Room("Impressionnisme", 400, 150, 120, 100, Color.rgb(220, 252, 231)));
        rooms.add(new Room("Art Moderne", 550, 150, 120, 100, Color.rgb(239, 246, 255)));
        rooms.add(new Room("Sculpture", 250, 450, 120, 100, Color.rgb(254, 226, 226)));
        rooms.add(new Room("Contemporain", 400, 450, 120, 100, Color.rgb(236, 254, 255)));
        rooms.add(new Room("Repos", 550, 350, 120, 80, Color.rgb(245, 245, 245)));
        rooms.add(new Room("Sortie", 800, 250, 150, 200, LIGHT_GRAY));
        
        logMessage("Layout initialisé - 8 salles configurées");
    }
    
    /**
     * Animation fluide 60 FPS
     */
    private void startFluidAnimation() {
        renderLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_666_666) { // 60 FPS
                    time += 0.016;
                    updateSimulation();
                    renderSimulation();
                    lastUpdate = now;
                }
            }
        };
        renderLoop.start();
    }
    
    /**
     * Mise à jour de la simulation
     */
    private void updateSimulation() {
        // Mettre à jour les agents
        for (Agent agent : agents.values()) {
            agent.update();
        }
        
        // Mettre à jour les groupes
        for (TourGroup group : tourGroups) {
            group.update();
        }
        
        // Supprimer les groupes terminés et recycler les guides
        Iterator<TourGroup> groupIterator = tourGroups.iterator();
        while (groupIterator.hasNext()) {
            TourGroup group = groupIterator.next();
            if (group.isCompleted()) {
                // Les touristes disparaissent
                for (Agent tourist : group.tourists) {
                    agents.remove(tourist.id);
                }
                
                // Le guide revient au coordinateur
                if (agents.containsKey("COORDINATEUR")) {
                    Agent coordinator = agents.get("COORDINATEUR");
                    group.guide.moveTo(coordinator.x, coordinator.y);
                    group.guide.status = AgentStatus.AVAILABLE;
                    logMessage("Guide " + group.guide.id + " revient au coordinateur - Groupe terminé");
                } else {
                    group.guide.moveTo(entrancePoint.x, entrancePoint.y);
                    group.guide.status = AgentStatus.AVAILABLE;
                }
                
                groupIterator.remove();
                logMessage("Groupe " + group.name + " terminé - Touristes sortis du système");
            }
        }
    }
    
    /**
     * Rendu moderne et fluide
     */
    private void renderSimulation() {
        // Fond blanc propre
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Rendu des salles avec style moderne
        for (Room room : rooms) {
            renderModernRoom(room);
        }
        
        // Couloirs avec design épuré
        renderModernCorridors();
        
        // Groupes avec visualisation claire
        for (TourGroup group : tourGroups) {
            renderModernTourGroup(group);
        }
        
        // Agents individuels
        for (Agent agent : agents.values()) {
            if (tourGroups.stream().noneMatch(g -> g.containsAgent(agent))) {
                renderModernAgent(agent);
            }
        }
        
        // Points d'entrée/sortie
        renderModernPoints();
        
        // Interface overlay minimaliste
        renderModernOverlay();
    }
    
    /**
     * Rendu moderne des salles
     */
    private void renderModernRoom(Room room) {
        // Ombre douce
        gc.setFill(Color.rgb(0, 0, 0, 0.05));
        gc.fillRoundRect(room.x + 2, room.y + 2, room.width, room.height, 12, 12);
        
        // Salle avec couleur moderne
        gc.setFill(room.color);
        gc.fillRoundRect(room.x, room.y, room.width, room.height, 12, 12);
        
        // Bordure subtile
        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(1);
        gc.strokeRoundRect(room.x, room.y, room.width, room.height, 12, 12);
        
        // Texte moderne
        gc.setFill(Color.rgb(75, 85, 99));
        gc.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        gc.fillText(room.name, room.x + 10, room.y + 25);
    }
    
    /**
     * Couloirs modernes épurés
     */
    private void renderModernCorridors() {
        gc.setFill(Color.rgb(248, 250, 252));
        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(1);
        
        // Couloir principal horizontal
        gc.fillRoundRect(200, 325, 450, 50, 25, 25);
        gc.strokeRoundRect(200, 325, 450, 50, 25, 25);
        
        // Connexions verticales
        gc.fillRoundRect(300, 250, 20, 75, 10, 10);
        gc.fillRoundRect(450, 250, 20, 75, 10, 10);
        gc.fillRoundRect(600, 250, 20, 75, 10, 10);
        gc.fillRoundRect(300, 400, 20, 50, 10, 10);
        gc.fillRoundRect(450, 400, 20, 50, 10, 10);
    }
    
    /**
     * Groupe moderne avec design épuré
     */
    private void renderModernTourGroup(TourGroup group) {
        if (group.guide == null || group.tourists.isEmpty()) return;
        
        Agent guide = group.guide;
        
        // Zone du groupe avec style moderne
        double groupRadius = 30 + group.tourists.size() * 2;
        gc.setStroke(Color.rgb(59, 130, 246, 0.4));
        gc.setLineWidth(2);
        gc.strokeOval(guide.x - groupRadius, guide.y - groupRadius, 
                     groupRadius * 2, groupRadius * 2);
        
        // Connexions subtiles
        gc.setStroke(Color.rgb(59, 130, 246, 0.2));
        gc.setLineWidth(1);
        for (Agent tourist : group.tourists) {
            gc.strokeLine(guide.x, guide.y, tourist.x, tourist.y);
        }
        
        // Agents du groupe
        renderModernAgent(guide);
        for (Agent tourist : group.tourists) {
            renderModernAgent(tourist);
        }
        
        // Info groupe moderne
        renderModernGroupInfo(group);
    }
    
    /**
     * Agent avec design moderne
     */
    private void renderModernAgent(Agent agent) {
        Color agentColor = getModernAgentColor(agent.type);
        double size = getAgentSize(agent.type);
        
        // Ombre moderne
        gc.setFill(Color.rgb(0, 0, 0, 0.1));
        gc.fillOval(agent.x - size/2 + 1, agent.y - size/2 + 1, size, size);
        
        // Corps de l'agent
        gc.setFill(agentColor);
        gc.fillOval(agent.x - size/2, agent.y - size/2, size, size);
        
        // Bordure moderne
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(agent.x - size/2, agent.y - size/2, size, size);
        
        // Indicateur de statut
        if (agent.status == AgentStatus.BUSY) {
            gc.setFill(SUCCESS_COLOR);
            gc.fillOval(agent.x + size/2 - 3, agent.y - size/2 + 1, 6, 6);
        }
        
        // Direction fluide
        if (agent.hasDestination()) {
            double angle = Math.atan2(agent.targetY - agent.y, agent.targetX - agent.x);
            double arrowX = agent.x + Math.cos(angle) * (size/2 + 10);
            double arrowY = agent.y + Math.sin(angle) * (size/2 + 10);
            
            gc.setFill(Color.rgb(59, 130, 246, 0.6));
            gc.fillOval(arrowX - 4, arrowY - 4, 8, 8);
        }
        
        // Label moderne
        gc.setFill(Color.rgb(107, 114, 128));
        gc.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 9));
        gc.fillText(agent.getShortName(), agent.x - 15, agent.y + size/2 + 15);
    }
    
    /**
     * Info groupe moderne avec card
     */
    private void renderModernGroupInfo(TourGroup group) {
        double infoX = group.guide.x + 35;
        double infoY = group.guide.y - 45;
        
        // Card moderne
        gc.setFill(Color.rgb(255, 255, 255, 0.95));
        gc.fillRoundRect(infoX, infoY, 140, 40, 8, 8);
        
        // Bordure subtile
        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(1);
        gc.strokeRoundRect(infoX, infoY, 140, 40, 8, 8);
        
        // Texte moderne
        gc.setFill(Color.rgb(31, 41, 55));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.fillText(group.name, infoX + 8, infoY + 15);
        
        gc.setFill(SECONDARY_COLOR);
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 9));
        gc.fillText(group.tourists.size() + " visiteurs • " + group.currentLocation, infoX + 8, infoY + 28);
    }
    
    /**
     * Points d'entrée/sortie modernes
     */
    private void renderModernPoints() {
        // Entrée
        gc.setFill(SUCCESS_COLOR);
        gc.fillOval(entrancePoint.x - 12, entrancePoint.y - 12, 24, 24);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.fillText("ENTRÉE", entrancePoint.x - 20, entrancePoint.y + 30);
        
        // Sortie
        gc.setFill(ERROR_COLOR);
        gc.fillOval(exitPoint.x - 12, exitPoint.y - 12, 24, 24);
        gc.setFill(Color.WHITE);
        gc.fillText("SORTIE", exitPoint.x - 18, exitPoint.y + 30);
    }
    
    /**
     * Overlay moderne minimaliste
     */
    private void renderModernOverlay() {
        // Compteurs en temps réel
        gc.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        gc.setFill(Color.rgb(107, 114, 128, 0.8));
        
        int y = 25;
        gc.fillText("Temps: " + String.format("%.1fs", time), CANVAS_WIDTH - 120, y);
        y += 20;
        gc.fillText("FPS: 60", CANVAS_WIDTH - 120, y);
    }
    
    // Actions système avec logique métier correcte
    
    private void startJadeSystem() {
        systemProgress.setVisible(true);
        
        new Thread(() -> {
            try {
                boolean success = SimpleLauncher.startJadeSystem();
                Platform.runLater(() -> {
                    systemProgress.setVisible(false);
                    
                    if (success) {
                        jadeSystemRunning = true;
                        systemStatusLabel.setText("Système JADE actif");
                        systemStatusLabel.setTextFill(SUCCESS_COLOR);
                        
                        // Coordinateur au centre
                        Agent coordinator = new Agent("COORDINATEUR", AgentType.COORDINATOR);
                        coordinator.setPosition(CANVAS_WIDTH/2, CANVAS_HEIGHT/2);
                        coordinator.status = AgentStatus.ACTIVE;
                        agents.put("COORDINATEUR", coordinator);
                        
                        logMessage("Système JADE démarré avec succès");
                        logMessage("Coordinateur initialisé en position centrale");
                    } else {
                        systemStatusLabel.setText("Échec du démarrage JADE");
                        systemStatusLabel.setTextFill(ERROR_COLOR);
                        logMessage("ERREUR: Échec du démarrage JADE");
                    }
                    
                    updateStatsDisplay();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    systemProgress.setVisible(false);
                    logMessage("ERREUR CRITIQUE: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void stopSystem() {
        jadeSystemRunning = false;
        SimpleLauncher.shutdownSystem();
        
        agents.clear();
        tourGroups.clear();
        
        systemStatusLabel.setText("Système arrêté");
        systemStatusLabel.setTextFill(SECONDARY_COLOR);
        
        logMessage("Système JADE arrêté - Tous les agents supprimés");
        updateStatsDisplay();
    }
    
    private void addGuide() {
        if (!jadeSystemRunning) {
            logMessage("ERREUR: Démarrez le système JADE d'abord");
            return;
        }
        
        String[] specializations = {"Renaissance", "Impressionnisme", "Moderne", "Contemporain", "Classique"};
        String spec = specializations[(int)(Math.random() * specializations.length)];
        String id = "Guide_" + spec.substring(0, 3).toUpperCase() + "_" + String.format("%02d", agents.size() + 1);
        
        Agent guide = new Agent(id, AgentType.GUIDE);
        guide.specialization = spec;
        guide.status = AgentStatus.AVAILABLE;
        
        // Position près du coordinateur
        if (agents.containsKey("COORDINATEUR")) {
            Agent coordinator = agents.get("COORDINATEUR");
            guide.setPosition(coordinator.x + (Math.random() - 0.5) * 60, 
                            coordinator.y + (Math.random() - 0.5) * 60);
        } else {
            guide.setPosition(entrancePoint.x + Math.random() * 80, 
                            entrancePoint.y + Math.random() * 80);
        }
        
        agents.put(id, guide);
        SimpleLauncher.addGuide(id, spec);
        
        logMessage("Guide ajouté: " + id + " - Spécialisation: " + spec);
        updateStatsDisplay();
    }
    
    private void addTourist() {
        if (!jadeSystemRunning) {
            logMessage("ERREUR: Démarrez le système JADE d'abord");
            return;
        }
        
        String[] nationalities = {"FR", "IT", "EN", "DE", "ES", "JP", "US", "CN"};
        String[] names = {"Alice", "Bob", "Charlie", "Diana", "Emma", "Felix", "Grace", "Hugo"};
        
        String nationality = nationalities[(int)(Math.random() * nationalities.length)];
        String name = names[(int)(Math.random() * names.length)];
        String id = name + "_" + nationality;
        
        // Éviter les doublons
        int counter = 1;
        while (agents.containsKey(id)) {
            id = name + "_" + nationality + "_" + String.format("%02d", counter++);
        }
        
        Agent tourist = new Agent(id, AgentType.TOURIST);
        tourist.nationality = nationality;
        tourist.status = AgentStatus.WAITING;
        tourist.setPosition(entrancePoint.x + Math.random() * 120, 
                          entrancePoint.y + Math.random() * 120);
        
        agents.put(id, tourist);
        SimpleLauncher.addTourist(id);
        
        logMessage("Touriste ajouté: " + id + " (" + nationality + ")");
        updateStatsDisplay();
        
        // Tentative d'assignation automatique
        scheduler.schedule(() -> Platform.runLater(this::tryAssignTouristsToGroups), 2, TimeUnit.SECONDS);
    }
    
    private void runScenario(String type) {
        if (!jadeSystemRunning) {
            logMessage("ERREUR: Système JADE requis pour les scénarios");
            return;
        }
        
        switch (type) {
            case "famille":
                logMessage("SCÉNARIO FAMILLE: 1 guide + 4 touristes");
                addGuide();
                scheduler.schedule(() -> Platform.runLater(() -> {
                    for (int i = 0; i < 4; i++) {
                        addTourist();
                        try { Thread.sleep(500); } catch (InterruptedException e) {}
                    }
                    scheduler.schedule(() -> Platform.runLater(this::formTourGroups), 3, TimeUnit.SECONDS);
                }), 1, TimeUnit.SECONDS);
                break;
                
            case "ecole":
                logMessage("SCÉNARIO ÉCOLE: 2 guides + 12 touristes");
                addGuide();
                addGuide();
                scheduler.schedule(() -> Platform.runLater(() -> {
                    for (int i = 0; i < 12; i++) {
                        addTourist();
                        try { Thread.sleep(300); } catch (InterruptedException e) {}
                    }
                    scheduler.schedule(() -> Platform.runLater(this::formTourGroups), 4, TimeUnit.SECONDS);
                }), 1, TimeUnit.SECONDS);
                break;
                
            case "stress":
                logMessage("SCÉNARIO STRESS: Arrivée continue de visiteurs");
                // Ajouter quelques guides d'abord
                for (int i = 0; i < 3; i++) {
                    addGuide();
                }
                
                // Puis créer un flux continu de touristes
                scheduler.scheduleAtFixedRate(() -> Platform.runLater(() -> {
                    if (agents.size() < 40) { // Limite de sécurité
                        addTourist();
                        if (Math.random() < 0.2) { // 20% chance d'ajouter un guide
                            addGuide();
                        }
                    }
                }), 2, 3, TimeUnit.SECONDS);
                
                // Formation de groupes périodique
                scheduler.scheduleAtFixedRate(() -> Platform.runLater(this::formTourGroups), 
                                            5, 8, TimeUnit.SECONDS);
                break;
        }
    }
    
    private void resetSimulation() {
        // Arrêter toutes les tâches programmées
        scheduler.shutdownNow();
        scheduler = Executors.newScheduledThreadPool(3);
        
        // Nettoyer les données
        agents.clear();
        tourGroups.clear();
        
        systemStatusLabel.setText("Simulation réinitialisée");
        systemStatusLabel.setTextFill(SECONDARY_COLOR);
        
        logMessage("Simulation réinitialisée - Tous les agents supprimés");
        updateStatsDisplay();
        
        // Redémarrer le monitoring
        startSystemMonitoring();
    }
    
    /**
     * Formation intelligente des groupes
     */
    private void formTourGroups() {
        List<Agent> availableGuides = agents.values().stream()
                .filter(a -> a.type == AgentType.GUIDE)
                .filter(a -> a.status == AgentStatus.AVAILABLE)
                .collect(java.util.stream.Collectors.toList());
        
        List<Agent> waitingTourists = agents.values().stream()
                .filter(a -> a.type == AgentType.TOURIST)
                .filter(a -> a.status == AgentStatus.WAITING)
                .collect(java.util.stream.Collectors.toList());
        
        if (availableGuides.isEmpty() || waitingTourists.isEmpty()) {
            return;
        }
        
        Collections.shuffle(waitingTourists);
        
        for (Agent guide : availableGuides) {
            if (waitingTourists.isEmpty()) break;
            
            // Taille de groupe optimal: 3-6 touristes
            int groupSize = Math.min(3 + (int)(Math.random() * 4), waitingTourists.size());
            List<Agent> groupTourists = new ArrayList<>();
            
            for (int i = 0; i < groupSize; i++) {
                groupTourists.add(waitingTourists.remove(0));
            }
            
            // Créer le groupe
            String groupName = guide.specialization + "_Tour_" + String.format("%02d", tourGroups.size() + 1);
            TourGroup tourGroup = new TourGroup(groupName, guide, groupTourists);
            tourGroups.add(tourGroup);
            
            // Mettre à jour les statuts
            guide.status = AgentStatus.BUSY;
            for (Agent tourist : groupTourists) {
                tourist.status = AgentStatus.IN_GROUP;
            }
            
            logMessage("Groupe formé: " + groupName + " - " + guide.id + " avec " + groupSize + " touristes");
        }
        
        updateStatsDisplay();
    }
    
    /**
     * Assignation automatique des nouveaux touristes
     */
    private void tryAssignTouristsToGroups() {
        List<Agent> waitingTourists = agents.values().stream()
            .filter(a -> a.type == AgentType.TOURIST)
            .filter(a -> a.status == AgentStatus.WAITING)
            .collect(java.util.stream.Collectors.toList());
        
        if (waitingTourists.isEmpty()) return;
        
        // Chercher des groupes existants non complets
        for (TourGroup group : tourGroups) {
            if (group.tourists.size() < 6 && !waitingTourists.isEmpty()) {
                Agent tourist = waitingTourists.remove(0);
                group.addTourist(tourist);
                tourist.status = AgentStatus.IN_GROUP;
                
                logMessage("Touriste " + tourist.id + " rejoint le groupe " + group.name);
            }
        }
        
        // Former de nouveaux groupes si nécessaire
        if (waitingTourists.size() >= 3) {
            formTourGroups();
        }
    }
    
    /**
     * Monitoring système en temps réel
     */
    private void startSystemMonitoring() {
        scheduler.scheduleAtFixedRate(this::updateStatsDisplay, 1, 2, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::simulateGroupMovements, 5, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::updateSystemMetrics, 3, 5, TimeUnit.SECONDS);
    }
    
    private void updateStatsDisplay() {
        Platform.runLater(() -> {
            // Métriques principales
            VBox agentsMetric = (VBox) root.lookup("#agentsMetric");
            VBox groupsMetric = (VBox) root.lookup("#groupsMetric");
            VBox activeMetric = (VBox) root.lookup("#activeMetric");
            
            if (agentsMetric != null) {
                Label agentsValue = (Label) agentsMetric.getChildren().get(0);
                agentsValue.setText(String.valueOf(agents.size()));
            }
            
            if (groupsMetric != null) {
                Label groupsValue = (Label) groupsMetric.getChildren().get(0);
                groupsValue.setText(String.valueOf(tourGroups.size()));
            }
            
            if (activeMetric != null) {
                Label activeValue = (Label) activeMetric.getChildren().get(0);
                long activeCount = agents.values().stream()
                    .filter(a -> a.status == AgentStatus.BUSY || a.status == AgentStatus.IN_GROUP)
                    .count();
                activeValue.setText(String.valueOf(activeCount));
            }
            
            // Détails des agents
            updateAgentDetails();
        });
    }
    
    private void updateAgentDetails() {
        agentStatsPanel.getChildren().clear();
        
        // Groupes actifs
        if (!tourGroups.isEmpty()) {
            Label groupsTitle = new Label("Groupes Actifs (" + tourGroups.size() + ")");
            groupsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            groupsTitle.setTextFill(Color.rgb(31, 41, 55));
            agentStatsPanel.getChildren().add(groupsTitle);
            
            for (TourGroup group : tourGroups) {
                agentStatsPanel.getChildren().add(createGroupDetailCard(group));
            }
        }
        
        // Agents en attente
        List<Agent> waitingAgents = agents.values().stream()
            .filter(a -> a.status == AgentStatus.AVAILABLE || a.status == AgentStatus.WAITING)
            .collect(java.util.stream.Collectors.toList());
        
        if (!waitingAgents.isEmpty()) {
            Label waitingTitle = new Label("En Attente (" + waitingAgents.size() + ")");
            waitingTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            waitingTitle.setTextFill(SECONDARY_COLOR);
            agentStatsPanel.getChildren().add(waitingTitle);
            
            for (Agent agent : waitingAgents) {
                agentStatsPanel.getChildren().add(createAgentDetailCard(agent));
            }
        }
    }
    
    private VBox createGroupDetailCard(TourGroup group) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white;" +
                     "-fx-background-radius: 8;" +
                     "-fx-border-color: " + toHexString(BORDER_COLOR) + ";" +
                     "-fx-border-radius: 8;" +
                     "-fx-border-width: 1;");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label groupName = new Label(group.name);
        groupName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        groupName.setTextFill(PRIMARY_COLOR);
        
        Label touristCount = new Label(group.tourists.size() + " visiteurs");
        touristCount.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        touristCount.setTextFill(SUCCESS_COLOR);
        
        header.getChildren().addAll(groupName, touristCount);
        
        Label guideInfo = new Label("Guide: " + group.guide.getShortName() + " (" + group.guide.specialization + ")");
        guideInfo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        guideInfo.setTextFill(SECONDARY_COLOR);
        
        Label locationInfo = new Label("📍 " + group.currentLocation + " • Œuvre " + (group.visitedArtworks + 1) + "/5");
        locationInfo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        locationInfo.setTextFill(SECONDARY_COLOR);
        
        card.getChildren().addAll(header, guideInfo, locationInfo);
        return card;
    }
    
    private HBox createAgentDetailCard(Agent agent) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: " + toHexString(LIGHT_GRAY) + ";" +
                     "-fx-background-radius: 6;" +
                     "-fx-border-radius: 6;");
        
        // Indicateur coloré
        javafx.scene.shape.Circle indicator = new javafx.scene.shape.Circle(4);
        indicator.setFill(getModernAgentColor(agent.type));
        
        VBox info = new VBox(2);
        
        Label nameLabel = new Label(agent.getShortName());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 10));
        nameLabel.setTextFill(Color.rgb(31, 41, 55));
        
        Label statusLabel = new Label(getStatusText(agent));
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 9));
        statusLabel.setTextFill(SECONDARY_COLOR);
        
        info.getChildren().addAll(nameLabel, statusLabel);
        
        card.getChildren().addAll(indicator, info);
        return card;
    }
    
    private void updateSystemMetrics() {
        Platform.runLater(() -> {
            VBox metricsContent = (VBox) root.lookup("#systemMetrics");
            if (metricsContent == null) return;
            
            metricsContent.getChildren().clear();
            
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long totalMemory = runtime.totalMemory() / (1024 * 1024);
            
            metricsContent.getChildren().addAll(
                createMetricRow("Temps de fonctionnement", formatTime((long)time)),
                createMetricRow("Mémoire utilisée", usedMemory + " MB / " + totalMemory + " MB"),
                createMetricRow("Groupes actifs", String.valueOf(tourGroups.size())),
                createMetricRow("Taux d'occupation", calculateOccupancyRate() + "%")
            );
        });
    }
    
    private HBox createMetricRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        labelText.setTextFill(SECONDARY_COLOR);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label valueText = new Label(value);
        valueText.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 10));
        valueText.setTextFill(Color.rgb(31, 41, 55));
        
        row.getChildren().addAll(labelText, spacer, valueText);
        return row;
    }
    
    /**
     * Simulation des mouvements de groupe
     */
    private void simulateGroupMovements() {
        if (!jadeSystemRunning || tourGroups.isEmpty()) return;
        
        Platform.runLater(() -> {
            for (TourGroup group : tourGroups) {
                if (Math.random() < 0.6) { // 60% chance de bouger
                    moveGroupToNextLocation(group);
                }
            }
        });
    }
    
    private void moveGroupToNextLocation(TourGroup group) {
        if (group.visitedArtworks >= 5) {
            // Visite terminée
            group.moveToDestination(exitPoint.x, exitPoint.y, "Sortie");
            logMessage("Groupe " + group.name + " termine sa visite - Direction sortie");
            
            // Marquer comme terminé après délai
            scheduler.schedule(() -> Platform.runLater(() -> {
                group.completed = true;
                logMessage("Groupe " + group.name + " quitte le musée");
            }), 8, TimeUnit.SECONDS);
            
            return;
        }
        
        // Sélectionner une salle au hasard
        Room targetRoom = rooms.get(2 + (int)(Math.random() * (rooms.size() - 4))); // Éviter accueil/sortie
        double targetX = targetRoom.x + targetRoom.width/2;
        double targetY = targetRoom.y + targetRoom.height/2;
        
        group.moveToDestination(targetX, targetY, targetRoom.name);
        group.visitedArtworks++;
        
        logMessage("Groupe " + group.name + " visite " + targetRoom.name + " (" + group.visitedArtworks + "/5)");
    }
    
    // Méthodes utilitaires
    
    private Color getModernAgentColor(AgentType type) {
        switch (type) {
            case COORDINATOR: return Color.rgb(168, 85, 247);
            case GUIDE: return PRIMARY_COLOR;
            case TOURIST: return SUCCESS_COLOR;
            default: return SECONDARY_COLOR;
        }
    }
    
    private double getAgentSize(AgentType type) {
        switch (type) {
            case COORDINATOR: return 20;
            case GUIDE: return 14;
            case TOURIST: return 10;
            default: return 12;
        }
    }
    
    private String getStatusText(Agent agent) {
        String status = agent.status.toString().toLowerCase().replace('_', ' ');
        if (agent.type == AgentType.GUIDE && !agent.specialization.isEmpty()) {
            return status + " • " + agent.specialization;
        } else if (agent.type == AgentType.TOURIST && !agent.nationality.isEmpty()) {
            return status + " • " + agent.nationality;
        }
        return status;
    }
    
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        if (hours > 0) {
            return String.format("%dh %02dm", hours, minutes % 60);
        } else {
            return String.format("%dm %02ds", minutes, seconds % 60);
        }
    }
    
    private int calculateOccupancyRate() {
        if (agents.isEmpty()) return 0;
        long busyAgents = agents.values().stream()
            .filter(a -> a.status == AgentStatus.BUSY || a.status == AgentStatus.IN_GROUP)
            .count();
        return (int) ((busyAgents * 100) / agents.size());
    }
    
    private void logMessage(String message) {
        String timestamp = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logEntry = "[" + timestamp + "] " + message + "\n";
        
        Platform.runLater(() -> {
            logTerminal.appendText(logEntry);
            logTerminal.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
    
    private String getModernCSS() {
        return """
        .button {
            -fx-font-family: 'Segoe UI';
            -fx-cursor: hand;
        }
        .button:hover {
            -fx-scale-x: 1.02;
            -fx-scale-y: 1.02;
        }
        .label {
            -fx-font-family: 'Segoe UI';
        }
        .text-area {
            -fx-font-family: 'Segoe UI';
        }
        .scroll-pane {
            -fx-background-color: transparent;
        }
        .scroll-pane .viewport {
            -fx-background-color: transparent;
        }
        .scroll-pane .content {
            -fx-background-color: transparent;
        }
        .separator {
            -fx-background-color: #e5e7eb;
        }
        .progress-indicator {
            -fx-progress-color: #3b82f6;
        }
        """;
    }
    
    private void cleanup() {
        if (renderLoop != null) renderLoop.stop();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        SimpleLauncher.shutdownSystem();
    }
    
    // Classes internes
    
    public enum AgentType {
        COORDINATOR, GUIDE, TOURIST
    }
    
    public enum AgentStatus {
        AVAILABLE, WAITING, BUSY, IN_GROUP, ACTIVE
    }
    
    public class Agent {
        String id;
        AgentType type;
        AgentStatus status = AgentStatus.AVAILABLE;
        double x, y, targetX, targetY;
        String specialization = "";
        String nationality = "";
        double speed = 1.5;
        
        public Agent(String id, AgentType type) {
            this.id = id;
            this.type = type;
            this.targetX = this.x;
            this.targetY = this.y;
        }
        
        public void setPosition(double x, double y) {
            this.x = x;
            this.y = y;
            this.targetX = x;
            this.targetY = y;
        }
        
        public void moveTo(double x, double y) {
            this.targetX = x;
            this.targetY = y;
        }
        
        public boolean hasDestination() {
            return Math.abs(targetX - x) > 3 || Math.abs(targetY - y) > 3;
        }
        
        public String getShortName() {
            return id.length() > 12 ? id.substring(0, 12) + "..." : id;
        }
        
        public void update() {
            if (hasDestination()) {
                double dx = targetX - x;
                double dy = targetY - y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance > speed) {
                    x += (dx / distance) * speed;
                    y += (dy / distance) * speed;
                } else {
                    x = targetX;
                    y = targetY;
                }
            }
        }
    }
    
    public class TourGroup {
        String name;
        Agent guide;
        List<Agent> tourists;
        String currentLocation = "Accueil";
        int visitedArtworks = 0;
        boolean completed = false;
        
        public TourGroup(String name, Agent guide, List<Agent> tourists) {
            this.name = name;
            this.guide = guide;
            this.tourists = new ArrayList<>(tourists);
            
            // Position initiale près de l'entrée
            moveToDestination(entrancePoint.x + 50, entrancePoint.y, "Accueil");
        }
        
        public void addTourist(Agent tourist) {
            tourists.add(tourist);
            // Repositionner le groupe
            moveToDestination(guide.targetX, guide.targetY, currentLocation);
        }
        
        public boolean containsAgent(Agent agent) {
            return guide.equals(agent) || tourists.contains(agent);
        }
        
        public void moveToDestination(double x, double y, String locationName) {
            currentLocation = locationName;
            
            // Guide au centre
            guide.moveTo(x, y);
            
            // Touristes en formation circulaire
            for (int i = 0; i < tourists.size(); i++) {
                double angle = (i * 2 * Math.PI) / tourists.size();
                double radius = 25 + (i / 4) * 12;
                
                double touristX = x + Math.cos(angle) * radius;
                double touristY = y + Math.sin(angle) * radius;
                
                tourists.get(i).moveTo(touristX, touristY);
            }
        }
        
        public void update() {
            // Vérifier si le groupe est arrivé
            if (!guide.hasDestination() && currentLocation.equals("Sortie")) {
                boolean allArrived = tourists.stream().noneMatch(Agent::hasDestination);
                if (allArrived) {
                    completed = true;
                }
            }
        }
        
        public boolean isCompleted() {
            return completed;
        }
    }
    
    public class Room {
        String name;
        double x, y, width, height;
        Color color;
        
        public Room(String name, double x, double y, double width, double height, Color color) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    }
    
    public class Point2D {
        double x, y;
        
        public Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    
    // Stub pour SimpleLauncher
    public static class SimpleLauncher {
        public static boolean startJadeSystem() {
            try {
                Thread.sleep(1500);
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        }
        
        public static void shutdownSystem() {}
        public static void addTourist(String name) {}
        public static void addGuide(String name, String specialization) {}
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}