# Système Multi-Agents Guide Touristique Intelligent

## Description du Projet

Ce projet implémente un système multi-agents basé sur la méthodologie AEIO pour un guide touristique intelligent dans un environnement muséal. Le système utilise JADE (Java Agent DEvelopment Framework) avec une interface graphique JavaFX 2D moderne et une architecture modulaire refactorisée pour une maintenabilité optimale.

## Architecture Refactorisée

### Structure des Packages

```
src/
├── main/java/
│   ├── agents/
│   │   ├── base/
│   │   │   ├── BaseAgent.java           # Classe abstraite commune
│   │   │   ├── AgentStatus.java         # État et propriétés des agents
│   │   │   ├── MessageHandler.java      # Gestion générique des messages
│   │   │   └── StatusReporter.java      # Rapports périodiques
│   │   ├── tourist/
│   │   │   ├── TouristAgent.java        # Agent touriste avec comportement de groupe
│   │   │   ├── TouristProfile.java      # Profil et préférences
│   │   │   ├── Personality.java         # Traits de personnalité
│   │   │   ├── SatisfactionTracker.java # Suivi de satisfaction
│   │   │   └── BehaviorManager.java     # Gestion comportementale
│   │   ├── guide/
│   │   │   ├── GuideAgent.java          # Agent guide avec gestion de groupe
│   │   │   ├── GuideProfile.java        # Expertise et compétences
│   │   │   ├── TourManager.java         # Gestion des visites
│   │   │   └── GroupHandler.java        # Dynamique de groupe
│   │   └── coordinator/
│   │       └── CoordinatorAgent.java    # Superviseur global (à implémenter)
│   ├── utils/
│   │   └── ServiceFinder.java           # Utilitaire de recherche JADE
│   └── launcher/
│       ├── MuseumSystemLauncher.java    # Interface Swing classique
│       ├── MuseumVisualizationApp.java  # Interface JavaFX moderne
│       └── SimpleLauncher.java          # Lanceur JADE simplifié
└── test/java/
    └── agents/
        ├── tourist/
        └── guide/
```

## Prérequis

### Logiciels Requis

