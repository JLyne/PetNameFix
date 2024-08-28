package io.github.tanguygab.petnamefix;

import com.google.common.base.Optional;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class PipelineInjector {

    private static final String DECODER_NAME = "PetNameFix";

	public PipelineInjector() {
        Bukkit.getServer().getOnlinePlayers().forEach(this::inject);
    }

    public void unload() {
        Bukkit.getServer().getOnlinePlayers().forEach(this::uninject);
    }


    public void inject(Player player) {
        final Channel channel = getChannel(player);
        if (channel != null && channel.pipeline().names().contains("packet_handler"))
            try {
                uninject(player);
                channel.pipeline().addBefore("packet_handler", DECODER_NAME, new BukkitChannelDuplexHandler());
            } catch (Exception ignored) {}
    }

    public void uninject(Player player) {
        final Channel channel = getChannel(player);
        if (channel != null && channel.pipeline().names().contains(DECODER_NAME))
            channel.pipeline().remove(DECODER_NAME);
    }

    private Channel getChannel(Player player) {
        return ((CraftPlayer) player).getHandle().connection.connection.channel;
    }

    public class BukkitChannelDuplexHandler extends ChannelDuplexHandler {

        @Override
        public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
            if(packet instanceof ClientboundBundlePacket bundle) {
                for(Packet<? super ClientGamePacketListener> p : bundle.subPackets()) {
                    if(p instanceof ClientboundSetEntityDataPacket metadata) {
                        checkMetaData(metadata);
                    }
                }

            } else if(packet instanceof ClientboundSetEntityDataPacket metadata) {
                checkMetaData(metadata);
            }

            super.write(ctx, packet, promise);
        }
    }

    private void checkMetaData(ClientboundSetEntityDataPacket packet) {
        List<SynchedEntityData.DataValue<?>> items = packet.packedItems();

        try {
			for (Iterator<SynchedEntityData.DataValue<?>> iterator = items.iterator(); iterator.hasNext(); ) {
				SynchedEntityData.DataValue<?> item = iterator.next();
				if (item == null) continue;
				int slot;
				Object value;

				slot = item.id();
				value = item.value();

				/* DataWatcher position of pet owner field */
				if (slot == 18) {
					if (value instanceof java.util.Optional || value instanceof Optional) {
						iterator.remove();
					}
				}
			}
        } catch (ConcurrentModificationException e) {
            //no idea how can this list change in another thread since it's created for the packet but whatever, try again
            checkMetaData(packet);
        }
    }
}
