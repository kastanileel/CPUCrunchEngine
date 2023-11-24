package src.engine.core.matutils;

public class Quaternion {
    public float w, x, y, z;

    public Quaternion(float w, float x, float y, float z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Method to normalize the quaternion
    public void normalize() {
        float magnitude = (float) Math.sqrt(w * w + x * x + y * y + z * z);
        w /= magnitude;
        x /= magnitude;
        y /= magnitude;
        z /= magnitude;
    }

    // Helper function to rotate a vector by a quaternion
    // Helper function to rotate a vector by a quaternion
    public static Vector3 rotateByQuaternion(Vector3 v, Quaternion q) {
        Quaternion qv = new Quaternion(0, v.x, v.y, v.z);
        Quaternion qConjugate = getConjugate(q);
        Quaternion rotated = Quaternion.mul(qv, Quaternion.mul(q, qConjugate));
        return new Vector3(rotated.x, rotated.y, rotated.z);
    }


    // Helper function to rotate a vector by the inverse of a quaternion
    public static Vector3 rotateByQuaternionInverse(Vector3 v, Quaternion q) {
        Quaternion qConjugate = getConjugate(q);
        return rotateByQuaternion(v, qConjugate);
    }

    // Helper function to get the conjugate of a quaternion
    public static Quaternion getConjugate(Quaternion q) {
        return new Quaternion(q.w, -q.x, -q.y, -q.z);
    }

    // Helper function to multiply two quaternions
    public static Quaternion mul(Quaternion a, Quaternion b) {
        return new Quaternion(
                a.w * b.w - a.x * b.x - a.y * b.y - a.z * b.z,
                a.w * b.x + a.x * b.w + a.y * b.z - a.z * b.y,
                a.w * b.y - a.x * b.z + a.y * b.w + a.z * b.x,
                a.w * b.z + a.x * b.y - a.y * b.x + a.z * b.w);
    }



}
