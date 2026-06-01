package io.github.bananapuncher714.crafters.implementation.v26_1;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import io.github.bananapuncher714.crafters.PublicCrafters;
import io.github.bananapuncher714.crafters.display.AbstractItemDisplay;
import io.github.bananapuncher714.crafters.events.ItemDisplayCreateEvent;
import io.github.bananapuncher714.crafters.events.ItemResultDisplayCreateEvent;

public class VirtualDisplayListener implements Listener {
    private final PublicCrafters plugin;
    
    protected VirtualDisplayListener( PublicCrafters plugin ) {
        this.plugin = plugin;
    }
    
    @EventHandler
    private void onPlayerJoinEvent( PlayerJoinEvent event ) {
        if ( !plugin.isVirtual() ) {
            return;
        }
        Player player = event.getPlayer();
        
        ModernVirtualResultDisplay.spawnAll( player );
        ModernVirtualItemDisplay.spawnAll( player );
    }
    
    @EventHandler
    private void onPlayerChangeWorldEvent( PlayerChangedWorldEvent event ) {
        if ( !plugin.isVirtual() ) {
            return;
        }
        Player player = event.getPlayer();
        ModernVirtualResultDisplay.despawnAll( event.getFrom(), player );
        ModernVirtualResultDisplay.spawnAll( player );
        ModernVirtualItemDisplay.despawnAll( event.getFrom(), player );
        ModernVirtualItemDisplay.spawnAll( player );
    }
    
    @EventHandler
    private void onItemDisplayCreateEvent( ItemDisplayCreateEvent event ) {
        if ( !plugin.isVirtual() ) {
            return;
        }
        
        AbstractItemDisplay display = event.getItemDisplay();
        ModernVirtualItemDisplay vDisplay = new ModernVirtualItemDisplay( display.getCraftDisplay(), display.getLocation(), display.getItem(), display.getSlot() );
        event.setItemDisplay( vDisplay );
    }
    
    @EventHandler
    private void onItemResultDisplayCreateEvent( ItemResultDisplayCreateEvent event ) {
        if ( !plugin.isVirtual() ) {
            return;
        }
        AbstractItemDisplay display = event.getItemDisplay();
        ModernVirtualResultDisplay vDisplay = new ModernVirtualResultDisplay( display.getCraftDisplay(), display.getLocation(), display.getItem() );
        event.setCraftResultDisplay( vDisplay );
    }
}
