package src.engine.core.rendering;


import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Triangle;
import src.engine.core.matutils.Vector3;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * This class is the drawing window.
 * It contains the image buffer and offers methods for drawing textured and untextured triangles.
 */
public class DrawingWindow extends JPanel {
    private GraphicsConfiguration graphicsConf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private BufferedImage imageBuffer;
    private Graphics graphics;

    int debugOffset = 0;


    public DrawingWindow(int width, int height){

        this.setSize(width,height);
        imageBuffer = graphicsConf.createCompatibleImage(
                this.getWidth(), this.getHeight());

        graphics = imageBuffer.getGraphics();

        ((Graphics2D) graphics).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

    }

    public void blue(){
        graphics.setColor(Color.blue);
    }

    public void setColor(int r, int g, int b){
        graphics.setColor(new Color(r,g,b));
    }

    public void drawPoint(float x, float y, float x2, float y2){
        graphics.fillOval((int) x, (int) y, (int) x2, (int) y2);
    }

    public void red(){
        graphics.setColor(Color.red);
    }
    public void green(){
        graphics.setColor(Color.green);
    }

    public void drawRect(int x, int y, int x2, int y2){
        graphics.fillRect(x,y,x2, y2);
    }

    /***
     * This method draws the image buffer after every object in this render cycle is drawn.
     *
     * The technique is called double buffering. It prevents flickering.
     */
    public void redraw() {
        this.getGraphics().drawImage(imageBuffer, 0, 0, this);
    }

    /***
     * This method draws a triangle on the image buffer.
     *
     * This method is used for untextured triangles.
     * @param triangle The projected triangle to draw.
     */


