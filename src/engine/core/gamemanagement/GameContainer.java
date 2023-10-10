package src.engine.core.gamemanagement;


import src.engine.core.inputsystem.MKeyListener;
import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Vector3;
import src.engine.core.rendering.SimpleRenderpipeline;

import java.io.IOException;

public class GameContainer {



    public static void main(String[] args) throws IOException, InterruptedException {
        new GameContainer();
    }

    EntityManager manager;
    GameSystems.Render renderingSystem;
    GameSystems.Velocity velocitySystem;

    GameSystems.RenderMulti renderMultiSystem;

    GameContainer() throws IOException, InterruptedException {
        manager = new EntityManager(5);
        renderingSystem = new GameSystems.Render();
        velocitySystem = new GameSystems.Velocity();
        renderMultiSystem = new GameSystems.RenderMulti();

        MKeyListener mKeyListener = new MKeyListener();

        int id = manager.createEntity(GameComponents.TRANSFORM  | GameComponents.RENDER);
        if (id > -1) {

            manager.rendering[id].mesh = new Mesh("./src/objects/teapot/teapot.obj");
            manager.transform[id].pos = new Vector3(0.0f, -1.5f, 18.0f);
            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
            manager.transform[id].scale = new Vector3(1.0f, 1.0f, 1.0f);
            manager.rendering[id].name = "teapot";
        }

        id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
        if(id > -1){
            manager.rendering[id].mesh = new Mesh("./src/objects/rock/rock.obj", "./src/objects/rock/rock8.png");
            manager.transform[id].pos = new Vector3(0.0f, -1.0f, 10.0f);
            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
            manager.transform[id].scale = new Vector3(1.0f, 1.0f, 1.0f);
            manager.rendering[id].name = "rock";
        }


        startGameLoop();
    }

    void startGameLoop() throws InterruptedException {

        long lastTime = System.nanoTime() / 1000000;
        int counter = 0;

        while(true) {
            velocitySystem.update(manager);


          // renderingSystem.update(manager);
         renderMultiSystem.update(manager);

          //counter++;

        }

        //System.out.println(System.nanoTime()/1000000 - lastTime);
    }

}