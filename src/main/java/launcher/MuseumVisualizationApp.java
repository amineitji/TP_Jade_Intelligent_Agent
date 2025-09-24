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
import javafx.scene.effect.GaussianBlur;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Interface moderne et scientifique pour le système multi-agents JADE
 * Design inspiré d'Apple - Simple, moderne et élégant
 */
public class MuseumVisualizationApp extends Application {

    // Configuration responsive - dimensions adaptatives
    private final DoubleProperty canvasWidth = new SimpleDoubleProperty(800);
    private final DoubleProperty canvasHeight = new SimpleDoubleProperty(500);
    
    // Couleurs Apple-style modernes
    private static final Color BG_COLOR = Color.rgb(248, 249, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY_COLOR = Color.rgb(0, 122, 255); // iOS Blue
    private static final Color SUCCESS_COLOR = Color.rgb(52, 199, 89); // iOS Green
    private static final Color WARNING_COLOR = Color.rgb(255, 149, 0); // iOS Orange
    private static final Color ERROR_COLOR = Color.rgb(255, 59, 48); // iOS Red
    private static final Color SECONDARY_COLOR = Color.rgb(142, 142, 147); // iOS Gray
    private static final Color LIGHT_GRAY = Color.rgb(242, 242, 247);
    private static final Color BORDER_COLOR = Color.rgb(209, 209, 214);
    private static final Color SURFACE_COLOR = Color.rgb(255, 255, 255, 0.8);
    
    // Composants GUI responsive
    private Canvas simulationCanvas;
    private GraphicsContext gc;
    private BorderPane root;
    private VBox agentStatsPanel;
    private TextArea logTerminal;
    private Label systemStatusLabel;
    private ProgressIndicator systemProgress;
    private ScrollPane rightScrollPane;
    private SplitPane mainSplitPane;
    
    // Animation fluide
    private AnimationTimer renderLoop;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    
    // Données système
    private Map<String, Agent> agents = new HashMap<>();
    private List<TourGroup> tourGroups = new ArrayList<>();
    private List<Room> rooms = new ArrayList<>();
    private Point2D entrancePoint = new Point2D(80, 250);
    private Point2D exitPoint = new Point2D(720, 250);
    private boolean jadeSystemRunning = false;
    private double time = 0;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Système Multi-Agents JADE");
        
        // Configuration responsive de la fenêtre
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(1400);
        primaryStage.setHeight(900);
        
        initializeModernInterface(primaryStage);
        initializeRoomLayout();
        setupResponsiveBehavior(primaryStage);
        startFluidAnimation();
        startSystemMonitoring();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("data:text/css," + getAppleStyleCSS());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(e -> cleanup());
    }
    
