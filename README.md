# Système Multi-Agents Guide Touristique Intelligent

## Description du Projet

Ce projet implémente un système multi-agents basé sur la méthodologie AEIO pour un guide touristique intelligent dans un environnement muséal. Le système utilise JADE (Java Agent DEvelopment Framework) avec une interface graphique 2D vue du dessus style jeu vidéo.

## Architecture du Système

### Agents Implémentés

1. **Agent Guide** (`GuideAgent.java`)
   - Spécialisations : Renaissance, Moderne, Impressionniste, Contemporain
   - Gestion de groupes de touristes (3-8 personnes)
   - Adaptation pédagogique selon le profil du groupe
   - Communication avec le coordinateur

2. **Agent Touriste** (`TouristAgent.java`)
   - Profils diversifiés (nationalité, âge, préférences)
   - États dynamiques (satisfaction, fatigue, intérêt)
   - Comportements personnalisés selon la personnalité

3. **Agent Coordinateur** (`CoordinatorAgent.java`)
   - Supervision globale du musée
   - Optimisation des ressources
   - Gestion des situations d'urgence
   - Collecte de statistiques

### Interface Graphique

- **Vue 2D du musée** avec représentation des agents en temps réel
- **Animations fluides** des déplacements
- **Indicateurs visuels** de satisfaction et fatigue
- **Panneau de contrôle** interactif
- **Logs système** en temps réel

## Prérequis

### Logiciels Requis

- **Java JDK 8 ou supérieur**
- **JADE Framework** (version 4.5.0 ou supérieure)
- **IDE Java** (IntelliJ IDEA, Eclipse, ou NetBeans recommandé)

### Téléchargement de JADE

1. Aller sur le site officiel JADE : http://jade.tilab.com/
2. Télécharger JADE (Binary Distribution)
3. Extraire l'archive dans un dossier (ex: `C:\jade` ou `/opt/jade`)

## Installation et Configuration

### Étape 1 : Préparation du Projet

```bash
# Créer le répertoire du projet
mkdir MuseumGuideSystem
cd MuseumGuideSystem

# Créer la structure des dossiers
mkdir src
mkdir lib
mkdir classes
```

### Étape 2 : Copier les Fichiers JADE

Copier les fichiers JAR de JADE dans le dossier `lib/` :
- `jade.jar`
- `commons-codec.jar` (si présent dans JADE)

### Étape 3 : Placer les Fichiers Source

Placer tous les fichiers Java dans le dossier `src/` :
- `GuideAgent.java`
- `TouristAgent.java`
- `CoordinatorAgent.java`
- `MuseumGUI.java`
- `MuseumSystemLauncher.java`

## Compilation

### Option 1 : Compilation en Ligne de Commande

```bash
# Windows
javac -cp "lib\jade.jar;lib\*" -d classes src\*.java

# Linux/Mac
javac -cp "lib/jade.jar:lib/*" -d classes src/*.java
```

### Option 2 : Utilisation d'un IDE

1. **Créer un nouveau projet Java**
2. **Ajouter JADE au classpath** :
   - IntelliJ : File → Project Structure → Libraries → + → Java → Sélectionner jade.jar
   - Eclipse : Propriétés du projet → Java Build Path → Libraries → Add External JARs
