package src.engine.core.gamemanagement;

import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Quaternion;
import src.engine.core.matutils.Vector3;

public class GameComponents {

    public final static int

            TRANSFORM = 1,
            RENDER = 1 << 2,
            PHYSICSBODY = 1 << 3,
            PLAYERMOVEMENT = 1 << 4,
            COLLIDER = 1 << 5,
            BULLET = 1 << 6,
            PICKUPWEAPON = 1 << 7,
            DAMAGEABLE = 1 << 8,
            AIBEHAVIOR = 1 << 9;


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
            Hide,
            //OneColorAndOutline,
            //OneColorDerivedFromTexture,

        }
        public Mesh mesh;

        public RenderType renderType;

        public Vector3 modelRotation = new Vector3(0.0f, 0.0f, 0.0f);
        public Vector3 modelTranslation = new Vector3(0.0f, 0.0f, 0.0f);
        public Vector3 modelPosition = new Vector3(0.0f, 0.0f, 0.0f);


    }


    public static class PhysicsBody{
        public Vector3 velocity = new Vector3(0.0f, 0.0f, 0.0f);
        public float speed = 1.0f;
        public Vector3 force = new Vector3(0.0f, 0.0f, 0.0f);
        public Vector3 acceleration = new Vector3(0.0f, 0.0f, 0.0f);
        public float mass = 1.0f;


    }

    public static class PlayerMovement{

        public static enum WeaponType {
            PISTOL,
            MACHINE_GUN,
            SHOTGUN,
            SNIPER,
        }

        public WeaponType weaponType;
        public float mouseSpeed = 0.01f;
        public float moveSpeed = 2.0f;

        public float jumpIntensity = 20.0f;
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

        public static enum ColliderTag{
            PLAYER,
            ENEMY,
            BULLET,
            OBSTACLE,
            GROUND,
            WALL,
            PICKUPWEAPON,
            NONE
        }

        public ColliderTag colliderTag = ColliderTag.NONE;
    }
  
    
    public static class Bullet{
        public float speed = 10.0f;
        public float lifeTime = 5.0f;
        public int damage = 1;

        public Vector3 direction = new Vector3(0, 0, 0);


    }

    public static class PickupWeapon {

        public PlayerMovement.WeaponType weaponType;
    }

    public static class Damageable{
        public int health = 10;
    }

    public enum State {
        WANDERING,
        CHASING,
        ATTACKING
    }

    public static class AIBEHAVIOR {
        public State currentState;
        public float timeSinceLastDirectionChange;
        public float wanderingDuration;
        public float wanderingSpeed;

    }

}