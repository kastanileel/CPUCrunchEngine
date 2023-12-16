package src.engine.core.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.*;

public class MusicPlayer {

    // enum for sound effects
    public enum SoundEffect {
        Shoot("src/sound/shoot.wav");


        private final String path;

        SoundEffect(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }
    private static volatile MusicPlayer instance;
    private ExecutorService threadPool;
    private HashMap<String, Clip> soundClips;

    private MusicPlayer(){
        threadPool = Executors.newCachedThreadPool();
        soundClips = new HashMap<>();
        // Initialize resources
    }

    public static MusicPlayer getInstance(){
        if(instance == null) {
            synchronized(MusicPlayer.class) {
                if(instance == null)
                    instance = new MusicPlayer();
            }
        }
        return instance;
    }

    public void playSound(String sound) {
        threadPool.execute(() -> {
            Clip clip = loadClip(sound); // Implement loadClip to load and return a Clip
            soundClips.put(sound, clip);
            clip.start();
        });
    }

    public void playSound(SoundEffect sound) {
        threadPool.execute(() -> {
            Clip clip = loadClip(sound.getPath()); // Implement loadClip to load and return a Clip
            soundClips.put(sound.getPath(), clip);
            clip.start();
        });
    }

    private Clip loadClip(String filePath) {
        try {
            // Open an audio input stream from the file path
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            // Get a clip resource
            Clip clip = AudioSystem.getClip();

            // Open the clip and load samples from the audio input stream
            clip.open(audioStream);

            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace(); // Handle exceptions appropriately
            return null;
        }
    }

    public void loopMusic(String sound) {
        threadPool.execute(() -> {
            Clip clip = loadClip(sound); // Implement loadClip to load and return a Clip
            soundClips.put(sound, clip);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        });
    }

    // Additional methods to stop sounds, manage resources, etc.
}
