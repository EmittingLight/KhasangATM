package com.yaga;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ATM {
    private static final String CLIENTS_FILE = "clients.csv";
    private static final Map<Long, Client> clients = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean exit = false;

    public static void main(String[] args) {
        try {
            loadClientsFromFile();

            while (!exit) {
                System.out.println("����� ���������� � ��������!");

                boolean createNewAccount = false;
                while (true) {
                    System.out.print("���� �� � ��� ����? (��/���): ");
                    String hasAccountChoice = scanner.nextLine();
                    if (hasAccountChoice.equalsIgnoreCase("���")) {
                        createNewAccount = true;
                        break;
                    } else if (hasAccountChoice.equalsIgnoreCase("��")) {
                        break;
                    } else {
                        System.out.println("��� ����� �� ����������. ����������, ������� '��' ��� '���'.");
                    }
                }

                long accountNumber = 0;
                if (!createNewAccount) {
                    while (true) {
                        System.out.print("������� ����� ����� (7 ����): ");
                        String accountNumberInput = scanner.nextLine();
                        if (accountNumberInput.length() == 7 && accountNumberInput.matches("\\d+")) {
                            accountNumber = Long.parseLong(accountNumberInput);
                            break;
                        } else {
                            System.out.println("����� ����� ������ �������� �� 7 ����. ����������, ���������� ��� ���.");
                        }
                    }
                }

                Client client = getClient(accountNumber);
                if (client == null) {
                    if (createNewAccount) {
                        client = createNewAccount();
                        saveClientsToFile();
                    } else {
                        System.out.println("���� �� ������. �� ��������!");
                        continue;
                    }
                }

                while (!exit) {
                    if (client.isBlocked()) {
                        System.out.println("���� ������������. ���������� � ����.");
                        System.out.print("������ �������������� ���� ��� ������ �������� �����? (��/���): ");
                        String unblockChoice = scanner.nextLine();
                        if (unblockChoice.equalsIgnoreCase("��")) {
                            System.out.print("������� ������� �����: ");
                            String enteredCodeWord = scanner.nextLine();
                            if (client.checkCodeWord(enteredCodeWord)) {
                                client.unblockAccount();
                                saveClientsToFile();
                                System.out.println("���� ������� �������������.");
                            } else {
                                System.out.println("�������� ������� �����. ���� �������� ���������������.");
                            }
                        }
                    }

                    int pinAttempts = 0;
                    while (pinAttempts < 3) {
                        System.out.print("������� PIN-���: ");
                        String enteredPin = scanner.nextLine();

                        if (client.checkPin(enteredPin)) {
                            handleClientOperations(client);
                            break;
                        } else {
                            pinAttempts++;
                            System.out.println("�������� PIN-���. ������� ��������: " + (3 - pinAttempts));
                            if (pinAttempts == 3) {
                                client.blockAccount();
                                saveClientsToFile();
                                System.out.println("���� ������������. ���������� � ����.");
                                exit = true;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("��������� ������: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void loadClientsFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(CLIENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) {
                    try {
                        String name = parts[0];
                        long accountNumber = Long.parseLong(parts[1]);
                        String pin = parts[2];
                        String codeWord = parts[3];
                        double balance = Double.parseDouble(parts[4]);
                        boolean blocked = Boolean.parseBoolean(parts[5]);
                        int failedPinAttempts = Integer.parseInt(parts[6]);
                        Client client = new Client(name, accountNumber, pin, codeWord, balance, blocked, failedPinAttempts);
                        clients.put(accountNumber, client);
                    } catch (NumberFormatException e) {
                        System.err.println("������ ��� �������� ������ �������: " + e.getMessage());
                    }
                }
            }
        }
    }

    private static void saveClientsToFile() throws IOException {
        try (FileWriter writer = new FileWriter(CLIENTS_FILE)) {
            for (Client client : clients.values()) {
                writer.write(
                        client.getName() + "," + client.getAccountNumber() + ","
                                + client.getPin() + "," + client.getCodeWord() + ","
                                + client.getBalance() + "," + client.isBlocked() + ","
                                + client.getFailedPinAttempts() + "\n"
                );
            }
        }
    }

    private static Client getClient(long accountNumber) {
        return clients.get(accountNumber);
    }

    private static Client createNewAccount() {
        Client newClient = Client.createNewClient();
        clients.put(newClient.getAccountNumber(), newClient);

        System.out.println("���� ������� ������!");
        System.out.println("���: " + newClient.getName());
        System.out.println("������� �����: " + newClient.getCodeWord());
        System.out.println("����� �����: " + newClient.getAccountNumber());
        System.out.println("���-���: " + newClient.getPin());

        return newClient;
    }

    private static void handleClientOperations(Client client) {
        while (true) {
            System.out.println("�������� ��������:");
            System.out.println("1. �������� �������");
            System.out.println("2. �������� �������");
            System.out.println("3. ������ �������");
            System.out.println("4. ������� ����� �������");
            System.out.println("5. ����������� ����");
            System.out.println("6. �����");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("������ �����: " + client.getBalance());
                    break;
                case 2:
                    System.out.print("������� ����� ��� ��������: ");
                    double depositAmount = scanner.nextDouble();
                    scanner.nextLine();
                    if (depositAmount <= 0) {
                        System.out.println("����� ������ ���� �������������.");
                    } else {
                        client.deposit(depositAmount);
                        try {
                            saveClientsToFile();
                        } catch (IOException e) {
                            System.err.println("������ ��� ������ � ���� ��������: " + e.getMessage());
                        }
                        System.out.println("�������� ������� �������.");
                    }
                    break;
                case 3:
                    System.out.print("������� ����� ��� ������: ");
                    double withdrawalAmount = scanner.nextDouble();
                    scanner.nextLine();
                    if (withdrawalAmount <= 0) {
                        System.out.println("����� ������ ���� �������������.");
                    } else {
                        if (client.withdraw(withdrawalAmount)) {
                            try {
                                saveClientsToFile();
                            } catch (IOException e) {
                                System.err.println("������ ��� ������ � ���� ��������: " + e.getMessage());
                            }
                            System.out.println("�������� ����� �������.");
                        } else {
                            System.out.println("������������ ������� �� �����.");
                        }
                    }
                    break;
                case 4:
                    System.out.print("������� ����� ����� ��� ��������: ");
                    long targetAccountNumber = scanner.nextLong();
                    scanner.nextLine();
                    Client targetClient = getClient(targetAccountNumber);
                    if (targetClient == null) {
                        System.out.println("���� � ��������� ������� �� ����������.");
                    } else if (targetClient == client) {
                        System.out.println("������ ��������� �������� �� ����������� ����.");
                    } else {
                        System.out.print("������� ����� ��� ��������: ");
                        double transferAmount = scanner.nextDouble();
                        scanner.nextLine();
                        if (transferAmount <= 0) {
                            System.out.println("����� ��� �������� ������ ���� �������������.");
                        } else if (client.withdraw(transferAmount)) {
                            targetClient.deposit(transferAmount);
                            try {
                                saveClientsToFile();
                            } catch (IOException e) {
                                System.err.println("������ ��� ������ � ���� ��������: " + e.getMessage());
                            }
                            System.out.println("������� �������� �������.");
                        } else {
                            System.out.println("������������ ������� �� ����� ��� ��������.");
                        }
                    }
                    break;
                case 5:
                    client.blockAccount();
                    try {
                        saveClientsToFile();
                    } catch (IOException e) {
                        System.err.println("������ ��� ������ � ���� ��������: " + e.getMessage());
                    }
                    System.out.println("���� ������������.");
                    return;
                case 6:
                    System.out.println("�� ��������!");
                    exit = true;
                    return;
                default:
                    System.out.println("�������� ����� ��������.");
            }
        }
    }
}

