package com.vlad2305m.mixin.client;

import com.vlad2305m.ChatqalcClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
abstract class ChatScreenMixin {
    @Shadow protected TextFieldWidget chatField;

    @Unique private int messageHistorySize;
    @Unique public boolean sendMessage(String chatText, boolean addToHistory) {return false;}

    @Inject(at = @At("HEAD"), method = "keyPressed(III)Z", cancellable = true)
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        Runnable exit = ()->{
            cir.setReturnValue(true);
            messageHistorySize++;
        };
        if (keyCode == 257) {
            if (modifiers == 2 && ChatqalcClient.executeToChat(chatField)) exit.run();
            else if (modifiers == 3 && ChatqalcClient.executeAndBroadcast(chatField, (s)->sendMessage(s, false))) exit.run();
            else if (modifiers == 1 && ChatqalcClient.executeToInput(chatField)) exit.run();
        } else if (keyCode == 258) {
            if (modifiers == 2 && ChatqalcClient.substituteWord(chatField)) exit.run();
            else if (modifiers == 1 && ChatqalcClient.getCompletions(chatField)) exit.run();
        } else if (keyCode == 335 && ChatqalcClient.executeToChat(chatField)) exit.run();
    }
}
