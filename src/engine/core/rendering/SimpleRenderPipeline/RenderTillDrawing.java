package src.engine.core.rendering.SimpleRenderPipeline;

import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Triangle;
import src.engine.core.matutils.Vector3;

import java.util.LinkedList;

public class RenderTillDrawing extends Thread {

    public volatile boolean run = false;

    public int id;


    ObjectBuffer objectBuffer;

    RenderTillDrawing(ObjectBuffer objectBuffer) {
        this.objectBuffer = objectBuffer;

        this.start();
        // print threaded "soos"
    }

    @Override
    public void run() {
        while (true) {
            if (run) {


                int size = objectBuffer.trianglesToRender.size() / objectBuffer.threadCount;
                int remainder = objectBuffer.trianglesToRender.size() % objectBuffer.threadCount;


                if (id < remainder) {
                    size += 1;
                }


                LinkedList<Triangle> triangles = objectBuffer.trianglesToRender;
                int highestIndex = 0;

                for (int j = 0; j < size; j++) {
                    int i = id + j * objectBuffer.threadCount;

                    Triangle triangle = triangles.get(i);
                    triangle.vertices[0] = RenderMaths.multiplyMatrixVector(triangle.vertices[0], objectBuffer.projectionMatrix);
                    triangle.vertices[1] = RenderMaths.multiplyMatrixVector(triangle.vertices[1], objectBuffer.projectionMatrix);
                    triangle.vertices[2] = RenderMaths.multiplyMatrixVector(triangle.vertices[2], objectBuffer.projectionMatrix);

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


                    triangle.vertices[0].x *= 0.5f * objectBuffer.width;
                    triangle.vertices[0].y *= 0.5f * objectBuffer.height;
                    triangle.vertices[1].x *= 0.5f * objectBuffer.width;
                    triangle.vertices[1].y *= 0.5f * objectBuffer.height;
                    triangle.vertices[2].x *= 0.5f * objectBuffer.width;
                    triangle.vertices[2].y *= 0.5f * objectBuffer.height;

                    // if(renderObject.mesh.texture != null)
                    //   drawingWindow.drawTriangle(triangle, renderObject.mesh.textureTriangles[triangle.textureIndex], renderObject.mesh.texture);
                    //else
                    objectBuffer.setFinishTri(i);

                    highestIndex = i;

                }
                run = false;
                objectBuffer.setFinish(id);
            }
        }
    }


}
