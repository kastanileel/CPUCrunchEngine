package src.engine.core.gamemanagement;

public class EntityManager {
    public int flag[];

    public GameComponents.Transform transform[];
    public GameComponents.Rendering rendering[];
    public final int size;

    public EntityManager(int size) {
        this.size = size;

        transform = new GameComponents.Transform[size];
        rendering = new GameComponents.Rendering[size];
        flag = new int[size];
    }

    public int createEntity(int flag) {
        for (int i = 0; i < size; i++){
            if (this.flag[i] == 0) {
                this.flag[i] = flag;
                if ((flag & GameComponents.TRANSFORM) > 0) transform[i] = new GameComponents.Transform();
                if ((flag & GameComponents.RENDER) > 0) rendering[i] = new GameComponents.Rendering();
                return i;
            }
        }
        return -1;
    }
}