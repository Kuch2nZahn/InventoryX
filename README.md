# Outdated
This repository is no longer up to date. However, there is a newer plugin, with which even more menus, and even more design options can be implemented!

# [Click here to go to the newer version](https://github.com/TheWebcode/Y)

## Inventory X

## What is InventoryX?

InventoryX is a painstakingly developed plugin to make it easier for Minecraft plugin programmers to create inventories. Since the Bukkit Inventory API is kept fairly simple, InventoryX makes it easy to create

- Small inventories
- Large inventories
- Animated inventories
- Multi-page inventories
- Anvil GUI inventories

InventoryX is still based on the original Paper/Spigot API, but is designed to make it as easy as possible for programmers to create an inventory the way they want it.

## Why should I use InventoryX?

As already mentioned, it is very complicated to create inventories with the normal Spigot/Paper API. This can sometimes require multiple Java classes, and larger chunks of code. With InventoryX, however, it's very easy to create, animate, manage, and use a menu over and over again in a class that doesn't require a lot of code

## How do I import InventoryX?


We recommend the simple import via Maven:
```
<dependency>
<groupId>io.github.thewebcode</groupId>
<artifactId>InventoryX</artifactId>
<version>MC-VERSION</version>
</dependency>
```

Replace MC-VERSION with the Minecraft version. As an an example:

```
<dependency>
<groupId>io.github.thewebcode</groupId>
<artifactId>InventoryX</artifactId>
<version>1.19</version>
</dependency>
```