3. **Importer les fichiers source**
4. **Compiler** (généralement automatique dans l'IDE)

## Exécution

### Méthode 1 : Depuis l'IDE

1. Configurer la classe principale : `MuseumSystemLauncher`
2. S'assurer que JADE est dans le classpath
3. Exécuter le projet

### Méthode 2 : Ligne de Commande

```bash
# Windows
java -cp "classes;lib\jade.jar;lib\*" MuseumSystemLauncher

# Linux/Mac
java -cp "classes:lib/jade.jar:lib/*" MuseumSystemLauncher
```

### Méthode 3 : Script de Lancement

**Windows (`run.bat`)**:
```batch
@echo off
set JADE_CP=lib\jade.jar
set PROJECT_CP=classes
java -cp "%PROJECT_CP%;%JADE_CP%" MuseumSystemLauncher
pause
```

**Linux/Mac (`run.sh`)**:
```bash
#!/bin/bash
JADE_CP="lib/jade.jar"
PROJECT_CP="classes"
java -cp "${PROJECT_CP}:${JADE_CP}" MuseumSystemLauncher
```

## Utilisation de l'Interface Graphique

### Démarrage
1. Lancer le système via `MuseumSystemLauncher`
2. L'interface graphique s'ouvre automatiquement
3. Les agents JADE se créent et commencent à interagir

### Contrôles Disponibles
- **Démarrer Simulation** : Lance la démonstration automatique
- **Pause/Reprise** : Contrôle de l'animation
- **Reset** : Remet à zéro la simulation
- **Ajouter Touriste** : Ajoute un nouveau touriste
- **Slider Vitesse** : Ajuste la vitesse d'animation

### Éléments Visuels

#### Légende des Couleurs
- **Bleu** : Agents Guides
- **Rouge/Orange/Vert** : Agents Touristes (selon satisfaction)
- **Orange** : Tableaux/Œuvres d'art
- **Gris** : Zones spéciales (Point A, Salle de repos, Sortie)

#### Indicateurs
- **Barres de progression** : Satisfaction (vert) et Fatigue (rouge) des touristes
- **Points jaunes** : Agents en mouvement
- **Textes** : Noms des agents et spécialisations

## Fonctionnalités Démontrées

### Comportements des Agents

1. **Formation de Groupes**
   - Les touristes rejoignent automatiquement les guides disponibles
   - Seuil minimum de 3 touristes pour démarrer une visite

2. **Visites Guidées**
   - Parcours adapté selon la spécialisation du guide
   - Explications personnalisées selon le profil des touristes
   - Gestion du rythme selon la fatigue du groupe

3. **Interactions Sociales**
   - Questions des touristes curieux
   - Partage d'opinions entre touristes
   - Négociation pour les pauses

4. **Coordination Globale**
   - Surveillance des flux par le coordinateur
   - Recommandations aux guides
   - Gestion des situations d'urgence simulées

### Métriques Observables

- **Satisfaction des touristes** en temps réel
- **Taux d'occupation** des différentes zones
- **Performance des guides** (efficacité, satisfaction générée)
- **Statistiques de popularité** des œuvres

## Dépannage

### Problèmes Courants

1. **ClassNotFoundException pour JADE**
   - Vérifier que jade.jar est dans le classpath
   - S'assurer d'utiliser la bonne version de JADE

2. **Agents ne se créent pas**
   - Vérifier les logs dans la console
   - S'assurer qu'aucun autre processus n'utilise le port 1099

3. **Interface graphique ne s'affiche pas**
   - Vérifier la configuration Java Swing
   - Essayer avec différentes versions de Java

4. **Erreur de mémoire**
   - Augmenter la mémoire JVM : `java -Xmx512m -cp ...`

### Logs et Debugging

- Les logs système s'affichent dans la console
- L'interface graphique inclut une zone de logs en temps réel
- Mode debug activable dans `MuseumLogger`

## Extensions Possibles

### Fonctionnalités Avancées
1. **Sauvegarde/Chargement** de configurations de musée
2. **Statistiques détaillées** exportables
3. **Mode éditeur** pour créer de nouveaux parcours
4. **Intelligence artificielle** plus avancée pour les agents
5. **Simulation de plusieurs musées** simultanément

### Intégrations Techniques
1. **Base de données** pour persistance des données
2. **Services web** pour données externes
3. **Machine learning** pour optimisation automatique
4. **Réalité augmentée** pour interface 3D

## Structure des Fichiers

```
MuseumGuideSystem/
├── src/
│   ├── GuideAgent.java
│   ├── TouristAgent.java
│   ├── CoordinatorAgent.java
│   ├── MuseumGUI.java
│   └── MuseumSystemLauncher.java
├── lib/
│   └── jade.jar
├── classes/
│   └── (fichiers compilés)
├── run.bat (Windows)
├── run.sh (Linux/Mac)
└── README.md
```

## Support et Documentation

- **Documentation JADE** : http://jade.tilab.com/documentation/
- **Tutoriels JADE** : http://jade.tilab.com/documentation/tutorials/
- **Code source** : Commenté en français pour faciliter la compréhension