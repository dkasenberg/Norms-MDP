package rabinizer.formulas;

public class FOperator extends FormulaUnary {

    @Override
    public String operator() {
        return "F";
    }

    public FOperator(Formula f) {
        super(f);
    }

    public Formula copy() {
        return new FOperator(operand);
    }
    
    public Formula applyParam(String k, String v) {
        return new FOperator(operand.applyParam(k, v));
    }
    
    @Override
    public FOperator ThisTypeUnary(Formula operand) {
        return new FOperator(operand);
    }

    @Override
    public Formula unfold() {
        // U(F phi) = U(phi) \/ X F U(phi)
        return new Disjunction(operand.unfold(), /*new XOperator*/ (this));
    }

    @Override
    public Formula unfoldNoG() {
        // U(F phi) = U(phi) \/ X F U(phi)
        return new Disjunction(operand.unfoldNoG(), /*new XOperator*/ (this));
    }

    @Override
    public Formula toNNF() {
        return new FOperator(operand.toNNF());
    }

    @Override
    public Formula negationToNNF() {
        return new GOperator(operand.negationToNNF());
    }

}
