# CoreMachina
A java game engine without utilizing the gpu.


## How the engine works
- Scene is structured as a tree of GameNodes
- Instantiated GameNode = Entity
- GameNodes are the basic building blocks of the engine and by instantiating and subscribing them to certain systems you can add functionality to them
- GameNodes are part of the Entity-Component-System architecture
- If you want to add functionality to a GameNode, first of all create a System that extends the GameSystem class
- For each system you must define a list of required components
- Then create a new GameNodeObject and subscribe it to the system
- The system will automatically update the MasterComponent of the GameNodeObject via its unique id
- The MasterComponent is bascially a list of Components
- Components are just data containers
