package io.carolynn.casino.games.diceGames;

import io.carolynn.casino.Person;
import io.carolynn.casino.dice.DiceManager;
import io.carolynn.casino.games.Game;

import java.util.*;

public class Craps extends Game {

    private DiceManager diceManager;
    private int point;
    private int diceValue;
    private int bet;
    private Scanner scanner;
    private ArrayList<Integer> fieldValues;

    private LinkedHashMap<String, Integer> lineComeFieldBets;
    private ArrayList<String> lineComeFieldNames;
    private LinkedHashMap<String,Integer> oddsBets;
    private LinkedHashMap<String, HashMap<Integer, Integer>> placeBets;
    private LinkedHashMap<String, HashMap<Integer, Integer>> comePointsBets;
    private ArrayList<HashMap<Integer,Double>> betPayouts;
    private HashMap<Integer, Double> placeWinPayout;
    private HashMap<Integer, Double> placeLosePayout;
    private HashMap<Integer, Double> passLineComeBetOddsPayout;
    private HashMap<Integer, Double> dontPassLineDontComeOddsPayout;
    private HashMap<Integer, Double> fieldBetPayout;

    public Craps(Person player){
        super(player);
        this.diceManager = new DiceManager(2);
        this.point = 0;
        this.bet = 0;
        this.diceValue = 0;
        this.scanner = new Scanner(System.in);
        this.fieldValues = new ArrayList<>(Arrays.asList(2, 3, 4, 9, 10, 11, 12));
        this.lineComeFieldBets = new LinkedHashMap<>();
        this.lineComeFieldNames = new ArrayList<>(Arrays.asList("pass","don't pass", "come","don't come", "field"));
        lineComeFieldNames.stream().forEach(e->lineComeFieldBets.put(e,0));
        this.oddsBets = new LinkedHashMap<>();
        oddsBets.put("come",0);
        oddsBets.put("don't come",0);
        oddsBets.put("pass",0);
        oddsBets.put("don't pass",0);
        this.placeBets = new LinkedHashMap<>();
        placeBets.put("place lose",new HashMap<>());
        placeBets.put("place win", new HashMap<>());
        this.comePointsBets = new LinkedHashMap<>();
        comePointsBets.put("come", new HashMap<>());
        comePointsBets.put("don't come", new HashMap<>());

        CrapsBetPayouts payouts = new CrapsBetPayouts();
        this.placeLosePayout = payouts.getPlaceLosePayout();
        this.placeWinPayout = payouts.getPlaceWinPayout();
        this.passLineComeBetOddsPayout = payouts.getPassLineComeBetOddsPayout();
        this.dontPassLineDontComeOddsPayout = payouts.getDontPassLineDontComeOddsPayout();
        this.fieldBetPayout = payouts.getFieldBetPayout();

        this.betPayouts = new ArrayList<>(Arrays.asList(placeLosePayout,placeWinPayout,
                passLineComeBetOddsPayout,dontPassLineDontComeOddsPayout,fieldBetPayout));
    }

    @Override
    public void start() {
        System.out.println("Welcome to Craps!\n  Minimum bet amount is 5 chips.  If at anytime your available chip amount goes" +
                "below 5, you will be booted from the game and returned to the Main Menu to replenish your chips");
        if(!insufficientFunds()){
            runGame();
        }
        end();
    }

    @Override
    public void runGame() {
        do{
            placeInitialBet();
            comeOutRoll();
        } while (keepPlaying() && !insufficientFunds());
    }


    public int rollDice(){
        diceManager.rollDice(2);
        diceValue = diceManager.totalValue();
        return diceValue;
    }

    public void comeOutRoll(){
        rollDice();
        StringBuilder builder = new StringBuilder("Make your first roll!\n")
                .append(diceManager.toStringPictures())
                .append(" Total value: ")
                .append(diceValue);
        if(diceValue==2 || diceValue == 3){
            builder.append(". You crapped out. Pass Line bets lose and Don't Pass Line bets win.\n")
                    .append(passLineBetResult("pass",false))
                    .append(passLineBetResult("don't pass", true));
        } else if(diceValue == 7 || diceValue == 11){
            builder.append(". You rolled a natural! Pass Line bets win and Don't Pass loses.\n")
                    .append(passLineBetResult("pass",true))
                    .append(passLineBetResult("don't pass", false));
        } else if(diceValue == 12) {
            builder.append(". Pass Line looses and Don't Pass bets are pushed to next round.\n")
                    .append(passLineBetResult("pass",false));
            System.out.println(builder.toString());
        } else {
            builder.append(". The point is now ")
                    .append(diceValue);
            System.out.println(builder.toString());
            point = diceValue;

        }
    }

    public void placeInitialBet(){
        String answer = "";
        do{
        System.out.println("You must place an initial Pass Line Bet. To make a bet on the Pass Line, type 'pass'. " +
                "To make a bet on the Don't Pass Line, type 'don't pass'.");
        answer = scanner.nextLine();
        if(answer.equalsIgnoreCase("pass")){
            makeLineComeFieldBet("pass");
            break;
        } else if (answer.equalsIgnoreCase("don't pass")){
            makeLineComeFieldBet("don't pass");
            break;
        } else {
            System.out.println("Invalid Input");
        }
        } while (true);
    }

    public String getPlayerSecondBetType(){
        ArrayList<String> betTypes = new ArrayList<>(Arrays.asList("come","don't come","pass","don't pass","field","odds",
                "place win","place lose"));
        String answer = "";
        do {
            System.out.println("What type of bet would you like to place for phase two? Your options are: " +
                    "\ncome \ndon't come \npass \ndon't pass \nfield \nodds \nplace win \nplace lose");
            answer = scanner.nextLine().toLowerCase();
            if(!betTypes.contains(answer)){
                System.out.println("Invalid input. ");
            }
        } while (!betTypes.contains(answer));
        return answer;
    }

