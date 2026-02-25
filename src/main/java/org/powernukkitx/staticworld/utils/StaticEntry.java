package org.powernukkitx.staticworld.utils;

import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StaticEntry {

    private final static String ENABLED = "enabled";
    private final static String CHUNKS = "chunks";

    @Getter
    @Setter
    private boolean enabled;
    private final String level;
    private final LongArraySet chunkHashes = new LongArraySet();

    private final Config config;

    @SneakyThrows
    public StaticEntry(Level level) {
        this.level = level.getName();
        File file = new File(level.getFolderPath(), "staticworld.yml");
        if(!file.exists()) file.createNewFile();
        this.config = new Config(file);
        this.enabled = config.getBoolean(ENABLED, false);
        this.chunkHashes.addAll(config.getLongList(CHUNKS));
    }

    public boolean canLoad(int chunkX, int chunkZ) {
        long chunkHash = Level.chunkHash(chunkX, chunkZ);
        return canLoad(chunkHash);
    }

    public boolean canLoad(long chunkHash) {
        return chunkHashes.contains(chunkHash);
    }

    public void allowChunk(int chunkX, int chunkZ) {
        long chunkHash = Level.chunkHash(chunkX, chunkZ);
        if(!this.canLoad(chunkHash)) {
            this.chunkHashes.add(chunkHash);
        }
    }

    public void denyChunk(int chunkX, int chunkZ) {
        long chunkHash = Level.chunkHash(chunkX, chunkZ);
        this.chunkHashes.remove(chunkHash);
    }

    public void save() {
        config.set(ENABLED, enabled);
        config.set(CHUNKS, chunkHashes.toArray(Long[]::new));
        config.save();
    }

}
