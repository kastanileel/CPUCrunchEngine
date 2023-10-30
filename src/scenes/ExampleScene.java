package src.scenes;

import src.engine.core.gamemanagement.GameComponents;
import src.engine.core.gamemanagement.Scene;
import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Vector3;

import java.awt.*;
import java.io.IOException;

public class ExampleScene extends Scene {
    public ExampleScene(int size, String name) {
        super(size, name);
    }

    @Override
    public void createScene() {
        // for loop to create floor*/

        try {
       for(int width = 0; width < 1; width ++){
            for(int length = 0; length < 1; length ++){
                int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
                if(id > -1){

                    manager.rendering[id].mesh = new Mesh("./src/objects/rock/plane.obj", Color.blue);
                    manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor;

                    manager.transform[id].pos = new Vector3(-15.0f + width, -1.0f, -15.0f + length);
                    manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                    manager.transform[id].scale = new Vector3(1.0f, 1.0f, 1.0f);

                }
            }
        }

       int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
            if(id > -1){
                manager.rendering[id].mesh = new Mesh("./src/objects/rock/rock.obj", "./src/objects/rock/rock128.png");
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured;
                manager.transform[id].pos = new Vector3(0.0f, -0.5f, 5.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.7f, .7f, .7f);

            }

       /* int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
        if(id > -1){
            manager.rendering[id].mesh = new Mesh("./src/objects/rock/fn49.obj", Color.red);
            manager.transform[id].pos = new Vector3(0.0f, 0.0f, 0);
            manager.transform[id].rot = new Vector3(0.0f, 90.0f, 9.0f);
            manager.transform[id].scale = new Vector3(.1f,.1f,.1f);

        }

        id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
        if(id > -1){
            manager.rendering[id].mesh = new Mesh("./src/objects/sightseeker/sightseeker.obj", "./src/objects/sightseeker/texture.png");
            manager.transform[id].pos = new Vector3(5.0f, 0.0f, 0);
            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
            manager.transform[id].scale = new Vector3(1.f,1.f,1.f);

        }

    for(int width = 0; width < 2; width ++){
                for(int length = 0; length < 30; length ++){
                    int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
                    if(id > -1){
                        manager.rendering[id].mesh = new Mesh("./src/objects/rock/planeTex.obj", "./src/objects/rock/rock8.png");
                        manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured;
                        manager.transform[id].pos = new Vector3(15.0f, 0.0f + width, -15.0f + length);
                        manager.transform[id].rot = new Vector3(3.1415f/2.0f, 0.0f, 3.1415f/2.0f);
                        manager.transform[id].scale = new Vector3(0.25f, 0.25f, 0.25f);

                    }
                }
            }


        // create wall


        id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER);
        if(id > -1){
            manager.rendering[id].mesh = new Mesh("./src/objects/rock/rock.obj", "./src/objects/rock/rock8.png", true);
            manager.transform[id].pos = new Vector3(0.0f, -0.5f, 9.0f);
            manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
            manager.transform[id].scale = new Vector3(1.0f, 1.0f, 1.0f);

        }*/

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
