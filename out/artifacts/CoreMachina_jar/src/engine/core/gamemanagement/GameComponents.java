package src.engine.core.gamemanagement;

import src.engine.core.inputsystem.MKeyListener;
import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Vector3;

public class GameComponents {

    public final static int

            TRANSFORM = 1 << 0,
            RENDER = 1 << 1,
            NAME = 1 << 2,
            VELOCITY = 1 << 3;

    public static class Transform {
        Vector3 pos;
        Vector3 rot;
        Vector3 scale;
    }

    public static class Rendering {
        Mesh mesh;

        String name;


    }

    public static class Velocity{
        Vector3 velocity;
        float lifetime;
        float speed = 1.0f;
    }

    public static class BulletBehaviour {
        float lifetime;
    }

    public static class Movement {
        Vector3 position;

    }



}