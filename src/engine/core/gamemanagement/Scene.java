package src.engine.core.gamemanagement;

public abstract class Scene {

    protected EntityManager manager;
    private String name;

    public Scene(int size, String name) {
        manager = new EntityManager(size);

        this.name = name;
    }

    // Method in which the scene is configurated
    public abstract void createScene();

    public String getName(){
        return name;
    }

    public EntityManager getEntityManager() {
        return manager;
    }
}
