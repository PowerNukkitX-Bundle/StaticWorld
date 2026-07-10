package org.powernukkitx.staticworld.listener;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.level.LevelLoadEvent;
import org.powernukkitx.event.level.LevelUnloadEvent;
import org.powernukkitx.event.player.PlayerPreChunkRequestEvent;
import org.powernukkitx.event.player.PlayerTeleportEvent;
import org.powernukkitx.level.Level;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.EmptyArrays;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;
import org.cloudburstmc.protocol.bedrock.packet.LevelChunkPacket;
import org.powernukkitx.staticworld.utils.StaticEntry;

import static org.powernukkitx.staticworld.StaticWorld.ENTRIES;

public class StaticWorldListener implements Listener {

    public StaticWorldListener() {
        for(Level level : Server.getInstance().getLevels().values()) {
            this.addLevel(level);
        }
    }

    private void addLevel(Level level) {
        synchronized (ENTRIES) {
            ENTRIES.put(level.getName(), new StaticEntry(level));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLevelLoad(LevelLoadEvent event) {
        this.addLevel(event.getLevel());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLevelUnload(LevelUnloadEvent event) {
        String level = event.getLevel().getName();
        synchronized (ENTRIES) {
            ENTRIES.remove(level).save();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPreChunkRequest(PlayerPreChunkRequestEvent event) {
        if(!event.isForced()) {
            Level level = event.getPlayer().getLevel();
            String levelName = level.getName();
            StaticEntry entry = ENTRIES.get(levelName);
            if(entry != null) {
                if(entry.isEnabled()) {
                    if(!entry.canLoad(event.getChunkX(), event.getChunkZ())) {
                        event.setCancelled();
                    }
                }
            } else throw new NullPointerException("Level {} has no StaticEntry");
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Level from = event.getFrom().getLevel();
        Level to = event.getTo().getLevel();
        if(from != to) {
            if(ENTRIES.containsKey(to.getName())) {
                Player player = event.getPlayer();
                for(long chunkHash : player.getUsedChunks()) {
                    LevelChunkPacket packet = new LevelChunkPacket();
                    packet.setChunkX(Level.getHashX(chunkHash));
                    packet.setChunkZ(Level.getHashZ(chunkHash));
                    packet.setDimension(DimensionType.from(from.getDimension()));
                    packet.setSerializedChunkData(Unpooled.buffer());
                    player.sendPacketImmediately(packet);
                }
            }
        }
    }

}
