package com.vlad2305m.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vlad2305m.ChatqalcClient;
import com.vlad2305m.PlatformSpecificStuff;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChatqalcConfig {

    public static class Shortcut {
        public int key;
        public int modifiers;

        public Shortcut() { this(0, 0); }
        public Shortcut(int key, int modifiers) {
            this.key = key;
            this.modifiers = modifiers;
        }

        public boolean matches(int k, int mods) {
            return key == k && modifiers == mods;
        }

        public boolean isSet() { return key != 0; }
    }

    public static class Shortcuts {
        public Shortcut executeToChat      = new Shortcut(257, 2); // Ctrl+Enter
        public Shortcut executeAndBroadcast = new Shortcut(257, 3); // Ctrl+Shift+Enter
        public Shortcut executeToInput     = new Shortcut(257, 1); // Shift+Enter
        public Shortcut substituteWord     = new Shortcut(258, 2); // Ctrl+Tab
        public Shortcut getCompletions     = new Shortcut(258, 1); // Shift+Tab
        public Shortcut numpadEnterExecute = new Shortcut(335, 0); // Numpad Enter
    }

    public boolean autoStartQalc = true;
    public boolean showIndicatorLabel = true;
    public boolean showCompletionsInChat = true;
    public String qalcBinaryPath = "";
    public Shortcuts shortcuts = new Shortcuts();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ChatqalcConfig INSTANCE;

    public static synchronized ChatqalcConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static Path configPath() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("chatqalc");
        try { Files.createDirectories(configDir); } catch (IOException ignored) {}
        return configDir.resolve("settings.json");
    }

    public static synchronized void load() {
        Path path = configPath();
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                INSTANCE = GSON.fromJson(r, ChatqalcConfig.class);
                if (INSTANCE != null) {
                    migrateModifierSchemeIfStale();
                    return;
                }
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                ChatqalcClient.LOGGER.warn("Failed to load chatqalc config, using defaults: {}", e.toString());
            }
        }
        INSTANCE = new ChatqalcConfig();
        if (INSTANCE.qalcBinaryPath == null || INSTANCE.qalcBinaryPath.isBlank()) {
            INSTANCE.qalcBinaryPath = PlatformSpecificStuff.qalcFile();
        }
        save();
    }

    private static void migrateModifierSchemeIfStale() {
        Shortcuts s = INSTANCE.shortcuts;
        if (s == null) { INSTANCE.shortcuts = new Shortcuts(); return; }

        Shortcuts newDefaults = new Shortcuts();
        boolean customized =
                s.executeToChat.key != newDefaults.executeToChat.key ||
                s.executeToChat.modifiers != newDefaults.executeToChat.modifiers ||
                s.executeAndBroadcast.key != newDefaults.executeAndBroadcast.key ||
                s.executeAndBroadcast.modifiers != newDefaults.executeAndBroadcast.modifiers ||
                s.executeToInput.key != newDefaults.executeToInput.key ||
                s.executeToInput.modifiers != newDefaults.executeToInput.modifiers ||
                s.substituteWord.key != newDefaults.substituteWord.key ||
                s.substituteWord.modifiers != newDefaults.substituteWord.modifiers ||
                s.getCompletions.key != newDefaults.getCompletions.key ||
                s.getCompletions.modifiers != newDefaults.getCompletions.modifiers ||
                s.numpadEnterExecute.key != newDefaults.numpadEnterExecute.key ||
                s.numpadEnterExecute.modifiers != newDefaults.numpadEnterExecute.modifiers;

        boolean anyBit1or4 =
                (s.executeToChat.modifiers & 5) != 0 ||
                (s.executeAndBroadcast.modifiers & 5) != 0 ||
                (s.executeToInput.modifiers & 5) != 0 ||
                (s.substituteWord.modifiers & 5) != 0 ||
                (s.getCompletions.modifiers & 5) != 0 ||
                (s.numpadEnterExecute.modifiers & 5) != 0;

        if (customized && anyBit1or4) {
            ChatqalcClient.LOGGER.warn(
                    "Detected stale chatqalc keybinds from old modifier scheme - resetting to defaults.");
            INSTANCE.shortcuts = new Shortcuts();
            save();
        }
    }

    public static synchronized void save() {
        if (INSTANCE == null) return;
        try (Writer w = Files.newBufferedWriter(configPath())) {
            GSON.toJson(INSTANCE, w);
        } catch (IOException e) {
            ChatqalcClient.LOGGER.warn("Failed to save chatqalc config: {}", e.toString());
        }
    }

    public static synchronized void resetToDefaults() {
        INSTANCE = new ChatqalcConfig();
        if (INSTANCE.qalcBinaryPath == null || INSTANCE.qalcBinaryPath.isBlank()) {
            INSTANCE.qalcBinaryPath = PlatformSpecificStuff.qalcFile();
        }
        save();
    }

    public ChatqalcConfig.Shortcut getShortcut(String id) {
        return switch (id) {
            case "executeToChat"       -> shortcuts.executeToChat;
            case "executeAndBroadcast" -> shortcuts.executeAndBroadcast;
            case "executeToInput"      -> shortcuts.executeToInput;
            case "substituteWord"      -> shortcuts.substituteWord;
            case "getCompletions"      -> shortcuts.getCompletions;
            case "numpadEnterExecute"  -> shortcuts.numpadEnterExecute;
            default -> throw new IllegalArgumentException("Unknown shortcut id: " + id);
        };
    }

    public static ChatqalcConfig.Shortcut defaultShortcut(String id) {
        Shortcuts d = new Shortcuts();
        return switch (id) {
            case "executeToChat"       -> d.executeToChat;
            case "executeAndBroadcast" -> d.executeAndBroadcast;
            case "executeToInput"      -> d.executeToInput;
            case "substituteWord"      -> d.substituteWord;
            case "getCompletions"      -> d.getCompletions;
            case "numpadEnterExecute"  -> d.numpadEnterExecute;
            default -> throw new IllegalArgumentException("Unknown shortcut id: " + id);
        };
    }
}
