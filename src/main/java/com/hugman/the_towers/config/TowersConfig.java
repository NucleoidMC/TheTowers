package com.hugman.the_towers.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

import java.util.List;

public record TowersConfig(PlayerConfig playerConfig, List<GameTeam> teamConfig, Identifier mapTemplateId, int maxHealth, boolean healthStealth, int respawnCooldown) {
	public static final Codec<TowersConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(TowersConfig::playerConfig),
			GameTeam.CODEC.listOf().fieldOf("teams").forGetter(TowersConfig::teamConfig),
			Identifier.CODEC.fieldOf("map").forGetter(TowersConfig::mapTemplateId),
			Codec.INT.fieldOf("max_health").forGetter(TowersConfig::maxHealth),
			Codec.BOOL.fieldOf("health_stealth").forGetter(TowersConfig::healthStealth),
			Codec.INT.optionalFieldOf("respawn_cooldown", 5).forGetter(TowersConfig::respawnCooldown)
	).apply(instance, TowersConfig::new));
}
