package src.engine.core.rendering;

import src.engine.core.inputsystem.MKeyListener;

import static java.lang.Math.*;

public class RayCasterTest {

    Frame frame;
    DrawingWindow drawingWindow;
int mapX = 8;
int mapY  = 8;      //map height
int mapS = 64;      //map cube size
    int map[]=           //the map array. Edit to change level but keep the outer walls
            {
                    1,1,1,1,1,1,1,1,
                    1,0,1,0,0,0,0,1,
                    1,0,1,0,0,0,0,1,
                    1,0,1,0,0,0,0,1,
                    1,0,0,0,0,0,0,1,
                    1,0,0,0,0,1,0,1,
                    1,0,0,0,0,0,0,1,
                    1,1,1,1,1,1,1,1,
            };

    void drawMap2D()
    {
        int x,y,xo,yo;
        for(y=0;y<mapY;y++)
        {
            for(x=0;x<mapX;x++)
            {
                if(map[y*mapX+x]==1)
                { drawingWindow.red();}
                else{ drawingWindow.blue();}
                xo=x*mapS; yo=y*mapS;

                drawingWindow.drawRect(xo+1, yo+1, mapS + xo -1, mapS + yo -1);

            }
        }
    }//-----------------------------------------------------------------------------


    //------------------------PLAYER------------------------------------------------
    double M_PI = 3.141569;
    float degToRad(int a) { return (float) (a*M_PI/180.0);}
    int FixAng(int a){ if(a>359){ a-=360;} if(a<0){ a+=360;} return a;}

    float px,py,pdx,pdy,pa;

    public void addKeyListener(MKeyListener mKeyListener) {
        frame.addKeyListener(mKeyListener);
    }

    void drawPlayer2D()
    {
        drawingWindow.green();
        drawingWindow.drawRect((int) px,(int) py,(int) px+ (int)pdx*20, (int)py+ (int) pdy*20);
        // glVertex2i(px,py); glVertex2i(px+pdx*20,py+pdy*20); glEnd();
    }

    void Buttons()
    {
        if(MKeyListener.keyList['a']){ pa+=0.5; pa=FixAng((int) pa); pdx= (float) cos(degToRad((int) pa)); pdy= (float) -sin(degToRad((int) pa));}
            if(MKeyListener.keyList['d']){ pa-=0.5; pa=FixAng((int) pa); pdx= (float) cos(degToRad((int) pa)); pdy= (float) -sin(degToRad((int) pa));}
                if(MKeyListener.keyList['w']){ px+=pdx*0.1; py+=pdy*0.1;}
                    if(MKeyListener.keyList['s']){ px-=pdx*0.1; py-=pdy*0.1;}

    }//-----------------------------------------------------------------------------


    //---------------------------Draw Rays and Walls--------------------------------
    //float distance(ax,ay,bx,by,ang){ return cos(degToRad(ang))*(bx-ax)-sin(degToRad(ang))*(by-ay);}

