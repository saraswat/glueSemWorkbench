package Prover;

import gluePaP.linearLogic.*;
import gluePaP.semantics.SemAtom;
import gluePaP.semantics.SemFunction;
import gluePaP.semantics.SemRepresentation;

import java.util.*;

import static gluePaP.semantics.SemAtom.SemSort.VAR;

public class LLProver {

    private List<Equality> equalities;
    private Set<SemAtom> identifierDatabase = new HashSet<>();
    private Stack<Premise> agenda = new Stack<>();
    private List<Premise> database = new ArrayList<>();
    private List<Premise> solutions = new ArrayList<>();
    private Sequent seq;


    public LLProver(Sequent seq) {
        this.seq = seq;
    }

    /*
        Does a deduction of a given sequent by evaluating the list of premises on its LHS
        and trying to find a valid proof for its RHS.
        TODO Check if compilation works properly
         */
    public List<Premise> deduce() throws ProverException,VariableBindingException {
        /*
        Initialize an agenda stack initially containing all premises from the sequent.
        Premises are popped from the stack into the database and additionally created
        premises get pushed onto the stack.
        Then initialize a database of all premises which is used to look for possible
        implication elimination steps.
        */
        agenda.removeAllElements();
        database.clear();
        solutions.clear();

        for (Premise p: seq.getLhs()) {

            /*
            * Check all premises for nested formulas. Alle nested formulas
            * (with two or more nested operators) are compiled following the algorithm
            * outlined by Hepple(1996). All extracted assumptions are added to the agenda
            * as new premises with new IDs. Assumptions are premises that contain themselves
            * in their set of assumptions, but in the course of the derivation they may carry
            * additional assumptions (when they combine with other assumptions).
            * */
            /*
            NOTE: due to the design of the conversion algorithm, a given term's discharge is always
            contained in that term's set of assumptions. This shouldn't be a problem, however, as
            terms with discharges are by design always formulas and can therefore not be arguments.
            Their assumptions are thus not relevant in the derivation process.
            */
            // TODO unnecessary checks?
            agenda.push(convert(p));
        }
        seq.getLhs().clear();
        seq.getLhs().addAll(agenda);
        /*
        Initialize the set containing the IDs of all premises of the sequent.
        This set is used to determine possible goal terms.
        */
        HashSet<Integer> goalIDs = seq.getMaxIDSet();

        /*
        The algorithm loops over the agenda until it is empty or until a premise is created
        that contains all indexes of the sequent's premises and is therefore the goal.
        */
        while (!agenda.empty()) {
            Premise curr_premise = agenda.pop();
            // add premise to database
            database.add(curr_premise);
            for (int i = 0; i < database.size(); i++) {
                Premise db_premise = database.get(i);

                if (db_premise == curr_premise)
                    continue;

                /*
                Check if the database term is a (complex) formula, if so try to do an
                implication elimination step with the current term on the agenda (curr_premise).
                If successful add the newly created Premise to the database.
                */
                if (db_premise.getGlueTerm() instanceof LLFormula) {

                    Premise new_premise = this.combinePremises(db_premise,curr_premise);
                    if (new_premise != null) {
                        new_premise.setHistory(db_premise,curr_premise);
                        System.out.println("Combining premises " + db_premise +" and " + curr_premise + " : " + new_premise);
                        if (new_premise.getPremiseIDs().equals(goalIDs)) {
                            solutions.add(new_premise);
                        }
                        else {
                            agenda.push(new_premise);
                        }
                        continue;
                    }
                }
                /*
                Check if the current term on the agenda is a (complex) formula. If so do the same procedure
                as above, but reverse (apply db_premise to curr_premise).
                 */
                if (curr_premise.getGlueTerm() instanceof LLFormula) {
                    Premise new_premise = this.combinePremises(curr_premise,db_premise);
                    if (new_premise != null) {
                        new_premise.setHistory(curr_premise,db_premise);
                        System.out.println("Combining premises " + curr_premise +" and " + db_premise + " : " + new_premise);

                        if (new_premise.getPremiseIDs().equals(goalIDs)) {
                            solutions.add(new_premise);
                        }
                        else {
                            agenda.push(new_premise);
                        }
                    }
                }
            }
        }

        /*
        All premises of the agenda were added to the database. If there are
        no possible solutions now, return a ProverException, otherwise return
        the set of solutions.
        */
        if (solutions.isEmpty())
            throw new ProverException("No valid proof found for premises");
        else
            return solutions;
    }


