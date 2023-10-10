package src.engine.core.rendering.SimpleRenderPipeline;

import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Triangle;
import src.engine.core.matutils.Vector3;
import src.engine.core.rendering.Camera;

import java.awt.*;
import java.util.LinkedList;

public class RenderTillDepthSort extends Thread {

    public volatile boolean run = false;

    public int id;


    ObjectBuffer objectBuffer;

    RenderTillDepthSort(ObjectBuffer objectBuffer){
        this.objectBuffer = objectBuffer;

        this.start();
        // print threaded "soos"
    }



    @Override
    public void run() {
        while (true){
            if(run){
                int size = objectBuffer.triangles.length;
                int startPos = id * (size / objectBuffer.threadCount);
                int endPos = startPos + (size / objectBuffer.threadCount);

                LinkedList<Triangle> triangles = new LinkedList<>();

                for(int i = startPos; i < endPos; i++){
                    // render object

                    Triangle triangle = objectBuffer.triangles[i];

                    Triangle tri = triangle.clone();

                    // apply model matrix
                    tri.vertices[0] = RenderMaths.multiplyMatrixVector(tri.vertices[0], objectBuffer.modelMatrix);
                    tri.vertices[1] = RenderMaths.multiplyMatrixVector(tri.vertices[1], objectBuffer.modelMatrix);
                    tri.vertices[2] = RenderMaths.multiplyMatrixVector(tri.vertices[2], objectBuffer.modelMatrix);




                    //calculate normal of triangle for lighting
                    Vector3 normal, line1, line2;

                    line1 = RenderMaths.substractVectors(tri.vertices[1], tri.vertices[0]);
                    line2 = RenderMaths.substractVectors(tri.vertices[2], tri.vertices[0]);
                    normal = RenderMaths.crossProduct(line2, line1);
                    normal = RenderMaths.normalizeVector(normal);

                    Vector3 cameraRay = RenderMaths.substractVectors(tri.vertices[0], Camera.getInstance().position);

                    if (RenderMaths.dotProduct(cameraRay, normal) > 0.0f) {
                        tri.textureIndex = i;
                        Vector3 lightDirection = new Vector3(0.0f, 0.0f, -1.0f);
                        lightDirection = RenderMaths.normalizeVector(lightDirection);

                        float dp = RenderMaths.dotProduct(normal, lightDirection);
                        tri.brightness = dp;

                        tri.color = Color.getHSBColor(0.7f, 1.0f, Float.min(0.99f, Float.max(dp, 0.05f)));

                        // apply view matrix
                        tri.vertices[0] = RenderMaths.multiplyMatrixVector(tri.vertices[0], objectBuffer.viewMatrix);
                        tri.vertices[1] = RenderMaths.multiplyMatrixVector(tri.vertices[1], objectBuffer.viewMatrix);
                        tri.vertices[2] = RenderMaths.multiplyMatrixVector(tri.vertices[2], objectBuffer.viewMatrix);

                        triangles.add(tri);
                    }


                }

                objectBuffer.addTriangles(triangles);
                run = false;
                objectBuffer.setFinish(id);

            }
        }

    }
}
