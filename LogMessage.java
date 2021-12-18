import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.function.UnaryOperator;

@SuppressWarnings(value = {"unused"})
public class LogMessage {
    public static final class Message {
        public final String msg;
        public final int severity;

        public Message(String msg, int severity) {
            this.msg = msg;
            this.severity = severity;
        }
    }

    private static final HashMap<Integer, String> levels = new HashMap<>();

    // Constants that can be used during the construction of this class
    /**
     * Constant; sets a log message's severity to DEBUG with a weight of zero
     */
    public static final int DEBUG = 0;
    /**
     * Constant; sets a log message's severity to INFO with a weight of one
     */
    public static final int INFO = 1;
    /**
     * Constant; sets a log message's severity to WARN with a weight of two
     */
    public static final int WARN = 2;
    /**
     * Constant; sets a log message's severity to ERROR with a weight of three
     */
    public static final int ERROR = 3;
    /**
     * Constant; sets a log message's severity to FATAL with a weight of four
     */
    public static final int FATAL = 4;

    static {
        levels.put(0, "DEBUG");
        levels.put(1, "INFO");
        levels.put(2, "WARN");
        levels.put(3, "ERROR");
        levels.put(4, "FATAL");
    }

    /**
     * Update this with {@link #setDefaultOutputStreams(OutputStream...)}. Defaults to {@link System#out}
     */
    private static OutputStream[] defaultOutputStreams = new OutputStream[]{System.out};
    /**
     * A {@link String} representation of the log message
     */
    private String FINAL;

    public LogMessage(LogMessage.Message msg) {
        this(msg.msg, msg.severity);
    }

    /**
     * <p>
     * Construct a {@link LogMessage} to build a message and a respective error level. This constructor
     * will write the output to the value set at {@link #defaultOutputStreams} (Unless specifically edited,
     * should be {@link System#out}).
     * <p>
     * Logs follow the format of:<pre>
     *     [&lt;Timestamp&gt;] [&lt;Error-Level&gt;] at &lt;Calling-Point&gt;: &lt;Message&gt;</pre>
     * <p>
     * A log's error level should follow this rule:
     * <ol start="0">
     *     <li>
     *         <b>Debug</b> logs are used to diagnose applications. Debugging messages can inform about what the
     *         program is doing at a certain step and what it gets as a result of these actions. For example, a
     *         message can contain information about the output of some function, so that we can see if it should
     *         be fixed.
     *     <li>
     *         <b>Info</b> is used to log important information about an application. It is used to log service
     *         start, service stop, configurations, assumptions. For example, the information about the user who has
     *         just registered on the website.
     *     <li>
     *         <b>Warn</b> logs are considered to be the first level of application failure. They are usually
     *         applied to log repeated attempts to access a resource, missing secondary data, or switching from a
     *         primary server to a back-up server. For instance, there can be a message about a possible
     *         disconnection with the server.
     *     <li>
     *         <b>Error</b> log level is used for more critical problems. These kinds of issues usually affect the
     *         result of the operation but do not terminate the program. Errors are considered to be the second
     *         level of application failures. For example, there can be a message that a user could not log in
     *         because the database was temporarily unavailable.
     *     <li>
     *         <b>Fatal</b> is the third level of application failures. It is used to indicate a much more serious
     *         error that causes the termination of the program. Such a message may say that the program totally
     *         failed and depending on the time and conditions of this failure the developers can find out how to
     *         fix the problem.
     * </ol>
     *
     * @param message is the message to be logged.
     * @param level   Select either {@link #DEBUG}, {@link #INFO}, {@link #WARN}, {@link #ERROR}, or {@link #FATAL}
     *                in accordance with the chart above to serve as an indication of the log's severity.
     */
    public LogMessage(String message, int level) {
        this(message, level, defaultOutputStreams);
    }

    /**
     * Allows for logging variables, or just an object by itself.
     *
     * @param obj      The object to be logged ({@link Object#toString()})
     * @param logValue whether or not to include some text in front of the variable or not.
     */
    public LogMessage(Object obj, boolean logValue) {
        this(logValue ? String.format("variable of %s = %s", obj.getClass(), obj) : obj.toString(), LogMessage.DEBUG);
    }

