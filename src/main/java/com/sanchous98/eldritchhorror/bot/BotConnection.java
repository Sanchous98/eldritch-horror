package com.sanchous98.eldritchhorror.bot;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jspecify.annotations.Nullable;

/**
 * A discarding serverbound {@link Connection} for a {@link BotPlayer}. There is no client and no
 * Netty channel, so every outgoing {@code send} is dropped and no inbound protocol is ever set up.
 *
 * <p>This mirrors the {@code FakeConnection} used by NeoForge's {@code FakePlayer}, but additionally
 * drops the outbound {@code send} variants and {@link #flushChannel()} so that server broadcasts
 * (player-list updates, chunk batches, chat, death messages) that reach the bot can never dereference
 * a null channel or queue unbounded pending actions.
 */
public final class BotConnection extends Connection {

    public BotConnection() {
        super(PacketFlow.SERVERBOUND);
    }

    @Override
    public void setListenerForServerboundHandshake(PacketListener packetListener) {
        // No handshake: the bot has no client to negotiate one.
    }

    @Override
    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> protocol, T packetListener) {
        // No inbound pipeline: the bot never receives packets.
    }

    @Override
    public void send(Packet<?> packet) {
        // Discarding connection: no client to receive it.
    }

    @Override
    public void send(Packet<?> packet, @Nullable ChannelFutureListener listener) {
        // Discarding connection: no client to receive it.
    }

    @Override
    public void send(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush) {
        // Discarding connection: no client to receive it.
    }

    @Override
    public void flushChannel() {
        // Nothing is ever queued, so there is nothing to flush.
    }
}
