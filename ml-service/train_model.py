import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report
import joblib

def generate_synthetic_data(n_samples=1000):
    np.random.seed(42)
    
    # Features
    monthly_income = np.random.normal(50000, 15000, n_samples)
    credit_score = np.random.normal(650, 100, n_samples)
    loan_amount = np.random.normal(200000, 100000, n_samples)
    tenure_months = np.random.choice([12, 24, 36, 48, 60], n_samples)
    existing_debt = np.random.normal(15000, 5000, n_samples)
    
    # Categorical Features (Mapped to Integers)
    # Employment: 0=Unemployed, 1=Self-Employed, 2=Salaried
    employment_status = np.random.choice([0, 1, 2], n_samples, p=[0.1, 0.3, 0.6])
    
    # Purpose: 0=Personal, 1=Education, 2=Home, 3=Business
    loan_purpose = np.random.choice([0, 1, 2, 3], n_samples)
    
    # Target variable logic
    # Higher income, higher credit score, lower loan amount, salaried, low debt -> Higher chance
    approval_prob = (
        (monthly_income / 100000) * 0.4 + # Increased weight for income
        (credit_score / 850) * 0.3 -      # Decreased weight for credit score
        (loan_amount / 500000) * 0.15 -
        (existing_debt / 50000) * 0.2 +
        (employment_status * 0.1) # Salaried gets boost
    )

    # Penalize if loan amount is too high compared to income (e.g., > 15x income)
    high_loan_ratio = (loan_amount > (monthly_income * 15)).astype(int)
    approval_prob -= high_loan_ratio * 0.3

    # Penalize very low absolute income
    low_income = (monthly_income < 10000).astype(int)
    approval_prob -= low_income * 0.4 # Increased penalty

    # Penalize low credit score
    low_credit = (credit_score < 600).astype(int)
    approval_prob -= low_credit * 0.4
    
    # Add some noise
    approval_prob += np.random.normal(0, 0.1, n_samples)
    
    # Convert to binary class (1 = Approved, 0 = Rejected)
    is_approved = (approval_prob > 0.45).astype(int) # Adjusted threshold
    
    df = pd.DataFrame({
        'monthly_income': monthly_income,
        'credit_score': credit_score,
        'loan_amount': loan_amount,
        'tenure_months': tenure_months,
        'existing_debt': existing_debt,
        'employment_status': employment_status,
        'loan_purpose': loan_purpose,
        'is_approved': is_approved
    })
    
    return df

def train_model():
    print("Generating synthetic data...")
    df = generate_synthetic_data(4000)
    
    X = df[['monthly_income', 'credit_score', 'loan_amount', 'tenure_months', 'existing_debt', 'employment_status', 'loan_purpose']]
    y = df['is_approved']
    
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    print("Training Random Forest Classifier...")
    clf = RandomForestClassifier(n_estimators=100, random_state=42)
    clf.fit(X_train, y_train)
    
    y_pred = clf.predict(X_test)
    print("Model Accuracy:", accuracy_score(y_test, y_pred))
    print("\nClassification Report:\n", classification_report(y_test, y_pred))

    # Precision (How correct the positive predictions were)
    # Recall (How many real approved cases you found)
    # F1 Score (Harmonic mean of precision and recall)
    # Support (Number of actual cases)
    
    # Save the model
    joblib.dump(clf, 'credit_risk_model.pkl')
    print("Model saved to credit_risk_model.pkl")

if __name__ == "__main__":
    train_model()
