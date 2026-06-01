package io.github.bananapuncher714.crafters.display;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.bananapuncher714.crafters.PublicCrafters;
import io.github.bananapuncher714.crafters.events.CraftDisplayDestroyEvent;
import io.github.bananapuncher714.crafters.events.CraftDisplayUpdateEvent;
import io.github.bananapuncher714.crafters.events.ItemDisplayCreateEvent;
import io.github.bananapuncher714.crafters.events.ItemDisplayDestroyEvent;
import io.github.bananapuncher714.crafters.implementation.api.PublicCraftingInventory;
import io.github.bananapuncher714.crafters.events.ItemDisplayEvent;
import io.github.bananapuncher714.crafters.events.ItemResultDisplayCreateEvent;
import io.github.bananapuncher714.crafters.events.ItemResultDisplayDestroyEvent;

/**
 * This is a per-inventory object, it manages all the 9 slots of a crafting table;
 * Created on 2017-12-07
 * 
 * @author BananaPuncher714
 */
public class CraftDisplay {
	protected final List< AbstractItemDisplay > displays = new ArrayList< AbstractItemDisplay >();
	protected AbstractItemDisplay resultDisplay;
	protected final Location blockLoc;
	protected final PublicCraftingInventory inventory;

	/**
	 * Provide a PublicCraftingInventory, and the default height is used
	 * 
	 * @param inventory
	 * It must be a {@link PublicCraftingInventory}, NOT your normal Bukkit inventory
	 */
	public CraftDisplay( PublicCraftingInventory inventory ) {
		blockLoc = inventory.getLocation();
		this.inventory = inventory;
		for ( int i = 0; i < 9; i++ ) {
		    displays.add( null );
		}
		updateDisplays( false );
	}

	/**
	 * All this really does is run the {@link #run()} method some ticks after this is called
	 */
	public void update( boolean force ) {	    
	    Bukkit.getScheduler().scheduleSyncDelayedTask( PublicCrafters.getInstance(), () -> { updateDisplays( force ); }, PublicCrafters.getInstance().getUpdateDelay() );
	}
	
	/**
	 * Non-forceful update
	 */
	public void update() {
	    update( false );
	}
	
	/**
	 * This forces an update immediately, without delay and forcefully.
	 */
	public void forceUpdate() {
		if ( inventory == null ) {
			return;
		}

		CraftDisplayUpdateEvent updateEvent = new CraftDisplayUpdateEvent( this );
		Bukkit.getPluginManager().callEvent( updateEvent );
		if ( updateEvent.isCancelled() ) {
			return;
		}

		for ( int i = 0; i < 3; i++ ) {
			for ( int j = 0; j < 3; j++ ) {
				update( i, j, true );
			}
		}
		
		updateResult();
	}
	
	/**
	 * This is the passive update method; see {@link #update(int, int, boolean)}
	 * 
	 * @param col
	 * The columns of a workbench, from left to right, counting 0 to 2
	 * @param row
	 * The rows of a workbench, from top to bottom, counting 0 to 2
	 */
	public void update( int col, int row ) {
		update( col, row, false );
	}
	
	/**
	 * Update the displays to show what item is being crafted, non-forcefully
	 */
	public void updateResult() {
	    updateResult( false );
	}
	
