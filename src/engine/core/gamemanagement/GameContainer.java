package src.engine.core.gamemanagement;


import src.engine.core.inputsystem.MKeyListener;
import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Vector3;
import src.engine.core.rendering.SimpleRenderpipeline;

import java.io.IOException;

public class GameContainer {



    public static void main(String[] args) throws IOException {
        new GameContainer();
    }

    EntityManager manager;
    GameSystems.Render renderingSystem;
    GameSystems.Velocity velocitySystem;

    GameContainer() throws IOException {
        manager = new EntityManager(5);
        renderingSystem = new GameSystems.Render();
        velocitySystem = new GameSystems.Velocity();

        MKeyListener mKeyListener = new MKeyListener();

        int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
        if (id > -1) {

            //print current path
            System.out.println(System.getProperty("user.dir"));
            manager.rendering[id].mesh = new Mesh("./src/objects/teapot/teapot.obj");
            manager.transform[id].pos = new Vector3(0.0f, -1.5f, -8.0f);
            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
            manager.transform[id].scale = new Vector3(1.0f, 1.0f, 1.0f);
        }


        /*id = manager.createEntity(GameComponents.POS | GameComponents.RENDER);
        if (id > -1) {
            manager.pos[id].x = 0;
            manager.pos[id].y = 0;
            manager.rendering[id].name = "tree";
        }*/
        startGameLoop();
    }

    void startGameLoop() {

        long lastTime = System.nanoTime() / 1000000000;
        int counter = 0;

        while(true) {
            velocitySystem.update(manager);
            renderingSystem.update(manager);


        }
    }

}