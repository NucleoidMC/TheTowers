package com.hugman.the_towers.game.map.parts;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hugman.the_towers.TheTowers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Set;

public record GeneratorType(ItemStack stack, long interval) {
	public static final Codec<GeneratorType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.CODEC.fieldOf("stack").forGetter(GeneratorType::stack),
			Codec.LONG.fieldOf("interval").forGetter(GeneratorType::interval)
	).apply(instance, GeneratorType::new));

	private static final TinyRegistry<GeneratorType> REGISTRY = TinyRegistry.create();

	public static void register() {
		ResourceManagerHelper serverData = ResourceManagerHelper.get(ResourceType.SERVER_DATA);

		serverData.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return TheTowers.id("generator_types");
			}

			@Override
			public void reload(ResourceManager manager) {
				REGISTRY.clear();

				Collection<Identifier> resources = manager.findResources("towers_generator_types", path -> path.endsWith(".json"));

				for(Identifier path : resources) {
					try {
						Resource resource = manager.getResource(path);
						try(Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
							JsonElement json = new JsonParser().parse(reader);

							Identifier identifier = identifierFromPath(path);

							DataResult<GeneratorType> result = CODEC.decode(JsonOps.INSTANCE, json).map(Pair::getFirst);

							result.result().ifPresent(generatorType -> REGISTRY.register(identifier, generatorType));
							result.error().ifPresent(error -> TheTowers.LOGGER.error("Failed to decode The Towers generator type at {}: {}", path, error.toString()));
						}
					}
					catch(IOException e) {
						TheTowers.LOGGER.error("Failed to read The Towers generator type at {}", path, e);
					}
				}
			}
		});
	}

	private static Identifier identifierFromPath(Identifier location) {
		String path = location.getPath();
		path = path.substring("towers_generator_types/".length(), path.length() - ".json".length());
		return new Identifier(location.getNamespace(), path);
	}

	@Nullable
	public static GeneratorType get(Identifier identifier) {
		return REGISTRY.get(identifier);
	}

	public static Set<Identifier> getKeys() {
		return REGISTRY.keySet();
	}
}
