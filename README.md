# Student Group Manager (SGM)
ITMO Programming project implementing console application for study groups collection management

## About
This application is implementation of several labs (5-8) of the first-year ITMO programming course.
- **Lab 5** - basic configuration (core): commands, i/o handle, data parsing
- **Lab 6** - client/server separation, RabbitMQ message broker: async requests & responses, data serialization, multi-client support
- **Lab 7** - ...
- **Lab 8** - ...

## Requirements
- **Java:** 17 or higher
- **Gradle:** depends on plugins
- **Kotlin:** 2.0.10 or higher
- **Dokka:** 2.0.0 for docs generation
- **Docker & Docker compose:** depends on plugins

## How to run project
1. Clone repository
   ```git clone https://github.com/TheGeniusOfEternity/student-group-manager.git```
2. Go into project dir
   ```cd student-group-manager```
3. Run RabbitMQ container:
   ```docker-compose up --build```
3. Build & Run project
   ```./gradlew build```
   ```./gradlew run```

## Docs generation
- Docs are generated via **Dokka** plugin
- Directory with results docs is: **'build/dokka/javadoc/'**

## Testing
- **JUnit Platform** is used
- For test launch use ```./gradlew test```

## JAR-file creation
- Project is building into JAR-file with all dependecies
- Main Class is: **Main.kt**
