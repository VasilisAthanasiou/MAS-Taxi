# MAS-Taxi

---
Multi Agent Taxi. This is a school assignment. 


# Useful commands

---
## Compiling and setting up an agent
- >To compile an agent run ``javac -cp ./lib/jade.jar ./src/corepack/TaxiAgent.java``.
- >After an agent is compiled add the .class file to /lib/jade.jar under corepack using WINRAR.

## Running Main Container and custom agent
- To run the main container and a custom taxi agent run ``java -cp .\lib\jade.jar jade.Boot -gui -agents <any name>:corepack.TaxiAgent``


#Architecture

---

#Agent

---

##Actions

---
### Conscious
- MOVE (UP, DOWN, LEFT, RIGHT)
- INTERACT (PICK, PLACE) - Pick up and place client
### Unconscious
- SEE (3x3 Kernel)

>Conscious actions cost 1 point. Unconscious actions are free.
##Beliefs

---
- Agent doesn't know where the walls are initially.
- Agent learns of clients location via messages from environment.
- Agent remembers previous best paths to goals (Hard to implement. May not implement).

##Desires

---
- Find client / Pick up on empty cell (-10)
- Deliver client (+20) / Place client to wrong location (-10)
- Don't crash on wall / Crashing on a wall costs (-100)

##Intentions

---
- ACT
  (Perform conscious actions)
- THINK
  (Compute optimal path for goal)




