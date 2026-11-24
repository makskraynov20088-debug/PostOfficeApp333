package ru.post.automation.ui;

import ru.post.automation.api.ApiClient;
import ru.post.automation.api.ApiServer;
import ru.post.automation.api.Json;
import ru.post.automation.model.Shipment;
import ru.post.automation.model.ShipmentStatus;
import ru.post.automation.service.RegistrationService;
import ru.post.automation.service.SmsNotifier;
import ru.post.automation.service.StorageFeeCalculator;
import ru.post.automation.service.TariffCalculator;
import ru.post.automation.service.TrackingService;
import ru.post.automation.service.WarehouseService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class MainWindow {
    private final TariffCalculator tariff = new TariffCalculator();
    private final TrackingService tracking = new TrackingService();
    private final RegistrationService registration =
            new RegistrationService(tariff, new WarehouseService(20), tracking, new SmsNotifier());
    private final StorageFeeCalculator fees = new StorageFeeCalculator();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().show());
    }

    public void show() {
        JFrame frame = new JFrame("АРМ оператора — Почта России");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel title = new JLabel("Почта России — АРМ оператора", JLabel.CENTER);
        title.setOpaque(true);
        title.setBackground(new Color(0, 70, 170));
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Приём отправления", registrationTab());
        tabs.addTab("Отслеживание", trackingTab());
        tabs.addTab("Стоимость", tariffTab());
        tabs.addTab("Хранение", storageTab());
        tabs.addTab("REST API", apiTab());
        frame.add(title, BorderLayout.NORTH);
        frame.add(tabs, BorderLayout.CENTER);
        frame.setSize(760, 520);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel form() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        return p;
    }

    private JTextField addField(JPanel p, int row, String label) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        p.add(new JLabel(label), c);
        JTextField field = new JTextField(24);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        p.add(field, c);
        return field;
    }

    private JComboBox<String> addZone(JPanel p, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        p.add(new JLabel("Зона доставки"), c);
        JComboBox<String> box = new JComboBox<>(new String[]{"1", "2", "3", "4", "5"});
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(box, c);
        return box;
    }

    private JButton addButton(JPanel p, int row, String text) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 4, 4, 4);
        c.gridx = 1;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        JButton b = new JButton(text);
        p.add(b, c);
        return b;
    }

    private JTextArea addResult(JPanel p, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 4, 4, 4);
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        JTextArea area = new JTextArea(7, 40);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(area.getFont().deriveFont(14f));
        p.add(new JScrollPane(area), c);
        return area;
    }

    private void ok(JTextArea area, String text) {
        area.setForeground(Color.BLACK);
        area.setText(text);
    }

    private void fail(JTextArea area, String text) {
        area.setForeground(Color.RED);
        area.setText(text);
    }

    private JPanel registrationTab() {
        JPanel p = form();
        JTextField sender = addField(p, 0, "Отправитель");
        JTextField recipient = addField(p, 1, "Получатель");
        JTextField phone = addField(p, 2, "Телефон получателя (+7XXXXXXXXXX)");
        JTextField weight = addField(p, 3, "Вес, кг");
        JComboBox<String> zone = addZone(p, 4);
        JButton button = addButton(p, 5, "Принять отправление");
        JTextArea result = addResult(p, 6);
        button.addActionListener(e -> {
            try {
                Shipment s = registration.register(sender.getText().trim(), recipient.getText().trim(),
                        phone.getText().trim(), Double.parseDouble(weight.getText().trim().replace(',', '.')),
                        Integer.parseInt((String) zone.getSelectedItem()));
                ok(result, "Отправление принято\nТрек-номер: " + s.getTrackNumber() + "\nСтоимость: "
                        + s.getCost() + " руб.\nЯчейка склада: " + s.getCellCode());
            } catch (NumberFormatException ex) {
                fail(result, "Вес нужно ввести числом");
            } catch (RuntimeException ex) {
                fail(result, ex.getMessage());
            }
        });
        return p;
    }

    private JPanel trackingTab() {
        JPanel p = form();
        JTextField track = addField(p, 0, "Трек-номер (14 цифр)");
        JButton find = addButton(p, 1, "Найти");
        JButton next = addButton(p, 2, "Перевести на следующий статус");
        JTextArea result = addResult(p, 3);
        find.addActionListener(e -> {
            try {
                show(result, tracking.find(track.getText().trim()));
            } catch (RuntimeException ex) {
                fail(result, ex.getMessage());
            }
        });
        next.addActionListener(e -> {
            try {
                String number = track.getText().trim();
                ShipmentStatus current = tracking.find(number).getStatus();
                ShipmentStatus[] all = ShipmentStatus.values();
                if (current.ordinal() == all.length - 1) {
                    fail(result, "Отправление уже вручено");
                } else {
                    show(result, registration.moveStatus(number, all[current.ordinal() + 1]));
                }
            } catch (RuntimeException ex) {
                fail(result, ex.getMessage());
            }
        });
        return p;
    }

    private void show(JTextArea area, Shipment s) {
        ok(area, "Получатель: " + s.getRecipientName() + "\nСтатус: " + s.getStatus().getTitle()
                + "\nЯчейка: " + s.getCellCode() + "\nИстория: " + s.getHistory());
    }

    private JPanel tariffTab() {
        JPanel p = form();
        JTextField weight = addField(p, 0, "Вес, кг");
        JComboBox<String> zone = addZone(p, 1);
        JButton button = addButton(p, 2, "Рассчитать");
        JTextArea result = addResult(p, 3);
        button.addActionListener(e -> {
            try {
                double w = Double.parseDouble(weight.getText().trim().replace(',', '.'));
                double cost = tariff.calculate(w, Integer.parseInt((String) zone.getSelectedItem()));
                ok(result, "Стоимость пересылки: " + cost + " руб.");
            } catch (NumberFormatException ex) {
                fail(result, "Вес нужно ввести числом");
            } catch (RuntimeException ex) {
                fail(result, ex.getMessage());
            }
        });
        return p;
    }

    private JPanel storageTab() {
        JPanel p = form();
        JTextField days = addField(p, 0, "Срок хранения, дней");
        JButton button = addButton(p, 1, "Рассчитать");
        JTextArea result = addResult(p, 2);
        button.addActionListener(e -> {
            try {
                double fee = fees.calculate(Integer.parseInt(days.getText().trim()));
                ok(result, "Плата за хранение: " + fee + " руб.");
            } catch (NumberFormatException ex) {
                fail(result, "Срок нужно ввести целым числом");
            } catch (RuntimeException ex) {
                fail(result, ex.getMessage());
            }
        });
        return p;
    }

    private JPanel apiTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JButton button = new JButton("Запустить демонстрацию REST API");
        JTextArea out = new JTextArea();
        out.setEditable(false);
        out.setLineWrap(true);
        out.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        button.addActionListener(e -> {
            try {
                out.setText(runApiDemo());
            } catch (Exception ex) {
                out.setText("Ошибка: " + ex.getMessage());
            }
        });
        p.add(button, BorderLayout.NORTH);
        p.add(new JScrollPane(out), BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(700, 400));
        return p;
    }

    private String runApiDemo() throws Exception {
        TrackingService apiTracking = new TrackingService();
        RegistrationService apiRegistration = new RegistrationService(
                tariff, new WarehouseService(20), apiTracking, new SmsNotifier());
        ApiServer server = new ApiServer(0, apiRegistration, apiTracking);
        server.start();
        try {
            ApiClient client = new ApiClient("http://localhost:" + server.getPort());
            String body = "{\"sender\":\"Крайнов Максим\",\"recipient\":\"Попов Дмитрий\","
                    + "\"phone\":\"+79052223344\",\"weightKg\":2.2,\"zone\":2}";
            StringBuilder sb = new StringBuilder();
            sb.append("POST /api/shipments\n").append(body).append("\n");
            ApiClient.Reply created = client.post("/api/shipments", body);
            sb.append("-> ").append(created.status()).append(" ").append(created.body()).append("\n\n");
            String track = Json.parse(created.body()).get("trackNumber");
            sb.append("GET /api/shipments/").append(track).append("\n");
            ApiClient.Reply found = client.get(track);
            sb.append("-> ").append(found.status()).append(" ").append(found.body()).append("\n\n");
            sb.append("GET /api/shipments/00000000000000\n");
            ApiClient.Reply missing = client.get("00000000000000");
            sb.append("-> ").append(missing.status()).append(" ").append(missing.body()).append("\n");
            return sb.toString();
        } finally {
            server.stop();
        }
    }
}