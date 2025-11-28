# Charge and Repayment Logic

This document explains how charges are applied, tracked, and paid off in the FinanceFlow Loan Management System.

## Overview

Charges (e.g., late fees, processing fees) are distinct entities linked to a Loan Account. They are tracked separately from the loan's principal and interest schedule but are prioritized during repayment.

## Charge Lifecycle

1.  **Creation**: A charge is added to a loan via the `ChargeService`. It is initially marked as `isPaid = false`.
2.  **Repayment**: When a repayment is made, the system first checks for any outstanding (unpaid) charges.
3.  **Deduction**: The repayment amount is first used to pay off these charges.
    - If the amount covers the charge, the charge is marked `isPaid = true`.
    - If the amount is insufficient to cover a charge, the system currently stops (or could be enhanced to handle partial payments).
4.  **Installment Payment**: Any remaining amount after paying charges is then applied to the loan's installments (EMI).

## Foreclosure

A loan cannot be foreclosed if there are any outstanding charges. The system checks for `Charge` entities linked to the loan where `isPaid` is `false`.

## Technical Details

- **Entity**: `Charge` has an `isPaid` boolean field.
- **Repository**: `ChargeRepository` provides methods to find unpaid charges (`findByLoanAccountAndIsPaidFalse`).
- **Service**: `RepaymentServiceImpl` orchestrates the payment logic, ensuring charges are settled before installments.
