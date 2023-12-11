package src.engine.core.gamemanagement;

public class EntityManager {

    public int flag[];

    public GameComponents.Transform[] transform;
    public GameComponents.Rendering[] rendering;
    public GameComponents.PlayerMovement[] playerMovement;
    public GameComponents.PhysicsBody[] physicsBody;
    public GameComponents.Bullet[] bullet;
    public final int size;

    public EntityManager(int size) {
        this.size = size;

        transform = new GameComponents.Transform[size];
        rendering = new GameComponents.Rendering[size];
        playerMovement = new GameComponents.PlayerMovement[size];
        physicsBody = new GameComponents.PhysicsBody[size];
        bullet = new GameComponents.Bullet[size];

        flag = new int[size];

    }

    public int createEntity(int flag) {
        for (int i = 0; i < size; i++){
            if (this.flag[i] == 0) {
                this.flag[i] = flag;
                if ((flag & GameComponents.TRANSFORM) > 0) transform[i] = new GameComponents.Transform();
                if ((flag & GameComponents.RENDER) > 0) rendering[i] = new GameComponents.Rendering();
                if ((flag & GameComponents.PLAYERMOVEMENT) > 0) playerMovement[i] = new GameComponents.PlayerMovement();
                if ((flag & GameComponents.PHYSICSBODY) > 0) physicsBody[i] = new GameComponents.PhysicsBody();
                if ((flag & GameComponents.BULLET) > 0) bullet[i] = new GameComponents.Bullet();

                return i;
            }
        }
        return -1;
    }

}