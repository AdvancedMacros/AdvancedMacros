/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
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
******************************************************************************/
package org.luaj.vm2_v3_0_1.lib.jse;


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.luaj.vm2_v3_0_1.Globals;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.compiler.LuaC;
import org.luaj.vm2_v3_0_1.lib.LibFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

/** 
 * Subclass of {@link LibFunction} which implements the features of the luajava package. 
 * <p> 
 * Luajava is an approach to mixing lua and java using simple functions that bind 
 * java classes and methods to lua dynamically.  The API is documented on the 
 * <a href="http://www.keplerproject.org/luajava/">luajava</a> documentation pages.
 * 
 * <p>
 * Typically, this library is included as part of a call to 
 * {@link org.luaj.vm2_v3_0_1.lib.jse.JsePlatform#standardGlobals()}
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * System.out.println( globals.get("luajava").get("bindClass").call( LuaValue.valueOf("java.lang.System") ).invokeMethod("currentTimeMillis") );
 * } </pre>
 * <p>
 * To instantiate and use it directly, 
 * link it into your globals table via {@link Globals#load} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.load(new PackageLib());
 * globals.load(new LuajavaLib());
 * globals.load( 
 *      "sys = luajava.bindClass('java.lang.System')\n"+
 *      "print ( sys:currentTimeMillis() )\n", "main.lua" ).call(); 
 * } </pre>
 * <p>
 * 
 * The {@code luajava} library is available 
 * on all JSE platforms via the call to {@link org.luaj.vm2_v3_0_1.lib.jse.JsePlatform#standardGlobals()}
 * and the luajava api's are simply invoked from lua.  
 * Because it makes extensive use of Java's reflection API, it is not available 
 * on JME, but can be used in Android applications.
 * <p>
 * This has been implemented to match as closely as possible the behavior in the corresponding library in C.
 * 
 * @see LibFunction
 * @see org.luaj.vm2_v3_0_1.lib.jse.JsePlatform
 * @see org.luaj.vm2.lib.jme.JmePlatform
 * @see LuaC
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 * @see <a href="http://www.keplerproject.org/luajava/manual.html#luareference">http://www.keplerproject.org/luajava/manual.html#luareference</a>
 */
public class LuajavaLib extends VarArgFunction {

	static final int INIT           = 0;
	static final int BINDCLASS      = 1;
	static final int NEWINSTANCE	= 2;
	static final int NEW			= 3;
	static final int CREATEPROXY	= 4;
	static final int LOADLIB		= 5;
	static final int GETMETHOD      = 6; //class.x defaults to field if a matching method also exists
	static final int GETFIELD       = 7;
	static final int GETDECLAREDMETHODS=8;
	static final int GETDECLAREDFIELDS =9;
	static final int GETSUPERCLASS  = 10;
	static final int GETINTERFACES  = 11;
	static final int INSTANCEOF     = 12;
	static final int GETINNERCLASSES= 13;
	static final int DESCRIBEMETHOD = 14;
	static final int SPLITOVERLOADED =15;


	static final String[] NAMES = {
		"bindClass", 
		"newInstance", 
		"new", 
		"createProxy", 
		"loadLib",
		"getMethod",
		"getField",
		"getDeclaredMethods",
		"getDeclaredFields",
		"getSuperClass",
		"getInterfaces",
		"instanceof",
		"getInnerClasses",
		"describeMethod",
		"splitOverloaded"
	};
	
	static final int METHOD_MODIFIERS_VARARGS = 0x80;

	public LuajavaLib() {
	}

