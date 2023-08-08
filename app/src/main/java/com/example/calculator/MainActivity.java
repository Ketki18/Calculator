package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private TextView txtView1, txtView2;
    private String input = "";
    private String operator = "";
    private static final Map<String, Integer> precedence = new HashMap<>();

    static {
        precedence.put("+", 1);
        precedence.put("-", 1);
        precedence.put("*", 2);
        precedence.put("/", 2);
        precedence.put("%", 2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtView1 = findViewById(R.id.txtView1);
        txtView2 = findViewById(R.id.txtView2);

        // Set click listeners for all number and operator buttons
        Button[] numButtons = new Button[]{
                findViewById(R.id.btn0), findViewById(R.id.btn1), findViewById(R.id.btn2),
                findViewById(R.id.btn3), findViewById(R.id.btn4), findViewById(R.id.btn5),
                findViewById(R.id.btn6), findViewById(R.id.btn7), findViewById(R.id.btn8),
                findViewById(R.id.btn9),findViewById(R.id.btnPoint)
        };

        for (Button numButton : numButtons) {
            numButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = ((Button) v).getText().toString();
                    input += text;
                    txtView1.setText(input);
                }
            });
        }

        Button[] operatorButtons = new Button[]{
                findViewById(R.id.btnAdd), findViewById(R.id.btnSub),
                findViewById(R.id.btnMul), findViewById(R.id.btnDiv),
                findViewById(R.id.btnMod)
        };

        for (Button operatorButton : operatorButtons) {
            operatorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = ((Button) v).getText().toString();
                    if (!input.isEmpty() && isOperator(input.charAt(input.length() - 1))) {
                        input = input.substring(0, input.length() - 1);
                    }
                    operator = text;
                    input += text;
                    txtView1.setText(input);
                }
            });
        }

        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!input.isEmpty()) {
                    input = input.substring(0, input.length() - 1);
                    txtView1.setText(input);
                }
            }
        });

        Button btnAC = findViewById(R.id.btnAC);
        btnAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input = "";
                operator = "";
                txtView1.setText("");
                txtView2.setText("");
            }
        });

        // Equal button
        Button btnEqual = findViewById(R.id.btnRes);
        btnEqual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!input.isEmpty() && !operator.isEmpty()) {
                    BigDecimal result = evaluateExpression(input);
                    if (result != null) {
                        txtView2.setText(result.stripTrailingZeros().toPlainString());
                        input = result.stripTrailingZeros().toPlainString();
                        operator = "";
                    } else {
                        txtView2.setText("Error");
                    }
                }
            }
        });
    }

    private BigDecimal evaluateExpression(String expression) {
        Deque<String> postfixQueue = shuntingYard(expression);
        Deque<BigDecimal> valueStack = new ArrayDeque<>();

        while (!postfixQueue.isEmpty()) {
            String token = postfixQueue.poll();

            if (isNumber(token)) {
                valueStack.push(new BigDecimal(token));
            } else if (isOperator(token)) {
                BigDecimal operand2 = valueStack.pop();
                BigDecimal operand1 = valueStack.pop();
                BigDecimal result = performOperation(operand1, operand2, token);
                if (result == null) {
                    return null; // Error occurred during calculation
                }
                valueStack.push(result);
            }
        }

        return valueStack.pop();
    }

    private Deque<String> shuntingYard(String expression) {
        Deque<String> outputQueue = new ArrayDeque<>();
        Deque<String> operatorStack = new ArrayDeque<>();
        StringBuilder currentNumber = new StringBuilder();

        for (char ch : expression.toCharArray()) {
            if (Character.isDigit(ch) || ch == '.') {
                currentNumber.append(ch);
            } else {
                if (currentNumber.length() > 0) {
                    outputQueue.add(currentNumber.toString());
                    currentNumber.setLength(0); // Reset the currentNumber StringBuilder
                }

                String token = String.valueOf(ch).trim();
                if (!token.isEmpty()) {
                    while (!operatorStack.isEmpty() && isOperator(operatorStack.peek())) {
                        String topOperator = operatorStack.peek();
                        if ((precedence.get(token) <= precedence.get(topOperator))) {
                            outputQueue.add(operatorStack.pop());
                        } else {
                            break;
                        }
                    }
                    operatorStack.push(token);
                }
            }
        }

        // Add the last number, if any
        if (currentNumber.length() > 0) {
            outputQueue.add(currentNumber.toString());
        }

        while (!operatorStack.isEmpty()) {
            outputQueue.add(operatorStack.pop());
        }

        return outputQueue;
    }



    private boolean isOperator(String str) {
        return precedence.containsKey(str);
    }

    private boolean isOperator(char ch) {
        return isOperator(Character.toString(ch));
    }

    private boolean isNumber(String str) {
        try {
            new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private BigDecimal performOperation(BigDecimal operand1, BigDecimal operand2, String operator) {
        switch (operator) {
            case "+":
                return operand1.add(operand2);
            case "-":
                return operand1.subtract(operand2);
            case "*":
                return operand1.multiply(operand2);
            case "/":
                if (operand2.equals(BigDecimal.ZERO)) {
                    return null; // Division by zero
                }
                return operand1.divide(operand2, 4, RoundingMode.HALF_UP);
            case "%":
                return operand1.remainder(operand2).setScale(4, RoundingMode.HALF_UP);
            default:
                return null; // Unknown operator
        }
    }
}
