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
    
    // État du musée
    private Map<String, Integer> locationOccupancy = new ConcurrentHashMap<>();
    private Map<String, Boolean> tableauAvailability = new ConcurrentHashMap<>();
    private Map<AID, GuideStatus> guideStatus = new ConcurrentHashMap<>();
    
    // Statistiques globales
    private int totalVisitors = 0;
    private int activeGroups = 0;
    private double averageMuseumSatisfaction = 0.5;
    private Map<String, Integer> popularityStats = new ConcurrentHashMap<>();
    
    // Configuration du musée
    private final int MAX_CAPACITY_PER_LOCATION = 15;
    private final String[] MUSEUM_LOCATIONS = {
        "PointA", "Tableau1", "Tableau2", "Tableau3", "Tableau4", "Tableau5",
        "SalleRepos", "Sortie"
    };
    
    protected void setup() {
        System.out.println("Agent Coordinateur démarré - Gestion du musée intelligente");
        
        // Initialisation de l'état du musée
        initializeMuseum();
        
        // Enregistrement du service
        registerService();
        
        // Ajout des comportements
        addBehaviour(new AgentRegistrationHandler());
        addBehaviour(new GuideReportHandler());
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
    
    // Optimisation des ressources
    private class ResourceOptimizer extends TickerBehaviour {
        public ResourceOptimizer() {
            super(CoordinatorAgent.this, 15000); // Optimisation toutes les 15 secondes
        }
        
        protected void onTick() {
            optimizeResourceAllocation();
            manageMuseumFlow();
            checkForBottlenecks();
        }
    }
    
    // Collection de statistiques
    private class StatisticsCollector extends TickerBehaviour {
        public StatisticsCollector() {
            super(CoordinatorAgent.this, 30000); // Statistiques toutes les 30 secondes
        }
        
        protected void onTick() {
            collectStatistics();
            generateReports();
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
        guideStatus.put(guideAID, new GuideStatus());
        
        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
        reply.addReceiver(guideAID);
        reply.setContent("REGISTRATION_CONFIRMED");
        send(reply);
        
        System.out.println("Coordinateur: Guide " + guideAID.getLocalName() + " enregistré (" + guides.size() + " guides actifs)");
    }
    
    private void registerTourist(AID touristAID) {
        tourists.add(touristAID);
        totalVisitors++;
        
        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
        reply.addReceiver(touristAID);
        reply.setContent("WELCOME_MUSEUM");
        send(reply);
        
        System.out.println("Coordinateur: Touriste " + touristAID.getLocalName() + " enregistré (Total visiteurs: " + totalVisitors + ")");
    }
    
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
        }
    }
    
    private void updateLocationOccupancy(String location, int groupSize) {
        locationOccupancy.put(location, groupSize);
        
        // Vérifier les seuils de capacité
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
        // Identifier les guides les plus efficaces
        AID bestGuide = null;
        double bestPerformance = 0.0;
        
        for (Map.Entry<AID, GuideStatus> entry : guideStatus.entrySet()) {
            GuideStatus status = entry.getValue();
            double performance = status.averageSatisfaction * (1.0 - status.averageFatigue);
            
            if (performance > bestPerformance) {
                bestPerformance = performance;
                bestGuide = entry.getKey();
            }
        }
        
        if (bestGuide != null && bestPerformance > 0.7) {
            // Possibilité d'assigner plus de touristes au meilleur guide
            System.out.println("Coordinateur: Meilleure performance - Guide " + bestGuide.getLocalName() + " (" + String.format("%.2f", bestPerformance) + ")");
        }
    }
    
    private void manageMuseumFlow() {
        // Détecter les zones d'engorgement
        for (Map.Entry<String, Integer> entry : locationOccupancy.entrySet()) {
            String location = entry.getKey();
            int occupancy = entry.getValue();
            
            if (occupancy > MAX_CAPACITY_PER_LOCATION * 0.9) {
                // Recommander des itinéraires alternatifs
                recommendAlternativeRoutes(location);
            }
        }
    }
    
    private void recommendAlternativeRoutes(String congestedLocation) {
        // Envoyer des recommandations aux guides pour éviter la zone congestionnée
        for (AID guideAID : guides) {
            GuideStatus status = guideStatus.get(guideAID);
            if (status != null && !status.currentLocation.equals(congestedLocation)) {
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
        
        // Identifier les transitions les plus fréquentes
        for (GuideStatus status : guideStatus.values()) {
            if (status.previousLocation != null && status.currentLocation != null) {
                String transition = status.previousLocation + "->" + status.currentLocation;
                transitionCount.put(transition, transitionCount.getOrDefault(transition, 0) + 1);
            }
            status.previousLocation = status.currentLocation;
        }
        
        // Identifier les goulots d'étranglement potentiels
        for (Map.Entry<String, Integer> entry : transitionCount.entrySet()) {
            if (entry.getValue() > 3) { // Seuil arbitraire
                System.out.println("Coordinateur: Transition fréquente détectée: " + entry.getKey() + " (" + entry.getValue() + " occurrences)");
            }
        }
    }
    
    private void collectStatistics() {
        activeGroups = 0;
        double totalSatisfaction = 0.0;
        int satisfactionCount = 0;
        
        for (GuideStatus status : guideStatus.values()) {
            if (status.groupSize > 0) {
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
        System.out.println("\n=== RAPPORT COORDINATEUR ===");
        System.out.println("Guides actifs: " + guides.size());
        System.out.println("Touristes enregistrés: " + tourists.size());
        System.out.println("Groupes en visite: " + activeGroups);
        System.out.println("Satisfaction moyenne musée: " + String.format("%.2f", averageMuseumSatisfaction));
        
        // Top 3 des tableaux les plus populaires
        System.out.println("\nTableaux les plus visités:");
        popularityStats.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " visites"));
        
        // État des zones
        System.out.println("\nOccupation des zones:");
        locationOccupancy.entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .forEach(entry -> {
                double percentage = (entry.getValue() * 100.0) / MAX_CAPACITY_PER_LOCATION;
                System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " personnes (" + 
                                 String.format("%.1f", percentage) + "%)");
            });
        
        System.out.println("===========================\n");
    }
    
    private void monitorMuseumState() {
        // Surveillance générale de l'état du musée
        long currentTime = System.currentTimeMillis();
        
        // Vérifier les guides qui n'ont pas donné de nouvelles depuis longtemps
        for (Map.Entry<AID, GuideStatus> entry : guideStatus.entrySet()) {
            GuideStatus status = entry.getValue();
            if (currentTime - status.lastReportTime > 60000) { // 1 minute sans nouvelles
                System.out.println("Coordinateur: ALERTE - Pas de nouvelles du guide " + 
                                 entry.getKey().getLocalName() + " depuis plus d'1 minute");
            }
        }
    }
    
    private void handleEmergencies() {
        // Gestion des situations d'urgence
        Random rand = new Random();
        
        // Simulation d'événements exceptionnels (très rare)
        if (rand.nextDouble() < 0.001) { // 0.1% de chance
            String emergencyType = rand.nextBoolean() ? "MAINTENANCE" : "SECURITY";
            String affectedTableau = "Tableau" + (rand.nextInt(5) + 1);
            
            handleEmergency(emergencyType, affectedTableau);
        }
    }
    
    private void handleEmergency(String emergencyType, String location) {
        System.out.println("Coordinateur: URGENCE " + emergencyType + " détectée - " + location);
        
        // Marquer la zone comme indisponible
        if (location.startsWith("Tableau")) {
            tableauAvailability.put(location, false);
        }
        
        // Informer tous les guides
        for (AID guideAID : guides) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(guideAID);
            msg.setContent("EMERGENCY:" + emergencyType + ":" + location + ":AVOID_LOCATION");
            send(msg);
        }
        
        // Programmer la résolution (simulation)
        Random rand = new Random();
        addBehaviour(new WakerBehaviour(this, 30000 + rand.nextInt(60000)) { // 30s à 1m30
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
        
        // Informer les guides
        for (AID guideAID : guides) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(guideAID);
            msg.setContent("EMERGENCY_RESOLVED:" + location + ":LOCATION_AVAILABLE");
            send(msg);
        }
    }
    
    private void updateGlobalMetrics() {
        // Mise à jour des métriques globales pour l'interface graphique
        // Ces informations peuvent être utilisées par l'interface pour afficher l'état du système
    }
    
    // Classe interne pour stocker le statut des guides
    private class GuideStatus {
        String currentLocation = "PointA";
        String previousLocation = null;
        int groupSize = 0;
        int currentTableau = 0;
        double averageSatisfaction = 0.5;
        double averageFatigue = 0.0;
        long lastReportTime = System.currentTimeMillis();
        
        @Override
        public String toString() {
            return String.format("Status[%s, groupe:%d, tableau:%d, sat:%.2f, fatigue:%.2f]",
                currentLocation, groupSize, currentTableau, averageSatisfaction, averageFatigue);
        }
    }
    
    // Getters pour l'interface graphique
    public Set<AID> getGuides() { return new HashSet<>(guides); }
    public Set<AID> getTourists() { return new HashSet<>(tourists); }
    public Map<String, Integer> getLocationOccupancy() { return new HashMap<>(locationOccupancy); }
    public Map<String, Boolean> getTableauAvailability() { return new HashMap<>(tableauAvailability); }
    public Map<AID, GuideStatus> getGuideStatus() { return new HashMap<>(guideStatus); }
    public int getTotalVisitors() { return totalVisitors; }
    public int getActiveGroups() { return activeGroups; }
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