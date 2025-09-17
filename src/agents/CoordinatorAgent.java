package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.*;

public class CoordinatorAgent extends Agent {
    private Map<String, Integer> guideWorkload;
    private Map<String, String> activeAssignments; // groupId -> guideId
    private List<String> waitingGroups;
    private int totalVisitsCompleted;
    private int totalTouristsServed;
    private long systemStartTime;
    
    @Override
    protected void setup() {
        System.out.println("=== COORDINATEUR D'EXPOSITION DÉMARRÉ ===");
        System.out.println("🏛️  Système de gestion de visites guidées actif");
        
        guideWorkload = new HashMap<>();
        activeAssignments = new HashMap<>();
        waitingGroups = new ArrayList<>();
        totalVisitsCompleted = 0;
        totalTouristsServed = 0;
        systemStartTime = System.currentTimeMillis();
        
        // Enregistrement dans le service d'annuaire
        registerInYellowPages();
        
        // Ajout des comportements
        addBehaviour(new MonitorSystemBehaviour());
        addBehaviour(new HandleOptimizationBehaviour());
        addBehaviour(new GenerateStatisticsBehaviour());
        addBehaviour(new EmergencyHandlingBehaviour());
        
        System.out.println("✅ Coordinateur prêt à gérer l'exposition");
    }
    