- **Java JDK 11 ou supérieur** (LTS recommandé)
- **Apache Maven 3.6+** (recommandé pour la gestion des dépendances)
- **JavaFX SDK** (si utilisation de l'interface moderne)
- **IDE Java** (IntelliJ IDEA, Eclipse, VS Code)

### Installation Java et Maven

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk maven

# macOS avec Homebrew
brew install openjdk@17 maven

# Windows avec Chocolatey
choco install openjdk maven

# Vérification
java --version
mvn --version
```

## Configuration du Projet

### Option 1 : Projet Maven (Recommandé)

#### **Création du projet**
```bash
# Création avec archetype Maven
mvn archetype:generate \
    -DgroupId=com.museum.agents \
    -DartifactId=museum-guide-system \
    -DarchetypeArtifactId=maven-archetype-quickstart \
    -DinteractiveMode=false

cd museum-guide-system
```

#### **Configuration pom.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.museum.agents</groupId>
    <artifactId>museum-guide-system</artifactId>
    <version>2.1.0</version>
    <packaging>jar</packaging>
    
    <name>Museum Guide Multi-Agent System</name>
    <description>Système multi-agents AEIO pour guide touristique intelligent</description>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <jade.version>4.5.0</jade.version>
        <javafx.version>17.0.2</javafx.version>
        <junit.version>5.9.2</junit.version>
    </properties>
    
    <dependencies>
        <!-- JADE Framework -->
        <dependency>
            <groupId>com.tilab.jade</groupId>
            <artifactId>jade</artifactId>
            <version>${jade.version}</version>
        </dependency>
        
        <!-- JavaFX pour interface moderne -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        
        <!-- Tests -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        
        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.6</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <!-- Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
            
            <!-- JavaFX Plugin -->
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>launcher.MuseumVisualizationApp</mainClass>
                </configuration>
            </plugin>
            
            <!-- Exec Plugin pour exécution -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>launcher.MuseumVisualizationApp</mainClass>
                </configuration>
            </plugin>
            
            <!-- Surefire pour tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0</version>
            </plugin>
            
            <!-- Assembly Plugin pour JAR exécutable -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.4.2</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>launcher.MuseumVisualizationApp</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Option 2 : Structure Classique

```bash
mkdir -p museum-guide-system/{src,lib,classes,docs}
cd museum-guide-system

# Téléchargement JADE
wget https://jade.tilab.com/download/JADE-bin-4.5.0.zip
unzip JADE-bin-4.5.0.zip -d lib/
```

## Compilation et Build

### Commandes Maven (Recommandé)

```bash
# Compilation complète
mvn clean compile

# Exécution des tests
mvn test

# Packaging JAR
mvn package

# Installation en local
mvn install

# Nettoyage
mvn clean

# Compilation et exécution directe
mvn compile exec:java

# Interface JavaFX moderne
mvn javafx:run

# Génération documentation
mvn javadoc:javadoc

# Analyse de code
mvn site
```

### Scripts de Build Automatisés

**build.sh** (Linux/Mac):
```bash
#!/bin/bash
set -e

echo "🏗️  Build du Système Multi-Agents Musée"

# Vérification prérequis
command -v mvn >/dev/null 2>&1 || { echo "❌ Maven requis"; exit 1; }
command -v java >/dev/null 2>&1 || { echo "❌ Java requis"; exit 1; }

# Build Maven
echo "📦 Compilation avec Maven..."
mvn clean compile

echo "🧪 Exécution des tests..."
mvn test

echo "📋 Génération du JAR..."
mvn package

echo "✅ Build terminé avec succès!"
echo "📍 JAR disponible dans: target/museum-guide-system-2.1.0-jar-with-dependencies.jar"
```

**build.bat** (Windows):
```batch
@echo off
echo 🏗️ Build du Système Multi-Agents Musée

REM Vérification Maven
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven requis
    exit /b 1
)

REM Build
echo 📦 Compilation avec Maven...
call mvn clean compile

echo 🧪 Exécution des tests...
call mvn test

echo 📋 Génération du JAR...
call mvn package

echo ✅ Build terminé avec succès!
echo 📍 JAR disponible dans: target\museum-guide-system-2.1.0-jar-with-dependencies.jar
pause
```

## Exécution

### Méthodes de Lancement

#### **1. Via Maven (Développement)**
```bash
# Interface JavaFX moderne (recommandée)
mvn javafx:run

# Ou avec exec plugin
mvn exec:java -Dexec.mainClass="launcher.MuseumVisualizationApp"

# Interface Swing classique
mvn exec:java -Dexec.mainClass="launcher.MuseumSystemLauncher"

# Avec arguments JVM
mvn exec:java -Dexec.mainClass="launcher.MuseumVisualizationApp" \
              -Dexec.args="-Xmx1024m"
```

#### **2. JAR Exécutable (Production)**
```bash
# Génération du JAR
mvn package

# Exécution du JAR
java --module-path /path/to/javafx/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/museum-guide-system-2.1.0-jar-with-dependencies.jar

# Avec script simplifié
./run.sh
```

#### **3. Scripts de Lancement**

**run.sh** (Linux/Mac):
```bash
#!/bin/bash

# Configuration
JAR_FILE="target/museum-guide-system-2.1.0-jar-with-dependencies.jar"
JAVAFX_PATH="/usr/share/openjfx/lib"  # Adapter selon installation
MAIN_CLASS="launcher.MuseumVisualizationApp"

# Détection JavaFX
if [ ! -d "$JAVAFX_PATH" ]; then
    echo "⚠️  JavaFX non trouvé, utilisation de l'interface Swing"
    MAIN_CLASS="launcher.MuseumSystemLauncher"
    java -Xmx1024m -jar "$JAR_FILE" "$MAIN_CLASS"
