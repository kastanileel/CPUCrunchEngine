package src.engine.core.gamemanagement;

import src.engine.core.rendering.DrawingWindow;
import src.engine.core.tools.MKeyListener;
import src.engine.core.tools.MMouseListener;
import src.engine.core.tools.MusicPlayer;
import src.scenes.ExampleScene;

import java.util.HashMap;

public class GameContainer {



    EntityManager manager;
    GameSystems.Renderer rasterizer;
    GameSystems.PyhsicsHandler physicsHandler;
    GameSystems.PlayerMovement playerMovement;
    GameSystems.BulletSystem bulletSystem;

    GameSystems.CollisionSystem collisionSystem;

    GameSystems.PickupWeapon pickupWeapon;

    GameSystems.DamageSystem damageSystem;

    GameSystems.EnemySystem enemySystem;


    GameSystems.GameLogicSystem gameLogicSystem;

    GameSystems.hotkeyMenuSystem hotkeyMenuSystem;

    HashMap<String, Scene> scenes;
    static String currentSceneName = "";


    GameContainer() throws Exception {
        scenes = new HashMap<>();
        manager = new EntityManager(2000);

        rasterizer = new GameSystems.Renderer();
        collisionSystem = new GameSystems.CollisionSystem();

        physicsHandler = new GameSystems.PyhsicsHandler();
        playerMovement = new GameSystems.PlayerMovement();

        bulletSystem = new GameSystems.BulletSystem();

        pickupWeapon = new GameSystems.PickupWeapon();

        damageSystem = new GameSystems.DamageSystem();

        enemySystem = new GameSystems.EnemySystem();

        gameLogicSystem = new GameSystems.GameLogicSystem();

        hotkeyMenuSystem = new GameSystems.hotkeyMenuSystem();


       Scene example = new ExampleScene(1000, "example");
       Scene start = new ExampleScene(1000, "start");
       scenes.put(example.getName(), example);
       scenes.put(start.getName(), start);

       currentSceneName = "start";

        startGameLoop();
    }

    void startGameLoop() throws Exception {

        String activeSceneName = "";

        long lastTime = System.nanoTime() / 1000000;

        MusicPlayer.getInstance().loopMusic("src/sound/misc/music.wav");

        boolean lastStateM = false;

        boolean lastStatem = false;

        boolean pauseResume = false;

        boolean waitingForStart = true;

        while(true) {
            long currentSystemTime = System.nanoTime();
            float deltaTime = ((float) currentSystemTime / 1000000 - (float) lastTime / 1000000) / 1000.0f;


            lastTime = currentSystemTime;

            if(!currentSceneName.equals(activeSceneName)){
                Scene activeScene = scenes.get(currentSceneName);

                activeScene.createScene();
                manager = activeScene.getEntityManager();

                activeSceneName = currentSceneName;

                rasterizer.start(manager);

                collisionSystem.start(manager);
                playerMovement.start(manager);

                enemySystem.start(manager);
                gameLogicSystem.start(manager);
                hotkeyMenuSystem.start(manager);
            }

            MKeyListener keyListener = MKeyListener.getInstance();

            if (waitingForStart) {
                hotkeyMenuSystem.update(manager, deltaTime);
                deltaTime = 0;
                rasterizer.update(manager, deltaTime);
                for (boolean key : keyListener.getKeyList()) {
                    if (key) {
                        waitingForStart = false;
                        currentSceneName = "example";
                    }
                }

            } else {
                if (keyListener.isKeyPressed('P') != lastStateM && keyListener.isKeyPressed('P') || keyListener.isKeyPressed('p') != lastStatem && keyListener.isKeyPressed('p')) {
                    if (pauseResume) {
                        pauseResume = false;
                        DrawingWindow.onPause = false;
                    } else {
                        pauseResume = true;
                        DrawingWindow.onPause = true;
                    }
                }

                lastStateM = keyListener.isKeyPressed('P');
                lastStatem = keyListener.isKeyPressed('p');


                if (pauseResume) {
                    deltaTime = 0;
                }

                collisionSystem.update(manager, deltaTime);
                physicsHandler.update(manager, deltaTime);
                playerMovement.update(manager, deltaTime);
                bulletSystem.update(manager, deltaTime);
                pickupWeapon.update(manager, deltaTime);
                damageSystem.update(manager, deltaTime);
                enemySystem.update(manager, deltaTime);
                gameLogicSystem.update(manager, deltaTime);
                rasterizer.update(manager, deltaTime);
                hotkeyMenuSystem.update(manager, deltaTime);

                MMouseListener.getInstance().update();
                manager.clearDestroyedEntities();
            }
        }

        //System.out.println(System.nanoTime()/1000000 - lastTime);
    }




    public static void main(String[] args) throws Exception {
        new GameContainer();
    }
}

