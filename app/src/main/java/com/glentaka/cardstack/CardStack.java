package com.glentaka.cardstack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class CardStack extends Activity {

    private List<Card> stack;
    private List<Card> cardHistory;
    private Map<Integer, Integer> counts;
    private int numDice;
    private int numSides;
    private int numExtra;
    private boolean keepScreenOn;
    private boolean uniformDistribution;
    // -1 = all
    private int cardsToShow;
    private boolean showCardsRemaining;
    private boolean showShuffles;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_stack);
        prefs = getSharedPreferences(getString(R.string.prefsFile), Context.MODE_PRIVATE);
        numDice = prefs.getInt("numDice", getResources().getInteger(R.integer.numDiceDefault));
        numSides = prefs.getInt("numSides", getResources().getInteger(R.integer.numSidesDefault));
        numExtra = prefs.getInt("numExtra", getResources().getInteger(R.integer.numExtraDefault));
        cardsToShow = prefs.getInt("cardsToShow", getResources().getInteger(R.integer.numToShowDefault));
        keepScreenOn = prefs.getBoolean("keepScreenOn", getResources().getBoolean(R.bool.keepScreenOnDefault));
        showCardsRemaining = prefs.getBoolean("showCardsRemaining", getResources().getBoolean(R.bool.showRemainingDefault));
        showShuffles = prefs.getBoolean("showShuffles", getResources().getBoolean(R.bool.showShufflesDefault));
        uniformDistribution = prefs.getBoolean("uniformDistribution", getResources().getBoolean(R.bool.uniformDistributionDefault));
        updateScreen(keepScreenOn);
        if(savedInstanceState != null) {
            stack = stringToStack(savedInstanceState.getString(stackKey));
            cardHistory = stringToStack(savedInstanceState.getString(historyKey));
        } else {
            reset();
        }
        if(prefs.contains(stackKey)) {
            restoreState();
        }
        updateCounts();
        updateViews();

        Button b = (Button) findViewById(R.id.flipButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stack.size() == 0) {
                    reroll();
                    cardHistory.add(0, new Card());
                }
                Card c = stack.remove(0);
                counts.put(c.sum(), counts.get(c.sum())-1);
                cardHistory.add(0, c);
                updateViews();
            }
        });
    }

    private String stackToString(List<Card> cards) {
        StringBuilder sb = new StringBuilder();
        for(Card c : cards) {
            String separator = "";
            for(int i : c.getRolls()) {
                sb.append(separator + i);
                separator = ",";
            }
            sb.append(';');
        }
        return sb.toString();
    }

    private List<Card> stringToStack(String ss) {
        List<Card> cards = Lists.newArrayList();
        for(String s : ss.split(";")) {
            Card c = new Card();
            for(String r : s.split(",")) {
                if(r.length() > 0) {
                    c.addRoll(Integer.parseInt(r));
                }
            }
            cards.add(c);
        }
        return cards;
    }

    private static String stackKey = "cardStack";
    private static String historyKey = "cardHistory";

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putString(stackKey, stackToString(stack));
        savedInstanceState.putString(historyKey, stackToString(cardHistory));

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        stack = stringToStack(savedInstanceState.getString(stackKey));
        cardHistory = stringToStack(savedInstanceState.getString(historyKey));
        updateCounts();
        updateViews();
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("numDice", numDice);
        editor.putInt("numSides", numSides);
        editor.putInt("numExtra", numExtra);
        editor.putInt("cardsToShow", cardsToShow);
        editor.putBoolean("keepScreenOn", keepScreenOn);
        editor.putBoolean("showCardsRemaining", showCardsRemaining);
        editor.putBoolean("showShuffles", showShuffles);
        editor.putBoolean("uniformDistribution", uniformDistribution);
        editor.apply();
    }

    private void updateViews() {
        TextView rollView = (TextView) findViewById(R.id.flippedCardsTextView);
        TextView lastDrawnTextView = (TextView) findViewById(R.id.lastDrawnTextView);
        TextView remainingRollsView = (TextView) findViewById(R.id.remainingRollsTextView);
        if(cardHistory.size() > 0) {
            lastDrawnTextView.setText(cardHistory.get(0).toString());
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for(int i = 1; i < cardHistory.size() && (count < cardsToShow || cardsToShow == -1); i++) {
                if(cardHistory.get(i).sum() > 0) {
                    count++;
                    sb.append(cardHistory.get(i) + "\n");
                } else if (showShuffles) {
                    sb.append(cardHistory.get(i) + "\n");
                }
            }
            rollView.setText(sb.toString());
        } else {
            lastDrawnTextView.setText("");
            rollView.setText("");
        }
        if(showCardsRemaining) {
            StringBuilder sb = new StringBuilder();
            sb.append("Remaining rolls:");
            for(int i = numDice; i <= numDice*numSides; i++) {
                sb.append("\n" + i + " - " + counts.get(i));
            }
            remainingRollsView.setText(sb.toString());
        } else {
            remainingRollsView.setText("");
        }
    }

    private void reroll() {
        stack = buildDeck(numDice, numSides, numExtra);
        fisherYates(stack, 5);
        updateCounts();
    }

    private void reset() {
        reroll();
        cardHistory = Lists.newArrayList();
        updateViews();
    }

    private void fisherYates(List deck, int n) {
        Random rand = new Random();
        for(;n>0;n--) {
            for (int i = deck.size() - 1; i >= 0; i--) {
                int shuffle = rand.nextInt(i + 1);
                Object temp = deck.get(i);
                deck.set(i, deck.get(shuffle));
                deck.set(shuffle, temp);
            }
        }
    }

    class Card {
        List<Integer> rolls;

        public Card() {
            rolls = Lists.newArrayList();
        }

        public Card(List<Integer> rolls) {
            this();
            this.rolls.addAll(rolls);
        }

        public Card(Card card) {
            this(card.getRolls());
        }

        public Card(Card card, int roll) {
            this(card);
            rolls.add(roll);
        }

        public List<Integer> getRolls() {
            return rolls;
        }

        public int sum() {
            int sum = 0;
            for(int i : rolls) {
                sum += i;
            }
            return sum;
        }

        public void addRoll(int i) {
            rolls.add(i);
        }

        public String toString() {
            if(rolls.size() == 0) {
                return "Reshuffle";
            }
            StringBuilder s = new StringBuilder();
            s.append("Dice: ");
            String separator = "";
            for(int i : rolls) {
                s.append(separator + i);
                separator = ", ";
            }
            s.append(" Roll: " + sum());
            return s.toString();
        }
    }

    private void updateCounts() {
        counts = Maps.newHashMap();
        for(int i = numDice; i <= numDice * numSides; i++ ) {
            counts.put(i,0);
        }
        for(Card c : stack) {
            int sum = c.sum();
            //loading an invalid state
            if(!counts.containsKey(sum)) {
                reset();
                return;
            }
            counts.put(sum, counts.get(sum) + 1);
        }
    }

    private List<Card> buildDeck(int numDice, int sidesOnDice, int extraCards) {
        if(numDice < 1) {
            throw new IllegalArgumentException("Cannot have less than 1 die.");
        }
        List<Card> cards = buildDeckHelper(new Card(), numDice, sidesOnDice);
        Map<Integer, List<Card>> cardMap = Maps.newHashMap();
        if(uniformDistribution) {
            for (int i = numDice; i <= numDice * numSides; i++) {
                cardMap.put(i, Lists.<Card>newArrayList());
            }
            for (Card c : cards) {
                cardMap.get(c.sum()).add(c);
            }
        }
        Random random = new Random();
        if(extraCards > 0) {
            //duplicate a random card
            if(uniformDistribution) {
                for (int i = 0; i < extraCards; i++) {
                    List<Card> cl = cardMap.get(random.nextInt(numDice*numSides+1-numDice)+numDice);
                    Card c = cl.get(random.nextInt(cl.size()));
                    cards.add(new Card(c));
                }
            } else {
                for (int i = 0; i < extraCards; i++) {
                    cards.add(new Card(cards.get(random.nextInt(cards.size()))));
                }
            }
        } else if (extraCards < 0) {
            //delete a random card
            if(uniformDistribution) {
                for (int i = 0; i < extraCards; i++) {
                    List<Card> cl = cardMap.get(random.nextInt(numDice*numSides+1-numDice)+numDice);
                    Card c = cl.get(random.nextInt(cl.size()));
                    cards.remove(c);
                }
            } else {
                for (int i = 0; i < extraCards; i++) {
                    cards.remove(random.nextInt(cards.size()));
                }
            }
        }
        return cards;
    }

    private List<Card> buildDeckHelper(Card baseCard, int numDice, int sidesOnDice) {
        List<Card> cards = Lists.newArrayList();
        if(numDice == 1) {
            for(int i = 1; i <= sidesOnDice; i++) {
                cards.add(new Card(baseCard, i));
            }
        } else {
            for(int i = 1; i <= sidesOnDice; i++) {
                cards.addAll(buildDeckHelper(new Card(baseCard, i), numDice-1, sidesOnDice));
            }
        }
        return cards;
    }

    private void saveState() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(stackKey, stackToString(stack));
        editor.putString(historyKey, stackToString(cardHistory));
        editor.apply();
    }

    private void restoreState() {
        stack = stringToStack(prefs.getString(stackKey,""));
        cardHistory = stringToStack(prefs.getString(historyKey,""));
        updateCounts();
    }

    protected void onDestroy() {
        super.onDestroy();

        saveState();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_card_stack, menu);
        return true;
    }

    private void updateScreen(boolean keepScreenOn) {
        if(keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_reset) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_reset_title)
                    .setPositiveButton(R.string.dialog_reset_pos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reset();
                        }
                    })
                    .setNegativeButton(R.string.dialog_reset_neg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create().show();
        }

        final Context context = this;

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_settings) {
            final LayoutInflater inflater = CardStack.this.getLayoutInflater();
            final AlertDialog diag = new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_settings_title)
                    .setView(inflater.inflate(R.layout.dialog_settings, null))
                    .setPositiveButton(R.string.dialog_settings_pos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton(R.string.dialog_settings_neg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create();
            diag.show();
            ((EditText) diag.findViewById(R.id.numExtraEditText)).setText("" + numExtra);
            ((EditText) diag.findViewById(R.id.numSidesEditText)).setText(""+ numSides);
            ((EditText) diag.findViewById(R.id.numDiceEditText)).setText(""+numDice);
            ((EditText) diag.findViewById(R.id.numRollsEditText)).setText(""+Math.max(cardsToShow, 0));
            ((CheckBox) diag.findViewById(R.id.showShufflesCheckBox)).setChecked(showShuffles);
            ((CheckBox) diag.findViewById(R.id.keepScreenOnCheckBox)).setChecked(keepScreenOn);
            ((CheckBox) diag.findViewById(R.id.showRemainingCheckBox)).setChecked(showCardsRemaining);
            ((CheckBox) diag.findViewById(R.id.uniformCheckBox)).setChecked(uniformDistribution);
            ((CheckBox) diag.findViewById(R.id.showAllCheckBox)).setChecked(cardsToShow == -1);
            ((CheckBox) diag.findViewById(R.id.showAllCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((TextView) diag.findViewById(R.id.numRollsEditText)).setEnabled(!isChecked);
                }
            });
            ((TextView) diag.findViewById(R.id.numRollsEditText)).setEnabled(cardsToShow != -1);
            diag.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        cardsToShow = Integer.parseInt(((TextView) diag.findViewById(R.id.numRollsEditText)).getText().toString());
                        if (((CheckBox) diag.findViewById(R.id.showAllCheckBox)).isChecked()) {
                            cardsToShow = -1;
                        }
                        showShuffles = ((CheckBox) diag.findViewById(R.id.showShufflesCheckBox)).isChecked();
                        showCardsRemaining = ((CheckBox) diag.findViewById(R.id.showRemainingCheckBox)).isChecked();
                        keepScreenOn = ((CheckBox) diag.findViewById(R.id.keepScreenOnCheckBox)).isChecked();
                        updateScreen(keepScreenOn);
                        updateViews();
                        saveSettings();
                        final int nd = Integer.parseInt(((TextView) diag.findViewById(R.id.numDiceEditText)).getText().toString());
                        if(nd > 4 || nd < 1) {
                            Toast.makeText(v.getContext(), "# of dice must be between 1-4", Toast.LENGTH_LONG).show();
                            return;
                        }
                        final int sd = Integer.parseInt(((TextView) diag.findViewById(R.id.numSidesEditText)).getText().toString());
                        if(sd > 12 || sd < 1) {
                            Toast.makeText(v.getContext(), "# of sides must be between 1-12", Toast.LENGTH_LONG).show();
                            return;
                        }
                        final int ec = Integer.parseInt(((TextView) diag.findViewById(R.id.numExtraEditText)).getText().toString());
                        if(ec > 100 || ec < -100) {
                            Toast.makeText(v.getContext(), "# of extra must be between \u00b1100", Toast.LENGTH_LONG).show();
                            return;
                        }
                        final boolean uniform =  ((CheckBox) diag.findViewById(R.id.uniformCheckBox)).isChecked();
                        if (nd != numDice || sd != numSides || ec != numExtra || uniform != uniformDistribution) {
                            final AlertDialog d = new AlertDialog.Builder(context)
                                    .setTitle(R.string.dialog_reset_title)
                                    .setMessage(getString(R.string.confirmSettingsWarningText))
                                    .setPositiveButton(R.string.dialog_reset_pos, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            numDice = nd;
                                            numSides = sd;
                                            numExtra = ec;
                                            uniformDistribution = uniform;
                                            reset();
                                            updateViews();
                                            saveSettings();
                                            diag.dismiss();
                                        }
                                    })
                                    .setNegativeButton(R.string.dialog_reset_neg, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ((TextView) diag.findViewById(R.id.numDiceEditText)).setText(""+numDice);
                                            ((TextView) diag.findViewById(R.id.numSidesEditText)).setText(""+numSides);
                                            ((TextView) diag.findViewById(R.id.numExtraEditText)).setText(""+numExtra);
                                            ((CheckBox) diag.findViewById(R.id.uniformCheckBox)).setChecked(uniformDistribution);
                                        }
                                    }).create();
                            d.show();
                        } else {
                            diag.dismiss();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(v.getContext(), "Please enter valid numbers.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }
}
