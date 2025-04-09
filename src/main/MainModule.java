package main;

import dao.ILoanRepository;
import dao.ILoanRepositoryImpl;
import entity.CarLoan;
import entity.Customer;
import entity.HomeLoan;
import entity.Loan;
import exception.InvalidLoanException;

import java.util.List;
import java.util.Scanner;

public class MainModule {
    private static Scanner scanner = new Scanner(System.in);
    private static ILoanRepository loanRepository = new ILoanRepositoryImpl();

    public static void main(String[] args) {
        boolean exit = false;

        System.out.println("Welcome to the Loan Management System");

        while (!exit) {
            displayMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    applyForLoan();
                    break;
                case 2:
                    getAllLoans();
                    break;
                case 3:
                    getLoanById();
                    break;
                case 4:
                    checkLoanStatus();
                    break;
                case 5:
                    calculateLoanInterest();
                    break;
                case 6:
                    calculateEMI();
                    break;
                case 7:
                    makeLoanRepayment();
                    break;
                case 8:
                    exit = true;
                    System.out.println("Thank you!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            System.out.println();
        }

        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("\n===== LOAN MANAGEMENT SYSTEM MENU =====");
        System.out.println("1. Apply for a Loan");
        System.out.println("2. View All Loans");
        System.out.println("3. Get Loan Details by ID");
        System.out.println("4. Check Loan Status (Approve/Reject)");
        System.out.println("5. Calculate Loan Interest");
        System.out.println("6. Calculate Loan EMI");
        System.out.println("7. Make Loan Repayment");
        System.out.println("8. Exit");
        System.out.println("=======================================");
    }

    private static void applyForLoan() {
        System.out.println("\n===== APPLY FOR A LOAN =====");

        // Get customer details
        Customer customer = getCustomerDetails();

        // Get common loan details
        int loanId = getIntInput("Enter Loan ID: ");
        double principalAmount = getDoubleInput("Enter Principal Amount: ");
        double interestRate = getDoubleInput("Enter Interest Rate (%): ");
        int loanTerm = getIntInput("Enter Loan Term (months): ");

        // Choose loan type
        System.out.println("\nChoose Loan Type:");
        System.out.println("1. Home Loan");
        System.out.println("2. Car Loan");
        int loanTypeChoice = getIntInput("Enter your choice: ");

        Loan loan;

        if (loanTypeChoice == 1) {
            System.out.print("Enter Property Address: ");
            String propertyAddress = scanner.nextLine();
            int propertyValue = getIntInput("Enter Property Value: ");

            loan = new HomeLoan(loanId, customer, principalAmount, interestRate, loanTerm,
                    "Pending", propertyAddress, propertyValue);
        } else if (loanTypeChoice == 2) {
            System.out.print("Enter Car Model: ");
            String carModel = scanner.nextLine();
            int carValue = getIntInput("Enter Car Value: ");

            loan = new CarLoan(loanId, customer, principalAmount, interestRate, loanTerm,
                    "Pending", carModel, carValue);
        } else {
            System.out.println("Invalid loan type selected. Application cancelled.");
            return;
        }

        // Calculate and display EMI and interest
        double emi = loanRepository.calculateEMI(principalAmount, interestRate, loanTerm);
        double interest = loanRepository.calculateInterest(principalAmount, interestRate, loanTerm);

        System.out.println("\nLoan Summary:");
        System.out.println("Monthly EMI: " + String.format("%.2f", emi));
        System.out.println("Total Interest: " + String.format("%.2f", interest));
        System.out.println("Total Amount to be Paid: " + String.format("%.2f", (principalAmount + interest)));

        // Apply for the loan
        boolean success = loanRepository.applyLoan(loan);

        if (success) {
            System.out.println("Loan application completed successfully.");
        } else {
            System.out.println("Loan application failed.");
        }
    }

    private static Customer getCustomerDetails() {
        System.out.println("\n--- Customer Details ---");
        int customerId = getIntInput("Enter Customer ID: ");
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine();
        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine();
        System.out.print("Enter Address: ");
        String address = scanner.nextLine();
        int creditScore = getIntInput("Enter Credit Score (300-850): ");

        return new Customer(customerId, name, email, phone, address, creditScore);
    }

    private static void getAllLoans() {
        System.out.println("\n===== ALL LOANS =====");
        List<Loan> loans = loanRepository.getAllLoan();

        if (loans.isEmpty()) {
            System.out.println("No loans found in the system.");
        } else {
            System.out.println("Total Loans: " + loans.size());
            for (Loan loan : loans) {
                System.out.println("\n" + loan);
            }
        }
    }

    private static void getLoanById() {
        System.out.println("\n===== GET LOAN BY ID =====");
        int loanId = getIntInput("Enter Loan ID: ");

        try {
            Loan loan = loanRepository.getLoanById(loanId);
            System.out.println("\nLoan details:");
            System.out.println(loan);

            // Display EMI and interest information
            double emi = loanRepository.calculateEMI(loanId);
            double interest = loanRepository.calculateInterest(loanId);

            System.out.println("\nPayment Information:");
            System.out.println("Monthly EMI: " + String.format("%.2f", emi));
            System.out.println("Total Interest: " + String.format("%.2f", interest));
            System.out.println("Total Amount: " + String.format("%.2f", (loan.getPrincipalAmount() + interest)));

        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void checkLoanStatus() {
        System.out.println("\n===== CHECK LOAN STATUS =====");
        int loanId = getIntInput("Enter Loan ID: ");

        try {
            boolean approved = loanRepository.loanStatus(loanId);
            System.out.println("Loan status update: " + (approved ? "APPROVED" : "REJECTED"));
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void calculateLoanInterest() {
        System.out.println("\n===== CALCULATE LOAN INTEREST =====");
        System.out.println("1. Calculate for existing loan (by ID)");
        System.out.println("2. Calculate for new loan parameters");
        int choice = getIntInput("Enter your choice: ");

        if (choice == 1) {
            int loanId = getIntInput("Enter Loan ID: ");
            try {
                double interest = loanRepository.calculateInterest(loanId);
                System.out.println("Total Interest Amount: " + String.format("%.2f", interest));
            } catch (InvalidLoanException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else if (choice == 2) {
            double principal = getDoubleInput("Enter Principal Amount: ");
            double rate = getDoubleInput("Enter Interest Rate (%): ");
            int term = getIntInput("Enter Loan Term (months): ");

            double interest = loanRepository.calculateInterest(principal, rate, term);
            System.out.println("Total Interest Amount: " + String.format("%.2f", interest));
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private static void calculateEMI() {
        System.out.println("\n===== CALCULATE LOAN EMI =====");
        System.out.println("1. Calculate for existing loan (by ID)");
        System.out.println("2. Calculate for new loan parameters");
        int choice = getIntInput("Enter your choice: ");

        if (choice == 1) {
            int loanId = getIntInput("Enter Loan ID: ");
            try {
                double emi = loanRepository.calculateEMI(loanId);
                System.out.println("Monthly EMI Amount: " + String.format("%.2f", emi));
            } catch (InvalidLoanException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else if (choice == 2) {
            double principal = getDoubleInput("Enter Principal Amount: ");
            double rate = getDoubleInput("Enter Interest Rate (%): ");
            int term = getIntInput("Enter Loan Term (months): ");

            double emi = loanRepository.calculateEMI(principal, rate, term);
            System.out.println("Monthly EMI Amount: " + String.format("%.2f", emi));
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private static void makeLoanRepayment() {
        System.out.println("\n===== MAKE LOAN REPAYMENT =====");
        int loanId = getIntInput("Enter Loan ID: ");

        try {
            Loan loan = loanRepository.getLoanById(loanId);
            double emi = loanRepository.calculateEMI(loanId);

            System.out.println("Loan Details:");
            System.out.println("Loan ID: " + loan.getLoanId());
            System.out.println("Customer: " + loan.getCustomer().getName());
            System.out.println("Principal Amount: " + loan.getPrincipalAmount());
            System.out.println("Monthly EMI: " + String.format("%.2f", emi));

            double amount = getDoubleInput("Enter payment amount (must be at least one EMI amount): ");

            boolean success = loanRepository.loanRepayment(loanId, amount);

            if (success) {
                System.out.println("Payment processed successfully.");
            } else {
                System.out.println("Payment processing failed.");
            }

        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Helper methods for input validation
    private static int getIntInput(String prompt) {
        int value = 0;
        boolean valid = false;

        while (!valid) {
            System.out.print(prompt);
            try {
                value = Integer.parseInt(scanner.nextLine());
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        return value;
    }

    private static double getDoubleInput(String prompt) {
        double value = 0;
        boolean valid = false;

        while (!valid) {
            System.out.print(prompt);
            try {
                value = Double.parseDouble(scanner.nextLine());
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        return value;
    }
}