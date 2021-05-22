package com.hugman.the_towers.game;

import com.hugman.the_towers.game.map.TheTowersMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
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
		this.spawnPlayerAt(player, map.getCenter());
	}

	public void spawnPlayerAtSpawn(ServerPlayerEntity player, GameTeam team) {
		BlockPos spawnPosition = new BlockPos(this.map.getTeamRegion(team).getSpawn().getCenter());
		this.spawnPlayerAt(player, spawnPosition);
	}

	public void spawnPlayerAt(ServerPlayerEntity player, BlockPos pos) {
		ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		this.gameSpace.getWorld().getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());
		player.teleport(this.gameSpace.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
	}
}
