package src.engine.core.gamemanagement;

public abstract class GameSystem {

    public abstract void update(EntityManager manager, float deltaTime) throws Exception;
}
