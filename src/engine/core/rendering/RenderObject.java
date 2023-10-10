package src.engine.core.rendering;


import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.Vector3;

import java.io.IOException;

/***
 * This class is the base class for all objects that are rendered in the scene.
 */
public abstract class RenderObject {

    public Vector3 position, rotation;
    public Mesh mesh;

    /***
     * Creates a new RenderObject with the given model path.
     * The RenderObject won't have a texture.
     * @param modelPath The path to the model file.
     * @throws IOException If the model file can't be found.
     */
    public RenderObject(String modelPath) throws

            IOException {
        mesh = new Mesh(modelPath);
        position = new Vector3();
        rotation = new Vector3();
    }

    /***
     * Creates a new RenderObject with the given model and texture path.
     * @param modelPath The path to the model file.
     * @param texturePath The path to the texture file.
     * @throws IOException If the model or texture file can't be found.
     */
    public RenderObject(String modelPath, String texturePath) throws IOException {
        mesh = new Mesh(modelPath, texturePath);
        position = new Vector3();
        rotation = new Vector3();
    }


    /***
     * This method is called every render cycle.
     */
    public abstract void update();
}
