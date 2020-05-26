package main.java.process_scheduler.threads.templates;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;

public class CodeCompiler {
    private HashMap<String, Integer> variables;
    private HashMap<String, Integer> calculationResults;
    private boolean exitFound;
    private boolean syntaxError;
    private LinkedList<String> codeResults;
    private boolean calculationError;
    private int calculationErrorLine;


    public CodeCompiler(){
        variables = new HashMap<>();
        calculationResults = new HashMap<>();
        exitFound = false;
        syntaxError = false;
        calculationError = false;
        codeResults = new LinkedList<>();
    }

    public void compile(Vector<Output> output, Type type ){
        if(type == Type.arithmetic){
            for(Output lineCode : output) {
                //if arithmetic is addition
                if(calculationError){
                    break;
                }
                if(lineCode.getExit()){
                    this.exitFound = true;
                    break;
                }else if(lineCode.getVariable() != null){
                    Map.Entry<String,Integer> entry = lineCode.getVariable().entrySet().iterator().next();
                    variables.put(entry.getKey(), entry.getValue());
                }else if(lineCode.getArithmeticCalculation() != null && lineCode.getCalculationType() == Output.Type.addition){
                    if(calculateResult(lineCode, lineCode.getLine())){
                        calculationError = true;
                        calculationErrorLine = lineCode.getLine();
                    }

                }else if(lineCode.getIOOutput() != null){
                    if(lineCode.getIOOutput().isError()){
                        codeResults.add("Syntax error at line: " + lineCode.getIOOutput().getLineNumber());
                        syntaxError = true;
                        break;
                    }else if(!lineCode.getIOOutput().isVariable()){
//                        output1.getIOOutput().getOutput().indexOf("'") != -1)
                        if(lineCode.getIOOutput().getOutput().matches(RegexExpressions.STRING_REGEX)){
                            codeResults.add(lineCode.getIOOutput().getOutput().substring(1, lineCode.getIOOutput().getOutput().length() - 1));
                        }else{
                            codeResults.add(lineCode.getIOOutput().getOutput());
                        }

                    }else if(lineCode.getIOOutput().isVariable()){
                        //variables can only be assigned once and cannot be reassigned
                        boolean found = false;
                        for (Map.Entry<String, Integer> entry : calculationResults.entrySet()) {
                            if (entry.getKey().equals("var " + lineCode.getIOOutput().getOutput())) {
                                codeResults.add(String.valueOf(entry.getValue()));
                                found = true;
                            }
                        }
                        if(!found){
                            for(Map.Entry<String, Integer> entry2 : variables.entrySet()){
                                if(entry2.getKey().equals("var " + lineCode.getIOOutput().getOutput())){
                                    codeResults.add(String.valueOf(entry2.getValue()));
                                }
                            }
                        }

                    }

                }if(lineCode.isError()){
                    codeResults.add("Syntax error at line: " + lineCode.getLine());
                    syntaxError = true;
                    break;
                }


            }


            if (calculationError) {
                variables.clear();
                calculationResults.clear();
                codeResults.clear();
                codeResults.add("Calculation error at line: " + calculationErrorLine);
            }

            if(!exitFound && !calculationError && !syntaxError){
                variables.clear();
                calculationResults.clear();
                codeResults.clear();
                codeResults.add("No exit; statement found");
            }

        }

    }

    //calculates result of arithmetic
    private boolean calculateResult(Output output, int line){ ;
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
            if(variables.containsKey("var " + secondAddend)){
                calculationResults.put(entry.getKey(), Integer.parseInt(firstAddend) + variables.get("var " + secondAddend));
                variables.put(entry.getKey(), calculationResults.get(entry.getKey()));
            }else{
                return true;
            }
        }else if(!checkIfInteger(firstAddend) && checkIfInteger(secondAddend)){
            if(variables.containsKey("var " + firstAddend)) {
                calculationResults.put(entry.getKey(), Integer.parseInt(secondAddend) + variables.get("var " + firstAddend));
                variables.put(entry.getKey(), calculationResults.get(entry.getKey()));
            }else{
                return true;
            }
        }else if(!checkIfInteger(firstAddend) && !checkIfInteger(secondAddend)){
            if(variables.containsKey("var " + firstAddend) && variables.containsKey("var " + secondAddend)) {
                calculationResults.put(entry.getKey(), variables.get("var " + firstAddend) + variables.get("var " + secondAddend));
                variables.put(entry.getKey(), calculationResults.get(entry.getKey()));
            }else{
                return true;
            }
        }
        return false;
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

    public enum Type{
        arithmetic,
        io
    }

    public LinkedList<String> getCodeResults() {
        return codeResults;
    }
}
