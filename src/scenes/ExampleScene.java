package src.scenes;

import src.engine.core.gamemanagement.GameComponents;
import src.engine.core.gamemanagement.Scene;
import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Quaternion;
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




            int id;




            id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PICKUPWEAPON);
            if (id > -1) {
                manager.rendering[id].mesh = new Mesh("./src/objects/rock/rock.obj", Color.CYAN);//"./src/objects/rock/rock64.png");
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OutlineOnly;
                manager.transform[id].pos = new Vector3(-0.5f, 0.5f, 15.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.7f, .7f, .7f);

            }



         /*id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PHYSICSBODY | GameComponents.COLLIDER | GameComponents.DAMAGEABLE | GameComponents.AIBEHAVIOR);
            if (id > -1) {
                manager.rendering[id].mesh = new Mesh("./src/objects/enemies/gunTurret/gunnerTurret.obj", Color.GREEN);
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor; // Or other render types

                manager.transform[id].pos = new Vector3(0.0f, 0.0f, 5.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 3.1415f);
                manager.transform[id].scale = new Vector3(.2f, .2f, .2f);

                manager.rendering[id].modelRotation = new Vector3(0.0f, 3.1415f, 0.0f);
                manager.aiBehavior[id].enemyType = GameComponents.EnemyType.GUNTURRED;
            }*/

           /* id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PHYSICSBODY | GameComponents.COLLIDER | GameComponents.DAMAGEABLE | GameComponents.AIBEHAVIOR);
            if (id > -1) {
                // Set up the transformation component
                manager.rendering[id].mesh = new Mesh("./src/objects/enemies/groundEnemy/groundEnemy.obj", Color.GRAY);
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor; // Or other render types

                manager.transform[id].pos = new Vector3(0f, -0.9f, 10.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.2f, .2f, .2f);

                manager.aiBehavior[id].enemyType = GameComponents.EnemyType.GROUNDENEMY;
            }

            id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.RENDER | GameComponents.PHYSICSBODY | GameComponents.COLLIDER | GameComponents.DAMAGEABLE | GameComponents.AIBEHAVIOR);
            if (id > -1) {
                // Set up the transformation component
                manager.rendering[id].mesh = new Mesh("./src/objects/sightseeker/sightseeker.obj", "./src/objects/sightseeker/texture.png");
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured; // Or other render types

                manager.transform[id].pos = new Vector3(5f, 0f, 15.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.2f, .2f, .2f);

                manager.aiBehavior[id].enemyType = GameComponents.EnemyType.SIGHTSEEKER;
            }*/



            /*id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.PLAYERMOVEMENT
                    | GameComponents.PHYSICSBODY | GameComponents.RENDER | GameComponents.COLLIDER);
            if (id > -1){
                manager.transform[id].pos = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.2f, 0.2f, 0.2f);
                manager.playerMovement[id].mouseSpeed = 0.35f;
                manager.playerMovement[id].moveSpeed = 6.0f;
                manager.playerMovement[id].cameraOffset = new Vector3(0, 0, 0);
                manager.physicsBody[id].speed = 3.0f;
                manager.physicsBody[id].velocity = new Vector3(0, 0, 0);

                manager.playerMovement[id].weaponType = GameComponents.PlayerMovement.WeaponType.PISTOL;

                manager.rendering[id].mesh = new Mesh("./src/objects/guns/pistol/startPistol.obj", Color.GRAY);
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor;
                manager.rendering[id].modelTranslation = new Vector3(-0.5f, -0.7f, 3.0f);
                manager.rendering[id].modelRotation = new Vector3(0.0f, 3.0f, 0.0f);

                manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                manager.collider[id].colliderSize = new Vector3(1.0f, 1.0f, 1.0f);
                manager.collider[id].center = manager.transform[id].pos;
                manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.PLAYER;
            }*/

            id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.PLAYERMOVEMENT
                    | GameComponents.PHYSICSBODY | GameComponents.RENDER | GameComponents.COLLIDER | GameComponents.DAMAGEABLE);
            if (id > -1) {
                manager.transform[id].pos = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.05f, 0.05f, 0.05f);
                manager.playerMovement[id].mouseSpeed = 0.08f;
                manager.playerMovement[id].moveSpeed = 4.50f;
                manager.playerMovement[id].cameraOffset = new Vector3(0, 0, 0);
                manager.physicsBody[id].speed = 3.0f;
                manager.physicsBody[id].velocity = new Vector3(0, 0, 0);

                manager.playerMovement[id].weaponType = GameComponents.PlayerMovement.WeaponType.MACHINE_GUN;

                manager.rendering[id].mesh = new Mesh("./src/objects/guns/machineGun/AKM.obj", "./src/objects/guns/machineGun/AKM_texture.png");
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.Textured;
                manager.rendering[id].modelTranslation = new Vector3(-0.5f, -0.5f, 3.0f);
                manager.rendering[id].modelRotation = new Vector3(0.0f, 0.0f, 0.0f);

                manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                manager.collider[id].colliderSize = new Vector3(1.0f, 1.0f, 1.0f);
                manager.collider[id].center = manager.transform[id].pos;
                manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.PLAYER;


            }

          /*id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.PLAYERMOVEMENT
                    | GameComponents.PHYSICSBODY | GameComponents.RENDER | GameComponents.COLLIDER);
            if (id > -1){
                manager.transform[id].pos = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.05f, 0.05f, 0.05f);
                manager.playerMovement[id].mouseSpeed = 0.35f;
                manager.playerMovement[id].moveSpeed = 6.0f;
                manager.playerMovement[id].cameraOffset = new Vector3(0, 0, 0);
                manager.physicsBody[id].speed = 3.0f;
                manager.physicsBody[id].velocity = new Vector3(0, 0, 0);

                manager.playerMovement[id].weaponType = GameComponents.PlayerMovement.WeaponType.SHOTGUN;

                manager.rendering[id].mesh = new Mesh("./src/objects/guns/shotgun/superShotgun.obj", Color.GRAY);
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor;
                manager.rendering[id].modelTranslation = new Vector3(-0.5f, -0.7f, 3.0f);
                manager.rendering[id].modelRotation = new Vector3(0.0f, 0.0f, 0.0f);

                manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                manager.collider[id].colliderSize = new Vector3(1.0f, 1.0f, 1.0f);
                manager.collider[id].center = manager.transform[id].pos;
                manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.PLAYER;
            }*/

           /*id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.PLAYERMOVEMENT
                    | GameComponents.PHYSICSBODY | GameComponents.RENDER | GameComponents.COLLIDER);
            if (id > -1){
                manager.transform[id].pos = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.04f, 0.04f, 0.04f);
                manager.playerMovement[id].mouseSpeed = 0.05f;
                manager.playerMovement[id].moveSpeed = 4.5f;
                manager.playerMovement[id].cameraOffset = new Vector3(0, 0, 0);
                manager.physicsBody[id].speed = 4.5f;
                manager.physicsBody[id].velocity = new Vector3(0, 0, 0);

                manager.playerMovement[id].weaponType = GameComponents.PlayerMovement.WeaponType.SNIPER;

                manager.rendering[id].mesh = new Mesh("./src/objects/guns/sniper/AWP.obj", Color.GRAY);
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor;
                manager.rendering[id].modelTranslation = new Vector3(-0.5f, -0.7f, 4.2f);
                manager.rendering[id].modelRotation = new Vector3(0.0f, 0.0f, 0.0f);

                manager.collider[id].colliderType = GameComponents.Collider.ColliderType.SPHERE;
                manager.collider[id].colliderSize = new Vector3(1.0f, 1.0f, 1.0f);
                manager.collider[id].center = manager.transform[id].pos;
                manager.collider[id].colliderTag = GameComponents.Collider.ColliderTag.PLAYER;
            }*/

         /*   id = manager.createEntity(GameComponents.TRANSFORM | GameComponents.PLAYERMOVEMENT
                    | GameComponents.PHYSICSBODY | GameComponents.RENDER);
            if (id > -1){
                manager.transform[id].pos = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].rot = new Vector3(0.0f, 0.0f, 0.0f);
                manager.transform[id].scale = new Vector3(.05f, .05f, .05f);
                manager.playerMovement[id].mouseSpeed = 0.05f;
                manager.playerMovement[id].moveSpeed = 6.0f;
                manager.playerMovement[id].cameraOffset = new Vector3(0, 0, 0);
                manager.physicsBody[id].speed = 3.0f;
                manager.physicsBody[id].velocity = new Vector3(0, 0, 0);
                manager.playerMovement[id].weaponType = GameComponents.PlayerMovement.WeaponType.PISTOL;

                manager.rendering[id].mesh = new Mesh("./src/objects/guns/knife/combatKnife.obj", Color.RED);
                manager.rendering[id].renderType = GameComponents.Rendering.RenderType.OneColor;
                manager.rendering[id].modelTranslation = new Vector3(0.5f, -0.7f, 3.0f);
                manager.rendering[id].modelRotation = new Vector3(0.0f, -3.8415f/2.0f , 0.0f);
            } */


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
