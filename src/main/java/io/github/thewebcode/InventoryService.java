package io.github.thewebcode;

import io.github.thewebcode.menuutility.PlayerMenuUtility;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class InventoryService {
    private HashMap<Player, PlayerMenuUtility> playerMenuUtilityHashMap;

    public InventoryService(){
        this.playerMenuUtilityHashMap = new HashMap<>();
    }

    public PlayerMenuUtility getPlayerMenuUtility(Player player){
        if(!(playerMenuUtilityHashMap.containsKey(player))){
            PlayerMenuUtility utility = new PlayerMenuUtility(player);
            playerMenuUtilityHashMap.put(player, utility);

            return utility;
        }

        return playerMenuUtilityHashMap.get(player);
    }
}
