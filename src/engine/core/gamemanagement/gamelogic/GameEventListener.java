package src.engine.core.gamemanagement.gamelogic;

public interface GameEventListener {

    void onFinishLevel(int level);
    void onPlayerDeath();
    //implement game over

    void onKillEnemy();
}
