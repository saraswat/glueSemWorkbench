package gluePaP.parser;

/*
    Todo: Already add possibility to parse complex formulas including parenthesis e.g. (f -o g) -o h
 */

import gluePaP.linearLogic.Atom;
import gluePaP.linearLogic.LLTerm;

import java.util.List;

public class ParserMain {
    public String Input;
    public List<LLTerm> Premises;
    public Atom Goal;

    public ParserMain(String input)
    {
        this.Input = input;

        // TODO Handle unwanted exceptions (Number of array arguments)
        String[] In = Input.split("=>");
        String[] stringPremises = In[0].split(",");


        if (stringPremises.length != 0)
        {
            // TODO Create semantic parser
            LinearLogicParser llParser = new LinearLogicParser();
            for (String stringPremise :  stringPremises) {
                String[] glueRepresentation = stringPremise.split(":");

                llParser.UnparsedPremises.add(glueRepresentation[1]);
            }
        }
        else
            {
            throw new IllegalArgumentException();
        }

    }



    /*
    public void parse(String str) {
        // Initialize list of left hand side terms for the sequent
        List<LLTerm> lhs_terms;
        // Initialize new sequent
        //Sequent input_seq = new Sequent();
        // Read input string characterwise
        for (int i = 0; i < str.length(); i++) {
            int c = (int) str.charAt(i);

            // character is a whitespace
            if (c == 32)
                continue;
                // character is a lower case letter
            else if (c >= 97 || c <= 122) {
                lhs_terms.add(new LLConstant(Character.toString((char) c)));
            }
            // character is an upper case letter
            else if (c >= 65 || c <= 90) {
                lhs_terms.add(new LLVariable(Character.toString((char) c)));
            }
            // character is a left parenthesis, set scope
            else if (c == 40) {

            }
            // character is a comma, delimits premises
            else if (c == 44) {

            }
            // character is an equal sign, might be first part of consequent separator (=>)
            else if (c == 61) {

            }
            // character is a minus, might be first part of linear implication
            else if (c == 45) {

            } else {
                // return exception?
            }


        }
    }
    */
}