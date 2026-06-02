package io.github.bananapuncher714.crafters.implementation.api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class InventoryData {
    public List< ItemStack > items;
    public BlockFace face;
    
    public InventoryData() {
        items = new ArrayList< ItemStack >();
        face = BlockFace.SELF;
    }
    
    public InventoryData( List< ItemStack > items ) {
        this( items, BlockFace.SELF );
    }
    
    public InventoryData( List< ItemStack > items, BlockFace face ) {
        this.items = items;
        this.face = face;
    }
}
