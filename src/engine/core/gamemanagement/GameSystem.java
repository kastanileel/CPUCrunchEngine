package src.engine.core.gamemanagement;

public abstract class GameSystem {

    public abstract void start(EntityManager manager) throws Exception;

    public abstract void update(EntityManager manager, float deltaTime) throws Exception;
}
