package com.mimicenzymes.litematicafiller.utils;

import net.minecraft.item.ItemStack;

public class PrinterIntegration {
    private static Class<?> inventoryUtilsClass;
    private static Class<?> switchItemClass;
    private static java.lang.reflect.Field lastNeedItemListField;
    private static java.lang.reflect.Method switchItemMethod;
    private static java.lang.reflect.Field isOpenHandlerField;

    static {
        try {
            inventoryUtilsClass = Class.forName("me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils");
            switchItemClass = Class.forName("me.aleksilassila.litematica.printer.printer.zxy.inventory.SwitchItem");
            lastNeedItemListField = inventoryUtilsClass.getDeclaredField("lastNeedItemList");
            lastNeedItemListField.setAccessible(true);
            switchItemMethod = inventoryUtilsClass.getDeclaredMethod("switchItem");
            switchItemMethod.setAccessible(true);
            isOpenHandlerField = inventoryUtilsClass.getDeclaredField("isOpenHandler");
            isOpenHandlerField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void addNeedItem(ItemStack stack) {
        try {
            java.util.HashSet<net.minecraft.item.Item> set = (java.util.HashSet<net.minecraft.item.Item>) lastNeedItemListField.get(null);
            set.add(stack.getItem());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void triggerSwitch() {
        try {
            switchItemMethod.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isOpenHandler() {
        try {
            return (boolean) isOpenHandlerField.get(null);
        } catch (Exception e) {
            return false;
        }
    }
}