package dao;

import entity.CarLoan;
import entity.Customer;
import entity.HomeLoan;
import entity.Loan;
import exception.InvalidLoanException;
import util.DBConnUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ILoanRepositoryImpl implements ILoanRepository {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private Scanner scanner = new Scanner(System.in);

    @Override
    public boolean applyLoan(Loan loan) {
        System.out.println("Loan Application Details:");
        System.out.println(loan);
        System.out.print("Confirm loan application (Yes/No): ");
        String confirmation = scanner.nextLine();

        if (!confirmation.equalsIgnoreCase("Yes")) {
            System.out.println("Loan application cancelled.");
            return false;
        }

        try {
            connection = DBConnUtil.getConnection();

            // First, insert or update customer
            String customerQuery = "INSERT INTO customers (customer_id, name, email_address, phone_number, address, credit_score) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name=?, email_address=?, phone_number=?, address=?, credit_score=?";

            preparedStatement = connection.prepareStatement(customerQuery);
            preparedStatement.setInt(1, loan.getCustomer().getCustomerId());
            preparedStatement.setString(2, loan.getCustomer().getName());
            preparedStatement.setString(3, loan.getCustomer().getEmailAddress());
            preparedStatement.setString(4, loan.getCustomer().getPhoneNumber());
            preparedStatement.setString(5, loan.getCustomer().getAddress());
            preparedStatement.setInt(6, loan.getCustomer().getCreditScore());
            preparedStatement.setString(7, loan.getCustomer().getName());
            preparedStatement.setString(8, loan.getCustomer().getEmailAddress());
            preparedStatement.setString(9, loan.getCustomer().getPhoneNumber());
            preparedStatement.setString(10, loan.getCustomer().getAddress());
            preparedStatement.setInt(11, loan.getCustomer().getCreditScore());

            preparedStatement.executeUpdate();

            // Then, insert the loan
            String loanQuery = "INSERT INTO loans (loan_id, customer_id, principal_amount, interest_rate, loan_term, loan_type, loan_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(loanQuery);
            preparedStatement.setInt(1, loan.getLoanId());
            preparedStatement.setInt(2, loan.getCustomer().getCustomerId());
            preparedStatement.setDouble(3, loan.getPrincipalAmount());
            preparedStatement.setDouble(4, loan.getInterestRate());
            preparedStatement.setInt(5, loan.getLoanTerm());
            preparedStatement.setString(6, loan.getLoanType());
            preparedStatement.setString(7, "Pending"); // Initial status is Pending

            preparedStatement.executeUpdate();

            // Insert additional details based on loan type
            if (loan instanceof HomeLoan) {
                HomeLoan homeLoan = (HomeLoan) loan;
                String homeQuery = "INSERT INTO home_loans (loan_id, property_address, property_value) VALUES (?, ?, ?)";

                preparedStatement = connection.prepareStatement(homeQuery);
                preparedStatement.setInt(1, loan.getLoanId());
                preparedStatement.setString(2, homeLoan.getPropertyAddress());
                preparedStatement.setInt(3, homeLoan.getPropertyValue());

                preparedStatement.executeUpdate();
            } else if (loan instanceof CarLoan) {
                CarLoan carLoan = (CarLoan) loan;
                String carQuery = "INSERT INTO car_loans (loan_id, car_model, car_value) VALUES (?, ?, ?)";

                preparedStatement = connection.prepareStatement(carQuery);
                preparedStatement.setInt(1, loan.getLoanId());
                preparedStatement.setString(2, carLoan.getCarModel());
                preparedStatement.setInt(3, carLoan.getCarValue());

                preparedStatement.executeUpdate();
            }

            System.out.println("Loan application submitted successfully. Loan status is Pending.");
            return true;

        } catch (SQLException e) {
            System.err.println("Error applying for loan: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    @Override
    public double calculateInterest(int loanId) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new InvalidLoanException("Loan not found with ID: " + loanId);
        }

        return calculateInterest(loan.getPrincipalAmount(), loan.getInterestRate(), loan.getLoanTerm());
    }

    @Override
    public double calculateInterest(double principalAmount, double interestRate, int loanTerm) {
        // Interest = (Principal Amount * Interest Rate * Loan Tenure) / 12
        return (principalAmount * interestRate * loanTerm) / 1200; // 1200 = 12 * 100 (converting percentage)
    }

    @Override
    public boolean loanStatus(int loanId) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new InvalidLoanException("Loan not found with ID: " + loanId);
        }

        boolean isApproved = loan.getCustomer().getCreditScore() > 650;
        String status = isApproved ? "Approved" : "Rejected";

        try {
            connection = DBConnUtil.getConnection();
            String updateQuery = "UPDATE loans SET loan_status = ? WHERE loan_id = ?";

            preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, loanId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Loan with ID " + loanId + " has been " + status + ".");
                loan.setLoanStatus(status);
                return isApproved;
            } else {
                System.out.println("Failed to update loan status.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating loan status: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    @Override
    public double calculateEMI(int loanId) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new InvalidLoanException("Loan not found with ID: " + loanId);
        }

        return calculateEMI(loan.getPrincipalAmount(), loan.getInterestRate(), loan.getLoanTerm());
    }

    @Override
    public double calculateEMI(double principalAmount, double interestRate, int loanTerm) {
        // Convert annual interest rate to monthly and decimal form
        double monthlyInterestRate = interestRate / (12 * 100);

        // Calculate EMI using formula: [P * R * (1+R)^N] / [(1+R)^N-1]
        double emi = (principalAmount * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, loanTerm))
                / (Math.pow(1 + monthlyInterestRate, loanTerm) - 1);

        return emi;
    }

    @Override
    public boolean loanRepayment(int loanId, double amount) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new InvalidLoanException("Loan not found with ID: " + loanId);
        }

        double emiAmount = calculateEMI(loanId);

        if (amount < emiAmount) {
            System.out.println("Payment rejected. Amount is less than a single EMI payment of " + String.format("%.2f", emiAmount));
            return false;
        }

        // Calculate how many EMIs can be paid with the amount
        int numberOfEmis = (int) (amount / emiAmount);
        double actualPayment = numberOfEmis * emiAmount;

        try {
            connection = DBConnUtil.getConnection();
            String updateQuery = "UPDATE loans SET remaining_emis = remaining_emis - ? WHERE loan_id = ?";

            // First, check if remaining_emis exists or initialize it
            String checkQuery = "SELECT remaining_emis FROM loans WHERE loan_id = ?";
            preparedStatement = connection.prepareStatement(checkQuery);
            preparedStatement.setInt(1, loanId);
            resultSet = preparedStatement.executeQuery();

            int remainingEmis;
            if (resultSet.next()) {
                remainingEmis = resultSet.getInt("remaining_emis");
                if (resultSet.wasNull()) {
                    // Initialize remaining_emis if it's NULL
                    remainingEmis = loan.getLoanTerm();
                    String initQuery = "UPDATE loans SET remaining_emis = ? WHERE loan_id = ?";
                    preparedStatement = connection.prepareStatement(initQuery);
                    preparedStatement.setInt(1, remainingEmis);
                    preparedStatement.setInt(2, loanId);
                    preparedStatement.executeUpdate();
                }
            } else {
                throw new InvalidLoanException("Cannot find remaining EMIs for loan ID: " + loanId);
            }

            // Now update with the payment
            preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setInt(1, numberOfEmis);
            preparedStatement.setInt(2, loanId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Payment of " + String.format("%.2f", actualPayment) + " processed successfully.");
                System.out.println(numberOfEmis + " EMIs have been paid.");

                // Check if loan is fully paid
                preparedStatement = connection.prepareStatement(checkQuery);
                preparedStatement.setInt(1, loanId);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next() && resultSet.getInt("remaining_emis") <= 0) {
                    String completeQuery = "UPDATE loans SET loan_status = 'Completed' WHERE loan_id = ?";
                    preparedStatement = connection.prepareStatement(completeQuery);
                    preparedStatement.setInt(1, loanId);
                    preparedStatement.executeUpdate();
                    System.out.println("Loan has been fully paid off!");
                }

                return true;
            } else {
                System.out.println("Failed to process payment.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error processing payment: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    @Override
    public List<Loan> getAllLoan() {
        List<Loan> loans = new ArrayList<>();

        try {
            connection = DBConnUtil.getConnection();
            String query = "SELECT l.*, c.* FROM loans l JOIN customers c ON l.customer_id = c.customer_id";

            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                loans.add(extractLoanFromResultSet(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving all loans: " + e.getMessage());
        } finally {
            closeResources();
        }

        return loans;
    }

    @Override
    public Loan getLoanById(int loanId) throws InvalidLoanException {
        try {
            connection = DBConnUtil.getConnection();
            String query = "SELECT l.*, c.* FROM loans l JOIN customers c ON l.customer_id = c.customer_id WHERE l.loan_id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, loanId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extractLoanFromResultSet(resultSet);
            } else {
                throw new InvalidLoanException("No loan found with ID: " + loanId);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving loan by ID: " + e.getMessage());
            throw new InvalidLoanException("Error retrieving loan: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    // Helper method to extract loan from result set
    private Loan extractLoanFromResultSet(ResultSet rs) throws SQLException {
        // Extract customer details
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setName(rs.getString("name"));
        customer.setEmailAddress(rs.getString("email_address"));
        customer.setPhoneNumber(rs.getString("phone_number"));
        customer.setAddress(rs.getString("address"));
        customer.setCreditScore(rs.getInt("credit_score"));

        // Extract basic loan details
        int loanId = rs.getInt("loan_id");
        double principalAmount = rs.getDouble("principal_amount");
        double interestRate = rs.getDouble("interest_rate");
        int loanTerm = rs.getInt("loan_term");
        String loanType = rs.getString("loan_type");
        String loanStatus = rs.getString("loan_status");

        // Create base loan object
        Loan loan = new Loan(loanId, customer, principalAmount, interestRate, loanTerm, loanType, loanStatus);

        // For specific loan types, get additional details
        if (loanType.equals("HomeLoan")) {
            try {
                String homeQuery = "SELECT * FROM home_loans WHERE loan_id = ?";
                PreparedStatement homeStmt = connection.prepareStatement(homeQuery);
                homeStmt.setInt(1, loanId);
                ResultSet homeRs = homeStmt.executeQuery();

                if (homeRs.next()) {
                    HomeLoan homeLoan = new HomeLoan(loanId, customer, principalAmount, interestRate, loanTerm,
                            loanStatus, homeRs.getString("property_address"), homeRs.getInt("property_value"));
                    homeRs.close();
                    homeStmt.close();
                    return homeLoan;
                }
                homeRs.close();
                homeStmt.close();
            } catch (SQLException e) {
                System.err.println("Error retrieving home loan details: " + e.getMessage());
            }
        } else if (loanType.equals("CarLoan")) {
            try {
                String carQuery = "SELECT * FROM car_loans WHERE loan_id = ?";
                PreparedStatement carStmt = connection.prepareStatement(carQuery);
                carStmt.setInt(1, loanId);
                ResultSet carRs = carStmt.executeQuery();

                if (carRs.next()) {
                    CarLoan carLoan = new CarLoan(loanId, customer, principalAmount, interestRate, loanTerm,
                            loanStatus, carRs.getString("car_model"), carRs.getInt("car_value"));
                    carRs.close();
                    carStmt.close();
                    return carLoan;
                }
                carRs.close();
                carStmt.close();
            } catch (SQLException e) {
                System.err.println("Error retrieving car loan details: " + e.getMessage());
            }
        }

        return loan;
    }

    // Helper method to close resources
    private void closeResources() {
        try {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (connection != null) DBConnUtil.closeConnection(connection);
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}