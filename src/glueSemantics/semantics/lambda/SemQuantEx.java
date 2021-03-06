/*
 * Copyright 2018 Moritz Messmer and Mark-Matthias Zymla.
 * This file is part of the Glue Semantics Workbench
 * The Glue Semantics Workbench is free software and distributed under the conditions of the GNU General Public License,
 * without any warranty.
 * You should have received a copy of the GNU General Public License along with the source code.
 * If not, please visit http://www.gnu.org/licenses/ for more information.
 */

package glueSemantics.semantics.lambda;

import glueSemantics.semantics.SemanticRepresentation;
import prover.ProverException;

import static glueSemantics.semantics.lambda.SemQuantEx.SemQuant.UNI;
import static glueSemantics.semantics.lambda.SemType.AtomicType.T;

public class SemQuantEx extends SemanticExpression {
    private final SemQuant quantifier;
    private final SemAtom binder;
    private final SemanticRepresentation quantBody;

    public SemQuantEx(SemQuant quantifier, SemAtom binder, SemanticRepresentation quantBody) {
        this.quantifier = quantifier;
        this.binder = binder;
        this.quantBody = quantBody;
    }

    private SemQuantEx(SemQuantEx s) {
        this.quantifier = s.quantifier;
        this.binder = s.binder;
        this.quantBody = s.quantBody.clone();
    }

    public enum SemQuant {
        UNI,EX
    }

    public SemAtom getBinder() {
        return binder;
    }

    public SemanticRepresentation getQuantBody() {
        return quantBody;
    }

    @Override
    public SemType getType() {
        return new SemType(T);
    }

    @Override
    public SemanticRepresentation betaReduce() throws ProverException {
        return new SemQuantEx(quantifier, binder, quantBody.betaReduce());
    }

    @Override
    public SemanticRepresentation applyTo(SemanticRepresentation var, SemanticRepresentation arg) throws ProverException {
        return new SemQuantEx(quantifier, binder, quantBody.applyTo(var,arg));
    }

    @Override
    public SemanticExpression clone() {
        return new SemQuantEx(this);
    }

    @Override
    public String toString() {
        if (quantifier == UNI)
            return String.valueOf('\u2200') + binder + "[" + quantBody + "]";
        else
            return String.valueOf('\u2203') + binder + "[" + quantBody + "]";
    }
}
