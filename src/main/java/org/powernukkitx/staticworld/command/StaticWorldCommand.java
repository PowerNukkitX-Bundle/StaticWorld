package org.powernukkitx.staticworld.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.tree.ParamList;
import cn.nukkit.command.utils.CommandLogger;
import cn.nukkit.math.AxisAlignedBB;
import org.powernukkitx.simpleworldedit.utils.PlayerManager;
import org.powernukkitx.simpleworldedit.utils.Selection;
import org.powernukkitx.staticworld.StaticWorld;
import org.powernukkitx.staticworld.utils.StaticEntry;

import java.util.Map;

import static org.powernukkitx.staticworld.StaticWorld.ENTRIES;

public class StaticWorldCommand extends PluginCommand<StaticWorld> {

    public StaticWorldCommand() {
        super("staticworld", StaticWorld.get());
        this.setPermission("staticworld.command");
        this.setDescription("Manage your static worlds");
        this.commandParameters.put("operator", new CommandParameter[]{
                CommandParameter.newEnum("operator", false, new String[]{"enable", "disable", "allowchunks", "denychunks"}),
        });
        this.enableParamTree();
    }

    @Override
    public int execute(CommandSender sender, String commandLabel, Map.Entry<String, ParamList> result, CommandLogger log) {
        var list = result.getValue();
        if(sender instanceof Player player) {
            String levelName = player.getLevelName();
            String operator = list.getResult(0);
            synchronized (ENTRIES) {
                StaticEntry entry = ENTRIES.get(levelName);
                switch (operator) {
                    case "enable" -> {
                        entry.setEnabled(true);
                        log.addSuccess("§aEnabled StaticWorld for " + levelName);
                    }
                    case "disable" -> {
                        entry.setEnabled(false);
                        log.addSuccess("§eDisabled StaticWorld for " + levelName);
                    }
                    default -> {
                        boolean allow = operator.equals("allowchunks");
                        if(Server.getInstance().getPluginManager().getPlugin("SimpleWorldEdit") != null) {
                            Selection selection = PlayerManager.get(player).getSelection();
                            if(selection.isValid()) {
                                AxisAlignedBB boundingBox = selection.getBoundingBox();
                                for(int x = ((int) boundingBox.getMinX() >> 4); x < ((int) boundingBox.getMaxX() >> 4); x++) {
                                    for(int z = ((int) boundingBox.getMinZ() >> 4); z < ((int) boundingBox.getMaxZ() >> 4); z++) {
                                        if(allow) {
                                            entry.allowChunk(x, z);
                                        } else entry.denyChunk(x, z);
                                    }
                                }
                                log.addSuccess((allow ? "§aAllowed" : "§eDenied") + " your selection from StaticWorld for " + levelName);
                            } else log.addError("§cYour selection is not valid!");
                        } else log.addError("§cSimpleWorldEdit is required to perform this action.");
                    }
                }
                entry.save();
            }
            log.output();
            return 1;
        } else return 0;
    }
}
