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
                System.out.println("Добро пожаловать в банкомат!");

                boolean createNewAccount = false;
                while (true) {
                    System.out.print("Есть ли у вас счет? (да/нет): ");
                    String hasAccountChoice = scanner.nextLine();
                    if (hasAccountChoice.equalsIgnoreCase("нет")) {
                        createNewAccount = true;
                        break;
                    } else if (hasAccountChoice.equalsIgnoreCase("да")) {
                        break;
                    } else {
                        System.out.println("Ваш ответ не корректный. Пожалуйста, введите 'да' или 'нет'.");
                    }
                }

                long accountNumber = 0;
                if (!createNewAccount) {
                    while (true) {
                        System.out.print("Введите номер счета (7 цифр): ");
                        String accountNumberInput = scanner.nextLine();
                        if (accountNumberInput.length() == 7 && accountNumberInput.matches("\\d+")) {
                            accountNumber = Long.parseLong(accountNumberInput);
                            break;
                        } else {
                            System.out.println("Номер счета должен состоять из 7 цифр. Пожалуйста, попробуйте еще раз.");
                        }
                    }
                }

                Client client = getClient(accountNumber);
                if (client == null) {
                    if (createNewAccount) {
                        client = createNewAccount();
                        saveClientsToFile();
                    } else {
                        System.out.println("Счет не найден. До свидания!");
                        continue;
                    }
                }

                while (!exit) {
                    if (client.isBlocked()) {
                        System.out.println("Счет заблокирован. Обратитесь в банк.");
                        System.out.print("Хотите разблокировать счет при помощи кодового слова? (да/нет): ");
                        String unblockChoice = scanner.nextLine();
                        if (unblockChoice.equalsIgnoreCase("да")) {
                            System.out.print("Введите кодовое слово: ");
                            String enteredCodeWord = scanner.nextLine();
                            if (client.checkCodeWord(enteredCodeWord)) {
                                client.unblockAccount();
                                saveClientsToFile();
                                System.out.println("Счет успешно разблокирован.");
                            } else {
                                System.out.println("Неверное кодовое слово. Счет остается заблокированным.");
                            }
                        }
                    }

                    int pinAttempts = 0;
                    while (pinAttempts < 3) {
                        System.out.print("Введите PIN-код: ");
                        String enteredPin = scanner.nextLine();

                        if (client.checkPin(enteredPin)) {
                            handleClientOperations(client);
                            break;
                        } else {
                            pinAttempts++;
                            System.out.println("Неверный PIN-код. Попыток осталось: " + (3 - pinAttempts));
                            if (pinAttempts == 3) {
                                client.blockAccount();
                                saveClientsToFile();
                                System.out.println("Счет заблокирован. Обратитесь в банк.");
                                exit = true;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Произошла ошибка: " + e.getMessage());
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
                        System.err.println("Ошибка при парсинге данных клиента: " + e.getMessage());
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

        System.out.println("Счет успешно создан!");
        System.out.println("Имя: " + newClient.getName());
        System.out.println("Кодовое слово: " + newClient.getCodeWord());
        System.out.println("Номер счета: " + newClient.getAccountNumber());
        System.out.println("Пин-код: " + newClient.getPin());

        return newClient;
    }

    private static void handleClientOperations(Client client) {
        while (true) {
            System.out.println("Выберите операцию:");
            System.out.println("1. Просмотр баланса");
            System.out.println("2. Внесение средств");
            System.out.println("3. Снятие средств");
            System.out.println("4. Перевод между счетами");
            System.out.println("5. Блокировать счет");
            System.out.println("6. Выход");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Баланс счета: " + client.getBalance());
                    break;
                case 2:
                    System.out.print("Введите сумму для внесения: ");
                    double depositAmount = scanner.nextDouble();
                    scanner.nextLine();
                    if (depositAmount <= 0) {
                        System.out.println("Сумма должна быть положительной.");
                    } else {
                        client.deposit(depositAmount);
                        try {
                            saveClientsToFile();
                        } catch (IOException e) {
                            System.err.println("Ошибка при записи в файл клиентов: " + e.getMessage());
                        }
                        System.out.println("Средства внесены успешно.");
                    }
                    break;
                case 3:
                    System.out.print("Введите сумму для снятия: ");
                    double withdrawalAmount = scanner.nextDouble();
                    scanner.nextLine();
                    if (withdrawalAmount <= 0) {
                        System.out.println("Сумма должна быть положительной.");
                    } else {
                        if (client.withdraw(withdrawalAmount)) {
                            try {
                                saveClientsToFile();
                            } catch (IOException e) {
                                System.err.println("Ошибка при записи в файл клиентов: " + e.getMessage());
                            }
                            System.out.println("Средства сняты успешно.");
                        } else {
                            System.out.println("Недостаточно средств на счете.");
                        }
                    }
                    break;
                case 4:
                    System.out.print("Введите номер счета для перевода: ");
                    long targetAccountNumber = scanner.nextLong();
                    scanner.nextLine();
                    Client targetClient = getClient(targetAccountNumber);
                    if (targetClient == null) {
                        System.out.println("Счет с указанным номером не существует.");
                    } else if (targetClient == client) {
                        System.out.println("Нельзя перевести средства на собственный счет.");
                    } else {
                        System.out.print("Введите сумму для перевода: ");
                        double transferAmount = scanner.nextDouble();
                        scanner.nextLine();
                        if (transferAmount <= 0) {
                            System.out.println("Сумма для перевода должна быть положительной.");
                        } else if (client.withdraw(transferAmount)) {
                            targetClient.deposit(transferAmount);
                            try {
                                saveClientsToFile();
                            } catch (IOException e) {
                                System.err.println("Ошибка при записи в файл клиентов: " + e.getMessage());
                            }
                            System.out.println("Перевод выполнен успешно.");
                        } else {
                            System.out.println("Недостаточно средств на счете для перевода.");
                        }
                    }
                    break;
                case 5:
                    client.blockAccount();
                    try {
                        saveClientsToFile();
                    } catch (IOException e) {
                        System.err.println("Ошибка при записи в файл клиентов: " + e.getMessage());
                    }
                    System.out.println("Счет заблокирован.");
                    return;
                case 6:
                    System.out.println("До свидания!");
                    exit = true;
                    return;
                default:
                    System.out.println("Неверный выбор операции.");
            }
        }
    }
}

