package src.engine.core.gamemanagement.gamelogic;

import java.util.LinkedList;

public class EventSystem {

    public static EventSystem instance = new EventSystem();

    private EventSystem(){}

    public static EventSystem getInstance(){
        if(instance == null) instance = new EventSystem();
        return instance;
    }

    private LinkedList<GameEventListener> listeners = new LinkedList<>();

    public void addListener(GameEventListener listener){
        listeners.add(listener);
    }

    public void removeListener(GameEventListener listener){
        listeners.remove(listener);
    }

    public void onFinishLevel(int level){
        for (GameEventListener listener : listeners){
            listener.onFinishLevel(level);
        }
    }

    public void onPlayerDeath(){
        for (GameEventListener listener : listeners){
            listener.onPlayerDeath();
        }
    }

    public void onKillEnemy(){
        for (GameEventListener listener : listeners){
            listener.onKillEnemy();
        }
    }
}
