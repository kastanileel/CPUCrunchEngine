package src.engine.core.dataContainers;

import src.engine.core.matutils.Vector3;

import java.util.LinkedList;

public class CollisionInformation {
    LinkedList<CollisionEvent> collisionEvents = new LinkedList<>();

    //Flush the collision information
    public void flush(){
        for (int i = 0; i<collisionEvents.size();i++) {
            collisionEvents.removeLast();
        }
    }

    public class CollisionEvent{
        public Vector3 position;
        public int colliderID1;
        public int colliderID2;

    }

}


