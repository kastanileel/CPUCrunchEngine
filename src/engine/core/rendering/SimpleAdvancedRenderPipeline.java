package src.engine.core.rendering;


import src.engine.core.gamemanagement.GameComponents;
import src.engine.core.inputtools.MMouseListener;
import src.engine.core.matutils.Mesh;
import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Triangle;


import src.engine.core.inputtools.MKeyListener;
import src.engine.core.matutils.Vector3;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/***
 * This class is responsible for rendering the scene.
 *
 * The render pipeline is a singleton class, so there can only be one render pipeline.
 */
public class SimpleAdvancedRenderPipeline {

    private LinkedList<Triangle> trianglesToRender;

    private ArrayList<Mesh> meshesToRender;
    private Mesh mesh;
    private float[][] projectionMatrix;
    private float width, height;
    public static SimpleAdvancedRenderPipeline instance;
    private DrawingWindow drawingWindow;
    private Frame frame;

    private boolean finishedStepTwo;

    public synchronized void setFinishedStepTwo(boolean finished) {
        this.finishedStepTwo = finished;
    }

    public synchronized boolean isFinishedStepTwo() {
        return finishedStepTwo;
    }

    private SimpleAdvancedRenderPipeline(int width, int height, int textureMaxAccuracy, int textureMinAccuracy){
        this.width = width;
        this.height = height;
        frame = new Frame((int) width, (int) height, textureMaxAccuracy, textureMinAccuracy);
        frame.setVisible(true);
        drawingWindow = frame.getPanel();

        MMouseListener.getInstance().attachToFrame(frame);
        MKeyListener.getInstance().attachToFrame(frame);

        this.trianglesToRender = new LinkedList<>();
        this.meshesToRender = new ArrayList<>();
    }

    public static SimpleAdvancedRenderPipeline getInstance(int width, int height, int textureMaxAccuracy, int textureMinAccuracy){
        if(instance == null)
            instance = new SimpleAdvancedRenderPipeline(width, height, textureMaxAccuracy, textureMinAccuracy);
        return instance;
    }

    public static float shortestDistFromPointToPlane(Vector3 p, Vector3 plane_n, Vector3 plane_p) {
        return (plane_n.x * p.x + plane_n.y * p.y + plane_n.z * p.z - RenderMaths.dotProduct(plane_n, plane_p));
    }

    public static Vector3 Vector_IntersectPlane(Vector3 plane_p, Vector3 plane_n, Vector3 lineStart, Vector3 lineEnd) {
        plane_n = RenderMaths.normalizeVector(plane_n);
        float plane_d = -RenderMaths.dotProduct(plane_n, plane_p);
        float ad = RenderMaths.dotProduct(lineStart, plane_n);
        float bd = RenderMaths.dotProduct(lineEnd, plane_n);
        float t = (-plane_d - ad) / (bd - ad);
        Vector3 lineStartToEnd = RenderMaths.substractVectors(lineEnd, lineStart);
        Vector3 lineToIntersect = RenderMaths.multiplyVector(lineStartToEnd, t);
        return RenderMaths.addVectors(lineStart, lineToIntersect);
    }


