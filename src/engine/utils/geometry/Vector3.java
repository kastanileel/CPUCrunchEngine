package src.engine.utils.geometry;

public class Vector3 {
    public float x, y, z;
    public float w = 1.0f;

    public Vector3(){ }
    public Vector3(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Vector3 clone() {
        return new Vector3(x, y, z);
    }

}

