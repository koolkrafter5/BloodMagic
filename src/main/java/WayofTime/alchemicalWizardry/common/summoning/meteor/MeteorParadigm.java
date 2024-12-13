package WayofTime.alchemicalWizardry.common.summoning.meteor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import WayofTime.alchemicalWizardry.AlchemicalWizardry;
import WayofTime.alchemicalWizardry.api.alchemy.energy.Reagent;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.common.blocks.TileEntityOres;

public class MeteorParadigm {

    public List<MeteorParadigmComponent> componentList = new ArrayList<>();
    public List<MeteorParadigmComponent> fillerList = new ArrayList<>();
    public ItemStack focusStack;
    public int radius;
    public int cost;
    public int fillerChance; // Out of 100

    public static Random rand = new Random();

    public MeteorParadigm(ItemStack focusStack, int radius, int cost) {
        new MeteorParadigm(focusStack, radius, cost, 0);
    }

    public MeteorParadigm(ItemStack focusStack, int radius, int cost, int fillerChance) {
        this.focusStack = focusStack;
        this.radius = radius;
        this.cost = cost;
        this.fillerChance = fillerChance;
    }

    // modId:itemName:meta:weight
    private static final Pattern itemNamePattern = Pattern.compile("(.*):(.*):(\\d+):(\\d+)");
    // OREDICT:oreDictName:weight
    private static final Pattern oredictPattern = Pattern.compile("OREDICT:(.*):(\\d+)");

    public static List<MeteorParadigmComponent> parseStringArray(String[] blockArray) {
        List<MeteorParadigmComponent> addList = new ArrayList<>();
        for (int i = 0; i < blockArray.length; ++i) {
            String blockName = blockArray[i];
            boolean success = false;

            Matcher matcher = itemNamePattern.matcher(blockName);
            if (matcher.matches()) {
                String modID = matcher.group(1);
                String itemName = matcher.group(2);
                int meta = Integer.parseInt(matcher.group(3));
                int weight = Integer.parseInt(matcher.group(4));

                ItemStack stack = GameRegistry.findItemStack(modID, itemName, 1);
                if (stack != null && stack.getItem() instanceof ItemBlock) {
                    stack.setItemDamage(meta);
                    addList.add(new MeteorParadigmComponent(stack, weight));
                    success = true;
                }

            } else if ((matcher = oredictPattern.matcher(blockName)).matches()) {
                String oreDict = matcher.group(1);
                int weight = Integer.parseInt(matcher.group(2));

                List<ItemStack> list = OreDictionary.getOres(oreDict);
                for (ItemStack stack : list) {
                    if (stack != null && stack.getItem() instanceof ItemBlock) {
                        addList.add(new MeteorParadigmComponent(stack, weight));
                        success = true;
                        break;
                    }
                }

            } else {
                // Legacy config
                String oreDict = blockName;
                int weight = Integer.parseInt(blockArray[++i]);

                List<ItemStack> list = OreDictionary.getOres(oreDict);
                for (ItemStack stack : list) {
                    if (stack != null && stack.getItem() instanceof ItemBlock) {
                        addList.add(new MeteorParadigmComponent(stack, weight));
                        success = true;
                        break;
                    }
                }
            }

            if (!success) {
                AlchemicalWizardry.logger.warn("Unable to add Meteor Paradigm \"" + blockName + "\"");
            }
        }
        return addList;
    }

    public int getTotalListWeight(List<MeteorParadigmComponent> blockList) {
        int totalWeight = 0;
        for (MeteorParadigmComponent mpc : blockList) {
            totalWeight += mpc.getWeight();
        }
        return totalWeight;
    }

    public void createMeteorImpact(World world, int x, int y, int z, ArrayList<Reagent> reagents) {
        int radius = getNewRadius(this.radius, reagents);
        int fillerChance = getNewFillerChance(this.fillerChance, reagents);

        world.createExplosion(null, x, y, z, radius * 4, AlchemicalWizardry.doMeteorsDestroyBlocks);

        List<MeteorParadigmComponent> fillerList;

        fillerList = getNewFillerList(this.fillerList, reagents);

        int totalComponentWeight = getTotalListWeight(componentList);
        int totalFillerWeight = getTotalListWeight(fillerList);

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int k = -radius; k <= radius; k++) {
                    if (i * i + j * j + k * k >= (radius + 0.50f) * (radius + 0.50f)) {
                        continue;
                    }

                    if (!world.isAirBlock(x + i, y + j, z + k)) {
                        continue;
                    }

                    if (fillerChance <= 0 || world.rand.nextInt(100) >= fillerChance) {
                        setMeteorBlock(x + i, y + j, z + k, world, componentList, totalComponentWeight);
                    } else {
                        setMeteorBlock(x + i, y + j, z + k, world, fillerList, totalFillerWeight);
                    }
                }
            }
        }
    }

    private int getNewRadius(int radius, ArrayList<Reagent> reagents) {
        radius += MeteorReagentRegistry.getLargestRadiusIncrease(reagents);
        radius += MeteorReagentRegistry.getLargestRadiusDecrease(reagents);
        return Math.max(radius, 1);
    }

    private int getNewFillerChance(int fillerChance, ArrayList<Reagent> reagents) {
        fillerChance += MeteorReagentRegistry.getLargestFillerChanceIncrease(reagents);
        fillerChance += MeteorReagentRegistry.getLargestFillerChanceDecrease(reagents);
        fillerChance *= MeteorReagentRegistry.getLargestFillerChanceMultiplier(reagents);
        fillerChance *= MeteorReagentRegistry.getSmallestFillerChanceMultiplier(reagents);
        return Math.min(fillerChance, 100);
    }

    private List<MeteorParadigmComponent> getNewFillerList(List<MeteorParadigmComponent> fillerList,
            ArrayList<Reagent> reagents) {
        List<MeteorParadigmComponent> reagentFillers = new ArrayList<>(MeteorReagentRegistry.getFillerList(reagents));
        if (reagentFillers.isEmpty()) {
            return fillerList;
        } else {
            return reagentFillers;
        }
    }

    private void setMeteorBlock(int x, int y, int z, World world, List<MeteorParadigmComponent> blockList,
            int totalListWeight) {
        int randNum = world.rand.nextInt(totalListWeight);
        for (MeteorParadigmComponent mpc : blockList) {
            randNum -= mpc.getWeight();

            if (randNum < 0) {
                ItemStack blockStack = mpc.getValidBlockParadigm();
                if (blockStack != null && blockStack.getItem() instanceof ItemBlock) {
                    ((ItemBlock) blockStack.getItem())
                            .placeBlockAt(blockStack, null, world, x, y, z, 0, 0, 0, 0, blockStack.getItemDamage());
                    if (AlchemicalWizardry.isGregTechLoaded) setGTOresNaturalIfNeeded(world, x, y, z);
                    world.markBlockForUpdate(x, y, z);
                    break;
                }
            }
        }
    }

    @Optional.Method(modid = "gregtech")
    private static void setGTOresNaturalIfNeeded(World world, int x, int y, int z) {
        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityOres) {
            ((TileEntityOres) tileEntity).mNatural = true;
        }
    }
}
