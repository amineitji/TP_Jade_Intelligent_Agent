# RAPPORT - Système de Gestion de Visites Guidées d'Exposition

## Analyse AEOI (Agents, Environnement, Organisation, Interactions)

---

## 📋 **CONTEXTE DU PROJET**

Le système développé permet la gestion automatisée de visites guidées dans une exposition d'art. Il coordonne l'attribution de guides à des groupes de touristes pour leur faire découvrir une collection de 8 tableaux célèbres avec des explications détaillées et une optimisation intelligente des ressources.

---

## 🤖 **A - AGENTS**

### **1. Agent Guide (`GuideAgent`)**

**Type :** Agent réactif et proactif

**Caractéristiques :**
- **Autonomie** : Décide d'accepter ou refuser les demandes selon sa disponibilité
- **Réactivité** : Répond aux demandes de visite en temps réel
- **Proactivité** : Conduit activement la visite selon un planning structuré
- **Capacités** : Gestion de groupes jusqu'à 15 personnes maximum

**Comportements principaux :**
- `ReceiveTouristRequestsBehaviour` : Traite les demandes de visite
- `ConductTourBehaviour` : Conduit la visite étape par étape
- **État interne** : Disponibilité, groupe actuel, progression dans l'exposition

**Connaissances :**
- Base de données complète des 8 tableaux
- Explications détaillées pour chaque œuvre d'art
- Techniques pédagogiques adaptées aux groupes

### **2. Agent Groupe de Touristes (`TouristGroupAgent`)**

**Type :** Agent réactif avec comportement social

**Caractéristiques :**
- **Identité** : Nationalité, taille, langue, tranche d'âge
- **Objectif** : Obtenir une visite guidée complète
- **Comportement social** : Interactions de groupe, questions, réactions

**Comportements principaux :**
- `RequestGuideBehaviour` : Recherche et demande un guide
- `ReceiveGuideInfoBehaviour` : Écoute et réagit aux explications
- `GroupInteractionBehaviour` : Simule la dynamique de groupe

**États possibles :**
- `EN_ATTENTE` : Recherche d'un guide
- `ASSIGNE_GUIDE` : Guide trouvé
- `EN_VISITE` : Visite en cours
- `VISITE_TERMINEE` : Visite achevée

### **3. Agent Coordinateur (`CoordinatorAgent`)**

**Type :** Agent superviseur et optimisateur

**Caractéristiques :**
- **Supervision globale** : Vue d'ensemble du système
- **Optimisation** : Algorithmes d'assignation intelligente
- **Monitoring** : Surveillance continue des performances

**Comportements principaux :**
- `MonitorSystemBehaviour` : Surveillance du système
- `HandleOptimizationBehaviour` : Traitement des demandes d'optimisation
- `GenerateStatisticsBehaviour` : Production de rapports
- `EmergencyHandlingBehaviour` : Gestion des situations critiques

**Fonctions d'optimisation :**
- Détection de goulots d'étranglement
- Répartition équitable de la charge
- Suggestions d'amélioration

---

## 🌍 **E - ENVIRONNEMENT**

### **1. Environnement Physique Simulé**

**Exposition d'art structurée :**
- **Point de rendez-vous** : "Point A - Entrée principale"
- **8 emplacements** correspondant aux tableaux
- **Parcours séquentiel** défini et optimisé

**Collection d'art :**
1. **La Joconde** (Léonard de Vinci, 1503) - Salle Renaissance
2. **La Nuit Étoilée** (Van Gogh, 1889) - Salle Impressionnisme  
3. **Guernica** (Picasso, 1937) - Salle Art Moderne
4. **Le Cri** (Munch, 1893) - Salle Expressionnisme
5. **La Persistance de la Mémoire** (Dalí, 1931) - Salle Surréalisme
6. **Les Demoiselles d'Avignon** (Picasso, 1907) - Salle Cubisme
7. **La Grande Vague** (Hokusai, 1830) - Salle Art Asiatique
8. **American Gothic** (Wood, 1930) - Salle Art Américain

### **2. Environnement Technologique**