	/**
	 * Update the displays to show what item is being crafted
	 */
	public void updateResult( boolean force ) {
		ItemStack result = inventory.getResult();
		if ( result == null || result.getType() == Material.AIR ) {
			if ( resultDisplay != null ) {
				ItemDisplayEvent event = new ItemResultDisplayDestroyEvent( resultDisplay );
				Bukkit.getPluginManager().callEvent( event );
				resultDisplay.remove();
				resultDisplay = null;
			}
			return;
		}
		if ( resultDisplay != null ) {
			ItemStack resultItem = resultDisplay.getItem();
			if ( result.isSimilar( resultItem ) && !force ) {
				return;
			}
			
			ItemDisplayEvent destroyEvent = new ItemResultDisplayDestroyEvent( resultDisplay );
			Bukkit.getPluginManager().callEvent( destroyEvent );
			resultDisplay.remove();
			resultDisplay = null;
		}
		
		if ( PublicCrafters.getInstance().isShowResult() ) {
			resultDisplay = new CraftResultDisplay( this, PublicCrafters.getInstance().getResultHeight(), result );
			ItemResultDisplayCreateEvent createEvent = new ItemResultDisplayCreateEvent( resultDisplay, blockLoc.clone() );
			Bukkit.getPluginManager().callEvent( createEvent );
			if ( createEvent.isCancelled() ) {
				return;
			}
			
			resultDisplay = createEvent.getItemDisplay();
			resultDisplay.init();
		}
	}
	
	/**
	 * Don't call this from the ItemDisplayCreateEvent or the ItemDisplayDestroyEvent, as it will cause a stackOverflowException;
	 * 
	 * @param col
	 * The columns of a workbench, from left to right, counting 0 to 2
	 * @param row
	 * The rows of a workbench, from top to bottom, counting 0 to 2
	 * @param force
	 * Whether or not to force an update if the items in the current slot are similar
	 */
	public void update( int col, int row, boolean force ) {
		List< ItemStack > bukkitItems = inventory.getBukkitItems();
		int index = col + 3 * row;
		ItemStack item = bukkitItems.get( index );
		AbstractItemDisplay display = displays.get( index );
		if ( display != null && ( display.getItem().getType() == Material.AIR || item.getType() == Material.AIR ) ) {
			ItemDisplayDestroyEvent event = new ItemDisplayDestroyEvent( display );
			Bukkit.getPluginManager().callEvent( event );
			display.remove();
			displays.set( index, null );
			return;
		}
		if ( item.getType() == Material.AIR ) {
			return;
		}
		if ( force || display == null || !item.isSimilar( display.getItem() ) ) {
			display = new ItemDisplay( this, item, PublicCrafters.getInstance().getHeight(), index );
			ItemDisplayCreateEvent event = new ItemDisplayCreateEvent( blockLoc.clone(), display );
			Bukkit.getPluginManager().callEvent( event );
			if ( event.isCancelled() ) {
				displays.set( index, null );
				return;
			}
			display = event.getItemDisplay();
			
			// Remove the previous display
			AbstractItemDisplay prev = displays.set( index, display );
			if ( prev != null ) {
			    prev.remove();
			}

			display.init();
		}
	}
	
	/**
	 * Simply stops each of the 9 ItemDisplays this is responsible for
	 */
	public void stop() {
		Bukkit.getPluginManager().callEvent( new CraftDisplayDestroyEvent( this ) );
		for ( AbstractItemDisplay display : displays ) {
			if ( display != null ) {
				ItemDisplayDestroyEvent event = new ItemDisplayDestroyEvent( display );
				Bukkit.getPluginManager().callEvent( event );
				display.remove();
			}
		}
		
		if ( resultDisplay != null ) {
			resultDisplay.remove();
		}
	}

	public PublicCraftingInventory getInventory() {
		return inventory;
	}
	
	public List< AbstractItemDisplay > getItemDisplays() {
		return displays;
	}
	
	public Location getLocation() {
		return blockLoc;
	}
	
	private void updateDisplays( boolean force ) {
		if ( inventory == null ) {
			return;
		}

		CraftDisplayUpdateEvent updateEvent = new CraftDisplayUpdateEvent( this );
		Bukkit.getPluginManager().callEvent( updateEvent );
		if ( updateEvent.isCancelled() ) {
			return;
		}

		for ( int i = 0; i < 3; i++ ) {
			for ( int j = 0; j < 3; j++ ) {
				update( i, j, force );
			}
		}
		
		updateResult( force );
	}
}
