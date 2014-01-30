/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/

package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.Bundle;
import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

/**
 * Test code for the type checker
 * 
 * @author Steven Arzt
 */
public class TypeTestCode {
	
	public void typeTest1() {
		String tainted = TelephonyManager.getDeviceId();
		
		Object obj = (Object) tainted;
		String newStr = obj.toString();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(newStr);
	}
	
	private class A {
		String data;
		String data2;
		
		public A() {
			
		}
		
		@SuppressWarnings("unused")
		public A(String data) {
			this.data = data;
		}
		
		String bar() {
			return this.data;
		}
		
		void leak() {
			ConnectionManager cm = new ConnectionManager();
			cm.publish("A: " + data);
		}
		
		@Override
		public String toString() {
			return "data: " + data + ", data2: " + data2;
		}
	}
	
	private class B extends A {
		String foo() {
			return this.data;
		}
		
		@Override
		void leak() {
			ConnectionManager cm = new ConnectionManager();
			cm.publish("B: " + data);
		}
	}
	
	private class B2 extends A {
		@Override
		void leak() {
			ConnectionManager cm = new ConnectionManager();
			cm.publish("B2: " + data);
		}
	}
	
	private class C {
		String data;
	}
	
	public void classCastTest1() {
		String tainted = TelephonyManager.getDeviceId();
		B b = new B();
		b.data = tainted;
		
		A a = (A) b;
		String newStr = a.bar();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(newStr);
	}

	public void classCastTest2() {
		String tainted = TelephonyManager.getDeviceId();
		B b = new B();
		b.data = tainted;
		
		A a = (A) b;
		B b2 = (B) a;
		String newStr = b2.foo();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(newStr);
	}

	public void classCastTest3() {
		String tainted = TelephonyManager.getDeviceId();
		B b = new B();
		b.data = tainted;
		
		A a = (A) b;
		B b2 = (B) a;
		String newStr = b2.bar();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(newStr);
	}

	public void instanceofTest1() {
		String tainted = TelephonyManager.getDeviceId();
		
		A a;
		if (tainted.startsWith("x"))
			a = new A();
		else
			a = new B();
		a.data = tainted;

		ConnectionManager cm = new ConnectionManager();
		if (a instanceof A)
			cm.publish(a.bar());
		else if (a instanceof B)
			cm.publish(((B) a).foo());
		else {
			Object o = (Object) a;
			C c = (C) o;
			cm.publish(c.data);
		}
	}
	
	private void callIt(A a) {
		a.leak();
	}
	
	public void callTargetTest1() {
		A b2 = new B2();
		callIt(b2);
		b2.data = TelephonyManager.getDeviceId();
		
		A b = new B();
		b.data = TelephonyManager.getDeviceId();
		callIt(b);
	}
	
	public void arrayObjectCastTest() {
		Object obj = Bundle.get("foo");
		A foo2[] = (A[]) obj;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(foo2[0].data);
	}

	public void arrayObjectCastTest2() {
		Object obj = Bundle.get("foo");
		A foo2[] = (A[]) obj;
		obj = foo2[0];
		A a = (A) obj;
		a.data2 = a.data;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(a.data);
	}
	
	public void callTypeTest() {
		String[] x = new String[1];
		x[0] = TelephonyManager.getDeviceId();
		objArgFunction(x);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(x[0]);
	}
	
	private void objArgFunction(Object[] x) {
		System.out.println(x);
	}

	public void callTypeTest2() {
		String[] x = new String[1];
		objArgFunction2(x);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(x[0]);
	}
	
	private void objArgFunction2(Object[] x) {
		x[0] = TelephonyManager.getDeviceId();
	}
	
	public void arrayCastAndAliasTest() {
		String[] x = new String[1];
		Object y = x;
		x[0] = TelephonyManager.getDeviceId();
		Object obj = y;
		String[] out = (String[]) obj;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(out[0]);
	}
	
	public void arrayCastAndAliasTest2() {
		String[] x = new String[1];
		Object e = x;
		Object a = (Object) e;
		Object z = a;
		Object y = z;
		x[0] = TelephonyManager.getDeviceId();
		Object obj = y;
		String[] out = (String[]) obj;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(out[0]);
	}

