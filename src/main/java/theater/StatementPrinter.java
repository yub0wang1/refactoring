package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    public final Invoice getInvoice() {
        return invoice;
    }

    public final Map<String, Play> getPlays() {
        return plays;
    }

    /**
     * Returns the play associated with the given performance.
     * @param performance the performance to look up
     * @return the play associated with the performance
     */
    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    /**
     * Calculates the amount owed for a given performance.
     * @param performance the performance to calculate the amount for
     * @return the amount owed in cents
     * @throws RuntimeException if the play type is not known
     */
    private int getAmount(Performance performance) {
        int result = 0;
        final Play play = getPlay(performance);

        switch (play.getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }

        return result;
    }

    /**
     * Calculates the volume credits earned for a given performance.
     * @param performance the performance to calculate credits for
     * @return the volume credits earned
     */
    private int getVolumeCredits(Performance performance) {
        int result = 0;
        final Play play = getPlay(performance);

        // add volume credits
        result += Math.max(performance.getAudience()
                - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        if ("comedy".equals(play.getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }

        return result;
    }

    /**
     * Formats an amount in cents as US dollars.
     * @param amountInCents the amount in cents to format
     * @return the formatted dollar amount as a string
     */
    private String usd(int amountInCents) {
        return NumberFormat.getCurrencyInstance(Locale.US)
                .format(amountInCents / Constants.PERCENT_FACTOR);
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;
        final StringBuilder result = new StringBuilder("Statement for "
                + invoice.getCustomer() + System.lineSeparator());

        for (Performance performance : invoice.getPerformances()) {
            final Play play = getPlay(performance);

            volumeCredits += getVolumeCredits(performance);

            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n",
                    play.getName(),
                    usd(getAmount(performance)),
                    performance.getAudience()));
            totalAmount += getAmount(performance);
        }
        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }
}