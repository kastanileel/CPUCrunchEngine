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

    public static class Velocity extends GameSystem{

        @Override
        public void start(EntityManager manager){

        }

        @Override
        public void update(EntityManager manager, float deltaTime){
            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.VELOCITY;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    manager.transform[i].pos.x += manager.velocity[i].velocity.x  * deltaTime * manager.velocity[i].speed;
                    manager.transform[i].pos.y += manager.velocity[i].velocity.y  * deltaTime * manager.velocity[i].speed;
                    manager.transform[i].pos.z += manager.velocity[i].velocity.z  * deltaTime * manager.velocity[i].speed;
                }
            }
        }
    }



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

        @Override
        public void start(EntityManager manager) {

        }

        @Override
        public void update(EntityManager manager, float deltaTime) {
            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.PLAYERMOVEMENT;
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
            if (keyListener.isKeyPressed(KeyEvent.VK_W)) {
                manager.transform[id].pos.z -= 0.1f * deltaTime;
                System.out.println(manager.transform[id].pos.z);
            }
            if (keyListener.isKeyPressed(KeyEvent.VK_S)) {
                manager.transform[id].pos.z += 0.1f * deltaTime;
                System.out.println(manager.transform[id].pos.z);
            }
            if (keyListener.isKeyPressed(KeyEvent.VK_A)) {
                manager.transform[id].pos.x -= 0.1f * deltaTime;
                System.out.println(manager.transform[id].pos.x);
            }
            if (keyListener.isKeyPressed(KeyEvent.VK_D)) {
                manager.transform[id].pos.x += 0.1f * deltaTime;
                System.out.println(manager.transform[id].pos.x);
            }

            // set camera position to player position
            Camera cam = Camera.getInstance();
            cam.position = manager.transform[id].pos;

            // offset camera position
            cam.position = RenderMaths.addVectors(cam.position, manager.playerMovement[id].cameraOffset);
        }
    }
}