**Plateforme JADE :**
- **Services d'annuaire** (Pages Jaunes) pour la découverte d'agents
- **Communication ACL** pour les échanges inter-agents
- **Conteneur d'agents** pour l'hébergement
- **Interface graphique** pour le monitoring

**Contraintes système :**
- Capacité maximale par guide : 15 personnes
- Temps d'explication par tableau : 5 secondes (simulation)
- Horaires d'ouverture : 9h-18h
- Nombre de guides initial : 5

### **3. Environnement Dynamique**

**Facteurs variables :**
- Arrivée continue de nouveaux groupes
- Événements aléatoires (maintenance, VIP, urgences)
- Charge variable selon les heures
- Diversité des nationalités et langues

---

## 🏢 **O - ORGANISATION**

### **1. Structure Hiérarchique**

```
        CoordinatorAgent
              |
    ┌─────────┼─────────┐
    │         │         │
GuideAgent GuideAgent ... (×5)
    │         │
GroupAgent GroupAgent ... (variable)
```

### **2. Rôles et Responsabilités**

**Niveau Supervision (Coordinateur) :**
- Optimisation globale des assignations
- Monitoring des performances système
- Gestion des urgences et situations exceptionnelles
- Production de statistiques et rapports

**Niveau Opérationnel (Guides) :**
- Prestation directe du service de visite
- Gestion pédagogique des groupes
- Expertise sur les œuvres d'art
- Communication avec les touristes

**Niveau Client (Groupes) :**
- Expression des besoins de visite
- Participation active aux explications
- Feedback et évaluation de satisfaction

### **3. Mécanismes de Coordination**

**Services d'annuaire JADE :**
- Enregistrement des services disponibles
- Découverte dynamique des agents
- Types de services : "guide-service", "tourist-service", "coordinator-service"

**Protocoles de communication :**
- **REQUEST/AGREE/REFUSE** pour les négociations
- **INFORM** pour le partage d'informations
- **CONFIRM** pour les accusés de réception

### **4. Politique d'Assignation**

**Critères de sélection :**
- Disponibilité du guide
- Taille du groupe (≤ 15 personnes)
- File d'attente FIFO avec priorités VIP
- Équilibrage de charge entre guides

**Gestion des conflits :**
- File d'attente pour les groupes en surplus
- Réassignation automatique en cas de problème
- Priorisation des groupes VIP

---

## 🔄 **I - INTERACTIONS**

### **1. Interactions Primaires**

#### **Demande de Visite (Groupe → Guide)**
```
GroupAgent → GuideAgent : REQUEST("TOUR_REQUEST:groupId:taille")
GuideAgent → GroupAgent : AGREE("TOUR_ACCEPTED:groupId") 
                          | REFUSE("TOUR_REFUSED:UNAVAILABLE")
```

**Séquence :**
1. Groupe recherche guides disponibles via Pages Jaunes
2. Envoi de demande avec identifiant et taille
3. Guide évalue sa capacité et disponibilité
4. Réponse d'acceptation ou de refus

#### **Conduite de Visite (Guide → Groupe)**
```
GuideAgent → GroupAgent : INFORM("TABLEAU_INFO:groupId:tableau:explication")
```

**Processus :**
1. Guide présente chaque tableau séquentiellement
2. Transmission d'informations détaillées
3. Groupe simule réactions et interactions
4. Progression contrôlée tableau par tableau

#### **Fin de Visite**
```
GuideAgent → GroupAgent : INFORM("TOUR_FINISHED:groupId")
GuideAgent → Coordinator : INFORM("TOUR_COMPLETED:groupId:guideId:taille")
```

### **2. Interactions de Supervision**

#### **Monitoring Système (Coordinateur ↔ Tous)**
- Surveillance périodique des états d'agents
- Collecte de métriques de performance
- Détection automatique d'anomalies

#### **Optimisation (Coordinateur → Guides/Groupes)**
```
Coordinator → All : INFORM("EMERGENCY_PROTOCOL:action")
Coordinator ← Agent : REQUEST("PRIORITY_GROUP:groupId")
```

### **3. Interactions Complexes**

