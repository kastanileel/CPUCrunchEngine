package src.scenes;

import src.engine.core.gamemanagement.GameComponents;
import src.engine.core.gamemanagement.Scene;
import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Quaternion;
import src.engine.core.matutils.Vector3;

import java.awt.*;

public class startScene extends Scene {
    public startScene(int size, String name) {
        super(size, name);
    }

    @Override
    public void createScene() {
        try {
            for (int width = 0; width < 30; width++) {
                for (int length = 0; length < 30; length++) {
                    int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.COLLIDER);
                    if (id > -1) {
                        manager.rendering[id].mesh = new Mesh("./src/objects/rock/plane.obj", Color.blue);
                        manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor;
                        manager.transform[id].pos = new Vector3(-15.0f + width, -1.0f, -15.0f + length);
                        manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                        manager.transform[id].scale = new Vector3(1.0f, 1.0f, 1.0f);

                        manager.collider[id].colliderType = GameComponents.Collider.ColliderType.BOX;
                        manager.collider[id].colliderSize = new Vector3(0.9f, 0.9f, 0.9f);
                        manager.collider[id].colliderRotation = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
                        manager.collider[id].center = manager.transform[id].pos;
                        manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.GROUND;

                    }
                }
            }

            int id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PICKUPWEAPON);
            if (id > -1) {
                manager.rendering[id].mesh = new Mesh("./src/objects/rock/rock.obj", Color.CYAN);//"./src/objects/rock/rock64.png");
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OutlineOnly;
                manager.transform[id].pos = new Vector3(0.0f, 1.0f, -10.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.7f, .7f, .7f);

            }
            id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PICKUPWEAPON);
            if (id > -1) {
                manager.rendering[id].mesh = new Mesh("./src/objects/rock/rock.obj", Color.RED);//"./src/objects/rock/rock64.png");
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OutlineOnly;
                manager.transform[id].pos = new Vector3(0.0f, 1.0f, 10.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.7f, .7f, .7f);

            }

            manager.createEntity(GameComponents.STARTSCENE);


        } catch (Exception e){
            throw new RuntimeException(e);

        }
    }
}
