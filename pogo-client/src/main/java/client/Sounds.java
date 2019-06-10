package client;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;
import java.net.URISyntaxException;
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
                Media sound = null;
                try {
                    System.out.println(Sounds.class.getResource("/sounds/" + o.toString()).toURI().toString());
                    sound = new Media(Sounds.class.getResource("/sounds/" + o.toString()).toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(sound);
                    variants.add(mediaPlayer);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

        void play() {
            if(variants.size() == 0) return;
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
            obj = new JSONParser().parse(new InputStreamReader(Sounds.class.getResourceAsStream("/sounds/sounds.json")));
        } catch (ParseException e) {
            System.out.println("Problem parsing sounds.json");
            e.printStackTrace();
        } catch (IOException e) {
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
