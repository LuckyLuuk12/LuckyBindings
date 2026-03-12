package nl.kablan.luckybindings.config.option;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * A config option for selecting registered Minecraft blocks.
 */
public class BlockOption extends BaseOption<String> {
    public BlockOption(String name, String description, String defaultValue) {
        super(name, description, defaultValue);
    }

    @Override
    public String getType() {
        return "block";
    }

    /**
     * Get the Block represented by this option's value.
     * Returns `Blocks.AIR` if the block ID is invalid.
     */
    public Block getBlock() {
        try {
            Identifier id = Identifier.tryParse(getValue());
            if (id == null) {
                id = Identifier.withDefaultNamespace(getValue());
            }
            if (BuiltInRegistries.BLOCK.containsKey(id)) {
                return BuiltInRegistries.BLOCK.getValue(id);
            }
        } catch (Exception ignored) {
        }
        return Blocks.AIR;
    }

    /**
     * Check if the current value is a valid-registered block.
     */
    public boolean isValidBlock() {
        try {
            Identifier id = Identifier.tryParse(getValue());
            if (id == null) {
                id = Identifier.withDefaultNamespace(getValue());
            }
            return BuiltInRegistries.BLOCK.containsKey(id);
        } catch (Exception ignored) {
        }
        return false;
    }
}