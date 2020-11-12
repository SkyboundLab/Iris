package com.volmit.iris.manager;

import com.volmit.iris.Iris;
import com.volmit.iris.object.*;
import com.volmit.iris.util.KMap;
import com.volmit.iris.util.ObjectResourceLoader;
import com.volmit.iris.util.RNG;
import com.volmit.iris.util.ResourceLoader;
import lombok.Data;

import java.io.File;
import java.util.function.Function;

@Data
public class IrisDataManager
{
	public static final KMap<Integer, IrisDataManager> managers = new KMap<>();
	private ResourceLoader<IrisBiome> biomeLoader;
	private ResourceLoader<IrisLootTable> lootLoader;
	private ResourceLoader<IrisRegion> regionLoader;
	private ResourceLoader<IrisDimension> dimensionLoader;
	private ResourceLoader<IrisGenerator> generatorLoader;
	private ResourceLoader<IrisStructure> structureLoader;
	private ResourceLoader<IrisEntity> entityLoader;
	private ResourceLoader<IrisBlockData> blockLoader;
	private ObjectResourceLoader objectLoader;
	private boolean closed;
	private final File dataFolder;
	private final int id;

	public IrisDataManager(File dataFolder)
	{
		this(dataFolder, false);
	}

	public IrisDataManager(File dataFolder, boolean oneshot)
	{
		this.dataFolder = dataFolder;
		this.id = RNG.r.imax();
		closed = false;
		hotloaded();

		if(!oneshot)
		{
			managers.put(id, this);
		}
	}

	public void close()
	{
		closed = true;
		managers.remove(id);
		dump();
		this.lootLoader =  null;
		this.entityLoader =  null;
		this.regionLoader =  null;
		this.biomeLoader =  null;
		this.dimensionLoader =  null;
		this.structureLoader =  null;
		this.generatorLoader =  null;
		this.blockLoader =  null;
		this.objectLoader = null;
	}

	public static void dumpManagers()
	{
		for(IrisDataManager i : managers.v())
		{
			Iris.warn(i.getId() + " @ " + i.getDataFolder().getAbsolutePath());
			printData(i.lootLoader);
			printData(i.entityLoader);
			printData(i.regionLoader);
			printData(i.biomeLoader);
			printData(i.dimensionLoader);
			printData(i.structureLoader);
			printData(i.generatorLoader);
			printData(i.blockLoader);
			printData(i.objectLoader);
		}
	}

	private static void printData(ResourceLoader<?> rl)
	{
		Iris.warn("  " + rl.getResourceTypeName() + " @ /" + rl.getFolderName() + ": Cache=" + rl.getLoadCache().size() + " Folders=" + rl.getFolders().size());
	}

	public IrisDataManager copy() {
		return new IrisDataManager(dataFolder);
	}

	public void hotloaded()
	{
		if(closed)
		{
			return;
		}

		File packs = dataFolder;
		packs.mkdirs();
		this.lootLoader = new ResourceLoader<>(packs, this, "loot", "Loot", IrisLootTable.class);
		this.entityLoader = new ResourceLoader<>(packs,this,  "entities", "Entity", IrisEntity.class);
		this.regionLoader = new ResourceLoader<>(packs, this, "regions", "Region", IrisRegion.class);
		this.biomeLoader = new ResourceLoader<>(packs, this, "biomes", "Biome", IrisBiome.class);
		this.dimensionLoader = new ResourceLoader<>(packs, this, "dimensions", "Dimension", IrisDimension.class);
		this.structureLoader = new ResourceLoader<>(packs, this, "structures", "Structure", IrisStructure.class);
		this.generatorLoader = new ResourceLoader<>(packs, this, "generators", "Generator", IrisGenerator.class);
		this.blockLoader = new ResourceLoader<>(packs,this,  "blocks", "Block", IrisBlockData.class);
		this.objectLoader = new ObjectResourceLoader(packs, this, "objects", "Object");
	}

	public void dump()
	{
		if(closed)
		{
			return;
		}
		biomeLoader.clearCache();
		blockLoader.clearCache();
		lootLoader.clearCache();
		objectLoader.clearCache();
		regionLoader.clearCache();
		dimensionLoader.clearCache();
		entityLoader.clearCache();
		generatorLoader.clearCache();
		structureLoader.clearCache();
	}

	public void clearLists()
	{
		if(closed)
		{
			return;
		}

		lootLoader.clearList();
		blockLoader.clearList();
		entityLoader.clearList();
		biomeLoader.clearList();
		regionLoader.clearList();
		dimensionLoader.clearList();
		generatorLoader.clearList();
		structureLoader.clearList();
		objectLoader.clearList();
	}

	public static IrisObject loadAnyObject(String key)
	{
		return loadAny(key, (dm) -> dm.getObjectLoader().load(key, false));
	}

	public static IrisBiome loadAnyBiome(String key)
	{
		return loadAny(key, (dm) -> dm.getBiomeLoader().load(key, false));
	}

	public static IrisStructure loadAnyStructure(String key)
	{
		return loadAny(key, (dm) -> dm.getStructureLoader().load(key, false));
	}

	public static IrisEntity loadAnyEntity(String key)
	{
		return loadAny(key, (dm) -> dm.getEntityLoader().load(key, false));
	}

	public static IrisLootTable loadAnyLootTable(String key)
	{
		return loadAny(key, (dm) -> dm.getLootLoader().load(key, false));
	}

	public static IrisBlockData loadAnyBlock(String key)
	{
		return loadAny(key, (dm) -> dm.getBlockLoader().load(key, false));
	}

	public static IrisRegion loadAnyRegion(String key)
	{
		return loadAny(key, (dm) -> dm.getRegionLoader().load(key, false));
	}

	public static IrisDimension loadAnyDimension(String key)
	{
		return loadAny(key, (dm) -> dm.getDimensionLoader().load(key, false));
	}

	public static IrisGenerator loadAnyGenerator(String key)
	{
		return loadAny(key, (dm) -> dm.getGeneratorLoader().load(key, false));
	}

	public static <T extends IrisRegistrant> T loadAny(String key, Function<IrisDataManager, T> v) {
		try
		{
			for(File i : Iris.instance.getDataFolder("packs").listFiles())
			{
				if(i.isDirectory())
				{
					IrisDataManager dm = new IrisDataManager(i, true);
					T t = v.apply(dm);

					if(t != null)
					{
						return t;
					}
				}
			}
		}

		catch(Throwable e)
		{
			e.printStackTrace();
		}

		return null;
	}
}