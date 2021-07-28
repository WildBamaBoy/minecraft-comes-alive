package mca.resources;

import mca.client.resources.ColorPaletteLoader;
import mca.client.resources.ColorPalette;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.util.Identifier;

public class FabricColorPaletteLoader extends ColorPaletteLoader implements IdentifiableResourceReloadListener {

    public FabricColorPaletteLoader(ColorPalette palette) {
        super(palette);
    }

    @Override
    public Identifier getFabricId() {
        return palette.getId();
    }
}