    public static Triangle[] Triangle_ClipAgainstPlane(Vector3 plane_p, Vector3 plane_n, Triangle in_tri) {
        // Make sure plane normal is indeed normal
        plane_n = RenderMaths.normalizeVector(plane_n);

        // Return signed shortest distance from point to plane, plane normal must be normalised


        // Create two temporary storage arrays to classify points either side of plane
        // If distance sign is positive, point lies on "inside" of plane
        Vector3[] inside_points = new Vector3[3];
        int nInsidePointCount = 0;
        Vector3[] outside_points = new Vector3[3];
        int nOutsidePointCount = 0;

        // Get signed distance of each point in triangle to plane
        float d0 = shortestDistFromPointToPlane(in_tri.vertices[0], plane_n, plane_p);
        float d1 = shortestDistFromPointToPlane(in_tri.vertices[1], plane_n, plane_p);
        float d2 = shortestDistFromPointToPlane(in_tri.vertices[2], plane_n, plane_p);

        if (d0 >= 0) {
            inside_points[nInsidePointCount] = in_tri.vertices[0];
            nInsidePointCount += 1;
        } else {
            outside_points[nOutsidePointCount  ] = in_tri.vertices[0];
            nOutsidePointCount += 1;
        }
        if (d1 >= 0) {
            inside_points[nInsidePointCount  ] = in_tri.vertices[1];
            nInsidePointCount += 1;
        } else {
            outside_points[nOutsidePointCount  ] = in_tri.vertices[1];
            nOutsidePointCount += 1;
        }
        if (d2 >= 0) {
            inside_points[nInsidePointCount  ] = in_tri.vertices[2];
            nInsidePointCount += 1;
        } else {
            outside_points[nOutsidePointCount  ] = in_tri.vertices[2];
            nOutsidePointCount += 1;
        }

        // Now classify triangle points, and break the input triangle into
        // smaller output triangles if required. There are four possible
        // outcomes...

        if (nInsidePointCount == 0) {
            // All points lie on the outside of plane, so clip whole triangle
            // It ceases to exist

            return null; // No returned triangles are valid
        }

        if (nInsidePointCount == 3) {
            Triangle[] erg = new Triangle[1];
            // All points lie on the inside of plane, so do nothing
            // and allow the triangle to simply pass through
            Triangle out_tri1;
            out_tri1 = in_tri;


            erg[0] = out_tri1;
            return erg; // Just the one returned original triangle is valid
        }

        if (nInsidePointCount == 1) {
            Triangle[] erg = new Triangle[1];
            // Triangle should be clipped. As two points lie outside
            // the plane, the triangle simply becomes a smaller triangle

            // Copy appearance info to new triangle

            Triangle out_tri1 = in_tri.clone();
            // The inside point is valid, so keep that...
            out_tri1.vertices[0] = inside_points[0];

            // but the two new points are at the locations where the
            // original sides of the triangle (lines) intersect with the plane
            out_tri1.vertices[1] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[0]);
            out_tri1.vertices[2] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[1]);


            erg[0] = out_tri1;

