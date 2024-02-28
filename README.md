# CPUCrunchEngine/ Alien Arena Shootout
A java game engine without utilizing the gpu. And an arena shooter built with it.
***

# Screenshots from the game
![montage](https://github.com/kastanileel/CPUCrunchEngine/assets/56845913/c6070d5c-467f-464c-a960-7fd035d67279)
# Test Scene Engine:
![image](https://github.com/kastanileel/CPUCrunchEngine/assets/56845913/aae9ebac-9528-471b-89c4-78afc767a3f4)
***
# Run the Game
1. Clone this repository
2. Run the main method in the file: src/engine/core/gamemanagement/GameContainer.java
***
# Documentation
## Getting Started 
1. Clone the repository
2. Open the project in your IDE
3. Run the 'GameContainer.java' file in 'src/engine/core/gamemanagement'

## 1. Conifguration
The game engine is configured in the 'config.properties' file in the 'src/engine/configuration' folder. 

You can change the existing properties or add new ones. In general, the properties are used for **configuring the systems**
of the engine.
> E.g. you could modify the screen width or add a new property for the gravitational acceleration.


To access the set properties in your code, you can call the **Singleton** 'Configurator' class. 
> E.g: Configuration of the render pipeline
> ```java
> // Get the width of the screen for the render pipeline
> int width = Configurator.getInstance().getIntProperty("screen_width");

## 2. Object creation

### 2.1. Scene preparation
First of all the game needs a **Scene**. A scene is a container for a certain amount of Objects. Usually, a game has 
multiple scenes to structure the game and limit the amout of objects that are rendered at the same time.
Start by creating a new Class that derives from the **Scene** class.
> ```java
> public class TestScene extends Scene { ... }

Afterwards, you'd need to call the **super constructor** and override the **createScene()** method. The **createScene()**
method is used to create the objects that are part of the scene.
> ```java
> public TestScene() {
>    super("TestScene", 1000); // name, maxObjects
> }
> 
> @Override
> public void createScene() {
>   // Create objects here
> }

### 2.2. Object creation
In order to display "something" at a certain position, you need to create an object with the Components **Transform and 
Rendering** through an **EntityManager**. Usually each scene has its own EntityManager, assigned in the parent class.
> Example of loading a simple .obj file:
> ```java
> try {
>   // Request a new ID from the EntityManager for the new object
>   int id = entityManager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
>   if(id > -1){
>      manager.rendering[id].mesh = new Mesh("./path/to/file.obj", Color.blue); 
>      // assign a certain rendering type to the object -> i.e. how the render pipeline handles the object
>      manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor;
>      manager.transform[id].pos = new Vector3(0.0f, 0.0f, 0.0f);
>      manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
>      manager.transform[id].scale = new Vector3(1.0f, 1.0f, 1.0f);
>    }
> } catch (IOException e) {
>    e.printStackTrace();
> }
> ```
> **Important**: While requesting an ID from the EntityManager, you need to specify out of which **Components** the object is 
> composed

### 2.3. Add the Scene to the Game
After creating the Scene and adding the objects to it, you need to add the Scene to the **GameContainer**. The GameContainer
is the main class of the game and is responsible for the **game loop and calling the update methods of the Systems**.

These are the steps to add the Scene to the GameContainer and therefore to the game:
1. Create a new instance of the Scene in the GameContainer-Construcor
2. Put the scene into the **HashMap scenes** in the GameContainer
> ```java
> public GameContainer() {
>   ...
>   // Create a new instance of the Scene
>   Scene testScene = new TestScene();
> 
>   // Put the scene into the HashMap
>   scenes.put(testScene.getName(), testScene);
>   ...
> }

3. In order to **activate the scene**, you must set the static variable **currentSceneName** of the GameContainer to the name
of the scene you want to activate.
> ```java
> GameContainer.currentSceneName = "TestScene";

## 3. Rendering
The rendering is handled by the **RenderPipeline**. The RenderPipeline is a **Singleton** but usually you don't need to
access it directly, because the engine already has a System and a Component for rendering. 

### 3.1. Confiuration of the RenderPipeline
The RenderPipeline can be configured in the 'config.properties' file in the 'src/engine/configuration' folder. 
Currently you can set the following properties that affect the rendering:

- **screen_width**: The width of the screen in pixels
- **screen_height**: The height of the screen in pixels
- **fov**: The field of view of the camera
- **near_plane**: The near plane of the camera
- **far_plane**: The far plane of the camera
- **render_distance**: The distance from the camera at which objects are rendered
- **textureMaxAccuracy**: The maximum accuracy of the textures
- **textureMinAccuracy**: The minimum accuracy of the textures

### 3.2. Rendering Types
The RenderPipeline supports different rendering types. A **Triangle** has an assigned rendering type, which determines
the way it is displayed. Currently the following rendering types are supported:
- **OneColor**: The Object is displayed in one color (specified in the Mesh-Constructor Call)
- **OutlineOnly**: Only the outline of the Object is displayed in one color (specified in the Mesh-Constructor Call)
- **Textured**: The Object is displayed with a texture (specified in the Mesh-Constructor Call)
- **TexturedAndOutline**: The Object is displayed with and an additional outline (specified in the Mesh-Constructor Call)

## 4. Camera
The camera is as well implemented as a **Singleton**. You can access it by calling the **getInstance()** method of the 
**Camera** class. The Scene is rendered from the perspective of the camera. In order to move the camera, you can call get
the Instance and modify the **position** and the **rotation** vectors directly.
> ```java
> // modify the position directly
> Camera.getInstance().position = new Vector3(0.0f, 0.0f, 0.0f);
> 
> // store the instance in a variable and modify the rotation
> Camera camera = Camera.getInstance();
> camera.rotation.y = 3.14159f;
## 5. Input (InputTools)
The InputTools are a collection of **Singletons** that are used to query the input of the user.Currently there are two Tools
implemented: One for **Keyboard** and one for **Mouse** input.

### 5.1. Keyboard (MKeyListener)
The System is as easy as it gets: Get the instance and look up the key you want to query in an array of booleans. The index
of the key is the char value of the key.
> ```java
> // Get the instance
> MKeyListener keyboard = MKeyListener.getInstance();
> 
> // Check if the 'A' key is pressed
> if(keyboard.keyList['A']) {
>    // Do something
> }


### 5.2. Mouse (MMouseListener)
The System for the mouse offers four different methods to receive input from the mouse:
- **getMouseX()**: Returns the change in position of the mouse since the last call in the x-axis
- **getMouseY()**: Returns the change in position of the mouse since the last call in the y-axis
- **isRightButtonPressed()**: Returns the state of the right mouse button
- **isLeftButtonPressed()**: Returns the state of the left mouse button

## 6. Systems and Components: Add new behaviour to objects
The fundamental architecture of the game engine is based on the **Entity-Component-System** pattern.

The big advantage of this pattern is that the behaviour of an object is not defined by its class but by the **Components**
therefore an overcomplicated hierarchy of classes is prevented.
> => Objects are composed out of components and not based on any class

Let's say you want to continuously rotate an object. 

### 6.1. Create and assign a new Component
The first step is to create a new **Component** in the **"GameComponents.java"** file. The Component must be assigned to 
each object that should be rotated.
> ```java
> public static class RotationMarker {
>    // basically an empty class because the behaviour is defined in the System
>    // and the Component is only used to mark the object
> }

After programming the Component, you need to apply it to the object when it is created. (See 2.2. Object creation)
> ```java
> // Request a new ID from the EntityManager for the new object
> int id = entityManager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.ROTATION_MARKER);

### 6.2. Create a Bitmask for the Component
As you can see, the Entity Manger resives a **Bitmask** as a parameter. The Bitmask is used to specify which Components the
object is composed of.

Because of this, we need to add the **ROTATION_MARKER** Component to the Bitmask in the "GameComponents.java" file.
> ```java
> public final static int
>
>    TRANSFORM = 1,
>    RENDER = 1 << 1,
>    VELOCITY = 1 << 3;
>    ROTATION_MARKER = 1 << 4;

> In the above example the bitmask would be 1101 (binary) (TRANSFORM = 1, RENDER = 10, ROTATION_MARKER = 1000)

### 6.3. Create a new System
The next step is to create a new **System** in the **"GameSystems.java"** file. 

A System is responsible for the behaviour of objects that have a certain Component. In this case the System is responsible
for the rotation of objects that have the **RotationMarker** Component.

Create a new class that derives from the **System** class in the **"GameSystems.java"** file.
> ```java
> public static class RotationSystem extends System { ... }

Afterwards, you need to override the **update()** method. The update method is called every frame and is used to update the
**Component-Values** of the objects.
> ```java
> @Override
> public void update(EntityManager manager, float deltaTime){
>    // Specify which Components the System needs to work
>    int requiredComponents = GameComponents.TRANSFORM | GameComponents.ROTATION_MARKER;
>    
>    // iterate over all objects that have the required Components
>     for (int i = 0; i < manager.size; i++) { 
>        if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
>           // Update the Component-Values
>           manager.transform[i].rot.y += 0.01f * deltaTime;
>        }
>    }
> }

## 7. Timer/ Delay (Design Pattern)
There is a certain way to implement a delay in the game engine:

Let's say we want to rotate an object for 5 seconds and then stop for 3 seconds.

We start by modifying the **RotationMarker Component** from 6.1. and adding a "timer" variable and a "state" variable to it.
> ```java
> public static class RotationMarker {
>    public float timer = 0.0f;
>    public int state = 0; // state = 0 -> rotating, state = 1 -> stopped
> }

Afterwards, we modify the **RotationSystem** from 6.2. and add implement the delay logic.
> ```java
> ...
> if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
> // check if the timer is <= 0.0f (=> we must change the state)
>    if(manager.rotationMarker[i].timer <= 0.0f) {
>    
>       // check which state we are in and change the state and the timer accordingly
>       if(manager.rotationMarker[i].state == 0) {
>          manager.rotationMarker[i].state = 1;
>          // stop rotating for 3 seconds
>          manager.rotationMarker[i].timer = 3.0f;
>      } else {
>         manager.rotationMarker[i].state = 0;
>         // rotate for 5 seconds
>         manager.rotationMarker[i].timer = 5.0f;
>      }
>    }
>    else {
>       // update the timer and rotate the object if the state is 0
>       manager.rotationMarker[i].timer -= deltaTime;
>       if(manager.rotationMarker[i].state == 0) {
>          manager.transform[i].rot.y += 0.01f * deltaTime;
>       }
>    }
> }
> ...
 
**In Essence:** 
- The timer is used to determine when the state should be changed
- The timer is updated every frame with the deltaTime (time since last frame)
