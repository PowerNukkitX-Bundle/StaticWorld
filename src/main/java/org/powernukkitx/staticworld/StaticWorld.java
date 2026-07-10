package org.powernukkitx.staticworld;

import org.powernukkitx.Server;
import org.powernukkitx.plugin.PluginBase;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.powernukkitx.staticworld.command.StaticWorldCommand;
import org.powernukkitx.staticworld.listener.StaticWorldListener;
import org.powernukkitx.staticworld.utils.StaticEntry;


public class StaticWorld extends PluginBase {

    protected static StaticWorld INSTANCE = null;

    public static final Object2ObjectArrayMap<String, StaticEntry> ENTRIES = new Object2ObjectArrayMap<>();

    @Override
    public void onLoad() {
        INSTANCE = this;
        Server.getInstance().getCommandMap().register("staticworld", new StaticWorldCommand());
    }

    @Override
    public void onEnable() {
        Server.getInstance().getPluginManager().registerEvents(new StaticWorldListener(), this);
    }

    public static StaticWorld get() {
        return INSTANCE;
    }
}
