package src.engine.core.gamemanagement;

import src.engine.core.inputsystem.MKeyListener;
import src.engine.core.rendering.Camera;
import src.engine.core.rendering.RenderPipeline;

public class GameSystems {

    public static class Velocity {
        public void update(EntityManager manager) {
            int required_GameComponents = GameComponents.POS | GameComponents.VEL;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents){
                   // manager.pos[i].x += manager.vel[i].velx;
                 //   manager.pos[i].y += manager.vel[i].vely;

                    float dx = (float) -Math.cos(Camera.getInstance().rotY);
                    float dy = (float) -Math.sin(Camera.getInstance().rotY);
                    if(MKeyListener.keyList['w']){
                        Camera.getInstance().x  += Camera.getInstance().rotX / 5;
                        Camera.getInstance().y  += Camera.getInstance().rotZ /5;
                    }
                    if(MKeyListener.keyList['s']){
                        Camera.getInstance().x  -= Camera.getInstance().rotX;
                        Camera.getInstance().y  -= Camera.getInstance().rotZ;
                    }
                    if(MKeyListener.keyList['a']){
                        Camera.getInstance().rotY+=0.001; Camera.getInstance().rotY=FixAng(Camera.getInstance().rotY);
                        Camera.getInstance().rotX= (float) Math.cos(degToRad(Camera.getInstance().rotY));
                        Camera.getInstance().rotZ= (float) -Math.sin(degToRad(Camera.getInstance().rotY));
                    }
                    if(MKeyListener.keyList['d']){
                        Camera.getInstance().rotY-=0.001; Camera.getInstance().rotY=FixAng(Camera.getInstance().rotY);
                        Camera.getInstance().rotX= (float) Math.cos(degToRad(Camera.getInstance().rotY));
                        Camera.getInstance().rotZ= (float) -Math.sin(degToRad(Camera.getInstance().rotY));
                    }
                }
            }
        }
    }

    static float FixAng(float a){ if(a>359){ a-=360;} if(a<0){ a+=360;} return a;}
    static float degToRad(float a) { return a;}

    public static class Render {
        public void update(EntityManager manager) {

            RenderPipeline.getInstance().clear();

            int required_GameComponents = GameComponents.POS | GameComponents.RENDER;

            RenderPipeline.getInstance().clear();
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {


                    if(manager.rendering[i].rayCast) {
                        RenderPipeline.getInstance().queueForCast(manager.pos[i].x, manager.pos[i].y);

                    }
                   // System.out.println(String.format("%s: (%f x, %f y)", manager.rendering[i].name, manager.pos[i].y, manager.pos[i].y));
                }


            }

            Camera c = Camera.getInstance();

            System.out.println("Camera:" + c.x + ", " + c.y + "|" + c.rotY);

            RenderPipeline.getInstance().rayCast();

            RenderPipeline.getInstance().redraw();
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