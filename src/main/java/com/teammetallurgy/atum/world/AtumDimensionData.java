package com.teammetallurgy.atum.world;

import com.teammetallurgy.atum.Atum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AtumDimensionData extends SavedData {
    public static final String ID = "atum_dimension_data";
    private boolean hasStartStructureSpawned;
    private boolean isStorming;
    private final List<BoundingBox> beatenPyramids = new ArrayList<>();

    public static AtumDimensionData load(@Nonnull CompoundTag nbt) {
        AtumDimensionData data = new AtumDimensionData();
        data.hasStartStructureSpawned = nbt.getBoolean("HasStartStructureSpawned");
        data.isStorming = nbt.getBoolean("IsStorming");

        if (nbt.contains("PyramidBoxes", Tag.TAG_INT_ARRAY)) {
            data.readPyramidBox(nbt.getIntArray("PyramidBoxes"));
        } else {
            ListTag boxes = nbt.getList("PyramidBoxes", Tag.TAG_INT_ARRAY);
            for (int i = 0; i < boxes.size(); i++) {
                data.readPyramidBox(boxes.getIntArray(i));
            }
        }

        return data;
    }

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag nbt) {
        nbt.putBoolean("HasStartStructureSpawned", this.hasStartStructureSpawned);
        nbt.putBoolean("IsStorming", this.isStorming);

        ListTag boxes = new ListTag();
        for (BoundingBox box : this.beatenPyramids) {
            boxes.add(new IntArrayTag(new int[] {
                    box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()
            }));
        }
        if (!boxes.isEmpty()) {
            nbt.put("PyramidBoxes", boxes);
        } else {
            nbt.remove("PyramidBoxes");
        }
        return nbt;
    }

    private void readPyramidBox(int[] bounds) {
        if (bounds.length == 6) {
            this.beatenPyramids.add(new BoundingBox(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]));
        } else {
            Atum.LOG.warn("Skipping pyramid bounds with {} coordinates", bounds.length);
        }
    }

    public boolean hasStartStructureSpawned() {
        return this.hasStartStructureSpawned;
    }

    public boolean isStorming() {
        return this.isStorming;
    }

    public List<BoundingBox> getBeatenPyramids() {
        return this.beatenPyramids;
    }

    public void setHasStartStructureSpawned(boolean hasStartStructureSpawned) {
        this.hasStartStructureSpawned = hasStartStructureSpawned;
        this.setDirty();
    }

    public void setStorming(boolean isStorming) {
        this.isStorming = isStorming;
        this.setDirty();
    }

    public void addBeatenPyramid(BoundingBox box) {
        this.beatenPyramids.add(box);
        this.setDirty();
    }
}
