package io.github.thewebcode.anvilgui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface VersionWrapper {


    int getNextContainerId(Player player, Object container);

    void handleInventoryCloseEvent(Player player);

    void sendPacketOpenWindow(Player player, int containerId, String inventoryTitle);

    void sendPacketCloseWindow(Player player, int containerId);

    void setActiveContainerDefault(Player player);

    void setActiveContainer(Player player, Object container);

    void setActiveContainerId(Object container, int containerId);

    void addActiveContainerSlotListener(Object container, Player player);

    Inventory toBukkitInventory(Object container);

    Object newContainerAnvil(Player player, String title);
}