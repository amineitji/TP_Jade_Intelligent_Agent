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
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import agents.tourist.TouristProfile;
import agents.tourist.Personality;
import agents.tourist.SatisfactionTracker;
import agents.tourist.BehaviorManager;
import agents.guide.GuideProfile;
import agents.guide.GroupHandler;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Interface de visualisation du système multi-agents du musée
 * Intégration cohérente avec les agents BDI (GuideAgent, TouristAgent)
 * VERSION CORRIGÉE: Communication fixée et gestion des salles améliorée
 */
public class MuseumVisualizationApp extends Application {

    // Configuration
    private static final double CANVAS_WIDTH = 1000;
    private static final double CANVAS_HEIGHT = 700;
    
    // Palette iOS Gaming
    private static final Color BG_COLOR = Color.rgb(22, 22, 24);
    private static final Color SURFACE_COLOR = Color.rgb(28, 28, 30);
    private static final Color CARD_COLOR = Color.rgb(44, 44, 46);
    private static final Color PRIMARY = Color.rgb(10, 132, 255);
    private static final Color SUCCESS = Color.rgb(48, 209, 88);
    private static final Color WARNING = Color.rgb(255, 159, 10);
    private static final Color DANGER = Color.rgb(255, 69, 58);
    private static final Color PURPLE = Color.rgb(191, 90, 242);
    private static final Color CYAN = Color.rgb(100, 210, 255);
    private static final Color PINK = Color.rgb(255, 55, 95);
    private static final Color TEXT_PRIMARY = Color.rgb(255, 255, 255);
    private static final Color TEXT_SECONDARY = Color.rgb(152, 152, 157);
    
    // Types d'agents selon l'architecture BDI
    public enum AgentType {
        COORDINATOR, GUIDE, TOURIST
    }
    
    // États des visites
    public enum TourState {
        AVAILABLE,    // Guide disponible
        TOURING,      // En visite
        WAITING,      // Touriste en attente
        FINISHED      // Visite terminée
    }
    
    // Composants principaux
    private Canvas canvas;
    private GraphicsContext gc;
    private BorderPane root;
    
    // Panels latéraux
    private VBox leftPanel;
    private VBox rightPanel;
    private VBox agentListPanel;
    private VBox metricsPanel;
    private VBox roomsPanel;
    private VBox eventsPanel;
    
    // Animation
    private AnimationTimer gameLoop;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private double time = 0;
    private double deltaTime = 0.016;
    
    // Système Multi-Agents BDI
    private Map<String, VisualAgent> agents = new HashMap<>();
    private List<VisualStarGroup> starGroups = new ArrayList<>();
    private List<VisualRoom> rooms = new ArrayList<>();
    private List<VisualNetworkLink> networkLinks = new ArrayList<>();
    private ConcurrentLinkedQueue<VisualGameEvent> eventQueue = new ConcurrentLinkedQueue<>();
    
    // État du système
    private boolean systemRunning = false;
    private VisualAgent selectedAgent = null;
    private VisualStarGroup selectedGroup = null;
    
    // Gestion des visites cycliques
    private Queue<List<VisualAgent>> waitingTouristGroups = new LinkedList<>();
    private double lastGroupArrival = 0;
    private int groupCounter = 0;
    
    // CORRECTION: Configuration du système
    private int groupSizeMin = 3;
    private int groupSizeMax = 7;
    private double groupArrivalInterval = 15.0;
    private int numberOfGuides = 3;
    
    // CORRECTION: Gestion des salles occupées
    private Map<String, String> roomOccupancy = new HashMap<>(); // Salle -> ID du groupe
    private Set<String> waitingForRoom = new HashSet<>(); // Groupes en attente d'une salle
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Museum AI System - Visualisation Multi-Agents BDI");
        primaryStage.setWidth(1600);
        primaryStage.setHeight(900);
        
        initializeUI();
        initializeGameWorld();
        startGameLoop();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("data:text/css," + getIOSGamingCSS());
        