    /*
    implementation of the linear implication elimination rule for indexed premises
    check if arg is equivalent to LHS of func and then return RHS of func
    then check if the sets of indexes are disjoint
    if both checks succeed a new Premise is created containing the unified set of indexes
    and the RHS LL term of func (see below)
    */
    private Premise combinePremises(Premise func, Premise arg) throws VariableBindingException {


        // possible substitutions for variables and constants
        LinkedHashSet<Equality> eqs = ((LLFormula) func.getGlueTerm()).getLhs().checkCompatibility(arg.getGlueTerm());

        if (eqs == null) {return null;}

        if (eqs.size() > 0) {

            //If there are duplicate bindings no valid proof can be reached.
            if (LLProver.checkDuplicateBinding(eqs)) {
                throw new VariableBindingException();
            } else {
                //instantiates variables with constants (i.e. skolemizes the formula so it can take a constant)
                for (Equality eq : eqs) {
                    ((LLFormula) func.getGlueTerm()).instantiateVariables(eq);
                    System.out.println(eq);
                }
            }

        }

        Premise combined;

            /*
            * No assumptions or discharges involved, proceed with a "normal" implication elimination
            * */
        if (arg.getGlueTerm().assumptions.isEmpty()
                && arg.getGlueTerm().discharges.isEmpty()
                && func.getGlueTerm().assumptions.isEmpty()
                && func.getGlueTerm().discharges.isEmpty()) {
            return combineDisjointID(func, arg);
        }
            /*
            * Func or arg contain assumptions, but no discharges.
            * Combine the terms and their sets of assumptions
            * */
        else if ((!arg.getGlueTerm().assumptions.isEmpty()
                || !func.getGlueTerm().assumptions.isEmpty())
                && arg.getGlueTerm().discharges.isEmpty()
                && func.getGlueTerm().discharges.isEmpty()) {
            combined = combineDisjointID(func, arg);
            try {
                combined.getGlueTerm().assumptions = new HashSet<>();
                    /* create new set of assumptions which can be modified independently from
                    the set of assumptions of arg and func and add all assumptions to it */
                combined.getGlueTerm().assumptions.addAll(arg.getGlueTerm().assumptions);
                combined.getGlueTerm().assumptions.addAll(func.getGlueTerm().assumptions);
                //    LLTerm discharge = func.getGlueTerm().getDischarge();
                //   combined.getGlueTerm().assumptions.remove(discharge);
                // add this back to the functor's assumptions


                //                     arg.getGlueTerm().assumptions.add(discharge);


            } catch (NullPointerException npe){
                return null;
            }
            return combined;
        }
            /*
            Functor has discharges, check if they are a subset of the argument's assumptions.
            If so call combineDisjointID which checks the ID sets of func and arg and then
            does the actual implication elimination step. For the new premise, all assumptions
            from arg are copied, except the one that was discharged in func.
            func: (b[a] -o c); arg: {a,(x -o y)} ==> c with assumption {(x -o y)}
            */
        else if (!func.getGlueTerm().discharges.isEmpty()) {
            if (arg.getGlueTerm().assumptions.containsAll(func.getGlueTerm().discharges))
            {

                combined = combineDisjointID(func, arg);
                /* create new set of assumptions which can be modified independently from
                the sets of assumptions of arg and func and add all assumptions to it*/
                combined.getGlueTerm().assumptions = new HashSet<>();
                combined.getGlueTerm().assumptions.addAll(arg.getGlueTerm().assumptions);
                combined.getGlueTerm().assumptions.addAll(func.getGlueTerm().assumptions);
                combined.getGlueTerm().assumptions.removeAll(func.getGlueTerm().discharges);

                return combined;
            }
        }

        // The discharges are somehow incompatible, return null.
        return null;
    }


    /*
    * Check if the LHS of func is equivalent to arg
    * and if the two sets of indexes associated with them are disjoint.
    * If so return the simplified term (the RHS of func) with combined ID sets.
    * */
    private Premise combineDisjointID(Premise func, Premise arg) {
        HashSet<Integer> combined_IDs = new HashSet<>();
        if (((LLFormula) func.getGlueTerm()).getLhs().checkEquivalence(arg.getGlueTerm())
                && Collections.disjoint(func.getPremiseIDs(),arg.getPremiseIDs())){
            combined_IDs.addAll(func.getPremiseIDs());
            combined_IDs.addAll(arg.getPremiseIDs());

            /*Mark: this is a problem since if we use the same func twice
            the resulting object uses the same term in both occasions.
            Thus, if a future modification of one instances of the term occurs,
            the other "copy" will also receive this modification leading to
            unwanted combinations of terms.
            Moritz: Solved by creating a new LLAtom as copy of the RHS of func. If the RHS
            is an LLFormula then just copy the reference, it shouldn't cause any problems.
            */
            if (((LLFormula) func.getGlueTerm()).getRhs() instanceof  LLAtom)
                return new Premise(combined_IDs,new LLAtom((LLAtom) ((LLFormula) func.getGlueTerm()).getRhs()));
            return new Premise(combined_IDs,((LLFormula) func.getGlueTerm()).getRhs());
        }
        return null;
    }


    // wrapper method for conversion
    //public Premise convert(Premise term) {return (LLFormula) convert((LLTerm) term);}


