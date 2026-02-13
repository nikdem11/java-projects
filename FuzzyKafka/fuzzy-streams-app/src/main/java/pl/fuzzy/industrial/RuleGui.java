package pl.fuzzy.industrial;

import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import net.sourceforge.jFuzzyLogic.rule.LinguisticTerm;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RuleGui {
    public static void show(FuzzyService fuzzyService) {
        JFrame frame = new JFrame("Fuzzy Logic - Panel Sterowania");
        frame.setSize(1000, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // SEKCJA GÓRNA
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField fieldCondition = new JTextField(25);
        JTextField fieldAction = new JTextField(25);
        JButton btnAdd = new JButton("Dodaj i Przeładuj Logikę");
        JLabel labelStatus = new JLabel("Status: System gotowy");

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Warunek (IF...):"), gbc);
        gbc.gridx = 1;
        formPanel.add(fieldCondition, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Wniosek (THEN...):"), gbc);
        gbc.gridx = 1;
        formPanel.add(fieldAction, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(btnAdd, gbc);

        gbc.gridy = 3;
        labelStatus.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(labelStatus, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // SEKCJA DOLNA
        JTabbedPane tabbedPane = new JTabbedPane();
        JTextArea areaRules = new JTextArea();
        JTextArea areaVars = new JTextArea();
        areaRules.setEditable(false);
        areaVars.setEditable(false);

        JPanel chartControlPanel = new JPanel(new FlowLayout((FlowLayout.LEFT)));
        chartControlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JComboBox<String> comboVars = new JComboBox<>();
        JButton btnShowChart = new JButton("Otworz wykres w nowym oknie");

        chartControlPanel.add(new JLabel("Wybierz zmienna: "));
        chartControlPanel.add(comboVars);
        chartControlPanel.add(btnShowChart);

        tabbedPane.addTab("Obecne reguły", new JScrollPane(areaRules));
        tabbedPane.addTab("Zmienne i terminy lingwistyczne", new JScrollPane(areaVars));
        tabbedPane.addTab("Wykresy zbiorow", chartControlPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // LOGIKA ODŚWIEŻANIA
        Runnable refreshPreview = () -> {
            var fb = fuzzyService.getFis().getFunctionBlock("predictive_maintenance");
            Map<String, Variable> vars = fb.getVariables();

            // 1. Podgląd reguł
            String rawRules = fb.getRuleBlocks().values().stream()
                    .flatMap(rb -> rb.getRules().stream())
                    .map(Rule::toString)
                    .collect(Collectors.joining("\n"));
            areaRules.setText(rawRules);

            // 2. Podgląd zmiennych
            StringBuilder varsInfo = new StringBuilder("--- ZMIENNE WEJŚCIOWE ---\n");

            // PIERWSZY PRZEBIEG: Tylko wejścia
            vars.values().stream().filter(Variable::isInput).forEach(v -> {
                List<String> sortedTerms = v.getLinguisticTerms().values().stream()
                        .sorted(Comparator.comparingDouble(t -> t.getMembershipFunction().getParameter(0)))
                        .map(LinguisticTerm::getTermName)
                        .collect(Collectors.toList());

                varsInfo.append(v.getName()).append(" ").append(sortedTerms).append("\n");
            });

            // DRUGI PRZEBIEG: Tylko wyjścia
            varsInfo.append("\n--- ZMIENNE WYJŚCIOWE ---\n");
            vars.values().stream().filter(Variable::isOutput).forEach(v -> {
                List<String> sortedTerms = v.getLinguisticTerms().values().stream()
                        .sorted(Comparator.comparingDouble(t -> t.getMembershipFunction().getParameter(0)))
                        .map(LinguisticTerm::getTermName)
                        .collect(Collectors.toList());

                varsInfo.append(v.getName()).append(" ").append(sortedTerms).append("\n");
            });

            areaVars.setText(varsInfo.toString());
            comboVars.removeAllItems();
            vars.keySet().forEach(comboVars::addItem);

        };

        btnShowChart.addActionListener( e -> {
            String selectedName = (String) comboVars.getSelectedItem();
            if (selectedName != null) {
                Variable var = fuzzyService.getFis()
                        .getFunctionBlock("predictive_maintenance")
                        .getVariable(selectedName);
                if (var != null) {
                    JFuzzyChart.get().chart(var, true);
                }
            }
        });

        btnAdd.addActionListener(e -> {
            if (fuzzyService.validateRule(fieldCondition.getText(), fieldAction.getText())) {
                fuzzyService.addNewRuleToFile(fieldCondition.getText(), fieldAction.getText());
                fuzzyService.loadRules();
                refreshPreview.run();
                labelStatus.setText("Status: Dodano i przeładowano!");
                labelStatus.setForeground(new Color(0, 120, 0));
            } else {
                labelStatus.setText("Status: Błąd składni!");
                labelStatus.setForeground(Color.RED);
            }
        });

        refreshPreview.run();
        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}