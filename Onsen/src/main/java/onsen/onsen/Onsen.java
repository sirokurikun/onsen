package onsen.onsen;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

public final class Onsen extends JavaPlugin implements Listener {

    private static Onsen instance;

    public static Onsen getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("onsen")) {
            if (!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            if (args.length <= 0) {
                return false;
            }
            if(args[0].equalsIgnoreCase("reload")){
                if (sender.hasPermission("set.op")) {
                    reloadConfig();
                    getLogger().info("configリロードしました");
                    sender.sendMessage("[server] OnsenPL configリロードしました");
                } else {
                    sender.sendMessage("権限者のみ使えます");
                }
                return false;
            }

            if(args[0].equalsIgnoreCase("request")){
                if (args.length <= 1) {
                    return false;
                }
                if(args[1].equalsIgnoreCase(args[1])){
                    ItemStack itempack = player.getInventory().getItemInMainHand();
                    ItemMeta itemMeta = itempack.getItemMeta();
                    String itemdisplayname = itemMeta.getDisplayName();
                    if(itemdisplayname.equals((ChatColor.translateAlternateColorCodes('&',"&5温泉リクエストチケット")))) {
                        World world = player.getWorld();
                        String worldname = world.getName();
                        String displayname = player.getName();
                        Location loc = player.getLocation();
                        int x = loc.getBlockX();
                        int y = loc.getBlockY();
                        int z = loc.getBlockZ();
                        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a温泉リクエストチケット"));
                        List<String> lore = new ArrayList<String>();
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&dプレイヤー名&f:" + displayname));
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&dワールド名&f:" + worldname));
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&dX座標&f:" + x));
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&dY座標&f:" + y));
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&dZ座標&f:" + z));
                        lore.add(ChatColor.translateAlternateColorCodes('&',"&d温泉名&f:"+ args[1]));
                        if (args.length <= 2) {
                            return false;
                        }
                        if(args[2].equalsIgnoreCase(args[2])){
                            lore.add(ChatColor.translateAlternateColorCodes('&',"&d希望アイテム&f:"+args[2]));
                        }
                        itemMeta.setLore(lore);
                        itempack.setItemMeta(itemMeta);
                        player.sendMessage("リクエスト用として情報を記載しました");
                    }
                }
                return false;
            }

            if(args[0].equalsIgnoreCase("menu")){
                Player p = (Player) sender;
                p.sendMessage("OnsenMenuを開きました！");
                OnsenMenu(p);
                return false;
            }

            String data = this.getConfig().getString(args[0]);
            if (data == null) {
                player.sendMessage(ChatColor.RED + "そのような名前の温泉はありません\n正しいコマンドを入力してください" + ChatColor.WHITE + "例)/onsen normal");
                return true;
            }

            String[] loc = data.split(",");
            World world = Bukkit.getServer().getWorld(loc[0]);
            double x = Double.parseDouble(loc[1]);
            double y = Double.parseDouble(loc[2]);
            double z = Double.parseDouble(loc[3]);
            Location location = new Location(world, x, y, z);
            player.teleport(location);
            player.sendMessage(ChatColor.GOLD + "温泉へ移動しました。ゆっくり休んでね" + ChatColor.AQUA + "^w^");
            return false;
        }

        if (cmd.getName().equalsIgnoreCase("setonsen")) {
            if (!(sender instanceof Player)) return false;
            if (args.length <= 0) {
                return false;
            }
            if(sender.hasPermission("set.op")){
                Player player = (Player) sender;
                Location loc = player.getLocation();
                String world = loc.getWorld().getName();
                int x = loc.getBlockX();
                int y = loc.getBlockY();
                int z = loc.getBlockZ();
                this.getConfig().set(args[0],world + "," + x + "," + y + "," + z);
                this.saveConfig();
                player.sendMessage("温泉を登録しました");
                getServer().dispatchCommand(getServer().getConsoleSender(), "mm i give " + player.getName() + " onsen_ticket 5");
                return false;
            }else {
                sender.sendMessage("権限者のみ使えます");
            }
        }
        return true;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        Player player = (Player)event.getWhoClicked();
        if(event.getView().getTitle().equals("§cOnsenMenu")){
            event.setCancelled(true);
            int slot = event.getSlot();
            //  SLOTが一致するKeyを探す
            String name = null;
            for (String key : getConfig().getConfigurationSection("teleport").getKeys(false)) {
                int targetNumber = getConfig().getInt("teleport." + key + ".slot");
                //  if
                if(slot == targetNumber){
                    name = key;  //  名前を入れてあげる
                    break;  //  loopから抜ける
                }
            }
            //  なかった場合
            if(name == null){
                player.sendMessage("そのスロットには温泉がないため\nテレポートできません。");
                return;
            }
            //  コマンドを実行
            Bukkit.dispatchCommand(player,"onsen " + name);
        }
    }

    @EventHandler
    public void onBlockDamege(BlockDamageEvent e){
        Player player = e.getPlayer();
        ItemStack itempack = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = itempack.getItemMeta();
        if(itemMeta == null) return;
        String itemdisplayname = itemMeta.getDisplayName();
        if(itemdisplayname.equals((ChatColor.translateAlternateColorCodes('&',"&a温泉リクエストチケット")))) {
            Block clickedBlock = e.getBlock();
            if (clickedBlock.getType() == Material.OAK_WALL_SIGN) {
                Sign sign = (Sign) clickedBlock.getState();
                String line = sign.getLine(0);
                if(line.equals("温泉リクエスト")){
                    Block block = e.getBlock();
                    for (BlockFace face : new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,}) {
                        Block relative = block.getRelative(face);
                        if (relative.getType() == Material.CHEST) {
                            Chest chest = (Chest) relative.getState();
                            Inventory inv = chest.getInventory();
                            int emptySlot = inv.firstEmpty();
                            if (emptySlot == -1) {
                                player.sendMessage("チェストが満タンです。運営に報告してください");
                                return;
                            }else{
                                inv.addItem(itempack);
                                itempack.setAmount(0);
                                player.sendMessage("リクエスト用紙を申請しました");
                            }
                        }
                    }
                }
            }
        }
    }

    public void OnsenMenu(Player p) {
        Inventory inv = getServer().createInventory(null, 54, "§cOnsenMenu");
        for (String key : getConfig().getConfigurationSection("teleport").getKeys(false)) {
            //  MaterialName
            String materialName = getConfig().getString("teleport." + key + ".item");
            Material material = Material.valueOf(materialName);  //  これで指定した名前と一致するマテリアルが取得できる
            //  ItemStack
            ItemStack itemstack = new ItemStack(material);
            itemstack.addUnsafeEnchantment(Enchantment.DURABILITY,1);
            //  ItemMeta
            ItemMeta meta = itemstack.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            //  Itemの名前
            String name = getConfig().getString("teleport." + key + ".name");
            meta.setDisplayName(name);
            itemstack.setItemMeta(meta);
            //  Itemの説明
            //  slot number
            int slot = getConfig().getInt("teleport." + key + ".slot");
            //  配置
            inv.setItem(slot,itemstack);
        }
        p.openInventory(inv);
    }
}
