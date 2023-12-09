package src.engine.core.gamemanagement;

import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Vector3;

public class GameComponents {

    public final static int

            TRANSFORM = 1,
            RENDER = 1 << 2,

            PHYSICSBODY = 1 << 3,
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


    public static class PhysicsBody{
        public Vector3 velocity = new Vector3(0.0f, 0.0f, 0.0f);
        public Vector3 maxVelocity = new Vector3(2.5f, 3.0f, 5.0f);
        public float speed = 1.0f;
        public Vector3 force = new Vector3(0.0f, 0.0f, 0.0f);
        public Vector3 acceleration = new Vector3(0.0f, 0.0f, 0.0f);
        public float mass = 1.0f;


    }

    public static class PlayerMovement{
        public float mouseSpeed = 0.03f;
        public float moveSpeed = 4.0f;

        public float jumpIntensity = 20.0f;
        public Vector3 cameraOffset = new Vector3(0, 0, 0);
    }

}