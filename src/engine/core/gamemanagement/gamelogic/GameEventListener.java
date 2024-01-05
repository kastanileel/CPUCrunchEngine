package src.engine.core.gamemanagement.gamelogic;

public interface GameEventListener {

    void onFinishLevel(int level);
    //implement round transition sound
    void onPlayerDeath();
    //implement game over

    void onKillEnemy();
}
