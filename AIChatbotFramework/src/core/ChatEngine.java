
package core;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatEngine {

    private final Random random = new Random();

    public String processMessage(String message) {

        if (message == null || message.trim().isEmpty()) {
            return "Please enter a valid message.";
        }

        String text = message.trim();
        String command = text.toLowerCase();

        // Greeting Commands
        if (command.equals("hello") || command.equals("hi")
                || command.equals("hey")) {

            return "Hello! Welcome to ChatForge. How can I help you today?";
        }

        // Help Command
        if (command.equals("help")) {

            return "Available Commands:\n"
                    + "hello, hi, hey - Greeting\n"
                    + "help - Show commands\n"
                    + "bye - Exit chat\n"
                    + "time - Current time\n"
                    + "date - Current date\n"
                    + "joke - Random joke\n"
                    + "motivate - Motivational quote\n"
                    + "about - About ChatForge\n"
                    + "#calculator - Calculator\n"
                    + "#maths - Mathematics skill\n"
                    + "#science - Science skill\n"
                    + "#gk - General knowledge\n"
                    + "#social - Social studies\n"
                    + "#chat - Casual conversation\n"
                    + "#tutor - Learning assistant";
        }

        // Goodbye Command
        if (command.equals("bye")) {

            return "Goodbye! Have a nice day.";
        }

        // Time Command
        if (command.equals("time")) {

            LocalTime currentTime = LocalTime.now();

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("hh:mm:ss a");

            return "Current time: " + currentTime.format(formatter);
        }

        // Date Command
        if (command.equals("date")) {

            LocalDate currentDate = LocalDate.now();

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd-MM-yyyy");

            return "Today's date: " + currentDate.format(formatter);
        }

        // Joke Command
        if (command.equals("joke")) {

            String[] jokes = {
                    "Why do programmers prefer dark mode? Because light attracts bugs!",
                    "Why did the Java developer wear glasses? Because he couldn't C#!",
                    "Why do Java programmers hate the ocean? Too many Null Pointers!",
                    "A programmer's wife says: Go to the store and buy milk. If they have eggs, buy 6. He returns with 6 cartons of milk."
            };

            return jokes[random.nextInt(jokes.length)];
        }

        // Motivation Command
        if (command.equals("motivate")) {

            String[] quotes = {
                    "Believe in yourself. You are capable of amazing things!",
                    "Every expert was once a beginner.",
                    "Success is the sum of small efforts repeated every day.",
                    "Never stop learning. Your future self will thank you."
            };

            return quotes[random.nextInt(quotes.length)];
        }

        // About Command
        if (command.equals("about")) {

            return "ChatForge is a Core Java-based console chatbot "
                    + "designed to provide interactive commands, "
                    + "learning assistance, and useful utilities.";
        }

        // Calculator Command
        if (command.startsWith("#calculator")) {

            return calculate(text);
        }

        // Maths Skill
        if (command.equals("#maths")) {

            return "Maths Skill Activated!\n"
                    + "Mathematics helps us solve problems using numbers, "
                    + "patterns, and logical reasoning.\n"
                    + "Example: Area of a rectangle = Length x Width.";
        }

        // Science Skill
        if (command.equals("#science")) {

            return "Science Skill Activated!\n"
                    + "Fun Fact: Light travels approximately "
                    + "300,000 kilometers per second.";
        }

        // General Knowledge Skill
        if (command.equals("#gk")) {

            return "General Knowledge Skill Activated!\n"
                    + "Question: What is the capital of India?\n"
                    + "Answer: New Delhi.";
        }

        // Social Studies Skill
        if (command.equals("#social")) {

            return "Social Studies Skill Activated!\n"
                    + "Social Studies covers history, geography, "
                    + "civics, and economics.";
        }

        // Casual Chat Skill
        if (command.equals("#chat")) {

            return "Chat Mode Activated!\n"
                    + "Hey there! I'm ChatForge. "
                    + "Let's have a conversation.";
        }

        // Tutor Skill
        if (command.equals("#tutor")) {

            return "Tutor Mode Activated!\n"
                    + "I'm your learning assistant. "
                    + "Ask me about Java, programming, "
                    + "mathematics, or computer science.";
        }

        // Unknown Command
        return "Sorry, I don't understand that command. "
                + "Type 'help' to see available commands.";
    }

    // Simple Calculator Method
    private String calculate(String text) {

        String expression = text.substring("#calculator".length()).trim();

        if (expression.isEmpty()) {

            return "Calculator Mode Activated!\n"
                    + "Usage: #calculator 10 + 5\n"
                    + "Supported operators: +, -, *, /";
        }

        Pattern pattern = Pattern.compile(
                "(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)"
        );

        Matcher matcher = pattern.matcher(expression);

        if (!matcher.matches()) {

            return "Invalid expression.\n"
                    + "Example: #calculator 10 + 5";
        }

        try {

            double firstNumber =
                    Double.parseDouble(matcher.group(1));

            String operator = matcher.group(2);

            double secondNumber =
                    Double.parseDouble(matcher.group(3));

            double result;

            switch (operator) {

                case "+":
                    result = firstNumber + secondNumber;
                    break;

                case "-":
                    result = firstNumber - secondNumber;
                    break;

                case "*":
                    result = firstNumber * secondNumber;
                    break;

                case "/":

                    if (secondNumber == 0) {
                        return "Error: Cannot divide by zero.";
                    }

                    result = firstNumber / secondNumber;
                    break;

                default:
                    return "Unsupported operator.";
            }

            return "Calculator Result: " + result;

        } catch (NumberFormatException e) {

            return "Invalid numbers entered.";
        }
    }
}
