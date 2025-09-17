# Système de Gestion de Visites Guidées d'Exposition - JADE

## 📖 Description

Ce projet implémente un système multi-agents utilisant la plateforme JADE pour gérer des visites guidées dans une exposition d'art. Le système coordonne automatiquement l'attribution de guides à des groupes de touristes pour leur faire découvrir une collection de 8 tableaux célèbres avec des explications détaillées.

## 🎯 Fonctionnalités Principales

### Agents du Système

1. **Agent Guide** (`GuideAgent`)
   - Gère les visites guidées pour les groupes de touristes
   - Fournit des explications détaillées sur chaque tableau
   - Limite la taille des groupes (max 15 personnes)
   - Suit la progression de la visite

2. **Agent Groupe de Touristes** (`TouristGroupAgent`)
   - Représente un groupe de visiteurs avec nationalité, taille et caractéristiques
   - Recherche et demande un guide disponible
   - Simule les interactions et réactions du groupe
   - Génère des statistiques de satisfaction

3. **Agent Coordinateur** (`CoordinatorAgent`)
   - Supervise l'ensemble du système
   - Optimise l'attribution des guides aux groupes
   - Génère des statistiques en temps réel
   - Gère les situations d'urgence
   - Détecte les goulots d'étranglement

### Collection d'Art

L'exposition comprend 8 œuvres célèbres :
- **La Joconde** - Léonard de Vinci (1503)
- **La Nuit Étoilée** - Vincent van Gogh (1889)
- **Guernica** - Pablo Picasso (1937)
- **Le Cri** - Edvard Munch (1893)
- **La Persistance de la Mémoire** - Salvador Dalí (1931)
- **Les Demoiselles d'Avignon** - Pablo Picasso (1907)
- **La Grande Vague de Kanagawa** - Hokusai (1830)
- **American Gothic** - Grant Wood (1930)

## 🛠️ Architecture Technique

### Structure du Projet
```
src/
├── agents/
│   ├── GuideAgent.java
│   ├── TouristGroupAgent.java
│   └── CoordinatorAgent.java
├── models/
│   ├── Tableau.java
│   └── VisitorGroup.java
├── config/
│   └── ExhibitionConfig.java
└── main/
    └── ExpositionSystem.java
```

### Technologies Utilisées
- **JADE** (Java Agent DEvelopment Framework)
- **Java 8+**
- Services d'annuaire JADE (Pages Jaunes)
- Communication par messages ACL (Agent Communication Language)

## 🚀 Installation et Exécution

### Prérequis
1. Java JDK 8 ou supérieur
2. JADE Framework (`lib/jade.jar` dans le projet)
3. IDE Java (Eclipse, IntelliJ, etc.) - optionnel

