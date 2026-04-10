package com.mimicenzymes.litematicafiller.core;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlotMapper {

    private final Map<Integer, Integer> playerToUiMap = new HashMap<>();
    private final List<Integer> containerUiSlots = new ArrayList<>(); // 按 UI 顺序存放容器槽的 UI 索引

    public SlotMapper(ScreenHandler handler, PlayerInventory playerInv) {
        for (int uiSlotId = 0; uiSlotId < handler.slots.size(); uiSlotId++) {
            Slot slot = handler.slots.get(uiSlotId);
            if (slot.inventory == null) continue;

            if (slot.inventory == playerInv) {
                // 玩家槽：用槽位索引映射到 UI 槽
                playerToUiMap.putIfAbsent(slot.getIndex(), uiSlotId);
            } else {
                // 容器槽：按 UI 顺序添加到列表（不依赖 slot.getIndex()）
                containerUiSlots.add(uiSlotId);
            }
        }
    }

    public int getUiSlotForPlayer(int playerSlotIndex) {
        return playerToUiMap.getOrDefault(playerSlotIndex, -1);
    }

    public int getUiSlotForContainer(int containerSlotIndex) {
        // 容器槽索引就是列表中的顺序索引
        if (containerSlotIndex >= 0 && containerSlotIndex < containerUiSlots.size()) {
            return containerUiSlots.get(containerSlotIndex);
        }
        return -1;
    }
}