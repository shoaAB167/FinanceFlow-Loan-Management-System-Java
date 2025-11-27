# Credit Risk Assessment ML Service

This service provides real-time credit risk assessment using a Machine Learning model (Random Forest Classifier). It evaluates loan applications based on various financial factors to predict approval probability and assign a risk score.

## 🚀 Features

- **Real-time Prediction**: REST API endpoint for instant credit risk evaluation.
- **Robust Model**: Trained on 4000 synthetic records with realistic financial patterns.
- **Risk Scoring**: Returns a probability score (0-1) indicating the likelihood of approval.
- **Explainable Logic**: Considers income, credit score, loan amount, debt, and employment status.

## 🛠️ Setup & Installation

### Prerequisites

- Python 3.8+
- pip

### Installation

1. Navigate to the `ml-service` directory:
   ```bash
   cd ml-service
   ```
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

## 🏃‍♂️ Running the Service

### 1. Train the Model

Before running the API, you need to generate the model file (`credit_risk_model.pkl`).

```bash
python train_model.py
```

_This script generates 4000 synthetic records, trains the Random Forest model, and saves it._

### 2. Start the API Server

```bash
python app.py
```

The service will start on `http://localhost:5000`.

## 🔌 API Documentation

### Predict Credit Risk

**Endpoint**: `POST /predict`

**Request Body**:

```json
{
  "monthlyIncome": 50000,
  "creditScore": 700,
  "principal": 200000,
  "tenureMonths": 24,
  "existingDebt": 10000,
  "employmentStatus": "SALARIED",
  "loanPurpose": "HOME"
}
```

**Response**:

```json
{
  "isApproved": true,
  "riskScore": 0.78,
  "reason": "Model prediction based on historical data"
}
```

## 🧠 Model Logic

The model evaluates the following factors:

- **Income**: Higher income increases approval chance.
- **Credit Score**: Score < 600 is heavily penalized.
- **Loan Amount**: High loan-to-income ratio reduces approval chance.
- **Existing Debt**: High debt burden reduces approval chance.
- **Employment**: Salaried individuals get a slight boost.

## 🧪 Testing

Run the included test suite to verify the service against various scenarios:

```bash
python test_api.py
```

_This runs 11 test cases covering ideal candidates, high-risk profiles, student loans, etc._
