// RemoteInventoryResponsePacket.java
package com.mimicenzymes.litematicafiller.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RemoteInventoryResponsePacket(boolean success) implements CustomPayload {
    public static final Id<RemoteInventoryResponsePacket> ID = new Id<>(Identifier.of("litematica_container_filler", "remote_inventory_response"));
    public static final PacketCodec<RegistryByteBuf, RemoteInventoryResponsePacket> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBoolean(value.success),
            buf -> new RemoteInventoryResponsePacket(buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}