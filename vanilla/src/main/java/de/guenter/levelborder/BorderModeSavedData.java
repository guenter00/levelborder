package de.guenter.levelborder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.util.datafix.DataFixTypes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

public class BorderModeSavedData extends SavedData {
    public BorderMode borderMode;
    public boolean disableBorderShrink;
    public Map<String, Double> maxBorderSizes;

    public BorderModeSavedData() {
        this.borderMode = BorderMode.OWN;
        this.disableBorderShrink = false;
        this.maxBorderSizes = new HashMap<>();
    }

    public BorderModeSavedData(BorderMode borderMode, boolean disableBorderShrink, Map<String, Double> maxBorderSizes) {
        this.borderMode = borderMode;
        this.disableBorderShrink = disableBorderShrink;
        this.maxBorderSizes = new HashMap<>(maxBorderSizes);
    }

    private static final Codec<BorderMode> BORDER_MODE_CODEC = Codec.STRING.xmap(BorderMode::valueOf, BorderMode::name);

    private static final Codec<Map<String, Double>> STRING_DOUBLE_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE);

    public static final Codec<BorderModeSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BORDER_MODE_CODEC.optionalFieldOf("levelBorderMode", BorderMode.OWN).forGetter(sd -> sd.borderMode),
            Codec.BOOL.optionalFieldOf("disableBorderShrink", false).forGetter(sd -> sd.disableBorderShrink),
            STRING_DOUBLE_MAP_CODEC.optionalFieldOf("maxBorderSizes", Map.of()).forGetter(sd -> sd.maxBorderSizes)
    ).apply(instance, BorderModeSavedData::new));

    public static final SavedDataType<BorderModeSavedData> TYPE = new SavedDataType<BorderModeSavedData>(
            Identifier.withDefaultNamespace( "levelborder"),
            BorderModeSavedData::new,
            CODEC,
            DataFixTypes.LEVEL
    );
}