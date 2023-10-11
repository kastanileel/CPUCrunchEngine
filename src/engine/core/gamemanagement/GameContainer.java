package src.engine.core.gamemanagement;


import src.engine.core.inputsystem.MKeyListener;
import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Vector3;
import src.engine.core.rendering.Camera;
import src.engine.core.rendering.SimpleRenderpipeline;

import java.awt.*;
import java.io.IOException;

public class GameContainer {



    public static void main(String[] args) throws IOException, InterruptedException {
        new GameContainer();
    }

    EntityManager manager;
    GameSystems.Movement movementSystem;
    GameSystems.Rasterizer rasterizer;
    GameSystems.CameraMovement cameraMovement;

    GameSystems.Velocity velocity;

    GameContainer() throws IOException, InterruptedException {
        manager = new EntityManager(1000);
        movementSystem = new GameSystems.Movement();
        rasterizer = new GameSystems.Rasterizer();
        cameraMovement = new GameSystems.CameraMovement();
        velocity = new GameSystems.Velocity();

       // Camera.getInstance().rotation.x = (float) 0.8407718;
        //Camera.getInstance().rotation.y = -(float) Math.PI/2;


        // for loop to create floor*/
        for(int width = 0; width < 30; width ++){
            for(int length = 0; length < 30; length ++){
               int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
                if(id > -1){
                    manager.rendering[id].mesh = new Mesh("./src/objects/rock/plane.obj");
                    manager.transform[id].pos = new Vector3(-15.0f + width, -1.0f, -15.0f + length);
                    manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                    manager.transform[id].scale = new Vector3(1.0f, 1.0f, 1.0f);
                    manager.rendering[id].name = "environment";
                }
            }
        }
        int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
        if(id > -1){
            manager.rendering[id].mesh = new Mesh("./src/objects/teapot/teapot.obj");
            manager.transform[id].pos = new Vector3(0.0f, 0.0f, 0);
            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
            manager.transform[id].scale = new Vector3(.2f,.2f,.2f);
            manager.rendering[id].name = "pistol";
        }

        id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
        if(id > -1){
            manager.rendering[id].mesh = new Mesh("./src/objects/teapot/teapot.obj", Color.red);
            manager.transform[id].pos = new Vector3(5.0f, 0.0f, 0);
            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
            manager.transform[id].scale = new Vector3(.2f,.2f,.2f);
            manager.rendering[id].name = "environment";
        }



       // create wall
        id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
        if(id > -1){
            manager.rendering[id].mesh = new Mesh("./src/objects/rock/rock.obj", "./src/objects/rock/rock64.png");
            manager.transform[id].pos = new Vector3(5.0f, -1.0f, 5.0f);
            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
            manager.transform[id].scale = new Vector3(1.0f, 1.0f, 1.0f);
            manager.rendering[id].name = "rock";
        }
       /*  id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
        if(id > -1){
            manager.rendering[id].mesh = new Mesh("./src/objects/rock/plane.obj");
            manager.transform[id].pos = new Vector3(0, -1.0f, -15);
            manager.transform[id].rot = new Vector3(90.0f, 0.0f, 0.0f);
            manager.transform[id].scale = new Vector3(30.0f, 3.0f, 3.0f);
            manager.rendering[id].name = "room";
        }

        id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
        if(id > -1){
            manager.rendering[id].mesh = new Mesh("./src/objects/rock/plane.obj");
            manager.transform[id].pos = new Vector3(15, -1.0f, 0);
            manager.transform[id].rot = new Vector3(0, 0.0f, 90.0f);
            manager.transform[id].scale = new Vector3(3.0f, 3.0f, 30.0f);
            manager.rendering[id].name = "room";
        }

        id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
        if(id > -1){
            manager.rendering[id].mesh = new Mesh("./src/objects/rock/plane.obj");
            manager.transform[id].pos = new Vector3(-15, -1.0f, 0);
            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 270.0f);
            manager.transform[id].scale = new Vector3(3.0f, 3.0f, 30.0f);
            manager.rendering[id].name = "room";
        }


        id = manager.createEntity(GameComponents.TRANSFORM );
        if(id > -1){
            //manager.rendering[id].mesh = new Mesh("./src/objects/rock/rock.obj", "./src/objects/rock/rock8.png");
            manager.transform[id].pos = new Vector3(0.0f, -1.0f, 10.0f);
            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
            manager.transform[id].scale = new Vector3(1.0f, 1.0f, 1.0f);
        //    manager.rendering[id].name = "rock";
        }
    */




        startGameLoop();
    }

    void startGameLoop() throws InterruptedException, IOException {

        long lastTime = System.nanoTime() / 1000000;
        int counter = 0;

        while(true) {
            float deltaTime = (System.nanoTime() / 1000000 - lastTime) / 1000.0f;
            lastTime = System.nanoTime() / 1000000;
            movementSystem.update(manager, deltaTime);
            rasterizer.update(manager, deltaTime);
            cameraMovement.update(manager, deltaTime);
            velocity.update(manager, deltaTime);

          //counter++;

        }

        //System.out.println(System.nanoTime()/1000000 - lastTime);
    }

}