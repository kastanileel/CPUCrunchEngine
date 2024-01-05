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
        PICKUP_PISTOL("src/sound/guns/pistol/pickupPistol.wav"),
        SHOOT_PISTOL("src/sound/guns/pistol/pistolShot.wav"),
        RELOAD_PISTOL("src/sound/guns/pistol/pistolReload.wav"),
        //Shotgun SFX
        PICKUP_SHOTGUN("src/sound/guns/shotgun/pickupShotgun.wav"),
        SHOOT_SHOTGUN("src/sound/guns/shotgun/shotgunShot.wav"),
        RELOAD_SHOTGUN("src/sound/guns/shotgun/reloadShotgun.wav"),
        //AK SFX
        SHOOT_AK("src/sound/guns/AKM/AKM_shoot.wav"),
        PICKUP_AK("src/sound/guns/AKM/AKM_rack.wav"),
        RELOAD_AK("src/sound/guns/AKM/AKM_reload.wav"),
        //Sniper SFX
        SHOOT_SNIPER("src/sound/guns/sniper/sniperShot.wav"),
        PICKUP_SNIPER("src/sound/guns/sniper/sniperPickup.wav"),
        RELOAD_SNIPER("src/sound/guns/sniper/sniperReload.wav"),

        SCOPE("src/sound/guns/sniper/scope.wav"),
        Knife("src/sound/misc/knife.wav"),

        //Enemy SFX
        GE_DEATH("src/sound/enemies/groundEnemy/groundEnemy_death.wav"),
        //add attack and idle

        GUNNER_DEATH("src/sound/enemies/gunTurret/gunTurret_death.wav"),
        //add attack and idle

        SIGHTSEEKER_IDLE("src/sound/enemies/sightSeeker/hovering.wav"),
        SIGHSEEKER_ATTACK("src/sound/enemies/sightSeeker/laserShot.wav"),
        SIGHTSEEKER_DEATH("src/sound/enemies/sightSeeker/sightSeeker_death.wav");


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

    public void playRandomPlayerSound() {
        String[] playerSounds = {"src/sound/player/player_damage_1.wav",
                "src/sound/player/player_damage_2.wav",
                "src/sound/player/player_damage_3.wav",
                "src/sound/player/player_damage_4.wav",
                "src/sound/player/player_damage_5.wav",
                "src/sound/player/player_damage_6.wav",
                "src/sound/player/player_damage_7.wav",
                "src/sound/player/player_damage_8.wav"
        };
        threadPool.execute(() -> {
            int random = (int) (Math.random() * playerSounds.length);
            Clip clip = loadClip(playerSounds[random]); // Implement loadClip to load and return a Clip
            soundClips.put(playerSounds[random], clip);
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