	public void arrayIncompatibleCastAndAliasTest() {
		String[] x = new String[1];
		Object e = x;
		String a = (String) e;
		Object a2 = a;
		String[] z = (String[]) a2;
		Object y = z;
		z[0] = TelephonyManager.getDeviceId();
		Object obj = y;
		String[] out = (String[]) obj;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(out[0]);
	}
	
	private static String[] aci_x;
	private static Object aci_e;
	private static String aci_a;
	private static Object aci_a2;
	private static String[] aci_z;
	private static Object aci_y;
	private static Object aci_obj;
	private static String[] aci_out;
	
	public void arrayIncompatibleCastAndAliasTest2() {
		aci_x = new String[1];
		aci_e = aci_x;
		aci_a = (String) aci_e;
		aci_a2 = aci_a;
		aci_z = (String[]) aci_a2;
		aci_y = aci_z;
		aci_z[0] = TelephonyManager.getDeviceId();
		aci_obj = aci_y;
		aci_out = (String[]) aci_obj;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(aci_out[0]);
	}

	private class X {
		private B b;
		private Object o;
		
		public X() {
			this.b = new B();
		}
	}

	public void fieldIncompatibleCastAndAliasTest() {
		X x = new X();
		x.b.data = TelephonyManager.getDeviceId();
		Object e = x;
		String a = (String) e;
		Object z = a;
		Object y = z;
		X x2 = (X) y; 
		ConnectionManager cm = new ConnectionManager();
		cm.publish(x2.b.data);
	}

	public void twoDimensionArrayTest() {
		String[] x = new String[1];
		Object y = x;
		x[0] = TelephonyManager.getDeviceId();
		Object[] foo = new Object[1];
		foo[0] = y;
		Object bar = foo;
		String[][] out = (String[][]) bar;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(out[0][0]);
	}
	
	public void arrayBackPropTypeTest() {
		Object[] oarr = new Object[2];
		Object odata = new String[] { TelephonyManager.getDeviceId() };
		oarr[0] = odata;
		oarr[1] = TelephonyManager.getDeviceId();
		Object o = oarr[1];
		ConnectionManager cm = new ConnectionManager();
		cm.publish((String) o);
	}
	
	public void arrayBackPropTypeTest2() {
		Object[] oarr = new Object[2];
		Object odata = new String[] { TelephonyManager.getDeviceId() };
		Object foo = odata;
		oarr[0] = odata;
		oarr[1] = TelephonyManager.getDeviceId();
		String[] o = (String[]) foo;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(o[1]);
	}
	
	public void arrayBackPropTypeTest3() {
		Object[] oarr = new Object[2];
		Object foo = oarr;
		Object odata = new String[] { TelephonyManager.getDeviceId() };
		oarr[0] = odata;
		oarr[1] = TelephonyManager.getDeviceId();
		String[] o = (String[]) foo;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(o[1]);
	}
	
	public void arrayBackPropTypeTest4() {
		Object[] oarr = new Object[2];
		Object foo = oarr;
		Object odata = new String[] { TelephonyManager.getDeviceId() };
		String[] sa = (String[]) oarr[1];
		String s = (String) oarr[1];
		System.out.println(s + sa);
		oarr[0] = odata;
		oarr[1] = TelephonyManager.getDeviceId();
		String[] o = (String[]) foo;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(o[1]);
	}

	public void arrayBackPropTypeTest5() {
		Object[] oarr = new Object[2];
		Object foo = oarr;
		Object odata = new String[] { TelephonyManager.getDeviceId() };
		String[] sa = (String[]) oarr[1];
		String s = (String) oarr[1];
		System.out.println(s + sa);
		oarr[1] = TelephonyManager.getDeviceId();	// different ordering of array writes
		oarr[0] = odata;
		String[] o = (String[]) foo;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(o[1]);
	}

	public void objectTypeBackPropTest() {
		X b = new X();
		X c = b;
		b.o = TelephonyManager.getDeviceId();
		b.o = new String[] { TelephonyManager.getDeviceId() };
		ConnectionManager cm = new ConnectionManager();
		cm.publish(((String[]) c.o)[1]);
	}
	
}
