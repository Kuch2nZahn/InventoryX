package io.github.thewebcode.menuutility;

import org.bukkit.entity.Player;

public class TargetPlayerMenuUtility extends PlayerMenuUtility {
    private Player target;

    public TargetPlayerMenuUtility(Player player, Player target){
        super(player);
        this.target = target;
    }

    public Player getTarget() {
        return target;
    }
}

