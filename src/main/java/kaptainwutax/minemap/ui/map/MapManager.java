package kaptainwutax.minemap.ui.map;

import kaptainwutax.mathutils.util.Mth;
import kaptainwutax.minemap.init.Configs;
import kaptainwutax.minemap.listener.Events;
import kaptainwutax.seedutils.mc.pos.BPos;
import kaptainwutax.seedutils.util.math.Vec3i;

import java.awt.*;

public class MapManager {

    public static final int DEFAULT_REGION_SIZE = 512;

    private final MapPanel panel;
    public final int blocksPerFragment;
    public double pixelsPerFragment;

    public double centerX;
    public double centerY;

    public Point mousePointer;

    public MapManager(MapPanel panel) {
        this(panel, DEFAULT_REGION_SIZE);
    }

    public MapManager(MapPanel panel, int blocksPerFragment) {
        this.panel = panel;
        this.blocksPerFragment = blocksPerFragment;
        this.pixelsPerFragment = (int)(300.0D * (this.blocksPerFragment / DEFAULT_REGION_SIZE));

        this.panel.addMouseMotionListener(Events.Mouse.onDragged(e -> {
            int dx = e.getX() - this.mousePointer.x;
            int dy = e.getY() - this.mousePointer.y;
            this.mousePointer = e.getPoint();
            this.centerX += dx;
            this.centerY += dy;
            this.panel.repaint();
        }));

        this.panel.addMouseMotionListener(Events.Mouse.onMoved(e -> {
            BPos pos = this.getPos(e.getX(), e.getY());
            int x = pos.getX();
            int z = pos.getZ();

            this.panel.displayBar.tooltip.updateBiomeDisplay(x, z);

            this.panel.scheduler.forEachFragment(fragment -> {
                fragment.onHovered(pos.getX(), pos.getZ());
            });

            this.panel.repaint();
        }));

        this.panel.addMouseListener(Events.Mouse.onPressed(e -> {
            this.mousePointer = e.getPoint();
            this.panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }));

        this.panel.addMouseListener(Events.Mouse.onReleased(mouseEvent -> {
            this.panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }));

        this.panel.addMouseWheelListener(e -> {
            if(!e.isControlDown()) {
                double newPixelsPerFragment = this.pixelsPerFragment;

                if(e.getUnitsToScroll() > 0) {
                    newPixelsPerFragment /= e.getUnitsToScroll() / 2.0D;
                } else {
                    newPixelsPerFragment *= -e.getUnitsToScroll() / 2.0D;
                }

                if(newPixelsPerFragment > 2000.0D * (double)this.blocksPerFragment / DEFAULT_REGION_SIZE) {
                    newPixelsPerFragment = 2000.0D * (this.blocksPerFragment / 512.0D);
                }

                if(Configs.USER_PROFILE.getUserSettings().restrictMaximumZoom
                        && newPixelsPerFragment < 40.0D * (double)this.blocksPerFragment / DEFAULT_REGION_SIZE) {
                    newPixelsPerFragment = 40.0D * (this.blocksPerFragment / 512.0D);
                }

                double scaleFactor = newPixelsPerFragment / this.pixelsPerFragment;
                this.centerX *= scaleFactor;
                this.centerY *= scaleFactor;
                this.pixelsPerFragment = newPixelsPerFragment;
                this.panel.repaint();
            } else {
                int layerId = this.panel.getContext().getLayerId();
                layerId += e.getUnitsToScroll() < 0 ? 1 : -1;
                layerId = Mth.clamp(layerId, 0, this.panel.getContext().getBiomeSource().getLayerCount() - 1);

                if(this.panel.getContext().getLayerId() != layerId) {
                    this.panel.getContext().setLayerId(layerId);
                    this.panel.displayBar.settings.layerDropdown.selectIfPresent(layerId);
                    this.panel.restart();
                }
            }
        });
    }

    public Vec3i getScreenSize() {
        return new Vec3i(this.panel.getWidth(), 0, this.panel.getHeight());
    }

    public BPos getCenterPos() {
        Vec3i screenSize = this.getScreenSize();
        return getPos(screenSize.getX() / 2.0D, screenSize.getZ() / 2.0D);
    }

    public void setCenterPos(int blockX, int blockZ) {
        double scaleFactor = this.pixelsPerFragment / this.blocksPerFragment;
        this.centerX = -blockX * scaleFactor;
        this.centerY = -blockZ * scaleFactor;
        this.panel.repaint();
    }

    public BPos getPos(double mouseX, double mouseY) {
        Vec3i screenSize = this.getScreenSize();
        double x = (mouseX - screenSize.getX() / 2.0D - centerX) / screenSize.getX();
        double y = (mouseY - screenSize.getZ() / 2.0D - centerY) / screenSize.getZ();
        double blocksPerWidth = (screenSize.getX() / this.pixelsPerFragment) * (double)this.blocksPerFragment;
        double blocksPerHeight = (screenSize.getZ() / this.pixelsPerFragment) * (double)this.blocksPerFragment;
        x *= blocksPerWidth;
        y *= blocksPerHeight;
        int xi = (int)Math.round(x);
        int yi = (int)Math.round(y);
        return new BPos(xi, 0, yi);
    }

}
