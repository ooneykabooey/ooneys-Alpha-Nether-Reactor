package net.minecraft.src;

import java.util.List;

public class TileEntityNetherReactor extends TileEntity {

    private boolean isInitialized;
    private boolean hasFinished;
    private int curLevel;
    private short progress;
    private int TPS = 20;
    private static int NUM_PIG_ZOMBIE_SLOTS = 3;


    public TileEntityNetherReactor() {
        isInitialized = false;
        progress = 0;
        curLevel = 0;
        hasFinished = false;
    }

    public boolean shouldSave() {
        return true;
    }

    public void lightItUp(int x, int y, int z, EntityPlayer plr) {
        if (!isInitialized && !hasFinished) {
            curLevel = 0;
            System.out.println("Set Phase!");
            BlockNetherReactorCore.setPhase(worldObj, x, y, z, EnumReactorPhase.ACTIVATED);
            isInitialized = true;
            buildDome(x,y,z);

            // Set night.
            long timeOfDay = worldObj.worldTime % 24000L;
            long startOfDay = worldObj.worldTime - timeOfDay;
            worldObj.setWorldTime(startOfDay + 14000L);
        }
    }

    @Override
    public void updateEntity() {
        if (isInitialized && !hasFinished) {

            if (progress % TPS == 0) { // MUST BE MODIFIED TO WORK RIGHT WITH DIFFERENT TICK SPEEDS.
                int currentTime = progress / TPS;
                if (currentTime < 10) {
                    tickGlowingRedstoneTransformation(currentTime);
                }
                if (currentTime > 42 && currentTime <= 45) {
                    // start with top layer and move down
                    int currentLayer = 45 - currentTime;
                    turnGlowingObsidianLayerToObsidian(currentLayer);
                }
                if (checkLevelChange(progress / TPS)) {
                    curLevel++;
                    spawnItems(getNumItemsPerLevel(curLevel));
                    trySpawnPigZombies(NUM_PIG_ZOMBIE_SLOTS, getNumEnemiesPerLevel(curLevel));
                }
            }

            progress++;

            if (progress > TPS * 46) {
                finishReactorRun();
            }

        } else if (hasFinished) {

            int finalSpawn = (TPS * 46) + (TPS * 3);

            if (progress <= finalSpawn) {
                progress++;
                if (progress == finalSpawn) {
                    if (playersAreCloseBy()) {
                        trySpawnPigZombies(2,3);
                    } else {
                        killPigZombies();
                    }
                }
            }
        }
    }

