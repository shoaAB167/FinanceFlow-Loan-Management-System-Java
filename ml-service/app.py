from flask import Flask, request, jsonify
import joblib
import pandas as pd
import numpy as np
import os

app = Flask(__name__)

# Load model
MODEL_PATH = 'credit_risk_model.pkl'
if os.path.exists(MODEL_PATH):
    model = joblib.load(MODEL_PATH)
else:
    model = None
    print("Warning: Model file not found. Please run train_model.py first.")

def map_employment_status(status):
    status = str(status).upper()
    if 'SALARIED' in status:
        return 2
    elif 'SELF' in status:
        return 1
    else:
        return 0 # Unemployed or unknown

def map_loan_purpose(purpose):
    purpose = str(purpose).upper()
    if 'EDUCATION' in purpose:
        return 1
    elif 'HOME' in purpose:
        return 2
    elif 'BUSINESS' in purpose:
        return 3
    else:
        return 0 # Personal or default

@app.route('/predict', methods=['POST'])
def predict():
    if not model:
        return jsonify({'error': 'Model not loaded'}), 500
    
    try:
        data = request.get_json()
        
        # Extract and map features
        features = pd.DataFrame([{
            'monthly_income': data.get('monthlyIncome', 0),
            'credit_score': data.get('creditScore', 0),
            'loan_amount': data.get('principal', 0),
            'tenure_months': data.get('tenureMonths', 0),
            'existing_debt': data.get('existingDebt', 0),
            'employment_status': map_employment_status(data.get('employmentStatus', '')),
            'loan_purpose': map_loan_purpose(data.get('loanPurpose', ''))
        }])
        
        # Predict
        prediction = model.predict(features)[0]
        probability = model.predict_proba(features)[0][1]
        
        result = {
            'isApproved': bool(prediction),
            'riskScore': float(probability),
            'reason': 'Model prediction based on historical data' if prediction else 'High risk detected by ML model'
        }
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({'error': str(e)}), 400

if __name__ == '__main__':
    app.run(port=5000, debug=True)