    /**
     * Interface moderne inspirée d'Apple
     */
    private void initializeModernInterface(Stage stage) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + toHexString(BG_COLOR) + ";");
        
        // Header moderne avec effet de transparence
        VBox headerWrapper = createAppleHeader();
        root.setTop(headerWrapper);
        
        // Configuration du layout principal avec SplitPane responsive
        mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        
        // Zone de simulation adaptative
        VBox centerPanel = createResponsiveSimulationPanel(stage);
        
        // Panel latéral avec design Apple
        VBox rightPanel = createAppleStatsPanel();
        rightScrollPane = new ScrollPane(rightPanel);
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        rightScrollPane.setPrefWidth(350);
        rightScrollPane.setMinWidth(300);
        rightScrollPane.setMaxWidth(400);
        
        mainSplitPane.getItems().addAll(centerPanel, rightScrollPane);
        mainSplitPane.setDividerPositions(0.75);
        
        root.setCenter(mainSplitPane);
        
        // Footer minimaliste
        VBox bottomPanel = createAppleLogPanel();
        root.setBottom(bottomPanel);
    }
    
    /**
     * Header Apple-style avec effet glassmorphism
     */
    private VBox createAppleHeader() {
        VBox headerWrapper = new VBox();
        
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " +
            "rgba(255,255,255,0.95), rgba(255,255,255,0.9));" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + toHexString(BORDER_COLOR) + ";" +
            "-fx-border-width: 0 0 0.5 0;"
        );
        
        Text title = new Text("Système Multi-Agents");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setFill(Color.rgb(28, 28, 30));
        
        systemProgress = new ProgressIndicator();
        systemProgress.setPrefSize(20, 20);
        systemProgress.setVisible(false);
        
        systemStatusLabel = new Label("Système inactif");
        systemStatusLabel.setTextFill(SECONDARY_COLOR);
        systemStatusLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        
        // Boutons Apple-style
        Button startBtn = createAppleButton("Démarrer", PRIMARY_COLOR, false, this::startJadeSystem);
        Button stopBtn = createAppleButton("Arrêter", ERROR_COLOR, false, this::stopSystem);
        Button addGuideBtn = createAppleButton("+ Guide", SUCCESS_COLOR, true, this::addGuide);
        Button addTouristBtn = createAppleButton("+ Touriste", WARNING_COLOR, true, this::addTourist);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(title, spacer, systemProgress, systemStatusLabel, 
                                   startBtn, stopBtn, addGuideBtn, addTouristBtn);
        
        headerWrapper.getChildren().add(header);
        return headerWrapper;
    }
    
    /**
     * Panel de simulation responsive
     */
    private VBox createResponsiveSimulationPanel(Stage stage) {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        
        // Titre avec style Apple
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Simulation Temps Réel");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.rgb(28, 28, 30));
        
        // Indicateurs de performance en temps réel
        Label performanceLabel = new Label("●");
        performanceLabel.setTextFill(SUCCESS_COLOR);
        performanceLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label fpsLabel = new Label("60 FPS");
        fpsLabel.setTextFill(SECONDARY_COLOR);
        fpsLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        
        titleBox.getChildren().addAll(title, performanceLabel, fpsLabel);
        
        // Canvas responsive dans une card
        VBox canvasContainer = new VBox();
        canvasContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 2);" +
            "-fx-padding: 0;"
        );
        
        simulationCanvas = new Canvas();
        
        // Binding responsive du canvas
        canvasWidth.bind(Bindings.createDoubleBinding(
            () -> Math.max(600, stage.widthProperty().get() * 0.6 - 100),
            stage.widthProperty()
        ));
        
        canvasHeight.bind(Bindings.createDoubleBinding(
            () -> Math.max(400, stage.heightProperty().get() * 0.5 - 100),
            stage.heightProperty()
        ));
        
        simulationCanvas.widthProperty().bind(canvasWidth);
        simulationCanvas.heightProperty().bind(canvasHeight);
        
        gc = simulationCanvas.getGraphicsContext2D();
        
        canvasContainer.getChildren().add(simulationCanvas);
        
        // Controls avec design Apple épuré
        HBox controls = createAppleControls();
        
        VBox.setVgrow(canvasContainer, Priority.ALWAYS);
        panel.getChildren().addAll(titleBox, canvasContainer, controls);
        return panel;
    }
    
    /**
     * Contrôles Apple-style avec animations subtiles
     */
    private HBox createAppleControls() {
        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(20, 0, 10, 0));
        
        Button scenarioFamille = createAppleButton("Famille", PRIMARY_COLOR, true, () -> runScenario("famille"));
        Button scenarioEcole = createAppleButton("École", SUCCESS_COLOR, true, () -> runScenario("ecole"));
        Button scenarioStress = createAppleButton("Test de Charge", WARNING_COLOR, true, () -> runScenario("stress"));
        Button resetBtn = createAppleButton("Reset", SECONDARY_COLOR, true, this::resetSimulation);
        
        controls.getChildren().addAll(scenarioFamille, scenarioEcole, scenarioStress, resetBtn);
        return controls;
    }
    
    /**
     * Panel de stats Apple-style
     */
    private VBox createAppleStatsPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: " + toHexString(BG_COLOR) + ";");
        
        Label title = new Label("Analyse en Temps Réel");
        title.setFont(Font.font("System", FontWeight.BOLD, 17));
        title.setTextFill(Color.rgb(28, 28, 30));
        
        // Card principale avec effet glassmorphism
        VBox statsCard = new VBox(12);
        statsCard.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95);" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 25, 0, 0, 3);"
        );
        
        // Métriques principales avec design Apple
        HBox metricsHeader = createAppleMetricsHeader();
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: " + toHexString(BORDER_COLOR) + ";");
        
        agentStatsPanel = new VBox(10);
        
        ScrollPane statsScroll = new ScrollPane(agentStatsPanel);
        statsScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        statsScroll.setPrefHeight(300);
        statsScroll.setFitToWidth(true);
        
        statsCard.getChildren().addAll(metricsHeader, separator, statsScroll);
        
        // Card système avec design minimal
        VBox systemCard = createAppleSystemCard();
        
        panel.getChildren().addAll(title, statsCard, systemCard);
        return panel;
    }
    
    /**
     * Métriques header Apple-style
     */
    private HBox createAppleMetricsHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER);
        
        VBox agentsBox = createAppleMetricBox("Agents", "0", PRIMARY_COLOR);
        VBox groupsBox = createAppleMetricBox("Groupes", "0", SUCCESS_COLOR);
        VBox activeBox = createAppleMetricBox("Actifs", "0", WARNING_COLOR);
        
        agentsBox.setId("agentsMetric");
        groupsBox.setId("groupsMetric");
        activeBox.setId("activeMetric");
        
        header.getChildren().addAll(agentsBox, groupsBox, activeBox);
        return header;
    }
    
    private VBox createAppleMetricBox(String label, String value, Color color) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setTextFill(color);
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("System", FontWeight.NORMAL, 11));
        labelText.setTextFill(SECONDARY_COLOR);
        
        box.getChildren().addAll(valueLabel, labelText);
        return box;
    }
    
    /**
     * Card système Apple-style
     */
    private VBox createAppleSystemCard() {
        VBox card = new VBox(12);
        card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95);" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 25, 0, 0, 3);"
        );
        
        Label title = new Label("Métriques Système");
        title.setFont(Font.font("System", FontWeight.BOLD, 15));
        title.setTextFill(Color.rgb(28, 28, 30));
        
        VBox metricsContent = new VBox(8);
        metricsContent.setId("systemMetrics");
        
        card.getChildren().addAll(title, metricsContent);
        return card;
    }
    
    /**
     * Panel de logs Apple-style
     */
    private VBox createAppleLogPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(0, 20, 20, 20));
        panel.setPrefHeight(140);
        panel.setMaxHeight(200);
        
        HBox logHeader = new HBox(12);
        logHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Journal Système");
        title.setFont(Font.font("System", FontWeight.BOLD, 15));
        title.setTextFill(Color.rgb(28, 28, 30));
        
        Button clearBtn = createAppleButton("Effacer", SECONDARY_COLOR, true, () -> logTerminal.clear());
        clearBtn.setPrefWidth(80);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        logHeader.getChildren().addAll(title, spacer, clearBtn);
        
        logTerminal = new TextArea();
        logTerminal.setEditable(false);
        logTerminal.setWrapText(true);
        logTerminal.setStyle(
            "-fx-control-inner-background: white;" +
            "-fx-text-fill: #1c1c1e;" +
            "-fx-font-family: 'Consolas', 'Monaco', monospace;" +
            "-fx-font-size: 11px;" +
            "-fx-border-color: " + toHexString(BORDER_COLOR) + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 8;"
        );
        
        VBox.setVgrow(logTerminal, Priority.ALWAYS);
        panel.getChildren().addAll(logHeader, logTerminal);
        return panel;
    }
    
    /**
     * Bouton Apple-style avec animations fluides
     */
    private Button createAppleButton(String text, Color color, boolean outlined, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("System", FontWeight.NORMAL, 13));
        
        if (outlined) {
            btn.setStyle(String.format(
                "-fx-background-color: rgba(255,255,255,0.9);" +
                "-fx-text-fill: %s;" +
                "-fx-border-color: %s;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 16;" +
                "-fx-cursor: hand;",
                toHexString(color), toHexString(color)));
        } else {
            btn.setTextFill(Color.WHITE);
            btn.setStyle(String.format(
                "-fx-background-color: %s;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 16;" +
                "-fx-cursor: hand;",
                toHexString(color)));
        }
        
        // Animations Apple subtiles
        btn.setOnMouseEntered(e -> {
            Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.millis(150), new KeyValue(btn.opacityProperty(), 0.85))
            );
            fadeIn.play();
        });
        
        btn.setOnMouseExited(e -> {
            Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.millis(150), new KeyValue(btn.opacityProperty(), 1.0))
            );
            fadeOut.play();
        });
        
        btn.setOnAction(e -> action.run());
        return btn;
    }
    
    /**
     * Configuration responsive
     */
    private void setupResponsiveBehavior(Stage stage) {
        // Mise à jour du layout des salles lors du redimensionnement
        canvasWidth.addListener((obs, oldVal, newVal) -> updateRoomLayout());
        canvasHeight.addListener((obs, oldVal, newVal) -> updateRoomLayout());
        
        // Ajustement de la police selon la taille de la fenêtre
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() < 1000) {
                // Mode compact
                rightScrollPane.setPrefWidth(280);
            } else {
                // Mode normal
                rightScrollPane.setPrefWidth(350);
            }
        });
    }
    
    /**
     * Layout des salles responsive
     */
    private void initializeRoomLayout() {
        updateRoomLayout();
        logMessage("Layout responsive initialisé");
    }
    
    private void updateRoomLayout() {
        rooms.clear();
        
        double w = canvasWidth.get();
        double h = canvasHeight.get();
        
        // Salles proportionnelles à la taille du canvas
        double roomW = w * 0.12;
        double roomH = h * 0.16;
        
        rooms.add(new Room("Accueil", w * 0.05, h * 0.35, roomW * 1.2, roomH * 1.5, LIGHT_GRAY));
        rooms.add(new Room("Renaissance", w * 0.25, h * 0.2, roomW, roomH, Color.rgb(255, 248, 220)));
        rooms.add(new Room("Impressionnisme", w * 0.4, h * 0.2, roomW, roomH, Color.rgb(230, 255, 230)));
        rooms.add(new Room("Art Moderne", w * 0.55, h * 0.2, roomW, roomH, Color.rgb(240, 248, 255)));
        rooms.add(new Room("Sculpture", w * 0.25, h * 0.65, roomW, roomH, Color.rgb(255, 235, 235)));
        rooms.add(new Room("Contemporain", w * 0.4, h * 0.65, roomW, roomH, Color.rgb(240, 255, 255)));
        rooms.add(new Room("Repos", w * 0.55, h * 0.5, roomW * 0.8, roomH * 0.7, Color.rgb(248, 248, 248)));
        rooms.add(new Room("Sortie", w * 0.8, h * 0.35, roomW * 1.2, roomH * 1.5, LIGHT_GRAY));
        
        // Mise à jour des points d'entrée/sortie
        entrancePoint = new Point2D(w * 0.1, h * 0.5);
        exitPoint = new Point2D(w * 0.9, h * 0.5);
    }
    
    /**
     * Animation fluide 60 FPS optimisée
     */
    private void startFluidAnimation() {
        renderLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_666_666) { // 60 FPS
                    time += 0.016;
                    updateSimulation();
                    renderAppleStyleSimulation();
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
        for (Agent agent : agents.values()) {
            agent.update();
        }
        
        for (TourGroup group : tourGroups) {
            group.update();
        }
        
        Iterator<TourGroup> groupIterator = tourGroups.iterator();
        while (groupIterator.hasNext()) {
            TourGroup group = groupIterator.next();
            if (group.isCompleted()) {
                for (Agent tourist : group.tourists) {
                    agents.remove(tourist.id);
                }
                
                if (agents.containsKey("COORDINATEUR")) {
                    Agent coordinator = agents.get("COORDINATEUR");
                    group.guide.moveTo(coordinator.x, coordinator.y);
                    group.guide.status = AgentStatus.AVAILABLE;
                    logMessage("Guide " + group.guide.id + " revient au coordinateur");
                } else {
                    group.guide.moveTo(entrancePoint.x, entrancePoint.y);
                    group.guide.status = AgentStatus.AVAILABLE;
                }
                
                groupIterator.remove();
                logMessage("Groupe " + group.name + " terminé");
            }
        }
    }
    
    /**
     * Rendu Apple-style moderne et fluide
     */
    private void renderAppleStyleSimulation() {
        double w = canvasWidth.get();
        double h = canvasHeight.get();
        
        // Fond dégradé subtil
        gc.setFill(Color.rgb(252, 252, 253));
        gc.fillRect(0, 0, w, h);
        
        // Rendu des salles avec style Apple
        for (Room room : rooms) {
            renderAppleRoom(room);
        }
        
        // Couloirs épurés
        renderAppleCorridors();
        
        // Groupes avec animations fluides
        for (TourGroup group : tourGroups) {
            renderAppleTourGroup(group);
        }
        
        // Agents individuels
        for (Agent agent : agents.values()) {
            if (tourGroups.stream().noneMatch(g -> g.containsAgent(agent))) {
                renderAppleAgent(agent);
            }
        }
        
        // Points d'entrée/sortie modernes
        renderApplePoints();
        
        // Overlay minimaliste
        renderAppleOverlay();
    }
    
    /**
     * Salles Apple-style avec ombres douces
     */
    private void renderAppleRoom(Room room) {
        // Ombre portée douce
        gc.setFill(Color.rgb(0, 0, 0, 0.04));
        gc.fillRoundRect(room.x + 1, room.y + 1, room.width, room.height, 16, 16);
        
        // Fond de la salle
        gc.setFill(room.color);
        gc.fillRoundRect(room.x, room.y, room.width, room.height, 16, 16);
        
        // Bordure subtile
        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(0.5);
        gc.strokeRoundRect(room.x, room.y, room.width, room.height, 16, 16);
        
        // Texte Apple-style
        gc.setFill(Color.rgb(28, 28, 30));
        gc.setFont(Font.font("System", FontWeight.NORMAL, Math.max(10, room.width * 0.08)));
        gc.fillText(room.name, room.x + 8, room.y + 20);
    }
    
    /**
     * Couloirs Apple-style
     */
    private void renderAppleCorridors() {
        double w = canvasWidth.get();
        double h = canvasHeight.get();
        
        gc.setFill(Color.rgb(248, 249, 250, 0.8));
        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(0.5);
        
        // Couloir principal
        double corridorY = h * 0.45;
        double corridorHeight = h * 0.1;
        gc.fillRoundRect(w * 0.2, corridorY, w * 0.6, corridorHeight, 20, 20);
        gc.strokeRoundRect(w * 0.2, corridorY, w * 0.6, corridorHeight, 20, 20);
    }
    
    /**
     * Groupe Apple-style avec animations
     */
    private void renderAppleTourGroup(TourGroup group) {
        if (group.guide == null || group.tourists.isEmpty()) return;
        
        Agent guide = group.guide;
        double groupRadius = 25 + group.tourists.size() * 1.5;
        
        // Zone du groupe avec effet subtil
        gc.setStroke(Color.rgb(0, 122, 255, 0.3));
        gc.setLineWidth(1.5);
        gc.strokeOval(guide.x - groupRadius, guide.y - groupRadius, 
                     groupRadius * 2, groupRadius * 2);
        
        // Connexions Apple-style
        gc.setStroke(Color.rgb(0, 122, 255, 0.15));
        gc.setLineWidth(1);
        for (Agent tourist : group.tourists) {
            gc.strokeLine(guide.x, guide.y, tourist.x, tourist.y);
        }
        
        // Agents du groupe
        renderAppleAgent(guide);
        for (Agent tourist : group.tourists) {
            renderAppleAgent(tourist);
        }
        
        // Info groupe Apple-style
        renderAppleGroupInfo(group);
    }
    
    /**
     * Agent Apple-style avec design moderne
     */
    private void renderAppleAgent(Agent agent) {
        Color agentColor = getAppleAgentColor(agent.type);
        double size = getAgentSize(agent.type);
        
        // Ombre portée douce
        gc.setFill(Color.rgb(0, 0, 0, 0.08));
        gc.fillOval(agent.x - size/2 + 0.5, agent.y - size/2 + 0.5, size, size);
        
        // Corps de l'agent
        gc.setFill(agentColor);
        gc.fillOval(agent.x - size/2, agent.y - size/2, size, size);
        
        // Bordure Apple-style
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(agent.x - size/2, agent.y - size/2, size, size);
        
        // Indicateur de statut
        if (agent.status == AgentStatus.BUSY || agent.status == AgentStatus.IN_GROUP) {
            gc.setFill(SUCCESS_COLOR);
            gc.fillOval(agent.x + size/2 - 2, agent.y - size/2 + 1, 4, 4);
        }
        
        // Direction avec animation
        if (agent.hasDestination()) {
            double angle = Math.atan2(agent.targetY - agent.y, agent.targetX - agent.x);
            double arrowX = agent.x + Math.cos(angle) * (size/2 + 8);
            double arrowY = agent.y + Math.sin(angle) * (size/2 + 8);
            
            gc.setFill(Color.rgb(0, 122, 255, 0.7));
            gc.fillOval(arrowX - 3, arrowY - 3, 6, 6);
        }
        
        // Label discret
        gc.setFill(SECONDARY_COLOR);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 9));
        gc.fillText(agent.getShortName(), agent.x - 12, agent.y + size/2 + 12);
    }
    
    /**
     * Info groupe Apple-style avec card flottante
     */
    private void renderAppleGroupInfo(TourGroup group) {
        double infoX = group.guide.x + 30;
        double infoY = group.guide.y - 35;
        double cardWidth = 130;
        double cardHeight = 32;
        
        // Card flottante avec effet glassmorphism
        gc.setFill(Color.rgb(255, 255, 255, 0.95));
        gc.fillRoundRect(infoX, infoY, cardWidth, cardHeight, 8, 8);
        
        // Ombre douce
        gc.setFill(Color.rgb(0, 0, 0, 0.05));
        gc.fillRoundRect(infoX + 0.5, infoY + 0.5, cardWidth, cardHeight, 8, 8);
        
        // Bordure subtile
        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(0.5);
        gc.strokeRoundRect(infoX, infoY, cardWidth, cardHeight, 8, 8);
        
        // Texte Apple-style
        gc.setFill(Color.rgb(28, 28, 30));
        gc.setFont(Font.font("System", FontWeight.BOLD, 9));
        gc.fillText(group.name, infoX + 6, infoY + 12);
        
        gc.setFill(SECONDARY_COLOR);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 8));
        gc.fillText(group.tourists.size() + " visiteurs • " + group.currentLocation, infoX + 6, infoY + 24);
    }
    
    /**
     * Points d'entrée/sortie Apple-style
     */
    private void renderApplePoints() {
        // Entrée
        gc.setFill(SUCCESS_COLOR);
        gc.fillOval(entrancePoint.x - 10, entrancePoint.y - 10, 20, 20);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 8));
        gc.fillText("ENTRÉE", entrancePoint.x - 18, entrancePoint.y + 25);
        
        // Sortie
        gc.setFill(ERROR_COLOR);
        gc.fillOval(exitPoint.x - 10, exitPoint.y - 10, 20, 20);
        gc.setFill(Color.WHITE);
        gc.fillText("SORTIE", exitPoint.x - 16, exitPoint.y + 25);
    }
    
    /**
     * Overlay Apple-style minimaliste
     */
    private void renderAppleOverlay() {
        double w = canvasWidth.get();
        
        // Compteurs discrets
        gc.setFont(Font.font("System", FontWeight.NORMAL, 11));
        gc.setFill(Color.rgb(142, 142, 147, 0.8));
        
        int y = 20;
        gc.fillText("Temps: " + String.format("%.1fs", time), w - 100, y);
        y += 16;
        gc.fillText("FPS: 60", w - 100, y);
    }
    
    // Actions système optimisées
    
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
                        coordinator.setPosition(canvasWidth.get()/2, canvasHeight.get()/2);
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
        
        if (agents.containsKey("COORDINATEUR")) {
            Agent coordinator = agents.get("COORDINATEUR");
            guide.setPosition(coordinator.x + (Math.random() - 0.5) * 50, 
                            coordinator.y + (Math.random() - 0.5) * 50);
        } else {
            guide.setPosition(entrancePoint.x + Math.random() * 60, 
                            entrancePoint.y + Math.random() * 60);
        }
        
        agents.put(id, guide);
        SimpleLauncher.addGuide(id, spec);
        
        logMessage("Guide ajouté: " + id + " - " + spec);
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
        
        int counter = 1;
        while (agents.containsKey(id)) {
            id = name + "_" + nationality + "_" + String.format("%02d", counter++);
        }
        
        Agent tourist = new Agent(id, AgentType.TOURIST);
        tourist.nationality = nationality;
        tourist.status = AgentStatus.WAITING;
        tourist.setPosition(entrancePoint.x + Math.random() * 80, 
                          entrancePoint.y + Math.random() * 80);
        
        agents.put(id, tourist);
        SimpleLauncher.addTourist(id);
        
        logMessage("Touriste ajouté: " + id + " (" + nationality + ")");
        updateStatsDisplay();
        
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
                for (int i = 0; i < 3; i++) {
                    addGuide();
                }
                
                scheduler.scheduleAtFixedRate(() -> Platform.runLater(() -> {
                    if (agents.size() < 40) {
                        addTourist();
                        if (Math.random() < 0.2) {
                            addGuide();
                        }
                    }
                }), 2, 3, TimeUnit.SECONDS);
                
                scheduler.scheduleAtFixedRate(() -> Platform.runLater(this::formTourGroups), 
                                            5, 8, TimeUnit.SECONDS);
                break;
        }
    }
    
    private void resetSimulation() {
        scheduler.shutdownNow();
        scheduler = Executors.newScheduledThreadPool(3);
        
        agents.clear();
        tourGroups.clear();
        
        systemStatusLabel.setText("Simulation réinitialisée");
        systemStatusLabel.setTextFill(SECONDARY_COLOR);
        
        logMessage("Simulation réinitialisée");
        updateStatsDisplay();
        
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
            
            int groupSize = Math.min(3 + (int)(Math.random() * 4), waitingTourists.size());
            List<Agent> groupTourists = new ArrayList<>();
            
            for (int i = 0; i < groupSize; i++) {
                groupTourists.add(waitingTourists.remove(0));
            }
            
            String groupName = guide.specialization + "_Tour_" + String.format("%02d", tourGroups.size() + 1);
            TourGroup tourGroup = new TourGroup(groupName, guide, groupTourists);
            tourGroups.add(tourGroup);
            
            guide.status = AgentStatus.BUSY;
            for (Agent tourist : groupTourists) {
                tourist.status = AgentStatus.IN_GROUP;
            }
            
            logMessage("Groupe formé: " + groupName + " avec " + groupSize + " touristes");
        }
        
        updateStatsDisplay();
    }
    
    private void tryAssignTouristsToGroups() {
        List<Agent> waitingTourists = agents.values().stream()
            .filter(a -> a.type == AgentType.TOURIST)
            .filter(a -> a.status == AgentStatus.WAITING)
            .collect(java.util.stream.Collectors.toList());
        
        if (waitingTourists.isEmpty()) return;
        
        for (TourGroup group : tourGroups) {
            if (group.tourists.size() < 6 && !waitingTourists.isEmpty()) {
                Agent tourist = waitingTourists.remove(0);
                group.addTourist(tourist);
                tourist.status = AgentStatus.IN_GROUP;
                
                logMessage("Touriste " + tourist.id + " rejoint " + group.name);
            }
        }
        
        if (waitingTourists.size() >= 3) {
            formTourGroups();
        }
    }
    
    /**
     * Monitoring système
     */
    private void startSystemMonitoring() {
        scheduler.scheduleAtFixedRate(this::updateStatsDisplay, 1, 2, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::simulateGroupMovements, 5, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::updateSystemMetrics, 3, 5, TimeUnit.SECONDS);
    }
    
    private void updateStatsDisplay() {
        Platform.runLater(() -> {
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
            
            updateAgentDetails();
        });
    }
    
    private void updateAgentDetails() {
        agentStatsPanel.getChildren().clear();
        
        if (!tourGroups.isEmpty()) {
            Label groupsTitle = new Label("Groupes Actifs (" + tourGroups.size() + ")");
            groupsTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
            groupsTitle.setTextFill(Color.rgb(28, 28, 30));
            agentStatsPanel.getChildren().add(groupsTitle);
            
            for (TourGroup group : tourGroups) {
                agentStatsPanel.getChildren().add(createAppleGroupDetailCard(group));
            }
        }
        
        List<Agent> waitingAgents = agents.values().stream()
            .filter(a -> a.status == AgentStatus.AVAILABLE || a.status == AgentStatus.WAITING)
            .collect(java.util.stream.Collectors.toList());
        
        if (!waitingAgents.isEmpty()) {
            Label waitingTitle = new Label("En Attente (" + waitingAgents.size() + ")");
            waitingTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
            waitingTitle.setTextFill(SECONDARY_COLOR);
            agentStatsPanel.getChildren().add(waitingTitle);
            
            for (Agent agent : waitingAgents) {
                agentStatsPanel.getChildren().add(createAppleAgentDetailCard(agent));
            }
        }
    }
    
    private VBox createAppleGroupDetailCard(TourGroup group) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10));
        card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + toHexString(BORDER_COLOR) + ";" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 0.5;"
        );
        
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label groupName = new Label(group.name);
        groupName.setFont(Font.font("System", FontWeight.BOLD, 11));
        groupName.setTextFill(PRIMARY_COLOR);
        
        Label touristCount = new Label(group.tourists.size() + " visiteurs");
        touristCount.setFont(Font.font("System", FontWeight.NORMAL, 10));
        touristCount.setTextFill(SUCCESS_COLOR);
        
        header.getChildren().addAll(groupName, touristCount);
        
        Label guideInfo = new Label("Guide: " + group.guide.getShortName());
        guideInfo.setFont(Font.font("System", FontWeight.NORMAL, 10));
        guideInfo.setTextFill(SECONDARY_COLOR);
        
        Label locationInfo = new Label("📍 " + group.currentLocation + " • Œuvre " + (group.visitedArtworks + 1) + "/5");
        locationInfo.setFont(Font.font("System", FontWeight.NORMAL, 10));
        locationInfo.setTextFill(SECONDARY_COLOR);
        
        card.getChildren().addAll(header, guideInfo, locationInfo);
        return card;
    }
    
    private HBox createAppleAgentDetailCard(Agent agent) {
        HBox card = new HBox(8);
        card.setPadding(new Insets(6, 10, 6, 10));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color: " + toHexString(LIGHT_GRAY) + ";" +
            "-fx-background-radius: 8;"
        );
        
        javafx.scene.shape.Circle indicator = new javafx.scene.shape.Circle(3);
        indicator.setFill(getAppleAgentColor(agent.type));
        
        VBox info = new VBox(2);
        
        Label nameLabel = new Label(agent.getShortName());
        nameLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        nameLabel.setTextFill(Color.rgb(28, 28, 30));
        
        Label statusLabel = new Label(getStatusText(agent));
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 9));
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
                createAppleMetricRow("Temps", formatTime((long)time)),
                createAppleMetricRow("Mémoire", usedMemory + " MB"),
                createAppleMetricRow("Groupes", String.valueOf(tourGroups.size())),
                createAppleMetricRow("Taux activité", calculateOccupancyRate() + "%")
            );
        });
    }
    
    private HBox createAppleMetricRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("System", FontWeight.NORMAL, 10));
        labelText.setTextFill(SECONDARY_COLOR);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label valueText = new Label(value);
        valueText.setFont(Font.font("System", FontWeight.NORMAL, 10));
        valueText.setTextFill(Color.rgb(28, 28, 30));
        
        row.getChildren().addAll(labelText, spacer, valueText);
        return row;
    }
    
    private void simulateGroupMovements() {
        if (!jadeSystemRunning || tourGroups.isEmpty()) return;
        
        Platform.runLater(() -> {
            for (TourGroup group : tourGroups) {
                if (Math.random() < 0.6) {
                    moveGroupToNextLocation(group);
                }
            }
        });
    }
    
    private void moveGroupToNextLocation(TourGroup group) {
        if (group.visitedArtworks >= 5) {
            group.moveToDestination(exitPoint.x, exitPoint.y, "Sortie");
            logMessage("Groupe " + group.name + " termine sa visite");
            
            scheduler.schedule(() -> Platform.runLater(() -> {
                group.completed = true;
                logMessage("Groupe " + group.name + " quitte le musée");
            }), 8, TimeUnit.SECONDS);
            
            return;
        }
        
        Room targetRoom = rooms.get(2 + (int)(Math.random() * (rooms.size() - 4)));
        double targetX = targetRoom.x + targetRoom.width/2;
        double targetY = targetRoom.y + targetRoom.height/2;
        
        group.moveToDestination(targetX, targetY, targetRoom.name);
        group.visitedArtworks++;
        
        logMessage("Groupe " + group.name + " visite " + targetRoom.name + " (" + group.visitedArtworks + "/5)");
    }
    
    // Méthodes utilitaires Apple-style
    
    private Color getAppleAgentColor(AgentType type) {
        switch (type) {
            case COORDINATOR: return Color.rgb(175, 82, 222);
            case GUIDE: return PRIMARY_COLOR;
            case TOURIST: return SUCCESS_COLOR;
            default: return SECONDARY_COLOR;
        }
    }
    
    private double getAgentSize(AgentType type) {
        switch (type) {
            case COORDINATOR: return 16;
            case GUIDE: return 12;
            case TOURIST: return 8;
            default: return 10;
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
    
    /**
     * CSS Apple-style moderne
     */
    private String getAppleStyleCSS() {
        return """
        .root {
            -fx-font-family: 'System', 'Helvetica Neue', 'Arial';
        }
        .button {
            -fx-font-family: 'System';
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);
        }
        .button:hover {
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);
        }
        .label {
            -fx-font-family: 'System';
        }
        .text-area {
            -fx-font-family: 'Consolas', 'Monaco', monospace;
        }
        .scroll-pane {
            -fx-background-color: transparent;
            -fx-background: transparent;
        }
        .scroll-pane .viewport {
            -fx-background-color: transparent;
        }
        .scroll-pane .content {
            -fx-background-color: transparent;
        }
        .scroll-bar:vertical {
            -fx-background-color: transparent;
        }
        .scroll-bar:horizontal {
            -fx-background-color: transparent;
        }
        .increment-button, .decrement-button {
            -fx-background-color: transparent;
        }
        .scroll-bar:vertical .track {
            -fx-background-color: rgba(0,0,0,0.05);
            -fx-background-radius: 4;
        }
        .scroll-bar:vertical .thumb {
            -fx-background-color: rgba(0,0,0,0.2);
            -fx-background-radius: 4;
        }
        .separator {
            -fx-background-color: rgba(209, 209, 214, 0.5);
        }
        .progress-indicator {
            -fx-progress-color: #007AFF;
        }
        .split-pane {
            -fx-background-color: transparent;
        }
        .split-pane .split-pane-divider {
            -fx-background-color: rgba(209, 209, 214, 0.5);
            -fx-padding: 0 1 0 1;
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
        double speed = 1.2;
        
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
            return Math.abs(targetX - x) > 2 || Math.abs(targetY - y) > 2;
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
            
            moveToDestination(entrancePoint.x + 40, entrancePoint.y, "Accueil");
        }
        
        public void addTourist(Agent tourist) {
            tourists.add(tourist);
            moveToDestination(guide.targetX, guide.targetY, currentLocation);
        }
        
        public boolean containsAgent(Agent agent) {
            return guide.equals(agent) || tourists.contains(agent);
        }
        
        public void moveToDestination(double x, double y, String locationName) {
            currentLocation = locationName;
            
            guide.moveTo(x, y);
            
            for (int i = 0; i < tourists.size(); i++) {
                double angle = (i * 2 * Math.PI) / tourists.size();
                double radius = 20 + (i / 3) * 8;
                
                double touristX = x + Math.cos(angle) * radius;
                double touristY = y + Math.sin(angle) * radius;
                
                tourists.get(i).moveTo(touristX, touristY);
            }
        }
        
        public void update() {
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
        
        public static void shutdownSystem() {
            // Simulation de l'arrêt du système JADE
        }
        
        public static void addTourist(String name) {
            // Simulation de l'ajout d'un touriste dans JADE
        }
        
        public static void addGuide(String name, String specialization) {
            // Simulation de l'ajout d'un guide dans JADE
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}