### Configuration
1. Télécharger JADE depuis [jade.tilab.com](http://jade.tilab.com/)
2. Placer `jade.jar` dans le dossier `lib/` du projet
3. Compiler tous les fichiers Java

## 🚀 **Compilation et Lancement**

### **Compilation du Projet**
```bash
# Compilation de tous les fichiers Java avec JADE dans le classpath
javac -cp "lib/jade.jar" src/agents/*.java src/models/*.java src/config/*.java src/main/*.java
```

### **Lancement du Système**

#### **Option 1 : Lancement Direct (Recommandé)**
```bash
# Lancement du système complet avec interface JADE GUI
java -cp "lib/jade.jar:src" main.ExpositionSystem
```

#### **Option 2 : Lancement avec JADE Boot (Manuel)**
```bash
# Démarrage de JADE avec GUI pour créer les agents manuellement
java -cp "lib/jade.jar:src" jade.Boot -gui

# Puis dans l'interface JADE, créer les agents :
# - CoordinatorAgent : agents.CoordinatorAgent
# - GuideAgent : agents.GuideAgent  
# - TouristGroupAgent : agents.TouristGroupAgent
```

#### **Option 3 : Lancement en Mode Debug**
```bash
# Avec logs détaillés pour le debugging
java -cp "lib/jade.jar:src" -Djade.debug=true main.ExpositionSystem
```

### **Pour Windows**
```cmd
# Compilation
javac -cp "lib\jade.jar" src\agents\*.java src\models\*.java src\config\*.java src\main\*.java

# Lancement
java -cp "lib\jade.jar;src" main.ExpositionSystem
```

### **Pour Linux/Mac**
```bash
# Compilation
javac -cp "lib/jade.jar" src/agents/*.java src/models/*.java src/config/*.java src/main/*.java

# Lancement
java -cp "lib/jade.jar:src" main.ExpositionSystem
```

### **Vérification du Lancement**
Une fois lancé, vous devriez voir :
```
🏛️  DÉMARRAGE DU SYSTÈME D'EXPOSITION
=====================================
👔 Coordinateur démarré
👨‍🏫 Guide GuideMartin démarré
👨‍🏫 Guide GuideSophie démarré
👥 Groupe_Français démarré (8 personnes, Française)
...
```

### **Paramètres Optionnels**
```bash
# Désactiver l'interface graphique JADE
java -cp "lib/jade.jar:src" -Djade.gui=false main.ExpositionSystem

# Modifier la durée de simulation via propriétés système
java -cp "lib/jade.jar:src" -Dsimulation.duration=5 main.ExpositionSystem
```

### Paramètres de Configuration
Dans `ExhibitionConfig.java` :
- `NOMBRE_GUIDES_INITIAL = 5` : Nombre de guides au démarrage
- `TAILLE_MAX_GROUPE = 15` : Taille maximale d'un groupe
- `TEMPS_EXPLICATION_PAR_TABLEAU = 5` : Temps par tableau (en secondes)
- `DUREE_SIMULATION_MINUTES = 3` : Durée de la simulation

## 📊 Fonctionnement du Système

### Cycle de Vie d'une Visite

1. **Arrivée du Groupe**
   - Création d'un agent groupe avec nationalité et taille
   - Enregistrement au point de rendez-vous (Point A)

2. **Recherche de Guide**
   - Consultation des services d'annuaire JADE
   - Envoi de demande aux guides disponibles
   - Attribution automatique ou mise en file d'attente

3. **Visite Guidée**
   - Présentation séquentielle des 8 tableaux
   - Explications détaillées pour chaque œuvre
   - Simulation d'interactions (questions, réactions)

4. **Fin de Visite**
   - Génération de statistiques de satisfaction
   - Libération du guide pour un nouveau groupe
   - Calcul des métriques de performance

### Communication Inter-Agents

#### Types de Messages
- `TOUR_REQUEST` : Demande de visite d'un groupe
- `TOUR_ACCEPTED` : Acceptation par un guide
- `TOUR_REFUSED` : Refus (guide indisponible)
- `TABLEAU_INFO` : Information sur un tableau
- `TOUR_FINISHED` : Fin de visite
- `EMERGENCY` : Situation d'urgence
- `PRIORITY_GROUP` : Groupe prioritaire (VIP)

#### Protocoles de Communication
```
Groupe → Guide : REQUEST(TOUR_REQUEST:groupId:taille)
Guide → Groupe : AGREE(TOUR_ACCEPTED:groupId) | REFUSE(TOUR_REFUSED)
Guide → Groupe : INFORM(TABLEAU_INFO:groupId:tableau:explication)
Guide → Coordinateur : INFORM(TOUR_FINISHED:groupId:guideId:taille)
```

## 📈 Métriques et Statistiques

### Indicateurs de Performance
- **Visites complétées** : Nombre total de groupes ayant terminé
- **Touristes servis** : Nombre total de personnes guidées
- **Taille moyenne des groupes** : Calcul automatique
- **Taux de satisfaction** : Basé sur plusieurs critères
- **Charge de travail des guides** : Répartition équitable
- **Temps d'attente moyen** : Optimisation continue

### Facteurs de Satisfaction
- Durée de visite (optimal : 45-90 minutes)
- Nombre de tableaux vus (minimum 6/8)
- Taille du groupe (optimal ≤ 8 personnes)
- Statut VIP (+bonus)
- Facteur aléatoire (variations naturelles)

## 🎛️ Fonctionnalités Avancées

### Gestion des Urgences
Le coordinateur peut traiter :
- **Situations d'urgence** : Pause automatique des visites
- **Problèmes de guides** : Réassignation automatique
- **Groupes prioritaires** : Traitement en tête de file

### Optimisation Automatique
- **Détection de goulots d'étranglement** : >3 groupes en attente
- **Suggestions d'amélioration** : Guides supplémentaires, circuits alternatifs
- **Répartition de charge** : Équilibrage entre guides

### Simulation Réaliste
- **Nationalités diverses** : 16 nationalités supportées
- **Langues multiples** : Adaptation automatique
- **Groupes familiaux** : Détection par tranche d'âge
- **Réactions dynamiques** : Questions, exclamations, interactions

## 🔧 Personnalisation

### Ajout de Nouveaux Tableaux
```java
// Dans ExhibitionConfig.java
tableaux.add(new Tableau(
    "TAB009", "Nom du Tableau", "Artiste", 2024, "Style",
    "Description courte",
    "Explication détaillée pour le guide...",
    largeur, hauteur, "Emplacement"
));
```

### Modification des Paramètres
```java
// Configuration des guides
public static final int NOMBRE_GUIDES_INITIAL = 3; // Réduire/augmenter
public static final int TAILLE_MAX_GROUPE = 10;    // Ajuster la capacité

// Configuration simulation
public static final int DUREE_SIMULATION_MINUTES = 5; // Prolonger
```

### Ajout de Nationalités
```java
// Dans getSupportedNationalities()
return Arrays.asList(
    "Française", "Allemande", /* ... existantes ... */
    "Mexicaine", "Norvégienne" // Nouvelles nationalités
);
```

## 🐛 Résolution de Problèmes

### Problèmes Courants

1. **Agents ne se trouvent pas**
   - Vérifier que JADE est correctement configuré
   - S'assurer que tous les agents sont enregistrés dans les Pages Jaunes

2. **Pas de guides disponibles**
   - Augmenter `NOMBRE_GUIDES_INITIAL`
   - Réduire `TAILLE_MAX_GROUPE`

3. **Simulation trop rapide/lente**
   - Ajuster `TEMPS_EXPLICATION_PAR_TABLEAU`
   - Modifier les intervalles dans `TickerBehaviour`

### Logs et Debugging
```java
// Activer les logs détaillés
System.setProperty("jade.debug", "true");

// Observer les messages dans JADE GUI
// Tools → Introspector → Sélectionner agent
```

## 📋 Exemple d'Exécution

### Sortie Console Typique
```
🏛️  DÉMARRAGE DU SYSTÈME D'EXPOSITION
=====================================
👔 Coordinateur démarré
👨‍🏫 Guide GuideMartin démarré
👨‍🏫 Guide GuideSophie démarré
👥 Groupe_Français démarré (8 personnes, Française)
👥 Groupe_Allemands démarré (12 personnes, Allemande)

✓ Groupe Groupe_Français assigné au guide GuideMartin
La visite commence au Point A - Entrée principale !

=== GUIDE GuideMartin ===
Tableau 1/8: La Joconde
Groupe: Groupe_Français
Explication: Chef-d'œuvre de Léonard de Vinci...
================================

👥 GROUPE Groupe_Français (Française)
📍 Devant: La Joconde
👂 Écoute les explications du guide GuideMartin
💬 Le groupe semble captivé par les explications
❓ Un touriste demande: "Quelle est la technique utilisée ici ?"

📊 ÉTAT DU SYSTÈME
👨‍🏫 Guides actifs: 5
👥 Groupes présents: 7
🔄 Assignments actives: 2
⏳ Groupes en attente: 1

🎉 VISITE TERMINÉE pour le groupe Groupe_Français
📋 Bilan de la visite:
   - Groupe: Française (8 personnes)
   - Guide: GuideMartin
   - Tableaux vus: 8
   - Satisfaction: Excellente ⭐⭐⭐⭐⭐
```

## 🎨 Extensions Possibles

### Améliorations Suggérées
1. **Interface Web** : Monitoring en temps réel
2. **Base de données** : Persistance des statistiques
3. **IA/ML** : Prédiction des préférences de groupes
4. **Réalité Augmentée** : Intégration avec dispositifs mobiles
5. **Multilinguisme** : Guides parlant plusieurs langues
6. **Circuits thématiques** : Parcours spécialisés par style/époque

### Intégrations Avancées
- **Capteurs IoT** : Détection de foule, température
- **Blockchain** : Traçabilité des œuvres
- **API externes** : Météo, événements, réservations
- **Machine Learning** : Optimisation prédictive des flux

## 📝 Licence et Contributions

### Structure de Fichiers
- `agents/` : Logique métier des agents
- `models/` : Modèles de données
- `config/` : Configuration centralisée
- `main/` : Point d'entrée du système

### Bonnes Pratiques
- Respecter les conventions de nommage JADE
- Gérer proprement les ressources (deregister agents)
- Implémenter la gestion d'erreurs
- Documenter les protocoles de communication

## 🔍 Tests et Validation

### Scénarios de Test
1. **Test de charge** : 20+ groupes simultanés
2. **Test de robustesse** : Pannes de guides
3. **Test de satisfaction** : Différentes configurations
4. **Test d'optimisation** : Goulots d'étranglement volontaires

### Métriques de Validation
- Tous les groupes doivent être servis
- Aucun groupe ne doit attendre >5 minutes (simulation)
- Satisfaction moyenne >3/5
- Répartition équitable entre guides

---

**Développé avec JADE Framework**  
*Système de gestion intelligente d'exposition d'art*

## 🆘 Support

Pour toute question ou problème :
1. Consulter la documentation JADE officielle
2. Vérifier les logs du coordinateur
3. Utiliser l'interface graphique JADE pour le debugging
4. Ajuster les paramètres dans `ExhibitionConfig.java`

**Version du système** : 1.0  
**Compatibilité JADE** : 4.5+  
**Dernière mise à jour** : 2025