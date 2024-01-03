package src.engine.core.tools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.*;

public class MusicPlayer {

    // enum for sound effects
    public enum SoundEffect {
        //Shoot("src/sound/shoot.wav"),
        //BIG_SHOOT("src/sound/destroy.wav"),
        //Pistol SFX
        PICKUP_PISTOL("src/sound/pickupPistol.wav"),
        SHOOT_PISTOL("src/sound/pistolShot.wav"),
        RELOAD_PISTOL("src/sound/pistolReload.wav"),
        //Shotgun SFX
        PICKUP_SHOTGUN("src/sound/pickupShotgun.wav"),
        SHOOT_SHOTGUN("src/sound/shotgunShot.wav"),
        RELOAD_SHOTGUN("src/sound/reloadShotgun.wav"),
        //AK SFX
        SHOOT_AK("src/sound/AKM_shoot.wav"),
        PICKUP_AK("src/sound/AKM_rack.wav"),
        RELOAD_AK("src/sound/AKM_reload.wav"),
        //Sniper SFX
        SHOOT_SNIPER("src/sound/sniperShot.wav"),
        PICKUP_SNIPER("src/sound/sniperPickup.wav"),
        RELOAD_SNIPER("src/sound/sniperReload.wav"),

        SCOPE("src/sound/scope.wav"),
        Knife("src/sound/knife.wav");


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

    private MusicPlayer() {
        threadPool = Executors.newCachedThreadPool();
        soundClips = new HashMap<>();
        // Initialize resources
    }

    public static MusicPlayer getInstance() {
        if (instance == null) {
            synchronized (MusicPlayer.class) {
                if (instance == null)
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

    public void pauseResume(String sound) {
        threadPool.execute(() -> {
            Clip clip = soundClips.get(sound);
            if (clip.isActive()) {
                clip.stop();
            } else {
                clip.start();
            }
        });
    }

    public void stopGameMusic(){
        for (Clip clip : soundClips.values()) {
            if (clip.isRunning()) {
                clip.stop(); // Stop the clip if it is running
            }
            clip.close(); // Close the clip to release resources
        }
        soundClips.clear();
    }

    // Additional methods to stop sounds, manage resources, etc.
}
