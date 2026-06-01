package io.github.bananapuncher714.crafters.implementation.v26_1;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import com.mojang.math.Transformation;

import io.github.bananapuncher714.crafters.display.CraftDisplay;
import io.github.bananapuncher714.crafters.display.ItemDisplay;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class ModernVirtualItemDisplay extends ItemDisplay {
    private static Map< Location, Entity > items = new HashMap< Location, Entity >();
    
    public ModernVirtualItemDisplay( CraftDisplay container, Location loc, ItemStack item, int slot ) {
        super( container, loc, item, slot );
    }

    @Override
    public void init() {
        for ( Player player : location.getWorld().getPlayers() ) {
            spawn( location, player, item, getSlot() );
        }
        if ( location.getWorld().getPlayers().isEmpty() ) {
            spawn( location, null, item, getSlot() );
        }
    }

    @Override
    public void remove() {
        for ( Player player : location.getWorld().getPlayers() ) {
            kill( player, items.get( location ) );
        }
        items.remove( location );
    }

    public static void spawnAll( Player player ) {
        for ( Entry< Location, Entity > entry : items.entrySet() ) {
            if ( entry.getKey().getWorld() == player.getWorld() ) {
                respawn( player, entry.getValue() );
            }
        }
    }

    public static void despawnAll( World world, Player player ) {
        for ( Entry< Location, Entity > entry : items.entrySet() ) {
            if ( entry.getKey().getWorld() == world ) {
                kill( player, entry.getValue() );
            }
        }
    }

    public static void spawn( Location loc, Player player, ItemStack item, int slot ) {
        Entity display;
        
        if ( !items.containsKey( loc ) ) {
            net.minecraft.world.entity.Display.ItemDisplay disp = new net.minecraft.world.entity.Display.ItemDisplay( EntityType.ITEM_DISPLAY, ( ( CraftWorld ) loc.getWorld() ).getHandle() );
            
            disp.setItemStack( CraftItemStack.asNMSCopy( item ) );
            disp.setPos( loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ() );
            disp.setBrightnessOverride( Brightness.FULL_BRIGHT );
            
            // It really shouldn't be this complicated to get the correct location from the previous display...
            // TODO Fix this and make the parent class use a more standardized location that makes more sense
            // that way we don't have to manually calculate it ourselves in case if something changes
            
            int row = slot / 3;
            int col = slot % 3;
            Matrix4f matrix = new Matrix4f().translate( 0.3125f + ( col * 0.1875f ), 0.125f, 0.3125f + ( row * 0.1875f ) ).scale( 0.1875f );
            
            if ( !item.getType().isBlock() ) {
                // Rotate the item so it's facing upwards
                matrix.rotate( ( float ) Math.toRadians( -90 ), 1, 0, 0 );
            }
            
            com.mojang.math.Transformation transformation = new com.mojang.math.Transformation( matrix );
            disp.setTransformation( new Transformation( transformation.translation(), transformation.leftRotation(), transformation.scale(), transformation.rightRotation() ) );
            display = disp;

            items.put( loc, display );
        } else {
            display = items.get( loc );
        }
        
        if ( player != null ) {
            respawn( player, display );
        }
    }
    
    private static void respawn( Player player, Entity item ) {
        ServerPlayer serverPlayer = ( ( CraftPlayer ) player ).getHandle();

        serverPlayer.connection.send( item.getAddEntityPacket( new ServerEntity( null, item, 0, false, null, null ) ) );
        item.refreshEntityData( serverPlayer );
    }

    public static void kill( Player player, Entity item ) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket( item.getId() );
        ( ( CraftPlayer ) player ).getHandle().connection.send( packet );
    }
}
