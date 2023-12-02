package src.engine.core.gamemanagement;

import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Vector3;

public class GameComponents {

    public final static int

            TRANSFORM = 1,
            RENDER = 1 << 1,
            VELOCITY = 1 << 3,
            PLAYERMOVEMENT = 1 << 4;

    public static class Transform {
        public Vector3 pos;
        public Vector3 rot;
        public Vector3 scale;
    }

    public static class Rendering {
        public static enum RenderType{
            OutlineOnly,
            OneColor,
            Textured,
            TexturedAndOutline,
            Emissive,
            //OneColorAndOutline,
            //OneColorDerivedFromTexture,

        }
        public Mesh mesh;

        public RenderType renderType;


    }

    public static class Velocity{
        Vector3 velocity;
        float speed = 1.0f;
    }

    public static class PlayerMovement{
        public float mouseSpeed = 0.1f;
        public float moveSpeed = 3.0f;
        public Vector3 cameraOffset = new Vector3(0, 0, 0);
    }

}