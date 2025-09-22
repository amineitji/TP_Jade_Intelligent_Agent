import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CoordinatorAgent extends Agent {
    // Gestion des agents du système
    private Set<AID> guides = new HashSet<>();
    private Set<AID> tourists = new HashSet<>();
    private Queue<AID> waitingTourists = new LinkedList<>(); // File d'attente des touristes
    private Map<AID, GuideStatus> guideStatus = new ConcurrentHashMap<>();
    
    // État du musée
    private Map<String, Integer> locationOccupancy = new ConcurrentHashMap<>();
    private Map<String, Boolean> tableauAvailability = new ConcurrentHashMap<>();
    
    // Statistiques globales
    private int totalVisitors = 0;
    private int completedTours = 0;
    private int activeGroups = 0;
    private double averageMuseumSatisfaction = 0.5;
    private Map<String, Integer> popularityStats = new ConcurrentHashMap<>();
    
    // Configuration du musée
    private final int MAX_CAPACITY_PER_LOCATION = 15;
    private final int MIN_GROUP_SIZE = 3;
    private final int MAX_GROUP_SIZE = 8;
    private final String[] MUSEUM_LOCATIONS = {
        "PointA", "Tableau1", "Tableau2", "Tableau3", "Tableau4", "Tableau5",
        "SalleRepos", "Sortie"
    };
    
    protected void setup() {
        System.out.println("Agent Coordinateur démarré - Gestion du musée intelligente avec recyclage");
        
        // Initialisation de l'état du musée
        initializeMuseum();
        
        // Enregistrement du service
        registerService();
        
        // Ajout des comportements
        addBehaviour(new AgentRegistrationHandler());
        addBehaviour(new GuideReportHandler());
        addBehaviour(new TourCompletionHandler());
        addBehaviour(new GroupFormationManager());
        addBehaviour(new ResourceOptimizer());
        addBehaviour(new StatisticsCollector());
        addBehaviour(new MuseumMonitor());
        
        System.out.println("Coordinateur prêt - Capacité max par zone: " + MAX_CAPACITY_PER_LOCATION);
    }
    
    private void initializeMuseum() {
        // Initialiser les occupations
        for (String location : MUSEUM_LOCATIONS) {
            locationOccupancy.put(location, 0);
        }
        
        // Tous les tableaux disponibles au début
        for (int i = 1; i <= 5; i++) {
            tableauAvailability.put("Tableau" + i, true);
        }
        
        // Initialiser les statistiques de popularité
        for (int i = 1; i <= 5; i++) {
            popularityStats.put("Tableau" + i, 0);
        }
    }
    
    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("coordinator-service");
        sd.setName("museum-coordinator");
        dfd.addServices(sd);
        
        try {
            DFService.register(this, dfd);
            System.out.println("Coordinateur enregistré dans les Pages Jaunes");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    // Gestion de l'enregistrement des agents
    private class AgentRegistrationHandler extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                if ("REGISTER_GUIDE".equals(content)) {
                    registerGuide(msg.getSender());
                } else if ("REGISTER_TOURIST".equals(content)) {
                    registerTourist(msg.getSender());
                }
            } else {
                block();
            }
        }
    }
    
    // Traitement des rapports des guides
    private class GuideReportHandler extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);
            
            if (msg != null && msg.getContent().startsWith("REPORT:")) {
                processGuideReport(msg);
            } else {
                block();
            }
        }
    }
    
    // NOUVEAU : Gestionnaire de fin de visite et recyclage
    private class TourCompletionHandler extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                if (content.startsWith("TOUR_COMPLETED:")) {
                    handleTourCompletion(msg);
                } else if (content.startsWith("GUIDE_AVAILABLE:")) {
                    handleGuideAvailable(msg);
                } else {
                    // Remettre le message dans la queue s'il ne nous concerne pas
                    putBack(msg);
                }
            } else {
                block();
            }
        }
    }
    
    // NOUVEAU : Gestionnaire de formation de groupes
    private class GroupFormationManager extends TickerBehaviour {
        public GroupFormationManager() {
            super(CoordinatorAgent.this, 10000); // Vérifier toutes les 10 secondes
        }
        
        protected void onTick() {
            formNewGroups();
            redistributeTourists();
        }
    }
    
    // Optimisation des ressources avec logique de recyclage
    private class ResourceOptimizer extends TickerBehaviour {
        public ResourceOptimizer() {
            super(CoordinatorAgent.this, 15000); // Optimisation toutes les 15 secondes
        }
        
        protected void onTick() {
            optimizeResourceAllocation();
            manageMuseumFlow();
            checkForBottlenecks();
            balanceGuideWorkload(); // NOUVEAU
        }
    }
    
    // Collection de statistiques améliorée
    private class StatisticsCollector extends TickerBehaviour {
        public StatisticsCollector() {
            super(CoordinatorAgent.this, 30000); // Statistiques toutes les 30 secondes
        }
        
        protected void onTick() {
            collectStatistics();
            generateReports();
            notifyGUIUpdate(); // NOUVEAU : synchronisation GUI
        }
    }
    
    // Surveillance générale du musée
    private class MuseumMonitor extends TickerBehaviour {
        public MuseumMonitor() {
            super(CoordinatorAgent.this, 10000); // Surveillance toutes les 10 secondes
        }
        
        protected void onTick() {
            monitorMuseumState();
            handleEmergencies();
            updateGlobalMetrics();
        }
    }
    
    private void registerGuide(AID guideAID) {
        guides.add(guideAID);
        GuideStatus status = new GuideStatus();
        status.isAvailable = true; // NOUVEAU : statut de disponibilité
        guideStatus.put(guideAID, status);
        
        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
        reply.addReceiver(guideAID);
        reply.setContent("REGISTRATION_CONFIRMED");
        send(reply);
        
        System.out.println("Coordinateur: Guide " + guideAID.getLocalName() + " enregistré (" + guides.size() + " guides actifs)");
        
        // Tentative immédiate de formation de groupe
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                tryAssignTouristsToGuide(guideAID);
            }
        });
    }
    
    private void registerTourist(AID touristAID) {
        tourists.add(touristAID);
        waitingTourists.offer(touristAID); // Ajouter à la file d'attente
        totalVisitors++;
        
        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
        reply.addReceiver(touristAID);
        reply.setContent("WELCOME_MUSEUM");
        send(reply);
        
        System.out.println("Coordinateur: Touriste " + touristAID.getLocalName() + " enregistré et ajouté à la file d'attente (Total visiteurs: " + totalVisitors + ")");
        
        // Tentative immédiate d'assignation
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                tryFormNewGroup();
            }
        });
    }
    
    // NOUVEAU : Gestion de la fin de visite
    private void handleTourCompletion(ACLMessage msg) {
        AID guideAID = msg.getSender();
        String content = msg.getContent();
        String[] parts = content.split(":");
        
        if (parts.length >= 2) {
            int groupSize = Integer.parseInt(parts[1]);
            GuideStatus status = guideStatus.get(guideAID);
            
            if (status != null) {
                // Marquer le guide comme disponible
                status.isAvailable = true;
                status.groupSize = 0;
                status.currentLocation = "PointA";
                status.completedTours++;
                
                completedTours++;
                activeGroups = Math.max(0, activeGroups - 1);
                
                System.out.println("Coordinateur: Guide " + guideAID.getLocalName() + 
                                 " a terminé une visite (Groupe de " + groupSize + " personnes)");
                
                // Confirmer la réception et préparer le recyclage
                ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                reply.addReceiver(guideAID);
                reply.setContent("TOUR_COMPLETION_ACKNOWLEDGED");
                send(reply);
                
                // Programmer l'assignation de nouveaux touristes après une courte pause
                addBehaviour(new WakerBehaviour(this, 3000) {
                    protected void onWake() {
                        tryAssignTouristsToGuide(guideAID);
                    }
                });
            }
        }
    }
    
    // NOUVEAU : Gestion de la disponibilité des guides
    private void handleGuideAvailable(ACLMessage msg) {
        AID guideAID = msg.getSender();
        GuideStatus status = guideStatus.get(guideAID);
        
        if (status != null) {
            status.isAvailable = true;
            status.groupSize = 0;
            status.currentLocation = "PointA";
            
            System.out.println("Coordinateur: Guide " + guideAID.getLocalName() + " signale sa disponibilité");
            
            // Tentative d'assignation immédiate
            addBehaviour(new OneShotBehaviour() {
                public void action() {
                    tryAssignTouristsToGuide(guideAID);
                }
            });
        }
    }
    
    // NOUVEAU : Formation intelligente de groupes
    private void formNewGroups() {
        if (waitingTourists.isEmpty()) return;
        
        // Trouver un guide disponible
        AID availableGuide = findAvailableGuide();
        if (availableGuide == null) return;
        
        // Vérifier s'il y a assez de touristes
        if (waitingTourists.size() >= MIN_GROUP_SIZE) {
            tryAssignTouristsToGuide(availableGuide);
        }
    }
    
    // NOUVEAU : Tentative d'assignation de touristes à un guide
    private void tryAssignTouristsToGuide(AID guideAID) {
        GuideStatus status = guideStatus.get(guideAID);
        if (status == null || !status.isAvailable || waitingTourists.isEmpty()) {
            return;
        }
        
        // Déterminer la taille du groupe
        int groupSize = Math.min(waitingTourists.size(), MAX_GROUP_SIZE);
        if (groupSize < MIN_GROUP_SIZE && waitingTourists.size() < MIN_GROUP_SIZE) {
            return; // Pas assez de touristes, attendre
        }
        
        groupSize = Math.max(groupSize, MIN_GROUP_SIZE);
        
        // Assigner les touristes au guide
        List<AID> assignedTourists = new ArrayList<>();
        for (int i = 0; i < groupSize && !waitingTourists.isEmpty(); i++) {
            assignedTourists.add(waitingTourists.poll());
        }
        
        if (!assignedTourists.isEmpty()) {
            // Marquer le guide comme occupé
            status.isAvailable = false;
            status.groupSize = assignedTourists.size();
            status.assignedTourists = new ArrayList<>(assignedTourists);
            activeGroups++;
            
            // Informer le guide de ses nouveaux touristes
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(guideAID);
            
            StringBuilder touristList = new StringBuilder("ASSIGN_TOURISTS:");
            for (AID tourist : assignedTourists) {
                touristList.append(tourist.getLocalName()).append(",");
            }
            
            msg.setContent(touristList.toString());
            send(msg);
            
            // Informer les touristes de leur guide
            for (AID tourist : assignedTourists) {
                ACLMessage touristMsg = new ACLMessage(ACLMessage.INFORM);
                touristMsg.addReceiver(tourist);
                touristMsg.setContent("ASSIGNED_TO_GUIDE:" + guideAID.getLocalName());
                send(touristMsg);
            }
            
            System.out.println("Coordinateur: Groupe de " + assignedTourists.size() + 
                             " touristes assigné au guide " + guideAID.getLocalName());
            
            notifyGUIUpdate();
        }
    }
    
    // NOUVEAU : Trouver un guide disponible avec logique intelligente
    private AID findAvailableGuide() {
        AID bestGuide = null;
        double bestScore = -1.0;
        
        for (Map.Entry<AID, GuideStatus> entry : guideStatus.entrySet()) {
            GuideStatus status = entry.getValue();
            
            if (status.isAvailable) {
                // Score basé sur l'efficacité et l'équité
                double score = status.averageSatisfaction * 0.6 + 
                              (1.0 - status.averageFatigue) * 0.3 +
                              (1.0 / (status.completedTours + 1)) * 0.1; // Favoriser les guides moins utilisés
                
                if (score > bestScore) {
                    bestScore = score;
                    bestGuide = entry.getKey();
                }
            }
        }
        
        return bestGuide;
    }
    
    // NOUVEAU : Tentative de formation de nouveau groupe
    private void tryFormNewGroup() {
        if (waitingTourists.size() >= MIN_GROUP_SIZE) {
            AID availableGuide = findAvailableGuide();
            if (availableGuide != null) {
                tryAssignTouristsToGuide(availableGuide);
            }
        }
    }
    
    // NOUVEAU : Redistribution intelligente des touristes
    private void redistributeTourists() {
        // Si des touristes attendent trop longtemps, essayer de les redistribuer
        if (waitingTourists.size() >= MIN_GROUP_SIZE * 2) {
            System.out.println("Coordinateur: File d'attente importante (" + waitingTourists.size() + 
                             " touristes), tentative de redistribution");
            
            AID availableGuide = findAvailableGuide();
            if (availableGuide != null) {
                tryAssignTouristsToGuide(availableGuide);
            }
        }
    }
    
    // NOUVEAU : Équilibrage de la charge de travail des guides
    private void balanceGuideWorkload() {
        if (guides.size() < 2) return;
        
        GuideStatus mostWorkedGuide = null;
        GuideStatus leastWorkedGuide = null;
        int maxTours = -1;
        int minTours = Integer.MAX_VALUE;
        
        for (GuideStatus status : guideStatus.values()) {
            if (status.completedTours > maxTours) {
                maxTours = status.completedTours;
                mostWorkedGuide = status;
            }
            if (status.completedTours < minTours) {
                minTours = status.completedTours;
                leastWorkedGuide = status;
            }
        }
        
        // Si la différence est trop importante, privilégier le guide moins utilisé
        if (mostWorkedGuide != null && leastWorkedGuide != null && (maxTours - minTours) > 2) {
            System.out.println("Coordinateur: Équilibrage nécessaire - Écart de " + (maxTours - minTours) + " visites");
        }
    }
    
    // NOUVEAU : Notification de mise à jour GUI
    private void notifyGUIUpdate() {
        // Cette méthode peut être étendue pour communiquer avec l'interface graphique
        System.out.println("GUI_UPDATE:STATS:" + guides.size() + ":" + tourists.size() + 
                         ":" + activeGroups + ":" + waitingTourists.size());
    }
    
    // Reste du code existant avec quelques améliorations...
    
    private void processGuideReport(ACLMessage msg) {
        String content = msg.getContent();
        // Format: "REPORT:location:groupSize:currentTableau:satisfaction:fatigue"
        String[] parts = content.split(":");
        
        if (parts.length >= 6) {
            AID guideAID = msg.getSender();
            String location = parts[1];
            int groupSize = Integer.parseInt(parts[2]);
            int currentTableau = Integer.parseInt(parts[3]);
            double satisfaction = Double.parseDouble(parts[4]);
            double fatigue = Double.parseDouble(parts[5]);
            
            // Mettre à jour le statut du guide
            GuideStatus status = guideStatus.get(guideAID);
            if (status != null) {
                status.currentLocation = location;
                status.groupSize = groupSize;
                status.currentTableau = currentTableau;
                status.averageSatisfaction = satisfaction;
                status.averageFatigue = fatigue;
                status.lastReportTime = System.currentTimeMillis();
            }
            
            // Mettre à jour l'occupation des lieux
            updateLocationOccupancy(location, groupSize);
            
            // Mettre à jour les statistiques de popularité
            if (location.startsWith("Tableau")) {
                popularityStats.put(location, popularityStats.get(location) + 1);
            }
            
            // Vérifier s'il faut donner des recommandations
            if (satisfaction < 0.3 || fatigue > 0.8) {
                sendGuidanceToGuide(guideAID, satisfaction, fatigue);
            }
            
            // Notification GUI
            notifyGUIUpdate();
        }
    }
    
    // Classe interne pour stocker le statut des guides (améliorée)
    private class GuideStatus {
        String currentLocation = "PointA";
        String previousLocation = null;
        int groupSize = 0;
        int currentTableau = 0;
        double averageSatisfaction = 0.5;
        double averageFatigue = 0.0;
        long lastReportTime = System.currentTimeMillis();
        
        // NOUVEAUX champs
        boolean isAvailable = true;
        int completedTours = 0;
        List<AID> assignedTourists = new ArrayList<>();
        String specialization = "";
        
        @Override
        public String toString() {
            return String.format("Status[%s, groupe:%d, tableau:%d, sat:%.2f, fatigue:%.2f, disponible:%s, tours:%d]",
                currentLocation, groupSize, currentTableau, averageSatisfaction, averageFatigue, 
                isAvailable, completedTours);
        }
    }
    
    // Amélioration des statistiques
    private void collectStatistics() {
        activeGroups = 0;
        double totalSatisfaction = 0.0;
        int satisfactionCount = 0;
        
        for (GuideStatus status : guideStatus.values()) {
            if (status.groupSize > 0 && !status.isAvailable) {
                activeGroups++;
                totalSatisfaction += status.averageSatisfaction;
                satisfactionCount++;
            }
        }
        
        if (satisfactionCount > 0) {
            averageMuseumSatisfaction = totalSatisfaction / satisfactionCount;
        }
    }
    
    private void generateReports() {
        System.out.println("\n=== RAPPORT COORDINATEUR AVANCÉ ===");
        System.out.println("Guides actifs: " + guides.size());
        System.out.println("Guides disponibles: " + countAvailableGuides());
        System.out.println("Touristes enregistrés: " + tourists.size());
        System.out.println("Touristes en attente: " + waitingTourists.size());
        System.out.println("Groupes en visite: " + activeGroups);
        System.out.println("Visites complétées: " + completedTours);
        System.out.println("Satisfaction moyenne musée: " + String.format("%.2f", averageMuseumSatisfaction));
        
        // Statistiques des guides
        System.out.println("\nStatut des guides:");
        for (Map.Entry<AID, GuideStatus> entry : guideStatus.entrySet()) {
            GuideStatus status = entry.getValue();
            System.out.println("  " + entry.getKey().getLocalName() + ": " + status);
        }
        
        System.out.println("=====================================\n");
    }
    
    private int countAvailableGuides() {
        return (int) guideStatus.values().stream().filter(status -> status.isAvailable).count();
    }
    
    // Autres méthodes existantes restent identiques...
    private void updateLocationOccupancy(String location, int groupSize) {
        locationOccupancy.put(location, groupSize);
        
        if (groupSize > MAX_CAPACITY_PER_LOCATION * 0.8) {
            System.out.println("Coordinateur: ATTENTION - Zone " + location + " proche de la capacité maximale (" + groupSize + "/" + MAX_CAPACITY_PER_LOCATION + ")");
        }
    }
    
    private void sendGuidanceToGuide(AID guideAID, double satisfaction, double fatigue) {
        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
        msg.addReceiver(guideAID);
        
        String guidance = "";
        if (satisfaction < 0.3) {
            guidance = "GUIDANCE:LOW_SATISFACTION:Considérez adapter votre approche pédagogique";
        } else if (fatigue > 0.8) {
            guidance = "GUIDANCE:HIGH_FATIGUE:Proposez une pause dans la SalleRepos";
        }
        
        if (!guidance.isEmpty()) {
            msg.setContent(guidance);
            send(msg);
            System.out.println("Coordinateur envoie conseil au guide " + guideAID.getLocalName() + ": " + guidance);
        }
    }
    
    private void optimizeResourceAllocation() {
        // Logique d'optimisation existante...
        AID bestGuide = null;
        double bestPerformance = 0.0;
        
        for (Map.Entry<AID, GuideStatus> entry : guideStatus.entrySet()) {
            GuideStatus status = entry.getValue();
            if (status.isAvailable) continue; // Skip available guides
            
            double performance = status.averageSatisfaction * (1.0 - status.averageFatigue);
            
            if (performance > bestPerformance) {
                bestPerformance = performance;
                bestGuide = entry.getKey();
            }
        }
        
        if (bestGuide != null && bestPerformance > 0.7) {
            System.out.println("Coordinateur: Meilleure performance actuelle - Guide " + bestGuide.getLocalName() + " (" + String.format("%.2f", bestPerformance) + ")");
        }
    }
    
    private void manageMuseumFlow() {
        // Détecter les zones d'engorgement
        for (Map.Entry<String, Integer> entry : locationOccupancy.entrySet()) {
            String location = entry.getKey();
            int occupancy = entry.getValue();
            
            if (occupancy > MAX_CAPACITY_PER_LOCATION * 0.9) {
                recommendAlternativeRoutes(location);
            }
        }
    }
    
    private void recommendAlternativeRoutes(String congestedLocation) {
        // Envoyer des recommandations aux guides pour éviter la zone congestionnée
        for (AID guideAID : guides) {
            GuideStatus status = guideStatus.get(guideAID);
            if (status != null && !status.currentLocation.equals(congestedLocation) && !status.isAvailable) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(guideAID);
                msg.setContent("ROUTE_ADVICE:AVOID:" + congestedLocation + ":CONGESTION");
                send(msg);
            }
        }
        
        System.out.println("Coordinateur: Recommandation d'évitement pour " + congestedLocation + " (congestion)");
    }
    
    private void checkForBottlenecks() {
        // Analyser les patterns de circulation
        Map<String, Integer> transitionCount = new HashMap<>();
        
        for (GuideStatus status : guideStatus.values()) {
            if (status.previousLocation != null && status.currentLocation != null) {
                String transition = status.previousLocation + "->" + status.currentLocation;
                transitionCount.put(transition, transitionCount.getOrDefault(transition, 0) + 1);
            }
            status.previousLocation = status.currentLocation;
        }
        
        for (Map.Entry<String, Integer> entry : transitionCount.entrySet()) {
            if (entry.getValue() > 3) {
                System.out.println("Coordinateur: Transition fréquente détectée: " + entry.getKey() + " (" + entry.getValue() + " occurrences)");
            }
        }
    }
    
    private void monitorMuseumState() {
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<AID, GuideStatus> entry : guideStatus.entrySet()) {
            GuideStatus status = entry.getValue();
            if (!status.isAvailable && currentTime - status.lastReportTime > 60000) {
                System.out.println("Coordinateur: ALERTE - Pas de nouvelles du guide " + 
                                 entry.getKey().getLocalName() + " depuis plus d'1 minute");
            }
        }
    }
    
    private void handleEmergencies() {
        // Simulation d'événements exceptionnels (très rare)
        Random rand = new Random();
        if (rand.nextDouble() < 0.001) {
            String emergencyType = rand.nextBoolean() ? "MAINTENANCE" : "SECURITY";
            String affectedTableau = "Tableau" + (rand.nextInt(5) + 1);
            
            handleEmergency(emergencyType, affectedTableau);
        }
    }
    
    private void handleEmergency(String emergencyType, String location) {
        System.out.println("Coordinateur: URGENCE " + emergencyType + " détectée - " + location);
        
        if (location.startsWith("Tableau")) {
            tableauAvailability.put(location, false);
        }
        
        for (AID guideAID : guides) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(guideAID);
            msg.setContent("EMERGENCY:" + emergencyType + ":" + location + ":AVOID_LOCATION");
            send(msg);
        }
        
        Random rand = new Random();
        addBehaviour(new WakerBehaviour(this, 30000 + rand.nextInt(60000)) {
            protected void onWake() {
                resolveEmergency(location);
            }
        });
    }
    
    private void resolveEmergency(String location) {
        System.out.println("Coordinateur: Urgence résolue - " + location + " de nouveau disponible");
        
        if (location.startsWith("Tableau")) {
            tableauAvailability.put(location, true);
        }
        
        for (AID guideAID : guides) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(guideAID);
            msg.setContent("EMERGENCY_RESOLVED:" + location + ":LOCATION_AVAILABLE");
            send(msg);
        }
    }
    
    private void updateGlobalMetrics() {
        // Mise à jour des métriques globales pour l'interface graphique
        notifyGUIUpdate();
    }
    
    // Getters améliorés pour l'interface graphique
    public Set<AID> getGuides() { return new HashSet<>(guides); }
    public Set<AID> getTourists() { return new HashSet<>(tourists); }
    public Queue<AID> getWaitingTourists() { return new LinkedList<>(waitingTourists); }
    public Map<String, Integer> getLocationOccupancy() { return new HashMap<>(locationOccupancy); }
    public Map<String, Boolean> getTableauAvailability() { return new HashMap<>(tableauAvailability); }
    public Map<AID, GuideStatus> getGuideStatus() { return new HashMap<>(guideStatus); }
    public int getTotalVisitors() { return totalVisitors; }
    public int getActiveGroups() { return activeGroups; }
    public int getCompletedTours() { return completedTours; }
    public int getWaitingTouristsCount() { return waitingTourists.size(); }
    public double getAverageMuseumSatisfaction() { return averageMuseumSatisfaction; }
    public Map<String, Integer> getPopularityStats() { return new HashMap<>(popularityStats); }
    public int getMaxCapacityPerLocation() { return MAX_CAPACITY_PER_LOCATION; }
    public String[] getMuseumLocations() { return MUSEUM_LOCATIONS.clone(); }
    
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agent Coordinateur terminé - Musée fermé");
    }
}