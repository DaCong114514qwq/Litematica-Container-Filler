// SearchItem.java
package com.mimicenzymes.litematicafiller.core;

import net.minecraft.item.ItemStack;
import red.jackf.chesttracker.api.providers.MemoryLocation;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.api.criteria.builtin.AnyOfCriterion;
import red.jackf.whereisit.client.api.events.SearchRequestPopulator;

import java.util.List;
import java.util.Optional;

public class SearchItem {
    /**
     * 在打印机库存中搜索指定物品，返回第一个找到的容器位置和维度。
     * @param stack 要搜索的物品
     * @return Optional 包含 (维度 Key, 位置)
     */
    public static Optional<MemoryLocation> findItem(ItemStack stack) {
        var memoryBank = PrinterMemoryManager.getMemory();
        if (memoryBank == null) {
            return Optional.empty();
        }

        SearchRequest request = new SearchRequest();
        AnyOfCriterion any = new AnyOfCriterion();
        SearchRequestPopulator.addItemStack(any, stack, SearchRequestPopulator.Context.FAVOURITE);
        if (!any.valid()) return Optional.empty();
        request.accept(any.compact());

        // 遍历所有内存键（目前打印机库存只有一个键，即我们创建的，但为了通用）
        for (var keyId : memoryBank.getMemoryKeys()) {
            List<SearchResult> results = memoryBank.doSearch(keyId, request);
            if (!results.isEmpty()) {
                SearchResult result = results.get(0);
                // 构造 MemoryLocation（我们仅用位置和维度，暂时不需要覆盖）
                return Optional.of(MemoryLocation.inWorld(keyId, result.pos()));
            }
        }
        return Optional.empty();
    }

    // 可选：支持拼音搜索，但 CT 的搜索已经集成了拼音（通过 ItemStackUtilMixin），这里只需调用其 API
}