    /***
     * This method clears the image buffer before every render cycle.
     */
    public void clear(){
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    public void drawRow(int x){
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.fillRect(x, 0, 1, 300);

    }



    public synchronized void drawTriangle(Triangle triangle){
        graphics.setColor(triangle.color);




        graphics.fillPolygon( new Polygon(
                new int[]{(int) triangle.vertices[0].x, (int) triangle.vertices[1].x, (int) triangle.vertices[2].x},
                new int[]{(int) triangle.vertices[0].y, (int) triangle.vertices[1].y, (int) triangle.vertices[2].y},
                3
        ));
    }

    public synchronized void drawTriangle(Triangle triangle, Color color){
        graphics.setColor(color);




        graphics.fillPolygon( new Polygon(
                new int[]{(int) triangle.vertices[0].x, (int) triangle.vertices[1].x, (int) triangle.vertices[2].x},
                new int[]{(int) triangle.vertices[0].y, (int) triangle.vertices[1].y, (int) triangle.vertices[2].y},
                3
        ));
    }

    public void drawTriangleOutline(Triangle triangle){
        graphics.setColor(triangle.color);

        // set stroke
        ((Graphics2D) graphics).setStroke(new BasicStroke(2));

        graphics.drawPolygon( new Polygon(
                new int[]{(int) triangle.vertices[0].x, (int) triangle.vertices[1].x, (int) triangle.vertices[2].x},
                new int[]{(int) triangle.vertices[0].y, (int) triangle.vertices[1].y, (int) triangle.vertices[2].y},
                3
        ));
    }

    public void drawTriangleOutline(Triangle triangle, Color color, int stroke){
        graphics.setColor(color);

        // set stroke
        ((Graphics2D) graphics).setStroke(new BasicStroke(stroke));

        graphics.drawPolygon( new Polygon(
                new int[]{(int) triangle.vertices[0].x, (int) triangle.vertices[1].x, (int) triangle.vertices[2].x},
                new int[]{(int) triangle.vertices[0].y, (int) triangle.vertices[1].y, (int) triangle.vertices[2].y},
                3
        ));
    }



    /***
     * This method draws a textured triangle on the image buffer.
     * @param triangle The projected triangle to draw.
     * @param textureTriangle The corresponding texture triangle with uv coordinates.
     * @param texture The texture to draw.
     */
    public void drawTriangle(Triangle triangle, Triangle textureTriangle, BufferedImage texture) {

        Graphics2D g2d = (Graphics2D) graphics;

        float colorDarken = Math.max(Math.min(1.0f, triangle.brightness * 1.5f), 0.2f);


        // Loop through the bounding box of the triangle and render each pixel
        int minX = (int) Math.floor(Math.min(triangle.vertices[0].x, Math.min(triangle.vertices[1].x, triangle.vertices[2].x)));
        int maxX = (int) Math.ceil(Math.max(triangle.vertices[0].x, Math.max(triangle.vertices[1].x, triangle.vertices[2].x)));
        int minY = (int) Math.floor(Math.min(triangle.vertices[0].y, Math.min(triangle.vertices[1].y, triangle.vertices[2].y)));
        int maxY = (int) Math.ceil(Math.max(triangle.vertices[0].y, Math.max(triangle.vertices[1].y, triangle.vertices[2].y)));



        for (int y = minY; y <= maxY; y+= 1) {
            for (int x = minX; x <= maxX; x+= 1) {

                Vector3 barycentric = RenderMaths.getBarycentricCoordinates(triangle.vertices[0], triangle.vertices[1], triangle.vertices[2], new Vector3(x , y , 0));

                if (barycentric.x < 0 || barycentric.y < 0 || barycentric.z < 0) {
                    continue; // Pixel is outside the triangle
                }

                // Calculate texture coordinates of the pixel
                float u = barycentric.x * textureTriangle.vertices[0].x + barycentric.y * textureTriangle.vertices[1].x + barycentric.z * textureTriangle.vertices[2].x;
                float v = barycentric.x * textureTriangle.vertices[0].y + barycentric.y * textureTriangle.vertices[1].y + barycentric.z * textureTriangle.vertices[2].y;

                Color color = new Color(texture.getRGB((int) (u * texture.getWidth()), (int) (v * texture.getHeight())));
                color = new Color((int) (color.getRed() * colorDarken), (int) (color.getGreen() * colorDarken), (int) (color.getBlue() * colorDarken));

                // Draw the pixel
                g2d.setColor(color);
                g2d.fillRect(x, y, 1, 1);
            }
        }
    }

    public void drawTriangleImproved(Triangle triangle, Triangle textureTriangle, BufferedImage texture) {

        //System.out.println("drawTriangleImproved");
        int maxAccuracy = 128;
        int accuracy = maxAccuracy;

        // calculate distance from camera to triangle (midpoint of triangle)
        float distance = triangle.distance;

        //System.out.println(distance);

        // the bigger the distance the less accurate the triangle is drawn
        accuracy = (int) (accuracy/(distance/10.0f));
        // accuracy must be at least 1
        accuracy = Math.max(16, accuracy);
        accuracy = Math.min(maxAccuracy, accuracy);
       
        Graphics2D g2d = (Graphics2D) graphics;

        float colorDarken = Math.max(Math.min(1.0f, triangle.brightness * 1.5f), 0.2f);


        // Loop through the bounding box of the triangle and render each pixel
        int minX = (int) Math.floor(Math.min(triangle.vertices[0].x, Math.min(triangle.vertices[1].x, triangle.vertices[2].x)));
        int maxX = (int) Math.ceil(Math.max(triangle.vertices[0].x, Math.max(triangle.vertices[1].x, triangle.vertices[2].x)));
        int minY = (int) Math.floor(Math.min(triangle.vertices[0].y, Math.min(triangle.vertices[1].y, triangle.vertices[2].y)));
        int maxY = (int) Math.ceil(Math.max(triangle.vertices[0].y, Math.max(triangle.vertices[1].y, triangle.vertices[2].y)));

        // divide bounding box width and height / 100 and round up
       // accuracy += (int) Math.ceil(Math.max(maxX - minX, maxY - minY) / 100.0f);
        
        
        int xInkrement = Math.max(1, (maxX - minX) / accuracy) + 1;
        int yInkrement = Math.max(1, (maxY - minY) / accuracy) + 1;


         //drawTriangle(triangle, new Color(102, 84, 61, 255));

         int lastY = 0;
         int lastX = 0;

        for (int y = minY; y <= maxY; y+= yInkrement) {
            for (int x = minX; x <= maxX; x+= xInkrement) {

                Vector3 barycentric = RenderMaths.getBarycentricCoordinates(triangle.vertices[0], triangle.vertices[1], triangle.vertices[2], new Vector3(x , y , 0));

                if (barycentric.x < 0 || barycentric.y < 0 || barycentric.z < 0) {
                    continue; // Pixel is outside the triangle
                }

                lastY = y;
                lastX = x;

                // Calculate texture coordinates of the pixel
                float u = barycentric.x * textureTriangle.vertices[0].x + barycentric.y * textureTriangle.vertices[1].x + barycentric.z * textureTriangle.vertices[2].x;
                float v = barycentric.x * textureTriangle.vertices[0].y + barycentric.y * textureTriangle.vertices[1].y + barycentric.z * textureTriangle.vertices[2].y;

                Color color = new Color(texture.getRGB((int) (u * texture.getWidth()), (int) (v * texture.getHeight())));
                color = new Color((int) (color.getRed() * colorDarken), (int) (color.getGreen() * colorDarken), (int) (color.getBlue() * colorDarken));

                // Draw the pixel
                g2d.setColor(color);


                g2d.fillRect(x - xInkrement,y - yInkrement, xInkrement , yInkrement );
            }
           // g2d.fillRect(lastX, lastY, xInkrement , yInkrement );
        }
        //g2d.fillRect(lastX, lastY, xInkrement , yInkrement );




        int thickness = (int) (accuracy/10.0f);
     //  if(distance < 10)
          //  drawTriangleOutline(triangle,new Color(255, 0, 0, 255), thickness);
    }


    public void drawTriangle1(Triangle triangle, Triangle textureTriangle, BufferedImage texture) {
        Graphics2D g2d = (Graphics2D) graphics;

        float colorDarken = Math.max(Math.min(1.0f, triangle.brightness * 1.5f), 0.2f);

        int minX = (int) Math.floor(Math.min(triangle.vertices[0].x, Math.min(triangle.vertices[1].x, triangle.vertices[2].x)));
        int maxX = (int) Math.ceil(Math.max(triangle.vertices[0].x, Math.max(triangle.vertices[1].x, triangle.vertices[2].x)));
        int minY = (int) Math.floor(Math.min(triangle.vertices[0].y, Math.min(triangle.vertices[1].y, triangle.vertices[2].y)));
        int maxY = (int) Math.ceil(Math.max(triangle.vertices[0].y, Math.max(triangle.vertices[1].y, triangle.vertices[2].y)));

        BufferedImage triangleImage = new BufferedImage(maxX - minX + 1, maxY - minY + 1, BufferedImage.TYPE_INT_ARGB);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Vector3 barycentric = RenderMaths.getBarycentricCoordinates(triangle.vertices[0], triangle.vertices[1], triangle.vertices[2], new Vector3(x, y, 0));

                if (barycentric.x < 0 || barycentric.y < 0 || barycentric.z < 0) {
                    continue;
                }

                float u = barycentric.x * textureTriangle.vertices[0].x + barycentric.y * textureTriangle.vertices[1].x + barycentric.z * textureTriangle.vertices[2].x;
                float v = barycentric.x * textureTriangle.vertices[0].y + barycentric.y * textureTriangle.vertices[1].y + barycentric.z * textureTriangle.vertices[2].y;

                int rgb = texture.getRGB((int) (u * texture.getWidth()), (int) (v * texture.getHeight()));
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                red = (int) Math.min(255, red * colorDarken);
                green = (int) Math.min(255, green * colorDarken);
                blue = (int) Math.min(255, blue * colorDarken);

                triangleImage.setRGB(x - minX, y - minY, (rgb & 0xFF000000) | (red << 16) | (green << 8) | blue);
            }
        }

