package fr.hugman.plasmid.api.game.attachment;

import net.minecraft.resources.ResourceLocation;
import xyz.nucleoid.plasmid.api.game.GameAttachment;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.impl.Plasmid;

public class PlasmidGameAttachments {
    public static final GameAttachment<GameTeamList> TEAM_LIST = GameAttachment.create(ResourceLocation.fromNamespaceAndPath(Plasmid.ID, "team_list"));
}
