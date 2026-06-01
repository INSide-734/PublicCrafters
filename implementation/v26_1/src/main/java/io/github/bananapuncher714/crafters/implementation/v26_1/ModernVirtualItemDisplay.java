package io.github.bananapuncher714.crafters.implementation.v26_1;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftMetaBlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.joml.Matrix4f;

import com.mojang.math.Transformation;

import io.github.bananapuncher714.crafters.display.AbstractItemDisplay;
import io.github.bananapuncher714.crafters.display.CraftDisplay;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class ModernVirtualItemDisplay extends AbstractItemDisplay {
    private static Map< Location, Entity > items = new HashMap< Location, Entity >();
    
    protected Location location;
    
    public ModernVirtualItemDisplay( CraftDisplay container, ItemStack item, double height, int slot ) {
        super( container, item, slot );
        
        int row = slot / 3;
        int col = slot % 3;
        location = container.getLocation().clone().add( 0.3125 + ( col * 0.1875 ), 0.4375 + height, 0.3125 + ( row * 0.1875 ) );
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
            Display disp;
            Matrix4f matrix = new Matrix4f().scale( 0.1875f );
            
            ItemMeta meta = item.getItemMeta();
            if ( item.getType().isBlock() ) {
                net.minecraft.world.entity.Display.BlockDisplay blockDisp = new net.minecraft.world.entity.Display.BlockDisplay( EntityType.BLOCK_DISPLAY, ( ( CraftWorld ) loc.getWorld() ).getHandle() );
                if ( meta instanceof CraftMetaBlockState ) {
                    BlockState state = ( ( CraftMetaBlockState ) meta ).getBlockState();
                    blockDisp.setBlockState( ( ( CraftBlockState ) state ).getHandle() );
                } else {
                    blockDisp.setBlockState( CraftBlockType.bukkitToMinecraft( item.getType() ).defaultBlockState() );
                }
                disp = blockDisp;

                // Block displays start from the bottom left rather than the center
                matrix.rotate( ( float ) Math.PI, 0, 1, 0 );
                matrix.translate( -0.5f, -0.5f, -0.5f );
            } else {
                net.minecraft.world.entity.Display.ItemDisplay itemDisp = new net.minecraft.world.entity.Display.ItemDisplay( EntityType.ITEM_DISPLAY, ( ( CraftWorld ) loc.getWorld() ).getHandle() );
                itemDisp.setItemStack( CraftItemStack.asNMSCopy( item ) );
                disp = itemDisp;
                
                // Rotate the item so it's facing upwards
                matrix.rotate( ( float ) Math.toRadians( -90 ), 1, 0, 0 );
            }
            
            disp.setPos( loc.getX(), loc.getY(), loc.getZ() );
            disp.setBrightnessOverride( Brightness.FULL_BRIGHT );
            
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
