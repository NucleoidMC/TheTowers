package com.hugman.the_towers.game;

import com.hugman.the_towers.game.map.TheTowersMap;
import com.hugman.the_towers.game.map.TheTowersTeamRegion;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.player.GameTeam;

public class TheTowersSpawner {
	private final GameSpace gameSpace;
	private final TheTowersMap map;

	public TheTowersSpawner(GameSpace gameSpace, TheTowersMap map) {
		this.gameSpace = gameSpace;
		this.map = map;
	}

	public void spawnPlayerAtCenter(ServerPlayerEntity player) {
		this.spawnPlayerAt(player, map.getCenter(), 0.0F, 0.0F);
	}

	public void spawnPlayerAtSpawn(ServerPlayerEntity player, GameTeam team) {
		TheTowersTeamRegion region = this.map.getTeamRegion(team);
		BlockPos spawnPosition = new BlockPos(region.getSpawn().getCenter());
		this.spawnPlayerAt(player, spawnPosition, region.getSpawnYaw(), region.getSpawnPitch());
	}

	public void spawnPlayerAt(ServerPlayerEntity player, BlockPos pos, float yaw, float pitch) {
		ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		this.gameSpace.getWorld().getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());
		player.teleport(this.gameSpace.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, yaw, pitch);
	}
}
