package kaptainwutax.minemap.ui.map;

import kaptainwutax.featureutils.Feature;
import kaptainwutax.minemap.ui.DrawInfo;
import kaptainwutax.minemap.ui.map.fragment.Fragment;
import kaptainwutax.minemap.ui.map.icon.*;
import kaptainwutax.seedutils.mc.pos.BPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IconManager {

    private final MapContext context;
    public Map<Feature<?, ?>, IconRenderer> renderers;

    public IconManager(MapContext context) {
        this.context = context;
        this.renderers = context.getFeatures().stream().collect(Collectors.toMap(f -> f, f -> NullIcon.INSTANCE));

        this.override(
                SpawnIcon::new, RegionIcon::new, MineshaftIcon::new,
                EndGatewayIcon::new, c -> new StrongholdIcon(c, 128)
        );
    }

    public MapContext getContext() {
        return this.context;
    }

    public IconManager clear() {
        return this.override(c -> NullIcon.INSTANCE);
    }

    @SafeVarargs
    public final IconManager override(Function<MapContext, IconRenderer>... renderers) {
        for(Function<MapContext, IconRenderer> factory: renderers) {
            IconRenderer renderer = factory.apply(this.getContext());

            for(Feature<?, ?> feature: new ArrayList<>(this.renderers.keySet())) {
                if(!renderer.isValidFeature(feature))continue;
                this.renderers.replace(feature, renderer);
            }
        }

        return this;
    }

    public List<BPos> getPositions(Feature<?, ?> feature, Fragment fragment) {
        List<BPos> positions = new ArrayList<>();
        this.renderers.get(feature).addPositions(feature, fragment, positions);
        return positions;
    }

    public void render(Graphics graphics, DrawInfo info, Feature<?, ?> feature, Fragment fragment, BPos pos) {
        this.renderers.get(feature).render(graphics, info, feature, fragment, pos);
    }

}