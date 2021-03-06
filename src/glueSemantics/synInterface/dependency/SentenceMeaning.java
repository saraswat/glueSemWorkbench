/*
 * Copyright 2018 Moritz Messmer and Mark-Matthias Zymla.
 * This file is part of the Glue Semantics Workbench
 * The Glue Semantics Workbench is free software and distributed under the conditions of the GNU General Public License,
 * without any warranty.
 * You should have received a copy of the GNU General Public License along with the source code.
 * If not, please visit http://www.gnu.org/licenses/ for more information.
 */

package glueSemantics.synInterface.dependency;

import prover.LLProver;
import prover.ProverException;
import prover.VariableBindingException;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import glueSemantics.lexicon.*;
import glueSemantics.linearLogic.Premise;
import glueSemantics.linearLogic.Sequent;

import java.util.*;

import static glueSemantics.synInterface.dependency.LexVariableHandler.variableType.LLatomE;

// Sentence meaning is a set of glue representations (i.e. a set that represents the available premises)

/**
 * This class determines which lexical entry (if any) is constructed for a given word in the sentence
 * based on its syntactic structure. (Mainly dependency structure for now)
 */
public class SentenceMeaning {
    private GrammaticalStructure dependencyStructure;
    private LinkedHashMap<IndexedWord,List<Tuple>> dependencyMap;
    private List<LexicalEntry> lexicalEntries;


    public List<LexicalEntry> getLexicalEntries() {
        return lexicalEntries;
    }



    public SentenceMeaning(String sentence) throws VariableBindingException, LexicalParserException {
        DependencyParser dp = new DependencyParser();
        GrammaticalStructure parsedSentence =  dp.parse(sentence);

        lexicalEntries = extractFromDependencyParse(parsedSentence);

        Sequent testseq = new Sequent(lexicalEntries);

    }

    /**
     * Extract lexical entries from a dependency parse as created by the Stanford CoreNLP tools.
     * @param parsedSentence A GrammaticalStructure object from the CoreNLP dependency parser
     * @return A list of LexicalEntries
     */
    public List<LexicalEntry> extractFromDependencyParse(GrammaticalStructure parsedSentence) throws LexicalParserException {
        this.dependencyStructure = parsedSentence;
        LexVariableHandler.resetVars();
        /* A depdency map is a hash map whose key is a word in the parsed sentence and whose value is
        a list of all (direct) dependencies of this word. For example:
        Every dog barks.
        every = []
        dog = det(every)
        barks = subj(dog)
        Thus we have a flat structure that still preserves possible transitive relations
        (i.e. A - B - C => A - C) For reference see Unhammer(2010; LFG-based Constituent and Function Alignment for Parallel Treebanking)
        for a similar approach on LFG
         */

        this.dependencyMap = generateDependencyMap();
        System.out.println(dependencyStructure.typedDependencies());

        // Returns the root verb
        IndexedWord root = returnRoot();

        //SubCatFrame produced from syntactic input; is used to derive meaning constructors

        SubcatFrame subcatFrame = new SubcatFrame();

        // Collection of LLFormulas for generating premises
        List<LexicalEntry> lexicalEntries = new ArrayList<>();

        /* Arity of the root verb;
        so that complements can also be analyzed like this
        */
        Integer rootArity = 0;

        Iterator it = dependencyMap.get(root).iterator();

        while (it.hasNext())
        {
            Tuple t = (Tuple)it.next();
            //Basic categorization based on Universal dependency tags
            //All types of modifiers
            if (t.left.contains("mod"))
            {
                System.out.println( t.right.toString() + " This is a modifier");
            }
            //All types of complements
            else if (t.left.contains("comp") && !t.left.contains("compound"))
            {
                rootArity++;
                System.out.println( t.right.toString() + " This is a complement");
            }

            //Processes subject
            else if (t.left.contains("subj")) {


                HashMap<String,List<LexicalEntry>> subj =
                        extractArgumentEntries(subcatFrame,"agent",
                                t.right,
                                LexVariableHandler.returnNewVar(LLatomE));
                List<LexicalEntry> main = subj.get("main");

                lexicalEntries.add(main.get(0));
                subj.remove("main");

                //Adds modifiers of the subject
                if (!subj.keySet().isEmpty()) {
                    for (String key : subj.keySet()) {
                        lexicalEntries.addAll(subj.get(key));
                    }
                }
                it.remove();
                rootArity++;
                System.out.println( t.right.toString() + " This is a subject");
            }

            //Processes object -- Same problem as subject
            else if (t.left.contains("obj"))
            {
                HashMap<String,List<LexicalEntry>> obj = extractArgumentEntries(subcatFrame,"patient",t.right,
                        LexVariableHandler.returnNewVar(LLatomE));

                List<LexicalEntry> main = (List<LexicalEntry>) obj.get("main");

                lexicalEntries.add(main.get(0));
                obj.remove("main");

                //Adds modifiers of the object
                if (!obj.keySet().isEmpty()) {
                    for (String key : obj.keySet()) {

                        lexicalEntries.addAll(obj.get(key));
                    }
                }
                it.remove();
                rootArity++;
                System.out.println( t.right.toString() + " This is an object");
            }
            else {
                throw new LexicalParserException("Unknown grammatical function: \'"+ t.left + "\' for lexical entry \""
                        + t.right.value() +"\"");
            }
        }

        /* Verb is generated last based on the structure of the sentence
        The verb is generated when all its dependencies have been processed
        */
        Verb rootverb;

        if (dependencyMap.get(root).isEmpty()) {
            rootverb = new Verb(subcatFrame,root.value());
            lexicalEntries.add(rootverb);

        }

        System.out.println(root.toString() + " has arity " + rootArity);

        return lexicalEntries;
    }


