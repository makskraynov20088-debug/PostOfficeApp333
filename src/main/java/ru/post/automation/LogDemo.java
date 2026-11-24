package ru.post.automation;

import ru.post.automation.service.RegistrationService;
import ru.post.automation.service.SmsNotifier;
import ru.post.automation.service.TariffCalculator;
import ru.post.automation.service.TrackingService;
import ru.post.automation.service.WarehouseService;

public class LogDemo {
    public static void main(String[] args) {
        RegistrationService registration = new RegistrationService(
                new TariffCalculator(), new WarehouseService(2), new TrackingService(), new SmsNotifier());
        String[] names = {"Попов Дмитрий", "Васильева Мария", "Орлов Андрей"};
        for (String name : names) {
            try {
                registration.register("Крайнов Максим", name, "+79052223344", 1.5, 2);
            } catch (RuntimeException e) {
                System.out.println("Сбой при приёме отправления: " + e.getMessage());
            }
        }
    }
}
