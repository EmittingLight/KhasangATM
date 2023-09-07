package com.yaga;


import java.util.Random;
import java.util.Scanner;

class Client {
    private String name;
    private long accountNumber;
    private String pin;
    private String codeWord;
    private double balance;
    private boolean blocked;
    private int failedPinAttempts;

    public Client(String name, long accountNumber, String pin, String codeWord, double balance, boolean blocked, int failedPinAttempts) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.codeWord = codeWord;
        this.balance = balance;
        this.blocked = blocked;
        this.failedPinAttempts = failedPinAttempts;
    }



    public double getBalance() {
        return balance;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void blockAccount() {
        blocked = true;
    }

    public void unblockAccount() {
        blocked = false;
    }

    public boolean checkPin(String enteredPin) {
        if (blocked) {
            return false;
        }
        if (pin.equals(enteredPin)) {
            failedPinAttempts = 0;
            return true;
        } else {
            failedPinAttempts++;
            if (failedPinAttempts >= 3) {
                blockAccount();
            }
            return false;
        }
    }

    public void deposit(double amount) {
        if (!blocked) {
            balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (!blocked && balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public String getCodeWord() {
        return codeWord;
    }

    public String getName() {
        return name;
    }

    public String getPin() {
        return pin;
    }

    public int getFailedPinAttempts() {
        return failedPinAttempts;
    }


    public static Client createNewClient() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("¬ведите ваше им€: ");
        String name = scanner.nextLine();
        System.out.print("¬ведите кодовое слово: ");
        String codeWord = scanner.nextLine();


        long accountNumber = generateRandomAccountNumber();
        String pin = generateRandomPIN();

        return new Client(name, accountNumber, pin, codeWord, 0, false, 0);
    }


    private static long generateRandomAccountNumber() {
        Random random = new Random();
        long accountNumber = 1000000 + random.nextInt(9000000);
        return accountNumber;
    }


    private static String generateRandomPIN() {
        Random random = new Random();
        int pin = 1000 + random.nextInt(9000);
        return Integer.toString(pin);
    }

    public boolean checkCodeWord(String enteredCodeWord) {
        return codeWord.equals(enteredCodeWord);
    }

}