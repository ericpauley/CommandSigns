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
import org.bukkit.Sound;
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
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Creates a tapped link between an originator Player and a recipient Player
 * originator and recipient can be the same player Allows sendMessage() methods
 * to be intercepted if the silent flag is set
 */
public class CommandSignsPlayerProxy implements Player {

	private Player originator;
	private Player recipient;
	boolean silent;

	public CommandSignsPlayerProxy(Player originator) {
		this(originator, originator, false);
	}

	public CommandSignsPlayerProxy(Player originator, boolean silent) {
		this(originator, originator, silent);
	}

	public CommandSignsPlayerProxy(Player originator, Player recipient) {
		this(originator, recipient, false);
	}

	public CommandSignsPlayerProxy(Player originator, Player recipient,
			boolean silent) {
		this.originator = originator;
		this.recipient = recipient;
		this.silent = silent;
	}

	public void abandonConversation(Conversation conversation) {
		originator.abandonConversation(conversation);
	}

	public void abandonConversation(Conversation conversation,
			ConversationAbandonedEvent details) {
		originator.abandonConversation(conversation, details);
	}

	public void acceptConversationInput(String input) {
		originator.acceptConversationInput(input);
	}

	public PermissionAttachment addAttachment(Plugin arg0) {
		return originator.addAttachment(arg0);
	}

	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		return originator.addAttachment(arg0, arg1);
	}

	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2) {
		return originator.addAttachment(arg0, arg1, arg2);
	}

	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2, int arg3) {
		return originator.addAttachment(arg0, arg1, arg2, arg3);
	}

	public boolean addPotionEffect(PotionEffect effect) {
		return originator.addPotionEffect(effect);
	}

	public boolean addPotionEffect(PotionEffect effect, boolean force) {
		return originator.addPotionEffect(effect, force);
	}

	public boolean addPotionEffects(Collection<PotionEffect> effects) {
		return originator.addPotionEffects(effects);
	}

	public void awardAchievement(Achievement achievement) {
		originator.awardAchievement(achievement);
	}

	public boolean beginConversation(Conversation conversation) {
		return originator.beginConversation(conversation);
	}

	public boolean canSee(Player player) {
		return originator.canSee(player);
	}

	public void chat(String msg) {
		originator.chat(msg);
	}

	public void closeInventory() {
		originator.closeInventory();
	}

	public void damage(int amount) {
		originator.damage(amount);
	}

	public void damage(int amount, Entity source) {
		originator.damage(amount, source);
	}

	public boolean eject() {
		return originator.eject();
	}

	public Collection<PotionEffect> getActivePotionEffects() {
		return originator.getActivePotionEffects();
	}

	public InetSocketAddress getAddress() {
		return originator.getAddress();
	}

	public boolean getAllowFlight() {
		return originator.getAllowFlight();
	}

	public Location getBedSpawnLocation() {
		return originator.getBedSpawnLocation();
	}

	public Location getCompassTarget() {
		return originator.getCompassTarget();
	}

	public String getDisplayName() {
		return originator.getDisplayName();
	}

	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return originator.getEffectivePermissions();
	}

	public Inventory getEnderChest() {
		return originator.getEnderChest();
	}

	public int getEntityId() {
		return originator.getEntityId();
	}

	public float getExhaustion() {
		return originator.getExhaustion();
	}

	public float getExp() {
		return originator.getExp();
	}

	public int getExpToLevel() {
		return originator.getExpToLevel();
	}

	public double getEyeHeight() {
		return originator.getEyeHeight();
	}

	public double getEyeHeight(boolean ignoreSneaking) {
		return originator.getEyeHeight(ignoreSneaking);
	}

	public Location getEyeLocation() {
		return originator.getEyeLocation();
	}

	public float getFallDistance() {
		return originator.getFallDistance();
	}

	public int getFireTicks() {
		return originator.getFireTicks();
	}

	public long getFirstPlayed() {
		return originator.getFirstPlayed();
	}

	public float getFlySpeed() {
		return originator.getFlySpeed();
	}

	public int getFoodLevel() {
		return originator.getFoodLevel();
	}

	public GameMode getGameMode() {
		return originator.getGameMode();
	}

	public int getHealth() {
		return originator.getHealth();
	}

	public PlayerInventory getInventory() {
		return originator.getInventory();
	}

	public ItemStack getItemInHand() {
		return originator.getItemInHand();
	}

	public ItemStack getItemOnCursor() {
		return originator.getItemOnCursor();
	}

	public Player getKiller() {
		return originator.getKiller();
	}

	public int getLastDamage() {
		return originator.getLastDamage();
	}

	public EntityDamageEvent getLastDamageCause() {
		return originator.getLastDamageCause();
	}

	public long getLastPlayed() {
		return originator.getLastPlayed();
	}

	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> transparent,
			int maxDistance) {
		return originator.getLastTwoTargetBlocks(transparent, maxDistance);
	}

	public int getLevel() {
		return originator.getLevel();
	}

	public List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance) {
		return originator.getLineOfSight(transparent, maxDistance);
	}

	public Set<String> getListeningPluginChannels() {
		return originator.getListeningPluginChannels();
	}

	public Location getLocation() {
		return originator.getLocation();
	}

	public int getMaxFireTicks() {
		return originator.getMaxFireTicks();
	}

	public int getMaxHealth() {
		return originator.getMaxHealth();
	}

	public int getMaximumAir() {
		return originator.getMaximumAir();
	}

	public int getMaximumNoDamageTicks() {
		return originator.getMaximumNoDamageTicks();
	}

	public List<MetadataValue> getMetadata(String metadataKey) {
		return originator.getMetadata(metadataKey);
	}

	public String getName() {
		return originator.getName();
	}

	public List<Entity> getNearbyEntities(double x, double y, double z) {
		return originator.getNearbyEntities(x, y, z);
	}

	public int getNoDamageTicks() {
		return originator.getNoDamageTicks();
	}

	public InventoryView getOpenInventory() {
		return originator.getOpenInventory();
	}

	public Entity getPassenger() {
		return originator.getPassenger();
	}

	public Player getPlayer() {
		return originator.getPlayer();
	}

	public String getPlayerListName() {
		return originator.getPlayerListName();
	}

	public long getPlayerTime() {
		return originator.getPlayerTime();
	}

	public long getPlayerTimeOffset() {
		return originator.getPlayerTimeOffset();
	}

	public int getRemainingAir() {
		return originator.getRemainingAir();
	}

	public float getSaturation() {
		return originator.getSaturation();
	}

	public Server getServer() {
		return originator.getServer();
	}

	public int getSleepTicks() {
		return originator.getSleepTicks();
	}

	public Block getTargetBlock(HashSet<Byte> transparent, int maxDistance) {
		return originator.getTargetBlock(transparent, maxDistance);
	}

	public int getTicksLived() {
		return originator.getTicksLived();
	}

	public int getTotalExperience() {
		return originator.getTotalExperience();
	}

	public EntityType getType() {
		return originator.getType();
	}

	public UUID getUniqueId() {
		return originator.getUniqueId();
	}

	public Entity getVehicle() {
		return originator.getVehicle();
	}

	public Vector getVelocity() {
		return originator.getVelocity();
	}

	public float getWalkSpeed() {
		return originator.getWalkSpeed();
	}

	public World getWorld() {
		return originator.getWorld();
	}

	public void giveExp(int amount) {
		originator.giveExp(amount);
	}

	public boolean hasLineOfSight(Entity arg0) {
		return originator.hasLineOfSight(arg0);
	}

	public boolean hasMetadata(String metadataKey) {
		return originator.hasMetadata(metadataKey);
	}

	public boolean hasPermission(Permission arg0) {
		return originator.hasPermission(arg0);
	}

	public boolean hasPermission(String arg0) {
		return originator.hasPermission(arg0);
	}

	public boolean hasPlayedBefore() {
		return originator.hasPlayedBefore();
	}

	public boolean hasPotionEffect(PotionEffectType type) {
		return originator.hasPotionEffect(type);
	}

	public void hidePlayer(Player player) {
		originator.hidePlayer(player);
	}

	public void incrementStatistic(Statistic statistic) {
		originator.incrementStatistic(statistic);
	}

	public void incrementStatistic(Statistic statistic, int amount) {
		originator.incrementStatistic(statistic, amount);
	}

	public void incrementStatistic(Statistic statistic, Material material) {
		originator.incrementStatistic(statistic, material);
	}

	public void incrementStatistic(Statistic statistic, Material material,
			int amount) {
		originator.incrementStatistic(statistic, material, amount);
	}

	public boolean isBanned() {
		return originator.isBanned();
	}

	public boolean isBlocking() {
		return originator.isBlocking();
	}

	public boolean isConversing() {
		return originator.isConversing();
	}

	public boolean isDead() {
		return originator.isDead();
	}

	public boolean isEmpty() {
		return originator.isEmpty();
	}

	public boolean isFlying() {
		return originator.isFlying();
	}

	public boolean isInsideVehicle() {
		return originator.isInsideVehicle();
	}

	public boolean isOnline() {
		return originator.isOnline();
	}

	public boolean isOp() {
		return originator.isOp();
	}

	public boolean isPermissionSet(Permission arg0) {
		return originator.isPermissionSet(arg0);
	}

	public boolean isPermissionSet(String arg0) {
		return originator.isPermissionSet(arg0);
	}

	public boolean isPlayerTimeRelative() {
		return originator.isPlayerTimeRelative();
	}

	public boolean isSilent() {
		return silent;
	}

	public boolean isSleeping() {
		return originator.isSleeping();
	}

	public boolean isSleepingIgnored() {
		return originator.isSleepingIgnored();
	}

	public boolean isSneaking() {
		return originator.isSneaking();
	}

	public boolean isSprinting() {
		return originator.isSprinting();
	}

	public boolean isValid() {
		return originator.isValid();
	}

	public boolean isWhitelisted() {
		return originator.isWhitelisted();
	}

	public void kickPlayer(String message) {
		originator.kickPlayer(message);
	}

	public <T extends Projectile> T launchProjectile(
			Class<? extends T> projectile) {
		return originator.launchProjectile(projectile);
	}

	public boolean leaveVehicle() {
		return originator.leaveVehicle();
	}

	public void loadData() {
		originator.loadData();
	}

	public InventoryView openEnchanting(Location location, boolean force) {
		return originator.openEnchanting(location, force);
	}

	public InventoryView openInventory(Inventory inventory) {
		return originator.openInventory(inventory);
	}

	public void openInventory(InventoryView inventory) {
		originator.openInventory(inventory);
	}

	public InventoryView openWorkbench(Location location, boolean force) {
		return originator.openWorkbench(location, force);
	}

	public boolean performCommand(String command) {
		return originator.performCommand(command);
	}

	public void playEffect(EntityEffect type) {
		originator.playEffect(type);
	}

	public void playEffect(Location loc, Effect effect, int data) {
		originator.playEffect(loc, effect, data);
	}

	public <T> void playEffect(Location loc, Effect effect, T data) {
		originator.playEffect(loc, effect, data);
	}

	public void playNote(Location loc, byte instrument, byte note) {
		originator.playNote(loc, instrument, note);
	}

	public void playNote(Location loc, Instrument instrument, Note note) {
		originator.playNote(loc, instrument, note);
	}

	public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
		originator.playSound(arg0, arg1, arg2, arg3);
	}

	public void recalculatePermissions() {
		originator.recalculatePermissions();
	}

	public void remove() {
		originator.remove();
	}

	public void removeAttachment(PermissionAttachment arg0) {
		originator.removeAttachment(arg0);
	}

	public void removeMetadata(String metadataKey, Plugin owningPlugin) {
		originator.removeMetadata(metadataKey, owningPlugin);
	}

	public void removePotionEffect(PotionEffectType type) {
		originator.removePotionEffect(type);
	}

	public void resetPlayerTime() {
		originator.resetPlayerTime();
	}

	public void saveData() {
		originator.saveData();
	}

	public void sendBlockChange(Location loc, int material, byte data) {
		originator.sendBlockChange(loc, material, data);
	}

	public void sendBlockChange(Location loc, Material material, byte data) {
		originator.sendBlockChange(loc, material, data);
	}

	public boolean sendChunkChange(Location loc, int sx, int sy, int sz,
			byte[] data) {
		return originator.sendChunkChange(loc, sx, sy, sz, data);
	}

	public void sendMap(MapView map) {
		originator.sendMap(map);
	}

	public void sendMessage(String message) {
		if (!silent && recipient != null)
			recipient.sendMessage(message);
	}

	public void sendMessage(String[] messages) {
		if (!silent && recipient != null)
			recipient.sendMessage(messages);
	}

	public void sendPluginMessage(Plugin source, String channel, byte[] message) {
		originator.sendPluginMessage(source, channel, message);
	}

	public void sendRawMessage(String message) {
		if (!silent && recipient != null)
			recipient.sendRawMessage(message);
	}

	public Map<String, Object> serialize() {
		return originator.serialize();
	}

	public void setAllowFlight(boolean flight) {
		originator.setAllowFlight(flight);
	}

	public void setBanned(boolean banned) {
		originator.setBanned(banned);
	}

	public void setBedSpawnLocation(Location location) {
		originator.setBedSpawnLocation(location);
	}

	public void setCompassTarget(Location loc) {
		originator.setCompassTarget(loc);
	}

	public void setDisplayName(String name) {
		originator.setDisplayName(name);
	}

	public void setExhaustion(float value) {
		originator.setExhaustion(value);
	}

	public void setExp(float exp) {
		originator.setExp(exp);
	}

	public void setFallDistance(float distance) {
		originator.setFallDistance(distance);
	}

	public void setFireTicks(int ticks) {
		originator.setFireTicks(ticks);
	}

	public void setFlying(boolean value) {
		originator.setFlying(value);
	}

	public void setFlySpeed(float arg0) throws IllegalArgumentException {
		originator.setFlySpeed(arg0);
	}

	public void setFoodLevel(int value) {
		originator.setFoodLevel(value);
	}

	public void setGameMode(GameMode mode) {
		originator.setGameMode(mode);
	}

	public void setHealth(int health) {
		originator.setHealth(health);
	}

	public void setItemInHand(ItemStack item) {
		originator.setItemInHand(item);
	}

	public void setItemOnCursor(ItemStack item) {
		originator.setItemOnCursor(item);
	}

	public void setLastDamage(int damage) {
		originator.setLastDamage(damage);
	}

	public void setLastDamageCause(EntityDamageEvent event) {
		originator.setLastDamageCause(event);
	}

	public void setLevel(int level) {
		originator.setLevel(level);
	}

	public void setMaximumAir(int ticks) {
		originator.setMaximumAir(ticks);
	}

	public void setMaximumNoDamageTicks(int ticks) {
		originator.setMaximumNoDamageTicks(ticks);
	}

	public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
		originator.setMetadata(metadataKey, newMetadataValue);
	}

	public void setNoDamageTicks(int ticks) {
		originator.setNoDamageTicks(ticks);
	}

	public void setOp(boolean arg0) {
		originator.setOp(arg0);
	}

	public boolean setPassenger(Entity passenger) {
		return originator.setPassenger(passenger);
	}

	public void setPlayerListName(String name) {
		originator.setPlayerListName(name);
	}

	public void setPlayerTime(long time, boolean relative) {
		originator.setPlayerTime(time, relative);
	}

	public void setRemainingAir(int ticks) {
		originator.setRemainingAir(ticks);
	}

	public void setSaturation(float value) {
		originator.setSaturation(value);
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public void setSleepingIgnored(boolean isSleeping) {
		originator.setSleepingIgnored(isSleeping);
	}

	public void setSneaking(boolean sneak) {
		originator.setSneaking(sneak);
	}

	public void setSprinting(boolean sprinting) {
		originator.setSprinting(sprinting);
	}

	public void setTicksLived(int value) {
		originator.setTicksLived(value);
	}

	public void setTotalExperience(int exp) {
		originator.setTotalExperience(exp);
	}

	public void setVelocity(Vector velocity) {
		originator.setVelocity(velocity);
	}

	public void setWalkSpeed(float arg0) throws IllegalArgumentException {
		originator.setWalkSpeed(arg0);
	}

	public void setWhitelisted(boolean value) {
		originator.setWhitelisted(value);
	}

	public boolean setWindowProperty(Property prop, int value) {
		return originator.setWindowProperty(prop, value);
	}

	@SuppressWarnings("deprecation")
	public Arrow shootArrow() {
		return originator.shootArrow();
	}

	public void showPlayer(Player player) {
		originator.showPlayer(player);
	}

	public boolean teleport(Entity destination) {
		return originator.teleport(destination);
	}

	public boolean teleport(Entity destination, TeleportCause cause) {
		return originator.teleport(destination, cause);
	}

	public boolean teleport(Location location) {
		return originator.teleport(location);
	}

	public boolean teleport(Location location, TeleportCause cause) {
		return originator.teleport(location, cause);
	}

	@SuppressWarnings("deprecation")
	public Egg throwEgg() {
		return originator.throwEgg();
	}

	@SuppressWarnings("deprecation")
	public Snowball throwSnowball() {
		return originator.throwSnowball();
	}

	@SuppressWarnings("deprecation")
	public void updateInventory() {
		originator.updateInventory();
	}

}
