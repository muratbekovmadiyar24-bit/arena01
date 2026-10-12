import java.util.Locale;
import java.util.Scanner;

public class Main {

    static final int MAX_HP = 40;
    static final int HEAL_AMOUNT = 15;
    static final int XP_REWARD = 20;
    static final int HERO_POWER = 10;

    // Проверка допуска героя на арену
    static boolean checkAdmission(int level, String heroClass, boolean shield) {
        return level >= 10 && (shield || heroClass.equals("маг"));
    }

    // Расчёт HP после получения урона
    static int calculateDamage(int currentHp, int damage) {
        return Math.max(0, currentHp - damage);
    }

    // Расчёт HP после лечения
    static int calculateHeal(int currentHp) {
        return Math.min(MAX_HP, currentHp + HEAL_AMOUNT);
    }

    // Вывод итогов игры
    static void printResult(String hero, int wins, int xp, int hp, String result) {
        System.out.println("\n========== ИТОГ ==========");
        System.out.println("Герой: " + hero);
        System.out.println("Побед: " + wins);
        System.out.println("XP: " + xp);
        System.out.println("HP: " + hp);
        System.out.println("Итог: " + result);
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Массивы варианта А — Руины
        String[] enemies = {"Крыса", "Скелет", "Страж"};
        int[] enemyMaxHp = {12, 18, 24};
        int[] enemyDamage = {6, 10, 12};

        // =========================
        // РЕГИСТРАЦИЯ
        // =========================

        String heroName;

        while (true) {
            System.out.print("Введите имя героя: ");

            if (!scanner.hasNextLine()) {
                return;
            }

            heroName = scanner.nextLine().strip();

            if (heroName.length() >= 1 && heroName.length() <= 30) {
                break;
            }

            System.out.println("Ошибка: имя должно содержать от 1 до 30 символов.");
        }

        String heroClass;

        while (true) {
            System.out.print("Введите класс (воин/маг/лучник): ");

            if (!scanner.hasNextLine()) {
                return;
            }

            heroClass = scanner.nextLine()
                    .strip()
                    .toLowerCase(Locale.ROOT);

            if (heroClass.equals("воин")
                    || heroClass.equals("маг")
                    || heroClass.equals("лучник")) {
                break;
            }

            System.out.println("Неизвестный класс. Повторите ввод.");
        }

        int level;

        while (true) {
            System.out.print("Введите уровень (1-80): ");

            if (!scanner.hasNextInt()) {
                System.out.println("Ошибка: введите целое число.");
                scanner.nextLine();
                continue;
            }

            level = scanner.nextInt();
            scanner.nextLine();

            if (level >= 1 && level <= 80) {
                break;
            }

            System.out.println("Уровень должен быть от 1 до 80.");
        }

        boolean shield;

        while (true) {
            System.out.print("Есть ли щит? (да/нет): ");

            if (!scanner.hasNextLine()) {
                return;
            }

            String answer = scanner.nextLine()
                    .strip()
                    .toLowerCase(Locale.ROOT);

            if (answer.equals("да")) {
                shield = true;
                break;
            }

            if (answer.equals("нет")) {
                shield = false;
                break;
            }

            System.out.println("Введите только «да» или «нет».");
        }

        // =========================
        // ПРОВЕРКА ДОПУСКА
        // =========================

        if (level < 10) {
            System.out.println("Отказ: недостаточный уровень.");
            return;
        }

        if (!shield && !heroClass.equals("маг")) {
            System.out.println("Отказ: отсутствует нужная защита.");
            return;
        }

        System.out.println("\nДопущен: " + heroName + ", " + heroClass);

        // =========================
        // НАЧАЛО АРЕНЫ
        // =========================

        int heroHp = MAX_HP;
        int xp = 0;
        int wins = 0;

        boolean gameInterrupted = false;

        // Три боя
        for (int i = 0; i < enemies.length; i++) {

            int enemyHp = enemyMaxHp[i];
            boolean healUsed = false;

            System.out.println("\n============================");
            System.out.println("Раунд " + (i + 1));
            System.out.println("Противник: " + enemies[i]);
            System.out.println("============================");

            while (heroHp > 0 && enemyHp > 0) {

                System.out.println("\nHP героя: " + heroHp + "/" + MAX_HP);
                System.out.println("HP " + enemies[i] + ": " + enemyHp);
                System.out.println("1 — атаковать");
                System.out.println("2 — лечиться");
                System.out.println("0 — закончить игру");

                System.out.print("Ваш ход: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Неизвестная команда.");
                    scanner.nextLine();
                    continue;
                }

                int command = scanner.nextInt();
                scanner.nextLine();

                // Выход из игры
                if (command == 0) {
                    gameInterrupted = true;
                    break;
                }

                // Атака
                if (command == 1) {

                    enemyHp = calculateDamage(enemyHp, HERO_POWER);

                    System.out.println(
                            "Герой атакует! Урон: " + HERO_POWER
                    );

                    // Если противник погиб — он НЕ отвечает
                    if (enemyHp <= 0) {
                        xp += XP_REWARD;
                        wins++;

                        System.out.println(
                                enemies[i] + " побеждён!"
                        );

                        System.out.println(
                                "HP героя: " + heroHp +
                                        ", XP: " + xp
                        );

                        break;
                    }

                    // Ответ противника
                    heroHp = calculateDamage(
                            heroHp,
                            enemyDamage[i]
                    );

                    System.out.println(
                            enemies[i] + " наносит " +
                                    enemyDamage[i] + " урона!"
                    );

                    continue;
                }

                // Лечение
                if (command == 2) {

                    if (healUsed) {
                        System.out.println(
                                "Лечение уже использовано в этом бою."
                        );
                        continue;
                    }

                    heroHp = calculateHeal(heroHp);
                    healUsed = true;

                    System.out.println(
                            "Герой восстановил HP. Текущее HP: "
                                    + heroHp
                    );

                    // Живой противник отвечает
                    heroHp = calculateDamage(
                            heroHp,
                            enemyDamage[i]
                    );

                    System.out.println(
                            enemies[i] + " наносит " +
                                    enemyDamage[i] + " урона!"
                    );

                    continue;
                }

                // Неизвестная команда
                System.out.println("Неизвестная команда.");
            }

            // Игра была прервана командой 0
            if (gameInterrupted) {
                printResult(
                        heroName,
                        wins,
                        xp,
                        heroHp,
                        "Игра прервана"
                );
                scanner.close();
                return;
            }

            // Герой погиб
            if (heroHp <= 0) {
                printResult(
                        heroName,
                        wins,
                        xp,
                        heroHp,
                        "Поражение"
                );
                scanner.close();
                return;
            }

            // Победа над противником
            System.out.println(
                    "Раунд " + (i + 1) +
                            ": победа, HP героя " +
                            heroHp + ", XP " + xp
            );
        }

        // =========================
        // ФИНАЛ
        // =========================

        if (wins == 3) {
            printResult(
                    heroName,
                    wins,
                    xp,
                    heroHp,
                    "Арена пройдена"
            );
        }

        scanner.close();
    }
}