    /**
     * <p>
     * Construct a {@link LogMessage} to build a message and a respective error level. This constructor
     * will write the output to the value given as the <tt>outputs</tt> parameter.
     * <p>
     * Logs follow the format of:<pre>
     *     [&lt;Timestamp&gt;] [&lt;Error-Level&gt;] at &lt;Calling-Point&gt;: &lt;Message&gt;</pre>
     * <p>
     * A log's error level should follow this rule:
     *     <ol start="0">
     *         <li>
     *             <b>Debug</b> logs are used to diagnose applications. Debugging messages can inform about what the
     *             program is doing at a certain step and what it gets as a result of these actions. For example, a
     *             message can contain information about the output of some function, so that we can see if it should
     *             be fixed.
     *         <li>
     *             <b>Info</b> is used to log important information about an application. It is used to log service
     *             start, service stop, configurations, assumptions. For example, the information about the user who has
     *             just registered on the website.
     *         <li>
     *             <b>Warn</b> logs are considered to be the first level of application failure. They are usually
     *             applied to log repeated attempts to access a resource, missing secondary data, or switching from a
     *             primary server to a back-up server. For instance, there can be a message about a possible
     *             disconnection with the server.
     *         <li>
     *             <b>Error</b> log level is used for more critical problems. These kinds of issues usually affect the
     *             result of the operation but do not terminate the program. Errors are considered to be the second
     *             level of application failures. For example, there can be a message that a user could not log in
     *             because the database was temporarily unavailable.
     *         <li>
     *             <b>Fatal</b> is the third level of application failures. It is used to indicate a much more serious
     *             error that causes the termination of the program. Such a message may say that the program totally
     *             failed and depending on the time and conditions of this failure the developers can find out how to
     *             fix the problem.
     *     </ol>
     * <p>
     *     This constructor allows for the setting of a specific {@link OutputStream} through which the log is
     *     written. Any value presented here will override a value that might have been set using
     *     {@link #setDefaultOutputStreams(OutputStream...)}
     *
     * @param message is the message to be logged.
     * @param level   Select either {@link #DEBUG}, {@link #INFO}, {@link #WARN}, {@link #ERROR}, or {@link #FATAL}
     *                in accordance with the chart above to serve as an indication of the log's severity.
     * @param outputs Select where you'd like the logger to write to, must extend {@link OutputStream}.
     */
    public LogMessage(String message, int level, OutputStream... outputs) {
        try {
            this.FINAL = LogMessage.build(message, level);

            if (outputs != null) {
                defaultOutputStreams = Arrays.copyOf(outputs, outputs.length);
                for (OutputStream output : removeDuplicateElements(defaultOutputStreams)) {
                    output.write(this.FINAL.getBytes());
                    output.write(0x0A); //newline
                }
            }
        } catch (IOException io) {
            new LogMessage(io.getMessage(), LogMessage.ERROR, System.out);
            io.printStackTrace();
        }
    }