    // TODO add lists for modifiers and skeletons (see Dick's code)
    /*
    The LHS of the LHS of f will become an assumption which in turn gets converted as well.
    The assumption gets converted as well and is marked as an assumption
    by adding itself to its set of assumptions. That is, an LLTerm "a" is an assumption
    iff its set of assumptions contains "a". This way of marking assumptions allows easy
    combination with other assumptions and LLTerms with discharges.
    All extracted assumptions are stored in a HashSet in dependency
    Ex. if f = ((a -o b) -o c) then dependency = (b -o c) and assumption = {a}
    Dependency is a new formula consisting of the rest of f, that is, the RHS of the LHS of f
    and the RHS of f.
    */
    public Premise convert(Premise p) {
        if (p.getGlueTerm() instanceof LLFormula) {
            LLFormula f = (LLFormula) p.getGlueTerm();

            // TODO the formula is a modifer no need to convert it
            /*if (f.getLhs().checkEquivalence(f.getRhs()))
                return term;
             */
            // TODO add semantic operations for conversion steps (i.e. lambda abstraction)
            if (f.getLhs() instanceof LLFormula &&
                    ((LLFormula) f.getLhs()).getOperator() instanceof LLImplication) {
                return convertNested(p);
            }
            else {
                // the term is of the form (A -o B), where A is an atomic formula
                // no conversion step needed on the glue side, but lambda abstraction on
                // the meaning side is necessary. But only if there has been no conversion?
                p.setSemTerm(this.convertSemantics(p.getSemTerm()));
                return p;
            }
        }
        return p;
    }

    /*
    * Recursively converts a semantic term by replacing the variables with newly created ones    *
    * */
    // TODO turn private again after testing
    public SemRepresentation convertSemantics(SemRepresentation sem) {
        if (sem instanceof SemFunction) {
            // create new variable with the type of the binder of the inner function
            SemAtom var = new SemAtom(VAR,"u",((SemFunction) sem).getBinder().getType());
            // apply var
            SemRepresentation compiled = ((SemFunction) sem).apply(var);
            SemRepresentation inner = convertSemantics(compiled);
            // return new function with the applied variable as binder
            return new SemFunction(var,inner);
        }
        return sem;
    }

    private Premise convertNested(Premise p) {
        if (p.getGlueTerm() instanceof LLFormula) {
            LLFormula f = (LLFormula) p.getGlueTerm();
            Premise assumption = convertNested(new Premise(seq.getNewID(), ((LLFormula) f.getLhs()).getLhs()));
            assumption.getGlueTerm().assumptions.add(assumption.getGlueTerm());
            agenda.add(assumption);
            Premise dependency = convertNested(new Premise(p.getPremiseIDs(), new LLFormula(f.getTermId(), ((LLFormula) f.getLhs()).getRhs(),
                    f.getOperator(), f.getRhs(), f.isPolarity(), f.getVariable())));
            //dependency.getGlueTerm().assumptions.addAll(assumption.getGlueTerm().assumptions);
                /* NOTE:
                * In cases where a formula like ((((a -o b) -o c) -o d) -o e) is compiled
                * we want to derive the assumptions {a} and {(b -o c)} and the dependency
                * (d -o e)[(b -o c)[a]], that is, a discharge with a nested discharge.
                * However, for a formula like (((a -o (b -o c)) -o d)
                * we want to get {a} and {b} and the dependency (c -o d)[a,b], that is,
                * with a list of (atomic) discharges. Otherwise we would run into a dead end
                * while combining the extracted premises.
                * This is achieved by only adding the single assumption that is currently
                * being extracted to the dependency's discharges.
                * */
            dependency.getGlueTerm().discharges.add(assumption.getGlueTerm());
            return dependency;
        }
        return p;
    }


    //returns false if a variable is assigned more than one value
    public static boolean checkDuplicateBinding(LinkedHashSet<Equality> in) {
         List<Equality> eqs = new ArrayList<>();
         eqs.addAll(0,in);

         // no multiple assignments possible
        if (eqs.size() <= 1)
            return false;

        for (int i = 0; i < eqs.size(); i++)
        {
            for (int j = 0; j <eqs.size(); j++)
            {
                if (eqs.get(i).getVariable().getName().equals(eqs.get(j).getVariable().getName())
                        && eqs.get(i).getVariable().getType().equals(eqs.get(j).getVariable().getType())
                        && !(eqs.get(i).getConstant().getName().equals(eqs.get(j).getConstant().getName())))
                {
                    return true;
                }
            }
        }
        return false;
    }


    /*
    * Check if semantic atom a is already in the database of identifiers.
    * If not, add it and return true; otherwise return false.
    * */
    public boolean addIdentifier(SemAtom a) {
        if (this.identifierDatabase.contains(a))
            return false;
        else {
            this.identifierDatabase.add(a);
            return true;
        }
    }

    // Returns the set of used semantic identifiers in this proof
    public Set<SemAtom> getIdentifiers() {
        return this.identifierDatabase;
    }


}
