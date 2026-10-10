package com.sanchous98.eldritchhorror.bot;

import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.Nullable;

/**
 * A headless, network-less {@link ServerPlayer} that the server and the mod's per-player systems
 * treat as a real player: it is registered in the player list, ticked every server tick, and can
 * take damage and die exactly like a connecting player (it is deliberately <b>not</b> permanently
 * invulnerable, so combat and sanity work).
 *
 * <p>Unlike NeoForge's {@code FakePlayer} it does <b>not</b> no-op {@code tick}/{@code die}, so it
 * participates in the world. Outgoing packets are dropped by the bound {@link BotConnection} and by
 * {@link BotListener}; there is never a real channel.
 */
public class BotPlayer extends ServerPlayer {

    private final BotConnection botConnection = new BotConnection();

    public BotPlayer(MinecraftServer server, ServerLevel level, GameProfile profile) {
        super(server, level, profile, ClientInformation.createDefault());
        this.connection = new BotListener(server, this, this.botConnection);
    }

    /** The discarding connection this bot's server listener is bound to. */
    public BotConnection botConnection() {
        return this.botConnection;
    }

    @Override
    public boolean isFakePlayer() {
        return true;
    }

    /**
     * The server-side counterpart of a real connection, bound to a {@link BotConnection}. It drops
     * every outgoing packet before NeoForge's payload validation runs (there is no negotiated
     * channel), and tolerates the per-tick flush the server performs for every listed player.
     */
    private static final class BotListener extends ServerGamePacketListenerImpl {

        private BotListener(MinecraftServer server, ServerPlayer player, BotConnection connection) {
            super(server, connection, player,
                    CommonListenerCookie.createInitial(player.getGameProfile(), false));
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
        public boolean hasChannel(net.minecraft.resources.Identifier payloadId) {
            // No negotiated channels, and the discarding connection has no Netty channel to query.
            return false;
        }
    }
}
