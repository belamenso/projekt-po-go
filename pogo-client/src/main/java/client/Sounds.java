package client;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

class Sounds {
    private static Random RNG = new Random();

    private static class Sound {
        List<MediaPlayer> variants;

        Sound(JSONArray files) {
            variants = new ArrayList<>();
            for(Object o : files) {
                //System.out.println("po-go/resources/sounds/" + o.toString());
                Media sound = new Media(new File("po-go/resources/sounds/" + o.toString()).toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(sound);
                variants.add(mediaPlayer);
            }
        }

        void play() {
            MediaPlayer sound = variants.get(RNG.nextInt(variants.size()));
            sound.stop();
            sound.play();
        }
    }

    private static Map<String, Sound> sounds;

    static void playSound(String name) {
        Settings.assertConfigurationExists();
        Settings settings = Settings.readSettings();
        if(!settings.soundOn) return;
        Sound s = sounds.get(name);
        System.out.println("playing sound: " + name);
        if(s != null) {
            s.play();
        }
    }

    static void loadSounds() {
        sounds = new HashMap<>();

        Object obj = null;
        try {
            File f = new File(Sounds.class.getResource("/sounds/sounds.json").toString());
            System.out.println(f.exists());
            obj = new JSONParser().parse(f.toURI().toString());
        } catch (ParseException e) {
            System.out.println("Problem parsing sounds.json");
            e.printStackTrace();
        }

        JSONObject jo = (JSONObject) obj;

        Map entries = (Map) jo.get("sounds");

        Iterator<Map.Entry> itr1 = entries.entrySet().iterator();

        while (itr1.hasNext()) {
            Map.Entry pair = itr1.next();
            sounds.put(pair.getKey().toString(), new Sound((JSONArray) pair.getValue()));
            System.out.println(pair.getKey() + " : " + pair.getValue());
        }
    }
}
