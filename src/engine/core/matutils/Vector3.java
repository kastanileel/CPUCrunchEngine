package src.engine.core.matutils;

public class Vector3 {
    public float x, y, z;
    public float w = 1.0f;

    public Vector3() {
    }

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Vector3 clone() {
        return new Vector3(x, y, z);
    }

    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }


    public Vector3 divide(float other) {
        return new Vector3(x / other, y / other, z / other);
    }


    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public static float dot(Vector3 u, Vector3 v) {
        return u.x * v.x + u.y * v.y + u.z * v.z;
    }

    public float distanceSquared(Vector3 other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        float dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public Vector3 normalize() {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        return new Vector3(x / length, y / length, z / length);
    }

    public Vector3 scale(float scaleFactor) {
        return new Vector3(this.x * scaleFactor, this.y * scaleFactor, this.z * scaleFactor);
    }

    public float distance(Vector3 center) {
        return (float) Math.sqrt(distanceSquared(center));
    }
}

