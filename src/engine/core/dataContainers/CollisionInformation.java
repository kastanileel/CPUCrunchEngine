package src.engine.core.dataContainers;

import src.engine.core.matutils.Vector3;

import java.util.LinkedList;

public class CollisionInformation {
    public  LinkedList<CollisionEvent> collisionEvents = new LinkedList<>();

    //Flush the collision information
    public void flush(){
        for (int i = 0; i<collisionEvents.size();i++) {
            collisionEvents.removeLast();
        }
    }

    public static class CollisionEvent{
        public Vector3 position;
        public EntityPair entityIDs;

        public CollisionEvent(Vector3 position, EntityPair entityIDs) {
            this.position = position;
            this.entityIDs = entityIDs;
        }

    }

    public static class EntityPair {
        private int entity1;
        private int entity2;

        public EntityPair(int entity1, int entity2) {
            this.entity1 = entity1;
            this.entity2 = entity2;
        }

        public int getFirst() {
            return entity1;
        }

        public int getSecond() {
            return entity2;
        }
    }

}


