package fr.hugman.plasmid.api.game.team.provider;

import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

//TODO: registry
public interface TeamListProvider {
    GameTeamList get(Random random);
}
