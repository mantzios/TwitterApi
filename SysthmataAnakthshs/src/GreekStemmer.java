
import java.util.Locale;

public class GreekStemmer {
	/**
     * Buffer for the terms while stemming them.
     */
    private StringBuffer sb = new StringBuffer();
    /**
     * Indicates if a term is handled as a noun.
     */
    private boolean uppercase = false;
    /**
     * Amount of characters that are removed with <tt>substitute()</tt> while stemming.
     */
    private int substCount = 0;

    public GreekStemmer() {
    }

    /**
     * Stemms the given term to an unique <tt>discriminator</tt>.
     *
     * @param term  The term that should be stemmed.
     * @return      Discriminator for <tt>term</tt>
     */

    public String stem(String term) {

        // Check if the term is stemmable
        //if (!isStemmable(term)){
        //    return term;
        //}

        //Check if term is numeric
      //DIKH MOY ALLAGH GIATI XTYPAGE TO INDEXfILES
      //  if (term.matches("^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$"))
      //      return "";

        // Remove first level suffixes only if the term is 4 letters or more
        if (term.length() >= 4) {

            // Remove the 3 letter suffixes
            if (term.endsWith("оус")||
                term.endsWith("еис")||
                term.endsWith("еым")||
                term.endsWith("оум"))
                term = term.substring(0,term.length() - 3);

            // Remove the 2 letter suffixes
            else if (term.endsWith("ос")||
                     term.endsWith("гс")||
                     term.endsWith("ес")||
                     term.endsWith("ым")||
                     term.endsWith("оу")||
                     term.endsWith("ои")||
                     term.endsWith("ас")||
                     term.endsWith("ыс")||
                     term.endsWith("аи")||
                     term.endsWith("ус")||
                     term.endsWith("ом")||
                     term.endsWith("ам")||
                     term.endsWith("еи"))

                     term = term.substring(0,term.length() - 2);

            // Remove the 1 letter suffixes
            else if (term.endsWith("а")||
                     term.endsWith("г")||
                     term.endsWith("о")||
                     term.endsWith("е")||
                     term.endsWith("ы")||
                     term.endsWith("у")||
                     term.endsWith("и"))

                     term = term.substring(0,term.length() - 1);
        }
        return term;
    }
}    
