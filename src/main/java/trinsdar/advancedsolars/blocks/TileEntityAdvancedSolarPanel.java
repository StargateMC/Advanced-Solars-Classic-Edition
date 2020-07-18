package trinsdar.advancedsolars.blocks;

import ic2.api.item.ElectricItem;
import ic2.core.RotationList;
import ic2.core.block.base.tile.TileEntityGeneratorBase;
import ic2.core.inventory.container.ContainerIC2;
import ic2.core.inventory.filters.CommonFilters;
import ic2.core.inventory.gui.GuiComponentContainer;
import ic2.core.inventory.management.AccessRule;
import ic2.core.inventory.management.InventoryHandler;
import ic2.core.inventory.management.SlotType;
import ic2.core.platform.lang.components.base.LocaleComp;
import ic2.core.util.math.Box2D;
import ic2.core.util.math.Vec2i;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import org.jetbrains.annotations.NotNull;
import trinsdar.advancedsolars.AdvancedSolarsClassic;
import trinsdar.advancedsolars.util.AdvancedSolarLang;
import trinsdar.advancedsolars.util.AdvancedSolarsConfig;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.stations.SpaceStationObject;

public class TileEntityAdvancedSolarPanel extends TileEntityGeneratorBase {
    double config;
    int ticker;
    protected double lowerProduction;
    int maxOutput;
    public TileEntityAdvancedSolarPanel() {
        super(4);
        this.tier = 1;
        this.ticker = 127;
        this.production = 16;
        this.lowerProduction = 2.0D;
        this.maxStorage = 32000;
        this.maxOutput = 32;
        this.config = (double) AdvancedSolarsConfig.powerGeneration.energyGeneratorSolarAdvanced;
    }

    protected void addSlots(InventoryHandler handler) {
        handler.registerDefaultSideAccess(AccessRule.None, RotationList.UP);
        handler.registerDefaultSideAccess(AccessRule.Both, RotationList.UP.invert());
        handler.registerDefaultSlotAccess(AccessRule.Both, 0, 1, 2, 3);
        handler.registerDefaultSlotsForSide(RotationList.DOWN, 0, 1, 2, 3);
        handler.registerInputFilter(CommonFilters.ChargeEU, 0, 1, 2, 3);
        handler.registerSlotType(SlotType.Charge, 0, 1, 2, 3);
    }

    @Override
    public ContainerIC2 getGuiContainer(EntityPlayer player) {
        return new ContainerAdvancedSolarPanel(player.inventory, this);
    }

    @Override
    public LocaleComp getBlockName() {
        return AdvancedSolarLang.advancedSolarPanel;
    }

    @Override
    public ResourceLocation getTexture() {
        return new ResourceLocation(AdvancedSolarsClassic.MODID, "textures/sprites/guiadvancedsolarpanel.png");
    }

    public int getMaxOutput(){
        return maxOutput;
    }
    
    public double getProduction() {
        return this.production * this.getMultiplier();
    }
    
    public double getLowerProduction() {
        return this.lowerProduction * this.getMultiplier();
    }
    @Override
    public boolean isConverting() {
        if (this.skyBlockCheck()){
            if (isSunVisible()){
                return this.storage + this.getProduction() <= this.maxStorage;
            }else {
                return this.storage + this.getLowerProduction() <= this.maxStorage;
            }
        }else {
            return false;
        }
    }

    public boolean isInSpace() {
        return this.world.provider.getDimension() == -2;
    }

    public boolean isInFTL() {
            if (isInSpace()) {
                SpaceStationObject object = (SpaceStationObject)SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords(this.getPos());
                if (object != null) {
                    if (object.isInFTL()) return true;
                }
                return false;
            } else {
                return false;
            }
    }
    
    public int getOrbitalDist() {
        if (isInSpace()) {
        SpaceStationObject object = (SpaceStationObject)SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords(this.getPos());
            if (object != null) {
                if (object.isInFTL()) return -1;
                return object.getOrbitingPlanet().getOrbitalDist();
            } else {
                return -1;
            }
        } else {
            DimensionProperties props = DimensionManager.getEffectiveDimId(this.world, this.getPos());
            if (props != null) {
                return props.getOrbitalDist();
            } else {
                return -1;
            }
        }
    }
    
    public double getMultiplier() {
        if (isInOrbitOfStar()) return 10.0;
        if (isInOrbitOfBlackhole()) return 0.01;
        if (getOrbitalDist() == -1) return 0.0;
        if (isInFTL()) return 0.0;
        if (isInSpace() ) return 2/getOrbitalDist();
        if (!isInSpace() ) return 1/getOrbitalDist();
        return 0.0;
    }
    
