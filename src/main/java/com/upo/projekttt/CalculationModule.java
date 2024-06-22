package com.upo.projekttt;

public class CalculationModule {
    private boolean faulty;
    private String binaryInput1;
    private String binaryInput2;
    private String result;

    public CalculationModule() {
        this.faulty = false;
    }

    public void setInputs(String input1, String input2) {
        this.binaryInput1 = input1;
        this.binaryInput2 = input2;
        calculateResult();
    }

    private void calculateResult() {
        this.result = BinaryXOR.xor(binaryInput1, binaryInput2);
    }

    public String getResult() {
        return result;
    }

    public void introduceFault(int bitPosition) {
        char[] resultArray = result.toCharArray();
        resultArray[bitPosition] = resultArray[bitPosition] == '0' ? '1' : '0';
        this.result = new String(resultArray);
        this.faulty = true;
    }

    public boolean isFaulty() {
        return faulty;
    }
}