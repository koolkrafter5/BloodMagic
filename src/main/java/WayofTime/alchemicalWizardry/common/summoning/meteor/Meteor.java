package WayofTime.alchemicalWizardry.common.summoning.meteor;

import java.io.*;

import com.google.gson.JsonObject;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import cpw.mods.fml.common.registry.GameRegistry;

public class Meteor {

    private String[] ores;
    private int radius;
    private int cost;
    private String focusModId;
    private String focusName;
    private int focusMeta;
    private String[] filler;
    private int fillerChance;

    public static void loadConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File("config/BloodMagic/meteors");
        File[] files = file.listFiles();
        if (files != null) {
            try {
                for (File f : files) {
                    if (f.isDirectory()) {
                        continue;
                    }
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    Meteor m = gson.fromJson(br, Meteor.class);
                    MeteorRegistry.registerMeteorParadigm(
                            findItemStack(m.focusModId, m.focusName, m.focusMeta),
                            m.ores,
                            m.radius,
                            m.cost,
                            m.filler,
                            m.fillerChance);
                }
            } catch (FileNotFoundException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private static ItemStack findItemStack(String modid, String name, int meta) {
        ItemStack is = GameRegistry.findItemStack(modid, name, 1);
        if (is == null) return null;
        Items.feather.setDamage(is, meta);
        return is;
    }
}
