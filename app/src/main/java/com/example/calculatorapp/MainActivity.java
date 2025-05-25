package com.example.calculatorapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextView displayTextView;
    private StringBuilder expressionDisplayBuilder = new StringBuilder();
    private StringBuilder currentNumberBuilder = new StringBuilder();

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##########");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayTextView = findViewById(R.id.calculator_display_textview);

        List<Integer> numberButtonIds = Arrays.asList(
                R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_4,
                R.id.button_5, R.id.button_6, R.id.button_7, R.id.button_8, R.id.button_9
        );

        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(this::onNumberClick);
        }

        findViewById(R.id.button_add).setOnClickListener(this::onOperatorClick);
        findViewById(R.id.button_subtract).setOnClickListener(this::onOperatorClick);
        findViewById(R.id.button_multiply).setOnClickListener(this::onOperatorClick);
        findViewById(R.id.button_divide).setOnClickListener(this::onOperatorClick);

        findViewById(R.id.button_decimal).setOnClickListener(v -> onDecimalClick());
        findViewById(R.id.button_equals).setOnClickListener(v -> onEqualsClick());
        findViewById(R.id.button_back).setOnClickListener(v -> onBackspaceClick());
        findViewById(R.id.button_plus_minus).setOnClickListener(v -> onSignChangeClick());
        findViewById(R.id.button_percent).setOnClickListener(v -> onPercentClick());

        Button backButton = findViewById(R.id.button_back);
        backButton.setOnLongClickListener(v -> {
            onClearClick();
            return true;
        });


        updateDisplay("0");
    }

    private void updateDisplay(String textToShow) {
        if (textToShow == null || textToShow.isEmpty()) {
            if (expressionDisplayBuilder.length() == 0 && currentNumberBuilder.length() == 0) {
                displayTextView.setText("0");
            } else {
                refreshDisplay();
            }
        } else {
            displayTextView.setText(textToShow);
        }
    }

    private void refreshDisplay() {
        if (expressionDisplayBuilder.length() == 0 && currentNumberBuilder.length() == 0) {
            displayTextView.setText("0");
        } else {
            String text = expressionDisplayBuilder.toString() + currentNumberBuilder.toString();
            displayTextView.setText(text.isEmpty() ? "0" : text);
        }
    }


    private void onNumberClick(View view) {
        Button button = (Button) view;
        String number = button.getText().toString();
        if (currentNumberBuilder.toString().equals("0") && !number.equals("0")) {
            currentNumberBuilder.setLength(0);
        }
        if (currentNumberBuilder.toString().equals("-0") && !number.equals("0")) {
            currentNumberBuilder.setLength(1); // Keep "-"
        }

        currentNumberBuilder.append(number);
        refreshDisplay();
    }

    private void onDecimalClick() {
        if (currentNumberBuilder.indexOf(".") == -1) {
            if (currentNumberBuilder.length() == 0) {
                currentNumberBuilder.append("0");
            }
            currentNumberBuilder.append(".");
            refreshDisplay();
        }
    }

    private void appendCurrentNumberToExpression() {
        if (currentNumberBuilder.length() > 0) {
            String numberToAppend = currentNumberBuilder.toString();
            if (numberToAppend.startsWith("-")) {
                String trimmedExpression = expressionDisplayBuilder.toString().trim();
                if (!trimmedExpression.isEmpty() && !trimmedExpression.endsWith("(")) {
                    expressionDisplayBuilder.append("( ").append(numberToAppend).append(" )");
                } else {
                    expressionDisplayBuilder.append(numberToAppend);
                }
            } else {
                expressionDisplayBuilder.append(numberToAppend);
            }
            currentNumberBuilder.setLength(0);
        }
    }

    private void onOperatorClick(View view) {
        Button button = (Button) view;
        String operator = button.getText().toString();

        appendCurrentNumberToExpression();

        String exprStr = expressionDisplayBuilder.toString().trim();
        if (exprStr.endsWith("+") || exprStr.endsWith("−") || exprStr.endsWith("×") || exprStr.endsWith("÷")) {
            if ( (operator.equals("−") && !exprStr.endsWith("−")) || (!operator.equals("−")) ) {
                //
            }
        }


        expressionDisplayBuilder.append(" ").append(operator).append(" ");
        refreshDisplay();
    }

    private void onEqualsClick() {
        appendCurrentNumberToExpression();

        if (expressionDisplayBuilder.length() > 0) {
            try {
                double result = evaluateExpression(expressionDisplayBuilder.toString());
                String resultStr = decimalFormat.format(result);
                updateDisplay(resultStr);
                expressionDisplayBuilder.setLength(0);
                currentNumberBuilder.append(resultStr);
            } catch (Exception e) {
                updateDisplay("Error: " + e.getMessage());
                expressionDisplayBuilder.setLength(0);
                currentNumberBuilder.setLength(0);
            }
        } else if (currentNumberBuilder.length() > 0) {
            refreshDisplay();
        }
    }

    private void onClearClick() {
        expressionDisplayBuilder.setLength(0);
        currentNumberBuilder.setLength(0);
        updateDisplay("0");
    }

    private void onBackspaceClick() {
        if (currentNumberBuilder.length() > 0) {
            currentNumberBuilder.deleteCharAt(currentNumberBuilder.length() - 1);
        } else if (expressionDisplayBuilder.length() > 0) {
            String expr = expressionDisplayBuilder.toString();
            if (expr.endsWith(" ) ")) {
                expressionDisplayBuilder.setLength(expr.lastIndexOf("( "));
            } else if (expr.endsWith(" ")) {
                expressionDisplayBuilder.setLength(expr.trim().length());
                if (expressionDisplayBuilder.length() > 0 && !Character.isDigit(expressionDisplayBuilder.charAt(expressionDisplayBuilder.length()-1))) {
                    expressionDisplayBuilder.deleteCharAt(expressionDisplayBuilder.length()-1);
                }
                expressionDisplayBuilder.setLength(expressionDisplayBuilder.toString().trim().length());
            } else if (!expr.isEmpty()){
                expressionDisplayBuilder.deleteCharAt(expressionDisplayBuilder.length() - 1);
            }
        }
        refreshDisplay();
    }

    private void onSignChangeClick() { // +/-
        if (currentNumberBuilder.length() > 0) {
            if (currentNumberBuilder.charAt(0) == '-') {
                currentNumberBuilder.deleteCharAt(0);
            } else {
                currentNumberBuilder.insert(0, "-");
            }
        } else if (expressionDisplayBuilder.length() > 0) {
            String expr = expressionDisplayBuilder.toString().trim();
            String[] tokens = expr.split(" ");
            if (tokens.length > 0) {
                String lastToken = tokens[tokens.length -1];
                try {
                    Double.parseDouble(lastToken);
                    currentNumberBuilder.append(lastToken);
                    expressionDisplayBuilder.setLength(expr.lastIndexOf(lastToken));
                    expressionDisplayBuilder.setLength(expressionDisplayBuilder.toString().trim().length());

                    if (currentNumberBuilder.charAt(0) == '-') {
                        currentNumberBuilder.deleteCharAt(0);
                    } else {
                        currentNumberBuilder.insert(0, "-");
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        refreshDisplay();
    }

    private void onPercentClick() {
        if (currentNumberBuilder.length() > 0) {
            try {
                double value = Double.parseDouble(currentNumberBuilder.toString());
                double result = value / 100.0;
                currentNumberBuilder.setLength(0);
                currentNumberBuilder.append(decimalFormat.format(result));
            } catch (NumberFormatException e) {
            }
        }
        refreshDisplay();
    }

    private double evaluateExpression(String expression) throws ArithmeticException, IllegalArgumentException {
        String cleanExpression = expression.trim();
        if (cleanExpression.isEmpty()) return 0.0;

        cleanExpression = cleanExpression.replaceAll("\\( -(\\d*\\.?\\d+) \\)", "-$1");
        cleanExpression = cleanExpression.replaceAll("\\( (\\d*\\.?\\d+) \\)", "$1");


        List<String> initialSpacedTokens = Arrays.asList(cleanExpression.split("\\s+"));
        List<String> processedSpacedTokens = new ArrayList<>();


        List<Object> parsedTokens = new ArrayList<>();
        for(String token : initialSpacedTokens) {
            if (token.isEmpty()) continue;
            if (token.equals("+") || token.equals("−") || token.equals("×") || token.equals("÷")) {
                parsedTokens.add(token.charAt(0));
            } else {
                try {
                    parsedTokens.add(Double.parseDouble(token));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid token: " + token + " in expression: " + cleanExpression);
                }
            }
        }

        if (parsedTokens.isEmpty()) {
            throw new IllegalArgumentException("Empty expression after parsing");
        }

        if (parsedTokens.get(0) instanceof Character) {
            if (parsedTokens.size() > 1 && parsedTokens.get(0).equals('−') && parsedTokens.get(1) instanceof Double) {
                parsedTokens.set(0, - (Double)parsedTokens.get(1));
                parsedTokens.remove(1);
            } else if (parsedTokens.get(0).equals('+') && parsedTokens.size() > 1 && parsedTokens.get(1) instanceof Double) {
                parsedTokens.set(0, parsedTokens.get(1));
                parsedTokens.remove(1);
            } else {
                throw new IllegalArgumentException("Expression cannot start with an operator (unless unary minus handled by number): " + parsedTokens.get(0));
            }
        }


        List<Object> pass1 = new ArrayList<>();
        int i = 0;
        while (i < parsedTokens.size()) {
            Object currentTokenObj = parsedTokens.get(i);
            if (currentTokenObj instanceof Character && ( ((Character)currentTokenObj) == '×' || ((Character)currentTokenObj) == '÷') ) {
                if (pass1.isEmpty() || !(pass1.get(pass1.size()-1) instanceof Double)) {
                    throw new IllegalArgumentException("Missing left operand for " + currentTokenObj);
                }
                Character op = (Character) currentTokenObj;
                Double left = (Double) pass1.remove(pass1.size() - 1);

                i++;
                if (i >= parsedTokens.size() || !(parsedTokens.get(i) instanceof Double)) {
                    throw new IllegalArgumentException("Missing right operand for " + op);
                }
                Double right = (Double) parsedTokens.get(i);

                if (op == '×') {
                    pass1.add(left * right);
                } else { // op == '÷'
                    if (right == 0) throw new ArithmeticException("Division by zero");
                    pass1.add(left / right);
                }
            } else {
                pass1.add(currentTokenObj);
            }
            i++;
        }

        if (pass1.isEmpty()) {
            return 0.0;
        }
        if (!(pass1.get(0) instanceof Double)) {
            throw new IllegalArgumentException("Invalid expression structure after pass 1. Expected number, got: " + pass1.get(0));
        }

        Double result = (Double) pass1.get(0);
        for (i = 1; i < pass1.size(); i += 2) {
            if (i + 1 >= pass1.size() || !(pass1.get(i) instanceof Character) || !(pass1.get(i+1) instanceof Double) ) {
                throw new IllegalArgumentException("Invalid expression structure for addition/subtraction.");
            }
            Character op = (Character) pass1.get(i);
            Double operand = (Double) pass1.get(i + 1);

            if (op == '+') {
                result += operand;
            } else if (op == '−') {
                result -= operand;
            } else {
                throw new IllegalArgumentException("Unknown operator in pass 2: " + op);
            }
        }
        return result;
    }
}