    private void registerInYellowPages() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("coordinator-service");
        sd.setName("Coordinateur d'exposition");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        
        System.out.println("\n=== RAPPORT FINAL DU COORDINATEUR ===");
        System.out.println("📊 Statistiques de la session:");
        System.out.println("   - Visites complétées: " + totalVisitsCompleted);
        System.out.println("   - Touristes servis: " + totalTouristsServed);
        long uptime = (System.currentTimeMillis() - systemStartTime) / 1000;
        System.out.println("   - Durée d'activité: " + uptime + " secondes");
        System.out.println("🏛️  Coordinateur d'exposition terminé");
    }
    
    // Surveillance globale du système
    private class MonitorSystemBehaviour extends TickerBehaviour {
        public MonitorSystemBehaviour() {
            super(CoordinatorAgent.this, 10000); // Toutes les 10 secondes
        }
        
        @Override
        protected void onTick() {
            updateSystemStatus();
            checkForOptimizations();
            detectBottlenecks();
        }
        
        private void updateSystemStatus() {
            // Mise à jour des informations sur les guides
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("guide-service");
            template.addServices(sd);
            
            try {
                DFAgentDescription[] guides = DFService.search(myAgent, template);
                
                // Mise à jour des informations sur les groupes
                sd.setType("tourist-service");
                template = new DFAgentDescription();
                template.addServices(sd);
                DFAgentDescription[] groups = DFService.search(myAgent, template);
                
                if (getTickCount() % 6 == 0) { // Toutes les minutes
                    System.out.println("\n📊 ÉTAT DU SYSTÈME");
                    System.out.println("👨‍🏫 Guides actifs: " + guides.length);
                    System.out.println("👥 Groupes présents: " + groups.length);
                    System.out.println("🔄 Assignments actives: " + activeAssignments.size());
                    System.out.println("⏳ Groupes en attente: " + waitingGroups.size());
                }
                
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
        
        private void checkForOptimizations() {
            // Vérification s'il y a des optimisations possibles
            if (!waitingGroups.isEmpty() && hasAvailableGuides()) {
                System.out.println("🔧 Opportunité d'optimisation détectée");
                addBehaviour(new OptimizeAssignmentsBehaviour());
            }
        }
        
        private void detectBottlenecks() {
            // Détection de goulots d'étranglement
            if (waitingGroups.size() > 3) {
                System.out.println("⚠️  Goulot d'étranglement détecté: " + 
                    waitingGroups.size() + " groupes en attente");
                
                // Suggestions d'amélioration
                suggestImprovements();
            }
        }
        
        private boolean hasAvailableGuides() {
            try {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("guide-service");
                template.addServices(sd);
                DFAgentDescription[] guides = DFService.search(myAgent, template);
                
                return guides.length > activeAssignments.size();
            } catch (FIPAException fe) {
                return false;
            }
        }
        
        private void suggestImprovements() {
            System.out.println("💡 Suggestions d'amélioration:");
            System.out.println("   - Ajouter des guides supplémentaires");
            System.out.println("   - Réduire la taille maximale des groupes");
            System.out.println("   - Optimiser les horaires de visite");
            System.out.println("   - Créer des circuits alternatifs");
        }
    }
    
    // Comportement d'optimisation des assignations
    private class OptimizeAssignmentsBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("🔧 Lancement de l'optimisation des assignations...");
            
            // Algorithme simple d'optimisation
            try {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("guide-service");
                template.addServices(sd);
                DFAgentDescription[] guides = DFService.search(myAgent, template);
                
                // Recherche des guides disponibles
                List<String> availableGuides = new ArrayList<>();
                for (DFAgentDescription guide : guides) {
                    String guideName = guide.getName().getLocalName();
                    if (!activeAssignments.containsValue(guideName)) {
                        availableGuides.add(guideName);
                    }
                }
                
                if (!availableGuides.isEmpty() && !waitingGroups.isEmpty()) {
                    String selectedGuide = availableGuides.get(0);
                    String waitingGroup = waitingGroups.remove(0);
                    
                    activeAssignments.put(waitingGroup, selectedGuide);
                    
                    System.out.println("✅ Optimisation: Groupe " + waitingGroup + 
                        " assigné au guide " + selectedGuide);
                }
                
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }
    
    // Gestion des communications entre agents
    private class HandleOptimizationBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                
                if (content.startsWith("TOUR_STARTED")) {
                    handleTourStarted(content);
                } else if (content.startsWith("TOUR_COMPLETED")) {
                    handleTourCompleted(content);
                } else if (content.startsWith("GROUP_WAITING")) {
                    handleGroupWaiting(content);
                } else if (content.startsWith("GUIDE_AVAILABLE")) {
                    handleGuideAvailable(content);
                }
            } else {
                block();
            }
        }
        
        private void handleTourStarted(String content) {
            String[] parts = content.split(":");
            if (parts.length >= 3) {
                String groupId = parts[1];
                String guideId = parts[2];
                
                activeAssignments.put(groupId, guideId);
                waitingGroups.remove(groupId);
                
                System.out.println("📝 Enregistrement: Visite démarrée - Groupe " + 
                    groupId + " avec guide " + guideId);
            }
        }
        
        private void handleTourCompleted(String content) {
            String[] parts = content.split(":");
            if (parts.length >= 3) {
                String groupId = parts[1];
                String guideId = parts[2];
                int groupSize = Integer.parseInt(parts[3]);
                
                activeAssignments.remove(groupId);
                totalVisitsCompleted++;
                totalTouristsServed += groupSize;
                
                System.out.println("✅ Visite terminée - Groupe " + groupId + 
                    " (" + groupSize + " personnes)");
                System.out.println("📊 Total: " + totalVisitsCompleted + 
                    " visites, " + totalTouristsServed + " touristes servis");
                
                // Le guide redevient disponible
                if (guideWorkload.containsKey(guideId)) {
                    guideWorkload.put(guideId, guideWorkload.get(guideId) + 1);
                } else {
                    guideWorkload.put(guideId, 1);
                }
            }
        }
        
        private void handleGroupWaiting(String content) {
            String[] parts = content.split(":");
            if (parts.length >= 2) {
                String groupId = parts[1];
                
                if (!waitingGroups.contains(groupId)) {
                    waitingGroups.add(groupId);
                    System.out.println("⏳ Groupe " + groupId + " ajouté à la file d'attente");
                }
            }
        }
        
        private void handleGuideAvailable(String content) {
            String[] parts = content.split(":");
            if (parts.length >= 2) {
                String guideId = parts[1];
                System.out.println("👨‍🏫 Guide " + guideId + " maintenant disponible");
                
                // Tentative d'assignation immédiate si groupes en attente
                if (!waitingGroups.isEmpty()) {
                    addBehaviour(new OptimizeAssignmentsBehaviour());
                }
            }
        }
    }
    
    // Génération de statistiques périodiques
    private class GenerateStatisticsBehaviour extends TickerBehaviour {
        public GenerateStatisticsBehaviour() {
            super(CoordinatorAgent.this, 30000); // Toutes les 30 secondes
        }
        
        @Override
        protected void onTick() {
            generateStatisticsReport();
        }
        
        private void generateStatisticsReport() {
            long currentTime = System.currentTimeMillis();
            long uptime = (currentTime - systemStartTime) / 1000;
            
            System.out.println("\n📈 RAPPORT STATISTIQUES (+" + uptime + "s)");
            System.out.println("════════════════════════════════════");
            
            // Statistiques générales
            System.out.println("🎯 Performance globale:");
            System.out.println("   • Visites complétées: " + totalVisitsCompleted);
            System.out.println("   • Touristes servis: " + totalTouristsServed);
            if (totalVisitsCompleted > 0) {
                double avgGroupSize = (double) totalTouristsServed / totalVisitsCompleted;
                System.out.println("   • Taille moyenne des groupes: " + 
                    String.format("%.1f", avgGroupSize) + " personnes");
            }
            
            // Charge de travail des guides
            System.out.println("\n👨‍🏫 Charge de travail des guides:");
            if (guideWorkload.isEmpty()) {
                System.out.println("   • Aucune visite terminée pour le moment");
            } else {
                for (Map.Entry<String, Integer> entry : guideWorkload.entrySet()) {
                    System.out.println("   • Guide " + entry.getKey() + 
                        ": " + entry.getValue() + " visites terminées");
                }
            }
            
            // État actuel
            System.out.println("\n🔄 État actuel:");
            System.out.println("   • Assignations actives: " + activeAssignments.size());
            System.out.println("   • Groupes en attente: " + waitingGroups.size());
            
            // Efficacité du système
            if (uptime > 0) {
                double visitsPerMinute = (double) totalVisitsCompleted / (uptime / 60.0);
                System.out.println("   • Taux de visites: " + 
                    String.format("%.2f", visitsPerMinute) + " visites/min");
            }
            
            System.out.println("════════════════════════════════════\n");
        }
    }
    
    // Gestion des situations d'urgence
    private class EmergencyHandlingBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                
                if (content.startsWith("EMERGENCY")) {
                    handleEmergency(msg, content);
                } else if (content.startsWith("PRIORITY_GROUP")) {
                    handlePriorityGroup(msg, content);
                } else if (content.startsWith("GUIDE_PROBLEM")) {
                    handleGuideProblem(msg, content);
                }
            } else {
                block();
            }
        }
        
        private void handleEmergency(ACLMessage msg, String content) {
            System.out.println("🚨 URGENCE DÉTECTÉE: " + content);
            
            // Protocole d'urgence
            ACLMessage broadcast = new ACLMessage(ACLMessage.INFORM);
            broadcast.setContent("EMERGENCY_PROTOCOL:PAUSE_TOURS");
            
            // Envoi à tous les agents
            try {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("guide-service");
                template.addServices(sd);
                DFAgentDescription[] guides = DFService.search(myAgent, template);
                
                for (DFAgentDescription guide : guides) {
                    broadcast.addReceiver(guide.getName());
                }
                
                sd.setType("tourist-service");
                template = new DFAgentDescription();
                template.addServices(sd);
                DFAgentDescription[] groups = DFService.search(myAgent, template);
                
                for (DFAgentDescription group : groups) {
                    broadcast.addReceiver(group.getName());
                }
                
                send(broadcast);
                
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
            
            // Réponse à l'expéditeur
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.CONFIRM);
            reply.setContent("EMERGENCY_ACKNOWLEDGED");
            send(reply);
        }
        
        private void handlePriorityGroup(ACLMessage msg, String content) {
            String[] parts = content.split(":");
            if (parts.length >= 2) {
                String priorityGroupId = parts[1];
                
                System.out.println("⭐ Groupe prioritaire identifié: " + priorityGroupId);
                
                // Placer en tête de file d'attente
                waitingGroups.remove(priorityGroupId);
                waitingGroups.add(0, priorityGroupId);
                
                // Tentative d'assignation immédiate
                addBehaviour(new OptimizeAssignmentsBehaviour());
                
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("PRIORITY_PROCESSED:" + priorityGroupId);
                send(reply);
            }
        }
        
        private void handleGuideProblem(ACLMessage msg, String content) {
            String[] parts = content.split(":");
            if (parts.length >= 2) {
                String problematicGuide = parts[1];
                
                System.out.println("⚠️ Problème signalé avec le guide: " + problematicGuide);
                
                // Recherche d'un guide de remplacement
                String affectedGroup = null;
                for (Map.Entry<String, String> assignment : activeAssignments.entrySet()) {
                    if (assignment.getValue().equals(problematicGuide)) {
                        affectedGroup = assignment.getKey();
                        break;
                    }
                }
                
                if (affectedGroup != null) {
                    activeAssignments.remove(affectedGroup);
                    waitingGroups.add(0, affectedGroup); // Priorité haute
                    
                    System.out.println("🔄 Groupe " + affectedGroup + 
                        " replacé en priorité pour nouveau guide");
                    
                    addBehaviour(new OptimizeAssignmentsBehaviour());
                }
                
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("GUIDE_PROBLEM_HANDLED:" + problematicGuide);
                send(reply);
            }
        }
    }
    
    // Méthodes utilitaires publiques
    public Map<String, Integer> getGuideWorkload() {
        return new HashMap<>(guideWorkload);
    }
    
    public Map<String, String> getActiveAssignments() {
        return new HashMap<>(activeAssignments);
    }
    
    public List<String> getWaitingGroups() {
        return new ArrayList<>(waitingGroups);
    }
    
    public int getTotalVisitsCompleted() {
        return totalVisitsCompleted;
    }
    
    public int getTotalTouristsServed() {
        return totalTouristsServed;
    }
}