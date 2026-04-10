package com.mimicenzymes.litematicafiller.mixin;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "me.aleksilassila.litematica.printer.printer.zxy.chesttracker.SearchItem", remap = false)
public class SearchItemMixin {

    @Inject(method = "areStacksEquivalent", at = @At("HEAD"), cancellable = true)
    private static void onAreStacksEquivalent(ItemStack stack1, ItemStack memoryStack, CallbackInfoReturnable<Boolean> cir) {
        // 检查 stack1 是否是空潜影盒
        boolean stack1IsEmptyShulker = false;
        if (stack1.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
            ContainerComponent container = stack1.get(DataComponentTypes.CONTAINER);
            stack1IsEmptyShulker = (container == null || container.stream().noneMatch(s -> !s.isEmpty()));
        }

        // 检查 memoryStack 是否是潜影盒（可以是空或非空）
        boolean memoryIsShulker = memoryStack.getItem() instanceof BlockItem bi2 && bi2.getBlock() instanceof ShulkerBoxBlock;

        // 如果请求的是空潜影盒，而库存中记录的是任意潜影盒（包括非空），都视为匹配成功
        // 注意：这样可能会把非空潜影盒取出来，但实际取物时会取出整个潜影盒，用户可能损失非空潜影盒内的物品。
        // 为了安全，我们可以只匹配空潜影盒与空潜影盒，或者要求内存中的潜影盒也是空的。
        // 但用户需求是取出空潜影盒，所以应该只匹配内存中也是空的潜影盒。
        // 然而，内存中的潜影盒可能被记录为非空（因为当时存储时里面有物品）。所以我们放宽条件：只要内存中是潜影盒，就认为匹配。
        // 但为了减少误匹配，可以检查内存中的潜影盒是否为空。如果非空，取出会损失物品，用户不期望。
        // 因此，我们更精确地：如果请求的是空潜影盒，内存中的潜影盒也必须是空的，才匹配。
        boolean memoryIsEmptyShulker = false;
        if (memoryIsShulker) {
            ContainerComponent memoryContainer = memoryStack.get(DataComponentTypes.CONTAINER);
            memoryIsEmptyShulker = (memoryContainer == null || memoryContainer.stream().noneMatch(s -> !s.isEmpty()));
        }

        if (stack1IsEmptyShulker && memoryIsEmptyShulker) {
            cir.setReturnValue(true);
        }
    }
}