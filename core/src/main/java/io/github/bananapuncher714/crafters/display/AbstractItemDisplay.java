package io.github.bananapuncher714.crafters.display;

import org.bukkit.inventory.ItemStack;

/**
 * The basic unit that is responsible for managing the items that appear on the crafting table
 * If you want to change what appears, you should extend this class and listen for the ItemDisplayCreateEvent;
 * Created on 2026-05-31
 *  
 * @author BananaPuncher714
 *
 */
public abstract class AbstractItemDisplay {
	protected final int slot;
	protected final ItemStack item;
	protected final CraftDisplay parent;
	
	public AbstractItemDisplay( CraftDisplay container, ItemStack item, int slot ) {
		this.item = item;
		this.slot = slot;
        this.parent = container;
	}
	
	/**
	 * This is the important method when creating a subclass.
	 */
	public abstract void init();

	/**
	 * This is a method you will want to override too, it is called whenever an ItemDisplay is no longer needed, and on server stop.
	 */
	public abstract void remove();
	
	public ItemStack getItem() {
		return item;
	}
	
	public CraftDisplay getCraftDisplay() {
		return parent;
	}
	
	public int getSlot() {
		return slot;
	}
}
