package src.engine.core.gamemanagement;

import src.engine.configuration.Configurator;


import src.engine.core.dataContainers.BoundingBox;
import src.engine.core.dataContainers.CollisionInformation;
import src.engine.core.gamemanagement.gamelogic.EventSystem;
import src.engine.core.gamemanagement.gamelogic.GameEventListener;
import src.engine.core.matutils.Vector3;
import src.engine.core.rendering.SimpleAdvancedRenderPipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static src.engine.core.matutils.Collision.*;

import src.engine.core.rendering.DrawingWindow;
import src.engine.core.tools.MKeyListener;
import src.engine.core.tools.MMouseListener;
import src.engine.core.matutils.Mesh;

import src.engine.core.matutils.RenderMaths;
import src.engine.core.rendering.Camera;
import src.engine.core.tools.MusicPlayer;

import java.awt.*;


public class GameSystems {

    public static Random randomS = new Random();
    public static float bulletLifeTime = 2.0f;


    public static class CollisionSystem extends GameSystem {

        @Override
        public void start(EntityManager manager) {
            //Fill the collision list
            manager.collisionList.clear();
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & GameComponents.COLLIDER) == GameComponents.COLLIDER) {
                    manager.collisionList.put(i, new CollisionInformation());
                }
            }
        }

        @Override
        public void update(EntityManager manager, float deltaTime) {
            //Iterate over the collision components and compute the collisions
            //First, flush the information list
            BoundingBox[] boundingBoxes = new BoundingBox[manager.size];

            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.COLLIDER;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    manager.collisionList.get(i).flush();

                    BoundingBox bBox = createBoundingBox(manager.collider[i], manager.collider[i].colliderRotation);
                    boundingBoxes[i] = bBox;
                }
            }

            List<CollisionInformation.EntityPair> collisionPairs = new ArrayList<>();

            for (int i = 0; i < manager.size; i++) {
                for (int j = i + 1; j < manager.size; j++) {
                    if (!(boundingBoxes[i] == null || boundingBoxes[j] == null) && checkCollision(boundingBoxes[i], boundingBoxes[j])
                            && (manager.collider[i].colliderType == GameComponents.Collider.ColliderType.BOX && manager.collider[j].colliderType == GameComponents.Collider.ColliderType.SPHERE
                            || manager.collider[i].colliderType == GameComponents.Collider.ColliderType.SPHERE && manager.collider[j].colliderType == GameComponents.Collider.ColliderType.BOX
                            || manager.collider[i].colliderType == GameComponents.Collider.ColliderType.SPHERE && manager.collider[j].colliderType == GameComponents.Collider.ColliderType.SPHERE

                    )) {
                        collisionPairs.add(new CollisionInformation.EntityPair(i, j));
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
                if (isCollision) {
                    manager.collisionList.get(pair.getFirst()).collisionEvents.add(new CollisionInformation.CollisionEvent(hitPosition, pair));
                    manager.collisionList.get(pair.getSecond()).collisionEvents.add(new CollisionInformation.CollisionEvent(hitPosition, pair));
                }
            }


        }
    }


    public static class Renderer extends GameSystem {

        ExecutorService executor = Executors.newFixedThreadPool(2); // N is the number of threads

        int width = Integer.parseInt(Configurator.getInstance().get("windowWidth"));
        int height = Integer.parseInt(Configurator.getInstance().get("windowHeight"));

        int textureMaxAccuracy = Integer.parseInt(Configurator.getInstance().get("textureMaxAccuracy"));
        int textureMinAccuracy = Integer.parseInt(Configurator.getInstance().get("textureMinAccuracy"));
        long lastTime = System.nanoTime() / 1000000000;
        int counter = 0;


        @Override
        public void start(EntityManager manager) throws InterruptedException {
            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.RENDER;

            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    // iterate over mesh and set render type on each triangle
                    for (int j = 0; j < manager.rendering[i].mesh.triangles.length; j++) {
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
                    renderPip.renderObject(manager.rendering[i].mesh, RenderMaths.addVectors(manager.transform[i].pos, manager.rendering[i].modelPosition), RenderMaths.addVectors(manager.transform[i].rot, manager.rendering[i].modelRotation), manager.transform[i].scale);
                }
            }

            // run this in a second thread
            renderPip.stepTwo(deltaTime);
            renderPip.draw();

            DrawingWindow.fadeColor(deltaTime);

        }

    }

    public static class PlayerMovement extends GameSystem {
        MKeyListener keyListener = MKeyListener.getInstance();
        float jumpTime = 0.0f;
        float maxJumpTime = 0.5f;

        float dashTime = 0.0f;
        float maxDashTime = 0.5f;

        float shootingCooldown = 0.0f;

        float knifeCooldown = 0.0f;
        float knifeTime = 0.5f;
        boolean knifing = false;

        int magazinePistol = 10;
        int magazineMachineGun = 30;
        int magazineShotgun = 2;
        int magazineSniper = 5;

        float rotating = 0.0f;
        boolean scopedIn = false;

        Vector3 knifeDir = new Vector3();

        static int knife;
        private GameComponents.Rendering.RenderType weaponRenderType;

        float defaultMoveSpeed, defaultMouseSpeed;

        @Override
        public void start(EntityManager manager) throws IOException {
            // create knife
            knife = manager.createEntity(GameComponents.TRANSFORM | GameComponents.PHYSICSBODY | GameComponents.RENDER | GameComponents.COLLIDER);
            if (knife > -1) {
                manager.transform[knife].pos = new Vector3(0.0f, -3.1f, 0.0f);
                manager.transform[knife].pos.y += 0.065f;
                manager.transform[knife].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[knife].scale = new Vector3(0.05f, 0.05f, 0.05f);
                manager.physicsBody[knife].mass = 0.1f;

                manager.rendering[knife].mesh = new Mesh("src/objects/guns/knife/combatKnife.obj", "src/objects/guns/knife/combatKnife128.png");//"./src/objects/rock/rock64.png");
                manager.rendering[knife].renderType = GameComponents.Rendering.RenderType.Hide;
                manager.rendering[knife].modelRotation = new Vector3(0.1f, -3.1415f / 2.0f, 3.0015f / 2.0f);
                manager.physicsBody[knife].speed = 100.0f;

                manager.collider[knife].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                manager.collider[knife].center = manager.transform[knife].pos;
                manager.collider[knife].colliderSize = new Vector3(2.0f, 3.0f, 2.0f);
                manager.collider[knife].colliderTag = GameComponents.Collider.ColliderTag.KNIFE;

            }

            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.PLAYERMOVEMENT | GameComponents.PHYSICSBODY;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    defaultMouseSpeed = manager.playerMovement[i].mouseSpeed;
                    defaultMoveSpeed = manager.playerMovement[i].moveSpeed;
                    //modify player health for testing here
                    manager.damageable[i].health = 100;
                    DrawingWindow.playerHealth = manager.damageable[i].health;
                }
            }

        }

        @Override
        public void update(EntityManager manager, float deltaTime) {
            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.PLAYERMOVEMENT | GameComponents.PHYSICSBODY;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {

                    DrawingWindow.playerPos = manager.transform[i].pos;

                    doCameraRotation(manager, i, deltaTime);
                    doPlayerMovement(manager, i, deltaTime);
                    doShooting(manager, i, deltaTime);
                    handleCollision(manager, i);
                    if (knifing)
                        handleKnife(manager, i);


                }
            }

        }

        private void doCameraRotation(EntityManager manager, int id, float deltaTime) {


            Camera cam = Camera.getInstance();
            cam.rotation.y += MMouseListener.getInstance().getMouseDeltaX() * manager.playerMovement[id].mouseSpeed * deltaTime;

            float mouseY = MMouseListener.getInstance().getMouseDeltaY();
            cam.rotation.x += mouseY * deltaTime * manager.playerMovement[id].mouseSpeed;

            float rotationSpeed = 11.0f * deltaTime * manager.playerMovement[id].mouseSpeed * Math.min(2.0f, rotating + 1.0f);

            if (MKeyListener.getInstance().isKeyPressed('l'))
                cam.rotation.y += rotationSpeed;
            if (MKeyListener.getInstance().isKeyPressed('j'))
                cam.rotation.y -= rotationSpeed;
            if (MKeyListener.getInstance().isKeyPressed('i'))
                cam.rotation.x -= 3.0f * deltaTime * manager.playerMovement[id].mouseSpeed;
            if (MKeyListener.getInstance().isKeyPressed('k'))
                cam.rotation.x += 3.0f * deltaTime * manager.playerMovement[id].mouseSpeed;

            if (MKeyListener.getInstance().isKeyPressed('l') || MKeyListener.getInstance().isKeyPressed('j')) {
                rotating += deltaTime;
            } else
                rotating = 0.0f;

            // Clamp the vertical rotation to a range if you don't want it to flip over
            if (cam.rotation.x > 0.2f)
                cam.rotation.x = 0.2f;
            if (cam.rotation.x < -0.4f)
                cam.rotation.x = -0.4f;

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

            if (keyListener.isKeyPressed('W') || keyListener.isKeyPressed('w')) {
                forward = moveSpeed;
            }
            if (keyListener.isKeyPressed('S') || keyListener.isKeyPressed('s')) {
                forward = -moveSpeed / 2.0f;
            }
            if (keyListener.isKeyPressed('A') || keyListener.isKeyPressed('a')) {
                right = moveSpeed / 1.7f;
            }
            if (keyListener.isKeyPressed('D') || keyListener.isKeyPressed('d')) {
                right = -moveSpeed / 1.7f;
            }

            // check if dash is pressed
            if (keyListener.isKeyPressed('f') || keyListener.isKeyPressed('F')) {
                if (dashTime < maxDashTime) {
                    moveSpeed *= 4.0f;
                    dashTime += deltaTime;
                }
            } else {
                dashTime = 0.0f;
            }
            // calculate the forward vector
            Camera cam = Camera.getInstance();
            float cosY = (float) Math.cos(cam.rotation.y);
            float sinY = (float) Math.sin(cam.rotation.y);

            manager.physicsBody[id].force.z = (forward * cosY + right * sinY) * moveSpeed * manager.physicsBody[id].mass;
            manager.physicsBody[id].force.x = (-sinY * forward + right * cosY) * moveSpeed * manager.physicsBody[id].mass;


            if (keyListener.isKeyPressed(' ')) {
                if (jumpTime < maxJumpTime) {
                    manager.physicsBody[id].force.y = manager.playerMovement[id].jumpIntensity * manager.physicsBody[id].mass;
                    jumpTime += deltaTime;
                }
            } else {
                jumpTime = 0.0f;
            }

            // set and offset camera position
            cam.position = RenderMaths.addVectors(manager.transform[id].pos, manager.playerMovement[id].cameraOffset);
        }

        private void doShooting(EntityManager manager, int id, float deltaTime) {
            SimpleAdvancedRenderPipeline.fFov = 150.0f;

            shootingCooldown -= deltaTime;
            knifeCooldown -= deltaTime;

            switch (manager.playerMovement[id].weaponType) {
                case PISTOL -> {
                    DrawingWindow.currentAmmo = magazinePistol;
                    if (MMouseListener.getInstance().isLeftButtonPressed() || MKeyListener.getInstance().isKeyPressed('u') || MKeyListener.getInstance().isKeyPressed('U')) {
                        if (shootingCooldown <= 0.0f && magazinePistol > 0) {


                            pistol(manager, id, deltaTime);
                            magazinePistol--;
                            return;
                        } else if (magazinePistol == 0) {
                            shootingCooldown = 5.0f;
                            System.out.println("Reloading Pistol!");
                            int randomInt = randomS.nextInt(5);
                            if(randomInt == 0)
                                MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.MORE_BULLETS);
                            MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.RELOAD_PISTOL);
                            magazinePistol = 10;
                        }
                    }
                    if (MMouseListener.getInstance().isRightButtonPressed() || MKeyListener.getInstance().isKeyPressed('e') || MKeyListener.getInstance().isKeyPressed('E')) {

                        knife(manager, id);
                    }
                }
                case MACHINE_GUN -> {
                    DrawingWindow.currentAmmo = magazineMachineGun;
                    if (MMouseListener.getInstance().isLeftButtonPressed() || MKeyListener.getInstance().isKeyPressed('u') || MKeyListener.getInstance().isKeyPressed('U')) {
                        if (shootingCooldown <= 0.0f && magazineMachineGun > 0) {

                            machineGun(manager, id, deltaTime);
                            magazineMachineGun--;
                            return;
                        } else if (magazineMachineGun == 0) {
                            shootingCooldown = 5.0f;
                            System.out.println("Reloading Machine Gun!");
                            int randomInt = randomS.nextInt(5);
                            if(randomInt == 0)
                                MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.MORE_BULLETS);
                            MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.RELOAD_AK);
                            magazineMachineGun = 30;
                        }
                    }
                    if (MMouseListener.getInstance().isRightButtonPressed() || MKeyListener.getInstance().isKeyPressed('e') || MKeyListener.getInstance().isKeyPressed('E')) {

                        knife(manager, id);
                    }

                }
                case SHOTGUN -> {
                    DrawingWindow.currentAmmo = magazineShotgun;
                    if (MMouseListener.getInstance().isLeftButtonPressed() || MKeyListener.getInstance().isKeyPressed('u') || MKeyListener.getInstance().isKeyPressed('U')) {
                        if (shootingCooldown <= 0.0f && magazineShotgun > 0) {

                            shotgun(manager, id, deltaTime);
                            magazineShotgun--;
                            return;
                        } else if (magazineShotgun == 0) {
                            shootingCooldown = 4.0f;
                            System.out.println("Reloading Shotgun!");
                            int randomInt = randomS.nextInt(5);
                            if(randomInt == 0)
                                MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.MORE_BULLETS);
                            MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.RELOAD_SHOTGUN);
                            magazineShotgun = 2;
                        }
                    }

                    if (MMouseListener.getInstance().isRightButtonPressed() || MKeyListener.getInstance().isKeyPressed('e') || MKeyListener.getInstance().isKeyPressed('E')) {
                        if (shootingCooldown <= 0.0f && magazineShotgun > 1) {

                            shotgunDouble(manager, id, deltaTime);
                            magazineShotgun--;
                            magazineShotgun--;
                            return;
                        } else if (magazineShotgun == 0) {
                            shootingCooldown = 4.0f;
                            System.out.println("Reloading Shotgun!");
                            int randomInt = randomS.nextInt(5);
                            if(randomInt == 0)
                                MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.MORE_BULLETS);
                            MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.RELOAD_SHOTGUN);
                            magazineShotgun = 2;
                        }
                    }

                }
                case SNIPER -> {
                    DrawingWindow.currentAmmo = magazineSniper;
                    if (MMouseListener.getInstance().isLeftButtonPressed() || MKeyListener.getInstance().isKeyPressed('u') || MKeyListener.getInstance().isKeyPressed('U')) {
                        if (shootingCooldown <= 0.0f && magazineSniper > 0) {

                            snipe(manager, id, deltaTime);
                            magazineSniper--;
                        } else if (magazineSniper == 0) {
                            shootingCooldown = 6.0f;
                            System.out.println("Reloading Sniper!");
                            int randomInt = randomS.nextInt(5);
                            if(randomInt == 0)
                                MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.MORE_BULLETS);
                            MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.RELOAD_SNIPER);
                            magazineSniper = 5;
                        }
                    }


                    if (MMouseListener.getInstance().isRightButtonPressed() || MKeyListener.getInstance().isKeyPressed('e') || MKeyListener.getInstance().isKeyPressed('E')) {
                        if (!scopedIn) {
                            MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.SCOPE);
                            scopedIn = true;
                        }
                        SimpleAdvancedRenderPipeline.fFov = 40.0f;
                        DrawingWindow.snipe = true;
                        manager.playerMovement[id].mouseSpeed = defaultMouseSpeed / 2.0f;
                        manager.playerMovement[id].moveSpeed = defaultMoveSpeed / 2.0f;
                    } else {
                        scopedIn = false;

                        SimpleAdvancedRenderPipeline.fFov = 150.0f;
                        DrawingWindow.snipe = false;
                        manager.playerMovement[id].mouseSpeed = defaultMouseSpeed;
                        manager.playerMovement[id].moveSpeed = defaultMoveSpeed;
                    }

                }
            }

            if (knifing)
                animateKnife(manager, id, deltaTime);

        }

        private void pistol(EntityManager manager, int id, float deltaTime) {
            // 1. set cooldown
            shootingCooldown = 0.4f;

            Vector3 direction = RenderMaths.rotateVectorY(new Vector3(0.0f, 0.0f, 1.0f), manager.transform[id].rot.y);
            RenderMaths.normalizeVector(direction);

            direction.y = (float) Math.sin(-Camera.getInstance().rotation.x);

            direction = RenderMaths.normalizeVector(direction);

            shoot(manager, id, direction, 150.0f, 2.0f, 15, MusicPlayer.SoundEffect.SHOOT_PISTOL);

        }

        private void machineGun(EntityManager manager, int id, float deltaTime) {
            // 1. set cooldown
            shootingCooldown = 0.1f;

            Vector3 direction = RenderMaths.rotateVectorY(new Vector3(0.0f, 0.0f, 1.0f), manager.transform[id].rot.y);
            RenderMaths.normalizeVector(direction);

            direction.y = (float) Math.sin(-Camera.getInstance().rotation.x);

            direction = RenderMaths.normalizeVector(direction);


            float factor = 0.15f;
            // generate random, small offset
            float x = (float) Math.random() * 0.1f - 0.01f;
            float y = (float) Math.random() * 0.1f - 0.01f;
            float z = (float) Math.random() * 0.1f - 0.01f;

            shoot(manager, id, RenderMaths.addVectors(direction, new Vector3(x * factor, y * factor, z * factor)), 175.0f, 1.5f, 20, MusicPlayer.SoundEffect.SHOOT_AK);


        }

        private void snipe(EntityManager manager, int id, float deltaTime) {
            shootingCooldown = 2.5f;


            Vector3 direction = RenderMaths.rotateVectorY(new Vector3(0.0f, 0.0f, 1.0f), manager.transform[id].rot.y);
            RenderMaths.normalizeVector(direction);

            direction.y = (float) Math.sin(-Camera.getInstance().rotation.x);

            direction = RenderMaths.normalizeVector(direction);

            shoot(manager, id, direction, 200.0f, 1.5f, 100, MusicPlayer.SoundEffect.SHOOT_SNIPER);

        }

        private void shotgun(EntityManager manager, int id, float deltaTime) {
            shootingCooldown = 0.8f;

            Vector3 direction = RenderMaths.rotateVectorY(new Vector3(0.0f, 0.0f, 1.0f), manager.transform[id].rot.y);
            RenderMaths.normalizeVector(direction);

            direction.y = (float) Math.sin(-Camera.getInstance().rotation.x);

            direction = RenderMaths.normalizeVector(direction);

            shoot(manager, id, direction, 150.0f, 2.0f, 20, MusicPlayer.SoundEffect.SHOOT_SHOTGUN);

            for (int i = 0; i < 4; i++) {
                float factor = 0.4f;
                // generate random, small offset
                float x = (float) Math.random() * 0.1f - 0.05f;
                float y = (float) Math.random() * 0.1f - 0.05f;
                float z = (float) Math.random() * 0.1f - 0.05f;
                shoot(manager, id, RenderMaths.addVectors(direction, new Vector3(x * factor, y * factor, z * factor)), 150.0f, 2.0f, 20, MusicPlayer.SoundEffect.SHOOT_SHOTGUN);
            }
        }

        private void shotgunDouble(EntityManager manager, int id, float deltaTime) {
            shootingCooldown = 3.5f;

            //Add Double Shot

            Vector3 direction = RenderMaths.rotateVectorY(new Vector3(0.0f, 0.0f, 1.0f), manager.transform[id].rot.y);
            RenderMaths.normalizeVector(direction);

            direction.y = (float) Math.sin(-Camera.getInstance().rotation.x);

            direction = RenderMaths.normalizeVector(direction);
            //initial two pellets
            shoot(manager, id, direction, 150.0f, 2.0f, 20, MusicPlayer.SoundEffect.SHOOT_SHOTGUN);
            shoot(manager, id, direction, 150.0f, 2.0f, 20, MusicPlayer.SoundEffect.SHOOT_SHOTGUN);

            //remaining 8 pellets
            for (int i = 0; i < 8; i++) {
                float factor = 0.6f;
                // generate random, small offset
                float x = (float) Math.random() * 0.1f - 0.05f;
                float y = (float) Math.random() * 0.1f - 0.05f;
                float z = (float) Math.random() * 0.1f - 0.05f;
                shoot(manager, id, RenderMaths.addVectors(direction, new Vector3(x * factor, y * factor, z * factor)), 150.0f, 2.0f, 20, MusicPlayer.SoundEffect.SHOOT_SHOTGUN);
            }
        }

        private void knife(EntityManager manager, int id) {
            if (knifing)
                return;
            if (knifeCooldown <= 0.0f) {
                weaponRenderType = manager.rendering[id].renderType;
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Hide;
                manager.rendering[id].mesh.updateRenderType(GameComponents.Rendering.RenderType.Hide);
                knifing = true;
                knifeCooldown = knifeTime;
                manager.transform[knife].pos = Camera.getInstance().position.clone();
                manager.collider[knife].center = manager.transform[knife].pos;
                manager.rendering[knife].renderType = GameComponents.Rendering.RenderType.Textured;
                manager.rendering[knife].mesh.updateRenderType(GameComponents.Rendering.RenderType.Textured);

                // get direction of camera
                Vector3 direction = RenderMaths.rotateVectorY(new Vector3(0.0f, 0.0f, 3.0f), Camera.getInstance().rotation.y);

                // offset the knife
                manager.transform[knife].pos = RenderMaths.addVectors(Camera.getInstance().position.clone(), direction);
                manager.collider[knife].center = manager.transform[knife].pos;
                manager.transform[knife].pos.y -= 0.13f;


                // set rotation
                manager.transform[knife].rot = Camera.getInstance().rotation.clone();

                // rotate direction 90 degrees
                direction = RenderMaths.rotateVectorY(direction, 3.1415f / 2.0f);
                direction = RenderMaths.multiplyVector(direction, 0.3f);
                //offset the knife 0.5f to the right
                manager.transform[knife].pos = RenderMaths.addVectors(manager.transform[knife].pos, direction);
                manager.collider[knife].center = manager.transform[knife].pos;
                knifeDir = direction;

                MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.Knife);
            }
        }

        private void handleKnife(EntityManager manager, int id) {
            // check if knife is colliding with enemy
            for (CollisionInformation.CollisionEvent event : manager.collisionList.get(knife).collisionEvents) {
                // System.out.println("Collision with: " + event.entityIDs.getFirst() + " " + event.entityIDs.getSecond());
                if (event.entityIDs.getFirst() == knife) {
                    reactToKnifeCollision(manager, knife, event.entityIDs.getSecond());

                } else if (event.entityIDs.getSecond() == knife) {

                    reactToKnifeCollision(manager, knife, event.entityIDs.getFirst());
                }
            }
        }

        private void reactToKnifeCollision(EntityManager manager, int knife, int otherId) {
            GameComponents.Collider.ColliderTag tag = manager.collider[otherId].colliderTag;
            switch (tag) {
                case ENEMY -> {
                    manager.damageable[otherId].health -= 50;
                    System.out.println("Enemy health: " + manager.damageable[otherId].health);
                    if(!DamageSystem.damagedEntities.contains(otherId))
                        DamageSystem.damagedEntities.add(otherId);
                }
            }
        }

        private void animateKnife(EntityManager manager, int id, float deltaTime) {
            //float y = (knifeTime-knifeCooldown) * 2.0f;

            if (knifeCooldown < 0.0f) {
                knifing = false;
                manager.transform[knife].pos = new Vector3(0.0f, 0.1f, 0.0f);
                manager.collider[knife].center = manager.transform[knife].pos;
                manager.rendering[knife].modelPosition = new Vector3(0.0f, 0.0f, 0.0f);
                manager.rendering[id].renderType = weaponRenderType;
                manager.rendering[knife].renderType = GameComponents.Rendering.RenderType.Hide;

                manager.rendering[knife].mesh.updateRenderType(GameComponents.Rendering.RenderType.Hide);
                manager.rendering[id].mesh.updateRenderType(weaponRenderType);
                return;
            }

            manager.rendering[knife].modelPosition = RenderMaths.addVectors(manager.rendering[knife].modelPosition, RenderMaths.multiplyVector(knifeDir, -deltaTime * 4.0f));


        }

        private void shoot(EntityManager manager, int id, Vector3 direction, float speed, float lifeTime, int damage, MusicPlayer.SoundEffect soundEffect) {
            int bulletId = manager.createEntity(GameComponents.TRANSFORM | GameComponents.PHYSICSBODY | GameComponents.RENDER | GameComponents.BULLET | GameComponents.COLLIDER);
            if (bulletId > -1) {

                try {
                    manager.bullet[bulletId].shooter = GameComponents.Bullet.ShooterType.PLAYER;
                    manager.transform[bulletId].pos = Camera.getInstance().position.clone();
                    manager.transform[bulletId].pos.y += 0.065f;
                    manager.transform[bulletId].rot = manager.transform[id].rot.clone();
                    manager.transform[bulletId].scale = new Vector3(0.08f, 0.08f, 0.08f);
                    manager.physicsBody[bulletId].mass = 0.1f;
                    manager.bullet[bulletId].direction = direction;
                    manager.rendering[bulletId].mesh = new Mesh("./src/objects/guns/bullets/bullet.obj", Color.RED);
                    manager.rendering[bulletId].renderType = GameComponents.Rendering.RenderType.OneColor;
                    manager.rendering[bulletId].modelRotation = new Vector3(0.0f, 3.1415f / -2.0f, 0.0f);
                    manager.physicsBody[bulletId].speed = speed;
                    manager.bullet[bulletId].lifeTime = bulletLifeTime;
                    manager.bullet[bulletId].damage = damage;
                    manager.collider[bulletId].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                    manager.collider[bulletId].center = manager.transform[bulletId].pos;
                    manager.collider[bulletId].colliderSize = new Vector3(0.2f, 0.2f, 0.2f);
                    manager.collider[bulletId].colliderTag = GameComponents.Collider.ColliderTag.BULLET;

                    MusicPlayer.getInstance().playSound(soundEffect);

                } catch (Exception e) {
                    System.out.println("Error creating bullet");
                }
            }
        }

        private void handleCollision(EntityManager entityManager, int id) {
            for (CollisionInformation.CollisionEvent event : entityManager.collisionList.get(id).collisionEvents) {
                if (event.entityIDs.getFirst() == id) {
                    reactToCollisionTag(entityManager, id, event.entityIDs.getSecond());

                } else if (event.entityIDs.getSecond() == id) {

                    reactToCollisionTag(entityManager, id, event.entityIDs.getFirst());
                }
            }
        }

        private void reactToCollisionTag(EntityManager entityManager, int playerId, int otherId) {
            try {
                GameComponents.Collider.ColliderTag tag = entityManager.collider[otherId].colliderTag;
                switch (tag) {

                    case ENEMY -> {

                    }
                    case PICKUPWEAPON -> {
                        try {
                            changeWeapon(entityManager.pickupWeapon[otherId].weaponType, entityManager, playerId);
                        } catch (Exception e) {
                            System.out.println("oops");
                        }
                        if (entityManager.pickupWeapon[otherId].weaponType != GameComponents.PlayerMovement.WeaponType.HEALTHPACK) {
                            int randomInt = randomS.nextInt(5);
                            if (randomInt == 0) {
                                MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.BIGGER_GUN);
                            }
                        }

                        entityManager.destroyEntity(otherId);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reacting to collision tag adds");
            }
        }

        private void changeWeapon(GameComponents.PlayerMovement.WeaponType weaponType, EntityManager manager, int id) throws IOException {
            DrawingWindow.snipe = false;

            switch (weaponType) {
                case PISTOL -> {
                    manager.playerMovement[id].weaponType = GameComponents.PlayerMovement.WeaponType.PISTOL;
                    manager.transform[id].scale = new Vector3(.2f, 0.2f, 0.2f);
                    manager.rendering[id].mesh = new Mesh("./src/objects/guns/pistol/startPistol.obj", "./src/objects/guns/pistol/startPistol128.png");
                    manager.rendering[id].mesh.updateRenderType(GameComponents.Rendering.RenderType.Textured);
                    manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured;
                    manager.rendering[id].modelTranslation = new Vector3(-0.5f, -0.7f, 1.0f);
                    manager.rendering[id].modelRotation = new Vector3(0.0f, 3.0f, 0.0f);

                    MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.PICKUP_PISTOL);

                    DrawingWindow.weaponType = GameComponents.PlayerMovement.WeaponType.PISTOL;
                }
                case MACHINE_GUN -> {
                    manager.playerMovement[id].weaponType = GameComponents.PlayerMovement.WeaponType.MACHINE_GUN;
                    manager.transform[id].scale = new Vector3(.05f, 0.05f, 0.05f);
                    manager.rendering[id].mesh = new Mesh("./src/objects/guns/machineGun/AKM.obj", "./src/objects/guns/machineGun/AKM128.png");
                    manager.rendering[id].mesh.updateRenderType(GameComponents.Rendering.RenderType.Textured);
                    manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured;
                    manager.rendering[id].modelTranslation = new Vector3(-0.5f, -0.5f, 1.9f);
                    manager.rendering[id].modelRotation = new Vector3(0.0f, 0.0f, 0.0f);

                    MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.PICKUP_AK);

                    DrawingWindow.weaponType = GameComponents.PlayerMovement.WeaponType.MACHINE_GUN;
                }
                case SHOTGUN -> {
                    manager.playerMovement[id].weaponType = GameComponents.PlayerMovement.WeaponType.SHOTGUN;
                    manager.transform[id].scale = new Vector3(.05f, 0.05f, 0.05f);
                    manager.rendering[id].mesh = new Mesh("./src/objects/guns/shotgun/superShotgun.obj", "./src/objects/guns/shotgun/Shotgun128.png");
                    manager.rendering[id].mesh.updateRenderType(GameComponents.Rendering.RenderType.Textured);
                    manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured;
                    manager.rendering[id].modelTranslation = new Vector3(-0.5f, -0.7f, 1.9f);
                    manager.rendering[id].modelRotation = new Vector3(0.0f, 0.0f, 0.0f);

                    MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.PICKUP_SHOTGUN);

                    DrawingWindow.weaponType = GameComponents.PlayerMovement.WeaponType.SHOTGUN;
                }
                case SNIPER -> {
                    manager.playerMovement[id].weaponType = GameComponents.PlayerMovement.WeaponType.SNIPER;
                    manager.transform[id].scale = new Vector3(.04f, 0.04f, 0.04f);
                    manager.rendering[id].mesh = new Mesh("./src/objects/guns/sniper/AWP.obj", "./src/objects/guns/sniper/Sniper128.png");
                    manager.rendering[id].mesh.updateRenderType(GameComponents.Rendering.RenderType.Textured);
                    manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured;
                    manager.rendering[id].modelTranslation = new Vector3(-0.5f, -0.7f, 2.6f);
                    manager.rendering[id].modelRotation = new Vector3(0.0f, 0.0f, 0.0f);

                    MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.PICKUP_SNIPER);

                    DrawingWindow.weaponType = GameComponents.PlayerMovement.WeaponType.SNIPER;
                }
                case HEALTHPACK -> {
                    int healthIncrease = 20;
                    manager.damageable[id].health += healthIncrease;
                    if (manager.damageable[id].health > 100)
                        manager.damageable[id].health = 100;

                    DrawingWindow.playerHealth = manager.damageable[id].health;
                    System.out.println("Health: " + manager.damageable[id].health);
                    MusicPlayer.getInstance().playSound("src/sound/misc/heal.wav");
                }
            }
        }


    }

    public static class PyhsicsHandler extends GameSystem {
        @Override
        public void start(EntityManager manager) {

        }

        @Override
        public void update(EntityManager manager, float deltaTime) {

            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.PHYSICSBODY;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {

                    // Apply gravity -> change force
                    if (manager.playerMovement[i] != null) {

                        manager.physicsBody[i].force.y -= 9.81f * manager.physicsBody[i].mass;
                        handlePlayerCollision(manager, i);
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

        private void handlePlayerCollision(EntityManager manager, int id) {
            //System.out.println(manager.collisionList.size());

            if (manager.playerMovement[id] == null) {
                return;
            }

            for (CollisionInformation.CollisionEvent event : manager.collisionList.get(id).collisionEvents) {
                if (event.entityIDs.getFirst() == id) {
                    reactToCollisionTagPlayer(manager, id, event.entityIDs.getSecond());

                } else if (event.entityIDs.getSecond() == id) {

                    reactToCollisionTagPlayer(manager, id, event.entityIDs.getFirst());
                }
            }
        }

        private void reactToCollisionTagPlayer(EntityManager manager, int playerId, int otherId) {
            try {
                GameComponents.Collider.ColliderTag tag = manager.collider[otherId].colliderTag;
                switch (tag) {
                    case GROUND -> {
                        if (manager.physicsBody[playerId].velocity.y < 0.0f) {
                            manager.physicsBody[playerId].velocity.y = 0.0f;
                        }
                        if (manager.physicsBody[playerId].force.y < 0.0f) {
                            manager.physicsBody[playerId].force.y = 0.0f;
                        }
                    }
                    case OBSTACLE -> {
                        // get center of obstacle
                        Vector3 center = manager.collider[otherId].center;
                        Vector3 playerPos = manager.transform[playerId].pos;

                        Vector3 direction = RenderMaths.substractVectors(playerPos, center);

                        direction = RenderMaths.normalizeVector(direction);

                        manager.physicsBody[playerId].force.x = direction.x * 100.0f;
                        manager.physicsBody[playerId].force.z = direction.z * 100.0f;


                    }
                    case PLAYER -> {
                        Vector3 center = manager.collider[otherId].center;
                        Vector3 playerPos = manager.transform[playerId].pos;

                        Vector3 direction = RenderMaths.substractVectors(playerPos, center);

                        direction = RenderMaths.normalizeVector(direction);

                        manager.physicsBody[playerId].force.x = direction.x * 100.0f;
                        manager.physicsBody[playerId].force.z = direction.z * 100.0f;

                        DamageSystem.damagedEntities.add(playerId);
                        System.out.println("Playercollision " + manager.damageable[playerId].health);
                    }

                    case ENEMY -> {
                        System.out.println("colliding with enemy");
                        switch (manager.aiBehavior[otherId].enemyType) {
                            case SIGHTSEEKER -> {
                                manager.damageable[playerId].health -= manager.aiBehavior[otherId].damage;
                                manager.damageable[otherId].health -= manager.damageable[otherId].health;
                                if (!DamageSystem.damagedEntities.contains(otherId))
                                    DamageSystem.damagedEntities.add(otherId);

                                MusicPlayer.getInstance().playRandomPlayerSound();
                                MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.SIGHTSEEKER_DEATH);

                            }
                            case GROUNDENEMY, GUNTURRED, MALTESEEKER -> {
                                Vector3 center = manager.collider[otherId].center;
                                Vector3 playerPos = manager.transform[playerId].pos;

                                Vector3 direction = RenderMaths.substractVectors(playerPos, center);

                                direction = RenderMaths.normalizeVector(direction);

                                manager.physicsBody[playerId].force.x = direction.x * 100.0f;
                                manager.physicsBody[playerId].force.z = direction.z * 100.0f;
                            }
                        }

                        DrawingWindow.playerHealth = manager.damageable[playerId].health;

                        DamageSystem.damagedEntities.add(playerId);
                        System.out.println("Playercollision " + manager.damageable[playerId].health);
                    }
                }
            } catch (Exception e) {
                System.out.println("ouoiuoiuoi");
            }

        }
    }


    public static class BulletSystem extends GameSystem {

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
                    if (manager.bullet[i].lifeTime <= 0.0f) {
                        manager.destroyEntity(i);
                        continue;
                    }

                    handleBulletCollision(manager, i);

                    // add force to the bullet
                    manager.physicsBody[i].force.x = manager.bullet[i].direction.x * manager.physicsBody[i].mass * manager.physicsBody[i].speed;
                    manager.physicsBody[i].force.y = manager.bullet[i].direction.y * manager.physicsBody[i].mass * manager.physicsBody[i].speed;
                    manager.physicsBody[i].force.z = manager.bullet[i].direction.z * manager.physicsBody[i].mass * manager.physicsBody[i].speed;

                    // interpolate bullet position and follow position fully within the first 0.5 seconds of lifetime

                }
            }

        }

        private void handleBulletCollision(EntityManager manager, int id) {
            for (CollisionInformation.CollisionEvent event : manager.collisionList.get(id).collisionEvents) {
                if (event.entityIDs.getFirst() == id) {
                    reactToCollisionTagBullet(manager, id, event.entityIDs.getSecond());

                } else if (event.entityIDs.getSecond() == id) {

                    reactToCollisionTagBullet(manager, id, event.entityIDs.getFirst());
                }
            }
        }

        private void reactToCollisionTagBullet(EntityManager manager, int bulletId, int otherID) {

            try {
                if (manager.damageable[otherID] != null) {
                    if (!((manager.bullet[bulletId].shooter == GameComponents.Bullet.ShooterType.ENEMY && manager.collider[otherID].colliderTag == GameComponents.Collider.ColliderTag.ENEMY)
                            || (manager.bullet[bulletId].shooter == GameComponents.Bullet.ShooterType.PLAYER && manager.collider[otherID].colliderTag == GameComponents.Collider.ColliderTag.PLAYER))) {
                        System.out.println(manager.damageable[otherID].health);
                        if (!DamageSystem.damagedEntities.contains(otherID))
                            DamageSystem.damagedEntities.add(otherID);

                        manager.damageable[otherID].health -= manager.bullet[bulletId].damage;
                        if (manager.collider[otherID].colliderTag == GameComponents.Collider.ColliderTag.PLAYER) {
                            MusicPlayer.getInstance().playRandomPlayerSound();
                            DrawingWindow.playerHealth = manager.damageable[otherID].health;
                        }
                        System.out.println("Bullet hit: " + manager.collider[otherID].colliderTag.name());

                        manager.destroyEntity(bulletId);
                    }
                } else if (manager.collider[otherID].colliderTag == GameComponents.Collider.ColliderTag.OBSTACLE) {
                    manager.destroyEntity(bulletId);
                }

            } catch (Exception e) {
                System.out.println("beeeenis");
            }

        }
    }

    public static class DamageSystem extends GameSystem {

        public static ArrayList<Integer> damagedEntities = new ArrayList<>();

        @Override
        public void start(EntityManager manager) throws Exception {

        }

        @Override
        public void update(EntityManager manager, float deltaTime) throws Exception {
            // iterate over all damageable entities
            for (Integer id : damagedEntities) {
                switch (manager.collider[id].colliderTag) {
                    case PLAYER:
                        System.out.println("Player got hit");
                        if (manager.damageable[id].health <= 0) {
                            DrawingWindow.playerHealth = 0;
                            EventSystem.getInstance().onPlayerDeath();
                            manager.destroyEntity(id);
                        }
                        break;
                    case ENEMY:
                        System.out.println("Enemy got hit");
                        if (manager.damageable[id].health <= 0) {
                            manager.destroyEntity(id);
                            EventSystem.getInstance().onKillEnemy(manager, manager.aiBehavior[id].enemyType);
                        }
                        break;
                    default:
                        if (manager.damageable[id].health <= 0) {
                            manager.destroyEntity(id);
                        }
                        break;
                }
            }

            // flush damage list
            damagedEntities.clear();
        }
    }

    public static class PickupWeapon extends GameSystem {
        @Override
        public void start(EntityManager manager) throws Exception {

        }

        @Override
        public void update(EntityManager manager, float deltaTime) throws Exception {

            int required_GameComponents = GameComponents.PICKUPWEAPON | GameComponents.TRANSFORM | GameComponents.RENDER;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    manager.transform[i].rot.y += deltaTime * 2.0f;
                }
            }

        }
    }

    public static class EnemySystem extends GameSystem {
        private Vector3 playerPosition = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        private Vector3 distanceVectorPlayerEnemy = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        private final Random random = new Random();

        @Override
        public void start(EntityManager manager) throws Exception {
            for (int i = 0; i < manager.size; i++) {
                if (manager.playerMovement[i] != null) {
                    playerPosition = manager.transform[i].pos;
                }
            }
        }

        @Override
        public void update(EntityManager manager, float deltaTime) throws Exception {
            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PHYSICSBODY | GameComponents.COLLIDER | GameComponents.AIBEHAVIOR;
            for (int i = 0; i < manager.size; i++) {
                if (manager.playerMovement[i] != null) {
                    playerPosition = manager.transform[i].pos;
                }
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    switch (manager.aiBehavior[i].currentState) {
                        case COLLIDED:
                            updateAI(manager, i, deltaTime);
                            if (manager.aiBehavior[i].colliderbounceTime > 0) {
                                break;
                            }
                        case WANDERING, CHASING, ATTACKING:
                            distanceVectorPlayerEnemy = playerPosition.subtract(manager.transform[i].pos);
                            if (!(manager.aiBehavior[i].enemyType == GameComponents.EnemyType.MALTESEEKER && manager.aiBehavior[i].currentState == GameComponents.State.WANDERING)) {

                                float distancePlayerEnemy = distanceVectorPlayerEnemy.length();
                                if (distancePlayerEnemy > manager.aiBehavior[i].chasingDistance) {
                                    manager.aiBehavior[i].currentState = GameComponents.State.WANDERING;
                                } else if (distancePlayerEnemy > manager.aiBehavior[i].attackingDistance) {
                                    manager.aiBehavior[i].currentState = GameComponents.State.CHASING;
                                } else {
                                    manager.aiBehavior[i].currentState = GameComponents.State.ATTACKING;

                                }
                            } else {
                                if (manager.damageable[i].health < 100) {
                                    if (manager.aiBehavior[i].currentState == GameComponents.State.WANDERING) {
                                        MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.MALTESEEKER_SPAWN_AND_ATTACK);
                                    }
                                    manager.aiBehavior[i].currentState = GameComponents.State.CHASING;
                                }
                            }
                            updateAI(manager, i, deltaTime);
                            break;
                        case DEACTIVATED:
                            break;
                    }
                }
            }
        }

        private void updateAI(EntityManager manager, int entityId, float deltaTime) {
            switch (manager.aiBehavior[entityId].currentState) {
                case WANDERING:
                    switch (manager.aiBehavior[entityId].enemyType) {
                        case SIGHTSEEKER -> {
                            handleWanderingSightSeeker(manager, entityId, deltaTime);
                            rotateEnemy(manager, entityId, deltaTime, manager.aiBehavior[entityId].wanderingDirection);
                            calculateCollision(manager, entityId, deltaTime);

                        }
                        case GROUNDENEMY -> {
                            handleWanderingGroundEnemy(manager, entityId, deltaTime);
                            rotateEnemy(manager, entityId, deltaTime, manager.aiBehavior[entityId].wanderingDirection);
                            calculateCollision(manager, entityId, deltaTime);

                        }
                        case GUNTURRED -> {
                            rotateEnemy(manager, entityId, deltaTime, distanceVectorPlayerEnemy);

                        }
                        case MALTESEEKER -> {
                            handleWanderingMalteSightSeeker(manager, entityId, deltaTime);
                            rotateEnemy(manager, entityId, deltaTime, manager.aiBehavior[entityId].wanderingDirection);
                            calculateCollision(manager, entityId, deltaTime);
                            malteSeekerSoundHandle(manager, entityId, deltaTime);

                        }
                    }
                    break;
                case CHASING:
                    switch (manager.aiBehavior[entityId].enemyType) {
                        case SIGHTSEEKER -> {
                            handleChasingSightSeeker(manager, entityId, deltaTime);
                            rotateEnemy(manager, entityId, deltaTime, distanceVectorPlayerEnemy);
                            calculateCollision(manager, entityId, deltaTime);

                        }
                        case GROUNDENEMY -> {
                            handleChasingGroundEnemy(manager, entityId, deltaTime);
                            rotateEnemy(manager, entityId, deltaTime, distanceVectorPlayerEnemy);
                            handleAttacking(manager, entityId, deltaTime);
                            calculateCollision(manager, entityId, deltaTime);

                        }
                        case GUNTURRED -> {
                            rotateEnemy(manager, entityId, deltaTime, distanceVectorPlayerEnemy);
                        }
                        case MALTESEEKER -> {
                            handleChasingSightSeeker(manager, entityId, deltaTime);
                            rotateEnemy(manager, entityId, deltaTime, distanceVectorPlayerEnemy);
                            handleAttacking(manager, entityId, deltaTime);
                            calculateCollision(manager, entityId, deltaTime);
                            malteSeekerSoundHandle(manager, entityId, deltaTime);

                        }
                    }
                    break;
                case ATTACKING:
                    switch (manager.aiBehavior[entityId].enemyType) {
                        case MALTESEEKER:
                            malteSeekerSoundHandle(manager, entityId, deltaTime);
                        default:
                            rotateEnemy(manager, entityId, deltaTime, distanceVectorPlayerEnemy);
                            handleAttacking(manager, entityId, deltaTime);
                            break;
                    }
                case COLLIDED:
                    manager.aiBehavior[entityId].colliderbounceTime -= deltaTime;
                    break;
            }
        }

        private void handleWanderingMalteSightSeeker(EntityManager manager, int entityId, float deltaTime) {
            float maxWanderingDuration = 5f;
            GameComponents.PhysicsBody physicsBody = manager.physicsBody[entityId];
            GameComponents.AIBEHAVIOR aibehavior = manager.aiBehavior[entityId];

            if (aibehavior.timeSinceLastDirectionChange > aibehavior.wanderingDuration) {
                calculateWanderingDirection(manager, entityId, deltaTime);
                aibehavior.timeSinceLastDirectionChange = 0;
                aibehavior.wanderingDuration = (float) (Math.random() * maxWanderingDuration);
            } else {
                manager.aiBehavior[entityId].timeSinceLastDirectionChange += deltaTime;
                physicsBody.velocity = new Vector3(aibehavior.wanderingDirection.x * manager.physicsBody[entityId].speed, 0f, aibehavior.wanderingDirection.z * manager.physicsBody[entityId].speed);

            }
        }

        private void handleWanderingSightSeeker(EntityManager manager, int entityId, float deltaTime) {
            float maxWanderingDuration = 5f;
            GameComponents.PhysicsBody physicsBody = manager.physicsBody[entityId];
            GameComponents.AIBEHAVIOR aibehavior = manager.aiBehavior[entityId];

            if (aibehavior.timeSinceLastDirectionChange > aibehavior.wanderingDuration) {
                calculateWanderingDirection(manager, entityId, deltaTime);
                aibehavior.timeSinceLastDirectionChange = 0;
                aibehavior.wanderingDuration = (float) (Math.random() * maxWanderingDuration);
            } else {
                manager.aiBehavior[entityId].timeSinceLastDirectionChange += deltaTime;
                physicsBody.velocity = new Vector3(aibehavior.wanderingDirection.x, 0f, aibehavior.wanderingDirection.z);
                switch (random.nextInt(16)) {
                    case 1:
                        MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.RELOAD_PISTOL);
                        break;
                    case 2:
                        MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.RELOAD_PISTOL);
                        break;

                }
            }
        }

        private void handleWanderingGroundEnemy(EntityManager manager, int entityId, float deltaTime) {
            float maxWanderingDuration = 5f;
            GameComponents.PhysicsBody physicsBody = manager.physicsBody[entityId];
            GameComponents.AIBEHAVIOR aibehavior = manager.aiBehavior[entityId];

            if (aibehavior.timeSinceLastDirectionChange > aibehavior.wanderingDuration) {
                calculateWanderingDirection(manager, entityId, deltaTime);
                aibehavior.timeSinceLastDirectionChange = 0;
                aibehavior.wanderingDuration = (float) (Math.random() * maxWanderingDuration);
            } else {
                manager.aiBehavior[entityId].timeSinceLastDirectionChange += deltaTime;
                physicsBody.velocity = new Vector3(aibehavior.wanderingDirection.x, 0f, aibehavior.wanderingDirection.z);
            }
        }

        private void calculateWanderingDirection(EntityManager manager, int entityId, float deltaTime) {
            if (manager.aiBehavior[entityId].chooseWanderingCounter < random.nextInt(2)) {
                manager.aiBehavior[entityId].chooseWanderingCounter++;
                float angle = (float) (Math.random() * 2 * Math.PI);
                manager.aiBehavior[entityId].wanderingDirection = new Vector3(
                        (float) Math.cos(angle) * manager.physicsBody[entityId].speed * 0.25f,
                        0.0f,
                        (float) Math.sin(angle) * manager.physicsBody[entityId].speed * 0.25f
                );
            } else {
                manager.aiBehavior[entityId].chooseWanderingCounter = 0;
                Vector3 normalizedVectorToSpawn = RenderMaths.normalizeVector(manager.aiBehavior[entityId].spawnPoint.subtract(manager.transform[entityId].pos));
                manager.aiBehavior[entityId].wanderingDirection = new Vector3(
                        normalizedVectorToSpawn.x * 0.25f * manager.physicsBody[entityId].speed,
                        normalizedVectorToSpawn.y * 0.25f * manager.physicsBody[entityId].speed,
                        normalizedVectorToSpawn.z * 0.25f * manager.physicsBody[entityId].speed
                );

            }
        }

        private void rotateEnemy(EntityManager manager, int entityId, float deltaTime, Vector3 direction) {
            float currentAngleY = manager.transform[entityId].rot.y;

            Vector3 normalizedDirection = RenderMaths.normalizeVector(direction);

            float desiredAngleY = (float) Math.atan2(normalizedDirection.z, normalizedDirection.x);
            desiredAngleY = (desiredAngleY + (float) Math.PI * 2) % ((float) Math.PI * 2);

            currentAngleY = (currentAngleY + (float) Math.PI * 2) % ((float) Math.PI * 2);

            float angleDifference = desiredAngleY - currentAngleY;
            angleDifference = (angleDifference + (float) Math.PI) % ((float) Math.PI * 2) - (float) Math.PI;

            float rotationAmount = angleDifference;

            currentAngleY += rotationAmount;

            switch (manager.aiBehavior[entityId].enemyType) {
                case GUNTURRED, SIGHTSEEKER:
                    manager.transform[entityId].rot.y = currentAngleY + (float) Math.PI;
                    break;
                case GROUNDENEMY:
                    manager.transform[entityId].rot.y = currentAngleY + (float) (Math.PI / 2);
                    break;
                case MALTESEEKER:
                    manager.transform[entityId].rot.y = currentAngleY - (float) (Math.PI / 2);

            }
        }


        private void handleChasingSightSeeker(EntityManager manager, int entityId, float deltaTime) {
            Vector3 vector3 = RenderMaths.normalizeVector(distanceVectorPlayerEnemy);
            manager.physicsBody[entityId].velocity = new Vector3(
                    vector3.x * manager.physicsBody[entityId].speed,
                    (vector3.y - 0.1f) * manager.physicsBody[entityId].speed,
                    vector3.z * manager.physicsBody[entityId].speed
            );
        }

        private void handleChasingGroundEnemy(EntityManager manager, int entityId, float deltaTime) {
            Vector3 vector3 = RenderMaths.normalizeVector(distanceVectorPlayerEnemy);
            manager.physicsBody[entityId].velocity = new Vector3(
                    vector3.x * manager.physicsBody[entityId].speed,
                    manager.transform[entityId].pos.y - manager.aiBehavior[entityId].spawnPoint.y,
                    vector3.z * manager.physicsBody[entityId].speed
            );
        }

        private void handleAttacking(EntityManager manager, int entityId, float deltaTime) {
            manager.aiBehavior[entityId].shootingCooldown -= deltaTime;

            switch (manager.aiBehavior[entityId].enemyType) {
                case SIGHTSEEKER -> {
                    if (manager.aiBehavior[entityId].shootingCooldown <= 0.0f) {
                        sightseekerShotHandler(manager, entityId, deltaTime);
                    }
                }
                case GUNTURRED -> {
                    if (manager.aiBehavior[entityId].shootingCooldown <= 0.0f) {
                        gunturredShotHandler(manager, entityId, deltaTime);
                    }
                }
                case GROUNDENEMY -> {
                    if (manager.aiBehavior[entityId].shootingCooldown <= 0.0f) {
                        groundenemyShotHandler(manager, entityId, deltaTime);
                    }
                }
                case MALTESEEKER -> {
                    if (manager.aiBehavior[entityId].shootingCooldown <= 0.0f) {
                        malteseekerShotHandler(manager, entityId, deltaTime);
                    }
                }
            }
        }

        private void sightseekerShotHandler(EntityManager manager, int id, float deltaTime) {
            // 1. set cooldown
            manager.aiBehavior[id].shootingCooldown = 3f;
            //Bullet Spawnpoint adaption
            float yOffSet = 0f;
            //Scattering factor
            float factor = 0.7f;
            // generate random, small offset
            float x = (float) Math.random() * 0.1f - 0.01f;
            float y = (float) Math.random() * 0.1f - 0.01f;
            float z = (float) Math.random() * 0.1f - 0.01f;

            Vector3 normalizeVector = RenderMaths.normalizeVector(playerPosition.subtract(new Vector3(manager.transform[id].pos.x, manager.transform[id].pos.y + yOffSet, manager.transform[id].pos.z)));
            Vector3 direction = new Vector3(
                    normalizeVector.x + x * factor,
                    normalizeVector.y + y * factor,
                    normalizeVector.z + z * factor
            );


            shoot(manager, direction, id, 150.0f, 2.0f, manager.aiBehavior[id].damage, MusicPlayer.SoundEffect.SIGHSEEKER_ATTACK, yOffSet);

        }

        private void gunturredShotHandler(EntityManager manager, int id, float deltaTime) {
            // 1. set cooldown
            manager.aiBehavior[id].shootingCooldown = 3f;
            //Bullet Spawnpoint adaption
            float yOffSet = 0f;
            float leftRightOffset = 0.0f;

            if (manager.aiBehavior[id].leftRotated) {
                // offset the bullet to the right
                leftRightOffset += 1.5f;
            } else {

            }
            //Scattering factor
            float factor = 0.4f;
            // generate random, small offset
            float x = (float) Math.random() * 0.01f - 0.005f;
            float y = (float) Math.random() * 0.01f - 0.005f;
            float z = (float) Math.random() * 0.01f - 0.005f;

            Vector3 normalizeVector = RenderMaths.normalizeVector(playerPosition.subtract(new Vector3(manager.transform[id].pos.x, manager.transform[id].pos.y + yOffSet, manager.transform[id].pos.z)));
            Vector3 direction = new Vector3(
                    normalizeVector.x + x * factor,
                    normalizeVector.y + y * factor,
                    normalizeVector.z + z * factor
            );

            Vector3 offset = new Vector3();
            // direction is bullet forward vector... put it to the right


            shoot(manager, direction, id, 150.0f, 2.0f, manager.aiBehavior[id].damage, MusicPlayer.SoundEffect.SHOOT_SNIPER, yOffSet);

        }

        private void groundenemyShotHandler(EntityManager manager, int id, float deltaTime) {
            // 1. set cooldown
            manager.aiBehavior[id].shootingCooldown = 0.8f;
            //Bullet Spawnpoint adaption
            float yOffSet = 2f;
            //Scattering factor
            float factor = 0.1f;
            // generate random, small offset
            float x = (float) Math.random() * 0.4f - 0.2f;
            float y = (float) Math.random() * 0.4f - 0.2f;
            float z = (float) Math.random() * 0.4f - 0.2f;

            Vector3 normalizeVector = RenderMaths.normalizeVector(playerPosition.subtract(new Vector3(manager.transform[id].pos.x, manager.transform[id].pos.y + yOffSet, manager.transform[id].pos.z)));
            Vector3 direction = new Vector3(
                    normalizeVector.x + x * factor,
                    normalizeVector.y + y * factor,
                    normalizeVector.z + z * factor
            );

            shoot(manager, direction, id, 150.0f, 2.0f, manager.aiBehavior[id].damage, MusicPlayer.SoundEffect.SHOOT_AK, yOffSet);

        }

        private void malteseekerShotHandler(EntityManager manager, int id, float deltaTime) {
            // 1. set cooldown
            manager.aiBehavior[id].shootingCooldown = 0.8f;
            //Bullet Spawnpoint adaption
            float yOffSet = 0.75f;
            //Scattering factor
            float factor = 0.1f;
            // generate random, small offset
            float x = (float) Math.random() * 0.4f - 0.2f;
            float y = (float) Math.random() * 0.4f - 0.2f;
            float z = (float) Math.random() * 0.4f - 0.2f;

            Vector3 normalizeVector = RenderMaths.normalizeVector(playerPosition.subtract(new Vector3(manager.transform[id].pos.x, manager.transform[id].pos.y + yOffSet, manager.transform[id].pos.z)));
            Vector3 direction = new Vector3(
                    normalizeVector.x + x * factor,
                    normalizeVector.y + y * factor,
                    normalizeVector.z + z * factor
            );

            shoot(manager, direction, id, 150.0f, 2.0f, manager.aiBehavior[id].damage, MusicPlayer.SoundEffect.SHOOT_AK, yOffSet);

        }

        private void shoot(EntityManager manager, Vector3 direction, int id, float speed, float lifeTime,
                           int damage, MusicPlayer.SoundEffect soundEffect, float yOffSet) {
            int bulletId = manager.createEntity(GameComponents.TRANSFORM | GameComponents.PHYSICSBODY | GameComponents.RENDER | GameComponents.BULLET | GameComponents.COLLIDER);
            if (bulletId > -1) {

                try {
                    manager.bullet[bulletId].shooter = GameComponents.Bullet.ShooterType.ENEMY;
                    manager.transform[bulletId].pos = RenderMaths.addVectors(manager.transform[id].pos.clone(), RenderMaths.multiplyVector(RenderMaths.normalizeVector(direction.clone()), 1.65f));
                    manager.transform[bulletId].pos.y += yOffSet;
                    manager.transform[bulletId].rot = manager.transform[id].rot.clone();

                    switch (manager.aiBehavior[id].enemyType) {
                        case GUNTURRED:
                            manager.transform[bulletId].rot.y -= (float) Math.PI / 2;
                            break;
                        case GROUNDENEMY:
                            manager.transform[bulletId].rot.y += (float) Math.PI;
                            break;
                    }

                    manager.transform[bulletId].scale = new Vector3(0.13f, 0.13f, 0.13f);
                    manager.physicsBody[bulletId].mass = 0.1f;
                    manager.bullet[bulletId].direction = direction;
                    manager.rendering[bulletId].mesh = new Mesh("./src/objects/guns/bullets/bullet.obj", Color.RED);
                    manager.rendering[bulletId].renderType = GameComponents.Rendering.RenderType.OneColor;
                    manager.rendering[bulletId].modelRotation = new Vector3(0.0f, 3.1415f / -2.0f, 0.0f);
                    manager.physicsBody[bulletId].speed = speed;
                    manager.bullet[bulletId].lifeTime = bulletLifeTime;
                    manager.bullet[bulletId].damage = damage;
                    manager.collider[bulletId].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                    manager.collider[bulletId].center = manager.transform[bulletId].pos;
                    manager.collider[bulletId].colliderSize = new Vector3(0.2f, 0.2f, 0.2f);
                    manager.collider[bulletId].colliderTag = GameComponents.Collider.ColliderTag.BULLET;

                    MusicPlayer.getInstance().playSound(soundEffect);

                } catch (Exception e) {
                    System.out.println("Error creating bullet");
                }
            }
        }

        private void calculateCollision(EntityManager manager, int id, float deltatime) {
            if (manager.aiBehavior[id] == null) {
                return;
            }
            try {
                for (CollisionInformation.CollisionEvent event : manager.collisionList.get(id).collisionEvents) {
                    if (event.entityIDs.getFirst() == id) {
                        reactToCollisionTagEnemy(manager, id, event.entityIDs.getSecond(), deltatime);

                    } else if (event.entityIDs.getSecond() == id) {
                        reactToCollisionTagEnemy(manager, id, event.entityIDs.getFirst(), deltatime);
                    }
                }
            } catch (Exception e) {
                System.out.println("Schwau pau wow");
            }
        }

        private void reactToCollisionTagEnemy(EntityManager manager, int enemyId, int otherId, float deltatime) {
            try {
                GameComponents.Collider.ColliderTag tag = manager.collider[otherId].colliderTag;
                switch (tag) {
                    case OBSTACLE -> {
                        // get center of obstacle
                        Vector3 center = manager.collider[otherId].center;
                        Vector3 enemyPos = manager.transform[enemyId].pos;

                        Vector3 direction = RenderMaths.substractVectors(enemyPos, center);

                        direction = RenderMaths.normalizeVector(direction);

                        manager.physicsBody[enemyId].force.x = direction.x * 50.0f;
                        // manager.physicsBody[playerId].force.y = direction.y * 100.0f;
                        manager.physicsBody[enemyId].force.z = direction.z * 50.0f;
                        manager.aiBehavior[enemyId].wanderingDirection = new Vector3(direction.x, 0, direction.z).rotateY((float) Math.PI / 2);
                        //manager.aiBehavior[enemyId].currentState = GameComponents.State.COLLIDED;
                        manager.aiBehavior[enemyId].colliderbounceTime = 2f;
                    }
                }
            } catch (Exception e) {
                System.out.println("Sau now pow");
            }
        }

        private void malteSeekerSoundHandle(EntityManager manager, int id, float deltatime) {
            if (manager.aiBehavior[id].soundCooldown < 0) {
                switch (random.nextInt(4)) {
                    case 1:
                        MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.MALTESEEKER_RANDOMTALK1);
                        break;
                    case 2:
                        MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.MALTESEEKER_RANDOMTALK2);
                        break;
                    case 3:
                        MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.MALTESEEKER_RANDOMTALK3);
                        break;
                }
                manager.aiBehavior[id].soundCooldown = 6;
            } else {
                manager.aiBehavior[id].soundCooldown -= deltatime;
            }
        }
    }


    public static class GameLogicSystem extends GameSystem implements GameEventListener {

        EntityManager localManager;

        int level = 0;
        int score = 0;
        int livingEnemies = 0;
        boolean finishedLevel;

        float finishTimer = 0.0f;
        float cooldown = 7.0f;

        private int spawnedTurrets;
        private int startIndex;

        private Vector3[] spawnPositionsTurret = {
                new Vector3(-10.0f, 1.0f, -2.0f),
                new Vector3(-3.1f, 1.4f, 1.7f),
                new Vector3(9.3f, 1.4f, 9.1f),
                new Vector3(6.5f, 1.4f, 1.7f),
                new Vector3(6.7f, 1.4f, -3.575f),
                new Vector3(10f, 1.4f, -9.1f),
                new Vector3(6.7f, 1.4f, -9.1f),
                new Vector3(5.1f, 1.4f, -11f),
                new Vector3(2.45f, 1.4f, -8.4f)
        };

        private boolean[] leftRotated = {
                true,
                true,
                false,
                false,
                true,
                true,
                true,
                false,
                true
        };


        Vector3 weaponSpawn = new Vector3(0.0f, -0.5f, 0.0f);

        @Override
        public void start(EntityManager manager) throws Exception {
            int required_GameComponents = GameComponents.GAMELOGIC;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    EventSystem.getInstance().addListener(this);
                    level = 1;
                    loadNextLevel(manager);
                    DrawingWindow.currentEnemyCount = livingEnemies;
                    localManager = manager;
                }
            }
        }

        @Override
        public void update(EntityManager manager, float deltaTime) throws Exception {
            int required_GameComponents = GameComponents.GAMELOGIC;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    if (finishedLevel) {
                        if (finishTimer > 0.0f) {
                            finishTimer -= deltaTime;
                            DrawingWindow.levelCooldown = finishTimer;
                            return;
                        }
                        finishedLevel = false;
                        MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.LEVEL_FINISHED);

                        level += 1;

                        DrawingWindow.inGameState = DrawingWindow.InGameStates.DEFAULT;
                        loadNextLevel(manager);
                        DrawingWindow.currentEnemyCount = livingEnemies;
                    }
                }
            }
        }

        @Override
        public void onFinishLevel(int level) {
            finishedLevel = true;
            finishTimer = cooldown;
            DrawingWindow.inGameState = DrawingWindow.InGameStates.WAITINGFORNEXTLEVEL;
            try {
                spawnRandomWeapon(localManager);
            } catch (Exception e) {
                System.out.println("AAAAAAA benis aaa");
            }
        }

        @Override
        public void onPlayerDeath() {


            GameContainer.playerDeath = true;
            MusicPlayer.getInstance().stopGameMusic();

            DrawingWindow.windowState = DrawingWindow.WindowStates.DEATHSCREEN;

            System.out.println("Player died");
            MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.GAME_OVER);
        }

        @Override
        public void onKillEnemy(EntityManager manager, GameComponents.EnemyType enemyType) throws IOException {
            livingEnemies -= 1;
            DrawingWindow.currentEnemyCount = livingEnemies;
            if (enemyType == GameComponents.EnemyType.MALTESEEKER) {
                try {
                    spawnHealthPack(manager);
                } catch (Exception e) {
                    System.out.println("Could not spawn healthpack");
                }
            }
            if (livingEnemies == 0) {
                onFinishLevel(level);
            }
        }

        private void spawnRandomWeapon(EntityManager manager) throws IOException {
            Random rand = new Random();
            int type = rand.nextInt(3);

            switch (type) {
                case 0 -> spawnShotgun(manager);
                case 1 -> spawnMachinegun(manager);
                case 2 -> spawnSniper(manager);
            }

        }

        private void spawnHealthPack(EntityManager manager) throws IOException {
            int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PICKUPWEAPON | GameComponents.COLLIDER);
            if (id > -1) {
                Random rand = new Random();
                int x = 13;
                int z = 13;
                if (rand.nextBoolean())
                    x = -x;

                if (rand.nextBoolean())
                    z = -z;


                manager.rendering[id].mesh = new Mesh("./src/objects/misc/healthPack.obj", "./src/objects/misc/healthPack128.png");
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured;
                manager.transform[id].pos = new Vector3(x, 0.0f, z);
                manager.transform[id].rot = new Vector3(0.0f, -1.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.20f, .20f, .20f);
                manager.rendering[id].mesh.updateRenderType(GameComponents.Rendering.RenderType.Textured);
                manager.pickupWeapon[id].weaponType = GameComponents.PlayerMovement.WeaponType.HEALTHPACK;

                manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                manager.collider[id].colliderSize = new Vector3(3.0f, 2.0f, 2.0f);
                manager.collider[id].center = manager.transform[id].pos;
                manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.PICKUPWEAPON;

            }
        }

        private void spawnShotgun(EntityManager manager) throws IOException {
            int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PICKUPWEAPON | GameComponents.COLLIDER);
            if (id > -1) {
                manager.rendering[id].mesh = new Mesh("./src/objects/guns/shotgun/superShotgun.obj", Color.RED);
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor;
                manager.transform[id].pos = weaponSpawn.clone();
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.025f, .025f, .025f);

                manager.pickupWeapon[id].weaponType = GameComponents.PlayerMovement.WeaponType.SHOTGUN;

                manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                manager.collider[id].colliderSize = new Vector3(2.0f, 2.0f, 2.0f);
                manager.collider[id].center = manager.transform[id].pos;
                manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.PICKUPWEAPON;

            }
        }

        private void spawnMachinegun(EntityManager manager) throws IOException {
            int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PICKUPWEAPON | GameComponents.COLLIDER);
            if (id > -1) {

                manager.rendering[id].mesh = new Mesh("./src/objects/guns/machineGun/AKM.obj", Color.RED);
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor;
                manager.transform[id].pos = weaponSpawn.clone();
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.025f, .025f, .025f);

                manager.pickupWeapon[id].weaponType = GameComponents.PlayerMovement.WeaponType.MACHINE_GUN;

                manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                manager.collider[id].colliderSize = new Vector3(2.0f, 2.0f, 2.0f);
                manager.collider[id].center = manager.transform[id].pos;
                manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.PICKUPWEAPON;

            }
        }

        private void spawnSniper(EntityManager manager) throws IOException {
            int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PICKUPWEAPON | GameComponents.COLLIDER);
            if (id > -1) {
                manager.rendering[id].mesh = new Mesh("./src/objects/guns/sniper/AWP.obj", Color.RED);
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor;
                manager.transform[id].pos = weaponSpawn.clone();
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.015f, .015f, .015f);

                manager.pickupWeapon[id].weaponType = GameComponents.PlayerMovement.WeaponType.SNIPER;

                manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                manager.collider[id].colliderSize = new Vector3(2.0f, 2.0f, 2.0f);
                manager.collider[id].center = manager.transform[id].pos;
                manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.PICKUPWEAPON;

            }
        }

        private void loadNextLevel(EntityManager manager) throws IOException {
            int maxMalteSeeker = 1;

            startIndex = randomS.nextInt(spawnPositionsTurret.length );

            spawnedTurrets = 0;

            DrawingWindow.level = level;

            livingEnemies = level;

            spawnHealthPack(manager);

            Random rand = new Random();

            for (int i = 0; i < livingEnemies; i++) {

                int enemyType = rand.nextInt(3);
                float spawnX;
                float spawnZ;

                if (rand.nextBoolean()) {
                    if (rand.nextBoolean())
                        spawnX = 15.0f;
                    else
                        spawnX = -15.0f;
                    spawnZ = ((float) rand.nextInt(15)) * 2.0f - 15.0f;
                } else {
                    if (rand.nextBoolean())
                        spawnZ = 15.0f;
                    else
                        spawnZ = -15.0f;
                    spawnX = ((float) rand.nextInt(15)) * 2.0f - 15.0f;
                }


                switch (enemyType) {
                    case 0 -> {
                        int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PHYSICSBODY | GameComponents.COLLIDER | GameComponents.DAMAGEABLE | GameComponents.AIBEHAVIOR);
                        if (id > -1) {

                            // Set up the transformation component
                            manager.rendering[id].mesh = new Mesh("./src/objects/enemies/groundEnemy/groundEnemy.obj", "./src/objects/enemies/groundEnemy/groundEnemy128.png");
                            manager.rendering[id].mesh.updateRenderType(GameComponents.Rendering.RenderType.Textured);
                            manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured; // Or other render types

                            manager.transform[id].pos = new Vector3(spawnX, -0.9f, spawnZ);
                            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                            manager.transform[id].scale = new Vector3(.4f, .4f, .4f);

                            manager.aiBehavior[id].spawnPoint = manager.transform[id].pos.clone();

                            manager.aiBehavior[id].enemyType = GameComponents.EnemyType.GROUNDENEMY;

                            manager.aiBehavior[id].shootingCooldown = 0.8f;
                            manager.physicsBody[id].speed = 1.0f;
                            manager.damageable[id].health = 10 * level;
                            manager.aiBehavior[id].chasingDistance = 40;
                            manager.aiBehavior[id].attackingDistance = 10;
                            manager.aiBehavior[id].damage = level;
                            manager.aiBehavior[id].wanderingDirection = new Vector3(1f, 0f, 1f);
                            manager.collider[id].colliderSize = new Vector3(4f, 4f, 1f);
                            manager.collider[id].center = manager.transform[id].pos;
                            manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.ENEMY;
                            manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                        }
                    }
                    case 1 -> {
                        int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PHYSICSBODY | GameComponents.COLLIDER | GameComponents.DAMAGEABLE | GameComponents.AIBEHAVIOR);
                        if (id > -1) {
                            // Set up the transformation component
                            if (randomS.nextInt(7) == 1 && maxMalteSeeker > 0) {
                                maxMalteSeeker--;
                                manager.rendering[id].mesh = new Mesh("./src/objects/sightseeker/sightseeker.obj", "./src/objects/enemies/sightseeker/textureMalte.png");
                                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured; // Or other render types
                                manager.rendering[id].mesh.updateRenderType(GameComponents.Rendering.RenderType.Textured);

                                manager.transform[id].pos = new Vector3(spawnX, 0f, spawnZ);
                                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                                manager.transform[id].scale = new Vector3(1f, 1f, 1f);

                                manager.aiBehavior[id].spawnPoint = manager.transform[id].pos.clone();

                                manager.aiBehavior[id].enemyType = GameComponents.EnemyType.MALTESEEKER;

                                manager.aiBehavior[id].shootingCooldown = 0.8f;
                                manager.physicsBody[id].speed = 4f;
                                manager.damageable[id].health = 100 + 10 * level;
                                manager.aiBehavior[id].chasingDistance = 50;
                                manager.aiBehavior[id].attackingDistance = 10;
                                manager.aiBehavior[id].damage = level;
                                manager.aiBehavior[id].wanderingDirection = new Vector3(1f, 1f, 1f);
                                manager.collider[id].colliderSize = new Vector3(2.0f, 1.0f, 1.0f);
                                manager.collider[id].center = manager.transform[id].pos;
                                manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.ENEMY;
                                manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;

                                MusicPlayer.getInstance().playSound(MusicPlayer.SoundEffect.MALTESEEKER_SPAWN_AND_ATTACK);


                            } else {
                                manager.rendering[id].mesh = new Mesh("./src/objects/sightseeker/sightseeker.obj", "./src/objects/enemies/sightseeker/sightseeker128.png");

                                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured; // Or other render types
                                manager.rendering[id].mesh.updateRenderType(GameComponents.Rendering.RenderType.Textured);

                                manager.transform[id].pos = new Vector3(spawnX, 0f, spawnZ);
                                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                                manager.transform[id].scale = new Vector3(1f, 1f, 1f);

                                manager.aiBehavior[id].spawnPoint = manager.transform[id].pos.clone();

                                manager.aiBehavior[id].enemyType = GameComponents.EnemyType.SIGHTSEEKER;

                                manager.aiBehavior[id].shootingCooldown = 3f;
                                manager.physicsBody[id].speed = 4f;
                                manager.damageable[id].health = 5 * level;
                                manager.aiBehavior[id].chasingDistance = 50;
                                manager.aiBehavior[id].attackingDistance = 0;
                                manager.aiBehavior[id].damage = level * 3;
                                manager.aiBehavior[id].wanderingDirection = new Vector3(1f, 1f, 1f);
                                manager.collider[id].colliderSize = new Vector3(2.0f, 1.0f, 1.0f);
                                manager.collider[id].center = manager.transform[id].pos;
                                manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.ENEMY;
                                manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                            }
                        }
                    }
                    case 2 -> {

                        if (spawnedTurrets >= spawnPositionsTurret.length) {
                            i--;
                            continue;
                        }

                        spawnedTurrets += 1;
                        startIndex = (startIndex + 1) % spawnPositionsTurret.length;

                        int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PHYSICSBODY | GameComponents.COLLIDER | GameComponents.DAMAGEABLE | GameComponents.AIBEHAVIOR);
                        if (id > -1) {
                            Color color = Color.yellow;
                            switch (randomS.nextInt( 4)) {
                                case 0 -> {
                                    color = Color.RED;
                                }
                                case 1 -> {
                                    color = Color.green;
                                }
                                case 2 -> {
                                    color = Color.yellow;
                                }
                                case 3 -> {
                                    color = Color.white;
                                }
                            }
                            int index = startIndex;

                            //float yS = randomS.nextFloat(1.0f, 3.2f);
                            float yS = ((float)randomS.nextInt(220))/ 100.0f + 1.0f;


                            manager.rendering[id].mesh = new Mesh("./src/objects/enemies/gunTurret/gunnerTurret.obj", color);
                            manager.rendering[id].mesh.updateRenderType(GameComponents.Rendering.RenderType.OneColor);
                            manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor; // Or other render types
                            manager.aiBehavior[id].leftRotated = leftRotated[index];

                            //manager.transform[id].pos = new Vector3(spawnX, /*rand.nextInt(1, 4)*/ 1, spawnZ);
                            manager.transform[id].pos = spawnPositionsTurret[index];
                            manager.transform[id].pos.y = yS;

                            if (leftRotated[index])
                                manager.transform[id].rot = new Vector3(-3.14f / 2, 0.0f, 3.1415f);

                            else
                                manager.transform[id].rot = new Vector3(3.14f / 2, 0.0f, 3.1415f);

                            manager.transform[id].scale = new Vector3(.2f, .2f, .2f);

                            manager.aiBehavior[id].spawnPoint = manager.transform[id].pos.clone();

                            manager.rendering[id].modelRotation = new Vector3(0.0f, 3.1415f, 0.0f);
                            manager.aiBehavior[id].enemyType = GameComponents.EnemyType.GUNTURRED;

                            manager.aiBehavior[id].shootingCooldown = 1f;
                            manager.physicsBody[id].speed = 0f;
                            manager.damageable[id].health = 5 * level;
                            manager.aiBehavior[id].chasingDistance = 100;
                            manager.aiBehavior[id].attackingDistance = manager.aiBehavior[id].chasingDistance;
                            manager.aiBehavior[id].damage = level;
                            manager.aiBehavior[id].wanderingDirection = new Vector3(1f, 0f, 1f);
                            manager.collider[id].colliderSize = new Vector3(3.2f, 3f, 3f);
                            manager.collider[id].center = RenderMaths.addVectors(manager.transform[id].pos, new Vector3(0.0f, -1.0f, 0.0f));
                            manager.rendering[id].modelTranslation = new Vector3(0.0f, 0.0f, 0.0f);
                            manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.ENEMY;
                            manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                        }
                    }


                }
            }


        }
    }

    public static class hotkeyMenuSystem extends GameSystem {
        private MKeyListener keyListener;

        private boolean lastStateM = false;

        private boolean lastStatem = false;

        @Override
        public void start(EntityManager manager) throws Exception {
            keyListener = MKeyListener.getInstance();

        }

        @Override
        public void update(EntityManager manager, float deltaTime) throws Exception {
            checkMusicKey();
            checkQuitKey();
            checkMusicLouder();
            checkMusicQuieter();
        }

        private void checkMusicKey() {
            //Stop or start gamemusic
            if (keyListener.isKeyPressed('M') != lastStateM && keyListener.isKeyPressed('M') || keyListener.isKeyPressed('m') != lastStatem && keyListener.isKeyPressed('m')) {
                MusicPlayer.getInstance().pauseResume("src/sound/misc/music.wav");
            }
            lastStateM = keyListener.isKeyPressed('M');
            lastStatem = keyListener.isKeyPressed('m');
        }

        private void checkMusicQuieter() {
            if (keyListener.isKeyPressed('Y') != lastStateM && keyListener.isKeyPressed('Y') || keyListener.isKeyPressed('y') != lastStatem && keyListener.isKeyPressed('y')) {
                if (MusicPlayer.getInstance().volume > 0.1f) {
                    MusicPlayer.getInstance().changeVolume(MusicPlayer.getInstance().volume - 0.1f);
                    System.out.println("Volume: " + MusicPlayer.getInstance().volume);

                }
            }
        }

        private void checkMusicLouder() {
            if (keyListener.isKeyPressed('X') != lastStateM && keyListener.isKeyPressed('X') || keyListener.isKeyPressed('x') != lastStatem && keyListener.isKeyPressed('x')) {
                if (MusicPlayer.getInstance().volume < 0.91f) {
                    MusicPlayer.getInstance().changeVolume(MusicPlayer.getInstance().volume + 0.1f);
                    System.out.println("Volume: " + MusicPlayer.getInstance().volume);
                }
            }
        }

        private void checkQuitKey() {
            if (keyListener.isKeyPressed('Q') != lastStateM && keyListener.isKeyPressed('Q') || keyListener.isKeyPressed('q') != lastStatem && keyListener.isKeyPressed('q')) {
                System.exit(0);
            }
        }
    }

    public static class startSceneSystem extends GameSystem {
        private Camera cam = Camera.getInstance();

        @Override
        public void start(EntityManager manager) throws Exception {
            int required_GameComponents = GameComponents.STARTSCENE;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    cam.position = new Vector3(0.0f, 0.0f, 0.0f);
                    cam.rotation = new Vector3(0.0f, 0.0f, 0.0f);
                    createDisplay(manager);

                }
            }

        }

        @Override
        public void update(EntityManager manager, float deltaTime) throws Exception {
            int required_GameComponents = GameComponents.CAMERAELEMENT | GameComponents.PHYSICSBODY | GameComponents.TRANSFORM;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    switch (manager.cameraElement[i].cameraElementType) {
                        case CAMERAFOCUSROUTE:
                            handleRoute(manager, i);
                            calculateRotation(manager.transform[i].pos);
                            break;
                        case CAMERAROUTE:
                            handleRoute(manager, i);
                            cam.position = manager.transform[i].pos;
                            break;
                    }
                }
            }
        }

        private void handleRoute(EntityManager manager, int i) {
            int counter = manager.cameraElement[i].listCounter;
            Vector3 nextPoint = manager.cameraElement[i].checkpointList.get(counter);
            Vector3 currentPos = manager.transform[i].pos;

            if (nextPoint.close(currentPos)) {
                if (counter == manager.cameraElement[i].checkpointList.size() - 1) {
                    manager.cameraElement[i].listCounter = 0;
                } else {
                    manager.cameraElement[i].listCounter++;
                }
            }

            Vector3 direction = nextPoint.subtract(currentPos);
            direction = RenderMaths.normalizeVector(direction);
            manager.physicsBody[i].force = RenderMaths.multiplyVector(direction, manager.physicsBody[i].speed);
        }

        public void calculateRotation(Vector3 focusPosition) {
            Vector3 direction = cam.position.subtract(focusPosition);
            float currentAngleY = cam.rotation.y;

            float desiredAngleY = (float) Math.atan2(direction.z, direction.x);
            desiredAngleY = (desiredAngleY + (float) Math.PI * 2) % ((float) Math.PI * 2);
            currentAngleY = (currentAngleY + (float) Math.PI * 2) % ((float) Math.PI * 2);
            float angleDifference = desiredAngleY - currentAngleY;
            angleDifference = (angleDifference + (float) Math.PI) % ((float) Math.PI * 2) - (float) Math.PI;
            float rotationAmount = angleDifference;
            currentAngleY += rotationAmount;

            cam.rotation.y = currentAngleY + (float) (Math.PI / 2);

            direction = direction.rotateY(cam.rotation.y);

            float currentAngleX = cam.rotation.x;
            float desiredAngleX = (float) Math.atan2(direction.y, -direction.z);
            desiredAngleX = (desiredAngleX + (float) Math.PI * 2) % ((float) Math.PI * 2);
            currentAngleX = (currentAngleX + (float) Math.PI * 2) % ((float) Math.PI * 2);
            float angleDifferenceX = desiredAngleX - currentAngleX;
            angleDifferenceX = (angleDifferenceX + (float) Math.PI) % ((float) Math.PI * 2) - (float) Math.PI;

            cam.rotation.x = currentAngleX + angleDifferenceX;
        }

        public void createDisplay(EntityManager manager) {
            try {
                ArrayList<Integer> visitIdList = new ArrayList<>();
                int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PICKUPWEAPON);
                if (id > -1) {
                    manager.rendering[id].mesh = new Mesh("./src/objects/rock/rock.obj", Color.CYAN);//"./src/objects/rock/rock64.png");
                    manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OutlineOnly;
                    manager.transform[id].pos = new Vector3(-0.5f, 0.0f, 18.0f);
                    manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                    manager.transform[id].scale = new Vector3(.7f, .7f, .7f);
                }
                visitIdList.add(id);

                id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PHYSICSBODY | GameComponents.COLLIDER | GameComponents.DAMAGEABLE | GameComponents.AIBEHAVIOR);
                if (id > -1) {
                    manager.rendering[id].mesh = new Mesh("./src/objects/enemies/groundEnemy/groundEnemy.obj", Color.GRAY);
                    manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor; // Or other render types


                    manager.transform[id].pos = new Vector3(-0.5f, -0.87f, 10.0f);
                    manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                    manager.transform[id].scale = new Vector3(.2f, .2f, .2f);

                    manager.aiBehavior[id].spawnPoint = manager.transform[id].pos.clone();

                    manager.aiBehavior[id].enemyType = GameComponents.EnemyType.GROUNDENEMY;

                    manager.aiBehavior[id].shootingCooldown = 6f;
                    manager.aiBehavior[id].currentState = GameComponents.State.DEACTIVATED;
                    manager.physicsBody[id].speed = 1f;
                    manager.damageable[id].health = 10;
                    manager.aiBehavior[id].chasingDistance = 40;
                    manager.aiBehavior[id].attackingDistance = 30;
                    manager.aiBehavior[id].damage = 5;
                    manager.aiBehavior[id].wanderingDirection = new Vector3(0f, 0f, -1f);
                    manager.collider[id].colliderSize = new Vector3(2f, 2f, 1f);
                    manager.collider[id].center = manager.transform[id].pos;
                    manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.ENEMY;
                    manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                }
                visitIdList.add(id);

                id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PHYSICSBODY | GameComponents.COLLIDER | GameComponents.DAMAGEABLE | GameComponents.AIBEHAVIOR);
                if (id > -1) {
                    manager.rendering[id].mesh = new Mesh("./src/objects/enemies/gunTurret/gunnerTurret.obj", Color.GREEN);
                    manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor; // Or other render types

                    manager.transform[id].pos = new Vector3(-2.5f, 0.5f, 12.0f);
                    manager.transform[id].rot = new Vector3(0.0f, 0.0f, 3.1415f);
                    manager.transform[id].scale = new Vector3(.2f, .2f, .2f);

                    manager.aiBehavior[id].spawnPoint = manager.transform[id].pos.clone();

                    manager.rendering[id].modelRotation = new Vector3(0.0f, 3.1415f, 0.0f);
                    manager.aiBehavior[id].enemyType = GameComponents.EnemyType.GUNTURRED;

                    manager.aiBehavior[id].shootingCooldown = 1f;
                    manager.aiBehavior[id].currentState = GameComponents.State.DEACTIVATED;
                    manager.physicsBody[id].speed = 0f;
                    manager.damageable[id].health = 5;
                    manager.aiBehavior[id].chasingDistance = 40;
                    manager.aiBehavior[id].attackingDistance = 40;
                    manager.aiBehavior[id].damage = 1;
                    manager.aiBehavior[id].wanderingDirection = new Vector3(1f, 0f, 1f);
                    manager.collider[id].colliderSize = new Vector3(1f, 1f, 1f);
                    manager.collider[id].center = manager.transform[id].pos;
                    manager.rendering[id].modelTranslation = new Vector3(0.0f, 1.0f, 0.0f);
                    manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.ENEMY;
                    manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                }
                visitIdList.add(id);

                id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PHYSICSBODY | GameComponents.COLLIDER | GameComponents.DAMAGEABLE | GameComponents.AIBEHAVIOR);
                if (id > -1) {
                    // Set up the transformation component
                    manager.rendering[id].mesh = new Mesh("./src/objects/sightseeker/sightseeker.obj", "./src/objects/sightseeker/texture.png");
                    manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured; // Or other render types
                    manager.rendering[id].mesh.updateRenderType(GameComponents.Rendering.RenderType.Textured);

                    manager.transform[id].pos = new Vector3(1f, 0.2f, 13.0f);
                    manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                    manager.transform[id].scale = new Vector3(.4f, .4f, .4f);

                    manager.aiBehavior[id].spawnPoint = manager.transform[id].pos.clone();

                    manager.aiBehavior[id].enemyType = GameComponents.EnemyType.SIGHTSEEKER;

                    manager.aiBehavior[id].shootingCooldown = 1f;
                    manager.aiBehavior[id].currentState = GameComponents.State.DEACTIVATED;
                    manager.physicsBody[id].speed = 4f;
                    manager.damageable[id].health = 5;
                    manager.aiBehavior[id].chasingDistance = 30;
                    manager.aiBehavior[id].attackingDistance = 5;
                    manager.aiBehavior[id].damage = 1;
                    manager.aiBehavior[id].wanderingDirection = new Vector3(1f, 0f, 1f);
                    manager.collider[id].colliderSize = new Vector3(1.0f, 1.0f, 1.0f);
                    manager.collider[id].center = manager.transform[id].pos;
                    manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.ENEMY;
                    manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                }
                visitIdList.add(id);

                id = manager.createEntity(GameComponents.CAMERAELEMENT | GameComponents.PHYSICSBODY | GameComponents.TRANSFORM);
                if (id > -1) {
                    manager.cameraElement[id].cameraElementType = GameComponents.cameraElementTypes.CAMERAROUTE;
                    ArrayList<Vector3> vector3ForRoute = new ArrayList<>();
                    vector3ForRoute.add(new Vector3(5f, -0.5f, 12f));
                    vector3ForRoute.add(new Vector3(10f, 0f, 0f));
                    vector3ForRoute.add(new Vector3(0f, 10f, 0f));
                    vector3ForRoute.add(new Vector3(-10f, 0f, 2f));
                    vector3ForRoute.add(new Vector3(10f, 0f, 2f));
                    vector3ForRoute.add(new Vector3(1f, 0f, 1f));
                    manager.cameraElement[id].checkpointList = vector3ForRoute;
                    manager.transform[id].pos = new Vector3(5f, 0.5f, 5.0f);
                    manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                    manager.transform[id].scale = new Vector3(0.0f, 0.0f, 0.0f);
                    manager.physicsBody[id].speed = 2f;

                }

                id = manager.createEntity(GameComponents.CAMERAELEMENT | GameComponents.PHYSICSBODY | GameComponents.TRANSFORM);
                if (id > -1) {
                    manager.cameraElement[id].cameraElementType = GameComponents.cameraElementTypes.CAMERAFOCUSROUTE;
                    ArrayList<Vector3> vector3ForRoute = new ArrayList<>();
                    vector3ForRoute.add(manager.transform[visitIdList.get(1)].pos);
                    vector3ForRoute.add(manager.transform[visitIdList.get(2)].pos);
                    vector3ForRoute.add(manager.transform[visitIdList.get(0)].pos);
                    vector3ForRoute.add(manager.transform[visitIdList.get(3)].pos);
                    manager.cameraElement[id].checkpointList = vector3ForRoute;
                    manager.transform[id].pos = new Vector3(-2.5f, 0.5f, 6.5f);
                    manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                    manager.transform[id].scale = new Vector3(0.0f, 0.0f, 0.0f);

                }


            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
