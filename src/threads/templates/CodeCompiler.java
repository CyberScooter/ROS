package threads.templates;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class CodeCompiler {
    private HashMap<String, Integer> variables;
    private HashMap<String, Integer> calculationResults;


    public CodeCompiler(){
        variables = new HashMap<>();
        calculationResults = new HashMap<>();
    }

    public void compile(Vector<Output> output, Type type ){
        if(type == Type.arithmetic){
            for(Output output1 : output) {
                //if arithmetic is addition
                if(output1.getExit()){
                    break;
                }else if(output1.getVariable() != null){
                    Map.Entry<String,Integer> entry = output1.getVariable().entrySet().iterator().next();
                    variables.put(entry.getKey(), entry.getValue());
                }else if(output1.getArithmeticCalculation() != null && output1.getCalculationType() == Output.Type.addition){
                    calculateResult(output1);

                }else if(output1.getIOOutput() != null){
                    if(!output1.getIOOutput().isVariable()){
                        System.out.println(output1.getIOOutput().getOutput().substring(1, output1.getIOOutput().getOutput().length() - 1));
                    }else if(output1.getIOOutput().isVariable()){
                        for (Map.Entry<String, Integer> entry : calculationResults.entrySet()) {
                            if (entry.getKey().equals("var " + output1.getIOOutput().getOutput())) {
                                System.out.println(entry.getValue());
                            }
                        }
                    }else if(output1.getIOOutput().isError()){
                        System.out.println("Syntax error at line: " + output1.getIOOutput().getLineNumber());
                    }

                }else if(output1.isError()){
                    System.out.println(output1.getErrorMessage());
                }
            }

        }
    }

    private void calculateResult(Output output){ ;
        Map.Entry<String,String> entry = output.getArithmeticCalculation().entrySet().iterator().next();
        String value = entry.getValue();
        int indexAtEquals = value.indexOf("=");
        int indexAtPlus = value.indexOf("+");
        String firstAddend = value.substring(indexAtEquals + 1, indexAtPlus).trim();
        String secondAddend = value.substring(indexAtPlus+1).trim();
        if(checkIfInteger(firstAddend) && checkIfInteger(secondAddend)){
            calculationResults.put(entry.getKey(), Integer.parseInt(firstAddend) + Integer.parseInt(secondAddend));
            variables.put(entry.getKey(), calculationResults.get(entry.getKey()));
        }else if(checkIfInteger(firstAddend) && !checkIfInteger(secondAddend)){
            if(variables.containsKey(secondAddend)){
                calculationResults.put(entry.getKey(), Integer.parseInt(firstAddend) + variables.get("var " + secondAddend));
                variables.put(entry.getKey(), calculationResults.get(entry.getKey()));
            }
        }else if(!checkIfInteger(firstAddend) && checkIfInteger(secondAddend)){
            if(variables.containsKey("var " + firstAddend)) {
                calculationResults.put(entry.getKey(), Integer.parseInt(secondAddend) + variables.get("var " + firstAddend));
                variables.put(entry.getKey(), calculationResults.get(entry.getKey()));
            }
        }else if(!checkIfInteger(firstAddend) && !checkIfInteger(secondAddend)){
            if(variables.containsKey("var " + firstAddend) && variables.containsKey("var " + secondAddend)) {
                calculationResults.put(entry.getKey(), variables.get("var " + firstAddend) + variables.get("var " + secondAddend));
                variables.put(entry.getKey(), calculationResults.get(entry.getKey()));
            }
        }
    }

    public boolean checkIfInteger(String value){
        try{
            Integer.parseInt(value);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    public static boolean checkCalculationSyntax(String calculation){
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
        Compilable compiler = (Compilable) scriptEngine;
        try {
            compiler.compile(calculation);
            return true;
        } catch (ScriptException e) {
            return false;
        }
    }

        public static enum Type{
        arithmetic,
        io
    }

}
