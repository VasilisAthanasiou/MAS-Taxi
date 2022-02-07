# MAS-Taxi

---
Multi Agent Taxi. This is a school assignment. 


# Useful commands

---
## Compiling and setting up an agent with windows cmd
- >To compile an agent run ``javac -cp ./lib/jade.jar ./src/corepack/TaxiAgent.java``.
- >After an agent is compiled add the .class file to /lib/jade.jar under corepack using WINRAR.

## Running Main Container and custom agent
- To run the main container and a custom taxi agent run ``java -cp .\lib\jade.jar jade.Boot -agents <any name>:corepack.TaxiAgent;<any name>:corepack.World``
- To run the main container and run multiple taxi agents ``java -cp .\lib\jade.jar jade.Boot -agents <any name>:corepack.TaxiAgent;<any other name>:corepack.TaxiAgent;<any name>:corepack.World``

## Examples
- Single agent ``java -cp .\lib\jade.jar jade.Boot -agents Agent-1:corepack.TaxiAgent;World:corepack.World``
- Multi agent ``java -cp .\lib\jade.jar jade.Boot -agents Agent-1:corepack.TaxiAgent;Agent-2:corepack.TaxiAgent;Agent-3:corepack.TaxiAgent;World:corepack.World``


# Architecture

---
# Agent

---

## Actions

---
- MOVE (UP, DOWN, LEFT, RIGHT)

## Beliefs

---
- Agent has a graph representation of the world in its knowledge base
- Agent learns of clients location via messages from environment.


## Desires

---
- Find client / Pick up on empty cell 
- Deliver client  

## Intentions

---
- ACT
  (Perform actions)
- PLAN
  (Compute optimal path for goal using A* Algorithm)




