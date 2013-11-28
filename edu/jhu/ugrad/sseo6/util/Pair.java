package edu.jhu.ugrad.sseo6.util;

public class Pair <A, B> {
	public A a;
	public B b;
	
	public Pair(){}
	
	public Pair(A a, B b){
		this.a = a;
		this.b = b;
	}
	
	public void setA(A a){
		this.a = a;
	}
	
	public void setB(B b){
		this.b = b;
	}
	
	@Override
	public int hashCode() {
		return a.hashCode() + b.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Pair<?,?>))
			return false;
		
		return ((Pair<?,?>)obj).a.equals(a) && ((Pair<?,?>)obj).b.equals(b);
	}
}