else
    echo "🎮 Lancement de l'interface JavaFX moderne"
    java -Xmx1024m \
         --module-path "$JAVAFX_PATH" \
         --add-modules javafx.controls,javafx.fxml \
         -Djava.net.preferIPv4Stack=true \
         -jar "$JAR_FILE"
fi
```

**run.bat** (Windows):
```batch
@echo off
setlocal

REM Configuration
set JAR_FILE=target\museum-guide-system-2.1.0-jar-with-dependencies.jar
set JAVAFX_PATH=C:\Program Files\Java\javafx-17.0.2\lib
set MAIN_CLASS=launcher.MuseumVisualizationApp

REM Vérification JAR
if not exist "%JAR_FILE%" (
    echo ❌ JAR non trouvé. Exécutez 'mvn package' d'abord.
    pause
    exit /b 1
)

REM Lancement
echo 🎮 Lancement du Système Multi-Agents Musée
java -Xmx1024m ^
     --module-path "%JAVAFX_PATH%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -Djava.net.preferIPv4Stack=true ^
     -jar "%JAR_FILE%"

pause
```

### Configuration JVM Optimisée

**application.properties**:
```properties
# JVM Settings
jvm.memory.initial=256m
jvm.memory.maximum=1024m
jvm.gc.collector=G1

# JADE Settings
jade.gui.enabled=false
jade.platform.port=1099
jade.platform.host=localhost

# Application Settings
museum.tourist.max=50
museum.guide.count=3
museum.visualization.fps=60
```

**Utilisation**:
```bash
java -Xms256m -Xmx1024m \
     -XX:+UseG1GC \
     -Djava.net.preferIPv4Stack=true \
     -Djade.core.Agent.gui=false \
     --module-path "$JAVAFX_PATH" \
     --add-modules javafx.controls,javafx.fxml \
     -jar museum-guide-system-2.1.0-jar-with-dependencies.jar
```

## Développement et Tests

### Commandes de Développement

```bash
# Mode développement avec auto-reload
mvn compile exec:java -Dexec.mainClass="launcher.MuseumVisualizationApp"

# Tests avec couverture
mvn test jacoco:report

# Analyse qualité code
mvn sonar:sonar

# Génération site documentation
mvn site

# Vérification dépendances
mvn dependency:analyze
mvn dependency:tree

# Mise à jour versions
mvn versions:display-dependency-updates
```

### Structure des Tests

```bash
src/test/java/
├── agents/
│   ├── base/
│   │   ├── BaseAgentTest.java
│   │   └── AgentStatusTest.java
│   ├── tourist/
│   │   ├── TouristAgentTest.java
│   │   ├── TouristProfileTest.java
│   │   ├── PersonalityTest.java
│   │   └── BehaviorManagerTest.java
│   └── guide/
│       ├── GuideAgentTest.java
│       ├── GuideProfileTest.java
│       └── TourManagerTest.java
├── utils/
│   └── ServiceFinderTest.java
└── integration/
    ├── TouristGuideInteractionTest.java
    └── SystemIntegrationTest.java
```

### Tests Unitaires avec JUnit 5

```java
// Exemple de test
@Test
@DisplayName("Touriste doit poser des questions selon sa curiosité")
void testTouristQuestionBehavior() {
    // Arrange
    TouristProfile profile = new TouristProfile("TestTourist");
    profile.getPersonality().setCuriosity(0.9);
    
    // Act
    boolean shouldAsk = profile.shouldAskQuestion();
    
    // Assert
    assertTrue(shouldAsk, "Touriste très curieux devrait poser des questions");
}
```

### Tests d'Intégration

```java
@TestMethodOrder(OrderAnnotation.class)
class SystemIntegrationTest {
    
    @Test
    @Order(1)
    @DisplayName("Démarrage du système JADE")
    void testJadeSystemStartup() {
        boolean started = SimpleLauncher.startJadeSystem();
        assertTrue(started, "JADE devrait démarrer correctement");
    }
    
