package launcher;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Visualisation améliorée du système multi-agents musée avec animations fluides
 */
public class MuseumVisualizationApp extends Application {

    // Dimensions du musée
    private static final int CANVAS_WIDTH = 1400;
    private static final int CANVAS_HEIGHT = 900;
    
    // Composants JavaFX
    private Canvas museumCanvas;
    private GraphicsContext gc;
    private Label statusLabel;
    private Label statsLabel;
    private TextArea logArea;
    
    // Animation et effets
    private AnimationTimer gameLoop;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private Timeline lightingAnimation;
    
    // Propriété pour l'animation d'éclairage
    private DoubleProperty lightIntensityProperty = new SimpleDoubleProperty(1.0);
    
    // Données de simulation
    private Map<String, Agent> agents = new HashMap<>();
    private List<TourGroup> tourGroups = new ArrayList<>();
    private List<MuseumRoom> rooms = new ArrayList<>();
    private List<Corridor> corridors = new ArrayList<>();
    private List<Artwork> artworks = new ArrayList<>();
    private double time = 0;
    
    // État du système
    private boolean jadeRunning = false;
    private Point2D entrancePoint = new Point2D(120, 750);
    private Point2D exitPoint = new Point2D(1200, 750);
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Musée Virtuel Intelligent - Système Multi-Agents");
        
        // Style moderne pour la fenêtre
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background: linear-gradient(to bottom, #1a252f, #2c3e50);");
        
        // Panel principal avec effet glassmorphisme
        VBox centerPanel = createModernVisualizationPanel();
        root.setCenter(centerPanel);
        
        // Panel de contrôle moderne
        VBox controlPanel = createModernControlPanel();
        root.setRight(controlPanel);
        
        // Barre de statut moderne
        HBox statusPanel = createModernStatusPanel();
        root.setBottom(statusPanel);
        
        Scene scene = new Scene(root, 1800, 1000);
        scene.getStylesheets().add("data:text/css," +
            ".button:hover { -fx-scale-x: 1.05; -fx-scale-y: 1.05; }" +
            ".label { -fx-text-fill: white; }");
        
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> cleanup());
        
