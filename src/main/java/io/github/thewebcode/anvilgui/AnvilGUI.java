package io.github.thewebcode.anvilgui;

import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class AnvilGUI {

    private static VersionWrapper WRAPPER = new Wrapper1_19_R1();


    private final Plugin plugin;

    private final Player player;

    private String inventoryTitle;

    private ItemStack inputLeft;

    private ItemStack inputRight;

    private final boolean preventClose;

    private final Consumer<Player> closeListener;

    private final BiFunction<Player, String, Response> completeFunction;

    private final Consumer<Player> inputLeftClickListener;
    private final Consumer<Player> inputRightClickListener;

    private int containerId;

    private Inventory inventory;

    private final ListenUp listener = new ListenUp();

    private boolean open;

    @Deprecated
    public AnvilGUI(Plugin plugin, Player holder, String insert, BiFunction<Player, String, String> biFunction) {
        this(plugin, holder, "Repair & Name", insert, null, null, false, null, null, null, (player, text) -> {
            String response = biFunction.apply(player, text);
            if (response != null) {
                return Response.text(response);
            } else {
                return Response.close();
            }
        });
    }

    private AnvilGUI(
            Plugin plugin,
            Player player,
            String inventoryTitle,
            String itemText,
            ItemStack inputLeft,
            ItemStack inputRight,
            boolean preventClose,
            Consumer<Player> closeListener,
            Consumer<Player> inputLeftClickListener,
            Consumer<Player> inputRightClickListener,
            BiFunction<Player, String, Response> completeFunction) {
        this.plugin = plugin;
        this.player = player;
        this.inventoryTitle = inventoryTitle;
        this.inputLeft = inputLeft;
        this.inputRight = inputRight;
        this.preventClose = preventClose;
        this.closeListener = closeListener;
        this.inputLeftClickListener = inputLeftClickListener;
        this.inputRightClickListener = inputRightClickListener;
        this.completeFunction = completeFunction;

        if (itemText != null) {
            if (inputLeft == null) {
                this.inputLeft = new ItemStack(Material.PAPER);
            }

            ItemMeta paperMeta = this.inputLeft.getItemMeta();
            paperMeta.setDisplayName(itemText);
            this.inputLeft.setItemMeta(paperMeta);
        }

        openInventory();
    }

    private void openInventory() {
        WRAPPER.handleInventoryCloseEvent(player);
        WRAPPER.setActiveContainerDefault(player);

        Bukkit.getPluginManager().registerEvents(listener, plugin);

        final Object container = WRAPPER.newContainerAnvil(player, inventoryTitle);

        inventory = WRAPPER.toBukkitInventory(container);
        inventory.setItem(Slot.INPUT_LEFT, this.inputLeft);
        if (this.inputRight != null) {
            inventory.setItem(Slot.INPUT_RIGHT, this.inputRight);
        }

        containerId = WRAPPER.getNextContainerId(player, container);
        WRAPPER.sendPacketOpenWindow(player, containerId, inventoryTitle);
        WRAPPER.setActiveContainer(player, container);
        WRAPPER.setActiveContainerId(container, containerId);
        WRAPPER.addActiveContainerSlotListener(container, player);
        open = true;
    }

    public void closeInventory() {
        closeInventory(true);
    }

    private void closeInventory(boolean sendClosePacket) {
        if (!open) {
            return;
        }

        open = false;

        HandlerList.unregisterAll(listener);

        if (sendClosePacket) {
            WRAPPER.handleInventoryCloseEvent(player);
            WRAPPER.setActiveContainerDefault(player);
            WRAPPER.sendPacketCloseWindow(player, containerId);
        }

        if (closeListener != null) {
            closeListener.accept(player);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    private class ListenUp implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getInventory().equals(inventory)
                    && (event.getRawSlot() < 3 || event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))) {
                event.setCancelled(true);
                final Player clicker = (Player) event.getWhoClicked();
                if (event.getRawSlot() == Slot.OUTPUT) {
                    final ItemStack clicked = inventory.getItem(Slot.OUTPUT);
                    if (clicked == null || clicked.getType() == Material.AIR) return;

                    final Response response = completeFunction.apply(
                            clicker,
                            clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : "");
                    if (response.getText() != null) {
                        final ItemMeta meta = clicked.getItemMeta();
                        meta.setDisplayName(response.getText());
                        clicked.setItemMeta(meta);
                        inventory.setItem(Slot.INPUT_LEFT, clicked);
                    } else if (response.getInventoryToOpen() != null) {
                        clicker.openInventory(response.getInventoryToOpen());
                    } else {
                        closeInventory();
                    }
                } else if (event.getRawSlot() == Slot.INPUT_LEFT) {
                    if (inputLeftClickListener != null) {
                        inputLeftClickListener.accept(player);
                    }
                } else if (event.getRawSlot() == Slot.INPUT_RIGHT) {
                    if (inputRightClickListener != null) {
                        inputRightClickListener.accept(player);
                    }
                }
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event) {
            if (event.getInventory().equals(inventory)) {
                for (int slot : Slot.values()) {
                    if (event.getRawSlots().contains(slot)) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if (open && event.getInventory().equals(inventory)) {
                closeInventory(false);
                if (preventClose) {
                    Bukkit.getScheduler().runTask(plugin, AnvilGUI.this::openInventory);
                }
            }
        }
    }

    public static class Builder {


        private Consumer<Player> closeListener;

        private boolean preventClose = false;

        private Consumer<Player> inputLeftClickListener;

        private Consumer<Player> inputRightClickListener;

        private BiFunction<Player, String, Response> completeFunction;

        private Plugin plugin;

        private String title = "Repair & Name";

        private String itemText;

        private ItemStack itemLeft;

        private ItemStack itemRight;

        public Builder preventClose() {
            preventClose = true;
            return this;
        }

        public Builder onClose(Consumer<Player> closeListener) {
            Validate.notNull(closeListener, "closeListener cannot be null");
            this.closeListener = closeListener;
            return this;
        }

        public Builder onLeftInputClick(Consumer<Player> inputLeftClickListener) {
            this.inputLeftClickListener = inputLeftClickListener;
            return this;
        }

        public Builder onRightInputClick(Consumer<Player> inputRightClickListener) {
            this.inputRightClickListener = inputRightClickListener;
            return this;
        }

        public Builder onComplete(BiFunction<Player, String, Response> completeFunction) {
            Validate.notNull(completeFunction, "Complete function cannot be null");
            this.completeFunction = completeFunction;
            return this;
        }

        public Builder plugin(Plugin plugin) {
            Validate.notNull(plugin, "Plugin cannot be null");
            this.plugin = plugin;
            return this;
        }

        public Builder text(String text) {
            Validate.notNull(text, "Text cannot be null");
            this.itemText = text;
            return this;
        }

        public Builder title(String title) {
            Validate.notNull(title, "title cannot be null");
            this.title = title;
            return this;
        }


        @Deprecated
        public Builder item(ItemStack item) {
            return itemLeft(item);
        }


        public Builder itemLeft(ItemStack item) {
            Validate.notNull(item, "item cannot be null");
            this.itemLeft = item;
            return this;
        }


        public Builder itemRight(ItemStack item) {
            this.itemRight = item;
            return this;
        }


        public AnvilGUI open(Player player) {
            Validate.notNull(plugin, "Plugin cannot be null");
            Validate.notNull(completeFunction, "Complete function cannot be null");
            Validate.notNull(player, "Player cannot be null");
            return new AnvilGUI(
                    plugin,
                    player,
                    title,
                    itemText,
                    itemLeft,
                    itemRight,
                    preventClose,
                    closeListener,
                    inputLeftClickListener,
                    inputRightClickListener,
                    completeFunction);
        }
    }


    public static class Response {


        private final String text;

        private final Inventory openInventory;


        private Response(String text, Inventory openInventory) {
            this.text = text;
            this.openInventory = openInventory;
        }

        public String getText() {
            return text;
        }


        public Inventory getInventoryToOpen() {
            return openInventory;
        }


        public static Response close() {
            return new Response(null, null);
        }


        public static Response text(String text) {
            return new Response(text, null);
        }


        public static Response openInventory(Inventory inventory) {
            return new Response(null, inventory);
        }
    }

    public static class Slot {

        private static final int[] values = new int[] {Slot.INPUT_LEFT, Slot.INPUT_RIGHT, Slot.OUTPUT};


        public static final int INPUT_LEFT = 0;

        public static final int INPUT_RIGHT = 1;

        public static final int OUTPUT = 2;


        public static int[] values() {
            return values;
        }
    }
}