        canvas.setOnMouseClicked(e -> handleCanvasClick(e.getX(), e.getY()));
        canvas.setOnMouseMoved(e -> handleCanvasHover(e.getX(), e.getY()));
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(e -> shutdown());
    }
    
    private void initializeUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #161618;");
        
        HBox header = createIOSHeader();
        root.setTop(header);
        
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle(
            "-fx-background-color: #1c1c1e;" +
            "-fx-border-color: #3a3a3c;" +
            "-fx-border-width: 1;"
        );
        
        leftPanel = createScenariosPanel();
        rightPanel = createMetricsPanel();
        
        HBox mainContent = new HBox();
        mainContent.getChildren().addAll(leftPanel, canvasContainer, rightPanel);
        HBox.setHgrow(canvasContainer, Priority.ALWAYS);
        
        root.setCenter(mainContent);
    }
    
    private HBox createIOSHeader() {
        HBox header = new HBox(30);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: rgba(28, 28, 30, 0.95);" +
            "-fx-border-color: #3a3a3c;" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        StackPane logo = new StackPane();
        Circle outer = new Circle(18, Color.TRANSPARENT);
        outer.setStroke(PRIMARY);
        outer.setStrokeWidth(2);
        Circle inner = new Circle(12, PRIMARY);
        logo.getChildren().addAll(outer, inner);
        
        RotateTransition rotate = new RotateTransition(Duration.seconds(4), logo);
        rotate.setByAngle(360);
        rotate.setCycleCount(Timeline.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();
        
        VBox titleBox = new VBox(2);
        Text title = new Text("MUSEUM BDI AGENTS");
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
        title.setFill(TEXT_PRIMARY);
        
        Text subtitle = new Text("Multi-Agent Simulation (Beliefs-Desires-Intentions)");
        subtitle.setFont(Font.font("SF Pro Text", 12));
        subtitle.setFill(TEXT_SECONDARY);
        
        titleBox.getChildren().addAll(title, subtitle);
        
        // CORRECTION: Ajouter menu de configuration
        VBox configMenu = createConfigurationMenu();
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button startBtn = createIOSButton("START", SUCCESS, () -> startSystem());
        Button pauseBtn = createIOSButton("PAUSE", WARNING, () -> pauseSystem());
        Button resetBtn = createIOSButton("RESET", DANGER, () -> resetSystem());
        
        header.getChildren().addAll(logo, titleBox, configMenu, spacer, startBtn, pauseBtn, resetBtn);
        return header;
    }
    
    private VBox createConfigurationMenu() {
        VBox configBox = new VBox(5);
        configBox.setAlignment(Pos.CENTER_LEFT);
        
        Label configTitle = new Label("CONFIGURATION");
        configTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 10));
        configTitle.setTextFill(TEXT_SECONDARY);
        
        // CORRECTION: Tout sur une seule ligne horizontale
        HBox allConfigBox = new HBox(15);
        allConfigBox.setAlignment(Pos.CENTER_LEFT);
        
        // Taille des groupes
        HBox groupSizeBox = new HBox(5);
        groupSizeBox.setAlignment(Pos.CENTER_LEFT);
        
        Label groupSizeLabel = new Label("Taille:");
        groupSizeLabel.setFont(Font.font("SF Pro Text", 9));
        groupSizeLabel.setTextFill(TEXT_PRIMARY);
        
        Spinner<Integer> maxSizeSpinner = new Spinner<>(3, 12, groupSizeMax);
        maxSizeSpinner.setPrefWidth(50);
        maxSizeSpinner.setStyle("-fx-background-color: #2c2c2e; -fx-text-fill: white;");
        
        Spinner<Integer> minSizeSpinner = new Spinner<>(2, 8, groupSizeMin);
        minSizeSpinner.setPrefWidth(50);
        minSizeSpinner.setStyle("-fx-background-color: #2c2c2e; -fx-text-fill: white;");
        minSizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            groupSizeMin = newVal;
            if (groupSizeMin > groupSizeMax) {
                groupSizeMax = groupSizeMin + 2;
                maxSizeSpinner.getValueFactory().setValue(groupSizeMax);
            }
        });
        
        Label toLabel = new Label("à");
        toLabel.setFont(Font.font("SF Pro Text", 8));
        toLabel.setTextFill(TEXT_SECONDARY);
        
        maxSizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            groupSizeMax = newVal;
            if (groupSizeMax < groupSizeMin) {
                groupSizeMin = groupSizeMax - 2;
                minSizeSpinner.getValueFactory().setValue(Math.max(2, groupSizeMin));
            }
        });
        
        groupSizeBox.getChildren().addAll(groupSizeLabel, minSizeSpinner, toLabel, maxSizeSpinner);
        
        // Fréquence d'arrivée
        HBox frequencyBox = new HBox(5);
        frequencyBox.setAlignment(Pos.CENTER_LEFT);
        
        Label frequencyLabel = new Label("Fréq(s):");
        frequencyLabel.setFont(Font.font("SF Pro Text", 9));
        frequencyLabel.setTextFill(TEXT_PRIMARY);
        
        Spinner<Double> frequencySpinner = new Spinner<>(5.0, 60.0, groupArrivalInterval, 5.0);
        frequencySpinner.setPrefWidth(60);
        frequencySpinner.setStyle("-fx-background-color: #2c2c2e; -fx-text-fill: white;");
        frequencySpinner.valueProperty().addListener((obs, oldVal, newVal) -> groupArrivalInterval = newVal);
        
        frequencyBox.getChildren().addAll(frequencyLabel, frequencySpinner);
        
        // Nombre de guides
        HBox guidesBox = new HBox(5);
        guidesBox.setAlignment(Pos.CENTER_LEFT);
        
        Label guidesLabel = new Label("Guides:");
        guidesLabel.setFont(Font.font("SF Pro Text", 9));
        guidesLabel.setTextFill(TEXT_PRIMARY);
        
        Spinner<Integer> guidesSpinner = new Spinner<>(1, 6, numberOfGuides);
        guidesSpinner.setPrefWidth(50);
        guidesSpinner.setStyle("-fx-background-color: #2c2c2e; -fx-text-fill: white;");
        guidesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> numberOfGuides = newVal);
        
        guidesBox.getChildren().addAll(guidesLabel, guidesSpinner);
        
        // Assembler tout sur une ligne
        allConfigBox.getChildren().addAll(groupSizeBox, frequencyBox, guidesBox);
        configBox.getChildren().addAll(configTitle, allConfigBox);
        return configBox;
    }
    
    private VBox createScenariosPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(220);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #1c1c1e;");
        
        Label title = new Label("SCÉNARIOS BDI");
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 14));
        title.setTextFill(TEXT_PRIMARY);
        
        VBox scenarioButtons = new VBox(8);
        scenarioButtons.setPadding(new Insets(10, 0, 0, 0));
        
        scenarioButtons.getChildren().addAll(
            createScenarioButton("Formation Groupe", "👥", PRIMARY, () -> runScenario("formation")),
            createScenarioButton("Négociation Guide", "⚖️", WARNING, () -> runScenario("negotiation")),
            createScenarioButton("Urgence Touriste", "🚨", DANGER, () -> runScenario("emergency")),
            createScenarioButton("Groupe Scolaire", "🎓", SUCCESS, () -> runScenario("school")),
            createScenarioButton("Multi-Groupes", "🌐", PURPLE, () -> runScenario("multi")),
            createScenarioButton("Adaptation BDI", "🔄", CYAN, () -> runScenario("adaptation")),
            createScenarioButton("Test Cohésion", "⚡", PINK, () -> runScenario("cohesion"))
        );
        
        panel.getChildren().addAll(title, new Separator(), scenarioButtons);
        return panel;
    }
    
    private VBox createMetricsPanel() {
        VBox panel = new VBox(15);
        panel.setPrefWidth(320);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #1c1c1e;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1c1c1e;");
        
        VBox content = new VBox(15);
        
        metricsPanel = createBDIMetricsCard();
        agentListPanel = createAgentListCard();
        roomsPanel = createRoomsCard();
        eventsPanel = createEventsCard();
        
        content.getChildren().addAll(metricsPanel, agentListPanel, roomsPanel, eventsPanel);
        scrollPane.setContent(content);
        
        panel.getChildren().add(scrollPane);
        return panel;
    }
    
    private VBox createBDIMetricsCard() {
        VBox card = createCard("MÉTRIQUES BDI", PRIMARY);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        addMetric(grid, 0, "Agents BDI", "0", SUCCESS);
        addMetric(grid, 1, "Groupes", "0", WARNING);
        addMetric(grid, 2, "Cohésion", "0%", PRIMARY);
        addMetric(grid, 3, "Satisfaction", "0%", SUCCESS);
        addMetric(grid, 4, "Efficacité", "0%", CYAN);
        addMetric(grid, 5, "Messages/s", "0", PURPLE);
        
        card.getChildren().add(grid);
        return card;
    }
    
    private VBox createAgentListCard() {
        VBox card = createCard("AGENTS ACTIFS", SUCCESS);
        
        ListView<HBox> agentList = new ListView<>();
        agentList.setPrefHeight(200);
        agentList.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-control-inner-background: #2c2c2e;"
        );
        agentList.setId("agent_list");
        
        card.getChildren().add(agentList);
        return card;
    }
    
    private VBox createRoomsCard() {
        VBox card = createCard("SALLES MUSÉE", WARNING);
        
        VBox roomsList = new VBox(5);
        roomsList.setPadding(new Insets(10));
        roomsList.setId("rooms_list");
        
        card.getChildren().add(roomsList);
        return card;
    }
    
    private VBox createEventsCard() {
        VBox card = createCard("ÉVÉNEMENTS BDI", DANGER);
        
        VBox eventsList = new VBox(3);
        eventsList.setPadding(new Insets(10));
        eventsList.setId("events_list");
        eventsList.setPrefHeight(150);
        
        card.getChildren().add(eventsList);
        return card;
    }
    
    private VBox createCard(String title, Color accentColor) {
        VBox card = new VBox();
        card.setStyle(
            "-fx-background-color: #2c2c2e;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + toHex(accentColor.deriveColor(0, 1, 0.5, 0.3)) + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;"
        );
        
        HBox header = new HBox(10);
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setAlignment(Pos.CENTER_LEFT);
        
        Circle dot = new Circle(4, accentColor);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 12));
        titleLabel.setTextFill(TEXT_PRIMARY);
        
        header.getChildren().addAll(dot, titleLabel);
        card.getChildren().add(header);
        
        return card;
    }
    
    private Button createIOSButton(String text, Color color, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 12));
        btn.setTextFill(TEXT_PRIMARY);
        btn.setStyle(
            "-fx-background-color: " + toHex(color) + ";" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 16;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> 
            btn.setStyle(btn.getStyle() + "-fx-background-color: " + 
                        toHex(color.brighter()) + ";"));
        btn.setOnMouseExited(e -> 
            btn.setStyle(btn.getStyle().replace(toHex(color.brighter()), toHex(color))));
        
        btn.setOnAction(e -> action.run());
        return btn;
    }
    
    private Button createScenarioButton(String text, String emoji, Color color, Runnable action) {
        Button btn = new Button(emoji + "  " + text);
        btn.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 11));
        btn.setTextFill(TEXT_PRIMARY);
        btn.setPrefWidth(190);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(
            "-fx-background-color: " + toHex(color.deriveColor(0, 1, 0.3, 0.3)) + ";" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 15;" +
            "-fx-border-color: " + toHex(color.deriveColor(0, 1, 0.5, 0.5)) + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnAction(e -> {
            flashButton(btn, color);
            action.run();
        });
        
        return btn;
    }
    
    private void flashButton(Button btn, Color color) {
        Timeline flash = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(btn.styleProperty(), btn.getStyle())),
            new KeyFrame(Duration.millis(100), 
                new KeyValue(btn.styleProperty(), 
                    btn.getStyle().replace(
                        toHex(color.deriveColor(0, 1, 0.3, 0.3)),
                        toHex(color)))),
            new KeyFrame(Duration.millis(200), 
                new KeyValue(btn.styleProperty(), btn.getStyle()))
        );
        flash.play();
    }
    
    private void initializeGameWorld() {
        rooms.clear();
        roomOccupancy.clear(); // CORRECTION: Nettoyer l'occupation des salles
        
        rooms.add(new VisualRoom("Accueil", 80, 350, 100, 80, PRIMARY.deriveColor(0, 0.5, 1, 0.3)));
        rooms.add(new VisualRoom("Renaissance", 250, 120, 100, 80, SUCCESS.deriveColor(0, 0.5, 1, 0.3)));
        rooms.add(new VisualRoom("Art Moderne", 450, 120, 100, 80, WARNING.deriveColor(0, 0.5, 1, 0.3)));
        rooms.add(new VisualRoom("Impressionnisme", 650, 120, 100, 80, PURPLE.deriveColor(0, 0.5, 1, 0.3)));
        rooms.add(new VisualRoom("Sculptures", 650, 350, 100, 80, CYAN.deriveColor(0, 0.5, 1, 0.3)));
        rooms.add(new VisualRoom("Salle Repos", 450, 350, 100, 80, Color.GRAY.deriveColor(0, 0.5, 1, 0.3)));
        rooms.add(new VisualRoom("Sortie", 250, 550, 100, 80, DANGER.deriveColor(0, 0.5, 1, 0.3)));
        
        // Définir les parcours possibles entre salles
        defineRoomConnections();
    }
    
    private void defineRoomConnections() {
        // Chemin typique de visite : Accueil → Renaissance → Art Moderne → Impressionnisme → Sculptures → Repos → Sortie
        VisualRoom accueil = findRoom("Accueil");
        VisualRoom renaissance = findRoom("Renaissance");
        VisualRoom artModerne = findRoom("Art Moderne");
        VisualRoom impressionnisme = findRoom("Impressionnisme");
        VisualRoom sculptures = findRoom("Sculptures");
        VisualRoom repos = findRoom("Salle Repos");
        VisualRoom sortie = findRoom("Sortie");
        
        if (accueil != null) accueil.nextRooms.addAll(List.of(renaissance, artModerne));
        if (renaissance != null) renaissance.nextRooms.addAll(List.of(artModerne, impressionnisme));
        if (artModerne != null) artModerne.nextRooms.addAll(List.of(impressionnisme, sculptures));
        if (impressionnisme != null) impressionnisme.nextRooms.addAll(List.of(sculptures, repos));
        if (sculptures != null) sculptures.nextRooms.addAll(List.of(repos, sortie));
        if (repos != null) repos.nextRooms.add(sortie);
    }
    
    private VisualRoom findRoom(String name) {
        return rooms.stream().filter(r -> r.name.equals(name)).findFirst().orElse(null);
    }
    
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            long lastUpdate = System.nanoTime();
            
            @Override
            public void handle(long now) {
                double elapsedTime = (now - lastUpdate) / 1_000_000_000.0;
                
                if (elapsedTime >= deltaTime) {
                    time += deltaTime;
                    update();
                    render();
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }
    
    private void update() {
        if (!systemRunning) return;
        
        // CORRECTION: Vérifier si l'accueil peut accueillir de nouveaux groupes
        boolean canGenerateNewGroup = canAccommodateNewGroup();
        
        // Génération conditionnelle de groupes de touristes
        if (canGenerateNewGroup && time - lastGroupArrival > groupArrivalInterval + (Math.random() * 10 - 5)) {
            generateNewTouristGroup();
            lastGroupArrival = time;
        }
        
        // Assignation des guides aux groupes en attente
        assignGuidesToWaitingGroups();
        
        // Mise à jour des agents BDI
        for (VisualAgent agent : agents.values()) {
            agent.update(deltaTime);
        }
        
        // Mise à jour des groupes avec cohésion BDI
        for (VisualStarGroup group : starGroups) {
            group.updateBDI(deltaTime);
        }
        
        // CORRECTION: Nettoyer les liens expirés et réduire leur création
        networkLinks.removeIf(link -> link.isDead());
        for (VisualNetworkLink link : networkLinks) {
            link.update(deltaTime);
        }
        
        processEvents();
        
        if ((int)(time * 2) % 2 == 0) {
            updateMetrics();
        }
    }
    
    /**
     * CORRECTION: Vérifier si l'accueil peut accueillir un nouveau groupe
     */
    private boolean canAccommodateNewGroup() {
        int maxWaitingGroups = Math.max(1, numberOfGuides);
        int currentWaitingTourists = waitingTouristGroups.stream()
            .mapToInt(List::size)
            .sum();
        
        // Conditions pour autoriser un nouveau groupe :
        // 1. Pas trop de groupes en attente
        // 2. Pas trop de touristes individuels en attente
        // 3. Au moins un guide disponible ou qui va bientôt se libérer
        boolean hasSpaceForGroups = waitingTouristGroups.size() < maxWaitingGroups;
        boolean hasSpaceForTourists = currentWaitingTourists < numberOfGuides * 6; // Max 6 touristes par guide
        boolean hasAvailableGuide = agents.values().stream()
            .anyMatch(agent -> agent.type == AgentType.GUIDE && 
                      (agent.tourState == TourState.AVAILABLE || agent.tourState == TourState.WAITING));
        
        return hasSpaceForGroups && hasSpaceForTourists && hasAvailableGuide;
    }
    
    private void generateNewTouristGroup() {
        // CORRECTION: Régulation intelligente - vérifier la capacité d'accueil
        int maxWaitingGroups = Math.max(1, numberOfGuides); // Au maximum 1 groupe par guide
        int currentWaitingTourists = waitingTouristGroups.stream()
            .mapToInt(List::size)
            .sum();
        
        // Ne pas générer si trop de touristes en attente ou trop de groupes
        if (waitingTouristGroups.size() >= maxWaitingGroups || currentWaitingTourists >= numberOfGuides * 8) {
            addEvent("Accueil saturé - Arrivées suspendues temporairement", WARNING);
            return;
        }
        
        groupCounter++;
        List<VisualAgent> newGroup = new ArrayList<>();
        int groupSize = groupSizeMin + (int)(Math.random() * (groupSizeMax - groupSizeMin + 1));
        
        VisualRoom accueil = findRoom("Accueil");
        if (accueil == null) return;
        
        // CORRECTION: Calcul de position pour éviter les superpositions
        int currentWaitingGroupsCount = waitingTouristGroups.size();
        double baseX = accueil.x + 15; // Marge depuis le bord gauche
        double baseY = accueil.y + 15; // Marge depuis le haut
        
        // Espacement intelligent selon le nombre de groupes déjà présents
        double groupSpacingY = 70; // Espacement vertical entre groupes
        double currentGroupY = baseY + currentWaitingGroupsCount * groupSpacingY;
        
        // Si on dépasse la salle d'accueil, utiliser une deuxième colonne
        if (currentGroupY + 50 > accueil.y + accueil.height) {
            baseX += 120; // Deuxième colonne
            currentGroupY = baseY + (currentWaitingGroupsCount % 2) * groupSpacingY;
        }
        
        // Créer les touristes du nouveau groupe avec formation anti-superposition
        for (int i = 0; i < groupSize; i++) {
            VisualAgent tourist = new VisualAgent("TOURIST-G" + groupCounter + "-" + (i + 1), AgentType.TOURIST);
            
            // Formation en rectangle compact : 3 personnes par ligne
            int row = i / 3;
            int col = i % 3;
            double memberX = baseX + col * 25;
            double memberY = currentGroupY + row * 22;
            
            // Vérification des limites de la salle d'accueil
            if (memberX > accueil.x + accueil.width - 10) {
                memberX = accueil.x + accueil.width - 10;
            }
            if (memberY > accueil.y + accueil.height - 10) {
                memberY = accueil.y + accueil.height - 10;
            }
            
            tourist.setPosition(memberX, memberY);
            
            // Profil BDI diversifié
            tourist.profile = new TouristProfile("Tourist G" + groupCounter + "-" + (i + 1));
            tourist.color = SUCCESS.interpolate(WARNING, tourist.profile.getSatisfaction());
            tourist.satisfaction = tourist.profile.getSatisfaction();
            tourist.fatigue = tourist.profile.getFatigue();
            tourist.inGroup = false;
            tourist.tourState = TourState.WAITING;
            
            agents.put(tourist.id, tourist);
            newGroup.add(tourist);
        }
        
        waitingTouristGroups.offer(newGroup);
        addEvent("Nouveau groupe de " + groupSize + " touristes arrive (Total: " + 
                (currentWaitingTourists + groupSize) + " en attente)", SUCCESS);
    }
    
    private void assignGuidesToWaitingGroups() {
        if (waitingTouristGroups.isEmpty()) return;
        
        // Trouver un guide disponible
        VisualAgent availableGuide = findAvailableGuide();
        if (availableGuide == null) return;
        
        // Assigner le guide au premier groupe en attente
        List<VisualAgent> group = waitingTouristGroups.poll();
        if (group == null) return;
        
        startGuidedTour(availableGuide, group);
    }
    
    private VisualAgent findAvailableGuide() {
        for (VisualAgent agent : agents.values()) {
            if (agent.type == AgentType.GUIDE && agent.tourState == TourState.AVAILABLE) {
                return agent;
            }
        }
        return null;
    }
    
    private void startGuidedTour(VisualAgent guide, List<VisualAgent> tourists) {
        // CORRECTION: Le guide doit aller chercher les touristes à l'accueil
        VisualRoom accueil = findRoom("Accueil");
        if (accueil == null) return;
        
        // Créer le groupe étoilé pour la visite
        VisualStarGroup tourGroup = new VisualStarGroup(guide.id, "Visite " + groupCounter);
        tourGroup.color = guide.color;
        tourGroup.cohesion = 0.7;
        
        // CORRECTION: Si le guide n'est pas à l'accueil, l'y amener d'abord
        boolean guideAtWelcome = Math.abs(guide.x - (accueil.x + accueil.width/2)) < 50 && 
                                Math.abs(guide.y - (accueil.y + accueil.height/2)) < 50;
        
        if (!guideAtWelcome) {
            // Le guide va d'abord à l'accueil chercher les touristes
            guide.tourState = TourState.WAITING; // En transit vers accueil
            
            Timeline goToWelcome = new Timeline(
                new KeyFrame(Duration.seconds(2),
                    new KeyValue(guide.xProperty, accueil.x + accueil.width/2, Interpolator.EASE_BOTH),
                    new KeyValue(guide.yProperty, accueil.y + accueil.height/2, Interpolator.EASE_BOTH))
            );
            
            goToWelcome.setOnFinished(e -> {
                // Une fois arrivé à l'accueil, former le groupe
                formTourGroup(guide, tourists, tourGroup);
            });
            
            goToWelcome.play();
            addEvent("Guide " + guide.name + " va chercher un groupe à l'accueil", PRIMARY);
        } else {
            // Le guide est déjà à l'accueil, former le groupe directement
            formTourGroup(guide, tourists, tourGroup);
        }
    }
    
    private void formTourGroup(VisualAgent guide, List<VisualAgent> tourists, VisualStarGroup tourGroup) {
        // Assigner les touristes au groupe
        for (int i = 0; i < tourists.size(); i++) {
            VisualAgent tourist = tourists.get(i);
            tourist.inGroup = true;
            tourist.tourState = TourState.TOURING;
            tourist.assignedGuideId = guide.id;
            
            double angle = (Math.PI * 2 * i) / tourists.size();
            double radius = 60;
            
            VisualGroupMember member = new VisualGroupMember(tourist.id, angle, radius);
            member.x = tourist.x;
            member.y = tourist.y;
            member.beliefStrength = 0.7 + Math.random() * 0.3;
            member.intentionLevel = 0.6 + Math.random() * 0.4;
            tourGroup.members.add(member);
        }
        
        // Configurer le guide pour la visite
        guide.tourState = TourState.TOURING;
        guide.currentTourGroup = tourGroup;
        guide.tourProgress = 0;
        guide.currentRoomIndex = 0;
        guide.timeInCurrentRoom = 0;
        guide.visitPath = generateTourPath();
        
        tourGroup.guide = guide;
        tourGroup.updateBDIMetrics();
        starGroups.add(tourGroup);
        
        addEvent("Guide " + guide.name + " prend en charge " + tourists.size() + " touristes", SUCCESS);
    }
    
    private List<String> generateTourPath() {
        // Parcours standard mais adaptable
        return List.of("Accueil", "Renaissance", "Art Moderne", "Impressionnisme", "Sculptures", "Salle Repos", "Sortie");
    }
    
    private void render() {
        LinearGradient bg = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(28, 28, 30)),
            new Stop(1, Color.rgb(22, 22, 24))
        );
        gc.setFill(bg);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        renderAnimatedGrid();
        
        for (VisualRoom room : rooms) {
            renderMuseumRoom(room);
        }
        
        for (VisualNetworkLink link : networkLinks) {
            renderBDINetworkLink(link);
        }
        
        for (VisualStarGroup group : starGroups) {
            renderBDIStarGroup(group);
        }
        
        for (VisualAgent agent : agents.values()) {
            if (!agent.inGroup) {
                renderBDIAgent(agent);
            }
        }
        
        renderGamingHUD();
    }
    
    private void renderAnimatedGrid() {
        double gridSize = 40;
        double animOffset = (time * 20) % gridSize;
        
        gc.setStroke(Color.rgb(255, 255, 255, 0.02));
        gc.setLineWidth(1);
        
        for (double x = -animOffset; x < CANVAS_WIDTH; x += gridSize) {
            gc.strokeLine(x, 0, x, CANVAS_HEIGHT);
        }
        
        for (double y = -animOffset; y < CANVAS_HEIGHT; y += gridSize) {
            gc.strokeLine(0, y, CANVAS_WIDTH, y);
        }
        
        gc.setFill(PRIMARY.deriveColor(0, 1, 1, 0.1));
        for (double x = gridSize - animOffset; x < CANVAS_WIDTH; x += gridSize) {
            for (double y = gridSize - animOffset; y < CANVAS_HEIGHT; y += gridSize) {
                double pulse = Math.sin(time * 2 + x * 0.01 + y * 0.01) * 0.5 + 0.5;
                gc.setGlobalAlpha(pulse * 0.3);
                gc.fillOval(x - 2, y - 2, 4, 4);
            }
        }
        gc.setGlobalAlpha(1);
    }
    
    private void renderMuseumRoom(VisualRoom room) {
        // CORRECTION: Vérifier l'occupation réelle plutôt que juste le flag
        boolean isOccupied = roomOccupancy.containsKey(room.name);
        room.occupied = isOccupied;
        
        if (room.occupied) {
            gc.setFill(room.color.brighter());
            gc.setGlobalAlpha(0.3);
            for (int i = 3; i > 0; i--) {
                gc.fillRoundRect(
                    room.x - i * 2, 
                    room.y - i * 2, 
                    room.width + i * 4, 
                    room.height + i * 4, 
                    15, 15
                );
            }
            gc.setGlobalAlpha(1);
        }
        
        gc.setFill(room.occupied ? room.color.brighter() : room.color);
        gc.fillRoundRect(room.x, room.y, room.width, room.height, 10, 10);
        
        gc.setStroke(room.occupied ? room.color.brighter().brighter() : room.color.brighter());
        gc.setLineWidth(2);
        gc.strokeRoundRect(room.x, room.y, room.width, room.height, 10, 10);
        
        gc.setFill(TEXT_PRIMARY);
        gc.setFont(Font.font("SF Mono", FontWeight.BOLD, 11));
        double textX = room.x + (room.width - room.name.length() * 6) / 2;
        gc.fillText(room.name.toUpperCase(), textX, room.y + room.height / 2);
        
        // CORRECTION: Afficher quel groupe occupe la salle
        if (room.occupied) {
            gc.setFill(DANGER);
            gc.fillOval(room.x + room.width - 15, room.y + 5, 8, 8);
            
            // Afficher l'ID du groupe occupant
            String occupantId = roomOccupancy.get(room.name);
            if (occupantId != null) {
                gc.setFill(TEXT_SECONDARY);
                gc.setFont(Font.font("SF Mono", 8));
                gc.fillText(occupantId, room.x + 5, room.y + room.height - 5);
            }
        }
    }
    
    private void renderBDIStarGroup(VisualStarGroup group) {
        if (group.guide == null) return;
        
        VisualAgent guide = agents.get(group.guideId);
        if (guide == null) return;
        
        double pulse = 1 + Math.sin(time * 3) * 0.1;
        
        // Aura BDI du groupe avec indicateur de cohésion
        RadialGradient groupAura = new RadialGradient(
            0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, group.color.deriveColor(0, 1, 1, group.cohesion * 0.3)),
            new Stop(0.5, group.color.deriveColor(0, 1, 1, group.cohesion * 0.1)),
            new Stop(1, Color.TRANSPARENT)
        );
        
        gc.setFill(groupAura);
        double auraSize = group.radius * 2.5 * pulse * group.cohesion;
        gc.fillOval(guide.x - auraSize/2, guide.y - auraSize/2, auraSize, auraSize);
        
        // Liens BDI animés avec intensité selon la satisfaction
        gc.setStroke(group.color.deriveColor(0, 1, 1, 0.4 + group.avgSatisfaction * 0.3));
        gc.setLineWidth(1.5);
        
        for (VisualGroupMember member : group.members) {
            gc.setLineDashes(5, 10);
            gc.strokeLine(guide.x, guide.y, member.x, member.y);
            
            // Particule BDI qui voyage selon l'état d'intention
            double progress = (time * (0.5 + member.intentionLevel * 0.5) + member.offset) % 1;
            double particleX = guide.x + (member.x - guide.x) * progress;
            double particleY = guide.y + (member.y - guide.y) * progress;
            
            gc.setFill(group.color.brighter().deriveColor(0, 1, 1, member.beliefStrength));
            gc.fillOval(particleX - 3, particleY - 3, 6, 6);
        }
        gc.setLineDashes();
        
        renderBDIAgent(guide);
        
        for (VisualGroupMember member : group.members) {
            VisualAgent tourist = agents.get(member.agentId);
            if (tourist != null) {
                // Position influencée par l'état BDI
                tourist.x = member.x + Math.sin(time * 2 + member.angle) * (3 * member.intentionLevel);
                tourist.y = member.y + Math.cos(time * 2 + member.angle) * (3 * member.intentionLevel);
                renderBDIAgent(tourist);
            }
        }
        
        // Label avec état BDI
        gc.setFill(TEXT_PRIMARY);
        gc.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 12));
        gc.fillText(group.name + " (Cohésion: " + String.format("%.2f", group.cohesion) + ")", 
                   guide.x - 40, guide.y - group.radius - 20);
        
        // Indicateur de cohésion BDI
        gc.setStroke(SUCCESS.interpolate(DANGER, 1 - group.cohesion));
        gc.setLineWidth(3);
        gc.strokeArc(
            guide.x - 25, guide.y - 25, 50, 50,
            90, -group.cohesion * 360,
            ArcType.OPEN
        );
    }
    
    private void renderBDIAgent(VisualAgent agent) {
        double size = agent.getSize();
        
        // Ombre
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillOval(agent.x - size/2 + 2, agent.y - size/2 + 2, size, size);
        
        // Gradient BDI selon l'état de satisfaction
        Color baseColor = agent.color.interpolate(
            agent.satisfaction > 0.5 ? SUCCESS : DANGER, 
            Math.abs(agent.satisfaction - 0.5) * 0.3
        );
        
        RadialGradient agentGradient = new RadialGradient(
            0, 0, 0.3, 0.3, 0.6, true, CycleMethod.NO_CYCLE,
            new Stop(0, baseColor.brighter()),
            new Stop(0.7, baseColor),
            new Stop(1, baseColor.darker())
        );
        
        gc.setFill(agentGradient);
        gc.fillOval(agent.x - size/2, agent.y - size/2, size, size);
        
        // Bordure avec pulsation selon l'état BDI
        double intensityPulse = 1 + Math.sin(time * 3 + agent.id.hashCode()) * 0.2 * agent.satisfaction;
        gc.setStroke(baseColor.brighter().brighter());
        gc.setLineWidth(2 * intensityPulse);
        gc.strokeOval(agent.x - size/2, agent.y - size/2, size, size);
        
        // Icône type d'agent
        gc.setFill(TEXT_PRIMARY);
        gc.setFont(Font.font("SF Mono", FontWeight.BOLD, size/2));
        String icon = getAgentIcon(agent.type);
        gc.fillText(icon, agent.x - size/4, agent.y + size/4);
        
        // Nom avec profil BDI
        gc.setFont(Font.font("SF Pro Text", 9));
        gc.setFill(TEXT_SECONDARY);
        String displayName = agent.name;
        if (agent.profile != null) {
            displayName += " (" + agent.profile.getNationality() + ")";
        }
        gc.fillText(displayName, agent.x - 25, agent.y - size/2 - 5);
        
        // Barre d'état BDI
        if (agent.showStatus) {
            renderBDIStatusBar(agent, size);
        }
    }
    
    private String getAgentIcon(AgentType type) {
        switch (type) {
            case COORDINATOR: return "C";
            case GUIDE: return "G";
            case TOURIST: return "T";
            default: return "?";
        }
    }
    
    private void renderBDIStatusBar(VisualAgent agent, double size) {
        double barWidth = size * 1.8;
        double barHeight = 4;
        double barY = agent.y + size/2 + 10;
        
        // Barre satisfaction
        gc.setFill(Color.rgb(50, 50, 50, 0.7));
        gc.fillRoundRect(agent.x - barWidth/2, barY, barWidth, barHeight, 2, 2);
        
        Color satisfactionColor = agent.satisfaction > 0.6 ? SUCCESS : 
                                 agent.satisfaction > 0.3 ? WARNING : DANGER;
        gc.setFill(satisfactionColor);
        gc.fillRoundRect(agent.x - barWidth/2, barY, barWidth * agent.satisfaction, barHeight, 2, 2);
        
        // Barre fatigue si agent touriste
        if (agent.type == AgentType.TOURIST && agent.fatigue > 0.1) {
            gc.setFill(Color.rgb(50, 50, 50, 0.7));
            gc.fillRoundRect(agent.x - barWidth/2, barY + 6, barWidth, barHeight - 1, 2, 2);
            
            gc.setFill(DANGER.interpolate(WARNING, 1 - agent.fatigue));
            gc.fillRoundRect(agent.x - barWidth/2, barY + 6, barWidth * agent.fatigue, barHeight - 1, 2, 2);
        }
    }
    
    private void renderBDINetworkLink(VisualNetworkLink link) {
        VisualAgent from = agents.get(link.fromId);
        VisualAgent to = agents.get(link.toId);
        
        if (from == null || to == null) return;
        
        double progress = link.progress;
        double x = from.x + (to.x - from.x) * progress;
        double y = from.y + (to.y - from.y) * progress;
        
        // Traînée avec intensité selon le type de message BDI
        Color linkColor = link.color.deriveColor(0, 1, 1, 0.3 + link.intensity * 0.4);
        gc.setStroke(linkColor);
        gc.setLineWidth(1 + link.intensity);
        gc.strokeLine(from.x, from.y, x, y);
        
        // Paquet de données BDI
        gc.setFill(link.color.brighter());
        double packetSize = 4 + link.intensity * 2;
        gc.fillOval(x - packetSize, y - packetSize, packetSize * 2, packetSize * 2);
    }
    
    private void renderGamingHUD() {
        renderMiniMap();
        
        gc.setFill(SUCCESS);
        gc.setFont(Font.font("SF Mono", 10));
        gc.fillText("60 FPS", CANVAS_WIDTH - 50, 20);
        
        gc.setFill(TEXT_SECONDARY);
        gc.fillText(String.format("TEMPS BDI: %.1fs", time), 10, 20);
        
        // Indicateur système BDI
        gc.setFill(systemRunning ? SUCCESS : DANGER);
        gc.fillText("SYSTÈME: " + (systemRunning ? "ACTIF" : "ARRÊTÉ"), 10, 35);
        
        // CORRECTION: Afficher l'état des salles
        gc.setFill(TEXT_SECONDARY);
        gc.fillText("SALLES OCCUPÉES: " + roomOccupancy.size() + "/" + rooms.size(), 10, 50);
    }
    
    private void renderMiniMap() {
        double mmX = CANVAS_WIDTH - 160;
        double mmY = 10;
        double mmWidth = 150;
        double mmHeight = 100;
        
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRoundRect(mmX, mmY, mmWidth, mmHeight, 5, 5);
        
        gc.setStroke(PRIMARY.deriveColor(0, 1, 1, 0.5));
        gc.setLineWidth(1);
        gc.strokeRoundRect(mmX, mmY, mmWidth, mmHeight, 5, 5);
        
        double scale = 0.15;
        for (VisualAgent agent : agents.values()) {
            Color agentColor = agent.color;
            if (agent.inGroup) {
                agentColor = agentColor.brighter();
            }
            gc.setFill(agentColor);
            double ax = mmX + agent.x * scale;
            double ay = mmY + agent.y * scale;
            gc.fillOval(ax - 2, ay - 2, 4, 4);
        }
    }
    
    // Méthodes du système BDI
    
    private void startSystem() {
        systemRunning = true;
        agents.clear();
        starGroups.clear();
        networkLinks.clear();
        waitingTouristGroups.clear();
        roomOccupancy.clear();
        waitingForRoom.clear();
        
        // Créer coordinateur BDI
        VisualAgent coordinator = new VisualAgent("COORDINATEUR", AgentType.COORDINATOR);
        coordinator.setPosition(CANVAS_WIDTH / 2, 50);
        coordinator.color = PURPLE;
        coordinator.satisfaction = 0.8;
        coordinator.tourState = TourState.AVAILABLE;
        agents.put(coordinator.id, coordinator);
        
        // CORRECTION: Créer le nombre configuré de guides BDI
        String[] guideNames = {"Renaissance", "Moderne", "Impressionniste", "Sculptures", "Histoire", "Art"};
        Color[] guideColors = {PRIMARY, CYAN, PINK, SUCCESS, WARNING, PURPLE};
        
        for (int i = 0; i < numberOfGuides; i++) {
            String guideName = "GUIDE-" + guideNames[i % guideNames.length];
            if (i >= guideNames.length) {
                guideName += "-" + (i - guideNames.length + 2);
            }
            
            VisualAgent guide = new VisualAgent(guideName, AgentType.GUIDE);
            // Positionner les guides en zone d'attente
            guide.setPosition(850 + (i % 2) * 60, 100 + (i / 2) * 80);
            guide.color = guideColors[i % guideColors.length];
            guide.satisfaction = 0.7;
            guide.tourState = TourState.AVAILABLE;
            guide.guideProfile = new GuideProfile(guideNames[i % guideNames.length], 5 + i * 2);
            agents.put(guide.id, guide);
        }
        
        // Réinitialiser les compteurs
        lastGroupArrival = time;
        groupCounter = 0;
        
        updateMetrics();
        addEvent("Système BDI initialisé - " + numberOfGuides + " guides disponibles", SUCCESS);
    }
    
    private void pauseSystem() {
        systemRunning = !systemRunning;
        addEvent(systemRunning ? "Système BDI repris" : "Système BDI en pause", WARNING);
    }
    
    private void resetSystem() {
        systemRunning = false;
        agents.clear();
        starGroups.clear();
        networkLinks.clear();
        waitingTouristGroups.clear();
        roomOccupancy.clear(); // CORRECTION: Nettoyer l'occupation des salles
        waitingForRoom.clear();
        
        for (VisualRoom room : rooms) {
            room.occupied = false;
        }
        
        time = 0;
        groupCounter = 0;
        updateMetrics();
        addEvent("Système BDI réinitialisé", DANGER);
    }
    
    private void runScenario(String scenario) {
        switch (scenario) {
            case "formation":
                runBDIGroupFormation();
                break;
            case "negotiation":
                runBDINegotiation();
                break;
            case "emergency":
                runBDIEmergency();
                break;
            case "school":
                runBDISchoolGroup();
                break;
            case "multi":
                runBDIMultiGroups();
                break;
            case "adaptation":
                runBDIAdaptation();
                break;
            case "cohesion":
                runBDICohesionTest();
                break;
        }
    }
    
    private void runBDIGroupFormation() {
        // Ce scénario teste juste la formation de groupe sans affecter les visites cycliques
        addEvent("Test formation groupe BDI - Voir les visites automatiques", PRIMARY);
    }
    
    private void runBDINegotiation() {
        // Accélérer les arrivées de groupes pour tester la négociation entre guides
        groupArrivalInterval = 3.0; // Groupes arrivent plus vite
        addEvent("Mode négociation - Arrivées accélérées de groupes", WARNING);
    }
    
    private void runBDIEmergency() {
        // Créer une urgence qui affecte une visite en cours
        if (starGroups.isEmpty()) {
            addEvent("Aucune visite en cours pour simuler l'urgence", DANGER);
            return;
        }
        
        VisualStarGroup groupInTour = starGroups.get(0);
        if (!groupInTour.members.isEmpty()) {
            // Simuler une urgence avec un membre du groupe
            VisualGroupMember emergencyMember = groupInTour.members.get(0);
            VisualAgent emergencyTourist = agents.get(emergencyMember.agentId);
            
            if (emergencyTourist != null) {
                emergencyTourist.color = DANGER;
                emergencyTourist.satisfaction = 0.1;
                emergencyTourist.fatigue = 0.9;
                emergencyTourist.showStatus = true;
                
                // CORRECTION: Alerter seulement SON guide assigné
                if (emergencyTourist.assignedGuideId != null) {
                    VisualAgent assignedGuide = agents.get(emergencyTourist.assignedGuideId);
                    if (assignedGuide != null) {
                        networkLinks.add(new VisualNetworkLink(emergencyTourist.id, assignedGuide.id, DANGER, 1.0));
                    }
                }
                
                addEvent("URGENCE dans " + groupInTour.name + " - Intervention du guide assigné", DANGER);
            }
        }
    }
    
    private void runBDISchoolGroup() {
        // Créer un groupe scolaire plus large qui arrive à l'accueil
        List<VisualAgent> schoolGroup = new ArrayList<>();
        int groupSize = 12; // Groupe scolaire plus important
        
        VisualRoom accueil = findRoom("Accueil");
        if (accueil == null) return;
        
        for (int i = 0; i < groupSize; i++) {
            VisualAgent student = new VisualAgent("SCHOOL-STUDENT-" + (i + 1), AgentType.TOURIST);
            
            // Positionner dans l'accueil
            double offsetX = (Math.random() - 0.5) * 90;
            double offsetY = (Math.random() - 0.5) * 70;
            student.setPosition(accueil.x + accueil.width/2 + offsetX, accueil.y + accueil.height/2 + offsetY);
            
            // Profil étudiant BDI
            student.profile = new TouristProfile("Student" + (i + 1));
            student.profile.getPersonality().increaseExperience(0.1); // Curiosité étudiante
            student.color = SUCCESS.brighter();
            student.satisfaction = 0.8;
            student.fatigue = 0.2;
            student.inGroup = false;
            student.tourState = TourState.WAITING;
            
            agents.put(student.id, student);
            schoolGroup.add(student);
        }
        
        // Ajouter en priorité à la file d'attente
        waitingTouristGroups.offer(schoolGroup);
        addEvent("Groupe scolaire de " + groupSize + " étudiants arrivé", SUCCESS);
    }
    
    private void runBDIMultiGroups() {
        // Générer plusieurs groupes rapidement pour tester la coordination multi-guides
        for (int i = 0; i < 3; i++) {
            generateNewTouristGroup();
        }
        
        // Réduire l'intervalle entre arrivées pour voir plusieurs groupes simultanément
        groupArrivalInterval = 8.0;
        
        addEvent("3 groupes générés - Test coordination multi-guides", PURPLE);
    }
    
    private void runBDIAdaptation() {
        // Modifier les paramètres des visites existantes pour tester l'adaptation
        for (VisualStarGroup group : starGroups) {
            if (group.guide != null) {
                VisualAgent guide = agents.get(group.guideId);
                if (guide != null) {
                    // Adaptation: changer la durée de visite selon le groupe
                    guide.roomVisitDuration = 5.0 + Math.random() * 6.0; // Entre 5 et 11 secondes
                    
                    // Modifier la cohésion du groupe dynamiquement
                    Timeline adaptation = new Timeline(
                        new KeyFrame(Duration.seconds(0), new KeyValue(group.cohesionProperty, group.cohesion)),
                        new KeyFrame(Duration.seconds(3), new KeyValue(group.cohesionProperty, Math.min(1.0, group.cohesion + 0.2)))
                    );
                    adaptation.play();
                }
            }
        }
        
        addEvent("Adaptation BDI - Durées de visite ajustées dynamiquement", CYAN);
    }
    
    private void runBDICohesionTest() {
        if (starGroups.isEmpty()) {
            addEvent("Aucune visite en cours pour test de cohésion", DANGER);
            return;
        }
        
        // Tester la cohésion des groupes en visite
        for (VisualStarGroup group : starGroups) {
            // Simuler une perturbation puis récupération progressive
            Timeline cohesionTest = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(group.cohesionProperty, group.cohesion)),
                new KeyFrame(Duration.seconds(1), new KeyValue(group.cohesionProperty, Math.max(0.1, group.cohesion - 0.4))),
                new KeyFrame(Duration.seconds(4), new KeyValue(group.cohesionProperty, Math.min(1.0, group.cohesion + 0.1)))
            );
            cohesionTest.play();
            
            // Tester résilience des croyances des membres
            for (VisualGroupMember member : group.members) {
                Timeline beliefTest = new Timeline(
                    new KeyFrame(Duration.seconds(0), new KeyValue(member.beliefProperty, member.beliefStrength)),
                    new KeyFrame(Duration.seconds(1.5), new KeyValue(member.beliefProperty, Math.max(0.2, member.beliefStrength - 0.3))),
                    new KeyFrame(Duration.seconds(5), new KeyValue(member.beliefProperty, Math.min(1.0, member.beliefStrength + 0.05)))
                );
                beliefTest.play();
            }
        }
        
        addEvent("Test cohésion BDI sur groupes en visite - Résistance testée", PINK);
    }
    
    private void animateAgentMovement(VisualAgent agent, double targetX, double targetY) {
        Timeline move = new Timeline(
            new KeyFrame(Duration.seconds(1.5),
                new KeyValue(agent.xProperty, targetX, Interpolator.EASE_BOTH),
                new KeyValue(agent.yProperty, targetY, Interpolator.EASE_BOTH))
        );
        move.play();
    }
    
    // CORRECTION: Améliorer la gestion des salles
    
    /**
     * Vérifie si une salle peut être occupée par un groupe
     */
    private boolean canOccupyRoom(String roomName, String groupId) {
        if (roomName.equals("Accueil") || roomName.equals("Sortie")) {
            return true; // Ces salles peuvent être partagées
        }
        
        return !roomOccupancy.containsKey(roomName);
    }
    
    /**
     * Occupe une salle pour un groupe spécifique
     */
    private void occupyRoom(String roomName, String groupId) {
        if (!roomName.equals("Accueil") && !roomName.equals("Sortie")) {
            roomOccupancy.put(roomName, groupId);
            waitingForRoom.remove(groupId);
        }
    }
    
    /**
     * Libère une salle occupée par un groupe
     */
    private void freeRoom(String roomName, String groupId) {
        if (roomOccupancy.get(roomName) != null && roomOccupancy.get(roomName).equals(groupId)) {
            roomOccupancy.remove(roomName);
        }
    }
    
    /**
     * Trouve une salle alternative si la salle souhaitée est occupée
     */
    private String findAlternativeRoom(List<String> visitPath, int currentIndex, String groupId) {
        if (currentIndex >= visitPath.size()) return null;
        
        String desiredRoom = visitPath.get(currentIndex);
        
        // Si la salle souhaitée est libre, la retourner
        if (canOccupyRoom(desiredRoom, groupId)) {
            return desiredRoom;
        }
        
        // Sinon, chercher dans les salles connectées
        VisualRoom currentRoom = findRoom(desiredRoom);
        if (currentRoom != null && !currentRoom.nextRooms.isEmpty()) {
            for (VisualRoom nextRoom : currentRoom.nextRooms) {
                if (canOccupyRoom(nextRoom.name, groupId)) {
                    return nextRoom.name;
                }
            }
        }
        
        // En dernier recours, attendre
        waitingForRoom.add(groupId);
        return null;
    }
    
    // Méthodes utilitaires
    
    private VisualAgent getFirstGuide() {
        for (VisualAgent agent : agents.values()) {
            if (agent.type == AgentType.GUIDE) {
                return agent;
            }
        }
        return null;
    }
    
    private void addMetric(GridPane grid, int row, String name, String value, Color color) {
        Label nameLabel = new Label(name);
        nameLabel.setTextFill(TEXT_SECONDARY);
        nameLabel.setFont(Font.font("SF Pro Text", 10));
        
        Label valueLabel = new Label(value);
        valueLabel.setTextFill(color);
        valueLabel.setFont(Font.font("SF Mono", FontWeight.BOLD, 12));
        valueLabel.setId("metric_" + row);
        
        grid.add(nameLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }
    
    private void updateMetricValue(GridPane grid, int row, String newValue) {
        Label valueLabel = (Label) grid.lookup("#metric_" + row);
        if (valueLabel != null) {
            valueLabel.setText(newValue);
        }
    }
    
    private void addEvent(String message, Color color) {
        Platform.runLater(() -> {
            VBox eventsList = (VBox) root.lookup("#events_list");
            if (eventsList == null) return;
            
            HBox event = new HBox(8);
            event.setAlignment(Pos.CENTER_LEFT);
            event.setPadding(new Insets(3, 8, 3, 8));
            event.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 4;");
            
            Circle indicator = new Circle(3, color);
            Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            timeLabel.setTextFill(TEXT_SECONDARY);
            timeLabel.setFont(Font.font("SF Mono", 8));
            
            Label msgLabel = new Label(message);
            msgLabel.setTextFill(TEXT_PRIMARY);
            msgLabel.setFont(Font.font("SF Pro Text", 9));
            
            event.getChildren().addAll(indicator, timeLabel, msgLabel);
            eventsList.getChildren().add(0, event);
            
            if (eventsList.getChildren().size() > 20) {
                eventsList.getChildren().remove(20, eventsList.getChildren().size());
            }
        });
    }
    
    private void processEvents() {
        while (!eventQueue.isEmpty()) {
            VisualGameEvent event = eventQueue.poll();
            if (event != null) {
                addEvent(event.message, event.color);
            }
        }
    }
    
    private double calculateCohesion() {
        if (starGroups.isEmpty()) return 0.0;
        return starGroups.stream()
                       .mapToDouble(g -> g.cohesion)
                       .average()
                       .orElse(0.0);
    }
    
    private double calculateEfficiency() {
        if (agents.isEmpty()) return 0.0;
        return agents.values().stream()
                     .filter(a -> a.inGroup)
                     .count() / (double) agents.size();
    }
    
    private double calculateSatisfaction() {
        if (agents.isEmpty()) return 0.0;
        return agents.values().stream()
                     .mapToDouble(a -> a.satisfaction)
                     .average()
                     .orElse(0.0);
    }
    
    @SuppressWarnings("unchecked")
    private void updateAgentList() {
        ListView<HBox> list = (ListView<HBox>) root.lookup("#agent_list");
        if (list == null) return;
        
        list.getItems().clear();
        
        for (VisualAgent agent : agents.values()) {
            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(5, 10, 5, 10));
            
            Circle indicator = new Circle(4, agent.color);
            Label name = new Label(agent.name);
            name.setTextFill(TEXT_PRIMARY);
            name.setFont(Font.font("SF Pro Text", 10));
            
            Label type = new Label(agent.type.toString());
            type.setTextFill(TEXT_SECONDARY);
            type.setFont(Font.font("SF Pro Text", 9));
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label status = new Label(agent.inGroup ? "GROUPE" : "LIBRE");
            status.setTextFill(agent.inGroup ? SUCCESS : TEXT_SECONDARY);
            status.setFont(Font.font("SF Pro Text", 9));
            
            // Indicateur BDI
            if (agent.profile != null || agent.guideProfile != null) {
                Label bdiIndicator = new Label("BDI");
                bdiIndicator.setTextFill(PRIMARY);
                bdiIndicator.setFont(Font.font("SF Pro Text", 8));
                item.getChildren().addAll(indicator, name, type, bdiIndicator, spacer, status);
            } else {
                item.getChildren().addAll(indicator, name, type, spacer, status);
            }
            
            list.getItems().add(item);
        }
    }
    
    private void updateRoomsList() {
        VBox roomsList = (VBox) root.lookup("#rooms_list");
        if (roomsList == null) return;
        
        roomsList.getChildren().clear();
        
        for (VisualRoom room : rooms) {
            HBox roomItem = new HBox(8);
            roomItem.setAlignment(Pos.CENTER_LEFT);
            roomItem.setPadding(new Insets(3, 8, 3, 8));
            roomItem.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 4;");
            
            Circle statusDot = new Circle(3, room.occupied ? DANGER : SUCCESS);
            Label roomName = new Label(room.name);
            roomName.setTextFill(TEXT_PRIMARY);
            roomName.setFont(Font.font("SF Pro Text", 9));
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // CORRECTION: Afficher le groupe occupant la salle
            String statusText = room.occupied ? "OCCUPÉE" : "LIBRE";
            if (room.occupied && roomOccupancy.containsKey(room.name)) {
                statusText = roomOccupancy.get(room.name);
            }
            
            Label status = new Label(statusText);
            status.setTextFill(room.occupied ? DANGER : SUCCESS);
            status.setFont(Font.font("SF Mono", 8));
            
            roomItem.getChildren().addAll(statusDot, roomName, spacer, status);
            roomsList.getChildren().add(roomItem);
        }
    }
    
    private void handleCanvasClick(double x, double y) {
        for (VisualAgent agent : agents.values()) {
            double dx = x - agent.x;
            double dy = y - agent.y;
            if (Math.sqrt(dx*dx + dy*dy) < agent.getSize()) {
                selectedAgent = agent;
                agent.showStatus = !agent.showStatus;
                return;
            }
        }
    }
    
    private void handleCanvasHover(double x, double y) {
        for (VisualAgent agent : agents.values()) {
            double dx = x - agent.x;
            double dy = y - agent.y;
            agent.highlighted = Math.sqrt(dx*dx + dy*dy) < agent.getSize();
        }
    }
    
    private void updateMetrics() {
        Platform.runLater(() -> {
            GridPane grid = (GridPane) metricsPanel.getChildren().get(1);
            
            updateMetricValue(grid, 0, String.valueOf(agents.size()));
            updateMetricValue(grid, 1, String.valueOf(starGroups.size()));
            updateMetricValue(grid, 2, String.format("%.0f%%", calculateCohesion() * 100));
            updateMetricValue(grid, 3, String.format("%.0f%%", calculateSatisfaction() * 100));
            updateMetricValue(grid, 4, String.format("%.0f%%", calculateEfficiency() * 100));
            updateMetricValue(grid, 5, String.valueOf(networkLinks.size() * 2));
            
            updateAgentList();
            updateRoomsList();
        });
    }
    
    private String getIOSGamingCSS() {
        return """
            .button { -fx-cursor: hand; }
            .list-view .list-cell:filled:selected:focused, .list-view .list-cell:filled:selected {
                -fx-background-color: rgba(10, 132, 255, 0.3);
            }
            .list-view .list-cell:filled:hover {
                -fx-background-color: rgba(255, 255, 255, 0.05);
            }
            .scroll-pane > .viewport {
                -fx-background-color: transparent;
            }
            .scroll-pane {
                -fx-background-color: transparent;
            }
            .scroll-pane .corner {
                -fx-background-color: transparent;
            }
            """;
    }
    
    private void shutdown() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        Platform.exit();
    }
    
    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
    
    // Classes intégrées pour la visualisation BDI
    
    /**
     * Agent visuel intégrant les profils BDI réels
     */
    public class VisualAgent {
        String id;
        String name;
        AgentType type;
        
        DoubleProperty xProperty = new SimpleDoubleProperty();
        DoubleProperty yProperty = new SimpleDoubleProperty();
        double x, y;
        double vx = 0, vy = 0;
        
        Color color;
        double satisfaction = 0.7;
        double fatigue = 0.0;
        boolean inGroup = false;
        boolean highlighted = false;
        boolean showStatus = false;
        
        // CORRECTION: Ajouter l'ID du guide assigné pour éviter la communication croisée
        String assignedGuideId = null;
        
        // Intégration BDI
        TouristProfile profile; // Pour agents touristes
        GuideProfile guideProfile; // Pour agents guides
        BehaviorManager behaviorManager;
        SatisfactionTracker satisfactionTracker;
        
        // Gestion des visites guidées
        TourState tourState = TourState.AVAILABLE;
        VisualStarGroup currentTourGroup;
        List<String> visitPath = new ArrayList<>();
        int currentRoomIndex = 0;
        double timeInCurrentRoom = 0;
        double roomVisitDuration = 8.0; // 8 secondes par salle
        double tourProgress = 0;
        
        public VisualAgent(String name, AgentType type) {
            this.id = name + "_" + System.nanoTime();
            this.name = name;
            this.type = type;
            this.tourState = TourState.AVAILABLE;
            this.visitPath = new ArrayList<>();
            
            xProperty.addListener((obs, oldVal, newVal) -> x = newVal.doubleValue());
            yProperty.addListener((obs, oldVal, newVal) -> y = newVal.doubleValue());
            
            // Initialiser les composants BDI selon le type
            if (type == AgentType.TOURIST) {
                profile = new TouristProfile(name);
                behaviorManager = new BehaviorManager();
                satisfactionTracker = new SatisfactionTracker(profile);
            } else if (type == AgentType.GUIDE) {
                guideProfile = new GuideProfile(name);
            }
        }
        
        public void setPosition(double x, double y) {
            this.x = x;
            this.y = y;
            xProperty.set(x);
            yProperty.set(y);
        }
        
        public void update(double dt) {
            // Logique de visite guidée pour les guides
            if (type == AgentType.GUIDE && tourState == TourState.TOURING) {
                updateGuidedTour(dt);
            }
            
            // Mise à jour BDI pour touristes
            if (type == AgentType.TOURIST && profile != null) {
                updateTouristBDI(dt);
            }
            
            // Mouvement physique pour agents non en groupe ou guides actifs
            if (!inGroup || (type == AgentType.GUIDE && tourState == TourState.TOURING)) {
                x += vx * dt * 60;
                y += vy * dt * 60;
                
                vx *= 0.95;
                vy *= 0.95;
                
                if (x < 20 || x > CANVAS_WIDTH - 20) vx = -vx;
                if (y < 20 || y > CANVAS_HEIGHT - 20) vy = -vy;
                
                xProperty.set(x);
                yProperty.set(y);
            }
        }
        
        private void updateGuidedTour(double dt) {
            if (visitPath.isEmpty() || currentTourGroup == null) return;
            
            timeInCurrentRoom += dt;
            
            // Vérifier si il faut passer à la salle suivante
            if (timeInCurrentRoom >= roomVisitDuration) {
                moveToNextRoom();
            }
        }
        
        private void moveToNextRoom() {
            currentRoomIndex++;
            
            // Visite terminée
            if (currentRoomIndex >= visitPath.size()) {
                finishTour();
                return;
            }
            
            // CORRECTION: Utiliser la gestion améliorée des salles
            String nextRoomName = visitPath.get(currentRoomIndex);
            String groupId = currentTourGroup.name;
            
            // Vérifier si la salle est disponible ou trouver une alternative
            String roomToVisit = findAlternativeRoom(visitPath, currentRoomIndex, groupId);
            
            if (roomToVisit == null) {
                // Attendre que la salle se libère
                addEvent("Groupe " + groupId + " attend une salle disponible", WARNING);
                timeInCurrentRoom = roomVisitDuration - 2.0; // Réessayer dans 2 secondes
                return;
            }
            
            // Libérer la salle précédente
            if (currentRoomIndex > 0) {
                String previousRoomName = visitPath.get(currentRoomIndex - 1);
                freeRoom(previousRoomName, groupId);
            }
            
            // Occuper la nouvelle salle
            occupyRoom(roomToVisit, groupId);
            
            // Déplacer le groupe vers la nouvelle salle
            VisualRoom nextRoom = findRoom(roomToVisit);
            if (nextRoom != null) {
                animateAgentMovement(this, 
                    nextRoom.x + nextRoom.width/2, 
                    nextRoom.y + nextRoom.height/2);
                
                timeInCurrentRoom = 0;
                addEvent("Groupe " + groupId + " visite " + roomToVisit, 
                        currentTourGroup.color);
            }
        }
        
        private void finishTour() {
            if (currentTourGroup == null) return;
            
            String groupId = currentTourGroup.name;
            
            // Libérer la dernière salle (sauf si c'est la sortie)
            if (currentRoomIndex > 0 && currentRoomIndex <= visitPath.size()) {
                String lastRoomName = visitPath.get(currentRoomIndex - 1);
                if (!lastRoomName.equals("Sortie")) {
                    freeRoom(lastRoomName, groupId);
                }
            }
            
            // CORRECTION: Nettoyer complètement les références avant l'animation
            VisualStarGroup groupToRemove = currentTourGroup;
            
            // CORRECTION: Guide devient "EN TRANSIT" vers l'accueil (pas encore disponible)
            tourState = TourState.WAITING; // En transit, pas encore disponible
            currentTourGroup = null;
            currentRoomIndex = 0;
            timeInCurrentRoom = 0;
            inGroup = false;
            
            // Supprimer le groupe de la liste AVANT l'animation pour éviter le rendu fantôme
            starGroups.remove(groupToRemove);
            
            // Animation de sortie progressive pour les touristes
            for (VisualGroupMember member : groupToRemove.members) {
                VisualAgent tourist = agents.get(member.agentId);
                if (tourist != null) {
                    tourist.tourState = TourState.FINISHED;
                    tourist.inGroup = false;
                    tourist.assignedGuideId = null;
                    
                    Timeline exitAnimation = new Timeline(
                        new KeyFrame(Duration.seconds(0),
                            new KeyValue(tourist.xProperty, tourist.x),
                            new KeyValue(tourist.yProperty, tourist.y)),
                        new KeyFrame(Duration.seconds(2 + Math.random() * 3),
                            new KeyValue(tourist.xProperty, tourist.x + (Math.random() - 0.5) * 100),
                            new KeyValue(tourist.yProperty, CANVAS_HEIGHT + 50))
                    );
                    
                    exitAnimation.setOnFinished(e -> {
                        agents.remove(member.agentId);
                    });
                    
                    exitAnimation.play();
                }
            }
            
            addEvent("Visite " + groupId + " terminée - Guide retourne à l'accueil", SUCCESS);
            
            // CORRECTION: Guide retourne d'abord à l'accueil, puis devient disponible
            VisualRoom accueil = findRoom("Accueil");
            if (accueil != null) {
                Timeline returnToWelcome = new Timeline(
                    new KeyFrame(Duration.seconds(3), // 3 secondes pour retourner à l'accueil
                        new KeyValue(xProperty, accueil.x + accueil.width/2, Interpolator.EASE_BOTH),
                        new KeyValue(yProperty, accueil.y + accueil.height/2, Interpolator.EASE_BOTH))
                );
                
                returnToWelcome.setOnFinished(e -> {
                    // Une fois arrivé à l'accueil, le guide devient disponible
                    tourState = TourState.AVAILABLE;
                    addEvent("Guide " + name + " disponible pour nouvelle visite", PRIMARY);
                    
                    // CORRECTION: Vérifier immédiatement s'il y a des groupes en attente
                    // pour créer une continuité visuelle naturelle
                    if (!waitingTouristGroups.isEmpty()) {
                        Timeline delayBeforePickup = new Timeline(
                            new KeyFrame(Duration.seconds(1), evt -> {
                                // Donner une seconde au guide pour "voir" les touristes
                                // puis les prendre en charge automatiquement
                                if (tourState == TourState.AVAILABLE && !waitingTouristGroups.isEmpty()) {
                                    List<VisualAgent> waitingGroup = waitingTouristGroups.poll();
                                    if (waitingGroup != null) {
                                        startGuidedTour(this, waitingGroup);
                                    }
                                }
                            })
                        );
                        delayBeforePickup.play();
                    }
                });
                
                returnToWelcome.play();
            }
            
            // Libérer la salle "Sortie" après un délai
            Timeline delayedRoomFree = new Timeline(
                new KeyFrame(Duration.seconds(6), e -> freeRoom("Sortie", groupId))
            );
            delayedRoomFree.play();
        }
        
        private void updateTouristBDI(double dt) {
            // Mettre à jour le comportement BDI
            if (behaviorManager != null) {
                behaviorManager.updateBehaviors(profile);
                // Note: executeActiveBehaviors() retourne List<BehaviorAction> qui n'est pas public
                // On simule les comportements BDI directement
                simulateBDIBehaviors();
            }
            
            // Mettre à jour la satisfaction
            satisfaction = profile.getSatisfaction();
            fatigue = profile.getFatigue();
            
            // Évolution naturelle de l'état
            profile.increaseFatigue(dt * 0.01);
            
            // Couleur dynamique selon l'état BDI
            updateColorFromBDIState();
        }
        
        private void simulateBDIBehaviors() {
            // CORRECTION: Communication seulement avec le guide assigné
            Personality personality = profile.getPersonality();
            
            // Simulation comportement question - seulement avec le guide assigné
            if (personality.getCuriosity() > 0.7 && Math.random() < 0.05 && assignedGuideId != null) {
                VisualAgent assignedGuide = agents.get(assignedGuideId);
                if (assignedGuide != null && assignedGuide.type == AgentType.GUIDE) {
                    // Limiter la création de liens pour éviter la surcharge visuelle
                    boolean hasExistingLink = networkLinks.stream()
                        .anyMatch(link -> link.fromId.equals(id) && link.toId.equals(assignedGuideId));
                    
                    if (!hasExistingLink) {
                        networkLinks.add(new VisualNetworkLink(id, assignedGuideId, CYAN, 0.7));
                    }
                }
            }
            
            // Simulation comportement expression
            if (personality.getSocialness() > 0.6 && Math.random() < 0.05) {
                satisfaction = Math.min(1.0, satisfaction + 0.05);
            }
            
            // Simulation prise de photo
            if (satisfaction > 0.6 && Math.random() < 0.03) {
                satisfaction = Math.min(1.0, satisfaction + 0.1);
            }
        }
        
        private void updateColorFromBDIState() {
            if (profile != null) {
                // Couleur basée sur satisfaction et fatigue
                double hue = satisfaction * 120; // Rouge à vert
                double brightness = 1.0 - fatigue * 0.5;
                color = Color.hsb(hue, 0.8, brightness);
            }
        }
        
        public double getSize() {
            double base = type == AgentType.COORDINATOR ? 24 : 
                         type == AgentType.GUIDE ? 20 : 16;
            return highlighted ? base * 1.2 : base;
        }
    }
    
    /**
     * Membre de groupe avec états BDI
     */
    public class VisualGroupMember {
        String agentId;
        double x, y;
        double angle;
        double radius;
        double offset;
        
        // États BDI
        double beliefStrength = 0.7; // Force des croyances
        double intentionLevel = 0.8; // Niveau d'intention
        
        DoubleProperty radiusProperty = new SimpleDoubleProperty();
        DoubleProperty beliefProperty = new SimpleDoubleProperty();
        DoubleProperty intentionProperty = new SimpleDoubleProperty();
        
        public VisualGroupMember(String agentId, double angle, double radius) {
            this.agentId = agentId;
            this.angle = angle;
            this.radius = radius;
            this.offset = Math.random();
            
            radiusProperty.set(radius);
            beliefProperty.set(beliefStrength);
            intentionProperty.set(intentionLevel);
            
            radiusProperty.addListener((obs, oldVal, newVal) -> this.radius = newVal.doubleValue());
            beliefProperty.addListener((obs, oldVal, newVal) -> this.beliefStrength = newVal.doubleValue());
            intentionProperty.addListener((obs, oldVal, newVal) -> this.intentionLevel = newVal.doubleValue());
        }
        
        public void update(double dt) {
            angle += dt * 0.1 * intentionLevel; // Vitesse selon intention
        }
    }
    
    /**
     * Groupe étoilé avec métriques BDI
     */
    public class VisualStarGroup {
        String guideId;
        String name;
        VisualAgent guide;
        List<VisualGroupMember> members = new ArrayList<>();
        Color color;
        double radius = 80;
        
        // Métriques BDI
        double cohesion = 0.7;
        double avgSatisfaction = 0.7;
        double avgBelief = 0.7;
        double avgIntention = 0.8;
        
        DoubleProperty cohesionProperty = new SimpleDoubleProperty();
        
        public VisualStarGroup(String guideId, String name) {
            this.guideId = guideId;
            this.name = name;
            cohesionProperty.set(cohesion);
            cohesionProperty.addListener((obs, oldVal, newVal) -> this.cohesion = newVal.doubleValue());
        }
        
        public void updateBDI(double dt) {
            if (guide == null) {
                guide = agents.get(guideId);
            }
            
            if (guide == null) return;
            
            for (VisualGroupMember member : members) {
                member.update(dt);
                
                // Mise à jour position étoilée avec variabilité BDI
                double intentionFactor = member.intentionLevel;
                member.x = guide.x + Math.cos(member.angle) * member.radius * intentionFactor;
                member.y = guide.y + Math.sin(member.angle) * member.radius * intentionFactor;
            }
            
            // Recalculer les métriques BDI
            updateBDIMetrics();
        }
        
        public void updateBDIMetrics() {
            if (members.isEmpty()) return;
            
            // Cohésion basée sur la proximité des croyances
            double beliefVariance = calculateBeliefVariance();
            cohesion = Math.max(0.1, 1.0 - beliefVariance);
            cohesionProperty.set(cohesion);
            
            // Moyennes des états BDI
            avgBelief = members.stream().mapToDouble(m -> m.beliefStrength).average().orElse(0.5);
            avgIntention = members.stream().mapToDouble(m -> m.intentionLevel).average().orElse(0.5);
            
            // Satisfaction du groupe
            avgSatisfaction = 0.0;
            int count = 0;
            for (VisualGroupMember member : members) {
                VisualAgent agent = agents.get(member.agentId);
                if (agent != null) {
                    avgSatisfaction += agent.satisfaction;
                    count++;
                }
            }
            if (count > 0) {
                avgSatisfaction /= count;
            }
        }
        
        private double calculateBeliefVariance() {
            if (members.size() <= 1) return 0.0;
            
            double mean = avgBelief;
            double sumSquares = 0.0;
            
            for (VisualGroupMember member : members) {
                double diff = member.beliefStrength - mean;
                sumSquares += diff * diff;
            }
            
            return sumSquares / members.size();
        }
    }
    
    /**
     * Salle du musée
     */
    public class VisualRoom {
        String name;
        double x, y, width, height;
        Color color;
        boolean occupied = false;
        List<VisualRoom> nextRooms = new ArrayList<>(); // Salles connectées
        
        public VisualRoom(String name, double x, double y, double width, double height, Color color) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    }
    
    /**
     * Lien de communication BDI
     */
    public class VisualNetworkLink {
        String fromId, toId;
        Color color;
        double progress = 0;
        double speed = 1.5;
        double lifeTime = 3.0;
        double currentTime = 0;
        double intensity = 0.5; // Intensité du message BDI
        
        public VisualNetworkLink(String fromId, String toId, Color color, double intensity) {
            this.fromId = fromId;
            this.toId = toId;
            this.color = color;
            this.intensity = Math.max(0.1, Math.min(1.0, intensity));
            this.speed *= intensity; // Messages plus intenses voyagent plus vite
        }
        
        public void update(double dt) {
            currentTime += dt;
            progress = (currentTime * speed) % 1.0;
        }
        
        public boolean isDead() {
            return currentTime > lifeTime;
        }
    }
    
    /**
     * Événement de jeu
     */
    public class VisualGameEvent {
        String message;
        Color color;
        long timestamp;
        
        public VisualGameEvent(String message, Color color) {
            this.message = message;
            this.color = color;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    // Point d'entrée
    public static void main(String[] args) {
        launch(args);
    }
}