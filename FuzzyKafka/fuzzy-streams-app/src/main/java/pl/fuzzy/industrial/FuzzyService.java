package pl.fuzzy.industrial;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import java.util.HashSet;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public class FuzzyService {
    private volatile FIS fis;
    private final String fclPath;

    public FuzzyService(String fclPath) {
        this.fclPath = fclPath;
        loadRules();
    }

    public void loadRules() {
        FIS newFis = FIS.load(fclPath, true);
        if (newFis != null) {
            this.fis = newFis;
            System.out.println("[System] Plik .fcl załadowany poprawnie.");
        }
    }

    public FIS getFis() {
        return fis;
    }

    public Set<String> getAvailableVariables() {
        return fis.getFunctionBlock("predictive_maintenance").getVariables().keySet();
    }

    public boolean validateRule(String condition, String action) {
        Set<String> validVars = getAvailableVariables();
        boolean conditionValid = validVars.stream().anyMatch(condition::contains);
        boolean actionValid = validVars.stream().anyMatch(action::contains);
        return conditionValid && actionValid && condition.contains("IS") && action.contains("IS");
    }

    public void addNewRuleToFile(String condition, String action) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(fclPath));
            int ruleCount = 0;
            int insertIndex = -1;

            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().startsWith("RULE")) ruleCount++;
                if (lines.get(i).contains("END_RULEBLOCK")) {
                    insertIndex = i;
                    break;
                }
            }

            if (insertIndex != -1) {
                String newRule = "    RULE " + ruleCount + " : IF " + condition + " THEN " + action + ";";
                lines.add(insertIndex, newRule);
                Files.write(Paths.get(fclPath), lines);
                System.out.println("[System] Dodano regułę nr " + ruleCount);
            }
        } catch (IOException e) {
            System.err.println("[BŁĄD] Nie można zapisać do pliku FCL.");
        }
    }

    public Set<String> getInputVariables() {
        Map<String, Variable> allVars = fis.getFunctionBlock("predictive_maintenance").getVariables();
        Set<String> inputs = new HashSet<>();

        for (Map.Entry<String, Variable> entry : allVars.entrySet()) {
            // Sprawdzamy, czy zmienna jest wejściowa
            if (entry.getValue().isInput()) {
                inputs.add(entry.getKey());
            }
        }
        return inputs;
    }

}