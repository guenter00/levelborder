package de.guenter.levelborder;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.util.datafix.DataFixTypes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class BorderModeSavedData extends SavedData {
    public BorderMode borderMode;

    public BorderModeSavedData() {
        this.borderMode = BorderMode.OWN;
    }

    public BorderModeSavedData(BorderMode borderMode) {
        this.borderMode = borderMode;
    }

    private static final Codec<BorderMode> BORDER_MODE_CODEC = Codec.STRING.xmap(BorderMode::valueOf, BorderMode::name);

    public static final Codec<BorderModeSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BORDER_MODE_CODEC.optionalFieldOf("levelBorderMode", BorderMode.OWN).forGetter(sd -> sd.borderMode)
    ).apply(instance, BorderModeSavedData::new));

    public static final SavedDataType<BorderModeSavedData> TYPE = new SavedDataType<BorderModeSavedData>(
            "levelBorder",
            BorderModeSavedData::new,
            CODEC,
            DataFixTypes.LEVEL
    );
}