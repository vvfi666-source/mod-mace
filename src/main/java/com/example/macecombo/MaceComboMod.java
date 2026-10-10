package com.example.macecombo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class MaceComboMod implements ClientModInitializer {
    public static boolean enabled = false;
    public static Entity targetEntity = null;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Mace Combo Toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "Mace Combo"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                if (!enabled) targetEntity = null;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§eMace Combo: " + (enabled ? "§aВКЛ" : "§cВЫКЛ")), true);
                }
            }

            if (enabled && targetEntity != null) {
                handleLowHeightCombo(client);
            }
        });
    }

    private static void handleLowHeightCombo(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        if (!targetEntity.isAlive()) {
            targetEntity = null;
            return;
        }

        if (client.player.isOnGround()) {
            swapChestEquipment(client, true);
            client.interactionManager.interactItem(client.player, Hand.OFF_HAND);
        }

        if (client.player.fallDistance > 0.4f) {
            swapChestEquipment(client, false);
            client.interactionManager.attackEntity(client.player, targetEntity);
        }
    }

    public static void swapChestEquipment(MinecraftClient client, boolean wantElytra) {
        if (client.player == null) return;
        PlayerInventory inv = client.player.getInventory();
        var currentChest = inv.getArmorStack(2);

        if (wantElytra && !currentChest.isOf(Items.ELYTRA)) {
            int elytraSlot = findItem(inv, Items.ELYTRA);
            if (elytraSlot != -1) moveItemToChestSlot(client, elytraSlot);
        } else if (!wantElytra && currentChest.isOf(Items.ELYTRA)) {
            int armorSlot = findChestplate(inv);
            if (armorSlot != -1) moveItemToChestSlot(client, armorSlot);
        }
    }

    private static int findItem(PlayerInventory inv, var item) {
        for (int i = 0; i < inv.main.size(); i++) {
            if (inv.main.get(i).isOf(item)) return i;
        }
        return -1;
    }

    private static int findChestplate(PlayerInventory inv) {
        for (int i = 0; i < inv.main.size(); i++) {
            var stack = inv.main.get(i);
            if (stack.isOf(Items.NETHERITE_CHESTPLATE) || stack.isOf(Items.DIAMOND_CHESTPLATE)) return i;
        }
        return -1;
    }

    private static void moveItemToChestSlot(MinecraftClient client, int inventorySlot) {
        if (client.interactionManager == null || client.player == null) return;
        int slotId = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
        client.interactionManager.clickSlot(0, slotId, 0, SlotActionType.PICKUP, client.player);
        client.interactionManager.clickSlot(0, 6, 0, SlotActionType.PICKUP, client.player);
        client.interactionManager.clickSlot(0, slotId, 0, SlotActionType.PICKUP, client.player);
    }
}
