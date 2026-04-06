package net.minecraft.src;
import java.util.List;

public class EntityPigZombie extends EntityMob {
    private int angerLevel = 0;
    private int randomSoundDelay = 0;
    private static final ItemStack defaultHeldItem = new ItemStack(Item.swordGold, 1);

    public EntityPigZombie(World var1) {
        super(var1);
        this.texture = "/mob/pigzombie.png";
        this.moveSpeed = 0.5F;
        this.attackStrength = 5;
    }

    public void onUpdate() {
        this.moveSpeed = this.entityToAttack != null ? 0.6F : 0.5F;
        if(this.randomSoundDelay > 0 && --this.randomSoundDelay == 0) {
            this.worldObj.playSoundAtEntity(this, "mob.zombiepig.zpigangry", this.getSoundVolume() * 2.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
        }

        super.onUpdate();
    }

    public boolean canSpawn() {
        return this.worldObj.difficultySetting > 0 && this.worldObj.checkIfAABBIsClear(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox);
    }

    public void writeEntityToNBT(NBTTagCompound var1) {
        super.writeEntityToNBT(var1);
        var1.setShort("Anger", (short)this.angerLevel);
    }

    public void readEntityFromNBT(NBTTagCompound var1) {
        super.readEntityFromNBT(var1);
        this.angerLevel = var1.getShort("Anger");
    }

    protected Entity findPlayerToAttack() {
        if (this.angerLevel == 0) {
            Entity potentialTarget = super.findPlayerToAttack();
            if (potentialTarget != null && potentialTarget.getDistance(posX, posY, posZ) < 5) {
                return potentialTarget;
            }
            return null;
        }
        return super.findPlayerToAttack();
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();
    }

    public boolean attackEntity(Entity var1, int var2) {
        if(var1 instanceof EntityPlayer) {
            List var3 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expands(32.0D, 32.0D, 32.0D));

            for(int var4 = 0; var4 < var3.size(); ++var4) {
                Entity var5 = (Entity)var3.get(var4);
                if(var5 instanceof EntityPigZombie) {
                    EntityPigZombie var6 = (EntityPigZombie)var5;
                    var6.becomeAngryAt(var1);
                }
            }

            this.becomeAngryAt(var1);
        }

        return super.attackEntityFrom(var1, var2);
    }

    private void becomeAngryAt(Entity var1) {
        this.entityToAttack = var1;
        this.angerLevel = 400 + this.rand.nextInt(400);
        this.randomSoundDelay = this.rand.nextInt(40);
    }

    public void onDeath(Entity var1) {
        if(this.scoreValue > 0 && var1 != null) {
            var1.addToPlayerScore(this, this.scoreValue);
        }

        this.dead = true;
        int var2 = this.getDropItemId();
        if(var2 > 0) {
            int var3 = this.rand.nextInt(5);
            if (var3 == 0) {
                this.dropItem(var2, 1);
            }
        }
    }

    protected String getLivingSound() {
        return "mob.zombie";
    }

    protected String getHurtSound() {
        return "mob.zombiehurt";
    }

    protected String getDeathSound() {
        return "mob.zombiedeath";
    }

    protected int getDropItemId() {
        return Item.ingotGold.shiftedIndex;
    }

    public ItemStack getHeldItem() {
        return defaultHeldItem;
    }
}
