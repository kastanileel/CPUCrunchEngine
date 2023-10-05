package src.engine.core.gamemanagement;


import src.engine.core.inputsystem.MKeyListener;
import src.engine.core.rendering.RenderPipeline;

public class GameContainer {

    public static void main(String[] args) {
        new GameContainer();
    }

    EntityManager manager;
    GameSystems.Render renderingSystem;
    GameSystems.Velocity velocitySystem;

    GameContainer() {
        manager = new EntityManager(5);
        renderingSystem = new GameSystems.Render();
        velocitySystem = new GameSystems.Velocity();

        MKeyListener mKeyListener = new MKeyListener();
        RenderPipeline.getInstance().addKeyListener(mKeyListener);

        int id = manager.createEntity(GameComponents.POS | GameComponents.VEL | GameComponents.RENDER);
        if (id > -1) {
            manager.pos[id].x = 1;
            manager.pos[id].y = 1;
            manager.vel[id].velx = 0.1f;
            manager.vel[id].vely = 0.1f;
            manager.rendering[id].name = "player";
            manager.rendering[id].rayCast = true;
        }

        id = manager.createEntity(GameComponents.POS | GameComponents.VEL | GameComponents.RENDER);
        if (id > -1) {
            manager.pos[id].x = 2;
            manager.pos[id].y = 1;
            manager.vel[id].velx = 0.1f;
            manager.vel[id].vely = 0.1f;
            manager.rendering[id].name = "player";
            manager.rendering[id].rayCast = true;
        }

        id = manager.createEntity(GameComponents.POS | GameComponents.VEL | GameComponents.RENDER);
        if (id > -1) {
            manager.pos[id].x = 3;
            manager.pos[id].y = 1;
            manager.vel[id].velx = 0.1f;
            manager.vel[id].vely = 0.1f;
            manager.rendering[id].name = "player";
            manager.rendering[id].rayCast = true;
        }

        id = manager.createEntity(GameComponents.POS | GameComponents.VEL | GameComponents.RENDER);
        if (id > -1) {
            manager.pos[id].x = 2;
            manager.pos[id].y = 2;
            manager.vel[id].velx = 0.1f;
            manager.vel[id].vely = 0.1f;
            manager.rendering[id].name = "player";
            manager.rendering[id].rayCast = true;
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