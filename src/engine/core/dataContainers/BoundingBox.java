package src.engine.core.dataContainers;

import src.engine.core.matutils.Vector3;

public class BoundingBox {
    public Vector3 min;
    public Vector3 max;

    public BoundingBox(Vector3 min, Vector3 max) {
        this.min = min;
        this.max = max;
    }
}