    @Test
    @Order(2)
    @DisplayName("Création et assignation d'un groupe")
    void testGroupAssignment() {
        // Test complet du cycle de vie d'un groupe
    }
}
```

## Configuration IDE

### IntelliJ IDEA

**1. Import du projet**:
```bash
# Ouvrir IntelliJ → Open → Sélectionner pom.xml
# Ou via File → New → Project from Version Control
```

**2. Configuration Run/Debug**:
```
Main class: launcher.MuseumVisualizationApp
VM options: --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
Working directory: $PROJECT_DIR$
```

**3. Configuration Maven**:
- File → Settings → Build → Build Tools → Maven
- Maven home path: /path/to/maven
- User settings file: ~/.m2/settings.xml

### VS Code

**.vscode/launch.json**:
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Museum System - JavaFX",
            "request": "launch",
            "mainClass": "launcher.MuseumVisualizationApp",
            "vmArgs": "--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml",
            "projectName": "museum-guide-system"
        },
        {
            "type": "java",
            "name": "Museum System - Swing",
            "request": "launch",
            "mainClass": "launcher.MuseumSystemLauncher",
            "projectName": "museum-guide-system"
        }
    ]
}
```

**.vscode/settings.json**:
```json
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.compile.nullAnalysis.mode": "automatic",
    "java.maven.downloadSources": true,
    "files.exclude": {
        "**/target": true,
        "**/.classpath": true,
        "**/.project": true,
        "**/.settings": true,
        "**/.factorypath": true
    }
}
```

### Eclipse

**1. Import Maven Project**:
- File → Import → Existing Maven Projects
- Browse vers le dossier contenant pom.xml

**2. Configuration JavaFX**:
- Right-click projet → Properties → Java Build Path
- Modulepath → Add External JARs → Ajouter JavaFX JARs
- Run Configurations → VM Arguments: `--module-path /path/to/javafx --add-modules javafx.controls,javafx.fxml`

## Déploiement et Distribution

### JAR Exécutable

```bash
# Build complet avec dépendances
mvn clean package assembly:single

# Résultat dans target/
ls -la target/*jar-with-dependencies.jar
```

### Distribution Multi-plateforme

**create-distribution.sh**:
```bash
#!/bin/bash

VERSION="2.1.0"
DIST_DIR="dist"

# Création structure distribution
mkdir -p $DIST_DIR/{linux,windows,macos}

# Build
mvn clean package

# Linux
cp target/museum-guide-system-$VERSION-jar-with-dependencies.jar $DIST_DIR/linux/
cp scripts/run.sh $DIST_DIR/linux/
chmod +x $DIST_DIR/linux/run.sh

# Windows
cp target/museum-guide-system-$VERSION-jar-with-dependencies.jar $DIST_DIR/windows/
cp scripts/run.bat $DIST_DIR/windows/

# Archive finale
tar -czf museum-guide-system-$VERSION-linux.tar.gz -C $DIST_DIR linux/
zip -r museum-guide-system-$VERSION-windows.zip $DIST_DIR/windows/

echo "✅ Distribution créée dans $DIST_DIR/"
```

## Docker Support

**Dockerfile**:
```dockerfile
FROM openjdk:17-jdk-alpine

# Installation JavaFX
RUN apk add --no-cache openjfx

# Application
WORKDIR /app
COPY target/museum-guide-system-2.1.0-jar-with-dependencies.jar app.jar

# Exposition port JADE
EXPOSE 1099

# Lancement
CMD ["java", "-Xmx1024m", \
     "--module-path", "/usr/share/java/openjfx/lib", \
     "--add-modules", "javafx.controls,javafx.fxml", \
     "-Djava.net.preferIPv4Stack=true", \
     "-jar", "app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3.8'
services:
  museum-system:
    build: .
    ports:
      - "1099:1099"
      - "8080:8080"
    environment:
      - JAVA_OPTS=-Xmx1024m
      - DISPLAY=${DISPLAY}
    volumes:
      - /tmp/.X11-unix:/tmp/.X11-unix:rw
    networks:
      - museum-net

networks:
  museum-net:
    driver: bridge
```

