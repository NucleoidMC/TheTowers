package fr.hugman.plasmid.api.game_map.template.processor;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.hugman.plasmid.api.game.attachment.PlasmidGameAttachments;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.util.ColoredBlocks;

import java.util.HashMap;
import java.util.List;

public record TeamColorMapTemplateProcessor(List<DyeColor> baseColors) implements MapTemplateProcessor {
    public static final MapCodec<TeamColorMapTemplateProcessor> CODEC = DyeColor.CODEC.listOf().fieldOf("base_colors").xmap(TeamColorMapTemplateProcessor::new, TeamColorMapTemplateProcessor::baseColors);

    @Override
    public MapTemplateProcessorType<?> getType() {
        return MapTemplateProcessorType.TEAM_COLORS;
    }

    public void processTemplate(GameActivity activity, MapTemplate template) {
        var teamList = activity.getGameSpace().getAttachment(PlasmidGameAttachments.TEAM_LIST).list();

        if (teamList.size() > this.baseColors.size()) {
            throw new GameOpenException(Text.literal("Not enough base colors provided for the number of teams.")); //TODO: translate
        }

        var colorMap = new HashMap<DyeColor, DyeColor>();
        for (int i = 0; i < this.baseColors.size(); i++) {
            colorMap.put(this.baseColors.get(i), teamList.get(i).config().blockDyeColor());
        }

        template.getBounds().forEach(pos -> {
            var state = template.getBlockState(pos);
            var block = state.getBlock();
            Block newBlock = null;
            for (var entry : colorMap.entrySet()) {
                var baseColor = entry.getKey();
                var targetColor = entry.getValue();
                if (ColoredBlocks.wool(baseColor) == block) {
                    newBlock = ColoredBlocks.wool(targetColor);
                    break;
                } else if (ColoredBlocks.carpet(baseColor) == block) {
                    newBlock = ColoredBlocks.carpet(targetColor);
                    break;
                } else if (ColoredBlocks.terracotta(baseColor) == block) {
                    newBlock = ColoredBlocks.terracotta(targetColor);
                    break;
                } else if (ColoredBlocks.glazedTerracotta(baseColor) == block) {
                    newBlock = ColoredBlocks.glazedTerracotta(targetColor);
                    break;
                } else if (ColoredBlocks.concrete(baseColor) == block) {
                    newBlock = ColoredBlocks.concrete(targetColor);
                    break;

                } else if (ColoredBlocks.concretePowder(baseColor) == block) {
                    newBlock = ColoredBlocks.concretePowder(targetColor);
                    break;
                } else if (ColoredBlocks.glass(baseColor) == block) {
                    newBlock = ColoredBlocks.glass(targetColor);
                    break;
                } else if (ColoredBlocks.glassPane(baseColor) == block) {
                    newBlock = ColoredBlocks.glassPane(targetColor);
                    break;
                } else if (ColoredBlocks.bed(baseColor) == block) {
                    newBlock = ColoredBlocks.bed(targetColor);
                    break;
                } else if (ColoredBlocks.shulkerBox(baseColor) == block) {
                    newBlock = ColoredBlocks.shulkerBox(targetColor);
                    break;
                } else if (ColoredBlocks.candle(baseColor) == block) {
                    newBlock = ColoredBlocks.candle(targetColor);
                    break;
                } else if (ColoredBlocks.candleCake(baseColor) == block) {
                    newBlock = ColoredBlocks.candleCake(targetColor);
                    break;
                }
            }
            if (newBlock != null) {
                BlockState newState = newBlock.getDefaultState();
                for (Property property : state.getProperties()) {
                    newState = newState.contains(property) ? newState.with(property, state.get(property)) : newState;
                }
                template.setBlockState(pos, newState);
            }
        });
    }
}
