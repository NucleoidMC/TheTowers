package com.hugman.the_towers.map;

import com.hugman.the_towers.config.TowersConfig;
import net.minecraft.text.Text;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenException;

import java.io.IOException;

public class TowersMapGenerator {
    public static TowersMap loadFromConfig(GameOpenContext<TowersConfig> context) throws GameOpenException {
        try {
            var templateId = context.config().mapTemplateId();
            MapTemplate template = MapTemplateSerializer.loadFromResource(context.server(), templateId);
            return TowersMap.fromTemplate(context, template);
        } catch (IOException e) {
            throw new GameOpenException(Text.literal("Failed to load map template"), e);
        }
    }
}