        // Initialiser le musée moderne
        initializeModernMuseum();
        startEnhancedGameLoop();
        setupLightingEffects();
    }
    
    private VBox createModernVisualizationPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));
        
        // Titre avec effet néon
        Text title = new Text("🏛️ MUSÉE VIRTUEL INTELLIGENT");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 24));
        title.setFill(Color.WHITE);
        
        // Effet de lueur
        DropShadow glow = new DropShadow();
        glow.setColor(Color.CYAN);
        glow.setRadius(20);
        glow.setSpread(0.3);
        title.setEffect(glow);
        
        // Canvas avec bordure moderne et ombres
        museumCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = museumCanvas.getGraphicsContext2D();
        
        // Conteneur avec effet glassmorphisme
        StackPane canvasContainer = new StackPane();
        canvasContainer.getChildren().add(museumCanvas);
        canvasContainer.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.1);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: rgba(255, 255, 255, 0.2);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);"
        );
        
        // Interaction simple
        museumCanvas.setOnMouseClicked(e -> {
            if (jadeRunning && isNearEntrance(e.getX(), e.getY())) {
                createTourGroup();
            }
        });
        
        // Hover effect
        museumCanvas.setOnMouseMoved(e -> {
            if (isNearEntrance(e.getX(), e.getY()) && jadeRunning) {
                museumCanvas.setCursor(javafx.scene.Cursor.HAND);
            } else {
                museumCanvas.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });
        
        panel.getChildren().addAll(title, canvasContainer);
        return panel;
    }
    
    private VBox createModernControlPanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(380);
        panel.setPadding(new Insets(25));
        
        // Style glassmorphisme
        panel.setStyle(
            "-fx-background-color: rgba(52, 73, 94, 0.3);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: rgba(255, 255, 255, 0.1);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 3);"
        );
        
        // Titre du panneau
        Label controlTitle = new Label("🎮 CENTRE DE CONTRÔLE");
        controlTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        controlTitle.setTextFill(Color.WHITE);
        
        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.LIGHTBLUE);
        titleGlow.setRadius(10);
        controlTitle.setEffect(titleGlow);
        
        // Boutons modernes avec animations
        Button startBtn = createModernButton("🚀 Démarrer JADE", "#27ae60", this::startJadeSystem);
        Button stopBtn = createModernButton("🛑 Arrêter Système", "#e74c3c", this::stopJadeSystem);
        Button newGroupBtn = createModernButton("👥 Nouveau Groupe", "#3498db", this::createTourGroup);
        
        // Section scénarios
        Label scenarioLabel = new Label("🎭 SCÉNARIOS DE VISITE");
        scenarioLabel.setTextFill(Color.LIGHTCYAN);
        scenarioLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Button scenario1 = createModernButton("👨‍👩‍👧‍👦 Famille (6 pers.)", "#9b59b6", () -> createSpecificGroup(6, "Famille"));
        Button scenario2 = createModernButton("🏫 Groupe Scolaire (15 pers.)", "#e67e22", () -> createSpecificGroup(15, "École"));
        Button scenario3 = createModernButton("🌍 Touristes VIP (4 pers.)", "#1abc9c", () -> createSpecificGroup(4, "VIP"));
        Button scenario4 = createModernButton("🎨 Passionnés d'Art (8 pers.)", "#f39c12", () -> createSpecificGroup(8, "Art"));
        
        // Statistiques avec style moderne
        statsLabel = new Label();
        statsLabel.setTextFill(Color.LIGHTGREEN);
        statsLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        statsLabel.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.3);" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 15;"
        );
        updateStatsDisplay();
        
        // Journal moderne
        Label logLabel = new Label("📋 JOURNAL SYSTÈME");
        logLabel.setTextFill(Color.WHITE);
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        logArea = new TextArea();
        logArea.setPrefHeight(250);
        logArea.setEditable(false);
        logArea.setStyle(
            "-fx-control-inner-background: rgba(44, 62, 80, 0.8);" +
            "-fx-text-fill: #00ff88;" +
            "-fx-font-family: 'Consolas';" +
            "-fx-font-size: 11px;" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;"
        );
        
        panel.getChildren().addAll(
            controlTitle,
            new Separator(),
            startBtn, stopBtn, newGroupBtn,
            new Separator(),
            scenarioLabel, 
            scenario1, scenario2, scenario3, scenario4,
            new Separator(),
            new Label("📊 STATISTIQUES TEMPS RÉEL"),
            statsLabel,
            new Separator(),
            logLabel, logArea
        );
        
        return panel;
    }
    
    private Button createModernButton(String text, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(330);
        btn.setPrefHeight(45);
        
        btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + color + ", " + 
            Color.web(color).darker().toString().replace("0x", "#") + ");" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Arial';" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 15;" +
            "-fx-border-radius: 15;" +
            "-fx-border-color: rgba(255, 255, 255, 0.2);" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
        );
        
        // Animations de survol
        btn.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), btn);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
            
            btn.setStyle(btn.getStyle() + 
                "-fx-effect: dropshadow(gaussian, " + color + ", 15, 0.5, 0, 3);"
            );
        });
        
        btn.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), btn);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
            
            btn.setStyle(btn.getStyle().replaceAll("-fx-effect: dropshadow\\([^;]*\\);", ""));
        });
        
        btn.setOnAction(e -> {
            // Animation de clic
            ScaleTransition clickScale = new ScaleTransition(Duration.millis(100), btn);
            clickScale.setToX(0.95);
            clickScale.setToY(0.95);
            clickScale.setOnFinished(ev -> {
                ScaleTransition backScale = new ScaleTransition(Duration.millis(100), btn);
                backScale.setToX(1.05);
                backScale.setToY(1.05);
                backScale.play();
            });
            clickScale.play();
            
            action.run();
        });
        
        return btn;
    }
    
    private HBox createModernStatusPanel() {
        HBox panel = new HBox(30);
        panel.setPadding(new Insets(15, 30, 15, 30));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle(
            "-fx-background-color: linear-gradient(to right, rgba(52, 73, 94, 0.4), rgba(44, 62, 80, 0.4));" +
            "-fx-border-color: rgba(255, 255, 255, 0.1);" +
            "-fx-border-width: 1 0 0 0;"
        );
        
        statusLabel = new Label("⏳ Système en attente...");
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Indicateur de statut lumineux
        Circle statusIndicator = new Circle(8);
        statusIndicator.setFill(Color.GRAY);
        
        // Animation de l'indicateur de statut
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.seconds(0), new KeyValue(statusIndicator.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(statusIndicator.opacityProperty(), 0.3))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
        
        panel.getChildren().addAll(statusIndicator, statusLabel);
        return panel;
    }
    
    /**
     * Initialise un musée moderne avec design avancé
     */
    private void initializeModernMuseum() {
        // Entrée majestueuse
        rooms.add(new MuseumRoom("🏛️ HALL D'ACCUEIL", 70, 700, 200, 120, 
            Color.web("#3498db"), "ENTRANCE"));
        
        // Couloir principal élégant
        corridors.add(new Corridor(270, 720, 850, 80, "MAIN"));
        
        // Salles d'exposition premium
        rooms.add(new MuseumRoom("🎨 RENAISSANCE", 320, 550, 180, 140, 
            Color.web("#f39c12"), "EXHIBITION"));
        rooms.add(new MuseumRoom("🖼️ IMPRESSIONNISME", 550, 550, 180, 140, 
            Color.web("#e74c3c"), "EXHIBITION"));
        rooms.add(new MuseumRoom("🎭 ART MODERNE", 780, 550, 180, 140, 
            Color.web("#9b59b6"), "EXHIBITION"));
        
        // Couloirs de liaison
        corridors.add(new Corridor(390, 450, 80, 200, "VERTICAL"));
        corridors.add(new Corridor(620, 450, 80, 200, "VERTICAL"));
        corridors.add(new Corridor(850, 450, 80, 200, "VERTICAL"));
        
        // Étage supérieur
        rooms.add(new MuseumRoom("🏺 ANTIQUITÉS", 320, 300, 180, 140, 
            Color.web("#16a085"), "EXHIBITION"));
        rooms.add(new MuseumRoom("🌟 CONTEMPORAIN", 780, 300, 180, 140, 
            Color.web("#c0392b"), "EXHIBITION"));
        
        // Salle de repos luxueuse
        rooms.add(new MuseumRoom("☕ SALON VIP", 550, 350, 180, 120, 
            Color.web("#27ae60"), "REST"));
        
        // Couloir vers sortie
        corridors.add(new Corridor(1020, 650, 150, 80, "EXIT_CORRIDOR"));
        
        // Sortie élégante
        rooms.add(new MuseumRoom("🚪 SORTIE", 1150, 700, 150, 120, 
            Color.web("#95a5a6"), "EXIT"));
        
        // Ajouter des œuvres d'art
        addArtworks();
        
        logMessage("🏛️ Musée moderne initialisé avec " + rooms.size() + " espaces et " + 
                  corridors.size() + " passages");
    }
    
    private void addArtworks() {
        // Œuvres dans les salles d'exposition
        artworks.add(new Artwork("Mona Lisa", 400, 580, Color.GOLD));
        artworks.add(new Artwork("La Nuit Étoilée", 630, 580, Color.BLUE));
        artworks.add(new Artwork("Les Demoiselles", 860, 580, Color.PINK));
        artworks.add(new Artwork("Venus de Milo", 400, 350, Color.BEIGE));
        artworks.add(new Artwork("Composition", 860, 350, Color.RED));
    }
    
    private void setupLightingEffects() {
        lightingAnimation = new Timeline(
            new KeyFrame(Duration.seconds(0), new KeyValue(lightIntensityProperty, 1.0)),
            new KeyFrame(Duration.seconds(10), new KeyValue(lightIntensityProperty, 0.7)),
            new KeyFrame(Duration.seconds(20), new KeyValue(lightIntensityProperty, 1.0))
        );
        lightingAnimation.setCycleCount(Timeline.INDEFINITE);
        lightingAnimation.play();
    }
    
    private void startEnhancedGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_000_000) { // 60 FPS
                    updateEnhanced();
                    renderEnhanced();
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
        
        // Mise à jour des statistiques
        scheduler.scheduleAtFixedRate(this::updateStatsDisplay, 1, 2, TimeUnit.SECONDS);
    }
    
    private void updateEnhanced() {
        time += 0.016;
        
        // Mettre à jour les groupes avec comportement amélioré
        tourGroups.forEach(TourGroup::updateEnhanced);
        tourGroups.removeIf(TourGroup::isFinished);
        
        // Mettre à jour les agents avec physique améliorée
        agents.values().forEach(Agent::updateEnhanced);
    }
    
    private void renderEnhanced() {
        // Fond avec dégradé dynamique
        RadialGradient bg = new RadialGradient(
            0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#2c3e50")),
            new Stop(1, Color.web("#34495e"))
        );
        gc.setFill(bg);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Effets d'éclairage ambiant
        renderAmbientLighting();
        
        // Dessiner les éléments du musée
        renderCorridors();
        renderRooms();
        renderArtworks();
        
        // Agents avec rendu amélioré
        tourGroups.forEach(this::renderTourGroupEnhanced);
        agents.values().stream()
              .filter(a -> tourGroups.stream().noneMatch(g -> g.containsAgent(a)))
              .forEach(this::renderAgentEnhanced);
        
        // Interface et HUD
        renderEnhancedInterface();
        renderPathwaysEnhanced();
    }
    
    private void renderAmbientLighting() {
        // Éclairage dynamique basé sur l'heure
        double lightIntensity = lightIntensityProperty.get();
        double alpha = 0.1 + (lightIntensity * 0.2);
        
        RadialGradient ambientLight = new RadialGradient(
            0, 0, CANVAS_WIDTH/2, CANVAS_HEIGHT/2, CANVAS_WIDTH, false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(1, 1, 0.8, alpha)),
            new Stop(1, Color.TRANSPARENT)
        );
        
        gc.setFill(ambientLight);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }
    
    private void renderCorridors() {
        for (Corridor corridor : corridors) {
            // Dégradé pour les couloirs
            LinearGradient corridorGradient;
            if (corridor.width > corridor.height) {
                corridorGradient = new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#95a5a6")),
                    new Stop(0.5, Color.web("#bdc3c7")),
                    new Stop(1, Color.web("#95a5a6"))
                );
            } else {
                corridorGradient = new LinearGradient(
                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#95a5a6")),
                    new Stop(0.5, Color.web("#bdc3c7")),
                    new Stop(1, Color.web("#95a5a6"))
                );
            }
            
            gc.setFill(corridorGradient);
            gc.fillRoundRect(corridor.x, corridor.y, corridor.width, corridor.height, 10, 10);
            
            // Bordure élégante
            gc.setStroke(Color.web("#7f8c8d"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(corridor.x, corridor.y, corridor.width, corridor.height, 10, 10);
            
            // Lignes directrices avec animation
            gc.setStroke(Color.color(1, 1, 1, 0.3 + 0.2 * Math.sin(time * 2)));
            gc.setLineWidth(1);
            
            if (corridor.width > corridor.height) {
                double centerY = corridor.y + corridor.height / 2;
                for (int i = 0; i < corridor.width; i += 30) {
                    double alpha = 0.5 + 0.3 * Math.sin(time + i * 0.1);
                    gc.setStroke(Color.color(1, 1, 1, alpha));
                    gc.strokeLine(corridor.x + i, centerY - 5, corridor.x + i + 15, centerY + 5);
                }
            } else {
                double centerX = corridor.x + corridor.width / 2;
                for (int i = 0; i < corridor.height; i += 30) {
                    double alpha = 0.5 + 0.3 * Math.sin(time + i * 0.1);
                    gc.setStroke(Color.color(1, 1, 1, alpha));
                    gc.strokeLine(centerX - 5, corridor.y + i, centerX + 5, corridor.y + i + 15);
                }
            }
        }
    }
    
    private void renderRooms() {
        for (MuseumRoom room : rooms) {
            // Ombre portée avancée
            gc.setFill(Color.color(0, 0, 0, 0.4));
            gc.fillRoundRect(room.x + 6, room.y + 6, room.width, room.height, 15, 15);
            
            // Dégradé de la salle
            RadialGradient roomGradient = new RadialGradient(
                0, 0, 0.3, 0.3, 0.8, true, CycleMethod.NO_CYCLE,
                new Stop(0, room.color.brighter()),
                new Stop(1, room.color.darker())
            );
            
            gc.setFill(roomGradient);
            gc.fillRoundRect(room.x, room.y, room.width, room.height, 15, 15);
            
            // Bordure avec effet métallique
            LinearGradient borderGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE),
                new Stop(0.5, room.color.darker()),
                new Stop(1, Color.BLACK)
            );
            
            gc.setStroke(borderGradient);
            gc.setLineWidth(3);
            gc.strokeRoundRect(room.x, room.y, room.width, room.height, 15, 15);
            
            // Nom avec style moderne
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            
            // Ombre du texte
            gc.setFill(Color.BLACK);
            gc.fillText(room.name, room.x + 12, room.y + 27);
            gc.setFill(Color.WHITE);
            gc.fillText(room.name, room.x + 10, room.y + 25);
            
            // Icône selon le type
            renderRoomIcon(room);
        }
    }
    
    private void renderRoomIcon(MuseumRoom room) {
        double iconX = room.x + room.width - 40;
        double iconY = room.y + 35;
        
        // Cercle de fond pour l'icône
        gc.setFill(Color.color(0, 0, 0, 0.3));
        gc.fillOval(iconX - 15, iconY - 15, 30, 30);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        String icon = getIconForRoomType(room.type);
        
        // Animation de pulsation pour certaines icônes
        if ("EXHIBITION".equals(room.type)) {
            double scale = 1.0 + 0.1 * Math.sin(time * 3);
            gc.save();
            gc.translate(iconX, iconY);
            gc.scale(scale, scale);
            gc.fillText(icon, -10, 5);
            gc.restore();
        } else {
            gc.fillText(icon, iconX - 10, iconY + 5);
        }
    }
    
    private void renderArtworks() {
        for (Artwork artwork : artworks) {
            // Cadre de l'œuvre
            gc.setFill(Color.web("#8b4513"));
            gc.fillRoundRect(artwork.x - 8, artwork.y - 8, 16, 16, 3, 3);
            
            // Œuvre d'art
            gc.setFill(artwork.color);
            gc.fillRoundRect(artwork.x - 5, artwork.y - 5, 10, 10, 2, 2);
        }
    }
    
    private void renderTourGroupEnhanced(TourGroup group) {
        Agent guide = group.guide;
        List<Agent> tourists = group.tourists;
        
        // Aura du groupe avec dégradé
        RadialGradient groupAura = new RadialGradient(
            0, 0, guide.x, guide.y, 60, false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0.2, 0.6, 1, 0.3)),
            new Stop(1, Color.TRANSPARENT)
        );
        
        gc.setFill(groupAura);
        gc.fillOval(guide.x - 60, guide.y - 60, 120, 120);
        
        // Lignes de connexion fluides avec animation
        gc.setStroke(Color.color(0.3, 0.7, 1, 0.6));
        gc.setLineWidth(2);
        for (Agent tourist : tourists) {
            // Ligne courbe entre guide et touriste
            double controlX = (guide.x + tourist.x) / 2 + 20 * Math.sin(time + tourist.hashCode());
            double controlY = (guide.y + tourist.y) / 2 + 15 * Math.cos(time + tourist.hashCode());
            
            drawCurvedLine(guide.x, guide.y, tourist.x, tourist.y, controlX, controlY);
        }
        
        // Cercle de formation du groupe avec animation
        double groupRadius = 45 + tourists.size() * 3 + 5 * Math.sin(time * 2);
        gc.setStroke(Color.color(0.2, 0.8, 1, 0.7 + 0.3 * Math.sin(time * 3)));
        gc.setLineWidth(3);
        gc.setLineDashes(10, 5);
        gc.strokeOval(guide.x - groupRadius, guide.y - groupRadius, 
                     groupRadius * 2, groupRadius * 2);
        gc.setLineDashes(); // Reset
        
        // Dessiner le guide avec effet spécial
        renderAgentEnhanced(guide, 20, true);
        
        // Dessiner les touristes avec positions naturelles
        for (Agent tourist : tourists) {
            renderAgentEnhanced(tourist, 12, false);
        }
        
        // Information du groupe avec style moderne
        renderGroupInfo(group);
    }
    
    private void drawCurvedLine(double x1, double y1, double x2, double y2, double controlX, double controlY) {
        // Dessiner une courbe de Bézier quadratique
        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.quadraticCurveTo(controlX, controlY, x2, y2);
        gc.stroke();
    }
    
    private void renderGroupInfo(TourGroup group) {
        Agent guide = group.guide;
        
        // Bulle d'information avec glassmorphisme
        double bubbleX = guide.x + 30;
        double bubbleY = guide.y - 50;
        double bubbleWidth = 180;
        double bubbleHeight = 50;
        
        // Ombre de la bulle
        gc.setFill(Color.color(0, 0, 0, 0.2));
        gc.fillRoundRect(bubbleX + 2, bubbleY + 2, bubbleWidth, bubbleHeight, 15, 15);
        
        // Bulle principale
        RadialGradient bubbleGradient = new RadialGradient(
            0, 0, 0.3, 0.3, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0, 0, 0, 0.8)),
            new Stop(1, Color.color(0.1, 0.1, 0.2, 0.9))
        );
        gc.setFill(bubbleGradient);
        gc.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 15, 15);
        
        // Bordure brillante
        gc.setStroke(Color.color(1, 1, 1, 0.4));
        gc.setLineWidth(1);
        gc.strokeRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 15, 15);
        
        // Texte avec effet néon
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setFill(Color.LIGHTBLUE);
        gc.fillText("🎯 " + group.name, bubbleX + 10, bubbleY + 18);
        
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        gc.setFill(Color.LIGHTCYAN);
        gc.fillText("👥 " + group.tourists.size() + " visiteurs", bubbleX + 10, bubbleY + 32);
        gc.fillText("📍 " + group.currentRoom, bubbleX + 10, bubbleY + 44);
    }
    
    private void renderAgentEnhanced(Agent agent) {
        renderAgentEnhanced(agent, agent.isGuide ? 18 : 12, agent.isGuide);
    }
    
    private void renderAgentEnhanced(Agent agent, double size, boolean isGuide) {
        // Trail de mouvement
        if (agent.isMoving()) {
            renderMovementTrail(agent);
        }
        
        // Ombre dynamique
        double shadowOffset = 3 + Math.sin(time * 4) * 0.5;
        gc.setFill(Color.color(0, 0, 0, 0.4));
        gc.fillOval(agent.x - size/2 + shadowOffset, agent.y - size/2 + shadowOffset, size, size);
        
        // Corps de l'agent avec dégradé
        RadialGradient agentGradient = new RadialGradient(
            0, 0, 0.3, 0.3, 0.8, true, CycleMethod.NO_CYCLE,
            new Stop(0, agent.color.brighter().brighter()),
            new Stop(0.7, agent.color),
            new Stop(1, agent.color.darker())
        );
        
        gc.setFill(agentGradient);
        gc.fillOval(agent.x - size/2, agent.y - size/2, size, size);
        
        // Bordure avec effet métallique
        if (isGuide) {
            // Animation de pulsation dorée pour les guides
            double pulseIntensity = 0.8 + 0.4 * Math.sin(time * 5);
            gc.setStroke(Color.color(1, 0.8, 0, pulseIntensity));
            gc.setLineWidth(4);
            gc.strokeOval(agent.x - size/2, agent.y - size/2, size, size);
            
            // Couronne pour les guides
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(2);
            gc.strokeOval(agent.x - size/2 - 3, agent.y - size/2 - 3, size + 6, size + 6);
        } else {
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(agent.x - size/2, agent.y - size/2, size, size);
        }
        
        // Icône avec animation
        renderAgentIcon(agent, size, isGuide);
        
        // Indicateur de direction avec flèche 3D
        if (agent.hasTarget()) {
            renderDirectionIndicator(agent, size);
        }
        
        // État spéciaux (fatigue, intérêt, etc.)
        renderAgentState(agent, size);
    }
    
    private void renderMovementTrail(Agent agent) {
        // Trail de particules de mouvement
        List<Point2D> trailList = new ArrayList<>(agent.trailPositions);
        for (int i = 0; i < trailList.size(); i++) {
            Point2D pos = trailList.get(i);
            double alpha = (double) i / trailList.size() * 0.3;
            double trailSize = 3 + i * 0.5;
            
            gc.setFill(Color.color(
                agent.color.getRed(), 
                agent.color.getGreen(), 
                agent.color.getBlue(), 
                alpha
            ));
            gc.fillOval(pos.x - trailSize/2, pos.y - trailSize/2, trailSize, trailSize);
        }
    }
    
    private void renderAgentIcon(Agent agent, double size, boolean isGuide) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, isGuide ? 12 : 10));
        
        String icon = isGuide ? "🎓" : getIconForTouristType(agent.type);
        
        // Animation de bobbing pour les icônes
        double bobOffset = Math.sin(time * 4 + agent.hashCode()) * 1.5;
        
        gc.save();
        gc.translate(agent.x, agent.y + bobOffset);
        
        if (isGuide) {
            // Rotation légère pour les guides
            gc.rotate(Math.sin(time * 2) * 5);
        }
        
        gc.fillText(icon, -6, 4);
        gc.restore();
    }
    
    private void renderDirectionIndicator(Agent agent, double size) {
        double angle = Math.atan2(agent.targetY - agent.y, agent.targetX - agent.x);
        double arrowLength = size + 8;
        
        // Position de la flèche
        double arrowX = agent.x + Math.cos(angle) * arrowLength;
        double arrowY = agent.y + Math.sin(angle) * arrowLength;
        
        // Flèche 3D avec dégradé
        gc.save();
        gc.translate(arrowX, arrowY);
        gc.rotate(Math.toDegrees(angle));
        
        // Corps de la flèche
        LinearGradient arrowGradient = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, agent.color.brighter()),
            new Stop(1, agent.color.darker())
        );
        
        gc.setFill(arrowGradient);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        
        // Dessiner la flèche
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.lineTo(-8, -4);
        gc.lineTo(-6, 0);
        gc.lineTo(-8, 4);
        gc.closePath();
        gc.fill();
        gc.stroke();
        
        gc.restore();
    }
    
    private void renderAgentState(Agent agent, double size) {
        // États émotionnels/physiques
        if (agent.energy < 0.3) {
            // Fatigue - zigzags au-dessus de la tête
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(2);
            double zigzagY = agent.y - size/2 - 10;
            for (int i = 0; i < 3; i++) {
                double x1 = agent.x - 6 + i * 4;
                double x2 = x1 + 2;
                gc.strokeLine(x1, zigzagY, x2, zigzagY - 3);
                gc.strokeLine(x2, zigzagY - 3, x2 + 2, zigzagY);
            }
        }
        
        if (agent.interest > 0.8) {
            // Intérêt élevé - petite icône
            gc.setFill(Color.YELLOW);
            gc.fillOval(agent.x - 3, agent.y - size/2 - 8, 6, 6);
        }
    }
    
    private void renderPathwaysEnhanced() {
        // Chemin suggéré animé avec particules
        gc.setStroke(Color.color(0.2, 0.8, 0.3, 0.7));
        gc.setLineWidth(4);
        
        // Animation de pointillés fluides
        double dashOffset = (time * 20) % 20;
        gc.setLineDashes(10, 10);
        gc.setLineDashOffset(dashOffset);
        
        Point2D[] mainPath = {
            new Point2D(270, 750),  // Sortie de l'entrée
            new Point2D(410, 750),  // Couloir principal
            new Point2D(410, 690),  // Vers Renaissance
            new Point2D(640, 690),  // Vers Impressionnisme
            new Point2D(870, 690),  // Vers Art Moderne
            new Point2D(640, 470),  // Vers Salon VIP
            new Point2D(1120, 750), // Vers sortie
        };
        
        // Dessiner le chemin avec effet de lueur
        for (int i = 0; i < mainPath.length - 1; i++) {
            // Lueur
            gc.setLineWidth(8);
            gc.setStroke(Color.color(0.2, 0.8, 0.3, 0.3));
            gc.strokeLine(mainPath[i].x, mainPath[i].y, mainPath[i + 1].x, mainPath[i + 1].y);
            
            // Ligne principale
            gc.setLineWidth(4);
            gc.setStroke(Color.color(0.2, 0.8, 0.3, 0.8));
            gc.strokeLine(mainPath[i].x, mainPath[i].y, mainPath[i + 1].x, mainPath[i + 1].y);
        }
        
        gc.setLineDashes(); // Reset
        
        // Points d'intérêt avec animation
        for (Point2D point : mainPath) {
            double pulseSize = 6 + 3 * Math.sin(time * 4 + point.x * 0.01);
            gc.setFill(Color.color(0.3, 1, 0.4, 0.8));
            gc.fillOval(point.x - pulseSize/2, point.y - pulseSize/2, pulseSize, pulseSize);
        }
    }
    
    private void renderEnhancedInterface() {
        // Panneau d'information flottant moderne
        double panelX = 20;
        double panelY = 20;
        double panelWidth = 280;
        double panelHeight = 140;
        
        // Fond glassmorphisme
        RadialGradient panelBg = new RadialGradient(
            0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0, 0, 0, 0.8)),
            new Stop(1, Color.color(0.1, 0.1, 0.2, 0.9))
        );
        
        gc.setFill(panelBg);
        gc.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);
        
        // Bordure lumineuse
        gc.setStroke(Color.color(0.4, 0.8, 1, 0.6 + 0.4 * Math.sin(time)));
        gc.setLineWidth(2);
        gc.strokeRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);
        
        // Titre
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("🎮 CONTRÔLES", panelX + 15, panelY + 25);
        
        // Légende avec icônes animées
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        String[] legends = {
            "🎓 Guide touristique",
            "👥 Groupe de visiteurs", 
            "🏛️ Salles d'exposition",
            "☕ Zone de repos",
            "🌟 Chemin recommandé"
        };
        
        for (int i = 0; i < legends.length; i++) {
            double alpha = 0.8 + 0.2 * Math.sin(time * 2 + i);
            gc.setFill(Color.color(0.8, 1, 0.9, alpha));
            gc.fillText(legends[i], panelX + 15, panelY + 50 + i * 18);
        }
        
        // Instruction interactive
        if (jadeRunning && isNearEntrance(CANVAS_WIDTH/2, CANVAS_HEIGHT/2)) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setFill(Color.YELLOW);
            gc.fillText("💡 Cliquez près de l'entrée pour créer un groupe!", panelX, panelY + 160);
        }
        
        // Mini-carte en temps réel
        renderMiniMap();
    }
    
    private void renderMiniMap() {
        double miniX = CANVAS_WIDTH - 220;
        double miniY = 20;
        double miniWidth = 180;
        double miniHeight = 120;
        double scale = 0.15;
        
        // Fond de la mini-carte
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillRoundRect(miniX, miniY, miniWidth, miniHeight, 10, 10);
        
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(1);
        gc.strokeRoundRect(miniX, miniY, miniWidth, miniHeight, 10, 10);
        
        // Titre
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText("📍 Vue d'ensemble", miniX + 10, miniY + 15);
        
        // Salles simplifiées
        for (MuseumRoom room : rooms) {
            gc.setFill(room.color.darker());
            gc.fillRect(
                miniX + room.x * scale, 
                miniY + 20 + room.y * scale,
                room.width * scale, 
                room.height * scale
            );
        }
        
        // Agents sur la mini-carte
        for (Agent agent : agents.values()) {
            gc.setFill(agent.isGuide ? Color.GOLD : Color.LIGHTBLUE);
            double dotSize = agent.isGuide ? 3 : 2;
            gc.fillOval(
                miniX + agent.x * scale - dotSize/2,
                miniY + 20 + agent.y * scale - dotSize/2,
                dotSize, dotSize
            );
        }
    }
    
    // Système de particules pour effets visuels
    
    private void createParticleEffect(double x, double y, Color color) {
        String key = "effect_" + System.currentTimeMillis();
    }
    
    private void createTourGroupWithAnimation() {
        createSpecificGroup(4 + (int)(Math.random() * 6), "Groupe Animé");
        
        // Effet visuel de création
        createParticleEffect(entrancePoint.x, entrancePoint.y, Color.GOLD);
        
        // Animation de zoom sur l'entrée
        Timeline zoomEffect = new Timeline(
            new KeyFrame(Duration.millis(500), e -> {
                // Flash effet sur le canvas
                gc.setFill(Color.color(1, 1, 1, 0.3));
                gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            })
        );
        zoomEffect.play();
    }
    
    // Actions de contrôle améliorées