	@Override
	public Varargs invoke(Varargs args) {
		try {
			switch ( opcode ) {
			case INIT: {
				// LuaValue modname = args.arg1();
				LuaValue env = args.arg(2);
				LuaTable t = new LuaTable();
				bind( t, this.getClass(), NAMES, BINDCLASS );
				env.set("luajava", t);
				env.get("package").get("loaded").set("luajava", t);
				return t;
			}
			case BINDCLASS: {
				final Class clazz = classForName(args.checkjstring(1));
				return JavaClass.forClass(clazz);
			}
			case NEWINSTANCE:
			case NEW: {
				// get constructor
				final LuaValue c = args.checkvalue(1); 
				final Class clazz = (opcode==NEWINSTANCE? classForName(c.tojstring()): (Class) c.checkuserdata(Class.class));
				final Varargs consargs = args.subargs(2);
				return JavaClass.forClass(clazz).getConstructor().invoke(consargs);
			}
				
			case CREATEPROXY: {				
				final int niface = args.narg()-1;
				if ( niface <= 0 )
					throw new LuaError("no interfaces");
				final LuaValue lobj = args.checktable(niface+1);
				
				// get the interfaces
				final Class[] ifaces = new Class[niface];
				for ( int i=0; i<niface; i++ ) 
					ifaces[i] = classForName(args.checkjstring(i+1));
				
				// create the invocation handler
				InvocationHandler handler = new ProxyInvocationHandler(lobj);
				
				// create the proxy object
				Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), ifaces, handler);
				
				// return the proxy
				return LuaValue.userdataOf( proxy );
			}
			case LOADLIB: {
				// get constructor
				String classname = args.checkjstring(1);
				String methodname = args.checkjstring(2);
				Class clazz = classForName(classname);
				Method method = clazz.getMethod(methodname, new Class[] {});
				Object result = method.invoke(clazz, new Object[] {});
				if ( result instanceof LuaValue ) {
					return (LuaValue) result;
				} else {
					return NIL;
				}
			}
			case GETMETHOD:{ //TheIncgi until default:
				LuaValue clazz = args.arg1();
				LuaValue m;
				if((clazz instanceof JavaClass)) {
					m = ((JavaClass)clazz).getMethod(args.arg(2));
					if ( m != null )
						return m;
				}else if(clazz instanceof JavaInstance) {
					JavaInstance ji = (JavaInstance)clazz;
					if(ji.jclass == null) {
							ji.jclass = JavaClass.forClass(ji.m_instance.getClass());
					}
					m = ji.jclass.getMethod(args.arg(2));
					if(m!=null) return m;
				}
				return NIL;
				
			}
			case GETFIELD:{ //TODO test me
				LuaValue clazz = args.arg1();
				LuaValue m;
				if((clazz instanceof JavaClass)) {
					m = CoerceJavaToLua.coerce(((JavaClass)clazz).getField(args.arg(2)).get(clazz));
					if ( m != null )
						return m;
				}else if(clazz instanceof JavaInstance) {
					JavaInstance ji = (JavaInstance)clazz;
					if(ji.jclass == null) {
							ji.jclass = JavaClass.forClass(ji.m_instance.getClass());
					}
					m = CoerceJavaToLua.coerce(ji.jclass.getField(args.arg(2)).get(ji));
					if(m!=null) return m;
				}
				return NIL;
			}
			case GETDECLAREDMETHODS:{
				LuaValue clazz = args.arg1();
				LuaTable out = new LuaTable();
				HashMap<String, ArrayList<JavaMethod>> namedLists = new HashMap<>();
				if(clazz instanceof JavaInstance) {
					JavaInstance ji = (JavaInstance)clazz;
					if(ji.jclass == null) 
							ji.jclass = JavaClass.forClass(ji.m_instance.getClass());
					clazz = ji.jclass;
				}
				if((clazz instanceof JavaClass)) {
					JavaClass jc = (JavaClass) clazz;
					if(jc.m_instance instanceof Class ) {
						Class<?> theClass = (Class<?>) jc.m_instance;
						Method[] m = theClass.getDeclaredMethods();
						for ( int i=0; i<m.length; i++ ) {
							Method mi = m[i];
							String name = mi.getName();
							namedLists.computeIfAbsent(name, e->{return new ArrayList<>();}).add(JavaMethod.forMethod(mi));
							
							try {
								mi.setAccessible(true);
							} catch (SecurityException s) {}
						}
						for(Entry<String, ArrayList<JavaMethod>> e : namedLists.entrySet()) {
							out.set(e.getKey(), e.getValue().size() > 1?
									new JavaMethod.Overload(e.getValue().toArray(new JavaMethod[e.getValue().size()])) :
									e.getValue().get(0)
									);
						}
					}
				}
				return out;
			}
			case GETDECLAREDFIELDS:{
				LuaValue clazz = args.arg1();
				LuaTable out = new LuaTable();
				
				if(clazz instanceof JavaInstance) {
					JavaInstance ji = (JavaInstance)clazz;
					if(ji.jclass == null) 
							ji.jclass = JavaClass.forClass(ji.m_instance.getClass());
					clazz = ji.jclass;
				}
				if((clazz instanceof JavaClass)) {
					JavaClass jc = (JavaClass) clazz;
					if(jc.m_instance instanceof Class ) {
						Class<?> theClass = (Class<?>) jc.m_instance;
						Field[] fs = theClass.getDeclaredFields();
						for ( int i=0; i<fs.length; i++ ) {
							Field f = fs[i];
							String name = f.getName();
							out.set(out.length()+1,name);
							try {
								f.setAccessible(true);
							} catch (SecurityException s) {}
						}
					}
				}
				return out;
			}
			case GETSUPERCLASS:{
				LuaValue clazz = args.arg1();
				
				if(clazz instanceof JavaInstance) {
					JavaInstance ji = (JavaInstance)clazz;
					if(ji.jclass == null) 
							ji.jclass = JavaClass.forClass(ji.m_instance.getClass());
					clazz = ji.jclass;
				}
				if((clazz instanceof JavaClass)) {
					JavaClass jc = (JavaClass) clazz;
					if(jc.m_instance instanceof Class ) {
						return JavaClass.forClass( ((Class<?>)jc.m_instance).getSuperclass() );
					}
				}
				return NIL;
			}
			case GETINTERFACES:{
				LuaValue clazz = args.arg1();
				LuaTable out = new LuaTable();
				
				if(clazz instanceof JavaInstance) {
					JavaInstance ji = (JavaInstance)clazz;
					if(ji.jclass == null) 
							ji.jclass = JavaClass.forClass(ji.m_instance.getClass());
					clazz = ji.jclass;
				}
				if((clazz instanceof JavaClass)) {
					JavaClass jc = (JavaClass) clazz;
					if(jc.m_instance instanceof Class ) {
						Class<?> theClass = (Class<?>)jc.m_instance;
						Class<?>[] interfaces = theClass.getInterfaces();
						for(int i = 0; i<interfaces.length; i++)
							out.set(i+1, JavaClass.forClass(interfaces[i]));
					}
				}
				return out;
			}
			case INSTANCEOF:{
				LuaValue clazz2 = args.arg(2);
				LuaValue ji1    = args.arg(1);

				JavaInstance ji;
				if(ji1 instanceof JavaInstance) {
					ji = (JavaInstance)ji1;
					if(ji.jclass == null) 
							ji.jclass = JavaClass.forClass(ji.m_instance.getClass());
				}else {
					throw new LuaError("Arg 1 must be instance of java object");
				}
				JavaClass jc2;
				if(clazz2 instanceof JavaInstance) {
					jc2 = (JavaClass) clazz2;
					if(jc2.m_instance instanceof Class) {
						return valueOf(((Class<?>)jc2.m_instance).isInstance( ji ));
					}else{
						throw new LuaError("Arg 2 must be an instance of a java class");
					}
				}else {
					throw new LuaError("Arg 2 must be an instance of a java class");
				}
			}
			case GETINNERCLASSES:{
				LuaValue clazz = args.arg1();
				LuaTable out = new LuaTable();
				
				if(clazz instanceof JavaInstance) {
					JavaInstance ji = (JavaInstance)clazz;
					if(ji.jclass == null) 
							ji.jclass = JavaClass.forClass(ji.m_instance.getClass());
					clazz = ji.jclass;
				}
				if((clazz instanceof JavaClass)) {
					JavaClass jc = (JavaClass) clazz;
					if(jc.m_instance instanceof Class ) {
						Class<?> theClass = (Class<?>)jc.m_instance;
						Class<?>[] innerClasses = theClass.getDeclaredClasses();
						for(int i = 0; i<innerClasses.length; i++)
							out.set(i+1, JavaClass.forClass(innerClasses[i]));
					}
				}
				return out;
			}
			case DESCRIBEMETHOD:{
				LuaValue arg1 = args.arg1();
				if(arg1 instanceof JavaMethod.Overload) {
					JavaMethod.Overload ol = (JavaMethod.Overload)arg1;
					LuaTable out = new LuaTable();
					for(int i = 0; i<ol.methods.length; i++)
						out.set(i+1, getMethodDescriptor(ol.methods[i].method));
					return out;
				}else if(arg1 instanceof JavaMethod) {
					JavaMethod jm = (JavaMethod) arg1;
					return valueOf(getMethodDescriptor(jm.method));
				}else
					throw new LuaError("Expected java method, got "+arg1.typename());
			}
			case SPLITOVERLOADED:{
				LuaValue arg1 = args.arg1();
				if(arg1 instanceof JavaMethod.Overload) {
					JavaMethod.Overload ol = (JavaMethod.Overload)arg1;
					LuaTable out = new LuaTable();
					for (int i = 0; i < ol.methods.length; i++) {
						out.set(i+1, ol.methods[i]);
					}
					return out;
				}else if(arg1 instanceof JavaMethod)
					return arg1;
				throw new LuaError("Expected overloaded java method, got "+arg1.typename());
			}
			default:
				throw new LuaError("not yet supported: "+this);
			}
		} catch (LuaError e) {
			throw e;
		} catch (InvocationTargetException ite) {
			throw new LuaError(ite.getTargetException());
		} catch (Exception e) {
			throw new LuaError(e);
		}
	}

	// load classes using app loader to allow luaj to be used as an extension
	protected Class classForName(String name) throws ClassNotFoundException {
		return Class.forName(name);//Class.forName(name, true, ClassLoader.getSystemClassLoader());
	}
	
	private static final class ProxyInvocationHandler implements InvocationHandler {
		private final LuaValue lobj;

		private ProxyInvocationHandler(LuaValue lobj) {
			this.lobj = lobj;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String name = method.getName();
			LuaValue func = lobj.get(name);
			if ( func.isnil() )
				return null;
			boolean isvarargs = ((method.getModifiers() & METHOD_MODIFIERS_VARARGS) != 0);
			int n = args!=null? args.length: 0; 
			LuaValue[] v;
			if ( isvarargs ) {								
				Object o = args[--n];
				int m = Array.getLength( o );
				v = new LuaValue[n+m];
				for ( int i=0; i<n; i++ )
					v[i] = CoerceJavaToLua.coerce(args[i]);
				for ( int i=0; i<m; i++ )
					v[i+n] = CoerceJavaToLua.coerce(Array.get(o,i));								
			} else {
				v = new LuaValue[n];
				for ( int i=0; i<n; i++ )
					v[i] = CoerceJavaToLua.coerce(args[i]);
			}
			LuaValue result = func.invoke(v).arg1();
			return CoerceLuaToJava.coerce(result, method.getReturnType());
		}
	}
	
	//https://stackoverflow.com/a/32155781
	static String getDescriptorForClass(final Class c)
	{
	    if(c.isPrimitive())
	    {
	        if(c==byte.class)
	            return "B";
	        if(c==char.class)
	            return "C";
	        if(c==double.class)
	            return "D";
	        if(c==float.class)
	            return "F";
	        if(c==int.class)
	            return "I";
	        if(c==long.class)
	            return "J";
	        if(c==short.class)
	            return "S";
	        if(c==boolean.class)
	            return "Z";
	        if(c==void.class)
	            return "V";
	        throw new RuntimeException("Unrecognized primitive "+c);
	    }
	    if(c.isArray()) return c.getName().replace('.', '/');
	    return ('L'+c.getName()+';').replace('.', '/');
	}

	static String getMethodDescriptor(Method m)
	{
 	    String s="(";
	    for(final Class c:(m.getParameterTypes()))
	        s+=getDescriptorForClass(c);
	    s+=')';
	    return s+getDescriptorForClass(m.getReturnType());
	}
	
}