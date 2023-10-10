package src.engine.core.rendering.SimpleRenderPipeline;

import src.engine.core.matutils.Triangle;
import src.engine.core.rendering.DrawingWindow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ObjectBuffer {

    public volatile Triangle[] triangles;

    public float[][] modelMatrix;
    public float[][] viewMatrix;

    public float[][] projectionMatrix;

    public float width, height;
    public DrawingWindow drawingWindow;
    public int threadCount = 4;


    public boolean[] finish;

    public volatile boolean[] finishTri;
    public LinkedList<Triangle> trianglesToRender;


    public synchronized void setFinish(int id){
        finish[id] = true;
    }

    public synchronized void addTriangles(List<Triangle> triangles){
        trianglesToRender.addAll(triangles);
    }

    public void prepareSecondStep(){
        finishTri = new boolean[trianglesToRender.size()];
    }

    public synchronized void setFinishTri(int id){
        finishTri[id] = true;
    }

}