    public boolean isInOrbitOfBlackhole() {
        if (!isInSpace()) return false;
        SpaceStationObject object = (SpaceStationObject)SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords(this.getPos());
        if (object != null) {
            if (object.isInFTL()) return false;
            if (object.getOrbitingPlanet().isStar() && object.getOrbitingPlanet().hasBlackHole()) return true;
        } else {
            return false;
        }
        return false;
    }
    
    public boolean isInOrbitOfStar() {
        if (!isInSpace()) return false;
        SpaceStationObject object = (SpaceStationObject)SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords(this.getPos());
        if (object != null) {
            if (object.isInFTL()) return false;
            if (object.getOrbitingPlanet().isStar() && !object.getOrbitingPlanet().hasBlackHole()) return true;
        } else {
            return false;
        }
        return false;
    }
    
    @Override
    public boolean gainEnergy() {
        if (this.isConverting()) {
            if (this.skyBlockCheck()){
                if (isSunVisible()){
                    this.storage += this.getProduction();
                }else {
                    this.storage += this.getLowerProduction();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Box2D getEnergyBox() {
        return ContainerAdvancedSolarPanel.chargeBox;
    }

    @Override
    public Vec2i getEnergyPos() {
        return ContainerAdvancedSolarPanel.chargePos;
    }

    @Override
    public Class<? extends GuiScreen> getGuiClass(EntityPlayer player) {
        return GuiComponentContainer.class;
    }

    @Override
    public double getOfferedEnergy() {
        if (isSunVisible()){
            return (double)Math.min(this.storage, this.getProduction());
        }else {
            return Math.min(this.storage, this.getLowerProduction());
        }

    }


    @Override
    public void update() {

        int oldEnergy = this.storage;
        boolean active = this.gainEnergy();
        if (this.storage > 0) {
            for (ItemStack tStack : this.inventory) {
                if (this.storage <= 0) break;
                if (tStack.isEmpty()) continue; // No item to charge
                int charged = (int)(ElectricItem.manager.charge(tStack, (double)this.storage, this.tier, false, false));
                this.storage -= charged;
            }

            if (this.storage > this.maxStorage) {
                this.storage = this.maxStorage;
            }
        }

        if (!this.delayActiveUpdate()) {
            this.setActive(active);
        } else {
            if (this.ticksSinceLastActiveUpdate % this.getDelay() == 0) {
                this.setActive(this.activityMeter > 0);
                this.activityMeter = 0;
            }

            if (active) {
                ++this.activityMeter;
            } else {
                --this.activityMeter;
            }

            ++this.ticksSinceLastActiveUpdate;
        }

        if (oldEnergy != this.storage) {
            this.getNetwork().updateTileGuiField(this, "storage");
        }

        this.updateComparators();
    }

    @Override
    public boolean gainFuel() {
        return false;
    }

    @Override
    public int getOutput() {
        if (skyBlockCheck()){
            if (isSunVisible()){
                return (int)(this.getProduction() * this.config);
            }else {
                return (int)(this.getLowerProduction() * this.config);
            }
        }else {
            return 0;
        }

    }

    public boolean skyBlockCheck(){
        return this.getWorld().canBlockSeeSky(this.getPos().up()) && this.getWorld().provider.hasSkyLight();
    }

    public boolean isSunVisible(){
        return isSunVisible(this.getWorld(), this.getPos().up());
    }

    public static boolean isSunVisible(@NotNull World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        if (world.getWorldTime() %24000  < 12600){
            return BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT) && !biome.canRain() || !world.isRaining() && !world.isThundering();
        }
        return false;
    }

    @Override
    public boolean canSetFacing(EntityPlayer player, EnumFacing facing) {
        return facing != getFacing() && facing.getAxis().isHorizontal();
    }

    public double getWrenchDropRate() {
        return 1.0D;
    }

    public static class TileEntityHybridSolarPanel extends TileEntityAdvancedSolarPanel{
        public TileEntityHybridSolarPanel() {
            this.tier = 2;
            this.production = 128;
            this.lowerProduction = 16.0D;
            this.maxStorage = 100000;
            this.maxOutput = 128;
            this.config = (double) AdvancedSolarsConfig.powerGeneration.energyGeneratorSolarHybrid;
        }

        @Override
        public LocaleComp getBlockName() {
            return AdvancedSolarLang.hybridSolarPanel;
        }
    }

    public static class TileEntityUltimateHybridSolarPanel extends TileEntityAdvancedSolarPanel{
        public TileEntityUltimateHybridSolarPanel() {
            this.tier = 4;
            this.production =1024;
            this.lowerProduction = 128.0D;
            this.maxStorage = 1000000;
            this.maxOutput = 2048;
            this.config = (double) AdvancedSolarsConfig.powerGeneration.energyGeneratorSolarUltimateHybrid;
        }

        @Override
        public LocaleComp getBlockName() {
            return AdvancedSolarLang.ultimateHybridSolarPanel;
        }
    }
}
