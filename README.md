# CPUCrunchEngine
A java game engine without utilizing the gpu.

***
# Test Scene:
![image](https://github.com/kastanileel/CPUCrunchEngine/assets/56845913/aae9ebac-9528-471b-89c4-78afc767a3f4)

# ToDo:
- Improve Obj Parser
- Fix FPS bug when near a texture
- Optimize RenderPipeline
- Multithread RenderPipeline
- Introduce Physics and Collision
- Fix Camera-Upwards/Downwards rotatoin

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

## Camera
## Input
## Systems and Components
## Timer
