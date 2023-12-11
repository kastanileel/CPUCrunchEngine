package src.engine.core.tools;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MKeyListener extends KeyAdapter {

    private static MKeyListener instance;
    private boolean[] keyList;

    public static MKeyListener getInstance(){
        if (instance == null) instance = new MKeyListener();
        return instance;
    }

    private MKeyListener(){
        keyList = new boolean[128];
    }

    @Override
    public void keyPressed(KeyEvent event) {
        keyList[event.getKeyChar()] = true;
    }

    @Override
    public void keyReleased(KeyEvent event){
        keyList[event.getKeyChar()] = false;
    }

    public boolean isKeyPressed(int key){
        return keyList[key];
    }

    public void attachToFrame(Frame frame){
        frame.addKeyListener(this);
    }
}
