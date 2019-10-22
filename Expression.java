package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {
 
	public static String delims = " \t*+-/()[]";
			
	private static String 
	remove(String S, String C) {
		S = S.replace(C, "");
		return S;
	}
	
	private static String 
	replaceAll(String S) {
		 S = S.replace(" ", "");
		 S = S.replace("+", " ");
		 S = S.replace("-", " ");
		 S = S.replace("*", " ");
		 S = S.replace("/", " ");
		 S = S.replace("[", "[ ");
		 return S;
	 }
	
	private static String 
	replaceAllE(String S) {
		 S = S.replace(" ", "");
		 S = S.replace("+", " + ");
		 S = S.replace("-", " - ");
		 S = S.replace("*", " * ");
		 S = S.replace("/", " / ");
		 S = S.replace("[", "[ ");
		 S = S.replace("]", " ] ");
		 S = S.replace("(", " ( ");
		 S = S.replace(")", " ) ");
		 S = " " + S + " ";
		 return S;
	 }
	
	private static boolean containsv(ArrayList<Variable> v, String c) {
	    	for(int i = 0; i < v.size(); i++) {
	    		if(v.get(i).name.equals(c)) {
	    			return false;
	    		}
	    	}
	    	return true;   	
	 }
	 
	private static boolean containsa(ArrayList<Array> a, String c) {
	    	for(int i = 0; i < a.size(); i++) {
	    		if(a.get(i).name.equals(c)) {
	    			return false;
	    		}
	    	}
	    	return true;   	
	 } 
	
    /**
    * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
   
	public static void 
	makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	expr = remove(expr,"(");
    	expr = remove(expr,")");
    	expr = remove(expr,"]");
    	expr = replaceAll(expr);
    	StringTokenizer ex = new StringTokenizer(expr," ");
    	while(ex.countTokens() != 0) {
    		String curr = ex.nextToken();
    		if(Character.isLetter(curr.charAt(0))) {
    			if(curr.contains("[")) {
    				if(containsa(arrays, curr.substring(0, curr.length()-1))) {
    					arrays.add(new Array(curr.substring(0, curr.length()-1)) );
    				}
    			}
    			else {
    				if(containsv(vars, curr)) {
    					vars.add(new Variable(curr) );
    				}
    			}
    		}
    	}
    	
    }
    
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
 
    private static float Solve(Stack<String> S, Stack<Float> F) {
    	Stack<Float> FRe = new Stack<Float>();
    	Stack<String> SRe = new Stack<String>();
		while(F.size() >1) {
			float second = F.pop();
    		float first = F.pop();
    		String operater = S.pop();
    		if(operater.equals("*")) {
    			F.push(first * second);
    		}
    		if(operater.equals("/")) {
    			F.push(first / second);
    		}
    		if(operater.equals("-") || operater.equals("+")) {
    			F.push(first);
    			SRe.push(operater);
    			FRe.push(second);
    		}
		}
		FRe.push(F.pop());
    	while(FRe.size()>1) {
    		float second = FRe.pop();
    		float first = FRe.pop();
    		String operater = SRe.pop();
    		if(operater.equals("+")) {
    			FRe.push(second + first);
    		}
    		
    		else if(operater.equals("-")) {
    			FRe.push(second - first);
    		}
    	}
    	
    	return FRe.pop();
    }
    
    private static float EvalvRe(StringTokenizer T, ArrayList<Array> A) {
		Stack<Float> D = new Stack<Float>();
		Stack<String> F = new Stack<String>();
    	while(T.hasMoreTokens()) {
    		String S = T.nextToken();
    		if(S.equals(")") || S.equals("]") ) {
    			return Solve(F,D);
    		}
    		if(S.equals("(")) {
    			D.push(EvalvRe(T, A));
    		}
    		if(Character.isLetter(S.charAt(0))) {
    			String Arrayy = S.substring(0, S.length()-1);
    			D.push(getArray(A, Arrayy, ((int)EvalvRe(T, A))));
    		}
    		else if(Character.isDigit(S.charAt(0))) {
   				D.push(Float.parseFloat(S));
   			}
    		else if(S.equals("+") ||S.equals("-") ||S.equals("/") || S.equals("*")) {
   				F.push(S);
   			}
    	}
    	return D.pop();
    }

    private static float getArray(ArrayList<Array> arrays, String name, int index) {
    	for(int i = 0; i < arrays.size(); i++) {
    		if(arrays.get(i).name.equals(name)) {
    			return arrays.get(i).values[index];
    		}
    	}
    	return 0;
    }
    
	public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) { 
    	expr = replaceAllE(expr);
    	for(int i = 0; i < vars.size(); i++) {
    		expr = expr.replace(" " +vars.get(i).name +" ", "" + vars.get(i).value);
    	}
    	expr = expr.replace(" ","");
    	expr = replaceAllE(expr);
    	expr = "( "+ expr +" )";
    	StringTokenizer ex = new StringTokenizer(expr," ");
    	return EvalvRe(ex,arrays);
    }
}
