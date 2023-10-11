package src.engine.core.gamemanagement;

import src.engine.core.inputsystem.MKeyListener;
import src.engine.core.inputsystem.MMouseListener;
import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Vector3;
import src.engine.core.rendering.Camera;
import src.engine.core.rendering.SimpleAdvancedRenderPipeline;
import src.engine.core.rendering.SimpleRenderPipeline.RenderPipMultiThreaded;
import src.engine.core.rendering.SimpleRenderpipeline;

import java.io.IOException;
import java.util.Objects;

public class GameSystems {

    public static Vector3 position = new Vector3(0.0f, 0.0f, 0.0f);

    public static class Movement {
        public void update(EntityManager manager, float deltaTime) throws IOException {
            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.RENDER;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {

                    float speed = 15f;
                    // manager.pos[i].x += manager.vel[i].velx;
                    //  manager.transform[i].pos.z += 0.11f;
                    //  manager.transform[i].rot.x += 0.1f;
                    // manager.transform[i].rot.y += 0.31f;
                    // manager.transform[i].rot.z += 0.01f;
                    if(manager.rendering[i].name == "environment" | (manager.rendering[i].name == "bullet" && manager.velocity[i].lifetime > .5f)){



                    if(MKeyListener.getInstance().getKeyList()['w']){

                        // angle based on camera rotation
                        float angle = Camera.getInstance().rotation.y;
                        float x = (float) Math.sin(angle);
                        float z = (float) -Math.cos(angle);
                        manager.transform[i].pos.x += x * deltaTime * speed;
                        manager.transform[i].pos.z += z * deltaTime * speed;
                       // manager.transform[i].pos.x += 0.01f;

                    }
                    if(MKeyListener.getInstance().getKeyList()['a']){

                        float angle = Camera.getInstance().rotation.y;
                        float x = (float) Math.sin(angle);
                        float z = (float) -Math.cos(angle);
                        manager.transform[i].pos.x += z * deltaTime * speed/2;
                        manager.transform[i].pos.z -= x * deltaTime * speed/2;
                        //manager.transform[i].pos.z += 0.01f;

                    }
                    if(MKeyListener.getInstance().getKeyList()['s']){
                        float angle = Camera.getInstance().rotation.y;
                        float x = (float) Math.sin(angle);
                        float z = (float) -Math.cos(angle);
                        manager.transform[i].pos.x -= x * deltaTime* speed/2;
                        manager.transform[i].pos.z -= z * deltaTime* speed/2;

                    }
                    if(MKeyListener.getInstance().getKeyList()['d']){

                        float angle = Camera.getInstance().rotation.y;
                        float x = (float) Math.sin(angle);
                        float z = (float) -Math.cos(angle);
                        manager.transform[i].pos.x -= z * deltaTime* speed;
                        manager.transform[i].pos.z += x * deltaTime* speed;


                    }
                    }

                    if(manager.rendering[i].name == "pistol"){

                        float angle = Camera.getInstance().rotation.y;
                        float x = (float) Math.sin(angle);
                        float z = (float) -Math.cos(angle);

                        manager.transform[i].pos.x = Camera.getInstance().position.x;
                        manager.transform[i].pos.y = Camera.getInstance().position.y - 0.5f;
                        manager.transform[i].pos.z = Camera.getInstance().position.z;
                        manager.transform[i].rot.x = Camera.getInstance().rotation.x;
                        manager.transform[i].rot.y = Camera.getInstance().rotation.y - 3.3f*(float) Math.PI/2;
                        manager.transform[i].rot.z = Camera.getInstance().rotation.z;

                        manager.transform[i].pos.x -=  1.5f * x;
                        manager.transform[i].pos.z -=  1.5f * z;

                        // offset to the right
                        manager.transform[i].pos.x +=  0.5f * z;
                        manager.transform[i].pos.z -=  0.5f * x;
                    }

                    if(MMouseListener.getInstance().isLeftButtonPressed()){



                        int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.VELOCITY);
                        if(id > -1){
                            manager.rendering[id].mesh = new Mesh("./src/objects/rock/rock.obj", "./src/objects/rock/rock8.png");
                            manager.transform[id].pos = new Vector3(0.0f, -0.1f, 0);
                            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                            manager.transform[id].scale = new Vector3(.05f,.05f,.05f);
                            manager.rendering[id].name = "bullet";

                            float angle = Camera.getInstance().rotation.y;
                            float x = (float) -Math.sin(angle);
                            float z = (float) Math.cos(angle);
                            manager.velocity[id].velocity = new Vector3(x, -Camera.getInstance().rotation.x, z);

                            manager.velocity[id].lifetime = 2.0f;
                            manager.velocity[id].speed = 18.0f;
                        }

                    }

                   // System.out.println("Position: " + manager.transform[i].pos.x + " " + manager.transform[i].pos.y + " " + manager.transform[i].pos.z);
                   // System.out.println("Rotation: " + manager.transform[i].rot.x + " " + manager.transform[i].rot.y + " " + manager.transform[i].rot.z);
                 //   System.out.println("Scale: " + manager.transform[i].scale.x + " " + manager.transform[i].scale.y + " " + manager.transform[i].scale.z);
                   // System.out.println("///////");



                    //Camera.getInstance().rotation.y += 0.001f;
                }
            }
        }
    }

    public static class Velocity{
        public void update(EntityManager manager, float deltaTime){
            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.VELOCITY;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    manager.transform[i].pos.x += manager.velocity[i].velocity.x  * deltaTime * manager.velocity[i].speed;
                    manager.transform[i].pos.y += manager.velocity[i].velocity.y  * deltaTime * manager.velocity[i].speed;
                    manager.transform[i].pos.z += manager.velocity[i].velocity.z  * deltaTime * manager.velocity[i].speed;
                    manager.velocity[i].lifetime -= deltaTime;

                    if(manager.velocity[i].lifetime < 0.0f){
                        manager.flag[i] = 0;
                    }
                }
            }
        }
    }

    public static class Rasterizer {

        long lastTime = System.nanoTime() / 1000000000;
        int counter = 0;

        public void update(EntityManager manager, float deltaTime) throws InterruptedException {


            SimpleAdvancedRenderPipeline renderPip = SimpleAdvancedRenderPipeline.getInstance(800, 600);

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

            renderPip.stepTwo();


            renderPip.draw();

        }

    }

    public static class CameraMovement {

        public void update(EntityManager entityManager, float deltaTime) {

            Camera cam = Camera.getInstance();

            // Horizontal camera rotation (y-axis) based on horizontal mouse movement
             //cam.rotation.y = (float) Math.PI/2;




            // Vertical camera rotation (x-axis) based on vertical mouse movement
            cam.rotation.y += MMouseListener.getInstance().getMouseX() * 0.01f;


            // angle sin cos
            float angle = cam.rotation.y;
            float x = (float) Math.sin(angle);
            float z = (float) -Math.cos(angle);
            System.out.println(x > z);

            float mouseY = MMouseListener.getInstance().getMouseY();

            if(x>z){

                cam.rotation.x -= z * mouseY * 0.001f;
                cam.rotation.x += x * mouseY * 0.001f;
            }
            else {
                cam.rotation.x += z * mouseY * 0.001f;
                cam.rotation.x -= x * mouseY * 0.001f;
            }




            // Clamp the vertical rotation to a range if you don't want it to flip over
            if (cam.rotation.x > 0.1f)
                cam.rotation.x = 0.1f;
            if (cam.rotation.x < -0.1f)
                cam.rotation.x = -0.1f;

            if(cam.rotation.z > 0.1f)
                cam.rotation.z = 0.1f;
            if(cam.rotation.z < -0.1f)
                cam.rotation.z = -0.1f;
        }
    }


}