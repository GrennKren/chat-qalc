package com.vlad2305m.mixin.client;

import com.vlad2305m.ChatqalcClient;
import com.vlad2305m.config.ChatqalcConfig;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
abstract class ChatScreenMixin {

    @Shadow protected EditBox input;

    @Shadow public abstract void handleChatInput(String chatText, boolean addToHistory);

    @Inject(at = @At("HEAD"), method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", cancellable = true)
    private void chatqalc$keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        int keyCode = event.key();
        int modifiers = event.modifiers();

        ChatqalcConfig.Shortcuts s = ChatqalcConfig.get().shortcuts;

        boolean handled = false;

        if (s.executeToChat.matches(keyCode, modifiers)) {
            handled = ChatqalcClient.executeToChat(input);
        } else if (s.executeAndBroadcast.matches(keyCode, modifiers)) {
            handled = ChatqalcClient.executeAndBroadcast(input, (msg) -> handleChatInput(msg, false));
        } else if (s.executeToInput.matches(keyCode, modifiers)) {
            handled = ChatqalcClient.executeToInput(input);
        } else if (s.substituteWord.matches(keyCode, modifiers)) {
            handled = ChatqalcClient.substituteWord(input);
        } else if (s.getCompletions.matches(keyCode, modifiers)) {
            handled = ChatqalcClient.getCompletions(input);
        } else if (s.numpadEnterExecute.matches(keyCode, modifiers)) {
            handled = ChatqalcClient.executeToChat(input);
        }

        if (handled) {
            cir.setReturnValue(true);
        }
    }
}
