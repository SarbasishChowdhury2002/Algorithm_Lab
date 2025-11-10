import pandas as pd
import matplotlib.pyplot as plt

# Read data
df = pd.read_csv("times.csv")

# Clean headers (remove extra spaces)
df.columns = df.columns.str.strip()
print("Columns:", df.columns.tolist())

# Plot
for col in df.columns[1:]:
    plt.plot(df["Size"], df[col], marker='o', linestyle='-', label=col)

plt.xlabel("Number of Matrices")
plt.ylabel("Execution Time (ms)")
plt.title("Matrix Chain Multiplication: D&C vs DP")
plt.legend()
plt.grid(True, linestyle="--", alpha=0.7)
plt.xticks(df["Size"])

# ❌ remove log scale so 0 is visible
# plt.yscale("log")

plt.savefig("plot.png", dpi=300, bbox_inches="tight")
print("✅ Plot saved as plot.png (DP visible at 0)")
