package eu.ddmore.libpharmml.pkmacro.translation;

import java.util.Hashtable;
import java.util.Map;

import eu.ddmore.libpharmml.dom.commontypes.Rhs;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.MacroValue;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

/**
 * Class for helping to fetch paramaters in macros
 *
 */
class ParamResolver {
	
	private final Map<String,Rhs> data;
	private final String macroName;

	ParamResolver(PKMacro macro) throws InvalidMacroException {
		data = new Hashtable<String,Rhs>();
		for(MacroValue value : macro.getListOfValue()){
			if(value.getArgument() != null){
				if(value.getAssign() == null){
					throw new InvalidMacroException(value.getArgument());
				}
				data.put(value.getArgument(), value.getAssign());
			} else {
				if(value.getSymbRef() == null){
					throw new InvalidMacroException(
							"A macro parameter should either have an argument name "
							+ "or be defined by a symbol reference.");
				}
				data.put(value.getSymbRef().getSymbIdRef(), new Rhs(value.getSymbRef()));
			}
		}
		macroName = macro.getName();
	}
	
	Rhs getValue(String argument) throws InvalidMacroException{
		if(!data.containsKey(argument) || data.get(argument) == null){
			throw new InvalidMacroException("Argument \""+argument+"\" is undefined in \""+macroName+"\".");
		}
		return data.get(argument);
	}
	
	@SuppressWarnings("unchecked")
	<T> T getValue(String argument, Class<T> clazz) throws InvalidMacroException{
		Rhs rhs_value = getValue(argument);
		if(clazz.isInstance(rhs_value.getContent())){
			return (T) rhs_value.getContent();
		} else {
			throw new InvalidMacroException(argument + " must be defined as a "+ clazz +".");
		}
	}
	
	Rhs getValue(Object argument) throws InvalidMacroException{
		return getValue(argument.toString());
	}
	
	<T> T getValue(Object argument, Class<T> clazz) throws InvalidMacroException{
		return getValue(argument.toString(), clazz);
	}
	
	boolean contains(String argument){
		return data.containsKey(argument);
	}
	
	boolean contains(Object argument){
		return data.containsKey(argument.toString());
	}
}