    // write if initilized, if so, write progress and if its finished.
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        isInitialized = tag.getBoolean("IsInitialized");
        if (isInitialized) {
            progress = tag.getShort("Progress");
            hasFinished = tag.getBoolean("HasFinished");
        }
    }

    // write all values.
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("IsInitialized", isInitialized);
        tag.setShort("Progress", progress);
        tag.setBoolean("HasFinished", hasFinished);
    }

    public String getName() {
        return "NetherReactor";
    }

    public int getNumEnemiesPerLevel(int curLevel) {
        if (curLevel == 0) {
            return 3;
        }

        if (curLevel > 4) {
            return 2;
        }

        if (curLevel < 6) {
            return Math.max(0, worldObj.rand.nextInt(2));
        }

        return Math.max(0, worldObj.rand.nextInt(1));
    }

    public int getNumItemsPerLevel(int curLevel) {
        if (curLevel == 0) {
            return 3 * 3;
        }

        if (curLevel < 4) {
            return 5 * 3;
        }

        if (curLevel < 8) {
            return Math.max(0, worldObj.rand.nextInt(14 * 3) - 4);
        }

        return Math.max(0, worldObj.rand.nextInt(9 * 3) - 2);
    }

    public void spawnItems(int numItems) {
        for (int i = 0; i < numItems; ++i) {
            spawnItem();
        }
    }

    public Vec3D getSpawnPosition(float minDistance, float variableDistance, float offset) {
        float distance = minDistance + worldObj.rand.nextFloat() * variableDistance;
        float rad = worldObj.rand.nextFloat() * (float) (Math.PI * 2);
        return  Vec3D.createVectorHelper(Math.sin(rad) * distance + xCoord, offset + yCoord, Math.cos(rad) * distance + zCoord);
    }

    public void spawnEnemy() {
        EntityPigZombie mob = new EntityPigZombie(worldObj);
        Vec3D enemyPosition = getSpawnPosition(3, 4, -1);
        mob.setPosition(enemyPosition.xCoord, enemyPosition.yCoord, enemyPosition.zCoord);
        worldObj.spawnEntityInWorld(mob);
    }

    public void spawnItem() {
        Vec3D itemPosition = getSpawnPosition(3, 4, -1);

        EntityItem item = new EntityItem(worldObj, itemPosition.xCoord, itemPosition.yCoord, itemPosition.zCoord, getSpawnItem());
        item.setPosition(itemPosition.xCoord, itemPosition.yCoord, itemPosition.zCoord);

        item.delayBeforeCanPickup = 10;
        worldObj.spawnEntityInWorld(item);
    }

    ItemStack getSpawnItem() {
        int itemType = worldObj.rand.nextInt(10);

        switch (itemType) {
            case 0: return new ItemStack(Item.lightStoneDust.shiftedIndex, 3);
            case 1: return new ItemStack(Block.slowSand.blockID, worldObj.rand.nextInt(2) + 3);
            case 2: return new ItemStack(Item.arrow.shiftedIndex);
            case 3: return new ItemStack(Block.mushroomBrown.blockID);
            case 4: return new ItemStack(Block.mushroomRed.blockID);
            case 5: return new ItemStack(Item.brick.shiftedIndex, worldObj.rand.nextInt(2) + 1);
            case 6: return new ItemStack(Item.silk.shiftedIndex);
            case 7: return new ItemStack(Block.plantCyan.blockID);
            case 8: return new ItemStack(Item.porkRaw.shiftedIndex);
            default: return GetLowOddsSpawnItem();
        }
    }

    public ItemStack GetLowOddsSpawnItem() {
        if (worldObj.rand.nextInt(10) <= 9) {
            ItemStack[] items = {
                    new ItemStack(Item.reed.shiftedIndex),
                    new ItemStack(Item.doorWood.shiftedIndex),
                    new ItemStack(Item.book.shiftedIndex),
                    new ItemStack(Item.bow.shiftedIndex),
                    new ItemStack(Item.feather.shiftedIndex),
                    new ItemStack(Item.painting.shiftedIndex),
                    new ItemStack(Item.boat.shiftedIndex),
                    new ItemStack(Block.bookshelf.blockID)
            };
            int itemIndex = worldObj.rand.nextInt(items.length);
            ItemStack itemToSpawn = items[itemIndex];
            return itemToSpawn;
        }
        return new ItemStack(Block.blockClay.blockID);
    }

    public boolean checkLevelChange(int progress) {
        int[] levelChangeTime = {10,13,20,22,25,30,34,36,38,40};
        int count = levelChangeTime.length;
        for (int a = 0; a < count; ++a) {
            if (levelChangeTime[a] == progress) {
                return true;
            }
        }
        return false;
    }

    public void clearDomeSpace(int x, int y, int z) {
        for (int curX = -12; curX <= 12; ++curX) {
            for (int curY = -12; curY <= 12; ++curY) {
                for (int curZ = -12; curZ <= 12; ++curZ) {
                    if (curY > 2 || curX < -1 || curX > 1 || curZ < -1 || curZ > 1) {
                        worldObj.setBlock(curX + x, curY + y, curZ + z, 0);
                    }
                }
            }
        }
    }

    public void finishReactorRun() {

        if (hasFinished || !isInitialized) return; // prevent duplicate call if already finished or was broken.

        // Only update metadata if the block at this location is actually still the Core
        // (Prevents changing the metadata of another block that might have overwritten it)
        if (worldObj.getBlockId(xCoord, yCoord, zCoord) == Block.netherReactor.blockID) {
            BlockNetherReactorCore.setPhase(worldObj, xCoord, yCoord, zCoord, EnumReactorPhase.DEACTIVATED);
        }

        hasFinished = true;
        deteriorateDome(xCoord,yCoord,zCoord);

        for(int curX = xCoord - 1; curX <= xCoord + 1; ++curX) {
            for(int curY = yCoord - 1; curY <= yCoord + 1; ++curY) {
                for(int curZ = zCoord - 1; curZ <= zCoord + 1; ++curZ) {
                    if (curX != xCoord || curY != yCoord || curZ != zCoord) {
                        worldObj.setBlock(curX, curY, curZ, Block.obsidian.blockID);
                    }
                }
            }
        }
    }


    public int numOfFreeEnemySlots() {
        int numPigZombiesFound = 0;
        System.out.println("attempting numoffreeenemyslots..");
        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox((float)xCoord, (float)yCoord, (float)zCoord, xCoord + 1.0F, yCoord + 1.0F, zCoord + 1.0F);
        List<?> nearby = worldObj.getEntitiesWithinAABB(EntityPigZombie.class, bb.expand(7,7,7));
        for (int i = 0; i < nearby.size(); ++i) {
            if (nearby.get(i) instanceof EntityPigZombie && ((EntityPigZombie) nearby.get(i)).isEntityAlive()) {
                numPigZombiesFound++;
                System.out.println("PigZombie found");
            }
        }
        return NUM_PIG_ZOMBIE_SLOTS - numPigZombiesFound;
    }

    public void trySpawnPigZombies(int maxNumEnemies, int maxToSpawn) {
        if(worldObj.difficultySetting == 0) { // peaceful
            return;
        }
        int currentNumPigZombies = NUM_PIG_ZOMBIE_SLOTS - numOfFreeEnemySlots();
        if (currentNumPigZombies < maxNumEnemies) {
            for(int a = 0; a < maxToSpawn && currentNumPigZombies < maxNumEnemies; ++a) {
                spawnEnemy();
                currentNumPigZombies++;
            }
        }
    }


    public boolean tickGlowingRedstoneTransformation(int curTime) {
        switch (curTime) {
            case 2: return turnLayerToGlowingObsidian(0, Block.cobblestone.blockID);
            case 3: return turnLayerToGlowingObsidian(1, Block.cobblestone.blockID);
            case 4: return turnLayerToGlowingObsidian(2, Block.cobblestone.blockID);
            case 7: return turnLayerToGlowingObsidian(0, Block.blockGold.blockID);
            case 8: return turnLayerToGlowingObsidian(1, Block.blockGold.blockID);
            case 9: return turnLayerToGlowingObsidian(2, Block.blockGold.blockID);
        }
        return false;
    }

    public boolean turnLayerToGlowingObsidian(int layer, int type) {
        NetherReactorPattern pattern = new NetherReactorPattern();
        for (int checkX = -1; checkX <= 1; ++checkX) {
                for (int checkZ = -1; checkZ <= 1; ++checkZ) {
                    if (pattern.getTileAt(layer, checkX + 1, checkZ + 1) == type) {
                        worldObj.setBlock(xCoord + checkX, yCoord-1 + layer, zCoord + checkZ, Block.glowingObsidian.blockID);
                }
            }
        }
        return false;
    }

    public boolean turnGlowingObsidianLayerToObsidian(int layer) {
        NetherReactorPattern pattern = new NetherReactorPattern();
        for (int checkX = -1; checkX <= 1; ++checkX) {
            for (int checkZ = -1; checkZ <= 1; ++checkZ) {
                if (worldObj.getBlockId(xCoord + checkX, yCoord-1+layer, zCoord + checkZ) != Block.netherReactor.blockID) {
                    worldObj.setBlock(xCoord + checkX, yCoord-1 + layer, zCoord + checkZ, Block.obsidian.blockID);
                }
            }
        }
        return false;
    }


    public void buildDome(int x, int y, int z) {
        buildFloorVolume(x, y - 3, z, 8, 2, Block.bloodStone.blockID);
        buildHollowedVolume(x, y-1, z, 8, 4, Block.bloodStone.blockID, 0);
        buildFloorVolume(x, y - 1 + 4, z, 8, 1, Block.bloodStone.blockID);
        buildCrockedRoofVolume(false, x, y - 1 + 5, z, 8, 1, Block.bloodStone.blockID);
        buildCrockedRoofVolume(true, x, y - 1 + 6, z, 5, 8, Block.bloodStone.blockID);
        buildCrockedRoofVolume(false, x, y + -1 + 12, z, 3, 14, Block.bloodStone.blockID);
    }

    public void buildHollowedVolume(int x, int y, int z, int expandWidth, int height, int wallTileId, int clearTileId) {
        for (int curY = 0; curY < height; ++curY) {
            for (int curX = -expandWidth; curX <= expandWidth; ++curX) {
                for (int curZ = -expandWidth; curZ <= expandWidth; ++curZ) {
                    if (curX == -expandWidth || curX == expandWidth ||  curZ == -expandWidth || curZ == expandWidth) {
                        worldObj.setBlock(curX + x, curY + y, curZ + z, wallTileId);
                    } else if (curY > 2 || curX < -1 || curX > 1 || curZ < -1 || curZ > 1) {
                        worldObj.setBlock(curX + x, curY + y, curZ + z, clearTileId);
                    }
                }
            }
        }
    }

    public void buildFloorVolume(int x, int y, int z, int expandWidth, int height, int tileId) {
        for(int curY = 0; curY < height; ++curY) {
            for(int curX = -expandWidth; curX <= expandWidth; ++curX) {
                for(int curZ = -expandWidth; curZ <= expandWidth; ++curZ) {
                    worldObj.setBlock(curX + x, curY + y, curZ + z, tileId);
                }
            }
        }
    }

    public void buildCrockedRoofVolume(boolean inverted, int x, int y, int z, int expandWidth, int height, int tileId ) {
        int fullHeight = height + expandWidth;
        for(int curX = -expandWidth; curX <= expandWidth; ++curX) {
            for(int curZ = -expandWidth; curZ <= expandWidth; ++curZ) {
                int offset = inverted ? ((-curX - curZ) / 2) : ((curX + curZ) / 2);
                int acceptHeight = fullHeight + offset;
                for(int curY = 0; curY < fullHeight + expandWidth; ++curY) {
                    if(acceptHeight >= curY
                            && (isEdge(curX, expandWidth, curZ)
                            || acceptHeight == curY )) {
                        worldObj.setBlock(curX + x, curY + y, curZ + z, tileId);
                    }
                }
            }
        }
    }

    public boolean isEdge(int curX, int expandWidth, int curZ) {
        return (curX == -expandWidth || curX == expandWidth || curZ == -expandWidth ||  curZ == expandWidth);
    }

    public void deteriorateDome(int x, int y, int z) {
        deterioateHollowedVolume(x, y - 1, z, 8, 5, 0);
        deterioateCrockedRoofVolume(false, x, y - 1 + 5, z, 8, 1, 0);
        deterioateCrockedRoofVolume(true, x, y - 1 + 6, z, 5, 8, 0);
        deterioateCrockedRoofVolume(false, x, y + -1 + 12, z, 3, 14, 0);
    }

    public void deterioateCrockedRoofVolume(boolean inverted, int x, int y, int z, int expandWidth, int height, int tileId ) {
        int fullHeight = height + expandWidth;
        for(int curX = -expandWidth; curX <= expandWidth; ++curX) {
            for(int curZ = -expandWidth; curZ <= expandWidth; ++curZ) {
                int offset = inverted ? ((-curX - curZ) / 2) : ((curX + curZ) / 2);
                int acceptHeight = fullHeight + offset;
                for(int curY = 0; curY < fullHeight + expandWidth; ++curY) {
                    if(acceptHeight >= curY
                            && (isEdge(curX, expandWidth, curZ))) {
                        if(worldObj.rand.nextInt(4) == 0) {
                            worldObj.setBlock(curX + x, curY + y, curZ + z, tileId);
                        }
                    }
                }
            }
        }
    }

    public void deterioateHollowedVolume( int x, int y, int z, int expandWidth, int height, int tileId ) {
        for(int curY = 0; curY < height; ++curY) {
            for(int curX = -expandWidth; curX <= expandWidth; ++curX) {
                for(int curZ = -expandWidth; curZ <= expandWidth; ++curZ) {
                    if((curX == -expandWidth || curX == expandWidth)
                            || (curZ == -expandWidth || curZ == expandWidth)) {
                        if(worldObj.rand.nextInt(3) == 0)
                            worldObj.setBlock(curX + x, curY + y, curZ + z, tileId);
                    }
                }
            }
        }
    }

    public boolean playersAreCloseBy() {
        int numPlayers = 0;
        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox((float)xCoord, (float)yCoord, (float)zCoord, xCoord + 1.0F, yCoord + 1.0F, zCoord + 1.0F);
        List<?> nearby = worldObj.getEntitiesWithinAABB(EntityPlayer.class, bb.expand(40,40,40));
        for (int i = 0; i < nearby.size(); ++i) {
            if (nearby.get(i) instanceof EntityPlayer && ((EntityPlayer) nearby.get(i)).isEntityAlive()) {
                numPlayers++;
                System.out.println("Player close by." + ((EntityPlayer) nearby.get(i)).username);
            }
        }
        if (numPlayers == 0) {
            System.out.println("DEBUG: No players detected by AxisAlignedBB, players get excluded from getEntitiesWithinAABB().");
        }
        return numPlayers != 0;
    }

    public void killPigZombies() {
        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox((float)xCoord, (float)yCoord, (float)zCoord, xCoord + 1.0F, yCoord + 1.0F, zCoord + 1.0F);
        List<?> nearby = worldObj.getEntitiesWithinAABB(EntityPigZombie.class, bb.expand(40,40,40));
        for (int i = 0; i < nearby.size(); ++i) {
            if (nearby.get(i) instanceof EntityPigZombie && ((EntityPigZombie) nearby.get(i)).isEntityAlive()) {
                ((EntityPigZombie) nearby.get(i)).setEntityDead();
                System.out.println("PigZombie found, now dead.");
            }
        }
    }
}
