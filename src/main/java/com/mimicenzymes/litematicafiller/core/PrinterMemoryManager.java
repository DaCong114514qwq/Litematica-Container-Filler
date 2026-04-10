package com.mimicenzymes.litematicafiller.core;

import red.jackf.chesttracker.impl.memory.MemoryBankImpl;
import java.lang.reflect.Field;

public class PrinterMemoryManager {
    private static MemoryBankImpl PRINTER_MEMORY = null;

    public static void ensureInitialized() {
        if (PRINTER_MEMORY != null) return;
        try {
            Class<?> memoryUtilsClass = Class.forName("me.aleksilassila.litematica.printer.printer.zxy.chesttracker.MemoryUtils");
            Field field = memoryUtilsClass.getDeclaredField("PRINTER_MEMORY");
            field.setAccessible(true);
            PRINTER_MEMORY = (MemoryBankImpl) field.get(null);
            if (PRINTER_MEMORY == null) {
                System.err.println("[LitematicaFiller] 无法获取 litematica-printer 的 PRINTER_MEMORY");
            } else {
                System.out.println("[LitematicaFiller] 成功获取 litematica-printer 的打印机库存");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[LitematicaFiller] 反射获取 PRINTER_MEMORY 失败，可能 litematica-printer 未加载或版本不兼容");
        }
    }

    public static MemoryBankImpl getMemory() {
        ensureInitialized();
        return PRINTER_MEMORY;
    }
}