package net.minecraft.src;

public class ItemFishingRod extends Item {
    public ItemFishingRod(int var1) {
        super(var1);
        this.maxDamage = 64;
    }

    public boolean isFull3D() {
        return true;
    }

    public boolean shouldRotateAroundWhenRendering() {
        return true;
    }

    public ItemStack onItemRightClick(ItemStack var1, World var2, EntityPlayer var3) {
        if(var3.field_4128_n != null) {
            int var4 = var3.field_4128_n.func_4043_i();
            var1.damageItem(var4);
            var3.swingItem();
        } else {
            var2.playSoundAtEntity(var3, "random.bow", 0.5F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F));
            if(!var2.multiplayerWorld) {
                var2.spawnEntityInWorld(new EntityFish(var2, var3));
            }

            var3.swingItem();
        }

        return var1;
    }
}