    public void placePhaseTwoBet(String betType){
        int bet = betAmount();
        if(betType.equalsIgnoreCase("pass")|| betType.equalsIgnoreCase("don't pass") || betType.equalsIgnoreCase("come")||
                betType.equalsIgnoreCase("don't come") || betType.equalsIgnoreCase("field")){
            lineComeFieldBets.put(betType,bet);
        } else if (betType.equalsIgnoreCase("place win")|| betType.equalsIgnoreCase("place lose")){

        } else if (betType.equalsIgnoreCase("odds")){
            String oddsType = getOddsType();
            if(oddsType.equalsIgnoreCase("place win") || oddsType.equalsIgnoreCase("place lose")){
                makeOddsPlaceBet(oddsType);
            } else if(oddsType.equalsIgnoreCase("come") || oddsType.equalsIgnoreCase("don't come")){

            }
        }
    }

    public int betAmount(){
        int bet;
        do {
            System.out.println("How many chips would you like to bet?");
            bet = scanner.nextInt();
            if (bet > checkWallet()) {
                System.out.println("Insufficient funds.");
            } else if (bet < 5) {
                System.out.println("Your bet amount is too low. Minimum bet is 5.");
            }
        } while(bet> checkWallet() || bet<5);
        return bet;
    }

    private String getOddsType(){
        Scanner scan = new Scanner(System.in);
        ArrayList<String> oddsNames = new ArrayList<>(Arrays.asList("pass","don't pass", "come", "don't come"));
        String type = "";
        do{
            System.out.println("What type of odds would you like to place? ?\nPass\nDon't Pass\nCome\nDon't Come");
            type = scan.nextLine().toLowerCase();
        } while(!oddsNames.contains(type));
        return type;
    }

    public void makeOddsPlaceBet(String oddsType){
        int bet = betAmount();
        int point = betOnPoint();
        for(Map.Entry<String,HashMap<Integer,Integer>> entry: placeBets.entrySet()){
            if(oddsType.equals(entry.getKey())){
                entry.getValue().put(point,bet);
            }
        }

    }

    private void makePlaceBet(String betType){
        ArrayList<Integer> winLosePlaceValues = new ArrayList<>(Arrays.asList(4, 5, 6, 8, 9, 10));
        System.out.println("What number would you like to put a " + betType + " bet on? 4, 5, 6, 8, 9, or 10?");
        int num = scanner.nextInt();
        scanner.nextLine();
        int bet = betAmount();
        HashMap<Integer,Integer> map = new HashMap();
        map.put(num, bet);
        placeBets.put(betType,map);
    }

    public int betOnPoint(){
        Scanner scan = new Scanner(System.in);
        System.out.println("What number would you like to place your bet on?");
        return scan.nextInt();
    }

    public void makeLineComeFieldBet(String betType){
        int bet = betAmount();
        lineComeFieldBets.put(betType,bet);
    }

    public String passLineBetResult(String betType, boolean winLose){
        String x = "";
        if(winLose && lineComeFieldBets.get(betType)!=0){
            x =  "You won " + lineComeFieldBets.get(betType) + " chips on the " + betType + " line.\n";
            player.addChips(lineComeFieldBets.get(betType)*2);
        } else if (!winLose && lineComeFieldBets.get(betType)!=0){
            x = "You lost " + lineComeFieldBets.get(betType) + " chips on the " + betType + " line.\n";
        }
        return x;
    }

    public void phaseTwoRoll(){
        String betType = getPlayerSecondBetType();
        rollDice();

    }

    public int checkWallet(){
        return player.getChips();
    }

    public boolean insufficientFunds(){
        if(checkWallet()<=5){
            System.out.println("You have insufficient funds.  Returning to main menu.");
            return false;
        } else {
            return true;
        }
    }

    public boolean keepPlaying(){
        System.out.println("Would you like to play again? yes/no");
        Scanner scanner = new Scanner(System.in);
        String answer = "";
        boolean play = true;
        do{
            answer = scanner.nextLine();
            if(answer.equalsIgnoreCase("yes")){
                play =  true;
            } else if (answer.equalsIgnoreCase("no")){
                play=false;
            } else {
                System.out.println("Invalid answer.  Please type \'yes\' to play again or \'no\' to return to the Main Menu.");
            }
        } while (!answer.equalsIgnoreCase("yes") || !answer.equalsIgnoreCase("no"));
        return play;
    }

    @Override
    public void end() {
        System.out.println("Thank you for playing Craps!");
    }

    public DiceManager getDiceManager() {
        return diceManager;
    }
    public void setDiceManager(DiceManager diceManager) {
        this.diceManager = diceManager;
    }

    public Person getPlayer() {
        return player;
    }
    public void setPlayer(Person player) {
        this.player = player;
    }


    public void setPoint(int point) { this.point = point; }
    public int getPoint() { return point; }

    public int getDiceValue() { return diceValue; }
    public void setDiceValue(int diceValue) { this.diceValue = diceValue; }

    public LinkedHashMap<String, Integer> getLineComeFieldBets() { return lineComeFieldBets; }

    public LinkedHashMap<String, HashMap<Integer, Integer>> getPlaceBets() { return placeBets; }


    public ArrayList<HashMap<Integer,Double>> getBetPayouts() { return betPayouts; }

    public LinkedHashMap<String, HashMap<Integer, Integer>> getComePointsBets() { return comePointsBets; }
}
