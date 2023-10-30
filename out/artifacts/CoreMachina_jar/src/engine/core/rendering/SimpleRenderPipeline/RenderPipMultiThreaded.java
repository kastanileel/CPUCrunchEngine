package src.engine.core.rendering.SimpleRenderPipeline;

import src.engine.core.inputsystem.MKeyListener;
import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Triangle;
import src.engine.core.matutils.Vector3;
import src.engine.core.rendering.Camera;
import src.engine.core.rendering.DrawingWindow;
import src.engine.core.rendering.Frame;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RenderPipMultiThreaded {

    private float width, height;
    public static RenderPipMultiThreaded instance;
    private DrawingWindow drawingWindow;
    private Frame frame;

    private int firstStepThreads = 8;
    private int secondStepThreads = 1;
    private RenderTillDepthSort[] pips;
    private RenderTillDrawing[] pips2;

    private DrawingThread drawingThread;
    ObjectBuffer objectBuffer;

    private RenderPipMultiThreaded(int width, int height){
        this.width = width;
        this.height = height;
        frame = new Frame((int) width, (int) height);
        frame.setVisible(true);
        drawingWindow = frame.getPanel();

        objectBuffer = new ObjectBuffer();
        pips = new RenderTillDepthSort[firstStepThreads];
        pips2 = new RenderTillDrawing[secondStepThreads];

        for(int i = 0; i < firstStepThreads; i++){
            pips[i] = new RenderTillDepthSort(objectBuffer);
            pips[i].id = i;
        }

        for(int i = 0; i < secondStepThreads; i++){
            pips2[i] = new RenderTillDrawing(objectBuffer);
            pips2[i].id = i;
        }

        drawingThread = new DrawingThread(objectBuffer);


    }

    public static RenderPipMultiThreaded getInstance(int width, int height){
        if(instance == null)
            instance = new RenderPipMultiThreaded(width, height);
        return instance;
    }

    public void renderObject(Mesh mesh, Vector3 pos, Vector3 rot) throws InterruptedException {

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


        //for(int i = 0; i < mesh.triangles.length; i++){
            objectBuffer.trianglesToRender = new LinkedList<>();
            objectBuffer.triangles = mesh.triangles;
            objectBuffer.modelMatrix = modelMatrix;
            objectBuffer.viewMatrix = viewMatrix;
            objectBuffer.finish = new boolean[firstStepThreads];
            objectBuffer.threadCount = firstStepThreads;

            for(int j = 0; j < firstStepThreads; j++){
              //  pips[j] = new PipelineTillDepthSort(objectBuffer);
                pips[j].run = true;
               // pips[j].join();
            }
            boolean a = false;
            while (!a){
                a = true;
                for(int j = 0; j < firstStepThreads; j++){
                 //   System.out.print(objectBuffer.finish[j] + " ,");
                    if(!objectBuffer.finish[j]){
                        a = false;
                    }
                }

               // System.out.println();
            }


            // sort triangles by depth
        objectBuffer.trianglesToRender.sort((o1, o2) -> {
            float midPoint1 = o1.midPointVal();
            float midPoint2 = o2.midPointVal();

            if (midPoint1 > midPoint2)
                return 1;
            else if (midPoint1 == midPoint2)
                return 0;
            return -1;
        });



            objectBuffer.finish = new boolean[secondStepThreads];
            objectBuffer.projectionMatrix = projectionMatrix;
            objectBuffer.width = width;
            objectBuffer.height = height;
            objectBuffer.drawingWindow = drawingWindow;
            objectBuffer.threadCount = secondStepThreads;

        objectBuffer.prepareSecondStep();


        for(int j = 0; j < secondStepThreads; j++){
            //  pips[j] = new PipelineTillDepthSort(objectBuffer);
            pips2[j].run = true;

            // pips[j].join();
        }
        drawingThread.run = true;
        a = false;
        while (drawingThread.run){

        }


        }

    //}

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
