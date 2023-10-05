package src.engine.core.gamemanagement;

public class EntityManager {
    public int flag[];
    public GameComponents.Position pos[];
    public GameComponents.Velocity vel[];
    public GameComponents.Rendering rendering[];
    public final int size;

    public EntityManager(int size) {
        this.size = size;
        pos = new GameComponents.Position[size];
        vel = new GameComponents.Velocity[size];
        rendering = new GameComponents.Rendering[size];
        flag = new int[size];
    }

    public int createEntity(int flag) {
        for (int i = 0; i < size; i++){
            if (this.flag[i] == 0) {
                this.flag[i] = flag;
                if ((flag & GameComponents.POS) > 0) pos[i] = new GameComponents.Position();
                if ((flag & GameComponents.VEL) > 0) vel[i] = new GameComponents.Velocity();
                if ((flag & GameComponents.RENDER) > 0) rendering[i] = new GameComponents.Rendering();
                return i;
            }
        }
        return -1;
    }
}