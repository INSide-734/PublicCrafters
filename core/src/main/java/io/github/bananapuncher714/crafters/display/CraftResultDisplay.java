package io.github.bananapuncher714.crafters.display;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bananapuncher714.crafters.PublicCrafters;
import io.github.bananapuncher714.crafters.util.Utils;
import io.github.bananapuncher714.nbteditor.NBTEditor;

/**
 * This isn't completely reliable, as even though the item still exists, the item can disappear when shoved into lava by the client
 * 
 * @author BananaPuncher714
 */
public class CraftResultDisplay extends AbstractItemDisplay {
	protected final static Set< UUID > REGISTERED_ITEMS = new HashSet< UUID >();
	
	private UUID uuid;
	private UUID itemUUID;
	private double height;
	
	public CraftResultDisplay( CraftDisplay container, double height, ItemStack item ) {
		super( container, item, 10 );
		
		this.height = height;
	}
	
	@Override
	public void init() {
	    Location location = getCraftDisplay().getLocation().clone().add( .5, 1 + height, .5 );
		ArmorStand armorstand = getModelStand( location );
		uuid = armorstand.getUniqueId();
		
		Item itemDisplay = location.getWorld().dropItem( location, item );
		itemUUID = itemDisplay.getUniqueId();
		itemDisplay.setPickupDelay( Integer.MAX_VALUE );
		if ( PublicCrafters.getInstance().isShowResultName() ) {
			ItemMeta meta = item.getItemMeta();
			if ( meta.hasDisplayName() ) {
				itemDisplay.setCustomName( meta.getDisplayName() );
				itemDisplay.setCustomNameVisible( true );
			}
		}
		NBTEditor.set( itemDisplay, -32768, "Age" );
		NBTEditor.set( itemDisplay, ( byte ) 1, "Invulnerable" );
		
		armorstand.setPassenger( itemDisplay );
		
		REGISTERED_ITEMS.add( itemUUID );
	}
	
	@Override
	public void remove() {
	    World world = getCraftDisplay().getLocation().getWorld();
		Entity display = Utils.getEntityByUUID( uuid, world );
		if ( display != null ) {
			display.remove();
		}
		Entity item = Utils.getEntityByUUID( itemUUID, world );
		if ( item != null ) {
			item.remove();
		}
		
		REGISTERED_ITEMS.remove( itemUUID );
	}
	
	/**
	 * This is mostly for internal purposes, it creates a non-marker armorstand that is invulnerable and has all interactions disabled
	 * @param location
	 * The location where to spawn the stand
	 * @return
	 * The spawned and edited armorstand
	 */
	protected static ArmorStand getModelStand( Location location ) {
		ArmorStand model = ( ArmorStand ) location.getWorld().spawnEntity( location, EntityType.ARMOR_STAND );
		model.setVisible( false );
		model.setGravity( false );
		model.setSmall( true );
		model.setMarker( true );
		NBTEditor.set( model, 1, "DisabledSlots" );
		NBTEditor.set( model, ( byte ) 1, "Invulnerable" );
		return model;
	}
	
	public static boolean isRegistered( UUID uuid ) {
		return REGISTERED_ITEMS.contains( uuid );
	}
}
