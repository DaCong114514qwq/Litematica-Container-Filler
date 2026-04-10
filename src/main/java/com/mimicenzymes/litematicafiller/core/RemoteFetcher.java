package com.mimicenzymes.litematicafiller.core;

import com.mimicenzymes.litematicafiller.config.Configs;
import com.mimicenzymes.litematicafiller.network.RemoteInventoryPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import red.jackf.chesttracker.api.providers.MemoryLocation;

import java.util.Optional;

public class RemoteFetcher {
    private static boolean isWaiting = false;
    private static ItemStack targetItem = null;
    private static MemoryLocation targetLocation = null;
    private static long requestTime = 0;
    private static boolean success = false; // 新增：是否成功取到物品

    public static void fetch(ItemStack item) {
//        System.out.println("[LitematicaFiller-DEBUG] RemoteFetcher.fetch 开始，物品: " + item.getItem().getName().getString());
        if (!Configs.ENABLE_REMOTE_FETCH.getBooleanValue()) {
//            System.out.println("[LitematicaFiller-DEBUG] 远程取物开关未开启");
            return;
        }
        if (isWaiting) {
//            System.out.println("[LitematicaFiller-DEBUG] 已有远程取物进行中，跳过");
            return;
        }

        // 检查是否是空潜影盒
        boolean isEmptyShulker = false;
        if (item.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
            ContainerComponent container = item.get(DataComponentTypes.CONTAINER);
            isEmptyShulker = (container == null || container.stream().noneMatch(s -> !s.isEmpty()));
//            System.out.println("[LitematicaFiller-DEBUG] 物品是潜影盒，是否为空: " + isEmptyShulker);
        }

        if (isEmptyShulker) {
            // 空潜影盒：使用 LitematicaFiller 自己的远程取物流程
//            System.out.println("[LitematicaFiller-DEBUG] 进入空潜影盒特殊处理分支");
            Optional<MemoryLocation> location = SearchItem.findItem(item);
            if (location.isEmpty()) {
                onFetchFailed(item);
                return;
            }
            targetItem = item.copy();
            targetLocation = location.get();
            isWaiting = true;
            requestTime = System.currentTimeMillis();
            success = false;
            // 发送远程打开请求（复用 litematica-printer 的 OpenInventoryPacket）
            try {
                Class<?> openInvPacketClass = Class.forName("me.aleksilassila.litematica.printer.printer.zxy.inventory.OpenInventoryPacket");
                java.lang.reflect.Method sendMethod = openInvPacketClass.getMethod("sendOpenInventory", BlockPos.class, RegistryKey.class);
                RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, targetLocation.memoryKey());
                sendMethod.invoke(null, targetLocation.position(), worldKey);
//                System.out.println("[LitematicaFiller-DEBUG] 已发送远程打开请求");
            } catch (Exception e) {
                e.printStackTrace();
                onFetchFailed(item);
            }
            return;
        }

        // 非空物品：使用 litematica-printer 的标准取物流程
//        System.out.println("[LitematicaFiller-DEBUG] 使用 litematica-printer 的标准取物流程");
        try {
//            System.out.println("[LitematicaFiller-DEBUG] Use litematica-printer fetching stream");
            Class<?> invUtils = Class.forName("me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils");
            java.lang.reflect.Field needListField = invUtils.getDeclaredField("lastNeedItemList");
            needListField.setAccessible(true);
            java.util.Set<net.minecraft.item.Item> needList = (java.util.Set<net.minecraft.item.Item>) needListField.get(null);
            needList.add(item.getItem());
            java.lang.reflect.Method switchMethod = invUtils.getDeclaredMethod("switchItem");
            Boolean isSwitchedItem = (Boolean) switchMethod.invoke(null);
            if (!isSwitchedItem) {
                AutoFillerStateMachine.getInstance().onRemoteFetchFailed(item);
            }
            targetItem = item.copy();
            isWaiting = true;
            requestTime = System.currentTimeMillis();
            success = false;
        } catch (Exception e) {// 回退到自己的网络包（一般不会执行）
            Optional<MemoryLocation> location = SearchItem.findItem(item);
            if (location.isEmpty()) {
                onFetchFailed(item);
                return;
            }
            targetItem = item.copy();
            targetLocation = location.get();
            isWaiting = true;
            requestTime = System.currentTimeMillis();
            RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, targetLocation.memoryKey());
            ClientPlayNetworking.send(new RemoteInventoryPacket(targetLocation.position(), worldKey));
        }
    }

    public static MemoryLocation getTargetLocation() {
        return targetLocation;
    }

    public static void onResponse(boolean success) {
        if (!isWaiting) {
            return;
        }
        if (success) {
            AutoFillerStateMachine.getInstance().onRemoteFetchSuccess(targetItem, targetLocation);
        } else {
            AutoFillerStateMachine.getInstance().onRemoteFetchFailed(targetItem);
        }
        reset();
    }

    public static void onFetchSuccess() {
        if (!isWaiting) return;
        success = true;
        isWaiting = false;
        targetItem = null;
    }

    public static void onFetchFailed(ItemStack item) {
        if (!isWaiting) return;
        success = false;
        isWaiting = false;
        targetItem = null;
    }

    public static boolean isWaiting() { return isWaiting; }
    public static long getRequestTime() { return requestTime; }
    public static ItemStack getTargetItem() { return targetItem; }
    public static boolean isSuccess() { return success; }
    public static void reset() { isWaiting = false; targetItem = null; success = false; }
}