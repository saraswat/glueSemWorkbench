/*
 * Copyright 2018 Moritz Messmer and Mark-Matthias Zymla.
 * This file is part of the Glue Semantics Workbench
 * The Glue Semantics Workbench is free software and distributed under the conditions of the GNU General Public License,
 * without any warranty.
 * You should have received a copy of the GNU General Public License along with the source code.
 * If not, please visit http://www.gnu.org/licenses/ for more information.
 */

package glueSemantics.semantics;

public interface FunctionalApplication extends SemanticRepresentation{
    // TODO: which methods does this interface introduce? applyTo?

    public void instantiateFunctionalApp(SemanticRepresentation func, SemanticRepresentation arg);

}
