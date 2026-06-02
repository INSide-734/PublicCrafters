package io.github.bananapuncher714.crafters;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import io.github.bananapuncher714.crafters.implementation.api.InventoryData;
import io.github.bananapuncher714.crafters.implementation.api.PublicCraftingInventory;

/**
 * A simple itemstack serializer and deserializer with world/chunk organization;
 * Created on 2017-12-09
 * 
 * @author BananaPuncher714
 *
 */
public final class CraftInventoryLoader {

	public static void save( File directory, PublicCraftingInventory inventory ){
		directory.mkdirs();
		Location location = inventory.getLocation();
		Chunk chunk = location.getChunk();
		int x = chunk.getX();
		int z = chunk.getZ();
		World world = location.getWorld();
		
		File saveLoc = new File( directory + "/" + world.getName() + "/" + x + "_" + z + "/" );
		saveLoc.mkdirs();
		
		File saveFile = new File( saveLoc + "/" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() );
		
		try {
			saveFile.delete();
			saveFile.createNewFile();
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration( saveFile );
		
		InventoryData data = inventory.getData();
		
		int index = 0;
		for ( ItemStack item : data.items ) {
			config.set( "items." + index++, item );
		}
		
		if ( data.face != BlockFace.SELF ) {
		    config.set( "face", data.face.toString() );
		}
		
		try {
			config.save( saveFile );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	public static InventoryData getData( File baseDir, Location location, boolean delete ) {
		Chunk chunk = location.getChunk();
		int x = chunk.getX();
		int z = chunk.getZ();
		World world = location.getWorld();
		
		File saveLoc = new File( baseDir + "/" + world.getName() + "/" + x + "_" + z + "/" );

		File saveFile = new File( saveLoc + "/" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() );
		
		return getData( saveFile, delete );
	}
	
	public static InventoryData getData( File file, boolean delete ) {
	    InventoryData data = new InventoryData();
		
		if ( !file.exists() ) {
			while ( data.items.size() < 9 ) {
			    data.items.add( new ItemStack( Material.AIR ) );
			}
			return data;
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration( file );
		
		for ( int index = 0; index < 9; index++ ) {
		    data.items.add( config.getItemStack( "items." + index ) );
		}
		
		data.face = BlockFace.valueOf( config.getString( "face", "SELF" ) );
		
		if ( delete ) {
			file.delete();
		}
		
		return data;
	}
	
	public static Map< Location, InventoryData > loadChunk( File baseDir, World world, int x, int z, boolean delete ) {
		Map< Location, InventoryData > itemMap = new HashMap< Location, InventoryData >();
		
		File saveLoc = new File( baseDir + "/" + world.getName() + "/" + x + "_" + z + "/" );
		
		if ( !saveLoc.exists() ) {
			return itemMap;
		}
		
		for ( File file : saveLoc.listFiles() ) {
			String[] locArray = file.getName().split( "_" );
			Location location = new Location( world, Integer.parseInt( locArray[ 0 ] ), Integer.parseInt( locArray[ 1 ] ), Integer.parseInt( locArray[ 2 ] ) );
			itemMap.put( location, getData( file, delete ) );
			if ( delete ) {
				file.delete();
			}
		}
		
		if ( delete ) {
			saveLoc.delete();
		}
		
		return itemMap;
	}
}
