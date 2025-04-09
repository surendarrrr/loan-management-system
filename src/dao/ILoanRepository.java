package dao;

import entity.Loan;
import exception.InvalidLoanException;
import java.util.List;

public interface ILoanRepository {

    // Apply for a new loan
    boolean applyLoan(Loan loan);

    // Calculate interest for a loan by ID
    double calculateInterest(int loanId) throws InvalidLoanException;

    // Overloaded calculate interest with direct parameters
    double calculateInterest(double principalAmount, double interestRate, int loanTerm);

    // Check and update loan status based on credit score
    boolean loanStatus(int loanId) throws InvalidLoanException;

    // Calculate EMI for a loan by ID
    double calculateEMI(int loanId) throws InvalidLoanException;

    // Overloaded calculate EMI with direct parameters
    double calculateEMI(double principalAmount, double interestRate, int loanTerm);

    // Process loan repayment
    boolean loanRepayment(int loanId, double amount) throws InvalidLoanException;

    // Get all loans
    List<Loan> getAllLoan();

    // Get a loan by ID
    Loan getLoanById(int loanId) throws InvalidLoanException;
}