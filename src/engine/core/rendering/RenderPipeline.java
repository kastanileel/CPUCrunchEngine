package src.engine.core.rendering;

import src.engine.core.inputsystem.MKeyListener;

public class RenderPipeline {

    private static RenderPipeline instance;
    private int rayRomSizeX, rayRoomSizeY;

    private float[][] room;

    Frame frame;
    DrawingWindow drawingWindow;

    static int gridSize = 32;
    private RenderPipeline(){

        rayRomSizeX = 10;
        rayRoomSizeY = 10;

        room = new float[rayRomSizeX][rayRoomSizeY];

        int width = 1000;
        int height = 500;

        frame = new Frame((int) width, (int) height);
        frame.setVisible(true);
        drawingWindow = frame.getPanel();
    }


    public static RenderPipeline getInstance(){
        if(instance == null)
            instance = new RenderPipeline();

        return instance;
    }

    public void clear(){
        drawingWindow.clear();
    }

    public void queueForCast(float x, float y){
        room[(int) x][(int) y] = 1;                         // @@@@@@@@@@@@@@@@@ the value should be the height
                                                            // @@@@@@@@@@@@@@@@@ height should be configurable
    }

    public void rayCast() {
        // get x y z and rotX rotY and rotZ from Camera
        // do raycasting over room[][]




        double posX = Camera.getInstance().x, posY = Camera.getInstance().y;  //x and y start position
        double dirX = -1, dirY = 0; //initial direction vector
        double planeX = 0, planeY = 0.66; //the 2d raycaster version of camera plane



        drawingWindow.drawMap(room);
        drawingWindow.drawPlayer((int) posX, (int) posY, Camera.getInstance().rotY);

        renderRays2();
    }


    float M_PI = 3.1415f;
    float degToRad(float a) { return a;}
    int FixAng(int a){ if(a>359){ a-=360;} if(a<0){ a+=360;} return a;}

    public void renderRays2() {

        int mapX = 10;
        int mapY = 10;

        int r, mx, my, mp, dof, side;
        float vx, vy, rx, ry, ra, xo = 0, yo = 0, disV, disH;

        float px = Camera.getInstance().x;
        float py = Camera.getInstance().y;

        float pa = Camera.getInstance().rotY;

        ra = FixAng(pa + 30);                                                              //ray set back 30 degrees

        for (r = 0; r < 60; r++) {
            //---Vertical---
            dof = 0;
            side = 0;
            disV = 100000;
            float Tan = (float) Math.tan(degToRad(ra));
            if (Math.cos(degToRad(ra)) > 0.001) {
                rx = (((int) px >> 6) << 6) + 64;
                ry = (px - rx) * Tan + py;
                xo = 64;
                yo = -xo * Tan;
            }//looking left
            else if (Math.cos(degToRad(ra)) < -0.001) {
                rx = (float) ((((int) px >> 6) << 6) - 0.0001);
                ry = (px - rx) * Tan + py;
                xo = -64;
                yo = -xo * Tan;
            }//looking right
            else {
                rx = px;
                ry = py;
                dof = 8;
            }                                                  //looking up or down. no hit

            while (dof < 8) {
                mx = (int) (rx) >> 6;
                my = (int) (ry) >> 6;
                mp = my * mapX + mx;
                if (mx > 0 && my > 0&&  mx < mapX && my < mapY && room[mx][my] == 1) {
                    dof = 8;
                    disV = (float) (Math.cos(degToRad(ra)) * (rx - px) - Math.sin(degToRad(ra)) * (ry - py));
                }//hit
                else {
                    rx += xo;
                    ry += yo;
                    dof += 1;
                }                                               //check next horizontal
            }
            vx = rx;
            vy = ry;

            //---Horizontal---
            dof = 0;
            disH = 100000;
            Tan = (float) (1.0 / Tan);
            if (Math.sin(degToRad(ra)) > 0.001) {
                ry = (float) ((((int) py >> 6) << 6) - 0.0001);
                rx = (py - ry) * Tan + px;
                yo = -64;
                xo = -yo * Tan;
            }//looking up
            else if (Math.sin(degToRad(ra)) < -0.001) {
                ry = (((int) py >> 6) << 6) + 64;
                rx = (py - ry) * Tan + px;
                yo = 64;
                xo = -yo * Tan;
            }//looking down
            else {
                rx = px;
                ry = py;
                dof = 8;
            }                                                   //looking straight left or right

            while (dof < 8) {
                mx = (int) (rx) >> 6;
                my = (int) (ry) >> 6;
                mp = my * mapX + mx;
                if (mx > 0 && my > 0 && mx < mapX && my < mapY && room[mx][my] == 1) {
                    dof = 8;
                    disH = (float) (Math.cos(degToRad(ra)) * (rx - px) - Math.sin(degToRad(ra)) * (ry - py));
                }//hit
                else {
                    rx += xo;
                    ry += yo;
                    dof += 1;
                }                                               //check next horizontal
            }


            if (disV < disH) {
                rx = vx;
                ry = vy;
                disH = disV;

            }                  //horizontal hit first

            drawingWindow.drawLine(px,py,rx,ry);
        }
    }

    private float FixAng(float a)
    { if(a>359){ a-=360;} if(a<0){ a+=360;} return a;}
    public void redraw(){
        this.drawingWindow.redraw();
    }

    public void addKeyListener(MKeyListener mKeyListener) {
        frame.addKeyListener(mKeyListener);
    }
}
