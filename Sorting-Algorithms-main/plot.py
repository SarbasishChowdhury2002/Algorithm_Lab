import pandas as pd
import matplotlib.pyplot as plt

# Read data from CSV file
df = pd.read_csv("execution_times.csv")

for col in df.columns[1:]:
    plt.plot(df["Size"], df[col], marker='o', label=col)

plt.xlabel("Number of Elements")
plt.ylabel("Execution Time (ms)")
plt.title("Sorting Algorithm Performance")
plt.legend()
plt.grid(True)

# ✅ Force x-axis to use the exact CSV values
plt.xticks(df["Size"], rotation=45)

plt.show()
