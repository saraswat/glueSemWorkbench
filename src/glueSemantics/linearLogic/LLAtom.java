/*
 * Copyright 2018 Moritz Messmer and Mark-Matthias Zymla.
 * This file is part of the Glue Semantics Workbench
 * The Glue Semantics Workbench is free software and distributed under the conditions of the GNU General Public License,
 * without any warranty.
 * You should have received a copy of the GNU General Public License along with the source code.
 * If not, please visit http://www.gnu.org/licenses/ for more information.
 */

package glueSemantics.linearLogic;

import prover.Equality;

import java.util.*;

public class LLAtom extends LLTerm {



    public enum LLType {
        VAR,
        CONST
    }


    private String name;
    public LLType lltype;




    public LLAtom(String name, Type type, LLType lltype, boolean pol) {
        this.name = name;
        this.setType(type);
        this.setPolarity(pol);
        this.setLLtype(lltype);
    }

    //binder variables are not polarized -- they can occur in positive and negative formulas
    public LLAtom(String name, Type type, LLType lltype) {
        this.name = name;
        this.setType(type);
        this.setLLtype(lltype);
    }


    public LLAtom(LLAtom term) {
        this.assumptions = new HashSet<>(term.assumptions);
        this.discharges = new HashSet<>(term.discharges);
        this.name = term.getName();
        this.setType(term.getType());
        this.setPolarity(term.isPolarity());
        this.setLLtype(term.getLLtype());


    }

    @Override
    public String toString() {
        if (this.assumptions.isEmpty())
            return this.toPlainString();
        else {
            if (this.assumptions.size() == 1 && this.assumptions.contains(this) && this.discharges.isEmpty())
                return "{" + name + "}";
            else
                return name + this.printAssumptions();
        }
    }

    public String toPlainString() {
        return name;
    }


    // checks absolute equivalence (type and name)
    @Override
    public boolean checkEquivalence(LLTerm term) {
        if (term instanceof LLAtom) {
            if (this.name.equals(((LLAtom) term).name)
                    && this.getType().equals(((LLAtom) term).getType()))
            {
                return true;
            }
        }
        return false;

    }

    @Override
    public LinkedHashSet<Equality> checkCompatibility(LLTerm term) {
        if (term instanceof LLAtom) {
            if (this.getLLtype().equals(LLType.VAR)) {
                {
                    if (((LLAtom) term).getLLtype().equals(LLType.VAR)) {
                        // Not possible to unify two variables?
                        return null;
                    } else if ( ((LLAtom) term).getLLtype().equals(LLType.CONST) &&
                            this.getType().equals((term.getType()))) {
                        {
                            LinkedHashSet<Equality> newEq = new LinkedHashSet<>();
                            newEq.add(new Equality(this, (LLAtom) term));
                            return newEq;
                        }
                    }
                }
            } else if (this.getLLtype().equals(LLType.CONST)) {
                if (((LLAtom) term).getLLtype().equals(LLType.CONST)) {
                    if (this.getName().equals(((LLAtom) term).getName()) &&
                            this.getType().equals(term.getType())) {
                        LinkedHashSet<Equality> emptyList = new LinkedHashSet<>();
                        return emptyList;
                    } else
                        {
                            return null;
                        }
                } else if (((LLAtom) term).getLLtype().equals(LLType.VAR) &&
                        this.getType().equals(term.getType()))
                    {
                        LinkedHashSet<Equality> newEq = new LinkedHashSet<>();
                        newEq.add(new Equality((LLAtom) term,  this));
                        return newEq;
                    }


            }
        }
            return null;
    }



    // Getter and Setter name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and setter LLType
    public LLType getLLtype() {
        return lltype;
    }

    public void setLLtype(LLType lltype) {
        this.lltype = lltype;
    }


    @Override
    public LLTerm clone() {
        return new LLAtom(this   );
    }

    @Override
    public boolean isModifier() {
        return false;
    }
}
