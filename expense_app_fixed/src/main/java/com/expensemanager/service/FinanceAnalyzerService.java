package com.expensemanager.service;

import com.expensemanager.model.Transaction;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FinanceAnalyzerService {

    public List<String> generateInsights(List<Transaction> transactions) {
        List<String> insights = new ArrayList<>();

        if (transactions == null || transactions.isEmpty()) {
            insights.add("Start adding transactions to see insights!");
            return insights;
        }

        double totalIncome = transactions.stream()
            .filter(t -> "INCOME".equals(t.getType()))
            .mapToDouble(Transaction::getAmount).sum();

        double totalExpense = transactions.stream()
            .filter(t -> "EXPENSE".equals(t.getType()))
            .mapToDouble(Transaction::getAmount).sum();

        // Insight 1: Savings
        if (totalIncome > 0) {
            double savings = totalIncome - totalExpense;
            double savingsRate = (savings / totalIncome) * 100;
            if (savings > 0) {
                insights.add(String.format("You are saving %.1f%% of your income.", savingsRate));
            } else {
                insights.add("You are spending more than you earn!");
            }
        }

        // Insight 2: Top Category
        Map<String, Double> expenseByCategory = transactions.stream()
            .filter(t -> "EXPENSE".equals(t.getType()))
            .collect(Collectors.groupingBy(
                Transaction::getCategoryName,
                Collectors.summingDouble(Transaction::getAmount)
            ));

        expenseByCategory.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .ifPresent(entry -> {
                double percentage = (entry.getValue() / totalExpense) * 100;
                insights.add(String.format("You spent %.1f%% on %s this month.", percentage, entry.getKey()));
            });

        return insights;
    }
}
