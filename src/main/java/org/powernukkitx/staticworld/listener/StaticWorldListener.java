package org.powernukkitx.staticworld.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.level.LevelUnloadEvent;
import cn.nukkit.event.player.PlayerPreChunkRequestEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Level;
import cn.nukkit.network.protocol.LevelChunkPacket;
import io.netty.util.internal.EmptyArrays;
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
                    LevelChunkPacket chunk = new LevelChunkPacket();
                    chunk.chunkX = Level.getHashX(chunkHash);
                    chunk.chunkZ = Level.getHashZ(chunkHash);
                    chunk.data = EmptyArrays.EMPTY_BYTES;
                    player.dataPacketImmediately(chunk);
                }
            }
        }
    }

}
