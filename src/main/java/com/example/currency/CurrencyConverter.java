package com.example.currency;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyConverter extends JFrame {
    private JComboBox<String> fromCurrency, toCurrency;
    private JTextField amountField, resultField;
    private JButton convertButton;
    private JLabel statusLabel;
    private final Map<String, String> currencySymbols = new HashMap<>();
    
    // List of 160+ currencies supported by the API
    private final String[] currencies = {
        "AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN",
        "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BRL",
        "BSD", "BTC", "BTN", "BWP", "BYN", "BYR", "BZD", "CAD", "CDF", "CHF",
        "CLF", "CLP", "CNH", "CNY", "COP", "CRC", "CUC", "CUP", "CVE", "CZK",
        "DJF", "DKK", "DOP", "DZD", "EGP", "ERN", "ETB", "EUR", "FJD", "FKP",
        "GBP", "GEL", "GGP", "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD", "HKD",
        "HNL", "HRK", "HTG", "HUF", "IDR", "ILS", "IMP", "INR", "IQD", "IRR",
        "ISK", "JEP", "JMD", "JOD", "JPY", "KES", "KGS", "KHR", "KMF", "KPW",
        "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", "LTL",
        "LVL", "LYD", "MAD", "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRO",
        "MUR", "MVR", "MWK", "MXN", "MYR", "MZN", "NAD", "NGN", "NIO", "NOK",
        "NPR", "NZD", "OMR", "PAB", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG",
        "QAR", "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK",
        "SGD", "SHP", "SLL", "SOS", "SRD", "STD", "SVC", "SYP", "SZL", "THB",
        "TJS", "TMT", "TND", "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX",
        "USD", "UYU", "UZS", "VEF", "VND", "VUV", "WST", "XAF", "XAG", "XAU",
        "XCD", "XDR", "XOF", "XPF", "YER", "ZAR", "ZMK", "ZMW", "ZWL"
    };

    public CurrencyConverter() {
        initializeCurrencySymbols();
        initializeUI();
    }

    private void initializeCurrencySymbols() {
        // Common currency symbols
        currencySymbols.put("USD", "$");
        currencySymbols.put("EUR", "€");
        currencySymbols.put("GBP", "£");
        currencySymbols.put("JPY", "¥");
        currencySymbols.put("AUD", "A$");
        currencySymbols.put("CAD", "C$");
        currencySymbols.put("CHF", "CHF");
        currencySymbols.put("CNY", "¥");
        currencySymbols.put("INR", "₹");
        currencySymbols.put("MXN", "$");
        currencySymbols.put("BRL", "R$");
        currencySymbols.put("RUB", "₽");
        currencySymbols.put("ZAR", "R");
        currencySymbols.put("SGD", "S$");
        currencySymbols.put("HKD", "HK$");
        currencySymbols.put("SEK", "kr");
    }

    private void initializeUI() {
        setTitle("Currency Converter");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Main panel
        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Amount input
        mainPanel.add(new JLabel("Amount:"));
        amountField = new JTextField();
        mainPanel.add(amountField);

        // From currency
        mainPanel.add(new JLabel("From:"));
        fromCurrency = new JComboBox<>(currencies);
        fromCurrency.setSelectedItem("USD");
        mainPanel.add(fromCurrency);

        // To currency
        mainPanel.add(new JLabel("To:"));
        toCurrency = new JComboBox<>(currencies);
        toCurrency.setSelectedItem("EUR");
        mainPanel.add(toCurrency);

        // Convert button
        convertButton = new JButton("Convert");
        convertButton.addActionListener(e -> convertCurrency());
        mainPanel.add(convertButton);

        // Result field
        resultField = new JTextField();
        resultField.setEditable(false);
        resultField.setBackground(new Color(240, 240, 240));
        mainPanel.add(resultField);

        add(mainPanel, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void convertCurrency() {
        try {
            // Get input values
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                throw new NumberFormatException("Amount must be positive");
            }
            
            String from = (String) fromCurrency.getSelectedItem();
            String to = (String) toCurrency.getSelectedItem();
            
            // Update status
            statusLabel.setText("Fetching exchange rates...");
            convertButton.setEnabled(false);
            
            // Perform conversion in background thread
            new SwingWorker<Void, Void>() {
                private double result;
                private Exception error;
                
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        // Get exchange rate from API
                        double rate = getExchangeRate(from, to);
                        result = amount * rate;
                    } catch (Exception e) {
                        error = e;
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        if (error != null) {
                            throw error;
                        }
                        
                        // Get currency symbol
                        String symbol = currencySymbols.getOrDefault(to, to);
                        
                        // Format and display result
                        String formattedResult = String.format("%s %.2f", symbol, result);
                        resultField.setText(formattedResult);
                        statusLabel.setText("Conversion complete");
                    } catch (Exception e) {
                        resultField.setText("Error");
                        statusLabel.setText("Error: " + e.getMessage());
                        JOptionPane.showMessageDialog(CurrencyConverter.this,
                            "Conversion error: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        convertButton.setEnabled(true);
                    }
                }
            }.execute();
            
        } catch (NumberFormatException e) {
            resultField.setText("Error");
            statusLabel.setText("Error: Invalid amount");
            JOptionPane.showMessageDialog(this,
                "Please enter a valid positive number",
                "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double getExchangeRate(String from, String to) throws Exception {
        // API endpoint
        String apiUrl = "https://api.exchangerate-api.com/v4/latest/" + from;
        
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        if (conn.getResponseCode() != 200) {
            throw new Exception("API request failed with code: " + conn.getResponseCode());
        }
        
        // Read response
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        // Parse exchange rate using regex
        String regex = "\"" + to + "\":([0-9.]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(response.toString());
        
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        } else {
            throw new Exception("Exchange rate not found for " + to);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new CurrencyConverter().setVisible(true);
        });
    }
}