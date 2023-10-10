package src.engine.core.gamemanagement;

import src.engine.core.rendering.Camera;
import src.engine.core.rendering.SimpleAdvancedRenderPipeline;
import src.engine.core.rendering.SimpleRenderPipeline.RenderPipMultiThreaded;
import src.engine.core.rendering.SimpleRenderpipeline;

import java.util.Objects;

public class GameSystems {

    public static class Velocity {
        public void update(EntityManager manager) {
            int required_GameComponents = GameComponents.TRANSFORM;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents){
                   // manager.pos[i].x += manager.vel[i].velx;
                  //  manager.transform[i].pos.z += 0.11f;
                  //  manager.transform[i].rot.x += 0.1f;
                   // manager.transform[i].rot.y += 0.31f;
                   // manager.transform[i].rot.z += 0.01f;

                    manager.transform[i].rot.y += 0.01f;

                    if (Objects.equals(manager.rendering[i].name, "rock")){
                        manager.transform[i].pos.z -= 0.03f;
                    }

                    //Camera.getInstance().rotation.y += 0.001f;
                }
            }
        }
    }

    public static class Render {

        long lastTime = System.nanoTime() / 1000000000;
        int counter = 0;
        public void update(EntityManager manager) {

            SimpleRenderpipeline simpleRenderpipeline = SimpleRenderpipeline.getInstance(800, 600);

            if ((System.nanoTime() / 1000000000) - lastTime >= 1) {
                simpleRenderpipeline.setTitle("FPS:" + counter);
                lastTime = System.nanoTime() / 1000000000;
                counter = 0;
            }
            else {
                counter += 1;
            }


            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.RENDER;

            simpleRenderpipeline.clearBuffer();
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {

                    simpleRenderpipeline.renderObject(manager.rendering[i].mesh, manager.transform[i].pos, manager.transform[i].rot, manager.transform[i].scale);
                }


            }


            simpleRenderpipeline.draw();

        }

    }


    public static class RenderMulti {

        long lastTime = System.nanoTime() / 1000000000;
        int counter = 0;
        public void update(EntityManager manager) throws InterruptedException {



            SimpleAdvancedRenderPipeline renderPipMultiThreaded = SimpleAdvancedRenderPipeline.getInstance(800, 600);

            if ((System.nanoTime() / 1000000000) - lastTime >= 1) {
                renderPipMultiThreaded.setTitle("FPS:" + counter);
                lastTime = System.nanoTime() / 1000000000;
                counter = 0;
            }
            else {
                counter += 1;
            }


            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.RENDER;

            renderPipMultiThreaded.clearBuffer();
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {

                    renderPipMultiThreaded.renderObject(manager.rendering[i].mesh, manager.transform[i].pos, manager.transform[i].rot, manager.transform[i].scale);
                }


            }
            renderPipMultiThreaded.stepTwo();


            renderPipMultiThreaded.draw();

        }

    }

    public static class SimpleRender{

    }

}