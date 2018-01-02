package com.legacy.wasteland;

import com.legacy.wasteland.config.WorldConfig;
import com.legacy.wasteland.world.WastelandWorld;
import com.legacy.wasteland.world.biome.decorations.BiomeDecoratorWasteland;
import com.legacy.wasteland.world.util.WastelandWorldData;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WastelandEventHandler {
   public WastelandWorldData worldFileCache;
   public static BlockPos spawnLocation;
   public static boolean spawnSet;
   public static boolean bunkerSpawned;

   @SubscribeEvent
   public void loadData(Load event) {
      if(event.getWorld().getWorldType() == WastelandWorld.worldtype_wasteland) {
         this.worldFileCache = new WastelandWorldData("saves/" + FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName() + "/data/wasteland_cache.dat");
         if(!this.worldFileCache.checkIfExists()) {
            bunkerSpawned = false;
            spawnSet = false;
            this.worldFileCache.createFile();
         } else if(this.worldFileCache.loadSpawnLoc().getY() != 0) {
            spawnLocation = this.worldFileCache.loadSpawnLoc();
         }
      }

   }

   @SubscribeEvent
   public void onRespawnEvent(LivingUpdateEvent event) {
      if(event.getEntityLiving() instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)event.getEntityLiving();
         if(!spawnSet && spawnLocation != null) {
            BlockPos upPos = spawnLocation.up(1);
            if(player.getPosition().getY() == upPos.getY()) {
               spawnSet = true;
            }

            player.moveToBlockPosAndAngles(spawnLocation.up(1), 0.0F, 0.0F);
         }
      }

   }

   @SubscribeEvent
   public void setSpawnpointEvent(EntityJoinWorldEvent event) {
      if(event.getEntity() instanceof EntityPlayer && event.getWorld().getWorldType() == WastelandWorld.worldtype_wasteland) {
         EntityPlayer player = (EntityPlayer)event.getEntity();
         if(this.isNewPlayer(player)) {
            if(spawnLocation == null && player.getPosition() != BlockPos.ORIGIN) {
               spawnLocation = player.getPosition().down(9);
            }

            if(!spawnSet && spawnLocation != null) {
               if(!bunkerSpawned && WorldConfig.shouldSpawnBunker) {
                  if(!event.getWorld().isRemote) {
                     BiomeDecoratorWasteland.spawnBunker(event.getWorld());
                  }

                  this.worldFileCache.saveSpawnLoc(spawnLocation);
                  bunkerSpawned = true;
               }

               BlockPos pos = spawnLocation.up();
               player.setSpawnPoint(pos, true);
               player.setSpawnChunk(pos, true, 0);
            }

            this.worldFileCache.savePlayerName(player.getDisplayNameString());
         }
      }

   }

   private boolean isNewPlayer(EntityPlayer player) {
      List loadedPlayers = this.worldFileCache.getPlayerNames();
      return loadedPlayers == null || !loadedPlayers.contains(player.getDisplayNameString());
   }
}
