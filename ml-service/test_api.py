import urllib.request
import json

def test_prediction(data, case_name):
    url = 'http://127.0.0.1:5000/predict'
    json_data = json.dumps(data).encode('utf-8')
    req = urllib.request.Request(url, data=json_data, headers={'Content-Type': 'application/json'})

    print(f"\nTesting Case: {case_name}")
    try:
        with urllib.request.urlopen(req) as response:
            result = response.read().decode('utf-8')
            print(f"Status Code: {response.getcode()}")
            print(f"Response: {result}")
    except Exception as e:
        print(f"Error: {e}")

test_cases = [
    {
        "name": "1. Ideal Candidate (High Income, High Credit, Low Loan)",
        "data": {"monthlyIncome": 80000, "creditScore": 800, "principal": 100000, "tenureMonths": 24, "existingDebt": 5000, "employmentStatus": "SALARIED", "loanPurpose": "HOME"}
    },
    {
        "name": "2. Low Income, High Loan (Should Reject)",
        "data": {"monthlyIncome": 1000, "creditScore": 700, "principal": 200000, "tenureMonths": 24, "existingDebt": 10000, "employmentStatus": "SALARIED", "loanPurpose": "HOME"}
    },
    {
        "name": "3. High Debt Ratio (Should Reject)",
        "data": {"monthlyIncome": 50000, "creditScore": 700, "principal": 200000, "tenureMonths": 24, "existingDebt": 40000, "employmentStatus": "SALARIED", "loanPurpose": "PERSONAL"}
    },
    {
        "name": "4. Poor Credit Score (Should Reject)",
        "data": {"monthlyIncome": 60000, "creditScore": 500, "principal": 100000, "tenureMonths": 12, "existingDebt": 5000, "employmentStatus": "SALARIED", "loanPurpose": "EDUCATION"}
    },
    {
        "name": "5. Unemployed (Should Reject)",
        "data": {"monthlyIncome": 0, "creditScore": 650, "principal": 50000, "tenureMonths": 12, "existingDebt": 0, "employmentStatus": "UNEMPLOYED", "loanPurpose": "PERSONAL"}
    },
    {
        "name": "6. Self-Employed, Good Stats (Should Approve)",
        "data": {"monthlyIncome": 70000, "creditScore": 750, "principal": 300000, "tenureMonths": 36, "existingDebt": 10000, "employmentStatus": "SELF_EMPLOYED", "loanPurpose": "BUSINESS"}
    },
    {
        "name": "7. Borderline Case (Mid Income, Mid Credit)",
        "data": {"monthlyIncome": 30000, "creditScore": 650, "principal": 100000, "tenureMonths": 24, "existingDebt": 5000, "employmentStatus": "SALARIED", "loanPurpose": "PERSONAL"}
    },
    {
        "name": "8. High Loan, Long Tenure (Maybe Approve)",
        "data": {"monthlyIncome": 90000, "creditScore": 780, "principal": 500000, "tenureMonths": 60, "existingDebt": 20000, "employmentStatus": "SALARIED", "loanPurpose": "HOME"}
    },
    {
        "name": "9. Very Low Income, Small Loan (Should Reject due to min income)",
        "data": {"monthlyIncome": 5000, "creditScore": 700, "principal": 10000, "tenureMonths": 12, "existingDebt": 0, "employmentStatus": "SALARIED", "loanPurpose": "PERSONAL"}
    },
    {
        "name": "10. Student Loan (Low Income, Education)",
        "data": {"monthlyIncome": 10000, "creditScore": 650, "principal": 50000, "tenureMonths": 48, "existingDebt": 0, "employmentStatus": "STUDENT", "loanPurpose": "EDUCATION"}
    },
    {
        "name": "11. High Income, Bad Credit (Risky)",
        "data": {"monthlyIncome": 100000, "creditScore": 550, "principal": 200000, "tenureMonths": 24, "existingDebt": 10000, "employmentStatus": "SALARIED", "loanPurpose": "HOME"}
    }
]

for case in test_cases:
    test_prediction(case["data"], case["name"])
