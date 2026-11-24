package ru.post.automation;

import ru.post.automation.api.ApiClient;
import ru.post.automation.api.ApiServer;
import ru.post.automation.model.Shipment;
import ru.post.automation.model.ShipmentStatus;
import ru.post.automation.service.RegistrationService;
import ru.post.automation.service.SmsNotifier;
import ru.post.automation.service.StorageFeeCalculator;
import ru.post.automation.service.TariffCalculator;
import ru.post.automation.service.TrackingService;
import ru.post.automation.service.WarehouseService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        TariffCalculator tariff = new TariffCalculator();
        TrackingService tracking = new TrackingService();
        RegistrationService registration = new RegistrationService(
                tariff, new WarehouseService(20), tracking, new SmsNotifier());
        StorageFeeCalculator fees = new StorageFeeCalculator();
        Scanner in = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== АРМ оператора «Почта России» ===");
            System.out.println("1. Зарегистрировать отправление");
            System.out.println("2. Найти отправление по трек-номеру");
            System.out.println("3. Перевести отправление на следующий статус");
            System.out.println("4. Рассчитать стоимость пересылки");
            System.out.println("5. Рассчитать плату за хранение");
            System.out.println("6. Демонстрация REST API");
            System.out.println("0. Выход");
            System.out.print("Выберите пункт: ");
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        System.out.print("Отправитель: ");
                        String sender = in.nextLine();
                        System.out.print("Получатель: ");
                        String recipient = in.nextLine();
                        System.out.print("Телефон получателя (+7XXXXXXXXXX): ");
                        String phone = in.nextLine().trim();
                        System.out.print("Вес, кг: ");
                        double weight = Double.parseDouble(in.nextLine().replace(',', '.'));
                        System.out.print("Зона доставки (1-5): ");
                        int zone = Integer.parseInt(in.nextLine().trim());
                        Shipment s = registration.register(sender, recipient, phone, weight, zone);
                        System.out.println("Отправление принято. Трек-номер: " + s.getTrackNumber()
                                + ", стоимость " + s.getCost() + " руб., ячейка " + s.getCellCode());
                    }
                    case "2" -> {
                        System.out.print("Трек-номер: ");
                        Shipment s = tracking.find(in.nextLine().trim());
                        System.out.println("Получатель: " + s.getRecipientName() + ", статус: "
                                + s.getStatus().getTitle() + ", ячейка " + s.getCellCode());
                        System.out.println("История: " + s.getHistory());
                    }
                    case "3" -> {
                        System.out.print("Трек-номер: ");
                        String track = in.nextLine().trim();
                        ShipmentStatus current = tracking.find(track).getStatus();
                        ShipmentStatus[] all = ShipmentStatus.values();
                        if (current.ordinal() == all.length - 1) {
                            System.out.println("Отправление уже вручено");
                        } else {
                            Shipment s = registration.moveStatus(track, all[current.ordinal() + 1]);
                            System.out.println("Новый статус: " + s.getStatus().getTitle());
                        }
                    }
                    case "4" -> {
                        System.out.print("Вес, кг: ");
                        double weight = Double.parseDouble(in.nextLine().replace(',', '.'));
                        System.out.print("Зона доставки (1-5): ");
                        int zone = Integer.parseInt(in.nextLine().trim());
                        System.out.println("Стоимость: " + tariff.calculate(weight, zone) + " руб.");
                    }
                    case "5" -> {
                        System.out.print("Срок хранения, дней: ");
                        int days = Integer.parseInt(in.nextLine().trim());
                        System.out.println("Плата за хранение: " + fees.calculate(days) + " руб.");
                    }
                    case "6" -> runApiDemo(registration, tracking);
                    case "0" -> running = false;
                    default -> System.out.println("Нет такого пункта меню");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число");
            } catch (RuntimeException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static void runApiDemo(RegistrationService registration, TrackingService tracking) throws Exception {
        ApiServer server = new ApiServer(8080, registration, tracking);
        server.start();
        ApiClient client = new ApiClient("http://localhost:8080");
        String body = "{\"sender\":\"Крайнов Максим\",\"recipient\":\"Попов Дмитрий\","
                + "\"phone\":\"+79052223344\",\"weightKg\":2.2,\"zone\":2}";
        System.out.println("POST /api/shipments " + body);
        ApiClient.Reply created = client.post("/api/shipments", body);
        System.out.println(created.status() + " " + created.body());
        String track = ru.post.automation.api.Json.parse(created.body()).get("trackNumber");
        System.out.println("GET /api/shipments/" + track);
        ApiClient.Reply found = client.get(track);
        System.out.println(found.status() + " " + found.body());
        System.out.println("GET /api/shipments/00000000000000");
        ApiClient.Reply missing = client.get("00000000000000");
        System.out.println(missing.status() + " " + missing.body());
        server.stop();
    }
}