## Dépannage

### Problèmes Maven

```bash
# Cache corrompu
mvn dependency:purge-local-repository

# Réinitialisation complète
rm -rf ~/.m2/repository
mvn clean install

# Debug Maven
mvn -X compile

# Vérification dépendances
mvn dependency:resolve
```

### Problèmes JavaFX

```bash
# Test JavaFX disponible
java --list-modules | grep javafx

# Installation Ubuntu
sudo apt install openjfx

# Installation macOS
brew install openjdk@17 --cask
```

### Problèmes JADE

```bash
# Vérification port
netstat -an | grep 1099
lsof -i :1099

# Alternative port
mvn exec:java -Dexec.args="-Djade.core.Agent.port=1100"

# Debug JADE
mvn exec:java -Dexec.args="-Djade.core.Agent.debug=true"
```

## Scripts d'Automatisation

### Installation Complète

**install.sh**:
```bash
#!/bin/bash
set -e

echo "🚀 Installation Système Multi-Agents Musée"

# Vérification Java
java --version || {
    echo "❌ Java 11+ requis"
    exit 1
}

# Installation Maven si nécessaire
if ! command -v mvn &> /dev/null; then
    echo "📦 Installation Maven..."
    # Instructions selon OS
fi

# Clone et build
git clone https://github.com/user/museum-guide-system.git
cd museum-guide-system

# Build complet
mvn clean install

# Création scripts lancement
chmod +x scripts/*.sh

echo "✅ Installation terminée!"
echo "🎮 Lancement: ./scripts/run.sh"
```

### Monitoring et Logs

**monitor.sh**:
```bash
#!/bin/bash

LOG_DIR="logs"
mkdir -p $LOG_DIR

# Lancement avec logs détaillés
java -Xmx1024m \
     -Dlogback.configurationFile=config/logback.xml \
     -Dlog.dir=$LOG_DIR \
     --module-path /path/to/javafx \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/museum-guide-system-2.1.0-jar-with-dependencies.jar \
     2>&1 | tee $LOG_DIR/museum-system.log
```

## Contribution et Développement

### Workflow de Développement

```bash
# 1. Fork et clone
git clone https://github.com/username/museum-guide-system.git
cd museum-guide-system

# 2. Branche feature
git checkout -b feature/nouvelle-fonctionnalite

# 3. Développement
mvn compile
mvn test

# 4. Commit
git add .
git commit -m "feat(tourist): ajoute comportement adaptatif"

# 5. Push et PR
git push origin feature/nouvelle-fonctionnalite
```

### Standards de Code

```bash
# Formatage automatique
mvn spotless:apply

# Vérification qualité
mvn checkstyle:check
mvn spotbugs:check

# Tests obligatoires
mvn test
```

## Performance et Production

### Profiling

```bash
# Profiling JVM
java -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=museum-profile.jfr \
     -jar museum-guide-system-2.1.0-jar-with-dependencies.jar

# Analyse mémoire
java -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=./heapdump.hprof \
     -jar museum-guide-system-2.1.0-jar-with-dependencies.jar
```

### Configuration Production

```bash
# Variables d'environnement
export JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC"
export MUSEUM_CONFIG="config/production.properties"

# Lancement production
java $JAVA_OPTS \
     -Dspring.profiles.active=production \
     -Dconfig.location=$MUSEUM_CONFIG \
     -jar museum-guide-system-2.1.0-jar-with-dependencies.jar
```

---

**Version**: 2.1 Maven Edition  
**Dernière mise à jour**: 2024  
**Compatibilité**: Java 11+, Maven 3.6+, JADE 4.5+, JavaFX 17+