package threads.templates;

public class RegexExpressions {
    public final static String PRINT_REGEX = "print[ ][a-zA-Z0-9]+;";
    public final static String PRINT_STRING_REGEX = "print[ ]'[A-Za-z0-9]+';";
    public final static String PRINT_VARIABLE_REGEX = "print[ ][A-Za-z0-9]+;";
    public final static String INTEGER_VARIABLE_REGEX = "var[ ][a-z]+[ ]*=[ ]*[0-9]+;";
    public final static String CALCULATION_REGEX1 = "var[ ][a-zA-Z][ ]*=[ ]*[0-9a-zA-Z]+[ ]*\\+[ ]*[0-9a-zA-Z]+;";
    public final static String EXIT_REGEX = "exit;";
}
