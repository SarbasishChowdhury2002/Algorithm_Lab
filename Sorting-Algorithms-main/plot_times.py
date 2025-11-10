import pandas as pd
import matplotlib.pyplot as plt

# Read data from CSV file or copy-paste console output
df = pd.read_csv("execution_times.csv")

for col in df.columns[1:]:
    plt.plot(df["Size"], df[col], marker='o', label=col)

plt.xlabel("Number of Elements")
plt.ylabel("Execution Time (ms)")
plt.title("Sorting Algorithm Performance")
plt.legend()
plt.grid(True)
plt.show()
