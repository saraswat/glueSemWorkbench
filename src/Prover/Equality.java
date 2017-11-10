package Prover;

import com.sun.tools.corba.se.idl.constExpr.Equal;
import gluePaP.linearLogic.LLAtom;

public class Equality {

    private final LLAtom variable;
    private final LLAtom constant;

    public Equality(LLAtom variable, LLAtom constant)
    {
        this.variable = variable;
        this.constant = constant;
    }


    public LLAtom getVariable() {
        return variable;
    }

    public LLAtom getConstant() {
        return constant;
    }


    @Override
    public String toString() {
        return variable.getName() +variable.getTermId() + constant.getName() + variable.getTermId();
    }





    @Override
    public boolean equals(Object b)
    {


        if (!(b instanceof Equality))
        {
            return false;
        }
        if (b == this)
        {
            return true;
        }

        Equality eq = (Equality) b;

        return eq.variable.getName().equals(this.variable.getName()) &&
                eq.variable.getType().equals(this.variable.getType()) &&
                eq.constant.getName().equals(this.constant.getName()) &&
                eq.constant.getType().equals(this.constant.getType());

        /*

        if (this.variable.getName().equals(((Equality) b).variable.getName()) &&
                this.variable.getType().equals(((Equality) b).variable.getType()) &&
                this.constant.getName().equals(((Equality) b).constant.getName()) &&
                this.constant.getType().equals(((Equality) b).constant.getType()) )
        {
            return true;
        }
        return false;
    */
    }


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