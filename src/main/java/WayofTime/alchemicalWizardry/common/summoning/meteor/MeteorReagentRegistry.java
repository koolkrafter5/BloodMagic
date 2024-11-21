package WayofTime.alchemicalWizardry.common.summoning.meteor;

import WayofTime.alchemicalWizardry.api.alchemy.energy.Reagent;
import WayofTime.alchemicalWizardry.api.alchemy.energy.ReagentRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

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
                for (String reagent : ReagentRegistry.reagentList.keySet()) {
                    File f = new File("config/BloodMagic/meteors/reagents/" + reagent);
                    if (!f.isFile()) {
                        continue;
                    }
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    MeteorReagent r = gson.fromJson(br, MeteorReagent.class);
                    reagents.put(ReagentRegistry.getReagentForKey(reagent), r);
                }
            } catch (FileNotFoundException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    // Return the one largest increase to the radius.
    private static int getLargestRadiusIncrease(Reagent[] reagentList) {
        int increase = 0;
        for (Reagent r : reagentList) {
            int change = reagents.get(r).radiusChange;
            if (change > increase) {
                increase = change;
            }
        }
        return increase;
    }

    // Return the one largest decrease to the radius.
    private static int getLargestRadiusDecrease(Reagent[] reagentList) {
        int decrease = 0;
        for (Reagent r : reagentList) {
            int change = reagents.get(r).radiusChange;
            if (change < decrease) {
                decrease = change;
            }
        }
        return decrease;
    }

    // Return the one largest filler chance multiplier (above 1.0).
    private static double getLargestFillerChanceMultiplier(Reagent[] reagentList) {
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
    private static double getSmallestFillerChanceMultiplier(Reagent[] reagentList) {
        double mult = 1.0;
        for (Reagent r : reagentList) {
            double change = reagents.get(r).fillerChanceMultiplier;
            if (change > mult) {
                mult = change;
            }
        }
        return mult;
    }

    // Return a list of the blocks that the given reagents will replace the filler with.
    private static List<MeteorParadigmComponent> getFillerList(Reagent[] reagentList) {
        List<MeteorParadigmComponent> fillerList = new ArrayList<>();
        for (Reagent r : reagentList) {
            List<MeteorParadigmComponent> filler = reagents.get(r).fillerList;
            if (!filler.isEmpty()) {
                fillerList.addAll(filler);
            }
        }
        return fillerList;
    }

    public static void generateDefaultConfig() {
        Map<String, String[]> lineMap = new HashMap<>();
        lineMap.put("terrae", new String[]{"{",
                "  \"radiusChange\": 1,",
                "  \"fillerChanceMultiplier\": 1.06",
                "}",});
        lineMap.put("orbisTerrae", new String[]{"{",
                "  \"radiusChange\": 2,",
                "  \"fillerChanceMultiplier\": 1.12",
                "}",});
        lineMap.put("tenebrae", new String[]{"{",
                "  \"fillerList\":  [\"minecraft:obsidian:0:180\"]",
                "}",});
        lineMap.put("incendium", new String[]{"{",
                "  \"fillerList\":  [",
                "    \"minecraft:netherrack:0:60\",",
                "    \"minecraft:glowstone:0:60\",",
                "    \"minecraft:soul_sand:0:60\"",
                "  ]",
                "}",});
        lineMap.put("crystallos", new String[]{"{",
                "  \"fillerList\":  [\"minecraft:ice:0:180\"]",
                "}",});
        try {
            Files.createDirectories(Paths.get("config/BloodMagic/meteors/reagents/"));
            String[] reagents = {"terrae", "orbisTerrae", "tenebrae", "incendium", "crystallos"};
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
