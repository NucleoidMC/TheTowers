package com.hugman.the_towers.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

import java.util.List;

public record TheTowersConfig(PlayerConfig playerConfig, List<GameTeam> teamConfig, Identifier mapTemplateId, int maxHealth, boolean healthStealth, long respawnCooldown) {
	public static final Codec<TheTowersConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(TheTowersConfig::playerConfig),
			GameTeam.CODEC.listOf().fieldOf("teams").forGetter(TheTowersConfig::teamConfig),
			Identifier.CODEC.fieldOf("map").forGetter(TheTowersConfig::mapTemplateId),
			Codec.INT.fieldOf("max_health").forGetter(TheTowersConfig::maxHealth),
			Codec.BOOL.fieldOf("health_stealth").forGetter(TheTowersConfig::healthStealth),
			Codec.LONG.optionalFieldOf("respawn_cooldown", 5L).forGetter(TheTowersConfig::respawnCooldown)
	).apply(instance, TheTowersConfig::new));
}