    // generates a HashMap for search purposes; flat representation of dependency structure
    public LinkedHashMap<IndexedWord,List<Tuple>> generateDependencyMap()
    {
        LinkedHashMap<IndexedWord,List<Tuple>> dependencyMap = new LinkedHashMap<>();

        for (TypedDependency structure : dependencyStructure.typedDependencies())
        {
            //new entry if no key for the respective pred is available
            if (dependencyMap.get(structure.gov()) == null)

            {
                List<Tuple> values = new ArrayList<>();
                values.add(new Tuple(structure.reln().toString(),structure.dep()));
                dependencyMap.put(structure.gov(), values);
            }
            else
            {
                dependencyMap.get(structure.gov()).add(new Tuple(structure.reln().toString(),structure.dep()));
            }
        }
        return dependencyMap;
    }



    // checks if a word has a specific governing dependency relation
    public boolean hasDependencyType(String dependency,IndexedWord word)
    {
        for (Tuple tuple : dependencyMap.get(word))
        {
            if (dependency.equals(tuple.left))
            {
                return true;
            }
        }
        return false;
    }


    //Checks dominance relation disregarding dependency
    public boolean governsWord(IndexedWord word1, IndexedWord word2)
    {
        for (Tuple tuple : dependencyMap.get(word1))
        {
            if (word2 == tuple.right)
            {
                return true;
            }
        }
        return false;
    }


    //Returns the main verb of the sentence
    public IndexedWord returnRoot() throws LexicalParserException {

        for (TypedDependency td : dependencyStructure.typedDependencies())
        {
            if (td.reln().toString().equals("root"))
            {
                return td.dep();
            }
        }
        throw new LexicalParserException("No root verb found");
    }


    // Process (nominal) arguments (Subjects, objects):
    // extracts the nominal heads and all modifiers linked to it
    // returns a HashMap containing all lexical entries related to that argument
    private HashMap<String,List<LexicalEntry>>
    extractArgumentEntries(SubcatFrame subcatFrame, String role, IndexedWord iw, String identifier) throws LexicalParserException {

        //Method variables
        HashMap<String,List<LexicalEntry>> lexEn = new HashMap<>();

        if (iw.tag().equals("NN")) {
            Noun main = new Noun(LexicalEntry.LexType.N_NN, identifier, iw.value());
            subcatFrame.initializeQuantifiedRole(role, main, LexVariableHandler.returnNewVar(LLatomE));

            lexEn.put("main", new ArrayList<LexicalEntry>(Arrays.asList(main)));

            if (dependencyMap.get(iw) != null) {
                for (Tuple t : dependencyMap.get(iw)) {

                    if (t.left.equals("amod")) {
                        if (!lexEn.containsKey("mod")) {
                            List<LexicalEntry> modifiers = new ArrayList<>();
                            modifiers.add(new Modifier(identifier, t.right.value()));
                            lexEn.put("mod", modifiers);
                        } else {
                            lexEn.get("mod").add(new Modifier(identifier, t.right.value()));
                        }
                    } else if (t.left.equals("det")) {
                        Determiner det = new Determiner(subcatFrame, t.right.value(), role);

                        lexEn.put("det", new ArrayList<LexicalEntry>(Arrays.asList(det)));

                    } else {
                        throw new LexicalParserException("Unknown grammatical function: \"" + t.left + "\" for lexical entry \""
                                + "\"" + t.right.value() + "\"");
                    }
                }
            }
        } else if (iw.tag().equals("NNP")) {

            Noun main = new Noun(LexicalEntry.LexType.N_NNP,identifier,iw.value());
            subcatFrame.initializeRole(role,main);

            lexEn.put("main",new ArrayList<LexicalEntry>(Arrays.asList(main)));
        }


        return lexEn;
    }

}




