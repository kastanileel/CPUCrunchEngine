package src.engine.core.inputsystem;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MKeyListener extends KeyAdapter {
    public static boolean[] keyList;

    public MKeyListener(){
        keyList = new boolean[128];
    }

    @Override
    public void keyPressed(KeyEvent event) {
        System.out.println(event.getKeyCode());
        keyList[event.getKeyChar()] = true;
    }

    @Override
    public void keyReleased(KeyEvent event){
        keyList[event.getKeyChar()] = false;
    }
}
