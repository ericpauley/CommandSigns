package org.zonedabone.commandsigns;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class CommandSignsPlayerProxy implements Player {

	private Player proxy;
	
	public CommandSignsPlayerProxy(Player proxy){
		this.proxy = proxy;
	}

	public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
		proxy.abandonConversation(conversation, details);
	}

	public void abandonConversation(Conversation conversation) {
		proxy.abandonConversation(conversation);
	}

	public void acceptConversationInput(String input) {
		proxy.acceptConversationInput(input);
	}

	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		return proxy.addAttachment(arg0, arg1);
	}

	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
		return proxy.addAttachment(arg0, arg1, arg2, arg3);
	}

	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
		return proxy.addAttachment(arg0, arg1, arg2);
	}

	public PermissionAttachment addAttachment(Plugin arg0) {
		return proxy.addAttachment(arg0);
	}

	public boolean addPotionEffect(PotionEffect effect, boolean force) {
		return proxy.addPotionEffect(effect, force);
	}

	public boolean addPotionEffect(PotionEffect effect) {
		return proxy.addPotionEffect(effect);
	}

	public boolean addPotionEffects(Collection<PotionEffect> effects) {
		return proxy.addPotionEffects(effects);
	}

	public void awardAchievement(Achievement achievement) {
		proxy.awardAchievement(achievement);
	}

	public boolean beginConversation(Conversation conversation) {
		return proxy.beginConversation(conversation);
	}

	public boolean canSee(Player player) {
		return proxy.canSee(player);
	}

	public void chat(String msg) {
		proxy.chat(msg);
	}

	public void closeInventory() {
		proxy.closeInventory();
	}

	public void damage(int amount, Entity source) {
		proxy.damage(amount, source);
	}

	public void damage(int amount) {
		proxy.damage(amount);
	}

	public boolean eject() {
		return proxy.eject();
	}

	public Collection<PotionEffect> getActivePotionEffects() {
		return proxy.getActivePotionEffects();
	}

	public InetSocketAddress getAddress() {
		return proxy.getAddress();
	}

	public boolean getAllowFlight() {
		return proxy.getAllowFlight();
	}

	public Location getBedSpawnLocation() {
		return proxy.getBedSpawnLocation();
	}

	public Location getCompassTarget() {
		return proxy.getCompassTarget();
	}

	public String getDisplayName() {
		return proxy.getDisplayName();
	}

	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return proxy.getEffectivePermissions();
	}

	public int getEntityId() {
		return proxy.getEntityId();
	}

	public float getExhaustion() {
		return proxy.getExhaustion();
	}

	public float getExp() {
		return proxy.getExp();
	}

	public int getExpToLevel() {
		return proxy.getExpToLevel();
	}

	public double getEyeHeight() {
		return proxy.getEyeHeight();
	}

	public double getEyeHeight(boolean ignoreSneaking) {
		return proxy.getEyeHeight(ignoreSneaking);
	}

	public Location getEyeLocation() {
		return proxy.getEyeLocation();
	}

	public float getFallDistance() {
		return proxy.getFallDistance();
	}

	public int getFireTicks() {
		return proxy.getFireTicks();
	}

	public long getFirstPlayed() {
		return proxy.getFirstPlayed();
	}

	public int getFoodLevel() {
		return proxy.getFoodLevel();
	}

	public GameMode getGameMode() {
		return proxy.getGameMode();
	}

	public int getHealth() {
		return proxy.getHealth();
	}

	public PlayerInventory getInventory() {
		return proxy.getInventory();
	}

	public ItemStack getItemInHand() {
		return proxy.getItemInHand();
	}

	public ItemStack getItemOnCursor() {
		return proxy.getItemOnCursor();
	}

	public Player getKiller() {
		return proxy.getKiller();
	}

	public int getLastDamage() {
		return proxy.getLastDamage();
	}

	public EntityDamageEvent getLastDamageCause() {
		return proxy.getLastDamageCause();
	}

	public long getLastPlayed() {
		return proxy.getLastPlayed();
	}

	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance) {
		return proxy.getLastTwoTargetBlocks(transparent, maxDistance);
	}

	public int getLevel() {
		return proxy.getLevel();
	}

	public List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance) {
		return proxy.getLineOfSight(transparent, maxDistance);
	}

	public Set<String> getListeningPluginChannels() {
		return proxy.getListeningPluginChannels();
	}

	public Location getLocation() {
		return proxy.getLocation();
	}

	public int getMaxFireTicks() {
		return proxy.getMaxFireTicks();
	}

	public int getMaxHealth() {
		return proxy.getMaxHealth();
	}

	public int getMaximumAir() {
		return proxy.getMaximumAir();
	}

	public int getMaximumNoDamageTicks() {
		return proxy.getMaximumNoDamageTicks();
	}

	public List<MetadataValue> getMetadata(String metadataKey) {
		return proxy.getMetadata(metadataKey);
	}

	public String getName() {
		return proxy.getName();
	}

	public List<Entity> getNearbyEntities(double x, double y, double z) {
		return proxy.getNearbyEntities(x, y, z);
	}

	public int getNoDamageTicks() {
		return proxy.getNoDamageTicks();
	}

	public InventoryView getOpenInventory() {
		return proxy.getOpenInventory();
	}

	public Entity getPassenger() {
		return proxy.getPassenger();
	}

	public Player getPlayer() {
		return proxy.getPlayer();
	}

	public String getPlayerListName() {
		return proxy.getPlayerListName();
	}

	public long getPlayerTime() {
		return proxy.getPlayerTime();
	}

	public long getPlayerTimeOffset() {
		return proxy.getPlayerTimeOffset();
	}

	public int getRemainingAir() {
		return proxy.getRemainingAir();
	}

	public float getSaturation() {
		return proxy.getSaturation();
	}

	public Server getServer() {
		return proxy.getServer();
	}

	public int getSleepTicks() {
		return proxy.getSleepTicks();
	}

	public Block getTargetBlock(HashSet<Byte> transparent, int maxDistance) {
		return proxy.getTargetBlock(transparent, maxDistance);
	}

	public int getTicksLived() {
		return proxy.getTicksLived();
	}

	public int getTotalExperience() {
		return proxy.getTotalExperience();
	}

	public EntityType getType() {
		return proxy.getType();
	}

	public UUID getUniqueId() {
		return proxy.getUniqueId();
	}

	public Entity getVehicle() {
		return proxy.getVehicle();
	}

	public Vector getVelocity() {
		return proxy.getVelocity();
	}

	public World getWorld() {
		return proxy.getWorld();
	}

	public void giveExp(int amount) {
		proxy.giveExp(amount);
	}

	public boolean hasLineOfSight(Entity arg0) {
		return proxy.hasLineOfSight(arg0);
	}

	public boolean hasMetadata(String metadataKey) {
		return proxy.hasMetadata(metadataKey);
	}

	public boolean hasPermission(Permission arg0) {
		return proxy.hasPermission(arg0);
	}

	public boolean hasPermission(String arg0) {
		return proxy.hasPermission(arg0);
	}

	public boolean hasPlayedBefore() {
		return proxy.hasPlayedBefore();
	}

	public boolean hasPotionEffect(PotionEffectType type) {
		return proxy.hasPotionEffect(type);
	}

	public void hidePlayer(Player player) {
		proxy.hidePlayer(player);
	}

	public void incrementStatistic(Statistic statistic, int amount) {
		proxy.incrementStatistic(statistic, amount);
	}

	public void incrementStatistic(Statistic statistic, Material material, int amount) {
		proxy.incrementStatistic(statistic, material, amount);
	}

	public void incrementStatistic(Statistic statistic, Material material) {
		proxy.incrementStatistic(statistic, material);
	}

	public void incrementStatistic(Statistic statistic) {
		proxy.incrementStatistic(statistic);
	}

	public boolean isBanned() {
		return proxy.isBanned();
	}

	public boolean isBlocking() {
		return proxy.isBlocking();
	}

	public boolean isConversing() {
		return proxy.isConversing();
	}

	public boolean isDead() {
		return proxy.isDead();
	}

	public boolean isEmpty() {
		return proxy.isEmpty();
	}

	public boolean isFlying() {
		return proxy.isFlying();
	}

	public boolean isInsideVehicle() {
		return proxy.isInsideVehicle();
	}

	public boolean isOnline() {
		return proxy.isOnline();
	}

	public boolean isOp() {
		return proxy.isOp();
	}

	public boolean isPermissionSet(Permission arg0) {
		return proxy.isPermissionSet(arg0);
	}

	public boolean isPermissionSet(String arg0) {
		return proxy.isPermissionSet(arg0);
	}

	public boolean isPlayerTimeRelative() {
		return proxy.isPlayerTimeRelative();
	}

	public boolean isSleeping() {
		return proxy.isSleeping();
	}

	public boolean isSleepingIgnored() {
		return proxy.isSleepingIgnored();
	}

	public boolean isSneaking() {
		return proxy.isSneaking();
	}

	public boolean isSprinting() {
		return proxy.isSprinting();
	}

	public boolean isValid() {
		return proxy.isValid();
	}

	public boolean isWhitelisted() {
		return proxy.isWhitelisted();
	}

	public void kickPlayer(String message) {
		proxy.kickPlayer(message);
	}

	public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
		return proxy.launchProjectile(projectile);
	}

	public boolean leaveVehicle() {
		return proxy.leaveVehicle();
	}

	public void loadData() {
		proxy.loadData();
	}

	public InventoryView openEnchanting(Location location, boolean force) {
		return proxy.openEnchanting(location, force);
	}

	public InventoryView openInventory(Inventory inventory) {
		return proxy.openInventory(inventory);
	}

	public void openInventory(InventoryView inventory) {
		proxy.openInventory(inventory);
	}

	public InventoryView openWorkbench(Location location, boolean force) {
		return proxy.openWorkbench(location, force);
	}

	public boolean performCommand(String command) {
		return proxy.performCommand(command);
	}

	public void playEffect(EntityEffect type) {
		proxy.playEffect(type);
	}

	public void playEffect(Location loc, Effect effect, int data) {
		proxy.playEffect(loc, effect, data);
	}

	public <T> void playEffect(Location loc, Effect effect, T data) {
		proxy.playEffect(loc, effect, data);
	}

	public void playNote(Location loc, byte instrument, byte note) {
		proxy.playNote(loc, instrument, note);
	}

	public void playNote(Location loc, Instrument instrument, Note note) {
		proxy.playNote(loc, instrument, note);
	}

	public void recalculatePermissions() {
		proxy.recalculatePermissions();
	}

	public void remove() {
		proxy.remove();
	}

	public void removeAttachment(PermissionAttachment arg0) {
		proxy.removeAttachment(arg0);
	}

	public void removeMetadata(String metadataKey, Plugin owningPlugin) {
		proxy.removeMetadata(metadataKey, owningPlugin);
	}

	public void removePotionEffect(PotionEffectType type) {
		proxy.removePotionEffect(type);
	}

	public void resetPlayerTime() {
		proxy.resetPlayerTime();
	}

	public void saveData() {
		proxy.saveData();
	}

	public void sendBlockChange(Location loc, int material, byte data) {
		proxy.sendBlockChange(loc, material, data);
	}

	public void sendBlockChange(Location loc, Material material, byte data) {
		proxy.sendBlockChange(loc, material, data);
	}

	public boolean sendChunkChange(Location loc, int sx, int sy, int sz, byte[] data) {
		return proxy.sendChunkChange(loc, sx, sy, sz, data);
	}

	public void sendMap(MapView map) {
		proxy.sendMap(map);
	}

	public void sendMessage(String message) {
	}

	public void sendMessage(String[] messages) {
	}

	public void sendPluginMessage(Plugin source, String channel, byte[] message) {
		proxy.sendPluginMessage(source, channel, message);
	}

	public void sendRawMessage(String message) {
		proxy.sendRawMessage(message);
	}

	public Map<String, Object> serialize() {
		return proxy.serialize();
	}

	public void setAllowFlight(boolean flight) {
		proxy.setAllowFlight(flight);
	}

	public void setBanned(boolean banned) {
		proxy.setBanned(banned);
	}

	public void setBedSpawnLocation(Location location) {
		proxy.setBedSpawnLocation(location);
	}

	public void setCompassTarget(Location loc) {
		proxy.setCompassTarget(loc);
	}

	public void setDisplayName(String name) {
		proxy.setDisplayName(name);
	}

	public void setExhaustion(float value) {
		proxy.setExhaustion(value);
	}

	public void setExp(float exp) {
		proxy.setExp(exp);
	}

	public void setFallDistance(float distance) {
		proxy.setFallDistance(distance);
	}

	public void setFireTicks(int ticks) {
		proxy.setFireTicks(ticks);
	}

	public void setFlying(boolean value) {
		proxy.setFlying(value);
	}

	public void setFoodLevel(int value) {
		proxy.setFoodLevel(value);
	}

	public void setGameMode(GameMode mode) {
		proxy.setGameMode(mode);
	}

	public void setHealth(int health) {
		proxy.setHealth(health);
	}

	public void setItemInHand(ItemStack item) {
		proxy.setItemInHand(item);
	}

	public void setItemOnCursor(ItemStack item) {
		proxy.setItemOnCursor(item);
	}

	public void setLastDamage(int damage) {
		proxy.setLastDamage(damage);
	}

	public void setLastDamageCause(EntityDamageEvent event) {
		proxy.setLastDamageCause(event);
	}

	public void setLevel(int level) {
		proxy.setLevel(level);
	}

	public void setMaximumAir(int ticks) {
		proxy.setMaximumAir(ticks);
	}

	public void setMaximumNoDamageTicks(int ticks) {
		proxy.setMaximumNoDamageTicks(ticks);
	}

	public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
		proxy.setMetadata(metadataKey, newMetadataValue);
	}

	public void setNoDamageTicks(int ticks) {
		proxy.setNoDamageTicks(ticks);
	}

	public void setOp(boolean arg0) {
		proxy.setOp(arg0);
	}

	public boolean setPassenger(Entity passenger) {
		return proxy.setPassenger(passenger);
	}

	public void setPlayerListName(String name) {
		proxy.setPlayerListName(name);
	}

	public void setPlayerTime(long time, boolean relative) {
		proxy.setPlayerTime(time, relative);
	}

	public void setRemainingAir(int ticks) {
		proxy.setRemainingAir(ticks);
	}

	public void setSaturation(float value) {
		proxy.setSaturation(value);
	}

	public void setSleepingIgnored(boolean isSleeping) {
		proxy.setSleepingIgnored(isSleeping);
	}

	public void setSneaking(boolean sneak) {
		proxy.setSneaking(sneak);
	}

	public void setSprinting(boolean sprinting) {
		proxy.setSprinting(sprinting);
	}

	public void setTicksLived(int value) {
		proxy.setTicksLived(value);
	}

	public void setTotalExperience(int exp) {
		proxy.setTotalExperience(exp);
	}

	public void setVelocity(Vector velocity) {
		proxy.setVelocity(velocity);
	}

	public void setWhitelisted(boolean value) {
		proxy.setWhitelisted(value);
	}

	public boolean setWindowProperty(Property prop, int value) {
		return proxy.setWindowProperty(prop, value);
	}

	@SuppressWarnings("deprecation")
	public Arrow shootArrow() {
		return proxy.shootArrow();
	}

	public void showPlayer(Player player) {
		proxy.showPlayer(player);
	}

	public boolean teleport(Entity destination, TeleportCause cause) {
		return proxy.teleport(destination, cause);
	}

	public boolean teleport(Entity destination) {
		return proxy.teleport(destination);
	}

	public boolean teleport(Location location, TeleportCause cause) {
		return proxy.teleport(location, cause);
	}

	public boolean teleport(Location location) {
		return proxy.teleport(location);
	}

	@SuppressWarnings("deprecation")
	public Egg throwEgg() {
		return proxy.throwEgg();
	}

	@SuppressWarnings("deprecation")
	public Snowball throwSnowball() {
		return proxy.throwSnowball();
	}

	@SuppressWarnings("deprecation")
	public void updateInventory() {
		proxy.updateInventory();
	}

	public float getFlySpeed() {
		return proxy.getFlySpeed();
	}

	public float getWalkSpeed() {
		return proxy.getWalkSpeed();
	}

	public void setFlySpeed(float arg0) throws IllegalArgumentException {
		proxy.setFlySpeed(arg0);
	}

	public void setWalkSpeed(float arg0) throws IllegalArgumentException {
		proxy.setWalkSpeed(arg0);
	}

}
