package com.lauriethefish.betterportals.bukkit.portal.blockarray;

import com.lauriethefish.betterportals.bukkit.BetterPortals;
import com.lauriethefish.betterportals.bukkit.Config;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import lombok.Getter;

class BlockStateArray {
    private Config config;
    
    @Getter private boolean[] occlusion = null; // Array of which blocks fully block out light
    @Getter private Material[] materials = null; // Array of combined block IDs
    @Getter private byte[] dataValues = null;

    public BlockStateArray(BetterPortals pl) {
        this.config = pl.config;
    }

    boolean initialise() {
        if(occlusion != null) {return false;} // If we've already initialised both arrays, return false
        
        // Otherwise, initialise and return true
        occlusion = new boolean[config.totalArrayLength];
        materials = new Material[config.totalArrayLength];
        dataValues = new byte[config.totalArrayLength];
        return true;
    }

    // Updates the arrays at the location and index. Returns true if the block changed
    @SuppressWarnings("deprecation")
    boolean update(Location loc, int index) {
        Block block = loc.getBlock();

        Material material = block.getType();
        byte data = block.getData();
        // If it has changed
        if(materials[index] != material || dataValues[index] != data) {
            // Update the occlusion and combined ID arrays
            materials[index] = material;
            dataValues[index] = data;
            occlusion[index] = block.getType().isOccluding();
            return true;
        }   else    {
            return false;
        }
    }
}