            return erg; // Return the newly formed single triangle
        }

        Triangle[] erg = new Triangle[2];
        // Triangle should be clipped. As two points lie inside the plane,
        // the clipped triangle becomes a "quad". Fortunately, we can
        // represent a quad with two new triangles

        Triangle out_tri1 = in_tri.clone();
        Triangle out_tri2 = in_tri.clone();
        // Copy appearance info to new triangles


        // The first triangle consists of the two inside points and a new
        // point determined by the location where one side of the triangle
        // intersects with the plane
        out_tri1.vertices[0] = inside_points[0];
        out_tri1.vertices[1] = inside_points[1];
        out_tri1.vertices[2] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[0]);

        // The second triangle is composed of one of he inside points, a
        // new point determined by the intersection of the other side of the
        // triangle and the plane, and the newly created point above
        out_tri2.vertices[0] = inside_points[1];
        out_tri2.vertices[1] = out_tri1.vertices[2];
        out_tri2.vertices[2] = Vector_IntersectPlane(plane_p, plane_n, inside_points[1], outside_points[0]);

        erg[0] = out_tri1;
        erg[1] = out_tri2;

        return erg; // Return two newly formed triangles which form a quad
    }

    /***
     * This method is called every render cycle for each RenderObject in the scene.
     */
    public void renderObject(Mesh mesh, Vector3 pos, Vector3 rot, Vector3 scale) {

        int trianglesMeshId = -1;
        if(mesh.texture != null){

            int meshId = meshesToRender.size();
            meshesToRender.add(mesh);

            trianglesMeshId = meshId;
        }
        Camera camera = Camera.getInstance();

        // The model matrix is used to transform the object coordinates into world coordinates.
        // (essentially positioning the obj model in the scene)
        float[][] modelMatrix = RenderMaths.identityMatrix();
        modelMatrix[0][0] *= scale.x;
        modelMatrix[1][1] *= scale.y;
        modelMatrix[2][2] *= scale.z;
        modelMatrix = RenderMaths.multiplyMatrices(modelMatrix, RenderMaths.makeRotationMatrix(rot.x, rot.y, rot.z));
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
        float fNear = .1f;
        float fFar = 1000.0f;
        float fFov = 90.0f;
        projectionMatrix = RenderMaths.projectionMatrix(fFov, fNear, fFar, height / width);

        for (int i = 0; i < mesh.triangles.length; i++) {
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
                Vector3 lightDirection = new Vector3(0.0f, -1.0f, 1.0f);
                lightDirection = RenderMaths.normalizeVector(lightDirection);

                tri.brightness = RenderMaths.dotProduct(normal, lightDirection);
                
                // apply view matrix
                tri.vertices[0] = RenderMaths.multiplyMatrixVector(tri.vertices[0], viewMatrix);
                tri.vertices[1] = RenderMaths.multiplyMatrixVector(tri.vertices[1], viewMatrix);
                tri.vertices[2] = RenderMaths.multiplyMatrixVector(tri.vertices[2], viewMatrix);
                tri.meshIndex = trianglesMeshId;

                // set distance between camera and triangle
                Vector3 midPoint = RenderMaths.multiplyVector(RenderMaths.addVectors(tri.vertices[0], RenderMaths.addVectors(tri.vertices[1], tri.vertices[2])), 1.0f / 3.0f);
                tri.distance = RenderMaths.lengthVector(RenderMaths.substractVectors(midPoint, camera.position));

                trianglesToRender.add(tri);
            }
        }

    }
    public void stepTwo(){

        // depth sorting => triangles further away from the camera are drawn first
        trianglesToRender.sort((o1, o2) -> {
            float midPoint1 = o1.midPointVal();
            float midPoint2 = o2.midPointVal();

            if (midPoint1 < midPoint2)
                return 1;
            else if (midPoint1 == midPoint2)
                return 0;
            return -1;
        });

        for (Triangle tri : trianglesToRender) {

            Vector3 plane_p = new Vector3(0, 0, 1.f);
            Vector3 plane_n = new Vector3(0, 0, 0.1f);
            Triangle[] clipped = Triangle_ClipAgainstPlane(plane_p, plane_n, tri);
            int clipCount = (clipped == null) ? 0 : clipped.length;



            // @@@@@@@@@@@@ still need triangle clipping
        for (int i = 0; i < clipCount; i++) {

            tri.vertices[0] = RenderMaths.multiplyMatrixVector(tri.vertices[0], projectionMatrix);
            tri.vertices[1] = RenderMaths.multiplyMatrixVector(tri.vertices[1], projectionMatrix);
            tri.vertices[2] = RenderMaths.multiplyMatrixVector(tri.vertices[2], projectionMatrix);

            /***
             * normalize
             */
            tri.vertices[0] = RenderMaths.multiplyVector(tri.vertices[0], 1 / tri.vertices[0].w);
            tri.vertices[1] = RenderMaths.multiplyVector(tri.vertices[1], 1 / tri.vertices[1].w);
            tri.vertices[2] = RenderMaths.multiplyVector(tri.vertices[2], 1 / tri.vertices[2].w);



            //Offset into View => 0/0 is in the middle of the screen not in the right upper corner
            tri.vertices[0] = RenderMaths.addVectors(tri.vertices[0], new Vector3(1.0f, 1.0f, 0));
            tri.vertices[1] = RenderMaths.addVectors(tri.vertices[1], new Vector3(1.0f, 1.0f, 0));
            tri.vertices[2] = RenderMaths.addVectors(tri.vertices[2], new Vector3(1.0f, 1.0f, 0));


            tri.vertices[0].x *= 0.5f * width;
            tri.vertices[0].y *= 0.5f * height;
            tri.vertices[1].x *= 0.5f * width;
            tri.vertices[1].y *= 0.5f * height;
            tri.vertices[2].x *= 0.5f * width;
            tri.vertices[2].y *= 0.5f * height;

            List<Triangle> clippedTriangs = new LinkedList<>();

            Triangle[] rasterClipped = new Triangle[2];
            rasterClipped[0] = new Triangle();
            rasterClipped[1] = new Triangle();
            clippedTriangs.add(tri);
            int nNewTriangles = 1;

            for (int p = 0; p < 4; p++) {
                int nTrisToAdd = 0;
                while (nNewTriangles > 0) {
                    // Take triangle from front of queue
                    Triangle test = clippedTriangs.remove(0);
                    nNewTriangles--;

                    // Clip it against a plane. We only need to test each
                    // subsequent plane, against subsequent new triangles
                    // as all triangles after a plane clip are guaranteed
                    // to lie on the inside of the plane. I like how this
                    // comment is almost completely and utterly justified
                    switch (p) {
                        case 0 -> {
                            rasterClipped = Triangle_ClipAgainstPlane(new Vector3(0.0f, 0.0f, 0.0f), new Vector3(0.0f, 1.0f, 0.0f), test);
                            nTrisToAdd = (rasterClipped == null) ? 0 : clipped.length;
                        }
                        case 1 -> {
                            rasterClipped = Triangle_ClipAgainstPlane(new Vector3(0.0f, (float) frame.getHeight() - 1, 0.0f), new Vector3(0.0f, -1.0f, 0.0f), test);
                            nTrisToAdd = (rasterClipped == null) ? 0 : clipped.length;
                        }
                        case 2 -> {
                            rasterClipped = Triangle_ClipAgainstPlane(new Vector3(0.0f, 0.0f, 0.0f), new Vector3(1.0f, 0.0f, 0.0f), test);
                            nTrisToAdd = (rasterClipped == null) ? 0 : clipped.length;
                        }
                        case 3 -> {
                            rasterClipped = Triangle_ClipAgainstPlane(new Vector3((float) frame.getWidth() - 1, 0.0f, 0.0f), new Vector3(-1.0f, 0.0f, 0.0f), test);
                            nTrisToAdd = (rasterClipped == null) ? 0 : clipped.length;
                        }
                    }

                    // Clipping may yield a variable number of triangles, so
                    // add these new ones to the back of the queue for subsequent
                    // clipping against next planes
                    for (int w = 0; w < nTrisToAdd; w++)
                        clippedTriangs.add(clipped[w]);
                }
                nNewTriangles = clippedTriangs.size();
            }

            for (Triangle triangle : clippedTriangs) {
                // switch between different render types
                switch (triangle.renderType){
                    case OneColor -> drawingWindow.drawTriangle(triangle);
                    case OutlineOnly -> drawingWindow.drawTriangleOutline(triangle);
                    case Textured -> {
                        mesh =  meshesToRender.get(triangle.meshIndex);
                        drawingWindow.drawTriangleImproved(triangle, mesh.textureTriangles[triangle.textureIndex], mesh.texture);
                    }
                    case TexturedAndOutline -> {
                        mesh =  meshesToRender.get(triangle.meshIndex);
                        drawingWindow.drawTriangleImprovedOutline(triangle, mesh.textureTriangles[triangle.textureIndex], mesh.texture);
                    }
                    case Emissive -> {
                        drawingWindow.drawTriangleNoLighting(triangle);
                    }
                }

            }
        }


            //
        }

        trianglesToRender.clear();
        meshesToRender.clear();
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

}
