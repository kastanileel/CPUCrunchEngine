package src.engine.core.gamemanagement;

import src.engine.configuration.Configurator;

import src.engine.core.dataContainers.BoundingBox;
import src.engine.core.dataContainers.CollisionInformation;
import src.engine.core.matutils.Vector3;
import src.engine.core.rendering.SimpleAdvancedRenderPipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static src.engine.core.matutils.Collision.*;
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


    public static class CollisionSystem extends GameSystem{

        @Override
        public void start(EntityManager manager){
            //Fill the collision list
            manager.collisionList.clear();
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & GameComponents.COLLIDER) == GameComponents.COLLIDER) {
                    manager.collisionList.put(i,new CollisionInformation());
                }
            }
        }

        @Override
        public void update(EntityManager manager, float deltaTime){
            //Iterate over the collision components and compute the collisions
            //First, flush the information list
            BoundingBox[] boundingBoxes = new BoundingBox[manager.size];

            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.COLLIDER;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    manager.collisionList.get(i).flush();

                    BoundingBox bBox = createBoundingBox(manager.collider[i], manager.collider[i].colliderRotation);
                    boundingBoxes[i] = bBox;
                    //System.out.println(bBox.min.x + " "+ bBox.min.y + " "+ bBox.min.z + " | "+ bBox.max.x+" "+ bBox.max.y+" "+ bBox.max.z);
                }
            }

            List<CollisionInformation.EntityPair> collisionPairs = new ArrayList<>();

            for (int i = 0; i < manager.size; i++) {
                for (int j = i + 1; j < manager.size; j++) {
                    if (!(boundingBoxes[i] == null || boundingBoxes[j] == null) && checkCollision(boundingBoxes[i],boundingBoxes[j])
                            && (manager.collider[i].colliderType == GameComponents.Collider.ColliderType.BOX && manager.collider[j].colliderType == GameComponents.Collider.ColliderType.SPHERE
                            || manager.collider[i].colliderType == GameComponents.Collider.ColliderType.SPHERE && manager.collider[j].colliderType == GameComponents.Collider.ColliderType.BOX
                            || manager.collider[i].colliderType == GameComponents.Collider.ColliderType.SPHERE && manager.collider[j].colliderType == GameComponents.Collider.ColliderType.SPHERE

                    )){
                        collisionPairs.add(new CollisionInformation.EntityPair(i, j));
                        //System.out.println("Collision between: " + collisionPairs.get(0).getFirst() +"    " +collisionPairs.get(0).getSecond());
                    }
                }
            }
            //Now that we have the candidates, we can process the precise collisions.
            for (CollisionInformation.EntityPair pair : collisionPairs) {
                GameComponents.Collider colliderA = manager.collider[pair.getFirst()];
                GameComponents.Collider colliderB = manager.collider[pair.getSecond()];

                boolean isCollision = false;
                Vector3 hitPosition = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

                if (colliderA.colliderType == GameComponents.Collider.ColliderType.SPHERE && colliderB.colliderType == GameComponents.Collider.ColliderType.SPHERE) {
                    // Sphere-Sphere collision check
                    isCollision = checkSphereSphereCollision(colliderA, colliderB, hitPosition);
                } else if (colliderA.colliderType == GameComponents.Collider.ColliderType.BOX && colliderB.colliderType == GameComponents.Collider.ColliderType.SPHERE) {
                    // Box-Sphere collision check
                    isCollision = checkBoxSphereCollision(colliderA, colliderB, hitPosition);
                } else if (colliderA.colliderType == GameComponents.Collider.ColliderType.SPHERE && colliderB.colliderType == GameComponents.Collider.ColliderType.BOX) {
                    // Sphere-Box collision check (order reversed)
                    isCollision = checkBoxSphereCollision(colliderB, colliderA, hitPosition);
                }
                if(isCollision){
                    System.out.println("Collision between: " + pair.getFirst() +"    " +pair.getSecond());
                    System.out.println("Hit position: " + hitPosition.x + " " + hitPosition.y + " " + hitPosition.z);
                    manager.collisionList.get(pair.getFirst()).collisionEvents.add(new CollisionInformation.CollisionEvent(hitPosition, pair));
                    manager.collisionList.get(pair.getSecond()).collisionEvents.add(new CollisionInformation.CollisionEvent(hitPosition, pair));
                }
            }




        }
    }

    public static class RigidbodySystem extends GameSystem{

        @Override
        public void start(EntityManager manager) throws Exception {

        }

        @Override
        public void update(EntityManager manager, float deltaTime) throws Exception {

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
            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.PLAYERMOVEMENT | GameComponents.VELOCITY;
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
                forward = -moveSpeed/ 3.0f;
            }
            if (keyListener.isKeyPressed('A') || keyListener.isKeyPressed('a') ) {
                right = moveSpeed/2.0f;
            }
            if (keyListener.isKeyPressed('D') || keyListener.isKeyPressed('d') ) {
                right = -moveSpeed/2.0f;
            }

            // calculate the forward vector
            Camera cam = Camera.getInstance();
            float cosY = (float) Math.cos(Math.toRadians(cam.rotation.y));
            float sinY = (float) Math.sin(Math.toRadians(cam.rotation.y));

            // apply movement to velocity, important when beeing rotated 0 degrees we move along the z axis
            manager.velocity[id].velocity.x = (forward * sinY) + (right * cosY);
            manager.velocity[id].velocity.z = (forward * cosY) - (right * sinY);

            // set and offset camera position
            cam.position = RenderMaths.addVectors(manager.transform[id].pos, manager.playerMovement[id].cameraOffset);
        }
    }
}