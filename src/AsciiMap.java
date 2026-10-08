import java.util.Arrays;
import java.util.Scanner;
import java.util.Random;

public class AsciiMap {
    private int width;
    private int height;
    private char[][] grid;
    private char emptyChar;
    private final Random enemyRandom = new Random();

    public int playerx = 5;
    public int playery = 4;
    private char tileUnderPlayer;
    private boolean playerPlaced;

    static final int MAX_PLAYER_HEALTH = 750;
    static int playerhealth = MAX_PLAYER_HEALTH;

    static final int MAX_STIMS = 4;
    static final int STIM_HEAL = 300;
    static int stims = MAX_STIMS;
    static final int MAX_RESUPPLIES = 4;
    static int resupplies = MAX_RESUPPLIES;

    // Heals the player without going over max health. Returns how much was actually healed.
    static int heal(int amount) {
        int healed = Math.min(amount, MAX_PLAYER_HEALTH - playerhealth);
        playerhealth += healed;
        return healed;
    }

    public char getTileUnderPlayer() {
    return tileUnderPlayer;}

    public void clearTileUnderPlayer() {
    tileUnderPlayer = emptyChar;   // emptyChar is '.' for your map
}

    // Initialize the map with a default empty character
    public AsciiMap(int width, int height, char emptyChar) {
        this.width = width;
        this.height = height;
        this.emptyChar = emptyChar;
        this.grid = new char[height][width];
        clearMap();
    }
    // Fill the entire map with the empty character
    public void clearMap() {
        for (int i = 0; i < height; i++) {
            Arrays.fill(grid[i], emptyChar);
        }
    }
    // Edit a specific coordinate on the map
    public boolean setTile(int x, int y, char tile) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid[y][x] = tile;
            return true;
        }
        System.out.println("Error: Coordinates out of bounds!");
        return false;
    }
    // Place a tile on a random empty spot that isn't the player's start, so nothing overwrites anything else
    public void placeOnEmptyTile(char tile) {
        int x, y;
        do {
            x = enemyRandom.nextInt(width);
            y = enemyRandom.nextInt(height);
        } while (grid[y][x] != emptyChar || (x == playerx && y == playery));
        grid[y][x] = tile;
    }
    public boolean placePlayer(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        playerx = x;
        playery = y;
        tileUnderPlayer = grid[y][x];
        grid[y][x] = 'P';
        playerPlaced = true;
        return true;
    }

    public boolean movePlayer(int dx, int dy) {
        int newX = playerx + dx;
        int newY = playery + dy;
        if (!playerPlaced || newX < 0 || newX >= width || newY < 0 || newY >= height) {
            return false;
        }

        grid[playery][playerx] = tileUnderPlayer;
        tileUnderPlayer = grid[newY][newX];
        playerx = newX;
        playery = newY;
        grid[playery][playerx] = 'P';
        return true;
    }

    // Render the map to the console
public void draw() {
        // Clear console screen (optional, works in some terminals)
        System.out.flush();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {System.out.print(grid[y][x] + " ");}
            System.out.println(); // Move to the next row
        }
    }
