package mca.client.resources;

import java.io.IOException;

import mca.MCA;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.profiler.Profiler;

public class ColorPaletteLoader extends SinglePreparationResourceReloader<ColorPalette.Data> {

    protected final ColorPalette palette;

    public ColorPaletteLoader(ColorPalette palette) {
        this.palette = palette;
    }

    @Override
    protected ColorPalette.Data prepare(ResourceManager manager, Profiler profiler) {
        try (NativeImage img = NativeImage.read(manager.getResource(palette.getId()).getInputStream())) {
            return new ColorPalette.Data(
                    img.getWidth(),
                    img.getHeight(),
                    img.makePixelArray()
            );
        } catch (IOException e) {
            MCA.LOGGER.error("Failed to load color texture `{}`", palette.getId(), e);
        }
        return ColorPalette.EMPTY;
    }

    @Override
    protected void apply(ColorPalette.Data prepared, ResourceManager manager, Profiler profiler) {
        palette.data = prepared;
    }
}