private void startJadeSystem() {
        statusLabel.setText("🚀 Démarrage de JADE...");
        statusLabel.setTextFill(Color.ORANGE);
        
        HBox statusPanel = (HBox) statusLabel.getParent();
        Circle statusIndicator = (Circle) statusPanel.getChildren().get(0);
        statusIndicator.setFill(Color.ORANGE);
        
        new Thread(() -> {
            boolean success = SimpleLauncher.startJadeSystem();
            
            Platform.runLater(() -> {
                if (success) {
                    jadeRunning = true;
                    statusLabel.setText("✅ Musée Opérationnel - Prêt à accueillir");
                    statusLabel.setTextFill(Color.LIGHTGREEN);
                    statusIndicator.setFill(Color.LIGHTGREEN);
                    
                    logMessage("🏛️ Système JADE démarré - Musée prêt à accueillir des visiteurs");
                } else {
                    statusLabel.setText("❌ Échec démarrage JADE");
                    statusLabel.setTextFill(Color.LIGHTCORAL);
                    statusIndicator.setFill(Color.RED);
                    
                    logMessage("⚠️ Échec du démarrage de JADE");
                }
            });
        }).start();
    }    

    private void stopJadeSystem() {
        if (!jadeRunning) return;
        
        SimpleLauncher.shutdownSystem();
        jadeRunning = false;
        
        agents.clear();
        tourGroups.clear();
        
        statusLabel.setText("🔒 Musée fermé");
        statusLabel.setTextFill(Color.GRAY);
        
        HBox statusPanel = (HBox) statusLabel.getParent();
        Circle statusIndicator = (Circle) statusPanel.getChildren().get(0);
        statusIndicator.setFill(Color.GRAY);
        
        logMessage("🏛️ Système JADE arrêté - Musée fermé au public");
    }
    
    private void createTourGroup() {
        if (!jadeRunning) {
            logMessage("⚠️ Le musée doit être ouvert (JADE démarré) pour créer des groupes");
            return;
        }
        
        createSpecificGroup(4 + (int)(Math.random() * 6), "Visite Standard");
    }
    
    private void createSpecificGroup(int size, String type) {
        if (!jadeRunning) return;
        
        String groupName = type + "_" + (tourGroups.size() + 1);
        
        // Créer le guide avec animation d'apparition
        String guideName = "Guide_" + groupName;
        Agent guide = new Agent(guideName, 
            entrancePoint.x + 40 + Math.random() * 20, 
            entrancePoint.y - 30 + Math.random() * 20, 
            true
        );
        guide.color = Color.web("#e74c3c");
        guide.type = "GUIDE";
        agents.put(guideName, guide);
        
        // Créer les touristes avec formation naturelle
        List<Agent> tourists = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String touristName = "Tourist_" + groupName + "_" + (i + 1);
            
            // Formation en arc de cercle autour du guide
            double angle = (double) i / size * Math.PI + Math.PI/2;
            double radius = 40 + Math.random() * 20;
            double x = guide.x + Math.cos(angle) * radius;
            double y = guide.y + Math.sin(angle) * radius;
            
            Agent tourist = new Agent(touristName, x, y, false);
            tourist.color = getTouristColor(type);
            tourist.type = getTouristType(type, i);
            tourist.energy = 0.8 + Math.random() * 0.2;
            tourist.interest = 0.5 + Math.random() * 0.5;
            
            tourists.add(tourist);
            agents.put(touristName, tourist);
            
            // Ajouter l'agent JADE
            SimpleLauncher.addTourist(touristName);
        }
        
        // Créer le groupe avec IA améliorée
        TourGroup group = new TourGroup(groupName, guide, tourists, rooms);
        group.groupType = type;
        tourGroups.add(group);
        
        logMessage("🎯 Nouveau " + type.toLowerCase() + " créé: " + groupName + 
                  " avec " + size + " visiteurs");
    }
    
    // Méthodes utilitaires améliorées
    
    private Color getTouristColor(String groupType) {
        switch (groupType) {
            case "Famille": return Color.web("#3498db");
            case "École": return Color.web("#f39c12");
            case "VIP": return Color.web("#9b59b6");
            case "Art": return Color.web("#e74c3c");
            default: return Color.web("#2ecc71");
        }
    }
    
    private String getTouristType(String groupType, int index) {
        switch (groupType) {
            case "Famille":
                return (index < 2) ? "ADULT" : "CHILD";
            case "École":
                return (index == 0) ? "TEACHER" : "STUDENT";
            case "VIP":
                return "VIP";
            default:
                return "STANDARD";
        }
    }
    
    private String getIconForTouristType(String type) {
        switch (type) {
            case "ADULT": return "👨";
            case "CHILD": return "🧒";
            case "TEACHER": return "👩‍🏫";
            case "STUDENT": return "🎓";
            case "VIP": return "⭐";
            case "GUIDE": return "🎯";
            default: return "👤";
        }
    }
    
    private boolean isNearEntrance(double x, double y) {
        return Math.abs(x - entrancePoint.x) < 120 && Math.abs(y - entrancePoint.y) < 100;
    }
    
    private String getIconForRoomType(String type) {
        switch (type) {
            case "ENTRANCE": return "🏛️";
            case "EXHIBITION": return "🎨";
            case "REST": return "☕";
            case "EXIT": return "🚪";
            default: return "📍";
        }
    }
    
    private void updateStatsDisplay() {
        Platform.runLater(() -> {
            int guides = (int) agents.values().stream().filter(a -> a.isGuide).count();
            int tourists = (int) agents.values().stream().filter(a -> !a.isGuide).count();
            
            statsLabel.setText(String.format(
                "📊 STATISTIQUES LIVE\n" +
                "━━━━━━━━━━━━━━━━━━━━\n" +
                "🎯 Groupes actifs: %d\n" +
                "🎓 Guides: %d\n" +
                "👥 Visiteurs: %d\n" +
                "🏛️ Salles: %d\n" +
                "⏱️ Temps: %.0fs\n" +
                "💡 État: %s",
                tourGroups.size(), guides, tourists, rooms.size(), 
                time,
                jadeRunning ? "ACTIF" : "INACTIF"
            ));
        });
    }
    
    private void logMessage(String message) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.appendText("▶ [" + timestamp + "] " + message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    private void cleanup() {
        if (gameLoop != null) gameLoop.stop();
        if (lightingAnimation != null) lightingAnimation.stop();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        SimpleLauncher.shutdownSystem();
    }
    
    // Classes internes améliorées
    
    public static class Agent {
        String name, type;
        double x, y;
        double targetX, targetY;
        boolean isGuide;
        Color color;
        double speed, energy, interest;
        double rotation = 0;
        Queue<Point2D> trailPositions = new LinkedList<>();
        
        Agent(String name, double x, double y, boolean isGuide) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.targetX = x;
            this.targetY = y;
            this.isGuide = isGuide;
            this.speed = isGuide ? 1.8 : 2.2 + Math.random() * 0.6;
            this.energy = 1.0;
            this.interest = 0.5;
            this.type = isGuide ? "GUIDE" : "TOURIST";
        }
        
        void updateEnhanced() {
            // Mise à jour du trail
            trailPositions.offer(new Point2D(x, y));
            if (trailPositions.size() > 8) {
                trailPositions.poll();
            }
            
            // Mouvement fluide avec courbes
            double dx = targetX - x;
            double dy = targetY - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance > 2) {
                // Mouvement avec inertie et variation naturelle
                double moveX = (dx / distance) * speed;
                double moveY = (dy / distance) * speed;
                
                // Variation naturelle du mouvement
                double variation = Math.sin(System.currentTimeMillis() * 0.001 + hashCode()) * 0.5;
                moveX += variation * 0.3;
                moveY += variation * 0.2;
                
                x += moveX;
                y += moveY;
                
                // Mise à jour de la rotation pour l'orientation
                rotation = Math.atan2(dy, dx);
            }
            
            // Dégradation naturelle de l'énergie
            energy = Math.max(0.1, energy - 0.0001);
            
            // Variation de l'intérêt
            interest += (Math.random() - 0.5) * 0.01;
            interest = Math.max(0, Math.min(1, interest));
        }
        
        boolean isMoving() {
            return Math.abs(targetX - x) > 2 || Math.abs(targetY - y) > 2;
        }
        
        boolean hasTarget() {
            return targetX != x || targetY != y;
        }
        
        void setTarget(double x, double y) {
            this.targetX = x;
            this.targetY = y;
        }
    }
    
    public static class TourGroup {
        String name, groupType, currentRoom;
        Agent guide;
        List<Agent> tourists;
        List<MuseumRoom> rooms;
        int currentRoomIndex = 0;
        double timeInRoom = 0;
        double groupCohesion = 1.0;
        boolean isFinished = false;
        
        TourGroup(String name, Agent guide, List<Agent> tourists, List<MuseumRoom> rooms) {
            this.name = name;
            this.guide = guide;
            this.tourists = tourists;
            this.rooms = rooms;
            this.currentRoom = "Hall d'Accueil";
            this.groupType = "STANDARD";
            
            scheduleNextMove();
        }
        
        void updateEnhanced() {
            timeInRoom += 0.016;
            
            // Comportement de groupe intelligent avec formation dynamique
            updateGroupFormation();
            
            // Gestion de la cohésion du groupe
            updateGroupCohesion();
            
            // Transition vers la prochaine salle
            if (shouldMoveToNextRoom()) {
                moveToNextRoom();
            }
            
            // Gestion des états individuels
            updateIndividualStates();
        }
        
        private void updateGroupFormation() {
            // Formation en V ou en ligne selon l'espace
            for (int i = 0; i < tourists.size(); i++) {
                Agent tourist = tourists.get(i);
                
                // Position relative basée sur le type de formation
                double formationAngle = getFormationAngle(i);
                double formationDistance = getFormationDistance(i);
                
                // Variation naturelle de position
                double naturalVariation = Math.sin(timeInRoom + i) * 8;
                
                double targetX = guide.x + Math.cos(formationAngle) * formationDistance + naturalVariation;
                double targetY = guide.y + Math.sin(formationAngle) * formationDistance + naturalVariation;
                
                // Évitement des murs et obstacles
                targetX = Math.max(80, Math.min(CANVAS_WIDTH - 80, targetX));
                targetY = Math.max(80, Math.min(CANVAS_HEIGHT - 80, targetY));
                
                tourist.setTarget(targetX, targetY);
            }
        }
        
        private double getFormationAngle(int touristIndex) {
            switch (groupType) {
                case "École":
                    // Formation en ligne double
                    return Math.PI + (touristIndex % 2 == 0 ? -0.3 : 0.3);
                case "Famille":
                    // Formation en arc familial
                    return Math.PI - 0.5 + (double) touristIndex / tourists.size();
                case "VIP":
                    // Formation circulaire élégante
                    return touristIndex * 2 * Math.PI / tourists.size() + Math.PI;
                default:
                    // Formation standard en V
                    return Math.PI + (touristIndex % 2 == 0 ? -0.4 : 0.4) * (1 + touristIndex / 4);
            }
        }
        
        private double getFormationDistance(int touristIndex) {
            double baseDistance = 35 + tourists.size() * 2;
            
            switch (groupType) {
                case "École":
                    return baseDistance + (touristIndex / 2) * 15;
                case "VIP":
                    return baseDistance - 10;
                default:
                    return baseDistance + (touristIndex / 2) * 8;
            }
        }
        
        private void updateGroupCohesion() {
            // Calculer la cohésion basée sur la dispersion
            double averageDistance = tourists.stream()
                .mapToDouble(t -> Math.sqrt(Math.pow(t.x - guide.x, 2) + Math.pow(t.y - guide.y, 2)))
                .average().orElse(50);
            
            groupCohesion = Math.max(0.1, Math.min(1.0, 80.0 / averageDistance));
            
            // Ajuster la vitesse du guide basée sur la cohésion
            guide.speed = 1.5 + (1 - groupCohesion) * 0.5;
        }
        
        private boolean shouldMoveToNextRoom() {
            double minTimeInRoom = getMinTimeInRoom();
            return timeInRoom > minTimeInRoom && 
                   currentRoomIndex < rooms.size() - 1 &&
                   groupCohesion > 0.6;
        }
        
        private double getMinTimeInRoom() {
            switch (groupType) {
                case "École": return 12.0; // Plus de temps éducatif
                case "VIP": return 6.0;    // Visite plus rapide
                case "Art": return 15.0;   // Beaucoup de temps pour l'appréciation
                default: return 8.0;
            }
        }
        
        private void updateIndividualStates() {
            for (Agent tourist : tourists) {
                // Fatigue progressive
                if ("CHILD".equals(tourist.type)) {
                    tourist.energy -= 0.0003; // Les enfants se fatiguent plus vite
                } else {
                    tourist.energy -= 0.0001;
                }
                
                // Intérêt basé sur le type de salle et le type de touriste
                if (currentRoom.contains("ART") && "Art".equals(groupType)) {
                    tourist.interest = Math.min(1.0, tourist.interest + 0.001);
                }
                
                // Restauration d'énergie dans la salle de repos
                if ("REST".equals(getCurrentRoomType())) {
                    tourist.energy = Math.min(1.0, tourist.energy + 0.002);
                }
            }
        }
        
        void scheduleNextMove() {
            if (currentRoomIndex < rooms.size()) {
                MuseumRoom targetRoom = rooms.get(currentRoomIndex);
                
                if ("EXHIBITION".equals(targetRoom.type) || "REST".equals(targetRoom.type)) {
                    double targetX = targetRoom.x + targetRoom.width / 2;
                    double targetY = targetRoom.y + targetRoom.height / 2;
                    
                    // Variation basée sur le type de groupe
                    if ("École".equals(groupType)) {
                        targetX -= 20; // Position plus vers l'avant pour l'enseignant
                    } else if ("VIP".equals(groupType)) {
                        targetX += 10; // Position centrale privilégiée
                    }
                    
                    guide.setTarget(targetX, targetY);
                    currentRoom = targetRoom.name;
                }
            }
        }
        
        void moveToNextRoom() {
            currentRoomIndex++;
            timeInRoom = 0;
            
            if (currentRoomIndex >= rooms.size()) {
                // Fin de visite avec animation
                guide.setTarget(1200, 750);
                currentRoom = "Sortie";
                
                // Programmer la fin du groupe
                Timer finishTimer = new Timer();
                finishTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isFinished = true;
                    }
                }, 8000);
            } else {
                scheduleNextMove();
            }
        }
        
        private String getCurrentRoomType() {
            if (currentRoomIndex < rooms.size()) {
                return rooms.get(currentRoomIndex).type;
            }
            return "UNKNOWN";
        }
        
        boolean containsAgent(Agent agent) {
            return guide.equals(agent) || tourists.contains(agent);
        }
        
        boolean isFinished() {
            return isFinished;
        }
    }
    
    public static class MuseumRoom {
        String name, type;
        double x, y, width, height;
        Color color;
        double temperature = 22.0; // Température ambiante
        double crowdingLevel = 0.0; // Niveau d'encombrement
        
        MuseumRoom(String name, double x, double y, double width, double height, Color color, String type) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.type = type;
        }
    }
    
    public static class Corridor {
        double x, y, width, height;
        String type;
        
        Corridor(double x, double y, double width, double height, String type) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
        }
    }
    
    public static class Artwork {
        String name;
        double x, y;
        Color color;
        double value = Math.random() * 1000000; // Valeur artistique
        
        Artwork(String name, double x, double y, Color color) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
    
    public static class Point2D {
        double x, y;
        
        Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    
    // Stub pour SimpleLauncher (à remplacer par votre implémentation)
    public static class SimpleLauncher {
        public static boolean startJadeSystem() {
            // Simulation du démarrage JADE
            try {
                Thread.sleep(1000);
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        }
        
        public static void shutdownSystem() {
            // Simulation de l'arrêt JADE
        }
        
        public static void addTourist(String name) {
            // Simulation d'ajout d'agent touriste
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}