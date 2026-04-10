package com.mimicenzymes.litematicafiller.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record RemoteInventoryPacket(BlockPos pos, RegistryKey<World> worldKey) implements CustomPayload {
    public static final Id<RemoteInventoryPacket> ID = new Id<>(Identifier.of("litematica_container_filler", "remote_inventory"));
    public static final PacketCodec<RegistryByteBuf, RemoteInventoryPacket> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeBlockPos(value.pos);
                buf.writeIdentifier(value.worldKey.getValue()); // 用 Identifier 代替
            },
            buf -> new RemoteInventoryPacket(
                    buf.readBlockPos(),
                    RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier())
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}