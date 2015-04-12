/**
 * Copyright 2009 Wilfred Springer
 * Copyright 2012 Jason Pell
 * Copyright 2013 Antonio García-Domínguez
 * Copyright 2013 Roberto Ramírez Vique
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.flotsam.xeger.automated;

import nl.flotsam.xeger.Xeger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(value = Parameterized.class)
public class RegexGenerationSupportTest {

    public static final int MAX_ITERATIONS = 100;
    private Class<? extends Throwable> expected;
    private final String regex;
    private final boolean working;
    private static final Logger logger = Logger.getLogger(RegexGenerationSupportTest.class.getSimpleName());

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    public RegexGenerationSupportTest(boolean working, String regexToTest, Class<? extends Throwable> expected) {
        this.regex = regexToTest;
        this.working = working;
        this.expected = expected;
    }

    @Parameters(name="Regexp {1} should work? {0}")
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{
                {true, "", null},

                // Predefined character classes does not work
                {false, "\\d\\d", null},
                {false, "\\d{3}", null},
                {false, "\b(\\w+)\\s+\\1\b ", null},
                // Supported elements from java api
                {true, "[ab]{4,6}c", null},
                {true, "a|b", null},
                {true, "[abc]", null},
                {true, "[^abc]", null},
                {true, "a+", null},
                {true, "a*", null},
                {true, "ab", null},
                {true, "[a-zA-Z]", null},
                // union and intersection does not works
                {false, "[a-d[m-p]]", null},
                {false, "[a-z&&[def]]", null},
                {false, "[a-z&&[^bc]]", null},
                {false, "[a-z&&[^m-p]]", null},
                {true, "a|b", null},
                {false, "a||b", null},
                {false, "a|", null},
                {false, "|b", null},

                // predefined character classes
                {true, ".", null},
                {false, "\\d", null},
                {false, "\\D", null},
                {false, "\\s", null},
                {false, "\\S", null},
                {false, "\\w", null},
                {false, "\\W", null},
                // POSIX character classes
                {false, "\\p{Lower}", null},
                {false, "\\p{Upper}", null},
                {false, "\\p{ASCII}", null},
                {false, "\\p{Alpha}", null},
                {false, "\\p{Digit}", null},
                {false, "\\p{Alnum}", null},
                {false, "\\p{Punct}", null},
                {false, "\\p{Graph}", null},
                {false, "\\p{Print}", null},
                {false, "\\p{Blank}", null},
                {false, "\\p{Cntrl}", null},
                {false, "\\p{XDigit}", null},
                {false, "\\p{Space}", null},
                // java.lang.Character classes
                {false, "\\p{javaLowerCase}", null},
                {false, "\\p{javaUpperCase}", null},
                {false, "\\p{javaWhitespace}", null},
                {false, "\\p{javaMirrored}", null},
                // Classes for Unicode blocks and categories
                {false, "\\p{InGreek}", null},
                {false, "\\p{Lu}", null},
                {false, "\\p{Sc}", null},
                {false, "\\P{InGreek}", null},
                {false, "\\[\\p{L}&&[^\\p{Lu}]]", null},
                // Boundary matchers
                {false, "^aaaa", null},
                {false, "^abc$", null},
                {false, "a.*b$", null},
                {false, "\\b", null},
                {false, "\\B", null},
                {false, "\\A", null},
                {false, "\\G", null},
                {false, "\\Z", null},
                {false, "\\z", null},

                // Optionals
                {true, "A?BC", null},
                {true, "AB?C", null},
                {true, "ABC?", null},

                /*
                // Greedy quantifiers
                {false, "A*BC", StackOverflowError.class},
                {false, "AB*C", StackOverflowError.class},
                {false, "ABC*", StackOverflowError.class},

                {true, "A+BC", null},
                {true, "AB+C", null},
                {true, "ABC+", null},
                {true, "A{3}BC", null},
                {true, "AB{3}C", null},
                {true, "ABC{3}", null},
                {true, "A{0}BC", null},
                {true, "AB{0}C", null},
                {true, "ABC{0}", null},
                {true, "A{2}BC{2}", null},
                {true, "A{2}BC{2}", null},
                 */


                /*     TODO


X{n}	X, exactly n times
X{n,}	X, at least n times
X{n,m}	X, at least n but not more than m times

Reluctant quantifiers
X??	X, once or not at all
X*?	X, zero or more times
X+?	X, one or more times
X{n}?	X, exactly n times
X{n,}?	X, at least n times
X{n,m}?	X, at least n but not more than m times

Possessive quantifiers
X?+	X, once or not at all
X*+	X, zero or more times
X++	X, one or more times
X{n}+	X, exactly n times
X{n,}+	X, at least n times
X{n,m}+	X, at least n but not more than m times

Logical operators
XY	X followed by Y
X|Y	Either X or Y
(X)	X, as a capturing group

Back references
\n	Whatever the nth capturing group matched

Quotation
\	Nothing, but quotes the following character
\Q	Nothing, but quotes all characters until \E
\E	Nothing, but ends quoting started by \Q

Special constructs (non-capturing)
(?:X)	X, as a non-capturing group
(?idmsux-idmsux) 	Nothing, but turns match flags i d m s u x on - off
(?idmsux-idmsux:X)  	X, as a non-capturing group with the given flags i d m s u x on - off
(?=X)	X, via zero-width positive lookahead
(?!X)	X, via zero-width negative lookahead
(?<=X)	X, via zero-width positive lookbehind
(?<!X)	X, via zero-width negative lookbehind
(?>X)	X, as an independent, non-capturing group
                 */

        };
        return Arrays.asList(data);
    }

    @Test
    public void shouldNotGenerateTextCorrectly() throws Exception {

        if (expected != null) {
            exceptionRule.expect(expected);
        }
        try {
            Xeger generator = new Xeger(regex);
            boolean alwaysCorrect = true;
            String regexCleaned = regex.replace("\\", "");
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                String text = generator.generate();

                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO,
                            "For pattern \"{0}\" \t\t, generated: \"{1}\"",
                            new Object[]{regex, text});
                }

                if (working) {
                    assertTrue("text generated: " + text + " does match regexp: "
                            + regex, text.matches(regex));
                } else {
                    /**
                     * In case of non supported we have some which just sometimes works but at some point they doesn't.
                     *
                     * So what we want to check is that not on all the cases the match is correct.
                     */

                    if (text.matches(regex) && !text.equalsIgnoreCase(regexCleaned)) {
                        logger.warning("the current regex worked when shouldn't. Text generated: |" + text
                                + "| does match regexp: |" + regex + "|");
                    } else {
                        alwaysCorrect = false;
                    }
                }
            }

            if (!working) {
                assertFalse("Should has failed some time but always worked ... it is supported! regex: " +
                        regex, alwaysCorrect);
            }
        } catch (Exception maybeIgnored) {
            logger.log(Level.SEVERE, "Error with regex: "+ regex + " which works?: " + working, maybeIgnored);
            if (working) {
                throw maybeIgnored;
            }
        }

    }
}