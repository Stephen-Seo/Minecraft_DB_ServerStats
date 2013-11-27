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
}
