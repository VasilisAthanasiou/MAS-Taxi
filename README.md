# MAS-Taxi

---
Multi Agent Taxi. This is a school assignment. 


# Useful commands

---
## Compiling and setting up an agent
- >To compile an agent run ``javac -cp ./lib/jade.jar ./src/corepack/TaxiAgent.java``.
- >After an agent is compiled add the .class file to /lib/jade.jar under corepack using WINRAR.

## Running Main Container and custom agent
- To run the main container and a custom taxi agent run ``java -cp .\lib\jade.jar jade.Boot -agents <any name>:corepack.TaxiAgent <any name>:corepack.World``


# Architecture

---
# Agent

---

## Actions

---
- MOVE (UP, DOWN, LEFT, RIGHT)
- INTERACT (PICK, PLACE) - Pick up and place client (NOT IMPLEMENTED)

## Beliefs

---
- Agent has a graph representation of the world in its knowledge base
- Agent learns of clients location via messages from environment.


## Desires

---
- Find client / Pick up on empty cell 
- Deliver client  
- Don't crash on wall 

## Intentions

---
- ACT
  (Perform actions)
- PLAN
  (Compute optimal path for goal using A* Algorithm)




