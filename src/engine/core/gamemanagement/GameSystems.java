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

import src.engine.core.rendering.DrawingWindow;
import src.engine.core.tools.MKeyListener;
import src.engine.core.tools.MMouseListener;
import src.engine.core.matutils.Mesh;

import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Vector3;
import src.engine.core.rendering.Camera;
import src.engine.core.rendering.SimpleAdvancedRenderPipeline;
import src.engine.core.tools.MusicPlayer;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GameSystems {




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
                    renderPip.renderObject(manager.rendering[i].mesh, RenderMaths.addVectors(manager.transform[i].pos, manager.rendering[i].modelPosition) , RenderMaths.addVectors(manager.transform[i].rot, manager.rendering[i].modelRotation), manager.transform[i].scale);
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

        float shootingCooldown = 0.0f;

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
                    doShooting(manager, i, deltaTime);

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

            float yRot = cam.rotation.y;
            Vector3 vec = manager.rendering[id].modelTranslation;

            // rotate the model offset with the camera
            manager.rendering[id].modelPosition = RenderMaths.rotateVectorY(vec, yRot);

            manager.transform[id].rot.y = yRot;

        }
           
        private void doPlayerMovement(EntityManager manager, int id, float deltaTime) {

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
            float cosY = (float) Math.cos(cam.rotation.y);
            float sinY = (float) Math.sin(cam.rotation.y);

            manager.physicsBody[id].force.z = (forward * cosY + right * sinY) * moveSpeed * manager.physicsBody[id].mass;
            manager.physicsBody[id].force.x = (-sinY * forward + right* cosY) * moveSpeed * manager.physicsBody[id].mass;


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

        private void doShooting(EntityManager manager, int id, float deltaTime){
            SimpleAdvancedRenderPipeline.fFov = 120.0f;

            shootingCooldown -= deltaTime;

            switch (manager.playerMovement[id].weaponType){
                case PISTOL -> {
                    if(MMouseListener.getInstance().isLeftButtonPressed()){
                        if (shootingCooldown <= 0.0f) {
                            pistol(manager, id, deltaTime);
                            return;
                        }
                    }

                    if(MMouseListener.getInstance().isRightButtonPressed()){
                        knife();
                    }
                }
                case MACHINE_GUN -> {


                }
                case SHOTGUN -> {

                }
                case SNIPER ->{
                    if(MMouseListener.getInstance().isLeftButtonPressed()){
                        if (shootingCooldown <= 0.0f) {
                            snipe(manager, id, deltaTime);

                        }
                    }

                    if(MMouseListener.getInstance().isRightButtonPressed()){
                        SimpleAdvancedRenderPipeline.fFov = 40.0f;
                        DrawingWindow.snipe = true;
                    }
                    else {
                        SimpleAdvancedRenderPipeline.fFov = 120.0f;
                        DrawingWindow.snipe = false;
                    }

                }
            }

            shootingCooldown -= deltaTime;


        }

        private void pistol(EntityManager manager, int id, float deltaTime){
            // 1. set cooldown
            shootingCooldown = 0.5f;

            shootingCooldown = 1.5f;


            Vector3 direction = RenderMaths.rotateVectorY(new Vector3(0.0f, 0.0f, 1.0f), manager.transform[id].rot.y);
            RenderMaths.normalizeVector(direction);

            direction.y = (float) Math.sin(-Camera.getInstance().rotation.x);

            direction = RenderMaths.normalizeVector(direction);

            shoot(manager, id, direction, 150.0f, 2.0f, 1);

        }

        private void snipe(EntityManager manager, int id, float deltaTime){
            shootingCooldown = 1.5f;


            Vector3 direction = RenderMaths.rotateVectorY(new Vector3(0.0f, 0.0f, 1.0f), manager.transform[id].rot.y);
            RenderMaths.normalizeVector(direction);

            direction.y = (float) Math.sin(-Camera.getInstance().rotation.x);

            direction = RenderMaths.normalizeVector(direction);

            shoot(manager, id, direction, 250.0f, 2.0f, 5);

        }

        private void knife(){

        }

        private void shoot(EntityManager manager, int id, Vector3 direction, float speed, float lifeTime, int damage){
            int bulletId = manager.createEntity(GameComponents.TRANSFORM | GameComponents.PHYSICSBODY | GameComponents.RENDER | GameComponents.BULLET | GameComponents.COLLIDER);
            if(bulletId > -1){

                try {
                    manager.transform[bulletId].pos =  Camera.getInstance().position.clone();
                    manager.transform[bulletId].pos.y += 0.065f;
                    manager.transform[bulletId].rot = manager.transform[id].rot.clone();
                    manager.transform[bulletId].scale = new Vector3(0.13f, 0.13f, 0.13f);
                    manager.physicsBody[bulletId].mass = 0.1f;
                    manager.bullet[bulletId].direction = direction;
                    manager.rendering[bulletId].mesh = new Mesh("./src/objects/guns/bullets/bullet.obj", Color.RED);
                    manager.rendering[bulletId].renderType = GameComponents.Rendering.RenderType.OneColor;
                    manager.rendering[bulletId].modelRotation = new Vector3(0.0f, 3.1415f/ -2.0f, 0.0f);
                    manager.physicsBody[bulletId].speed = speed;
                    manager.bullet[bulletId].lifeTime = lifeTime;
                    manager.bullet[bulletId].damage = damage;
                    manager.collider[bulletId].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                    manager.collider[bulletId].center = manager.transform[bulletId].pos;
                    manager.collider[bulletId].colliderSize = new Vector3(0.2f, 0.2f, 0.2f);

                    MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.Explode);

                }
                catch (Exception e){
                    System.out.println("Error creating bullet");
                }
            }
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
                    if(manager.playerMovement[i] != null) {
                        if (manager.transform[i].pos.y > 0.0f) {
                            manager.physicsBody[i].force.y -= 9.81f * manager.physicsBody[i].mass;
                        } else {
                            // check if upwars force is applied
                            if (manager.physicsBody[i].force.y <= 0.0f) {
                                manager.physicsBody[i].force.y = 0.0f;
                                manager.transform[i].pos.y = 0.0f;
                            }

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


    public static class BulletSystem extends GameSystem{

        @Override
        public void start(EntityManager manager) throws Exception {

        }

        @Override
        public void update(EntityManager manager, float deltaTime) throws Exception {

            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.PHYSICSBODY | GameComponents.RENDER | GameComponents.BULLET;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {

                    // substract deltaTime from the bullet lifetime
                    manager.bullet[i].lifeTime -= deltaTime;

                    // check if the bullet is still alive
                    if(manager.bullet[i].lifeTime <= 0.0f){
                        manager.flag[i] = 0;
                        continue;
                    }

                    // add force to the bullet
                    manager.physicsBody[i].force.x = manager.bullet[i].direction.x * manager.physicsBody[i].mass * manager.physicsBody[i].speed;
                    manager.physicsBody[i].force.y = manager.bullet[i].direction.y * manager.physicsBody[i].mass * manager.physicsBody[i].speed;
                    manager.physicsBody[i].force.z = manager.bullet[i].direction.z * manager.physicsBody[i].mass * manager.physicsBody[i].speed;

                    // interpolate bullet position and follow position fully within the first 0.5 seconds of lifetime

                }
            }

        }


    }
}