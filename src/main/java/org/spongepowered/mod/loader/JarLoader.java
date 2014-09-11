/**
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2014 SpongePowered <http://spongepowered.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.loader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


/**
 * Loads jars dynamically at runtime
 */
public class JarLoader
{
    
    /**
     * The classloader used for this jar
     */
    URLClassLoader child;
    
    /**
     * Creates a new instance of JarLoader
     * @param loadJar The jar to load into the class
     * @throws MalformedURLException There was an error with the file you sent
     */
    public JarLoader(File loadJar) throws MalformedURLException {
        child = new URLClassLoader (new URL[]{loadJar.toURI().toURL()}, this.getClass().getClassLoader());
    }
    
    /**
     * Gets a class from this jar
     * @param qualifiedClassName The fully qualified class name of the class to load
     * @return The class in the jar with the given name
     * @throws InstantiationException There was an issue creating an instance of the class such as there being no 0 param constructors
     * @throws IllegalAccessException There was an issue creating an instance of the class such as it being private
     * @throws ClassNotFoundException The class attempting to be loaded does not exist
     */
    public ClassWrapper getClass(String qualifiedClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return new ClassWrapper(qualifiedClassName);
    }
    
    /**
     * Provides as a wrapper for a class loaded by the JarLoader
     */
    public class ClassWrapper {
        
        /**
         * The instance of this class being wrapped
         */
        Object classInstance;
        
        /**
         * The class being wrapped around
         */
        Class<?> wrappedClass;
        
        /**
         * The qualified name of the class being wrapped
         */
        String fullClass;
        
        /**
         * @param fullclass The qualified class name to load
         * @throws InstantiationException There was an issue creating an instance of the class such as there being no 0 param constructors
         * @throws IllegalAccessException There was an issue creating an instance of the class such as it being private
         * @throws ClassNotFoundException The class attempting to be loaded does not exist
         */
        ClassWrapper(String fullclass) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
            wrappedClass = Class.forName(fullclass, true, child);
            classInstance = wrappedClass.newInstance();
            fullClass = fullclass;
        }
        
        /**
         * @return The instance loaded by this class wrapper
         */
        public Object getInstance() {
            return classInstance;
        }
        
        /**
         * @return The class being wrapped by this
         */
        public Class<?> getWrappedClass() {
            return wrappedClass;
        }
        
        /**
         * @return The fully qualified name of the wrapped class
         */
        public String getClassName() {
            return fullClass;
        }
        
        /**
         * @param name The name of the method to get
         * @param parameterTypes The types of the parameters (int, object, etc)
         * @return The method with the given name and parameters
         * @throws NoSuchMethodException The method trying to be gotten does not exists
         * @throws SecurityException You do not have permission to access this method
         */
        public Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
            return wrappedClass.getMethod(name, parameterTypes);
        }
        
        /**
         * @param name The name of the method to call
         * @param args The method arguments
         * @return The return value  of the method (may be null if method is void)
         * @throws IllegalAccessException You do not have permission to access this
         * @throws IllegalArgumentException You put in invalid arguments
         * @throws InvocationTargetException This is an internal error, the class must not have loaded
         * @throws NoSuchMethodException That method does not exist
         * @throws SecurityException You do not have permission to access that method
         */
        public Object invokeMethod(String name, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
            Class<?>[] parameterTypes = new Class<?>[args.length];
            for(int i = 0; i < args.length; i++) parameterTypes[i] = args[i].getClass();
            return getMethod(name, parameterTypes).invoke(classInstance, args);
        }
        
        /**
         * Creates a clone of this class currently being accessed. A new class instance is used, so values set will not carry over
         * @return A clone of this class
         * @throws ClassNotFoundException The class you're trying to clone doesn't exist
         * @throws InstantiationException This class failed to instantiate
         * @throws IllegalAccessException You do not have permission to access that class
         */
        public ClassWrapper newInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
            return new ClassWrapper(fullClass);
        }
    }
}
