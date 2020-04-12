package threads.templates;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class CodeCompiler<T> {
    private HashMap<String, Integer> variables;
    private HashMap<String, Integer> calculationResults;


    public CodeCompiler(){
        variables = new HashMap<>();
        calculationResults = new HashMap<>();
    }

    public T compile(Vector<Output> output, Type type ){
        if(type == Type.arithmetic){
            for(Output output1 : output) {
                //if arithmetic is addition
                if(output1.getVariable() != null){
                    Map.Entry<String,Integer> entry = output1.getVariable().entrySet().iterator().next();
                    variables.put(entry.getKey(), entry.getValue());
                }else if(output1.getArithmeticCalculation() != null && output1.getCalculationType() == Output.Type.addition){
                    Map.Entry<String,String> entry = output1.getArithmeticCalculation().entrySet().iterator().next();
                    String value = entry.getValue();
                    int indexAtEquals = value.indexOf("=");
                    int indexAtPlus = value.indexOf("+");
                    String firstAddend = value.substring(indexAtEquals + 1, indexAtPlus).trim();
                    String secondAddend = value.substring(indexAtPlus+1).trim();
                    calculateResult(entry, firstAddend, secondAddend);


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

                }else if(output1.getExit()){
                    break;
                }else if(output1.isError()){
                    System.out.println(output1.getErrorMessage());
                }
            }

        }

        return null;

    }

    private void calculateResult(Map.Entry<String, String> entry, String firstValueForCalculation, String secondValueForCalculation){
        if(checkIfInteger(firstValueForCalculation) && checkIfInteger(secondValueForCalculation)){
            calculationResults.put(entry.getKey(), Integer.parseInt(firstValueForCalculation) + Integer.parseInt(secondValueForCalculation));
            variables.put(entry.getKey(), calculationResults.get(entry.getKey()));
        }else if(checkIfInteger(firstValueForCalculation) && !checkIfInteger(secondValueForCalculation)){
            if(variables.containsKey(secondValueForCalculation)){
                calculationResults.put(entry.getKey(), Integer.parseInt(firstValueForCalculation) + variables.get("var " + secondValueForCalculation));
                variables.put(entry.getKey(), calculationResults.get(entry.getKey()));
            }
        }else if(!checkIfInteger(firstValueForCalculation) && checkIfInteger(secondValueForCalculation)){
            if(variables.containsKey("var " + firstValueForCalculation)) {
                calculationResults.put(entry.getKey(), Integer.parseInt(secondValueForCalculation) + variables.get("var " + firstValueForCalculation));
                variables.put(entry.getKey(), calculationResults.get(entry.getKey()));
            }
        }else if(!checkIfInteger(firstValueForCalculation) && !checkIfInteger(secondValueForCalculation)){
            if(variables.containsKey("var " + firstValueForCalculation) && variables.containsKey("var " + secondValueForCalculation)) {
                calculationResults.put(entry.getKey(), variables.get("var " + firstValueForCalculation) + variables.get("var " + secondValueForCalculation));
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
