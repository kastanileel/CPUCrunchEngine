package src.engine.core.gamemanagement;


import src.engine.core.gamemanagement.gamelogic.EventSystem;
import src.engine.core.gamemanagement.gamelogic.GameEventListener;
import src.engine.core.tools.MKeyListener;
import src.engine.core.tools.MMouseListener;
import src.engine.core.tools.MusicPlayer;
import src.scenes.ExampleScene;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameContainer implements GameEventListener {


    EntityManager manager;
    GameSystems.Renderer rasterizer;
    GameSystems.PyhsicsHandler physicsHandler;
    GameSystems.PlayerMovement playerMovement;
    GameSystems.BulletSystem bulletSystem;

    GameSystems.CollisionSystem collisionSystem;

    GameSystems.PickupWeapon pickupWeapon;

    GameSystems.DamageSystem damageSystem;

    GameSystems.EnemySystem enemySystem;

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


        Scene example = new ExampleScene(1000, "example");
        scenes.put(example.getName(), example);

        currentSceneName = "example";

        EventSystem.getInstance().addListener(this);

        startGameLoop();
    }

    void startGameLoop() throws Exception {

        String activeSceneName = "";

        long lastTime = System.nanoTime() / 1000000;

        MusicPlayer.getInstance().loopMusic("src/sound/music.wav");

        MKeyListener keyListener = MKeyListener.getInstance();

        boolean lastStateM = false;

        boolean lastStatem = false;

        while (true) {
            long currentSystemTime = System.nanoTime();
            float deltaTime = ((float) currentSystemTime / 1000000 - (float) lastTime / 1000000) / 1000.0f;

            //Stop or start gamemusic
            if (keyListener.isKeyPressed('M') != lastStateM && keyListener.isKeyPressed('M') || keyListener.isKeyPressed('m') != lastStatem && keyListener.isKeyPressed('m')) {
                MusicPlayer.getInstance().pauseResume("src/sound/music.wav");
            }

            lastStateM = keyListener.isKeyPressed('M');
            lastStatem = keyListener.isKeyPressed('m');

            lastTime = currentSystemTime;

            if (!currentSceneName.equals(activeSceneName)) {
                Scene activeScene = scenes.get(currentSceneName);

                activeScene.createScene();
                manager = activeScene.getEntityManager();

                activeSceneName = currentSceneName;

                rasterizer.start(manager);

                collisionSystem.start(manager);
                playerMovement.start(manager);

                enemySystem.start(manager);
            }

            collisionSystem.update(manager, deltaTime);
            rasterizer.update(manager, deltaTime);
            physicsHandler.update(manager, deltaTime);
            playerMovement.update(manager, deltaTime);
            bulletSystem.update(manager, deltaTime);
            pickupWeapon.update(manager, deltaTime);
            damageSystem.update(manager, deltaTime);
            enemySystem.update(manager, deltaTime);


            MMouseListener.getInstance().update();
            manager.clearDestroyedEntities();

            int playerid = 0;
            int required_GameComponents = GameComponents.TRANSFORM | GameComponents.PLAYERMOVEMENT | GameComponents.PHYSICSBODY;
            for (int i = 0; i < manager.size; i++) {
                if ((manager.flag[i] & required_GameComponents) == required_GameComponents) {
                    playerid = i;
                }
            }
            if (playerid == 0) {
                onPlayerDeath();
                break;
            }
        }
        MusicPlayer.getInstance().stopGameMusic();
        startGameLoop();
        //System.out.println(System.nanoTime()/1000000 - lastTime);
    }


    @Override
    public void onFinishLevel(int level) {

        // maybe switch active scenes here

    }


    @Override
    public void onPlayerDeath() {
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


        Scene example = new ExampleScene(1000, "example");
        scenes.put(example.getName(), example);
    }

    public static void main(String[] args) throws Exception {
        new GameContainer();
    }
}

