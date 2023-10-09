package src.engine.core.rendering;

import src.engine.core.inputsystem.MKeyListener;

import static java.lang.Math.*;

public class RayCasterTest {


    Frame frame;
    DrawingWindow drawingWindow;
    int width, height;
    float playerX, playerY;
    float angle;
    float playerAngle, fov;

    int tileSize = 64;
    int[][] map = {
            {1, 1, 1, 1},
            {1, 0, 0, 1},
            {1, 0, 1, 1},
            {1, 1, 1, 1}
    };

    void drawRoom(){
        // iterate over map[][]
        for( int i = 0; i < map.length; i++){
            for (int j = 0; j<map[0].length; j++){
                if(map[i][j] == 1){
                    drawingWindow.setColor(255, 255, 255);
                    drawingWindow.drawRect(i*tileSize, j*tileSize, tileSize, tileSize);
                }
            }
        }
    }

    void drawPlayer(){
        drawingWindow.setColor(255, 0, 0);

        // tile size is 64 -> adjust player position


        drawingWindow.drawRect((int) playerX , (int) playerY ,tileSize/4, tileSize/4);

        // get dx and dy from angle
        float angleToRadians = (float) (angle * (Math.PI / 180));
        float dx = (float) cos(angleToRadians);
        float dy = (float) sin(angleToRadians);


        // draw line from player to x,y
        drawingWindow.setColor(255, 0, 0);
        drawingWindow.drawRect((int) (playerX + dx * 32), (int) (playerY + dy * 32), 8, 8);

    }


    void raycast(){
        //convert angle to radians
        float rayAngle = (float) (angle * (Math.PI / 180));

        float dx = (float) cos(rayAngle);
        float dy = (float) sin(rayAngle);

        float x = playerX/tileSize;
        float y = playerY/tileSize;

        float stepSize = 0.1f;

        // do raycasting over room[][]
        for (int i = 0; i < 100; i++) {
            x += dx * stepSize;
            y += dy * stepSize;

            int mapX = (int) x;
            int mapY = (int) y;

            if (map[mapY][mapX] == 1) {
                break;
            }
        }

        // draw line from player to x,y
        drawingWindow.setColor(125, 0, 255);
       drawingWindow.drawRect((int) (x*tileSize), (int) (y*tileSize), 4, 4);

        float distance = (float) Math.sqrt(Math.pow((x - playerX) , 2) + Math.pow((y - playerY) ,2));

        System.out.println("Distance: " + distance);
        float wallHeight = 3000.f / distance;

        drawingWindow.setColor(255, 255, 255);
        drawingWindow.drawRect(400, 400, 4, (int) wallHeight);



    }

    void checkButtons(){

        if(MKeyListener.keyList['w']){
            // move player based on angle
            float rayAngle = (float) (angle * (Math.PI / 180));

            float dx = (float) cos(rayAngle);
            float dy = (float) sin(rayAngle);

            playerX += dx * 0.1f;
            playerY += dy * 0.1f;
        }
        if(MKeyListener.keyList['s']){
            float rayAngle = (float) (angle * (Math.PI / 180));

            float dx = (float)cos(rayAngle);
            float dy = (float) sin(rayAngle);

            playerX -= dx * 0.1f;
            playerY -= dy * 0.1f;
        }
        if(MKeyListener.keyList['a']){
            angle -= 0.1f;
        }
        if(MKeyListener.keyList['d']){
            angle += 0.1f;
        }

        if(angle > 360)
            angle -= 360;
        if(angle < 0)
            angle += 360;
    }


    void display()
    {
        drawingWindow.clear();
        checkButtons();
        drawRoom();
        drawPlayer();
        raycast();


        drawingWindow.redraw();
    }


    public RayCasterTest(){


        angle = 0;
        width = 800;
        height = 800;
        frame = new Frame((int) width, (int) height);
        frame.setVisible(true);
        drawingWindow = frame.getPanel();

        playerX = 40;
        playerY = 40;


        MKeyListener mKeyListener = new MKeyListener();

        frame.addKeyListener(mKeyListener);



        while(true){
            display();
        }

    }

    public static void main(String[] args) {

        RayCasterTest rayCasterTest = new RayCasterTest();

    }

}