# 🍽️ The Crispy Fork - Digital Menu & Billing System

Welcome to **The Crispy Fork - Digital Menu & Billing System**, a robust, object-oriented console application engineered in Java using the industry-standard **Model-View-Controller (MVC)** architectural pattern. 

This system provides a frictionless interface for restaurant waiters and operators to manage table dining sessions, record orders with customized preparation instructions, view active dining rooms, compute dynamic taxes, split bills safely, and output beautifully aligned receipts.

---

## 🧑‍💻 Developer Profile

* **Author:** Palak Agarwal
* **Email:** [inagarwalpalak2005@gmail.com](mailto:inagarwalpalak2005@gmail.com)
* **Role:** Java Software Engineering Intern

---

## 🌟 Core Features

### 1. Robust Input Parsing Engine (Zero-Buffer Issues)
* **The Problem:** Traditional Java command-line apps mixing `nextInt()`, `nextByte()`, and `nextLine()` frequently experience scanner-skipping bugs due to leftover newline (`\n`) characters in the input stream.
* **The Solution:** The system is engineered to utilize complete line-based reading (`sc.nextLine()`) for every input prompt. Inputs are parsed dynamically, catching all format violations (`NumberFormatException`) and ensuring that invalid keystrokes do not crash the terminal or skip prompts.

### 2. Strict Domain Entity Validations
* Heavy reliance on defensive programming: every domain constructor validates parameters immediately.
* Prevents corrupted domain states like negative item prices, zero or negative quantities, empty food category fields, or table numbers out of bounds (1 to 50).

### 3. Smart Dine-in Session Flow & Cancellations
* Waiters can type `0` during order-taking to instantly cancel or finish their current addition loop.
* If a table session is brand new and has no items upon cancellation, the system purges it from the active transaction registry automatically, saving memory and keeping dashboard tables clean.

### 4. Interactive Split-Bill & GST Calculator
* Automatically calculates a constant `5%` tax (GST) on subtotal values.
* Allows split-billing configurations among arbitrary guest counts, with dedicated exception safeguards.

### 5. Architectural Cleanliness & Reordering
* Function structures in all source files are organized logically (Attributes ➡️ Constructor ➡️ Core Business / View Flow ➡️ Getters/Setters ➡️ Overrides).
* Rich, comprehensive JavaDocs have been written for all public APIs, parameters, exceptions, and return types.

---

## 📂 Project Architecture & Directory Structure

The project strictly follows the **MVC architectural pattern**:

```text
RestaurantManagement/
│
├── Main.java               # Main entry point bootstrapping the system
├── README.md               # Project documentation and specifications
│
├── model/                  # Domain Entities containing pure data and calculations
│   ├── MenuItem.java       # Represents a food item in the master catalog
│   ├── OrderItem.java      # Composition representing a MenuItem and its quantity
│   └── TableOrder.java     # Tracks ordered items, payment status, tax and splits per table
│
├── view/                   # Console UI layer handling scanner inputs and receipt print layouts
│   └── RestaurantView.java
│
└── controller/             # Bridge connecting View and Model, managing control flow
    └── RestaurantController.java
```

---

## 🚀 Execution & Setup Guide

### Prerequisites
* **Java Development Kit (JDK):** Version 8 or higher is required.

### 1. Compilation
To compile the system, open your terminal at the root of the workspace directory (`Java Internship/src`) and run:

```bash
javac Projects/RestaurantManagement/Main.java Projects/RestaurantManagement/model/*.java Projects/RestaurantManagement/view/*.java Projects/RestaurantManagement/controller/*.java
```

Alternatively, you can compile from inside the `RestaurantManagement` directory itself:

```bash
javac -d . Main.java model/*.java view/*.java controller/*.java
```

### 2. Running the Application
Once compiled successfully, run the application from the classroot using:

```bash
java Projects.RestaurantManagement.Main
```

---

## 🛠️ Technological Stack

* **Core Language:** Java Standard Edition (SE)
* **Framework:** Vanilla Java (No external dependencies)
* **Design Philosophy:** Clean Code, SOLID Principles, Model-View-Controller (MVC) Pattern, Exception-Safety, and Defensive Programming.
