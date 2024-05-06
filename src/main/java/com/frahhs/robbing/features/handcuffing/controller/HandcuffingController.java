package com.frahhs.robbing.features.handcuffing.controller;

import com.frahhs.robbing.Robbing;
import com.frahhs.robbing.features.handcuffing.events.ToggleHandcuffsEvent;
import com.frahhs.robbing.features.handcuffing.models.HandcuffingModel;
import com.frahhs.robbing.features.handcuffing.models.KidnappingModel;
import com.frahhs.robbing.items.RBMaterial;
import com.frahhs.robbing.managers.ConfigManager;
import com.frahhs.robbing.managers.MessagesManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller class for managing handcuffing actions.
 */
public class HandcuffingController {
    /** Map to store handcuffing cooldowns for each player. */
    public static Map<Player, Long> handcuffingCooldown = new HashMap<>();

    /**
     * Default constructor for HandcuffingController.
     */
    public HandcuffingController() {}

    /**
     * Puts handcuffs on a player.
     *
     * @param handcuffingModel The handcuffing model containing information about the handcuffing action.
     */
    public void putHandcuffs(HandcuffingModel handcuffingModel) {
        final ConfigManager configManager = Robbing.getInstance().getConfigManager();

        final Player handcuffer = handcuffingModel.getHandcuffer();
        final Player handcuffed = handcuffingModel.getHandcuffed();

        // Call toggle handcuffed event in server main thread
        Bukkit.getScheduler().runTask(Robbing.getPlugin(Robbing.class), () -> {
            ToggleHandcuffsEvent toggleHandcuffsEvent = new ToggleHandcuffsEvent(handcuffed, handcuffer, true);
            Bukkit.getPluginManager().callEvent(toggleHandcuffsEvent);

            // Check if event is cancelled
            if(toggleHandcuffsEvent.isCancelled())
                return;

            // Handcuffing stuff
            handcuffingModel.save();
            handcuffed.setAllowFlight(true);
            handcuffed.setGliding(false);

            // Send handcuffing messages...
            MessagesManager messagesManager = Robbing.getInstance().getMessagesManager();
            String message;
            message = messagesManager.getMessage("handcuffing.cuffed").replace("{player}", handcuffer.getDisplayName());
            handcuffed.sendMessage(message);
            message = messagesManager.getMessage("handcuffing.cuff").replace("{target}", handcuffed.getDisplayName());
            handcuffer.sendMessage(message);

            // Add handcuffing cooldown to handcuffer
            new Thread(() -> {
                try {
                    handcuffingCooldown.put(handcuffer, System.currentTimeMillis());
                    Thread.sleep( configManager.getInt("handcuffing.cooldown") * 1000L);
                    handcuffingCooldown.remove(handcuffer.getPlayer());
                } catch(InterruptedException v) {
                    System.out.println(v);
                }
            }).start();
        });
    }

    /**
     * Removes handcuffs from a player.
     *
     * @param handcuffed The player from whom to remove the handcuffs.
     * @param silent Indicates whether to send messages or not.
     */
    public void removeHandcuffs(Player handcuffed, boolean silent) {
        HandcuffingModel handcuffingModel = HandcuffingModel.getFromHandcuffed(handcuffed);
        final Player handcuffer = handcuffingModel.getHandcuffer();

        Bukkit.getScheduler().runTask(Robbing.getPlugin(Robbing.class), () -> {
            ToggleHandcuffsEvent toggleHandcuffsEvent = new ToggleHandcuffsEvent(handcuffed, handcuffer, false);
            Bukkit.getPluginManager().callEvent(toggleHandcuffsEvent);

            // Check if event is cancelled
            if(toggleHandcuffsEvent.isCancelled())
                return;

            // Handcuffing stuff
            handcuffingModel.remove();
            handcuffed.setAllowFlight(false);

            String message;
            MessagesManager messagesManager = Robbing.getInstance().getMessagesManager();
            // Send handcuffing messages...
            if(!silent) {
                message = messagesManager.getMessage("handcuffing.uncuffed").replace("{player}", handcuffer.getDisplayName());
                handcuffed.sendMessage(message);
                message = messagesManager.getMessage("handcuffing.uncuff").replace("{target}", handcuffed.getDisplayName());
                handcuffer.sendMessage(message);
            }

            // Check also if the target is in following mode and remove it
            KidnappingController kidnappingController = new KidnappingController();
            if(KidnappingModel.isKidnapped(handcuffed)) {
                kidnappingController.free(handcuffed);
                if(!silent) {
                    message = messagesManager.getMessage("follow.make_unfollow_cuffed");
                    message = message.replace("{target}", handcuffed.getDisplayName());
                    handcuffer.sendMessage(message);
                }
            }
        });
    }

    /**
     * Removes handcuffs from a player.
     *
     * @param handcuffed The player from whom to remove the handcuffs.
     */
    public void removeHandcuffs(Player handcuffed) {
        removeHandcuffs(handcuffed, false);
    }

    /**
     * Checks if a player is currently using handcuffs.
     *
     * @param p The player to check.
     * @return True if the player is using handcuffs, otherwise false.
     */
    public boolean isUsingHandcuffs(Player p) {
        // Handle if player have more than 1 handcuffs in hand
        int amount = p.getInventory().getItemInMainHand().getAmount();

        ItemStack handcuffsItemStack = Robbing.getInstance().getItemsManager().get(RBMaterial.HANDCUFFS);
        handcuffsItemStack.setAmount(amount);

        return p.getInventory().getItemInMainHand().equals(handcuffsItemStack);
    }

    /**
     * Checks if a player is handcuffed.
     *
     * @param handcuffed The player to check.
     * @return True if the player is handcuffed, otherwise false.
     */
    public boolean isHandcuffed(Player handcuffed) {
        return HandcuffingModel.isHandcuffed(handcuffed);
    }

    /**
     * Gets the player who handcuffed the specified player.
     *
     * @param handcuffed The player who is handcuffed.
     * @return The player who handcuffed the specified player.
     */
    public Player getHandcuffer(Player handcuffed) {
        if(!isHandcuffed(handcuffed))
            return null;

        HandcuffingModel handcuffingModel = HandcuffingModel.getFromHandcuffed(handcuffed);

        assert handcuffingModel != null;

        return handcuffingModel.getHandcuffer();
    }
}
