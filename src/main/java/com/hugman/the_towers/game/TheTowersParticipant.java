package com.hugman.the_towers.game;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class TheTowersParticipant {
	private final PlayerRef ref;
	private final GameSpace space;
	public boolean isRespawning;
	public long ticksUntilRespawn;

	TheTowersParticipant(PlayerRef ref, GameSpace space) {
		this.ref = ref;
		this.space = space;
	}

	public PlayerRef getRef() {
		return ref;
	}

	@Nullable
	public ServerPlayerEntity getPlayer() {
		ServerPlayerEntity player = this.ref.getEntity(space.getWorld());
		if(player != null) {
			if(space.containsPlayer(player)) {
				return player;
			}
		}
		return null;
	}
}
