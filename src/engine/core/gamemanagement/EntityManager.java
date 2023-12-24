package src.engine.core.gamemanagement;

import src.engine.core.dataContainers.CollisionInformation;

import java.util.HashMap;
import java.util.LinkedList;

public class EntityManager {

    public int flag[];

    public GameComponents.Transform[] transform;
    public GameComponents.Rendering[] rendering;
    public GameComponents.PlayerMovement[] playerMovement;
    public GameComponents.Collider collider[];
    public GameComponents.PhysicsBody[] physicsBody;
    public GameComponents.Bullet[] bullet;

    public GameComponents.PickupWeapon[] pickupWeapon;
    public GameComponents.Damageable[] damageable;

    public final int size;

    public HashMap<Integer, CollisionInformation> collisionList = new HashMap<>();
    public LinkedList<Integer> destroyList = new LinkedList<>();

    public EntityManager(int size) {
        this.size = size;

        transform = new GameComponents.Transform[size];
        rendering = new GameComponents.Rendering[size];

        collider = new GameComponents.Collider[size];
        playerMovement = new GameComponents.PlayerMovement[size];
        physicsBody = new GameComponents.PhysicsBody[size];
        bullet = new GameComponents.Bullet[size];

        pickupWeapon = new GameComponents.PickupWeapon[size];
        damageable = new GameComponents.Damageable[size];

        flag = new int[size];

    }

    public int createEntity(int flag) {
        for (int i = 0; i < size; i++){
            if (this.flag[i] == 0) {
                this.flag[i] = flag;
                if ((flag & GameComponents.TRANSFORM) > 0) transform[i] = new GameComponents.Transform();
                if ((flag & GameComponents.RENDER) > 0) rendering[i] = new GameComponents.Rendering();
                if((flag & GameComponents.COLLIDER)>0) {
                    collider[i] = new GameComponents.Collider();
                    collisionList.put(i,new CollisionInformation());
                }

                if ((flag & GameComponents.PLAYERMOVEMENT) > 0) playerMovement[i] = new GameComponents.PlayerMovement();
                if ((flag & GameComponents.PHYSICSBODY) > 0) physicsBody[i] = new GameComponents.PhysicsBody();
                if ((flag & GameComponents.BULLET) > 0) bullet[i] = new GameComponents.Bullet();
                if((flag & GameComponents.PICKUPWEAPON)>0) pickupWeapon[i] = new GameComponents.PickupWeapon();
                if((flag & GameComponents.DAMAGEABLE)>0) damageable[i] = new GameComponents.Damageable();

                return i;
            }
        }
        return -1;
    }

    public void destroyEntity(int entityID){
        destroyList.add(entityID);
    }

    public void clearDestroyedEntities() {
        for(Integer entityID: destroyList) {

            flag[entityID] = 0;
            if (transform[entityID] != null) transform[entityID] = null;
            if (rendering[entityID] != null) rendering[entityID] = null;
            if (collider[entityID] != null) collider[entityID] = null;
            if (playerMovement[entityID] != null) playerMovement[entityID] = null;
            if (physicsBody[entityID] != null) physicsBody[entityID] = null;
            if (bullet[entityID] != null) bullet[entityID] = null;
            if (pickupWeapon[entityID] != null) pickupWeapon[entityID] = null;
            if (damageable[entityID] != null) damageable[entityID] = null;

        }

        destroyList.clear();
    }

}