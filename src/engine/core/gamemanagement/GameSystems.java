package src.engine.core.gamemanagement;

import src.engine.core.rendering.SimpleRenderpipeline;

public class GameSystems {

    public static class Velocity {
        public void update(EntityManager manager) {
            int required_GameComponents = GameComponents.POS | GameComponents.VEL;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents){
                   // manager.pos[i].x += manager.vel[i].velx;

                }
            }
        }
    }

    public static class Render {
        public void update(EntityManager manager) {

            SimpleRenderpipeline simpleRenderpipeline = SimpleRenderpipeline.getInstance(800, 600);

            int required_GameComponents = GameComponents.POS | GameComponents.RENDER;

            simpleRenderpipeline.clearBuffer();
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {

                    simpleRenderpipeline.renderObject(manager.rendering[i].mesh, manager.rendering[i].pos, manager.rendering[i].rot);
                }


            }


            simpleRenderpipeline.draw();
        }
    }

    public static class RayCastRender {
        public void update(EntityManager manager) {
            int required_GameComponents = GameComponents.POS | GameComponents.RAYCAST;
            for (int i = 0; i < manager.size; i++){

            }
        }
    }

    public static class SimpleRender{

    }

}