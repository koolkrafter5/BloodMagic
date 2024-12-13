package WayofTime.alchemicalWizardry.common.summoning.meteor;

import static WayofTime.alchemicalWizardry.api.alchemy.energy.ReagentRegistry.reagentList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import WayofTime.alchemicalWizardry.AlchemicalWizardry;
import WayofTime.alchemicalWizardry.api.alchemy.energy.Reagent;
import WayofTime.alchemicalWizardry.api.alchemy.energy.ReagentRegistry;

public class MeteorReagentRegistry {

    public static Map<Reagent, MeteorReagent> reagents = new HashMap<>();

    public static void loadConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File("config/BloodMagic/meteors/reagents/");
        if (!file.isDirectory()) {
            MeteorReagentRegistry.generateDefaultConfig();
        }
        File[] files = file.listFiles();
        if (files != null) {
            try {
                for (String reagent : reagentList.keySet()) {
                    File f = new File("config/BloodMagic/meteors/reagents/" + reagent + ".json");
                    if (!f.isFile()) {
                        continue;
                    }
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    MeteorReagent r = gson.fromJson(br, MeteorReagent.class);
                    if (r.fillerList.length > 0) {
                        r.parsedFillerList = MeteorParadigm.parseStringArray(r.fillerList);
                    }
                    reagents.put(ReagentRegistry.getReagentForKey(reagent), r);
                }
            } catch (FileNotFoundException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    // Return the one largest radius increase.
    public static int getLargestRadiusIncrease(ArrayList<Reagent> reagentList) {
        int increase = 0;
        for (Reagent r : reagentList) {
            int change = reagents.get(r).radiusChange;
            if (change > increase) {
                increase = change;
            }
        }
        return increase;
    }

    // Return the one largest radius decrease.
    public static int getLargestRadiusDecrease(ArrayList<Reagent> reagentList) {
        int decrease = 0;
        for (Reagent r : reagentList) {
            int change = reagents.get(r).radiusChange;
            if (change < decrease) {
                decrease = change;
            }
        }
        return decrease;
    }

    // Return the one largest filler chance increase (above 0).
    public static int getLargestFillerChanceIncrease(ArrayList<Reagent> reagentList) {
        int mult = 1;
        for (Reagent r : reagentList) {
            int change = reagents.get(r).fillerChanceChange;
            if (change > mult) {
                mult = change;
            }
        }
        return mult;
    }

    // Return the one largest filler chance decrease (below 0).
    public static int getLargestFillerChanceDecrease(ArrayList<Reagent> reagentList) {
        int mult = 1;
        for (Reagent r : reagentList) {
            int change = reagents.get(r).fillerChanceChange;
            if (change < mult) {
                mult = change;
            }
        }
        return mult;
    }

    // Return the one largest filler chance multiplier (above 1.0).
    public static double getLargestFillerChanceMultiplier(ArrayList<Reagent> reagentList) {
        double mult = 1.0;
        for (Reagent r : reagentList) {
            double change = reagents.get(r).fillerChanceMultiplier;
            if (change > mult) {
                mult = change;
            }
        }
        return mult;
    }

    // Return the one smallest filler chance multiplier (below 1.0).
    public static double getSmallestFillerChanceMultiplier(ArrayList<Reagent> reagentList) {
        double mult = 1.0;
        for (Reagent r : reagentList) {
            double change = reagents.get(r).fillerChanceMultiplier;
            if (change < mult) {
                mult = change;
            }
        }
        return mult;
    }

    // Return a list of the blocks that the given reagents will use to replace filler.
    public static List<MeteorParadigmComponent> getFillerList(ArrayList<Reagent> reagentList) {
        List<MeteorParadigmComponent> fillerList = new ArrayList<>();
        for (Reagent r : reagentList) {
            List<MeteorParadigmComponent> filler = reagents.get(r).parsedFillerList;
            if (!filler.isEmpty()) {
                fillerList.addAll(filler);
            }
        }
        return fillerList;
    }

    public static boolean doExplosions(ArrayList<Reagent> reagentList) {
        for (Reagent r : reagentList) {
            if (reagents.get(r).disableExplosions) {
                return false;
            }
        }
        return true;
    }

    public static boolean doMeteorsDestroyBlocks(ArrayList<Reagent> reagentList) {
        for (Reagent r : reagentList) {
            if (reagents.get(r).toggleExplosionBlockDamage) {
                return !AlchemicalWizardry.doMeteorsDestroyBlocks;
            }
        }
        return AlchemicalWizardry.doMeteorsDestroyBlocks;
    }

    public static void generateDefaultConfig() {
        Map<String, String[]> lineMap = new HashMap<>();
        lineMap.put(
                "terrae",
                new String[] { "{", "  \"radiusChange\": 1,", "  \"fillerChanceMultiplier\": 1.06", "}", });
        lineMap.put(
                "orbisTerrae",
                new String[] { "{", "  \"radiusChange\": 2,", "  \"fillerChanceMultiplier\": 1.12", "}", });
        lineMap.put("tenebrae", new String[] { "{", "  \"fillerList\":  [\"minecraft:obsidian:0:180\"]", "}", });
        lineMap.put(
                "incendium",
                new String[] { "{", "  \"fillerList\":  [", "    \"minecraft:netherrack:0:60\",",
                        "    \"minecraft:glowstone:0:60\",", "    \"minecraft:soul_sand:0:60\"", "  ]", "}", });
        lineMap.put("crystallos", new String[] { "{", "  \"fillerList\":  [\"minecraft:ice:0:180\"]", "}", });
        try {
            Files.createDirectories(Paths.get("config/BloodMagic/meteors/reagents/"));
            String[] reagents = { "terrae", "orbisTerrae", "tenebrae", "incendium", "crystallos" };
            for (String reagent : reagents) {
                Path path = Paths.get("config/BloodMagic/meteors/reagents/" + reagent + ".json");
                Files.createFile(path);
                Files.write(path, Arrays.asList(lineMap.get(reagent)), StandardOpenOption.WRITE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