private static boolean containsSymbol(char[] symbols, char tile) {
        for (char symbol : symbols) {
            if (symbol == tile) {
                return true;
            }
        }
        return false;
    }

    private static Attack makeAttack(String name) {
        switch (name.toUpperCase()) {
            case "OBURST":    return new Attack("DDD", 1000, (int) (Math.random()*50), "orbital airburst strike");
            case "500K":      return new Attack("WDSSS", 1750, (int) (Math.random()*250), "500K bomb");
            case "ESTRIKE":   return new Attack("WDSD", 750, (int) (Math.random()*50), "eagle airstrike");
            case "ORAIL":     return new Attack("DWSSD", 150, 0, "orbital railcannon");
            case "OLASER":    return new Attack("DSWDS", 950, (int) (Math.random()*50), "orbital laser");
            case "PODS":      return new Attack("WDWA", 150, (int) (Math.random()*50), "eagle 110mm rocket pods");
            case "PRIMARY":   return new Attack("W", 50, (int) (Math.random()*10), "primary weapon");
            case "SECONDARY": return new Attack("W", 30, (int) (Math.random()*20), "secondary weapon");
            case "MEELEE":    return new Attack("W", 150, (int) (Math.random()*50), "meelee attack");
            default:          return null;
        }
    }

    private static int unitHealth(String enemyType) {
        switch (enemyType) {
            case "Soldier":    return 100;
            case "Meelee":     return 150;
            case "Marauder":   return 150;
            case "Devastator": return 500;
            case "Hulk":       return 1000;
            default:           return 5000;
        }
    }

    private static boolean enemyTurn(Patrol enemy, String enemyName, int enemyDamage) {
        System.out.println("The " + enemyName + " attacks you!");
        int damageTaken = enemy.scaleDamage(enemyDamage + (int) (Math.random()*50));
        playerhealth -= damageTaken;
        System.out.println("You took " + damageTaken + " damage. Health: " + Math.max(playerhealth, 0) + "/" + MAX_PLAYER_HEALTH);
        if (playerhealth <= 0) {
            System.out.println("You have been defeated by the " + enemyName + ".");
            return false;
        }
        return true;
    }

    private static boolean runCombat(Patrol enemy, String enemyName, int enemyDamage, Stratagem loadout, Scanner in) {
        while (true) {
            System.out.println("Please pick one of the combat options: PRIMARY, SECONDARY, "
                    + loadout.getStratagemOne() + ", " + loadout.getStratagemTwo() + ", "
                    + loadout.getStratagemThree() + ", " + loadout.getStratagemFour() + ", MEELEE, "
                    + "STIM (" + stims + " left), " + "RESUPPLY (" + resupplies + " left). NOTE: Use WASD to input the codes, as you would in the base game.");
            if (!in.hasNextLine()) return false;
            String attack = in.nextLine().trim();

            if (attack.equalsIgnoreCase("STIM")) {
                if (stims <= 0) {
                    System.out.println("You're out of stims!");
                    continue;
                }
                if (playerhealth == MAX_PLAYER_HEALTH) {
                    System.out.println("You're already at full health.");
                    continue;
                }
                stims--;
                int healed = heal(STIM_HEAL);
                System.out.println("You used a stim and recovered " + healed + " health. Health: "
                        + playerhealth + "/" + MAX_PLAYER_HEALTH + ". Stims left: " + stims);
                if (!enemyTurn(enemy, enemyName, enemyDamage)) return false;
                continue;

            } else if (attack.equalsIgnoreCase("RESUPPLY")) {
                if (resupplies <= 0) {
                    System.out.println("You're out of resupplies!");
                    continue;
                }
                resupplies--;
                stims = MAX_STIMS;
                System.out.println("You used a resupply and restored your stims. Stims: " + stims + ". Resupplies left: " + resupplies);
                if (!enemyTurn(enemy, enemyName, enemyDamage)) return false;
                continue;
            }

            boolean isEquippedStratagem = attack.equalsIgnoreCase(loadout.getStratagemOne())
                    || attack.equalsIgnoreCase(loadout.getStratagemTwo())
                    || attack.equalsIgnoreCase(loadout.getStratagemThree())
                    || attack.equalsIgnoreCase(loadout.getStratagemFour());
            boolean isStandardAttack = attack.equalsIgnoreCase("PRIMARY")
                    || attack.equalsIgnoreCase("SECONDARY")
                    || attack.equalsIgnoreCase("MEELEE");

            if (!isEquippedStratagem && !isStandardAttack) {
                System.out.println("Invalid command or stratagem not in your loadout.");
                continue;
            }

            Attack chosen = makeAttack(attack);
            if (isStandardAttack) {
                System.out.println("Please press W to confirm the " + chosen.attackName + ".");
            } else {
                System.out.println("Please enter the stratagem code for the " + chosen.attackName + ".");
            }
            if (!in.hasNextLine()) return false;
            String confirm = in.nextLine().trim();
            if (!confirm.equalsIgnoreCase(chosen.stratagemCode)) {
                System.out.println("Enter a valid stratagem code.");
                continue;
            }

            System.out.println("You used the " + chosen.attackName + ".");
            int membersBefore = enemy.patrolMembers;
            if (attack.equalsIgnoreCase("ORAIL")) {
                if (enemy.railcannonStrike()) {
                    System.out.println("The railcannon destroyed the " + enemy.strongestMember + "!");
                }
            } else {
                enemy.takeDamage(chosen.attackDamage - chosen.Stratagemdeviation);
            }
            int killed = membersBefore - enemy.patrolMembers;
            if (killed > 0) {
                System.out.println("You killed " + killed + (killed == 1 ? " enemy." : " enemies."));
            }

            if (!isStandardAttack && enemy.fabricators > 0) {
                enemy.fabricators--;
                System.out.println("You destroyed a fabricator! " + enemy.fabricators + " remaining.");
            }

            if (enemy.combinedHealth <= 0) {
                System.out.println("You defeated the " + enemyName + "!");
                return true;
            }

            if (enemy.fabricators > 0) {
                enemy.addGrunts(enemy.fabricators);
                System.out.println("The fabricators deploy " + enemy.fabricators + " reinforcements! (+"
                        + enemy.fabricators * Patrol.GRUNT_HEALTH + " health)");
            }

            System.out.println("The " + enemyName + " has " + enemy.combinedHealth + " health remaining. It has "
                    + enemy.patrolMembers + " members. "
                    + (enemy.strongestAlive ? "Its strongest member is a " + enemy.strongestMember + "."
                                            : "Its " + enemy.strongestMember + " has been destroyed."));
            if (enemy.fabricators > 0) {
                System.out.println("Fabricators remaining: " + enemy.fabricators);
            }
            if (!enemyTurn(enemy, enemyName, enemyDamage)) return false;
        }
    }

