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
        //Pistol SFX
        MORE_BULLETS("src/sound/guns/moreBullets.wav"),
        BIGGER_GUN("src/sound/guns/biggerWeapons.wav"),
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

        //Round over SFX
        LEVEL_FINISHED("src/sound/misc/newRound.wav"),

        //player Death SFX
        GAME_OVER("src/sound/misc/gameOver.wav"),

        //Enemy SFX
        GE_DEATH("src/sound/enemies/groundEnemy/groundEnemy_death.wav"),

        GUNNER_DEATH("src/sound/enemies/gunTurret/gunTurret_death.wav"),

        SIGHSEEKER_ATTACK("src/sound/misc/shoot.wav"),
        SIGHTSEEKER_DEATH("src/sound/enemies/sightSeeker/sightSeeker_death.wav"),

        MALTESEEKER_RANDOMTALK1("src/sound/enemies/malteSeeker/Alter_marcel.wav"),
        MALTESEEKER_RANDOMTALK2("src/sound/enemies/malteSeeker/Alter_marcel_tief_schnell.wav"),
        MALTESEEKER_SPAWN_AND_ATTACK("src/sound/enemies/malteSeeker/hallo.wav"),
        MALTESEEKER_RANDOMTALK3("src/sound/enemies/malteSeeker/Marcel_ist_immer_schuld.wav");


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
    public float volume = 0.8f;

    private MusicPlayer() {
        threadPool = Executors.newCachedThreadPool();
        soundClips = new HashMap<>();
        // Initialize resources
    }

    //adjust volume
    public void changeVolume(float volume) {
        this.volume = volume;
    }

    public void setVolume(Clip clip){
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        double gain = volume; // number between 0 and 1 (loudest)
        float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
        gainControl.setValue(dB);
    }

    //singleton

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
            setVolume(clip);
            clip.start();
        });
    }


    public void playSound(SoundEffect sound) {
        threadPool.execute(() -> {
            Clip clip = loadClip(sound.getPath()); // Implement loadClip to load and return a Clip
            soundClips.put(sound.getPath(), clip);
            setVolume(clip);
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
            setVolume(clip);
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
            setVolume(clip);
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
}
