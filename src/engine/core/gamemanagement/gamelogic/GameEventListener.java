package src.engine.core.gamemanagement.gamelogic;

import src.engine.core.gamemanagement.EntityManager;
import src.engine.core.gamemanagement.GameComponents;

import java.io.IOException;

public interface GameEventListener {

    void onFinishLevel(int level);
    void onPlayerDeath();
    void onKillEnemy(EntityManager manager, GameComponents.EnemyType enemyType) throws IOException;
}
