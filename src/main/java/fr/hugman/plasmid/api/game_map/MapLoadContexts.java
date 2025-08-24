package fr.hugman.plasmid.api.game_map;

import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.context.ContextType;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

public class MapLoadContexts {
	public static final ContextParameter<GameTeamList> TEAM_LIST = ContextParameter.of("team_list");

    public static final ContextType CONTEXT_TYPE = new ContextType.Builder().allow(TEAM_LIST).build();
}
