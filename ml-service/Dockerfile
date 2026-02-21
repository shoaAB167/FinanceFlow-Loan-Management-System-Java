# Use a lightweight Python image
FROM python:3.9-slim
WORKDIR /app

# Install dependencies
# We copy requirements.txt first to leverage Docker layer caching
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the application code
COPY . .

# Expose the port Flask runs on
EXPOSE 5000

# Command to run the application
# We use 'python app.py' to start the Flask server
CMD ["python", "app.py"]
