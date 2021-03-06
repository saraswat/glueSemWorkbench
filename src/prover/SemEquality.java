/*
 * Copyright 2018 Moritz Messmer and Mark-Matthias Zymla.
 * This file is part of the Glue Semantics Workbench
 * The Glue Semantics Workbench is free software and distributed under the conditions of the GNU General Public License,
 * without any warranty.
 * You should have received a copy of the GNU General Public License along with the source code.
 * If not, please visit http://www.gnu.org/licenses/ for more information.
 */

package prover;

import glueSemantics.semantics.lambda.SemAtom;

public class SemEquality {

    private final SemAtom variable;
    private final SemAtom constant;

    public SemEquality(SemAtom variable, SemAtom constant)
    {
        this.variable = variable;
        this.constant = constant;
    }


    public SemAtom getVariable() {
        return variable;
    }

    public SemAtom getConstant() {
        return constant;
    }


    @Override
    public String toString() {
        return variable.getName() +variable.getType() + constant.getName() + variable.getType();
    }



    // equals for this object yields true if within the constant and the variable name and type are equal
    @Override
    public boolean equals(Object b)
    {


        if (!(b instanceof SemEquality)) {
            return false;
        }
        if (b == this) {
            return true;
        }

        SemEquality eq = (SemEquality) b;

        return eq.variable.getName().equals(this.variable.getName()) &&
                eq.variable.getType().equals(this.variable.getType()) &&
                eq.constant.getName().equals(this.constant.getName()) &&
                eq.constant.getType().equals(this.constant.getType());

    }


    //used by equals() to determine similarity between elements relevant for equals()
    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + this.variable.getName().hashCode();
        result = 31 * result + this.variable.getType().hashCode();
        result = 31 * result + this.constant.getName().hashCode();
        result = 31 * result + this.constant.getType().hashCode();
        return result;
    }

}
