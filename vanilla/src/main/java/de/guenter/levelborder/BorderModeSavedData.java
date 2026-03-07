package de.guenter.levelborder;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.util.datafix.DataFixTypes;

public class BorderModeSavedData extends SavedData {
    private static final String TAG_BORDER_MODE = "levelBorderMode";
    public BorderMode borderMode;

    public BorderModeSavedData() {
        this.borderMode = BorderMode.OWN;
    }

    public BorderModeSavedData(BorderMode borderMode) {
        this.borderMode = borderMode;
    }

    public static BorderModeSavedData load(CompoundTag tag, HolderLookup.Provider registries) {        BorderMode mode = BorderMode.OWN;
        if (tag.contains(TAG_BORDER_MODE)) {
            try {
                mode = BorderMode.valueOf(tag.getString(TAG_BORDER_MODE));
            } catch (IllegalArgumentException e) {
                mode = BorderMode.OWN;
            }
        }
        return new BorderModeSavedData(mode);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString(TAG_BORDER_MODE, borderMode.name());
        return tag;
    }

    public static final SavedData.Factory<BorderModeSavedData> TYPE = new SavedData.Factory<>(
            BorderModeSavedData::new,
            BorderModeSavedData::load,
            DataFixTypes.LEVEL
    );
}