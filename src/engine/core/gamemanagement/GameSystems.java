package src.engine.core.gamemanagement;

import src.engine.configuration.Configurator;
import src.engine.core.inputtools.MKeyListener;
import src.engine.core.inputtools.MMouseListener;
import src.engine.core.matutils.RenderMaths;
import src.engine.core.rendering.Camera;
import src.engine.core.rendering.SimpleAdvancedRenderPipeline;

import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GameSystems {
    
    public static class Renderer extends GameSystem{

        ExecutorService executor = Executors.newFixedThreadPool(2); // N is the number of threads

        int width = Integer.parseInt(Configurator.getInstance().get("windowWidth"));
        int height = Integer.parseInt(Configurator.getInstance().get("windowHeight"));

        int textureMaxAccuracy = Integer.parseInt(Configurator.getInstance().get("textureMaxAccuracy"));
        int textureMinAccuracy = Integer.parseInt(Configurator.getInstance().get("textureMinAccuracy"));
        long lastTime = System.nanoTime() / 1000000000;
        int counter = 0;


        @Override
        public void start(EntityManager manager) throws InterruptedException{
            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.RENDER;

            for(int i = 0; i < manager.size; i++){
                if((manager.flag[i] & required_GameComponents) == required_GameComponents){
                    // iterate over mesh and set render type on each triangle
                    for (int j = 0; j<manager.rendering[i].mesh.triangles.length; j ++){
                        manager.rendering[i].mesh.triangles[j].renderType = manager.rendering[i].renderType;
                    }
                }
            }
        }

        @Override
        public void update(EntityManager manager, float deltaTime) throws InterruptedException {


            SimpleAdvancedRenderPipeline renderPip = SimpleAdvancedRenderPipeline.getInstance(width, height, textureMaxAccuracy, textureMinAccuracy);

            if ((System.nanoTime() / 1000000000) - lastTime >= 1) {
                renderPip.setTitle("FPS:" + counter);
                lastTime = System.nanoTime() / 1000000000;
                counter = 0;
            } else {
                counter += 1;
            }

            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.RENDER;

            renderPip.clearBuffer();

            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    renderPip.renderObject(manager.rendering[i].mesh, manager.transform[i].pos, manager.transform[i].rot, manager.transform[i].scale);
                }
            }

            // run this in a second thread
            renderPip.stepTwo();


            renderPip.draw();

        }

    }

    public static class PlayerMovement extends GameSystem{
        MKeyListener keyListener = MKeyListener.getInstance();
        float jumpTime = 0.0f;
        float maxJumpTime = 0.5f;

        float dashTime = 0.0f;
        float maxDashTime = 0.5f;

        @Override
        public void start(EntityManager manager) {

        }

        @Override
        public void update(EntityManager manager, float deltaTime) {
            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.PLAYERMOVEMENT | GameComponents.PHYSICSBODY;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    doCameraRotation(manager, i, deltaTime);
                    doPlayerMovement(manager, i, deltaTime);

                }
            }

        }

        private void doCameraRotation(EntityManager manager, int id, float deltaTime){


            Camera cam = Camera.getInstance();
            cam.rotation.y += MMouseListener.getInstance().getMouseDeltaX() * manager.playerMovement[id].mouseSpeed * deltaTime;

            float mouseY = MMouseListener.getInstance().getMouseDeltaY();
            cam.rotation.x += mouseY * deltaTime * manager.playerMovement[id].mouseSpeed;

            // Clamp the vertical rotation to a range if you don't want it to flip over
            if (cam.rotation.x > 0.1f)
                cam.rotation.x = 0.1f;
            if (cam.rotation.x < -0.2f)
                cam.rotation.x = -0.2f;

        }
           
        public void doPlayerMovement(EntityManager manager, int id, float deltaTime) {

            float moveSpeed = manager.playerMovement[id].moveSpeed;
            float forward = 0.0f;
            float right = 0.0f;

            if (keyListener.isKeyPressed('W') || keyListener.isKeyPressed('w') ) {
                forward = moveSpeed;
            }
            if (keyListener.isKeyPressed('S') || keyListener.isKeyPressed('s')) {
                forward = -moveSpeed/2.0f;
            }
            if (keyListener.isKeyPressed('A') || keyListener.isKeyPressed('a') ) {
                right = moveSpeed/1.7f;
            }
            if (keyListener.isKeyPressed('D') || keyListener.isKeyPressed('d') ) {
                right = -moveSpeed/1.7f;
            }

            // check if dash is pressed
            if(keyListener.isKeyPressed('f') || keyListener.isKeyPressed('F')){
                if(dashTime < maxDashTime){
                    moveSpeed *= 4.0f;
                    dashTime += deltaTime;
                }
            }
            else{
                dashTime = 0.0f;
            }
            // calculate the forward vector
            Camera cam = Camera.getInstance();
            float cosY = (float) Math.cos(Math.toRadians(cam.rotation.y));
            float sinY = (float) Math.sin(Math.toRadians(cam.rotation.y));

            manager.physicsBody[id].force.x = ((forward * sinY) + (right * cosY)) * manager.physicsBody[id].mass * moveSpeed;
            manager.physicsBody[id].force.z = ((forward * cosY) - (right * sinY)) * manager.physicsBody[id].mass * moveSpeed;


            if(keyListener.isKeyPressed(' ')){
                if(jumpTime < maxJumpTime){
                    manager.physicsBody[id].force.y = manager.playerMovement[id].jumpIntensity * manager.physicsBody[id].mass;
                    jumpTime += deltaTime;
                }
            }
            else{
                jumpTime = 0.0f;
            }

            // set and offset camera position
           cam.position = RenderMaths.addVectors(manager.transform[id].pos, manager.playerMovement[id].cameraOffset);
        }
    }

    public static class PyhsicsHandler extends GameSystem{
        @Override
        public void start(EntityManager manager) {

        }

        @Override
        public void update(EntityManager manager, float deltaTime) {

            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.PHYSICSBODY;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {

                    // apply gravity -> change force
                    // Apply gravity -> change force
                    if(manager.transform[i].pos.y > 0.0f){
                        manager.physicsBody[i].force.y -= 9.81f * manager.physicsBody[i].mass;
                    }
                    else {
                        // check if upwars force is applied
                        if(manager.physicsBody[i].force.y <= 0.0f){
                            manager.physicsBody[i].force.y = 0.0f;
                            manager.transform[i].pos.y = 0.0f;
                        }

                    }

                    // apply force -> change acceleration
                    manager.physicsBody[i].acceleration.x = manager.physicsBody[i].force.x / manager.physicsBody[i].mass;
                    manager.physicsBody[i].acceleration.y = manager.physicsBody[i].force.y / manager.physicsBody[i].mass;
                    manager.physicsBody[i].acceleration.z = manager.physicsBody[i].force.z / manager.physicsBody[i].mass;

                    // apply acceleration -> change velocity
                    manager.physicsBody[i].velocity.x += manager.physicsBody[i].acceleration.x * deltaTime;
                    manager.physicsBody[i].velocity.y += manager.physicsBody[i].acceleration.y * deltaTime;
                    manager.physicsBody[i].velocity.z += manager.physicsBody[i].acceleration.z * deltaTime;


                    // apply velocity -> change position
                    manager.transform[i].pos.x += manager.physicsBody[i].velocity.x * deltaTime;
                    manager.transform[i].pos.y += manager.physicsBody[i].velocity.y * deltaTime;
                    manager.transform[i].pos.z += manager.physicsBody[i].velocity.z * deltaTime;

                    // reset force
                    manager.physicsBody[i].force.x = 0.0f;
                    manager.physicsBody[i].force.y = 0.0f;
                    manager.physicsBody[i].force.z = 0.0f;

                    // apply friction
                    manager.physicsBody[i].velocity.x *= 0.95f;
                    manager.physicsBody[i].velocity.y *= 0.95f;
                    manager.physicsBody[i].velocity.z *= 0.95f;


                }
            }

        }
    }
}