    /**
     * Method to find the caller of any other method. Uses basic reflection and a call to the StackTrace to
     * find this value.
     *
     * @return the {@link java.lang.StackTraceElement StackTraceElement} of the caller of this class.
     * @throws NullPointerException  if there is a flaw in this program, and the regex sample could
     *                               not identify this own class.
     * @throws IllegalStateException if the Stack's elements are in an unexpected order.
     */
    protected static java.lang.StackTraceElement getCaller(int offset) throws NullPointerException, IllegalStateException {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        Class<?> clazz = LogMessage.class;

        // regex to locate this instance in the stack chain
        Pattern pattern = Pattern.compile(String.format("^%s\\.<init>\\(%s\\.java:\\d++\\)$", (
                (UnaryOperator<String>) str -> {
                    // lambda to escape periods/dollar signs in the package + class
                    if (!str.contains(".") && !str.contains("$")) {
                        return str;
                    }
                    StringBuilder res = new StringBuilder();
                    for (char c : str.toCharArray()) {
                        res.append(c == '.' ? "\\." : c == '$' ? "\\$" : c);
                    }
                    return res.toString();
                }
        ).apply(clazz.getName()), clazz.getSimpleName()));

        // matching StackTraceElements
        Integer thisIndex = null;
        for (int i = 0; i < trace.length; i++) {
            if (pattern.matcher(trace[i].toString()).find()) {
                thisIndex = i;
            }
        }

        // exception handling
        if (thisIndex == null) {
            System.out.println(Arrays.toString(trace));
            System.out.println(pattern.pattern());
            System.out.println(clazz.getName());
            throw new ExceptionInInitializerError("Error: could not find this own class");
        } else if (thisIndex + 1 > trace.length - 1) {
            throw new IllegalStateException("There is a discrepancy in the stack chain");
        }

        return trace[thisIndex + 1 + offset];
    }

    protected static java.lang.StackTraceElement getCaller() {
        return getCaller(0);
    }

    private static String build(String message, int level, int offset) {
        String datestamp = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss,SSS").format(new Date());
        String caller = getCaller(offset).toString();

        return String.format("[%s] [%s] at %s in thread %s: %s", datestamp,
                levels.get((Math.min(4, Math.max(level, 0)))), caller, Thread.currentThread().getName(), message);
    }

    public static String build(String message, int level) {
        return build(message, level, 0);
    }

    public static void logIf(LogMessage.Message msg, boolean condition) {
        if (condition) {
            try {
                String datestamp = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss,SSS").format(new Date());
                String actual = String.format("[%s] [%s] at %s in thread %s: %s", datestamp,
                        levels.get((Math.min(4, Math.max(msg.severity, 0)))), Thread.currentThread().getStackTrace()[2], Thread.currentThread().getName(), msg.msg);

                for (OutputStream os : LogMessage.defaultOutputStreams) {
                    os.write(actual.getBytes());
                    os.write(0x0A); //newline
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("error");
            }

        }
    }

    /**
     * Get the default {@link java.io.OutputStream}
     *
     * @return the value set at {@link #defaultOutputStreams}
     */
    public static OutputStream[] getDefaultOutputStreams() {
        return Arrays.copyOf(defaultOutputStreams, defaultOutputStreams.length);
    }

    /**
     * Set the default {@link java.io.OutputStream} that will be used when the {@link #LogMessage(String, int)}
     * constructor is used. This value is set to default as {@link System#out}
     *
     * @param defaultOutputStreams an {@link java.io.OutputStream} to serve as the means of displaying the log message.
     *                             If this is set to {@code null}, then the construction of {@link LogMessage} objects
     *                             will not print the log to an output source.
     */
    public static void setDefaultOutputStreams(OutputStream... defaultOutputStreams) {
        LogMessage.defaultOutputStreams =
                removeDuplicateElements(Arrays.copyOf(defaultOutputStreams, defaultOutputStreams.length));
    }

    /**
     * Remove duplicate elements in an array. This can be useful for other projects too!
     *
     * @param array the array to be analyzed.
     * @param <T>   the type of the array, should already be inferred.
     * @return An array of the type <tt>T</tt> with no repeating elements.
     */
    public static <T> T[] removeDuplicateElements(T[] array) {
        ArrayList<T> res = new ArrayList<>();
        HashMap<T, String> exist = new HashMap<>();
        for (T element : array) {
            if (exist.get(element) == null) {
                res.add(element);
                exist.put(element, element.toString());
            }
        }
        // instantiate a generic array
        @SuppressWarnings("unchecked")
        T[] copy = (T[]) Array.newInstance(array.getClass().getComponentType(), res.size());
        for (int i = 0; i < copy.length; i++) {
            copy[i] = res.get(i);
        }
        return copy;
    }

    /**
     * Get the message built in the constructor as a {@link String}.
     *
     * @return The value produced upon construction.
     */
    @Override
    public String toString() {
        return this.FINAL;
    }
}
