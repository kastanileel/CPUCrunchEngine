package src.engine.core.gamemanagement;

import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Vector3;

public class GameComponents {

    public final static int

            TRANSFORM = 1 << 0,
            RENDER = 1 << 1;

    public static class Transform {
        Vector3 pos;
        Vector3 rot;
        Vector3 scale;
    }

    public static class Rendering {
        Mesh mesh;

    }


}