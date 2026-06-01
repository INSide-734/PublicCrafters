package io.github.bananapuncher714.crafters.implementation.v26_1;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bananapuncher714.crafters.PublicCrafters;
import io.github.bananapuncher714.crafters.display.AbstractItemDisplay;
import io.github.bananapuncher714.crafters.display.CraftDisplay;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

public class ModernVirtualResultDisplay extends AbstractItemDisplay {
    private static Map< Location, ItemEntity > items = new HashMap< Location, ItemEntity >();
    private static Map< Location, Entity > bases = new HashMap< Location, Entity >();
    
    public ModernVirtualResultDisplay( CraftDisplay container, Location loc, ItemStack item ) {
        super( container, loc, item, 10 );
    }
    
    @Override
    public void init() {
        for ( Player player : location.getWorld().getPlayers() ) {
            spawn( location, player, item );
        }
        if ( location.getWorld().getPlayers().isEmpty() ) {
            spawn( location, null, item );
        }
    }
    
    @Override
    public void remove() {
        for ( Player player : location.getWorld().getPlayers() ) {
            kill( location, player );
        }
        items.remove( location );
        bases.remove( location );
    }
    
    public static void spawnAll( Player player ) {
        for ( Location key : items.keySet() ) {
            if ( player.getWorld() == key.getWorld() ) {
                ItemEntity item = items.get( key );
                Entity base = bases.get( key );
                respawn( player, item, base );
            }
        }
    }

    public static void despawnAll( World world, Player player ) {
        for ( Location location : items.keySet() ) {
            if ( location.getWorld() == world ) {
                kill( location, player );
            }
        }
    }
    
    public static void spawn( Location loc, Player p, ItemStack item ) {
        ItemEntity itemEntity;
        Entity base;

        if ( !items.containsKey( loc ) ) {
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy( item );
            itemEntity = new ItemEntity( ( ( CraftWorld ) loc.getWorld() ).getHandle(), loc.getX(), loc.getY(), loc.getZ(), nmsItem );
            itemEntity.setInvulnerable( true );
            itemEntity.setNoGravity( true );
            if ( PublicCrafters.getInstance().isShowResultName() ) {
                ItemMeta meta = item.getItemMeta();
                if ( meta.hasDisplayName() ) {
                    // Display a custom name if it has one and is enabled in the config
                    itemEntity.setCustomName( nmsItem.getItemName() );
                    itemEntity.setCustomNameVisible( true );
                }
            }

            // Summon the area cloud 0.375 less so that the item appears 1/4 above the crafting table
            AreaEffectCloud cloud = new AreaEffectCloud( ( ( CraftWorld ) loc.getWorld() ).getHandle(), loc.getX(), loc.getY() - 0.375, loc.getZ() );
            cloud.setRadius( 0 );
            cloud.setRadiusOnUse( 0 );
            cloud.setRadiusPerTick( 0 );
            cloud.setDuration( -1 );
            cloud.setDurationOnUse( 0 );
            cloud.setInvulnerable( true );
            cloud.setInvisible( true );
            cloud.setNoGravity( true );
            base = cloud;
            
            itemEntity.startRiding( base );
            
            items.put( loc, itemEntity );
            bases.put( loc, base );
        } else {
            itemEntity = items.get( loc );
            base = bases.get( loc );
        }

        if ( p != null ) {
            respawn( p, itemEntity, base );
        }
    }

    private static void respawn( Player player, ItemEntity itemEntity, Entity base ) {
        ServerPlayer serverPlayer = ( ( CraftPlayer ) player ).getHandle();

        serverPlayer.connection.send( itemEntity.getAddEntityPacket( new ServerEntity( null, itemEntity, 0, false, null, null ) ) );
        itemEntity.refreshEntityData( serverPlayer );
        serverPlayer.connection.send( base.getAddEntityPacket( new ServerEntity( null, base, 0, false, null, null ) ) );
        base.refreshEntityData( serverPlayer );

        serverPlayer.connection.send( new ClientboundSetPassengersPacket( base ) );
    }
    
    public static void kill( Location location, Player player ) {
        if ( !items.containsKey( location ) ) {
            return;
        }

        ItemEntity item = items.get( location );
        Entity base = bases.get( location );
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket( item.getId(), base.getId() );
        ( ( CraftPlayer ) player ).getHandle().connection.send( packet );
    }
}