        g2d.drawImage(triangleImage, minX, minY, null);
    }



    public void drawTriangle2(Triangle triangle, Triangle textureTriangle, BufferedImage texture) {
        Graphics2D g2d = (Graphics2D) graphics;

        float colorDarken = Math.max(Math.min(1.0f, triangle.brightness * 1.5f), 0.2f);

        int minX = (int) Math.floor(Math.min(triangle.vertices[0].x, Math.min(triangle.vertices[1].x, triangle.vertices[2].x)));
        int maxX = (int) Math.ceil(Math.max(triangle.vertices[0].x, Math.max(triangle.vertices[1].x, triangle.vertices[2].x)));
        int minY = (int) Math.floor(Math.min(triangle.vertices[0].y, Math.min(triangle.vertices[1].y, triangle.vertices[2].y)));
        int maxY = (int) Math.ceil(Math.max(triangle.vertices[0].y, Math.max(triangle.vertices[1].y, triangle.vertices[2].y)));

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        BufferedImage smallImage = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                float scaledX = x * ((float)width / 16) + minX;
                float scaledY = y * ((float)height / 16) + minY;

                Vector3 barycentric = RenderMaths.getBarycentricCoordinates(triangle.vertices[0], triangle.vertices[1], triangle.vertices[2], new Vector3(scaledX, scaledY, 0));

                if (barycentric.x < 0 || barycentric.y < 0 || barycentric.z < 0) {
                    continue;
                }

                float u = barycentric.x * textureTriangle.vertices[0].x + barycentric.y * textureTriangle.vertices[1].x + barycentric.z * textureTriangle.vertices[2].x;
                float v = barycentric.x * textureTriangle.vertices[0].y + barycentric.y * textureTriangle.vertices[1].y + barycentric.z * textureTriangle.vertices[2].y;

                int rgb = texture.getRGB((int) (u * texture.getWidth()), (int) (v * texture.getHeight()));
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                red = (int) Math.min(255, red * colorDarken);
                green = (int) Math.min(255, green * colorDarken);
                blue = (int) Math.min(255, blue * colorDarken);

                smallImage.setRGB(x, y, (rgb & 0xFF000000) | (red << 16) | (green << 8) | blue);
            }
        }

        BufferedImage triangleImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = triangleImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(smallImage, 0, 0, width, height, null);
        g2d.dispose();

        g2d = (Graphics2D) graphics;
        g2d.drawImage(triangleImage, minX, minY, null);
    }



    public void drawTriangle3(Triangle triangle, Triangle textureTriangle, BufferedImage texture) {
        Graphics2D g2d = (Graphics2D) graphics;

        // get system time
        long startTime = System.nanoTime();

        float rotation = (float) Math.PI / 4.0f;//*startTime / 10000000000.0f;

        // Define new triangle with vertices in screen space
        Triangle newTriangle = new Triangle(
                new Vector3(100, 300, 0),
                new Vector3(100, 100, 0),
                new Vector3(300, 300, 0)
        );

        Triangle newTextureTri = new Triangle(
                new Vector3(0.0f, 0.0f, 0),
                new Vector3(0.0f, 1f, 0),
                new Vector3(1.0f, 1f, 0)
        );

        double cx = (newTextureTri.vertices[0].x + newTextureTri.vertices[1].x + newTextureTri.vertices[2].x) / 3;
        double cy = (newTextureTri.vertices[0].y + newTextureTri.vertices[1].y + newTextureTri.vertices[2].y) / 3;

        Polygon texturePolygon = new Polygon(
                new int[]{(int) newTextureTri.vertices[0].x, (int) newTextureTri.vertices[1].x, (int) newTextureTri.vertices[2].x},
                new int[]{(int) newTextureTri.vertices[0].y, (int) newTextureTri.vertices[1].y, (int) newTextureTri.vertices[2].y},
                3
        );

        // get old width and height of texture polygon
        int oldWidth = texturePolygon.getBounds().width;
        int oldHeight = texturePolygon.getBounds().height;

        AffineTransform transform1 = new AffineTransform();
        transform1.rotate(rotation, cx, cy);
        Shape transformedShape = transform1.createTransformedShape(texturePolygon);


        if (transformedShape instanceof Polygon) {
            texturePolygon = (Polygon) transformedShape;
        } else {
            // Handle the case where the transformed shape isn't a polygon (unlikely with simple rotations, but possible with other transforms)

        }

        // get new width and height of texture polygon
        int newWidth = texturePolygon.getBounds().width;
        int newHeight = texturePolygon.getBounds().height;

        // scale texture polygon to fit old width and height
        AffineTransform transform2 = new AffineTransform();
        transform2.scale((double) oldWidth / newWidth, (double) oldHeight / newHeight);

        // apply this to the texture!!!!\
        AffineTransformOp op = new AffineTransformOp(transform2, AffineTransformOp.TYPE_BILINEAR);
        texture = op.filter(texture, null);


        // create polygon from newTextureTri


       // minX, minY, maxX, maxY of the rotated polygon
        int minX = texturePolygon.getBounds().x;
        int minY = texturePolygon.getBounds().y;
        int maxX = texturePolygon.getBounds().x + texturePolygon.getBounds().width;
        int maxY = texturePolygon.getBounds().y + texturePolygon.getBounds().height;

        if(minX < 0){
            minX = 0;
        }
        if(minY < 0){
            minY = 0;
        }


        //rotate texture for 135 degrees



        // calculate vector from center to top right corner saying center is origin
        Vector3 topRight = new Vector3((float) (0-cx) * texture.getWidth(), (float) (0-cy) * texture.getHeight(), 0);
        // rotate vector by rotation
        float newX = (float) (topRight.x * Math.cos(rotation) - topRight.y * Math.sin(rotation));
        float newY = (float) (topRight.x * Math.sin(rotation) + topRight.y * Math.cos(rotation));

        // calculate the x dist and the y dist, so we know how much to translate the texture
        float xDist = (float) Math.abs(newX - topRight.x);
        float yDist = (float) Math.abs(newY - topRight.y);

        // double the xDist and yDist because we only looked at one side of the triangle
        xDist *= 2;
        yDist *= 2;

        // calculate scale relative to the original width and height
        float tx = (xDist + texture.getWidth())/ texture.getWidth();
        float ty = (yDist + texture.getHeight())/ texture.getHeight();

        // based on

     //   System.out.println("XDist: " + xDist + ", YDist: " + yDist + ", scale: " + tx + ", " + ty);


        AffineTransform transform3 = new AffineTransform();
        // calculate center of texture
        cx = texture.getWidth() / 2;
        cy = texture.getHeight() / 2;
        transform3.rotate(rotation, cx, cy);
        AffineTransformOp op2 = new AffineTransformOp(transform3, AffineTransformOp.TYPE_BILINEAR);
        texture = op2.filter(texture, null);




        // cut out the part for the texture based on the new texture triangle
        BufferedImage newTexture = texture.getSubimage(
                (int) ( minX * texture.getWidth()),
                (int) ( minY * texture.getHeight()),
                (int) Math.floor(( maxX - minX) * texture.getWidth()),
                (int) Math.floor(( maxY - minY) * texture.getHeight())
        );


        Polygon trianglePolygon = new Polygon(
                new int[]{(int) newTriangle.vertices[0].x, (int) newTriangle.vertices[1].x, (int) newTriangle.vertices[2].x},
                new int[]{(int) newTriangle.vertices[0].y, (int) newTriangle.vertices[1].y, (int) newTriangle.vertices[2].y},
                3
        );

// Calculate the centroid of the triangle for rotation
        cx = (newTriangle.vertices[0].x + newTriangle.vertices[1].x + newTriangle.vertices[2].x) / 3;
        cy = (newTriangle.vertices[0].y + newTriangle.vertices[1].y + newTriangle.vertices[2].y) / 3;

// Rotate the polygon around its centroid
        AffineTransform transform = new AffineTransform();
        transform.rotate(rotation, cx, cy);
        transformedShape = transform.createTransformedShape(trianglePolygon);

// Convert the transformed shape to a polygon
        PathIterator iterator = transformedShape.getPathIterator(null);
        Polygon newPolygon = new Polygon();
        while (!iterator.isDone()) {
            double[] coords = new double[6];
            int type = iterator.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                newPolygon.addPoint((int) Math.round(coords[0]), (int) Math.round(coords[1]));
            }
            iterator.next();
        }
        trianglePolygon = newPolygon;


        // Get the triangle's bounding box
        Rectangle bounds = trianglePolygon.getBounds();

        // Set the clip to the triangle so that anything drawn will only appear inside the triangle
        //g2d.setClip(trianglePolygon);

        // Draw the texture, scaled and positioned to fit inside the triangle
        g2d.drawImage(newTexture, bounds.x, bounds.y, bounds.width, bounds.height, null);
    }




}
