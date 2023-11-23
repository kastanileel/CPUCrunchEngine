package src.engine.core.matutils;

public class Matrix3 {
    private Vector3[] rows;

    public Matrix3(Vector3 row1, Vector3 row2, Vector3 row3) {
        rows = new Vector3[]{row1, row2, row3};
    }

    public Vector3 multiply(Vector3 vector) {
        float x = rows[0].x * vector.x + rows[0].y * vector.y + rows[0].z * vector.z;
        float y = rows[1].x * vector.x + rows[1].y * vector.y + rows[1].z * vector.z;
        float z = rows[2].x * vector.x + rows[2].y * vector.y + rows[2].z * vector.z;
        return new Vector3(x, y, z);
    }
}
