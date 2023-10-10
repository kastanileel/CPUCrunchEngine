package src.engine.core.gamemanagement;


import src.engine.core.inputsystem.MKeyListener;
import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Vector3;

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

        int id = manager.createEntity(GameComponents.POS | GameComponents.VEL | GameComponents.RENDER);
        if (id > -1) {
            manager.pos[id].x = 1;
            manager.pos[id].y = 1;
            manager.vel[id].velx = 0.1f;
            manager.vel[id].vely = 0.1f;
            manager.rendering[id].name = "player";
            //print current path
            System.out.println(System.getProperty("user.dir"));
            manager.rendering[id].mesh = new Mesh("./src/objects/teapot/teapot.obj");
            manager.rendering[id].pos = new Vector3(0.0f, 0.0f, -8.0f);
            manager.rendering[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
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
        while(true) {
            velocitySystem.update(manager);
            renderingSystem.update(manager);
        }
    }

}