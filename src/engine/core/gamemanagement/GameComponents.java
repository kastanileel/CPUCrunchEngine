package src.engine.core.gamemanagement;

import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Quaternion;
import src.engine.core.matutils.Vector3;

public class GameComponents {

    public final static int

            TRANSFORM = 1,
            RENDER = 1 << 2,
            VELOCITY = 1 << 3,
            PLAYERMOVEMENT = 1 << 4,
            COLLIDER = 1 << 5;

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
        public Vector3 velocity;
        public float speed = 1.0f;
    }

    public static class PlayerMovement{
        public float mouseSpeed = 0.1f;
        public float moveSpeed = 1.0f;
        public Vector3 cameraOffset = new Vector3(0, 0, 0);
    }

    public static  class Collider{
        public static enum ColliderType{
            BOX,
            SPHERE
        }

        public ColliderType colliderType;
        public Vector3 center;
        public Vector3 colliderSize;

        public Quaternion colliderRotation;
    }

}