package net.minecraft.src;

public class BlockNetherReactorCore extends BlockContainer {

    public BlockNetherReactorCore(int var1, int var2) {
        super(var1, var2, Material.iron);
        this.isBlockContainer[var1] = true;
        this.blockIndexInTexture = 102;
    }

    public void onBlockAdded(World var1, int var2, int var3, int var4) {
        super.onBlockAdded(var1, var2, var3, var4);
    }

    public void onBlockRemoval(World world, int x, int y, int z) {
        TileEntity tile = world.getBlockTileEntity(x, y, z);

        // Safe null-check and type-check to prevent world corruption
        if (tile != null && tile instanceof TileEntityNetherReactor) {
            TileEntityNetherReactor reactor = (TileEntityNetherReactor) world.getBlockTileEntity(x, y, z);
            if ((getPhase(world, x, y, z) == EnumReactorPhase.ACTIVATED)) {
                reactor.finishReactorRun(); // If broken, immediately finish the event.
            }
        }

        super.onBlockRemoval(world, x, y, z);
    }

    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer plr) {
        TileEntityNetherReactor reactor = (TileEntityNetherReactor) world.getBlockTileEntity(x, y, z);
        NetherReactorPattern pattern = new NetherReactorPattern();


        for (int checkLevel = 0; checkLevel <= 2; ++checkLevel) {
            for (int checkX = -1; checkX <= 1; ++checkX) {
                for (int checkZ = -1; checkZ <= 1; ++checkZ) {
                    if (world.getBlockId(x + checkX, y + checkLevel - 1, z + checkZ) != pattern.getTileAt(checkLevel, checkX + 1, checkZ + 1)) {
                       sendPlayerReactorInfo("Not the correct pattern!", plr);

                        return false; // Not correct structure
                    }
                }
            }
        }

        if (!canActivate(world, x, y, z, plr)) return false;

        sendPlayerReactorInfo("Active!", plr);
        // Activating
        if (reactor != null) {
            reactor.lightItUp(x, y, z, plr);
        } else {
            System.out.println("BlockNetherReactorCore is null");
        }
        return true;

    }

    // only works in singleplayer.
    public void sendPlayerReactorInfo(String var1, EntityPlayer plr) {
        boolean isLocal = plr instanceof EntityPlayerSP;
        if (isLocal) {
            ((EntityPlayerSP) plr).getMinecraft().ingameGUI.addChatMessage(var1);
        }
    }

    private boolean canActivate(World world, int x, int y, int z, EntityPlayer plr) {
//        if (!allPlayersCloseToReactor(world, x, y, z)) { // not all players close
//            System.out.println("All players need to be close to the reactor.");
//            return false;
//        } TODO: Fix the commented out code.
        if (!(getPhase(world, x, y, z) == EnumReactorPhase.NORMAL)) {
            sendPlayerReactorInfo("Reactor is already active!", plr);
            return false;
        }
        if (y > 128 - 28) { // too high
            sendPlayerReactorInfo("The Nether reactor needs to be built lower down.", plr);
            return false;
        }
        if (y < 2) { // too low
            sendPlayerReactorInfo("The Nether reactor needs to be built higher up.", plr);
            return false;
        }

        return true;
    }

    @Override
    public int getBlockTextureFromSideAndMetadata(int side, int meta) {
        if (meta == 1) {
            return this.blockIndexInTexture + 1; // ACTIVATED
        } else if (meta == 2) {
            return this.blockIndexInTexture + 2; // DEACTIVATED
        }
        return this.blockIndexInTexture; // NORMAL
    }

    public static void setPhase(World world, int x, int y, int z, EnumReactorPhase phase) {
        int meta = phase == EnumReactorPhase.ACTIVATED ? 1 : (phase == EnumReactorPhase.DEACTIVATED ? 2 : 0);
        world.setBlockMetadataWithNotify(x, y, z, meta);
    }

    public static EnumReactorPhase getPhase(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        switch (meta) {
            case 1: return  EnumReactorPhase.ACTIVATED;
            case 2: return  EnumReactorPhase.DEACTIVATED;
            default: return EnumReactorPhase.NORMAL;
        }
    }

    private boolean allPlayersCloseToReactor(World world, int x, int y, int z) {
        for (int i = 0; i < world.playerEntities.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer)world.playerEntities.get(i);
            if (!(entityplayer.posX >= x - 5 && entityplayer.posX <= x + 5)) return false;
            if (!(entityplayer.posY - entityplayer.height >= y - 1 && entityplayer.posY - entityplayer.height <= y + 1)) return false;
            if (!(entityplayer.posZ >= z - 5 && entityplayer.posZ <= z + 5)) return false;
        }
        return true;
    }

    @Override
    protected TileEntity getBlockEntity() {return new TileEntityNetherReactor();}
}
