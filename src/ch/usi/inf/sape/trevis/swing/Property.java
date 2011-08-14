/*
 * This file is licensed to You under the "Simplified BSD License".
 * You may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/bsd-license.php
 *
 * See the COPYRIGHT file distributed with this work for information
 * regarding copyright ownership.
 */
package ch.usi.inf.sape.trevis.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.usi.inf.sape.trevis.model.attribute.BooleanAttribute;
import ch.usi.inf.sape.trevis.model.attribute.DoubleAttribute;
import ch.usi.inf.sape.trevis.model.attribute.LongAttribute;
import ch.usi.inf.sape.trevis.model.attribute.StringAttribute;


/**
 * A Property has a unique key, a descriptive name, and contains a value of a given type.
 * Properties are used to configure TreeView visualizations.
 * A TreeView refers to a Configuration, which is a set of Properties,
 * to determine its visual representation.
 * 
 * A Property can be of a scalar or a vector type.
 * Vector types are specified as array types (e.g., String[]).
 * A scalar type, or the component type of a vector type,
 * is specified as a Java class (e.g., String or LongAttribute).
 * A scalar type, or the component type of a vector type,
 * representing primitive Java types (e.g., int) 
 * have to be specified as their wrapper classes (e.g., Integer).
 * 
 * Example usage:
 * 
 * <code>
 * Property ip = new Property("STUDENT_AGE", "age", Integer.class, 1);
 * ip.setInt(5);
 * int v = ip.getInt();
 * 
 * Property sp = new Property("STUDENT_NAME", "name", String.class, "Jim");
 * sp.setString("John");
 * String sp = p.getString();
 * 
 * Property ivp = new Property("STUDENT_GRADES", "grades", Integer[].class, new Integer[] {1, 2, 3});
 * List<Integer> iv = ivp.getInts();
 * ivp.clear();
 * ivp.addInt(6);
 * ivp.addInt(5);
 * 
 * Property svp = new Property("STUDENT_ALIASES", "aliases", String[].class, new String[] {"Honza", "Jan"});
 * List<String> sv = svp.getStrings();
 * svp.clear();
 * svp.addString("Matt");
 * svp.addString("Matthew");
 * </code>
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class Property {

	private final String key;
	private final String name;
	private final Class<?> type;
	private Object value;
	private final ArrayList<PropertyListener> listeners;
	
	
	public Property(final String key, final String name, final Class<?> type, final Object initialValue) {
		this.key = key;
		this.name = name;
		this.type = type;
		if (initialValue==null) {
			throw new IllegalArgumentException("Initial value of property '"+key+"' must not be null");
		}
		if (isVector()) {
			if (!initialValue.getClass().isArray()) {
				throw new IllegalArgumentException("Initial value of property '"+key+"' must be a vector");			
			}
			if (type.getComponentType()!=initialValue.getClass().getComponentType()) {
				throw new IllegalArgumentException("Initial value of property '"+key+"' must be a vector of '"+type.getComponentType());
			}
			value = new ArrayList<Object>(Arrays.asList((Object[])initialValue));
			/*
			System.out.println("Initial value of vector property '"+name+"': ");
			System.out.println("  value: "+value);
			System.out.println("  size: "+((ArrayList)value).size());
			if (((ArrayList)value).size()>0) {
				System.out.println("  e0: "+((ArrayList)value).get(0));
			}
			*/
		} else {
			if (initialValue.getClass().isArray()) {
				throw new IllegalArgumentException("Initial value of property '"+key+"' must be a scalar");			
			}
			ensureCompatibleScalarType(initialValue);
			value = initialValue;			
		}
		listeners = new ArrayList<PropertyListener>();
	}
	
	public String getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isVector() {
		return type.isArray();
	}
	
	public Class<?> getType() {
		return type;
	}

	
	//--- type checking
	private void ensureScalar() {
		if (isVector()) {
			throw new IllegalStateException("Accessing vector property '"+key+"' as a scalar");
		}		
	}
	
	private void ensureScalarType(final Class<?> accessType) {
		ensureScalar();
		if (!type.isAssignableFrom(accessType)) {
			throw new IllegalStateException("Accessing scalar property '"+key+"' of type '"+type.getSimpleName()+"' as a '"+accessType.getSimpleName()+"'");
		}
	}

	private void ensureCompatibleScalarType(final Object value) {
		ensureScalar();
		if (!type.isInstance(value)) {
			throw new IllegalStateException("Storing a value of type '"+value.getClass().getSimpleName()+"' into scalar property '"+key+"' of type '"+type.getSimpleName()+"'");
		}
	}

	private void ensureVector() {
		if (!isVector()) {
			throw new IllegalStateException("Accessing scalar property '"+key+"' as a vector");
		}
	}
	
	private void ensureVectorType(final Class<?> accessType) {
		ensureVector();
		if (!type.getComponentType().isAssignableFrom(accessType.getComponentType())) {
			throw new IllegalStateException("Accessing vector property '"+key+"' of type '"+type.getSimpleName()+"' as a '"+accessType.getSimpleName()+"'");
		}
	}

	private void ensureCompatibleVectorType(final Object value) {
		ensureVector();
		if (!type.getComponentType().isInstance(value)) {
			throw new IllegalStateException("Storing a value of type '"+value.getClass().getSimpleName()+"' into vector property '"+key+"' of type '"+type.getSimpleName()+"'");
		}
	}
		
	
	//--- scalar accessors
	// generic getter and setter
	public Object getValue() {
		ensureScalar();
		return value;
	}
	
	public void setValue(final Object value) {
		ensureCompatibleScalarType(value);
		this.value = value;
		firePropertyChanged();
	}
	
	// typed getters and setters
	public int getInt() {
		ensureScalarType(Integer.class);
		return (Integer)value;
	}
	
	public void setInt(final int value) {
		ensureScalarType(Integer.class);
		this.value = value;
		firePropertyChanged();
	}
	
	public long getLong() {
		ensureScalarType(Long.class);
		return (Long)value;
	}
	
	public void setLong(final long value) {
		ensureScalarType(Long.class);
		this.value = value;
		firePropertyChanged();
	}
	
	public double getDouble() {
		ensureScalarType(Double.class);
		return (Double)value;
	}

	public void setDouble(final double value) {
		ensureScalarType(Double.class);
		this.value = value;
		firePropertyChanged();
	}

	public boolean getBoolean() {
		ensureScalarType(Boolean.class);
		return (Boolean)value;
	}
	
	public void setBoolean(final boolean value) {
		ensureScalarType(Boolean.class);
		this.value = value;
		firePropertyChanged();
	}

	public String getString() {
		ensureScalarType(String.class);
		return (String)value;
	}
	
	public void setString(final String value) {
		ensureScalarType(String.class);
		this.value = value;
		firePropertyChanged();
	}
	
	public LongAttribute getLongAttribute() {
		ensureScalarType(LongAttribute.class);
		return (LongAttribute)value;
	}
	
	public void setLongAttribute(final LongAttribute value) {
		ensureScalarType(LongAttribute.class);
		this.value = value;
		firePropertyChanged();
	}

	public DoubleAttribute getDoubleAttribute() {
		ensureScalarType(DoubleAttribute.class);
		return (DoubleAttribute)value;
	}
	
	public void setDoubleAttribute(final DoubleAttribute value) {
		ensureScalarType(DoubleAttribute.class);
		this.value = value;
		firePropertyChanged();
	}

	public BooleanAttribute getBooleanAttribute() {
		ensureScalarType(BooleanAttribute.class);
		return (BooleanAttribute)value;
	}
	
	public void setBooleanAttribute(final BooleanAttribute value) {
		ensureScalarType(BooleanAttribute.class);
		this.value = value;
		firePropertyChanged();
	}

	public StringAttribute getStringAttribute() {
		ensureScalarType(StringAttribute.class);
		return (StringAttribute)value;
	}
	
	public void setStringAttribute(final StringAttribute value) {
		ensureScalarType(StringAttribute.class);
		this.value = value;
		firePropertyChanged();
	}


	//--- vector accessors
	// type-independent methods
	public int getLength() {
		ensureVector();
		if (value==null) {
			return 0;
		}
		return ((ArrayList<Object>)value).size();
	}
	
	public void clear() {
		ensureVector();
		value = new ArrayList<Object>();
		firePropertyChanged();
	}
	
	// generic getter and adder
	public List<Object> getValues() {
		ensureVector();
		return (ArrayList<Object>)value;
	}
	
	public void addValue(final Object element) {
		ensureCompatibleVectorType(element);
		((List<Object>)value).add(element);
		firePropertyChanged();
	}
	
	// typed getters and adders
	public List<Integer> getInts() {
		ensureVectorType(Integer[].class);
		return (List<Integer>)value;
	}
	
	public void addInt(final int element) {
		ensureVectorType(Integer[].class);
		((List<Integer>)value).add(element);
		firePropertyChanged();
	}
	
	public List<Long> getLongs() {
		ensureVectorType(Long[].class);
		return (List<Long>)value;
	}
	
	public void addLong(final long element) {
		ensureVectorType(Long[].class);
		((List<Long>)value).add(element);
		firePropertyChanged();
	}
	
	public List<Double> getDoubles() {
		ensureVectorType(Double[].class);
		return (List<Double>)value;
	}

	public void addDouble(final double element) {
		ensureVectorType(Double[].class);
		((List<Double>)value).add(element);
		firePropertyChanged();
	}

	public List<Boolean> getBooleans() {
		ensureVectorType(Boolean[].class);
		return (List<Boolean>)value;
	}
	
	public void addBoolean(final boolean element) {
		ensureVectorType(Boolean[].class);
		((List<Boolean>)value).add(element);
		firePropertyChanged();
	}

	public List<String> getStrings() {
		ensureVectorType(String[].class);
		return (List<String>)value;
	}
	
	public void addString(final String element) {
		ensureVectorType(String[].class);
		((List<String>)value).add(element);
		firePropertyChanged();
	}
	
	public List<LongAttribute> getLongAttributes() {
		ensureVectorType(LongAttribute[].class);
		return (List<LongAttribute>)value;
	}
	
	public void addLongAttribute(final LongAttribute element) {
		ensureVectorType(LongAttribute[].class);
		((List<LongAttribute>)value).add(element);
		firePropertyChanged();
	}
	
	public List<DoubleAttribute> getDoubleAttributes() {
		ensureVectorType(DoubleAttribute[].class);
		return (List<DoubleAttribute>)value;
	}
	
	public void addDoubleAttribute(final DoubleAttribute element) {
		ensureVectorType(DoubleAttribute[].class);
		((List<DoubleAttribute>)value).add(element);
		firePropertyChanged();
	}
	
	public List<BooleanAttribute> getBooleanAttributes() {
		ensureVectorType(BooleanAttribute[].class);
		return (List<BooleanAttribute>)value;
	}
	
	public void addBooleanAttribute(final BooleanAttribute element) {
		ensureVectorType(BooleanAttribute[].class);
		((List<BooleanAttribute>)value).add(element);
		firePropertyChanged();
	}
	
	public List<StringAttribute> getStringAttributes() {
		ensureVectorType(StringAttribute[].class);
		return (List<StringAttribute>)value;
	}
	
	public void addStringAttribute(final StringAttribute element) {
		ensureVectorType(StringAttribute[].class);
		((List<StringAttribute>)value).add(element);
		firePropertyChanged();
	}
	
	
	//--- debug
	public void dump() {
		System.out.println("Key:   "+key);
		System.out.println("Name:  "+name);
		System.out.println("Type:  "+type);
		System.out.println("Value: "+value);
	}
	
	
	//--- listener management
	public void addPropertyListener(final PropertyListener li) {
		listeners.add(li);
	}
	
	public void removePropertyListener(final PropertyListener li) {
		listeners.remove(li);
	}
	
	protected final void firePropertyChanged() {
		for (final PropertyListener li : listeners) {
			li.propertyChanged(this);
		}
	}
	
}
