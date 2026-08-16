package com.vlad2305m;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.function.Consumer;

import static com.vlad2305m.MathEngine.reformatAnsiMinecraft;
import static com.vlad2305m.MathEngine.stripAnsi;
import static com.vlad2305m.config.ChatqalcConfig.get;

public class ChatqalcClient implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        get();
        if (get().autoStartQalc) {
            MathEngine.initMathEngine();
        }
    }

    public static boolean executeToChat(EditBox field) {
        String originalText = field.getValue();
        if (originalText.isBlank()) return false;

        ChatComponent chat = Minecraft.getInstance().gui.hud.getChat();
        MathEngine.addMessage = (s) -> {
            if (s != null) {
                String formatted = reformatAnsiMinecraft(s);
                if (get().showIndicatorLabel) {
                    formatted = "§6[Qalculate!]§r " + formatted;
                }
                final String msg = formatted;
                Minecraft.getInstance().execute(() ->
                        chat.addClientSystemMessage(Component.literal(msg)));
            }
        };
        MathEngine.eval(originalText);
        chat.addRecentChat(originalText);
        field.setValue("");
        return true;
    }

    public static boolean executeAndBroadcast(EditBox field, Consumer<String> broadcast) {
        String originalText = field.getValue();
        if (originalText.isBlank()) return false;
        MathEngine.addMessage = (s) -> {
            if (!s.isBlank() && !(s.charAt(0) == '>')) {
                final String stripped = stripAnsi(s);
                Minecraft.getInstance().execute(() -> broadcast.accept(stripped));
            }
        };
        MathEngine.eval(originalText);
        Minecraft.getInstance().gui.hud.getChat().addRecentChat(originalText);
        field.setValue("");
        return true;
    }

    public static boolean executeQuietly(String expr) {
        if (expr.isBlank()) return false;
        MathEngine.addMessage = (s) -> {};
        MathEngine.eval(expr);
        return true;
    }

    public static boolean executeToInput(EditBox field) {
        String originalText = field.getValue();
        if (originalText.isBlank()) return false;
        MathEngine.addMessage = (s) -> {
            if (!s.isBlank()) {
                final String stripped = stripAnsi(s.strip());
                Minecraft.getInstance().execute(() -> field.setValue(stripped));
            }
        };
        MathEngine.eval(originalText);
        Minecraft.getInstance().gui.hud.getChat().addRecentChat(originalText);
        return true;
    }

    public static boolean substituteWord(EditBox field) {
        String originalText = field.getValue();
        if (originalText.isBlank()) return false;
        int end = field.getCursorPosition();

        while (end > 0 && originalText.charAt(end - 1) == ' ') end--;

        int i;
        for (i = originalText.lastIndexOf(" ", end - 1); i > 0 && originalText.charAt(i - 1) == '\\'; i = originalText.lastIndexOf(" ", i - 1));
        int start = i + 1;
        String expr = originalText.substring(start, end).replace("\\ ", "");
        if (expr.isBlank()) return false;
        final int finalEnd = end;
        MathEngine.addMessage = (s) -> {
            if (s != null && !s.isBlank()) {
                final String newValue = stripAnsi(
                        originalText.substring(0, start) + s.strip() + originalText.substring(finalEnd));
                final int newCursor = start + s.length();
                Minecraft.getInstance().execute(() -> {
                    field.setValue(newValue);
                    field.moveCursorTo(newCursor, false);
                });
            }
        };
        MathEngine.evalSingle(expr);
        Minecraft.getInstance().gui.hud.getChat().addRecentChat(originalText);
        return true;
    }

    public static boolean getCompletions(EditBox field) {
        String originalText = field.getValue();
        if (originalText.isBlank()) return false;
        net.minecraft.client.gui.components.ChatComponent chat =
                Minecraft.getInstance().gui.hud.getChat();
        java.util.function.Consumer<String> renderThreadSink = (s) -> {
            Minecraft.getInstance().execute(() -> {
                chat.addClientSystemMessage(Component.literal(reformatAnsiMinecraft(s)));
            });
        };
        MathEngine.addMessage = (s) -> {
            if (s != null && !s.isBlank()
                    && !s.equals("  \033[0;36m0\033[0m = \033[0;36m0\033[0m")) {
                s = s.replace("\010", "");
                if (s.length() > 1 && s.charAt(0) == '>') {
                    final String replacement = stripAnsi(s.substring(1).strip());
                    Minecraft.getInstance().execute(() -> field.setValue(replacement));
                } else {
                    renderThreadSink.accept(s);
                }
            }
        };
        MathEngine.tabComp(originalText);
        return true;
    }
}
