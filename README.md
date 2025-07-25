# Dasher App README

- [ ] TODO Replace or update this README with instructions relevant to your application

To start the application in development mode, import it into your IDE and run the `Application` class. 
You can also start the application from the command line by running: 

```bash
./mvnw
```

To build the application in production mode, run:

```bash
./mvnw -Pproduction package
```

## Getting Started

The [Getting Started](https://vaadin.com/docs/latest/getting-started) guide will quickly familiarize you with your new
Dasher App implementation. You'll learn how to set up your development environment, understand the project 
structure, and find resources to help you add muscles to your skeleton — transforming it into a fully-featured 
application.


# GESTION KEYCLOAK 

### A execute apres chaque git pull request :  
1-
  .\keycloak-export-import.ps1 -action import
2-
  docker-compose restart


### A execute avant chaque git push request (En me signalant les modifications que tu fais dans keycloak) :  
".\keycloak-export-import.ps1 -action export"