package fr.hugman.plasmid.api.game_map.template.processor;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.hugman.plasmid.api.game.attachment.PlasmidGameAttachments;
import fr.hugman.plasmid.api.util.ColoredItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
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
        var teamList = activity.getGameSpace().getAttachment(PlasmidGameAttachments.TEAM_LIST).list(); //TODO: throw error if no team list is present

        if (teamList.size() > this.baseColors.size()) {
            throw new GameOpenException(Text.literal("Not enough base colors provided for the number of teams.")); //TODO: translate
        }

        var blockMap = new HashMap<Block, Block>();
        var blockEntityReplace = new HashMap<String, String>();
        for (int i = 0; i < this.baseColors.size(); i++) {
            var baseColor = this.baseColors.get(i);
            var teamColor = teamList.get(i).config().blockDyeColor();
            blockMap.put(ColoredBlocks.wool(baseColor), ColoredBlocks.wool(teamColor));
            blockMap.put(ColoredBlocks.carpet(baseColor), ColoredBlocks.carpet(teamColor));
            blockMap.put(ColoredBlocks.terracotta(baseColor), ColoredBlocks.terracotta(teamColor));
            blockMap.put(ColoredBlocks.glazedTerracotta(baseColor), ColoredBlocks.glazedTerracotta(teamColor));
            blockMap.put(ColoredBlocks.concrete(baseColor), ColoredBlocks.concrete(teamColor));
            blockMap.put(ColoredBlocks.concretePowder(baseColor), ColoredBlocks.concretePowder(teamColor));
            blockMap.put(ColoredBlocks.glass(baseColor), ColoredBlocks.glass(teamColor));
            blockMap.put(ColoredBlocks.glassPane(baseColor), ColoredBlocks.glassPane(teamColor));
            blockMap.put(ColoredBlocks.bed(baseColor), ColoredBlocks.bed(teamColor));
            blockMap.put(ColoredBlocks.shulkerBox(baseColor), ColoredBlocks.shulkerBox(teamColor));
            blockMap.put(ColoredBlocks.candle(baseColor), ColoredBlocks.candle(teamColor));
            blockMap.put(ColoredBlocks.candleCake(baseColor), ColoredBlocks.candleCake(teamColor));
            blockEntityReplace.put(Registries.ITEM.getId(ColoredItems.dye(baseColor)).toString(), Registries.ITEM.getId(ColoredItems.dye(teamColor)).toString());
            blockEntityReplace.put(Registries.ITEM.getId(ColoredItems.bundle(baseColor)).toString(), Registries.ITEM.getId(ColoredItems.bundle(teamColor)).toString());
            blockEntityReplace.put(Registries.ITEM.getId(ColoredItems.harness(baseColor)).toString(), Registries.ITEM.getId(ColoredItems.harness(teamColor)).toString());
        }

        new ReplaceBlocksTemplateProcessor(blockMap).processTemplate(activity, template);

        for(var entry : blockMap.entrySet()) {
            blockEntityReplace.put(Registries.BLOCK.getId(entry.getKey()).toString(), Registries.BLOCK.getId(entry.getValue()).toString());
        }
        new ReplaceBlockEntitiesTemplateProcessor(blockEntityReplace).processTemplate(activity, template);
    }
}
