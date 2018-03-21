/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.norminference;

import org.moeaframework.util.grammar.ContextFreeGrammar;
import org.moeaframework.util.grammar.Parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author dkasenberg
 */
public class LTLContextFreeGrammar {

    public static ContextFreeGrammar get(Collection<String> props) throws IOException {
        return get(props, true);
    }

    public static ContextFreeGrammar get(Collection<String> props, boolean includeTF) throws IOException {
        List<String> propList =new ArrayList<>(props);
        if(!includeTF && props.isEmpty()) {
            throw new RuntimeException("Grammar has no propositions and no literals.");
        }
        String bnf = "<ltl> ::= <unary-op> ( <ltl> ) | ( <ltl> ) <binary-op> ( <ltl> ) | <literal>\n"
                + "<unary-op> ::= ! | F | G | X\n"
                + "<binary-op> ::= & | '|' | '->' | U\n"
                + "<literal> ::= ";
        if(includeTF) {
            bnf = bnf + "true | false ";
        }
        for(int i = 0; i< props.size(); i++) {
            if(i != 0 || includeTF) {
                bnf = bnf + " | ";
            }
            bnf = bnf + propList.get(i);
        }

        ContextFreeGrammar grammar = Parser.load(new StringReader(bnf));
        return grammar;
    }
    
    
}
