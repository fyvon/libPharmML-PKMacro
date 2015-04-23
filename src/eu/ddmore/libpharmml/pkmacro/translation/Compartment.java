/*******************************************************************************
 * Copyright (c) 2015 European Molecular Biology Laboratory,
 * Heidelberg, Germany.
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of
 * the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on 
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations 
 * under the License.
 *******************************************************************************/
package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.Rhs;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.CompartmentMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

class Compartment extends AbstractCompartment {
	
	Compartment(String cmt, DerivativeVariable amount, Operand volume, Operand concentration) {
		super(cmt, amount, volume, concentration);
	}

	static Compartment fromMacro(CompartmentFactory cf, VariableFactory vf, CompartmentMacro macro) throws InvalidMacroException{
		ParamResolver resolver = new ParamResolver(macro);
		
		// Required parameters
		Rhs rhs_cmt = resolver.getValue("cmt");
		SymbolRef s = resolver.getValue("amount", SymbolRef.class);
		DerivativeVariable dv = resolveDerivativeVariable(vf, s);
		
		// Optionals
		Operand volume;
		if(resolver.contains("volume")){
			volume = resolver.getValue("volume",Operand.class);
		} else {
			volume = null;
		}
		Operand concentration;
		if(resolver.contains("concentration")){
			concentration = resolver.getValue("concentration",Operand.class);
		} else {
			concentration = null;
		}
		String cmt = rhs_cmt.getContent().toString();
		Compartment comp = new Compartment(cmt, dv, volume, concentration);
		cf.addCompartment(comp);
		return comp;
	}
	
}
