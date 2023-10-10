package src.engine.core.rendering;


import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Triangle;


import src.engine.core.inputsystem.MKeyListener;
import src.engine.core.matutils.Vector3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

/***
 * This class is responsible for rendering the scene.
 *
 * The render pipeline is a singleton class, so there can only be one render pipeline.
 */
public class SimpleRenderpipeline {

    private float width, height;
    public static SimpleRenderpipeline instance;
    private DrawingWindow drawingWindow;
    private Frame frame;

    private SimpleRenderpipeline(int width, int height){
        this.width = width;
        this.height = height;
        frame = new Frame((int) width, (int) height);
        frame.setVisible(true);
        drawingWindow = frame.getPanel();
    }

    public static SimpleRenderpipeline getInstance(int width, int height){
        if(instance == null)
            instance = new SimpleRenderpipeline(width, height);
        return instance;
    }

    /***
     * This method is called every render cycle for each RenderObject in the scene.
     */
    public void renderObject(Mesh mesh, Vector3 pos, Vector3 rot){
        List<Triangle> triangles = new ArrayList<>();
        Camera camera = Camera.getInstance();

        // The model matrix is used to transform the object coordinates into world coordinates.
        // (essentially positioning the obj model in the scene)
        float[][] modelMatrix = RenderMaths.identityMatrix();
        modelMatrix = RenderMaths.multiplyMatrices(modelMatrix, RenderMaths.makeRotationMatrix(rot.x,rot.y,rot.z));
        modelMatrix[0][3] = pos.x;
        modelMatrix[1][3] = pos.y;
        modelMatrix[2][3] = pos.z;
        // scale to fit the screen width and height


        // The view matrix is used to transform the world coordinates to camera coordinates.
        // (essentially positioning the camera in the scene otherwise we would always render
        // the origin of the scene)
        float[][] viewMatrix = camera.getViewMatrix();

        // The projection matrix is used to transform the camera coordinates to screen coordinates.
        // (essentially taking the 3D scene and projecting it onto a 2D screen
        // => basically taking a picture, the triangles are now only in 2D coordinates)
        float fNear = 0.1f;
        float fFar = 1000.0f;
        float fFov = 120.0f;
        float[][] projectionMatrix = RenderMaths.projectionMatrix(fFov, fNear, fFar, height/width);

        for(int i = 0; i < mesh.triangles.length; i++){
            Triangle triangle = mesh.triangles[i];
            Triangle tri = triangle.clone();

            // apply model matrix
            tri.vertices[0] = RenderMaths.multiplyMatrixVector(tri.vertices[0], modelMatrix);
            tri.vertices[1] = RenderMaths.multiplyMatrixVector(tri.vertices[1], modelMatrix);
            tri.vertices[2] = RenderMaths.multiplyMatrixVector(tri.vertices[2], modelMatrix);




            //calculate normal of triangle for lighting
            Vector3 normal, line1, line2;

            line1 = RenderMaths.substractVectors(tri.vertices[1], tri.vertices[0]);
            line2 = RenderMaths.substractVectors(tri.vertices[2], tri.vertices[0]);
            normal = RenderMaths.crossProduct(line2, line1);
            normal = RenderMaths.normalizeVector(normal);

            Vector3 cameraRay = RenderMaths.substractVectors(tri.vertices[0], camera.position);

            if (RenderMaths.dotProduct(cameraRay, normal) > 0.0f) {
                tri.textureIndex = i;
                Vector3 lightDirection = new Vector3(0.0f, 0.0f, -1.0f);
                lightDirection = RenderMaths.normalizeVector(lightDirection);

                float dp = RenderMaths.dotProduct(normal, lightDirection);
                tri.brightness = dp;

                tri.color = Color.getHSBColor(0.7f, 1.0f, Float.min(0.99f, Float.max(dp, 0.05f)));

                // apply view matrix
                tri.vertices[0] = RenderMaths.multiplyMatrixVector(tri.vertices[0], viewMatrix);
                tri.vertices[1] = RenderMaths.multiplyMatrixVector(tri.vertices[1], viewMatrix);
                tri.vertices[2] = RenderMaths.multiplyMatrixVector(tri.vertices[2], viewMatrix);

                triangles.add(tri);
            }
        }

        // depth sorting => triangles further away from the camera are drawn first
        triangles.sort((o1, o2) -> {
            float midPoint1 = o1.midPointVal();
            float midPoint2 = o2.midPointVal();

            if (midPoint1 > midPoint2)
                return 1;
            else if (midPoint1 == midPoint2)
                return 0;
            return -1;
        });

        // @@@@@@@@@@@@ still need triangle clipping
       for(int i = 0; i < triangles.size(); i++){
            Triangle triangle = triangles.get(i);
            triangle.vertices[0] = RenderMaths.multiplyMatrixVector(triangle.vertices[0], projectionMatrix);
            triangle.vertices[1] = RenderMaths.multiplyMatrixVector(triangle.vertices[1], projectionMatrix);
            triangle.vertices[2] = RenderMaths.multiplyMatrixVector(triangle.vertices[2], projectionMatrix);

            /***
             * normalize
             */
            triangle.vertices[0] = RenderMaths.multiplyVector(triangle.vertices[0], 1 / triangle.vertices[0].w);
            triangle.vertices[1] = RenderMaths.multiplyVector(triangle.vertices[1], 1 / triangle.vertices[1].w);
            triangle.vertices[2] = RenderMaths.multiplyVector(triangle.vertices[2], 1 / triangle.vertices[2].w);



            //Offset into View => 0/0 is in the middle of the screen not in the right upper corner
            triangle.vertices[0] = RenderMaths.addVectors(triangle.vertices[0], new Vector3(1.0f, 1.0f, 0));
            triangle.vertices[1] = RenderMaths.addVectors(triangle.vertices[1], new Vector3(1.0f, 1.0f, 0));
            triangle.vertices[2] = RenderMaths.addVectors(triangle.vertices[2], new Vector3(1.0f, 1.0f, 0));


            triangle.vertices[0].x *= 0.5f * width;
            triangle.vertices[0].y *= 0.5f * height;
            triangle.vertices[1].x *= 0.5f * width;
            triangle.vertices[1].y *= 0.5f * height;
            triangle.vertices[2].x *= 0.5f * width;
            triangle.vertices[2].y *= 0.5f * height;

           // if(renderObject.mesh.texture != null)
             //   drawingWindow.drawTriangle(triangle, renderObject.mesh.textureTriangles[triangle.textureIndex], renderObject.mesh.texture);
            //else
                drawingWindow.drawTriangle(triangle);
        }
    }

    public void setTitle(String title){
        frame.setTitle(title);
    }

    public void clearBuffer(){
        drawingWindow.clear();
    }

    public void draw(){
        drawingWindow.redraw();
    }

    public void addKeyListener(MKeyListener mKeyListener) {
        frame.addKeyListener(mKeyListener);
    }
}
