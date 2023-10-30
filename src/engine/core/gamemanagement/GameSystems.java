package src.engine.core.gamemanagement;

import src.engine.configuration.Configurator;
import src.engine.core.rendering.SimpleAdvancedRenderPipeline;

public class GameSystems {

    public static class Velocity extends GameSystem{

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

        int width = Integer.parseInt(Configurator.getInstance().get("windowWidth"));
        int height = Integer.parseInt(Configurator.getInstance().get("windowHeight"));

        int textureMaxAccuracy = Integer.parseInt(Configurator.getInstance().get("textureMaxAccuracy"));
        int textureMinAccuracy = Integer.parseInt(Configurator.getInstance().get("textureMinAccuracy"));
        long lastTime = System.nanoTime() / 1000000000;
        int counter = 0;

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

            renderPip.stepTwo();


            renderPip.draw();

        }

    }




}