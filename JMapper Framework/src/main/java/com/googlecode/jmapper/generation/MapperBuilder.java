/**
 * Copyright (C) 2012 - 2016 Alessandro Vurro.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.jmapper.generation;

import static com.googlecode.jmapper.generation.MapperGenerator.generateMapperClass;
import static com.googlecode.jmapper.util.ClassesManager.mapperClassName;
import static com.googlecode.jmapper.util.GeneralUtility.isNull;
import static com.googlecode.jmapper.xml.XmlBuilder.loadXml;

import java.util.HashSet;
import java.util.Set;

import com.googlecode.jmapper.Mapper;
import com.googlecode.jmapper.config.Error;
import com.googlecode.jmapper.enums.ChooseConfig;
import com.googlecode.jmapper.generation.beans.Method;

/**
 * MapperBuilder increases the readability of the code to retrieve the
 * parameters necessary to create or find a new IMapper class. <br>
 * For example:
 * 
 * <pre>
 * <code>from(source).to(destination)
 *      .analyzing(config)
 *      .presentIn(xmlPath)</code>
 * </pre>
 * 
 * @author Alessandro Vurro
 */
@SuppressWarnings("unchecked")
public class MapperBuilder {

	/** @return true if the mapper class exists, false otherwise */
	public boolean exist() {
		
		try {
			// it's important to find the mapper in the same class Loader of the app
			destination.getClassLoader().loadClass(mapperClassName(destination, source, path));
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * @param <D> Destination Class
	 * @param <S> Source Class
	 * @return the existent mapper class */
	public <D, S> Class<Mapper<D, S>> get() {
		
		try {
			// it's important to find the mapper in the same class Loader of the app
			return (Class<Mapper<D, S>>) destination.getClassLoader()
					.loadClass(mapperClassName(destination, source, path));
		} catch (Exception e) {
			return null;
		}
	}

	/** 
	 * @param <D> Destination Class
	 * @param <S> Source Class
	 * @return the generated mapper class 
	 * @throws Throwable any error made ​​by the generation of the mapper
	 * */
	public <D, S> Class<Mapper<D, S>> generate() throws Throwable {
		
		// the dynamic methods are treated differently from normal methods in the mapping.
		// a reference to this list is passed to the MapperConstructor and filled recursively
		Set<Method> dynamicMethodsToWrite = new HashSet<Method>();

		MapperConstructor mapperConstructor = new MapperConstructor(destination, source, config,loadXml(path).atRuntime(), dynamicMethodsToWrite)
	       											 .setMapperName(mapperClassName(destination, source, path));
	    
		// the dynamic methods are written and added directly to the Mapper Class
		Class<Mapper<D, S>> mapperClass = (Class<Mapper<D, S>>) generateMapperClass(destination.getClassLoader(), mapperConstructor, dynamicMethodsToWrite);

		try {
			// a new instance is created to check the mapper implementation
			mapperClass.newInstance();
		} catch (Throwable e) {
			if (isNull(path))	Error.illegalCode(destination, source, e);
			else				Error.illegalCode(destination, source, path, e);
		}
		return mapperClass;
	}

	/** destination class */
	private Class<?> destination;
	/** source class */
	private Class<?> source;
	/** configuration chosen from user */
	private ChooseConfig config;
	/** path of the xml file */
	private String path;

	/**
	 * Starting point for parameters setting. This method set the source
	 * instance variable.
	 * 
	 * @param source
	 *            source to set
	 * @return a new instance of MapperBuilder
	 */
	public static MapperBuilder from(Class<?> source) {
		return new MapperBuilder(source);
	}

	/**
	 * This method set the destination instance variable.
	 * 
	 * @param destination
	 *            destination to set
	 * @return this instance of MapperBuilder
	 */
	public MapperBuilder to(Class<?> destination) {
		this.destination = destination;
		return this;
	}

	/**
	 * This method set the config instance variable.
	 * 
	 * @param config
	 *            configuration to set
	 * @return this instance of MapperBuilder
	 */
	public MapperBuilder analyzing(ChooseConfig config) {
		this.config = config;
		return this;
	}

	/**
	 * This method set the xmlPath instance variable.
	 * 
	 * @param xmlPath
	 *            xml path to set
	 * @return this instance of MapperBuilder
	 */
	public MapperBuilder presentIn(String xmlPath) {
		this.path = xmlPath;
		return this;
	}

	/**
	 * This is the entry point of MapperBuilder.
	 * 
	 * @param source
	 *            source to set
	 */
	private MapperBuilder(Class<?> source) {
		this.source = source;
	}
}
