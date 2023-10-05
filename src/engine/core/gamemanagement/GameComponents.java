package src.engine.core.gamemanagement;

public class GameComponents {

    public final static int POS = 1 << 0,
            VEL = 1 << 1,
            RENDER = 1 << 2,
            RAYCAST = 1 << 3;

    public static class Velocity {
        float velx, vely;
    }

    public static class Position {
        float x, y;
    }

    public static class Rendering {
        String name; // for sake of having at least one field inside.
        boolean rayCast;
    }

    public static class RAYCAST {
        float width;
        float height;
    }
}