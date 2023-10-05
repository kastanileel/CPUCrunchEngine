package src.engine.core.rendering;

public class Camera {

    private static Camera instance;

    public float x, y, z;
    public float rotX, rotY, rotZ;

    private Camera(){

        x = 100;
        y = 100;
    }

    public static Camera getInstance(){
        if(instance == null)
            instance = new Camera();

        return instance;
    }
}
