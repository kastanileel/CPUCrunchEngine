package src.engine.core.gamemanagement;

public class EntityManager {

    public int flag[];

    public GameComponents.Transform transform[];
    public GameComponents.Rendering rendering[];
    public GameComponents.Velocity velocity[];

    public GameComponents.PlayerMovement playerMovement[];
    public final int size;

    public EntityManager(int size) {
        this.size = size;

        transform = new GameComponents.Transform[size];
        rendering = new GameComponents.Rendering[size];
        velocity = new GameComponents.Velocity[size];
        playerMovement = new GameComponents.PlayerMovement[size];
        flag = new int[size];

    }

    public int createEntity(int flag) {
        for (int i = 0; i < size; i++){
            if (this.flag[i] == 0) {
                this.flag[i] = flag;
                if ((flag & GameComponents.TRANSFORM) > 0) transform[i] = new GameComponents.Transform();
                if ((flag & GameComponents.RENDER) > 0) rendering[i] = new GameComponents.Rendering();
                if ((flag & GameComponents.VELOCITY) > 0) velocity[i] = new GameComponents.Velocity();
                if ((flag & GameComponents.PLAYERMOVEMENT) > 0) playerMovement[i] = new GameComponents.PlayerMovement();
                return i;
            }
        }
        return -1;
    }

}