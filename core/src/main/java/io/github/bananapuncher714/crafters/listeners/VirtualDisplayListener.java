package io.github.bananapuncher714.crafters.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import io.github.bananapuncher714.crafters.PublicCrafters;
import io.github.bananapuncher714.crafters.display.AbstractItemDisplay;
import io.github.bananapuncher714.crafters.display.VirtualCraftResultDisplay;
import io.github.bananapuncher714.crafters.display.VirtualItemDisplay;
import io.github.bananapuncher714.crafters.events.ItemDisplayCreateEvent;
import io.github.bananapuncher714.crafters.events.ItemResultDisplayCreateEvent;

public class VirtualDisplayListener implements Listener {
	private final PublicCrafters plugin;
	
	public VirtualDisplayListener( PublicCrafters plugin ) {
		this.plugin = plugin;
	}
	
	@EventHandler
	private void onPlayerJoinEvent( PlayerJoinEvent event ) {
		if ( !plugin.isVirtual() ) {
			return;
		}
		Player player = event.getPlayer();
		VirtualItemDisplay.spawnAll( player );
		VirtualCraftResultDisplay.spawnAll( player );
	}
	
	@EventHandler
	private void onPlayerChangeWorldEvent( PlayerChangedWorldEvent event ) {
		if ( !plugin.isVirtual() ) {
			return;
		}
		Player player = event.getPlayer();
		VirtualItemDisplay.despawnAll( event.getFrom(), player );
		VirtualItemDisplay.spawnAll( player );
		VirtualCraftResultDisplay.despawnAll( event.getFrom(), player );
		VirtualCraftResultDisplay.spawnAll( player );
	}
	
	@EventHandler( priority = EventPriority.HIGHEST )
	private void onItemDisplayCreateEvent( ItemDisplayCreateEvent event ) {
		if ( !plugin.isVirtual() ) {
			return;
		}
		
		AbstractItemDisplay display = event.getItemDisplay();
		VirtualItemDisplay vDisplay = new VirtualItemDisplay( display.getCraftDisplay(), display.getItem(), PublicCrafters.getInstance().getHeight(), display.getSlot() );
		event.setItemDisplay( vDisplay );
	}
	
	@EventHandler( priority = EventPriority.HIGHEST )
	private void onCraftResultCreateEvent( ItemResultDisplayCreateEvent event ) {
		if ( !plugin.isVirtual() ) {
			return;
		}
		AbstractItemDisplay display = event.getItemDisplay();
		VirtualCraftResultDisplay vDisplay = new VirtualCraftResultDisplay( display.getCraftDisplay(), PublicCrafters.getInstance().getResultHeight(), display.getItem() );
		event.setCraftResultDisplay( vDisplay );
	}
}