public static void main(String[] args) {

        AsciiMap map = new AsciiMap(10, 7, '.');
        char[] encampmentlist = { '0', '1', '2', '3', '4', '5', '6', '7'};
        char[] symbolList = {'A', 'L', 'L', 'M', 'B', '.', '.', '.'}; //A for SEAF Artillery, L for LIDAR (listed twice so it generates more often), M for SAMSITE, B for Terminate Illegal Broadcast, and * for sample POIs
        char[] objectiveList = {'D', 'S', 'N', 'C', 'R'}; //Destroy Transmission Network, Secure Blackbox, Secure Evidence, Neutralise Orbital Defenses, Destroy Command Bunker, Sabotage Supply Bases


        int poi1 = (int) (Math.random() * symbolList.length);
        int poi2 = (int) (Math.random() * symbolList.length);
        int poi3 = (int) (Math.random() * symbolList.length);
        int poi4 = (int) (Math.random() * symbolList.length);

        int camp1 = (int) (Math.random() * encampmentlist.length);
        int camp2 = (int) (Math.random() * encampmentlist.length);
        int camp3 = (int) (Math.random() * encampmentlist.length);
        int camp4 = (int) (Math.random() * encampmentlist.length);

        int objective = (int) (Math.random() * objectiveList.length);
        // Edit the map by adding structures/characters

        map.placeOnEmptyTile(symbolList[poi1]);
        map.placeOnEmptyTile(symbolList[poi2]);
        map.placeOnEmptyTile(symbolList[poi3]);
        map.placeOnEmptyTile(symbolList[poi4]);
        char currentObjective = objectiveList[objective];
        map.placeOnEmptyTile(currentObjective);
        map.placeOnEmptyTile(encampmentlist[camp1]);
        map.placeOnEmptyTile(encampmentlist[camp2]);
        map.placeOnEmptyTile(encampmentlist[camp3]);
        map.placeOnEmptyTile(encampmentlist[camp4]);
        map.placePlayer(map.playerx, map.playery);

        // Collect four valid stratagem entries before displaying the map.
        try (Scanner playermove = new Scanner(System.in)) {
        String[] validStratagems = {"500K", "ESTRIKE", "ORAIL", "OLASER", "OBURST", "PODS"};
        System.out.println("Choose four stratagems from: " + String.join(", ", validStratagems));
        String[] entries = new String[4];
        for (int i = 0; i < entries.length; i++) {
            while (true) {
                // Prompt BEFORE reading, so the player always knows input is expected.
                System.out.print("Stratagem " + (i + 1) + " of " + entries.length + ": ");
                System.out.flush();
                if (!playermove.hasNextLine()) {
                    System.out.println("\nNo more input. Exiting.");
                    return;
                }
                String entry = playermove.nextLine().trim();
                String match = null;
                for (String allowed : validStratagems) {
                    if (allowed.equalsIgnoreCase(entry)) {
                        match = allowed;
                        break;
                    }
                }
                if (match != null) {
                    entries[i] = match;
                    System.out.println("  Equipped " + match + ".");
                    break;
                }
                System.out.println("  Invalid stratagem. Choose one of: "
                        + String.join(", ", validStratagems));
            }
        }
        Stratagem loadout = new Stratagem(entries[0], entries[1], entries[2], entries[3]);
        System.out.println("Loadout: " + String.join(", ", entries));

        map.draw();
        boolean gameOver = false;

        while (!gameOver) {
            System.out.print("Move (N/E/S/W): ");
            System.out.flush();
            if (!playermove.hasNextLine()) break;
            String direction = playermove.nextLine().trim();

            boolean moved = true;
            if (direction.equalsIgnoreCase("n")) moved = map.movePlayer(0, -1);
            else if (direction.equalsIgnoreCase("e")) moved = map.movePlayer(1, 0);
            else if (direction.equalsIgnoreCase("s")) moved = map.movePlayer(0, 1);
            else if (direction.equalsIgnoreCase("w")) moved = map.movePlayer(-1, 0);
            else moved = false;

            if (!moved) {
                System.out.println("Invalid direction or move is out of bounds.");
                continue;
            }

            map.draw();
            char tile = map.getTileUnderPlayer();

            if (tile == currentObjective) {
                System.out.println("You reached the objective: " + currentObjective);
                gameOver = true;
            } else if (tile != '.' && containsSymbol(symbolList, tile)) {
                System.out.println("You reached a point of interest: " + tile);
                if(tile == 'A'){
                    //minigame
                }
                if(tile == 'L'){
                    System.out.println("Please press 'W' to activate the LIDAR.");
                    // On any failure the LIDAR tile stays on the map so the player can come back and retry
                    if (!playermove.hasNextLine()) break;
                    String answr = playermove.nextLine().trim();
                    if (answr.equalsIgnoreCase("W")) {
                        System.out.println("LIDAR activating...");
                        for (int i = 0; i <= 20; i++) {
                            String bar = "#".repeat(i) + "-".repeat(20 - i);
                            System.out.print("\rACTIVATING... [" + bar + "] " + (i * 5) + "%");
                            System.out.flush();
                            try { Thread.sleep(150); } catch (InterruptedException e) { }
                        }
                        System.out.println();
                        String code = String.format("%05d", (int)(Math.random()*100000));
                        System.out.println("This is a single-use LIDAR code. Remember it for future reference: " + code + ".");
                        System.out.println("Please press 'W' to confirm the LIDAR activation.");
                        if (!playermove.hasNextLine()) break;
                        if(playermove.nextLine().trim().equalsIgnoreCase("W")) {
                            // Move the cursor up 3 lines (code, prompt, typed "W") and erase everything below it
                            System.out.print("\033[3A\033[0J");
                            System.out.flush();
                            System.out.println("Input your 5-digit LIDAR code to confirm: ");
                                if(playermove.hasNextLine()) {
                                    String inputCode = playermove.nextLine().trim();
                                    if(inputCode.equals(code)) {
                                        System.out.println("LIDAR confirmed. Subobjective Completed.");
                                        map.clearTileUnderPlayer();
                                    } else {
                                        System.out.println("Incorrect code. LIDAR not activated.");
                                    }
                                } else {
                                    System.out.println("No input received. LIDAR not activated.");
                                    break;
                                }
                        } else {
                            System.out.println("Invalid input. LIDAR not activated.");
                        }
                    } else {
                        System.out.println("Invalid input. LIDAR not activated.");
                    }
                }
                if(tile == 'M'){
                    //minigame
                }
                if(tile == 'B'){
                    //minigame
                }

            }

else if (tile != '.' && containsSymbol(encampmentlist, tile)) {
            int difficulty = tile - '0';
            System.out.println("You have reached an encampment, difficulty " + difficulty);
                    String[] enemyList = {"Marauder", "Devastator", "Hulk", "WarStrider"};
                    int randomEnemyType = (int) (Math.random() * enemyList.length);
                    int patrolSize = (3*difficulty) + (int) (Math.random() * 20);
                    int patrolHealth;
                    if (enemyList[randomEnemyType].equals("Marauder")) {patrolHealth = 150 + (patrolSize - 1) * 100;}
                        else if (enemyList[randomEnemyType].equals("Devastator")) {patrolHealth = 500 + (patrolSize - 1) * 100;}
                        else if (enemyList[randomEnemyType].equals("Hulk")) {patrolHealth = 1000 + (patrolSize - 1) * 100;}
                        else {patrolHealth = 5000 + (patrolSize - 1) * 100;}
                    int fabricators = 1 + difficulty / 2;
                    int enemyDamage = 50 + 25 * difficulty;
                    Patrol encampmentGuards = new Patrol(patrolHealth, patrolSize, enemyList[randomEnemyType],
                            unitHealth(enemyList[randomEnemyType]), fabricators);
                    System.out.println("Health: " + encampmentGuards.combinedHealth);
                    System.out.println("Size: " + encampmentGuards.patrolMembers);
                    System.out.println("Strongest member: " + encampmentGuards.strongestMember);
                    System.out.println("Strongest member health: " + encampmentGuards.strongestMemberHealth);
                    System.out.println("Fabricators: " + encampmentGuards.fabricators + " (only stratagems can destroy them)");
                if (runCombat(encampmentGuards, "Automaton Encampment", enemyDamage, loadout, playermove)) {
                    map.clearTileUnderPlayer();
                    int healed = Math.min(50 + 50 * difficulty, MAX_PLAYER_HEALTH - playerhealth);
                    playerhealth += healed;
                    System.out.println("Encampment cleared! You recovered " + healed + " health. Health: "
                            + playerhealth + "/" + MAX_PLAYER_HEALTH);
                } else {
                    gameOver = true;
                }
            }

else if (tile == '.') {
int enemyRoll = map.enemyRandom.nextInt(10);
if (enemyRoll >= 7) {
                    System.out.println("You are now in combat with an Automaton Patrol!");
                    String[] enemyList = {"Soldier", "Meelee", "Marauder", "Devastator", "Hulk"};
                    int randomEnemyType = (int) (Math.random() * enemyList.length);
                    int patrolSize = 10 + (int) (Math.random() * 20);
                    String enemyType = enemyList[randomEnemyType];
                    int strongestHealth = unitHealth(enemyType);
                    int patrolHealth = strongestHealth + (patrolSize - 1) * 100;
                        Patrol bigPatrol = new Patrol(patrolHealth, patrolSize, enemyType, strongestHealth);
                        System.out.println("Health: " + bigPatrol.combinedHealth);
                        System.out.println("Size: " + bigPatrol.patrolMembers);
                        System.out.println("Strongest member: " + bigPatrol.strongestMember);
                if (runCombat(bigPatrol, "Automaton Patrol", 100, loadout, playermove)) {
                    map.clearTileUnderPlayer();
                    int patrolHealed = Math.min(50, MAX_PLAYER_HEALTH - playerhealth);
                    playerhealth += patrolHealed;
                    System.out.println("Patrol defeated! You recovered " + patrolHealed + " health. Health: "
                            + playerhealth + "/" + MAX_PLAYER_HEALTH);
                } else {
                    gameOver = true;
                }
            }
        }
        }
    }}}
