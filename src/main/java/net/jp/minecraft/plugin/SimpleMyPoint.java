package net.jp.minecraft.plugin;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

/**
 * MyHomeを設定し、コマンドでMyHomeに戻れるプラグイン
 * SimpleMyHome v0.0.2
 * @author syokkendesuyo
 */


public class SimpleMyPoint extends JavaPlugin implements Listener {


	/**
	 * プラグインが有効になったときに呼び出されるメソッド
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// ｽﾃｲﾀｽの送信に失敗 :-(
		}

		this.getConfig().options().copyDefaults(true);
		saveDefaultConfig();

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if(!(sender instanceof Player)){
			sender.sendMessage("Please excute this SimpleMyPoint command on a game!");
			sender.sendMessage("SimpleMyPoint コマンドはゲーム内で実行してください。");
		}
		else{

			Player player = (Player) sender;
			String displayname = player.getName();
			String uuid = player.getUniqueId().toString();
			String world =player.getLocation().getWorld().getName().toString();
			Double x =player.getLocation().getX();
			Double y =player.getLocation().getY();
			Double z =player.getLocation().getZ();
			float yaw = player.getLocation().getYaw();       //向いている方角と視線の高さをconfigに書き込みます
			float pitch = player.getLocation().getPitch();   //このデータはsethomeやwherehomeのコマンドで表示しない




			if (cmd.getName().equalsIgnoreCase("setpoint")) {
				if(args.length == 0){
					sender.sendMessage(ChatColor.AQUA + "□ /setpoint <地点名> で地点を登録できます。");
				}
				else{
					String point = args[0].toString();
					FileConfiguration tf = this.getConfig();

					if(!(tf.getString("points." + point + ".world") == null || tf.getString("points." + point + ".x") == null || tf.getString("points." + point + ".y") == null || tf.getString("points." + point + ".z") == null)){
						sender.sendMessage(ChatColor.RED + "□ " + point  + " は既に登録されています。");
					}
					else{
						this.getConfig().set("points." + point + ".registrant_name" , displayname);
						this.getConfig().set("points." + point + ".registrant_uuid" , uuid);
						this.getConfig().set("points." + point + ".world" , world);
						this.getConfig().set("points." + point + ".x" , x);
						this.getConfig().set("points." + point + ".y" , y);
						this.getConfig().set("points." + point + ".z" , z);
						this.getConfig().set("points." + point + ".yaw" , yaw);
						this.getConfig().set("points." + point + ".pitch" , pitch);

						saveConfig();

						//座標は四捨五入で表示する
						sender.sendMessage(ChatColor.AQUA + "□ 現在地を " + point  + " と定めました。[ "+ world + " , " + Math.round(x) + " , " + Math.round(y) + " , " + Math.round(z) + " ]");
						sender.sendMessage(ChatColor.AQUA + "□ /warp " + point  + " を実行すると現在設定した場所へテレポート可能です。");
					}
				}
				return true;
			}

			else if(cmd.getName().equalsIgnoreCase("warp")){
				if(args.length == 0){
					sender.sendMessage(ChatColor.AQUA + "□ /warp <地点名> で登録地点へワープできます。");
				}
				else{
					String point = args[0].toString();
					FileConfiguration tf = this.getConfig();

					if(tf.getString("points." + point + ".world") == null || tf.getString("points." + point + ".x") == null || tf.getString("points." + point + ".y") == null || tf.getString("points." + point + ".z") == null){
						sender.sendMessage(ChatColor.RED + "□ "+ point + "は登録されていないためテレポートできませんでした。");
					}
					else if(sender.hasPermission("smp.use." + point)){
						String getworld = this.getConfig().getString("points." + point + ".world");
						Double getx = this.getConfig().getDouble("points." + point + ".x" , x);
						Double gety = this.getConfig().getDouble("points." + point + ".y");
						Double getz = this.getConfig().getDouble("points." + point + ".z");
						float getyaw = (float) this.getConfig().getDouble("points." + point + ".yaw");
						float getpitch = (float) this.getConfig().getDouble("points." + point + ".pitch");

						//configから抽出したワールド名をbukkit仕様の"World"変数setworldへ代入
						//この処理が無いとワールドを取得できません。
						World setworld = Bukkit.getWorld(getworld);

						Location location = new Location(setworld, getx, gety, getz);
						location.setYaw(getyaw);
						location.setPitch(getpitch);
						player.teleport(location);

						sender.sendMessage(ChatColor.AQUA + "□ 登録地点 " + point + " にテレポートしました。");
					}
					else{
						sender.sendMessage(ChatColor.RED + "□ "+ point + " へは権限が無いためテレポートできません。");
					}
				}
				return true;
			}

			else if(cmd.getName().equalsIgnoreCase("detailpoint")){
				if(args.length == 0){
					sender.sendMessage(ChatColor.AQUA + "□ /detailpoint <地点名> で登録地点の情報を表示します。");
				}
				else{
					String point = args[0].toString();
					FileConfiguration tf = this.getConfig();

					if(tf.getString("points." + point + ".world") == null || tf.getString("points." + point + ".x") == null || tf.getString("points." + point + ".y") == null || tf.getString("points." + point + ".z") == null){
						sender.sendMessage(ChatColor.RED + "□ "+ point + "は登録されていないため詳細を表示できませんでした。");
					}
					else{
						String getworld = this.getConfig().getString("points." + point + ".world");
						String getname = this.getConfig().getString("points." + point + ".registrant_name");
						Double getx = this.getConfig().getDouble("points." + point + ".x" , x);
						Double gety = this.getConfig().getDouble("points." + point + ".y");
						Double getz = this.getConfig().getDouble("points." + point + ".z");

						sender.sendMessage(" ");
						sender.sendMessage(ChatColor.GOLD + "＝＝＝  登録地点：" + point + " ＝＝＝");
						sender.sendMessage(ChatColor.AQUA + "登録者名  ： " + ChatColor.WHITE + getname);
						sender.sendMessage(ChatColor.AQUA + "ワールド名： " + ChatColor.WHITE + getworld);
						sender.sendMessage(ChatColor.AQUA + "座標      ： " + ChatColor.WHITE + Math.round(getx) + " , " + Math.round(gety) + " , " + Math.round(getz));
						sender.sendMessage(" ");
					}
				}
				return true;
			}

			else if(cmd.getName().equalsIgnoreCase("deletepoint")){
				if(args.length == 0){
					sender.sendMessage(ChatColor.AQUA + "□ /deletepoint <地点名> で登録地点を削除します。");
				}
				else{
					String point = args[0].toString();
					FileConfiguration tf = this.getConfig();

					if(tf.getString("points." + point + ".world") == null || tf.getString("points." + point + ".x") == null || tf.getString("points." + point + ".y") == null || tf.getString("points." + point + ".z") == null){
						sender.sendMessage(ChatColor.RED + "□ " + point + "は登録されていないため削除できませんでした。");
					}
					else{
						this.getConfig().set("points." + point + ".registrant_name" , null);
						this.getConfig().set("points." + point + "..registrant_uuid" , null);
						this.getConfig().set("points." + point + ".world" , null);
						this.getConfig().set("points." + point + ".x" , null);
						this.getConfig().set("points." + point + ".y" , null);
						this.getConfig().set("points." + point + ".z" , null);
						this.getConfig().set("points." + point + ".yaw" , null);
						this.getConfig().set("points." + point + ".pitch" , null);

						saveConfig();

						sender.sendMessage(ChatColor.AQUA + "□ 登録地点 " + point + " を削除しました。");
					}
				}
				return true;
			}
		}
		return false;
	}
}
