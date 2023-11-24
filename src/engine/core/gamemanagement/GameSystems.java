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




}