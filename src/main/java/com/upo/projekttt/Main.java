package com.upo.projekttt;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Main extends Application {
    private List<CalculationModule> modules;
    private List<Boolean> moduleStatus;
    private TextField inputField1;
    private TextField inputField2;
    private Button calculateButton;
    private List<Label> resultLabels;
    private List<HBox> bitPanels;
    private List<Button[]> bitButtons;
    private Label messageLabel;
    private Button okButton;
    private boolean tmrActivated;
    private boolean allResultsDifferent;
    private String majorityResult;
    private final int BUTTON_SIZE = 50;
    private Canvas canvas;

    private GridPane gridPane;
    private List<String[]> binaryPairs;
    private String previousInput1 = "";
    private String previousInput2 = "";
    private int faultyModuleIndex = -1;
    private final int MAX_LENGTH = 8;

    @Override
    public void start(Stage primaryStage) {
        modules = new ArrayList<>();
        moduleStatus = new ArrayList<>();
        modules.add(new CalculationModule());
        modules.add(new CalculationModule());
        moduleStatus.add(true);
        moduleStatus.add(true);

        resultLabels = new ArrayList<>();
        bitPanels = new ArrayList<>();
        bitButtons = new ArrayList<>();

        tmrActivated = false;
        allResultsDifferent = false;
        majorityResult = "";

        binaryPairs = loadBinaryPairs("src/main/resources/com/upo/projekttt/binary_pairs.txt");

        primaryStage.setTitle("JTC TO TMR Simulation");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        HBox inputPanel = new HBox(10);
        inputPanel.setAlignment(Pos.CENTER);
        inputField1 = new TextField();
        inputField1.setPromptText("Wartość 1");
        inputField2 = new TextField();
        inputField2.setPromptText("Wartość 2");
        calculateButton = new Button("Oblicz XOR");
        inputPanel.getChildren().addAll(new Label("Wartość 1:"), inputField1, new Label("Wartość 2:"), inputField2, calculateButton);
        root.setTop(inputPanel);

        setTextFieldLimit(inputField1, MAX_LENGTH);
        setTextFieldLimit(inputField2, MAX_LENGTH);

        gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(20);
        gridPane.setVgap(20);

        canvas = new Canvas(800, 400);
        Pane canvasPane = new Pane(canvas);
        canvasPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

        StackPane centerPane = new StackPane();
        centerPane.getChildren().addAll(canvasPane, gridPane);
        root.setCenter(centerPane);

        VBox bottomPanel = new VBox(10);
        bottomPanel.setAlignment(Pos.CENTER);
        messageLabel = new Label(" ");
        messageLabel.setStyle("-fx-font-size: 16px;");
        okButton = new Button("OK");
        okButton.setVisible(false);
        bottomPanel.getChildren().addAll(messageLabel, okButton);
        root.setBottom(bottomPanel);

        calculateButton.setOnAction(e ->{
                    calculateResults();
                    drawConnections();
                }
        );
        okButton.setOnAction(e -> handleOkButton());

        addModuleUI("Wynik Modułu 1:", 0);
        addModuleUI("Wynik Modułu 2:", 1);

        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.show();

        drawConnections();
    }

    private void setTextFieldLimit(TextField textField, int maxLength) {
        textField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= maxLength ? change : null));
    }

    private void calculateResults() {
        String input1 = inputField1.getText();
        String input2 = inputField2.getText();

        if (input1.isEmpty() || input2.isEmpty() || (input1.equals(previousInput1) && input2.equals(previousInput2))) {
            String[] randomPair = getRandomBinaryPair();
            input1 = randomPair[0];
            input2 = randomPair[1];
            inputField1.setText(input1);
            inputField2.setText(input2);
        }

        previousInput1 = input1;
        previousInput2 = input2;

        resetModules();

        for (int i = 0; i < modules.size(); i++) {
            if (moduleStatus.get(i)) {
                modules.get(i).setInputs(input1, input2);
            }
        }

        displayResults();
        createBitButtons();

        boolean allDifferent = true;
        for (int i = 0; i < modules.size(); i++) {
            if (moduleStatus.get(i)) {
                String result = modules.get(i).getResult();
                for (int j = 0; j < modules.size(); j++) {
                    if (i != j && moduleStatus.get(j) && result.equals(modules.get(j).getResult())) {
                        allDifferent = false;
                        break;
                    }
                }
            }
        }

        if (allDifferent) {
            majorityResult = "";
        } else {
            for (int i = 0; i < modules.size(); i++) {
                if (moduleStatus.get(i)) {
                    majorityResult = modules.get(i).getResult();
                    break;
                }
            }
        }

        messageLabel.setText(" ");
        drawConnections();
    }



    private List<String[]> loadBinaryPairs(String filename) {
        try {
            return Files.lines(Paths.get(filename))
                    .map(line -> line.split(" "))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String[] getRandomBinaryPair() {
        Random random = new Random();
        return binaryPairs.get(random.nextInt(binaryPairs.size()));
    }

    private void displayResults() {
        for (int i = 0; i < modules.size(); i++) {
            resultLabels.get(i).setText("Wynik Modułu " + (i + 1) + ": " + modules.get(i).getResult());
        }
    }

    private void createBitButtons() {
        for (int i = 0; i < modules.size(); i++) {
            HBox bitPanel = bitPanels.get(i);
            bitPanel.getChildren().clear();

            String result = modules.get(i).getResult();
            Button[] buttons = new Button[result.length()];
            for (int j = 0; j < result.length(); j++) {
                final int moduleIndex = i;
                final int bitPosition = j;
                buttons[j] = new Button(String.valueOf(result.charAt(j)));
                buttons[j].setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
                if (moduleStatus.get(moduleIndex)) {
                    buttons[j].setOnAction(e -> {
                        modules.get(moduleIndex).introduceFault(bitPosition);
                        buttons[bitPosition].setText(String.valueOf(modules.get(moduleIndex).getResult().charAt(bitPosition)));
                        resultLabels.get(moduleIndex).setText("Wynik Modułu " + (moduleIndex + 1) + ": " + modules.get(moduleIndex).getResult());
                        checkForFault();
                        drawConnections();
                    });
                } else {
                    buttons[j].setDisable(true);
                }
                bitPanel.getChildren().add(buttons[j]);
            }
            bitButtons.set(i, buttons);
        }
    }

    private void checkForFault() {
        boolean discrepancyFound = false;
        String firstResult = null;
        for (int i = 0; i < modules.size(); i++) {
            if (moduleStatus.get(i)) {
                firstResult = modules.get(i).getResult();
                break;
            }
        }
        for (int i = 0; i < modules.size(); i++) {
            if (moduleStatus.get(i) && !modules.get(i).getResult().equals(firstResult)) {
                discrepancyFound = true;
                majorityResult = "";
                break;
            }
        }

        if (discrepancyFound) {
            messageLabel.setText("Znaleziono rozbieżność, zostanie dodany nowy moduł.");
            okButton.setVisible(true);
        } else {
            okButton.setVisible(false);
        }
    }

    private void handleOkButton() {
        if (tmrActivated) {
            resolveTMR();
        } else if (allResultsDifferent) {
            resetModules();
        } else {
            if (modules.size() >= 3) {
                replaceFaultyModule();
            } else {
                addNewModule();
            }
        }
    }

    private void addNewModule() {
        tmrActivated = true;
        CalculationModule newModule = new CalculationModule();
        newModule.setInputs(inputField1.getText(), inputField2.getText());
        modules.add(newModule);
        moduleStatus.add(true);
        addModuleUI("Wynik Modułu " + modules.size() + ":", modules.size() - 1);
        messageLabel.setText(" ");
        okButton.setVisible(true);
        createBitButtons();
        drawConnections();
    }

    private void replaceFaultyModule() {
        if (faultyModuleIndex >= 0 && faultyModuleIndex < modules.size()) {
            CalculationModule newModule = new CalculationModule();
            newModule.setInputs(inputField1.getText(), inputField2.getText());
            modules.set(faultyModuleIndex, newModule);
            moduleStatus.set(faultyModuleIndex, true);
            updateModuleUI(faultyModuleIndex);
            createBitButtons();
            messageLabel.setText("Moduł " + (faultyModuleIndex + 1) + " został zastąpiony.");
            faultyModuleIndex = -1;
            tmrActivated = true;
            okButton.setVisible(true);

            PauseTransition pause = new PauseTransition(Duration.millis(50));
            pause.setOnFinished(event -> drawConnections());
            pause.play();
        }
    }

    private void updateModuleUI(int index) {
        VBox moduleBox = (VBox) gridPane.getChildren().get(index);
        HBox bitPanel = bitPanels.get(index);
        Label resultLabel = resultLabels.get(index);

        moduleBox.getChildren().clear();
        Label moduleLabel = new Label("Moduł " + (index + 1));
        resultLabel.setText("Wynik Modułu " + (index + 1) + ": " + modules.get(index).getResult());
        moduleBox.getChildren().addAll(moduleLabel, bitPanel, resultLabel);

        drawConnections();
    }


    private void addModuleUI(String labelText, int index) {
        VBox moduleBox = new VBox(5);
        moduleBox.setStyle("-fx-border-color: black; -fx-padding: 10;");
        moduleBox.setAlignment(Pos.CENTER);

        Label moduleLabel = new Label("Moduł " + (index + 1));
        HBox bitPanel = new HBox(5);
        Label resultLabel = new Label(labelText + " " + modules.get(index).getResult());

        moduleBox.getChildren().addAll(moduleLabel, bitPanel, resultLabel);

        gridPane.add(moduleBox, 0, index);
        bitPanels.add(bitPanel);
        resultLabels.add(resultLabel);
        bitButtons.add(new Button[0]);

        PauseTransition pause = new PauseTransition(Duration.millis(50));
        pause.setOnFinished(event -> {
            resultLabel.setText(labelText + " " + modules.get(index).getResult());
            drawConnections();
        });
        pause.play();
    }


    private void drawConnections() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double voterX = 600;
        double voterY = 204;

        int numberOfModules = gridPane.getChildren().size();
        double initialOffset = voterY - ((numberOfModules - 1) * 40);

        for (int i = 0; i < numberOfModules; i++) {
            VBox moduleBox = (VBox) gridPane.getChildren().get(i);
            double moduleX = moduleBox.getBoundsInParent().getMaxX();
            double moduleY = moduleBox.getBoundsInParent().getMinY() + moduleBox.getBoundsInParent().getHeight() / 2;
            gc.strokeLine(moduleX, moduleY, voterX - 50, voterY);
        }

        gc.strokeRect(voterX - 50, voterY - 30, 100, 60);
        gc.fillText("Voter", voterX - 15, voterY + 5);
        gc.strokeLine(voterX + 50, voterY, voterX + 100, voterY);

        double startX = voterX + 105;
        gc.fillText(majorityResult, startX, voterY + 5);
    }



    private void resolveTMR() {
        List<Integer> activeModules = new ArrayList<>();
        for (int i = 0; i < modules.size(); i++) {
            if (moduleStatus.get(i)) {
                activeModules.add(i);
            }
        }

        if (activeModules.size() < 2) {
            messageLabel.setText("Za mało aktywnych modułów. Dodanie nowego modułu.");
            addNewModule();
            return;
        }

        int[] voteCount = new int[modules.size()];
        for (int i = 0; i < activeModules.size(); i++) {
            int index = activeModules.get(i);
            String result = modules.get(index).getResult();
            for (int j = 0; j < activeModules.size(); j++) {
                if (result.equals(modules.get(activeModules.get(j)).getResult())) {
                    voteCount[index]++;
                }
            }
        }

        int maxVotes = 0;
        int maxIndex = -1;
        boolean allDifferent = true;
        for (int i = 0; i < voteCount.length; i++) {
            if (voteCount[i] > maxVotes) {
                maxVotes = voteCount[i];
                maxIndex = i;
            }
            if (voteCount[i] > 1) {
                allDifferent = false;
            }
        }

        if (allDifferent) {
            messageLabel.setText("Nie można rozwiązać głosowania, wszystkie wyniki są różne.");
            allResultsDifferent = true;
            okButton.setVisible(true);
            tmrActivated = false;
            majorityResult = "";

            for (int i = 0; i < modules.size(); i++) {
                if (moduleStatus.get(i)) {
                    resultLabels.get(i).setTextFill(Color.RED);
                    resultLabels.get(i).setStyle("-fx-underline: true; -fx-text-fill: red;");
                }
            }
        } else {
            majorityResult = modules.get(maxIndex).getResult();
            messageLabel.setText("Moduł " + (maxIndex + 1) + " uznany za poprawny na podstawie głosowania większościowego.");

            for (int i = 0; i < modules.size(); i++) {
                if (moduleStatus.get(i) && !modules.get(i).getResult().equals(majorityResult)) {
                    freezeButtons(bitButtons.get(i));
                    resultLabels.get(i).setTextFill(Color.RED);
                    resultLabels.get(i).setStyle("-fx-underline: true; -fx-text-fill: red;");
                    messageLabel.setText(messageLabel.getText() + " Moduł " + (i + 1) + " zostanie odłączony.");
                    moduleStatus.set(i, false);
                    faultyModuleIndex = i;
                } else if (moduleStatus.get(i)) {
                    resultLabels.get(i).setTextFill(Color.GREEN);
                    resultLabels.get(i).setStyle("-fx-underline: true; -fx-text-fill: green;");
                }
            }

            tmrActivated = false;
            okButton.setVisible(false);
        }

        drawConnections();
    }


    private void resetModules() {
        modules = new ArrayList<>();
        moduleStatus = new ArrayList<>();
        modules.add(new CalculationModule());
        modules.add(new CalculationModule());
        moduleStatus.add(true);
        moduleStatus.add(true);

        resultLabels = new ArrayList<>();
        bitPanels = new ArrayList<>();
        bitButtons = new ArrayList<>();

        tmrActivated = false;
        allResultsDifferent = false;
        majorityResult = "";

        gridPane.getChildren().clear();

        addModuleUI("Wynik Modułu 1:", 0);
        addModuleUI("Wynik Modułu 2:", 1);

        messageLabel.setText(" ");
        okButton.setVisible(false);

        drawConnections();
    }

    private void freezeButtons(Button[] buttons) {
        for (Button button : buttons) {
            button.setDisable(true);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