    void drawRays2D()
    {
        //glColor3f(0,1,1); glBegin(GL_QUADS); glVertex2i(526,  0); glVertex2i(1006,  0); glVertex2i(1006,160); glVertex2i(526,160); glEnd();
       // glColor3f(0,0,1); glBegin(GL_QUADS); glVertex2i(526,160); glVertex2i(1006,160); glVertex2i(1006,320); glVertex2i(526,320); glEnd();

        int r,mx,my,mp,dof,side; float vx,vy,rx,ry,ra,xo = 0,yo = 0,disV,disH;

        ra=FixAng((int) (pa+30));                                                              //ray set back 30 degrees

        for(r=0;r<60;r++)
        {
            //---Vertical---
            dof=0; side=0; disV=100000;
            float Tan= (float) tan(degToRad((int) ra));
            if(cos(degToRad((int) ra))> 0.001){ rx=(((int)px>>6)<<6)+64;      ry=(px-rx)*Tan+py; xo= 64; yo=-xo*Tan;}//looking left
            else if(cos(degToRad((int) ra))<-0.001){ rx= (float) ((((int)px>>6)<<6) -0.0001); ry=(px-rx)*Tan+py; xo=-64; yo=-xo*Tan;}//looking right
            else { rx=px; ry=py; dof=8;}                                                  //looking up or down. no hit

            while(dof<8)
            {
                mx=(int)(rx)>>6; my=(int)(ry)>>6; mp=my*mapX+mx;
                if(mp>0 && mp<mapX*mapY && map[mp]==1){ dof=8; disV= (float) (cos(degToRad((int) ra))*(rx-px)-sin(degToRad((int) ra))*(ry-py));}//hit
                else{ rx+=xo; ry+=yo; dof+=1;}                                               //check next horizontal
            }
            vx=rx; vy=ry;

            //---Horizontal---
            dof=0; disH=100000;
            Tan= (float) (1.0/Tan);
            if(sin(degToRad((int) ra))> 0.001){ ry= (float) ((((int)py>>6)<<6) -0.0001); rx=(py-ry)*Tan+px; yo=-64; xo=-yo*Tan;}//looking up
            else if(sin(degToRad((int) ra))<-0.001){ ry=(((int)py>>6)<<6)+64;      rx=(py-ry)*Tan+px; yo= 64; xo=-yo*Tan;}//looking down
            else{ rx=px; ry=py; dof=8;}                                                   //looking straight left or right

            while(dof<8)
            {
                mx=(int)(rx)>>6; my=(int)(ry)>>6; mp=my*mapX+mx;
                if(mp>0 && mp<mapX*mapY && map[mp]==1){ dof=8; disH= (float) (cos(degToRad((int) ra))*(rx-px)-sin(degToRad((int) ra))*(ry-py));}//hit
                else{ rx+=xo; ry+=yo; dof+=1;}                                               //check next horizontal
            }

            drawingWindow.green();
            if(disV<disH){ rx=vx; ry=vy; disH=disV;// glColor3f(0,0.6,0);
                 }                  //horizontal hit first
           // glLineWidth(2); glBegin(GL_LINES); glVertex2i(px,py); glVertex2i(rx,ry); glEnd();//draw 2D ray
            drawingWindow.drawLine(px, py, rx, ry);

            int ca=FixAng((int) (pa-ra)); disH= (float) (disH*cos(degToRad(ca)));                            //fix fisheye
            int lineH = (int) ((mapS*320)/(disH)); if(lineH>320){ lineH=320;}                     //line height and limit
            int lineOff = 160 - (lineH>>1);                                               //line offset

           // drawingWindow.drawLine();
            //glLineWidth(8);glBegin(GL_LINES);glVertex2i(r*8+530,lineOff);glVertex2i(r*8+530,lineOff+lineH);glEnd();//draw vertical wall

            ra=FixAng((int) (ra-1));                                                              //go to next ray
        }
    }//-----------------------------------------------------------------------------


    void init()
    {
        drawingWindow.clear();
       // gluOrtho2D(0,1024,510,0);
        px=150; py=400; pa=90;
        pdx= (float) cos(degToRad((int) pa)); pdy= (float) -sin(degToRad((int) pa));
    }

    void display()
    {
       drawingWindow.clear();
        drawMap2D();
        drawPlayer2D();
        drawRays2D();
        Buttons();
      drawingWindow.redraw();
    }


    public RayCasterTest(){



        frame = new Frame((int) 1000, (int) 500);
        frame.setVisible(true);
        drawingWindow = frame.getPanel();

        MKeyListener mKeyListener = new MKeyListener();

        addKeyListener(mKeyListener);

        init();

        while(true){
           display();
        }

    }

    public static void main(String[] args) {

         RayCasterTest rayCasterTest = new RayCasterTest();



    }

}