Unfortunately, an import via Gradle is not available at the moment. Instead, you can download the library [here](https://github.com/TheWebcode/InventoryX/releases) and import it into your project.

## What versions is InventoryX compatible with?

| Version | Paper | Spigot |
|---------|-------|--------|
| 1.19.1  |   ❌    |   ❌     |
| 1.19  | ✅     |    ✅    |



## How to use InventoryX?

This is a fairly simple inventory, and almost all of the methods are self-explanatory:
```
public class SimpleMenu extends Menu {

    public SimpleMenu(PlayerMenuUtility utility) {
        super(utility);
    }

    @Override
    public String getMenuName() {
        return "A Simple Menu";
    }

    @Override
    public int getSlots() {
        return 27; // This number must be divisible by 9!
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getCurrentItem().getType() == Material.PAPER) {
            Player player = (Player) e.getWhoClicked();
            player.closeInventory();
            player.sendMessage("You clicked on a Paper!");
        }
    }

    @Override
    public void setMenuItems() {
        ItemStack item = makeItem(Material.PAPER, "I am an Item", "Click on me");

        inventory.addItem(item);
    }
}
```

In the setMenuItems() method, all items that should be in the menu are added. An InventoryClickEvent is passed in the handleMenu() method and any action is taken depending on the item.

However, for the inventories to work, the following code in an event listener is *extremely* important, otherwise the inventories won't work!

```
    @EventHandler
    public void onMenuClick(InventoryClickEvent e){
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu) {
            e.setCancelled(true); 
            if (e.getCurrentItem() == null) { 
                return;
            }
            Menu menu = (Menu) holder;
            menu.handleMenu(e);
        }
    }

```


Here is an example of a page based inventory. This requires an ArrayList with various objects that are to be displayed in the inventory and are divided into the individual pages in the setMenuItems() method:

```
public class KillPlayerMenu extends PaginatedMenu {

    public KillPlayerMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Choose a Player to Murder";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        ArrayList<Player> players = new ArrayList<Player>(getServer().getOnlinePlayers());
        if (e.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
            TargetPlayerMenuUtility playerMenuUtility = new TargetPlayerMenuUtility(player);
            playerMenuUtility.setTarget(Bukkit.getPlayer(UUID.fromString(e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(MenuManagerSystem.getPlugin(), "uuid"), PersistentDataType.STRING))));
            new KillConfirmMenu(playerMenuUtility).open();
        }else if (e.getCurrentItem().getType().equals(Material.BARRIER)) {
            p.closeInventory();
        }else if(e.getCurrentItem().getType().equals(Material.DARK_OAK_BUTTON)){
            if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Left")){
                if (page == 0){
                    p.sendMessage(ChatColor.GRAY + "You are already on the first page.");
                }else{
                    page = page - 1;
                    super.open();
                }
            }else if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Right")){
                if (!((index + 1) >= players.size())){
                    page = page + 1;
                    super.open();
                }else{
                    p.sendMessage(ChatColor.GRAY + "You are on the last page.");
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
        ArrayList<Player> players = new ArrayList<Player>(getServer().getOnlinePlayers());
        if(players != null && !players.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * page + i;
                if(index >= players.size()) break;
                if (players.get(index) != null){
                    ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD, 1);
                    ItemMeta playerMeta = playerItem.getItemMeta();
                    playerMeta.setDisplayName(ChatColor.RED + players.get(index).getDisplayName());

                    playerMeta.getPersistentDataContainer().set(new NamespacedKey(MenuManagerSystem.getPlugin(), "uuid"), PersistentDataType.STRING, players.get(index).getUniqueId().toString());
                    playerItem.setItemMeta(playerMeta);

                    inventory.addItem(playerItem);
                }
            }
        }
    }
}
```

The code to navigate through the individual pages is actually always the same. Page switching buttons are added to the menu in the addMenuBorder() method.
This if query and the subsequent for loop is very important to correctly distribute the items in the ArrayList to the individual pages of the menu:
```
if(players != null && !players.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * page + i;
                if(index >= players.size()) break;
                if (players.get(index) != null){
                    ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD, 1);
                    ItemMeta playerMeta = playerItem.getItemMeta();
                    playerMeta.setDisplayName(ChatColor.RED + players.get(index).getDisplayName());

                    playerMeta.getPersistentDataContainer().set(new NamespacedKey(MenuManagerSystem.getPlugin(), "uuid"), PersistentDataType.STRING, players.get(index).getUniqueId().toString());
                    playerItem.setItemMeta(playerMeta);

                    inventory.addItem(playerItem);
                }
            }
        }
```

And this is the code to make the buttons from the addMenuBorder() method work too:
```
if (e.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
            TargetPlayerMenuUtility playerMenuUtility = new TargetPlayerMenuUtility(player);
            playerMenuUtility.setTarget(Bukkit.getPlayer(UUID.fromString(e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(MenuManagerSystem.getPlugin(), "uuid"), PersistentDataType.STRING))));
            new KillConfirmMenu(playerMenuUtility).open();
        }else if (e.getCurrentItem().getType().equals(Material.BARRIER)) {
            p.closeInventory();
        }else if(e.getCurrentItem().getType().equals(Material.DARK_OAK_BUTTON)){
            if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Left")){
                if (page != 0){
                    page = page - 1;
                    super.open();
                }
            }else if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Right")){
                if (!((index + 1) >= players.size())){
                    page = page + 1;
                    super.open();
                }
            }
        }
```

In this menu we didn't use a PlayerMenuUtility, but a TargetPlayerMenuUtility. This is for purposes where one player (the player opening the inventory) applies effects to another player (the target). The TargetPlayerMenuUtility makes it possible to transfer a target in addition to an inventory owner.
This is an example of an Anil-GUI:

```
new AnvilGUI.Builder()
                .onComplete((player, txt) -> {
                    System.out.println("Age of: " + player.getName() + " : " + txt);
                    return AnvilGUI.Response.close();
                })
                .text("Here you can enter your age")
                .preventClose()
                .title("Enter your age:")
                .itemRight(Material.STONE_SWORD)
                .itemLeft(new ItemStack(Material.PAPER))
                .plugin(Main.pluginInstance)
                .open(p);
```


The Anvil gui is also quite simple. First, a builder is called, to which an instance of the JavaPlugin is passed with the .plugin() method. With the .open() method, the builder opens the GUI to the given player.
.itemLeft() and .itemRight() are passed to ItemStacks that end up in the left slot and the output slot of the AnvilGUI. These can also have names, ItemMeta, etc. The .title() method determines the name of the inventory. The .text() method which text should appear as a placeholder in the inventory.
.preventClose() just prevents the player from closing the inventory. .onComplete is an interface with only one method, which is why it makes sense to use Lambda here. Here a player and a text are passed. The player is the one who filled in the GUI, and the text is the text entered by the player.
It is important here that you return an AnvilGUI.Respone. The following parameters are available for selection:

- ```.close()``` - closes the inventory
- ```.openInventory()```- Opens another inventory
- ```.text()``` - Reopens the inventory but with a new placeholder text

You can use the openInventory() method to open a menu, for example. Otherwise, almost all methods are self-explanatory.

## Developer

- TheWebcode


