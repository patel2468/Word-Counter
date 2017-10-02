import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Program to test static methods {@code generateElements} and
 * {@code nextWordOrSeparator}.
 *
 * @author Harsh Patel
 *
 */
public final class WordCounter {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private WordCounter() {
    }

    /**
     * Unsure of correct documentation from slides.
     *
     * Comparator to sort in alphabetical order
     */
    private static class Alphabetical implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            return str1.compareToIgnoreCase(str2);
        }
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    public static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        String word = "";
        boolean isSeparator = false;
        int i = position;

        if (separators.contains(text.charAt(position))) {
            word += text.charAt(position);
        }

        StringBuffer buf = new StringBuffer();
        while (!separators.contains(text.charAt(position)) && i < text.length()
                && !isSeparator) {
            if (!separators.contains(text.charAt(i))) {
                buf.append(text.charAt(i));
            } else {
                isSeparator = true;
            }
            word = buf.toString();
            i++;
        }

        return word;
    }

    /**
     * Creates and returns a Map from an input stream that has each word as each
     * key and the count of each word as the values.
     *
     * @param in
     *            the input stream
     * @param separators
     *            set of characters that don't count as words
     * @return a map consisting of all of the words from the input stream with
     *         the number of times they appear
     */
    public static Map<String, Integer> wordCountsFromInput(SimpleReader in,
            Set<Character> separators) {
        Map<String, Integer> wordCounts = new Map1L<>();

        while (!in.atEOS()) {
            // Work with one line
            String line = in.nextLine();
            int pos = 0;

            while (pos < line.length()) {
                // Work with one word
                String word = nextWordOrSeparator(line, pos, separators);
                pos += word.length();

                // If the word is not a separator
                if (!separators.contains(word.charAt(0))) {
                    // Add it to the map or increment the count for the word
                    if (!wordCounts.hasKey(word)) {
                        wordCounts.add(word, 1);
                    } else {
                        wordCounts.replaceValue(word,
                                wordCounts.value(word) + 1);
                    }
                }
            }
        }
        return wordCounts;
    }

    /**
     * Creates and returns a queue containing all the keys from a map.
     *
     * @param map
     *            the map that the keys will be taken from
     * @return a queue consisting of the keys from map
     */
    public static Queue<String> createSetFromMapKeys(Map<String, Integer> map) {
        Queue<String> queue = new Queue1L<>();
        Map<String, Integer> temp = map.newInstance();

        // Take each key from map and put it into set
        temp.transferFrom(map);
        while (temp.size() > 0) {
            Map.Pair<String, Integer> wordCountPair = temp.removeAny();
            queue.enqueue(wordCountPair.key());
            // Add each key back to map
            map.add(wordCountPair.key(), wordCountPair.value());
        }
        return queue;
    }

    /**
     * Creates a table in HTML containing all the words in map/queue in alpha
     * order along with word counts.
     *
     * @param map
     *            the map that contains the words/word counts
     * @param queue
     *            the queue that containst the words in alpha order
     * @param out
     *            the output stream
     * @clears queue
     */
    public static void outputTableFromMap(Map<String, Integer> map,
            Queue<String> queue, SimpleWriter out) {
        while (queue.length() > 0) {
            String word = queue.dequeue();
            out.println("<tr>");
            out.println("<td> " + word + " </td>");
            out.println("<td> " + map.value(word) + " </td>");
            out.println("</tr>");
        }
    }

    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * <html> <head> <title> Title </title> </head> <body>
     * <h2>Header</h2>
     * <hr />
     * Table Headings
     *
     * @param out
     *            the output stream
     * @param fileName
     *            the name of the output file
     * @updates out.content
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(SimpleWriter out, String fileName) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        out.println("<html>");
        out.println("<head> <title> Words Counted in " + fileName
                + "</title> </head>");
        out.println("<body>");
        out.println("<h2> Words Counted in " + fileName + "</h2>");
        out.println("<hr />");
        out.println("<table border=\"1\" >");
        out.println("<tr>");
        out.println("<th> Words </th>");
        out.println("<th> Counts </th>");
        out.println("</tr>");
    }

    /**
     * Outputs the "closing" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * </table>
     * </body> </html>
     *
     * @param out
     *            the output stream
     * @updates out.contents
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        out.println("</table>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {

        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        String inputFileName;
        String outputFileName;

        //Get input file
        out.println("Enter name of inputFile: ");
        inputFileName = in.nextLine();
        SimpleReader fileIn = new SimpleReader1L(inputFileName);

        out.println("Enter name of output HTML file: ");
        outputFileName = in.nextLine();
        SimpleWriter fileOut = new SimpleWriter1L(outputFileName);

        //Output HTML headers
        outputHeader(fileOut, inputFileName);

        //Define separator characters
        Set<Character> separators = new Set1L<>();
        separators.add(' ');
        separators.add(',');
        separators.add('.');
        separators.add('-');

        //Create map containing all words and word counts
        Map<String, Integer> wordsMap = wordCountsFromInput(fileIn, separators);

        //Create set of keys from map so they can be alphabetized
        Queue<String> wordQueue = createSetFromMapKeys(wordsMap);

        //Alphabetize wordsSet
        Comparator<String> alpha = new Alphabetical();
        wordQueue.sort(alpha);

        //Output table in alphabetical order in HTML file
        outputTableFromMap(wordsMap, wordQueue, fileOut);
        //Output HTML footer
        outputFooter(fileOut);

        out.println("Done.");

        in.close();
        out.close();
    }
}
