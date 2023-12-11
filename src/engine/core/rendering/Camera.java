package src.engine.core.rendering;

import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Vector3;

public class Camera {

    private static Camera instance;

    public Vector3 position;
    public Vector3 rotation;

    private Camera() {
        position = new Vector3(0.0f, 3, 5.0f);
        rotation = new Vector3(0.0f, 0.0f, 0);
    }

    public static Camera getInstance() {
        if (instance == null)
            instance = new Camera();
        return instance;
    }

    public float[][] getViewMatrix() {

       float[][] viewMatrix = RenderMaths.identityMatrix();


         viewMatrix = RenderMaths.multiplyMatrices(viewMatrix, RenderMaths.makeRotationMatrix(rotation.x, rotation.y, rotation.z));
        viewMatrix[0][3] = position.x;
        viewMatrix[1][3] = position.y;
        viewMatrix[2][3] = position.z;

        viewMatrix = RenderMaths.inverseMatrix(viewMatrix);


                return viewMatrix;

    }
}
