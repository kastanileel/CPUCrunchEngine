package src.engine.core.rendering;

import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Vector3;

public class Camera {

    private static Camera instance;

    public Vector3 position;
    public Vector3 rotation;

    private Camera() {
        position = new Vector3(0.0f, 3, 0.0f);
        rotation = new Vector3(0.0f, 0.0f, 0);
    }

    public static Camera getInstance() {
        if (instance == null)
            instance = new Camera();
        return instance;
    }

    public float[][] getViewMatrix() {
        float[][] m = RenderMaths.makeRotationMatrix(rotation.x, rotation.y, rotation.z);

        m = RenderMaths.inverseMatrix(m);
        m[0][3] = -position.x;
        m[1][3] = -position.y;
        m[2][3] = -position.z;
        return m;
    }
}