#### **Scénario Nominal - Visite Réussie**
1. **Arrivée** : Groupe arrive au Point A
2. **Recherche** : Consultation des guides disponibles
3. **Assignation** : Demande acceptée par un guide
4. **Visite** : Parcours des 8 tableaux avec explications
5. **Évaluation** : Calcul de satisfaction automatique
6. **Libération** : Guide redevient disponible

#### **Scénario Dégradé - Saturation Système**
1. **Surcharge** : Plus de groupes que de guides disponibles
2. **File d'attente** : Groupes mis en attente
3. **Optimisation** : Coordinateur cherche des solutions
4. **Réassignation** : Attribution dès qu'un guide se libère
5. **Alerte** : Suggestions d'amélioration du système

#### **Scénario d'Urgence**
1. **Détection** : Problème signalé (guide malade, incident)
2. **Escalade** : Notification au coordinateur
3. **Protocole** : Pause ou réassignation d'urgence
4. **Récupération** : Reprise normale des opérations

### **4. Patterns de Communication**

**Communication 1-à-1 :**
- Guide ↔ Groupe assigné
- Coordination directe et personnalisée

**Communication 1-à-plusieurs :**
- Coordinateur → Tous les guides (broadcast d'urgence)
- Guide → Plusieurs groupes potentiels (recherche)

**Communication Asynchrone :**
- Demandes de visite non bloquantes
- Notifications d'événements
- Rapports statistiques périodiques

---

## 📊 **MÉTRIQUES ET INDICATEURS**

### **Indicateurs de Performance**
- **Taux d'occupation** : Pourcentage de guides actifs
- **Temps d'attente moyen** : Délai avant assignation d'un guide
- **Satisfaction moyenne** : Évaluation des groupes (1-5)
- **Throughput** : Nombre de visites complétées par heure

### **Indicateurs de Qualité**
- **Couverture exposition** : Pourcentage de tableaux vus par groupe
- **Durée optimale** : Respect des créneaux 45-90 minutes
- **Équilibrage** : Répartition équitable entre guides

---

## 🎯 **OBJECTIFS ET CONTRAINTES**

### **Objectifs Fonctionnels**
- ✅ Maximiser le nombre de visiteurs servis
- ✅ Assurer la qualité pédagogique des visites
- ✅ Optimiser l'utilisation des ressources (guides)
- ✅ Maintenir la satisfaction client élevée

### **Contraintes Opérationnelles**
- 📏 Limite de 15 personnes par groupe
- ⏰ Respect des horaires d'ouverture
- 🎨 Couverture complète des 8 tableaux
- 🌍 Support multilingue et multiculturel

### **Contraintes Techniques**
- 🔧 Robustesse face aux pannes d'agents
- ⚡ Réactivité en temps réel
- 📈 Scalabilité pour augmentation de charge
- 🔒 Cohérence des données distribuées

---

## 📈 **ÉVOLUTIVITÉ ET EXTENSIONS**

### **Extensions Possibles**
- **Parcours thématiques** : Circuits spécialisés par style/époque
- **Guides spécialisés** : Expertise par domaine artistique
- **Réservations** : Système de booking avancé
- **IoT Integration** : Capteurs de foule, localisation temps réel

### **Améliorations Système**
- **Machine Learning** : Prédiction des préférences de groupes
- **Optimisation avancée** : Algorithmes génétiques pour assignation
- **Multilinguisme** : IA de traduction en temps réel
- **Réalité Augmentée** : Guides virtuels avec dispositifs mobiles

---

## ✅ **CONCLUSION**

Ce système multi-agents démontre une architecture AEOI complète et efficace pour la gestion de visites guidées. L'organisation hiérarchique avec coordination intelligente, les interactions riches entre agents hétérogènes, et l'adaptation dynamique à l'environnement permettent une gestion optimale des ressources tout en maintenant une qualité de service élevée.

**Points forts identifiés :**
- Architecture modulaire et extensible
- Gestion intelligente des ressources
- Simulation réaliste des comportements humains
- Robustesse face aux situations exceptionnelles

**Domaines d'amélioration :**
- Intégration de données externes (météo, événements)
- Personnalisation avancée des parcours
- Interface utilisateur pour gestionnaires humains
- Mécanismes d'apprentissage adaptatif