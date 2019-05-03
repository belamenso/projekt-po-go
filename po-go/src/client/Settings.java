package client;

import java.io.*;

/**
 * Ustawienia klienta, które nie znikają po zamknięciu aplikacji.
 * Podczas inicjalizacji GUI (?) klienta należy wywołać assertConfigurationExists, który zapweni że przynajmniej
 *   domyślna konfiguracja istnieje.
 * Setery w tej klasie mają wyglądać tak jak setPort()
 * TODO W przyszłości będą tutaj jakieś ustawienia w stylu głośność dźwięku, kolor planszy itd
 */
public class Settings implements Serializable {
    /**
     * Wersja protokołu ustawień. Jeśli znaleziono starszą wersję, zostanie ona usunięta i zastąpiona domyślną.
     */
    int version = 1;

    // USTAWIENIA
    // TODO dodać tutaj rzeczy, które mają sens

    String port;
    String host;


    private Settings(String host, String port) {
        this.port = port;
        this.host = host;
    }

    /**
     * zaklada, że jest już po assertConfigurationExists()
     * TODO tak mają wyglądać settery w tej klasie
     */
    void setPort(String port) {
        Settings s = readSettings();
        s.port = port;
        writeConfiguration(s);
    }

    // /USTAWIENIA

    static Settings getDefaultSettings() {
        return new Settings("localhost", "33107");
    }

    private static String getSettingsFilePath() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return System.getProperty("user.home") + File.separator + "pogo.settings";
        } else {
            return System.getProperty("user.home") + File.separator + ".pogo.settings";
        }
    }

    private static void writeConfiguration(Settings settings) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getSettingsFilePath()));
            out.writeObject(settings);
            out.close();
        } catch (IOException e) {
            // TODO co teraz?
            e.printStackTrace();
            throw new AssertionError(); // ?
        }
    }

    static void assertConfigurationExists() {
        File configurationFile = new File(getSettingsFilePath());

        if (configurationFile.exists()) {
            Settings existingConfiguration = readSettings();
            System.out.println(existingConfiguration.version);
            if (existingConfiguration.version == getDefaultSettings().version) return;
        }

        writeConfiguration(getDefaultSettings());
    }

    static Settings readSettings() {
        String configurationFileName = getSettingsFilePath();
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(configurationFileName));
            try {
                return (Settings) in.readObject();
            } catch (InvalidClassException ex) {
                writeConfiguration(getDefaultSettings());
                return getDefaultSettings();
            }
        } catch (IOException e) {
            // TODO jeszcze większy problem. Co teraz?
            e.printStackTrace();
            return getDefaultSettings();
        } catch (ClassNotFoundException e) { // impossible
            e.printStackTrace();
            return getDefaultSettings();
        }
    }
}
