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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;

import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.SymbolType;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.SimpleParameter;
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModel;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

public class VariableFactory {
	
	public static String DEPOT_PREFIX = "Ad";
	public static String ABSORPTION_PREFIX = "Aa";
	public static String CENTRAL_CMT_PREFIX = "Ac";
	public static String PERIPH_CMT_PREFIX = "Ap";
		
	private final Map<String, Set<Integer>> variables_count;
	
	private final List<CommonVariableDefinition> variables;
	private final List<SimpleParameter> parameters;
	
	VariableFactory(StructuralModel sm) throws InvalidMacroException{
		variables_count = new HashMap<String, Set<Integer>>();
		variables = new ArrayList<CommonVariableDefinition>();
		parameters = new ArrayList<SimpleParameter>();
		
		for(JAXBElement<? extends CommonVariableDefinition> v : sm.getCommonVariable()){
			storeVariable(v.getValue());
		}
		for(SimpleParameter p : sm.getSimpleParameter()){
			storeParameter(p);
		}
	}
	
	boolean variableExists(String symbolId){
		return flattenVariableNames().contains(symbolId);
	}
	
	private List<String> flattenVariableNames(){
		List<String> list = new ArrayList<String>();
		for(String prefix : variables_count.keySet()){
			for(Integer index : variables_count.get(prefix)){
				list.add(prefix+index);
			}
		}
		return list;
	}
	
	/**
	 * Attempt to force variable creation. If the variable name already exists, a new one will be generated.
	 * @param prefix
	 * @param index
	 * @return
	 */
	VariableDefinition createVariable(String prefix, Integer index){
		if(variableExists(prefix+index)){
			return generateVariable(prefix);
		} else {
			VariableDefinition v = new VariableDefinition();
			v.setSymbId(prefix+index);
			v.setSymbolType(SymbolType.REAL);
			try {
				storeVariable(v);
			} catch (InvalidMacroException e) {
				// Should never happen. SymbId has been checked.
				throw new RuntimeException(e);
			}
			return v;
		}
	}
	
	/**
	 * Attempt to force variable creation. If the variable name already exists, a new one will be generated.
	 * @param prefix
	 * @param index
	 * @return
	 */
	DerivativeVariable createDerivativeVariable(String prefix, Integer index){
		if(variableExists(prefix+index)){
			return generateDerivativeVariable(prefix);
		} else {
			DerivativeVariable dv = new DerivativeVariable();
			dv.setSymbId(prefix+index);
			dv.setSymbolType(SymbolType.REAL);
			try {
				storeVariable(dv);
			} catch (InvalidMacroException e) {
				// Should never happen. SymbId has been checked.
				throw new RuntimeException(e);
			}
			return dv;
		}
	}
	
	DerivativeVariable generateDerivativeVariable(String prefix){
		DerivativeVariable dv = new DerivativeVariable();
		dv.setSymbId(generateVariableName(prefix));
		dv.setSymbolType(SymbolType.REAL);
		try {
			storeVariable(dv);
		} catch (InvalidMacroException e) {
			// Should never happen. SymbId has been generated.
			throw new RuntimeException(e);
		}
		return dv;
	}
	
	VariableDefinition generateVariable(String prefix){
		VariableDefinition v = new VariableDefinition();
		v.setSymbId(generateVariableName(prefix));
		v.setSymbolType(SymbolType.REAL);
		try {
			storeVariable(v);
		} catch (InvalidMacroException e) {
			// Should never happen. SymbId has been generated.
			throw new RuntimeException(e);
		}
		return v;
	}
	
	SimpleParameter generateParameter(String prefix){
		SimpleParameter p = new SimpleParameter();
		p.setSymbId(generateVariableName(prefix));
		try {
			storeParameter(p);
		} catch (InvalidMacroException e) {
			// Should never happen. SymbId has been generated.
			throw new RuntimeException(e);
		}
		return p;
	}

	private String generateVariableName(String name){
		return name + getFirstPossibleIndex(name);
	}
	
	private Integer getFirstPossibleIndex(String prefix){
		if(variables_count.containsKey(prefix)){
			return getMax(variables_count.get(prefix))+1;
		} else {
			return 1;
		}
	}
	
	private Integer getMax(Set<Integer> set){
		Integer max = 0;
		for(Integer value : set){
			if(value > max){
				max = value;
			}
		}
		return max;
	}
	
	SymbolRef createAndReferNewParameter(String name){
		SimpleParameter param = generateParameter(name);
		SymbolRef symbRef = new SymbolRef(param.getSymbId());
		return symbRef;
	}
	
	void storeVariable(CommonVariableDefinition variable) throws InvalidMacroException{
		VariableName varName = parseVariableName(variable.getSymbId());
		storeSymbol(varName.getPrefix(), varName.getIndex());
		variables.add(variable);
	}
	
	void storeParameter(SimpleParameter p) throws InvalidMacroException{
		VariableName varName = parseVariableName(p.getSymbId());
		storeSymbol(varName.getPrefix(), varName.getIndex());
		parameters.add(p);
	}
	
	private void storeSymbol(String prefix,Integer index) throws InvalidMacroException{
		if(!variables_count.containsKey(prefix)){
			variables_count.put(prefix, new HashSet<Integer>());
		}
		if(variables_count.get(prefix).contains(index)){
			throw new InvalidMacroException(prefix+"["+index+"] is duplicate symbol");
		}
		variables_count.get(prefix).add(index);
	}
	
	List<CommonVariableDefinition> getDefinedVariables(){
		return this.variables;
	}
	
	List<SimpleParameter> getDefinedParameters(){
		return parameters;
	}
	
	VariableName parseVariableName(String raw){
		Pattern p = Pattern.compile("([a-zA-Z]+)(\\d*)");
		Matcher m = p.matcher(raw);
		if(m.find()){
			String prefix = m.group(1);
			String index = m.group(2);
			Integer intIndex;
			if(index != null && index.matches("\\d+")){
				intIndex = Integer.valueOf(index);
			} else {
				intIndex = 0;
			}
			return new VariableName(prefix, intIndex);
		} else {
			return null;
		}
	}
	
	DerivativeVariable fetchDerivativeVariable(String symbId){
		for(CommonVariableDefinition v : variables){
			if(v instanceof DerivativeVariable){
				if(v.getSymbId().equals(symbId)){
					return (DerivativeVariable) v;
				}
			}
		}
		return null;
	}
	
	VariableDefinition fetchVariable(String symbId){
		for(CommonVariableDefinition v : variables){
			if(v instanceof VariableDefinition){
				if(v.getSymbId().equals(symbId)){
					return (VariableDefinition) v;
				}
			}
		}
		return null;
	}
	
	DerivativeVariable transformToDerivativeVariable(VariableDefinition v){
		DerivativeVariable dv = new DerivativeVariable(v.getSymbId(), v.getSymbolType());
		variables.remove(v);
		variables.add(dv);
		return dv;
	}
	
	class VariableName {
		private String prefix;
		private Integer index;
		
		public VariableName(String prefix, Integer index) {
			this.prefix = prefix;
			this.index = index;
		}
		
		String getPrefix(){
			return prefix;
		}
		
		Integer getIndex(){
			return index;
		}
	}
}
