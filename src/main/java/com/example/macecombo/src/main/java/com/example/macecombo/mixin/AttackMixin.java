package com.example.macecombo.mixin;

import com.example.macecombo.MaceComboMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class AttackMixin {
    @Inject(method = "doAttack", at = @At("HEAD"))
    private void onAttack(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (MaceComboMod.enabled && client.player != null && client.targetedEntity != null) {
            if (client.player.getMainHandStack().isOf(Items.MACE)) {
                MaceComboMod.targetEntity = client.targetedEntity;
                MaceComboMod.swapChestEquipment(client, true);
            }
        }
    }
}
