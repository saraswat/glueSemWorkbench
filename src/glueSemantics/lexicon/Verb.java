/*
 * Copyright 2018 Moritz Messmer and Mark-Matthias Zymla.
 * This file is part of the Glue Semantics Workbench
 * The Glue Semantics Workbench is free software and distributed under the conditions of the GNU General Public License,
 * without any warranty.
 * You should have received a copy of the GNU General Public License along with the source code.
 * If not, please visit http://www.gnu.org/licenses/ for more information.
 */

package glueSemantics.lexicon;

import glueSemantics.semantics.lambda.SemAtom;
import glueSemantics.semantics.lambda.SemFunction;
import glueSemantics.semantics.lambda.SemPred;
import glueSemantics.semantics.lambda.SemType;
import glueSemantics.synInterface.dependency.LexVariableHandler;
import glueSemantics.linearLogic.LLAtom;
import glueSemantics.linearLogic.LLFormula;
import glueSemantics.linearLogic.LLTerm;
import glueSemantics.synInterface.dependency.LexicalParserException;

import java.util.*;

public class Verb extends LexicalEntry {

    LexType lexType;
    private SubcatFrame subcatFrame;

    public Verb(SubcatFrame subcatFrame, String lemma) throws LexicalParserException {
        this.lexType = subcatFrame.getLextype();

        //f is standard variable for complete f-structure
        //g is standard variable for subject
        //h is standard variable for object
        switch (this.getLexType()) {
            case V_INTR:
                //Parentheses necessary for variable scope!
            {
                LexicalEntry agent = subcatFrame.getRole("agent");

                /*Linear Logic*/
                LLAtom agentRes = new LLAtom(subcatFrame.getScopeVar("agent"), LLTerm.Type.E, LLAtom.LLType.CONST, false);

                LLAtom fsem = new LLAtom(LexVariableHandler.returnNewVar(LexVariableHandler.variableType.LLatomT),
                        LLTerm.Type.T, LLAtom.LLType.CONST,true);

                this.setLlTerm(new LLFormula(agentRes,fsem,true ));

                /*Semantics*/
                SemAtom agentVar = new SemAtom(SemAtom.SemSort.VAR,
                        //binding variable
                        LexVariableHandler.returnNewVar(LexVariableHandler.variableType.SemVarE),
                        SemType.AtomicType.E
                );

                SemFunction verbSem = new SemFunction(agentVar,new SemPred(lemma,agentVar));

                this.setSem(verbSem);

                break;
            }

            case V_TRANS: {


                LexicalEntry agent = subcatFrame.getRole("agent");
                LexicalEntry patient = subcatFrame.getRole("patient");

                /*Linear Logic*/

                //generating consumer
                LLAtom agentRes = new LLAtom(subcatFrame.getScopeVar("agent"), LLTerm.Type.E, LLAtom.LLType.CONST, false);
                LLAtom patientRes = new LLAtom(subcatFrame.getScopeVar("patient"), LLTerm.Type.E, LLAtom.LLType.CONST, false);

                //generate semantics
                LLAtom fsem = new LLAtom(LexVariableHandler.returnNewVar(LexVariableHandler.variableType.LLatomT),
                        LLTerm.Type.T, LLAtom.LLType.CONST, true);

                LLFormula firstArg = new LLFormula(patientRes, fsem, true);

                this.setLlTerm(new LLFormula(agentRes, firstArg, true));


                /*Semantics*/
                SemAtom agentVar = new SemAtom(SemAtom.SemSort.VAR,
                        //binding variable
                        LexVariableHandler.returnNewVar(LexVariableHandler.variableType.SemVarE),
                        SemType.AtomicType.E
                );

                SemAtom patientVar = new SemAtom(SemAtom.SemSort.VAR,
                        //binding variable
                        LexVariableHandler.returnNewVar(LexVariableHandler.variableType.SemVarE),
                        SemType.AtomicType.E
                );


                SemFunction verbSem = new SemFunction(agentVar,
                        new SemFunction(patientVar,
                                new SemPred(lemma,agentVar,patientVar)));

                this.setSem(verbSem);

                break;
            }
            default:
                throw new LexicalParserException("Verb type not implemented yet");

        }
    }


    //Setter and Getter methods

    public LexType getLexType() {
        return lexType;
    }

    public void setLexType(LexType lexType) {
        this.lexType = lexType;
    }

    /*
    public String getLlFormula() {
        return llFormula;
    }

    public void setLlFormula(String llFormula) {
        this.llFormula = llFormula;
    }
*/

    //trivial version of generating LexType from SubCatFrame
    public LexType lexTypeFromSubCat(LinkedHashMap<String,LexicalEntry> subCat)
    {
        if (subCat.size() == 1)
        {
            return LexType.V_INTR;
        }
        if (subCat.size() == 2 && subCat.containsKey("patient"))
        {
            return LexType.V_TRANS;
        }

        return null;
    }
}


