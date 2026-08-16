package com.vlad2305m;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.vlad2305m.config.ChatqalcConfig;
import com.vlad2305m.config.KeyLabel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import static com.vlad2305m.ChatqalcClient.executeQuietly;
import static com.vlad2305m.MathEngine.initMathEngine;
import static com.vlad2305m.MathEngine.isBinaryPresent;
import static com.vlad2305m.MathEngine.isRunning;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> new ChatqalcConfigScreen(parentScreen);
    }

    public static class ChatqalcConfigScreen extends Screen {
        private final Screen parent;
        private HeaderAndFooterLayout layout;
        private CaptureTarget captureTarget;

        public ChatqalcConfigScreen(Screen parent) {
            super(Component.literal("ChatQalc Settings"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            super.init();
            ChatqalcConfig.get();
            captureTarget = null;
            layout = new HeaderAndFooterLayout(this);
            layout.addToHeader(new StringWidget(this.title, this.font));

            LinearLayout footer = LinearLayout.horizontal().spacing(8);
            footer.addChild(Button.builder(Component.literal("Done"), b -> onClose())
                    .width(150).build());
            footer.addChild(Button.builder(Component.literal("Reset to defaults"), b -> {
                ChatqalcConfig.resetToDefaults();
                rebuildBodyAndWidgets();
            }).width(150).build());
            layout.addToFooter(footer);

            rebuildBodyAndWidgets();
        }

        private void rebuildBodyAndWidgets() {
            clearWidgets();

            LinearLayout content = LinearLayout.vertical().spacing(4);
            content.defaultCellSetting().alignHorizontallyLeft().paddingLeft(20);

            buildQalcSection(content);
            buildBehaviourSection(content);
            buildShortcutsSection(content);

            layout.addToContents(content);
            layout.arrangeElements();

            layout.visitWidgets(w -> {
                if (w instanceof AbstractWidget aw) {
                    addRenderableWidget(aw);
                }
            });
        }

        private void buildQalcSection(LinearLayout content) {
            ChatqalcConfig cfg = ChatqalcConfig.get();
            content.addChild(new StringWidget(Component.literal("§nQalculate! process"),
                    this.font).setMaxWidth(380));
            content.addChild(new StringWidget(Component.literal(
                    "Binary path: §7" + effectiveQalcPath(cfg)
            ), this.font).setMaxWidth(380));
            content.addChild(new StringWidget(Component.literal(
                    "Binary present: " + (isBinaryPresent() ? "§ayes" : "§cno")
            ), this.font).setMaxWidth(380));
            content.addChild(new StringWidget(Component.literal(
                    "Process running: " + (isRunning() ? "§ayes" : "§cno")
            ), this.font).setMaxWidth(380));

            EditBox pathBox = new EditBox(this.font, 260, 20, Component.literal("Path"));
            pathBox.setHint(Component.literal("Override path (blank = auto)"));
            pathBox.setValue(cfg.qalcBinaryPath);
            pathBox.setResponder(s -> {
                ChatqalcConfig.get().qalcBinaryPath = s;
                ChatqalcConfig.save();
            });
            LinearLayout pathRow = LinearLayout.horizontal().spacing(8);
            pathRow.addChild(new StringWidget(Component.literal("Path:"), this.font));
            pathRow.addChild(pathBox);
            content.addChild(pathRow);

            LinearLayout actionRow = LinearLayout.horizontal().spacing(8);
            actionRow.addChild(Button.builder(Component.literal("Open Qalculate! GUI"), b -> {
                executeQuietly("exit");
                MathEngine.openConfig();
            }).width(150).build());
            actionRow.addChild(Button.builder(Component.literal("Restart qalc"), b -> {
                initMathEngine();
                rebuildBodyAndWidgets();
            }).width(150).build());
            content.addChild(actionRow);
        }

        private void buildBehaviourSection(LinearLayout content) {
            ChatqalcConfig cfg = ChatqalcConfig.get();
            content.addChild(new StringWidget(Component.literal("§nBehaviour"),
                    this.font).setMaxWidth(380));
            content.addChild(CycleButton.onOffBuilder(cfg.autoStartQalc)
                    .create(0, 0, 300, 20,
                            Component.literal("Auto-start qalc on launch"),
                            (btn, val) -> {
                                ChatqalcConfig.get().autoStartQalc = val;
                                ChatqalcConfig.save();
                            }));
            content.addChild(CycleButton.onOffBuilder(cfg.showIndicatorLabel)
                    .create(0, 0, 300, 20,
                            Component.literal("Show '[Qalculate!]' label in chat"),
                            (btn, val) -> {
                                ChatqalcConfig.get().showIndicatorLabel = val;
                                ChatqalcConfig.save();
                            }));
        }

        private void buildShortcutsSection(LinearLayout content) {
            content.addChild(new StringWidget(Component.literal("§nKeyboard shortcuts"),
                    this.font).setMaxWidth(380));
            content.addChild(new StringWidget(Component.literal(
                    "Click a row's button, then press the new key combination. Esc cancels.\n" +
                    "§7Hold modifier(s) first, then press the main key."
            ), this.font).setMaxWidth(380));

            content.addChild(shortcutRow("Execute to chat (local)",
                    "executeToChat"));
            content.addChild(shortcutRow("Execute & broadcast (server)",
                    "executeAndBroadcast"));
            content.addChild(shortcutRow("Execute \u2192 input field",
                    "executeToInput"));
            content.addChild(shortcutRow("Substitute word at cursor",
                    "substituteWord"));
            content.addChild(shortcutRow("Get completions",
                    "getCompletions"));
        }

        private LinearLayout shortcutRow(String label, String id) {
            ChatqalcConfig.Shortcut sc = ChatqalcConfig.get().getShortcut(id);
            LinearLayout row = LinearLayout.horizontal().spacing(8);
            row.addChild(new StringWidget(170, 20, Component.literal(label), this.font));
            row.addChild(Button.builder(
                    Component.literal(KeyLabel.format(sc.key, sc.modifiers)),
                    b -> {
                        captureTarget = new CaptureTarget(id, b);
                        b.setMessage(Component.literal("§ePress a key..."));
                    })
                    .width(140).build());
            row.addChild(Button.builder(Component.literal("Reset"), b -> {
                ChatqalcConfig.Shortcut def = ChatqalcConfig.defaultShortcut(id);
                ChatqalcConfig.Shortcut live = ChatqalcConfig.get().getShortcut(id);
                live.key = def.key;
                live.modifiers = def.modifiers;
                ChatqalcConfig.save();
                rebuildBodyAndWidgets();
            }).width(60).build());
            return row;
        }

        private String effectiveQalcPath(ChatqalcConfig cfg) {
            return (cfg.qalcBinaryPath != null && !cfg.qalcBinaryPath.isBlank())
                    ? cfg.qalcBinaryPath
                    : PlatformSpecificStuff.qalcFile();
        }

        @Override
        public void onClose() {
            ChatqalcConfig.save();
            if (this.minecraft != null) {
                this.minecraft.gui.setScreen(parent);
            }
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            this.extractTransparentBackground(context);
            super.extractRenderState(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
            if (captureTarget != null) {
                int keyCode = event.key();
                int mods = event.modifiers();

                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    ChatqalcConfig.Shortcut sc = ChatqalcConfig.get().getShortcut(captureTarget.id);
                    captureTarget.button.setMessage(Component.literal(
                            KeyLabel.format(sc.key, sc.modifiers)));
                    captureTarget = null;
                    return true;
                }

                if (KeyLabel.isModifierKey(keyCode)) {
                    int previewMods = mods | KeyLabel.modifierBit(keyCode);
                    captureTarget.button.setMessage(Component.literal(
                            "§e" + KeyLabel.format(0, previewMods) + "..."));
                    return true;
                }

                if (KeyLabel.isCaptureable(keyCode)) {
                    ChatqalcConfig.Shortcut sc = ChatqalcConfig.get().getShortcut(captureTarget.id);
                    sc.key = keyCode;
                    sc.modifiers = mods;
                    ChatqalcConfig.save();
                    captureTarget.button.setMessage(Component.literal(
                            KeyLabel.format(sc.key, sc.modifiers)));
                    captureTarget = null;
                    return true;
                }
            }
            return super.keyPressed(event);
        }

        @Override
        public boolean keyReleased(net.minecraft.client.input.KeyEvent event) {
            if (captureTarget != null && KeyLabel.isModifierKey(event.key())) {
                int mods = event.modifiers();
                if (mods == 0) {
                    captureTarget.button.setMessage(Component.literal("§ePress a key..."));
                } else {
                    captureTarget.button.setMessage(Component.literal(
                            "§e" + KeyLabel.format(0, mods) + "..."));
                }
                return true;
            }
            return super.keyReleased(event);
        }

        private static final class CaptureTarget {
            final String id;
            final Button button;
            CaptureTarget(String id, Button button) {
                this.id = id; this.button = button;
            }
        }
    }
}
